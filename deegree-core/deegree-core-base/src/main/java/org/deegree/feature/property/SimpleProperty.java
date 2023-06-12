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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xerces.xs.XSElementDeclaration;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.types.property.SimplePropertyType;

/**
 * TODO add documentation here
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class SimpleProperty implements Property {

	private SimplePropertyType pt;

	private PrimitiveValue value;

	public SimpleProperty(SimplePropertyType pt, PrimitiveValue value) {
		this.pt = pt;
		this.value = value;
	}

	public SimpleProperty(SimplePropertyType pt, String value) {
		this.pt = pt;
		this.value = new PrimitiveValue(value, pt.getPrimitiveType());
	}

	@Override
	public QName getName() {
		return pt.getName();
	}

	@Override
	public XSElementDeclaration getXSType() {
		return null;
	}

	@Override
	public PrimitiveValue getValue() {
		return value;
	}

	@Override
	public void setValue(TypedObjectNode value) {
		this.value = (PrimitiveValue) value;
	}

	@Override
	public SimplePropertyType getType() {
		return pt;
	}

	@Override
	public String toString() {
		return value == null ? "null" : value.toString();
	}

	@Override
	public Map<QName, PrimitiveValue> getAttributes() {
		return Collections.emptyMap();
	}

	@Override
	public List<TypedObjectNode> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public void setChildren(List<TypedObjectNode> children) {
		if (children.isEmpty()) {
			value = null;
		}
		else if (children.size() == 1) {
			value = (PrimitiveValue) children.get(0);
		}
		else {
			throw new IllegalArgumentException();
		}
	}

}