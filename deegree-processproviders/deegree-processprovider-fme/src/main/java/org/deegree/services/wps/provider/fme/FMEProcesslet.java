/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2022 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
  - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

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
package org.deegree.services.wps.provider.fme;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copyLarge;
import static org.deegree.commons.utils.net.HttpUtils.STREAM;
import static org.deegree.commons.utils.net.HttpUtils.UTF8STRING;
import static org.deegree.commons.utils.net.HttpUtils.post;
import static org.deegree.commons.utils.net.HttpUtils.postFullResponse;
import static org.deegree.commons.xml.XMLAdapter.writeElement;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.RequestUtils;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.input.ProcessletInput;
import org.deegree.services.wps.output.ComplexOutput;
import org.slf4j.Logger;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class FMEProcesslet implements Processlet {

	private static final Logger LOG = getLogger(FMEProcesslet.class);

	private final String fmeBaseUrl;

	private final String fmeRepo;

	private final String fmeWorkspace;

	private final String tokenUrl;

	private final Map<String, String> tokenMap;

	public FMEProcesslet(String baseUrl, String tokenUrl, Map<String, String> tokenMap, String repo, String workspace,
			FMEInvocationStrategy invocationStrategy) {
		this.fmeBaseUrl = baseUrl;
		this.tokenUrl = tokenUrl;
		this.tokenMap = tokenMap;
		this.fmeRepo = repo;
		this.fmeWorkspace = workspace;
	}

	public void destroy() {
		// nothing to destroy
	}

	public void init() {
		// nothing to init
	}

	public void process(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info)
			throws ProcessletException {
		try {
			Map<String, String> kvpMap = buildInputMap(in);

			processViaDatastreaming(out, kvpMap);
		}
		catch (Exception e) {
			LOG.error("Exception", e);
			throw new ProcessletException(e.getMessage());
		}
	}

	private void processViaDatastreaming(ProcessletOutputs out, Map<String, String> kvpMap)
			throws MalformedURLException, IOException, XMLStreamException {
		String url = this.fmeBaseUrl + "/fmeserver/streaming/fmedatastreaming/" + fmeRepo + "/"
				+ encodeReportedFmeUrisHack(fmeWorkspace);
		if (getVendorSpecificParameters().size() > 0) {
			url = url + "?" + getVendorSpecificParameters().entrySet()
				.stream() //
				.map(e -> e.getKey() + "=" + e.getValue()) //
				.collect(Collectors.joining("&"));
		}

		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", "fmetoken token=" + getSecurityToken());

		LOG.debug("Sending {}", url);
		Pair<InputStream, HttpResponse> p = postFullResponse(STREAM, url, kvpMap, headers, 0);

		InputStream is = p.first;
		ComplexOutput output = (ComplexOutput) out.getParameter("FMEResponse");
		Header contentType = p.second.getEntity().getContentType();
		LOG.debug("Content type: {}", contentType);
		if (contentType.getValue() != null
				&& (contentType.getValue().contains("xml") || contentType.getValue().contains("html"))) {
			copyXmlResponse(is, output);
		}
		else {
			copyBinaryResponse(is, output);
		}
	}

	private void copyXmlResponse(InputStream is, ComplexOutput output) throws XMLStreamException {
		try {
			XMLStreamWriter writer = output.getXMLStreamWriter();
			XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
			while (reader.getEventType() != START_ELEMENT) {
				reader.next();
			}
			writeElement(writer, reader);
		}
		finally {
			IOUtils.closeQuietly(is);
		}
	}

	private void copyBinaryResponse(InputStream is, ComplexOutput output) throws IOException {
		try {
			OutputStream os = output.getBinaryOutputStream();
			copyLarge(is, os);
		}
		finally {
			closeQuietly(is);
		}
	}

	private String getSecurityToken() throws IOException {
		LOG.debug("Sending {}", tokenUrl);
		return post(UTF8STRING, tokenUrl, tokenMap, null, 60000).trim();
	}

	private Map<String, String> buildInputMap(ProcessletInputs in) {
		Map<String, String> map = new HashMap<String, String>();
		if (in != null) {
			for (ProcessletInput input : in.getParameters()) {
				if (input instanceof LiteralInput) {
					map.put(input.getIdentifier().getCode(), ((LiteralInput) input).getValue());
				}
			}
		}
		return map;
	}

	private String encodeReportedFmeUrisHack(String uri) {
		// cannot use URLEncoder here, as this would encode slashes as well...
		return uri.replace(" ", "+");
	}

	private Map<String, String> getVendorSpecificParameters() {
		Map<String, String> kvpParams = RequestUtils.getCurrentThreadRequestParameters().get();
		Map<String, String> result = new HashMap<>();
		if (kvpParams.containsKey("TM_TAG"))
			result.put("tm_tag", kvpParams.get("TM_TAG"));
		if (kvpParams.containsKey("TM_TTL"))
			result.put("tm_ttl", kvpParams.get("TM_TTL"));
		if (kvpParams.containsKey("TM_TTC"))
			result.put("tm_ttc", kvpParams.get("TM_TTC"));
		return result;
	}

}
