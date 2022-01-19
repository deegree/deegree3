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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPVersion;
import org.apache.commons.fileupload.FileItem;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.protocol.ows.getcapabilities.GetCapabilities;
import org.deegree.services.OWS;
import org.deegree.services.OWSProvider;
import org.deegree.services.authentication.SecurityException;
import org.deegree.services.controller.exception.SOAPException;
import org.deegree.services.controller.exception.serializer.ExceptionSerializer;
import org.deegree.services.controller.exception.serializer.SOAPExceptionSerializer;
import org.deegree.services.controller.exception.serializer.SerializerProvider;
import org.deegree.services.controller.exception.serializer.SerializerProviderInitializer;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.i18n.Messages;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.deegree.services.ows.OWS110ExceptionReportSerializer;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public abstract class AbstractOWS implements OWS {

    private static final Logger LOG = LoggerFactory.getLogger( AbstractOWS.class );

    protected ResourceMetadata<OWS> metadata;

    /**
     * Versions offered by the {@link AbstractOWS} instance (depends on configuration).
     * <p>
     * Versions are sorted from lowest to highest in order to support the (old-style) version negotiation algorithm.
     * </p>
     */
    protected SortedSet<Version> offeredVersions = new TreeSet<Version>();

    protected Workspace workspace;

    private Object jaxbConfig;

    protected AbstractOWS( ResourceMetadata<OWS> metadata, Workspace workspace, Object jaxbConfig ) {
        this.metadata = metadata;
        this.workspace = workspace;
        this.jaxbConfig = jaxbConfig;
    }

    @Override
    public void init() {
        OwsGlobalConfigLoader loader = workspace.getInitializable( OwsGlobalConfigLoader.class );
        init( loader.getMetadataConfig(), loader.getMainConfig(), jaxbConfig );
    }

    /**
     * Initializes the {@link AbstractOWS} instance.
     * 
     * @param mainMetadataConf
     * @param serviceInformation
     * @param controllerConfig
     *            controller configuration, must not be null
     */
    protected abstract void init( DeegreeServicesMetadataType mainMetadataConf,
                                  DeegreeServiceControllerType mainControllerConf, Object controllerConfig );

    /**
     * @param requestedVersions
     * @throws ResourceInitException
     */
    protected final void validateAndSetOfferedVersions( Collection<String> requestedVersions )
                            throws ResourceInitException {
        for ( String requestedVersion : requestedVersions ) {
            Version version = Version.parseVersion( requestedVersion );
            if ( !( ( (OWSProvider) metadata.getProvider() ).getImplementationMetadata().getImplementedVersions().contains( Version.parseVersion( requestedVersion ) ) ) ) {
                String msg = "Version '" + requestedVersion + "' is not supported by the service implementation.";
                throw new ResourceInitException( msg );
            }
            offeredVersions.add( version );
        }
    }

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
    @Override
    public void doSOAP( SOAPEnvelope soapDoc, HttpServletRequest request, HttpResponseBuffer response,
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
        xmlWriter.writeStartElement( "soapenv", "Envelope", soapEnvNS );
        xmlWriter.writeNamespace( "soapenv", soapEnvNS );
        xmlWriter.writeNamespace( "xsi", xsiNS );
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
        StringBuilder s = new StringBuilder();
        for ( Version version : offeredVersions ) {
            s.append( "'" ).append( version ).append( "'" );
            if ( i++ != offeredVersions.size() - 1 ) {
                s.append( ", " );
            }
        }
        return s.toString();
    }

    /**
     * Returns the offered protocol versions.
     * 
     * @return the offered protocol versions
     */
    public List<String> getOfferedVersions() {
        List<String> versions = new ArrayList<String>( offeredVersions.size() );
        for ( Version version : offeredVersions ) {
            versions.add( version.toString() );
        }
        return versions;
    }

    /**
     * @param confFileURL
     * @param configVersionString
     * @throws ResourceInitException
     */
    protected void checkConfigVersion( String confFileURL, String configVersionString )
                            throws ResourceInitException {

        Version configVersion = Version.parseVersion( configVersionString );
        if ( !( (OWSProvider) metadata.getProvider() ).getImplementationMetadata().getSupportedConfigVersions().contains( configVersion ) ) {
            LOG.error( "" );
            LOG.error( "*** Configuration version mismatch ***", confFileURL );
            LOG.error( "" );
            StringBuilder msg = new StringBuilder( "File uses config version " ).append( configVersion );
            msg.append( ", but this deegree build only supports version(s): " );
            boolean separatorNeeded = false;
            for ( Version supportedVersion : ( (OWSProvider) metadata.getProvider() ).getImplementationMetadata().getSupportedConfigVersions() ) {
                msg.append( supportedVersion );
                if ( separatorNeeded ) {
                    msg.append( "," );
                }
                separatorNeeded = true;
            }
            msg.append( " for this file type. Information on resolving this issue can be found at 'http://wiki.deegree.org/deegreeWiki/deegree3/ConfigurationVersions'. " );
            throw new ResourceInitException( msg.toString() );
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

        if ( request.getAcceptVersions().size() > 0 ) {
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
                String versionsString = Version.getVersionsString( request.getAcceptVersionsAsVersions().toArray( new Version[request.getAcceptVersions().size()] ) );
                throw new OWSException( "Version negotiation failed. No support for version(s): " + versionsString,
                                        OWSException.VERSION_NEGOTIATION_FAILED );
            }
        } else if ( request.getVersion() != null ) {
            Version requestedVersion = null;
            try {
                requestedVersion = request.getVersionAsVersion();
            } catch ( InvalidParameterValueException e ) {
                throw new OWSException( "Version negotiation failed. Requested version: '" + requestedVersion
                                        + "' is not a valid OGC version identifier.",
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
     * @param additionalHeaders
     *            to add to the response.
     * @param serializer
     *            responsible for creating the appropriate response format of the exception. Could be overridden by a
     *            matching {@link SerializerProvider} on the classpath.
     * @param exception
     *            the cause, holding relevant information.
     * @param response
     *            to write to.
     * @throws ServletException
     *             if the exception could not be sent.
     */
    public void sendException( Map<String, String> additionalHeaders, ExceptionSerializer serializer,
                               OWSException exception, HttpResponseBuffer response )
                            throws ServletException {
        String userAgent = null;
        if ( OGCFrontController.getContext() != null ) {
            userAgent = OGCFrontController.getContext().getUserAgent();
        }

        SerializerProviderInitializer spi = workspace.getInitializable( SerializerProviderInitializer.class );

        ImplementationMetadata<?> md = ( (OWSProvider) metadata.getProvider() ).getImplementationMetadata();
        for ( SerializerProvider p : spi.getExceptionSerializers() ) {
            if ( p.matches( md ) ) {
                serializer = p.getSerializer( md, serializer );
            }
        }

        if ( !response.isCommitted() ) {
            try {
                response.reset();
            } catch ( IllegalStateException e ) {
                // rb: the illegal state exception occurred.
                throw new ServletException( e );
            }

            if ( additionalHeaders != null && additionalHeaders.size() > 0 ) {
                for ( String key : additionalHeaders.keySet() ) {
                    String value = additionalHeaders.get( key );
                    if ( key != null && !"".equals( key ) && value != null ) {
                        response.addHeader( key, value );
                    }
                }
            }

            try {
                serializer.serializeException( response, exception );
            } catch ( Exception e ) {
                LOG.error( "An error occurred while trying to send an exception: " + e.getLocalizedMessage(), e );
                throw new ServletException( e );
            }
        }

        if ( userAgent != null && userAgent.toLowerCase().contains( "mozilla" ) ) {
            response.setContentType( "application/xml" );
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
                                   OWSException exception, XMLExceptionSerializer serializer, String SOAPFaultCode,
                                   String SOAPMessage, String SOAPaction, String characterEncoding )
                            throws ServletException {

        String faultCode = SOAPFaultCode;
        if ( faultCode == null || "".equals( faultCode ) ) {
            faultCode = SOAPException.SENDER;
        }
        String message = SOAPMessage;
        if ( message == null || "".equals( message ) ) {
            if ( exception == null ) {
                message = "unknown";
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

        if ( "http://schemas.xmlsoap.org/soap/envelope/".equals( factory.getSoapVersionURI() ) ) {
            extraHeaders.put( "SOAPAction", action );
        }
        sendException( extraHeaders, new SOAPExceptionSerializer( version, header, factory, serializer ),
                       new SOAPException( message, faultCode, exception ), response );
    }

    /**
     * Returns the {@link ExceptionSerializer} for the given request version.
     * 
     * @param requestVersion
     *            version of the request, may be <code>null</code> (implies that the serializer for the highest
     *            supported version shall be returned)
     * @return suitable XML serializer, never <code>null</code>
     */
    public XMLExceptionSerializer getExceptionSerializer( Version requestVersion ) {
        return new OWS110ExceptionReportSerializer( Version.parseVersion( "1.1.0" ) );
    }

    @Override
    public ResourceMetadata<OWS> getMetadata() {
        return metadata;
    }

}
