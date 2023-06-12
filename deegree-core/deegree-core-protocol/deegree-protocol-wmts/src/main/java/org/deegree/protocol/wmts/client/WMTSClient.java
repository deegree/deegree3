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
package org.deegree.protocol.wmts.client;

import static org.deegree.protocol.wmts.WMTSConstants.VERSION_100;
import static org.deegree.protocol.wmts.WMTSConstants.WMTS_100_NS;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.deegree.protocol.ows.client.AbstractOWSClient;
import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.deegree.protocol.ows.http.OwsHttpClient;
import org.deegree.protocol.ows.http.OwsHttpResponse;
import org.deegree.protocol.wmts.WMTSConstants;
import org.deegree.protocol.wmts.ops.GetFeatureInfo;
import org.deegree.protocol.wmts.ops.GetTile;
import org.deegree.tile.TileMatrixSet;

/**
 * API-level client for accessing servers that implement the
 * <a href="http://www.opengeospatial.org/standards/wmts">OpenGIS Web Map Tile Service
 * (WMTS) 1.0.0</a> protocol.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public class WMTSClient extends AbstractOWSClient<WMTSCapabilitiesAdapter> {

	private List<Layer> layers;

	private List<TileMatrixSet> tileMatrixSets;

	/**
	 * Creates a new {@link WMTSClient} instance.
	 * @param capaUrl URL of a WMTS capabilities document, usually this is a KVP-encoded
	 * <code>GetCapabilities</code> request to a WMTS service, must not be
	 * <code>null</code>
	 * @param httpClient client for performing HTTP requests, can be <code>null</code>
	 * (use default)
	 * @throws OWSExceptionReport
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public WMTSClient(URL capaUrl, OwsHttpClient httpClient)
			throws OWSExceptionReport, XMLStreamException, IOException {
		super(capaUrl, httpClient);
	}

	/**
	 * Returns metadata about the offered layers.
	 * @return metadata about the offered layers, may be empty, but never
	 * <code>null</code>
	 * @throws XMLStreamException if parsing the <code>wmts:Layer</code> elements in the
	 * capabilities document fails
	 */
	public List<Layer> getLayers() throws XMLStreamException {
		if (layers == null) {
			initLayerInformation();
		}
		return layers;
	}

	/**
	 * Returns metadata on the specified layer.
	 * @param layerId identifier of the layer, must not be <code>null</code>
	 * @return metadata on the offered layers, may be <code>null</code> (no such layer)
	 * @throws XMLStreamException if parsing the <code>wmts:Layer</code> elements in the
	 * capabilities document fails
	 */
	public Layer getLayer(String layerId) throws XMLStreamException {
		List<Layer> layers = getLayers();
		for (Layer layer : layers) {
			if (layer.getIdentifier().equals(layerId)) {
				return layer;
			}
		}
		return null;
	}

	private synchronized void initLayerInformation() throws XMLStreamException {
		layers = capaDoc.parseLayers();
	}

	/**
	 * Returns the {@link TileMatrixSets} known to the server.
	 * @return tile matrix sets, may be empty, but never <code>null</code>
	 * @throws XMLStreamException if parsing the <code>wmts:TileMatrixSet</code> elements
	 * in the capabilities document fails
	 */
	public List<TileMatrixSet> getTileMatrixSets() throws XMLStreamException {
		if (tileMatrixSets == null) {
			initTileMatrixInformation();
		}
		return tileMatrixSets;
	}

	/**
	 * Returns the specified {@link TileMatrixSet}.
	 * @param tileMatrixSetId identifier of the tile matrix set, must not be
	 * <code>null</code>
	 * @return tile matrix set with the specified identifier or <code>null</code> if there
	 * is no such set
	 * @throws XMLStreamException if parsing the <code>wmts:TileMatrixSet</code> elements
	 * in the capabilities document fails
	 */
	public TileMatrixSet getTileMatrixSet(String tileMatrixSetId) throws XMLStreamException {
		List<TileMatrixSet> tileMatrixSets = getTileMatrixSets();
		for (TileMatrixSet tileMatrixSet : tileMatrixSets) {
			if (tileMatrixSet.getIdentifier().equals(tileMatrixSetId)) {
				return tileMatrixSet;
			}
		}
		return null;
	}

	private synchronized void initTileMatrixInformation() throws XMLStreamException {
		tileMatrixSets = capaDoc.parseTileMatrixSets();
	}

	/**
	 * Performs the given {@link GetTile} request.
	 * @param request <code>GetTile</code> requests, must not be <code>null</code>
	 * @return server response, never <code>null</code>
	 * @throws IOException
	 * @throws OWSExceptionReport
	 * @throws XMLStreamException
	 */
	public GetTileResponse getTile(GetTile request) throws IOException, OWSExceptionReport, XMLStreamException {
		Map<String, String> kvp = buildGetTileKvpMap(request);
		if (request.getOverriddenParameters() != null) {
			for (Entry<String, String> e : request.getOverriddenParameters().entrySet()) {
				if (kvp.containsKey(e.getKey().toLowerCase())) {
					kvp.put(e.getKey().toLowerCase(), e.getValue());
				}
				else
					kvp.put(e.getKey(), e.getValue());
			}
		}
		URL endPoint = getGetUrl(WMTSConstants.WMTSRequestType.GetTile.name());
		OwsHttpResponse response = httpClient.doGet(endPoint, kvp, null);
		response.assertHttpStatus200();
		response.assertNoXmlContentTypeAndExceptionReport();
		return new GetTileResponse(response);
	}

	private Map<String, String> buildGetTileKvpMap(GetTile request) {
		Map<String, String> kvp = new LinkedHashMap<String, String>();
		kvp.put("service", "WMTS");
		kvp.put("request", "GetTile");
		kvp.put("version", VERSION_100.toString());
		kvp.put("layer", request.getLayer());
		kvp.put("style", request.getStyle());
		kvp.put("format", request.getFormat());
		kvp.put("tilematrixset", request.getTileMatrixSet());
		kvp.put("tilematrix", request.getTileMatrix());
		kvp.put("tilerow", "" + request.getTileRow());
		kvp.put("tilecol", "" + request.getTileCol());
		return kvp;
	}

	public GetFeatureInfoResponse getFeatureInfo(GetFeatureInfo request)
			throws OWSExceptionReport, XMLStreamException, IOException {
		Map<String, String> kvp = buildGetFeatureInfoKvpMap(request);
		if (request.getOverriddenParameters() != null) {
			for (Entry<String, String> e : request.getOverriddenParameters().entrySet()) {
				if (kvp.containsKey(e.getKey().toLowerCase())) {
					kvp.put(e.getKey().toLowerCase(), e.getValue());
				}
				else
					kvp.put(e.getKey(), e.getValue());
			}
		}
		URL endPoint = getGetUrl(WMTSConstants.WMTSRequestType.GetTile.name());
		OwsHttpResponse response = httpClient.doGet(endPoint, kvp, null);
		response.assertHttpStatus200();
		response.assertNoXmlContentTypeAndExceptionReport();
		return new GetFeatureInfoResponse(response, request);
	}

	private Map<String, String> buildGetFeatureInfoKvpMap(GetFeatureInfo request) {
		Map<String, String> kvp = new LinkedHashMap<String, String>();
		kvp.put("service", "WMTS");
		kvp.put("request", "GetFeatureInfo");
		kvp.put("version", VERSION_100.toString());
		kvp.put("layer", request.getLayer());
		kvp.put("style", request.getStyle());
		kvp.put("info_format", request.getInfoFormat());
		kvp.put("tilematrixset", request.getTileMatrixSet());
		kvp.put("tilematrix", request.getTileMatrix());
		kvp.put("tilerow", Long.toString(request.getTileRow()));
		kvp.put("tilecol", Long.toString(request.getTileCol()));
		kvp.put("i", Integer.toString(request.getI()));
		kvp.put("j", Integer.toString(request.getJ()));
		return kvp;
	}

	@Override
	protected WMTSCapabilitiesAdapter getCapabilitiesAdapter(OMElement root, String version) throws IOException {

		QName rootElName = root.getQName();

		if (!new QName(WMTS_100_NS, "Capabilities").equals(rootElName)) {
			String msg = "Unexpected WMTS GetCapabilities response element: '" + rootElName + "'.";
			throw new IOException(msg);
		}

		WMTSCapabilitiesAdapter capaAdapter = new WMTSCapabilitiesAdapter();
		capaAdapter.setRootElement(root);
		return capaAdapter;
	}

}
