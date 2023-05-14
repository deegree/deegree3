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
package org.deegree.commons.tom.gml;

import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.gml.property.PropertyType;

/**
 * Defines a class of {@link GMLObject}s, i.e. objects with same name and same types of
 * properties.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public interface GMLObjectType {

	/**
	 * Returns the GML object type category.
	 *
	 * @returns category, never <code>null</code>
	 */
	GMLObjectCategory getCategory();

	/**
	 * Returns the name that objects of this type have.
	 * <p>
	 * In a GML encoding, this corresponds to the object's element name.
	 * </p>
	 * @return the name of the object, never <code>null</code>
	 */
	QName getName();

	/**
	 * Returns whether this type definition is abstract or not.
	 * @return <code>true</code>, if this type is abstract, <code>false</code> otherwise
	 */
	boolean isAbstract();

	/**
	 * Returns the declaration of the property with the given name.
	 * @param propName name of the property, must not be <code>null</code>
	 * @return the declaration of the property, or <code>null</code> if no such property
	 * is defined
	 */
	PropertyType getPropertyDeclaration(QName propName);

	/**
	 * Returns all property declarations of the object type.
	 * @return property declarations (in order), may be empty, but never <code>null</code>
	 */
	List<PropertyType> getPropertyDeclarations();

}
