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
package org.deegree.geometry.standard;

import static java.util.Collections.emptyList;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.tom.gml.GMLObjectType;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKTWriter;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.refs.GeometryReference;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.geometry.utils.GeometryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for the default {@link Geometry} implementation.
 * <p>
 * This implementation is built around
 * <a href="http://tsusiatsoftware.net/jts/main.html">JTS (Java Topology Suite)</a>
 * geometries which are used to evaluate topological predicates (e.g. intersects) and
 * perform spatial analysis operations (e.g union). Simple geometries (e.g.
 * {@link LineString}s are mapped to a corresponding JTS object, for complex ones (e.g.
 * {@link Curve}s with non-linear segments), the JTS geometry only approximates the
 * original geometry. See
 * <a href="https://wiki.deegree.org/deegreeWiki/deegree3/MappingComplexGeometries">this
 * page</a> for a discussion.
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public abstract class AbstractDefaultGeometry implements Geometry {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractDefaultGeometry.class);

	/**
	 * Used to built JTS geometries.
	 */
	protected final static org.locationtech.jts.geom.GeometryFactory jtsFactory = new org.locationtech.jts.geom.GeometryFactory();

	/** Geometry identifier. */
	protected String id;

	private GMLObjectType type;

	private List<Property> props;

	/** Reference to a coordinate system. */
	protected ICRS crs;

	protected PrecisionModel pm;

	// contains an equivalent (or best-fit) JTS geometry object
	protected org.locationtech.jts.geom.Geometry jtsGeometry;

	protected Envelope env;

	/**
	 * @param id
	 * @param crs
	 * @param pm
	 */
	public AbstractDefaultGeometry(String id, ICRS crs, PrecisionModel pm) {
		this.id = id;
		this.crs = crs;
		this.pm = pm;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public GMLObjectType getType() {
		return type;
	}

	@Override
	public void setType(GMLObjectType type) {
		this.type = type;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public ICRS getCoordinateSystem() {
		return crs;
	}

	@Override
	public void setCoordinateSystem(ICRS crs) {
		this.crs = crs;
	}

	@Override
	public PrecisionModel getPrecision() {
		return pm;
	}

	@Override
	public void setPrecision(PrecisionModel pm) {
		this.pm = pm;
	}

	@Override
	public boolean intersects(Geometry geometry) {
		JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair(this, geometry);
		return jtsGeoms.first.intersects(jtsGeoms.second);
	}

	@Override
	public boolean isDisjoint(Geometry geometry) {
		JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair(this, geometry);
		return jtsGeoms.first.disjoint(jtsGeoms.second);
	}

	@Override
	public boolean overlaps(Geometry geometry) {
		JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair(this, geometry);
		return jtsGeoms.first.overlaps(jtsGeoms.second);
	}

	@Override
	public boolean touches(Geometry geometry) {
		JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair(this, geometry);
		return jtsGeoms.first.touches(jtsGeoms.second);
	}

	@Override
	public boolean isWithin(Geometry geometry) {
		JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair(this, geometry);
		return jtsGeoms.first.within(jtsGeoms.second);
	}

	@Override
	public boolean isWithinDistance(Geometry geometry, Measure distance) {
		LOG.warn("TODO: Respect UOM in evaluation of topological predicate.");
		JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair(this, geometry);
		return jtsGeoms.first.isWithinDistance(jtsGeoms.second, distance.getValueAsDouble());
	}

	@Override
	public boolean isBeyond(Geometry geometry, Measure distance) {
		return !isWithinDistance(geometry, distance);
	}

	@Override
	public boolean contains(Geometry geometry) {
		JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair(this, geometry);
		return jtsGeoms.first.contains(jtsGeoms.second);
	}

	@Override
	public boolean crosses(Geometry geometry) {
		JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair(this, geometry);
		return jtsGeoms.first.crosses(jtsGeoms.second);
	}

	@Override
	public boolean equals(Geometry geometry) {
		JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair(this, geometry);
		return jtsGeoms.first.equals(jtsGeoms.second);
	}

	@Override
	public Point getCentroid() {
		return (Point) createFromJTS(getJTSGeometry().getCentroid(), crs);
	}

	@Override
	public Measure getDistance(Geometry geometry, Unit requestedUnit) {
		// TODO respect unit
		double dist = getJTSGeometry().distance(getAsDefaultGeometry(geometry).getJTSGeometry());
		return new Measure(Double.toString(dist), null);
	}

	@Override
	public Geometry getIntersection(Geometry geometry) {
		JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair(this, geometry);
		ICRS crs = this.crs;
		if (crs == null) {
			crs = geometry.getCoordinateSystem();
		}
		org.locationtech.jts.geom.Geometry jtsGeom = jtsGeoms.first.intersection(jtsGeoms.second);
		return createFromJTS(jtsGeom, crs);
	}

	@Override
	public Geometry getUnion(Geometry geometry) {
		JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair(this, geometry);
		org.locationtech.jts.geom.Geometry jtsGeom = jtsGeoms.first.union(jtsGeoms.second);
		return createFromJTS(jtsGeom, crs);
	}

	@Override
	public Geometry getDifference(Geometry geometry) {
		JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair(this, geometry);
		org.locationtech.jts.geom.Geometry jtsGeom = jtsGeoms.first.difference(jtsGeoms.second);
		return createFromJTS(jtsGeom, crs);
	}

	@Override
	public Geometry getBuffer(Measure distance) {
		// TODO get double in CoordinateSystem units
		double crsDistance = distance.getValueAsDouble();
		org.locationtech.jts.geom.Geometry jtsGeom = getJTSGeometry().buffer(crsDistance);
		return createFromJTS(jtsGeom, crs);
	}

	@Override
	public Geometry getConvexHull() {
		org.locationtech.jts.geom.Geometry jtsGeom = getJTSGeometry().convexHull();
		return createFromJTS(jtsGeom, crs);
	}

	@Override
	public Envelope getEnvelope() {
		if (env == null) {
			org.locationtech.jts.geom.Envelope jtsEnvelope = getJTSGeometry().getEnvelopeInternal();
			Point min = new DefaultPoint(null, crs, pm, new double[] { jtsEnvelope.getMinX(), jtsEnvelope.getMinY() });
			Point max = new DefaultPoint(null, crs, pm, new double[] { jtsEnvelope.getMaxX(), jtsEnvelope.getMaxY() });
			env = new DefaultEnvelope(null, crs, pm, min, max);
		}
		return env;
	}

	/**
	 * Returns an equivalent (or best-fit) JTS geometry object.
	 * @return an equivalent (or best-fit) JTS geometry
	 */
	public org.locationtech.jts.geom.Geometry getJTSGeometry() {
		if (jtsGeometry == null) {
			jtsGeometry = buildJTSGeometry();
		}
		return jtsGeometry;
	}

	protected org.locationtech.jts.geom.Geometry buildJTSGeometry() {
		throw new UnsupportedOperationException(
				"#buildJTSGeometry() is not implemented for " + this.getClass().getName());
	}

	@Override
	public List<Property> getProperties() {
		if (props == null) {
			return emptyList();
		}
		return props;
	}

	@Override
	public List<Property> getProperties(QName propName) {
		if (props == null) {
			return emptyList();
		}
		List<Property> namedProps = new ArrayList<Property>(props.size());
		for (Property property : props) {
			if (propName.equals(property.getName())) {
				namedProps.add(property);
			}
		}
		return namedProps;
	}

	@Override
	public void setProperties(List<Property> props) {
		this.props = props;
	}

	/**
	 * Deprecated: Use
	 * {@link GeometryUtils#createFromJTS(org.locationtech.jts.geom.Geometry, ICRS)}!
	 *
	 * Helper methods for creating {@link AbstractDefaultGeometry} from JTS geometries
	 * that have been derived from this geometry by JTS spatial analysis methods.
	 * @param jtsGeom
	 * @param crs
	 * @return geometry with precision model and CoordinateSystem information that are
	 * identical to the ones of this geometry, or null if the given geometry is an empty
	 * collection
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public AbstractDefaultGeometry createFromJTS(org.locationtech.jts.geom.Geometry jtsGeom, ICRS crs) {
		return GeometryUtils.createFromJTS(jtsGeom, crs);
	}

	protected static AbstractDefaultGeometry getAsDefaultGeometry(Geometry geometry) {
		if (geometry instanceof AbstractDefaultGeometry) {
			return (AbstractDefaultGeometry) geometry;
		}
		if (geometry instanceof GeometryReference<?>) {
			Geometry refGeometry = ((GeometryReference<?>) geometry).getReferencedObject();
			if (refGeometry instanceof AbstractDefaultGeometry) {
				return (AbstractDefaultGeometry) refGeometry;
			}
		}
		throw new RuntimeException("Cannot convert Geometry to AbstractDefaultGeometry.");
	}

	@Override
	public boolean isSFSCompliant() {
		return false;
	}

	@Override
	public String toString() {
		String wkt = WKTWriter.write(this);
		if (wkt.length() > 1000) {
			return wkt.substring(0, 1000) + " [...]";
		}
		return wkt;
	}

}
