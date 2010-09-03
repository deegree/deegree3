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

package org.deegree.cs.projections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.vecmath.Point2d;

import org.deegree.cs.CRSCodeType;
import org.deegree.cs.components.Axis;
import org.deegree.cs.components.Ellipsoid;
import org.deegree.cs.components.GeodeticDatum;
import org.deegree.cs.components.Unit;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.exceptions.ProjectionException;
import org.deegree.cs.transformations.helmert.Helmert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>ProjectionTest</code> is the base for all accuracy tests, this class doesn't really test anything.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class ProjectionBase {

    private static final double DELTA = 0.0000001;

    /**
     * Standard axis of a geographic crs
     */
    protected static final Axis[] axis_degree = new Axis[] { new Axis( Unit.DEGREE, "lon", Axis.AO_EAST ),
                                                            new Axis( Unit.DEGREE, "lat", Axis.AO_NORTH ) };

    /**
     * A common ellipsoid also known as GRS 1980
     */
    protected static final Ellipsoid ellipsoid_7019 = new Ellipsoid( 6378137.0, Unit.METRE, 298.257222101,
                                                                     new CRSCodeType[] { new CRSCodeType( "7019",
                                                                                                          "ESPG" ) } );

    /**
     * No wgs84 conversion needed.
     */
    /**
     * No wgs84 conversion needed.
     */
    protected static final Helmert wgs_1188 = new Helmert( GeographicCRS.WGS84, GeographicCRS.WGS84,
                                                           new CRSCodeType[] { new CRSCodeType( "1188", "ESPG" ) } );

    /**
     * European Terrestrial Reference System 1989
     */
    protected static final GeodeticDatum datum_6258 = new GeodeticDatum(
                                                                         ellipsoid_7019,
                                                                         wgs_1188,
                                                                         new CRSCodeType[] { new CRSCodeType( "6258",
                                                                                                              "ESPG" ) } );

    /**
     * Also known as ETRS89
     */
    protected static final GeographicCRS geographic_4258 = new GeographicCRS(
                                                                              datum_6258,
                                                                              axis_degree,
                                                                              new CRSCodeType[] { new CRSCodeType(
                                                                                                                   "4258",
                                                                                                                   "ESPG" ) } );

    private static Logger LOG = LoggerFactory.getLogger( ProjectionBase.class );

    private final static double METER_EPSILON = 0.15;

    private final static double DEGREE_EPSILON = 0.0000015;

    private final static Point2d epsilon = new Point2d( METER_EPSILON, METER_EPSILON );

    private final static Point2d epsilonDegree = new Point2d( DEGREE_EPSILON, DEGREE_EPSILON );

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
     * @param origPoint
     *            to transform
     * @param referencePoint
     *            to which the result shall be checked.
     * @param forwardEpsilons
     *            for each axis
     * @param projection
     *            to test.
     * @param inverse
     *            true if the inverse projection should be tested.
     * @return the result string.
     * @throws ProjectionException
     * @throws AssertionError
     *             if one of the axis of the transformed point do not lie within the given epsilon range.
     */
    protected String doAccuracyTest( Point2d origPoint, Point2d referencePoint, Point2d forwardEpsilons,
                                     Projection projection, boolean inverse )
                            throws ProjectionException {
        assertNotNull( projection );
        assertNotNull( origPoint );
        assertNotNull( referencePoint );
        assertNotNull( forwardEpsilons );
        Unit unitToUse = projection.getUnits();

        // Point point = GeometryFactory.createPoint( origPoint.x, origPoint.y, origPoint.z, sourceCRS );
        Point2d result = null;
        if ( inverse ) {
            result = projection.doInverseProjection( origPoint.x, origPoint.y );
            unitToUse = Unit.DEGREE;
        } else {
            result = projection.doProjection( origPoint.x, origPoint.y );
        }

        assertNotNull( result );

        boolean xFail = Math.abs( result.x - referencePoint.x ) > forwardEpsilons.x;
        String xString = createEpsilonString( xFail, result.x, referencePoint.x, forwardEpsilons.x,
                                              new Axis( unitToUse, "x", Axis.AO_EAST ) );
        boolean yFail = Math.abs( result.y - referencePoint.y ) > forwardEpsilons.y;
        String yString = createEpsilonString( yFail, result.y, referencePoint.y, forwardEpsilons.y,
                                              new Axis( unitToUse, "y", Axis.AO_NORTH ) );

        StringBuilder sb = new StringBuilder();
        if ( xFail || yFail ) {
            sb.append( "[FAILED]\n" );
        } else {
            sb.append( "[SUCCESS]\n" );
        }
        sb.append( xString );
        sb.append( "\n" ).append( yString );
        if ( xFail || yFail ) {
            throw new AssertionError( sb.toString() );
        }
        return sb.toString();
    }

    /**
     * Do a forward and inverse accuracy test, using the standard epsilon values.
     * 
     * @param projection
     * @param source
     * @param target
     * @throws ProjectionException
     */
    protected void doForwardAndInverse( Projection projection, Point2d source, Point2d target )
                            throws ProjectionException {
        StringBuilder output = new StringBuilder();
        output.append( "Projecting forward/inverse -> '" );
        output.append( projection.getName() ).append( "'.\n" );

        // forward projection.
        boolean forwardSuccess = true;
        try {
            output.append( "Forward Projection: " );
            output.append( doAccuracyTest( source, target, epsilon, projection, false ) );
        } catch ( AssertionError ae ) {
            output.append( ae.getLocalizedMessage() );
            forwardSuccess = false;
        }

        // inverse projection.
        boolean inverseSuccess = true;
        try {
            output.append( "\nInverse Projection: " );
            output.append( doAccuracyTest( target, source, epsilonDegree, projection, true ) );
        } catch ( AssertionError ae ) {
            output.append( ae.getLocalizedMessage() );
            inverseSuccess = false;
        }
        LOG.debug( output.toString() );
        if ( !forwardSuccess || !inverseSuccess ) {
            LOG.warn( output.toString() );
        }

        assertEquals( true, forwardSuccess );
        assertEquals( true, inverseSuccess );

    }

    /**
     * Helper method to test if the projections have given values, and do not change internally.
     * 
     * @param toBeTested
     * @param falseNorthing
     * @param falseEasting
     * @param naturalOrigin
     * @param units
     * @param scale
     * @param conformal
     * @param equalArea
     * @param name
     *            of the projection
     */
    protected void consistencyTest( Projection toBeTested, double falseNorthing, double falseEasting,
                                    Point2d naturalOrigin, Unit units, double scale, boolean conformal,
                                    boolean equalArea, String name ) {
        assertEquals( falseNorthing, toBeTested.getFalseNorthing(), DELTA );
        assertEquals( falseEasting, toBeTested.getFalseEasting(), DELTA );
        assertEquals( naturalOrigin, toBeTested.getNaturalOrigin() );
        assertEquals( units, toBeTested.getUnits() );
        assertEquals( scale, toBeTested.getScale(), DELTA );
        assertEquals( conformal, toBeTested.isConformal() );
        assertEquals( equalArea, toBeTested.isEqualArea() );

        // some other checks.
        assertEquals( Math.cos( naturalOrigin.y ), toBeTested.getCosphi0(), DELTA );
        assertEquals( Math.sin( naturalOrigin.y ), toBeTested.getSinphi0(), DELTA );
        assertEquals( name, toBeTested.getImplementationName() );
    }
}
