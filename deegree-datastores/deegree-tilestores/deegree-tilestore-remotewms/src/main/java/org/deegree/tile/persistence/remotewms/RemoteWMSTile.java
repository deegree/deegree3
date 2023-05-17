/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

package org.deegree.tile.persistence.remotewms;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.utils.RequestUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.FeatureCollection;
import org.deegree.geometry.Envelope;
import org.deegree.layer.LayerRef;
import org.deegree.protocol.wms.client.WMSClient;
import org.deegree.protocol.wms.ops.GetFeatureInfo;
import org.deegree.protocol.wms.ops.GetMap;
import org.deegree.tile.Tile;
import org.deegree.tile.TileIOException;

/**
 * {@link Tile} implementation used by the {@link RemoteWMSTileDataLevel}.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class RemoteWMSTile implements Tile {

	private final WMSClient client;

	private final GetMap gm;

	private final String outputFormat;

	private Map<String, String> defaultGetFeatureInfo;

	private Map<String, String> hardGetFeatureInfo;

	/**
	 * Creates a new {@link RemoteWMSTile} instance.
	 * @param client client to use for performing the {@link GetMap} request, never
	 * <code>null</code>
	 * @param gm request for retrieving the tile image, never <code>null</code>
	 * @param outputFormat if not null, images will be recoded into specified output
	 * format (use ImageIO like formats, eg. 'png')
	 * @param defaultGetFeatureInfo default parameters for remote GFI requests
	 * @param hardGetFeatureInfo replace parameters for remote GFI requests
	 */
	RemoteWMSTile(WMSClient client, GetMap gm, String outputFormat, Map<String, String> defaultGetFeatureInfo,
			Map<String, String> hardGetFeatureInfo) {
		this.client = client;
		this.gm = gm;
		this.outputFormat = outputFormat;
		this.defaultGetFeatureInfo = defaultGetFeatureInfo;
		this.hardGetFeatureInfo = hardGetFeatureInfo;
	}

	@Override
	public BufferedImage getAsImage() throws TileIOException {
		InputStream in = null;
		try {
			return ImageIO.read(in = getAsStream());
		}
		catch (IOException e) {
			throw new TileIOException("Error decoding image : " + e.getMessage(), e);
		}
		finally {
			IOUtils.closeQuietly(in);
		}
	}

	@Override
	public InputStream getAsStream() throws TileIOException {
		try {
			InputStream map = client.getMap(gm);

			if (map == null) {
				throw new TileIOException("A tile could not be fetched from remote WMS for an unknown reason.");
			}

			if (outputFormat != null) {
				BufferedImage img = ImageIO.read(map);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ImageIO.write(img, outputFormat, out);
				out.close();
				return new ByteArrayInputStream(out.toByteArray());
			}
			return map;
		}
		catch (SocketTimeoutException e) {
			String msg = "Error performing GetMap request, read timed out (timeout configured is "
					+ client.getReadTimeout() + " seconds).";
			throw new TileIOException(msg);
		}
		catch (UnknownHostException e) {
			throw new TileIOException("Error performing GetMap request, host could not be resolved: " + e.getMessage());
		}
		catch (IOException e) {
			throw new TileIOException("Error performing GetMap request: " + e.getMessage(), e);
		}
		catch (OWSException e) {
			throw new TileIOException("Error performing GetMap request: " + e.getMessage(), e);
		}
	}

	@Override
	public Envelope getEnvelope() {
		return gm.getBoundingBox();
	}

	@Override
	public FeatureCollection getFeatures(int i, int j, int limit) {
		FeatureCollection fc = null;
		try {
			List<String> layers = new ArrayList<String>();
			for (LayerRef layerRef : gm.getLayers()) {
				layers.add(layerRef.getName());
			}
			int width = gm.getWidth();
			int height = gm.getHeight();
			Envelope bbox = gm.getBoundingBox();
			ICRS crs = gm.getCoordinateSystem();
			GetFeatureInfo request = new GetFeatureInfo(layers, width, height, i, j, bbox, crs, limit);
			Map<String, String> overriddenParameters = new HashMap<String, String>();
			RequestUtils.replaceParameters(overriddenParameters, RequestUtils.getCurrentThreadRequestParameters().get(),
					defaultGetFeatureInfo, hardGetFeatureInfo);
			fc = client.doGetFeatureInfo(request, overriddenParameters);
		}
		catch (SocketTimeoutException e) {
			String msg = "Error performing GetFeatureInfo request, read timed out (timeout configured is "
					+ client.getReadTimeout() + " seconds).";
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
