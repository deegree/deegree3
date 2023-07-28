package org.deegree.commons.tom.gml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.gml.property.PropertyType;

/**
 * Generic implementation of {@link GMLObjectType}, can be used for representing arbitrary
 * object type declarations.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider </a>
 */
public class GenericGMLObjectType implements GMLObjectType {

	private final GMLObjectCategory category;

	private final QName name;

	private final Map<QName, PropertyType> propNameToDecl = new LinkedHashMap<QName, PropertyType>();

	private final boolean isAbstract;

	/**
	 * Creates a new {@link GenericGMLObjectType} instance.
	 * @param category GML object category, must not be <code>null</code>
	 * @param name qualified element name, must not be <code>null</code>
	 * @param propDecls property declarations, may be empty or <code>null</code>
	 * @param isAbstract <code>true</code>, if this object type declaration is abstract,
	 * <code>false</code> otherwise
	 */
	public GenericGMLObjectType(GMLObjectCategory category, QName name, List<PropertyType> propDecls,
			boolean isAbstract) {
		this.category = category;
		this.name = name;
		for (PropertyType propDecl : propDecls) {
			propNameToDecl.put(propDecl.getName(), propDecl);
		}
		this.isAbstract = isAbstract;
	}

	@Override
	public GMLObjectCategory getCategory() {
		return category;
	}

	@Override
	public QName getName() {
		return name;
	}

	@Override
	public PropertyType getPropertyDeclaration(QName propName) {
		return propNameToDecl.get(propName);
	}

	@Override
	public List<PropertyType> getPropertyDeclarations() {
		List<PropertyType> propDecls = new ArrayList<PropertyType>(propNameToDecl.size());
		for (QName propName : propNameToDecl.keySet()) {
			propDecls.add(propNameToDecl.get(propName));
		}
		return propDecls;
	}

	@Override
	public boolean isAbstract() {
		return isAbstract;
	}

	@Override
	public String toString() {
		String s = "- GML object type '" + name + "', abstract: " + isAbstract;
		for (QName ptName : propNameToDecl.keySet()) {
			PropertyType pt = propNameToDecl.get(ptName);
			s += "\n" + pt;
		}
		return s;
	}

}
