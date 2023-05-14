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

package org.deegree.commons.utils.net;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;
import static java.net.URLEncoder.encode;
import static java.util.Arrays.asList;
import static javax.imageio.ImageIO.read;
import static org.deegree.commons.utils.ArrayUtils.join;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.deegree.commons.utils.Pair;
import org.slf4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * <code>HttpUtils</code>
 *
 * Example use from rhino:
 *
 * <code>
 * var u = org.deegree.commons.utils.net.HttpUtils
 * u.retrieve(u.UTF8STRING, "http://demo.deegree.org/deegree-wms/services?request=capabilities&amp;service=WMS")
 * </code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class HttpUtils {

	private static final Logger LOG = getLogger(HttpUtils.class);

	private static final int DEFAULT_CONN_TIMEOUT = 10 * 1000;

	private static final int DEFAULT_SOCKET_TIMEOUT = 60 * 1000;

	/**
	 * <code>Worker</code> is used to specify how to return the stream from the remote
	 * location.
	 *
	 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
	 * @param <T>
	 */
	public interface Worker<T> {

		/**
		 * @param in
		 * @return some object created from the input stream
		 * @throws IOException
		 */
		T work(InputStream in) throws IOException;

	}

	/**
	 * Directly returns the stream.
	 */
	public static final Worker<InputStream> STREAM = new Worker<InputStream>() {
		@Override
		public InputStream work(InputStream in) {
			return in;
		}
	};

	/**
	 * Returns a decoded String.
	 */
	public static final Worker<String> UTF8STRING = getStringWorker("UTF-8");

	public static final Worker<JsonElement> JSON = new Worker<JsonElement>() {
		@Override
		public JsonElement work(InputStream in) throws IOException {
			return new JsonParser().parse(new InputStreamReader(in, "UTF-8"));
		}
	};

	/**
	 * Returns a BufferedImage.
	 */
	public static final Worker<BufferedImage> IMAGE = new Worker<BufferedImage>() {
		@Override
		public BufferedImage work(InputStream in) throws IOException {
			return read(in);
		}
	};

	/**
	 * @param encoding
	 * @return a string producer for a specific encoding
	 */
	public static Worker<String> getStringWorker(final String encoding) {
		return new Worker<String>() {
			@Override
			public String work(InputStream in) throws IOException {
				BufferedReader bin = new BufferedReader(new InputStreamReader(in, encoding));
				StringBuilder b = new StringBuilder();
				String str;
				while ((str = bin.readLine()) != null) {
					b.append(str).append("\n");
				}
				bin.close();
				return b.toString();
			}
		};
	}

	/**
	 * @param <T>
	 * @param worker
	 * @param url must be valid
	 * @return some object from the url
	 * @throws IOException
	 */
	public static <T> T retrieve(Worker<T> worker, DURL url) throws IOException {
		return worker.work(url.openStream());
	}

	/**
	 * @param <T>
	 * @param worker
	 * @param url
	 * @return some object from the url
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static <T> T retrieve(Worker<T> worker, String url) throws MalformedURLException, IOException {
		return retrieve(worker, new DURL(url));
	}

	/**
	 * @param <T>
	 * @param worker
	 * @param url
	 * @param map
	 * @return some object from the url, null, if url is not valid
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public static <T> T retrieve(Worker<T> worker, String url, Map<String, String> map)
			throws MalformedURLException, IOException {
		if (!url.endsWith("?") && !url.endsWith("&")) {
			url += url.indexOf("?") == -1 ? "?" : "&";
		}
		LinkedList<String> list = new LinkedList<String>();
		for (Entry<String, String> e : map.entrySet()) {
			list.add(encode(e.getKey(), "UTF-8") + "=" + encode(e.getValue(), "UTF-8"));
		}
		url += join("&", list);
		return retrieve(worker, url);
	}

	/**
	 * Performs an HTTP-Get request and provides typed access to the response.
	 * @param <T>
	 * @param worker
	 * @param url
	 * @param postBody
	 * @param headers may be null
	 * @return some object from the url
	 * @throws IOException
	 */
	public static <T> T post(Worker<T> worker, String url, InputStream postBody, Map<String, String> headers)
			throws IOException {
		DURL u = new DURL(url);
		DefaultHttpClient client = enableProxyUsage(new DefaultHttpClient(), u);
		HttpPost post = new HttpPost(url);
		post.setEntity(new InputStreamEntity(postBody, -1));
		if (headers != null) {
			for (String key : headers.keySet()) {
				post.addHeader(key, headers.get(key));
			}
		}
		return worker.work(client.execute(post).getEntity().getContent());
	}

	/**
	 * @param <T>
	 * @param worker
	 * @param url
	 * @param params KVP parameter map to set in the post body
	 * @param headers
	 * @return some object from the url
	 * @throws IOException
	 */
	public static <T> T post(Worker<T> worker, String url, Map<String, String> params, Map<String, String> headers,
			final int readTimeout) throws IOException {
		DURL u = new DURL(url);
		LOG.debug("Sending HTTP POST against {}", url);
		DefaultHttpClient client = enableProxyUsage(new DefaultHttpClient(), u);
		client.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy() {
			@Override
			public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
				long keepAlive = super.getKeepAliveDuration(response, context);
				if (keepAlive == -1) {
					keepAlive = readTimeout * 1000;
				}
				return keepAlive;
			}
		});
		HttpPost post = new HttpPost(url);
		List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>(params.size());
		for (Entry<String, String> e : params.entrySet()) {
			list.add(new BasicNameValuePair(e.getKey(), e.getValue()));
		}

		post.setEntity(new UrlEncodedFormEntity(list, "UTF-8"));
		if (headers != null) {
			for (String key : headers.keySet()) {
				post.addHeader(key, headers.get(key));
			}
		}
		HttpEntity entity = client.execute(post).getEntity();
		LOG.debug("Received response with content type {}", entity.getContentType());
		return worker.work(entity.getContent());
	}

	/**
	 * @param <T>
	 * @param worker
	 * @param url
	 * @param params KVP parameter map to set in the post body
	 * @param headers
	 * @return some object from the url and the http response object for complete
	 * inspection
	 * @throws IOException
	 */
	public static <T> Pair<T, HttpResponse> postFullResponse(Worker<T> worker, String url, Map<String, String> params,
			Map<String, String> headers, final int readTimeout) throws IOException {
		DURL u = new DURL(url);
		LOG.debug("Sending HTTP POST against {}", url);
		DefaultHttpClient client = enableProxyUsage(new DefaultHttpClient(), u);
		client.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy() {
			@Override
			public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
				long keepAlive = super.getKeepAliveDuration(response, context);
				if (keepAlive == -1) {
					keepAlive = readTimeout * 1000;
				}
				return keepAlive;
			}
		});
		HttpPost post = new HttpPost(url);
		List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>(params.size());
		for (Entry<String, String> e : params.entrySet()) {
			list.add(new BasicNameValuePair(e.getKey(), e.getValue()));
		}

		post.setEntity(new UrlEncodedFormEntity(list, "UTF-8"));
		if (headers != null) {
			for (String key : headers.keySet()) {
				post.addHeader(key, headers.get(key));
			}
		}
		HttpResponse resp = client.execute(post);
		HttpEntity entity = resp.getEntity();
		LOG.debug("Received response with content type {}", entity.getContentType());
		return new Pair<T, HttpResponse>(worker.work(entity.getContent()), resp);
	}

	private static void authenticate(DefaultHttpClient client, String user, String pass, DURL u) {
		client.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, pass));
		// preemptive authentication used to be easier in pre-4.x httpclient
		AuthCache authCache = new BasicAuthCache();
		BasicScheme basicAuth = new BasicScheme();
		HttpHost host = new HttpHost(u.getURL().getHost(), u.getURL().getPort());
		authCache.put(host, basicAuth);
		BasicHttpContext localcontext = new BasicHttpContext();
		localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
	}

	/**
	 * Performs an HTTP-Get request and provides typed access to the response.
	 * @param <T>
	 * @param worker
	 * @param url
	 * @param postBody
	 * @param headers may be null
	 * @param user optional username for HTTP basic authentication
	 * @param pass optional password for HTTP basic authentication
	 * @return some object from the url
	 * @throws IOException
	 */
	public static <T> T post(Worker<T> worker, String url, File postBody, Map<String, String> headers, String user,
			String pass) throws IOException {
		DURL u = new DURL(url);
		DefaultHttpClient client = enableProxyUsage(new DefaultHttpClient(), u);
		HttpPost post = new HttpPost(url);
		if (user != null && pass != null) {
			authenticate(client, user, pass, u);
		}
		post.setEntity(new FileEntity(postBody, (ContentType) null));
		if (headers != null) {
			for (String key : headers.keySet()) {
				post.addHeader(key, headers.get(key));
			}
		}
		return worker.work(client.execute(post).getEntity().getContent());
	}

	/**
	 * Performs an HTTP-Get request and provides typed access to the response.
	 * @param <T>
	 * @param worker
	 * @param url
	 * @param headers may be null
	 * @return some object from the url, null, if url is not valid
	 * @throws IOException
	 */
	public static <T> T get(Worker<T> worker, String url, Map<String, String> headers) throws IOException {
		return get(worker, url, headers, null, null);
	}

	/**
	 * Performs an HTTP-Get request and provides typed access to the response.
	 * @param <T>
	 * @param worker
	 * @param url
	 * @param headers may be null
	 * @param user optional username for HTTP basic authentication
	 * @param pass optional password for HTTP basic authentication
	 * @return some object from the url, null, if url is not valid
	 * @throws IOException
	 */
	public static <T> T get(Worker<T> worker, String url, Map<String, String> headers, String user, String pass)
			throws IOException {
		DURL u = new DURL(url);
		if (!u.valid()) {
			return null;
		}
		final String protocol = u.getURL().getProtocol();
		if (!("http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol))) {
			return worker.work(u.getURL().openStream());
		}
		DefaultHttpClient client = enableProxyUsage(new DefaultHttpClient(), u);
		if (user != null && pass != null) {
			authenticate(client, user, pass, u);
		}
		HttpGet get = new HttpGet(url);
		if (headers != null) {
			for (String key : headers.keySet()) {
				get.addHeader(key, headers.get(key));
			}
		}

		return worker.work(client.execute(get).getEntity().getContent());
	}

	/**
	 * Performs an HTTP-Get request and provides typed access to the response.
	 * @param <T>
	 * @param worker
	 * @param url
	 * @param headers may be null
	 * @param user optional username for HTTP basic authentication
	 * @param pass optional password for HTTP basic authentication
	 * @return some object from the url, null, if url is not valid
	 * @throws IOException
	 */
	public static <T> Pair<T, HttpResponse> getFullResponse(Worker<T> worker, String url, Map<String, String> headers,
			String user, String pass) throws IOException {
		DURL u = new DURL(url);
		if (!u.valid()) {
			return null;
		}
		DefaultHttpClient client = enableProxyUsage(new DefaultHttpClient(), u);
		if (user != null && pass != null) {
			authenticate(client, user, pass, u);
		}
		HttpGet get = new HttpGet(url);
		if (headers != null) {
			for (String key : headers.keySet()) {
				get.addHeader(key, headers.get(key));
			}
		}
		HttpResponse response = client.execute(get);
		return new Pair<T, HttpResponse>(worker.work(response.getEntity().getContent()), response);
	}

	public static void handleProxies(String protocol, DefaultHttpClient client, String host) {
		TreeSet<String> nops = new TreeSet<String>();

		String proxyHost = getProperty((protocol == null ? "" : protocol + ".") + "proxyHost");

		String proxyUser = getProperty((protocol == null ? "" : protocol + ".") + "proxyUser");
		String proxyPass = getProperty((protocol == null ? "" : protocol + ".") + "proxyPassword");

		if (proxyHost != null) {
			String nop = getProperty((protocol == null ? "" : protocol + ".") + "noProxyHosts");
			if (nop != null && !nop.equals("")) {
				nops.addAll(asList(nop.split("\\|")));
			}
			nop = getProperty((protocol == null ? "" : protocol + ".") + "nonProxyHosts");
			if (nop != null && !nop.equals("")) {
				nops.addAll(asList(nop.split("\\|")));
			}

			int proxyPort = parseInt(getProperty((protocol == null ? "" : protocol + ".") + "proxyPort"));

			if (LOG.isDebugEnabled()) {
				LOG.debug("Found the following no- and nonProxyHosts: {}", nops);
			}

			if (proxyUser != null) {
				Credentials creds = new UsernamePasswordCredentials(proxyUser, proxyPass);
				client.getCredentialsProvider().setCredentials(new AuthScope(proxyHost, proxyPort), creds);
			}

			if (!nops.contains(host)) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Using proxy {}:{}", proxyHost, proxyPort);
					if (protocol == null) {
						LOG.debug("This overrides the protocol specific settings, if there were any.");
					}
				}
				HttpHost proxy = new HttpHost(proxyHost, proxyPort);
				client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
			}
			else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Proxy was set, but {} was contained in the no-/nonProxyList!", host);
					if (protocol == null) {
						LOG.debug("If a protocol specific proxy has been set, it will be used anyway!");
					}
				}
			}
		}

		if (protocol != null) {
			handleProxies(null, client, host);
		}
	}

	public static <T> Pair<T, HttpResponse> getFullResponse(Worker<T> worker, String url, Map<String, String> headers,
			String user, String pass, final int readTimeout) throws IOException {
		DURL u = new DURL(url);
		if (!u.valid()) {
			return null;
		}
		DefaultHttpClient client = enableProxyUsage(new DefaultHttpClient(), u);
		client.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy() {
			@Override
			public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
				long keepAlive = super.getKeepAliveDuration(response, context);
				if (keepAlive == -1) {
					keepAlive = readTimeout * 1000;
				}
				return keepAlive;
			}
		});
		if (user != null && pass != null) {
			authenticate(client, user, pass, u);
		}
		HttpGet get = new HttpGet(url);
		if (headers != null) {
			for (String key : headers.keySet()) {
				get.addHeader(key, headers.get(key));
			}
		}
		HttpResponse response = client.execute(get);
		return new Pair<T, HttpResponse>(worker.work(response.getEntity().getContent()), response);
	}

	/**
	 * reads proxyHost and proxyPort from system parameters and sets them to the passed
	 * HttpClient instance
	 *
	 * @see HttpClient
	 * @param client
	 * @param url must be valid
	 * @return HttpClient with proxy configuration
	 */
	public static DefaultHttpClient enableProxyUsage(DefaultHttpClient client, DURL url) {
		HttpConnectionParams.setConnectionTimeout(client.getParams(), DEFAULT_CONN_TIMEOUT);
		client.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy() {
			@Override
			public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
				long keepAlive = super.getKeepAliveDuration(response, context);
				if (keepAlive == -1) {
					keepAlive = DEFAULT_SOCKET_TIMEOUT;
				}
				return keepAlive;
			}
		});
		String host = url.getURL().getHost();
		String protocol = url.getURL().getProtocol().toLowerCase();
		handleProxies(protocol, client, host);
		return client;
	}

}