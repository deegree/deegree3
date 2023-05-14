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
package org.deegree.tile.persistence.remotewmts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.deegree.commons.config.ResourceInitException;
import org.deegree.protocol.wmts.client.WMTSClient;
import org.deegree.tile.DefaultTileDataSet;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.persistence.remotewmts.jaxb.ParameterScopeType;
import org.deegree.tile.persistence.remotewmts.jaxb.ParameterUseType;
import org.deegree.tile.persistence.remotewmts.jaxb.RemoteWMTSTileStoreJAXB;
import org.deegree.tile.persistence.remotewmts.jaxb.RemoteWMTSTileStoreJAXB.TileDataSet.RequestParams;
import org.deegree.tile.persistence.remotewmts.jaxb.RemoteWMTSTileStoreJAXB.TileDataSet.RequestParams.Parameter;
import org.deegree.tile.tilematrixset.TileMatrixSetProvider;
import org.deegree.workspace.Workspace;

/**
 * Builds a tile data set from jaxb.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class TileDataSetBuilder {

	private WMTSClient client;

	private Workspace workspace;

	TileDataSetBuilder(WMTSClient client, Workspace workspace) {
		this.client = client;
		this.workspace = workspace;
	}

	Map<String, TileDataSet> buildTileDataSetMap(RemoteWMTSTileStoreJAXB config) throws ResourceInitException {

		Map<String, TileDataSet> map = new HashMap<String, TileDataSet>();
		for (RemoteWMTSTileStoreJAXB.TileDataSet tileDataSetConfig : config.getTileDataSet()) {
			String tileDataSetId = determineLayerId(tileDataSetConfig);
			String outputFormat = determineOutputFormat(tileDataSetConfig);
			TileDataSet tileDataSet = buildTileDataSet(tileDataSetConfig, outputFormat);
			map.put(tileDataSetId, tileDataSet);
		}
		return map;
	}

	private String determineLayerId(RemoteWMTSTileStoreJAXB.TileDataSet tileDataSetConfig) {
		if (tileDataSetConfig.getIdentifier() != null) {
			return tileDataSetConfig.getIdentifier();
		}
		return tileDataSetConfig.getRequestParams().getLayer();
	}

	private String determineOutputFormat(RemoteWMTSTileStoreJAXB.TileDataSet tileDataSetConfig) {
		String requestedOutputFormat = tileDataSetConfig.getOutputFormat();
		String requestFormat = tileDataSetConfig.getRequestParams().getFormat();
		if (requestedOutputFormat != null) {
			return requestedOutputFormat;
		}
		return requestFormat;
	}

	private TileDataSet buildTileDataSet(RemoteWMTSTileStoreJAXB.TileDataSet tileDataSetConfig, String outputFormat)
			throws ResourceInitException {

		if (outputFormat.startsWith("image/")) {
			outputFormat = outputFormat.substring(6);
		}

		RequestParams requestParams = tileDataSetConfig.getRequestParams();
		String requestTileMatrixSetId = requestParams.getTileMatrixSet();
		String workspaceTileMatrixSetId = tileDataSetConfig.getTileMatrixSetId();
		if (workspaceTileMatrixSetId == null) {
			workspaceTileMatrixSetId = requestTileMatrixSetId;
		}
		TileMatrixSet localTileMatrixSet = getLocalTileMatrixSet(requestTileMatrixSetId, workspaceTileMatrixSetId);
		TileMatrixSet remoteTileMatrixSet = getRemoteTileMatrixSet(requestTileMatrixSetId);

		List<TileDataLevel> dataLevels = buildTileDataLevels(localTileMatrixSet, remoteTileMatrixSet, requestParams,
				outputFormat);
		return new DefaultTileDataSet(dataLevels, localTileMatrixSet, "image/" + outputFormat);
	}

	private TileMatrixSet getLocalTileMatrixSet(String requestTileMatrixSetId, String workspaceTileMatrixSetId)
			throws ResourceInitException {
		String tileMatrixSetId = workspaceTileMatrixSetId;
		if (tileMatrixSetId == null) {
			tileMatrixSetId = requestTileMatrixSetId;
		}

		TileMatrixSet tileMatrixSet = workspace.getResource(TileMatrixSetProvider.class, tileMatrixSetId);
		if (tileMatrixSet == null) {
			String msg = "No local TileMatrixSet definition with identifier '" + tileMatrixSetId + "' available.";
			throw new ResourceInitException(msg);
		}
		return tileMatrixSet;
	}

	private TileMatrixSet getRemoteTileMatrixSet(String tileMatrixSetId) throws ResourceInitException {
		TileMatrixSet tileMatrixSet = null;
		try {
			tileMatrixSet = client.getTileMatrixSet(tileMatrixSetId);
		}
		catch (XMLStreamException e) {
			if (tileMatrixSet == null) {
				String msg = "No remote TileMatrixSet definition with identifier '" + tileMatrixSetId + "' available.";
				throw new ResourceInitException(msg);
			}
		}
		return tileMatrixSet;
	}

	private List<TileDataLevel> buildTileDataLevels(TileMatrixSet localTileMatrixSet, TileMatrixSet remoteTileMatrixSet,
			RequestParams requestParams, String outputFormat) {
		String layer = requestParams.getLayer();
		String style = requestParams.getStyle();
		String format = requestParams.getFormat();
		String remoteTileMatrixSetId = remoteTileMatrixSet.getIdentifier();

		Map<String, String> defaultGetMap = new HashMap<String, String>();
		Map<String, String> hardGetMap = new HashMap<String, String>();
		Map<String, String> defaultGetFeatureInfo = new HashMap<String, String>();
		Map<String, String> hardGetFeatureInfo = new HashMap<String, String>();
		extractParameters(requestParams.getParameter(), defaultGetMap, defaultGetFeatureInfo, hardGetMap,
				hardGetFeatureInfo);

		List<TileDataLevel> dataLevels = new ArrayList<TileDataLevel>();
		List<TileMatrix> localTileMatrices = localTileMatrixSet.getTileMatrices();
		List<TileMatrix> remoteTileMatrices = remoteTileMatrixSet.getTileMatrices();
		int numMatrices = remoteTileMatrices.size();
		if (localTileMatrices.size() < numMatrices) {
			numMatrices = localTileMatrices.size();
		}
		for (int i = 0; i < numMatrices; i++) {
			TileMatrix localTileMatrix = localTileMatrices.get(i);
			TileMatrix remoteTileMatrix = remoteTileMatrices.get(i);
			String remoteTileMatrixId = remoteTileMatrix.getIdentifier();
			TileDataLevel level = buildTileDataLevel(localTileMatrix, remoteTileMatrixSetId, remoteTileMatrixId, layer,
					style, format, outputFormat, defaultGetMap, hardGetMap, defaultGetFeatureInfo, hardGetFeatureInfo);
			dataLevels.add(level);
		}
		return dataLevels;
	}

	private TileDataLevel buildTileDataLevel(TileMatrix tileMatrix, String remoteTileMatrixSetId,
			String remoteTileMatrixId, String layer, String style, String format, String outputFormat,
			Map<String, String> defaultGetMap, Map<String, String> hardGetMap,
			Map<String, String> defaultGetFeatureInfo, Map<String, String> hardGetFeatureInfo) {
		return new RemoteWMTSTileDataLevel(tileMatrix, remoteTileMatrixSetId, remoteTileMatrixId, format, layer, style,
				client, outputFormat, defaultGetMap, hardGetMap, defaultGetFeatureInfo, hardGetFeatureInfo);
	}

	private static void extractParameters(List<Parameter> params, Map<String, String> defaultParametersGetMap,
			Map<String, String> defaultParametersGetFeatureInfo, Map<String, String> hardParametersGetMap,
			Map<String, String> hardParametersGetFeatureInfo) {
		if (params != null && !params.isEmpty()) {
			for (Parameter p : params) {
				String name = p.getName();
				String value = p.getValue();
				ParameterUseType use = p.getUse();
				ParameterScopeType scope = p.getScope();
				switch (use) {
					case ALLOW_OVERRIDE:
						switch (scope) {
							case GET_TILE:
								defaultParametersGetMap.put(name, value);
								break;
							case GET_FEATURE_INFO:
								defaultParametersGetFeatureInfo.put(name, value);
								break;
							default:
								defaultParametersGetMap.put(name, value);
								defaultParametersGetFeatureInfo.put(name, value);
								break;
						}
						break;
					case FIXED:
						switch (scope) {
							case GET_TILE:
								hardParametersGetMap.put(name, value);
								break;
							case GET_FEATURE_INFO:
								hardParametersGetFeatureInfo.put(name, value);
								break;
							default:
								hardParametersGetMap.put(name, value);
								hardParametersGetFeatureInfo.put(name, value);
								break;
						}
						break;
				}
			}
		}
	}

}
