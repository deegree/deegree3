//$HeadURL$
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
package org.deegree.model.spatialschema;

import java.io.Serializable;

import org.deegree.model.crs.CoordinateSystem;

import com.vividsolutions.jts.operation.buffer.BufferOp;

/**
 *
 * The basic interface for all geometries. it declares the methods that are common to all geometries. this doesn't means
 * for example that all geometries defines a valid boundary but is there asked for they should be able to answer (with
 * null).
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public interface Geometry extends Serializable {

    /**
     * (default) a semi-circle
     */
    int BUFFER_CAP_ROUND = BufferOp.CAP_ROUND;

    /**
     * a straight line perpendicular to the end segment
     */
    int BUFFER_CAP_BUTT = BufferOp.CAP_BUTT;

    /**
     * a half-square
     */
    int BUFFER_CAP_SQUARE = BufferOp.CAP_SQUARE;

    /**
     * @return the bounding box of a geometry
     */
    Envelope getEnvelope();

    /**
     * @return the boundary of a geometry
     */
    Boundary getBoundary();

    /**
     * The operation "dimension" shall return the inherent dimension of this Geometry, which shall be less than or equal
     * to the coordinate dimension. The dimension of a collection of geometric objects shall be the largest dimension of
     * any of its pieces. Points are 0-dimensional, curves are 1-dimensional, surfaces are 2-dimensional, and solids are
     * 3-dimensional.
     *
     * @return the for this geometry defined dimension.
     */
    int getDimension();

    /**
     * The operation "coordinateDimension" shall return the dimension of the coordinates that define this Geometry,
     * which must be the same as the coordinate dimension of the coordinate reference system for this Geometry.
     *
     * @return the actual dimension
     */
    int getCoordinateDimension();

    /**
     * @return the spatial reference system of a geometry
     */
    CoordinateSystem getCoordinateSystem();

    /**
     * @return true if no geometry values resp. points stored within the geometry.
     */
    boolean isEmpty();

    /**
     * The operation "distance" shall return the distance between this Geometry and another Geometry. This distance is
     * defined to be the greatest lower bound of the set of distances between all pairs of points that include one each
     * from each of the two Geometries. A "distance" value shall be a positive number associated to distance units such
     * as meters or standard foot. If necessary, the second geometric object shall be transformed into the same
     * coordinate reference system as the first before the distance is calculated.
     * <p>
     * </p>
     * If the geometric objects overlap, or touch, then their distance apart shall be zero. Some current implementations
     * use a "negative" distance for such cases, but the approach is neither consistent between implementations, nor
     * theoretically viable.
     *
     * @param other
     * @return the distance between this Geometry and another Geometry
     */
    double distance( Geometry other );

    /**
     * translate a geometry by the submitted values. if the length of <tt>d</tt> is smaller then the dimension of the
     * geometry only the first d.length'th coordinates will be translated. If the length of <tt>d</tt> is larger then
     * the dimension of the geometry an ArrayIndexOutOfBoundExceptions raises.
     *
     * @param d
     *            vector to translate the geometry with
     */
    void translate( double[] d );

    /**
     * The operation "centroid" shall return the mathematical centroid for this Geometry. The result is not guaranteed
     * to be on the object. For heterogeneous collections of primitives, the centroid only takes into account those of
     * the largest dimension. For example, when calculating the centroid of surfaces, an average is taken weighted by
     * area. Since curves have no area they do not contribute to the average.
     *
     * @return the centroid
     */
    Point getCentroid();

    /**
     * The operation "convexHull" shall return a Geometry that represents the convex hull of this Geometry.
     *
     * @return the convexHull
     */
    Geometry getConvexHull();

    /**
     * The operation "buffer" shall return a Geometry containing all points whose distance from this Geometry is less
     * than or equal to the "distance" passed as a parameter. The Geometry returned is in the same reference system as
     * this original Geometry. The dimension of the returned Geometry is normally the same as the coordinate dimension -
     * a collection of Surfaces in 2D space and a collection of Solids in 3D space, but this may be application defined.
     *
     * @param distance
     * @return a Geometry containing all points whose distance from this Geometry is less than or equal to the
     *         "distance" passed as a parameter
     */
    Geometry getBuffer( double distance );

    /**
     * The operation "buffer" shall return a Geometry containing all points whose distance from this Geometry is less
     * than or equal to the "distance" passed as a parameter. The Geometry returned is in the same reference system as
     * this original Geometry. The dimension of the returned Geometry is normally the same as the coordinate dimension -
     * a collection of Surfaces in 2D space and a collection of Solids in 3D space, but this may be application defined.
     *
     * @param distance
     * @param segments
     * @param capStyle
     * @return a Geometry containing all points whose distance from this Geometry is less than or equal to the
     *         "distance" passed as a parameter
     */
    Geometry getBuffer( double distance, int segments, int capStyle );

    /**
     * The Boolean valued operation "contains" shall return TRUE if this Geometry contains another Geometry.
     *
     * @param other
     * @return true if the other is conatained.
     */
    boolean contains( Geometry other );

    /**
     * The Boolean valued operation "contains" shall return TRUE if this Geometry contains a single point given by a
     * coordinate.
     *
     * @param position
     * @return true if this contains the position.
     */
    boolean contains( Position position );

    /**
     * The Boolean valued operation "intersects" shall return TRUE if this Geometry intersects another Geometry. Within
     * a Complex, the Primitives do not intersect one another. In general, topologically structured data uses shared
     * geometric objects to capture intersection information.
     *
     * @param other
     * @return true if this Geometry intersects another Geometry
     */
    boolean intersects( Geometry other );

    /**
     * The "union" operation shall return the set theoretic union of this Geometry and the passed Geometry.
     *
     * @param other
     * @return the union of this and the other.
     */
    Geometry union( Geometry other );

    /**
     * The "intersection" operation shall return the set theoretic intersection of this Geometry and the passed
     * Geometry.
     *
     * @param other
     * @return the intersection of this Geometry and the other Geometry.
     * @throws GeometryException
     */
    Geometry intersection( Geometry other )
                            throws GeometryException;

    /**
     * The "difference" operation shall return the set theoretic difference of this Geometry and the passed Geometry.
     *
     * @param other
     * @return the difference between this and the other.
     */
    Geometry difference( Geometry other );

    /**
     * provide optimized proximity queries within for a distance . calvin added on 10/21/2003
     *
     * @param other
     * @param distance
     * @return true if the geometry is in distance of this geometry
     */
    boolean isWithinDistance( Geometry other, double distance );

    /**
     * sets tolerance value use for topological operations
     *
     * @param tolerance
     */
    void setTolerance( double tolerance );

    /**
     * @return the tolerance value use for topological operations
     *
     */
    double getTolerance();

}
