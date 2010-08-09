//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.controller;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPVersion;
import org.apache.commons.fileupload.FileItem;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.ows.capabilities.GetCapabilities;
import org.deegree.services.authentication.SecurityException;
import org.deegree.services.controller.exception.ControllerException;
import org.deegree.services.controller.exception.ControllerInitException;
import org.deegree.services.controller.exception.SOAPException;
import org.deegree.services.controller.exception.serializer.ExceptionSerializer;
import org.deegree.services.controller.exception.serializer.SOAPExceptionSerializer;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.ows.OWSException110XMLAdapter;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.controller.utils.LoggingHttpResponseWrapper;
import org.deegree.services.i18n.Messages;
import org.deegree.services.jaxb.main.AddressType;
import org.deegree.services.jaxb.main.DCPType;
import org.deegree.services.jaxb.main.DeegreeServiceControllerType;
import org.deegree.services.jaxb.main.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.main.ServiceContactType;
import org.deegree.services.jaxb.main.ServiceIdentificationType;
import org.deegree.services.jaxb.main.ServiceProviderType;
import org.deegree.services.wpvs.controller.WPVSController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Each concrete extension of this class is responsible for handling requests to a specific OGC web service (WPS, WMS,
 * WFS, CSW,...).
 * 
 * @see OGCFrontController
 * @see ImplementationMetadata
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public abstract class AbstractOGCServiceController {

    private ImplementationMetadata<? extends Enum<?>> implementationMetadata;

    private static final Logger LOG = LoggerFactory.getLogger( AbstractOGCServiceController.class );

    /** Common configuration (metadata) of parent {@link OGCFrontController}. */
    protected DeegreeServicesMetadataType mainMetadataConf;

    protected DeegreeServiceControllerType mainControllerConf;

    private final static SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );

    /**
     * Versions offered by the {@link AbstractOGCServiceController} instance (depends on configuration).
     * <p>
     * Versions are sorted from lowest to highest in order to support the (old-style) version negotiation algorithm.
     * </p>
     */
    protected SortedSet<Version> offeredVersions = new TreeSet<Version>();

    /**
     * Initializes the {@link AbstractOGCServiceController} instance.
     * 
     * @param mainMetadataConf
     * @param serviceInformation
     * @param controllerConfig
     *            controller configuration, must not be null
     * @throws ControllerInitException
     *             if the config version does not match one of the supported versions
     */
    protected void init( DeegreeServicesMetadataType mainMetadataConf, DeegreeServiceControllerType mainControllerConf,
                         ImplementationMetadata<? extends Enum<?>> serviceInformation, XMLAdapter controllerConfig )
                            throws ControllerInitException {
        this.mainMetadataConf = mainMetadataConf;
        this.mainControllerConf = mainControllerConf;
        this.implementationMetadata = serviceInformation;
        String configVersion = controllerConfig.getRootElement().getAttributeValue( new QName( "configVersion" ) );
        checkConfigVersion( controllerConfig.getSystemId(), configVersion );
    }

    /**
     * Returns the names of all requests that are handled by this controller.
     * 
     * @return names of handled requests
     */
    public final Set<String> getHandledRequests() {
        return implementationMetadata.getHandledRequests();
    }

    /**
     * Returns all namespaces that are handled by this controller.
     * 
     * @return handled namespaces
     */
    public final Set<String> getHandledNamespaces() {
        return implementationMetadata.getHandledNamespaces();
    }

    /**
     * Called by the {@link OGCFrontController} to indicate to a subcontroller that it is being taken into service.
     * 
     * @param controllerConf
     *            provides access to the (always xml-based) configuration of the controller
     * @param serviceMetadata
     *            services metadata from the main service configuration for all services
     * @param serviceController
     *            from the main.xml
     * @throws ControllerInitException
     *             indicates that the initialization failed
     */
    public abstract void init( XMLAdapter controllerConf, DeegreeServicesMetadataType serviceMetadata,
                               DeegreeServiceControllerType serviceController )
                            throws ControllerInitException;

    /**
     * Called by the {@link OGCFrontController} to indicate to a subcontroller that the subcontroller is being taken out
     * of service.
     */
    public abstract void destroy();

    /**
     * @param requestedVersions
     * @throws ControllerInitException
     */
    protected final void validateAndSetOfferedVersions( Collection<String> requestedVersions )
                            throws ControllerInitException {
        for ( String requestedVersion : requestedVersions ) {
            Version version = Version.parseVersion( requestedVersion );
            if ( !implementationMetadata.getImplementedVersions().contains( Version.parseVersion( requestedVersion ) ) ) {
                String msg = "Version '" + requestedVersion + "' is not supported by the service implementation.";
                throw new ControllerInitException( msg );
            }
            offeredVersions.add( version );
        }
    }

    /**
     * Called by the {@link OGCFrontController} to allow this <code>AbstractOGCServiceController</code> to handle a KVP
     * request.
     * 
     * @param normalizedKVPParams
     *            request parameters (keys are uppercased)
     * @param request
     *            provides access to all information of the original HTTP request (NOTE: may be GET or POST)
     * @param response
     *            response that is sent to the client
     * @param multiParts
     *            A list of multiparts contained in the request. If the request was not a multipart request the list
     *            will be <code>null</code>. If multiparts were found, the requestDoc will be the first (xml-lized)
     *            {@link FileItem} in the list.
     * @throws ServletException
     * @throws IOException
     * @throws SecurityException
     */
    protected abstract void doKVP( Map<String, String> normalizedKVPParams, HttpServletRequest request,
                                   HttpResponseBuffer response, List<FileItem> multiParts )
                            throws ServletException, IOException, SecurityException;

    /**
     * Called by the {@link OGCFrontController} to allow this <code>AbstractOGCServiceController</code> to handle an XML
     * request.
     * 
     * @param xmlStream
     *            provides access to the XML request, cursor points to the START_ELEMENT event of the root element,
     *            never <code>null</code>
     * @param request
     *            provides access to all information of the original HTTP request (NOTE: may be GET or POST), never
     *            <code>null</code>
     * @param response
     *            response that is sent to the client, never <code>null</code>
     * @param multiParts
     *            A list of multiparts contained in the request. If the request was not a multipart request the list
     *            will be <code>null</code>. If multiparts were found, the xmlStream will provide access to the first
     *            (xml-lized) {@link FileItem} in the list of multi parts
     * @throws ServletException
     * @throws IOException
     * @throws SecurityException
     */
    protected abstract void doXML( XMLStreamReader xmlStream, HttpServletRequest request, HttpResponseBuffer response,
                                   List<FileItem> multiParts )
                            throws ServletException, IOException, SecurityException;

    /**
     * Called by the {@link OGCFrontController} to allow this <code>AbstractOGCServiceController</code> to handle a SOAP
     * request.
     * 
     * @param soapDoc
     *            <code>XMLAdapter</code> for parsing the SOAP request document
     * @param request
     *            provides access to all information of the original HTTP request (NOTE: may be GET or POST)
     * @param response
     *            response that is sent to the client
     * @param multiParts
     *            A list of multiparts contained in the request. If the request was not a multipart request the list
     *            will be <code>null</code>. If multiparts were found, the requestDoc will be the first (xml-lized)
     *            {@link FileItem} in the list.
     * @param factory
     *            initialized to the soap version of the request.
     * @throws ServletException
     * @throws IOException
     *             if an IOException occurred
     * @throws SecurityException
     */
    protected void doSOAP( SOAPEnvelope soapDoc, HttpServletRequest request, HttpResponseBuffer response,
                           List<FileItem> multiParts, SOAPFactory factory )
                            throws ServletException, IOException, SecurityException {
        sendSOAPException( soapDoc.getHeader(), factory, response, null, null, null,
                           "SOAP DCP is not implemented for this service.", request.getServerName(),
                           request.getCharacterEncoding() );
    }

    /**
     * Convenience method that may be used by controller implementations to produce OGC-SOAP responses.
     * <p>
     * Performs the following actions using the given {@link HttpResponseBuffer}:
     * <ul>
     * <li>Sets the content type to <code>application/soap+xml</code></li>
     * <li>Opens <code>soapenv:Envelope</code> and <code>soapenv:Body</code> elements</li>
     * </ul>
     * </p>
     * <p>
     * After calling this method, the controller may simply write the normal OGC-XML response using the
     * {@link HttpResponseBuffer#getXMLWriter()} object and call {@link #endSOAPResponse(HttpResponseBuffer)}
     * afterwards.
     * </p>
     * 
     * @see #endSOAPResponse(HttpResponseBuffer)
     * 
     * @param response
     * @throws XMLStreamException
     * @throws IOException
     */
    protected void beginSOAPResponse( HttpResponseBuffer response )
                            throws XMLStreamException, IOException {
        response.setContentType( "application/soap+xml" );
        XMLStreamWriter xmlWriter = response.getXMLWriter();
        String soapEnvNS = "http://www.w3.org/2003/05/soap-envelope";
        String xsiNS = "http://www.w3.org/2001/XMLSchema-instance";
        xmlWriter.setPrefix( "soapenv", soapEnvNS );
        xmlWriter.setPrefix( "xsi", xsiNS );

        xmlWriter.writeStartElement( soapEnvNS, "Envelope" );
        xmlWriter.writeAttribute( xsiNS, "schemaLocation",
                                  "http://www.w3.org/2003/05/soap-envelope http://www.w3.org/2003/05/soap-envelope" );

        xmlWriter.writeStartElement( soapEnvNS, "Body" );
    }

    /**
     * Finishes an OGC-SOAP response that has been initiated by {@link #beginSOAPResponse(HttpResponseBuffer)}.
     * 
     * @see #beginSOAPResponse(HttpResponseBuffer)
     * 
     * @param response
     * @throws IOException
     * @throws XMLStreamException
     */
    protected void endSOAPResponse( HttpResponseBuffer response )
                            throws IOException, XMLStreamException {
        XMLStreamWriter xmlWriter = response.getXMLWriter();
        // "soapenv:Body"
        xmlWriter.writeEndElement();
        // "soapenv:Envelope"
        xmlWriter.writeEndElement();
    }

    /**
     * Checks if a request version can be handled by this controller (i.e. if is supported by the implementation *and*
     * offered by the current configuration).
     * <p>
     * NOTE: This method does use exception code {@link OWSException#INVALID_PARAMETER_VALUE}, not
     * {@link OWSException#VERSION_NEGOTIATION_FAILED} -- the latter should only be used for failed GetCapabilities
     * requests.
     * </p>
     * 
     * @param requestedVersion
     *            version to be checked, may be null (causes exception)
     * @return <code>requestedVersion</code> (if it is not null), or highest version supported
     * @throws OWSException
     *             if the requested version is not available
     */
    protected Version checkVersion( Version requestedVersion )
                            throws OWSException {
        Version version = requestedVersion;
        if ( requestedVersion == null ) {
            LOG.debug( "Assuming version: " + offeredVersions.last() );
            version = offeredVersions.last();
        } else if ( !offeredVersions.contains( requestedVersion ) ) {
            throw new OWSException( Messages.get( "CONTROLLER_UNSUPPORTED_VERSION", requestedVersion,
                                                  getOfferedVersionsString() ), OWSException.INVALID_PARAMETER_VALUE );
        }
        return version;
    }

    /**
     * Returns the offered protocol versions.
     * 
     * @return the offered protocol versions
     */
    public String getOfferedVersionsString() {
        int i = 0;
        String s = "";
        for ( Version version : offeredVersions ) {
            s += "'" + version + "'";
            if ( i++ != offeredVersions.size() - 1 ) {
                s += ", ";
            }
        }
        return s;
    }

    /**
     * @param confFileURL
     * @param configVersionString
     * @throws ControllerInitException
     */
    protected void checkConfigVersion( String confFileURL, String configVersionString )
                            throws ControllerInitException {
        Version configVersion = Version.parseVersion( configVersionString );
        if ( !implementationMetadata.getSupportedConfigVersions().contains( configVersion ) ) {
            String msg = "Configuration file '" + confFileURL + " uses configuration format version " + configVersion
                         + ", but this deegree version only supports version(s): ";
            boolean separatorNeeded = false;
            for ( Version supportedVersion : implementationMetadata.getSupportedConfigVersions() ) {
                msg += supportedVersion;
                if ( separatorNeeded ) {
                    msg += ",";
                }
                separatorNeeded = true;
            }
            msg += " for this file type. Information on resolving this issue can be found at 'http://wiki.deegree.org/deegreeWiki/deegree3/ConfigurationVersions'. ";
            throw new ControllerInitException( msg );
        }
    }

    /**
     * Generic version negotiation algorithm for {@link GetCapabilities} requests according to OWS Common Specification
     * 1.1.0 (OGC 06-121r3), section 7.3.2 and D.11.
     * 
     * @param request
     *            <code>GetCapabilities</code> request
     * @return agreed version (used for response)
     * @throws OWSException
     *             if new-style version negotiation is used and no common version exists
     */
    protected Version negotiateVersion( GetCapabilities request )
                            throws OWSException {

        Version agreedVersion = null;

        if ( request.getVersion() != null ) {
            Version requestedVersion = null;
            try {
                requestedVersion = request.getVersionAsVersion();
            } catch ( InvalidParameterValueException e ) {
                throw new OWSException( "Version negotiation failed. Requested version: '" + requestedVersion
                                        + "' is not a valid version identifier.",
                                        OWSException.VERSION_NEGOTIATION_FAILED );
            }
            LOG.debug( "Performing old-style version negotiation" );
            if ( offeredVersions.contains( requestedVersion ) ) {
                agreedVersion = requestedVersion;
            } else {
                Version lowestOfferedVersion = offeredVersions.first();
                if ( requestedVersion.compareTo( lowestOfferedVersion ) < 0 ) {
                    // requested version is lower than lowest offered versions
                    agreedVersion = lowestOfferedVersion;
                } else {
                    // requested version is higher than lower offered versions
                    agreedVersion = lowestOfferedVersion;
                    for ( Version offeredVersion : offeredVersions ) {
                        // if the first version is found that is higher than the requested,
                        // break loop -> agreed version is the highest offered version that is lower
                        // than the requested one
                        if ( offeredVersion.compareTo( requestedVersion ) > 0 ) {
                            break;
                        }
                        agreedVersion = offeredVersion;
                    }
                }
            }
        } else if ( request.getAcceptVersions().size() > 0 ) {
            LOG.debug( "Performing new-style version negotiation" );
            for ( String acceptableVersionString : request.getAcceptVersions() ) {
                Version acceptableVersion = null;
                try {
                    acceptableVersion = Version.parseVersion( acceptableVersionString );
                } catch ( InvalidParameterValueException e ) {
                    throw new OWSException( "Version negotiation failed. Specified accept version: '"
                                            + acceptableVersionString + "' is not a valid version identifier.",
                                            OWSException.VERSION_NEGOTIATION_FAILED );
                }
                if ( offeredVersions.contains( acceptableVersion ) ) {
                    agreedVersion = acceptableVersion;
                    break;
                }
            }
            if ( agreedVersion == null ) {
                String versionsString = Version.getVersionsString( request.getAcceptVersionsAsVersions().toArray(
                                                                                                                  new Version[request.getAcceptVersions().size()] ) );
                throw new OWSException( "Version negotiation failed. No support for version(s): " + versionsString,
                                        OWSException.VERSION_NEGOTIATION_FAILED );
            }
        } else {
            LOG.debug( "No client version preference (may be old-style or new-style request)" );
            agreedVersion = offeredVersions.last();
        }

        LOG.debug( "- Agreed on version: " + agreedVersion );
        return agreedVersion;
    }

    /**
     * Sends an exception to the client.
     * 
     * @param <T>
     *            the type of the Exception, which should be subtype of controller exception
     * 
     * @param contentType
     *            of the exception response
     * @param encoding
     *            of the exception response
     * @param additionalHeaders
     *            to add to the response.
     * @param httpStatusCode
     *            of the exception response
     * @param serializer
     *            responsible for creating the appropriate response format of the exception.
     * @param exception
     *            the cause, holding relevant information.
     * @param response
     *            to write to.
     * @throws ServletException
     *             if the exception could not be sent.
     */
    public static <T extends ControllerException> void sendException( String contentType, String encoding,
                                                                      Map<String, String> additionalHeaders,
                                                                      int httpStatusCode,
                                                                      ExceptionSerializer<T> serializer, T exception,
                                                                      HttpServletResponse response )
                            throws ServletException {
        // take care of proper request logging
        LoggingHttpResponseWrapper wrapper = null;
        if ( response instanceof LoggingHttpResponseWrapper ) {
            wrapper = (LoggingHttpResponseWrapper) response;
        }
        if ( response instanceof HttpResponseBuffer ) {
            HttpServletResponse wrappee = ( (HttpResponseBuffer) response ).getWrappee();
            if ( wrappee instanceof LoggingHttpResponseWrapper ) {
                wrapper = (LoggingHttpResponseWrapper) wrappee;
            }
        }

        if ( !response.isCommitted() ) {
            try {
                response.reset();
            } catch ( IllegalStateException e ) {
                // rb: the illegal state exception occurred.
                throw new ServletException( e );
            }

            response.setContentType( contentType );
            response.setCharacterEncoding( encoding );
            if ( additionalHeaders != null && additionalHeaders.size() > 0 ) {
                for ( String key : additionalHeaders.keySet() ) {
                    String value = additionalHeaders.get( key );
                    if ( key != null && "".equals( key ) && value != null ) {
                        response.addHeader( key, value );
                    }
                }
            }
            // response.setStatus( httpStatusCode );

            try {
                serializer.serializeException( response.getOutputStream(), exception, encoding );
            } catch ( IOException e ) {
                LOG.error( "An error occurred while trying to send an exception: " + e.getLocalizedMessage(), e );
                throw new ServletException( e );
            }
            if ( wrapper != null ) {
                wrapper.setExceptionSent();
                wrapper.finalizeLogging();
            }
        }
    }

    /**
     * Encapsulates the given {@link OWSException} into a SOAP environment for which the given factory will be used.
     * 
     * @param header
     *            SOAPheaders to be set in the envelope, if missing no headers will be set.
     * @param factory
     *            to create the soap elements.
     * @param response
     *            to write to.
     * @param exception
     *            to write in the 'fault/detail' section
     * @param serializer
     *            to use for writing the {@link OWSException}.
     * @param SOAPFaultCode
     *            optional (see {@link SOAPException} for valid once. If missing {@link SOAPException#SENDER} will be
     *            used.
     * @param SOAPMessage
     *            optional message to explicitly set. If missing the owsException message will be used.
     * @param SOAPaction
     *            to set, optional.
     * @param characterEncoding
     *            of the response.
     * @throws ServletException
     */
    public void sendSOAPException( SOAPHeader header, SOAPFactory factory, HttpResponseBuffer response,
                                   OWSException exception, XMLExceptionSerializer<OWSException> serializer,
                                   String SOAPFaultCode, String SOAPMessage, String SOAPaction, String characterEncoding )
                            throws ServletException {

        String faultCode = SOAPFaultCode;
        if ( faultCode == null || "".equals( faultCode ) ) {
            faultCode = SOAPException.SENDER;
        }
        String message = SOAPMessage;
        if ( message == null || "".equals( message ) ) {
            if ( exception == null ) {
                message = "unknwown";
            } else {
                message = exception.getMessage();
            }
        }
        Map<String, String> extraHeaders = new HashMap<String, String>();
        SOAPVersion version = factory.getSOAPVersion();
        String action = "";
        if ( SOAPaction != null && "".equals( SOAPaction ) ) {
            action = SOAPaction;
        }

        int statusCode = 200;
        String contentType = "application:soap+xml; action=" + action;
        if ( "http://schemas.xmlsoap.org/soap/envelope/".equals( factory.getSoapVersionURI() ) ) {
            extraHeaders.put( "SOAPAction", action );
            statusCode = 500;

        }
        sendException( contentType, characterEncoding, extraHeaders, statusCode,
                       new SOAPExceptionSerializer( version, header, factory, serializer ),
                       new SOAPException( message, faultCode, exception ), response );
    }

    /**
     * @param configuredServiceProvider
     *            to be synchronized with the main configuration
     * @return the configured service provider, with missing values filled from the main configuration.
     */
    protected ServiceProviderType synchronizeServiceProviderWithMainControllerConf(
                                                                                    ServiceProviderType configuredServiceProvider ) {
        ServiceProviderType mainProvider = mainMetadataConf.getServiceProvider();
        ServiceProviderType result = configuredServiceProvider;
        if ( configuredServiceProvider == null ) {
            result = new ServiceProviderType();
        }
        if ( mainProvider != null ) {
            result.setProviderName( syncStrings( result.getProviderName(), mainProvider.getProviderName() ) );
            result.setProviderSite( syncStrings( result.getProviderSite(), mainProvider.getProviderSite() ) );
            result.setServiceContact( syncContactTypes( result.getServiceContact(), mainProvider.getServiceContact() ) );
        } else {
            LOG.info( "Unable to synchronize the given service provider information with the global configuration (read from services_metadata.xml) because your global configuration file did not provide a ServiceProvider section. You can supply service provider information valid for all services (in this context) by adding a ServiceProvider section in the services_metadata.xml." );
        }
        return result;
    }

    /**
     * @param serviceIdentification
     *            to be synchronized with the configuration of the main controller.
     * @return the service identification with all missing values filled in from the main controller service
     *         identification.
     */
    protected ServiceIdentificationType synchronizeServiceIdentificationWithMainController(
                                                                                            ServiceIdentificationType serviceIdentification ) {
        ServiceIdentificationType mainID = mainMetadataConf.getServiceIdentification();
        ServiceIdentificationType result = serviceIdentification;
        if ( mainID != null ) {
            if ( serviceIdentification == null ) {
                result = new ServiceIdentificationType();
            }
            result.setFees( syncStrings( result.getFees(), mainID.getFees() ) );
            if ( result.getAbstract().isEmpty() ) {
                result.getAbstract().addAll( mainID.getAbstract() );
            }
            if ( result.getAccessConstraints().isEmpty() ) {
                result.getAccessConstraints().addAll( mainID.getAccessConstraints() );
            }
            if ( result.getKeywords().isEmpty() ) {
                result.getKeywords().addAll( mainID.getKeywords() );
            }
            if ( result.getTitle().isEmpty() ) {
                result.getTitle().addAll( mainID.getTitle() );
            }
        } else {
            LOG.info( "Unable to synchronize the given service identification information with the global configuration (read from services_metadata.xml) because your global configuration file did not provide a ServiceIdentification section. You can supply service identification information valid for all services (in this context) by adding a ServiceIdentification section in the services_metadata.xml." );
        }
        return result;

    }

    /**
     * Synchronize the service contact information
     * 
     * @param localContact
     * @param mainContact
     * @return the merged service contact information
     */
    private ServiceContactType syncContactTypes( ServiceContactType localContact, ServiceContactType mainContact ) {
        ServiceContactType result = localContact;
        if ( mainContact != null ) {
            if ( localContact == null ) {
                result = new ServiceContactType();
            }

            // sync the addresses
            result.setAddress( syncAddressTypes( result.getAddress(), mainContact.getAddress() ) );
            result.setContactInstructions( syncStrings( result.getContactInstructions(),
                                                        mainContact.getContactInstructions() ) );
            result.setFacsimile( syncStrings( result.getFacsimile(), mainContact.getFacsimile() ) );
            result.setHoursOfService( syncStrings( result.getHoursOfService(), mainContact.getHoursOfService() ) );
            result.setOnlineResource( syncStrings( result.getOnlineResource(), mainContact.getOnlineResource() ) );
            result.setPhone( syncStrings( result.getPhone(), mainContact.getPhone() ) );

            // result.setIndividualName( syncStrings( result.getIndividualName(), mainContact.getIndividualName() ) );
            // result.setPositionName( syncStrings( result.getPositionName(), mainContact.getPositionName() ) );
            // result.setRole( syncStrings( result.getRole(), mainContact.getRole() ) );
            // if ( result.getElectronicMailAddress().isEmpty() ) {
            // result.getElectronicMailAddress().addAll( mainContact.getElectronicMailAddress() );
            // }

        }
        return result;
    }

    /**
     * Synchronize the address information.
     * 
     * @param localAddress
     * @param mainAddress
     * @return an address type with missing values filled in from the main address.
     */
    private AddressType syncAddressTypes( AddressType localAddress, AddressType mainAddress ) {
        AddressType result = localAddress;
        if ( mainAddress != null ) {
            if ( localAddress == null ) {
                result = new AddressType();
            }
            result.setAdministrativeArea( syncStrings( result.getAdministrativeArea(),
                                                       mainAddress.getAdministrativeArea() ) );
            result.setCity( syncStrings( result.getCity(), mainAddress.getCity() ) );
            result.setCountry( syncStrings( result.getCountry(), mainAddress.getCountry() ) );
            result.setPostalCode( syncStrings( result.getPostalCode(), mainAddress.getPostalCode() ) );
            if ( result.getDeliveryPoint().isEmpty() ) {
                result.getDeliveryPoint().addAll( mainAddress.getDeliveryPoint() );
            }
        }
        return result;
    }

    /**
     * Simple method checking for a null or empty string.
     * 
     * @param localValue
     * @param controllerValue
     * @return the localvalue or the controller value if it was empty or null.
     */
    private String syncStrings( String localValue, String controllerValue ) {
        boolean useController = ( localValue == null || "".equals( localValue.trim() ) );
        if ( useController ) {
            LOG.info( "Using main controller's value:" + controllerValue );
        }
        return useController ? controllerValue : localValue;
    }

    /**
     * Tries to load a schema file from the given location, which might be useful for the validation of configuration
     * files with JAXB.
     * 
     * @param schemaFile
     *            location like: "/META-INF/schemas/[SERVICE_NAME]/[VERSION]/[SERVICE_NAME]_service_configuration.xsd"
     * @return the schema for the given url or <code>null</code> if no schema could be loaded from the given url.
     */
    public Schema getSchemaForUrl( String schemaFile ) {
        URL url = WPVSController.class.getResource( schemaFile );
        Schema result = null;
        if ( url != null ) {
            try {
                result = sf.newSchema( url );
            } catch ( SAXException e ) {
                LOG.error( "No schema could be loaded from file: " + schemaFile + " because: "
                           + e.getLocalizedMessage(), e );
            }
        }
        return result;
    }

    /**
     * Create a JAXB {@link Unmarshaller} which is instantiated with the given classpath (as well as the common
     * configuration classpath). If the given schemalocation is not <code>null</code>, the unmarshaller will validate
     * against the schema file loaded from the given location.
     * 
     * @param jaxbPackage
     *            used for instantiating the unmarshaller
     * @param schemaLocation
     *            if not <code>null</code> this method will try to load the schema from location and set the validation
     *            in the unmarshaller. This location could be:
     *            "/META-INF/schemas/[SERVICE_NAME]/[VERSION]/[SERVICE_NAME]_service_configuration.xsd"
     * @return an unmarshaller which can be used to unmarshall a document with jaxb
     * @throws JAXBException
     *             if the {@link Unmarshaller} could not be created.
     */
    public Unmarshaller getUnmarshaller( String jaxbPackage, String schemaLocation )
                            throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance( jaxbPackage );
        Unmarshaller u = jc.createUnmarshaller();

        if ( schemaLocation != null ) {
            Schema configSchema = getSchemaForUrl( schemaLocation );
            if ( configSchema != null ) {
                u.setSchema( configSchema );
            } else {
                LOG.info( "Not using jaxb schema validation, because the schema could not be loaded." );
            }
        }
        return u;
    }

    /**
     * Check if the main controller configuration defines a dcp 'POST' url if not, set it with the values gotten from
     * the request.
     * 
     * @param requestWrapper
     */
    public void checkOrCreateDCPPostURL( HttpServletRequest requestWrapper ) {
        DCPType dcps = getControllerDCP();
        if ( dcps.getHTTPPost() == null || "".equals( dcps.getHTTPPost() ) ) {
            String reqUrl = getRequestURL( requestWrapper );
            dcps.setHTTPPost( reqUrl );
        }
    }

    /**
     * Check if the main controller configuration defines a dcp 'GET' url if not, set it with the values gotten from the
     * request.
     * 
     * @param requestWrapper
     *            to get the requested url from.
     */
    public void checkOrCreateDCPGetURL( HttpServletRequest requestWrapper ) {
        DCPType dcps = getControllerDCP();
        if ( dcps.getHTTPGet() != null && !"".equals( dcps.getHTTPGet() ) ) {
            String getUrl = dcps.getHTTPGet();
            if ( !getUrl.endsWith( "?" ) ) {
                getUrl += "?";
                LOG.info( "Setting dcp getURL with: " + getUrl );
                dcps.setHTTPGet( getUrl );
            }
        } else {
            String reqUrl = getRequestURL( requestWrapper );
            reqUrl = appendMissingQuestionMark( reqUrl );
            dcps.setHTTPGet( reqUrl );
        }
    }

    /**
     * Checks if the request url ends with a ? and if failing appends
     * 
     * @param request
     * @return the request url with a <code>?</code> at the end
     */
    private String appendMissingQuestionMark( String request ) {
        if ( !request.endsWith( "?" ) ) {
            return request + '?';
        }
        return request;
    }

    /**
     * @return the maincontrollers dcptype or if not defined set a new one.
     */
    private DCPType getControllerDCP() {
        DCPType dcps = mainControllerConf.getDCP();
        if ( dcps == null ) {
            mainControllerConf.setDCP( new DCPType() );
            dcps = mainControllerConf.getDCP();
        }
        return dcps;
    }

    /**
     * @param request
     * @return url to which the request was sent against.
     */
    private String getRequestURL( HttpServletRequest request ) {
        return request.getRequestURL().toString();
    }

    /**
     * Returns the {@link ExceptionSerializer} and mime-type suitable for the given request version.
     * 
     * @param requestVersion
     *            version of the request for which the exception has to be produced, may be <code>null</code> (implies
     *            that the serializer and mime type for the highest supported version shall be returned)
     * @return an OWSCommon 1.1.0 XML adapter by default, never <code>null</code>
     */
    public Pair<XMLExceptionSerializer<OWSException>, String> getExceptionSerializer( Version requestVersion ) {
        return new Pair<XMLExceptionSerializer<OWSException>, String>( new OWSException110XMLAdapter(), "text/xml" );
    }
}
