/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.gml.feature;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.gml.feature.GMLFeatureReader.BOUNDED_BY_GML31;
import static org.deegree.gml.feature.GMLFeatureReader.BOUNDED_BY_GML32;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.Features;
import org.deegree.feature.i18n.Messages;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.types.FeatureCollectionType;
import org.deegree.feature.types.property.ArrayPropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.geometry.Envelope;
import org.deegree.gml.GMLStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link StreamFeatureCollection} for GML feature collection elements.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class StreamFeatureCollection implements FeatureInputStream {

	private static Logger LOG = LoggerFactory.getLogger(StreamFeatureCollection.class);

	private final String fid;

	private final FeatureCollectionType ft;

	private final GMLFeatureReader featureReader;

	private final XMLStreamReaderWrapper xmlStream;

	private final Iterator<PropertyType> declIter;

	private ICRS activeCRS;

	private PropertyType activeDecl;

	private int propOccurences;

	// stores non-feature member properties
	private final List<Property> nonMemberProps = new ArrayList<Property>();

	private boolean featureArrayMode;

	/**
	 * Creates a new {@link StreamFeatureCollection} from the given
	 * {@link GMLStreamReader}.
	 * @param fid
	 * @param ft
	 * @param featureReader
	 * @param xmlStream
	 * @param crs
	 * @throws XMLStreamException
	 */
	StreamFeatureCollection(String fid, FeatureCollectionType ft, GMLFeatureReader featureReader,
			XMLStreamReaderWrapper xmlStream, ICRS crs) throws XMLStreamException {
		this.fid = fid;
		this.ft = ft;
		this.featureReader = featureReader;
		this.xmlStream = xmlStream;
		xmlStream.next();

		declIter = ft.getPropertyDeclarations().iterator();
		activeDecl = declIter.next();
		propOccurences = 0;
		activeCRS = crs;
	}

	public Feature read() throws IOException {
		Feature feature = null;
		try {
			while (feature == null && xmlStream.getEventType() != END_ELEMENT) {
				if (featureArrayMode) {
					feature = readFeatureArray();
				}
				else {
					feature = readProperty();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e.getMessage(), e);
		}
		return feature;
	}

	private Feature readFeatureArray() throws XMLParsingException, XMLStreamException, UnknownCRSException {
		Feature feature = null;
		if (xmlStream.getEventType() == START_ELEMENT) {
			feature = featureReader.parseFeature(xmlStream, activeCRS);
		}
		if (xmlStream.next() == END_ELEMENT) {
			LOG.debug("End of feature array property encountered.");
			featureArrayMode = false;
			propOccurences++;
		}
		return feature;
	}

	private Feature readProperty() throws XMLParsingException, XMLStreamException, UnknownCRSException {
		Feature feature = null;
		int event = xmlStream.getEventType();
		if (event == START_ELEMENT) {
			QName propName = xmlStream.getName();
			if (LOG.isDebugEnabled()) {
				LOG.debug("- property '" + propName + "'");
			}
			if (featureReader.findConcretePropertyType(propName, activeDecl) != null) {
				// current property element is equal to active declaration
				if (activeDecl.getMaxOccurs() != -1 && propOccurences > activeDecl.getMaxOccurs()) {
					String msg = Messages.getMessage("ERROR_PROPERTY_TOO_MANY_OCCURENCES", propName,
							activeDecl.getMaxOccurs(), ft.getName());
					throw new XMLParsingException(xmlStream, msg);
				}
			}
			else {
				// current property element is not equal to active declaration
				while (declIter.hasNext() && featureReader.findConcretePropertyType(propName, activeDecl) == null) {
					if (propOccurences < activeDecl.getMinOccurs()) {
						String msg = null;
						if (activeDecl.getMinOccurs() == 1) {
							msg = Messages.getMessage("ERROR_PROPERTY_MANDATORY", activeDecl.getName(), ft.getName());
						}
						else {
							msg = Messages.getMessage("ERROR_PROPERTY_TOO_FEW_OCCURENCES", activeDecl.getName(),
									activeDecl.getMinOccurs(), ft.getName());
						}
						throw new XMLParsingException(xmlStream, msg);
					}
					activeDecl = declIter.next();
					propOccurences = 0;
				}
				if (featureReader.findConcretePropertyType(propName, activeDecl) == null) {
					String msg = Messages.getMessage("ERROR_PROPERTY_UNEXPECTED", propName, ft.getName());
					throw new XMLParsingException(xmlStream, msg);
				}
			}

			PropertyType pt = featureReader.findConcretePropertyType(propName, activeDecl);
			if (pt instanceof FeaturePropertyType) {
				Property property = featureReader.parseProperty(xmlStream, pt, activeCRS);
				if (property != null) {
					feature = (Feature) property.getValue();
				}
				propOccurences++;
			}
			else if (pt instanceof ArrayPropertyType) {
				LOG.debug("Switching to feature array state");
				featureArrayMode = true;
			}
			else {
				Property property = featureReader.parseProperty(xmlStream, pt, activeCRS);
				if (property != null) {
					// if this is the "gml:boundedBy" property, override active CRS
					// (see GML spec. (where???))
					if (BOUNDED_BY_GML31.equals(activeDecl.getName())
							|| BOUNDED_BY_GML32.equals(activeDecl.getName())) {
						Envelope bbox = (Envelope) property.getValue();
						if (bbox.getCoordinateSystem() != null) {
							activeCRS = bbox.getCoordinateSystem();
							LOG.debug("- crs (from boundedBy): '" + activeCRS + "'");
						}
					}

					nonMemberProps.add(property);
				}
				propOccurences++;
			}
		}
		xmlStream.next();
		return feature;
	}

	@Override
	public void close() {
		// TODO what to do on close?
	}

	@Override
	public Iterator<Feature> iterator() {
		final Feature nextFeature;
		try {
			nextFeature = read();
		}
		catch (IOException e) {
			throw new RuntimeException();
		}
		return new Iterator<Feature>() {

			Feature next = nextFeature;

			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public Feature next() {
				if (next == null) {
					throw new NoSuchElementException();
				}
				Feature currentFeature = next;
				try {
					next = read();
				}
				catch (IOException e) {
					throw new RuntimeException();
				}
				return currentFeature;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public FeatureCollection toCollection() {
		return Features.toCollection(this);
	}

	@Override
	public int count() {
		int i = 0;
		for (@SuppressWarnings("unused")
		Feature f : this) {
			i++;
		}
		close();
		return i;
	}

}
