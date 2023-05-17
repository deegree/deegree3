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

import java.util.List;

import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.GMLObjectType;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;

/**
 * Base interface for all vector geometry types.
 * <p>
 * Root of deegree's ISO 19107/GML 3.1.1/GML 3.2.1 compliant geometry type hierarchy. All
 * geometries inherit methods for the common topological predicates (e.g.
 * {@link #intersects(Geometry)} and {@link #touches(Geometry)} as well as the usual
 * geometry creation methods (e.g {@link #getIntersection(Geometry)} and
 * {@link #getBuffer(Measure)}).
 * </p>
 * <p>
 * <h4>Topological predicates</h4> These are the methods for evaluating the common
 * topological predicates:
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
 * <h4>Set-theoretic methods</h4> Methods for deriving geometries that aid spatial
 * analysis tasks:
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
 * <h4>Simple Feature Specification (SFS) compliance</h4> TODO: check with
 * specification<br>
 * As the expressiveness of the ISO 19107 model is much more powerful than the
 * <a href="http://www.opengeospatial.org/standards/sfa">Simple Feature Specification
 * (SFS)</a>, a deegree {@link Geometry} is not automatically a compliant SFS geometry.
 * {@link #isSFSCompliant()} can be used to check if this is the case, it returns
 * <code>true</code> for the following subtypes / configurations:
 * <ul>
 * <li>{@link Point}</li>
 * <li>{@link LineString}</li>
 * <li>{@link Polygon} (with {@link LinearRing} boundaries)</li>
 * <li>{@link MultiPoint}</li>
 * <li>{@link MultiLineString}</li>
 * <li>{@link MultiPolygon} (if the members are SFS-compliant)</li>
 * <li>{@link MultiGeometry} (if the members are SFS-compliant)</li>
 * </ul>
 * If the geometry is not SFS-compliant, {@link SFSProfiler} can be used to simplify it
 * into an SFS geometry.
 * </p>
 * <p>
 * <h4>Notes on GML properties</h4> GML geometries allow for non-geometric properties,
 * such as the ones from the <code>gml:StandardObjectProperties</code> attribute group.
 * Additionally, extensions of core GML geometry elements (e.g.
 * <code>aixm:ElevatedPoint</code> from the AIXM 5.1 application schema) may define even
 * more properties which don't actually add to the geometric semantics of the element. The
 * properties of GML geometry can hence be divided like this:
 * <ul>
 * <li>Standard GML properties (e.g. <code>gml:name</code>)</li>
 * <li>Properties with a geometric semantics (e.g. <code>gml:pos/code>)</li>
 * <li>Additional properties (for application-schema defined geometry elements)</li>
 * </ul>
 * Note that the methods operating on properties ({@link #getProperties()},
 * {@link #getProperties(javax.xml.namespace.QName), {@link #setProperties(List)})
 * currently only deal with the standard and additional properties.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 *
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
	 * @return the type of geometry
	 */
	public GeometryType getGeometryType();

	/**
	 * Sets the id of the geometry.
	 * @param id id of the geometry
	 */
	public void setId(String id);

	/**
	 * Attaches {@link GMLObjectType} information to this geometry.
	 * @param type type information to be attached, may be <code>null</code>
	 */
	public void setType(GMLObjectType type);

	/**
	 * Returns the {@link PrecisionModel} of the geometry.
	 * @return the precision model
	 */
	public PrecisionModel getPrecision();

	/**
	 * Sets the {@link PrecisionModel} of the geometry.
	 * @param pm the precision model to set
	 */
	public void setPrecision(PrecisionModel pm);

	/**
	 * Returns the associated spatial reference system.
	 * @return spatial reference system, may be null
	 */
	public ICRS getCoordinateSystem();

	/**
	 * Sets the associated spatial reference system.
	 * @param crs spatial reference system, may be null
	 */
	public void setCoordinateSystem(ICRS crs);

	/**
	 * Sets the attached properties.
	 * @param props properties to be attached
	 */
	public void setProperties(List<Property> props);

	/**
	 * Returns whether this geometry complies with the <i>Simple Feature Specification
	 * (SFS)</i>.
	 * @return <code>true</code>, if this geometry complies with the SFS,
	 * <code>false</code> otherwise
	 */
	public boolean isSFSCompliant();

	/**
	 * Returns the coordinate dimension, i.e. the dimension of the space that the geometry
	 * is embedded in.
	 * @return the coordinate dimension
	 */
	public int getCoordinateDimension();

	/**
	 * Tests whether this geometry contains the specified geometry.
	 *
	 * TODO formal explanation (DE9IM)
	 * @param geometry the {@link Geometry} to test this {@link Geometry} against
	 * @return true if this {@link Geometry} contains <code>geometry</code>
	 */
	public boolean contains(Geometry geometry);

	/**
	 * Tests whether this geometry crosses the specified geometry.
	 *
	 * TODO formal explanation (DE9IM)
	 * @param geometry the {@link Geometry} to test this {@link Geometry} against
	 * @return true if this {@link Geometry} crosses <code>geometry</code>
	 */
	public boolean crosses(Geometry geometry);

	/**
	 * Tests whether this geometry is equal to the specified geometry.
	 *
	 * TODO formal explanation (DE9IM)
	 * @param geometry the {@link Geometry} to test this {@link Geometry} against
	 * @return true if this {@link Geometry} is equal to <code>geometry</code>
	 */
	public boolean equals(Geometry geometry);

	/**
	 * Tests whether this geometry intersects the specified geometry.
	 *
	 * TODO formal explanation (DE9IM)
	 * @param geometry the {@link Geometry} to test this {@link Geometry} against
	 * @return true if this {@link Geometry} intersects <code>geometry</code>
	 */
	public boolean intersects(Geometry geometry);

	/**
	 * Tests whether this geometry is beyond a specified distance of a second geometry.
	 * @param geometry second geometry
	 * @param distance
	 * @return true, iff the minimum distance between this and the second geometry is
	 * greater than <code>distance</code>
	 */
	public boolean isBeyond(Geometry geometry, Measure distance);

	/**
	 * Tests whether this geometry is disjoint from the specified geometry.
	 *
	 * TODO formal explanation (DE9IM)
	 * @param geometry the {@link Geometry} to test this {@link Geometry} against
	 * @return true if this {@link Geometry} is disjoint from <code>geometry</code>
	 */
	public boolean isDisjoint(Geometry geometry);

	/**
	 * tests whether the value of a geometric is topological located within this geometry.
	 * This method is the opposite of {@link #contains(Geometry)} method
	 * @param geometry
	 * @return true if passed geometry is located completly within this geometry
	 */
	public boolean isWithin(Geometry geometry);

	/**
	 * Tests whether this geometry is within a specified distance of a second geometry.
	 * @param geometry second geometry
	 * @param distance
	 * @return true, iff the distance between this and the second geometry is less than or
	 * equal <code>distance</code>
	 */
	public boolean isWithinDistance(Geometry geometry, Measure distance);

	/**
	 * Tests whether this geometry overlaps the specified geometry.
	 *
	 * TODO formal explanation (DE9IM)
	 * @param geometry the {@link Geometry} to test this {@link Geometry} against
	 * @return true if this {@link Geometry} overlaps <code>geometry</code>
	 */
	public boolean overlaps(Geometry geometry);

	/**
	 * Tests whether this geometry touches the specified geometry.
	 *
	 * TODO formal explanation (DE9IM)
	 * @param geometry the {@link Geometry} to test this {@link Geometry} against
	 * @return true if this {@link Geometry} touches <code>geometry</code>
	 */
	public boolean touches(Geometry geometry);

	/**
	 * Return a new {@link Geometry} that contains all points with a distance from this
	 * Geometry that is less than or equal to the specified distance.
	 * @param distance
	 * @return buffer geometry
	 */
	public Geometry getBuffer(Measure distance);

	/**
	 * Returns the centroid of the geometry.
	 * @return a {@link Point} that is the centroid of this geometry
	 */
	public Point getCentroid();

	/**
	 * Returns the minimal bounding box of the geometry.
	 * @return the minimal bounding box of the geometry
	 */
	public Envelope getEnvelope();

	/**
	 * Returns the set-theoretic difference of this and the passed {@link Geometry}.
	 * @param geometry other geometry, must not be null
	 * @return difference Geometry or <code>null</code> (empty set)
	 */
	public Geometry getDifference(Geometry geometry);

	/**
	 * Returns the set-theoretic intersection of this and the passed {@link Geometry}.
	 * @param geometry other geometry, must not be null
	 * @return intersection Geometry or <code>null</code> (empty set)
	 */
	public Geometry getIntersection(Geometry geometry);

	/**
	 * Returns the set-theoretic union of this and the passed {@link Geometry}.
	 * @param geometry other geometry, must not be null
	 * @return united Geometry (never null)
	 */
	public Geometry getUnion(Geometry geometry);

	/**
	 * Returns the convex hull of the geometry.
	 * @return convex hull of a Geometry
	 */
	public Geometry getConvexHull();

	/**
	 * Returns the minimum distance between this and the specified geometry.
	 * @param geometry second geometry
	 * @param requestedUnits unit of the
	 * @return shortest distance between the two geometries
	 */
	public Measure getDistance(Geometry geometry, Unit requestedUnits);

}
