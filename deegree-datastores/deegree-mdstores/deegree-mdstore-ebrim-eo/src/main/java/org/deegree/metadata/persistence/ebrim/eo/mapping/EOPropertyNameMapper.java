/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.metadata.persistence.ebrim.eo.mapping;

import static org.deegree.commons.tom.primitive.BaseType.DATE_TIME;
import static org.deegree.commons.tom.primitive.BaseType.DOUBLE;
import static org.deegree.commons.tom.primitive.BaseType.INTEGER;
import static org.deegree.commons.tom.primitive.BaseType.STRING;
import static org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapping.SlotType._string;
import static org.jaxen.saxpath.Axis.ATTRIBUTE;
import static org.jaxen.saxpath.Axis.CHILD;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.sql.DefaultPrimitiveConverter;
import org.deegree.commons.tom.sql.PrimitiveParticleConverter;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.ValueReference;
import org.deegree.geometry.utils.GeometryParticleConverter;
import org.deegree.metadata.ebrim.AliasedRIMType;
import org.deegree.metadata.ebrim.RIMType;
import org.deegree.metadata.persistence.ebrim.eo.EbrimEOMDStore;
import org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapper.Table;
import org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapping.SlotType;
import org.deegree.protocol.csw.MetadataStoreException;
import org.deegree.sqldialect.filter.Join;
import org.deegree.sqldialect.filter.PropertyNameMapper;
import org.deegree.sqldialect.filter.PropertyNameMapping;
import org.deegree.sqldialect.filter.TableAliasManager;
import org.deegree.sqldialect.filter.UnmappableException;
import org.deegree.sqldialect.postgis.PostGISGeometryConverter;
import org.jaxen.expr.EqualityExpr;
import org.jaxen.expr.Expr;
import org.jaxen.expr.LiteralExpr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.NameStep;
import org.jaxen.expr.PathExpr;
import org.jaxen.expr.Predicate;
import org.jaxen.expr.VariableReferenceExpr;
import org.slf4j.Logger;

/**
 * {@link PropertyNameMapper} for the {@link EbrimEOMDStore}.
 *
 * @author <a href="mailto:goltz@deegree.org">Lyn Goltz</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class EOPropertyNameMapper implements PropertyNameMapper {

	private static final Logger LOG = getLogger(EOPropertyNameMapper.class);

	private static final String RIM_NS = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0";

	private static final ICRS STORAGE_CRS = CRSManager.getCRSRef("EPSG:4326");

	private static final String STORAGE_SRID = "-1";

	// private static final String WRS_NS = "http://www.opengis.net/cat/wrs/1.0";

	private int aliasNo = 1;

	// aliases and queried types, in order
	private final LinkedHashMap<String, AliasedRIMType> aliasToType = new LinkedHashMap<String, AliasedRIMType>();

	private final LinkedHashMap<String, String> aliasToTableAlias = new LinkedHashMap<String, String>();

	private final LinkedHashMap<String, Table> aliasToTable = new LinkedHashMap<String, Table>();

	private final Map<ValueReference, PropertyNameMapping> propNameToMapping = new HashMap<ValueReference, PropertyNameMapping>();

	private final Map<ValueReference, SlotType> propNameToDataType = new HashMap<ValueReference, SlotType>();

	private final List<Join> additionalJoins = new ArrayList<Join>();

	private boolean useLegacyPredicates;

	/**
	 * Creates a new {@link EOPropertyNameMapper} instance configured for the specified
	 * registry objects / aliases.
	 * @param queryTypeNames queried type names, must not be <code>null</code>
	 * @param useLegacyPredicates if true, legacy-style PostGIS spatial predicates are
	 * used (e.g. <code>Intersects</code> instead of <code>ST_Intersects</code>)
	 * @throws MetadataStoreException if any of the queried type names is not known /
	 * supported
	 */
	public EOPropertyNameMapper(QName[] queryTypeNames, boolean useLegacyPredicates) throws MetadataStoreException {
		for (QName queryTypeName : queryTypeNames) {
			try {
				List<AliasedRIMType> aliasedTypes = AliasedRIMType.valueOf(queryTypeName);
				for (AliasedRIMType aliasedType : aliasedTypes) {
					if (aliasToType.keySet().contains(aliasedType.getAlias())) {
						String msg = "Each queried type must have a unique alias. However, alias '"
								+ aliasedType.getAlias() + "' is used multiple times in the query.";
						throw new MetadataStoreException(msg);
					}
					aliasToType.put(aliasedType.getAlias(), aliasedType);
				}
			}
			catch (IllegalArgumentException e) {
				String name = queryTypeName.getLocalPart();
				if (queryTypeName.getPrefix() != null && !queryTypeName.getPrefix().isEmpty()) {
					name = queryTypeName.getPrefix() + ":" + queryTypeName.getLocalPart();
				}
				String msg = "Queried type name '" + name + "' does not refer to a known ebRIM registry object.";
				throw new MetadataStoreException(msg);
			}
		}
		if (aliasToType.isEmpty()) {
			String msg = "No query type names specified.";
			throw new MetadataStoreException(msg);
		}

		for (AliasedRIMType queriedType : aliasToType.values()) {
			Table table = SlotMapper.getTable(queriedType.getType());
			if (table == null) {
				String msg = "Unsupported query type name " + queriedType + ".";
				throw new MetadataStoreException(msg);
			}
			String tableAlias = "X" + (aliasNo++);
			LOG.debug("Query type: " + queriedType + ", table: " + table + ", table alias: " + tableAlias);
			aliasToTableAlias.put(queriedType.getAlias(), tableAlias);
			aliasToTable.put(queriedType.getAlias(), table);
		}
	}

	/**
	 * Returns the queried types (including aliases).
	 * @return queried types, never <code>null</code>
	 */
	public Collection<AliasedRIMType> getQueryTypes() {
		return aliasToType.values();
	}

	/**
	 * Determines the query type to be returned.
	 * @param returnTypeNames return type names as specified in the query
	 * @return query type to be returned, never <code>null</code>
	 * @throws MetadataStoreException if the specified return type names do not correspond
	 * to the query types
	 */
	public AliasedRIMType getReturnType(QName[] returnTypeNames) throws MetadataStoreException {
		AliasedRIMType rt = null;
		if (returnTypeNames.length == 0) {
			if (aliasToType.size() != 1) {
				String msg = "Query is ambigous. Multiple query type names specified, but no return type selected.";
				throw new MetadataStoreException(msg);
			}
		}
		if (returnTypeNames.length == 1) {
			List<AliasedRIMType> returnTypes = AliasedRIMType.valueOf(returnTypeNames[0]);
			if (returnTypes.size() != 1) {
				String msg = "Selecting of multiple return types is not supported.";
				throw new MetadataStoreException(msg);
			}
			AliasedRIMType returnType = returnTypes.get(0);
			rt = aliasToType.get(returnType.getAlias());
			if (rt == null) {
				for (AliasedRIMType candidate : aliasToType.values()) {
					if (candidate.getType() == returnType.getType()) {
						LOG.warn("Relying on imprecise match for return type.");
						rt = candidate;
						break;
					}
				}
			}
			if (rt == null) {
				String msg = "Specified return type '" + returnType + "' is not among the list of queried types.";
				throw new MetadataStoreException(msg);
			}
		}
		else {
			String msg = "Selecting of multiple return types is not supported.";
			throw new MetadataStoreException(msg);
		}
		return rt;
	}

	public Table getTable(AliasedRIMType queryType) {
		return aliasToTable.get(queryType.getAlias());
	}

	public String getTableAlias(AliasedRIMType queryType) {
		return aliasToTableAlias.get(queryType.getAlias());
	}

	public List<Join> getAdditionalJoins() {
		return additionalJoins;
	}

	@Override
	public PropertyNameMapping getMapping(ValueReference propName, TableAliasManager aliasManager)
			throws FilterEvaluationException, UnmappableException {

		if (propNameToMapping.containsKey(propName)) {
			return propNameToMapping.get(propName);
		}

		Expr expr = propName.getAsXPath();
		LocationPath lpath = null;
		int firstStepIdx = 0;
		if (expr instanceof LocationPath) {
			firstStepIdx = 1;
			lpath = (LocationPath) expr;
		}
		else if (expr instanceof PathExpr) {
			lpath = ((PathExpr) expr).getLocationPath();
		}
		else {
			String msg = "Unable to map PropertyName '" + propName.getAsText()
					+ "': Not a valid location path expression.";
			throw new UnmappableException(msg);
		}

		List<?> steps = ((LocationPath) lpath).getSteps();
		if (steps.isEmpty()) {
			String msg = "Unable to map PropertyName '" + propName.getAsText() + "': empty location path.";
			throw new UnmappableException(msg);
		}

		int stepNo = 1;
		// check that every step in the location path is a name step on the child or
		// attribute axis (and nothing else)
		for (Object o : steps) {
			if (!(o instanceof NameStep)) {
				String msg = "Unable to map PropertyName '" + propName.getAsText() + "': step " + stepNo + " (" + o
						+ ") is not a name step.";
				throw new UnmappableException(msg);
			}
			NameStep step = (NameStep) o;
			if (step.getAxis() != ATTRIBUTE && step.getAxis() != CHILD) {
				String msg = "Unable to map PropertyName '" + propName.getAsText() + "': step " + stepNo + " (" + o
						+ ") is not a step on the child or attribute axis.";
				throw new UnmappableException(msg);
			}
			stepNo++;
		}

		AliasedRIMType type = null;
		if (expr instanceof LocationPath) {
			NameStep firstStep = (NameStep) steps.get(0);
			if ("*".equals(firstStep.getLocalName())) {
				String msg = "Unable to map PropertyName '" + propName.getAsText() + "'. First step must not be '*'.";
				throw new UnmappableException(msg);
			}
			if (!firstStep.getPredicates().isEmpty()) {
				String msg = "Unable to map PropertyName '" + propName.getAsText()
						+ "'. Predicates are not allowed for first step (" + firstStep + ").";
				throw new UnmappableException(msg);
			}
			type = aliasToType.get(firstStep.getLocalName());
		}
		else {
			Expr filterExpr = ((PathExpr) expr).getFilterExpr();
			if (filterExpr instanceof VariableReferenceExpr) {
				type = aliasToType.get(((VariableReferenceExpr) filterExpr).getVariableName());
			}
		}

		if (type == null) {
			String msg = "Unable to map PropertyName '" + propName.getAsText()
					+ "'. First step must refer to a query type name or alias.";
			throw new UnmappableException(msg);
		}

		switch (type.getType()) {
			case Association:
			case Classification:
			case ClassificationNode:
			case RegistryPackage:
			case ExtrinsicObject: {
				// nothing to do
				break;
			}
			default: {
				String msg = "Unable to map PropertyName '" + propName.getAsText()
						+ "'. Filter predicates on registry object type '" + type.getType().name()
						+ "' are not supported.";
				throw new UnmappableException(msg);
			}
		}

		List<NameStep> remainingSteps = new ArrayList<NameStep>(steps.size() - 1);
		for (int i = firstStepIdx; i < steps.size(); i++) {
			remainingSteps.add((NameStep) steps.get(i));
		}
		addMapping(propName, type, remainingSteps, null);
		return propNameToMapping.get(propName);
	}

	private void addMapping(ValueReference propName, AliasedRIMType type, List<NameStep> remainingSteps,
			List<Join> joins) throws UnmappableException {

		if (remainingSteps.isEmpty()) {
			throw new UnmappableException(
					"Invalid property name expression '" + propName + "'. Must not end on type name element.");
		}
		NameStep step = remainingSteps.get(0);

		// common attributes / elements
		if (isAttrStep(step, new QName("id"))) {
			addMapping(propName, joins, type, "id", _string);
		}
		else if (isAttrStep(step, new QName("objectType"))) {
			addMapping(propName, joins, type, "objectType", _string);
		}
		else if (isAttrStep(step, new QName("status"))) {
			addMapping(propName, joins, type, "status", _string);
		}
		else if (isElementStep(step, new QName(RIM_NS, "Name"))) {
			// TODO more checks for actually selected element
			addMapping(propName, joins, type, "name", _string);
		}
		else if (isElementStep(step, new QName(RIM_NS, "Description"))) {
			// TODO more checks for actually selected element
			addMapping(propName, joins, type, "description", _string);
		}
		else if (isElementStep(step, new QName(RIM_NS, "ExternalIdentifier"))) {
			// TODO more checks for actually selected element
			addMapping(propName, joins, type, "externalId", _string);
		}
		else {
			switch (type.getType()) {
				case Association:
					if (isAttrStep(step, new QName("sourceObject"))) {
						addMapping(propName, joins, type, "sourceObject", _string);
					}
					else if (isAttrStep(step, new QName("targetObject"))) {
						addMapping(propName, joins, type, "targetObject", _string);
					}
					else if (isAttrStep(step, new QName("associationType"))) {
						addMapping(propName, joins, type, "associationType", _string);
					}
					else {
						String msg = "No mapping for PropertyName '" + propName.getAsText() + "' available.";
						throw new UnmappableException(msg);
					}
					break;
				case Classification:
					if (isAttrStep(step, new QName("classificationScheme"))) {
						addMapping(propName, joins, type, "classificationScheme", _string);
					}
					else if (isAttrStep(step, new QName("classificationNode"))) {
						addMapping(propName, joins, type, "classificationNode", _string);
					}
					else if (isAttrStep(step, new QName("classifiedObject"))) {
						addMapping(propName, joins, type, "classifiedObject", _string);
					}
					else {
						String msg = "No mapping for PropertyName '" + propName.getAsText() + "' available.";
						throw new UnmappableException(msg);
					}
					break;
				case ClassificationNode: {
					if (isAttrStep(step, new QName("parent"))) {
						addMapping(propName, joins, type, "parent", _string);
					}
					else if (isAttrStep(step, new QName("code"))) {
						addMapping(propName, joins, type, "code", _string);
					}
					else if (isAttrStep(step, new QName("path"))) {
						addMapping(propName, joins, type, "path", _string);
					}
					else {
						String msg = "No mapping for PropertyName '" + propName.getAsText() + "' available.";
						throw new UnmappableException(msg);
					}
					break;
				}
				case RegistryPackage: {
					if (isElementStep(step, new QName(RIM_NS, "RegistryObjectList"))) {
						if (remainingSteps.size() < 2) {
							String msg = "Invalid property name expression '" + propName
									+ "'. Cannot target RegistryObjectList (but a node above or below).";
							throw new UnmappableException(msg);
						}
						NameStep next = remainingSteps.get(1);
						if (next.getAxis() != CHILD) {
							String msg = "Invalid property name expression '" + propName
									+ "'. Cannot target RegistryObjectList attributes.";
							throw new UnmappableException(msg);
						}
						String childTypeName = next.getLocalName();
						RIMType childType = RIMType.valueOf(childTypeName);
						switch (childType) {
							case Association:
							case Classification:
							case ClassificationNode:
							case ExtrinsicObject: {
								// nothing to do
								break;
							}
							case RegistryObject: {
								LOG.debug(
										"Assuming 'RegistryObject' step refers to 'ExtrinsicObject'. This works for ESA requests, but may be not enough.");
								childType = RIMType.ExtrinsicObject;
								break;
							}
							default: {
								String msg = "Unable to map PropertyName '" + propName.getAsText()
										+ "'. Filtering based on '" + childType.name()
										+ "' children of RegistryObjectList elements is not supported.";
								throw new UnmappableException(msg);
							}
						}

						String fromTable = getTable(type).name();
						String fromAlias = getTableAlias(type);
						String toTable = SlotMapper.getTable(childType).name();
						String toAlias = "X" + aliasNo++;
						Join join = new Join(fromTable, fromAlias, "internalId", toTable, toAlias,
								"fk_registrypackage");
						joins = Collections.singletonList(join);
						additionalJoins.add(join);
						addMapping(propName, null, remainingSteps.subList(2, remainingSteps.size()), joins);
					}
					break;
				}
				case ExtrinsicObject: {
					if (isElementStep(step, new QName(RIM_NS, "Slot"))) {
						SlotMapping slot = getSlot(propName, step);
						// TODO more checks for actually targeted node
						addMapping(propName, joins, type, slot.getColumn(), slot.getType());
					}
					else {
						String msg = "No mapping for PropertyName '" + propName.getAsText() + "' available.";
						throw new UnmappableException(msg);
					}
					break;
				}
				default: {
					String msg = "Unable to map PropertyName '" + propName.getAsText()
							+ "'. Filter predicates on registry object type '" + type.getType().name()
							+ "' are not supported.";
					throw new UnmappableException(msg);
				}
			}
		}
	}

	private SlotMapping getSlot(ValueReference propName, NameStep slotStep) throws UnmappableException {
		List<?> predicates = slotStep.getPredicates();
		if (predicates == null || predicates.isEmpty()) {
			String msg = "Unable to map PropertyName '" + propName.getAsText()
					+ "'. Slot steps must specify a single name predicate (../rim:Slot[@name=...]).";
			throw new UnmappableException(msg);
		}
		if (predicates.size() != 1) {
			String msg = "Unable to map PropertyName '" + propName.getAsText()
					+ "'. Slot steps must specify a single name predicate (../rim:Slot[@name=...]).";
			throw new UnmappableException(msg);
		}
		Expr expr = ((Predicate) predicates.get(0)).getExpr();
		if (expr == null || (!(expr instanceof EqualityExpr)) || !((EqualityExpr) expr).getOperator().equals("=")) {
			String msg = "Unable to map PropertyName '" + propName.getAsText()
					+ "'. Slot steps must specify a single name predicate (../rim:Slot[@name=...]).";
			throw new UnmappableException(msg);
		}
		Expr lhs = ((EqualityExpr) expr).getLHS();
		if (!(lhs instanceof LocationPath) || ((LocationPath) lhs).getSteps().size() != 1
				|| !(((LocationPath) lhs).getSteps().get(0) instanceof NameStep)
				|| ((NameStep) ((LocationPath) lhs).getSteps().get(0)).getAxis() != ATTRIBUTE
				|| !((NameStep) ((LocationPath) lhs).getSteps().get(0)).getLocalName().equals("name")) {
			String msg = "Unable to map PropertyName '" + propName.getAsText()
					+ "'. Slot steps must specify a single name predicate (../rim:Slot[@name=...]).";
			throw new UnmappableException(msg);
		}
		Expr rhs = ((EqualityExpr) expr).getRHS();
		if (!(rhs instanceof LiteralExpr)) {
			String msg = "Unable to map PropertyName '" + propName.getAsText()
					+ "'. Slot steps must specify a single name predicate (../rim:Slot[@name=...]).";
			throw new UnmappableException(msg);
		}
		String slotName = rhs.getText().substring(1, rhs.getText().length() - 1);
		SlotMapping slot = SlotMapper.getSlot(slotName);
		if (slot == null) {
			String msg = "Unable to map PropertyName '" + propName.getAsText() + "'. No mapping for slot name '"
					+ slotName + "' defined.";
			throw new UnmappableException(msg);
		}
		return slot;
	}

	private void addMapping(ValueReference propName, List<Join> joins, AliasedRIMType type, String column,
			SlotType dataType) {
		String table = getTableAlias(joins, type);
		PropertyNameMapping propMapping = null;
		if (dataType == SlotType._geom) {
			GeometryParticleConverter converter = new PostGISGeometryConverter(column, STORAGE_CRS, STORAGE_SRID,
					useLegacyPredicates);
			propMapping = new PropertyNameMapping(converter, joins, column, table);
		}
		else {
			BaseType bt = null;
			boolean isConcatenated = false;
			switch (dataType) {
				case _date: {
					bt = DATE_TIME;
					break;
				}
				case _double: {
					bt = DOUBLE;
					break;
				}
				case _int: {
					bt = INTEGER;
					break;
				}
				case _multiple: {
					isConcatenated = true;
					bt = STRING;
					break;
				}
				case _string: {
					bt = STRING;
					break;
				}
			}
			PrimitiveParticleConverter converter = new DefaultPrimitiveConverter(new PrimitiveType(bt), column,
					isConcatenated);
			propMapping = new PropertyNameMapping(converter, joins, column, table);
		}

		propNameToMapping.put(propName, propMapping);
		propNameToDataType.put(propName, dataType);
	}

	private String getTableAlias(List<Join> joins, AliasedRIMType type) {
		if (joins != null) {
			return joins.get(joins.size() - 1).getToTableAlias();
		}
		return getTableAlias(type);
	}

	private boolean isAttrStep(NameStep step, QName attrName) {
		if (step.getAxis() != ATTRIBUTE) {
			return false;
		}
		// TODO namespace awareness
		return attrName.getLocalPart().equals(step.getLocalName());
	}

	private boolean isElementStep(NameStep step, QName elName) {
		if (step.getAxis() != CHILD) {
			return false;
		}
		// TODO namespace awareness
		return elName.getLocalPart().equals(step.getLocalName());
	}

	@Override
	public PropertyNameMapping getSpatialMapping(ValueReference propName, TableAliasManager aliasManager)
			throws FilterEvaluationException, UnmappableException {
		return getMapping(propName, aliasManager);
	}

}
