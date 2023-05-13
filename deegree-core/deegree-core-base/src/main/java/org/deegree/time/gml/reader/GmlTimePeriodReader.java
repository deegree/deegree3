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

import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getAttributeValue;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.requireStartElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipElement;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.ReferenceResolver;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.gml.GMLStreamReader;
import org.deegree.time.position.TimePosition;
import org.deegree.time.primitive.GenericTimePeriod;
import org.deegree.time.primitive.RelatedTime;
import org.deegree.time.primitive.TimeInstant;
import org.deegree.time.primitive.TimePeriod;
import org.deegree.time.primitive.TimePositionOrInstant;
import org.deegree.time.primitive.reference.TimeInstantReference;

public class GmlTimePeriodReader extends AbstractGmlTimeGeometricPrimitiveReader {

	private static final String BEGIN = "begin";

	private static final String BEGIN_POSITION = "beginPosition";

	private static final String END = "end";

	private static final String END_POSITION = "endPosition";

	private static final String DURATION = "duration";

	private static final String TIME_INTERVAL = "timeInterval";

	public GmlTimePeriodReader(final GMLStreamReader gmlStreamReader) {
		super(gmlStreamReader);
	}

	/**
	 * Consumes the given <code>gml:TimePeriod</code> element event.
	 * <ul>
	 * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event
	 * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code>
	 * event</li>
	 * </ul>
	 * @param xmlStream must not be <code>null</code>
	 * @return corresponding {@link TimePeriod} object, never <code>null</code>
	 * @throws XMLStreamException
	 */
	public TimePeriod read(final XMLStreamReader xmlStream) throws XMLStreamException {
		final String gmlId = parseGmlId(xmlStream);
		// <attribute name="frame" type="anyURI" default="#ISO-8601"/>
		final String frame = getAttributeValue(xmlStream, FRAME);
		final List<Property> props = readGmlStandardProperties(xmlStream);
		// <element name="relatedTime" type="gml:RelatedTimeType" minOccurs="0"
		// maxOccurs="unbounded"/>
		final List<RelatedTime> relatedTimes = readRelatedTimes(xmlStream);
		// <choice>
		final TimePositionOrInstant begin = readRequiredBeginOrBeginPosition(xmlStream);
		// <choice>
		final TimePositionOrInstant end = readRequiredEndOrEndPosition(xmlStream);
		// <group ref="gml:timeLength" minOccurs="0"/>
		nextElement(xmlStream);
		skipDurationIfPresent(xmlStream);
		skipTimeIntervalIfPresent(xmlStream);
		return new GenericTimePeriod(gmlId, props, relatedTimes, frame, begin, end);
	}

	private TimePositionOrInstant readRequiredBeginOrBeginPosition(final XMLStreamReader xmlStream)
			throws XMLStreamException {
		if (!gmlStreamReader.getLaxMode()) {
			final List<QName> expected = getQnames(BEGIN, BEGIN_POSITION);
			requireStartElement(xmlStream, expected);
		}
		if (xmlStream.getLocalName().equals(BEGIN)) {
			// <element name="begin" type="gml:TimeInstantPropertyType"/>
			final TimeInstant instant = readTimeInstantPropertyType(xmlStream);
			nextElement(xmlStream);
			return instant;

		}
		else if (xmlStream.getLocalName().equals(BEGIN_POSITION)) {
			// <element name="beginPosition" type="gml:TimePositionType"/>
			final TimePosition pos = new GmlTimePositionTypeReader().read(xmlStream);
			nextElement(xmlStream);
			return pos;
		}
		return null;
	}

	private TimePositionOrInstant readRequiredEndOrEndPosition(final XMLStreamReader xmlStream)
			throws XMLStreamException {
		final List<QName> expected = getQnames(END, END_POSITION);
		requireStartElement(xmlStream, expected);
		if (xmlStream.getLocalName().equals(END)) {
			// <element name="end" type="gml:TimeInstantPropertyType"/>
			return readTimeInstantPropertyType(xmlStream);
		}
		// <element name="endPosition" type="gml:TimePositionType"/>
		return new GmlTimePositionTypeReader().read(xmlStream);
	}

	private void skipDurationIfPresent(final XMLStreamReader xmlStream) throws XMLStreamException {
		final QName elName = new QName(gmlNs, DURATION);
		if (xmlStream.isStartElement() && elName.equals(xmlStream.getName())) {
			skipElement(xmlStream);
			nextElement(xmlStream);
		}
	}

	private void skipTimeIntervalIfPresent(final XMLStreamReader xmlStream) throws XMLStreamException {
		final QName elName = new QName(gmlNs, TIME_INTERVAL);
		if (xmlStream.isStartElement() && elName.equals(xmlStream.getName())) {
			skipElement(xmlStream);
			nextElement(xmlStream);
		}
	}

	private TimeInstant readTimeInstantPropertyType(final XMLStreamReader xmlStream) throws XMLStreamException {
		final String href = xmlStream.getAttributeValue(XLNNS, "href");
		if (href != null) {
			final ReferenceResolver resolver = null;
			final TimeInstantReference reference = new TimeInstantReference(resolver, href, getSystemId());
			idContext.addReference(reference);
			skipElement(xmlStream);
			return reference;
		}
		nextElement(xmlStream);
		requireStartElement(xmlStream, new QName(gmlNs, "TimeInstant"));
		final GmlTimeInstantReader timeInstantReader = new GmlTimeInstantReader(gmlStreamReader);
		final TimeInstant read = timeInstantReader.read(xmlStream);
		nextElement(xmlStream);
		return read;
	}

	private List<QName> getQnames(final String... localNames) {
		final List<QName> qNames = new ArrayList<QName>();
		for (final String localName : localNames) {
			qNames.add(new QName(gmlNs, localName));
		}
		return qNames;
	}

}
