/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.layer.persistence.gdal;

import static org.deegree.commons.ows.metadata.DescriptionConverter.fromJaxb;
import static org.deegree.geometry.metadata.SpatialMetadataConverter.fromJaxb;
import static org.deegree.layer.config.ConfigUtils.parseDimensions;
import static org.deegree.layer.config.ConfigUtils.parseStyles;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.commons.gdal.GdalSettings;
import org.deegree.commons.ows.metadata.Description;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.layer.Layer;
import org.deegree.layer.config.ConfigUtils;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.MultipleLayerStore;
import org.deegree.layer.persistence.base.jaxb.ScaleDenominatorsType;
import org.deegree.layer.persistence.gdal.jaxb.GDALLayerType;
import org.deegree.layer.persistence.gdal.jaxb.GDALLayers;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * This class is responsible for building GDAL layer stores.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
class GdalLayerStoreBuilder implements ResourceBuilder<LayerStore> {

	private final GDALLayers cfg;

	private final Workspace workspace;

	private final ResourceMetadata<LayerStore> metadata;

	private final GdalSettings gdalSettings;

	GdalLayerStoreBuilder(GDALLayers cfg, Workspace workspace, ResourceMetadata<LayerStore> metadata) {
		this.cfg = cfg;
		this.workspace = workspace;
		this.metadata = metadata;
		gdalSettings = workspace.getInitializable(GdalSettings.class);
	}

	@Override
	public LayerStore build() {
		Map<String, Layer> layerNameToLayer = new HashMap<String, Layer>();
		for (GDALLayerType gdalLayerCfg : cfg.getGDALLayer()) {

			List<ICRS> crsList = fromJaxb(gdalLayerCfg.getCRS());
			ICRS crs = crsList.isEmpty() ? null : crsList.get(0);
			List<File> datasets = buildDatasets(gdalLayerCfg.getFile(), crs);
			LayerMetadata md = buildLayerMetadata(gdalLayerCfg, datasets);
			Pair<Map<String, Style>, Map<String, Style>> p = parseStyles(workspace, gdalLayerCfg.getName(),
					gdalLayerCfg.getStyleRef());
			md.setStyles(p.first);
			md.setLegendStyles(p.second);
			Layer layer = new GdalLayer(md, datasets, gdalSettings);
			layerNameToLayer.put(gdalLayerCfg.getName(), layer);
		}
		return new MultipleLayerStore(layerNameToLayer, metadata);
	}

	private List<File> buildDatasets(List<String> files, ICRS crs) {
		List<File> datasets = new ArrayList<File>(files.size());
		for (String path : files) {
			try {
				File file = metadata.getLocation().resolveToFile(path).getCanonicalFile();
				gdalSettings.getDatasetPool().addDataset(file, crs);
				datasets.add(file);
			}
			catch (IOException e) {
				throw new IllegalArgumentException(e.getMessage(), e);
			}
		}
		return datasets;
	}

	private LayerMetadata buildLayerMetadata(GDALLayerType lay, List<File> datasets) {
		SpatialMetadata smd = fromJaxb(lay.getEnvelope(), lay.getCRS());
		Description desc = fromJaxb(lay.getTitle(), lay.getAbstract(), lay.getKeywords());
		LayerMetadata md = new LayerMetadata(lay.getName(), desc, smd);
		md.setDimensions(parseDimensions(md.getName(), lay.getDimension()));
		md.setMapOptions(ConfigUtils.parseLayerOptions(lay.getLayerOptions()));
		md.setMetadataId(lay.getMetadataSetId());
		if (smd.getEnvelope() == null) {
			Envelope env = null;
			try {
				for (File file : datasets) {
					if (env == null) {
						env = gdalSettings.getDatasetPool().getEnvelope(file);
					}
					else {
						env = env.merge(gdalSettings.getDatasetPool().getEnvelope(file));
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			smd.setEnvelope(env);
		}
		if (smd.getCoordinateSystems() == null || smd.getCoordinateSystems().isEmpty()) {
			List<ICRS> crs = new ArrayList<ICRS>();
			crs.add(smd.getEnvelope().getCoordinateSystem());
			smd.setCoordinateSystems(crs);
		}
		ScaleDenominatorsType denoms = lay.getScaleDenominators();
		if (denoms != null) {
			md.setScaleDenominators(new DoublePair(denoms.getMin(), denoms.getMax()));
		}
		return md;
	}

}
