//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/test/org/deegree/CommonsTestSuite.java $
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

import java.util.List;

import junit.framework.Assert;

import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.curvesegments.Arc;
import org.deegree.geometry.primitive.curvesegments.Circle;
import org.deegree.geometry.standard.points.PointsBuilder;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.geomgraph.Position;

/**
 * Tests for {@link CurveLinearizer}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class CurveLinearizerTest {

    private org.deegree.geometry.GeometryFactory geomFac;

    private CurveLinearizer linearizer;

    private boolean outputWKT = false;

    @Before
    public void setUp() {
        geomFac = new org.deegree.geometry.GeometryFactory();
        linearizer = new CurveLinearizer( geomFac );
    }

    private double getDistance( Point p0, Point p1 ) {
        double dx = p1.getX() - p0.getX();
        double dy = p1.getY() - p0.getY();
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
                double delta = Math.sqrt( ( point.getX() - lastPoint.getX() ) * ( point.getX() - lastPoint.getX() )
                                          + ( point.getY() - lastPoint.getY() ) * ( point.getY() - lastPoint.getY() ) );
            }
            lastPoint = point;
        }
    }

    /**
     * Tests if {@link LinearizationUtil#isClockwise(Position, Position, Position)} determines the correct point order.
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
     * Tests if {@link LinearizationUtil#findCircleCenter(Point, Point, Point)} finds the correct midpoint.
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
     * Tests if {@link LinearizationUtil#linearizeCircle(Point, Point, Point, int)} produces sequences of positions that
     * coincide with the circle arc.
     */
    @Test
    public void testLinearizeCircle() {
        testLinearization( geomFac.createPoint( null, new double[] { 0, 2 }, null ),
                           geomFac.createPoint( null, new double[] { 2, 0 }, null ),
                           geomFac.createPoint( null, new double[] { -2, 0 }, null ), 10 );
        testLinearization( geomFac.createPoint( null, new double[] { 0, 2 }, null ),
                           geomFac.createPoint( null, new double[] { 2, 0 }, null ),
                           geomFac.createPoint( null, new double[] { -2, 0 }, null ), 1000 );
        testLinearization( geomFac.createPoint( null, new double[] { 8, -1 }, null ),
                           geomFac.createPoint( null, new double[] { 3, 1.6 }, null ),
                           geomFac.createPoint( null, new double[] { -110, 16.77777 }, null ), 1000 );
        testLinearization( geomFac.createPoint( null, new double[] { 8, -1 }, null ),
                           geomFac.createPoint( null, new double[] { 3, 1.6 }, null ),
                           geomFac.createPoint( null, new double[] { -110, 16.77777 }, null ), 10 );
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
                double delta = Math.sqrt( ( point.getX() - lastPoint.getX() ) * ( point.getX() - lastPoint.getX() )
                                          + ( point.getY() - lastPoint.getY() ) * ( point.getY() - lastPoint.getY() ) );
                Assert.assertEquals( 0.199115, delta, 0.000001 );
            }
            lastPoint = point;
        }
        Assert.assertEquals( 15, positions.size() );
        Assert.assertEquals( p0[0], positions.get( 0 ).getX() );
        Assert.assertEquals( p0[1], positions.get( 0 ).getY() );
        Assert.assertEquals( p2[0], positions.get( positions.size() - 1 ).getX() );
        Assert.assertEquals( p2[1], positions.get( positions.size() - 1 ).getY() );
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
        Assert.assertEquals( p0[0], positions.get( 0 ).getX() );
        Assert.assertEquals( p0[1], positions.get( 0 ).getY() );
        Assert.assertEquals( p2[0], positions.get( 1 ).getX() );
        Assert.assertEquals( p2[1], positions.get( 1 ).getY() );
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
        Assert.assertEquals( p0[0], positions.get( 0 ).getX() );
        Assert.assertEquals( p0[1], positions.get( 0 ).getY() );
        Assert.assertEquals( p1[0], positions.get( 1 ).getX() );
        Assert.assertEquals( p1[1], positions.get( 1 ).getY() );
        Assert.assertEquals( p0[0], positions.get( 2 ).getX() );
        Assert.assertEquals( p0[1], positions.get( 2 ).getY() );
    }

    /**
     * creates a circle or a an arc and outputs them to wkt.
     * 
     * @param first
     * @param second
     * @param third
     * @param outputWKT
     * @param isCircle
     * @return
     */
    private Points createLinearArc( double[] first, double[] second, double[] third, boolean isCircle ) {

        Point p0 = geomFac.createPoint( null, first, null );
        Point p1 = geomFac.createPoint( null, second, null );
        Point p2 = geomFac.createPoint( null, third, null );
        Arc arc = isCircle ? geomFac.createCircle( p0, p1, p2 ) : geomFac.createArc( p0, p1, p2 );

        PointsBuilder output = new PointsBuilder( 3 );
        if ( outputWKT ) {
            output.add( p0 );
            output.add( p1 );
            output.add( p2 );
            System.out.println( exportToWKT( output ) );
        }
        Points output2 = linearizer.linearize( arc, new NumPointsCriterion( 15 ) ).getControlPoints();
        if ( outputWKT ) {
            System.out.println( exportToWKT( output2 ) );
        }
        return output2;
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
     */
    @Test
    public void testVisualLinArcQDs34() {
        double[] p0 = new double[] { -1.7, 2.5 };
        double[] p1 = new double[] { -3, 0.5 };
        double[] p2 = new double[] { -2.3, -2.5 };
        Points positions = createLinearArc( p0, p1, p2, false );
        Assert.assertEquals(
                             exportToWKT( positions ),
                             "LINESTRING (-1.7 2.5, -2.0062688872329746 2.240298830592299, -2.2806895272878576 1.9471458995298256, -2.5196312406291184 1.6244197210066158, -2.7199327461350467 1.276390073461021, -2.878943985935244 0.907661508937122, -2.9945611865885216 0.5231124332760928, -3.06525469272973 0.1278305631275681, -3.090089204936819 -0.272954386293051, -3.0687361540641622 -0.6739398921371924, -3.0014780483254606 -1.069820778125759, -2.889204735612823 -1.455359403857582, -2.733401630502831 -1.825454960594941, -2.536130061710343 -2.1752109567191003, -2.3 -2.5)" );

        // inverse
        positions = createLinearArc( p2, p1, p0, false );
        Assert.assertEquals(
                             exportToWKT( positions ),
                             "LINESTRING (-2.3 -2.5, -2.536130061710342 -2.175210956719101, -2.7334016305028292 -1.8254549605949433, -2.8892047356128217 -1.4553594038575828, -3.0014780483254593 -1.0698207781257603, -3.0687361540641613 -0.6739398921371935, -3.090089204936818 -0.2729543862930522, -3.0652546927297295 0.1278305631275667, -2.994561186588521 0.5231124332760914, -2.8789439859352437 0.9076615089371205, -2.719932746135046 1.2763900734610192, -2.5196312406291184 1.6244197210066142, -2.2806895272878585 1.947145899529823, -2.0062688872329746 2.2402988305922977, -1.7 2.5)" );
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
     */
    @Test
    public void testVisualLinArcQDs12() {
        double[] p0 = new double[] { 1.7, 2.5 };
        double[] p1 = new double[] { 3, 0.5 };
        double[] p2 = new double[] { 2.3, -2.5 };
        Points positions = createLinearArc( p0, p1, p2, false );
        Assert.assertEquals(
                             exportToWKT( positions ),
                             "LINESTRING (1.7 2.5, 2.0062688872329746 2.240298830592299, 2.2806895272878585 1.9471458995298248, 2.519631240629119 1.6244197210066154, 2.7199327461350467 1.2763900734610207, 2.8789439859352446 0.9076615089371209, 2.994561186588522 0.523112433276092, 3.06525469272973 0.1278305631275676, 3.090089204936819 -0.272954386293051, 3.0687361540641622 -0.673939892137192, 3.0014780483254606 -1.0698207781257594, 2.8892047356128225 -1.4553594038575823, 2.7334016305028306 -1.8254549605949422, 2.5361300617103426 -2.175210956719101, 2.3 -2.5)" );

        // inverse
        positions = createLinearArc( p2, p1, p0, false );
        Assert.assertEquals(
                             exportToWKT( positions ),
                             "LINESTRING (2.3 -2.5, 2.536130061710342 -2.175210956719101, 2.73340163050283 -1.8254549605949422, 2.8892047356128217 -1.4553594038575821, 3.0014780483254597 -1.0698207781257598, 3.0687361540641613 -0.6739398921371924, 3.090089204936818 -0.2729543862930515, 3.065254692729729 0.1278305631275671, 2.994561186588521 0.5231124332760914, 2.8789439859352437 0.90766150893712, 2.7199327461350458 1.2763900734610196, 2.5196312406291184 1.6244197210066145, 2.2806895272878576 1.9471458995298239, 2.0062688872329733 2.240298830592298, 1.7 2.5)" );
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
     */
    @Test
    public void testVisualLinArcQDs14() {
        double[] p0 = new double[] { 1.7, 2.5 };
        double[] p1 = new double[] { 1, 3.5 };
        double[] p2 = new double[] { -2.3, 2.3 };
        Points positions = createLinearArc( p0, p1, p2, false );
        Assert.assertEquals(
                             exportToWKT( positions ),
                             "LINESTRING (1.7 2.5, 1.5473395210137388 2.855706401706767, 1.3312176406936835 3.176835034666232, 1.0591681063567637 3.452191743185483, 0.740674235686257 3.6721779254897573, 0.3868383397488121 3.8291251293693946, 0.0099947098605334 3.917562365147752, -0.3767203408881685 3.9344068177633686, -0.7598263936166157 3.8790713099738943, -1.1259688347495058 3.753484770644479, -1.4623843816917876 3.562024994620855, -1.7573459954208683 3.3113660380966015, -2.000571670881369 3.0102455690874796, -2.1835828552437717 2.6691602828938925, -2.3 2.3)" );

        // inverse
        positions = createLinearArc( p2, p1, p0, false );
        Assert.assertEquals(
                             exportToWKT( positions ),
                             "LINESTRING (-2.3 2.3, -2.1835828552437713 2.669160282893891, -2.0005716708813686 3.0102455690874788, -1.7573459954208672 3.3113660380966015, -1.4623843816917872 3.5620249946208546, -1.1259688347495054 3.7534847706444783, -0.7598263936166155 3.8790713099738934, -0.3767203408881684 3.9344068177633678, 0.0099947098605332 3.917562365147751, 0.3868383397488118 3.8291251293693938, 0.7406742356862566 3.6721779254897564, 1.0591681063567633 3.452191743185482, 1.3312176406936829 3.1768350346662313, 1.5473395210137377 2.855706401706767, 1.7 2.5)" );
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
     */
    @Test
    public void testVisualLinArcQDs23() {
        double[] p0 = new double[] { 2.3, -2.5 };
        double[] p1 = new double[] { 1, -3.5 };
        double[] p2 = new double[] { -2.3, -2.5 };
        Points positions = createLinearArc( p0, p1, p2, false );
        Assert.assertEquals(
                             exportToWKT( positions ),
                             "LINESTRING (2.3 -2.5, 2.056305411080411 -2.796039169198144, 1.7748006780263827 -3.056387642664756, 1.4606619456235808 -3.2762582845717096, 1.1196654135971809 -3.4516082418405465, 0.7580811272214825 -3.57921328176606, 0.3825576873857042 -3.6567270773260736, 0.0000000000000002 -3.682724350073748, -0.3825576873857038 -3.6567270773260736, -0.7580811272214822 -3.5792132817660605, -1.1196654135971806 -3.4516082418405465, -1.4606619456235796 -3.2762582845717096, -1.774800678026382 -3.056387642664757, -2.05630541108041 -2.7960391691981448, -2.3 -2.5)" );

        // inverse
        positions = createLinearArc( p2, p1, p0, false );
        Assert.assertEquals(
                             exportToWKT( positions ),
                             "LINESTRING (-2.3 -2.5, -2.05630541108041 -2.7960391691981448, -1.774800678026382 -3.056387642664757, -1.4606619456235796 -3.2762582845717096, -1.11966541359718 -3.451608241840547, -0.7580811272214816 -3.5792132817660605, -0.3825576873857038 -3.6567270773260736, 0.0000000000000002 -3.682724350073748, 0.3825576873857042 -3.6567270773260736, 0.7580811272214825 -3.57921328176606, 1.1196654135971809 -3.4516082418405465, 1.4606619456235808 -3.2762582845717096, 1.7748006780263827 -3.056387642664756, 2.056305411080411 -2.796039169198144, 2.3 -2.5)" );
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
     */
    @Test
    public void testLinearizeArcFourQDs14() {
        double[] p0 = new double[] { 1.7, 2.5 };
        double[] p1 = new double[] { 3, 0.5 };
        double[] p2 = new double[] { -2.3, 2.5 };
        Points positions = createLinearArc( p0, p1, p2, false );
        Assert.assertEquals(
                             exportToWKT( positions ),
                             "LINESTRING (1.7 2.5, 2.527698122123212 1.62577746297488, 2.9962725289439462 0.5168197311917763, 3.046213267629344 -0.6860331892554771, 2.671177757448691 -1.8300164022933552, 1.9187963111049968 -2.7698415907229377, 0.8846229830504091 -3.386148943550589, -0.2999999999999993 -3.600666107520471, -1.4846229830504079 -3.38614894355059, -2.5187963111049965 -2.7698415907229386, -3.2711777574486907 -1.8300164022933576, -3.646213267629344 -0.6860331892554783, -3.5962725289439477 0.5168197311917736, -3.1276981221232147 1.6257774629748767, -2.3 2.5)" );

        // inverse
        positions = createLinearArc( p2, p1, p0, false );
        Assert.assertEquals(
                             exportToWKT( positions ),
                             "LINESTRING (-2.3 2.5, -3.1276981221232125 1.6257774629748796, -3.5962725289439472 0.5168197311917759, -3.6462132676293444 -0.6860331892554774, -3.2711777574486915 -1.8300164022933556, -2.5187963111049974 -2.769841590722938, -1.484622983050408 -3.38614894355059, -0.3000000000000009 -3.600666107520471, 0.8846229830504062 -3.3861489435505905, 1.9187963111049955 -2.7698415907229386, 2.671177757448691 -1.8300164022933552, 3.0462132676293434 -0.6860331892554787, 2.996272528943947 0.5168197311917733, 2.5276981221232147 1.6257774629748762, 1.7 2.5)" );
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
        Assert.assertEquals(
                             exportToWKT( positions ).trim(),
                             "LINESTRING (-1.7 2.5, -2.7016581805723763 1.3123248162113281, -3.0888080679485044 -0.1923367462669825, -2.784769878851768 -1.7159680115230473, -1.8497621054164615 -2.956795121361711, -0.4689745042892599 -3.669057048933589, 1.0841110058779782 -3.711681584163786, 2.501886792452831 -3.0762264150943412, 3.5035449730252077 -1.888551231305669, 3.8906948604013363 -0.3838896688273583, 3.5866566713046 1.1397415964287065, 2.651648897869294 2.3805687062673706, 1.2708612967420923 3.092830633839249, -0.2822242134251457 3.1354551690694454, -1.7 2.5)" );

        // inverse
        positions = createLinearArc( p2, p1, p0, true );
        Assert.assertEquals(
                             exportToWKT( positions ).trim(),
                             "LINESTRING (-2.3 -2.5, -2.9922242297543886 -1.1090589269721165, -3.012389996713298 0.4444805140263983, -2.356503223413614 1.8529207837948922, -1.1544703292307306 2.8373030135193726, 0.3556313291764269 3.2026582299471276, 1.8747075981788681 2.876623351676962, 3.1018867924528313 1.9237735849056596, 3.7941110222072205 0.5328325118777755, 3.8142767891661298 -1.0207069291207391, 3.158390015866446 -2.429147198889233, 1.956357121683563 -3.413529428613714, 0.4462554632764054 -3.778884645041469, -1.072820805726036 -3.4528497667713034, -2.3 -2.5)" );

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
            coords[i] = new Coordinate( p.getX(), p.getY() );
        }
        CoordinateSequence cs = new CoordinateArraySequence( coords );
        LineString ls = new LineString( cs, new GeometryFactory() );
        return ls.toText();
    }
}
