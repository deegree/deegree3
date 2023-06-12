/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.gml.dictionary;

import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.gml.GMLObjectType;
import org.deegree.commons.tom.gml.GMLStdProps;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.StringOrRef;

/**
 * Default implementation of {@link Definition}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class GenericDefinition implements Definition {

	private String id;

	private GMLStdProps gmlProps;

	/**
	 * Creates a new {@link GenericDefinition} instance.
	 * @param id id of the definition, can be <code>null</code>
	 * @param gmlProps GML standard properties (which contain description and names), must
	 * not be <code>null</code>
	 */
	public GenericDefinition(String id, GMLStdProps gmlProps) {
		this.id = id;
		this.gmlProps = gmlProps;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public StringOrRef getDescription() {
		return gmlProps.getDescription();
	}

	@Override
	public CodeType[] getNames() {
		return gmlProps.getNames();
	}

	@Override
	public GMLStdProps getGMLProperties() {
		return gmlProps;
	}

	@Override
	public GMLObjectType getType() {
		throw new UnsupportedOperationException("Implement me");
	}

	@Override
	public List<Property> getProperties() {
		throw new UnsupportedOperationException("Implement me");
	}

	@Override
	public List<Property> getProperties(QName propName) {
		throw new UnsupportedOperationException("Implement me");
	}

}
