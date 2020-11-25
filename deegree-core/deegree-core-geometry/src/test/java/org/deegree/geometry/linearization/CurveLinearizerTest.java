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

package org.deegree.geometry.linearization;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKTReader;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.segments.Arc;
import org.deegree.geometry.primitive.segments.Circle;
import org.deegree.geometry.primitive.segments.CubicSpline;
import org.deegree.geometry.primitive.segments.CurveSegment;
import org.deegree.geometry.standard.curvesegments.DefaultArc;
import org.deegree.geometry.standard.curvesegments.DefaultCubicSpline;
import org.deegree.geometry.standard.points.PointsList;
import org.deegree.geometry.standard.primitive.DefaultCurve;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.io.ParseException;

/**
 * Tests for {@link CurveLinearizer}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CurveLinearizerTest {

    private static final Logger LOG = getLogger( CurveLinearizerTest.class );

    private org.deegree.geometry.GeometryFactory geomFac;

    private CurveLinearizer linearizer;

    private boolean outputWKT = false;

    @Before
    public void setUp() {
        geomFac = new org.deegree.geometry.GeometryFactory();
        linearizer = new CurveLinearizer( geomFac );
    }

    private double getDistance( Point p0, Point p1 ) {
        double dx = p1.get0() - p0.get0();
        double dy = p1.get1() - p0.get1();
        return Math.sqrt( dx * dx + dy * dy );
    }

    private void arePointsOnCircle( Point center, Point p0, Point p1, Point p2 ) {
        double dp0 = getDistance( center, p0 );
        double dp1 = getDistance( center, p1 );
        double dp2 = getDistance( center, p2 );
        Assert.assertEquals( dp0, dp1, 1E-9 );
        Assert.assertEquals( dp1, dp2, 1E-9 );
    }

    private void testLinearization( Point p0, Point p1, Point p2, int numPositions ) {
        Point center = linearizer.calcCircleCenter( p0, p1, p2 );
        double radius = getDistance( center, p0 );

        Circle circle = geomFac.createCircle( p0, p1, p2 );
        Points positions = linearizer.linearize( circle, new NumPointsCriterion( numPositions ) ).getControlPoints();
        for ( Point point : positions ) {
            double dist = getDistance( center, point );
            Assert.assertEquals( radius, dist, 1E-9 );
        }

        Point lastPoint = null;
        for ( Point point : positions ) {
            if ( lastPoint != null ) {
                Math.sqrt( ( point.get0() - lastPoint.get0() ) * ( point.get0() - lastPoint.get0() )
                           + ( point.get1() - lastPoint.get1() ) * ( point.get1() - lastPoint.get1() ) );
            }
            lastPoint = point;
        }
    }

    private void testLinearizationWithErrorCriterion( Point p0, Point p1, Point p2, double error, int maxNumPoints ) {
        Point center = linearizer.calcCircleCenter( p0, p1, p2 );
        double radius = getDistance( center, p0 );

        Arc arc = geomFac.createArc( p0, p1, p2 );
        Points positions = linearizer.linearize( arc, new MaxErrorCriterion( error, maxNumPoints ) ).getControlPoints();

        Point start = null;
        for ( Point point : positions ) {
            double dist = getDistance( center, point );
            // every segment start/end point must be *on* arc
            Assert.assertEquals( radius, dist, 0.00000001 );

            if ( start != null ) {
                Point end = point;
                // test distance of mid point of segment as well
                double minX = Math.min( start.get0(), end.get0() );
                double maxX = Math.max( start.get0(), end.get0() );
                double minY = Math.min( start.get1(), end.get1() );
                double maxY = Math.max( start.get1(), end.get1() );
                double[] coords = new double[] { ( maxX - minX ) / 2 + minX, ( maxY - minY ) / 2 + minY };
                Point mid = new DefaultPoint( null, start.getCoordinateSystem(), null, coords );
                dist = getDistance( center, mid );
                double actualError = Math.abs( radius - dist );
                Assert.assertTrue( "Actual error is " + actualError + ", allowed error is " + error,
                                   actualError < error );
            }
            start = point;
        }
    }

    @Test
    public void linearizeArcCircle() {
        List<CurveSegment> segments = new ArrayList<CurveSegment>();
        segments.add( createArc( 384776.006, 5740367.157, 384774.079, 5740367.526, 384772.318, 5740368.390 ) );
        segments.add( createArc( 384772.318, 5740368.390, 384770.240, 5740370.494, 384769.157, 5740373.245 ) );
        segments.add( createArc( 384769.157, 5740373.245, 384769.672, 5740377.484, 384772.453, 5740380.726 ) );
        segments.add( createArc( 384772.453, 5740380.726, 384780.502, 5740380.644, 384783.672, 5740373.244 ) );
        segments.add( createArc( 384783.672, 5740373.244, 384780.995, 5740368.743, 384776.006, 5740367.157 ) );
        Curve curve = new DefaultCurve( null, null, null, segments );
        linearizer.linearize( curve, new MaxErrorCriterion( 0.0001, 150 ) );
        // TODO check if error is within limits
    }

    private Arc createArc( double x1, double y1, double x2, double y2, double x3, double y3 ) {
        Point p1 = new DefaultPoint( null, null, null, new double[] { x1, y1 } );
        Point p2 = new DefaultPoint( null, null, null, new double[] { x2, y2 } );
        Point p3 = new DefaultPoint( null, null, null, new double[] { x3, y3 } );
        return new DefaultArc( p1, p2, p3 );
    }

    /**
     * Tests if {@link CurveLinearizer#isClockwise(Point, Point, Point)} determines the correct point order.
     */
    @Test
    public void testIsClockwise() {
        Point p0 = geomFac.createPoint( null, new double[] { -2, 0 }, null );
        Point p1 = geomFac.createPoint( null, new double[] { 0, 2 }, null );
        Point p2 = geomFac.createPoint( null, new double[] { 2, 0 }, null );
        Assert.assertTrue( linearizer.isClockwise( p0, p1, p2 ) );
        Assert.assertFalse( linearizer.isClockwise( p0, p2, p1 ) );

        p0 = geomFac.createPoint( null, new double[] { -2, 0 }, null );
        p1 = geomFac.createPoint( null, new double[] { 0, -2 }, null );
        p2 = geomFac.createPoint( null, new double[] { 2, 0 }, null );
        Assert.assertFalse( linearizer.isClockwise( p0, p1, p2 ) );
    }

    /**
     * Tests if {@link CurveLinearizer#calcCircleCenter(Point, Point, Point)} finds the correct midpoint.
     */
    @Test
    public void testFindCircleCenter() {
        Point p0 = geomFac.createPoint( null, new double[] { 8, -1 }, null );
        Point p1 = geomFac.createPoint( null, new double[] { 3, 1.6 }, null );
        Point p2 = geomFac.createPoint( null, new double[] { -110, 16.77777 }, null );
        Point center = linearizer.calcCircleCenter( p0, p1, p2 );
        arePointsOnCircle( center, p0, p1, p2 );
    }

    /**
     * Tests if interpolate(Point, Point, Point, int, boolean) produces sequences of positions that coincide with the
     * circle arc.
     */
    @Test
    public void testLinearizeCircle() {
        testLinearization( geomFac.createPoint( null, new double[] { 0, 2 }, null ),
                           geomFac.createPoint( null, new double[] { 2, 0 }, null ),
                           geomFac.createPoint( null, new double[] { -2, 0 }, null ), 10 );
        testLinearization( geomFac.createPoint( null, new double[] { 0, 2 }, null ),
                           geomFac.createPoint( null, new double[] { 2, 0 }, null ),
                           geomFac.createPoint( null, new double[] { -2, 0 }, null ), 1000 );
        testLinearizationWithErrorCriterion( geomFac.createPoint( null, new double[] { 0, 2 }, null ),
                                             geomFac.createPoint( null, new double[] { 2, 0 }, null ),
                                             geomFac.createPoint( null, new double[] { -2, 0 }, null ), 0.001, 1000 );
        testLinearization( geomFac.createPoint( null, new double[] { 8, -1 }, null ),
                           geomFac.createPoint( null, new double[] { 3, 1.6 }, null ),
                           geomFac.createPoint( null, new double[] { -110, 16.77777 }, null ), 1000 );
        testLinearization( geomFac.createPoint( null, new double[] { 8, -1 }, null ),
                           geomFac.createPoint( null, new double[] { 3, 1.6 }, null ),
                           geomFac.createPoint( null, new double[] { -110, 16.77777 }, null ), 10 );
        testLinearizationWithErrorCriterion( geomFac.createPoint( null, new double[] { 8, -1 }, null ),
                                             geomFac.createPoint( null, new double[] { 3, 1.6 }, null ),
                                             geomFac.createPoint( null, new double[] { -110, 16.77777 }, null ),
                                             0.0001, 1000 );
    }

    /**
     * Tests if {@link CurveLinearizer#linearizeCubicSpline(CubicSpline, LinearizationCriterion)} produces positions
     * that coincide with the real values.
     */
    @Test
    public void testLinearizeCubicSpline() {
        List<Point> pList = new ArrayList<Point>();
        pList.add( geomFac.createPoint( null, new double[] { -2, 0 }, null ) );
        pList.add( geomFac.createPoint( null, new double[] { -4, 0 }, null ) );
        pList.add( geomFac.createPoint( null, new double[] { -6, 1 }, null ) );
        CubicSpline spline = new DefaultCubicSpline( new PointsList( pList ),
                                                     geomFac.createPoint( null, new double[] { 0, -1 }, null ),
                                                     geomFac.createPoint( null, new double[] { -1, 1 }, null ) );

        int numOfPoints = 10000;

        Points pts = linearizer.linearize( spline, new NumPointsCriterion( numOfPoints ) ).getControlPoints();

        for ( int i = 0; i < numOfPoints; i++ ) {
            if ( Math.abs( pts.get( i ).get0() - -3 ) < 1E-3 ) {
                Assert.assertEquals( pts.get( i ).get1(), -0.535536466911, 1E-3 );
            }
            if ( Math.abs( pts.get( i ).get0() - -5 ) < 1E-3 ) {
                Assert.assertEquals( pts.get( i ).get1(), 0.4464878443629, 1E-3 );
            }
        }

    }

    /**
     * Test the delta (distance between two adjacent points) the number of linearization points and the end and begin
     * points
     */
    @Test
    public void testLinearizeArc() {
        double[] p0 = new double[] { .5, 0.5 };
        double[] p1 = new double[] { -.5, -.2 };
        double[] p2 = new double[] { -2, -0.6 };

        Points positions = createLinearArc( p0, p1, p2, false );

        Point lastPoint = null;
        for ( Point point : positions ) {
            if ( lastPoint != null ) {
                double delta = Math.sqrt( ( point.get0() - lastPoint.get0() ) * ( point.get0() - lastPoint.get0() )
                                          + ( point.get1() - lastPoint.get1() ) * ( point.get1() - lastPoint.get1() ) );
                Assert.assertEquals( 0.199115, delta, 0.000001 );
            }
            lastPoint = point;
        }
        Assert.assertEquals( 15, positions.size() );
        Assert.assertEquals( p0[0], positions.get( 0 ).get0(), 1E-15 );
        Assert.assertEquals( p0[1], positions.get( 0 ).get1(), 1E-15 );
        Assert.assertEquals( p2[0], positions.get( positions.size() - 1 ).get0(), 1E-15 );
        Assert.assertEquals( p2[1], positions.get( positions.size() - 1 ).get1(), 1E-15 );
    }

    /**
     * Tests the linearization of an arc with collinear control points (on a line).
     */
    @Test
    public void testLinearizeCollinearArc() {

        double[] p0 = new double[] { 0, 0 };
        double[] p1 = new double[] { 0, 1 };
        double[] p2 = new double[] { 0, 2 };

        Points positions = createLinearArc( p0, p1, p2, false );

        Assert.assertEquals( 2, positions.size() );
        Assert.assertEquals( p0[0], positions.get( 0 ).get0(), 1.0E-9 );
        Assert.assertEquals( p0[1], positions.get( 0 ).get1(), 1.0E-9 );
        Assert.assertEquals( p2[0], positions.get( 1 ).get0(), 1.0E-9 );
        Assert.assertEquals( p2[1], positions.get( 1 ).get1(), 1.0E-9 );
    }

    /**
     * Tests the linearization of a circle with collinear control points (on a line).
     */
    @Test
    public void testLinearizeCollinearCircle() {

        double[] p0 = new double[] { 0, 0 };
        double[] p1 = new double[] { 0, 1 };
        double[] p2 = new double[] { 0, 2 };

        Points positions = createLinearArc( p0, p1, p2, true );

        Assert.assertEquals( 3, positions.size() );
        Assert.assertEquals( p0[0], positions.get( 0 ).get0(), 1.0E-9 );
        Assert.assertEquals( p0[1], positions.get( 0 ).get1(), 1.0E-9 );
        Assert.assertEquals( p1[0], positions.get( 1 ).get0(), 1.0E-9 );
        Assert.assertEquals( p1[1], positions.get( 1 ).get1(), 1.0E-9 );
        Assert.assertEquals( p0[0], positions.get( 2 ).get0(), 1.0E-9 );
        Assert.assertEquals( p0[1], positions.get( 2 ).get1(), 1.0E-9 );
    }

    /**
     * creates a circle or a an arc and outputs them to wkt.
     * 
     * @param first
     * @param second
     * @param third
     * @param isCircle
     */
    private Points createLinearArc( double[] first, double[] second, double[] third, boolean isCircle ) {

        Point p0 = geomFac.createPoint( null, first, null );
        Point p1 = geomFac.createPoint( null, second, null );
        Point p2 = geomFac.createPoint( null, third, null );
        Arc arc = isCircle ? geomFac.createCircle( p0, p1, p2 ) : geomFac.createArc( p0, p1, p2 );

        List<Point> output = new ArrayList<Point>( 3 );
        if ( outputWKT ) {
            output.add( p0 );
            output.add( p1 );
            output.add( p2 );
            LOG.debug( exportToWKT( new PointsList( output ) ) );
        }
        Points output2 = linearizer.linearize( arc, new NumPointsCriterion( 15 ) ).getControlPoints();
        if ( outputWKT ) {
            LOG.debug( exportToWKT( output2 ) );
        }
        return output2;
    }

    private void testPoints( Points actual, String expected )
                            throws ParseException {
        Iterator<Point> it = actual.iterator();
        Geometry g = new WKTReader( CRSManager.getCRSRef( "EPSG:4326" ) ).read( expected );
        Points ps = ( (Curve) g ).getControlPoints();
        for ( Point p : ps ) {
            Point act = it.next();
            Assert.assertEquals( p.get0(), act.get0(), 1.0E-9 );
            Assert.assertEquals( p.get1(), act.get1(), 1.0E-9 );
        }
    }

    /**
     * Test an arc over quadrants 3 and 4 <code>
     *       |
     *   4   |   1
     * _______|_______
     *       |
     *   3   |   2
     *       |
     * </code>
     * 
     * @throws ParseException
     */
    @Test
    public void testVisualLinArcQDs34()
                            throws ParseException {
        double[] p0 = new double[] { -1.7, 2.5 };
        double[] p1 = new double[] { -3, 0.5 };
        double[] p2 = new double[] { -2.3, -2.5 };
        Points positions = createLinearArc( p0, p1, p2, false );
        testPoints( positions,
                    "LINESTRING (-1.7 2.5, -2.006268887232975 2.240298830592298, -2.280689527287858 1.9471458995298252, -2.5196312406291193 1.6244197210066154, -2.719932746135047 1.2763900734610205, -2.8789439859352446 0.9076615089371216, -2.994561186588522 0.5231124332760927, -3.0652546927297304 0.127830563127568, -3.0900892049368194 -0.272954386293051, -3.0687361540641627 -0.6739398921371922, -3.001478048325461 -1.069820778125759, -2.8892047356128234 -1.4553594038575817, -2.7334016305028315 -1.8254549605949406, -2.536130061710344 -2.1752109567191, -2.3 -2.5)" );

        // inverse
        positions = createLinearArc( p2, p1, p0, false );
        testPoints( positions,
                    "LINESTRING (-2.3 -2.5, -2.5361300617103426 -2.1752109567191003, -2.7334016305028297 -1.8254549605949428, -2.8892047356128225 -1.4553594038575826, -3.0014780483254597 -1.0698207781257598, -3.068736154064162 -0.6739398921371934, -3.0900892049368185 -0.2729543862930522, -3.06525469272973 0.1278305631275666, -2.9945611865885216 0.5231124332760911, -2.878943985935244 0.9076615089371203, -2.7199327461350467 1.276390073461019, -2.519631240629119 1.6244197210066136, -2.280689527287859 1.9471458995298225, -2.0062688872329755 2.240298830592297, -1.7 2.5)" );
    }

    /**
     * Test an arc over quadrants 1 and 2 <code>
     *       |
     *   4   |   1
     * _______|_______
     *       |
     *   3   |   2
     *       |
     * </code>
     * 
     * @throws ParseException
     */
    @Test
    public void testVisualLinArcQDs12()
                            throws ParseException {
        double[] p0 = new double[] { 1.7, 2.5 };
        double[] p1 = new double[] { 3, 0.5 };
        double[] p2 = new double[] { 2.3, -2.5 };
        Points positions = createLinearArc( p0, p1, p2, false );
        testPoints( positions,
                    "LINESTRING (1.7 2.5, 2.0062688872329746 2.2402988305922986, 2.2806895272878585 1.9471458995298248, 2.5196312406291193 1.6244197210066147, 2.7199327461350467 1.2763900734610203, 2.878943985935245 0.9076615089371205, 2.994561186588522 0.5231124332760916, 3.0652546927297304 0.1278305631275672, 3.090089204936819 -0.2729543862930513, 3.0687361540641622 -0.6739398921371922, 3.00147804832546 -1.0698207781257603, 2.8892047356128225 -1.4553594038575823, 2.73340163050283 -1.8254549605949426, 2.5361300617103426 -2.175210956719101, 2.3 -2.5)" );

        // inverse
        positions = createLinearArc( p2, p1, p0, false );
        testPoints( positions,
                    "LINESTRING (2.3 -2.5, 2.536130061710342 -2.175210956719101, 2.73340163050283 -1.8254549605949422, 2.8892047356128225 -1.4553594038575821, 3.0014780483254597 -1.0698207781257598, 3.068736154064162 -0.6739398921371922, 3.0900892049368185 -0.2729543862930511, 3.0652546927297295 0.1278305631275674, 2.994561186588521 0.5231124332760915, 2.878943985935244 0.9076615089371205, 2.7199327461350458 1.2763900734610198, 2.5196312406291184 1.6244197210066145, 2.280689527287857 1.9471458995298243, 2.0062688872329737 2.240298830592298, 1.7 2.5)" );

    }

    /**
     * Test an arc over quadrants 1 and 4 <code>
     *       |
     *   4   |   1
     * _______|_______
     *       |
     *   3   |   2
     *       |
     * </code>
     * 
     * @throws ParseException
     */
    @Test
    public void testVisualLinArcQDs14()
                            throws ParseException {
        double[] p0 = new double[] { 1.7, 2.5 };
        double[] p1 = new double[] { 1, 3.5 };
        double[] p2 = new double[] { -2.3, 2.3 };
        Points positions = createLinearArc( p0, p1, p2, false );
        testPoints( positions,
                    "LINESTRING (1.6999999999999997 2.5, 1.5473395210137384 2.855706401706767, 1.331217640693683 3.1768350346662317, 1.0591681063567635 3.452191743185483, 0.740674235686257 3.672177925489757, 0.3868383397488122 3.829125129369394, 0.0099947098605335 3.9175623651477514, -0.3767203408881685 3.934406817763368, -0.7598263936166152 3.879071309973894, -1.1259688347495056 3.7534847706444783, -1.4623843816917867 3.562024994620855, -1.7573459954208674 3.311366038096601, -2.0005716708813686 3.0102455690874788, -2.183582855243771 2.6691602828938916, -2.3 2.3)" );

        // inverse
        positions = createLinearArc( p2, p1, p0, false );
        testPoints( positions,
                    "LINESTRING (-2.3 2.3, -2.1835828552437717 2.669160282893891, -2.0005716708813686 3.0102455690874796, -1.7573459954208674 3.311366038096602, -1.4623843816917872 3.562024994620855, -1.1259688347495054 3.7534847706444787, -0.7598263936166152 3.8790713099738943, -0.3767203408881679 3.934406817763368, 0.0099947098605339 3.9175623651477514, 0.3868383397488126 3.8291251293693946, 0.7406742356862576 3.6721779254897573, 1.0591681063567644 3.4521917431854825, 1.331217640693684 3.1768350346662317, 1.5473395210137393 2.8557064017067675, 1.7000000000000002 2.5)" );

    }

    /**
     * Test an arc over quadrants 2 and 4 <code>
     *       |
     *   4   |   1
     * _______|_______
     *       |
     *   3   |   2
     *       |
     * </code>
     * 
     * @throws ParseException
     */
    @Test
    public void testVisualLinArcQDs23()
                            throws ParseException {
        double[] p0 = new double[] { 2.3, -2.5 };
        double[] p1 = new double[] { 1, -3.5 };
        double[] p2 = new double[] { -2.3, -2.5 };
        Points positions = createLinearArc( p0, p1, p2, false );
        testPoints( positions,
                    "LINESTRING (2.3 -2.5, 2.056305411080411 -2.7960391691981443, 1.774800678026383 -3.0563876426647565, 1.4606619456235812 -3.2762582845717096, 1.1196654135971806 -3.451608241840547, 0.7580811272214822 -3.5792132817660605, 0.3825576873857044 -3.6567270773260736, 0.0000000000000004 -3.682724350073748, -0.3825576873857036 -3.6567270773260736, -0.7580811272214821 -3.5792132817660605, -1.1196654135971804 -3.4516082418405465, -1.460661945623581 -3.276258284571709, -1.774800678026382 -3.0563876426647565, -2.0563054110804115 -2.796039169198144, -2.3 -2.5)" );

        // inverse
        positions = createLinearArc( p2, p1, p0, false );
        testPoints( positions,
                    "LINESTRING (-2.3 -2.5, -2.0563054110804107 -2.7960391691981448, -1.7748006780263823 -3.0563876426647565, -1.4606619456235799 -3.2762582845717096, -1.1196654135971802 -3.451608241840547, -0.7580811272214817 -3.5792132817660605, -0.3825576873857038 -3.6567270773260736, 0.0000000000000002 -3.682724350073748, 0.3825576873857042 -3.6567270773260736, 0.7580811272214827 -3.57921328176606, 1.119665413597181 -3.4516082418405465, 1.460661945623581 -3.2762582845717096, 1.774800678026383 -3.056387642664756, 2.056305411080411 -2.796039169198144, 2.3 -2.5)" );

    }

    /**
     * Test an arc over quadrants 1 and 4 over 3 and 2 <code>
     *       |
     *   4   |   1
     * _______|_______
     *       |
     *   3   |   2
     *       |
     * </code>
     * 
     * @throws ParseException
     */
    @Test
    public void testLinearizeArcFourQDs14()
                            throws ParseException {
        double[] p0 = new double[] { 1.7, 2.5 };
        double[] p1 = new double[] { 3, 0.5 };
        double[] p2 = new double[] { -2.3, 2.5 };
        Points positions = createLinearArc( p0, p1, p2, false );
        testPoints( positions,
                    "LINESTRING (1.7 2.5, 2.527698122123212 1.62577746297488, 2.9962725289439467 0.5168197311917764, 3.046213267629344 -0.6860331892554772, 2.671177757448691 -1.8300164022933552, 1.9187963111049964 -2.7698415907229377, 0.8846229830504084 -3.386148943550589, -0.3 -3.600666107520471, -1.4846229830504085 -3.386148943550589, -2.5187963111049965 -2.7698415907229386, -3.271177757448691 -1.8300164022933556, -3.646213267629344 -0.6860331892554776, -3.5962725289439472 0.5168197311917756, -3.1276981221232143 1.6257774629748782, -2.3 2.5)" );

        // inverse
        positions = createLinearArc( p2, p1, p0, false );
        LOG.debug( exportToWKT( positions ) );
        testPoints( positions,
                    "LINESTRING (-2.3 2.5, -3.127698122123212 1.62577746297488, -3.5962725289439463 0.5168197311917766, -3.6462132676293435 -0.6860331892554763, -3.2711777574486907 -1.8300164022933543, -2.518796311104997 -2.769841590722937, -1.4846229830504076 -3.3861489435505883, -0.300000000000001 -3.6006661075204693, 0.8846229830504059 -3.386148943550589, 1.918796311104995 -2.769841590722937, 2.67117775744869 -1.8300164022933538, 3.0462132676293425 -0.6860331892554776, 2.9962725289439462 0.5168197311917739, 2.5276981221232138 1.6257774629748767, 1.7000000000000002 2.5)" );

    }

    /**
     * Linearize a circle
     */
    @Test
    public void visualizeCircle() {
        double[] p0 = new double[] { -1.7, 2.5 };
        double[] p1 = new double[] { -3, 0.5 };
        double[] p2 = new double[] { -2.3, -2.5 };
        Points positions = createLinearArc( p0, p1, p2, true );
        Assert.assertEquals( exportToWKT( positions ).trim(),
                             "LINESTRING (-1.7 2.5, -2.7016581805723767 1.3123248162113277, -3.088808067948505 -0.1923367462669825, -2.7847698788517685 -1.715968011523047, -1.8497621054164624 -2.95679512136171, -0.4689745042892608 -3.6690570489335883, 1.0841110058779768 -3.711681584163785, 2.50188679245283 -3.0762264150943404, 3.5035449730252055 -1.8885512313056685, 3.8906948604013345 -0.3838896688273583, 3.5866566713045978 1.1397415964287063, 2.651648897869292 2.3805687062673697, 1.2708612967420905 3.092830633839248, -0.2822242134251467 3.1354551690694445, -1.7 2.5)" );

        // inverse
        positions = createLinearArc( p2, p1, p0, true );
        LOG.debug( exportToWKT( positions ).trim() );
        Assert.assertEquals( exportToWKT( positions ).trim(),
                             "LINESTRING (-2.3 -2.5, -2.992224229754389 -1.109058926972116, -3.0123899967132983 0.4444805140263982, -2.3565032234136143 1.8529207837948918, -1.1544703292307315 2.8373030135193718, 0.3556313291764255 3.2026582299471267, 1.8747075981788668 2.8766233516769613, 3.1018867924528286 1.9237735849056588, 3.794111022207218 0.5328325118777754, 3.814276789166127 -1.0207069291207391, 3.1583900158664444 -2.4291471988892326, 1.9563571216835611 -3.413529428613713, 0.4462554632764042 -3.778884645041468, -1.0728208057260369 -3.452849766771303, -2.3 -2.5)" );
    }

    /**
     * Little helper method to create a WKT.
     * 
     * @param points
     * @return the WKT
     */
    private String exportToWKT( Points points ) {
        Coordinate[] coords = new Coordinate[points.size()];
        for ( int i = 0; i < points.size(); ++i ) {
            Point p = points.get( i );
            coords[i] = new Coordinate( p.get0(), p.get1() );
        }
        CoordinateSequence cs = new CoordinateArraySequence( coords );
        LineString ls = new LineString( cs, new GeometryFactory() );
        return ls.toText();
    }
}
