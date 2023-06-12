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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.time.position.TimePosition;

public class GmlTimePositionTypeWriter {

	private static final String FRAME = "frame";

	private static final String CALENDAR_ERA_NAME = "calendarEraName";

	private static final String INDETERMINATE_POSITION = "indeterminatePosition";

	public void write(final TimePosition timePosition, final XMLStreamWriter writer) throws XMLStreamException {
		// <attribute name="frame" type="anyURI" default="#ISO-8601"/>
		writeAttributeIfNotNull(FRAME, timePosition.getFrame(), writer);
		// <attribute name="calendarEraName" type="string"/>
		writeAttributeIfNotNull(CALENDAR_ERA_NAME, timePosition.getCalendarEraName(), writer);
		// <attribute name="indeterminatePosition" type="gml:TimeIndeterminateValueType"/>
		String indeterminatePosition = null;
		if (timePosition.getIndeterminatePosition() != null) {
			indeterminatePosition = timePosition.getIndeterminatePosition().toString().toLowerCase();
		}
		writeAttributeIfNotNull(INDETERMINATE_POSITION, indeterminatePosition, writer);
		// gml:TimePositionUnion
		writeCharactersIfNotEmpty(timePosition.getValue(), writer);
	}

	private void writeAttributeIfNotNull(final String name, final String value, final XMLStreamWriter writer)
			throws XMLStreamException {
		if (value != null) {
			writer.writeAttribute(name, value);
		}
	}

	private void writeCharactersIfNotEmpty(final String value, final XMLStreamWriter writer) throws XMLStreamException {
		if (value != null && !value.isEmpty()) {
			writer.writeCharacters(value);
		}
	}

}