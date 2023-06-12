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
package org.deegree.layer.persistence.feature;

import static org.deegree.layer.config.ConfigUtils.parseStyles;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.utils.Pair;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.sort.SortProperty;
import org.deegree.layer.Layer;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.MultipleLayerStore;
import org.deegree.layer.persistence.feature.jaxb.FeatureLayerType;
import org.deegree.layer.persistence.feature.jaxb.FeatureLayers;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * Builds feature layers that are manually configured.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class ManualFeatureLayerBuilder {

	private FeatureLayers lays;

	private ResourceMetadata<LayerStore> metadata;

	private FeatureStore store;

	private Workspace workspace;

	ManualFeatureLayerBuilder(FeatureLayers lays, ResourceMetadata<LayerStore> metadata, FeatureStore store,
			Workspace workspace) {
		this.lays = lays;
		this.metadata = metadata;
		this.store = store;
		this.workspace = workspace;
	}

	MultipleLayerStore buildFeatureLayers() throws XMLStreamException, URISyntaxException, FeatureStoreException {
		Map<String, Layer> map = new LinkedHashMap<String, Layer>();
		int index = -1;
		for (FeatureLayerType lay : lays.getFeatureLayer()) {
			++index;
			QName featureType = lay.getFeatureType();

			// these methods do not use the dom elements but reparse the configuration
			// file using StAX due to bugs
			// in jaxb/woodstox when using multiple jaxb:dom bindings and DOMSources for
			// XMLStreamReaders
			OperatorFilter filter = QueryOptionsParser.parseFilter(index, metadata.getLocation().getAsStream());
			List<SortProperty> sortBy = QueryOptionsParser.parseSortBy(index, metadata.getLocation().getAsStream());
			List<SortProperty> sortByFeatureInfo = sortBy;
			if (sortBy != null && lay.getSortBy().isReverseFeatureInfo()) {
				sortByFeatureInfo = new ArrayList<SortProperty>();
				for (SortProperty prop : sortBy) {
					sortByFeatureInfo.add(new SortProperty(prop.getSortProperty(), !prop.getSortOrder()));
				}
			}

			LayerMetadata md = LayerMetadataBuilder.buildMetadata(lay, featureType, store);

			Pair<Map<String, Style>, Map<String, Style>> p = parseStyles(workspace, lay.getName(), lay.getStyleRef());
			md.setStyles(p.first);
			md.setLegendStyles(p.second);
			Layer l = new FeatureLayer(md, store, featureType, filter, sortBy, sortByFeatureInfo);
			map.put(lay.getName(), l);
		}
		return new MultipleLayerStore(map, metadata);
	}

}
