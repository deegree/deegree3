package org.deegree.model.geometry.standard.curvesegments;

import java.util.ArrayList;
import java.util.List;

import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.curvesegments.Bezier;
import org.deegree.model.geometry.primitive.curvesegments.Knot;
import org.deegree.model.geometry.primitive.curvesegments.LineStringSegment;

/**
 * Default implementation of {@link Bezier}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultBezier implements Bezier {

    private List<Point> controlPoints;

    private int polynomialDegree;

    private List<Knot> knots;

    public DefaultBezier( List<Point> controlPoints, int polynomialDegree, Knot knot1, Knot knot2 ) {
        this.controlPoints = controlPoints;
        this.polynomialDegree = polynomialDegree;
        knots = new ArrayList<Knot>( 2 );
        knots.add( knot1 );
        knots.add( knot2 );
    }

    @Override
    public Knot getKnot1() {
        return knots.get( 0 );
    }

    @Override
    public Knot getKnot2() {
        return knots.get( 1 );
    }

    @Override
    public double[] getAsArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LineStringSegment getAsLineStringSegment() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getCoordinateDimension() {
        return controlPoints.get( 0 ).getCoordinateDimension();
    }

    @Override
    public Interpolation getInterpolation() {
        return Interpolation.polynomialSpline;
    }

    @Override
    public List<Point> getPoints() {
        return controlPoints;
    }

    @Override
    public double[] getX() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double[] getY() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double[] getZ() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getPolynomialDegree() {
        return polynomialDegree;
    }

    @Override
    public List<Knot> getKnots() {
        return knots;
    }
}
