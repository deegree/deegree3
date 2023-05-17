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
package org.deegree.feature.property;

import static java.lang.Boolean.TRUE;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xerces.xs.XSElementDeclaration;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.Feature;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.geometry.Geometry;

/**
 * Allows the representation of arbitrary {@link Property}s.
 *
 * TODO Differentiation between value and children needs to be clarified and documented.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class GenericProperty implements Property {

	private static final QName XSI_NIL = new QName(XSINS, "nil");

	private final QName name;

	private final PropertyType declaration;

	private TypedObjectNode value;

	private Map<QName, PrimitiveValue> attrs = Collections.emptyMap();

	private List<TypedObjectNode> children = Collections.emptyList();

	private XSElementDeclaration xsType;

	/**
	 * Creates a new {@link GenericProperty} instance.
	 * @param declaration type information
	 * @param value property value, can be <code>null</code>
	 */
	public GenericProperty(PropertyType declaration, TypedObjectNode value) {
		this(declaration, null, value);
	}

	/**
	 * Creates a new {@link GenericProperty} instance.
	 * @param declaration type information
	 * @param name name of the property (does not necessarily match the name in the type
	 * information)
	 * @param value property value, can be <code>null</code>
	 */
	public GenericProperty(PropertyType declaration, QName name, TypedObjectNode value) {
		this.declaration = declaration;
		if (name == null) {
			this.name = declaration.getName();
		}
		else {
			this.name = name;
		}
		this.value = value;
		this.children = Collections.singletonList(value);

		if (declaration instanceof SimplePropertyType) {
			if (value != null && !(value instanceof PrimitiveValue)) {
				// TODO do more fine grained type checks
				String msg = "Invalid simple property (PrimitiveType="
						+ ((SimplePropertyType) declaration).getPrimitiveType().getBaseType().name()
						+ "): required class="
						+ ((SimplePropertyType) declaration).getPrimitiveType().getBaseType().getValueClass()
						+ ", but given '" + value.getClass() + ".";
				throw new IllegalArgumentException(msg);
			}
		}
	}

	/**
	 * Creates a new {@link GenericProperty} instance.
	 * @param declaration type information
	 * @param name name of the property (does not necessarily match the name in the type
	 * information)
	 * @param value primary property value, can be <code>null</code>
	 */
	public GenericProperty(PropertyType declaration, QName name, TypedObjectNode value,
			Map<QName, PrimitiveValue> attrs, List<TypedObjectNode> children) {
		this(declaration, name, value);
		this.attrs = attrs;
		this.children = children;
	}

	public GenericProperty(PropertyType declaration, QName name, TypedObjectNode value,
			Map<QName, PrimitiveValue> attrs, List<TypedObjectNode> children, XSElementDeclaration xsType) {
		this.declaration = declaration;
		if (name == null) {
			this.name = declaration.getName();
		}
		else {
			this.name = name;
		}
		this.attrs = attrs;
		this.children = children;
		this.xsType = xsType;
	}

	public GenericProperty(PropertyType declaration, QName name, TypedObjectNode value, boolean isNilled) {
		this(declaration, name, value);
		if (isNilled) {
			this.attrs = new HashMap<QName, PrimitiveValue>();
			this.attrs.put(XSI_NIL, new PrimitiveValue(TRUE));
		}
		this.children = Collections.singletonList(value);
	}

	public GenericProperty(PropertyType declaration, QName name, TypedObjectNode value,
			Map<QName, PrimitiveValue> attrs) {
		this(declaration, name, value);
		this.attrs = attrs;
		this.children = Collections.singletonList(value);
	}

	@Override
	public QName getName() {
		return name;
	}

	@Override
	public TypedObjectNode getValue() {
		if (value != null) {
			return value;
		}

		if (children == null || children.isEmpty()) {
			return null;
		}

		// // TODO
		if (declaration instanceof CustomPropertyType) {
			return new GenericXMLElement(name, declaration, attrs, children);
		}

		for (TypedObjectNode child : children) {
			if (declaration instanceof GeometryPropertyType && child instanceof Geometry) {
				return child;
			}
			if (declaration instanceof FeaturePropertyType && child instanceof Feature) {
				return child;
			}
			if (declaration instanceof SimplePropertyType && child instanceof PrimitiveValue) {
				return child;
			}
		}
		return null;
	}

	@Override
	public void setValue(TypedObjectNode value) {
		this.value = value;
		if (value != null) {
			this.children = Collections.singletonList(value);
		}
		else {
			this.children = Collections.emptyList();
		}
	}

	@Override
	public void setChildren(List<TypedObjectNode> children) {
		if (children == null) {
			this.children = Collections.emptyList();
			this.value = null;
		}
		else {
			this.children = children;
			if (children.size() == 1) {
				this.value = children.get(0);
			}
			else {
				this.value = null;
			}
		}
	}

	@Override
	public PropertyType getType() {
		return declaration;
	}

	@Override
	public String toString() {
		return value == null ? "null" : value.toString();
	}

	@Override
	public Map<QName, PrimitiveValue> getAttributes() {
		return attrs;
	}

	@Override
	public List<TypedObjectNode> getChildren() {
		return children;
	}

	@Override
	public XSElementDeclaration getXSType() {
		return xsType;
	}

}