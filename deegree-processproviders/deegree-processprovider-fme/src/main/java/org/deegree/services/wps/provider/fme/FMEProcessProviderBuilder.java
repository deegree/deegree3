/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2022 by:
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
package org.deegree.services.wps.provider.fme;

import static org.deegree.commons.utils.net.HttpUtils.UTF8STRING;
import static org.deegree.commons.utils.net.HttpUtils.postFullResponse;
import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.apache.http.HttpResponse;
import org.deegree.commons.json.JSONAdapter;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.net.HttpUtils;
import org.deegree.process.jaxb.java.LanguageStringType;
import org.deegree.process.jaxb.java.LiteralInputDefinition;
import org.deegree.process.jaxb.java.ProcessDefinition;
import org.deegree.services.wps.provider.ProcessProvider;
import org.deegree.services.wps.provider.fme.jaxb.FMEServer;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ResourceBuilder} building a {@link FMEProcessProvider}
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FMEProcessProviderBuilder implements ResourceBuilder<ProcessProvider> {

	private static final Logger LOG = LoggerFactory.getLogger(FMEProcessProviderBuilder.class);

	private Workspace workspace;

	private ResourceLocation<ProcessProvider> location;

	private AbstractResourceProvider<ProcessProvider> provider;

	public FMEProcessProviderBuilder(Workspace workspace, ResourceLocation<ProcessProvider> location,
			AbstractResourceProvider<ProcessProvider> provider) {
		this.workspace = workspace;
		this.location = location;
		this.provider = provider;
	}

	@Override
	public ProcessProvider build() {
		try {
			InputStream config = location.getAsStream();
			FMEServer server;
			server = (FMEServer) unmarshall("org.deegree.services.wps.provider.fme.jaxb", provider.getSchema(), config,
					workspace);
			String user = "";
			String pass = "";
			if (server.getUsername() != null) {
				user = server.getUsername();
				pass = server.getPassword();
			}
			String base = server.getAddress();
			if (base.endsWith("/")) {
				base = base.substring(0, base.length() - 1);
			}
			String resturl = base + "/fmerest/v3/";
			String tokenurl = base + "/fmetoken/service/generate";

			HashSet<String> repositories = new HashSet<String>();
			List<String> list = server.getRepository();
			if (list == null || list.isEmpty()) {
				repositories.add("wps");
			}
			else {
				for (String s : list) {
					repositories.add(s.trim().toLowerCase());
				}
			}

			Map<String, String> map = new HashMap<String, String>();
			map.put("user", user);
			map.put("password", pass);
			map.put("expiration", "1");
			map.put("timeunit", "hour");
			LOG.debug("Sending {}", tokenurl);
			Pair<String, HttpResponse> pair = postFullResponse(UTF8STRING, tokenurl, map, null, 0);
			if (pair.second.getStatusLine().getStatusCode() == 401) {
				throw new ResourceInitException(
						"Could not authenticate against token service. " + "Check username/password in configuration.");
			}
			String token = pair.first.trim();
			String url = resturl + "repositories?fmetoken=" + token;
			LOG.debug("Sending {}", url);
			JSONAdapter json = logAdapter(retrieve(url));
			Map<CodeType, FMEProcess> processes = new HashMap<CodeType, FMEProcess>();
			for (Object o : json.getJSONArrayForPath("$.items[*].name")) {
				String repo = (String) o;
				LOG.debug("Found repository {}.", repo);
				if (!repositories.contains(repo.trim().toLowerCase())) {
					LOG.debug("Skipping repository {} because it was not configured.", repo);
					continue;
				}
				String workspaces = resturl + "repositories/" + repo + "/items?fmetoken=" + token;
				LOG.debug("Sending {}", workspaces);
				JSONAdapter ws = logAdapter(retrieve(workspaces));
				for (Map<String, String> workspace : ws.getMapArrayForPath("$['items']")) {
					createProcesses(base, tokenurl, repositories, map, token, processes, repo, ws, workspace);
				}
			}
			FMEProcessMetadata metadata = new FMEProcessMetadata(workspace, location, provider);
			return new FMEProcessProvider(processes, metadata);
		}
		catch (Exception e) {
			if (e instanceof ResourceInitException) {
				throw (ResourceInitException) e;
			}
			throw new ResourceInitException("Error creating FME processes from configuration '"
					+ location.getIdentifier() + "': " + e.getMessage(), e);
		}
	}

	private void createProcesses(String base, String tokenurl, HashSet<String> repositories, Map<String, String> map,
			String token, Map<CodeType, FMEProcess> processes, String repo, JSONAdapter ws,
			Map<String, String> workspace) {
		try {
			FMEProcess process = createProcess(base, tokenurl, repositories, map, token, repo, ws, workspace);
			CodeType id = repositories.size() == 1 ? new CodeType(process.getDescription().getIdentifier().getValue())
					: new CodeType(process.getDescription().getIdentifier().getValue(), repo);
			LOG.debug("Created FMEProcess: " + id);
			processes.put(id, process);
		}
		catch (Exception e) {
			LOG.error("Unable to create FMEProcess from element '" + workspace + "': " + e.getMessage(), e);
		}
	}

	private FMEProcess createProcess(String base, String tokenurl, HashSet<String> repositories,
			Map<String, String> map, String token, String repo, JSONAdapter ws, Map<String, String> workspace)
			throws IOException, ResourceInitException {

		String name = workspace.get("name");
		String title = workspace.getOrDefault("title", null);
		String descr = workspace.get("description");
		String uri = "/fmerest/v3/repositories/" + repo + "/items/" + name;

		ProcessDefinition.InputParameters inputs;
		try {
			inputs = determineProcessInputs(base, uri, token);
		}
		catch (Exception e) {
			String msg = "Error determining process inputs: " + e.getMessage();
			throw new ResourceInitException(msg, e);
		}

		FMEInvocationStrategy invocationStrategy;
		try {
			invocationStrategy = determineInvocationStrategy(base, uri, token);
		}
		catch (Exception e) {
			String msg = "Error determining process outputs: " + e.getMessage();
			throw new ResourceInitException(msg, e);
		}

		return new FMEProcess(inputs, invocationStrategy, name, title, repo, descr, base, uri, tokenurl, token, map,
				repositories.size() != 1);
	}

	private ProcessDefinition.InputParameters determineProcessInputs(String base, String uri, String token)
			throws IOException {

		String url = base + encodeReportedFmeUrisHack(uri) + "/parameters?fmetoken=" + token;
		LOG.debug("Sending {}", url);
		JSONAdapter params = logAdapter(retrieve(url));

		ProcessDefinition.InputParameters inputs = new ProcessDefinition.InputParameters();
		for (Object paramObject : params.getJSONArrayForPath("")) {
			if (paramObject instanceof HashMap<?, ?>) {
				@SuppressWarnings("unchecked")
				HashMap<String, Object> param = (HashMap<String, Object>) paramObject;
				String name = (String) param.get("name");
				String description = (String) param.get("description");
				LiteralInputDefinition input = new LiteralInputDefinition();
				org.deegree.process.jaxb.java.CodeType id = new org.deegree.process.jaxb.java.CodeType();
				id.setValue(name);
				input.setIdentifier(id);
				LanguageStringType title = new LanguageStringType();
				title.setValue(description);
				input.setTitle(title);
				JAXBElement<LiteralInputDefinition> inEl;
				inEl = new JAXBElement<LiteralInputDefinition>(new QName(""), LiteralInputDefinition.class, input);
				inEl.getValue().setMinOccurs(BigInteger.valueOf(0));
				inputs.getProcessInput().add(inEl);
			}
			else {
				LOG.error("{} can not cast to JSONObject", paramObject.getClass());
			}

		}
		return inputs;
	}

	private FMEInvocationStrategy determineInvocationStrategy(String base, String uri, String token)
			throws IOException {
		return new FMEJobSubmitterInvocationStrategy();
	}

	private static JSONAdapter logAdapter(JSONAdapter adapter) {
		if (LOG.isDebugEnabled()) {
			try {
				LOG.debug("Response was\n{}", adapter.getJsonCtx());
			}
			catch (Throwable e) {
				LOG.trace("Stack trace while debugging: ", e);
			}
		}
		return adapter;
	}

	private String encodeReportedFmeUrisHack(String uri) {
		// cannot use URLEncoder here, as this would encode slashes as well...
		return uri.replace(" ", "+");
	}

	private JSONAdapter retrieve(String url) throws IOException {
		InputStream retrieve = HttpUtils.retrieve(HttpUtils.STREAM, url);
		return new JSONAdapter(retrieve);
	}

}
