//$HeadURL$ svn+ssh://sthomas@svn.wald.intevation.org/deegree/deegree3/services/trunk/src/org/deegree/services/controller/csw/CSWController.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.services.csw;

import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.VERSION_202;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.commons.fileupload.FileItem;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.XMLStreamWriterWrapper;
import org.deegree.protocol.csw.CSWConstants;
import org.deegree.protocol.csw.CSWConstants.CSWRequestType;
import org.deegree.protocol.csw.CSWConstants.Sections;
import org.deegree.protocol.ows.capabilities.GetCapabilities;
import org.deegree.record.persistence.RecordStoreException;
import org.deegree.services.authentication.SecurityException;
import org.deegree.services.authentication.soapauthentication.FailedAuthentication;
import org.deegree.services.controller.AbstractOGCServiceController;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.exception.ControllerInitException;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.ows.OWSException110XMLAdapter;
import org.deegree.services.controller.ows.OWSException120XMLAdapter;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.csw.capabilities.GetCapabilities202KVPAdapter;
import org.deegree.services.csw.capabilities.GetCapabilitiesVersionXMLAdapter;
import org.deegree.services.csw.describerecord.DescribeRecord;
import org.deegree.services.csw.describerecord.DescribeRecordKVPAdapter;
import org.deegree.services.csw.describerecord.DescribeRecordXMLAdapter;
import org.deegree.services.csw.exporthandling.DescribeRecordHandler;
import org.deegree.services.csw.exporthandling.GetCapabilitiesHandler;
import org.deegree.services.csw.exporthandling.GetRecordByIdHandler;
import org.deegree.services.csw.exporthandling.GetRecordsHandler;
import org.deegree.services.csw.exporthandling.TransactionHandler;
import org.deegree.services.csw.getrecordbyid.GetRecordById;
import org.deegree.services.csw.getrecordbyid.GetRecordByIdKVPAdapter;
import org.deegree.services.csw.getrecordbyid.GetRecordByIdXMLAdapter;
import org.deegree.services.csw.getrecords.GetRecords;
import org.deegree.services.csw.getrecords.GetRecordsKVPAdapter;
import org.deegree.services.csw.getrecords.GetRecordsXMLAdapter;
import org.deegree.services.csw.security.CSWSecurityManager;
import org.deegree.services.csw.security.DummyCSWSecurityManager;
import org.deegree.services.csw.transaction.Transaction;
import org.deegree.services.csw.transaction.TransactionKVPAdapter;
import org.deegree.services.csw.transaction.TransactionXMLAdapter;
import org.deegree.services.jaxb.csw.PublishedInformation;
import org.deegree.services.jaxb.csw.ServiceConfiguration;
import org.deegree.services.jaxb.main.DeegreeServiceControllerType;
import org.deegree.services.jaxb.main.DeegreeServicesMetadataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Handles CSW (Cataloge Service for Web) protocol requests and acts as the link between the {@link OGCFrontController}
 * and the {@link CSWService}.
 * <p>
 * Supported CSW protocol versions:
 * <ul>
 * <li>2.0.2</li>
 * </ul>
 * </p>
 * 
 * @see CSWService
 * @see AbstractOGCServiceController
 * @see OGCFrontController
 * 
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class CSWController extends AbstractOGCServiceController {

    private static final Logger LOG = LoggerFactory.getLogger( CSWController.class );

    private CSWService service;

    private CSWSecurityManager securityManager;

    private DescribeRecordHandler describeRecordHandler;

    private GetRecordsHandler getRecordsHandler;

    private TransactionHandler transactionHandler;

    private GetRecordByIdHandler getRecordByIdHandler;

    private static final ImplementationMetadata<CSWRequestType> IMPLEMENTATION_METADATA = new ImplementationMetadata<CSWRequestType>() {
        {
            supportedVersions = new Version[] { VERSION_202 };
            handledNamespaces = new String[] { CSW_202_NS };
            handledRequests = CSWRequestType.class;
            supportedConfigVersions = new Version[] { Version.parseVersion( "0.5.0" ) };
        }
    };

    @Override
    public void init( XMLAdapter controllerConf, DeegreeServicesMetadataType serviceMetadata,
                      DeegreeServiceControllerType mainConf )
                            throws ControllerInitException {
        init( serviceMetadata, mainConf, IMPLEMENTATION_METADATA, controllerConf );

        LOG.info( "Initializing CSW controller." );

        // check config version
        String configVersion = controllerConf.getRootElement().getAttributeValue( new QName( "configVersion" ) );
        checkConfigVersion( controllerConf.getSystemId(), configVersion );

        NamespaceContext nsContext = new NamespaceContext();
        nsContext.addNamespace( CSWConstants.CSW_PREFIX, "http://www.deegree.org/services/csw" );

        ServiceConfiguration sc = null;
        PublishedInformation pi = null;

        try {
            XPath xpath = new XPath( "csw:ServiceConfiguration", nsContext );

            final String additionalClasspath = "org.deegree.services.jaxb.csw";
            Unmarshaller u = getUnmarshaller( additionalClasspath, null );

            OMElement scElement = controllerConf.getRequiredElement( controllerConf.getRootElement(), xpath );

            // turn the application schema location into an absolute URL
            sc = (ServiceConfiguration) u.unmarshal( scElement.getXMLStreamReaderWithoutCaching() );

            u = JAXBContext.newInstance( "org.deegree.services.jaxb.csw" ).createUnmarshaller();

            xpath = new XPath( "csw:PublishedInformation", nsContext );
            OMElement piElement = controllerConf.getRequiredElement( controllerConf.getRootElement(), xpath );
            pi = (PublishedInformation) u.unmarshal( piElement.getXMLStreamReaderWithoutCaching() );

            service = new CSWService( sc, controllerConf.getSystemId() );

            if ( sc.getSecurityManager() == null ) {
                // then do nothing and step over
            } else {
                securityManager = sc.getSecurityManager().getDummySecurityManager() != null ? new DummyCSWSecurityManager()
                                                                                           : null;
            }

            String securityLogging = securityManager != null ? "A securityManager is specified: " + securityManager
                                                            : "There is no securityManager specified. No credentials needed and every operation can be requested anonymous.";
            LOG.debug( securityLogging );

        } catch ( XMLParsingException e ) {
            throw new ControllerInitException( "TODO", e );
        } catch ( JAXBException e ) {
            throw new ControllerInitException( "TODO", e );
        } catch ( RecordStoreException e ) {
            e.printStackTrace();
        }

        validateAndSetOfferedVersions( pi.getAcceptVersions().getVersion() );
        describeRecordHandler = new DescribeRecordHandler( service );
        getRecordsHandler = new GetRecordsHandler( service );
        transactionHandler = new TransactionHandler( service );
        getRecordByIdHandler = new GetRecordByIdHandler( service );

    }

    @Override
    protected void doKVP( Map<String, String> normalizedKVPParams, HttpServletRequest request,
                          HttpResponseBuffer response, List<FileItem> multiParts )
                            throws ServletException, IOException {

        try {
            String rootElement = KVPUtils.getRequired( normalizedKVPParams, "REQUEST" );
            CSWRequestType requestType = getRequestType( rootElement );

            Version requestVersion = getVersion( normalizedKVPParams.get( "VERSION" ) );

            String serviceAttr = KVPUtils.getRequired( normalizedKVPParams, "SERVICE" );
            if ( !"CSW".equals( serviceAttr ) ) {
                throw new OWSException( "Wrong service attribute: '" + serviceAttr + "' -- must be 'CSW'.",
                                        OWSException.INVALID_PARAMETER_VALUE, "service" );
            }
            if ( requestType != CSWRequestType.GetCapabilities ) {
                checkVersion( requestVersion );
            }

            switch ( requestType ) {

            case GetCapabilities:

                GetCapabilities getCapabilities = securityManager == null ? GetCapabilities202KVPAdapter.parse(
                                                                                                                requestVersion,
                                                                                                                normalizedKVPParams )
                                                                         : securityManager.preprocess(
                                                                                                       GetCapabilities202KVPAdapter.parse(
                                                                                                                                           requestVersion,
                                                                                                                                           normalizedKVPParams ),
                                                                                                       OGCFrontController.getContext().getCredentials(),
                                                                                                       false );
                doGetCapabilities( getCapabilities, request, response, false );
                break;
            case DescribeRecord:

                DescribeRecord descRec = securityManager == null ? DescribeRecordKVPAdapter.parse( normalizedKVPParams )
                                                                : securityManager.preprocess(
                                                                                              DescribeRecordKVPAdapter.parse( normalizedKVPParams ),
                                                                                              OGCFrontController.getContext().getCredentials(),
                                                                                              false );
                // describeRecordResponse = new DescribeRecordResponseXMLAdapter(service);
                describeRecordHandler.doDescribeRecord( descRec, response, false );
                break;

            case GetRecords:

                GetRecords getRec = securityManager == null ? GetRecordsKVPAdapter.parse( normalizedKVPParams )
                                                           : securityManager.preprocess(
                                                                                         GetRecordsKVPAdapter.parse( normalizedKVPParams ),
                                                                                         OGCFrontController.getContext().getCredentials(),
                                                                                         false );
                getRecordsHandler.doGetRecords( getRec, response, false );
                break;
            case GetRecordById:

                GetRecordById getRecBI = securityManager == null ? GetRecordByIdKVPAdapter.parse( normalizedKVPParams )
                                                                : securityManager.preprocess(
                                                                                              GetRecordByIdKVPAdapter.parse( normalizedKVPParams ),
                                                                                              OGCFrontController.getContext().getCredentials(),
                                                                                              false );

                getRecordByIdHandler.doGetRecordById( getRecBI, response, false );
                break;
            case Transaction:

                Transaction trans = securityManager == null ? TransactionKVPAdapter.parse( normalizedKVPParams )
                                                           : securityManager.preprocess(
                                                                                         TransactionKVPAdapter.parse( normalizedKVPParams ),
                                                                                         OGCFrontController.getContext().getCredentials(),
                                                                                         false );

                transactionHandler.doTransaction( trans, response, false );
                break;
            }

        } catch ( OWSException e ) {
            sendServiceException( e, response );
        } catch ( XMLStreamException e ) {
            e.printStackTrace();
        } catch ( SQLException e ) {
            e.printStackTrace();
        }

    }

    @Override
    protected void doXML( XMLStreamReader xmlStream, HttpServletRequest request, HttpResponseBuffer response,
                          List<FileItem> multiParts )
                            throws ServletException, IOException, SecurityException {

        response.setContentType( "text/xml" );

        try {
            XMLAdapter requestDoc = new XMLAdapter( xmlStream );
            String rootElement = requestDoc.getRootElement().getLocalName();
            CSWRequestType requestType = getRequestType( rootElement );

            // check if requested version is supported and offered (except for GetCapabilities)
            Version requestVersion = getVersion( requestDoc.getRootElement().getAttributeValue( new QName( "version" ) ) );
            if ( requestType != CSWRequestType.GetCapabilities ) {
                checkVersion( requestVersion );
            }

            switch ( requestType ) {

            case GetCapabilities:
                LOG.info( "SecManager: " + securityManager );
                GetCapabilitiesVersionXMLAdapter getCapabilitiesAdapter = new GetCapabilitiesVersionXMLAdapter();
                getCapabilitiesAdapter.setRootElement( requestDoc.getRootElement() );
                // if the securityManager is null don't care about anything. Even if here is the authorization in the
                // header.
                GetCapabilities cswRequest = securityManager == null
                                             && ( request.getHeader( "authorization" ) != null || request.getHeader( "authorization" ) == null ) ? getCapabilitiesAdapter.parse( requestVersion )
                                                                                                                                                : securityManager.preprocess(
                                                                                                                                                                              getCapabilitiesAdapter.parse( requestVersion ),
                                                                                                                                                                              OGCFrontController.getContext().getCredentials(),
                                                                                                                                                                              false );

                doGetCapabilities( cswRequest, request, response, false );
                break;
            case DescribeRecord:
                DescribeRecordXMLAdapter describeRecordAdapter = new DescribeRecordXMLAdapter();
                describeRecordAdapter.setRootElement( requestDoc.getRootElement() );

                DescribeRecord cswDRRequest = securityManager == null
                                              && ( request.getHeader( "authorization" ) != null || request.getHeader( "authorization" ) == null ) ? describeRecordAdapter.parse( requestVersion )
                                                                                                                                                 : securityManager.preprocess(
                                                                                                                                                                               describeRecordAdapter.parse( requestVersion ),
                                                                                                                                                                               OGCFrontController.getContext().getCredentials(),
                                                                                                                                                                               false );
                describeRecordHandler.doDescribeRecord( cswDRRequest, response, false );
                break;
            case GetRecords:
                GetRecordsXMLAdapter getRecordsAdapter = new GetRecordsXMLAdapter();
                getRecordsAdapter.setRootElement( requestDoc.getRootElement() );

                GetRecords cswGRRequest = securityManager == null
                                          && ( request.getHeader( "authorization" ) != null || request.getHeader( "authorization" ) == null ) ? getRecordsAdapter.parse( requestVersion )
                                                                                                                                             : securityManager.preprocess(
                                                                                                                                                                           getRecordsAdapter.parse( requestVersion ),
                                                                                                                                                                           OGCFrontController.getContext().getCredentials(),
                                                                                                                                                                           false );

                getRecordsHandler.doGetRecords( cswGRRequest, response, false );

                break;
            case GetRecordById:
                GetRecordByIdXMLAdapter getRecordByIdAdapter = new GetRecordByIdXMLAdapter();
                getRecordByIdAdapter.setRootElement( requestDoc.getRootElement() );

                GetRecordById cswGRBIRequest = securityManager == null
                                               && ( request.getHeader( "authorization" ) != null || request.getHeader( "authorization" ) == null ) ? getRecordByIdAdapter.parse( requestVersion )
                                                                                                                                                  : securityManager.preprocess(
                                                                                                                                                                                getRecordByIdAdapter.parse( requestVersion ),
                                                                                                                                                                                OGCFrontController.getContext().getCredentials(),
                                                                                                                                                                                false );
                getRecordByIdHandler.doGetRecordById( cswGRBIRequest, response, false );
                break;
            case Transaction:
                TransactionXMLAdapter transAdapter = new TransactionXMLAdapter();
                transAdapter.setRootElement( requestDoc.getRootElement() );

                Transaction cswTRequest = securityManager == null
                                          && ( request.getHeader( "authorization" ) != null || request.getHeader( "authorization" ) == null ) ? transAdapter.parse( requestVersion )
                                                                                                                                             : securityManager.preprocess(
                                                                                                                                                                           transAdapter.parse( requestVersion ),
                                                                                                                                                                           OGCFrontController.getContext().getCredentials(),
                                                                                                                                                                           false );
                transactionHandler.doTransaction( cswTRequest, response, false );
                break;
            }

        } catch ( OWSException e ) {
            sendServiceException( e, response );
        } catch ( MissingParameterException e ) {
            sendServiceException( new OWSException( e ), response );
        } catch ( InvalidParameterValueException e ) {
            sendServiceException( new OWSException( e ), response );
        } catch ( XMLStreamException e ) {
            e.printStackTrace();
        } catch ( SQLException e ) {

            e.printStackTrace();
        }

        // catch ( Exception e ) {
        // sendServiceException( new OWSException( e.getMessage(), ControllerException.NO_APPLICABLE_CODE ), response );
        // }

    }

    @Override
    protected void doSOAP( SOAPEnvelope soapDoc, HttpServletRequest request, HttpResponseBuffer response,
                           List<FileItem> multiParts, SOAPFactory factory )
                            throws OMException, ServletException {

        LOG.trace( "doSOAP invoked" );

        OMElement requestElement = soapDoc.getBody().getFirstElement();

        try {
            String rootElement = requestElement.getLocalName();

            CSWRequestType requestType = getRequestType( rootElement );

            Version requestVersion = getVersion( requestElement.getAttributeValue( new QName( "version" ) ) );
            // check if requested version is supported and offered (except for GetCapabilities)
            // if ( requestType != CSWRequestType.GetCapabilities ) {
            // checkVersion( requestVersion );
            // }

            beginSOAPResponse( response );

            switch ( requestType ) {
            case GetCapabilities:
                GetCapabilitiesVersionXMLAdapter getCapabilitiesAdapter = new GetCapabilitiesVersionXMLAdapter();
                getCapabilitiesAdapter.setRootElement( requestElement );

                GetCapabilities cswRequest = securityManager == null
                                             && ( request.getHeader( "authorization" ) != null || request.getHeader( "authorization" ) == null ) ? getCapabilitiesAdapter.parse( requestVersion )
                                                                                                                                                : securityManager.preprocess(
                                                                                                                                                                              getCapabilitiesAdapter.parse( requestVersion ),
                                                                                                                                                                              OGCFrontController.getContext().getCredentials(),
                                                                                                                                                                              true );
                doGetCapabilities( cswRequest, request, response, true );
                break;
            case DescribeRecord:
                DescribeRecordXMLAdapter describeRecordAdapter = new DescribeRecordXMLAdapter();
                describeRecordAdapter.setRootElement( requestElement );

                DescribeRecord cswDRRequest = securityManager == null
                                              && ( request.getHeader( "authorization" ) != null || request.getHeader( "authorization" ) == null ) ? describeRecordAdapter.parse( requestVersion )
                                                                                                                                                 : securityManager.preprocess(
                                                                                                                                                                               describeRecordAdapter.parse( requestVersion ),
                                                                                                                                                                               OGCFrontController.getContext().getCredentials(),
                                                                                                                                                                               true );
                describeRecordHandler.doDescribeRecord( cswDRRequest, response, true );
                break;
            case GetRecords:
                GetRecordsXMLAdapter getRecordsAdapter = new GetRecordsXMLAdapter();
                getRecordsAdapter.setRootElement( requestElement );

                GetRecords cswGRRequest = securityManager == null
                                          && ( request.getHeader( "authorization" ) != null || request.getHeader( "authorization" ) == null ) ? getRecordsAdapter.parse( requestVersion )
                                                                                                                                             : securityManager.preprocess(
                                                                                                                                                                           getRecordsAdapter.parse( requestVersion ),
                                                                                                                                                                           OGCFrontController.getContext().getCredentials(),
                                                                                                                                                                           true );
                getRecordsHandler.doGetRecords( cswGRRequest, response, true );
                break;
            case GetRecordById:

                GetRecordByIdXMLAdapter getRecordByIdAdapter = new GetRecordByIdXMLAdapter();
                getRecordByIdAdapter.setRootElement( requestElement );

                GetRecordById cswGRBIRequest = securityManager == null
                                               && ( request.getHeader( "authorization" ) != null || request.getHeader( "authorization" ) == null ) ? getRecordByIdAdapter.parse( requestVersion )
                                                                                                                                                  : securityManager.preprocess(
                                                                                                                                                                                getRecordByIdAdapter.parse( requestVersion ),
                                                                                                                                                                                OGCFrontController.getContext().getCredentials(),
                                                                                                                                                                                true );

                getRecordByIdHandler.doGetRecordById( cswGRBIRequest, response, true );
                break;
            case Transaction:
                TransactionXMLAdapter transAdapter = new TransactionXMLAdapter();
                transAdapter.setRootElement( requestElement );

                Transaction cswTRequest = securityManager == null
                                          && ( request.getHeader( "authorization" ) != null || request.getHeader( "authorization" ) == null ) ? transAdapter.parse( requestVersion )
                                                                                                                                             : securityManager.preprocess(
                                                                                                                                                                           transAdapter.parse( requestVersion ),
                                                                                                                                                                           OGCFrontController.getContext().getCredentials(),
                                                                                                                                                                           true );

                transactionHandler.doTransaction( cswTRequest, response, true );
                break;

            }

            // endSOAPResponse( response );
        } catch ( OWSException e ) {
            sendSOAPException( soapDoc.getHeader(), factory, response, e, new OWSException120XMLAdapter(), null, null,
                               request.getServerName(), request.getCharacterEncoding() );
        } catch ( XMLStreamException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        } catch ( SQLException e ) {
            e.printStackTrace();
        } catch ( MissingParameterException e ) {
            sendSOAPException( soapDoc.getHeader(), factory, response, new OWSException( e ),
                               new OWSException120XMLAdapter(), null, null, request.getServerName(),
                               request.getCharacterEncoding() );

        } catch ( InvalidParameterValueException e ) {
            sendSOAPException( soapDoc.getHeader(), factory, response, new OWSException( e ),
                               new OWSException120XMLAdapter(), null, null, request.getServerName(),
                               request.getCharacterEncoding() );
        } catch ( FailedAuthentication e ) {
            SOAPFault fault = factory.createSOAPFault();
            SOAPFaultCode faultCode = factory.createSOAPFaultCode( fault );
            faultCode.setText( e.getFaultCode11() );

            sendSOAPException( null, factory, response, null, new OWSException120XMLAdapter(), faultCode.getText(),
                               e.getMessage(), request.getServerName(), request.getCharacterEncoding() );
        }

    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    /**
     * Method for mapping the request operation to the implemented operations located in {@link CSWConstants}
     * 
     * @param requestName
     * @return CSWRequestType
     * @throws OWSException
     */
    private CSWRequestType getRequestType( String requestName )
                            throws OWSException {
        CSWRequestType requestType = null;
        try {

            requestType = IMPLEMENTATION_METADATA.getRequestTypeByName( requestName );
        } catch ( IllegalArgumentException e ) {
            throw new OWSException( e.getMessage(), OWSException.OPERATION_NOT_SUPPORTED );
        }
        return requestType;
    }

    /**
     * Exports the correct recognized request.
     * 
     * @param getCapabilitiesRequest
     * @param requestWrapper
     * @param response
     * @throws XMLStreamException
     * @throws IOException
     * @throws OWSException
     */
    private void doGetCapabilities( GetCapabilities getCapabilitiesRequest, HttpServletRequest requestWrapper,
                                    HttpResponseBuffer response, boolean isSoap )
                            throws XMLStreamException, IOException, OWSException {

        checkOrCreateDCPGetURL( requestWrapper );
        checkOrCreateDCPPostURL( requestWrapper );
        Set<Sections> sections = getSections( getCapabilitiesRequest );

        Version negotiatedVersion = negotiateVersion( getCapabilitiesRequest );
        response.setContentType( "text/xml; charset=UTF-8" );

        XMLStreamWriter xmlWriter = getXMLResponseWriter( response, null );
        GetCapabilitiesHandler.export( xmlWriter, mainMetadataConf, mainControllerConf, sections,
                                       mainMetadataConf.getServiceIdentification(), negotiatedVersion, isSoap );
        xmlWriter.flush();

    }

    /**
     * Returns an <code>XMLStreamWriter</code> for writing an XML response document.
     * 
     * @param writer
     *            writer to write the XML to, must not be null
     * @param schemaLocation
     *            allows to specify a value for the 'xsi:schemaLocation' attribute in the root element, must not be null
     * @return {@link XMLStreamWriter}
     * @throws XMLStreamException
     * @throws IOException
     */
    static XMLStreamWriter getXMLResponseWriter( HttpResponseBuffer writer, String schemaLocation )
                            throws XMLStreamException, IOException {

        if ( schemaLocation == null ) {
            return writer.getXMLWriter();
        }
        return new XMLStreamWriterWrapper( writer.getXMLWriter(), schemaLocation );
    }

    private void sendServiceException( OWSException ex, HttpResponseBuffer response )
                            throws ServletException {

        // TODO correct status code?
        sendException( "application/vnd.ogc.se_xml", "UTF-8", null, 300, new OWSException120XMLAdapter(), ex, response );
    }

    /**
     * Gets the sections described in the GetCapabilities operation.
     * 
     * @param capabilitiesReq
     * @return a set of type sections
     */
    private Set<Sections> getSections( GetCapabilities capabilitiesReq ) {
        Set<String> sections = capabilitiesReq.getSections();
        Set<Sections> result = new HashSet<Sections>();
        if ( !( sections.isEmpty() || sections.contains( "/" ) ) ) {
            final int length = "/CSW_Capabilities/".length();
            for ( String section : sections ) {
                if ( section.startsWith( "/CSW_Capabilities/" ) ) {
                    section = section.substring( length );
                }
                try {
                    result.add( Sections.valueOf( section ) );
                } catch ( IllegalArgumentException ex ) {
                    // unknown section name
                    // the spec does not say what to do, so we ignore it
                }
            }
        }
        return result;
    }

    /**
     * Parses the version.
     * 
     * @param versionString
     *            that should be parsed
     * @return {@link Version}
     * @throws OWSException
     */
    private Version getVersion( String versionString )
                            throws OWSException {
        Version version = null;
        if ( versionString != null ) {
            try {
                version = Version.parseVersion( versionString );
            } catch ( InvalidParameterValueException e ) {
                throw new OWSException( e );
            }
        }
        return version;
    }

    @Override
    public Pair<XMLExceptionSerializer<OWSException>, String> getExceptionSerializer( Version requestVersion ) {
        String mime = "application/vnd.ogc.se_xml";
        XMLExceptionSerializer<OWSException> serializer = new OWSException110XMLAdapter();
        return new Pair<XMLExceptionSerializer<OWSException>, String>( serializer, mime );
    }

}
