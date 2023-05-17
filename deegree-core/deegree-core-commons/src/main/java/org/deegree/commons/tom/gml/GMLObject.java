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

import org.deegree.commons.tom.Object;
import org.deegree.commons.tom.gml.property.Property;

/**
 * Base interface for GML objects.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public interface GMLObject extends Object {

	/**
	 * Returns the id of the GML object.
	 * <p>
	 * In a GML encoding of the object, this corresponds to the <code>gml:id</code> (GML 3
	 * and later) or <code>fid</code> (GML 2) attribute of the object element.
	 * </p>
	 * @return the id of the object, may be <code>null</code>
	 */
	@Override
	public String getId();

	/**
	 * Returns the type declaration for this object.
	 *
	 * TODO type declaration should always be available
	 * @return type declaration, may be <code>null</code> (no type declaration available)
	 */
	public GMLObjectType getType();

	/**
	 * Returns all properties of this object, in order.
	 * @return all properties, in order, may be empty, but never <code>null</code>
	 */
	public List<Property> getProperties();

	/**
	 * Returns all properties with the given name, in order.
	 * @param propName name of the requested properties, must not be <code>null</code>
	 * @return the properties with the given name, in order, may be empty (no such
	 * properties), but never <code>null</code>
	 */
	public List<Property> getProperties(QName propName);

}
