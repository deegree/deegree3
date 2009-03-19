package org.deegree.model.crs.configuration.deegree;

import junit.framework.TestCase;

import org.deegree.model.crs.CRSCodeType;
import org.deegree.model.crs.components.Axis;
import org.deegree.model.crs.components.Ellipsoid;
import org.deegree.model.crs.components.GeodeticDatum;
import org.deegree.model.crs.components.PrimeMeridian;
import org.deegree.model.crs.components.Unit;
import org.deegree.model.crs.configuration.CRSConfiguration;
import org.deegree.model.crs.configuration.CRSProvider;
import org.deegree.model.crs.configuration.deegree.db.DatabaseCRSProvider;
import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.crs.coordinatesystems.GeographicCRS;
import org.deegree.model.crs.coordinatesystems.ProjectedCRS;
import org.deegree.model.crs.projections.Projection;
import org.deegree.model.crs.projections.cylindric.TransverseMercator;
import org.deegree.model.crs.transformations.helmert.Helmert;
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
        CRSProvider provider = CRSConfiguration.getCRSConfiguration( "org.deegree.model.crs.configuration.deegree.db.DatabaseCRSProvider" ).getProvider();
        assertNotNull( provider );
        assertTrue( provider instanceof DatabaseCRSProvider );
        DatabaseCRSProvider dProvider = (DatabaseCRSProvider) provider;
        assertTrue( dProvider.canExport() );
        
        // connecting to the database
        dProvider.connectToDatabase();
        
        // disconnecting
        dProvider.closeDatabaseConnection();
    }

    /**
     * Tries to create a crs by id.
     */
    @Test
    public void testCRSByID() {
        CRSProvider provider = CRSConfiguration.getCRSConfiguration( "org.deegree.model.crs.configuration.deegree.db.DatabaseCRSProvider" ).getProvider();
        assertNotNull( provider );
        assertTrue( provider instanceof DatabaseCRSProvider );
        DatabaseCRSProvider dProvider = (DatabaseCRSProvider) provider;

        // connecting to the database
        dProvider.connectToDatabase();
        
        // try loading the gaus krueger zone 2.
        CoordinateSystem testCRS = dProvider.getCRSByID( "EPSG:31466" );
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
        assertEquals( CRSCodeType.valueOf( "EPSG:6314" ), datum.getCode() );
        
        // This assert will not be true since the new CRSIdentifiables have the series of codes reduced under the EPSG codespace
        //assertEquals( PrimeMeridian.GREENWICH, datum.getPrimeMeridian() ); 
        

        // test the ellips
        Ellipsoid ellips = datum.getEllipsoid();
        assertNotNull( ellips );
        assertEquals( CRSCodeType.valueOf( "EPSG:7004" ), ellips.getCode() );
        assertEquals( Unit.METRE, ellips.getUnits() );
        assertEquals( 6377397.155, ellips.getSemiMajorAxis() );
        assertEquals( 299.1528128, ellips.getInverseFlattening() );

        // test towgs84 params
        Helmert toWGS = datum.getWGS84Conversion();
        assertNotNull( toWGS );
        assertTrue( toWGS.hasValues() );
        assertEquals( CRSCodeType.valueOf( "EPSG:1777" ), toWGS.getCode() );
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
        assertEquals( CRSCodeType.valueOf( "EPSG:4314" ), geographic.getCode() );
        Axis[] ax = geographic.getAxis();
        assertEquals( 2, ax.length );
        assertEquals( Axis.AO_EAST, ax[0].getOrientation() );
        assertEquals( Unit.DEGREE, ax[0].getUnits() );
        assertEquals( Axis.AO_NORTH, ax[1].getOrientation() );
        assertEquals( Unit.DEGREE, ax[1].getUnits() );

        testCRS = dProvider.getCRSByID( "SOME_DUMMY_CODE" );
        assertTrue( testCRS == null );
        
//        dProvider.getAvailableCRSs();
        
        // disconnecting
        dProvider.closeDatabaseConnection();

    }
}
