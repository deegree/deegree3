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
package org.deegree.model.geometry;

import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.geometry.primitive.Envelope;

/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: poth $
 * 
 * @version. $Revision: 6251 $, $Date: 2007-03-19 16:59:28 +0100 (Mo, 19 Mrz 2007) $
 */
public interface Geometry {

    public Envelope getEnvelope();

    /**
     * 
     * @return convex hull of a Geometry
     */
    public Geometry getConvexHull();

    /**
     * returns the spatial reference system of a geometry
     * 
     * @return spatial reference system of a geometry
     */
    public CoordinateSystem getCoordinateSystem();

    /**
     * The operation "coordinateDimension" shall return the dimension of the coordinates that define
     * this Geometry, which must be the same as the coordinate dimension of the coordinate reference
     * system for this Geometry.
     * 
     * @return coordinate dimension (usually 2 or 3)
     */
    public int getCoordinateDimension();

    /**
     * The operation "buffer" shall return a Geometry containing all points whose distance from this
     * Geometry is less than or equal to the "distance" passed as a parameter. The Geometry returned
     * is in the same reference system as this original Geometry. The dimension of the returned
     * Geometry is normally the same as the coordinate dimension - a collection of Surfaces in 2D
     * space and a collection of Solids in 3D space, but this may be application defined.
     * 
     * @param distance
     * @return buffer geometry
     */
    public Geometry getBuffer( double distance );

    /**
     * The Boolean valued operation "contains" shall return TRUE if this Geometry contains another
     * Geometry.
     * 
     * @param geometry
     * @return true if this Geometry contains the other
     */
    public boolean contains( Geometry geometry );

    /**
     * The Boolean valued operation "intersects" shall return TRUE if this Geometry intersects
     * another Geometry. Within a Complex, the Primitives do not intersect one another. In general,
     * topologically structured data uses shared geometric objects to capture intersection
     * information.
     * 
     * @param geometry
     * @return true if both Geometries intersects
     */
    public boolean intersects( Geometry geometry );

    /**
     * The operation "distance" shall return the distance between this Geometry and another
     * Geometry. This distance is defined to be the greatest lower bound of the set of distances
     * between all pairs of points that include one each from each of the two Geometries. A
     * "distance" value shall be a positive number associated to distance units such as meters or
     * standard foot. If necessary, the second geometric object shall be transformed into the same
     * coordinate reference system as the first before the distance is calculated.
     * <p>
     * </p>
     * If the geometric objects overlap, or touch, then their distance apart shall be zero. Some
     * current implementations use a "negative" distance for such cases, but the approach is neither
     * consistent between implementations, nor theoretically viable.
     * 
     * @param geometry
     * @return distance between two geometries
     */
    public double distance( Geometry geometry );

    /**
     * The "union" operation shall return the set theoretic union of this Geometry and the passed
     * Geometry.
     * 
     * @param geometry
     * @return united Geometry
     */
    public Geometry union( Geometry geometry );

    /**
     * The "intersection" operation shall return the set theoretic intersection of this Geometry and
     * the passed Geometry.
     * 
     * @param geometry
     * @return intersection Geometry or <code>null</code>
     */
    public Geometry intersection( Geometry geometry );

    /**
     * The "difference" operation shall return the set theoretic difference of this Geometry and the
     * passed Geometry.
     * 
     * @param geometry
     * @return difference Geometry or <code>null</code>
     */
    public Geometry difference( Geometry geometry );

    /**
     * tests whether the value of a geometric is within a specified distance of this geometry.
     * 
     * @param geometry
     * @param distance
     * @return true if passed geometry is within a specified distance of this geometry.
     */
    public boolean isWithinDistance( Geometry geometry, double distance );
    
    /**
     * tests whether the value of a geometric is beyond a specified distance of this geometry.
     * 
     * @param geometry
     * @param distance
     * @return true if passed geometry is beyond a specified distance of this geometry.
     */
    public boolean isBeyond( Geometry geometry, double distance );

    /**
     * tests whether the value of a geometric is topological located within this geometry. This
     * method is the opposite of {@link #contains(Geometry)} method
     * 
     * @param geometry
     * @return true if passed geometry is located completly within this geometry
     */
    public boolean isWithin( Geometry geometry );

    /**
     * returns the precision coordinates of a geometry are stored
     * 
     * @return precision coordinates of a geometry are stored
     */
    public double getPrecision();

}