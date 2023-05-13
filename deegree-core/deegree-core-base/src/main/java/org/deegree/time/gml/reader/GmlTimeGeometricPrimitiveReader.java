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

import static org.deegree.commons.xml.stax.XMLStreamUtils.requireStartElement;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.commons.AbstractGMLObjectReader;
import org.deegree.time.primitive.TimeGeometricPrimitive;
import org.deegree.time.primitive.TimeInstant;
import org.deegree.time.primitive.TimePeriod;

public class GmlTimeGeometricPrimitiveReader extends AbstractGMLObjectReader {

	private static final String TIME_INSTANT = "TimeInstant";

	private static final String TIME_PERIOD = "TimePeriod";

	public GmlTimeGeometricPrimitiveReader(final GMLStreamReader gmlStream) {
		super(gmlStream);
	}

	/**
	 * Consumes the given <code>gml:AbstractTimeGeometricPrimitive</code> element event.
	 * <ul>
	 * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event
	 * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code>
	 * event</li>
	 * </ul>
	 * @param xmlStream must not be <code>null</code>
	 * @return corresponding {@link TimeGeometricPrimitive} object, never
	 * <code>null</code>
	 * @throws XMLStreamException
	 */
	public TimeGeometricPrimitive read(final XMLStreamReader xmlStream) throws XMLStreamException {
		final List<QName> expectedElements = getQnamesWithGmlNs(TIME_INSTANT, TIME_PERIOD);
		requireStartElement(xmlStream, expectedElements);
		if (TIME_INSTANT.equals(xmlStream.getLocalName())) {
			return readTimeInstant(xmlStream);
		}
		return readTimePeriod(xmlStream);
	}

	private TimeInstant readTimeInstant(final XMLStreamReader xmlStream) throws XMLStreamException {
		return new GmlTimeInstantReader(gmlStreamReader).read(xmlStream);
	}

	private TimePeriod readTimePeriod(final XMLStreamReader xmlStream) throws XMLStreamException {
		return new GmlTimePeriodReader(gmlStreamReader).read(xmlStream);
	}

	private List<QName> getQnamesWithGmlNs(final String... localNames) {
		final List<QName> qNames = new ArrayList<QName>();
		for (final String localName : localNames) {
			qNames.add(new QName(gmlNs, localName));
		}
		return qNames;
	}

}
