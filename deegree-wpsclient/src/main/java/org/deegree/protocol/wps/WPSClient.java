//$HeadURL: svn+ssh://georg@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.wps.execute.ExecuteResponse;
import org.deegree.protocol.wps.getcapabilities.ProcessBrief;
import org.deegree.protocol.wps.getcapabilities.WPSCapabilities;
import org.deegree.protocol.wps.tools.InputObject;
import org.deegree.protocol.wps.tools.LoadFile;
import org.deegree.protocol.wps.tools.OutputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:walenciak@uni-heidelberg.de">Georg Walenciak</a>
 * @author last edited by: $Author: walenciak $
 * 
 * @version $Revision: $, $Date: $
 */
public class WPSClient {

    private static Logger LOG = LoggerFactory.getLogger( WPSClient.class );

    private final WPSCapabilities serviceCapabilities;

    private final Map<String, ProcessInfo> processIdToProcess = new HashMap<String, ProcessInfo>();

    private String executeURL;

    private String describeProcessURL;


    /**
     * Public constructor to access a WPS instance based on it's GetCapabilities URL
     * 
     * @param capabilitiesURL
     *            url to a WPS instance
     * @throws MalformedURLException
     *             in case a GetCapabilities URL could not constructed from WPS Capabilities response
     */
    public WPSClient( URL capabilitiesURL ) throws MalformedURLException {
        try {
            serviceCapabilities = new WPSCapabilities( new XMLAdapter( capabilitiesURL ) );
            describeProcessURL = serviceCapabilities.getOperationURLasString( "DescribeProcess", true );
            if ( !describeProcessURL.endsWith( "?" ) ) {
                describeProcessURL += "?";
            }
            LOG.debug( "Using '" + describeProcessURL + "' for DescribeProcess requests (GET)." );
            executeURL = serviceCapabilities.getOperationURLasString( "Execute", false );
            if ( executeURL.endsWith( "?" ) ) {
                executeURL = executeURL.substring( 0, executeURL.length() - 1 );
            }
            LOG.debug( "Using '" + executeURL + "' for Execute requests (POST)." );
        } catch ( Exception e ) {
            LOG.error( e.getLocalizedMessage(), e );
            throw new NullPointerException( "Could not read from URL: " + capabilitiesURL + " error was: "
                                            + e.getLocalizedMessage() );
        }
    }

    /**
     * Returns the identifiers of all processes known to the WPS instance.
     * 
     * @return identifiers of all processes known to the WPS instance, never <code>null</code> 
     */
    public String[] getProcessIdentifiers() {
        
        
        List<ProcessBrief> processBriefList=this.serviceCapabilities.getProcessOfferings();

        int size = processBriefList.size();
        String[] identifier = new String[size];

        
        
        for ( int i = 0; i < size; i++ ) {
            identifier[i] = processBriefList.get( i ).getIdentifier();
        }
        return identifier;
    }

    /**
     * Returns the the process information for the process with the given identifier.
     * 
     * @param processId
     *            identifier of the process, must not be <code>null</code>
     * @return process information
     */
    public ProcessInfo getProcessInfo( String processId ) {
        ProcessInfo pi = null;
        synchronized ( processIdToProcess ) {
            pi = processIdToProcess.get( processId );
            if ( pi == null ) {
                pi = new ProcessInfo( describeProcessURL, processId );
            }
            processIdToProcess.put( processId, pi );
        }
        return pi;
    }

    /**
     * 
     * @return String response of the executeRequest as a String
     * 
     * @param inputObject[]
     *            
     * @param outputConfiguration[]
     * 
     * @param processIdentifier
     *            identifier of the process
     * 
     */
    public String executeProcessStringResult( InputObject[] inputobject, OutputConfiguration[] outputConfiguration, String processIdentifier ) {

        ProcessExecution processExecution = new ProcessExecution(
                                                                  getProcessInfo( processIdentifier ).getProcessDescription(),
                                                                  executeURL );

        for ( int i = 0; i < inputobject.length; i++ ) {
            processExecution.addInput(inputobject[i] );
        }
        
        for ( int i = 0; i < outputConfiguration.length; i++ ) {
            processExecution.addOutput(outputConfiguration[i]);
        }

        return processExecution.sendExecuteRequestStringReturn();
    }

    /**
     * 
     * @return Object result the of process as object
     * 
     * @param inputObject[]
     *            
     * @param outputConfiguration[]
     * 
     * @param processIdentifier
     *            identifier of the process
     * 
     */
    public Object executeProcessObejctResult( InputObject[] inputobject,OutputConfiguration[] outputConfiguration, String processIdentifier ) {

        ProcessExecution processExecution = new ProcessExecution(
                                                                  getProcessInfo( processIdentifier ).getProcessDescription(),
                                                                  executeURL );
        if (inputobject!=null)
        for ( int i = 0; i < inputobject.length; i++ ) {
            processExecution.addInput( inputobject[i] );
        }
        
        if (outputConfiguration!=null)
        for ( int i = 0; i < outputConfiguration.length; i++ ) {
            processExecution.addOutput(outputConfiguration[i]);
        }

        return processExecution.sendExecuteRequestExecuteObjectReturn();
    }

    /**
     * 
     * @return Object result the of process as ExecuteResponse object
     * 
     * @param InputObject
     *            [] Input of the process
     *            
     *@param outputConfiguration[]
     * 
     * @param processIdentifier
     *            identifier of the process
     *            
     *            
     * 
     */
    public ExecuteResponse executeProcessExecuteResponseResult( InputObject[] inputobject, OutputConfiguration[] outputConfiguration, String processIdentifier ) {

        ProcessExecution processExecution = new ProcessExecution(
                                                                  getProcessInfo( processIdentifier ).getProcessDescription(),
                                                                  describeProcessURL );
        if (inputobject!=null)
        for ( int i = 0; i < inputobject.length; i++ ) {
            processExecution.addInput(inputobject[i] );
        }

        if (outputConfiguration!=null)
        for ( int i = 0; i < outputConfiguration.length; i++ ) {
            processExecution.addOutput(outputConfiguration[i]);
        }
        return processExecution.sendExecuteRequestExecuteResponseReturn();
    }
    
    
    /**
     * 
     * @return Object result the of process as ExecuteResponse object
     * 
     * @param InputObject
     *            [] Input of the process
     *            
     *@param outputConfiguration[]
     * 
     * @param processIdentifier
     *            identifier of the process
     *            
     * 
     */
    public XMLAdapter executeProcessXMLAdapterResult( InputObject[] inputobject, OutputConfiguration[] outputConfiguration, String processIdentifier ) {

        ProcessExecution processExecution = new ProcessExecution(
                                                                  getProcessInfo( processIdentifier ).getProcessDescription(),
                                                                  describeProcessURL );

        for ( int i = 0; i < inputobject.length; i++ ) {
            processExecution.addInput(inputobject[i] );
        }
        
        for ( int i = 0; i < outputConfiguration.length; i++ ) {
            processExecution.addOutput(outputConfiguration[i]);
        }

        return processExecution.sendExecuteRequestXMLAdapterReturn();
    }
    
    

    /**
     * 
     * @return InputObject input of the process
     * 
     * @param identifier
     *            identifier of the input
     * 
     * @param input
     * 
     */
    public InputObject setInputasObject( String identifier, Object input ) {

        InputObject inputObject = new InputObject( identifier, input, false );

        return inputObject;
    }

    /**
     * 
     * @return InputObject input of the process
     * 
     * @param identifier
     *            identifier of the input
     * 
     * @param filePath
     *            path to the file of the input
     * 
     */
    public InputObject setInputasFile( String identifier, String filePath )
                            throws Exception {
        LoadFile loadFile = new LoadFile( filePath );
        Object object = loadFile.load();

        InputObject inputObject = new InputObject( identifier, object, false );
        return inputObject;
    }

    /**
     * 
     * @return InputObject input of the process
     * 
     * @param identifier
     *            identifier of the input
     * 
     * @param url
     *            of the input
     * 
     */
    public InputObject setInputasURL( String identifier, String url ) {

        InputObject inputObject = new InputObject( identifier, url, true );
        return inputObject;
    }

    public OutputConfiguration setOutputConfiguration(String identifier) {
        OutputConfiguration outputConfiguration = new OutputConfiguration(identifier);
        outputConfiguration.setAsReference( true );
        return outputConfiguration;
        

    }
}
