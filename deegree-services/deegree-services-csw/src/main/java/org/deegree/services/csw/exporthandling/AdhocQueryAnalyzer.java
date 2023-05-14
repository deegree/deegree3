/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.services.csw.exporthandling;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.uom.Measure;
import org.deegree.filter.Expression;
import org.deegree.filter.Filter;
import org.deegree.filter.Filter.Type;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.ComparisonOperator;
import org.deegree.filter.comparison.PropertyIsBetween;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.comparison.PropertyIsGreaterThan;
import org.deegree.filter.comparison.PropertyIsGreaterThanOrEqualTo;
import org.deegree.filter.comparison.PropertyIsLessThan;
import org.deegree.filter.comparison.PropertyIsLessThanOrEqualTo;
import org.deegree.filter.comparison.PropertyIsLike;
import org.deegree.filter.comparison.PropertyIsNotEqualTo;
import org.deegree.filter.comparison.PropertyIsNull;
import org.deegree.filter.expression.Add;
import org.deegree.filter.expression.Div;
import org.deegree.filter.expression.Function;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.Mul;
import org.deegree.filter.expression.Sub;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.logical.And;
import org.deegree.filter.logical.LogicalOperator;
import org.deegree.filter.logical.Not;
import org.deegree.filter.logical.Or;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.spatial.BBOX;
import org.deegree.filter.spatial.Beyond;
import org.deegree.filter.spatial.Contains;
import org.deegree.filter.spatial.Crosses;
import org.deegree.filter.spatial.DWithin;
import org.deegree.filter.spatial.Disjoint;
import org.deegree.filter.spatial.Equals;
import org.deegree.filter.spatial.Intersects;
import org.deegree.filter.spatial.Overlaps;
import org.deegree.filter.spatial.SpatialOperator;
import org.deegree.filter.spatial.Touches;
import org.deegree.filter.spatial.Within;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.metadata.ebrim.AdhocQuery;
import org.deegree.metadata.ebrim.RegistryObject;
import org.deegree.metadata.persistence.MetadataQuery;
import org.deegree.metadata.persistence.MetadataResultSet;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.protocol.csw.MetadataStoreException;
import org.deegree.services.csw.getrecords.GetRecords;
import org.deegree.services.csw.getrecords.Query;
import org.slf4j.Logger;

/**
 * Analyses a given {@link AdhocQuery} (from a {@link GetRecords} request and derives a
 * {@link MetadataQuery} as well as providing access to the contained {@link Query}.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class AdhocQueryAnalyzer {

	public static final String RIM_NS = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0";

	private static final Logger LOG = getLogger(AdhocQueryAnalyzer.class);

	private final Query getRecordsQuery;

	private final MetadataQuery mdQuery;

	/**
	 * Copies the encapsulated filter of the requested {@link AdhocQuery} replaced with
	 * the values of the request {@link AdhocQuery} and creates a MetadataQuery.
	 * @param adhocQuery the {@link AdhocQuery} out of the request, must not be
	 * <code>null</code>
	 * @param startPosition
	 * @param maxRecords
	 * @throws MetadataStoreException
	 * @throws IllegalArgumentException
	 */
	public AdhocQueryAnalyzer(AdhocQuery adhocQuery, int startPosition, int maxRecords,
			MetadataStore<RegistryObject> queryStore) throws MetadataStoreException {

		AdhocQuery storedAdhocQuery = getStoredQuery(adhocQuery.getId(), queryStore);

		getRecordsQuery = Query.getQuery(storedAdhocQuery.getQueryExpression().getFirstElement());
		Filter storedConstraint = getRecordsQuery.getConstraint();
		if (Type.ID_FILTER.equals(storedConstraint.getType())) {
			String msg = "Id filter are not supported yet!";
			LOG.debug(msg);
			throw new IllegalArgumentException(msg);
		}

		Map<String, String> values = new HashMap<String, String>();
		for (String slotName : storedAdhocQuery.getSlotNames()) {
			String value = adhocQuery.getSlotValue(slotName);
			if (value == null) {
				value = storedAdhocQuery.getSlotValue(slotName);
			}
			values.put(slotName, value);
		}
		OperatorFilter storedOpFilter = (OperatorFilter) storedConstraint;
		Filter filter = new OperatorFilter(copy(storedOpFilter.getOperator(), values));

		SortProperty[] storedSortCriteria = getRecordsQuery.getSortProps();
		SortProperty[] sortCriteria = null;
		if (storedSortCriteria != null) {
			sortCriteria = new SortProperty[storedSortCriteria.length];
			for (int j = 0; j < storedSortCriteria.length; j++) {
				sortCriteria[j] = new SortProperty(copy(storedSortCriteria[j].getSortProperty()),
						storedSortCriteria[j].getSortOrder());
			}
		}
		mdQuery = new MetadataQuery(getRecordsQuery.getQueryTypeNames(), getRecordsQuery.getReturnTypeNames(), filter,
				sortCriteria, startPosition, maxRecords);
	}

	private AdhocQuery getStoredQuery(String id, MetadataStore<RegistryObject> queryStore)
			throws MetadataStoreException {
		MetadataResultSet<RegistryObject> recordById;
		recordById = queryStore.getRecordById(Collections.singletonList(id),
				new QName[] { new QName(RIM_NS, "AdhocQuery", "rim") });
		recordById.next();
		Object storedQuery = recordById.getRecord();
		if (storedQuery == null || !(storedQuery instanceof AdhocQuery)) {
			String msg = "Could not find an stored AdhocQuery with id " + id;
			LOG.debug(msg);
			throw new IllegalArgumentException(msg);
		}
		return (AdhocQuery) storedQuery;
	}

	/**
	 * Returns the {@link Query} contained in the stored {@link AdhocQuery} (provides the
	 * requested element names).
	 * @return query, never <code>null</code>
	 */
	Query getGetRecordsQuery() {
		return getRecordsQuery;
	}

	/**
	 * Returns the {@link MetadataQuery} to perform against the {@link MetadataStore} for
	 * fetching the matching records
	 * @return metadata query, never <code>null</code>
	 */
	MetadataQuery getMetadataQuery() {
		return mdQuery;
	}

	private Operator copy(Operator op, Map<String, String> values) {
		switch (op.getType()) {
			case COMPARISON:
				switch (((ComparisonOperator) op).getSubType()) {
					case PROPERTY_IS_BETWEEN:
						PropertyIsBetween piw = (PropertyIsBetween) op;
						return new PropertyIsBetween(copyExpression(piw.getExpression(), values),
								copyExpression(piw.getLowerBoundary(), values),
								copyExpression(piw.getUpperBoundary(), values), piw.isMatchCase(),
								piw.getMatchAction());
					case PROPERTY_IS_EQUAL_TO:
						PropertyIsEqualTo pie = (PropertyIsEqualTo) op;
						return new PropertyIsEqualTo(copyExpression(pie.getParameter1(), values),
								copyExpression(pie.getParameter2(), values), pie.isMatchCase(), pie.getMatchAction());
					case PROPERTY_IS_GREATER_THAN:
						PropertyIsGreaterThan pigt = (PropertyIsGreaterThan) op;
						return new PropertyIsGreaterThan(copyExpression(pigt.getParameter1(), values),
								copyExpression(pigt.getParameter2(), values), pigt.isMatchCase(),
								pigt.getMatchAction());
					case PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO:
						PropertyIsGreaterThanOrEqualTo pigte = (PropertyIsGreaterThanOrEqualTo) op;
						return new PropertyIsGreaterThanOrEqualTo(copyExpression(pigte.getParameter1(), values),
								copyExpression(pigte.getParameter2(), values), pigte.isMatchCase(),
								pigte.getMatchAction());
					case PROPERTY_IS_LESS_THAN:
						PropertyIsLessThan pilt = (PropertyIsLessThan) op;
						return new PropertyIsLessThan(copyExpression(pilt.getParameter1(), values),
								copyExpression(pilt.getParameter2(), values), pilt.isMatchCase(),
								pilt.getMatchAction());
					case PROPERTY_IS_LESS_THAN_OR_EQUAL_TO:
						PropertyIsLessThanOrEqualTo pilte = (PropertyIsLessThanOrEqualTo) op;
						return new PropertyIsLessThanOrEqualTo(copyExpression(pilte.getParameter1(), values),
								copyExpression(pilte.getParameter2(), values), pilte.isMatchCase(),
								pilte.getMatchAction());
					case PROPERTY_IS_LIKE:
						PropertyIsLike pil = (PropertyIsLike) op;
						return new PropertyIsLike(copy(pil.getExpression()), copyExpression(pil.getPattern(), values),
								pil.getWildCard(), pil.getSingleChar(), pil.getEscapeChar(), pil.isMatchCase(),
								pil.getMatchAction());
					case PROPERTY_IS_NOT_EQUAL_TO:
						PropertyIsNotEqualTo pine = (PropertyIsNotEqualTo) op;
						return new PropertyIsNotEqualTo(copyExpression(pine.getParameter1(), values),
								copyExpression(pine.getParameter2(), values), pine.isMatchCase(),
								pine.getMatchAction());
					case PROPERTY_IS_NULL:
						PropertyIsNull pin = (PropertyIsNull) op;
						return new PropertyIsNull(copy(pin.getPropertyName()), pin.getMatchAction());
				}
				break;
			case LOGICAL:
				switch (((LogicalOperator) op).getSubType()) {
					case AND:
						And and = (And) op;
						Operator[] andParams = and.getParams();
						int i = 0;
						for (Operator param : andParams) {
							andParams[i++] = copy(param, values);
						}
						return new And(andParams);
					case OR:
						Or or = (Or) op;
						Operator[] orParams = or.getParams();
						int j = 0;
						for (Operator param : orParams) {
							orParams[j++] = copy(param, values);
						}
						return new Or(orParams);
					case NOT:
						return new Not(copy(((Not) op).getParameter(), values));
				}
				break;
			case SPATIAL:
				GeometryFactory gf = new GeometryFactory();
				switch (((SpatialOperator) op).getSubType()) {
					case BBOX:
						BBOX bbox = (BBOX) op;
						if (bbox.getValueReference() != null)
							return new BBOX(copy(bbox.getParam1()), bbox.getValueReference());
						Envelope env = bbox.getBoundingBox();
						Envelope newEnv = gf.createEnvelope(env.getMin().get0(), env.getMin().get1(),
								env.getMax().get0(), env.getMax().get0(), env.getCoordinateSystem());
						return new BBOX(copy(bbox.getParam1()), newEnv);
					case BEYOND:
						Beyond beyond = (Beyond) op;
						if (beyond.getValueReference() != null)
							return new Beyond(copy(beyond.getParam1()), beyond.getValueReference(),
									copy(beyond.getDistance()));
						return new Beyond(copy(beyond.getParam1()), beyond.getGeometry(), copy(beyond.getDistance()));
					case CONTAINS:
						Contains contains = (Contains) op;
						if (contains.getValueReference() != null)
							return new Contains(copy(contains.getParam1()), contains.getValueReference());
						return new Contains(copy(contains.getParam1()), contains.getGeometry());
					case CROSSES:
						Crosses crosses = (Crosses) op;
						if (crosses.getValueReference() != null)
							return new Crosses(copy(crosses.getParam1()), crosses.getValueReference());
						return new Crosses(copy(crosses.getParam1()), crosses.getGeometry());
					case DISJOINT:
						Disjoint disjoint = (Disjoint) op;
						if (disjoint.getValueReference() != null)
							return new Disjoint(copy(disjoint.getParam1()), disjoint.getValueReference());
						return new Disjoint(copy(disjoint.getParam1()), disjoint.getGeometry());
					case DWITHIN:
						DWithin dwithin = (DWithin) op;
						if (dwithin.getValueReference() != null)
							return new DWithin(copy(dwithin.getParam1()), dwithin.getValueReference(),
									copy(dwithin.getDistance()));
						return new DWithin(copy(dwithin.getParam1()), dwithin.getGeometry(),
								copy(dwithin.getDistance()));
					case EQUALS:
						Equals equals = (Equals) op;
						if (equals.getValueReference() != null)
							return new Equals(copy(equals.getParam1()), equals.getValueReference());
						return new Equals(copy(equals.getParam1()), equals.getGeometry());
					case INTERSECTS:
						Intersects intersects = (Intersects) op;
						if (intersects.getValueReference() != null)
							return new Intersects(copy(intersects.getParam1()), intersects.getValueReference());
						return new Intersects(copy(intersects.getParam1()), intersects.getGeometry());
					case OVERLAPS:
						Overlaps overlaps = (Overlaps) op;
						if (overlaps.getValueReference() != null)
							return new Overlaps(copy(overlaps.getParam1()), overlaps.getValueReference());
						return new Overlaps(copy(overlaps.getParam1()), overlaps.getGeometry());
					case TOUCHES:
						Touches touches = (Touches) op;
						if (touches.getValueReference() != null)
							return new Touches(copy(touches.getParam1()), touches.getValueReference());
						return new Touches(copy(touches.getParam1()), touches.getGeometry());
					case WITHIN:
						Within within = (Within) op;
						if (within.getValueReference() != null)
							return new Within(copy(within.getParam1()), within.getValueReference());
						return new Within(copy(within.getParam1()), within.getGeometry());
				}
				break;
		}
		return null;
	}

	private Literal<?> copy(Literal<?> literal, Map<String, String> values) {
		Object oldValue = literal.getValue();
		if (oldValue instanceof PrimitiveValue) {
			PrimitiveValue pv = (PrimitiveValue) oldValue;
			String text = pv.getAsText();
			if (pv.getType().getBaseType().equals(BaseType.STRING) && text.startsWith("$")) {
				String newValue = values.get(text.substring(1));
				return new Literal<PrimitiveValue>(new PrimitiveValue(newValue, new PrimitiveType(BaseType.STRING)),
						null);
			}
		}
		return new Literal<TypedObjectNode>(literal.getValue(), literal.getTypeName());
	}

	private Expression copyExpression(Expression expr, Map<String, String> values) {
		Expression newExpr = null;
		switch (expr.getType()) {
			case ADD:
				Add add = (Add) expr;
				return new Add(copyExpression(add.getParameter1(), values),
						copyExpression(add.getParameter2(), values));
			case DIV:
				Div div = (Div) expr;
				return new Div(copyExpression(div.getParameter1(), values),
						copyExpression(div.getParameter2(), values));
			case CUSTOM:
				// TODO
				break;
			case FUNCTION:
				Function fct = (Function) expr;
				List<Expression> params = new ArrayList<Expression>(fct.getParameters().size());
				for (Expression expression : fct.getParameters()) {
					params.add(expression);
				}
				return new Function(fct.getName(), params);
			case LITERAL:
				return copy((Literal<?>) expr, values);
			case MUL:
				Mul mul = (Mul) expr;
				return new Mul(copyExpression(mul.getParameter1(), values),
						copyExpression(mul.getParameter2(), values));
			case VALUE_REFERENCE:
				return copy((ValueReference) expr);
			case SUB:
				Sub sub = (Sub) expr;
				return new Sub(copyExpression(sub.getParameter1(), values),
						copyExpression(sub.getParameter2(), values));
		}
		return newExpr;
	}

	private ValueReference copy(Expression e) {
		ValueReference pn = (ValueReference) e;
		return new ValueReference(pn.getAsText(), pn.getNsContext());
	}

	private Measure copy(Measure distance) {
		return new Measure(distance.getValue(), distance.getUomUri());
	}

}