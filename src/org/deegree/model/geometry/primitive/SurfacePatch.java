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
public interface SurfacePatch {
    
    public enum INTERPOLATION {
        none, planar, spherical, elliptical, conic, tin, bilinear, biquadratic, bicubic, polynomialSpline, 
        rationalSpline, triangulatedSpline
    }

	public List<Curve> getBoundary();
    
    public double getArea();
    
    public int getCoordinateDimension();

}