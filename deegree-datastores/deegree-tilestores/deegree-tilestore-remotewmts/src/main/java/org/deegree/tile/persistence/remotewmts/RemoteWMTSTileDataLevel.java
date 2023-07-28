/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.tile.persistence.remotewmts;

import static org.deegree.tile.Tiles.calcTileEnvelope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.commons.utils.RequestUtils;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.wmts.client.WMTSClient;
import org.deegree.protocol.wmts.ops.GetTile;
import org.deegree.tile.Tile;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileMatrix;

/**
 * {@link TileDataLevel} that is backed by a remote WMTS instance.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
class RemoteWMTSTileDataLevel implements TileDataLevel {

	private final TileMatrix matrix;

	private final String remoteTileMatrixSetId;

	private final String remoteTileMatrixId;

	private final String requestFormat;

	private final String layer;

	private final String style;

	private final WMTSClient client;

	private final String outputFormat;

	private Map<String, String> defaultGetMap, hardGetMap, defaultGetFeatureInfo, hardGetFeatureInfo;

	/**
	 * Creates a new {@link RemoteWMTSTileDataLevel} instance.
	 * @param matrix tile matrix, must not be <code>null</code>
	 * @param remoteTileMatrixSetId tile matrix set identifier on the remote server, must
	 * not be <code>null</code>
	 * @param remoteTileMatrixId tile matrix identifier on the remote server, must not be
	 * <code>null</code>
	 * @param requestFormat format to use for requesting tile images, must not be
	 * <code>null</code>
	 * @param layer WMTS layer to request, must not be <code>null</code>
	 * @param style WMTS style to request, must not be <code>null</code>
	 * @param client WMTS client to use, must not be <code>null</code>
	 * @param outputFormat if not <code>null</code>, images will be recoded into specified
	 * output format (use ImageIO like formats, eg. 'png')
	 * @param hardGetMap
	 * @param defaultGetMap
	 */
	RemoteWMTSTileDataLevel(TileMatrix matrix, String remoteTileMatrixSetId, String remoteTileMatrixId,
			String requestFormat, String layer, String style, WMTSClient client, String outputFormat,
			Map<String, String> defaultGetMap, Map<String, String> hardGetMap,
			Map<String, String> defaultGetFeatureInfo, Map<String, String> hardGetFeatureInfo) {
		this.matrix = matrix;
		this.remoteTileMatrixSetId = remoteTileMatrixSetId;
		this.remoteTileMatrixId = remoteTileMatrixId;
		this.requestFormat = requestFormat;
		this.layer = layer;
		this.style = style;
		this.outputFormat = outputFormat;
		this.client = client;
		this.defaultGetMap = defaultGetMap;
		this.hardGetMap = hardGetMap;
		this.defaultGetFeatureInfo = defaultGetFeatureInfo;
		this.hardGetFeatureInfo = hardGetFeatureInfo;
	}

	@Override
	public TileMatrix getMetadata() {
		return matrix;
	}

	@Override
	public Tile getTile(long x, long y) {
		if (matrix.getNumTilesX() <= x || matrix.getNumTilesY() <= y || x < 0 || y < 0) {
			return null;
		}
		Envelope tileEnvelope = calcTileEnvelope(matrix, x, y);
		Map<String, String> overriddenParameters = new HashMap<String, String>();
		RequestUtils.replaceParameters(overriddenParameters, RequestUtils.getCurrentThreadRequestParameters().get(),
				defaultGetMap, hardGetMap);
		GetTile request = new GetTile(layer, style, requestFormat, remoteTileMatrixSetId, remoteTileMatrixId, x, y,
				overriddenParameters);
		return new RemoteWMTSTile(client, request, outputFormat, tileEnvelope, defaultGetFeatureInfo,
				hardGetFeatureInfo);
	}

	@Override
	public List<String> getStyles() {
		List<String> styles = new ArrayList<String>();
		styles.add(style);
		return styles;
	}

}
