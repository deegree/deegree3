//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.protocol.wfs.client;

import java.net.URL;

import javax.xml.namespace.QName;

import org.deegree.commons.utils.test.TestProperties;
import org.deegree.feature.Feature;
import org.deegree.feature.StreamFeatureCollection;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.util.Assert;

/**
 * Tests the {@link WFSClient} against various WFS server instances.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WFSClientTest {

    private static Logger LOG = LoggerFactory.getLogger( WFSClientTest.class );

    private static final String WFS_UTAH_DEMO_100_URL = "wfs.utahdemo100.url";
    
    private static final String WFS_UTAH_DEMO_110_URL = "wfs.utahdemo110.url";

    @Test
    public void testGetCapabilities100Utah()
                            throws Exception {

        String wfsUtahDemo100Url = TestProperties.getProperty( WFS_UTAH_DEMO_100_URL );
        if ( wfsUtahDemo100Url == null ) {
            LOG.warn( "Skipping test, property '" + WFS_UTAH_DEMO_100_URL + "' not found in ~/.deegree-test.properties" );
            return;
        }

        URL wfsCapaUrl = new URL( wfsUtahDemo100Url );
        WFSClient client = new WFSClient( wfsCapaUrl );
        Assert.equals( 18, client.getAppSchema().getFeatureTypes().length );
//        StreamFeatureCollection fc = client.getFeatures( new QName( "SGID93_LOCATION_UDOTMap_CityLocations" ) );
//        try {
//            Feature feature = null;
//            while ( ( feature = fc.read() ) != null ) {
//                System.out.println( feature.getId() );
//            }
//        } finally {
//            fc.close();
//        }
    }
    
    @Test
    public void testGetCapabilities110Utah()
                            throws Exception {

        String wfsUtahDemo100Url = TestProperties.getProperty( WFS_UTAH_DEMO_110_URL );
        if ( wfsUtahDemo100Url == null ) {
            LOG.warn( "Skipping test, property '" + WFS_UTAH_DEMO_110_URL + "' not found in ~/.deegree-test.properties" );
            return;
        }

        URL wfsCapaUrl = new URL( wfsUtahDemo100Url );
        WFSClient client = new WFSClient( wfsCapaUrl );
        Assert.equals( 18, client.getAppSchema().getFeatureTypes().length );
//        StreamFeatureCollection fc = client.getFeatures( new QName( "SGID93_LOCATION_UDOTMap_CityLocations" ) );
//        try {
//            Feature feature = null;
//            while ( ( feature = fc.read() ) != null ) {
//                System.out.println( feature.getId() );
//            }
//        } finally {
//            fc.close();
//        }
    }
}
