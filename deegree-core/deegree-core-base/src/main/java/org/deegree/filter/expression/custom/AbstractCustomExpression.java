/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.filter.expression.custom;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.filter.Expression;

/**
 * Base class for implementing {@link CustomExpression}s.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public abstract class AbstractCustomExpression implements CustomExpression {

	@Override
	public Type getType() {
		return Type.CUSTOM;
	}

	@Override
	public CustomExpression parse100(XMLStreamReader xmlStream) throws XMLStreamException {
		return parse(xmlStream);
	}

	@Override
	public CustomExpression parse110(XMLStreamReader xmlStream) throws XMLStreamException {
		return parse(xmlStream);
	}

	@Override
	public CustomExpression parse200(XMLStreamReader xmlStream) throws XMLStreamException {
		return parse(xmlStream);
	}

	/**
	 * @param xmlStream
	 * @throws XMLStreamException
	 */
	public abstract CustomExpression parse(XMLStreamReader xmlStream) throws XMLStreamException;

	@Override
	public Expression[] getParams() {
		return new Expression[0];
	}

	@Override
	public String toString(String indent) {
		return indent + "CustomExpression (" + getElementName() + ")";
	}

}