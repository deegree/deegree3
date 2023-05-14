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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.tile.persistence.remotewmts;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.deegree.commons.ows.exception.OWSException.OPERATION_NOT_SUPPORTED;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.ows.metadata.operation.Operation;
import org.deegree.commons.utils.RequestUtils;
import org.deegree.feature.FeatureCollection;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.ows.http.CloseRequiredInputStream;
import org.deegree.protocol.wmts.client.GetTileResponse;
import org.deegree.protocol.wmts.client.Layer;
import org.deegree.protocol.wmts.client.WMTSClient;
import org.deegree.protocol.wmts.ops.GetFeatureInfo;
import org.deegree.protocol.wmts.ops.GetTile;
import org.deegree.tile.Tile;
import org.deegree.tile.TileIOException;
import org.slf4j.Logger;

/**
 * {@link Tile} implementation for {@link RemoteWMTSTileDataLevel}.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
class RemoteWMTSTile implements Tile {

	private static final Logger LOG = getLogger(RemoteWMTSTile.class);

	private final WMTSClient client;

	private final GetTile request;

	private final String recodedOutputFormat;

	private final Envelope envelope;

	private Map<String, String> defaultGetFeatureInfo;

	private Map<String, String> hardGetFeatureInfo;

	/**
	 * Creates a new {@link RemoteWMTSTile} instance.
	 * @param client client to use for performing the {@link GetTile} request, must not be
	 * <code>null</code>
	 * @param request request for retrieving the tile image, must not be <code>null</code>
	 * @param recodedOutputFormat if not <code>null</code>, images will be recoded into
	 * specified output format (use ImageIO like formats, eg. 'png')
	 * @param envelope
	 * @param defaultGetFeatureInfo default parameters to be filled in for GFI
	 * @param hardGetFeatureInfo parameters to be overridden for GFI
	 */
	RemoteWMTSTile(WMTSClient client, GetTile request, String recodedOutputFormat, Envelope envelope,
			Map<String, String> defaultGetFeatureInfo, Map<String, String> hardGetFeatureInfo) {
		this.client = client;
		this.request = request;
		this.recodedOutputFormat = recodedOutputFormat;
		this.envelope = envelope;
		this.defaultGetFeatureInfo = defaultGetFeatureInfo;
		this.hardGetFeatureInfo = hardGetFeatureInfo;
	}

	@Override
	public BufferedImage getAsImage() throws TileIOException {

		CloseRequiredInputStream is = getNativeFormatRemoteStream();
		try {
			return ImageIO.read(is);
		}
		catch (IOException e) {
			throw new TileIOException("Error decoding remote WMTS response as image : " + e.getMessage(), e);
		}
		finally {
			closeQuietly(is);
		}
	}

	@Override
	public CloseRequiredInputStream getAsStream() throws TileIOException {
		if (recodedOutputFormat == null) {
			return getNativeFormatRemoteStream();
		}
		return getRecodedImageStream();
	}

	private CloseRequiredInputStream getNativeFormatRemoteStream() throws TileIOException {

		CloseRequiredInputStream is = null;
		try {
			GetTileResponse response = client.getTile(request);
			is = response.getAsRawResponse().getAsBinaryStream();
		}
		catch (Exception e) {
			throw new TileIOException(e.getMessage(), e);
		}
		return is;
	}

	private CloseRequiredInputStream getRecodedImageStream() throws TileIOException {
		BufferedImage img = getAsImage();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ImageIO.write(img, recodedOutputFormat, out);
			out.close();
		}
		catch (IOException e) {
			throw new TileIOException("Error recoding remote WMTS tile image: " + e.getMessage(), e);
		}
		return new CloseRequiredInputStream(null, new ByteArrayInputStream(out.toByteArray()));
	}

	@Override
	public Envelope getEnvelope() {
		return envelope;
	}

	@Override
	public FeatureCollection getFeatures(int i, int j, int limit) {
		FeatureCollection fc = null;
		try {
			Map<String, String> overriddenParameters = new HashMap<String, String>();
			RequestUtils.replaceParameters(overriddenParameters, RequestUtils.getCurrentThreadRequestParameters().get(),
					defaultGetFeatureInfo, hardGetFeatureInfo);
			Operation op = client.getOperations().getOperation("GetFeatureInfo");
			if (op == null) {
				throw new OWSException("The remote WMTS claims not to support GetFeatureInfo.",
						OPERATION_NOT_SUPPORTED);
			}
			Layer l = client.getLayer(this.request.getLayer());
			String infoformat = null;
			for (String fmt : l.getInfoFormats()) {
				if (fmt.startsWith("application/gml+xml")) {
					// use first gml format found
					infoformat = fmt;
					break;
				}
				if (fmt.startsWith("text/xml")) {
					infoformat = fmt;
					// continue, perhaps a proper gml format is found later on
				}
			}
			if (infoformat == null) {
				throw new OWSException("The remote WMTS does not offer a GML or XML format for this layer.",
						OPERATION_NOT_SUPPORTED);
			}
			LOG.debug("Selected {} as info format for GFI request.");
			GetFeatureInfo request = new GetFeatureInfo(this.request.getLayer(), this.request.getStyle(), infoformat,
					this.request.getTileMatrixSet(), this.request.getTileMatrix(), this.request.getTileRow(),
					this.request.getTileCol(), i, j, overriddenParameters);
			fc = client.getFeatureInfo(request).getFeatures();
		}
		catch (SocketTimeoutException e) {
			String msg = "Error performing GetFeatureInfo request, read timed out.";
			throw new TileIOException(msg);
		}
		catch (UnknownHostException e) {
			throw new TileIOException(
					"Error performing GetFeatureInfo request, host could not be resolved: " + e.getMessage());
		}
		catch (Exception e) {
			String msg = "Error executing GetFeatureInfo request on remote server: " + e.getMessage();
			throw new RuntimeException(msg, e);
		}
		return fc;
	}

}
