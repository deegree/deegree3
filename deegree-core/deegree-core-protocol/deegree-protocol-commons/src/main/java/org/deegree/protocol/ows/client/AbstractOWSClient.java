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
package org.deegree.protocol.ows.client;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.metadata.OperationsMetadata;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.ows.capabilities.OWSCapabilitiesAdapter;
import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.deegree.protocol.ows.http.OwsHttpClient;
import org.deegree.protocol.ows.http.OwsHttpClientImpl;
import org.deegree.protocol.ows.http.OwsHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides common base functionality for API-level client implementations that access OGC
 * web services.
 *
 * @param T OWSCapabilitiesAdapter type for the specific service
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public abstract class AbstractOWSClient<T extends OWSCapabilitiesAdapter> {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractOWSClient.class);

	protected T capaDoc;

	protected final OwsHttpClient httpClient;

	// service endpoint derived from capabilities URL (may be null)
	private URL capaBaseUrl;

	private ServiceIdentification identification;

	private ServiceProvider provider;

	private OperationsMetadata metadata;

	/**
	 * Creates a new {@link AbstractOWSClient} instance from the given capabilities
	 * document.
	 * @param capabilities OWS capabilities document, must not be <code>null</code>
	 * @param httpClient client for customizing HTTP communication, can be
	 * <code>null</code>
	 * @throws OWSExceptionReport if the server replied with a service exception report
	 * @throws XMLStreamException
	 * @throws IOException if a communication/network problem occured
	 */
	protected AbstractOWSClient(XMLAdapter capabilities, OwsHttpClient httpClient)
			throws OWSExceptionReport, XMLStreamException, IOException {
		if (capabilities == null) {
			throw new NullPointerException("Capabilities must not be null.");
		}
		try {
			capaBaseUrl = getGetUrl("GetCapabilities");
		}
		catch (Exception e) {
			LOG.warn("No GetCapabilities URL available.");
		}
		if (httpClient != null) {
			this.httpClient = httpClient;
		}
		else {
			this.httpClient = new OwsHttpClientImpl();
		}
		initCapabilities(capabilities);
	}

	/**
	 * Creates a new {@link AbstractOWSClient} instance from the given capabilities URL.
	 * @param capaUrl URL of a OWS capabilities document, usually this is a
	 * <code>GetCapabilities</code> request to the service, must not be <code>null</code>
	 * @param httpClient client for customizing HTTP communication, can be
	 * <code>null</code>
	 * @throws OWSExceptionReport if the server replied with a service exception report
	 * @throws XMLStreamException
	 * @throws IOException if a communication/network problem occured
	 */
	protected AbstractOWSClient(URL capaUrl, OwsHttpClient httpClient)
			throws IOException, OWSExceptionReport, XMLStreamException {
		if (capaUrl == null) {
			throw new NullPointerException("Capabilities URL must not be null.");
		}
		if (httpClient != null) {
			this.httpClient = httpClient;
		}
		else {
			this.httpClient = new OwsHttpClientImpl();
		}
		initCapabilities(capaUrl);

		String baseUrl = capaUrl.toString();
		int pos = baseUrl.indexOf('?');
		if (pos != -1) {
			baseUrl = baseUrl.substring(0, pos);
			try {
				capaBaseUrl = new URL(baseUrl);
			}
			catch (Throwable t) {
				LOG.warn(t.getMessage());
			}
		}
		else {
			capaBaseUrl = capaUrl;
		}
	}

	private void initCapabilities(URL capaUrl) throws IOException, OWSExceptionReport, XMLStreamException {

		if (shouldUseGet(capaUrl)) {
			OwsHttpResponse response = httpClient.doGet(capaUrl, null, null);
			response.assertHttpStatus200();
			XMLStreamReader responseAsXMLStream = response.getAsXMLStream();

			try {
				XMLAdapter xmlAdapter = new XMLAdapter(responseAsXMLStream);
				initCapabilities(xmlAdapter);
			}
			finally {
				responseAsXMLStream.close();
				response.close();
			}
		}
		else {
			XMLAdapter xmlAdapter = new XMLAdapter(capaUrl);
			initCapabilities(xmlAdapter);
		}
	}

	private boolean shouldUseGet(URL capaUrl) {
		String protocol = capaUrl.getProtocol();
		return "http".equals(protocol) || "https".equals(protocol);
	}

	protected void initCapabilities(XMLAdapter xmlAdapter) throws IOException {
		OMElement rootEl = xmlAdapter.getRootElement();
		String version = rootEl.getAttributeValue(new QName("version"));
		capaDoc = getCapabilitiesAdapter(xmlAdapter.getRootElement(), version);

		try {
			identification = capaDoc.parseServiceIdentification();
		}
		catch (Throwable t) {
			LOG.warn("Error parsing service identification section: " + t.getMessage());
		}
		try {
			provider = capaDoc.parseServiceProvider();
		}
		catch (Throwable t) {
			LOG.warn("Error parsing service provider section: " + t.getMessage());
		}
		try {
			metadata = capaDoc.parseOperationsMetadata();
		}
		catch (Throwable t) {
			LOG.warn("Error parsing metadata section: " + t.getMessage());
		}
	}

	/**
	 * Returns an {@link OWSCapabilitiesAdapter} instance suitable for the specific
	 * service and version.
	 * @param rootEl root element of the service capabilities, never <code>null</code>
	 * @param version capabilities version, may be <code>null</code> (for broken
	 * capabilities responses)
	 * @return capabilities adapter, must not be <code>null</code>
	 * @throws IOException
	 */
	protected abstract T getCapabilitiesAdapter(OMElement rootEl, String version) throws IOException;

	/**
	 * Returns the {@link ServiceIdentification} metadata provided by the server.
	 * @return identification metadata, can be <code>null</code>
	 */
	public final ServiceIdentification getIdentification() {
		return identification;
	}

	/**
	 * Returns the {@link ServiceProvider} metadata provided by the server.
	 * @return provider metadata, can be <code>null</code>
	 */
	public final ServiceProvider getProvider() {
		return provider;
	}

	/**
	 * Returns the {@link OperationsMetadata} provided by the server.
	 * @return operations metadata, can be <code>null</code>
	 */
	public final OperationsMetadata getOperations() {
		return metadata;
	}

	protected boolean isOperationSupported(String operationName) {
		if (metadata == null) {
			return false;
		}
		return metadata.getOperation(operationName) != null;
	}

	protected URL getGetUrl(String operationName) {

		List<URL> getUrls = getGetUrls(operationName);
		if (!getUrls.isEmpty()) {
			if (getUrls.size() > 1) {
				LOG.debug("Server announces multiple HTTP-GET URLs for operation '{}'. Using first one.",
						operationName);
			}
			return getUrls.get(0);
		}
		LOG.warn("Server doesn't announce HTTP-GET URL for operation '{}'. Deriving from capabilities base URL.",
				operationName);

		if (capaBaseUrl == null) {
			throw new RuntimeException("No endpoint available.");
		}
		return capaBaseUrl;
	}

	protected URL getPostUrl(String operationName) {

		List<URL> postUrls = getPostUrls(operationName);
		if (!postUrls.isEmpty()) {
			if (postUrls.size() > 1) {
				LOG.debug("Server announces multiple HTTP-POST URLs for operation '{}'. Using first one.",
						operationName);
			}
			return postUrls.get(0);
		}
		LOG.warn("Server doesn't announce HTTP-POST URL for operation '{}'. Deriving from capabilities base URL.",
				operationName);

		if (capaBaseUrl == null) {
			throw new RuntimeException("No endpoint available.");
		}
		return capaBaseUrl;
	}

	protected List<URL> getGetUrls(String operationName) {
		if (metadata == null) {
			return Collections.emptyList();
		}
		return metadata.getGetUrls(operationName);
	}

	protected List<URL> getPostUrls(String operationName) {
		if (metadata == null) {
			return Collections.emptyList();
		}
		return metadata.getPostUrls(operationName);
	}

	protected void closeQuietly(OwsHttpResponse response) {
		if (response != null) {
			response.close();
		}
	}

}
