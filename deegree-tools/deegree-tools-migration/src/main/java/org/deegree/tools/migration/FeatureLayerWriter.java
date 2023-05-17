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
package org.deegree.tools.migration;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.layer.persistence.LayerStoreProvider;
import org.deegree.tools.migration.FeatureLayerExtractor.FeatureLayer;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.WorkspaceUtils;

/**
 * Responsible for creating new feature layer configurations.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class FeatureLayerWriter {

	private Workspace workspace;

	FeatureLayerWriter(Workspace workspace) {
		this.workspace = workspace;
	}

	void writeLayerConfigs(HashMap<String, List<FeatureLayer>> map, String crs) throws XMLStreamException {
		XMLOutputFactory outfac = XMLOutputFactory.newInstance();

		for (Entry<String, List<FeatureLayer>> e : map.entrySet()) {
			String id = e.getKey();
			List<FeatureLayer> list = e.getValue();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();

			String flns = "http://www.deegree.org/layers/feature";
			String lns = "http://www.deegree.org/layers/base";
			String dns = "http://www.deegree.org/metadata/description";
			String gns = "http://www.deegree.org/metadata/spatial";

			XMLStreamWriter writer = new IndentingXMLStreamWriter(outfac.createXMLStreamWriter(bos));
			writer.writeStartDocument();
			writer.setDefaultNamespace(flns);
			writer.writeStartElement(flns, "FeatureLayers");
			writer.writeDefaultNamespace(flns);
			writer.writeNamespace("l", lns);
			writer.writeNamespace("d", dns);
			writer.writeNamespace("g", gns);

			XMLAdapter.writeElement(writer, flns, "FeatureStoreId", id);

			for (FeatureLayer l : list) {
				writer.writeStartElement(flns, "FeatureLayer");
				XMLAdapter.writeElement(writer, lns, "Name", l.name);
				XMLAdapter.writeElement(writer, dns, "Title", l.title);
				XMLAdapter.writeElement(writer, gns, "CRS", crs);
				if (!(Double.isInfinite(l.minscale) && Double.isInfinite(l.maxscale))) {
					writer.writeStartElement(lns, "ScaleDenominators");
					if (!Double.isInfinite(l.minscale)) {
						writer.writeAttribute("min", Double.toString(l.minscale));
					}
					if (!Double.isInfinite(l.maxscale)) {
						writer.writeAttribute("max", Double.toString(l.maxscale));
					}
					writer.writeEndElement();
				}
				if (l.style != null) {
					writer.writeStartElement(lns, "StyleRef");
					XMLAdapter.writeElement(writer, lns, "StyleStoreId", l.style);
					writer.writeEndElement();
				}

				writer.writeEndElement();
			}

			writer.writeEndElement();
			writer.close();

			WorkspaceUtils.activateSynthetic(workspace, LayerStoreProvider.class, id,
					new String(bos.toByteArray(), Charset.forName("UTF-8")));
		}
	}

}
