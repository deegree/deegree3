//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/ogcwebservices/wms/opertaion/GetMapTest.java $
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
package org.deegree.ogcwebservices.wms.opertaion;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.KVP2Map;
import org.deegree.ogcwebservices.wms.operation.GetMap;

/**
 *
 *
 *
 * @version $Revision: 18195 $
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: mschneider $
 *
 * @version 1.0. $Revision: 18195 $, $Date: 2009-06-18 17:55:39 +0200 (Do, 18 Jun 2009) $
 *
 * @since 2.0
 */
public class GetMapTest extends TestCase {
    private static ILogger LOG = LoggerFactory.getLogger( GetMapTest.class );

    /**
     * Constructor for GetCoverageTest
     *
     * @param arg0
     */
    public GetMapTest( String arg0 ) {
        super( arg0 );
    }

    /**
     * @return a new instance
     */
    public static Test suite() {
        return new TestSuite( GetMapTest.class );
    }

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp()
                            throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    @Override
    protected void tearDown()
                            throws Exception {
        super.tearDown();
    }

    /**
     * testing if a GetMap request can be parsed
     *
     * @throws Exception
     */
    public void testGetMap1()
                            throws Exception {
        String req = "SRS=EPSG:31467&REQUEST=GetMap&VERSION=1.1.1&transparent=true&"
                     + "BGCOLOR=0xFFFFFF&FORMAT=image/gif&"
                     + "LAYERS=FL,AF,AN,SN,BA,BM&STYLES=fl_umriss,af_dashed,,,,bm_symbol&"
                     + "BBOX=3561451,5939464,3561951,5939964&WIDTH=500&HEIGHT=500&myparam=uuuu";
        Map<String, String> map = KVP2Map.toMap( req );
        map.put( "ID", "1" );
        try {
            GetMap request = GetMap.create( map );
            LOG.logInfo( "---" );
            LOG.logInfo( request.getRequestParameter() );
        } catch ( Exception e ) {
            throw e;
        }
    }

    /**
     * testing if a GetMap request can be parsed
     *
     * @throws Exception
     */
    public void testGetMap2()
                            throws Exception {
        // String req = "SRS=EPSG:31467&REQUEST=GetMap&VERSION=1.1.1&transparent=true&BGCOLOR=0xFFFFFF&" +
        // "FORMAT=image/gif&LAYERS=FL,AF,AN,SN,SP00,HS,BM,rer&" +
        // "STYLES=,af_dashed,,sp00_umriss,,bm_symbol,,&" +
        // "BBOX=3561451,5939464,3561951,5939964&WIDTH=500&HEIGHT=500";
        String req = "version=1.1.1&REQUEST=GetMap&layers=cite:BasicPolygons,cite:Bridges,cite:BasicPolygons,cite:RoadSegments,cite:DividedRoutes&"
                     + "styles=default,,default,,&srs=EPSG:4326&bbox=-0.0042,-0.0024,0.0042,0.0024&width=168&height=96&"
                     + "format=image/png";
        Map<String, String> map = KVP2Map.toMap( req );
        map.put( "ID", "1" );
        GetMap request = GetMap.create( map );
        LOG.logInfo( request.getRequestParameter() );

    }

}
