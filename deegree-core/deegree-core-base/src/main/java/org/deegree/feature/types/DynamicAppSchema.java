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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.gml.GMLObjectCategory;
import org.deegree.commons.tom.gml.GMLObjectType;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.gml.schema.GMLSchemaInfoSet;

/**
 * {@link AppSchema} that allows to add {@link FeatureTypes} during runtime.
 *
 * @author <a href="schneider@lat-lon.de">Markus Schneider</a>
 */
public class DynamicAppSchema implements AppSchema {

	private final LinkedHashMap<QName, DynamicFeatureType> ftNameToFt = new LinkedHashMap<QName, DynamicFeatureType>();

	private final Set<String> namespaces = new HashSet<String>();

	// key: namespace prefix, value: namespace URI
	private final Map<String, String> prefixToNs = new HashMap<String, String>();

	/**
	 * Adds a new {@link DynamicFeatureType} for the given feature type name.
	 * @param ftName feature type name, must not be <code>null</code>
	 * @return new (and added) feature type instance, never <code>null</code>
	 */
	public DynamicFeatureType addFeatureType(QName ftName) {
		DynamicFeatureType ft = new DynamicFeatureType(ftName, this);
		ftNameToFt.put(ftName, ft);
		namespaces.add(ftName.getNamespaceURI());
		prefixToNs.put(ftName.getPrefix(), ftName.getNamespaceURI());
		return ft;
	}

	@Override
	public FeatureType[] getFeatureTypes() {
		return ftNameToFt.values().toArray(new FeatureType[ftNameToFt.size()]);
	}

	@Override
	public List<FeatureType> getFeatureTypes(String namespace, boolean includeCollections, boolean includeAbstracts) {
		return new ArrayList<FeatureType>(ftNameToFt.values());
	}

	@Override
	public FeatureType[] getRootFeatureTypes() {
		return getFeatureTypes();
	}

	@Override
	public DynamicFeatureType getFeatureType(QName ftName) {
		return ftNameToFt.get(ftName);
	}

	@Override
	public FeatureType[] getDirectSubtypes(FeatureType ft) {
		return new FeatureType[0];
	}

	@Override
	public FeatureType getParent(FeatureType ft) {
		return null;
	}

	@Override
	public FeatureType[] getSubtypes(FeatureType ft) {
		return new FeatureType[0];
	}

	@Override
	public FeatureType[] getConcreteSubtypes(FeatureType ft) {
		return new FeatureType[0];
	}

	@Override
	public GMLSchemaInfoSet getGMLSchema() {
		return null;
	}

	@Override
	public boolean isSubType(FeatureType ft, FeatureType substitution) {
		return ft == null || ft == substitution;
	}

	@Override
	public List<PropertyType> getNewPropertyDecls(FeatureType ft) {
		return ft.getPropertyDeclarations();
	}

	@Override
	public Map<FeatureType, FeatureType> getFtToSuperFt() {
		return emptyMap();
	}

	@Override
	public Map<String, String> getNamespaceBindings() {
		return prefixToNs;
	}

	@Override
	public Set<String> getAppNamespaces() {
		return namespaces;
	}

	@Override
	public List<String> getNamespacesDependencies(String ns) {
		return emptyList();
	}

	@Override
	public List<GMLObjectType> getGeometryTypes() {
		return Collections.emptyList();
	}

	@Override
	public GMLObjectType getGeometryType(QName name) {
		return null;
	}

	@Override
	public List<GMLObjectType> getSubstitutions(QName name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GMLObjectType> getDirectSubstitutions(QName name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AppSchemaGeometryHierarchy getGeometryHierarchy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<GMLObjectType, GMLObjectType> getGeometryToSuperType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GMLObjectType getGmlObjectType(QName name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GMLObjectType> getGmlObjectTypes(GMLObjectCategory timeObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GMLObjectType> getGmlObjectTypes() {
		// TODO Auto-generated method stub
		return null;
	}

}
