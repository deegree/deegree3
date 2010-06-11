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
import java.util.ArrayList;
import java.util.List;

import org.deegree.protocol.wps.describeprocess.DescribeProcess;
import org.deegree.protocol.wps.describeprocess.ProcessDescription;
import org.deegree.protocol.wps.execute.ExecuteResponse;
import org.deegree.protocol.wps.getcapabilities.WPSCapabilities;
import org.deegree.protocol.wps.tools.BuildExecuteObjects;
import org.deegree.protocol.wps.tools.InputObject;
import org.deegree.protocol.wps.tools.LoadFile;
import org.deegree.protocol.wps.tools.OutputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command line client (CLI) tool to access org.deegree.protocol.wps.WPSClient
 * 
 * @author <a href="mailto:kiehle@lat-lon.de">Christian Kiehle</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WPSCLI {

    private static Logger LOG = LoggerFactory.getLogger( WPSCLI.class );

    private static final String FULL_SERVICE_URL = "http://ows7.lat-lon.de/d3WPS_JTS/services?service=WPS&version=1.0.0&request=GetCapabilities";

    private static final String BASE_URL = "http://ows7.lat-lon.de/d3WPS_JTS/services?";

    /**
     * 
     * @param args
     * @throws MalformedURLException
     *             in case the provided service URL is malformed
     */
    public static void main( String[] args )
                            throws Exception {

        // Construct a new WPSClient using a WPS capabilities url
        WPSClient100 client = new WPSClient100( new URL( FULL_SERVICE_URL ) );
        WPSCapabilities capabilities = client.getCapabilities();
        // ProcessInfo pInfo = client.getProcess( "Centroid" );

        capabilities.getOperationURLasString( "DescribeProcess", true );

        DescribeProcess dP = new DescribeProcess(
                                                  new URL(
                                                           BASE_URL
                                                                                   + "service=WPS&version=1.0.0&request=DescribeProcess&IDENTIFIER=Buffer" ) );

        ProcessDescription processDescription = new ProcessDescription();
        processDescription = dP.getProcessDescriptions().get( 0 );

        // String
        // input="http://sigma.openplans.org/geoserver/ows?service=WFS&request=GetFeature&typename=tiger:poi&namespace=xmlns%28tiger=http://sigma.openplans.org/tiger%29&outputformat=text%2Fxml%3B+subtype%3Dgml%2F3.1.1&FILTER=%28%3CFilter%20xmlns:tiger=%22http://sigma.openplans.org/tiger%22%3E%3CPropertyIsEqualTo%3E%3CPropertyName%3Etiger:objectid%3/PropertyName%3E%3CLiteral%3E3%3C/Literal%3E%3C/PropertyIsEqualTo%3E%3C/Filter%3E%29";

        LoadFile loadFile = new LoadFile( "curve.xml" );
        String input = loadFile.load();

        ProcessExecution processExecution = new ProcessExecution( processDescription, BASE_URL );
        processExecution.addInput( "GMLInput", input, false );
        processExecution.addInput( "BufferDistance", "23", false );

        ExecuteResponse executeResponse = new ExecuteResponse( processExecution.sendExecuteRequest() );

    }

}
