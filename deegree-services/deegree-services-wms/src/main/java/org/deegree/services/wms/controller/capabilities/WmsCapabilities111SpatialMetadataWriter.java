/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.wms.controller.capabilities;

import static org.deegree.commons.xml.XMLAdapter.writeElement;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.io.CoordinateFormatter;
import org.deegree.geometry.io.DecimalCoordinateFormatter;
import org.slf4j.Logger;

/**
 * Responsible for writing out envelopes and crs.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class WmsCapabilities111SpatialMetadataWriter {

	private static final Logger LOG = getLogger(WmsCapabilities111SpatialMetadataWriter.class);

	public static void writeSrsAndEnvelope(XMLStreamWriter writer, List<ICRS> srs, Envelope layerEnv)
			throws XMLStreamException {
		for (ICRS crs : srs) {
			writeElement(writer, "SRS", crs.getAlias());
		}

		ICRS latlon;
		final CoordinateFormatter formatter = new DecimalCoordinateFormatter(8);
		try {
			latlon = CRSManager.lookup("CRS:84");
			if (layerEnv != null && layerEnv.getCoordinateDimension() >= 2) {
				Envelope bbox = new GeometryTransformer(latlon).transform(layerEnv);
				writer.writeStartElement("LatLonBoundingBox");
				writer.writeAttribute("minx", formatter.format(bbox.getMin().get0()));
				writer.writeAttribute("miny", formatter.format(bbox.getMin().get1()));
				writer.writeAttribute("maxx", formatter.format(bbox.getMax().get0()));
				writer.writeAttribute("maxy", formatter.format(bbox.getMax().get1()));
				writer.writeEndElement();

				for (ICRS crs : srs) {
					if (crs.getAlias().startsWith("AUTO")) {
						continue;
					}
					// try {
					// crs
					// } catch ( UnknownCRSException e ) {
					// LOG.warn( "Cannot find: {}", e.getLocalizedMessage() );
					// LOG.trace( "Stack trace:", e );
					// continue;
					// }
					Envelope envelope;
					try {
						if (layerEnv.getCoordinateSystem() == null) {
							envelope = new GeometryTransformer(crs).transform(layerEnv, latlon);
						}
						else {
							envelope = new GeometryTransformer(crs).transform(layerEnv);
						}
					}
					catch (Throwable e) {
						LOG.warn("Cannot transform: {}", e.getLocalizedMessage());
						LOG.trace("Stack trace:", e);
						continue;
					}

					writer.writeStartElement("BoundingBox");
					writer.writeAttribute("SRS", crs.getAlias());
					writer.writeAttribute("minx", formatter.format(envelope.getMin().get0()));
					writer.writeAttribute("miny", formatter.format(envelope.getMin().get1()));
					writer.writeAttribute("maxx", formatter.format(envelope.getMax().get0()));
					writer.writeAttribute("maxy", formatter.format(envelope.getMax().get1()));
					writer.writeEndElement();
				}
			}
		}
		catch (Throwable e) {
			LOG.warn("Cannot transform: {}", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
		}
	}

}
