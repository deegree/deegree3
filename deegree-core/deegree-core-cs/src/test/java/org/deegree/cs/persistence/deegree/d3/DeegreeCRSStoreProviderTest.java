//$HeadURL$
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

package org.deegree.cs.persistence.deegree.d3;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;

import org.deegree.cs.CRSCodeType;
import org.deegree.cs.components.Axis;
import org.deegree.cs.components.Ellipsoid;
import org.deegree.cs.components.GeodeticDatum;
import org.deegree.cs.components.PrimeMeridian;
import org.deegree.cs.components.Unit;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.coordinatesystems.ProjectedCRS;
import org.deegree.cs.exceptions.CRSStoreException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.cs.persistence.CRSStore;
import org.deegree.cs.persistence.deegree.DeegreeCRSStore;
import org.deegree.cs.projections.Projection;
import org.deegree.cs.projections.cylindric.TransverseMercator;
import org.deegree.cs.transformations.Transformation;
import org.deegree.cs.transformations.helmert.Helmert;
import org.junit.Test;

/**
 * {@link DeegreeCRSStoreProviderTest} test the loading of the default configuration as well as the loading of a
 * projected crs from the default configuration.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class DeegreeCRSStoreProviderTest {

    /**
     * Tries to load the default configuration, when no workspace is set!
     */
    @Test
    public void testLoadingDefaultConfiguration() {
        CRSManager.init( null );
        Collection<CRSStore> stores = CRSManager.getAll();
        assertNotNull( stores );
        assertTrue( stores.size() == 1 );
        for ( CRSStore store : stores ) {
            assertTrue( store instanceof DeegreeCRSStore );
        }
        CRSManager.destroy();
    }

    /**
     * Tries to create a crs by id.
     * 
     * @throws CRSStoreException
     */
    @Test
    public void testCRSByID()
                            throws CRSStoreException {
        CRSStore defaultStore = CRSManager.create( CRSManager.class.getResource( "default.xml" ) );
        assertNotNull( defaultStore );
        assertTrue( defaultStore instanceof DeegreeCRSStore );
        DeegreeCRSStore<?> dStore = (DeegreeCRSStore<?>) defaultStore;
        // try loading the gaus krueger zone 2. (transverse mercator)
        CoordinateSystem testCRS = dStore.getCRSByCode( new CRSCodeType( "epsg:31466" ) );
        testCRS_31466( testCRS, dStore );
        testCRS = dStore.getCRSByCode( new CRSCodeType( "SOME_DUMMY_CODE" ) );
        assertTrue( testCRS == null );
        // test mercator reading
        testCRS = dStore.getCRSByCode( new CRSCodeType( "epsg:3395" ) );
        assertTrue( testCRS != null );

        // stereographic alternative
        testCRS = dStore.getCRSByCode( new CRSCodeType( "epsg:2172" ) );
        assertTrue( testCRS != null );

        // lambertAzimuthal
        testCRS = dStore.getCRSByCode( new CRSCodeType( "epsg:2163" ) );
        assertTrue( testCRS != null );

        // lambert conformal conic
        testCRS = dStore.getCRSByCode( new CRSCodeType( "epsg:2851" ) );
        assertTrue( testCRS != null );
    }

    private void testCRS_31466( CoordinateSystem testCRS, DeegreeCRSStore<?> provider ) {
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
        assertEquals( "6314", datum.getCode().getCode() );
        assertEquals( PrimeMeridian.GREENWICH, datum.getPrimeMeridian() );

        // test the ellips
        Ellipsoid ellips = datum.getEllipsoid();
        assertNotNull( ellips );
        assertEquals( "7004", ellips.getCode().getCode() );
        assertEquals( Unit.METRE, ellips.getUnits() );
        assertEquals( 6377397.155, ellips.getSemiMajorAxis() );
        assertEquals( 299.1528128, ellips.getInverseFlattening() );

        // test towgs84 params
        Helmert toWGS = datum.getWGS84Conversion();
        if ( toWGS == null ) {
            Transformation trans = provider.getTransformation( realCRS.getGeographicCRS(), GeographicCRS.WGS84 );
            assertNotNull( trans );
            assertTrue( trans instanceof Helmert );
            toWGS = (Helmert) trans;
        }
        assertNotNull( toWGS );
        assertTrue( toWGS.hasValues() );
        assertEquals( "1777", toWGS.getCode().getCode() );
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
        assertEquals( "4314", geographic.getCode().getCode() );
        Axis[] ax = geographic.getAxis();
        assertEquals( 2, ax.length );
        assertEquals( Axis.AO_EAST, ax[0].getOrientation() );
        assertEquals( Unit.DEGREE, ax[0].getUnits() );
        assertEquals( Axis.AO_NORTH, ax[1].getOrientation() );
        assertEquals( Unit.DEGREE, ax[1].getUnits() );
    }

    /**
     * Test a cache
     * 
     * @throws CRSStoreException
     */
    public void testCache()
                            throws CRSStoreException {
        CRSStore defaultStore = CRSManager.create( CRSManager.class.getResource( "default.xml" ) );
        assertNotNull( defaultStore );
        assertTrue( defaultStore instanceof DeegreeCRSStore );
        DeegreeCRSStore<?> dStore = (DeegreeCRSStore<?>) defaultStore;

        CoordinateSystem testCRS = dStore.getCRSByCode( new CRSCodeType( "epsg:31466" ) );
        testCRS_31466( testCRS, dStore );

        testCRS = dStore.getCRSByCode( new CRSCodeType( "epsg:31466" ) );
        testCRS_31466( testCRS, dStore );
    }

}
