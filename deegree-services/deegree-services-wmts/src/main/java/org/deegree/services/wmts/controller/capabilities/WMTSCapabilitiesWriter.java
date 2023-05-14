/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.wmts.controller.capabilities;

import static org.deegree.commons.utils.MapUtils.DEFAULT_PIXEL_SIZE;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.metadata.Description;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.MapUtils;
import org.deegree.cs.components.Unit;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.featureinfo.FeatureInfoManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.io.CoordinateFormatter;
import org.deegree.geometry.io.DecimalCoordinateFormatter;
import org.deegree.geometry.primitive.Point;
import org.deegree.layer.Layer;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.layer.persistence.tile.TileLayer;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.ows.capabilities.OWSCapabilitiesXMLAdapter;
import org.deegree.theme.Theme;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>WMTSCapabilitiesWriter</code>
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class WMTSCapabilitiesWriter extends OWSCapabilitiesXMLAdapter {

	private static Logger LOG = LoggerFactory.getLogger(WMTSCapabilitiesWriter.class);

	static final String WMTSNS = "http://www.opengis.net/wmts/1.0";

	private static final String XSINS = "http://www.w3.org/2001/XMLSchema-instance";

	private final XMLStreamWriter writer;

	private final ServiceProvider provider;

	private final List<Theme> themes;

	private String mdurltemplate;

	private WmtsCapabilitiesMetadataWriter mdwriter;

	private WmtsLayerWriter layerWriter;

	private static GeometryTransformer transformer;

	// used for formatting WGS84 bounding box coordinates
	private static CoordinateFormatter formatter = new DecimalCoordinateFormatter();

	static {
		try {
			transformer = new GeometryTransformer("EPSG:4326");
		}
		catch (Exception e) {
			LOG.error("Could not initialize GeometryTransformer.");
		}
	}

	public WMTSCapabilitiesWriter(XMLStreamWriter writer, ServiceIdentification identification,
			ServiceProvider provider, List<Theme> themes, String mdurltemplate, FeatureInfoManager mgr) {
		this.writer = writer;
		this.provider = provider;
		this.themes = themes;
		if (mdurltemplate == null || mdurltemplate.isEmpty()) {
			mdurltemplate = OGCFrontController.getHttpGetURL();
			if (!(mdurltemplate.endsWith("?") || mdurltemplate.endsWith("&"))) {
				mdurltemplate += "?";
			}
			mdurltemplate += "service=CSW&request=GetRecordById&version=2.0.2&outputSchema=http%3A//www.isotc211.org/2005/gmd&elementSetName=full&id=${metadataSetId}";
		}
		this.mdurltemplate = mdurltemplate;
		this.mdwriter = new WmtsCapabilitiesMetadataWriter(writer, identification);
		this.layerWriter = new WmtsLayerWriter(mgr, writer, this);
	}

	public void export100() throws XMLStreamException {

		writer.setDefaultNamespace(WMTSNS);
		writer.writeStartElement(WMTSNS, "Capabilities");
		writer.setPrefix(OWS_PREFIX, OWS110_NS);
		writer.writeDefaultNamespace(WMTSNS);
		writer.writeNamespace(OWS_PREFIX, OWS110_NS);
		writer.writeNamespace("xlink", XLN_NS);
		writer.writeNamespace("xsi", XSINS);
		writer.writeAttribute("version", "1.0.0");
		writer.writeAttribute(XSINS, "schemaLocation",
				"http://www.opengis.net/wmts/1.0 http://schemas.opengis.net/wmts/1.0/wmtsGetCapabilities_response.xsd");

		mdwriter.exportServiceIdentification();
		exportServiceProvider110New(writer, provider);
		mdwriter.exportOperationsMetadata();

		exportContents(themes);

		exportThemes(themes);

		writer.writeEndElement(); // Capabilities
	}

	String getMdurltemplate() {
		return mdurltemplate;
	}

	private void exportThemes(List<Theme> themes) throws XMLStreamException {
		if (themes.isEmpty()) {
			return;
		}
		writer.writeStartElement(WMTSNS, "Themes");
		for (Theme t : themes) {
			exportTheme(t);
		}
		writer.writeEndElement();
	}

	private void exportTheme(Theme t) throws XMLStreamException {
		writer.writeStartElement(WMTSNS, "Theme");
		exportMetadata(t.getLayerMetadata(), false, null, null);

		for (Theme t2 : t.getThemes()) {
			exportTheme(t2);
		}
		exportLayers(t.getLayers());

		writer.writeEndElement();
	}

	void exportMetadata(LayerMetadata md, boolean idOnly, String otherid, Envelope env) throws XMLStreamException {
		if (!idOnly) {
			Description desc = md.getDescription();
			LanguageString title = desc.getTitle(null);
			if (title != null) {
				writeElement(writer, OWS110_NS, "Title", title.getString());
			}
			LanguageString abs = desc.getAbstract(null);
			if (abs != null) {
				writeElement(writer, OWS110_NS, "Abstract", abs.getString());
			}
			exportKeyWords110New(writer, desc.getKeywords());
			if (env != null) {
				env = getWgs84Envelope(env);
				writer.writeStartElement(OWS110_NS, "WGS84BoundingBox");
				Point min = env.getMin();
				Point max = env.getMax();
				double minX = -180.0;
				double minY = -90.0;
				double maxX = 180.0;
				double maxY = 90.0;
				try {
					minX = min.get0();
					minY = min.get1();
					maxX = max.get0();
					maxY = max.get1();
				}
				catch (ArrayIndexOutOfBoundsException e) {
					LOG.error(
							"Cannot generate WGS84 envelope for tile layer '" + md.getName() + "'. Using full extent.",
							e);
					minX = -180.0;
					minY = -90.0;
					maxX = 180.0;
					maxY = 90.0;
				}
				writer.writeStartElement(OWS110_NS, "LowerCorner");
				writer.writeCharacters(formatter.format(minX) + " " + formatter.format(minY));
				writer.writeEndElement();
				writer.writeStartElement(OWS110_NS, "UpperCorner");
				writer.writeCharacters(formatter.format(maxX) + " " + formatter.format(maxY));
				writer.writeEndElement();
				writer.writeEndElement();
			}
		}
		if (otherid == null) {
			writeElement(writer, OWS110_NS, "Identifier", md.getName());
			if (md.getMetadataId() != null) {
				writer.writeStartElement(OWS110_NS, "Metadata");
				writer.writeAttribute(XLN_NS, "href", mdurltemplate.replace("${metadataSetId}", md.getMetadataId()));
				writer.writeEndElement();
			}
		}
		else {
			writeElement(writer, OWS110_NS, "Identifier", otherid);
		}
	}

	private Envelope getWgs84Envelope(Envelope env) {
		try {
			env = transformer.transform(env);
		}
		catch (Exception e) {
			LOG.error("Cannot transform TileLayer envelope to WGS84.");
		}
		return env;
	}

	private void exportLayers(List<Layer> layers) throws XMLStreamException {
		for (Layer l : layers) {
			if (l instanceof TileLayer) {
				writeElement(writer, WMTSNS, "LayerRef", l.getMetadata().getName());
			}
		}
	}

	private void exportContents(List<Theme> themes) throws XMLStreamException {
		writer.writeStartElement(WMTSNS, "Contents");

		// actually used tile matrix sets are going to be collected the first way through
		// the layers
		Set<TileMatrixSet> matrixSets = new LinkedHashSet<TileMatrixSet>();

		layerWriter.writeLayers(themes, matrixSets);

		for (TileMatrixSet tms : matrixSets) {
			exportTileMatrixSet(tms);
		}

		writer.writeEndElement();
	}

	private void exportTileMatrixSet(TileMatrixSet tms) throws XMLStreamException {
		writer.writeStartElement(WMTSNS, "TileMatrixSet");

		exportMetadata(null, true, tms.getIdentifier(), null);
		ICRS cs = tms.getSpatialMetadata().getCoordinateSystems().get(0);
		writeElement(writer, OWS110_NS, "SupportedCRS", cs.getAlias());
		String wknScaleSet = tms.getWellKnownScaleSet();
		if (wknScaleSet != null) {
			writeElement(writer, WMTSNS, "WellKnownScaleSet", wknScaleSet);
		}

		for (TileMatrix tm : tms.getTileMatrices()) {
			writer.writeStartElement(WMTSNS, "TileMatrix");
			double scale;
			if (cs.getUnits()[0].equals(Unit.DEGREE) && wknScaleSet == null) {
				scale = MapUtils.calcScaleFromDegrees(tm.getResolution());
			}
			else {
				scale = tm.getResolution() / DEFAULT_PIXEL_SIZE;
			}
			writeElement(writer, OWS110_NS, "Identifier", tm.getIdentifier());
			writeElement(writer, WMTSNS, "ScaleDenominator", scale + "");
			Envelope env = tm.getSpatialMetadata().getEnvelope();
			writeElement(writer, WMTSNS, "TopLeftCorner", env.getMin().get0() + " " + env.getMax().get1());
			writeElement(writer, WMTSNS, "TileWidth", "" + tm.getTilePixelsX());
			writeElement(writer, WMTSNS, "TileHeight", "" + tm.getTilePixelsY());
			writeElement(writer, WMTSNS, "MatrixWidth", "" + tm.getNumTilesX());
			writeElement(writer, WMTSNS, "MatrixHeight", "" + tm.getNumTilesY());

			writer.writeEndElement();
		}

		writer.writeEndElement();
	}

}
