package org.deegree.model.geometry.standard.curvesegments;

import java.util.List;

import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.curvesegments.Clothoid;
import org.deegree.model.geometry.primitive.curvesegments.LineStringSegment;

/**
 * Default implementation of {@link Clothoid}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultClothoid implements Clothoid {

    private AffinePlacement referenceLocation;

    private double scaleFactor;

    private double startParameter;

    private double endParameter;

    /**
     * Creates a new <code>DefaultClothoid</code> instance from the given parameters.
     * 
     * @param referenceLocation
     *            the affine mapping that places the curve defined by the Fresnel Integrals into the coordinate
     *            reference system of this object
     * @param scaleFactor
     *            the value for the constant in the Fresnel's integrals
     * @param startParameter
     *            the arc length distance from the inflection point that will be the start point for this curve segment
     * @param endParameter
     *            the arc length distance from the inflection point that will be the end point for this curve segment
     */
    public DefaultClothoid( AffinePlacement referenceLocation, double scaleFactor, double startParameter,
                            double endParameter ) {
        this.referenceLocation = referenceLocation;
        this.scaleFactor = scaleFactor;
        this.startParameter = startParameter;
        this.endParameter = endParameter;
    }

    @Override
    public AffinePlacement getReferenceLocation() {
        return referenceLocation;
    }

    @Override
    public double getScaleFactor() {
        return scaleFactor;
    }

    @Override
    public double getStartParameter() {
        return startParameter;
    }

    @Override
    public double getEndParameter() {
        return endParameter;
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
        return referenceLocation.getOutDimension();
    }

    @Override
    public Interpolation getInterpolation() {
        return Interpolation.clothoid;
    }

    @Override
    public List<Point> getPoints() {
        throw new UnsupportedOperationException();
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
}
