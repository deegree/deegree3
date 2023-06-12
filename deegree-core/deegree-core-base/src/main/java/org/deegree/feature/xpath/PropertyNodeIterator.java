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
package org.deegree.feature.xpath;

import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.feature.xpath.node.GMLObjectNode;
import org.deegree.feature.xpath.node.PropertyNode;
import org.deegree.feature.xpath.node.TimePositionAdapter;
import org.deegree.time.position.TimePosition;
import org.deegree.time.primitive.TimeInstant;
import org.deegree.time.primitive.TimePeriod;

/**
 * {@link Iterator} over property nodes of a feature node.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
class PropertyNodeIterator implements Iterator<PropertyNode> {

	private GMLObjectNode<GMLObject, GMLObject> parent;

	private Iterator<Property> propertyIter;

	PropertyNodeIterator(final GMLObjectNode<GMLObject, GMLObject> parent) {
		this.parent = parent;
		final GMLObject object = parent.getValue();
		final List<Property> props = new ArrayList<Property>();
		if (object.getProperties() != null) {
			props.addAll(object.getProperties());
		}
		if (object instanceof TimeInstant) {
			final TimePosition position = ((TimeInstant) object).getPosition();
			props.add(new TimePositionAdapter().getAsXMLElement(new QName(GML3_2_NS, "timePosition"), position));
		}
		else if (object instanceof TimePeriod) {
			final TimePosition beginPosition = ((TimePeriod) object).getBeginPosition();
			if (beginPosition != null) {
				props.add(new TimePositionAdapter().getAsXMLElement(new QName(GML3_2_NS, "beginPosition"),
						beginPosition));
			}
			final TimePosition endPosition = ((TimePeriod) object).getEndPosition();
			if (endPosition != null) {
				props.add(new TimePositionAdapter().getAsXMLElement(new QName(GML3_2_NS, "endPosition"), endPosition));
			}
		}
		propertyIter = props.iterator();
	}

	@Override
	public boolean hasNext() {
		return propertyIter != null && propertyIter.hasNext();
	}

	@Override
	public PropertyNode next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		Property prop = null;
		if (propertyIter != null && propertyIter.hasNext()) {
			prop = propertyIter.next();
		}
		return new PropertyNode(parent, prop);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
