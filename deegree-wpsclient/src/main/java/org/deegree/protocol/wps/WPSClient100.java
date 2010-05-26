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

import static org.deegree.protocol.i18n.Messages.get;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.wps.describeprocess.DescribeProcess;
import org.deegree.protocol.wps.getcapabilities.ProcessOffering;
import org.deegree.protocol.wps.getcapabilities.WPSCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WPSClient provides an abstraction layer to access a Web Processing Service (WPS). It may be invoked from a command
 * line tool or from a web application
 * 
 * TODO Enhance Exception handling
 * 
 * @author <a href="mailto:kiehle@lat-lon.de">Christian Kiehle</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WPSClient100 {

    private static Logger LOG = LoggerFactory.getLogger( WPSClient100.class );

    private WPSCapabilities serviceCapabilities;

    private Map<String, ProcessInfo> processIdToProcess = new HashMap<String, ProcessInfo>();

    private XMLAdapter capabilitesDoc;

    /**
     * Public constructor to access a WPS instance based on it's GetCapabilities URL
     * 
     * @param capabilitiesURL
     *            url to a WPS instance
     * @throws MalformedURLException
     *             in case a DescribeProcess URL could not constructed from WPS Capabilities response
     * 
     */
    public WPSClient100( URL capabilitiesURL ) throws Exception {
        try {
            this.capabilitesDoc = new XMLAdapter( capabilitiesURL );
        } catch ( Exception e ) {
            LOG.error( e.getLocalizedMessage(), e );
            throw new NullPointerException( "Could not read from URL: " + capabilitiesURL + " error was: "
                                            + e.getLocalizedMessage() );
        }
        checkCapabilities( this.capabilitesDoc );
        populateOffering( capabilitiesURL );
    }

    /**
     * Public constructor to access a WPS instance based on it's capabilities XML Document
     * 
     * @param capabilitesDoc
     */
    public WPSClient100( XMLAdapter capabilitesDoc ) {
        checkCapabilities( capabilitesDoc );
        this.capabilitesDoc = capabilitesDoc;
    }

    /**
     * Basic check for correctness of most important WPS capabilities document values. TODO enhance!
     * 
     * @param capabilities
     */
    private void checkCapabilities( XMLAdapter capabilities ) {
        OMElement root = capabilities.getRootElement();
        String version = root.getAttributeValue( new QName( "version" ) );
        if ( !"1.0.0".equals( version ) ) {
            throw new IllegalArgumentException( get( "WPSCLIENT.WRONG_VERSION_CAPABILITIES", version, "1.0.0" ) );
        }
        String service = root.getAttributeValue( new QName( "service" ) );
        if ( !service.equalsIgnoreCase( "WPS" ) ) {
            throw new IllegalArgumentException( get( "WPSCLIENT.NO_WPS_CAPABILITIES", service, "WPS" ) );
        }
    }

    /**
     * Public constructor to access a WPS instance based on it's base URL
     * 
     * @param serviceBaseURL
     *            baseURL, e.g. http://foo.bar/service?
     * @param version
     *            OGC service version number, e.g. 1.0.0
     * @param get
     *            true means HTTP GET, false means HTTP POST
     * @throws MalformedURLException
     *             MalformedURLException in case a DescribeProcess URL could not constructed from WPS Capabilities
     *             response
     */
    public WPSClient100( URL serviceBaseURL, String version, boolean get ) throws Exception {
        if ( !get ) {
            LOG.debug( "Capabilities should be transmitted through HTTP Post" );
            // TODO implement
            throw new Exception( "GetCapabilities by Post currently not supported" );
        }
        StringBuilder parameterString = new StringBuilder( serviceBaseURL.toString() );
        if ( !serviceBaseURL.toString().endsWith( "?" ) ) {
            parameterString.append( "?" );
        }
        parameterString.append( "service=WPS" );
        parameterString.append( "&request=GetCapabilities" );
        if ( version != null && !"".equals( version ) ) {
            parameterString.append( "&Version=" );
            parameterString.append( version );
            // TODO check if version is well-formed
        }
        LOG.debug( parameterString.toString() );
        URL serviceCapabilities = new URL( parameterString.toString() );
        populateOffering( serviceCapabilities );
    }

    /**
     * populates the Process Offering based on the GetCapabilities Response
     * 
     * @param capabilitiesURL
     *            URL to GetCapabilities interface
     */
    private void populateOffering( URL capabilitiesURL )
                            throws Exception {
        serviceCapabilities = new WPSCapabilities( new XMLAdapter( capabilitiesURL ) );
        // TODO populate map on demand (much faster for WPS with lots of processes)
        for ( ProcessOffering offering : serviceCapabilities.getProcessOfferings() ) {
            // fetch full metadata (params) using DescribeProcess
            ProcessInfo info = fetchProcessInfo( offering.getIdentifier() );
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
    private ProcessInfo fetchProcessInfo( String processIdentifier )
                            throws Exception {
        ProcessInfo processInfo = new ProcessInfo( processIdentifier );
        // URL operationsURL = this.serviceCapabilities.getOperationsMetadata().getOperationByName( "DescribeProcess"
        // ).getDcp().getHttp().getGet().getUrl();
        // since implementation of above methods is missing, set it statically here for testing
        // TODO replace implementation
        URL operationsURL = null;
        StringBuilder sb = new StringBuilder( serviceCapabilities.getOperationURLasString( "DescribeProcess", true ) );
        sb.append( "request=DescribeProcess&Version=1.0.0&identifier=" );
        sb.append( processIdentifier );
        operationsURL = new URL( sb.toString() );
        DescribeProcess dp = new DescribeProcess( operationsURL );
        LOG.debug( "DataInputs[0]:" + dp.getProcessDescriptions().get( 0 ).getDataInputs().get( 0 ) );
        return processInfo;
    }

}
