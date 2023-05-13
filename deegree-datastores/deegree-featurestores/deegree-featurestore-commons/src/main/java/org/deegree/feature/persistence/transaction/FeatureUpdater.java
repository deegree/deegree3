/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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
package org.deegree.feature.persistence.transaction;

import static java.util.Collections.emptyList;
import static org.deegree.protocol.wfs.transaction.action.UpdateAction.REMOVE;
import static org.deegree.protocol.wfs.transaction.action.UpdateAction.REPLACE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.xpath.TypedObjectNodeXPathEvaluator;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.ValueReference;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.primitive.Surface;
import org.deegree.gml.utils.GMLObjectWalker;
import org.deegree.protocol.wfs.transaction.action.ParsedPropertyReplacement;
import org.deegree.protocol.wfs.transaction.action.UpdateAction;

public class FeatureUpdater {

	/**
	 * Updates the given {@link Feature} instance.
	 *
	 * TODO Use a copy of the original feature to avoid modifications on rollback.
	 * Difficult part: Consider updating of references.
	 * @param feature feature to be updated, must not be <code>null</code>
	 * @param replacementProps properties to be replaced, must not be <code>null</code>
	 * @throws FeatureStoreException if the update would result in an invalid feature
	 * instance
	 */
	public void update(final Feature feature, final List<ParsedPropertyReplacement> replacementProps)
			throws FeatureStoreException {
		for (final ParsedPropertyReplacement replacement : replacementProps) {
			update(feature, replacement);
		}
	}

	private void update(final Feature feature, final ParsedPropertyReplacement replacement)
			throws FeatureStoreException {
		final List<Property> targetProps = getTargetProperties(feature, replacement.getValueReference());
		Property prop = replacement.getNewValue();
		UpdateAction updateAction = replacement.getUpdateAction();
		GenericProperty newProp = new GenericProperty(prop.getType(), null);
		if (prop.getValue() != null) {
			newProp.setValue(prop.getValue());
		}
		else if (!prop.getChildren().isEmpty() && prop.getChildren().get(0) != null) {
			newProp.setChildren(prop.getChildren());
		}
		else if (updateAction == null) {
			updateAction = REMOVE;
		}
		if (updateAction == null) {
			updateAction = REPLACE;
		}
		int idx = replacement.getIndex();
		final List<Property> ps = feature.getProperties();
		switch (updateAction) {
			case INSERT_AFTER:
				if (!targetProps.isEmpty()) {
					idx = ps.indexOf(targetProps.get(targetProps.size() - 1));
					ps.add(idx + 1, newProp);
				}
				else {
					// old code path, still needed?
					final ListIterator<Property> iter = ps.listIterator();
					while (iter.hasNext()) {
						if (iter.next().getType().getName().equals(prop.getType().getName())) {
							--idx;
						}
						if (idx < 0) {
							iter.add(newProp);
							break;
						}
					}
				}
				break;
			case INSERT_BEFORE:
				if (!targetProps.isEmpty()) {
					idx = ps.indexOf(targetProps.get(0));
					ps.add(idx, newProp);
				}
				else {
					// old code path, still needed?
					final ListIterator<Property> iter = ps.listIterator();
					while (iter.hasNext()) {
						if (iter.next().getType().getName().equals(prop.getType().getName())) {
							--idx;
						}
						if (idx == 0) {
							iter.add(newProp);
							break;
						}
					}
				}
				break;
			case REMOVE:
				ps.removeAll(targetProps);
				break;
			case REPLACE:
				if (!targetProps.isEmpty()) {
					idx = ps.indexOf(targetProps.get(0));
					ps.removeAll(targetProps);
					ps.add(idx, newProp);
				}
				else {
					// old code path, still needed?
					final ListIterator<Property> iter = ps.listIterator();
					while (iter.hasNext()) {
						if (iter.next().getType().getName().equals(prop.getType().getName())) {
							--idx;
						}
						if (idx < 0) {
							iter.set(newProp);
							break;
						}
					}
				}
				break;
		}
		validateProperties(feature, feature.getProperties());
		checkForDuplicateIds(feature);
	}

	private List<Property> getTargetProperties(final Feature feature, final ValueReference propName)
			throws FeatureStoreException {
		if (propName == null) {
			return emptyList();
		}
		try {
			final TypedObjectNodeXPathEvaluator evaluator = new TypedObjectNodeXPathEvaluator();
			final TypedObjectNode[] nodes = evaluator.eval(feature, propName);
			final List<Property> props = new ArrayList<Property>();
			if (nodes != null) {
				for (final TypedObjectNode node : nodes) {
					if (!(node instanceof Property)) {
						final String msg = propName + " does not refer to a property.";
						throw new FeatureStoreException(msg);
					}
					props.add((Property) node);
				}
			}
			return props;
		}
		catch (FilterEvaluationException e) {
			throw new FeatureStoreException(e.getMessage(), e);
		}
	}

	private void validateProperties(final Feature feature, final List<Property> props) {
		Map<PropertyType, Integer> ptToCount = new HashMap<PropertyType, Integer>();
		for (Property prop : props) {
			if (prop.getValue() instanceof Geometry) {
				Geometry geom = (Geometry) prop.getValue();
				if (geom != null) {
					Property current = feature.getProperties(prop.getType().getName()).get(0);
					Geometry currentGeom = current != null ? ((Geometry) current.getValue()) : null;
					// check compatibility (CRS) for geometry replacements (CITE
					// wfs:wfs-1.1.0-Transaction-tc7.2)
					if (currentGeom != null && currentGeom.getCoordinateDimension() != geom.getCoordinateDimension()) {
						String msg = "Cannot replace given geometry property '" + prop.getType().getName()
								+ "' with given value (wrong dimension).";
						throw new InvalidParameterValueException(msg);
					}
					// check compatibility (geometry type) for geometry replacements (CITE
					// wfs:wfs-1.1.0-Transaction-tc10.1)
					QName qname = new QName("http://cite.opengeospatial.org/gmlsf", "surfaceProperty");
					if (!(geom instanceof Surface) && prop.getType().getName().equals(qname)) {
						String msg = "Cannot replace given geometry property '" + prop.getType().getName()
								+ "' with given value (wrong type).";
						throw new InvalidParameterValueException(msg);
					}
				}
			}

			Integer count = ptToCount.get(prop.getType());
			if (count == null) {
				count = 1;
			}
			else {
				count++;
			}
			ptToCount.put(prop.getType(), count);
		}
		for (PropertyType pt : feature.getType().getPropertyDeclarations()) {
			int count = ptToCount.get(pt) == null ? 0 : ptToCount.get(pt);
			if (count < pt.getMinOccurs()) {
				String msg = "Update would result in invalid feature: property '" + pt.getName()
						+ "' must be present at least " + pt.getMinOccurs() + " time(s).";
				throw new InvalidParameterValueException(msg);
			}
			else if (pt.getMaxOccurs() != -1 && count > pt.getMaxOccurs()) {
				String msg = "Update would result in invalid feature: property '" + pt.getName()
						+ "' must be present no more than " + pt.getMaxOccurs() + " time(s).";
				throw new InvalidParameterValueException(msg);
			}
		}
	}

	private void checkForDuplicateIds(final Feature feature) throws FeatureStoreException {
		final IdChecker idChecker = new IdChecker();
		try {
			new GMLObjectWalker(idChecker).traverse(feature);
		}
		catch (final Exception e) {
			throw new FeatureStoreException(e.getMessage());
		}
	}

}
