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
package org.deegree.cs.persistence.deegree.d3;

import static java.util.Collections.singletonMap;
import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.deegree.commons.utils.net.DURL;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.cs.exceptions.CRSStoreException;
import org.deegree.cs.persistence.CRSStore;
import org.deegree.cs.persistence.CRSStoreProvider;
import org.deegree.cs.persistence.deegree.d3.jaxb.DeegreeCRSStoreConfig;
import org.deegree.cs.transformations.TransformationFactory.DSTransform;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * {@link DeegreeCRSStoreProvider} for the {@link DeegreeCRSStore} (deegree3!)
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
public class DeegreeCRSStoreProvider implements CRSStoreProvider {

	private static final Logger LOG = getLogger(DeegreeCRSStoreProvider.class);

	private static final String CONFIG_NS = "http://www.deegree.org/crs/stores/deegree";

	private static final String CONFIG_JAXB_PACKAGE = "org.deegree.cs.persistence.deegree.d3.jaxb";

	private static final URL CONFIG_SCHEMA = DeegreeCRSStoreProvider.class
		.getResource("/META-INF/schemas/crs/stores/deegree/deegree.xsd");

	private static final String CONFIG_TEMPLATE = "/META-INF/schemas/crs/stores/deegree/example.xml";

	@Override
	public String getConfigNamespace() {
		return CONFIG_NS;
	}

	@Override
	public URL getConfigSchema() {
		return CONFIG_SCHEMA;
	}

	public static Map<String, URL> getConfigTemplates() {
		return singletonMap("example", DeegreeCRSStoreProvider.class.getResource(CONFIG_TEMPLATE));
	}

	@Override
	public CRSStore getCRSStore(URL configURL, Workspace workspace) throws CRSStoreException {
		DeegreeCRSStore crsStore = null;
		try {
			DeegreeCRSStoreConfig config = (DeegreeCRSStoreConfig) unmarshall(CONFIG_JAXB_PACKAGE, CONFIG_SCHEMA,
					new DURL(configURL.toExternalForm()).openStream(), workspace);
			XMLAdapter adapter = new XMLAdapter();
			adapter.setSystemId(configURL.toString());

			String parserFile = config.getFile();
			if (parserFile == null || parserFile.trim().length() == 0) {
				String msg = "Error in crs store configuration file '" + configURL + "': parserFile must not be null!";
				LOG.error(msg);
				throw new CRSStoreException(msg);
			}
			crsStore = new DeegreeCRSStore(DSTransform.fromSchema(config), adapter.resolve(parserFile));
		}
		catch (JAXBException e) {
			String msg = "Error in crs store configuration file '" + configURL + "': " + e.getMessage();
			LOG.error(msg);
			throw new CRSStoreException(msg, e);
		}
		catch (MalformedURLException e) {
			String msg = "Error in file declaration in the crs store configuration file '" + configURL + "': "
					+ e.getMessage();
			LOG.error(msg);
			throw new CRSStoreException(msg, e);
		}
		catch (Exception e) {
			String msg = "Error when loading crs store configuration file '" + configURL + "': " + e.getMessage();
			LOG.error(msg);
			throw new CRSStoreException(msg, e);
		}
		return crsStore;
	}

}
