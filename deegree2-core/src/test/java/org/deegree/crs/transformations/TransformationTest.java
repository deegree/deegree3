//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/crs/transformations/TransformationTest.java $
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

package org.deegree.crs.transformations;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import junit.framework.TestCase;

import org.deegree.crs.Identifiable;
import org.deegree.crs.components.Axis;
import org.deegree.crs.components.Ellipsoid;
import org.deegree.crs.components.GeodeticDatum;
import org.deegree.crs.components.Unit;
import org.deegree.crs.coordinatesystems.CompoundCRS;
import org.deegree.crs.coordinatesystems.GeocentricCRS;
import org.deegree.crs.coordinatesystems.GeographicCRS;
import org.deegree.crs.coordinatesystems.ProjectedCRS;
import org.deegree.crs.projections.Projection;
import org.deegree.crs.projections.ProjectionTest;
import org.deegree.crs.projections.azimuthal.StereographicAlternative;
import org.deegree.crs.projections.cylindric.Mercator;
import org.deegree.crs.projections.cylindric.TransverseMercator;
import org.deegree.crs.transformations.helmert.Helmert;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CRSTransformationException;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;

/**
 * <code>TransformationTest</code> a junit test class for testing the accuracy of various transformations.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author: aschmitz $
 * 
 * @version $Revision: 30132 $, $Date: 2011-03-22 16:00:06 +0100 (Di, 22 Mrz 2011) $
 * 
 */
public class TransformationTest extends TestCase {

    private static ILogger LOG = LoggerFactory.getLogger( ProjectionTest.class );

    private final static double METER_EPSILON = 0.15;

    private final static double DEGREE_EPSILON = 0.0000015;

    private final static Point3d epsilon = new Point3d( METER_EPSILON, METER_EPSILON, 0.4 );

    private final static Point3d epsilonDegree = new Point3d( DEGREE_EPSILON, DEGREE_EPSILON, 0.4 );

    /**
     * Used axis
     */
    private final static Axis[] axis_degree = new Axis[] { new Axis( Unit.DEGREE, "lon", Axis.AO_EAST ),
                                                          new Axis( Unit.DEGREE, "lat", Axis.AO_NORTH ) };

    private final static Axis[] axis_projection = new Axis[] { new Axis( "x", Axis.AO_EAST ),
                                                              new Axis( "y", Axis.AO_NORTH ) };

    private final static Axis[] axis_geocentric = new Axis[] { new Axis( Unit.METRE, "X", Axis.AO_FRONT ),
                                                              new Axis( Unit.METRE, "Y", Axis.AO_EAST ),
                                                              new Axis( Unit.METRE, "Z", Axis.AO_NORTH ) };

    private final static Axis heightAxis = new Axis( Unit.METRE, "z", Axis.AO_UP );

    /**
     * Used ellipsoids
     */
    private final static Ellipsoid ellipsoid_7004 = new Ellipsoid( 6377397.155, Unit.METRE, 299.1528128,
                                                                   new String[] { "EPSG:7004" } );

    private final static Ellipsoid ellipsoid_7019 = new Ellipsoid( 6378137.0, Unit.METRE, 298.257222101,
                                                                   new String[] { "EPSG:7019" } );

    /**
     * Used to wgs
     */
    private final static Helmert wgs_56 = new Helmert( 565.04, 49.91, 465.84, -0.40941295127179994, 0.3608190255680464,
                                                       -1.8684910003505757, 4.0772, GeographicCRS.WGS84,
                                                       GeographicCRS.WGS84, new String[] { "TOWGS_56" } );

    private final static Helmert wgs_1188 = new Helmert( GeographicCRS.WGS84, GeographicCRS.WGS84,
                                                         new String[] { "EPSG:1188" } );

    private final static Helmert wgs_1777 = new Helmert( 598.1, 73.7, 418.2, 0.202, 0.045, -2.455, 6.7,
                                                         GeographicCRS.WGS84, GeographicCRS.WGS84,
                                                         new String[] { "EPSG:1777" } );

    /**
     * Used datums
     */
    private final static GeodeticDatum datum_171 = new GeodeticDatum( ellipsoid_7004, wgs_56,
                                                                      new String[] { "DATUM_171" } );

    private final static GeodeticDatum datum_6258 = new GeodeticDatum( ellipsoid_7019, wgs_1188,
                                                                       new String[] { "EPSG:6258" } );

    private final static GeodeticDatum datum_6314 = new GeodeticDatum( ellipsoid_7004, wgs_1777,
                                                                       new String[] { "EPSG:6314" } );

    private final static GeodeticDatum datum_6171 = new GeodeticDatum( ellipsoid_7019, wgs_1188,
                                                                       new String[] { "EPSG:6171" } );

    /**
     * Used geocentric crs's
     */
    private final static GeocentricCRS geocentric_4964 = new GeocentricCRS(
                                                                            datum_6171,
                                                                            axis_geocentric,
                                                                            new Identifiable(
                                                                                              new String[] { "EPSG:4964" } ) );

    private final static GeocentricCRS geocentric_dummy = new GeocentricCRS(
                                                                             datum_6314,
                                                                             axis_geocentric,
                                                                             new Identifiable(
                                                                                               new String[] { "NO_REAL_GEOCENTRIC" } ) );

    /**
     * Used geographic crs's
     */
    private final static GeographicCRS geographic_204 = new GeographicCRS( datum_171, axis_degree,
                                                                           new String[] { "GEO_CRS_204" } );

    private final static GeographicCRS geographic_4258 = new GeographicCRS( datum_6258, axis_degree,
                                                                            new String[] { "EPSG:4258" } );

    private final static GeographicCRS geographic_4314 = new GeographicCRS( datum_6314, axis_degree,
                                                                            new String[] { "EPSG:4314" } );

    /**
     * Used projections
     */
    private final static Projection projection_28992 = new StereographicAlternative(
                                                                                     geographic_204,
                                                                                     463000.0,
                                                                                     155000.0,
                                                                                     new Point2d(
                                                                                                  Math.toRadians( 5.38763888888889 ),
                                                                                                  Math.toRadians( 52.15616055555555 ) ),
                                                                                     Unit.METRE, 0.9999079 );

    private final static Projection projection_25832 = new TransverseMercator( true, geographic_4258, 0, 500000.0,
                                                                               new Point2d( Math.toRadians( 9 ), 0 ),
                                                                               Unit.METRE, 0.9996 );

    private final static Projection projection_31467 = new TransverseMercator( geographic_4314, 0, 3500000.0,
                                                                               new Point2d( Math.toRadians( 9 ),
                                                                                            Math.toRadians( 0 ) ),
                                                                               Unit.METRE );

    private final static Projection projection_OSM = new Mercator( GeographicCRS.WGS84, 0, 0, new Point2d( 0, 0 ),
                                                                   Unit.METRE, 1 );

    /**
     * Used projected crs's
     */
    private final static ProjectedCRS projected_28992 = new ProjectedCRS( projection_28992, axis_projection,
                                                                          new String[] { "EPSG:28992" } );

    private final static ProjectedCRS projected_25832 = new ProjectedCRS( projection_25832, axis_projection,
                                                                          new String[] { "EPSG:25832" } );

    private final static ProjectedCRS projected_31467 = new ProjectedCRS( projection_31467, axis_projection,
                                                                          new String[] { "EPSG:31467" } );

    private final static ProjectedCRS projected_OSM = new ProjectedCRS( projection_OSM, axis_projection,
                                                                        new String[] { "OSM" } );

    /**
     * Creates a {@link GeoTransformer} for the given coordinate system.
     * 
     * @param targetCrs
     *            to which incoming coordinates will be transformed.
     * @return the transformer which is able to transform coordinates to the given crs..
     */
    private GeoTransformer getGeotransformer( CoordinateSystem targetCrs ) {
        assertNotNull( targetCrs );
        return new GeoTransformer( targetCrs );
    }

    /**
     * Creates an epsilon string with following layout axis.getName: origPoint - resultPoint = epsilon Unit.getName().
     * 
     * @param sourceCoordinate
     *            on the given axis
     * @param targetCoordinate
     *            on the given axis
     * @param allowedEpsilon
     *            defined by test.
     * @param axis
     *            of the coordinates
     * @return a String representation.
     */
    private String createEpsilonString( boolean failure, double sourceCoordinate, double targetCoordinate,
                                        double allowedEpsilon, Axis axis ) {
        double epsilon = sourceCoordinate - targetCoordinate;
        StringBuilder sb = new StringBuilder( 400 );
        sb.append( axis.getName() ).append( " (result - orig = error [allowedError]): " );
        sb.append( sourceCoordinate ).append( " - " ).append( targetCoordinate );
        sb.append( " = " ).append( epsilon ).append( axis.getUnits() );
        sb.append( " [" ).append( allowedEpsilon ).append( axis.getUnits() ).append( "]" );
        if ( failure ) {
            sb.append( " [FAILURE]" );
        }
        return sb.toString();
    }

    /**
     * Transforms the given coordinates in the sourceCRS to the given targetCRS and checks if they lie within the given
     * epsilon range to the reference point. If successful the transformed will be logged.
     * 
     * @param sourcePoint
     *            to transform
     * @param targetPoint
     *            to which the result shall be checked.
     * @param epsilons
     *            for each axis
     * @param sourceCRS
     *            of the origPoint
     * @param targetCRS
     *            of the targetPoint.
     * @return the string containing the success string.
     * @throws CRSTransformationException
     * @throws AssertionError
     *             if one of the axis of the transformed point do not lie within the given epsilon range.
     */
    private String doAccuracyTest( Point3d sourcePoint, Point3d targetPoint, Point3d epsilons,
                                   CoordinateSystem sourceCRS, CoordinateSystem targetCRS )
                            throws CRSTransformationException {
        assertNotNull( sourceCRS );
        assertNotNull( targetCRS );
        assertNotNull( sourcePoint );
        assertNotNull( targetPoint );
        assertNotNull( epsilons );

        GeoTransformer transformer = getGeotransformer( targetCRS );
        Point point = GeometryFactory.createPoint( sourcePoint.x, sourcePoint.y, sourcePoint.z, sourceCRS );

        Point pp = (Point) transformer.transform( point );
        assertNotNull( pp );
        boolean xFail = Math.abs( pp.getX() - targetPoint.x ) > epsilons.x;
        String xString = createEpsilonString( xFail, pp.getX(), targetPoint.x, epsilons.x,
                                              targetCRS.getCRS().getAxis()[0] );
        boolean yFail = Math.abs( pp.getY() - targetPoint.y ) > epsilons.y;
        String yString = createEpsilonString( yFail, pp.getY(), targetPoint.y, epsilons.y,
                                              targetCRS.getCRS().getAxis()[1] );

        // Z-Axis if available.
        boolean zFail = false;
        String zString = "";
        if ( targetCRS.getDimension() == 3 ) {
            zFail = Math.abs( pp.getZ() - targetPoint.z ) > epsilons.z;
            zString = createEpsilonString( zFail, pp.getZ(), targetPoint.z, epsilons.z, targetCRS.getCRS().getAxis()[2] );
        }
        StringBuilder sb = new StringBuilder();
        if ( xFail || yFail || zFail ) {
            sb.append( "[FAILED] " );
        } else {
            sb.append( "[SUCCESS] " );
        }
        sb.append( "Transformation (" ).append( sourceCRS.getIdentifier() );
        sb.append( " -> " ).append( targetCRS.getIdentifier() ).append( ")\n" );
        sb.append( xString );
        sb.append( "\n" ).append( yString );
        if ( targetCRS.getDimension() == 3 ) {
            sb.append( "\n" ).append( zString );
        }
        if ( xFail || yFail || zFail ) {
            throw new AssertionError( sb.toString() );
        }
        return sb.toString();
    }

    /**
     * Do an forward and inverse accuracy test.
     * 
     * @param sourceCRS
     * @param targetCRS
     * @param source
     * @param target
     * @param forwardEpsilon
     * @param inverseEpsilon
     * @throws CRSTransformationException
     */
    private void doForwardAndInverse( org.deegree.crs.coordinatesystems.CoordinateSystem sourceCRS,
                                      org.deegree.crs.coordinatesystems.CoordinateSystem targetCRS, Point3d source,
                                      Point3d target, Point3d forwardEpsilon, Point3d inverseEpsilon )
                            throws CRSTransformationException {
        StringBuilder output = new StringBuilder();
        output.append( "Transforming forward crs with id: '" );
        output.append( sourceCRS.getIdentifier() );
        output.append( "' and crs with id: '" );
        output.append( targetCRS.getIdentifier() );
        output.append( "'." );

        // forward transform.
        boolean forwardSuccess = true;
        try {
            output.append( doAccuracyTest( source, target, forwardEpsilon, CRSFactory.create( sourceCRS ),
                                           CRSFactory.create( targetCRS ) ) );
        } catch ( AssertionError ae ) {
            output.append( ae.getLocalizedMessage() );
            forwardSuccess = false;
        }
        if ( !forwardSuccess ) {
            LOG.logError( output.toString() );
        }

        // inverse transform.
        output = new StringBuilder( "Transforming inverse crs with id: '" );
        output.append( targetCRS.getIdentifier() );
        output.append( "' and crs with id: '" );
        output.append( sourceCRS.getIdentifier() );
        output.append( "'." );
        boolean inverseSuccess = true;
        try {
            output.append( doAccuracyTest( target, source, inverseEpsilon, CRSFactory.create( targetCRS ),
                                           CRSFactory.create( sourceCRS ) ) );
        } catch ( AssertionError ae ) {
            output.append( ae.getLocalizedMessage() );
            inverseSuccess = false;
        }
        if ( !inverseSuccess ) {
            LOG.logError( output.toString() );
        }
        // LOG.logInfo( output.toString() );

        assertEquals( true, forwardSuccess );
        assertEquals( true, inverseSuccess );

    }

    /**
     * Test the forward/inverse transformation from a compound_projected crs (EPSG:28992) to another compound_projected
     * crs (EPSG:25832)
     * 
     * @throws CRSTransformationException
     */
    // removed due to crs problems
    // public void testCompoundToCompound()
    // throws CRSTransformationException {
    // // Source crs espg:28992
    // CompoundCRS sourceCRS = new CompoundCRS( heightAxis, projected_28992, 20,
    // new Identifiable( new String[] { projected_28992.getIdentifier()
    // + "_compound" } ) );
    //
    // // Target crs espg:25832
    // CompoundCRS targetCRS = new CompoundCRS( heightAxis, projected_25832, 20,
    // new Identifiable( new String[] { projected_25832.getIdentifier()
    // + "_compound" } ) );
    //
    // // reference created with coord tool from http://www.rdnap.nl/ (NL/Amsterdam/dam)
    // Point3d sourcePoint = new Point3d( 121397.572, 487325.817, 6.029 );
    // Point3d targetPoint = new Point3d( 220513.823d, 5810438.891, 49 );
    // doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, epsilon, epsilon );
    // }

    /**
     * Test the transformation from a compound_projected crs (EPSG:28992_compound) to a geographic crs (EPSG:4258)
     * coordinate system .
     * 
     * @throws CRSTransformationException
     */
    // removed due to crs problems
    // public void testCompoundToGeographic()
    // throws CRSTransformationException {
    // // Source crs espg:28992
    // CompoundCRS sourceCRS = new CompoundCRS( heightAxis, projected_28992, 20,
    // new Identifiable( new String[] { projected_28992.getIdentifier()
    // + "_compound" } ) );
    //
    // // Target crs espg:4258
    // GeographicCRS targetCRS = geographic_4258;
    //
    // // reference created with coord tool from http://www.rdnap.nl/ denoting (NL/Groningen/lichtboei)
    // Point3d sourcePoint = new Point3d( 236694.856, 583952.500, 1.307 );
    // Point3d targetPoint = new Point3d( 6.610765, 53.235916, 42 );
    //
    // doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, epsilonDegree, new Point3d( METER_EPSILON,
    // 0.17, 0.6 ) );
    // }

    /**
     * Test the forward/inverse transformation from a compound_projected crs (EPSG:31467) to a geocentric crs
     * (EPSG:4964)
     * 
     * @throws CRSTransformationException
     */
    // removed due to crs problems
    // public void testCompoundToGeocentric()
    // throws CRSTransformationException {
    //
    // // source crs epsg:31467
    // CompoundCRS sourceCRS = new CompoundCRS( heightAxis, projected_31467, 20,
    // new Identifiable( new String[] { projected_31467.getIdentifier()
    // + "_compound" } ) );
    //
    // // Target crs EPSG:4964
    // GeocentricCRS targetCRS = geocentric_4964;
    //
    // // do the testing
    // Point3d sourcePoint = new Point3d( 3532465.57, 5301523.49, 817 );
    // Point3d targetPoint = new Point3d( 4230602.192492622, 702858.4858986374, 4706428.360722791 );
    //
    // doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, epsilon, epsilon );
    // }

    /**
     * Test the forward/inverse transformation from a compound_geographic crs (EPSG:4326) to a projected crs
     * (EPSG:31467)
     * 
     * @throws CRSTransformationException
     */
    public void testCompoundToProjected()
                            throws CRSTransformationException {

        // Source WGS:84_compound
        CompoundCRS sourceCRS = new CompoundCRS( heightAxis, GeographicCRS.WGS84, 20,
                                                 new Identifiable( new String[] { GeographicCRS.WGS84.getIdentifier()
                                                                                  + "_compound" } ) );

        // Target EPSG:31467
        ProjectedCRS targetCRS = projected_31467;

        // kind regards to vodafone for supplying reference points.
        Point3d sourcePoint = new Point3d( 9.432778, 47.851111, 870.6 );
        Point3d targetPoint = new Point3d( 3532465.57, 5301523.49, 817 );

        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, epsilon, epsilonDegree );
    }

    /**
     * Test the forward/inverse transformation from a projected crs (EPSG:28992) to another projected crs (EPSG:25832)
     * 
     * @throws CRSTransformationException
     */
    public void testProjectedToProjected()
                            throws CRSTransformationException {
        // Source crs espg:28992
        ProjectedCRS sourceCRS = projected_28992;

        // Target crs espg:25832
        ProjectedCRS targetCRS = projected_25832;

        // reference created with coord tool from http://www.rdnap.nl/ (NL/hoensbroek)
        Point3d sourcePoint = new Point3d( 191968.31999475454, 326455.285005203, Double.NaN );
        Point3d targetPoint = new Point3d( 283065.845, 5646206.125, Double.NaN );

        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, epsilon, epsilon );
    }

    /**
     * Test the forward/inverse transformation from a projected crs (EPSG:31467) to another projected crs (OSM)
     * 
     * @throws CRSTransformationException
     */
    public void testProjectedToProjectedOSM()
                            throws CRSTransformationException {
        // Source crs espg:28992
        ProjectedCRS sourceCRS = projected_31467;

        // Target crs espg:25832
        ProjectedCRS targetCRS = projected_OSM;

        // No reference, using proj4 values.
        Point3d sourcePoint = new Point3d( 3532465.57, 5301523.49, Double.NaN );
        Point3d targetPoint = new Point3d( 1050052.04373, 6050425.53778, Double.NaN );

        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, epsilon, epsilon );
    }

    /**
     * Test the forward/inverse transformation from a projected crs (EPSG:31467) to a geographic crs (EPSG:4258)
     * 
     * @throws CRSTransformationException
     */
    public void testProjectedToGeographic()
                            throws CRSTransformationException {
        // Source crs espg:31467
        ProjectedCRS sourceCRS = projected_31467;

        // Target crs espg:4258
        GeographicCRS targetCRS = geographic_4258;

        // with kind regards to vodafone for supplying reference points
        Point3d sourcePoint = new Point3d( 3532465.57, 5301523.49, Double.NaN );
        Point3d targetPoint = new Point3d( 9.432778, 47.851111, Double.NaN );

        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, epsilonDegree, epsilon );
    }

    /**
     * Test the forward/inverse transformation from a projected crs (EPSG:28992) to a geocentric crs (EPSG:4964)
     * 
     * @throws CRSTransformationException
     */
    public void testProjectedToGeocentric()
                            throws CRSTransformationException {
        ProjectedCRS sourceCRS = projected_28992;

        // Target crs EPSG:4964
        GeocentricCRS targetCRS = geocentric_4964;

        // do the testing created reference points with deegree (not a fine test!!)
        Point3d sourcePoint = new Point3d( 191968.31999475454, 326455.285005203, Double.NaN );
        Point3d targetPoint = new Point3d( 4006964.9993508584, 414997.8479008863, 4928439.8089122595 );

        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, epsilon, epsilon );
    }

    /**
     * Test the forward/inverse transformation from a geographic crs (EPSG:4314) to another geographic crs (EPSG:4258)
     * 
     * @throws CRSTransformationException
     */
    public void testGeographicToGeographic()
                            throws CRSTransformationException {

        // source crs epsg:4314
        GeographicCRS sourceCRS = geographic_4314;
        // target crs epsg:4258
        GeographicCRS targetCRS = geographic_4258;

        // with kind regards to vodafone for supplying reference points.
        Point3d sourcePoint = new Point3d( 8.83319047, 54.90017335, Double.NaN );
        Point3d targetPoint = new Point3d( 8.83213115, 54.89846442, Double.NaN );

        // do the testing
        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, epsilonDegree, epsilonDegree );
    }

    /**
     * Test the forward/inverse transformation from a geographic crs (EPSG:4314) to a geocentric crs (EPSG:4964)
     * 
     * @throws CRSTransformationException
     * 
     * @throws CRSTransformationException
     */
    public void testGeographicToGeocentric()
                            throws CRSTransformationException {
        // source crs epsg:4314
        GeographicCRS sourceCRS = geographic_4314;
        // target crs epsg:4964
        GeocentricCRS targetCRS = geocentric_4964;

        // created with deegree not a fine reference
        Point3d sourcePoint = new Point3d( 8.83319047, 54.90017335, Double.NaN );
        Point3d targetPoint = new Point3d( 3632280.522352362, 564392.6943947134, 5194921.3092999635 );
        // do the testing
        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, epsilon, epsilonDegree );
    }

    /**
     * Test the forward/inverse transformation from a geocentric (dummy based on bessel) to another geocentric crs
     * (EPSG:4964 based on etrs89)
     * 
     * @throws CRSTransformationException
     */
    // removed due to crs problems
    // public void testGeocentricToGeocentric()
    // throws CRSTransformationException {
    // // source crs is a dummy based on the epsg:4314 == bessel datum.
    // GeocentricCRS sourceCRS = geocentric_dummy;
    //
    // // target crs epsg:4964 etrs89 based
    // GeocentricCRS targetCRS = geocentric_4964;
    //
    // // created with deegree not a fine reference
    // Point3d sourcePoint = new Point3d( 3631650.239831989, 564363.5250884632, 5194468.545970947 );
    // Point3d targetPoint = new Point3d( 3632280.522352362, 564392.6943947134, 5194921.3092999635 );
    //
    // // do the testing
    // doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, epsilon, epsilonDegree );
    // }

    /**
     * Test a transformation using the CRSFactory twice, to ensure cache consistency.
     * 
     * @throws CRSTransformationException
     * @throws UnknownCRSException
     */
    public void testUsingDeegreeCRSProvider()
                            throws CRSTransformationException, UnknownCRSException {
        CoordinateSystem sourceCRS = CRSFactory.create( "EPSG:31467" );
        assertNotNull( sourceCRS );
        assertTrue( sourceCRS.getCRS() instanceof ProjectedCRS );

        CoordinateSystem targetCRS = CRSFactory.create( "EPSG:4258" );
        assertNotNull( targetCRS );
        assertTrue( targetCRS.getCRS() instanceof GeographicCRS );

        // with kind regards to vodafone for supplying reference points
        Point3d sourcePoint = new Point3d( 3532465.57, 5301523.49, Double.NaN );
        Point3d targetPoint = new Point3d( 9.432778, 47.851111, Double.NaN );

        doForwardAndInverse( sourceCRS.getCRS(), targetCRS.getCRS(), sourcePoint, targetPoint, epsilonDegree, epsilon );

        sourceCRS = CRSFactory.create( "EPSG:31467" );
        assertNotNull( sourceCRS );
        assertTrue( sourceCRS.getCRS() instanceof ProjectedCRS );

        targetCRS = CRSFactory.create( "EPSG:4258" );
        assertNotNull( targetCRS );
        assertTrue( targetCRS.getCRS() instanceof GeographicCRS );

        doForwardAndInverse( sourceCRS.getCRS(), targetCRS.getCRS(), sourcePoint, targetPoint, epsilonDegree, epsilon );
    }
}
