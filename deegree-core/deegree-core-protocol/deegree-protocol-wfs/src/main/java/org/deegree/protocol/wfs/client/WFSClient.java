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
package org.deegree.protocol.wfs.client;

import static org.deegree.protocol.wfs.WFSRequestType.DescribeFeatureType;
import static org.deegree.protocol.wfs.WFSRequestType.GetFeature;
import static org.deegree.protocol.wfs.WFSVersion.WFS_100;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.io.StreamBufferStore;
import org.deegree.commons.xml.GenericLSInput;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.Filter;
import org.deegree.filter.xml.Filter110XMLEncoder;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.deegree.protocol.ows.client.AbstractOWSClient;
import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.deegree.protocol.ows.http.OwsHttpResponse;
import org.deegree.protocol.wfs.WFSVersion;
import org.deegree.protocol.wfs.capabilities.WFS100CapabilitiesAdapter;
import org.deegree.protocol.wfs.capabilities.WFS110CapabilitiesAdapter;
import org.deegree.protocol.wfs.capabilities.WFS200CapabilitiesAdapter;
import org.deegree.protocol.wfs.capabilities.WFSCapabilitiesAdapter;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getfeature.xml.GetFeature110XMLEncoder;
import org.deegree.protocol.wfs.getgmlobject.GetGmlObject;
import org.deegree.protocol.wfs.metadata.WFSFeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;

/**
 * API-level client for accessing servers that implement the
 * <a href="http://www.opengeospatial.org/standards/wfs">OpenGIS Web Feature Service (WFS)
 * 1.0.0/1.1.0/2.0.0</a> protocol.
 *
 * <h4>Initialization</h4> In the initial step, one constructs a new {@link WFSClient}
 * instance by invoking the constructor with a URL to a WFS capabilities document. This
 * usually is a <code>GetCapabilities</code> request (including necessary parameters) to a
 * WFS service.
 *
 * <pre>
 * ...
 *   URL capabilitiesUrl = new URL( "http://...?service=WFS&version=1.0.0&request=GetCapabilities" );
 *   WFSClient wfsClient = new WFSClient( capabilitiesUrl );
 * ...
 * </pre>
 *
 * Afterwards, the initialized {@link WFSClient} instance is bound to the specified
 * service and WFS protocol version. Now, it's possible to access service metadata,
 * feature type information as well as performing queries.
 *
 * <h4>Accessing service metadata</h4> The method {@link #getMetadata()} allows to access
 * service metadata announced by the service, such as title, abstract, provider etc.
 *
 * <h4>Accessing feature type information</h4> ...
 *
 * <h4>Retrieving feature instances</h4> ...
 *
 * <h4>Retrieving individual GML objects</h4> ...
 *
 * <h4>Performing transactions</h4> ...
 *
 * <h4>Locking features</h4> ...
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class WFSClient extends AbstractOWSClient<WFSCapabilitiesAdapter> {

	private static final Logger LOG = LoggerFactory.getLogger(WFSClient.class);

	private WFSVersion version;

	private final List<WFSFeatureType> wfsFts;

	private final Map<QName, WFSFeatureType> ftNameTowfsFt = new LinkedHashMap<QName, WFSFeatureType>();

	private AppSchema schema;

	/**
	 * Creates a new {@link WFSClient} instance with default behavior.
	 * @param capaUrl url of a WFS capabilities document, usually this is a KVP-encoded
	 * <code>GetCapabilities</code> request to a WFS service, must not be
	 * <code>null</code>
	 * @throws OWSExceptionReport if the server responded with an exception report
	 * @throws XMLStreamException
	 * @throws IOException if a communication/network problem occured
	 */
	public WFSClient(URL capaUrl) throws OWSExceptionReport, XMLStreamException, IOException {
		super(capaUrl, null);
		wfsFts = capaDoc.parseFeatureTypeList();
		for (WFSFeatureType wfsFt : wfsFts) {
			ftNameTowfsFt.put(wfsFt.getName(), wfsFt);
		}
	}

	/**
	 * Creates a new {@link WFSClient} instance with options.
	 * @param capaUrl url of a WFS capabilities document, usually this is a
	 * <code>GetCapabilities</code> request to a WFS service, must not be
	 * <code>null</code>
	 * @param schema application schema that describes the feature types offered by the
	 * service, can be <code>null</code> (in this case, <code>DescribeFeatureType</code>
	 * requests will be performed to determine the schema)
	 * @throws OWSExceptionReport if the server responded with a service exception report
	 * @throws XMLStreamException
	 * @throws IOException if a communication/network problem occured
	 */
	public WFSClient(URL capaUrl, AppSchema schema) throws OWSExceptionReport, XMLStreamException, IOException {
		this(capaUrl);
		this.schema = schema;
	}

	/**
	 * Returns the WFS protocol version in use.
	 * @return the WFS protocol version in use, never <code>null</code>
	 */
	public WFSVersion getServiceVersion() {
		return version;
	}

	/**
	 * Returns (metadata of) all feature types offered by the service.
	 * @return metadata of the feature types, never <code>null</code>
	 */
	public List<WFSFeatureType> getFeatureTypes() {
		return wfsFts;
	}

	/**
	 * Returns (metadata of) the specified feature type offered by the service.
	 * @return metadata of the feature type, or <code>null</code> if no such feature type
	 * exists
	 */
	public WFSFeatureType getFeatureType(QName ftName) {
		return ftNameTowfsFt.get(ftName);
	}

	/**
	 * Returns the (GML) {@link AppSchema} for all {@link FeatureType}s offered by this
	 * server.
	 * @return application schema, never <code>null</code>
	 * @throws OWSExceptionReport if the server responded with a service exception report
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	public synchronized AppSchema getAppSchema() throws OWSExceptionReport, XMLStreamException, IOException {

		if (schema == null) {
			URL endPoint = getGetUrl(DescribeFeatureType.name());
			LinkedHashMap<String, String> kvp = new LinkedHashMap<String, String>();
			kvp.put("service", "WFS");
			kvp.put("version", version.getOGCVersion().toString());
			kvp.put("request", "DescribeFeatureType");

			OwsHttpResponse response = httpClient.doGet(endPoint, kvp, null);
			XMLStreamReader xmlStream = null;
			StreamBufferStore tmpStore = null;
			try {
				xmlStream = response.getAsXMLStream();
				tmpStore = XMLStreamUtils.serialize(xmlStream);
			}
			finally {
				response.close();
			}

			try {
				LSInput input = new GenericLSInput();
				input.setByteStream(tmpStore.getInputStream());
				input.setSystemId(xmlStream.getLocation().getSystemId());
				GMLAppSchemaReader schemaDecoder = new GMLAppSchemaReader(null, null, input);
				schema = schemaDecoder.extractAppSchema();
			}
			catch (Throwable t) {
				String msg = "Error parsing DescribeFeatureType response as GML application schema: " + t.getMessage();
				throw new IOException(msg, t);
			}
		}
		return schema;
	}

	/**
	 * Queries features of the specified feature type and with an optional filter.
	 * @param ftName
	 * @param filter
	 * @return query reponse, never <code>null</code>
	 * @throws IOException
	 * @throws XMLStreamException
	 * @throws TransformationException
	 * @throws UnknownCRSException
	 */
	public GetFeatureResponse<Feature> getFeatures(QName ftName, Filter filter)
			throws OWSExceptionReport, XMLStreamException, IOException, UnknownCRSException, TransformationException {

		URL endPoint = getGetUrl(GetFeature.name());
		LinkedHashMap<String, String> kvp = new LinkedHashMap<String, String>();
		kvp.put("service", "WFS");
		kvp.put("version", version.getOGCVersion().toString());
		kvp.put("request", "GetFeature");
		kvp.put("typeName", ftName.getLocalPart());
		if (filter != null) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(bos, "UTF-8");
			switch (version) {
				case WFS_110:
					Filter110XMLEncoder.export(filter, xmlWriter);
					break;
				case WFS_100:
				case WFS_200:
					String msg = "Only exporting of WFS 1.1.0 filters is currently implemented.";
					throw new UnsupportedOperationException(msg);
			}
			xmlWriter.close();
			kvp.put("filter", bos.toString("UTF-8"));
		}

		OwsHttpResponse response = httpClient.doGet(endPoint, kvp, null);

		GMLVersion gmlVersion = getAppSchema().getGMLSchema().getVersion();
		return new GetFeatureResponse<Feature>(response, getAppSchema(), gmlVersion);
	}

	/**
	 * Performs the given {@link GetFeature} request.
	 * @return WFS response, never <code>null</code>
	 * @throws OWSExceptionReport if the server responded with a service exception report
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public GetFeatureResponse<Feature> doGetFeature(GetFeature request)
			throws OWSExceptionReport, XMLStreamException, IOException {

		URL endPoint = getPostUrl(GetFeature.name());

		StreamBufferStore requestSink = new StreamBufferStore();
		try {
			XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(requestSink);
			// TODO handle other WFS versions
			GetFeature110XMLEncoder.export(request, null, xmlWriter);
			xmlWriter.close();
			requestSink.close();
		}
		catch (Throwable t) {
			throw new RuntimeException("Error creating XML request: " + request);
		}

		OwsHttpResponse response = httpClient.doPost(endPoint, "text/xml", requestSink, null);

		GMLVersion gmlVersion = getAppSchema().getGMLSchema().getVersion();
		return new GetFeatureResponse<Feature>(response, getAppSchema(), gmlVersion);
	}

	public GMLObject getGMLObject(GetGmlObject request) {
		return null;
	}

	@Override
	protected WFSCapabilitiesAdapter getCapabilitiesAdapter(OMElement root, String versionAttr) throws IOException {

		QName rootEl = root.getQName();

		// for all versions (1.0.0/1.1.0/2.0.0), root element is "WFS_Capabilities"
		if ("WFS_Capabilities".equalsIgnoreCase(rootEl.getLocalPart())) {
			version = WFS_100;
			if (versionAttr != null) {
				try {
					Version ogcVersion = Version.parseVersion(versionAttr);
					version = WFSVersion.valueOf(ogcVersion);
				}
				catch (Throwable t) {
					String msg = "WFS capabilities document has unsupported version '" + versionAttr + "'.";
					throw new IllegalArgumentException(msg);
				}
			}
			else {
				LOG.warn("No version attribute in WFS capabilities document. Defaulting to 1.0.0.");
			}
		}
		else {
			// TODO
			String msg = "Unexpected GetCapabilities response element: '" + rootEl + "'.";
			throw new IOException(msg);
		}

		switch (version) {
			case WFS_100: {
				WFS100CapabilitiesAdapter capaDoc = new WFS100CapabilitiesAdapter();
				capaDoc.setRootElement(root);
				return capaDoc;
			}
			case WFS_110: {
				WFS110CapabilitiesAdapter capaDoc = new WFS110CapabilitiesAdapter();
				capaDoc.setRootElement(root);
				return capaDoc;
			}
			case WFS_200: {
				WFS200CapabilitiesAdapter capaDoc = new WFS200CapabilitiesAdapter();
				capaDoc.setRootElement(root);
				return capaDoc;
			}
		}
		throw new RuntimeException("Internal error: unhandled WFS service version '" + version + "'.");
	}

}
