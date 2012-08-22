//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/ogcwebservices/wcs/WCServiceTest.java $
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
package org.deegree.ogcwebservices.wcs;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.ImageUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.coverage.grid.AbstractGridCoverage;
import org.deegree.ogcwebservices.OGCRequestFactory;
import org.deegree.ogcwebservices.wcs.configuration.WCSConfiguration;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageDescription;
import org.deegree.ogcwebservices.wcs.describecoverage.DescribeCoverage;
import org.deegree.ogcwebservices.wcs.getcapabilities.WCSGetCapabilities;
import org.deegree.ogcwebservices.wcs.getcoverage.GetCoverage;
import org.deegree.ogcwebservices.wcs.getcoverage.ResultCoverage;
import org.w3c.dom.Document;

import alltests.Configuration;

/**
 *
 *
 * @version $Revision: 18195 $
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author: mschneider $
 *
 * @version 1.0. $Revision: 18195 $, $Date: 2009-06-18 17:55:39 +0200 (Do, 18 Jun 2009) $
 *
 * @since 2.0
 */
public class WCServiceTest extends TestCase {

    private static ILogger LOG = LoggerFactory.getLogger( WCServiceTest.class );

    /**
     * @return a test
     */
    public static Test suite() {
        return new TestSuite( WCServiceTest.class );
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
    public WCServiceTest( String arg0 ) {
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
            WCSConfiguration configuration = WCSConfiguration.create( Configuration.getWCSConfigurationURL() );
            WCService service = new WCService( configuration );
            // StringBuffer sb = new StringBuffer();
            // sb.append( "http://127.0.0.1/deegreewcs/wcs?service=WCS&" );
            // sb.append( "request=GetCapabilities&version=1.0.0" );
            Map<String, String> map = new HashMap<String, String>();
            map.put( "REQUEST", "GetCapabilities" );
            map.put( "VERSION", "1.0.0" );
            map.put( "SERVICE", "WCS" );
            WCSGetCapabilities getCapa = (WCSGetCapabilities) WCSGetCapabilities.create( map );
            // (WCSGetCapabilities) OGCRequestFactory.createFromKVP(sb.toString());
            Object o = service.doService( getCapa );
            XMLFragment xml = XMLFactory.export( (WCSConfiguration) o );
            // xml.write( System.out );
            LOG.logInfo( xml.getAsPrettyString() );
        } catch ( Exception e ) {
            fail( StringTools.stackTraceToString( e ) );
        }
    }

    /**
     * reads a deegree WCS configuration file and performs a GetCoverage request Steps:
     * <ul>
     * <li>read configuration file
     * <li>read a GetCoverage request object
     * <li>perform the request
     * </ul>
     */
    public void _testGetCoverage1() {
        try {
            WCSConfiguration configuration = WCSConfiguration.create( Configuration.getWCSConfigurationURL() );
            WCService service = new WCService( configuration );
            StringBuffer sb = new StringBuffer();
            sb.append( Configuration.PROTOCOL + "://" + Configuration.HOST ).append( ':' ).append( Configuration.PORT ).append(
                                                                                                                                Configuration.WCS_WEB_CONTEXT ).append(
                                                                                                                                                                        '/' ).append(
                                                                                                                                                                                      Configuration.WCS_SERVLET );

            String req = "<?xml version='1.0' encoding='UTF-8'?><GetCoverage "
                         + "xmlns='http://www.opengis.net/wcs' xmlns:gml='http://www.opengis.net/gml' "
                         + "service='WCS' version='1.0.0'><sourceCoverage>Mapneatline</sourceCoverage>"
                         + "<domainSubset><spatialSubset><gml:Envelope srsName='EPSG:4326'>"
                         + "<gml:pos dimension='2'>-1,-1</gml:pos><gml:pos dimension='2'>1,1"
                         + "</gml:pos></gml:Envelope><gml:Grid dimension='2'><gml:limits>"
                         + "<gml:GridEnvelope><gml:low>0 0</gml:low><gml:high>300 300</gml:high>"
                         + "</gml:GridEnvelope></gml:limits><gml:axisName>x</gml:axisName>"
                         + "<gml:axisName>y</gml:axisName></gml:Grid></spatialSubset></domainSubset>"
                         + "<output><crs>EPSG:4326</crs><format>jpeg</format></output></GetCoverage>";
            StringReader reader = new StringReader( req );
            Document doc = XMLTools.parse( reader );

            GetCoverage desc = (GetCoverage) OGCRequestFactory.createFromXML( doc );
            ResultCoverage o = (ResultCoverage) service.doService( desc );
            BufferedImage bi = ( (AbstractGridCoverage) o.getCoverage() ).getAsImage( 500, 500 );

            FileOutputStream fos = new FileOutputStream( Configuration.getWCSBaseDir().getPath() + "/kannweg1.tif" );
            ImageUtils.saveImage( bi, fos, "tif", 1 );
            fos.close();
        } catch ( Exception e ) {
            fail( StringTools.stackTraceToString( e ) );
        }
    }

    /**
     * reads a deegree WCS configuration file and performs a GetCoverage request. same as testGetCoverage1() but uses
     * nameIndexed data source Steps:
     * <ul>
     * <li>read configuration file
     * <li>read a GetCoverage request object
     * <li>perform the request
     * </ul>
     *
     */
    public void _testGetCoverage2() {
        try {
            WCSConfiguration configuration = WCSConfiguration.create( Configuration.getWCSConfigurationURL() );
            WCService service = new WCService( configuration );

            Map<String, String> map = new HashMap<String, String>();
            map.put( "SERVICE", "WCS" );
            map.put( "REQUEST", "GetCoverage" );
            map.put( "VERSION", "1.0.0" );
            map.put( "COVERAGE", "dem" );
            map.put( "CRS", "EPSG:4326" );
            map.put( "BBOX", "-122.6261,37.4531,-122.0777,38.0" );
            map.put( "WIDTH", "828" );
            map.put( "HEIGHT", "823" );
            map.put( "FORMAT", "GeoTiff" );

            // StringBuffer sb = new StringBuffer();
            // sb.append(Configuration.PROTOCOL + "://" + Configuration.HOST).append(':').append(
            // Configuration.PORT).append('/').append(Configuration.WCS_WEB_CONTEXT).append(
            // '/').append(Configuration.WCS_SERVLET).append("?service=WCS&").append(
            // "request=GetCoverage&version=1.0.0&coverage=dem&").append(
            // "crs=EPSG:4326&BBOX=-122.6261,37.4531,-122.0777,38.0&Width=828&height=823&")
            // .append("format=GeoTiff");
            GetCoverage desc = GetCoverage.create( map );
            ResultCoverage o = (ResultCoverage) service.doService( desc );
            BufferedImage bi = ( (AbstractGridCoverage) o.getCoverage() ).getAsImage( 828, 823 );
            LOG.logInfo( o.toString());
            FileOutputStream fos = new FileOutputStream(
                                                         new URL( Configuration.getWCSBaseDir(), "/kannweg2.tif" ).getFile() );
            ImageUtils.saveImage( bi, fos, "tif", 1 );
            fos.close();
        } catch ( Exception e ) {
            fail( StringTools.stackTraceToString( e ) );
        }
    }

    /**
     * reads a deegree WCS configuration file and performs a GetCoverage request Steps:
     * <ul>
     * <li>read configuration file
     * <li>read a GetCoverage request object
     * <li>perform the request
     * </ul>
     *
     */
    public void _testGetCoverage3() {
        try {
            WCSConfiguration configuration = WCSConfiguration.create( Configuration.getWCSConfigurationURL() );
            WCService service = new WCService( configuration );

            Map<String, String> map = new HashMap<String, String>();
            map.put( "SERVICE", "WCS" );
            map.put( "REQUEST", "GetCoverage" );
            map.put( "VERSION", "1.0.0" );
            map.put( "COVERAGE", "europe" );
            map.put( "CRS", "EPSG:4326" );
            map.put( "BBOX", "-5,40,20,60" );
            map.put( "WIDTH", "800" );
            map.put( "HEIGHT", "800" );
            map.put( "FORMAT", "jpeg" );

            // StringBuffer sb = new StringBuffer();
            // sb.append(Configuration.PROTOCOL + "://" + Configuration.HOST)
            // .append(':').append(Configuration.PORT).append('/')
            // .append(Configuration.WCS_WEB_CONTEXT).append('/')
            // .append(Configuration.WCS_SERVLET).append("?service=WCS&")
            // .append("request=GetCoverage&version=1.0.0&coverage=europe&")
            // .append( "crs=EPSG:4326&BBOX=-5,40,20,60&Width=800&height=800&")
            // .append("format=jpeg");
            GetCoverage desc = GetCoverage.create( map );
            ResultCoverage o = (ResultCoverage) service.doService( desc );
            BufferedImage bi = ( (AbstractGridCoverage) o.getCoverage() ).getAsImage( 800, 800 );

            FileOutputStream fos = new FileOutputStream( Configuration.getWCSBaseDir().getPath() + "/kannweg3.tif" );
            ImageUtils.saveImage( bi, fos, "tif", 1 );
            fos.close();
        } catch ( Exception e ) {
            fail( StringTools.stackTraceToString( e ) );
        }
    }

    /**
     * reads a deegree WCS configuration file and performs a DescribeCoverage request Steps:
     * <ul>
     * <li>read configuration file
     * <li>read a DescribeCoverage request object
     * <li>perform the request
     * </ul>
     *
     */
    public void _testDescribeCoverage() {
        try {
            WCSConfiguration configuration = WCSConfiguration.create( Configuration.getWCSConfigurationURL() );
            WCService service = new WCService( configuration );

            Map<String, String> map = new HashMap<String, String>();
            map.put( "SERVICE", "WCS" );
            map.put( "REQUEST", "DescribeCoverage" );
            map.put( "VERSION", "1.0.0" );
            map.put( "COVERAGE", "europe" );

            // StringBuffer sb = new StringBuffer();
            // sb.append(Configuration.PROTOCOL + "://" + Configuration.HOST).append(':').append(
            // Configuration.PORT).append('/').append(Configuration.WCS_WEB_CONTEXT).append(
            // '/').append(Configuration.WCS_SERVLET).append("?service=WCS&").append(
            // "request=DescribeCoverage&version=1.0.0&coverage=europe");

            DescribeCoverage desc = DescribeCoverage.create( map );
            CoverageDescription o = (CoverageDescription) service.doService( desc );
            LOG.logInfo( Arrays.toString( o.getCoverageOfferings() ) );
        } catch ( Exception e ) {
            fail( StringTools.stackTraceToString( e ) );
        }
    }

}
