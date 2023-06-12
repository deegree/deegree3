/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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
package org.deegree.time.gml.reader;

import static java.util.Collections.emptyList;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipElement;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.gml.property.Property;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.commons.AbstractGMLObjectReader;
import org.deegree.time.primitive.RelatedTime;

class AbstractGmlTimeGeometricPrimitiveReader extends AbstractGMLObjectReader {

	protected static final String FRAME = "frame";

	protected AbstractGmlTimeGeometricPrimitiveReader(GMLStreamReader gmlStreamReader) {
		super(gmlStreamReader);
	}

	// TODO implement actual parsing
	protected List<Property> readGmlStandardProperties(final XMLStreamReader xmlStream) throws XMLStreamException {
		final List<Property> props = new ArrayList<Property>();
		nextElement(xmlStream);
		while (xmlStream.isStartElement() && isStandardProperty(xmlStream.getName())) {
			skipElement(xmlStream);
			nextElement(xmlStream);
		}
		return props;
	}

	// TODO implement actual parsing
	protected List<RelatedTime> readRelatedTimes(final XMLStreamReader xmlStream) throws XMLStreamException {
		final QName elName = new QName(gmlNs, "relatedTime");
		while (xmlStream.isStartElement() && elName.equals(xmlStream.getName())) {
			skipElement(xmlStream);
			nextElement(xmlStream);
		}
		return emptyList();
	}

	private boolean isStandardProperty(QName name) {
		if (gmlNs.equals(name.getNamespaceURI())) {
			String localName = name.getLocalPart();
			return "metaDataProperty".equals(localName) || "description".equals(localName)
					|| "descriptionReference".equals(localName) || "identifier".equals(localName)
					|| "name".equals(localName);
		}
		return false;
	}

}
