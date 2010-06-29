//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.protocol.wps;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.wps.describeprocess.ProcessDescription;
import org.deegree.protocol.wps.execute.Execute;
import org.deegree.protocol.wps.execute.ExecuteResponse;
import org.deegree.protocol.wps.execute.Output;
import org.deegree.protocol.wps.execute.ProcessOutputs;
import org.deegree.protocol.wps.tools.BuildExecuteObjects;
import org.deegree.protocol.wps.tools.InputObject;
import org.deegree.protocol.wps.tools.OutputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * ProcessExecution provides WPS process execution information
 * 
 * TODO impelement me!
 * 
 * @author <a href="mailto:walenciak@uni-heidelberg.de">Georg Walenciak</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ProcessExecution {

    private OutputConfiguration outputConfiguration;

    private InputObject inputObject;

    private List<InputObject> inputObjectList = new ArrayList();

    private List<OutputConfiguration> outputConfigurationList = new ArrayList();

    private ProcessDescription processDescription;

    private String executeRequestString;

    private String baseURL;

    private static Logger LOG = LoggerFactory.getLogger( ProcessExecution.class );

    public ProcessExecution( ProcessDescription processDescription, String baseURL ) {
        this.processDescription = processDescription;
        this.baseURL = baseURL;
    }

    /**
     * 
     * @return percentage completed
     */
    public int getPercentComplete() {
        // TODO impelement me!
        return -1;
    }

    /**
     * 
     * @return outputParams
     */
    public Object[] getOutputParams() {
        // TODO impelement me!
        return null;
    }

    public void addInput( String identifier, String input, boolean asRef ) {
        InputObject inputObject = new InputObject( identifier, input, asRef );
        this.inputObject = inputObject;
        inputObjectList.add( inputObject );
    }

    public void addInput( InputObject inputObject ) {
        this.inputObject = inputObject;
        inputObjectList.add( inputObject );
    }

    public void addOutput( String identifier ) {
        OutputConfiguration outputConfiguration = new OutputConfiguration( "identifier" );
        this.outputConfiguration = outputConfiguration;
        outputConfigurationList.add( outputConfiguration );
    }

    public void addOutput( OutputConfiguration outputConfiguration ) {
        this.outputConfiguration = outputConfiguration;
        outputConfigurationList.add( outputConfiguration );
    }

    public XMLAdapter sendExecuteRequestXMLAdapterReturn() {

        BuildExecuteObjects buildExecuteObjects = new BuildExecuteObjects( inputObjectList, outputConfigurationList,
                                                                           processDescription );

        ByteArrayOutputStream byteArrayOutputStream = buildExecuteObjects.createExecuteRequest();

        XMLAdapter xmlAdapter = null;
        try {
            // Construct data
            String data = byteArrayOutputStream.toString();
            // Send data
            URL url = new URL( this.baseURL );
            URLConnection conn = url.openConnection();
            conn.setDoInput( true );

            conn.setDoOutput( true );
            conn.setUseCaches( false );
            conn.setRequestProperty( "Content-Type", "application/xml" );

            OutputStreamWriter wr = new OutputStreamWriter( conn.getOutputStream() );
            wr.write( data );
            wr.flush();

            // Get the response

            xmlAdapter = new XMLAdapter( conn.getInputStream() );

            wr.close();

        } catch ( Exception e ) {
        }

        return xmlAdapter;

    }

    public String sendExecuteRequestStringReturn() {

        LOG.info( "Sending execute request to: " + this.baseURL );

        BuildExecuteObjects buildExecuteObjects = new BuildExecuteObjects( inputObjectList, outputConfigurationList,
                                                                           processDescription );

        ByteArrayOutputStream byteArrayOutputStream = buildExecuteObjects.createExecuteRequest();

        String inResponse = "";
        try {
            // Construct data
            String data = byteArrayOutputStream.toString();
            // Send data
            URL url = new URL( this.baseURL );
            URLConnection conn = url.openConnection();
            conn.setDoInput( true );

            conn.setDoOutput( true );
            conn.setUseCaches( false );
            conn.setRequestProperty( "Content-Type", "application/xml" );

            OutputStreamWriter wr = new OutputStreamWriter( conn.getOutputStream() );
            wr.write( data );
            wr.flush();

            // Get the response

            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );

            String inLine;
            while ( ( inLine = bufferedReader.readLine() ) != null ) {
                inResponse = inResponse + inLine;
            }

            wr.close();

        } catch ( Exception e ) {
        }

        LOG.info( "Execute response received" );

        return inResponse;

    }

    public ExecuteResponse sendExecuteRequestExecuteResponseReturn() {

        LOG.info( "Sending execute request to: " + this.baseURL );

        BuildExecuteObjects buildExecuteObjects = new BuildExecuteObjects( inputObjectList, outputConfigurationList,
                                                                           processDescription );

        ByteArrayOutputStream byteArrayOutputStream = buildExecuteObjects.createExecuteRequest();

        XMLAdapter xmlAdapter = null;
        try {
            // Construct data
            String data = byteArrayOutputStream.toString();

            // Send data
            URL url = new URL( this.baseURL );
            URLConnection conn = url.openConnection();
            conn.setDoInput( true );

            conn.setDoOutput( true );
            conn.setUseCaches( false );
            conn.setRequestProperty( "Content-Type", "application/xml" );

            OutputStreamWriter wr = new OutputStreamWriter( conn.getOutputStream() );
            wr.write( data );
            wr.flush();

            // Get the response

            xmlAdapter = new XMLAdapter( conn.getInputStream() );

            wr.close();

        } catch ( Exception e ) {
        }

        ExecuteResponse executeResponse = new ExecuteResponse( xmlAdapter );

        LOG.info( "Execute response received" );

        return executeResponse;

    }

    public Object sendExecuteRequestExecuteObjectReturn() {

        LOG.info( "Sending execute request to: " + this.baseURL );

        BuildExecuteObjects buildExecuteObjects = new BuildExecuteObjects( inputObjectList, outputConfigurationList,
                                                                           processDescription );

        Object object = null;
        ByteArrayOutputStream byteArrayOutputStream = buildExecuteObjects.createExecuteRequest();

        XMLAdapter xmlAdapter = null;
        try {
            // Construct data
            String data = byteArrayOutputStream.toString();
            // Send data
            URL url = new URL( this.baseURL );
            URLConnection conn = url.openConnection();
            conn.setDoInput( true );

            conn.setDoOutput( true );
            conn.setUseCaches( false );
            conn.setRequestProperty( "Content-Type", "application/xml" );

            OutputStreamWriter wr = new OutputStreamWriter( conn.getOutputStream() );
            wr.write( data );
            wr.flush();

            // Get the response

            xmlAdapter = new XMLAdapter( conn.getInputStream() );

            wr.close();

        } catch ( Exception e ) {
        }

        ExecuteResponse executeResponse = new ExecuteResponse( xmlAdapter );
        if ( executeResponse.getProcessOutputs().getOutputs() != null ) {

            if ( executeResponse.getProcessOutputs().getOutputs().get( 0 ).getOutputReference() != null )
                if ( executeResponse.getProcessOutputs().getOutputs().get( 0 ).getOutputReference().getHref() != null )
                    object = executeResponse.getProcessOutputs().getOutputs().get( 0 ).getOutputReference().getHref();
            if ( executeResponse.getProcessOutputs().getOutputs().get( 0 ).getDataType() != null ) {
                if ( executeResponse.getProcessOutputs().getOutputs().get( 0 ).getDataType().getComplexData().getObject() != null )
                    object = executeResponse.getProcessOutputs().getOutputs().get( 0 ).getDataType().getComplexData().getObject();

                if ( executeResponse.getProcessOutputs().getOutputs().get( 0 ).getDataType().getLiteralData() != null )
                    if ( executeResponse.getProcessOutputs().getOutputs().get( 0 ).getDataType().getLiteralData().getLiteralData() != null )
                        object = executeResponse.getProcessOutputs().getOutputs().get( 0 ).getDataType().getLiteralData().getLiteralData();
            }
        }

        LOG.info( "Execute response received" );

        return object;
    }

    public List<Output> sendExecuteRequestOutputList() {

        LOG.info( "Sending execute request to: " + this.baseURL );

        BuildExecuteObjects buildExecuteObjects = new BuildExecuteObjects( inputObjectList, outputConfigurationList,
                                                                           processDescription );

        Object object = null;
        ByteArrayOutputStream byteArrayOutputStream = buildExecuteObjects.createExecuteRequest();

        XMLAdapter xmlAdapter = null;
        try {
            // Construct data
            String data = byteArrayOutputStream.toString();
            // Send data
            URL url = new URL( this.baseURL );
            URLConnection conn = url.openConnection();
            conn.setDoInput( true );

            conn.setDoOutput( true );
            conn.setUseCaches( false );
            conn.setRequestProperty( "Content-Type", "application/xml" );

            OutputStreamWriter wr = new OutputStreamWriter( conn.getOutputStream() );
            wr.write( data );
            wr.flush();

            // Get the response

            xmlAdapter = new XMLAdapter( conn.getInputStream() );

            wr.close();

        } catch ( Exception e ) {
        }

        ExecuteResponse executeResponse = new ExecuteResponse( xmlAdapter );

        List<Output> outputs = executeResponse.getProcessOutputs().getOutputs();

        LOG.info( "Execute response received" );

        return outputs;
    }

    public String returnExecuteRequest() {

        BuildExecuteObjects buildExecuteObjects = new BuildExecuteObjects( inputObjectList, outputConfigurationList,
                                                                           processDescription );
        String request = buildExecuteObjects.createExecuteRequest().toString();

        return request;
    }

}
