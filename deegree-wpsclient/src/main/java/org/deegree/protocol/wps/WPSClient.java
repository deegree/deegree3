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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.wps.describeprocess.DescribeProcess;
import org.deegree.protocol.wps.getcapabilities.ProcessOffering;
import org.deegree.protocol.wps.getcapabilities.WPSCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WPSClient provides an abstraction layer to access a Web Processing Service (WPS). It may be invoced from a command
 * line tool or from a web application
 * 
 * @author <a href="mailto:kiehle@lat-lon.de">Christian Kiehle</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WPSClient {

    private static Logger LOG = LoggerFactory.getLogger( WPSClient.class );

    private WPSCapabilities serviceCapabilities;

    private Map<String, ProcessInfo> processIdToProcess = new HashMap<String, ProcessInfo>();
   

    /**
     * Public constructor to access a WPS instance based on it's GetCapabilities URL
     * 
     * @param capabilitiesURL url to a WPS instance
     *            
     */
    public WPSClient( URL capabilitiesURL ) {        
        LOG.debug( "Creating WPSClient for URL " + capabilitiesURL );        
        populateOffering(capabilitiesURL);
    }
    
    /**
     * Public constructor to access a WPS instance based on it's base URL
     * @param serviceBaseURL baseURL, e.g. http://foo.bar/service?
     * @param version OGC service version  number, e.g. 1.0.0
     * @param usePost true in case the request should be send by HTTP POST
     * @throws MalformedURLException 
     */
    public WPSClient( URL serviceBaseURL, String version, boolean usePost) throws Exception {
        if (usePost){
            LOG.debug( "Capabilities should be transmitted through HTTP Post");
            // TODO implement
            throw new Exception("GetCapabilities by Post currently not supported");
        }
        StringBuilder parameterString = new StringBuilder(serviceBaseURL.toString());
        //TODO check if question mark is included in url
        parameterString.append( "service=WPS");
        parameterString.append( "&request=GetCapabilities" );
        if (version != null && !"".equals( version )){            
            parameterString.append( "&Version=" );
            parameterString.append( version );  
        //TODO check if version is well-formed
        }        
        LOG.debug( parameterString.toString());
        URL serviceCapabilities = new URL(parameterString.toString());        
        populateOffering(serviceCapabilities);        
    }
    

    /**
     * populates the Process Offering based on the GetCapabilities Response
     * @param capabilitiesURL URL to GetCapabilities interface
     */
    private void populateOffering(URL capabilitiesURL) {
        serviceCapabilities = new WPSCapabilities( new XMLAdapter( capabilitiesURL ) );
        // TODO populate map on demand (much faster for WPS with lots of processes)
        for ( ProcessOffering offering : serviceCapabilities.getProcessOfferings() ) {
            // fetch full metadata (params) using DescribeProcess
            ProcessInfo info = fetchProcessInfo( offering.getIdentifier());
            processIdToProcess.put( offering.getIdentifier(), info );
        }
    }

    /**
     * 
     * @return the capabilities of a service
     */
    public WPSCapabilities getCapabilities() {
        return serviceCapabilities;
    }

    /**
     * 
     * @return ProcessInfo[] containing ProcessInfo elements
     */
    public ProcessInfo[] getProcesses() {
        return processIdToProcess.values().toArray( new ProcessInfo[processIdToProcess.size()] );
    }

    /**
     * 
     * @param id
     *            process identifier
     * @return ProcessInfo object containing all relevant process information
     */
    public ProcessInfo getProcess( String id ) {
        return processIdToProcess.get( id );
    }

    /**
     * 
     * @param processIdentifier
     * @return ProcessInfo object containing all relevant process information
     */
    private ProcessInfo fetchProcessInfo( String processIdentifier ) {
        ProcessInfo processInfo = new ProcessInfo(processIdentifier);   
        //URL operationsURL = this.serviceCapabilities.getOperationsMetadata().getOperationByName( "DescribeProcess" ).getDcp().getHttp().getGet().getUrl();
        //since implementation of above methods is missing, set it statically here for testing
        // TODO replace implementation
        URL operationsURL = null;
        StringBuilder sb = new StringBuilder("http://ows7.lat-lon.de/d3WPS_JTS/services?service=WPS&version=1.0.0&request=DescribeProcess&identifier=");
        sb.append( processIdentifier );
        try {
            operationsURL = new URL(sb.toString());
        } catch ( MalformedURLException e ) {
            LOG.error( e.getMessage() );
        }
        DescribeProcess dp = new DescribeProcess( operationsURL );
        LOG.debug( "DataInputs[0]:" + dp.getProcessDescriptions().get( 0 ).getDataInputs().get(0) );
        return processInfo;
    }
}
