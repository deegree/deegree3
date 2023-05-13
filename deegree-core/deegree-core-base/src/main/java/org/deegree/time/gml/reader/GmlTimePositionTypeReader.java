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

import static org.deegree.commons.xml.stax.XMLStreamUtils.getAttributeValue;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.time.position.IndeterminateValue;
import org.deegree.time.position.TimePosition;

public class GmlTimePositionTypeReader {

	private static final String FRAME = "frame";

	private static final String CALENDAR_ERA_NAME = "calendarEraName";

	private static final String INDETERMINATE_POSITION = "indeterminatePosition";

	/**
	 * Consumes and parses the given <code>gml:TimePositionType</code> element event.
	 * <ul>
	 * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event
	 * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code>
	 * event</li>
	 * </ul>
	 * @param xmlStream must not be <code>null</code>
	 * @return corresponding {@link TimePosition} object, never <code>null</code>
	 * @throws XMLStreamException
	 */
	public TimePosition read(final XMLStreamReader xmlStream) throws XMLStreamException {
		// <attribute name="frame" type="anyURI" default="#ISO-8601"/>
		final String frame = getAttributeValue(xmlStream, FRAME);
		// <attribute name="calendarEraName" type="string"/>
		final String calendarEraName = getAttributeValue(xmlStream, CALENDAR_ERA_NAME);
		// <attribute name="indeterminatePosition" type="gml:TimeIndeterminateValueType"/>
		final IndeterminateValue indeterminatePosition = parseIndeterminateValueIfPresent(xmlStream);
		// gml:TimePositionUnion
		final String timePositionUnion = xmlStream.getElementText().trim();
		return new TimePosition(frame, calendarEraName, indeterminatePosition, timePositionUnion);
	}

	private IndeterminateValue parseIndeterminateValueIfPresent(final XMLStreamReader xmlStream) {
		final String indeterminatePosition = getAttributeValue(xmlStream, INDETERMINATE_POSITION);
		if (indeterminatePosition == null) {
			return null;
		}
		return IndeterminateValue.valueOf(indeterminatePosition.toUpperCase());
	}

}
