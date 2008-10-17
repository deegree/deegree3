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

import org.deegree.model.crs.coordinatesystems.CoordinateSystem;

/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public interface Curve extends Primitive {

    /**
     * orientation of a curve
     * 
     */
    public enum Orientation {
        /**
         * Indicating a positive orientation
         */
        positive, 
        /**
         * Indicating a negative orientation
         */
        negative, 
        /**
         * the orientation is not known.
         */
        unknown
    }

    /**
     * 
     * @return a curves x-coordinates as an array
     */
    public double[] getX();

    /**
     * 
     * @return a curves y-coordinates as an array
     */
    public double[] getY();

    /**
     * 
     * @return a curves z-coordinates as an array
     */
    public double[] getZ();

    /**
     * 
     * @return all coordinated as an array. The array will be constructed an concatination of the
     *         arrays of the segements points. For a three dimensional case it looks like:
     *         [x0,y0,z0,x1,y1,z1, ... ,xn,yn,zn]
     */
    public double[] getAsArray();

    /**
     * 
     * @return points constructing a curve as {@link List}
     */
    public List<Point> getPoints();

    /**
     * 
     * @return a curves orientation
     */
    public Orientation getOrientation();

    /**
     * 
     * @return length of a curve measured in units of the assigend {@link CoordinateSystem}
     */
    public double getLength();

    /**
     * The boundary of a curve is the set of points at either end of the curve. If the curve is a
     * cycle, the two ends are identical, and the curve (if topologically closed) is considered to
     * not have a boundary.
     * 
     * @return boundary of a curve. If a curve does not have a boundary because it is closed an
     *         empty {@link List} shall be retruned
     */
    public List<Point> getBoundary();

    /**
     * The boundary of a curve is the set of points at either end of the curve. If the curve is a
     * cycle, the two ends are identical, and the curve (if topologically closed) is considered to
     * not have a boundary.
     * 
     * @return true if the first and last {@link Point} of a curve are identical
     */
    public boolean isClosed();

    /**
     * 
     * @return segments forming a curve. a simple curves consists of one segment
     */
    public List<CurveSegment> getCurveSegments();

}