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
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.requireStartElement;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.gml.property.Property;
import org.deegree.gml.GMLStreamReader;
import org.deegree.time.position.TimePosition;
import org.deegree.time.primitive.GenericTimeInstant;
import org.deegree.time.primitive.RelatedTime;
import org.deegree.time.primitive.TimeInstant;

public class GmlTimeInstantReader extends AbstractGmlTimeGeometricPrimitiveReader {

	public GmlTimeInstantReader(final GMLStreamReader gmlStreamReader) {
		super(gmlStreamReader);
	}

	/**
	 * Consumes the given <code>gml:TimeInstant</code> element event.
	 * <ul>
	 * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event
	 * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code>
	 * event</li>
	 * </ul>
	 * @param xmlStream must not be <code>null</code>
	 * @return corresponding {@link TimeInstant} object, never <code>null</code>
	 * @throws XMLStreamException
	 */
	public TimeInstant read(final XMLStreamReader xmlStream) throws XMLStreamException {
		final String gmlId = parseGmlId(xmlStream);
		// <attribute name="frame" type="anyURI" default="#ISO-8601"/>
		final String frame = getAttributeValue(xmlStream, FRAME);
		final List<Property> props = readGmlStandardProperties(xmlStream);
		// <element name="relatedTime" type="gml:RelatedTimeType" minOccurs="0"
		// maxOccurs="unbounded"/>
		final List<RelatedTime> relatedTimes = readRelatedTimes(xmlStream);
		// <element name="timePosition" type="gml:TimePositionType">
		final TimePosition timePosition = readRequiredTimePosition(xmlStream);
		nextElement(xmlStream);
		return new GenericTimeInstant(gmlId, props, relatedTimes, frame, timePosition);
	}

	private TimePosition readRequiredTimePosition(XMLStreamReader xmlStream) throws XMLStreamException {
		requireStartElement(xmlStream, new QName(gmlNs, "timePosition"));
		return new GmlTimePositionTypeReader().read(xmlStream);
	}

}
