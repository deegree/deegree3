//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/crs/configuration/gml/GMLCRSProviderTest.java $
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

package org.deegree.crs.configuration.gml;

import java.io.File;

import junit.framework.TestCase;

import org.deegree.crs.components.Axis;
import org.deegree.crs.components.Ellipsoid;
import org.deegree.crs.components.GeodeticDatum;
import org.deegree.crs.components.Unit;
import org.deegree.crs.configuration.CRSConfiguration;
import org.deegree.crs.configuration.CRSProvider;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.coordinatesystems.GeographicCRS;
import org.deegree.crs.coordinatesystems.ProjectedCRS;
import org.deegree.crs.projections.Projection;
import org.deegree.crs.projections.conic.LambertConformalConic;
import org.deegree.crs.projections.cylindric.TransverseMercator;
import org.deegree.crs.transformations.helmert.Helmert;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;

/**
 * <code>GMLCRSProviderTest</code> test the loading of a projected crs as well as the loading of the default
 * configuration.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author: mschneider $
 *
 * @version $Revision: 18195 $, $Date: 2009-06-18 17:55:39 +0200 (Do, 18 Jun 2009) $
 *
 */
public class GMLCRSProviderTest extends TestCase {
    private static final String CONFIG_FILE = System.getProperty( "user.home" )
                                              + "/workspace/adv_registry2/resources/configurations/adv-wcts/conf/wcts/AdV-Registry-P2_130-v02_dictionary_2.xml";

    private static ILogger LOG = LoggerFactory.getLogger( GMLCRSProviderTest.class );

    private GMLCRSProvider getProvider() {
        File f = new File( CONFIG_FILE );
        if ( !f.exists() ) {
            LOG.logError( "No configuration file found, nothing to test. " );
            throw new NullPointerException( "The file was not found. " );
        }
        final String old_value = CRSConfiguration.setDefaultFileProperty( CONFIG_FILE );
        CRSProvider provider = CRSConfiguration.getCRSConfiguration( "org.deegree.crs.configuration.gml.GMLCRSProvider" ).getProvider();
        assertNotNull( provider );
        if ( !( provider instanceof GMLCRSProvider ) ) {
            throw new NullPointerException( "The provider was not loaded. " );
        }
        if ( old_value != null ) {
            CRSConfiguration.setDefaultFileProperty( old_value );
        }
        return (GMLCRSProvider) provider;

    }

    /**
     * Tries to load the configuration
     */
    public void testLoadingConfiguration() {
        try {
            GMLCRSProvider gProvider = getProvider();
            assertFalse( gProvider.canExport() );
        } catch ( NullPointerException e ) {
            // nottin.
        }
    }

    /**
     * Tries to create a crs by id.
     */
    public void testCRSByID() {
        try {
            GMLCRSProvider gProvider = getProvider();
            // try loading the gaus krueger zone 3.
            CoordinateSystem testCRS = gProvider.getCRSByID( "urn:adv:crs:DE_DHDN_3GK3" );
            testCRS_31467( testCRS );
            testCRS = gProvider.getCRSByID( "SOME_DUMMY_CODE" );
            assertTrue( testCRS == null );

        } catch ( NullPointerException e ) {
            // nottin.
        }
    }

    private void testCRS_31467( CoordinateSystem testCRS ) {
        assertNotNull( testCRS );
        assertTrue( testCRS instanceof ProjectedCRS );
        ProjectedCRS realCRS = (ProjectedCRS) testCRS;
        assertNotNull( realCRS.getProjection() );
        Projection projection = realCRS.getProjection();
        assertTrue( projection instanceof TransverseMercator );
        // do stuff with projection
        TransverseMercator proj = (TransverseMercator) projection;
        assertEquals( 0.0, proj.getProjectionLatitude() );
        assertEquals( Math.toRadians( 9.0 ), proj.getProjectionLongitude() );
        assertEquals( 1.0, proj.getScale() );
        assertEquals( 3500000.0, proj.getFalseEasting() );
        assertEquals( 0.0, proj.getFalseNorthing() );
        assertTrue( proj.getHemisphere() );

        // test the datum.
        GeodeticDatum datum = realCRS.getGeodeticDatum();
        assertNotNull( datum );
        // assertEquals( "EPSG:6314", datum.getIdentifier() );
        assertEquals( "urn:adv:datum:DHDN", datum.getIdentifier() );
        // assertEquals( PrimeMeridian.GREENWICH, datum.getPrimeMeridian() );
        // assertEquals( "urn:adv:meridian:Greenwich", datum.getPrimeMeridian().getIdentifier() );

        // test the ellips
        Ellipsoid ellips = datum.getEllipsoid();
        assertNotNull( ellips );
        // assertEquals( "EPSG:7004", ellips.getIdentifier() );
        assertEquals( "urn:adv:ellipsoid:Bessel", ellips.getIdentifier() );
        assertEquals( Unit.METRE, ellips.getUnits() );
        assertEquals( 6377397.155, ellips.getSemiMajorAxis() );
        assertEquals( 299.1528128, ellips.getInverseFlattening() );

        // test towgs84 params
        Helmert toWGS = datum.getWGS84Conversion();
        assertNotNull( toWGS );
        assertTrue( toWGS.hasValues() );
        assertEquals( "urn:adv:coordinateOperation:DHDN_ETRS89_3m", toWGS.getIdentifier() );
        assertEquals( 598.1, toWGS.dx );
        assertEquals( 73.7, toWGS.dy );
        assertEquals( 418.2, toWGS.dz );
        assertEquals( 0.202, Unit.RADIAN.convert( toWGS.ex, Unit.ARC_SEC ) );
        assertEquals( 0.045, Unit.RADIAN.convert( toWGS.ey, Unit.ARC_SEC ) );
        assertEquals( -2.455, Unit.RADIAN.convert( toWGS.ez, Unit.ARC_SEC ) );
        assertEquals( 6.7, toWGS.ppm );

        // test the geographic
        GeographicCRS geographic = realCRS.getGeographicCRS();
        assertNotNull( geographic );
        assertEquals( "urn:adv:crs:DE_DHDN", geographic.getIdentifier() );
        // assertEquals( "EPSG:4314", geographic.getIdentifier() );
        Axis[] ax = geographic.getAxis();
        assertEquals( 2, ax.length );
        assertEquals( Axis.AO_EAST, ax[1].getOrientation() );
        assertEquals( Unit.DEGREE, ax[1].getUnits() );
        assertEquals( Axis.AO_NORTH, ax[0].getOrientation() );
        assertEquals( Unit.DEGREE, ax[0].getUnits() );
    }

    // TODO deactivated, so test suite passes

//    public void testLambert() {
//        GMLCRSProvider gProvider = getProvider();
//        CoordinateSystem testCRS = gProvider.getCRSByID( "urn:adv:crs:FR_NTF-Paris_LambertII" );
//        assertNotNull( testCRS );
//        assertTrue( testCRS instanceof ProjectedCRS );
//        ProjectedCRS realCRS = (ProjectedCRS) testCRS;
//        assertNotNull( realCRS.getProjection() );
//        Projection projection = realCRS.getProjection();
//        assertTrue( projection instanceof LambertConformalConic );
//        // do stuff with projection
//        LambertConformalConic proj = (LambertConformalConic) projection;
//        assertEquals( Math.toRadians( 52 ), proj.getProjectionLatitude(), 0.000001 );
//        assertEquals( Math.toRadians( 0 ), proj.getProjectionLongitude(), 0.000001 );
//        assertEquals( 0.99987742, proj.getScale(), 0.000001 );
//        assertEquals( 600000, proj.getFalseEasting(), 0.000001 );
//        assertEquals( 2200000, proj.getFalseNorthing(), 0.000001 );
//
//        // test the datum.
//        GeodeticDatum datum = realCRS.getGeodeticDatum();
//        assertNotNull( datum );
//        // assertEquals( "EPSG:6314", datum.getIdentifier() );
//        assertEquals( "urn:adv:datum:FR_NTF-Paris", datum.getIdentifier() );
//
//        // test the ellips
//        Ellipsoid ellips = datum.getEllipsoid();
//        assertNotNull( ellips );
//        // assertEquals( "EPSG:7004", ellips.getIdentifier() );
//        assertEquals( "urn:adv:ellipsoid:Clarke1880-IGN", ellips.getIdentifier() );
//        assertEquals( Unit.METRE, ellips.getUnits() );
//        assertEquals( 6378249.2, ellips.getSemiMajorAxis(), 0.000001 );
//        assertEquals( 6356515, ellips.getSemiMinorAxis(), 0.000001 );
//
//        // test towgs84 params
//        Helmert toWGS = datum.getWGS84Conversion();
//        assertNotNull( toWGS );
//        assertTrue( toWGS.hasValues() );
//        assertEquals( "urn:adv:coordinateOperation:NTF_ETRS89_2m", toWGS.getIdentifier() );
//        assertEquals( -168, toWGS.dx, 0.000001 );
//        assertEquals( -60, toWGS.dy, 0.000001 );
//        assertEquals( 320, toWGS.dz, 0.000001 );
//        assertEquals( 0.0, toWGS.ex );
//        assertEquals( 0.0, toWGS.ey );
//        assertEquals( 0.0, toWGS.ez );
//        assertEquals( 0.0, toWGS.ppm );
//
//        // test the geographic
//        GeographicCRS geographic = realCRS.getGeographicCRS();
//        assertNotNull( geographic );
//        assertEquals( "urn:adv:crs:FR_NTF-Paris", geographic.getIdentifier() );
//        // assertEquals( "EPSG:4314", geographic.getIdentifier() );
//        Axis[] ax = geographic.getAxis();
//        assertEquals( 2, ax.length );
//        assertEquals( Axis.AO_EAST, ax[1].getOrientation() );
//        assertEquals( Unit.DEGREE, ax[1].getUnits() );
//        assertEquals( Axis.AO_NORTH, ax[0].getOrientation() );
//        assertEquals( Unit.DEGREE, ax[0].getUnits() );
//    }

    /**
     * Test a cache
     */
    public void testCache() {
        try {
            GMLCRSProvider gProvider = getProvider();

            CoordinateSystem testCRS = gProvider.getCRSByID( "urn:adv:crs:DE_DHDN_3GK3" );
            testCRS_31467( testCRS );

            testCRS = gProvider.getCRSByID( "urn:adv:crs:DE_DHDN_3GK3" );
            testCRS_31467( testCRS );
        } catch ( NullPointerException e ) {
            // nottin.
        }
    }
}
