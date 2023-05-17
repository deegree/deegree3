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
package org.deegree.layer.persistence.coverage;

import static org.deegree.layer.config.ConfigUtils.parseStyles;
import static org.deegree.layer.persistence.coverage.LayerMetadataBuilder.buildLayerMetadata;

import java.util.HashMap;
import java.util.Map;

import org.deegree.commons.utils.Pair;
import org.deegree.coverage.Coverage;
import org.deegree.coverage.persistence.CoverageProvider;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.MultiResolutionRaster;
import org.deegree.layer.Layer;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.MultipleLayerStore;
import org.deegree.layer.persistence.coverage.jaxb.CoverageLayerType;
import org.deegree.layer.persistence.coverage.jaxb.CoverageLayers;
import org.deegree.layer.persistence.coverage.jaxb.FeatureInfoModeType;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * Converts manual coverage layer config beans to layers.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class ManualCoverageLayerBuilder {

	private Workspace workspace;

	private ResourceMetadata<LayerStore> metadata;

	ManualCoverageLayerBuilder(Workspace workspace, ResourceMetadata<LayerStore> metadata) {
		this.workspace = workspace;
		this.metadata = metadata;
	}

	LayerStore buildManual(CoverageLayers cfg) {
		Map<String, Layer> map = new HashMap<String, Layer>();

		Coverage cov = workspace.getResource(CoverageProvider.class, cfg.getCoverageStoreId());

		for (CoverageLayerType lay : cfg.getCoverageLayer()) {
			LayerMetadata md = buildLayerMetadata(lay, cov);
			CoverageFeatureInfoMode infoMode = null;
			if (FeatureInfoModeType.POINT == lay.getFeatureInfoMode()) {
				infoMode = CoverageFeatureInfoMode.POINT;
			}
			else if (FeatureInfoModeType.INTERPOLATION == lay.getFeatureInfoMode()) {
				infoMode = CoverageFeatureInfoMode.INTERPOLATION;
			}

			Pair<Map<String, Style>, Map<String, Style>> p = parseStyles(workspace, lay.getName(), lay.getStyleRef());
			md.setStyles(p.first);
			md.setLegendStyles(p.second);
			Layer l = new CoverageLayer(md, cov instanceof AbstractRaster ? (AbstractRaster) cov : null,
					cov instanceof MultiResolutionRaster ? (MultiResolutionRaster) cov : null, infoMode);
			map.put(lay.getName(), l);
		}

		return new MultipleLayerStore(map, metadata);
	}

}
