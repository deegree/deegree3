package org.deegree.model.geometry.linearization;

import java.util.List;

import junit.framework.Assert;

import org.deegree.model.geometry.GeometryFactoryCreator;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.curvesegments.Arc;
import org.deegree.model.geometry.primitive.curvesegments.Circle;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geomgraph.Position;

public class CurveLinearizerTest {

    private org.deegree.model.geometry.GeometryFactory geomFac;

    private CurveLinearizer linearizer;

    @Before
    public void setUp() {
        geomFac = GeometryFactoryCreator.getInstance().getGeometryFactory();
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
        List<Point> positions = linearizer.linearizeCircle( circle, new NumPointsCriterion( numPositions ) ).getControlPoints();
        for ( Point point : positions ) {
            double dist = getDistance( center, point );
            Assert.assertEquals( radius, dist, 1E-9 );
        }
        
        Point lastPoint = null;
        for ( Point point : positions ) {
            if (lastPoint != null) {
                double delta = Math.sqrt( (point.getX() - lastPoint.getX()) * (point.getX() - lastPoint.getX()) +
                (point.getY() - lastPoint.getY()) * (point.getY() - lastPoint.getY()));
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

    @Test
    public void testLinearizeArc() {
        Point p0 = geomFac.createPoint( null, new double[] { 118075.83, 407620.65 }, null );
        Point p1 = geomFac.createPoint( null, new double[] { 118082.289, 407621.62 }, null );
        Point p2 = geomFac.createPoint( null, new double[] { 118087.099, 407626.038 }, null );
        Arc arc = geomFac.createArc( p0, p1, p2 );
        List<Point> positions = linearizer.linearizeArc( arc, new NumPointsCriterion( 150 ) ).getControlPoints();
        Point lastPoint = null;
        for ( Point point : positions ) {
            if (lastPoint != null) {
                double delta = Math.sqrt( (point.getX() - lastPoint.getX()) * (point.getX() - lastPoint.getX()) +
                (point.getY() - lastPoint.getY()) * (point.getY() - lastPoint.getY()));
                Assert.assertEquals( 0.0889, delta, 0.0001 );
            }
            lastPoint = point;
        }
        Assert.assertEquals(150, positions.size());
        Assert.assertEquals( p0.getX(), positions.get( 0 ).getX() );
        Assert.assertEquals( p0.getY(), positions.get( 0 ).getY() );
        Assert.assertEquals( p2.getX(), positions.get( positions.size() - 1 ).getX() );
        Assert.assertEquals( p2.getY(), positions.get( positions.size() - 1 ).getY() );
    }
}
