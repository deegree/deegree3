//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/
package org.deegree.model.geometry.primitive;

import java.util.List;

import org.deegree.model.geometry.primitive.curvesegments.LineStringSegment;

/**
 * A <code>CurveSegment</code> is a portion of a {@link Curve} in which a single interpolation method is used.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public interface CurveSegment {

    /**
     * All known curve segment interpolations.
     */
    public enum Interpolation {
        /**
         * A linear interpolation.
         */
        linear,
        /**
         * A geodesic interpolation.
         */
        geodesic,
        /**
         * A circularArcCenterPointWithRadius interpolation.
         */        
        circularArcCenterPointWithRadius,
        /**
         * A circularArc3Points interpolation.
         */
        circularArc3Points,        
        /**
         * A circularArc2PointWithBulge interpolation.
         */
        circularArc2PointWithBulge,
        /**
         * A elliptical interpolation.
         */
        elliptical,
        /**
         * A clothoid interpolation.
         */
        clothoid,         
        /**
         * A conic interpolation.
         */
        conic,
        /**
         * A cubicSpline interpolation.
         */
        cubicSpline,
        /**
         * A polynomialSpline interpolation.
         */
        polynomialSpline,
        /**
         * A rationalSpline interpolation.
         */
        rationalSpline 
    }
    
    /**
     * 
     * @return a segments x-coordinates as an array
     */
    public double[] getX();

    /**
     * 
     * @return a segments y-coordinates as an array
     */
    public double[] getY();

    /**
     * 
     * @return a segments z-coordinates as an array
     */
    public double[] getZ();

    /**
     * 
     * @return all coordinated as an array. The array will be constructed an concatination of the arrays of the
     *         segements points. For a three dimensional case it looks like: [x0,y0,z0,x1,y1,z1, ... ,xn,yn,zn]
     */
    public double[] getAsArray();

    /**
     * 
     * @return points constructing a segments as {@link List}
     */
    public List<Point> getPoints();

    /**
     * 
     * @return dimension of a curve segment coordinates (2 for flat surfaces; 3 for surfaces in a 3D space)
     */
    public int getCoordinateDimension();

    /**
     * 
     * @return interpolation method used by this curve segment
     */
    public Interpolation getInterpolation();

    /**
     * Returns a linear interpolated representation of this <code>CurveSegment</code>.
     * <p>
     * Please note that this operation returns an approximated representations if this <code>CurveSegment</code> is not
     * a {@link LineStringSegment}.
     * </p>
     * 
     * @return a linear interpolated representation of this <code>CurveSegment</code>
     */
    public LineStringSegment getAsLineStringSegment();
}