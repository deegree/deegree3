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
package org.deegree.ogcwebservices.wps.execute;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.Code;
import org.deegree.datatypes.values.TypedLiteral;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wps.ServerBusyException;
import org.deegree.ogcwebservices.wps.WPService;
import org.deegree.ogcwebservices.wps.configuration.WPSConfiguration;
import org.deegree.ogcwebservices.wps.describeprocess.ComplexData;
import org.deegree.ogcwebservices.wps.describeprocess.InputDescription;
import org.deegree.ogcwebservices.wps.describeprocess.OutputDescription;
import org.deegree.ogcwebservices.wps.describeprocess.ProcessDescription;
import org.deegree.ogcwebservices.wps.describeprocess.ProcessDescription.DataInputs;
import org.deegree.ogcwebservices.wps.describeprocess.ProcessDescription.ProcessOutputs;
import org.deegree.ogcwebservices.wps.execute.IOValue.ComplexValueReference;

/**
 * WPSExecuteRequestHandler.java
 *
 * Created on 10.03.2006. 12:11:28h
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ExecuteRequestHandler {

    private static final ILogger LOG = LoggerFactory.getLogger( ExecuteRequestHandler.class );

    private WPService wpService = null;

    private static WPSConfiguration wpsConfiguration = null;

    /**
     *
     * @param wpService
     */
    public ExecuteRequestHandler( WPService wpService ) {
        this.wpService = wpService;
        // Get configuration from current wps instance
        wpsConfiguration = this.wpService.getConfiguration();
    }

    /**
     *
     * @param executeRequest
     * @return the response
     * @throws OGCWebServiceException
     */
    public ExecuteResponse handleRequest( ExecuteRequest executeRequest )
                            throws OGCWebServiceException {

        // Get the map of registered Processes from wps configuration
        Map<String, Process> registeredProcessesMap = wpsConfiguration.getRegisteredProcesses();

        // Get the identifier of the process to be executed from the
        // ExecuteRequest
        Code executeProcessIdentifier = executeRequest.getIdentifier();

        // Get the requested process from the registered processes
        Process process = registeredProcessesMap.get( executeProcessIdentifier.getCode().toUpperCase() );

        if ( null == process ) {

            // Unknown process was requested
            String msg = "A process with identifier '" + executeRequest.getIdentifier().getCode()
                         + "' is not known to this wpserver.";
            LOG.logDebug( msg );
            throw new InvalidParameterValueException( getClass().getName(), msg );
        }

        // Get request queue manager from configuration
        RequestQueueManager requestQueueManager = wpsConfiguration.getDeegreeParams().getRequestQueueManager();

        // add current request to queue
        boolean success = requestQueueManager.addRequestToQueue( executeRequest );

        Status status = null;

        if ( success ) {
            status = new Status();
            String msg = "Your execute request has been successfully added to the RequestQueue. The current length of the RequestQueue is "
                         + requestQueueManager.getLengthOfQueue();
            LOG.logDebug( msg );
            status.setProcessAccepted( msg );
        } else {
            String msg = "The server is too busy to accept and queue the request at this time.";
            LOG.logError( msg );
            throw new ServerBusyException( msg );
        }

        // TODO implement multiple threads handling
        if ( 0 < requestQueueManager.getLengthOfQueue() ) {
            executeRequest = requestQueueManager.getRequestFromQueue();
        }

        // Instantiate new execute response
        // this response will be populated during the following workflow
        ExecuteResponse executeResponse = new ExecuteResponse();

        // Set identifier in response according to requested process
        executeResponse.setIdentifier( process.getProcessDescription().getIdentifier() );

        // Add current status to response
        executeResponse.setStatus( status );

        // Set version in response according to configuration
        executeResponse.setVersion( wpsConfiguration.getVersion() );

        // Copy inputs from request to response
        ExecuteDataInputs executeDataInputs = executeRequest.getDataInputs();
        if ( null != executeDataInputs ) {
            executeResponse.setDataInputs( executeDataInputs );
        }

        // Copy outputdefinitions from request to response
        OutputDefinitions outputDefinitions = executeRequest.getOutputDefinitions();
        if ( null != outputDefinitions ) {
            executeResponse.setOutputDefinitions( outputDefinitions );
        }

        handleStoreParameter( process, executeRequest, executeResponse );

        return executeResponse;
    }

    /**
     *
     * @param process
     * @param executeRequest
     * @param executeResponse
     * @throws OGCWebServiceException
     */
    private static void handleStoreParameter( Process process, ExecuteRequest executeRequest,
                                              ExecuteResponse executeResponse )
                            throws OGCWebServiceException {

        boolean store = executeRequest.isStore();

        if ( store ) {

            // TODO store (optional) currently not supported
            // @see OGC 05-007r4 Table 27
            // @see OGC 05-007r4 Subclauses 10.3.1 and 10.3.2
            throw new InvalidParameterValueException( "store", "Store is not supported by this WPServer instance." );
            // handleStatusParameter(process, executeRequest, executeResponse);

        }

        // Get configured process outputs
        ProcessOutputs configuredProcessOutputs = process.getProcessDescription().getProcessOutputs();

        // Get list of outputdescriptions from configured process outputs
        List<OutputDescription> outputDescriptionsList = configuredProcessOutputs.getOutput();

        /*
         * @see OGC 05-007r4 Subclauses 10.3.1 and 10.3.2
         * @see OGC 05-007r4 Table 27: If the store parameter is false, there is only one output,
         *      and that output has a ComplexValue, then this ComplexValue shall be returned to the
         *      client outside of any ExecuteResponse document.
         *
         */
        if ( 1 == outputDescriptionsList.size() ) {
            OutputDescription outputDescription = outputDescriptionsList.get( 0 );

            ComplexData complexOutput = outputDescription.getComplexOutput();

            if ( null != complexOutput ) {
                executeResponse.setDirectResponse( true );
                startProcess( process, executeRequest, executeResponse );
            } else {
                handleStatusParameter( process, executeRequest, executeResponse );
            }
        } else {
            handleStatusParameter( process, executeRequest, executeResponse );
        }

    }

    /**
     *
     * @param process
     * @param executeRequest
     * @param executeResponse
     * @throws OGCWebServiceException
     */
    private static void handleStatusParameter( Process process, ExecuteRequest executeRequest,
                                               ExecuteResponse executeResponse )
                            throws OGCWebServiceException {

        boolean status = executeRequest.isStatus();

        if ( status ) {

            // TODO status currently not supported (optional)
            // @see OGC 05-007r4 Table 27
            // @see OGC 05-007r4 Subclauses 10.3.1 and 10.3.2

            throw new InvalidParameterValueException( "status", "Status is not supported by this WPServer instance." );

            // save passed response at web accessible location, get full path
            // and add it as statuslocation to response
            // saveExecuteResponse();
            // executeResponse.setStatusLocation(fullpath);

            // return response to client

            // startProcess(process, executeRequest, executeResponse);

            // update status to process started

            // updateSavedExecuteResponse();

        }
        startProcess( process, executeRequest, executeResponse );

    }

    /**
     *
     * @param process
     * @param executeRequest
     * @param executeResponse
     * @throws OGCWebServiceException
     */
    private static void startProcess( Process process, ExecuteRequest executeRequest, ExecuteResponse executeResponse )
                            throws OGCWebServiceException {

        // Extract map of provided inputs from request
        Map<String, IOValue> inputs = extractProcessInputs( process.getProcessDescription(), executeRequest );

        // Extract outputDefinitions from request
        OutputDefinitions outputDefinitions = executeRequest.getOutputDefinitions();

        ExecuteResponse.ProcessOutputs processOutputs = process.execute( inputs, outputDefinitions );

        if ( null != processOutputs ) {

            // Update status in response to successfull
            Status status = executeResponse.getStatus();
            status.setProcessSucceeded( "The " + process.getProcessDescription().getIdentifier().getCode()
                                        + " process has been successfully completed" );
            executeResponse.setStatus( status );

            // Add processOutputs to response
            executeResponse.setProcessOutputs( processOutputs );
        }
    }

    /**
     * Extract required process inputs from <code>ExecuteRequest</code>
     *
     * @param executeRequest
     * @return processInputs
     * @throws OGCWebServiceException
     */
    private static Map<String, IOValue> extractProcessInputs( ProcessDescription processDescription,
                                                              ExecuteRequest executeRequest )
                            throws OGCWebServiceException {

        // TODO if complexvaluereferences are included --> load data via a new
        // threaded method

        // Get inputs provided in executerequest
        ExecuteDataInputs executeDataInputs = executeRequest.getDataInputs();
        Map<String, IOValue> providedInputs = executeDataInputs.getInputs();

        // Prepare result map
        Map<String, IOValue> processInputs = null;

        // Get required process inputs from processDescription
        DataInputs configuredDataInputs = processDescription.getDataInputs();
        List<InputDescription> inputDescriptions = configuredDataInputs.getInputDescriptions();

        // Get inputDescription for each configured input
        int size = inputDescriptions.size();

        if ( 0 < size ) {
            processInputs = new HashMap<String, IOValue>( size );

            for ( int i = 0; i < size; i++ ) {

                InputDescription inputDescription = inputDescriptions.get( i );
                String identifier = inputDescription.getIdentifier().getCode();
                int minOccurs = inputDescription.getMinimumOccurs();

                IOValue ioValue = providedInputs.get( identifier );

                if ( null == ioValue && 1 == minOccurs ) {
                    throw new MissingParameterValueException( identifier, "A required process input is missing" );
                }

                if ( inputDescription.isBoundingBoxData() ) {
                    Envelope boundingBoxValue = ioValue.getBoundingBoxValue();
                    if ( null == boundingBoxValue ) {
                        throw new InvalidParameterValueException( identifier,
                                                                  "The type of the provided process input is wrong" );
                    }

                } else if ( inputDescription.isLiteralData() ) {
                    TypedLiteral literalValue = ioValue.getLiteralValue();
                    if ( null == literalValue ) {
                        throw new InvalidParameterValueException( identifier,
                                                                  "The type of the provided process input is wrong" );
                    }

                } else if ( inputDescription.isComplexData() ) {
                    ComplexValue complexValue = ioValue.getComplexValue();
                    ComplexValueReference complexValuereference = ioValue.getComplexValueReference();
                    if ( null == complexValue && null == complexValuereference ) {
                        throw new InvalidParameterValueException( identifier,
                                                                  "The type of the provided process input is wrong" );
                    }
                    // complexValuereference Implementation provided by Beate Stollberg
                    if ( complexValuereference != null ) {

                        try {
                            LOG.logInfo( "complexValuereference.reference.toString() "
                                         + complexValuereference.reference.toString() );
                            String sReference = complexValuereference.reference.toString();
                            int nIndexQuestionmark = sReference.indexOf( "?" );
                            LOG.logInfo( "nIndexQuestionmark " + nIndexQuestionmark );

                            FeatureCollection result = null;

                            if ( nIndexQuestionmark != -1 ) {
                                String[] asReference = new String[2];
                                asReference[0] = sReference.substring( 0, nIndexQuestionmark );
                                asReference[1] = sReference.substring( nIndexQuestionmark + 1 );
                                URL url = new URL( asReference[0] );
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                conn.setRequestMethod( "GET" );
                                conn.setUseCaches( false );
                                conn.setDoInput( true );
                                conn.setDoOutput( true );
                                DataOutputStream dos = new DataOutputStream( conn.getOutputStream() );
                                dos.writeBytes( asReference[1] );
                                dos.close();
                                InputStream in = conn.getInputStream();
                                InputStreamReader isr = new InputStreamReader( in );
                                GMLFeatureCollectionDocument doc = new GMLFeatureCollectionDocument();
                                doc.load( isr, url.toString() );
                                result = doc.parse();
                            } else {
                                GMLFeatureCollectionDocument doc = new GMLFeatureCollectionDocument();
                                doc.load( new URL( sReference ) );
                                result = doc.parse();
                            }
                            ComplexValue cv = new ComplexValue( complexValuereference.format,
                                                                complexValuereference.encoding,
                                                                complexValuereference.schema, result );
                            IOValue ioValueNew = new IOValue( ioValue.getIdentifier(), ioValue.getTitle(),
                                                              ioValue.getAbstract(), ioValue.getBoundingBoxValue(), cv,
                                                              null, null );
                            ioValue = ioValueNew;
                        } catch ( Exception e ) {
                            throw new OGCWebServiceException(
                                                              "Error occured while requesting ComplexData from ComplexValueReference! "
                                                                                      + e.getMessage() );
                        }
                    }
                }

                LOG.logDebug( "Found required process input '" + identifier + "' in execute request." );
                processInputs.put( identifier, ioValue );
            }
        }
        return processInputs;
    }
}
