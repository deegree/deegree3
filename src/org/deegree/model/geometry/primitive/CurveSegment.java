package org.deegree.model.geometry.primitive;

import java.util.List;

/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public interface CurveSegment {

    public enum INTERPOLATION {
        linear, geodesic, circularArc3Points, circularArc2PointWithBulge, elliptical, conic, cubicSpline, 
        polynomialSpline, rationalSpline
    };

    public double[] getX();

    public double[] getY();

    public double[] getZ();

    public double[] getAsArray();

    public List<Point> getPoints();

    public int getCoordinateDimension();
    
    public INTERPOLATION getInterpolation();

}