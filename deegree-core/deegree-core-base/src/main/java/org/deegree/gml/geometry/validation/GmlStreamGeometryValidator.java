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
package org.deegree.gml.geometry.validation;

import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.validation.GeometryValidationEventHandler;
import org.deegree.geometry.validation.GeometryValidator;
import org.deegree.geometry.validation.event.GeometryValidationEvent;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.geometry.GMLGeometryReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates all geometry elements (at all levels of the document) of a GML stream.
 * <p>
 * The validator's reaction on topological issues is controlled by providing a
 * {@link GmlGeometryValidationEventHandler} which can also be used for generating
 * validity reports.
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class GmlStreamGeometryValidator {

	private static final Logger LOG = LoggerFactory.getLogger(GmlStreamGeometryValidator.class);

	private final GMLStreamReader gmlStream;

	private final GMLGeometryReader geomParser;

	private final XMLStreamReaderWrapper xmlStream;

	private final GmlGeometryValidationEventHandler gmlErrorHandler;

	/**
	 * Creates a new {@link GmlStreamGeometryValidator} instance.
	 * @param gmlStream GML stream, must not be <code>null</code>
	 * @param eventHandler event handler that controls the reaction on topological events
	 * and genearates validity reports
	 */
	public GmlStreamGeometryValidator(GMLStreamReader gmlStream, GmlGeometryValidationEventHandler eventHandler) {
		xmlStream = new XMLStreamReaderWrapper(gmlStream.getXMLReader(),
				gmlStream.getXMLReader().getLocation().getSystemId());
		geomParser = gmlStream.getGeometryReader();
		this.gmlStream = gmlStream;
		this.gmlErrorHandler = eventHandler;
	}

	/**
	 * Starts the validation.
	 * @throws XMLStreamException
	 * @throws UnknownCRSException
	 */
	public void validateGeometries() throws XMLStreamException, UnknownCRSException {

		while (xmlStream.getEventType() != END_DOCUMENT) {
			if (xmlStream.isStartElement()) {
				if (gmlStream.isGeometryElement()) {
					validateGeometryElement();
				}
			}
			xmlStream.next();
		}
	}

	private void validateGeometryElement() throws UnknownCRSException {

		Location location = xmlStream.getLocation();
		LOG.debug("Validating GML geometry element ('" + xmlStream.getLocalName() + "') at line: "
				+ location.getLineNumber() + ", column: " + location.getColumnNumber() + ".");

		GmlElementIdentifier identifier = new GmlElementIdentifier(xmlStream);
		ValidationEventRedirector eventRedirector = new ValidationEventRedirector(gmlErrorHandler, identifier);
		GeometryValidator geometryValidator = new GeometryValidator(eventRedirector);
		try {
			Geometry geometry = geomParser.parse(xmlStream);
			geometryValidator.validateGeometry(geometry);
		}
		catch (XMLParsingException e) {
			gmlErrorHandler.parsingError(identifier, e);
		}
		catch (XMLStreamException e) {
			gmlErrorHandler.parsingError(identifier, e);
		}
	}

	private class ValidationEventRedirector implements GeometryValidationEventHandler {

		private final GmlGeometryValidationEventHandler gmlErrorHandler;

		private final GmlElementIdentifier topLevelGeometryElement;

		private ValidationEventRedirector(GmlGeometryValidationEventHandler gmlErrorHandler,
				GmlElementIdentifier topLevelGeometryElement) {
			this.gmlErrorHandler = gmlErrorHandler;
			this.topLevelGeometryElement = topLevelGeometryElement;
		}

		@Override
		public boolean fireEvent(GeometryValidationEvent event) {
			return gmlErrorHandler.topologicalEvent(new GmlGeometryValidationEvent(event, getAffectedElements()));
		}

		private List<GmlElementIdentifier> getAffectedElements() {
			return Collections.singletonList(topLevelGeometryElement);
		}

	}

}
