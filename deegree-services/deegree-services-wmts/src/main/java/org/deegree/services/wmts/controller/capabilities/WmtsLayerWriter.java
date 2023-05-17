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
package org.deegree.services.wmts.controller.capabilities;

import static org.deegree.services.wmts.controller.capabilities.WMTSCapabilitiesWriter.WMTSNS;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.featureinfo.FeatureInfoManager;
import org.deegree.layer.Layer;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.layer.persistence.tile.TileLayer;
import org.deegree.services.ows.capabilities.OWSCapabilitiesXMLAdapter;
import org.deegree.theme.Theme;
import org.deegree.theme.Themes;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.TileMatrixSet;

/**
 * Responsible to write out layer capability section.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class WmtsLayerWriter extends OWSCapabilitiesXMLAdapter {

	private final FeatureInfoManager mgr;

	private final XMLStreamWriter writer;

	private final WMTSCapabilitiesWriter capWriter;

	WmtsLayerWriter(FeatureInfoManager mgr, XMLStreamWriter writer, WMTSCapabilitiesWriter capWriter) {
		this.mgr = mgr;
		this.writer = writer;
		this.capWriter = capWriter;
	}

	void writeLayers(List<Theme> themes, Set<TileMatrixSet> matrixSets) throws XMLStreamException {
		for (Theme t : themes) {
			for (Layer l : Themes.getAllLayers(t)) {
				if (l instanceof TileLayer) {
					exportLayer(matrixSets, (TileLayer) l);
				}
			}
		}
	}

	private void exportLayer(Set<TileMatrixSet> matrixSets, TileLayer tl) throws XMLStreamException {
		LayerMetadata md = tl.getMetadata();
		for (TileDataSet tds : tl.getTileDataSets()) {
			matrixSets.add(tds.getTileMatrixSet());
		}

		writer.writeStartElement(WMTSNS, "Layer");

		capWriter.exportMetadata(md, false, null, tl.getMetadata().getSpatialMetadata().getEnvelope());
		writer.writeStartElement(WMTSNS, "Style");
		writeElement(writer, OWS110_NS, "Identifier", "default");
		writer.writeEndElement();
		List<String> fmts = new ArrayList<String>();
		for (TileDataSet tds : tl.getTileDataSets()) {
			String fmt = tds.getNativeImageFormat();
			if (!fmts.contains(fmt)) {
				fmts.add(fmt);
			}
		}
		for (String fmt : fmts) {
			writeElement(writer, WMTSNS, "Format", fmt);
		}
		if (md.isQueryable()) {
			for (String fmt : mgr.getSupportedFormats()) {
				writeElement(writer, WMTSNS, "InfoFormat", fmt);
			}
		}
		for (TileDataSet tds : tl.getTileDataSets()) {
			writer.writeStartElement(WMTSNS, "TileMatrixSetLink");
			writeElement(writer, WMTSNS, "TileMatrixSet", tds.getTileMatrixSet().getIdentifier());
			writer.writeEndElement();
		}

		writer.writeEndElement();
	}

}
