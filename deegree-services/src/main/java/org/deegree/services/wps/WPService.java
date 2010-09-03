//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-services/src/main/java/org/deegree/services/wps/WPService.java $
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

package org.deegree.services.wps;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.deegree.protocol.wps.WPSConstants.VERSION_100;
import static org.deegree.protocol.wps.WPSConstants.WPS_100_NS;
import static org.deegree.services.controller.ows.OWSException.OPERATION_NOT_SUPPORTED;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.commons.fileupload.FileItem;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.FileUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.TempFileManager;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.protocol.ows.capabilities.GetCapabilities;
import org.deegree.protocol.ows.capabilities.GetCapabilitiesKVPParser;
import org.deegree.protocol.wps.WPSConstants.WPSRequestType;
import org.deegree.protocol.wps.capabilities.GetCapabilitiesXMLAdapter;
import org.deegree.protocol.wps.describeprocess.DescribeProcessRequest;
import org.deegree.protocol.wps.describeprocess.DescribeProcessRequestKVPAdapter;
import org.deegree.protocol.wps.describeprocess.DescribeProcessRequestXMLAdapter;
import org.deegree.services.controller.AbstractOGCServiceController;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.exception.ControllerException;
import org.deegree.services.controller.exception.ControllerInitException;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.ows.OWSException110XMLAdapter;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.exception.ServiceInitException;
import org.deegree.services.jaxb.main.DeegreeServiceControllerType;
import org.deegree.services.jaxb.main.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.wps.ProcessDefinition;
import org.deegree.services.jaxb.wps.PublishedInformation;
import org.deegree.services.jaxb.wps.ServiceConfiguration;
import org.deegree.services.wps.capabilities.CapabilitiesXMLWriter;
import org.deegree.services.wps.describeprocess.DescribeProcessResponseXMLAdapter;
import org.deegree.services.wps.execute.ExecuteRequest;
import org.deegree.services.wps.execute.ExecuteRequestKVPAdapter;
import org.deegree.services.wps.execute.ExecuteRequestXMLAdapter;
import org.deegree.services.wps.execute.ResponseDocument;
import org.deegree.services.wps.storage.OutputStorage;
import org.deegree.services.wps.storage.ResponseDocumentStorage;
import org.deegree.services.wps.storage.StorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles WPS (WebProcessingService) requests.
 * <p>
 * Supported WPS protocol versions:
 * <ul>
 * <li>1.0.0</li>
 * </ul>
 * </p>
 * 
 * @see OGCFrontController
 * @see ProcessManager
 * @see ExecutionManager
 * 
 * @author <a href="mailto:padberg@uni-bonn.de">Alexander Padberg</a>
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 25786 $, $Date: 2010-08-09 19:55:41 +0200 (Mo, 09 Aug 2010) $
 */
public class WPService extends AbstractOGCServiceController {

    private static final Logger LOG = LoggerFactory.getLogger( WPService.class );

    private static final ImplementationMetadata<WPSRequestType> IMPLEMENTATION_METADATA = new ImplementationMetadata<WPSRequestType>() {
        {
            supportedVersions = new Version[] { VERSION_100 };
            handledNamespaces = new String[] { WPS_100_NS };
            handledRequests = WPSRequestType.class;
            supportedConfigVersions = new Version[] { Version.parseVersion( "0.5.0" ) };
        }
    };

    private static final CodeType ALL_PROCESSES_IDENTIFIER = new CodeType( "ALL" );

    private StorageManager storageManager;

    private ProcessManager service;

    private ServiceConfiguration sc;

    private ExecutionManager executeHandler;

    private File serviceWSDLFile;

    private Map<CodeType, File> processIdToWSDL = new HashMap<CodeType, File>();

    @Override
    public void init( XMLAdapter controllerConf, DeegreeServicesMetadataType serviceMetadata,
                      DeegreeServiceControllerType mainConf )
                            throws ControllerInitException {

        init( serviceMetadata, mainConf, IMPLEMENTATION_METADATA, controllerConf );

        storageManager = new StorageManager( TempFileManager.getBaseDir() );

        NamespaceContext nsContext = new NamespaceContext();
        nsContext.addNamespace( "wps", "http://www.deegree.org/services/wps" );

        // Get ServiceConfiguration from configFile
        try {
            JAXBContext jc = JAXBContext.newInstance( "org.deegree.services.jaxb.wps" );
            Unmarshaller u = jc.createUnmarshaller();
            OMElement serviceConfigurationElement = controllerConf.getRequiredElement(
                                                                                       controllerConf.getRootElement(),
                                                                                       new XPath(
                                                                                                  "wps:ServiceConfiguration",
                                                                                                  nsContext ) );
            sc = (ServiceConfiguration) u.unmarshal( serviceConfigurationElement.getXMLStreamReaderWithoutCaching() );
        } catch ( XMLParsingException e ) {
            throw new ControllerInitException( "TODO", e );
        } catch ( JAXBException e ) {
            throw new ControllerInitException( "TODO", e );
        }

        URL controllerConfURL;
        try {
            controllerConfURL = new URL( controllerConf.getSystemId() );
            File resolvedProcessesDir = FileUtils.getAsFile( new URL( controllerConfURL, sc.getProcessesDirectory() ) );
            this.service = new ProcessManager( resolvedProcessesDir );

            OMElement piElement = controllerConf.getRequiredElement( controllerConf.getRootElement(),
                                                                     new XPath( "wps:PublishedInformation", nsContext ) );

            PublishedInformationXMLAdapter piXMLAdapter = new PublishedInformationXMLAdapter();
            piXMLAdapter.setRootElement( piElement );
            piXMLAdapter.setSystemId( controllerConf.getSystemId() );
            PublishedInformation pi = piXMLAdapter.parse();
            validateAndSetOfferedVersions( pi.getOfferedVersions().getVersion() );

            executeHandler = new ExecutionManager( this, storageManager );

            // WSDL stuff
            serviceWSDLFile = FileUtils.getAsFile( new URL( controllerConfURL, "service.wsdl" ) );
        } catch ( MalformedURLException e ) {
            throw new ControllerInitException( "Problem resolving file resource: " + e.getMessage() );
        } catch ( ServiceInitException e ) {
            throw new ControllerInitException( "Problem initializing service: " + e.getMessage() );
        }
    }

    @Override
    public void destroy() {
        service.destroy();
    }

    @Override
    public void doKVP( Map<String, String> kvpParamsUC, HttpServletRequest request, HttpResponseBuffer response,
                       List<FileItem> multiParts )
                            throws ServletException, IOException {

        LOG.trace( "doKVP invoked, version: " + kvpParamsUC.get( "VERSION" ) );

        try {
            String requestName = KVPUtils.getRequired( kvpParamsUC, "REQUEST" );
            WPSRequestType requestType = getRequestTypeByName( requestName );

            // check if requested version is supported and offered (except for GetCapabilities)
            if ( requestType != WPSRequestType.GetCapabilities ) {
                checkVersion( getVersion( KVPUtils.getRequired( kvpParamsUC, "VERSION" ) ) );
            }

            switch ( requestType ) {
            case GetCapabilities:
                GetCapabilities getCapabilitiesRequest = GetCapabilitiesKVPParser.parse( kvpParamsUC );
                doGetCapabilities( getCapabilitiesRequest, response );
                break;
            case DescribeProcess:
                DescribeProcessRequest describeProcessRequest = DescribeProcessRequestKVPAdapter.parse100( kvpParamsUC );
                doDescribeProcess( describeProcessRequest, response );
                break;
            case Execute:
                ExecuteRequest executeRequest = ExecuteRequestKVPAdapter.parse100( kvpParamsUC, service.getProcesses() );
                doExecute( executeRequest, response );
                break;
            case GetOutput:
                doGetOutput( kvpParamsUC.get( "IDENTIFIER" ), response );
                break;
            case GetResponseDocument:
                doGetResponseDocument( kvpParamsUC.get( "IDENTIFIER" ), response );
                break;
            case GetWPSWSDL:
                String identifier = kvpParamsUC.get( "IDENTIFIER" );
                CodeType processId = identifier != null ? new CodeType( identifier ) : null;
                doGetWSDL( processId, response );
                break;
            }
        } catch ( MissingParameterException e ) {
            sendServiceException( new OWSException( e.getMessage(), OWSException.MISSING_PARAMETER_VALUE ), response );
        } catch ( OWSException e ) {
            sendServiceException( e, response );
        } catch ( XMLStreamException e ) {
            LOG.debug( e.getMessage() );
        } catch ( UnknownCRSException e ) {
            LOG.debug( e.getMessage() );
        }
    }

    @Override
    public void doXML( XMLStreamReader xmlStream, HttpServletRequest request, HttpResponseBuffer response,
                       List<FileItem> multiParts )
                            throws ServletException, IOException {

        LOG.trace( "doXML invoked" );

        try {
            WPSRequestType requestType = getRequestTypeByName( xmlStream.getLocalName() );

            // check if requested version is supported and offered (except for GetCapabilities)
            Version requestVersion = getVersion( xmlStream.getAttributeValue( null, "version" ) );
            if ( requestType != WPSRequestType.GetCapabilities ) {
                checkVersion( requestVersion );
            }

            switch ( requestType ) {
            case GetCapabilities:
                GetCapabilitiesXMLAdapter getCapabilitiesAdapter = new GetCapabilitiesXMLAdapter();
                getCapabilitiesAdapter.load( xmlStream );
                GetCapabilities getCapabilitiesRequest = getCapabilitiesAdapter.parse100();
                doGetCapabilities( getCapabilitiesRequest, response );
                break;
            case DescribeProcess:
                DescribeProcessRequestXMLAdapter describeProcessAdapter = new DescribeProcessRequestXMLAdapter();
                describeProcessAdapter.load( xmlStream );
                DescribeProcessRequest describeProcessRequest = describeProcessAdapter.parse100();
                doDescribeProcess( describeProcessRequest, response );
                break;
            case Execute:
                // TODO switch to StaX-based parsing
                ExecuteRequestXMLAdapter executeAdapter = new ExecuteRequestXMLAdapter( service.getProcesses() );
                executeAdapter.load( xmlStream );
                ExecuteRequest executeRequest = executeAdapter.parse100();
                doExecute( executeRequest, response );
                break;
            case GetOutput:
            case GetResponseDocument:
            case GetWPSWSDL:
                String msg = "Request type '" + requestType.name() + "' is only support as KVP request.";
                throw new OWSException( msg, OWSException.OPERATION_NOT_SUPPORTED );
            }
        } catch ( OWSException e ) {
            sendServiceException( e, response );
        } catch ( XMLStreamException e ) {
            LOG.debug( e.getMessage() );
        } catch ( UnknownCRSException e ) {
            LOG.debug( e.getMessage() );
        }
    }

    @Override
    public void doSOAP( SOAPEnvelope soapDoc, HttpServletRequest request, HttpResponseBuffer response,
                        List<FileItem> multiParts, SOAPFactory factory )
                            throws ServletException, IOException {

        LOG.trace( "doSOAP invoked" );
        OMElement requestElement = soapDoc.getBody().getFirstElement();
        try {
            WPSRequestType requestType = getRequestTypeByName( requestElement.getLocalName() );

            // check if requested version is supported and offered (except for GetCapabilities)
            Version requestVersion = getVersion( requestElement.getAttributeValue( new QName( "version" ) ) );
            if ( requestType != WPSRequestType.GetCapabilities ) {
                checkVersion( requestVersion );
            }

            beginSOAPResponse( response );

            switch ( requestType ) {
            case GetCapabilities:
                GetCapabilitiesXMLAdapter getCapabilitiesAdapter = new GetCapabilitiesXMLAdapter();
                getCapabilitiesAdapter.setRootElement( requestElement );
                // getCapabilitiesAdapter.setSystemId( soapDoc.getSystemId() );
                GetCapabilities getCapabilitiesRequest = getCapabilitiesAdapter.parse100();
                doGetCapabilities( getCapabilitiesRequest, response );
                break;
            case DescribeProcess:
                DescribeProcessRequestXMLAdapter describeProcessAdapter = new DescribeProcessRequestXMLAdapter();
                describeProcessAdapter.setRootElement( requestElement );
                // describeProcessAdapter.setSystemId( soapDoc.getSystemId() );
                DescribeProcessRequest describeProcessRequest = describeProcessAdapter.parse100();
                doDescribeProcess( describeProcessRequest, response );
                break;
            case Execute:
                // TODO switch to StaX-based parsing
                ExecuteRequestXMLAdapter executeAdapter = new ExecuteRequestXMLAdapter( service.getProcesses() );
                executeAdapter.setRootElement( requestElement );
                // executeAdapter.setSystemId( soapDoc.getSystemId() );
                ExecuteRequest executeRequest = executeAdapter.parse100();
                doExecute( executeRequest, response );
                break;
            case GetOutput:
            case GetResponseDocument:
            case GetWPSWSDL:
                String msg = "Request type '" + requestType.name() + "' is only support as KVP request.";
                throw new OWSException( msg, OWSException.OPERATION_NOT_SUPPORTED );
            }

            endSOAPResponse( response );

        } catch ( OWSException e ) {
            sendSOAPException( soapDoc.getHeader(), factory, response, e, null, null, null, request.getServerName(),
                               request.getCharacterEncoding() );
        } catch ( XMLStreamException e ) {
            LOG.debug( e.getMessage(), e );
        } catch ( UnknownCRSException e ) {
            LOG.debug( e.getMessage(), e );
        }
    }

    /**
     * Returns the underlying {@link ProcessManager} instance.
     * 
     * @return the underlying {@link ProcessManager}, never <code>null</code>
     */
    public ProcessManager getProcessManager() {
        return service;
    }

    /**
     * Returns the associated {@link ExecutionManager} instance.
     * 
     * @return the associated {@link ExecutionManager}, never <code>null</code>
     */
    public ExecutionManager getExecutionManager() {
        return executeHandler;
    }

    @Override
    public Pair<XMLExceptionSerializer<OWSException>, String> getExceptionSerializer( Version requestVersion ) {
        return new Pair<XMLExceptionSerializer<OWSException>, String>( new OWSException110XMLAdapter(), "text/xml" );
    }

    private WPSRequestType getRequestTypeByName( String requestName )
                            throws OWSException {
        WPSRequestType requestType = null;
        try {
            requestType = IMPLEMENTATION_METADATA.getRequestTypeByName( requestName );
        } catch ( IllegalArgumentException e ) {
            throw new OWSException( e.getMessage(), OPERATION_NOT_SUPPORTED );
        }
        return requestType;
    }

    private Version getVersion( String versionString )
                            throws OWSException {

        Version version = null;
        if ( versionString != null ) {
            try {
                version = Version.parseVersion( versionString );
            } catch ( IllegalArgumentException e ) {
                throw new OWSException( "Specified request version '" + versionString
                                        + "' is not a valid OGC version string.", OWSException.INVALID_PARAMETER_VALUE );
            }
        }
        return version;
    }

    private void doGetCapabilities( GetCapabilities request, HttpResponseBuffer response )
                            throws OWSException, XMLStreamException, IOException {

        LOG.trace( "doGetCapabilities invoked, request: " + request );

        // generic check if requested version is supported (currently this is only 1.0.0)
        negotiateVersion( request );

        response.setContentType( "text/xml; charset=UTF-8" );
        XMLStreamWriter xmlWriter = response.getXMLWriter();
        String wsdlURL = null;
        if ( serviceWSDLFile != null ) {
            wsdlURL = OGCFrontController.getHttpGetURL() + "service=WPS&version=1.0.0&request=GetWPSWSDL";
        }
        CapabilitiesXMLWriter.export100( xmlWriter, service.getProcesses(), mainMetadataConf, wsdlURL );

        LOG.trace( "doGetCapabilities finished" );
    }

    private void doDescribeProcess( DescribeProcessRequest request, HttpResponseBuffer response )
                            throws OWSException {

        LOG.trace( "doDescribeProcess invoked, request: " + request );

        // check that all requested processes exist (and resolve special value 'ALL')
        List<WPSProcess> processes = new ArrayList<WPSProcess>();
        for ( CodeType identifier : request.getIdentifiers() ) {
            LOG.debug( "Looking up process '" + identifier + "'" );
            if ( ALL_PROCESSES_IDENTIFIER.equals( identifier ) ) {
                processes.addAll( service.getProcesses().values() );
                break;
            }
            WPSProcess process = service.getProcess( identifier );
            if ( process != null ) {
                processes.add( process );
            } else {
                throw new OWSException( "InvalidParameterValue: Identifier\nNo process with id " + identifier
                                        + " is registered in the WPS.", OWSException.INVALID_PARAMETER_VALUE );
            }
        }

        try {
            response.setContentType( "text/xml; charset=UTF-8" );
            XMLStreamWriter xmlWriter = response.getXMLWriter();

            Map<ProcessDefinition, String> processDefToWSDLUrl = new HashMap<ProcessDefinition, String>();
            for ( WPSProcess process : processes ) {
                ProcessDefinition processDef = process.getDescription();
                CodeType processId = new CodeType( processDef.getIdentifier().getValue(),
                                                   processDef.getIdentifier().getCodeSpace() );
                if ( processIdToWSDL.containsKey( processId ) ) {
                    String wsdlURL = OGCFrontController.getHttpGetURL()
                                     + "service=WPS&version=1.0.0&request=GetWPSWSDL&identifier=" + processId.getCode();
                    processDefToWSDLUrl.put( processDef, wsdlURL );
                }
            }

            // TransformCoordinates tc = new TransformCoordinates();
            // ProcessDescription pd = tc.getClass().getAnnotation( ProcessDescription.class );
            // List<ProcessDescription> pdA = new LinkedList<ProcessDescription>();
            // if ( pd != null ) {
            // pdA.add( pd );
            // }
            // TODO what about annotations?
            DescribeProcessResponseXMLAdapter.export100( xmlWriter, processes, processDefToWSDLUrl, null );
            xmlWriter.flush();
        } catch ( XMLStreamException e ) {
            e.printStackTrace();
            LOG.error( "Internal error: " + e.getMessage() );
            throw new OWSException( "Error occured while creating response for DescribeProcess operation",
                                    ControllerException.NO_APPLICABLE_CODE );
        } catch ( IOException e ) {
            throw new OWSException( "Error occured while creating response for DescribeProcess operation",
                                    ControllerException.NO_APPLICABLE_CODE );
        } catch ( Exception e ) {
            e.printStackTrace();
            LOG.error( "Internal error: " + e.getMessage() );
        }

        LOG.trace( "doDescribeProcess finished" );
    }

    private void doExecute( ExecuteRequest request, HttpResponseBuffer response )
                            throws OWSException {

        LOG.trace( "doExecute invoked, request: " + request.toString() );
        long start = System.currentTimeMillis();

        CodeType processId = request.getProcessId();
        WPSProcess process = service.getProcess( processId );
        if ( process == null ) {
            String msg = "Internal error. Process '" + processId + "' not found.";
            throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE );
        }

        try {
            if ( request.getResponseForm() == null || request.getResponseForm() instanceof ResponseDocument ) {
                executeHandler.handleResponseDocumentOutput( request, response, process );
            } else {
                executeHandler.handleRawDataOutput( request, response, process );
            }
        } catch ( OWSException e ) {
            throw e;
        } catch ( Exception e ) {
            LOG.debug( e.getMessage(), e );
            throw new OWSException( e.getMessage(), ControllerException.NO_APPLICABLE_CODE );
        }

        long elapsed = System.currentTimeMillis() - start;
        LOG.debug( "doExecute took " + elapsed + " milliseconds" );

        LOG.trace( "doExecute finished" );
    }

    private void doGetOutput( String storedOutputId, HttpResponseBuffer response ) {

        LOG.trace( "doGetOutput invoked, requested stored output: " + storedOutputId );
        OutputStorage resource = storageManager.lookupOutputStorage( storedOutputId );

        if ( resource == null ) {
            try {
                response.sendError( 404, "No stored output with id '" + storedOutputId + "' found." );
            } catch ( IOException e ) {
                LOG.debug( "Error sending exception report to client.", e );
            }
        } else {
            resource.sendResource( response );
        }

        LOG.trace( "doGetOutput finished" );
    }

    private void doGetResponseDocument( String responseId, HttpResponseBuffer response ) {

        LOG.trace( "doGetResponseDocument invoked, requested stored response document: " + responseId );
        ResponseDocumentStorage resource = storageManager.lookupResponseDocumentStorage( responseId );
        executeHandler.sendResponseDocument( response, resource );

        LOG.trace( "doGetResponseDocument finished" );
    }

    private void doGetWSDL( CodeType processId, HttpResponseBuffer response ) {

        LOG.trace( "doGetWSDL invoked, requested resource: " + processId );

        File wsdlFile = serviceWSDLFile;
        if ( processId != null ) {
            wsdlFile = processIdToWSDL.get( processId );
        }

        if ( wsdlFile == null || !wsdlFile.exists() ) {
            try {
                response.sendError( 404, "WSDL document not available." );
            } catch ( IOException e ) {
                LOG.debug( "Error sending exception report to client.", e );
            }
        } else {
            try {
                response.setContentType( "text/xml" );
                response.setContentLength( (int) wsdlFile.length() );
                OutputStream os = response.getOutputStream();
                InputStream is = new FileInputStream( wsdlFile );
                byte[] buffer = new byte[4096];
                int numBytes = -1;
                while ( ( numBytes = is.read( buffer ) ) != -1 ) {
                    os.write( buffer, 0, numBytes );
                }
                os.flush();
            } catch ( IOException e ) {
                LOG.debug( "Error sending WSDL document to client.", e );
            }
        }

        LOG.trace( "doGetWSDL finished" );
    }

    private void sendServiceException( OWSException ex, HttpResponseBuffer response )
                            throws ServletException {
        // TODO use correct exception code here (400)
        sendException( "text/xml", "UTF-8", null, 200, new OWSException110XMLAdapter(), ex, response );
    }
}
