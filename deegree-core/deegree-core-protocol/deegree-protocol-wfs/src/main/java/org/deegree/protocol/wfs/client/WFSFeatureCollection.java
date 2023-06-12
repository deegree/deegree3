/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.protocol.wfs.client;

import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.AppSchema;
import org.deegree.geometry.Envelope;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.StreamFeatureCollection;
import org.deegree.gml.reference.GmlDocumentIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides convenient stream-based access to the payload of a WFS <code>GetFeature</code>
 * response.
 * <p>
 * Supported versions:
 * <ul>
 * <li>WFS 1.0.0</li>
 * <li>WFS 1.1.0</li>
 * <li>WFS 2.0.0</li>
 * </ul>
 * </p>
 * <p>
 * TODO in order to make this usable for <b>really</b> large amounts of complex features,
 * {@link GmlDocumentIdContext} needs to be rewritten (so it doesn't keep references to
 * all features in memory).
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class WFSFeatureCollection<T> {

	private static Logger LOG = LoggerFactory.getLogger(WFSFeatureCollection.class);

	private static final QName WFS200_MEMBER = new QName(WFS_200_NS, "member");

	private static final QName WFS200_TUPLE = new QName(WFS_200_NS, "Tuple");

	private static final QName WFS200_SF_COLLECTION = new QName(WFS_200_NS, "SimpleFeatureCollection");

	private final XMLStreamReader xmlStream;

	private final GMLStreamReader gmlStream;

	// only used in non-WFS 2.0 mode
	private StreamFeatureCollection fc;

	private final String lockId;

	private final String timeStamp;

	private BigInteger numberMatched;

	private BigInteger numberReturned;

	private String nextUri;

	private String previousUri;

	private Envelope boundedBy;

	/**
	 * Creates a new {@link WFSFeatureCollection} instance.
	 * @param xmlStream
	 * @param gmlVersion
	 * @param schema
	 * @throws XMLStreamException
	 * @throws UnknownCRSException
	 * @throws XMLParsingException
	 */
	WFSFeatureCollection(XMLStreamReader xmlStream, GMLVersion gmlVersion, AppSchema schema)
			throws XMLStreamException, XMLParsingException, UnknownCRSException {

		XMLStreamUtils.skipStartDocument(xmlStream);
		this.xmlStream = xmlStream;

		if (WFS_200_NS.equals(xmlStream.getNamespaceURI())) {
			LOG.debug("WFS 2.0 response");

			// <xsd:attribute name="timeStamp" type="xsd:dateTime" use="required"/>
			timeStamp = xmlStream.getAttributeValue(null, "timeStamp");

			// <xsd:attribute name="numberMatched" type="wfs:nonNegativeIntegerOrUnknown"
			// use="required"/>
			String numberMatchedAttr = xmlStream.getAttributeValue(null, "numberMatched");
			if (numberMatchedAttr != null && !("unknown").equals(numberMatchedAttr)) {
				try {
					numberMatched = new BigInteger(numberMatchedAttr);
				}
				catch (NumberFormatException e) {
					LOG.warn("Unable to parse value of 'numberMatched' attribute (='" + numberMatchedAttr
							+ "'). Neither 'unknown' nor an integer value.");
				}
			}

			// <xsd:attribute name="numberReturned" type="xsd:nonNegativeInteger"
			// use="required"/>
			// CR ??? pending:
			// <xsd:attribute name="numberReturned" type="wfs:nonNegativeIntegerOrUnknown"
			// use="required"/>
			String numberReturnedAttr = xmlStream.getAttributeValue(null, "numberReturned");
			if (numberReturnedAttr != null && !("unknown").equals(numberMatchedAttr)) {
				try {
					numberReturned = new BigInteger(numberReturnedAttr);
				}
				catch (NumberFormatException e) {
					LOG.warn("Unable to parse value of 'numberReturned' attribute (='" + numberReturnedAttr
							+ "'). Neither 'unknown' nor an integer value.");
				}
			}

			// <xsd:attribute name="next" type="xsd:anyURI"/>
			nextUri = xmlStream.getAttributeValue(null, "next");

			// <xsd:attribute name="previous" type="xsd:anyURI"/>
			previousUri = xmlStream.getAttributeValue(null, "previous");

			// <xsd:attribute name="lockId" type="xsd:string"/>
			lockId = xmlStream.getAttributeValue(null, "lockId");

			gmlStream = GMLInputFactory.createGMLStreamReader(gmlVersion, xmlStream);
			gmlStream.setApplicationSchema(schema);

			// TODO <xsd:element ref="wfs:boundedBy" minOccurs="0"/>

			// forward to first wfs:member element
			XMLStreamUtils.nextElement(xmlStream);
			while (!xmlStream.isEndElement()) {
				if (WFS200_MEMBER.equals(xmlStream.getName())) {
					break;
				}
				XMLStreamUtils.nextElement(xmlStream);
			}
		}
		else {
			LOG.debug("WFS 1.0.0/1.1.0 response");

			// <xsd:attribute name="lockId" type="xsd:string" use="optional">
			lockId = xmlStream.getAttributeValue(null, "lockId");

			// <xsd:attribute name="timeStamp" type="xsd:dateTime" use="optional">
			timeStamp = xmlStream.getAttributeValue(null, "timeStamp");

			// <xsd:attribute name="numberOfFeatures" type="xsd:nonNegativeInteger"
			// use="optional">
			String numberOfFeaturesAttr = xmlStream.getAttributeValue(null, "numberOfFeatures");
			if (numberOfFeaturesAttr != null) {
				try {
					numberMatched = new BigInteger(numberOfFeaturesAttr);
				}
				catch (NumberFormatException e) {
					LOG.warn("Unable to parse value of 'numberOfFeatures' attribute (='" + numberOfFeaturesAttr
							+ "'). Not an integer value.");
				}
			}

			gmlStream = GMLInputFactory.createGMLStreamReader(gmlVersion, xmlStream);
			gmlStream.setApplicationSchema(schema);
			fc = gmlStream.readFeatureCollectionStream();
		}
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public BigInteger getNumberReturned() {
		return numberReturned;
	}

	public BigInteger getNumberMatched() {
		return numberMatched;
	}

	public String getNextUri() {
		return nextUri;
	}

	public String getPreviousUri() {
		return previousUri;
	}

	public String getLockId() {
		return lockId;
	}

	public Envelope getBoundedBy() {
		return boundedBy;
	}

	@SuppressWarnings("unchecked")
	public Iterator<T> getMembers() {
		if (fc != null) {
			return (Iterator<T>) fc.iterator();
		}
		return new Iterator<T>() {

			@Override
			public boolean hasNext() {
				return xmlStream.isStartElement() && WFS200_MEMBER.equals(xmlStream.getName());
			}

			@Override
			public T next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				T value = null;
				try {
					value = parse200MemberProperty();
				}
				catch (Throwable t) {
					throw new RuntimeException(t.getMessage(), t);
				}
				return value;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public Iterator<GMLObject> getAdditionalObjects() {
		return null;
	}

	public GmlDocumentIdContext getIdContext() {
		return null;
	}

	public FeatureCollection toCollection() {
		return null;
	}

	/**
	 * Returns the object contained in the <code>wfs:member</code> property that the
	 * <code>XMLStreamReader</code> currently points at.
	 * <p>
	 * <ul>
	 * <li>Pre-condition: cursor must point at the <code>START_ELEMENT</code> event
	 * (<code>wfs:member</code>)</li>
	 * <li>Post-condition: cursor points at the next element event after the corresponding
	 * <code>END_ELEMENT</code> event</li>
	 * </p>
	 * @return parsed object, never <code>null</code>
	 * @throws XMLStreamException
	 * @throws NoSuchElementException
	 * @throws UnknownCRSException
	 * @throws XMLParsingException
	 */
	@SuppressWarnings("unchecked")
	private T parse200MemberProperty()
			throws NoSuchElementException, XMLStreamException, XMLParsingException, UnknownCRSException {

		T value = null;

		nextElement(xmlStream);

		if (xmlStream.isEndElement()) {
			// must be xlinked
			throw new UnsupportedOperationException(
					"Parsing of wfs:member properties with xlinked content is not supported yet.");
		}

		QName elName = xmlStream.getName();
		if (WFS200_TUPLE.equals(elName)) {
			throw new UnsupportedOperationException("Parsing of wfs:Tuple elements is not supported yet.");
		}
		else if (WFS200_SF_COLLECTION.equals(elName)) {
			throw new UnsupportedOperationException(
					"Parsing of wfs:SimpleFeatureCollection elements is not supported yet.");
		}
		else {
			value = (T) gmlStream.read();
		}

		nextElement(xmlStream);
		nextElement(xmlStream);

		return value;
	}

}
