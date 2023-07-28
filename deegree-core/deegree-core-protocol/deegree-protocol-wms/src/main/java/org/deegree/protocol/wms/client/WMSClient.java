/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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

package org.deegree.protocol.wms.client;

import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR;
import static java.lang.Math.abs;
import static org.deegree.commons.ows.exception.OWSException.NO_APPLICABLE_CODE;
import static org.deegree.commons.proxy.ProxySettings.getHttpProxyPassword;
import static org.deegree.commons.proxy.ProxySettings.getHttpProxyUser;
import static org.deegree.commons.utils.ArrayUtils.join;
import static org.deegree.commons.utils.kvp.KVPUtils.toQueryString;
import static org.deegree.commons.utils.math.MathUtils.round;
import static org.deegree.commons.utils.net.HttpUtils.IMAGE;
import static org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation.OUTER;
import static org.deegree.coverage.raster.interpolation.InterpolationType.BILINEAR;
import static org.deegree.coverage.raster.utils.RasterFactory.rasterDataFromImage;
import static org.deegree.coverage.raster.utils.RasterFactory.rasterDataToImage;
import static org.deegree.protocol.i18n.Messages.get;
import static org.deegree.protocol.oldwms.WMSConstants.VERSION_111;
import static org.deegree.protocol.oldwms.WMSConstants.VERSION_130;
import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.GetCapabilities;
import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.GetFeatureInfo;
import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.GetMap;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import org.deegree.commons.concurrent.Executor;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.proxy.ProxySettings;
import org.deegree.commons.struct.Tree;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.TunableParameter;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XmlHttpUtils;
import org.deegree.coverage.raster.RasterTransformer;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.cs.components.Axis;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.FeatureCollection;
import org.deegree.featureinfo.parsing.DefaultFeatureInfoParser;
import org.deegree.featureinfo.parsing.FeatureInfoParser;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.layer.LayerRef;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.protocol.ows.client.AbstractOWSClient;
import org.deegree.protocol.ows.exception.OWSExceptionReader;
import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.deegree.protocol.ows.http.OwsHttpClientImpl;
import org.deegree.protocol.ows.http.OwsHttpResponse;
import org.deegree.protocol.wms.WMSConstants.WMSRequestType;
import org.deegree.protocol.wms.ops.GetFeatureInfo;
import org.deegree.protocol.wms.ops.GetMap;
import org.deegree.rendering.r2d.RenderHelper;
import org.deegree.style.StyleRef;
import org.slf4j.Logger;

/**
 * API-level client for accessing servers that implement the
 * <a href="http://www.opengeospatial.org/standards/wms">OpenGIS Web Map Service (WMS)
 * 1.1.1/1.3.0</a> protocol.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class WMSClient extends AbstractOWSClient<WMSCapabilitiesAdapter> {

	// needed in the worker
	static final Logger LOG = getLogger(WMSClient.class);

	// needed in the worker
	int maxMapWidth = -1;

	// needed in the worker
	int maxMapHeight = -1;

	int connectionTimeout = 5;

	int requestTimeout = 60;

	private Version wmsVersion;

	private String httpBasicUser;

	private String httpBasicPass;

	/**
	 * @param url
	 * @param connectionTimeout default is 5 seconds
	 * @param requestTimeout default is 60 seconds
	 * @param user http basic username
	 * @param pass http basic password
	 * @throws XMLStreamException
	 * @throws OWSExceptionReport
	 * @throws IOException
	 */
	public WMSClient(URL url, int connectionTimeout, int requestTimeout, String user, String pass)
			throws IOException, OWSExceptionReport, XMLStreamException {
		super(url, new OwsHttpClientImpl(connectionTimeout * 1000, requestTimeout * 1000, user, pass));
		this.connectionTimeout = connectionTimeout;
		this.requestTimeout = requestTimeout;
		capaDoc.parseWMSSpecificCapabilities(getOperations());
		checkCapabilities();
		this.httpBasicUser = user;
		this.httpBasicPass = pass;
	}

	/**
	 * @param url
	 * @param connectionTimeout default is 5 seconds
	 * @param requestTimeout default is 60 seconds
	 * @throws IOException
	 * @throws XMLStreamException
	 * @throws OWSExceptionReport
	 */
	public WMSClient(URL url, int connectionTimeout, int requestTimeout)
			throws OWSExceptionReport, XMLStreamException, IOException {
		this(url, connectionTimeout, requestTimeout, null, null);
	}

	/**
	 * @param url
	 * @throws IOException
	 * @throws XMLStreamException
	 * @throws OWSExceptionReport
	 */
	public WMSClient(URL url) throws OWSExceptionReport, XMLStreamException, IOException {
		super(url, null);
		capaDoc.parseWMSSpecificCapabilities(getOperations());
		checkCapabilities();
	}

	/**
	 * @param capabilities
	 * @throws IOException
	 * @throws XMLStreamException
	 * @throws OWSExceptionReport
	 */
	public WMSClient(XMLAdapter capabilities) throws IOException, OWSExceptionReport, XMLStreamException {
		super(capabilities, null);
		capaDoc.parseWMSSpecificCapabilities(getOperations());
		checkCapabilities();
	}

	/**
	 * Sets the maximum map size that the server will process. If a larger map is
	 * requested, it will be broken down into multiple GetMap requests.
	 * @param maxWidth maximum number of pixels in x-direction, or -1 for unrestricted
	 * width
	 * @param maxHeight maximum number of pixels in y-direction, or -1 for unrestricted
	 * height
	 */
	public void setMaxMapDimensions(int maxWidth, int maxHeight) {
		maxMapWidth = maxWidth;
		maxMapHeight = maxHeight;
	}

	private void checkCapabilities() {
		List<Version> supportedVersions = getIdentification().getServiceTypeVersion();
		for (Version version : supportedVersions) {
			if (VERSION_111.equals(version) || VERSION_130.equals(version)) {
				return;
			}
		}
		throw new IllegalArgumentException(
				get("WMSCLIENT.WRONG_VERSION_CAPABILITIES", supportedVersions, VERSION_111 + ", " + VERSION_130));
	}

	/**
	 * TODO implement updateSequence handling to improve network performance
	 */
	public void refreshCapabilities() {
		String url = getAddress(GetCapabilities, true);
		url = repairGetUrl(url);
		url += "request=GetCapabilities&version=1.1.1&service=WMS";
		try {
			XMLAdapter adapter;
			if (httpBasicUser != null) {
				adapter = new XMLAdapter();
				adapter.load(new URL(url), httpBasicUser, httpBasicPass);
			}
			else {
				adapter = new XMLAdapter(new URL(url));
			}
			initCapabilities(adapter);
			checkCapabilities();
		}
		catch (MalformedURLException e) {
			LOG.debug("Malformed capabilities URL?", e);
		}
		catch (IOException e) {
			LOG.debug("Malformed capabilities URL?", e);
		}
	}

	private String repairGetUrl(String url) {
		if (!url.endsWith("?") && !url.endsWith("&")) {
			url += url.indexOf("?") == -1 ? "?" : "&";
		}
		return url;
	}

	/**
	 * @param srs
	 * @param layers
	 * @return the merged envelope, or null, if none was found
	 */
	public Envelope getBoundingBox(String srs, List<String> layers) {
		Envelope res = null;

		for (String name : layers) {
			if (res == null) {
				res = getBoundingBox(srs, name);
			}
			else {
				res = res.merge(getBoundingBox(srs, name));
			}
		}

		return res;
	}

	/**
	 * @param hardParameters parameters to override in the request, may be null
	 * @throws IOException
	 */
	public Pair<BufferedImage, String> getMap(GetMap getMap, Map<String, String> hardParameters, int timeout)
			throws IOException {
		return getMap(getMap, hardParameters, timeout, false);
	}

	/**
	 * @param hardParameters parameters to override in the request, may be null
	 * @throws IOException
	 */
	public Pair<BufferedImage, String> getMap(GetMap getMap, Map<String, String> hardParameters, int timeout,
			boolean errorsInImage) throws IOException {
		if (VERSION_111.equals(wmsVersion)) {
			Worker worker = new Worker(getMap.getLayers(), getMap.getStyles(), getMap.getWidth(), getMap.getHeight(),
					getMap.getBoundingBox(), getMap.getCoordinateSystem(), getMap.getFormat(), getMap.getTransparent(),
					errorsInImage, false, null, hardParameters);

			Pair<BufferedImage, String> result;
			try {
				if (timeout == -1) {
					result = worker.call();
				}
				else {
					result = Executor.getInstance().performSynchronously(worker, timeout * 1000);
				}
			}
			catch (Throwable e) {
				throw new IOException(e.getMessage(), e);
			}

			return result;
		}
		throw new IllegalArgumentException("GetMap request for other versions than 1.1.1 are not supported yet.");
	}

	/**
	 * Performs a <code>GetFeatureInfo</code> request and returns the response as a
	 * {@link FeatureCollection}.
	 * @param request request parameter, must not be <code>null</code>
	 * @param hardParams raw parameters for augmenting overriding KVPs, must not be
	 * <code>null</code>
	 * @return response parsed as feature collection, never <code>null</code>
	 * @throws IOException
	 * @throws OWSExceptionReport
	 * @throws XMLStreamException
	 */
	public FeatureCollection doGetFeatureInfo(GetFeatureInfo request, Map<String, String> hardParams)
			throws IOException, OWSExceptionReport, XMLStreamException {
		return doGetFeatureInfo(request, hardParams, new DefaultFeatureInfoParser());
	}

	/**
	 * Performs a <code>GetFeatureInfo</code> request and returns the response as a
	 * {@link FeatureCollection}.
	 * @param request request parameter, must not be <code>null</code>
	 * @param hardParams raw parameters for augmenting overriding KVPs, must not be
	 * <code>null</code>
	 * @param featureInfoParser used to parse the feature info response as feature
	 * collection, never <code>null</code>
	 * @return response parsed as feature collection, never <code>null</code>
	 * @throws IOException
	 * @throws OWSExceptionReport
	 * @throws XMLStreamException
	 */
	public FeatureCollection doGetFeatureInfo(GetFeatureInfo request, Map<String, String> hardParams,
			FeatureInfoParser featureInfoParser) throws IOException, OWSExceptionReport, XMLStreamException {

		Map<String, String> params = buildGetFeatureInfoParamMap(request, hardParams);
		overrideHardParams(params, hardParams);

		OwsHttpResponse response = null;
		InputStream responseStream = null;
		try {
			URL url = getGetUrl(GetFeatureInfo.name());
			response = httpClient.doGet(url, params, null);
			response.assertHttpStatus200();
			responseStream = response.getAsBinaryStream();
			String csvLayerNames = join(",", request.getQueryLayers());
			return featureInfoParser.parseAsFeatureCollection(responseStream, csvLayerNames);
		}
		finally {
			IOUtils.closeQuietly(responseStream);
			closeQuietly(response);
		}
	}

	private Map<String, String> buildGetFeatureInfoParamMap(GetFeatureInfo gfi, Map<String, String> hardParams) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("request", "GetFeatureInfo");
		params.put("version", wmsVersion.toString());
		params.put("service", "WMS");
		String csvLayerNames = join(",", gfi.getQueryLayers());
		params.put("layers", csvLayerNames);
		params.put("query_layers", csvLayerNames);
		params.put("styles", "");
		params.put("width", Integer.toString(gfi.getWidth()));
		params.put("height", Integer.toString(gfi.getHeight()));
		params.put("format", getFormats(GetMap).getFirst());
		params.put("feature_count", Integer.toString(gfi.getFeatureCount()));

		if (wmsVersion.equals(VERSION_111)) {
			params.put("x", Integer.toString(gfi.getX()));
			params.put("y", Integer.toString(gfi.getY()));
			params.put("srs", gfi.getCoordinateSystem().getAlias());
			String fmt = "text/xml";
			LinkedList<String> fmts = getFormats(WMSRequestType.GetFeatureInfo);
			if (fmts.contains("application/vnd.ogc.gml")) {
				fmt = "application/vnd.ogc.gml";
			}
			params.put("info_format", fmt);
			Envelope bbox = gfi.getEnvelope();
			params.put("bbox", bbox.getMin().get0() + "," + bbox.getMin().get1() + "," + bbox.getMax().get0() + ","
					+ bbox.getMax().get1());
		}
		else {
			params.put("i", Integer.toString(gfi.getX()));
			params.put("j", Integer.toString(gfi.getY()));
			params.put("crs", gfi.getCoordinateSystem().getAlias());
			String fmt = "text/xml";
			LinkedList<String> fmts = getFormats(WMSRequestType.GetFeatureInfo);
			if (fmts.contains("application/vnd.ogc.gml")) {
				fmt = "application/vnd.ogc.gml";
			}
			params.put("info_format", fmt);
			Envelope bbox = gfi.getEnvelope();
			if (axisFlipped(bbox.getCoordinateSystem())) {
				params.put("bbox", bbox.getMin().get0() + "," + bbox.getMin().get1() + "," + bbox.getMax().get0() + ","
						+ bbox.getMax().get1());
			}
			else {
				params.put("bbox", bbox.getMin().get1() + "," + bbox.getMin().get0() + "," + bbox.getMax().get1() + ","
						+ bbox.getMax().get0());
			}
		}
		return params;
	}

	private void overrideHardParams(Map<String, String> params, Map<String, String> hardParameters) {
		if (hardParameters != null) {
			for (Entry<String, String> e : hardParameters.entrySet()) {
				if (params.containsKey(e.getKey().toLowerCase())) {
					LOG.debug("Overriding preset parameter {}.", e.getKey());
					params.put(e.getKey().toLowerCase(), e.getValue());
				}
				else
					params.put(e.getKey(), e.getValue());
			}
		}
	}

	// -----------------------------------------------------------------------
	// Callable that does the HTTP communication, so WMSClient111#getMap()
	// can return with a reliable timeout
	// -----------------------------------------------------------------------

	private class Worker implements Callable<Pair<BufferedImage, String>> {

		private List<LayerRef> layers;

		private List<StyleRef> styles;

		private int width;

		private int height;

		private Envelope bbox;

		private ICRS srs;

		private String format;

		private boolean transparent;

		private boolean errorsInImage;

		private boolean validate;

		private List<String> validationErrors;

		private final Map<String, String> hardParameters;

		Worker(List<LayerRef> layers, List<StyleRef> styles, int width, int height, Envelope bbox, ICRS srs,
				String format, boolean transparent, boolean errorsInImage, boolean validate,
				List<String> validationErrors, Map<String, String> hardParameters) {
			this.layers = layers;
			this.styles = styles;
			this.width = width;
			this.height = height;
			this.bbox = bbox;
			this.srs = srs;
			this.format = format;
			this.transparent = transparent;
			this.errorsInImage = errorsInImage;
			this.validate = validate;
			this.validationErrors = validationErrors;
			this.hardParameters = hardParameters;
		}

		@Override
		public Pair<BufferedImage, String> call() throws Exception {
			return getMap(layers, styles, width, height, bbox, srs, format, transparent, errorsInImage, validate,
					validationErrors);
		}

		private Pair<BufferedImage, String> getMap(List<LayerRef> layers, List<StyleRef> styles, int width, int height,
				Envelope bbox, ICRS srs, String format, boolean transparent, boolean errorsInImage, boolean validate,
				List<String> validationErrors) throws IOException {
			if ((maxMapWidth != -1 && width > maxMapWidth) || (maxMapHeight != -1 && height > maxMapHeight)) {
				return getTiledMap(layers, styles, width, height, bbox, srs, format, transparent, errorsInImage,
						validate, validationErrors);
			}

			Pair<BufferedImage, String> res = new Pair<BufferedImage, String>();

			try {
				if (validate) {
					LinkedList<String> formats = getFormats(GetMap);
					if (!formats.contains(format)) {
						format = formats.get(0);
						validationErrors.add("Using format " + format + " instead.");
					}
					// TODO validate srs, width, height, rest, etc
				}

				Envelope reqEnv = bbox;
				int reqWidth = width;
				int reqHeight = height;

				RasterTransformer rtrans = new RasterTransformer(bbox.getCoordinateSystem());
				if (bbox.getCoordinateSystem() != null && !bbox.getCoordinateSystem().equals(srs)) {
					LOG.debug("Transforming bbox {} to {}.", bbox, srs);
					reqEnv = new GeometryTransformer(srs).transform(bbox);

					double scale = RenderHelper.calcScaleWMS111(width, height, bbox, bbox.getCoordinateSystem());
					double newScale = RenderHelper.calcScaleWMS111(width, height, reqEnv, CRSManager.getCRSRef(srs));
					double ratio = scale / newScale;

					reqWidth = abs(round(ratio * width));
					reqHeight = abs(round(ratio * height));
				}

				String url = getAddress(GetMap, true);
				if (url == null) {
					LOG.warn(get("WMSCLIENT.SERVER_NO_GETMAP_URL"), "Capabilities: ", capaDoc);
					return null;
				}
				if (!url.endsWith("?") && !url.endsWith("&")) {
					url += url.indexOf("?") == -1 ? "?" : "&";
				}

				Map<String, String> map = new HashMap<String, String>();
				map.put("request", "GetMap");
				map.put("version", "1.1.1");
				map.put("service", "WMS");
				map.put("layers", join(",", layers));
				String stylesParam = "";
				if (styles != null && !styles.isEmpty()) {
					boolean isFirst = true;
					StringBuilder sb = new StringBuilder();
					for (StyleRef style : styles) {
						if (!isFirst) {
							sb.append(",");
						}
						if (style != null) {
							sb.append(style);
						}
						else {
							sb.append("default");
						}
						isFirst = false;
					}
					stylesParam = sb.toString();
				}
				map.put("styles", stylesParam);
				map.put("width", Integer.toString(reqWidth));
				map.put("height", Integer.toString(reqHeight));
				map.put("bbox", reqEnv.getMin().get0() + "," + reqEnv.getMin().get1() + "," + reqEnv.getMax().get0()
						+ "," + reqEnv.getMax().get1());
				map.put("srs", srs.getAlias());
				map.put("format", format);
				map.put("transparent", Boolean.toString(transparent));
				if (hardParameters != null) {
					for (Entry<String, String> e : hardParameters.entrySet()) {
						if (map.containsKey(e.getKey().toLowerCase())) {
							LOG.debug("Overriding preset parameter {}.", e.getKey());
							map.put(e.getKey().toLowerCase(), e.getValue());
						}
						else
							map.put(e.getKey(), e.getValue());
					}
				}

				if (TunableParameter.get("deegree.protocol.wms.client.fallback", false)) {
					doGetMapUrlConnection(url, map, res);
				}
				else {
					doGetMapHttpClient(url, map, res);
				}

				// hack to ensure correct raster transformations. 4byte_abgr seems to be
				// working best with current api
				if (res.first != null && res.first.getType() != TYPE_4BYTE_ABGR) {
					BufferedImage img = new BufferedImage(res.first.getWidth(), res.first.getHeight(), TYPE_4BYTE_ABGR);
					Graphics2D g = img.createGraphics();
					g.drawImage(res.first, 0, 0, null);
					g.dispose();
					res.first = img;
				}

				if (res.first != null && !reqEnv.getCoordinateSystem().equals(bbox.getCoordinateSystem())) {
					LOG.debug("Performing raster transformation.");
					RasterGeoReference env = RasterGeoReference.create(OUTER, reqEnv, reqWidth, reqHeight);
					RasterData data = rasterDataFromImage(res.first);
					SimpleRaster raster = new SimpleRaster(data, reqEnv, env, null);

					SimpleRaster transformed = rtrans.transform(raster, bbox, width, height, BILINEAR)
						.getAsSimpleRaster();

					res.first = rasterDataToImage(transformed.getRasterData());
				}

				LOG.debug("Received response.");
			}
			catch (Throwable e) {
				LOG.info("Error performing GetMap request: " + e.getMessage());
				LOG.trace("Stack trace:", e);
				res.second = e.getMessage();
			}

			if (errorsInImage && res.first == null) {
				// TODO create image of type RGBA / RGB
				res.first = createErrorImage(res.second, width, height,
						transparent ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR);
				res.second = null;
			}

			if (LOG.isDebugEnabled() && res.first != null) {
				File tmpFile = File.createTempFile("WMSClient", ".png");
				ImageIO.write(res.first, "png", tmpFile);
			}

			return res;
		}

		private void doGetMapHttpClient(String url, Map<String, String> map, Pair<BufferedImage, String> res)
				throws Exception {
			OwsHttpResponse response = null;
			try {
				LOG.trace("Requesting from {}", url);
				response = httpClient.doGet(new URL(url), map, null);
				response.assertHttpStatus200();
				ContentType responseContentType = ContentType.getLenient(response.getAsHttpResponse().getEntity());
				String responseMimeType = responseContentType != null ? responseContentType.getMimeType() : null;
				LOG.trace("Content type is {}", responseMimeType);

				if (responseMimeType != null && responseMimeType.startsWith(format)) {
					try (InputStream is = response.getAsBinaryStream()) {
						res.first = IMAGE.work(is);
					}
				}
				else if (responseMimeType != null && responseMimeType.startsWith("application/vnd.ogc.se_xml")) {
					try (InputStream is = response.getAsBinaryStream()) {
						res.second = XmlHttpUtils.XML.work(is).toString();
					}
				}
				else { // try and find out the hard way
					try (InputStream is = response.getAsBinaryStream()) {
						res.first = IMAGE.work(is);
					}
					if (res.first == null) {
						closeQuietly(response);
						response = httpClient.doGet(new URL(url), map, null);
						response.assertHttpStatus200();
						try (InputStream is = response.getAsBinaryStream()) {
							res.second = XmlHttpUtils.XML.work(is).toString();
						}
					}
				}
			}
			finally {
				closeQuietly(response);
			}
		}

		private void doGetMapUrlConnection(String url, Map<String, String> map, Pair<BufferedImage, String> res)
				throws Exception {
			url += toQueryString(map);
			URL theUrl = new URL(url);
			LOG.debug("Connecting to URL " + theUrl);
			URLConnection conn = ProxySettings.openURLConnection(theUrl, ProxySettings.getHttpProxyUser(true),
					ProxySettings.getHttpProxyPassword(true), httpBasicUser, httpBasicPass);
			conn.setConnectTimeout(connectionTimeout * 1000);
			conn.setReadTimeout(requestTimeout * 1000);
			conn.connect();
			LOG.debug("Connected.");
			if (LOG.isTraceEnabled()) {
				LOG.trace("Requesting from " + theUrl);
				LOG.trace("Content type is " + conn.getContentType());
				LOG.trace("Content encoding is " + conn.getContentEncoding());
			}
			if (conn.getContentType() != null && conn.getContentType().startsWith(format)) {
				res.first = IMAGE.work(conn.getInputStream());
			}
			else if (conn.getContentType() != null && conn.getContentType().startsWith("application/vnd.ogc.se_xml")) {
				res.second = XmlHttpUtils.XML.work(conn.getInputStream()).toString();
			}
			else { // try and find out the hard way
				res.first = IMAGE.work(conn.getInputStream());
				if (res.first == null) {
					conn = theUrl.openConnection();
					res.second = XmlHttpUtils.XML.work(conn.getInputStream()).toString();
				}
			}
		}

		private BufferedImage createErrorImage(String error, int width, int height, int type) {

			BufferedImage result = new BufferedImage(width, height, type);
			Graphics2D g = (Graphics2D) result.getGraphics();
			// TODO use optimized coordinates and font size
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, width - 1, height - 1);
			g.setColor(Color.BLACK);
			g.drawString("Error: " + error, 0, 12);
			return result;

		}

		// TODO handle axis direction and order correctly, depends on srs
		private Pair<BufferedImage, String> getTiledMap(List<LayerRef> layers, List<StyleRef> styles, int width,
				int height, Envelope bbox, ICRS srs, String format, boolean transparent, boolean errorsInImage,
				boolean validate, List<String> validationErrors) throws IOException {

			Pair<BufferedImage, String> response = new Pair<BufferedImage, String>();
			BufferedImage compositedImage = null;
			if (transparent) {
				// TODO create image of type RGBA
				compositedImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			}
			else {
				// TODO create image of type RGB
				compositedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
			}

			response.first = compositedImage;

			RasterGeoReference rasterEnv = RasterGeoReference.create(OriginLocation.OUTER, bbox, width, height);

			if (maxMapWidth != -1) {
				int xMin = 0;
				while (xMin <= width - 1) {
					int xMax = xMin + maxMapWidth - 1;
					if (xMax > width - 1) {
						xMax = width - 1;
					}
					if (maxMapHeight != -1) {
						int yMin = 0;
						while (yMin <= height - 1) {
							int yMax = yMin + maxMapHeight - 1;
							if (yMax > height - 1) {
								yMax = height - 1;
							}
							getAndSetSubImage(compositedImage, layers, xMin, (xMax - xMin) + 1, yMin, (yMax - yMin) + 1,
									rasterEnv, srs, format, transparent, errorsInImage);
							yMin = yMax + 1;
						}
					}
					xMin = xMax + 1;
				}
			}
			else {
				if (maxMapHeight != -1) {
					int yMin = 0;
					while (yMin <= height - 1) {
						int yMax = yMin + maxMapHeight - 1;
						if (yMax > height - 1) {
							yMax = height - 1;
						}
						int xMin = 0;
						int xMax = width - 1;
						getAndSetSubImage(compositedImage, layers, xMin, (xMax - xMin) + 1, yMin, (yMax - yMin) + 1,
								rasterEnv, srs, format, transparent, errorsInImage);
						yMin = yMax + 1;
					}
				}
			}
			return response;
		}

		private void getAndSetSubImage(BufferedImage targetImage, List<LayerRef> layers, int xMin, int width, int yMin,
				int height, RasterGeoReference rasterEnv, ICRS crs, String format, boolean transparent,
				boolean errorsInImage) throws IOException {

			double[] min = rasterEnv.getWorldCoordinate(xMin, yMin + height);
			double[] max = rasterEnv.getWorldCoordinate(xMin + width, yMin);

			Envelope env = new GeometryFactory().createEnvelope(min, max, crs);
			Pair<BufferedImage, String> response = getMap(layers, styles, width, height, env, crs, format, transparent,
					errorsInImage, false, null);
			if (response.second != null) {
				throw new IOException(response.second);
			}
			targetImage.getGraphics().drawImage(response.first, xMin, yMin, null);
		}

	}

	public InputStream getMap(GetMap getMap) throws IOException, OWSException {
		Map<String, String> map = new HashMap<String, String>();
		map.put("request", "GetMap");
		map.put("version", wmsVersion.toString());
		map.put("service", "WMS");
		map.put("layers", join(",", getMap.getLayers()));
		map.put("styles", "");
		LinkedList<StyleRef> styles = new LinkedList<StyleRef>(getMap.getStyles());
		if (styles.size() > 0) {
			while (styles.size() < getMap.getLayers().size()) {
				styles.add(new StyleRef("default"));
			}
			map.put("styles", join(",", styles));
		}
		map.put("width", Integer.toString(getMap.getWidth()));
		map.put("height", Integer.toString(getMap.getHeight()));
		map.put("transparent", "true");
		Envelope bbox = getMap.getBoundingBox();
		if (axisFlipped(bbox.getCoordinateSystem())) {
			map.put("bbox", bbox.getMin().get0() + "," + bbox.getMin().get1() + "," + bbox.getMax().get0() + ","
					+ bbox.getMax().get1());
		}
		else {
			map.put("bbox", bbox.getMin().get1() + "," + bbox.getMin().get0() + "," + bbox.getMax().get1() + ","
					+ bbox.getMax().get0());
		}
		if (wmsVersion.equals(VERSION_111)) {
			map.put("srs", getMap.getCoordinateSystem().getAlias());
		}
		else {
			map.put("crs", getMap.getCoordinateSystem().getAlias());
		}
		map.put("format", getMap.getFormat());

		if (getMap.getOverriddenParameters() != null) {
			for (Entry<String, String> e : getMap.getOverriddenParameters().entrySet()) {
				if (map.containsKey(e.getKey().toLowerCase())) {
					LOG.debug("Overriding preset parameter {}.", e.getKey());
					map.put(e.getKey().toLowerCase(), e.getValue());
				}
				else
					map.put(e.getKey(), e.getValue());
			}
		}

		String url = getAddress(GetMap, true);
		if (url == null) {
			LOG.warn(get("WMSCLIENT.SERVER_NO_GETMAP_URL"), "Capabilities: ", capaDoc);
			return null;
		}
		url = repairGetUrl(url);
		String query = url + toQueryString(map);

		URL theUrl = new URL(query);
		LOG.debug("Connecting to URL " + theUrl);
		URLConnection conn = ProxySettings.openURLConnection(theUrl, getHttpProxyUser(true), getHttpProxyPassword(true),
				httpBasicUser, httpBasicPass);
		conn.setConnectTimeout(connectionTimeout * 1000);
		conn.setReadTimeout(requestTimeout * 1000);
		conn.connect();
		LOG.debug("Connected.");

		String fld = conn.getHeaderField("Content-Type");
		if (fld != null && !(fld.startsWith(getMap.getFormat()) || fld.startsWith("image"))) {
			XMLInputFactory fac = XMLInputFactory.newInstance();
			try {
				OWSExceptionReport rep = OWSExceptionReader
					.parseExceptionReport(fac.createXMLStreamReader(conn.getInputStream()));
				throw rep.getExceptions().get(0);
			}
			catch (Throwable e) {
				throw new OWSException(e.getMessage(), e, NO_APPLICABLE_CODE);
			}
		}

		return conn.getInputStream();
	}

	private boolean axisFlipped(ICRS crs) {
		if (crs.getAlias().startsWith("EPSG:")) {
			crs = CRSManager.getCRSRef("urn:ogc:def:crs:EPSG::" + crs.getAlias().substring(5));
		}
		return wmsVersion.equals(VERSION_111) || crs.getAxis()[0].getOrientation() == Axis.AO_EAST;
	}

	protected WMSCapabilitiesAdapter getCapabilitiesAdapter(OMElement root, String version) throws IOException {
		if (version != null) {
			wmsVersion = Version.parseVersion(version);
		}
		else {
			LOG.warn("No version attribute in WMS capabilities document. Defaulting to 1.1.1.");
			wmsVersion = VERSION_111;
		}
		if (VERSION_111.equals(wmsVersion)) {
			return new WMS111CapabilitiesAdapter(root);
		}
		else if (VERSION_130.equals(wmsVersion)) {
			return new WMS130CapabilitiesAdapter(root);
		}
		throw new IllegalArgumentException(get("WMSCLIENT.WRONG_VERSION_CAPABILITIES",
				getIdentification().getServiceTypeVersion(), VERSION_111 + ", " + VERSION_130));
	}

	public boolean isOperationSupported(WMSRequestType request) {
		return capaDoc.isOperationSupported(request);
	}

	public LinkedList<String> getFormats(WMSRequestType request) {
		return capaDoc.getFormats(request);
	}

	public String getAddress(WMSRequestType request, boolean get) {
		return capaDoc.getAddress(request, get);
	}

	public boolean hasLayer(String name) {
		return capaDoc.hasLayer(name);
	}

	public LinkedList<String> getCoordinateSystems(String name) {
		return capaDoc.getCoordinateSystems(name);
	}

	public Envelope getLatLonBoundingBox(String layer) {
		return capaDoc.getLatLonBoundingBox(layer);
	}

	public Envelope getLatLonBoundingBox(List<String> layers) {
		return capaDoc.getLatLonBoundingBox(layers);
	}

	public Envelope getBoundingBox(String srs, String layer) {
		return capaDoc.getBoundingBox(srs, layer);
	}

	public List<String> getNamedLayers() {
		return capaDoc.getNamedLayers();
	}

	public Tree<LayerMetadata> getLayerTree() {
		return capaDoc.getLayerTree();
	}

	/**
	 * @param prefix of the element containging the extended capabilities, may be
	 * <code>null</code>
	 * @param localName localName of the element containing the extended capabilities,
	 * never <code>null</code>
	 * @param namespaceUri of the element containging the extended capabilities, may be
	 * <code>null</code>
	 * @return the {@link OMElement} containing the extended capabilities, may be
	 * <code>null</code> if no extended capabilities exists
	 */
	public OMElement getExtendedCapabilities(String prefix, String localName, String namespaceUri) {
		return capaDoc.getExtendedCapabilities(prefix, localName, namespaceUri);
	}

	public int getConnectTimeout() {
		return connectionTimeout;
	}

	public int getReadTimeout() {
		return requestTimeout;
	}

}
