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
package org.deegree.feature.types;

import static org.deegree.commons.tom.gml.GMLObjectCategory.FEATURE;
import static org.deegree.commons.tom.primitive.BaseType.STRING;
import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.GEOMETRY;
import static org.deegree.feature.types.property.ValueRepresentation.BOTH;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.gml.GMLObjectCategory;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.feature.Feature;
import org.deegree.feature.GenericFeature;
import org.deegree.feature.property.ExtraProps;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.SimplePropertyType;

/**
 * {@link FeatureType} that allows to add property declarations after construction.
 *
 * @author <a href="schneider@lat-lon.de">Markus Schneider</a>
 */
public class DynamicFeatureType implements FeatureType {

	private final QName ftName;

	private final DynamicAppSchema appSchema;

	private final LinkedList<PropertyType> props = new LinkedList<PropertyType>();

	private final Map<QName, PropertyType> propNameToDecl = new HashMap<QName, PropertyType>();

	/**
	 * Creates a new {@link DynamicFeatureType} instance.
	 * @param ftName feature type name, must not be <code>null</code>
	 * @param appSchema corresponding application schema, must not be <code>null</code>
	 */
	public DynamicFeatureType(QName ftName, DynamicAppSchema appSchema) {
		this.ftName = ftName;
		this.appSchema = appSchema;
	}

	/**
	 * Adds a new {@link SimplePropertyType} declaration.
	 * @param pre predecessor property, can be <code>null</code>
	 * @param propName property name, must not be <code>null</code>
	 * @return new (and added) property declaration, never <code>null</code>
	 */
	public SimplePropertyType addSimplePropertyDeclaration(PropertyType pre, QName propName) {
		SimplePropertyType pt = new SimplePropertyType(propName, 0, 1, STRING, null, null);
		props.add(props.indexOf(pre) + 1, pt);
		propNameToDecl.put(propName, pt);
		return pt;
	}

	/**
	 * Adds a new {@link GeometryPropertyType} declaration.
	 * @param pre predecessor property, can be <code>null</code>
	 * @param propName property name, must not be <code>null</code>
	 * @return new (and added) property declaration, never <code>null</code>
	 */
	public GeometryPropertyType addGeometryPropertyDeclaration(PropertyType pre, QName propName) {
		GeometryPropertyType pt = new GeometryPropertyType(propName, 0, 1, null, null, GEOMETRY, DIM_2, BOTH);
		props.add(props.indexOf(pre) + 1, pt);
		propNameToDecl.put(propName, pt);
		return pt;
	}

	/**
	 * Adds a new {@link FeaturePropertyType} declaration.
	 * @param pre predecessor property, can be <code>null</code>
	 * @param propName property name, must not be <code>null</code>
	 * @param valueFt value feature type, must not be <code>null</code>
	 * @return new (and added) property declaration, never <code>null</code>
	 */
	public FeaturePropertyType addFeaturePropertyDeclaration(PropertyType pre, QName propName, FeatureType valueFt) {
		QName valueFtName = valueFt == null ? null : valueFt.getName();
		FeaturePropertyType pt = new FeaturePropertyType(propName, 0, 1, null, null, valueFtName, BOTH);
		props.add(props.indexOf(pre) + 1, pt);
		propNameToDecl.put(propName, pt);
		return pt;
	}

	@Override
	public GMLObjectCategory getCategory() {
		return FEATURE;
	}

	@Override
	public QName getName() {
		return ftName;
	}

	@Override
	public PropertyType getPropertyDeclaration(QName propName) {
		return propNameToDecl.get(propName);
	}

	@Override
	public List<PropertyType> getPropertyDeclarations() {
		return props;
	}

	@Override
	public GeometryPropertyType getDefaultGeometryPropertyDeclaration() {
		GeometryPropertyType geoPt = null;
		for (QName propName : propNameToDecl.keySet()) {
			PropertyType pt = propNameToDecl.get(propName);
			if (pt instanceof GeometryPropertyType) {
				geoPt = (GeometryPropertyType) pt;
				break;
			}
		}
		return geoPt;
	}

	@Override
	public boolean isAbstract() {
		return false;
	}

	@Override
	public Feature newFeatureInstance(String fid, List<Property> props, ExtraProps extraProps) {
		return newFeature(fid, props, extraProps);
	}

	@Override
	public Feature newFeature(String fid, List<Property> props, ExtraProps extraProps) {
		return new GenericFeature(this, fid, props, extraProps);
	}

	@Override
	public AppSchema getSchema() {
		return appSchema;
	}

	@Override
	public String toString() {
		String s = "- Feature type '" + ftName + "'";
		for (PropertyType pt : props) {
			s += "\n" + pt;
		}
		return s;
	}

}
