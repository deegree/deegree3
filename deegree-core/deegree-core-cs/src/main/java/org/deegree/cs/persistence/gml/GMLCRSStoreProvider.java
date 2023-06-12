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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.cs.persistence.gml;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;
import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.deegree.commons.utils.net.DURL;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.cs.exceptions.CRSStoreException;
import org.deegree.cs.i18n.Messages;
import org.deegree.cs.persistence.CRSStore;
import org.deegree.cs.persistence.CRSStoreProvider;
import org.deegree.cs.persistence.gml.jaxb.GMLCRSStoreConfig;
import org.deegree.cs.persistence.gml.jaxb.Param;
import org.deegree.cs.transformations.TransformationFactory.DSTransform;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * {@link GMLCRSStoreProvider} for {@link GMLCRSStore}
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
public class GMLCRSStoreProvider implements CRSStoreProvider {

	private static final Logger LOG = getLogger(GMLCRSStoreProvider.class);

	private static final String CONFIG_NS = "http://www.deegree.org/crs/stores/gml";

	private static final String CONFIG_JAXB_PACKAGE = "org.deegree.cs.persistence.gml.jaxb";

	private static final URL CONFIG_SCHEMA = GMLCRSStoreProvider.class
		.getResource("/META-INF/schemas/crs/stores/gml/gml.xsd");

	@Override
	public String getConfigNamespace() {
		return CONFIG_NS;
	}

	@Override
	public URL getConfigSchema() {
		return CONFIG_SCHEMA;
	}

	@Override
	public CRSStore getCRSStore(URL configURL, Workspace workspace) throws CRSStoreException {
		try {
			GMLCRSStoreConfig config = (GMLCRSStoreConfig) unmarshall(CONFIG_JAXB_PACKAGE, CONFIG_SCHEMA,
					new DURL(configURL.toExternalForm()).openStream(), workspace);

			GMLCRSStore crsStore = new GMLCRSStore(DSTransform.fromSchema(config));
			GMLResource resource = null;

			String resourceClassName = config.getGMLResourceClass();
			if (resourceClassName != null && resourceClassName.trim().length() > 0) {
				try {
					List<Param> configParams = config.getParam();
					Map<String, List<String>> params = new HashMap<String, List<String>>();
					for (Param param : configParams) {
						params.put(param.getName(), param.getValue());
					}
					// use reflection to instantiate the configured resource.

					Class<?> t;
					try {
						t = Class.forName(resourceClassName);
					}
					catch (Exception e) {
						LOG.debug("Could not find class from classname '" + resourceClassName
								+ "'. Search in the additional modules in the workspace.");
						t = Class.forName(resourceClassName, false, workspace.getModuleClassLoader());
					}
					LOG.debug("Trying to load configured CRS provider from classname: " + resourceClassName);
					Constructor<?> constructor = t.getConstructor(GMLCRSStore.class, Map.class);
					if (constructor == null) {
						LOG.error("No constructor ( " + this.getClass() + ", Properties.class) found in class:"
								+ resourceClassName);
					}
					else {
						resource = (GMLResource) constructor.newInstance(crsStore, params);
					}
				}
				catch (Exception t) {
					LOG.error(Messages.getMessage("CRS_CONFIG_INSTANTIATION_ERROR", resourceClassName, t.getMessage()),
							t);
				}
				LOG.info("The configured class: " + resourceClassName + " was instantiated.");
			}
			if (resource == null) {
				LOG.info("Trying to instantiate the default GMLFileResource");
				XMLAdapter adapter = new XMLAdapter(configURL);
				URL resolvedGMLFile = adapter.resolve(config.getGMLFile());

				resource = new GMLFileResource(crsStore, resolvedGMLFile);
			}
			crsStore.setResolver(resource);
			return crsStore;
		}
		catch (JAXBException e) {
			String msg = "Error in gml crs store configuration file '" + configURL + "': " + e.getMessage();
			LOG.error(msg);
			throw new CRSStoreException(msg, e);
		}
		catch (Exception e) {
			String msg = "Error in GMLFile declaration in gml crs store configuration file '" + configURL + "': "
					+ e.getMessage();
			LOG.error(msg);
			throw new CRSStoreException(msg, e);
		}
	}

}