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
package org.deegree.feature.xpath.node;

import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.feature.Feature;
import org.deegree.geometry.Geometry;
import org.deegree.time.primitive.TimeInstant;
import org.deegree.time.primitive.TimePeriod;

/**
 * {@link ElementNode} that wraps a {@link GMLObject} and it's parent.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class GMLObjectNode<V extends GMLObject, P extends TypedObjectNode> extends ElementNode<V> {

	private XPathNode<P> parentNode;

	private V object;

	public GMLObjectNode(XPathNode<P> parentNode, V object) {
		super(getName(object));
		this.parentNode = parentNode;
		this.object = object;
	}

	private static QName getName(GMLObject object) {
		if (object.getType() != null) {
			return object.getType().getName();
		}
		if (object instanceof Feature) {
			return ((Feature) object).getName();
		}
		else if (object instanceof Geometry) {
			// TODO should be covered by the type
			return new QName("GEOMETRY");
		}
		else if (object instanceof TimeInstant) {
			return new QName(GML3_2_NS, "TimeInstant");
		}
		else if (object instanceof TimePeriod) {
			return new QName(GML3_2_NS, "TimePeriod");
		}
		throw new IllegalArgumentException(
				"Creating GMLObjectNode from " + object.getClass() + " needs implementation.");
	}

	@Override
	public XPathNode<P> getParent() {
		return parentNode;
	}

	@Override
	public V getValue() {
		return object;
	}

}
