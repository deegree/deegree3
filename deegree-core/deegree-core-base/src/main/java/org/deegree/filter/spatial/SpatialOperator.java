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
package org.deegree.filter.spatial;

import java.util.HashMap;
import java.util.Map;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.Operator;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.i18n.Messages;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines a topological predicate that can be evaluated on {@link Geometry} valued
 * objects.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public abstract class SpatialOperator implements Operator {

	private static final Logger LOG = LoggerFactory.getLogger(SpatialOperator.class);

	private final Map<String, Geometry> srsNameToTransformedGeometry = new HashMap<String, Geometry>();

	protected final Expression param1;

	protected final ValueReference param2AsValueReference;

	protected final Geometry param2AsGeometry;

	/**
	 * Instantiates a {@link SpatialOperator} without second parameter (may be stored in
	 * the implementation).
	 * @param param1 may actually be <code>null</code> (deegree extension to cope with
	 * features that have only hidden geometry props)
	 * @param geometry second parameter, never <code>null</code>
	 */
	protected SpatialOperator(Expression param1, Geometry geometry) {
		this(param1, geometry, null);
	}

	/**
	 * Instantiates a {@link Intersects} operator with value reference as second
	 * parameter.
	 * @param param1 may actually be <code>null</code> (deegree extension to cope with
	 * features that have only hidden geometry props)
	 * @param valueReference second parameter,never <code>null</code>
	 */
	protected SpatialOperator(Expression param1, ValueReference valueReference) {
		this(param1, null, valueReference);
	}

	private SpatialOperator(Expression param1, Geometry geometry, ValueReference valueReference) {
		this.param1 = param1;
		this.param2AsGeometry = geometry;
		this.param2AsValueReference = valueReference;
	}

	/**
	 * Convenience enum type for discriminating the different {@link SpatialOperator}
	 * types.
	 */
	public enum SubType {

		/**
		 * True iff the two operands are identical. The {@link SpatialOperator} is an
		 * instance of {@link Equals}.
		 */
		EQUALS,
		/**
		 * True iff the two operands are disjoint. The {@link SpatialOperator} is an
		 * instance of {@link Disjoint}.
		 */
		DISJOINT,
		/**
		 * True iff the two operands touch. The {@link SpatialOperator} is an instance of
		 * {@link Touches}.
		 */
		TOUCHES,
		/**
		 * True iff the first operand is completely inside the second. The
		 * {@link SpatialOperator} is an instance of {@link Within}.
		 */
		WITHIN,
		/**
		 * True iff ... The {@link SpatialOperator} is an instance of {@link Overlaps}.
		 */
		OVERLAPS,
		/** True iff ... The {@link SpatialOperator} is an instance of {@link Crosses}. */
		CROSSES,
		/**
		 * True iff ... The {@link SpatialOperator} is an instance of {@link Intersects}.
		 */
		INTERSECTS,
		/**
		 * True iff ... The {@link SpatialOperator} is an instance of {@link Contains}.
		 */
		CONTAINS,
		/** True iff ... The {@link SpatialOperator} is an instance of {@link DWithin}. */
		DWITHIN,
		/** True iff ... The {@link SpatialOperator} is an instance of {@link Beyond}. */
		BEYOND,
		/** True iff ... The {@link SpatialOperator} is an instance of {@link BBOX}. */
		BBOX

	}

	/**
	 * Always returns {@link Operator.Type#SPATIAL} (for {@link SpatialOperator}
	 * instances).
	 * @return {@link Operator.Type#SPATIAL}
	 */
	public Type getType() {
		return Type.SPATIAL;
	}

	/**
	 * Returns the type of spatial operator. Use this to safely determine the subtype of
	 * {@link SpatialOperator}.
	 * @return type of spatial operator
	 */
	public SubType getSubType() {
		return SubType.valueOf(getClass().getSimpleName().toUpperCase());
	}

	/**
	 * Returns the first spatial parameter.
	 * @return the first spatial parameter, may be <code>null</code> (target default
	 * geometry property of object)
	 */
	public Expression getParam1() {
		return param1;
	}

	/**
	 * @return the second parameter, <code>null</code> if it is a geometry
	 */
	public ValueReference getValueReference() {
		return param2AsValueReference;
	}

	/**
	 * @return the second parameter, <code>null</code> if it is a value reference
	 */
	public Geometry getGeometry() {
		return param2AsGeometry;
	}

	/**
	 * Returns the name of the spatial property to be considered.
	 * @return the name of the property, may be <code>null</code> (target default geometry
	 * property of object)
	 * @deprecated use {@link #getParam1()} instead
	 */
	public ValueReference getPropName() {
		return (ValueReference) param1;
	}

	/**
	 * Performs a checked cast to {@link Geometry}. If the given value is neither null nor
	 * a {@link Geometry} instance, a corresponding {@link FilterEvaluationException} is
	 * thrown.
	 * @param value
	 * @return the very same value (if it is a {@link Geometry} or <code>null</code>)
	 * @throws FilterEvaluationException if the value is neither <code>null</code> nor a
	 * {@link Geometry}
	 */
	protected Geometry checkGeometryOrNull(TypedObjectNode value) throws FilterEvaluationException {
		Geometry geom = null;
		if (value != null) {
			if (value instanceof Geometry) {
				geom = (Geometry) value;
			}
			else if (value instanceof Property && ((Property) value).getValue() instanceof Geometry) {
				geom = (Geometry) ((Property) value).getValue();
			}
			else if (value instanceof GenericXMLElement) {
				GenericXMLElement xml = (GenericXMLElement) value;
				if (xml.getChildren().isEmpty()) {
					String msg = Messages.getMessage("FILTER_EVALUATION_NOT_GEOMETRY", getType().name(), value);
					throw new FilterEvaluationException(msg);
				}
				TypedObjectNode maybeGeom = xml.getChildren().get(0);
				if (maybeGeom instanceof Geometry) {
					return (Geometry) maybeGeom;
				}
				String msg = Messages.getMessage("FILTER_EVALUATION_NOT_GEOMETRY", getType().name(), value);
				throw new FilterEvaluationException(msg);
			}
			else {
				String msg = Messages.getMessage("FILTER_EVALUATION_NOT_GEOMETRY", getType().name(), value);
				throw new FilterEvaluationException(msg);
			}
		}
		return geom;
	}

	/**
	 * Returns a version of the given geometry literal that has the same srs as the given
	 * geometry parameter.
	 * @param param geometry parameter, must not be <code>null</code>
	 * @param literal geometry literal, must not be <code>null</code>
	 * @return literal geometry with the same srs as the parameter geometry
	 * @throws FilterEvaluationException if the transformation failed
	 */
	protected Geometry getCompatibleGeometry(Geometry param, Geometry literal) throws FilterEvaluationException {
		Geometry transformedLiteral = literal;
		ICRS paramCRS = param.getCoordinateSystem();
		ICRS literalCRS = literal.getCoordinateSystem();
		if (literalCRS != null && !(paramCRS.equals(literalCRS))) {
			LOG.debug("Need transformed literal geometry for evaluation: " + literalCRS.getAlias() + " -> "
					+ paramCRS.getAlias());
			transformedLiteral = srsNameToTransformedGeometry.get(paramCRS.getAlias());
			if (transformedLiteral == null) {
				try {
					GeometryTransformer transformer = new GeometryTransformer(paramCRS);
					transformedLiteral = transformer.transform(literal);
					srsNameToTransformedGeometry.put(paramCRS.getAlias(), transformedLiteral);
				}
				catch (Exception e) {
					throw new FilterEvaluationException(e.getMessage());
				}
			}
		}
		return transformedLiteral;
	}

	public abstract Object[] getParams();

}
