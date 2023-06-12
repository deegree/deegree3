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
package org.deegree.console.metadata;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.deegree.workspace.ResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class ResourceProviderMetadata {

	private static Logger LOG = LoggerFactory.getLogger(ResourceProviderMetadata.class);

	private String wizardView = "/console/jsf/wizard";

	private final Map<String, ConfigExample> exampleNameToExample = new HashMap<String, ConfigExample>();

	private String name;

	private static final Map<String, ResourceProviderMetadata> rpClassNameToMd = new HashMap<String, ResourceProviderMetadata>();

	private ResourceProviderMetadata(ResourceProvider<?> rp) {
		String className = rp.getClass().getName();
		name = rp.getClass().getSimpleName();
		URL url = rp.getClass().getResource("/META-INF/console/resourceprovider/" + className);
		if (url != null) {
			LOG.debug("Loading resource provider metadata from '" + url + "'");
			Properties props = new Properties();
			InputStream is = null;
			try {
				is = url.openStream();
				props.load(is);
				if (props.containsKey("name")) {
					name = props.getProperty("name").trim();
				}
				if (props.containsKey("wizard")) {
					wizardView = props.getProperty("wizard").trim();
				}
				int i = 1;
				while (true) {
					final String examplePrefix = "example" + i++ + "_";
					String exampleLocation = props.getProperty(examplePrefix + "location");
					if (exampleLocation == null) {
						break;
					}
					exampleLocation = exampleLocation.trim();
					final URL exampleUrl = this.getClass().getResource(exampleLocation);
					if (exampleUrl == null) {
						LOG.error("Configuration example file '" + exampleLocation + "' is missing on classpath.");
						continue;
					}
					final String exampleName = getExampleDisplayName(props, examplePrefix, exampleLocation);
					String exampleDescription = null;
					if (props.containsKey(examplePrefix + "description")) {
						exampleDescription = props.getProperty(examplePrefix + "description").trim();
					}
					ConfigExample example = new ConfigExample(exampleUrl, exampleName, exampleDescription);
					exampleNameToExample.put(exampleName, example);
				}
			}
			catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
			finally {
				closeQuietly(is);
			}
		}
	}

	private String getExampleDisplayName(final Properties props, final String examplePrefix,
			final String exampleLocation) {
		if (props.containsKey(examplePrefix + "name")) {
			return props.getProperty(examplePrefix + "name").trim();
		}
		final int fileNameStart = exampleLocation.lastIndexOf("/") + 1;
		String exampleName = exampleLocation.substring(fileNameStart);
		final int fileNameEnd = exampleName.lastIndexOf('.');
		if (fileNameEnd != -1) {
			exampleName = exampleName.substring(0, fileNameEnd);
		}
		return exampleName;
	}

	public static synchronized ResourceProviderMetadata getMetadata(ResourceProvider<?> rp) {
		if (!rpClassNameToMd.containsKey(rp.getClass().getName())) {
			rpClassNameToMd.put(rp.getClass().getName(), new ResourceProviderMetadata(rp));
		}
		return rpClassNameToMd.get(rp.getClass().getName());
	}

	public String getName() {
		return name;
	}

	public Map<String, ConfigExample> getExamples() {
		return exampleNameToExample;
	}

	public String getConfigWizardView() {
		return wizardView;
	}

}
