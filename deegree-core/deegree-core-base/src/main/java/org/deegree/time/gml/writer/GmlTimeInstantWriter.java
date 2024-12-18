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
package org.deegree.time.gml.writer;

import static java.util.UUID.randomUUID;
import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;

import java.util.UUID;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.time.position.TimePosition;
import org.deegree.time.primitive.TimeInstant;

public class GmlTimeInstantWriter {

	private static final String FRAME = "frame";

	private static final String TIME_POSITION = "timePosition";

	private static final String gmlNs = GML3_2_NS;

	private static final String gmlPrefix = "gml";

	public void write(final XMLStreamWriter writer, final TimeInstant timeInstant) throws XMLStreamException {
		writer.writeStartElement(gmlPrefix, "TimeInstant", gmlNs);
		writeGmlId(writer, timeInstant);
		// <attribute name="frame" type="anyURI" default="#ISO-8601"/>
		writeAttributeIfNotNull(FRAME, timeInstant.getFrame(), writer);
		// <element name="relatedTime" type="gml:RelatedTimeType" minOccurs="0"
		// maxOccurs="unbounded"/>
		// <element name="timePosition" type="gml:TimePositionType">
		writeTimePosition(writer, timeInstant.getPosition());
		writer.writeEndElement();
	}

	private void writeGmlId(final XMLStreamWriter writer, final TimeInstant timeInstant) throws XMLStreamException {
		if (timeInstant.getId() != null) {
			writer.writeAttribute(gmlPrefix, gmlNs, "id", timeInstant.getId());
		}
		else {
			writer.writeAttribute(gmlPrefix, gmlNs, "id", "uuid." + randomUUID());
		}
	}

	private void writeAttributeIfNotNull(final String name, final String value, final XMLStreamWriter writer)
			throws XMLStreamException {
		if (value != null) {
			writer.writeAttribute(name, value);
		}
	}

	private void writeTimePosition(final XMLStreamWriter writer, final TimePosition timePosition)
			throws XMLStreamException {
		writer.writeStartElement(gmlPrefix, TIME_POSITION, gmlNs);
		new GmlTimePositionTypeWriter().write(timePosition, writer);
		writer.writeEndElement();
	}

}
