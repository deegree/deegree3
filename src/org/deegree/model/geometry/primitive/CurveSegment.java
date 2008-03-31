package org.deegree.model.geometry.primitive;

import java.util.List;

/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: poth $
 * 
 * @version. $Revision: 6251 $, $Date: 2007-03-19 16:59:28 +0100 (Mo, 19 Mrz 2007) $
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