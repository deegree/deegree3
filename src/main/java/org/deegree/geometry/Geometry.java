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

import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.cs.CRS;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.Point;
import org.deegree.gml.GMLObject;
import org.deegree.gml.props.GMLStdProps;

/**
 * Base interface for all vector geometry types.
 * <p>
 * Root of the ISO 19107/GML 3.1.1/GML 3.2.1 compliant geometry type hierarchy. All geometries inherit methods for the
 * common topological predicates (e.g. {@link #intersects(Geometry)} and {@link #touches(Geometry)} as well as the usual
 * geometry creation methods (e.g {@link #getIntersection(Geometry)} and {@link #getBuffer(Measure)}).
 * </p>
 * <p>
 * <h4>Topological predicates</h4>
 * These are the methods for evaluting the common topological predicates:
 * <ul>
 * <li>{@link #contains(Geometry)}</li>
 * <li>{@link #crosses(Geometry)}</li>
 * <li>{@link #equals(Geometry)}</li>
 * <li>{@link #intersects(Geometry)}</li>
 * <li>{@link #isDisjoint(Geometry)}</li>
 * <li>{@link #isWithin(Geometry)}</li>
 * <li>{@link #isWithinDistance(Geometry,Measure)}</li>
 * <li>{@link #touches(Geometry)}</li>
 * </ul>
 * </p>
 * <p>
 * <h4>Set-theoretic methods</h4>
 * Methods for deriving geometries that aid spatial analysis tasks:
 * <ul>
 * <li>{@link #getBuffer(Measure)}</li>
 * <li>{@link #getCentroid()}</li>
 * <li>{@link #getConvexHull()}</li>
 * <li>{@link #getDifference(Geometry)}</li>
 * <li>{@link #getEnvelope()}</li>
 * <li>{@link #getIntersection(Geometry)}</li>
 * <li>{@link #getUnion(Geometry)}</li>
 * </ul>
 * Distance calculation:
 * <ul>
 * <li>{@link #getDistance(Geometry, Unit)}</li>
 * </ul>
 * </p>
 * <p>
 * <h4>Notes on the representation of GML geometries</h4>
 * The "StandardObjectProperties" defined by GML (e.g. multiple <code>gml:name</code> elements or
 * <code>gml:description</code>) which are inherited by any GML geometry type definition are treated in a specific way.
 * They are modelled using the {@link GMLStdProps} class. This design decision has been driven by the goal to
 * make the implementation less GML (and GML-version) specific and to allow for example to export a {@link Geometry}
 * instance as either GML 2, GML 3.1 or GML 3.2 (different namespaces and types for the standard properties).
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public interface Geometry extends GMLObject {

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
     * In a GML representation of the geometry, this corresponds to the <code>gml:id</code> (GML 3 and later) or
     * <code>gid</code> (GML 2) attribute of the geometry element.
     * </p>
     * 
     * @return the id of the geometry, or null if it is an anonymous (unidentified) geometry
     */
    public String getId();

    /**
     * Sets the id of the geometry.
     * 
     * @param id
     *            id of the geometry
     */
    public void setId( String id );

    /**
     * Returns the {@link PrecisionModel} of the geometry.
     * 
     * @return the precision model
     */
    public PrecisionModel getPrecision();

    /**
     * Sets the {@link PrecisionModel} of the geometry.
     * 
     * @param pm
     *            the precision model to set
     */
    public void setPrecision( PrecisionModel pm );

    /**
     * Returns the associated spatial reference system.
     * 
     * @return spatial reference system, may be null
     */
    public CRS getCoordinateSystem();

    /**
     * Sets the associated spatial reference system.
     * 
     * @param crs
     *            spatial reference system, may be null
     */
    public void setCoordinateSystem( CRS crs );

    /**
     * Sets the attached properties (e.g. GML standard properties, such as <code>gml:name</code>).
     * 
     * @param props
     *            properties to be attached
     */
    public void setGMLProperties( GMLStdProps props );

    /**
     * Returns the coordinate dimension, i.e. the dimension of the space that the geometry is embedded in.
     * 
     * @return the coordinate dimension
     */
    public int getCoordinateDimension();

    /**
     * Tests whether this geometry contains the specified geometry.
     * 
     * TODO formal explanation (DE9IM)
     * 
     * @param geometry
     *            the {@link Geometry} to test this {@link Geometry} against
     * @return true if this {@link Geometry} contains <code>geometry</code>
     */
    public boolean contains( Geometry geometry );

    /**
     * Tests whether this geometry crosses the specified geometry.
     * 
     * TODO formal explanation (DE9IM)
     * 
     * @param geometry
     *            the {@link Geometry} to test this {@link Geometry} against
     * @return true if this {@link Geometry} crosses <code>geometry</code>
     */
    public boolean crosses( Geometry geometry );

    /**
     * Tests whether this geometry is equal to the specified geometry.
     * 
     * TODO formal explanation (DE9IM)
     * 
     * @param geometry
     *            the {@link Geometry} to test this {@link Geometry} against
     * @return true if this {@link Geometry} is equal to <code>geometry</code>
     */
    public boolean equals( Geometry geometry );

    /**
     * Tests whether this geometry intersects the specified geometry.
     * 
     * TODO formal explanation (DE9IM)
     * 
     * @param geometry
     *            the {@link Geometry} to test this {@link Geometry} against
     * @return true if this {@link Geometry} intersects <code>geometry</code>
     */
    public boolean intersects( Geometry geometry );

    /**
     * Tests whether this geometry is beyond a specified distance of a second geometry.
     * 
     * @param geometry
     *            second geometry
     * @param distance
     * @return true, iff the minimum distance between this and the second geometry is greater than <code>distance</code>
     */
    public boolean isBeyond( Geometry geometry, Measure distance );

    /**
     * Tests whether this geometry is disjoint from the specified geometry.
     * 
     * TODO formal explanation (DE9IM)
     * 
     * @param geometry
     *            the {@link Geometry} to test this {@link Geometry} against
     * @return true if this {@link Geometry} is disjoint from <code>geometry</code>
     */
    public boolean isDisjoint( Geometry geometry );

    /**
     * tests whether the value of a geometric is topological located within this geometry. This method is the opposite
     * of {@link #contains(Geometry)} method
     * 
     * @param geometry
     * @return true if passed geometry is located completly within this geometry
     */
    public boolean isWithin( Geometry geometry );

    /**
     * Tests whether this geometry is within a specified distance of a second geometry.
     * 
     * @param geometry
     *            second geometry
     * @param distance
     * @return true, iff the distance between this and the second geometry is less than or equal <code>distance</code>
     */
    public boolean isWithinDistance( Geometry geometry, Measure distance );

    /**
     * Tests whether this geometry overlaps the specified geometry.
     * 
     * TODO formal explanation (DE9IM)
     * 
     * @param geometry
     *            the {@link Geometry} to test this {@link Geometry} against
     * @return true if this {@link Geometry} overlaps <code>geometry</code>
     */
    public boolean overlaps( Geometry geometry );

    /**
     * Tests whether this geometry touches the specified geometry.
     * 
     * TODO formal explanation (DE9IM)
     * 
     * @param geometry
     *            the {@link Geometry} to test this {@link Geometry} against
     * @return true if this {@link Geometry} touches <code>geometry</code>
     */
    public boolean touches( Geometry geometry );

    /**
     * Return a new {@link Geometry} that contains all points with a distance from this Geometry that is less than or
     * equal to the specified distance.
     * 
     * @param distance
     * @return buffer geometry
     */
    public Geometry getBuffer( Measure distance );

    /**
     * Returns the centroid of the geometry.
     * 
     * @return a {@link Point} that is the centroid of this geometry
     */
    public Point getCentroid();

    /**
     * Returns the minimal bounding box of the geometry.
     * 
     * @return the minimal bounding box of the geometry
     */
    public Envelope getEnvelope();

    /**
     * Returns the set-theoretic difference of this and the passed {@link Geometry}.
     * 
     * @param geometry
     *            other geometry, must not be null
     * @return difference Geometry or <code>null</code> (empty set)
     */
    public Geometry getDifference( Geometry geometry );

    /**
     * Returns the set-theoretic intersection of this and the passed {@link Geometry}.
     * 
     * @param geometry
     *            other geometry, must not be null
     * @return intersection Geometry or <code>null</code> (empty set)
     */
    public Geometry getIntersection( Geometry geometry );

    /**
     * Returns the set-theoretic union of this and the passed {@link Geometry}.
     * 
     * @param geometry
     *            other geometry, must not be null
     * @return united Geometry (never null)
     */
    public Geometry getUnion( Geometry geometry );

    /**
     * Returns the convex hull of the geometry.
     * 
     * @return convex hull of a Geometry
     */
    public Geometry getConvexHull();

    /**
     * Returns the minimum distance between this and the specified geometry.
     * 
     * @param geometry
     *            second geometry
     * @param requestedUnits
     *            unit of the
     * @return shortest distance between the two geometries
     */
    public Measure getDistance( Geometry geometry, Unit requestedUnits );
}
