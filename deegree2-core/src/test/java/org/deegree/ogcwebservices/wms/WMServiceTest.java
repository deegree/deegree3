//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/ogcwebservices/wms/WMServiceTest.java $
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
package org.deegree.ogcwebservices.wms;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.ogcwebservices.wms.operation.GetFeatureInfo;
import org.deegree.ogcwebservices.wms.operation.GetFeatureInfoResult;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.deegree.ogcwebservices.wms.operation.GetMapResult;
import org.deegree.ogcwebservices.wms.operation.WMSGetCapabilities;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author: mschneider $
 * @version 1.0. $Revision: 18195 $, $Date: 2009-06-18 17:55:39 +0200 (Do, 18 Jun 2009) $
 * @since 2.0
 */
public class WMServiceTest extends TestCase {

    private static ILogger LOG = LoggerFactory.getLogger( WMServiceTest.class );

    /**
     * @return a new instance
     */
    public static Test suite() {
        return new TestSuite( WMServiceTest.class );
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
     * Constructor for GetCoverageTest
     *
     * @param arg0
     */
    public WMServiceTest( String arg0 ) {
        super( arg0 );
    }

    /**
     * reads a deegree WCS configuration file and performs a GetCapbilities request Steps:
     * <ul>
     * <li>read configuration file
     * <li>read a GetCapabilites request object
     * <li>perform the request
     * </ul>
     *
     */
    public void testGetCapabilities() {
        try {
            URL url = new File( "resources/wms/wms_configuration_1-3-0_schema-supported_version_1-3-0.xml" ).toURL();
            WMServiceFactory.setConfiguration( url );
            WMService service = WMServiceFactory.getService();

            Map<String, String> map = new HashMap<String, String>();
            map.put( "SERVICE", "WMS" );
            map.put( "REQUEST", "GetCapabilities" );
            map.put( "VERSION", "1.1.1" );

            // StringBuffer sb = new StringBuffer();
            // sb.append("http://127.0.0.1/deegree/ogcwebservice?service=WMS&");
            // sb.append("request=GetCapabilities&version=1.1.1");

            WMSGetCapabilities getCapa = WMSGetCapabilities.create( map );
            Object o = service.doService( getCapa );
            LOG.logInfo( "------------------------" );
            LOG.logInfo( o.toString() );
            LOG.logInfo( "------------------------" );
        } catch ( Exception e ) {
            LOG.logInfo( StringTools.stackTraceToString( e ) );
            fail( StringTools.stackTraceToString( e ) );
        }
    }

    /**
     * reads a deegree WMS configuration file and performs a GetMap request Steps:
     * <ul>
     * <li>read configuration file
     * <li>read a GetMap request object
     * <li>perform the request
     * </ul>
     *
     */
    public void testGetMap()
                            throws Exception {
        URL url = new File( "resources/wms/wms_configuration_1-3-0_schema-supported_version_1-3-0.xml" ).toURL();
        WMServiceFactory.setConfiguration( url );
        WMService service = WMServiceFactory.getService();

        Map<String, String> map = new HashMap<String, String>();
        map.put( "ID", "89" );
        map.put( "SERVICE", "WMS" );
        map.put( "REQUEST", "GetMap" );
        map.put( "VERSION", "1.1.1" );
        map.put( "FORMAT", "image/jpeg" );
        map.put( "TRANSPARENT", "false" );
        map.put( "WIDTH", "450" );
        map.put( "HEIGHT", "450" );
        map.put( "EXCEPTIONS", "application/vnd.ogc.se_inimage" );
        map.put( "BBOX", "-0.01,-0.01,0.01,0.01" );
        map.put( "BGCOLOR", "0xffffff" );
        map.put( "LAYERS", "cite:Buildings" );
        map.put( "STYLES", "" );
        map.put( "SRS", "EPSG:4326" );

        // StringBuffer sb = new StringBuffer();
        // sb.append("http://127.0.0.1/deegree/ogcwebservice?SERVICE=WMS&VERSION=1.1.1&");
        // sb.append("REQUEST=GetMap&FORMAT=image/jpeg&TRANSPARENT=false&WIDTH=450&");
        // sb.append("HEIGHT=450&EXCEPTIONS=application/vnd.ogc.se_inimage&");
        // sb.append("BGCOLOR=0xffffff&BBOX=-0.01,-0.01,0.01,0.01");
        // sb.append("&LAYERS=cite:Buildings");
        // sb.append("&STYLES=&SRS=EPSG:4326");

        GetMap getMap = GetMap.create( map );

        Object o = service.doService( getMap );
        BufferedImage bi = (BufferedImage) ( (GetMapResult) o ).getMap();
        // FileOutputStream fos = new FileOutputStream(Configuration.WMS_BASEDIR + "/kannweg2.tif");
        FileOutputStream fos = new FileOutputStream( File.createTempFile( "deegree", ".png" ) );
        ImageIO.write( bi, "png", fos );
        fos.close();
    }

    /**
     * reads a deegree WMS configuration file and performs a GetMap request Steps:
     * <ul>
     * <li>read configuration file
     * <li>read a GetMap request object
     * <li>perform the request
     * </ul>
     *
     */
    public void testGetFeatureInfo() {
        try {
            URL url = new File( "resources/wms/wms_configuration_1-3-0_schema-supported_version_1-3-0.xml" ).toURL();
            WMServiceFactory.setConfiguration( url );
            WMService service = WMServiceFactory.getService();

            Map<String, String> map = new HashMap<String, String>();
            map.put( "ID", "89" );
            map.put( "SERVICE", "WMS" );
            map.put( "REQUEST", "GetFeatureInfo" );
            map.put( "VERSION", "1.1.1" );
            map.put( "FORMAT", "image/jpeg" );
            map.put( "TRANSPARENT", "false" );
            map.put( "WIDTH", "450" );
            map.put( "HEIGHT", "450" );
            map.put( "EXCEPTIONS", "application/vnd.ogc.se_xml" );
            map.put( "BBOX", "-9.920374269209355E-4,-4.833549380567929E-4,0.001381009448079065,0.0018896919369432081" );
            map.put( "BGCOLOR", "0xffffff" );
            map.put( "LAYERS", "cite:Forests,cite:Lakes,cite:Ponds,cite:NamedPlaces,"
                               + "cite:RoadSegments,cite:MapNeatline,cite:Streams,cite:DividedRoutes,"
                               + "cite:Buildings,cite:BuildingCenters,cite:Bridges" );
            map.put( "STYLES", "" );
            map.put( "SRS", "EPSG:4326" );
            map.put( "QUERY_LAYERS", "cite:Forests,cite:Lakes,cite:Ponds,cite:NamedPlaces,"
                                     + "cite:RoadSegments,cite:DividedRoutes,cite:Buildings,"
                                     + "cite:BuildingCenters,cite:Bridges" );
            map.put( "FEATURE_COUNT", "999" );
            map.put( "INFO_FORMAT", "text/html" );
            map.put( "X", "224" );
            map.put( "Y", "226" );

            // StringBuffer sb = new StringBuffer();
            // sb.append("http://127.0.0.1/deegreewms/wms?service=WMS&VERSION=1.1.1&");
            // sb.append("REQUEST=GetFeatureInfo&TRANSPARENCY=false&WIDTH=450&HEIGHT");
            // sb.append("=450&FORMAT=image/jpeg&EXCEPTIONS=application/vnd.ogc.se_xml");
            // sb.append("&BGCOLOR=0xffffff&SRS=EPSG:4326&BBOX=-9.920374269209355E-4,");
            // sb.append("-4.833549380567929E-4,0.001381009448079065,0.0018896919369432081");
            // sb.append("&LAYERS=cite:Forests,cite:Lakes,cite:Ponds,cite:NamedPlaces,");
            // sb.append("cite:RoadSegments,cite:MapNeatline,cite:Streams,cite:DividedRoutes,");
            // sb.append("cite:Buildings,cite:BuildingCenters,cite:Bridges&STYLES=&");
            // sb.append("QUERY_LAYERS=cite:Forests,cite:Lakes,cite:Ponds,cite:NamedPlaces,");
            // sb.append("cite:RoadSegments,cite:DividedRoutes,cite:Buildings,");
            // sb.append("cite:BuildingCenters,cite:Bridges&FEATURE_COUNT=999&INFO_FORMAT=");
            // sb.append("text/html&X=224&Y=226");

            GetFeatureInfo getFI = GetFeatureInfo.create( map );
            Object o = service.doService( getFI );
            LOG.logInfo( "------------------------" );
            LOG.logInfo( ( (GetFeatureInfoResult) o ).getFeatureInfo() );
            LOG.logInfo( "------------------------" );
        } catch ( Exception e ) {
            LOG.logInfo( StringTools.stackTraceToString( e ) );
            fail( StringTools.stackTraceToString( e ) );
        }
    }

}
