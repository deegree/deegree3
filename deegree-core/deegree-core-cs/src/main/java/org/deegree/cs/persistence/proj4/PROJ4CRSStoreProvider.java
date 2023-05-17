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
package org.deegree.cs.persistence.proj4;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.net.URL;

import javax.xml.bind.JAXBException;

import org.deegree.commons.utils.net.DURL;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.cs.exceptions.CRSStoreException;
import org.deegree.cs.persistence.CRSStore;
import org.deegree.cs.persistence.CRSStoreProvider;
import org.deegree.cs.persistence.proj4.jaxb.PROJ4CRSStoreConfig;
import org.deegree.cs.transformations.TransformationFactory.DSTransform;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * {@link PROJ4CRSStoreProvider} for {@link PROJ4CRSStore}
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
public class PROJ4CRSStoreProvider implements CRSStoreProvider {

	private static final Logger LOG = getLogger(PROJ4CRSStoreProvider.class);

	private static final String CONFIG_NS = "http://www.deegree.org/crs/stores/proj4";

	private static final String CONFIG_JAXB_PACKAGE = "org.deegree.cs.persistence.proj4.jaxb";

	private static final URL CONFIG_SCHEMA = PROJ4CRSStoreProvider.class
		.getResource("/META-INF/schemas/crs/stores/proj4/proj4.xsd");

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
			PROJ4CRSStoreConfig config = (PROJ4CRSStoreConfig) unmarshall(CONFIG_JAXB_PACKAGE, CONFIG_SCHEMA,
					new DURL(configURL.toExternalForm()).openStream(), workspace);

			PROJ4CRSStore crsStore = new PROJ4CRSStore(DSTransform.fromSchema(config));
			ProjFileResource resource = null;
			XMLAdapter adapter = new XMLAdapter(configURL);
			URL fileUrl = adapter.resolve(config.getFile());

			resource = new ProjFileResource(new File(fileUrl.toExternalForm()));

			crsStore.setResolver(resource);
			return crsStore;
		}
		catch (JAXBException e) {
			String msg = "Error in proj4 crs store configuration file '" + configURL + "': " + e.getMessage();
			LOG.error(msg);
			throw new CRSStoreException(msg, e);
		}
		catch (Exception e) {
			String msg = "Error in file declaraition inproj4 crs store configuration file '" + configURL + "': "
					+ e.getMessage();
			LOG.error(msg);
			throw new CRSStoreException(msg, e);
		}
	}

}
