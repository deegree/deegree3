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
package org.deegree.geometry;

import org.deegree.commons.types.gml.StandardObjectProperties;
import org.deegree.crs.CRS;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.uom.ValueWithUnit;

/**
 * Base interface for all vector geometry types.
 * <p>
 * This is the root of an ISO 19107/GML 3.1.1/GML 3.2.1 compliant geometry type hierarchy.
 * </p>
 * <p>
 * <h4>Notes on the representation of GML geometries</h4>
 * The "StandardObjectProperties" defined by GML (e.g. multiple <code>gml:name</code> elements or
 * <code>gml:description</code>) which are inherited by any GML geometry type definition are treated in a specific way.
 * They are modelled using the {@link StandardObjectProperties} class. This design decision has been driven by the goal
 * to make the implementation less GML (and GML-version) specific and to allow for example to export a {@link Geometry}
 * instance as either GML 3.2.1 or GML 3.1.1 (different namespaces for the standard properties).
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public interface Geometry {

    /**
     * Convenience enum type for discriminating the different geometry variants.
     */
    public enum GeometryType {
        /** Envelope */
        ENVELOPE,
        /** Primitive geometry */
        PRIMITIVE_GEOMETRY,
        /** Composited geometry */
        COMPOSITE_GEOMETRY,
        /** Multi (aggregate) geometry */
        MULTI_GEOMETRY,
    }

    /**
     * Returns the type of geometry.
     * 
     * @return the type of geometry
     */
    public GeometryType getGeometryType();

    /**
     * Returns the id of the geometry.
     * <p>
     * In an GML representation of the geometry, this corresponds to the <code>gml:id</code> (GML 3 and later) or
     * <code>gid</code> (GML 2) attribute of the geometry element.
     * </p>
     * 
     * @return the id of the feature
     */
    public String getId();

    /**
     * Returns the minimal bounding box of the geometry.
     * 
     * @return the minimal bounding box of the geometry
     */
    public Envelope getEnvelope();

    /**
     * Returns the coordinate dimension, i.e. the dimension of the space that the geometry is embedded in.
     * 
     * @return the coordinate dimension
     */
    public int getCoordinateDimension();

    /**
     * Returns the associated spatial reference system.
     * 
     * @return spatial reference system, may be null
     */
    public CRS getCoordinateSystem();

    /**
     * 
     * @return convex hull of a Geometry
     */
    public Geometry getConvexHull();

    /**
     * The operation "buffer" shall return a Geometry containing all points whose distance from this Geometry is less
     * than or equal to the "distance" passed as a parameter. The Geometry returned is in the same reference system as
     * this original Geometry. The dimension of the returned Geometry is normally the same as the coordinate dimension -
     * a collection of Surfaces in 2D space and a collection of Solids in 3D space, but this may be application defined.
     * 
     * @param distance
     * @return buffer geometry
     */
    public Geometry getBuffer( ValueWithUnit distance );

    /**
     * The Boolean valued operation "contains" shall return TRUE if this Geometry contains another Geometry.
     * 
     * @param geometry
     * @return true if this Geometry contains the other
     */
    public boolean contains( Geometry geometry );

    /**
     * The Boolean valued operation "contains" shall return TRUE if this Geometry crosses another Geometry.
     * 
     * @param geometry
     * @return true if this Geometry contains the other
     */
    public boolean crosses( Geometry geometry );

    /**
     * The Boolean valued operation "intersects" shall return TRUE if this Geometry intersects another Geometry. Within
     * a Complex, the Primitives do not intersect one another. In general, topologically structured data uses shared
     * geometric objects to capture intersection information.
     * 
     * @param geometry
     * @return true if both Geometries intersects
     */
    public boolean intersects( Geometry geometry );

    /**
     * 
     * @param geometry
     * @return true if both Geometries are disjoint
     */
    public boolean isDisjoint( Geometry geometry );

    /**
     * The Boolean valued operation "overlaps"...
     * 
     * @param geometry
     * @return true if both Geometries overlap
     */
    public boolean overlaps( Geometry geometry );

    /**
     * The Boolean valued operation "touches"...
     * 
     * @param geometry
     * @return true if both Geometries overlap
     */
    public boolean touches( Geometry geometry );

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
     * @param geometry
     * @return distance between two geometries
     */
    public double distance( Geometry geometry );

    /**
     * The "union" operation shall return the set theoretic union of this Geometry and the passed Geometry.
     * 
     * @param geometry
     * @return united Geometry
     */
    public Geometry union( Geometry geometry );

    /**
     * The "intersection" operation shall return the set theoretic intersection of this Geometry and the passed
     * Geometry.
     * 
     * @param geometry
     * @return intersection Geometry or <code>null</code>
     */
    public Geometry intersection( Geometry geometry );

    /**
     * The "difference" operation shall return the set theoretic difference of this Geometry and the passed Geometry.
     * 
     * @param geometry
     * @return difference Geometry or <code>null</code>
     */
    public Geometry difference( Geometry geometry );

    /**
     * Returns true if this geometry is equal to the specified geometry. The behaviour of this method is not 100%
     * specified and may differ with other implementations. E.g. a MULTIPOINT(A, B) could be equal to MULTIPOINT(B, A)
     * or not, depending on the implementation. If the internal order is the same however, this method returns the
     * expected result.
     * 
     * @param geometry
     * @return true if the geometries are equal
     */
    public boolean equals( Geometry geometry );

    /**
     * tests whether the value of a geometric is within a specified distance of this geometry.
     * 
     * @param geometry
     * @param distance
     * @return true if passed geometry is within a specified distance of this geometry.
     */
    public boolean isWithinDistance( Geometry geometry, ValueWithUnit distance );

    /**
     * tests whether the value of a geometric is beyond a specified distance of this geometry.
     * 
     * @param geometry
     * @param distance
     * @return true if passed geometry is beyond a specified distance of this geometry.
     */
    public boolean isBeyond( Geometry geometry, ValueWithUnit distance );

    /**
     * tests whether the value of a geometric is topological located within this geometry. This method is the opposite
     * of {@link #contains(Geometry)} method
     * 
     * @param geometry
     * @return true if passed geometry is located completly within this geometry
     */
    public boolean isWithin( Geometry geometry );

    /**
     * Returns the {@link PrecisionModel} of the geometry.
     * 
     * @return the precision model
     */
    public PrecisionModel getPrecision();

    /**
     * Returns a representation of the standard GML properties (e.g. <code>gml:name</code> or
     * <code>gml:description</code).
     * 
     * @return a representation of the standard GML properties, may be null
     */
    public StandardObjectProperties getStandardGMLProperties();

    /**
     * Sets the standard GML properties (e.g. <code>gml:name</code> or <code>gml:description</code).
     * 
     * @param standardProps
     *            representation of the standard GML properties
     */
    public void setStandardGMLProperties( StandardObjectProperties standardProps );

    /**
     * Returns an equivalent (or best-fit) JTS geometry object.
     * 
     * @return an equivalent (or best-fit) JTS geometry
     */
    public com.vividsolutions.jts.geom.Geometry getJTSGeometry();
}
