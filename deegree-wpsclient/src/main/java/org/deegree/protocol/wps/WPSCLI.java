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

import org.deegree.protocol.wps.getcapabilities.WPSCapabilities;
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
     * @throws MalformedURLException in case the provided service URL is malformed
     */
    public static void main( String[] args )
                            throws Exception {

        // Construct a new WPSClient using a WPS capabilities url
        WPSClient100 client = new WPSClient100( new URL( FULL_SERVICE_URL ) );
        WPSCapabilities capabilities = client.getCapabilities();
        //ProcessInfo pInfo = client.getProcess( "Centroid" );
        
        capabilities.getOperationURLasString( "DescribeProcess" , true);
        
        

        // iterate over the process offerings of the service capabilities
        for ( int i = 0; i < capabilities.getProcessOfferings().size(); i++ ) {
            LOG.info( capabilities.getProcessOfferings().get( i ).getIdentifier() + ": "
                                + capabilities.getProcessOfferings().get( i ).getAbstract() );
        }        
        LOG.info( "WSDL: " + capabilities.getWSDL() );
        // TODO add more useful output here
    }
}
