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
package org.deegree.services.wmts.controller;

import static org.deegree.commons.ows.exception.OWSException.INVALID_PARAMETER_VALUE;
import static org.deegree.commons.ows.exception.OWSException.OPERATION_NOT_SUPPORTED;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.featureinfo.FeatureInfoManager;
import org.deegree.layer.Layer;
import org.deegree.layer.persistence.tile.TileLayer;
import org.deegree.protocol.wmts.ops.GetFeatureInfo;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.wmts.jaxb.FeatureInfoFormatsType;
import org.deegree.theme.Theme;
import org.deegree.theme.Themes;
import org.deegree.workspace.ResourceInitException;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;

/**
 * Responsible for handling GetFeatureInfo requests.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
class FeatureInfoHandler {

	private FeatureInfoManager featureInfoManager;

	private Map<String, TileLayer> layers;

	FeatureInfoHandler(FeatureInfoFormatsType conf, ResourceLocation<?> location, Workspace workspace,
			List<Theme> themes) throws ResourceInitException {
		featureInfoManager = FeatureInfoManagerBuilder.buildFeatureInfoManager(conf, location, workspace);

		layers = new HashMap<String, TileLayer>();
		for (Theme theme : themes) {
			for (Layer l : Themes.getAllLayers(theme)) {
				if (l instanceof TileLayer) {
					layers.put(l.getMetadata().getName(), ((TileLayer) l));
				}
			}
		}
	}

	void getFeatureInfo(Map<String, String> map, HttpResponseBuffer response)
			throws OWSException, IOException, XMLStreamException {
		GetFeatureInfo gfi = new GetFeatureInfo(map);
		TileLayer l = checkLayerConfigured(gfi);
		checkLayerFeatureInfoEnabled(gfi, l);
		FeatureInfoFetcher fetcher = new FeatureInfoFetcher(l, gfi);
		fetcher.fetch(featureInfoManager, response);
	}

	private TileLayer checkLayerConfigured(GetFeatureInfo gfi) throws OWSException {
		TileLayer l = layers.get(gfi.getLayer());
		if (l == null) {
			throw new OWSException("No layer with name '" + gfi.getLayer() + "' configured.", INVALID_PARAMETER_VALUE,
					"layer");
		}

		String infoFormat = gfi.getInfoFormat();
		if (!featureInfoManager.getSupportedFormats().contains(infoFormat)) {
			throw new OWSException("FeatureInfo format '" + infoFormat + "' is unknown.", INVALID_PARAMETER_VALUE,
					"infoFormat");
		}
		TileDataSet tds = l.getTileDataSet(gfi.getTileMatrixSet());
		if (tds == null) {
			throw new OWSException(
					"The TileMatrixSet parameter value of '" + gfi.getTileMatrixSet() + "' is not valid.",
					INVALID_PARAMETER_VALUE, "tileMatrixSet");
		}
		TileDataLevel tdl = tds.getTileDataLevel(gfi.getTileMatrix());
		if (tdl == null) {
			throw new OWSException("The TileMatrix parameter value of '" + gfi.getTileMatrix() + "' is not valid.",
					INVALID_PARAMETER_VALUE, "tileMatrix");
		}
		double width = tdl.getMetadata().getTilePixelsX();
		double height = tdl.getMetadata().getTilePixelsY();
		if (gfi.getI() >= width || gfi.getI() < 0) {
			throw new OWSException("The I parameter does not fit in the image dimension.", "TileOutOfRange", "I");
		}
		if (gfi.getJ() >= height || gfi.getJ() < 0) {
			throw new OWSException("The J parameter does not fit in the image dimension.", "TileOutOfRange", "J");
		}
		return l;
	}

	private void checkLayerFeatureInfoEnabled(GetFeatureInfo gfi, TileLayer l) throws OWSException {
		if (!l.getMetadata().isQueryable()) {
			throw new OWSException("Layer '" + gfi.getLayer() + "' not configured for FeatureInfo.",
					OPERATION_NOT_SUPPORTED);
		}
	}

	public FeatureInfoManager getManager() {
		return featureInfoManager;
	}

}
