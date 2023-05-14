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
package org.deegree.tools.migration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.deegree.services.OWS;
import org.deegree.services.OwsManager;
import org.deegree.services.wms.controller.WMSController;
import org.deegree.workspace.Workspace;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
class FeatureLayerExtractor {

	private Workspace workspace;

	private Pattern stylepattern;

	private int logicalLayerCount = 0;

	FeatureLayerExtractor(Workspace workspace) {
		this.workspace = workspace;
		stylepattern = Pattern.compile("\\.\\./styles/([A-Za-z_0-9]+)\\.xml");
	}

	void extract() throws XMLStreamException {
		HashMap<String, List<FeatureLayer>> map = new HashMap<String, List<FeatureLayer>>();
		String crs = analyseWmsConfigurations(map);
		new FeatureLayerWriter(workspace).writeLayerConfigs(map, crs);
	}

	private String analyseWmsConfigurations(HashMap<String, List<FeatureLayer>> map) throws XMLStreamException {
		String crs = null;

		OwsManager mgr = workspace.getResourceManager(OwsManager.class);

		XMLInputFactory infac = XMLInputFactory.newInstance();

		List<OWS> wmss = mgr.getByOWSClass(WMSController.class);
		for (OWS ows : wmss) {
			StreamSource streamSource = new StreamSource(ows.getMetadata().getLocation().getAsStream());
			XMLStreamReader reader = infac.createXMLStreamReader(streamSource);
			reader.next();

			while (reader.hasNext()) {
				if (crs == null && reader.isStartElement() && reader.getLocalName().equals("CRS")) {
					crs = reader.getElementText();
				}

				if (reader.isStartElement() && (reader.getLocalName().equals("RequestableLayer")
						|| reader.getLocalName().equals("LogicalLayer"))) {
					extractLayer(reader, map);
				}
				reader.next();
			}
		}
		return crs;
	}

	private void extractLayer(XMLStreamReader reader, HashMap<String, List<FeatureLayer>> map)
			throws XMLStreamException {
		FeatureLayer l = new FeatureLayer();

		extractMetadata(reader, l);

		reader.nextTag();
		if (reader.getLocalName().equals("ScaleDenominators")) {
			if (reader.getAttributeValue(null, "min") != null) {
				l.minscale = Double.parseDouble(reader.getAttributeValue(null, "min"));
			}
			if (reader.getAttributeValue(null, "max") != null) {
				l.maxscale = Double.parseDouble(reader.getAttributeValue(null, "max"));
			}
			reader.nextTag();
			reader.nextTag();
		}

		String ftid = null;
		if (reader.getLocalName().equals("FeatureStoreId")) {
			ftid = reader.getElementText();
			reader.nextTag();
		}

		if (reader.getLocalName().equals("DirectStyle")) {
			reader.nextTag();
			String text = reader.getElementText();
			Matcher m = stylepattern.matcher(text);
			if (m.find()) {
				l.style = m.group(1);
			}
			else {
				l.style = text;
			}
		}
		if (ftid == null) {
			return;
		}
		List<FeatureLayer> list = map.get(ftid);
		if (list == null) {
			list = new ArrayList<FeatureLayer>();
			map.put(ftid, list);
		}
		list.add(l);
	}

	private void extractMetadata(XMLStreamReader reader, FeatureLayer l) throws XMLStreamException {
		if (reader.getLocalName().equals("RequestableLayer")) {
			reader.nextTag();
			l.name = reader.getElementText();
			reader.nextTag();
			l.title = reader.getElementText();
		}
		else {
			l.name = "LogicalLayer_" + ++logicalLayerCount;
			l.title = l.name;
		}
	}

	static class FeatureLayer {

		String name;

		String title;

		String style;

		double minscale = Double.NEGATIVE_INFINITY, maxscale = Double.POSITIVE_INFINITY;

	}

}
