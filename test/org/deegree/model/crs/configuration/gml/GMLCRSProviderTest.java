//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.model.crs.configuration.gml;

import java.io.File;

import junit.framework.TestCase;

import org.deegree.model.crs.components.Axis;
import org.deegree.model.crs.components.Ellipsoid;
import org.deegree.model.crs.components.GeodeticDatum;
import org.deegree.model.crs.components.Unit;
import org.deegree.model.crs.configuration.CRSConfiguration;
import org.deegree.model.crs.configuration.CRSProvider;
import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.crs.coordinatesystems.GeographicCRS;
import org.deegree.model.crs.coordinatesystems.ProjectedCRS;
import org.deegree.model.crs.projections.Projection;
import org.deegree.model.crs.projections.cylindric.TransverseMercator;
import org.deegree.model.crs.transformations.helmert.Helmert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>GMLCRSProviderTest</code> test the loading of a projected crs as well as the loading of the default
 * configuration.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class GMLCRSProviderTest extends TestCase {
    private static final String CONFIG_FILE = "${user.home}/workspace/adv_registry2/docs/specification/AdV-Registry-P2_130-v02_dictionary_2.xml";

    private static Logger LOG = LoggerFactory.getLogger( GMLCRSProviderTest.class );

    private GMLCRSProvider getProvider() {
        File f = new File( CONFIG_FILE );
        if ( !f.exists() ) {
            LOG.error( "No configuration file found, nothing to test. " );
            throw new NullPointerException( "The file was not found. " );
        }
        final String old_value = CRSConfiguration.setDefaultFileProperty( CONFIG_FILE );
        CRSProvider provider = CRSConfiguration.getCRSConfiguration( "org.deegree.crs.configuration.gml.GMLCRSProvider" ).getProvider();
        assertNotNull( provider );
        if ( !( provider instanceof GMLCRSProvider ) ) {
            throw new NullPointerException( "The provider was not loaded. " );
        }
        CRSConfiguration.setDefaultFileProperty( old_value );
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
