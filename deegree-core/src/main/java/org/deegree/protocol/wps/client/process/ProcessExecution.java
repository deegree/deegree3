//$HeadURL$
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
package org.deegree.protocol.wps.client.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.protocol.ows.OWSExceptionReader;
import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.protocol.wps.WPSConstants;
import org.deegree.protocol.wps.WPSConstants.ExecutionState;
import org.deegree.protocol.wps.client.WPSClient;
import org.deegree.protocol.wps.client.output.type.OutputType;
import org.deegree.protocol.wps.client.process.execute.ExecutionOutputs;
import org.deegree.protocol.wps.client.process.execute.ExecutionResponse;
import org.deegree.protocol.wps.client.process.execute.OutputFormat;
import org.deegree.protocol.wps.client.process.execute.ResponseFormat;
import org.deegree.protocol.wps.client.wps100.ExecuteRequest100Writer;
import org.deegree.protocol.wps.client.wps100.ExecuteResponse100Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an execution context for a {@link Process} that uses the <code>ResponseDocument</code> output mode.
 * <p>
 * NOTE: This class is not thread-safe.
 * </p>
 * 
 * @see Process
 * @see RawProcessExecution
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ProcessExecution extends AbstractProcessExecution {

    private static Logger LOG = LoggerFactory.getLogger( ProcessExecution.class );

    private final List<OutputFormat> outputDefs = new ArrayList<OutputFormat>();

    private ResponseFormat responseFormat;

    private ExecutionResponse lastResponse;

    /**
     * Creates a new {@link ProcessExecution} instance.
     * 
     * @param client
     *            associated WPS client instance, must not be <code>null</code>
     * @param process
     *            associated process instance, must not be <code>null</code>
     */
    ProcessExecution( WPSClient client, Process process ) {
        super( client, process );
    }

    /**
     * Adds the specified parameter to the list of explicitly requested output parameters.
     * <p>
     * Calling this method sets the <code>ResponseForm</code> to <code>ResponseDocument</code>.
     * </p>
     * 
     * @param id
     *            identifier of the output parameter, must not be <code>null</code>
     * @param idCodeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (for identifiers without codespace)
     * @param uom
     *            requested unit of measure, may be <code>null</code> (indicates that the default mime type from the
     *            parameter description applies). This parameter only applies for literal outputs.
     * @param asRef
     *            if true, the output should be returned by the process as a reference, otherwise it will be embedded in
     *            the response document
     * @param mimeType
     *            requested mime type, may be <code>null</code> (indicates that the default mime type from the parameter
     *            description applies)
     * @param encoding
     *            requested encoding, may be <code>null</code> (indicates that the default encoding from the parameter
     *            description applies)
     * @param schema
     *            requested schema, may be <code>null</code> (indicates that the default schema from the parameter
     *            description applies)
     */
    public void addOutput( String id, String idCodeSpace, String uom, boolean asRef, String mimeType, String encoding,
                           String schema ) {
        outputDefs.add( new OutputFormat( new CodeType( id ), uom, asRef, mimeType, encoding, schema ) );
    }

    /**
     * Executes the process and returns the outputs.
     * 
     * @return process outputs, never <code>null</code>
     * @throws IOException
     *             if a communication/network problem occured
     * @throws OWSException
     *             if the server replied with an exception
     * @throws XMLStreamException
     */
    public ExecutionOutputs execute()
                            throws OWSException, IOException, XMLStreamException {

        lastResponse = sendExecute( false );
        OWSException report = lastResponse.getStatus().getExceptionReport();
        if ( report != null ) {
            throw report;
        }
        return lastResponse.getOutputs();
    }

    /**
     * Executes the process asynchronously.
     * <p>
     * This method issues the <code>Execute</code> request against the server and returns immediately.
     * </p>
     * 
     * @throws IOException
     *             if a communication/network problem occured
     * @throws OWSException
     *             if the server replied with an exception
     * @throws XMLStreamException
     */
    public void executeAsync()
                            throws IOException, OWSException, XMLStreamException {

        // needed, because ResponseDocument must be set in any case for async mode
        if ( outputDefs.isEmpty() ) {
            for ( OutputType output : process.getOutputTypes() ) {
                OutputFormat outputDef = new OutputFormat( output.getId(), null, false, null, null, null );
                outputDefs.add( outputDef );
            }
        }
        lastResponse = sendExecute( true );
    }

    /**
     * Returns the outputs of the process execution.
     * 
     * @return the outputs of the process execution, or <code>null</code> if the current state is not
     *         {@link ExecutionState#SUCCEEDED}
     * @throws OWSException
     *             if the server replied with an exception
     */
    public ExecutionOutputs getOutputs()
                            throws OWSException {
        if ( lastResponse == null ) {
            return null;
        }
        OWSException report = lastResponse.getStatus().getExceptionReport();
        if ( report != null ) {
            throw report;
        }
        return lastResponse.getOutputs();
    }

    /**
     * Returns the current state of the execution.
     * 
     * @return state of the execution, or <code>null</code> if the execution has not been started yet
     * @throws IOException
     *             if a communication/network problem occured
     * @throws OWSException
     *             if the server replied with an exception
     * @throws XMLStreamException
     */
    public ExecutionState getState()
                            throws OWSException, IOException, XMLStreamException {
        if ( lastResponse == null ) {
            return null;
        }
        if ( lastResponse.getStatus().getState() != ExecutionState.SUCCEEDED
             && lastResponse.getStatus().getState() != ExecutionState.FAILED ) {
            URL statusLocation = lastResponse.getStatusLocation();
            if ( statusLocation == null ) {
                throw new RuntimeException( "Cannot update status. No statusLocation provided." );
            }
            LOG.debug( "Polling response document from status location: " + statusLocation );
            XMLInputFactory inFactory = XMLInputFactory.newInstance();
            InputStream is = statusLocation.openStream();
            XMLStreamReader xmlReader = inFactory.createXMLStreamReader( is );
            StAXParsingHelper.nextElement( xmlReader );
            if ( OWSExceptionReader.isException( xmlReader ) ) {
                throw OWSExceptionReader.parseException( xmlReader );
            }
            ExecuteResponse100Reader reader = new ExecuteResponse100Reader( xmlReader );
            lastResponse = reader.parse100();
        }
        return lastResponse.getStatus().getState();
    }

    /**
     * Returns the status message.
     * 
     * @return status message, or <code>null</code> if the execution has not been started yet or no status message
     *         available
     */
    public String getStatusMessage() {
        if ( lastResponse == null ) {
            return null;
        }
        return lastResponse.getStatus().getStatusMessage();
    }

    /**
     * Returns the web-accessible URL for retrieving the execute response.
     * <p>
     * For asynchronous operation, this URL may provide access to a dynamic document that's changing until the process
     * is finished.
     * </p>
     * 
     * @return web-accessible URL, or <code>null</code> if the execution has not been started yet or no status location
     *         is available
     */
    public URL getStatusLocation() {
        if ( lastResponse == null ) {
            return null;
        }
        return lastResponse.getStatusLocation();
    }

    /**
     * Returns the percentage of the process that has been completed.
     * 
     * @return the completed percentage of the process, or <code>null</code> if the execution has not been started yet
     *         or no completion percentage provided by the process
     */
    public Integer getPercentCompleted() {
        if ( lastResponse == null ) {
            return null;
        }
        return lastResponse.getStatus().getPercentCompleted();
    }

    /**
     * Returns the creation time for the process execution as reported by the server.
     * 
     * @return creation time, or <code>null</code> if the execution has not been started yet
     */
    public String getCreationTime() {
        if ( lastResponse == null ) {
            return null;
        }
        return lastResponse.getStatus().getCreationTime();
    }

    /**
     * Returns the exception report.
     * <p>
     * NOTE: An exception report is only available if state is {@link ExecutionState#FAILED}.
     * </p>
     * 
     * @return an exception report in case the execution failed, <code>null</code> otherwise
     */
    public OWSException getExceptionReport() {
        if ( lastResponse == null ) {
            return null;
        }
        return lastResponse.getStatus().getExceptionReport();
    }

    private ExecutionResponse sendExecute( boolean async )
                            throws OWSException, XMLStreamException, IOException {

        responseFormat = new ResponseFormat( false, async, false, async, outputDefs );

        // TODO what if server only supports Get?
        URL url = client.getExecuteURL( true );

        URLConnection conn = url.openConnection();
        conn.setDoOutput( true );
        conn.setUseCaches( false );
        // TODO does this need configurability?
        conn.setRequestProperty( "Content-Type", "application/xml" );

        XMLOutputFactory outFactory = XMLOutputFactory.newInstance();

        OutputStream os = conn.getOutputStream();
        XMLInputFactory inFactory = XMLInputFactory.newInstance();

        if ( LOG.isDebugEnabled() ) {
            File logFile = File.createTempFile( "wpsclient", "request.xml" );
            XMLStreamWriter logWriter = outFactory.createXMLStreamWriter( new FileOutputStream( logFile ), "UTF-8" );
            ExecuteRequest100Writer executer = new ExecuteRequest100Writer( logWriter );
            executer.write100( process.getId(), inputs, responseFormat );
            logWriter.close();
            LOG.debug( "WPS request can be found at " + logFile );

            InputStream is = new FileInputStream( logFile );
            byte[] buffer = new byte[1024];
            int read = 0;
            while ( ( read = is.read( buffer ) ) != -1 ) {
                os.write( buffer, 0, read );
            }
            is.close();
            os.close();
        } else {
            XMLStreamWriter writer = outFactory.createXMLStreamWriter( os );
            ExecuteRequest100Writer executer = new ExecuteRequest100Writer( writer );
            executer.write100( process.getId(), inputs, responseFormat );
            writer.close();
        }

        InputStream responseStream = conn.getInputStream();

        if ( LOG.isDebugEnabled() ) {
            File logFile = File.createTempFile( "wpsclient", "response" );
            OutputStream logStream = new FileOutputStream( logFile );

            byte[] buffer = new byte[1024];
            int read = 0;
            while ( ( read = responseStream.read( buffer ) ) != -1 ) {
                logStream.write( buffer, 0, read );
            }
            logStream.close();

            responseStream = new FileInputStream( logFile );
            LOG.debug( "WPS response can be found at " + logFile );
        }

        // String outputContent = conn.getContentType();
        // TODO determine XML reader encoding based on mime type
        XMLStreamReader reader = inFactory.createXMLStreamReader( responseStream );
        StAXParsingHelper.nextElement( reader );
        if ( OWSExceptionReader.isException( reader ) ) {
            throw OWSExceptionReader.parseException( reader );
        }
        if ( new QName( WPSConstants.WPS_100_NS, "ExecuteResponse" ).equals( reader.getName() ) ) {
            ExecuteResponse100Reader responseReader = new ExecuteResponse100Reader( reader );
            lastResponse = responseReader.parse100();
            reader.close();

        } else {
            throw new RuntimeException( "Unexpected Execute response: root element is '" + reader.getName() + "'" );
        }
        return lastResponse;
    }
}