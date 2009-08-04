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
package org.deegree.crs.configuration.deegree;

import junit.framework.TestCase;

import org.deegree.crs.CRSCodeType;
import org.deegree.crs.components.Axis;
import org.deegree.crs.components.Ellipsoid;
import org.deegree.crs.components.GeodeticDatum;
import org.deegree.crs.components.Unit;
import org.deegree.crs.configuration.CRSConfiguration;
import org.deegree.crs.configuration.CRSProvider;
import org.deegree.crs.configuration.deegree.db.DatabaseCRSProvider;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.coordinatesystems.GeographicCRS;
import org.deegree.crs.coordinatesystems.ProjectedCRS;
import org.deegree.crs.projections.Projection;
import org.deegree.crs.projections.cylindric.TransverseMercator;
import org.deegree.crs.transformations.helmert.Helmert;
import org.junit.Test;

/**
 * <code>DBCRSProviderTest</code> test the loading of a projected crs as well as the loading of the default
 * configuration using the Database backend .
 *
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 *
 * @author last edited by: $Author: aionita $
 *
 * @version $Revision: 15508 $, $Date: 2009-01-06 12:08:22 +0100 (Tue, 06 Jan 2009) $
 *
 */
public class DatabaseCRSProviderTest extends TestCase {

    /**
     * Tries to load the configuration
     */
    @Test
    public void testLoadingConfiguration() {
        CRSProvider provider = CRSConfiguration.getCRSConfiguration(
                                                                     "org.deegree.crs.configuration.deegree.db.DatabaseCRSProvider" ).getProvider();
        assertNotNull( provider );
        assertTrue( provider instanceof DatabaseCRSProvider );
        DatabaseCRSProvider dProvider = (DatabaseCRSProvider) provider;
        assertTrue( dProvider.canExport() );
    }

    /**
     * Tries to create a crs by id.
     */
    @Test
    public void testCRSByID() {
        CRSProvider provider = CRSConfiguration.getCRSConfiguration(
                                                                     "org.deegree.crs.configuration.deegree.db.DatabaseCRSProvider" ).getProvider();
        assertNotNull( provider );
        assertTrue( provider instanceof DatabaseCRSProvider );
        DatabaseCRSProvider dProvider = (DatabaseCRSProvider) provider;

        // try loading the gaus krueger zone 2.
        CoordinateSystem testCRS = dProvider.getCRSByCode( new CRSCodeType( "31466", "EPSG" ) );
        assertNotNull( testCRS );
        assertTrue( testCRS instanceof ProjectedCRS );
        ProjectedCRS realCRS = (ProjectedCRS) testCRS;
        assertNotNull( realCRS.getProjection() );
        Projection projection = realCRS.getProjection();
        assertTrue( projection instanceof TransverseMercator );
        // do stuff with projection
        TransverseMercator proj = (TransverseMercator) projection;
        assertEquals( 0.0, proj.getProjectionLatitude() );
        assertEquals( Math.toRadians( 6.0 ), proj.getProjectionLongitude() );
        assertEquals( 1.0, proj.getScale() );
        assertEquals( 2500000.0, proj.getFalseEasting() );
        assertEquals( 0.0, proj.getFalseNorthing() );
        assertTrue( proj.getHemisphere() );

        // test the datum.
        GeodeticDatum datum = realCRS.getGeodeticDatum();
        assertNotNull( datum );
        assertTrue( datum.getCode().getCode().equals( "6314" ) );

        // This assert will not be true since the new CRSIdentifiables have the series of codes reduced under the EPSG
        // codespace
        // assertEquals( PrimeMeridian.GREENWICH, datum.getPrimeMeridian() );

        // test the ellips
        Ellipsoid ellips = datum.getEllipsoid();
        assertNotNull( ellips );
        assertTrue( ellips.getCode().getCode().equals( "7004" ) );
        assertEquals( Unit.METRE, ellips.getUnits() );
        assertEquals( 6377397.155, ellips.getSemiMajorAxis() );
        assertEquals( 299.1528128, ellips.getInverseFlattening() );

        // test towgs84 params
        Helmert toWGS = datum.getWGS84Conversion();
        assertNotNull( toWGS );
        assertTrue( toWGS.hasValues() );
        assertTrue( toWGS.getCode().getCode().equals( "1777" ) );
        assertEquals( 598.1, toWGS.dx );
        assertEquals( 73.7, toWGS.dy );
        assertEquals( 418.2, toWGS.dz );
        assertEquals( 0.202, toWGS.ex );
        assertEquals( 0.045, toWGS.ey );
        assertEquals( -2.455, toWGS.ez );
        assertEquals( 6.7, toWGS.ppm );

        // test the geographic
        GeographicCRS geographic = realCRS.getGeographicCRS();
        assertNotNull( geographic );
        assertTrue( geographic.getCode().getCode().equals( "4314" ) );
        Axis[] ax = geographic.getAxis();
        assertEquals( 2, ax.length );
        assertEquals( Axis.AO_EAST, ax[0].getOrientation() );
        assertEquals( Unit.DEGREE, ax[0].getUnits() );
        assertEquals( Axis.AO_NORTH, ax[1].getOrientation() );
        assertEquals( Unit.DEGREE, ax[1].getUnits() );

        testCRS = dProvider.getCRSByCode( new CRSCodeType( "SOME_DUMMY_CODE" ) );
        assertTrue( testCRS == null );

        // dProvider.getAvailableCRSs();

    }
}
