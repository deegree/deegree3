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
package org.deegree.feature.types.property;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;

/**
 * A {@link PropertyType} that defines a property with a primitive value, i.e. a value
 * that can be represented as a single {@link String}.
 *
 * @see BaseType
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class SimplePropertyType extends AbstractPropertyType {

	private final PrimitiveType pt;

	private String codeList;

	public SimplePropertyType(QName name, int minOccurs, int maxOccurs, BaseType type, XSElementDeclaration elDecl,
			List<PropertyType> substitutions) {
		super(name, minOccurs, maxOccurs, elDecl, substitutions);
		this.pt = new PrimitiveType(type);
	}

	public SimplePropertyType(QName name, int minOccurs, int maxOccurs, BaseType type, XSElementDeclaration elDecl,
			List<PropertyType> substitutions, XSSimpleTypeDefinition xsdType) {
		super(name, minOccurs, maxOccurs, elDecl, substitutions);
		this.pt = new PrimitiveType(xsdType);
	}

	public void setCodeList(String codeList) {
		this.codeList = codeList;
	}

	public String getCodeList() {
		return codeList;
	}

	/**
	 * Returns the primitive type.
	 * @return the primitive type, never <code>null</code>
	 */
	public PrimitiveType getPrimitiveType() {
		return pt;
	}

	@Override
	public String toString() {
		return "- simple property type: '" + name + "', minOccurs=" + minOccurs + ", maxOccurs=" + maxOccurs
				+ ", type: " + pt;
	}

}