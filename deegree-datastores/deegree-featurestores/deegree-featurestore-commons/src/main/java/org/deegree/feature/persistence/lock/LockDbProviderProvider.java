/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.feature.persistence.lock;

import static org.deegree.db.ConnectionProviderUtils.getSyntheticProvider;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.utils.TempFileManager;
import org.deegree.db.ConnectionProvider;
import org.deegree.db.ConnectionProviderProvider;
import org.deegree.db.legacy.LegacyConnectionProviderMetadata;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * ConnectionProviderProvider which provides the feature locking database connection (H2).
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class LockDbProviderProvider extends ConnectionProviderProvider {

	private static final Logger LOG = getLogger(LockDbProviderProvider.class);

	@Override
	public String getNamespace() {
		return null;
	}

	@Override
	public ResourceMetadata<ConnectionProvider> createFromLocation(Workspace workspace,
			ResourceLocation<ConnectionProvider> location) {
		return null;
	}

	@Override
	public URL getSchema() {
		return null;
	}

	@Override
	public List<ResourceMetadata<ConnectionProvider>> getAdditionalResources(Workspace workspace) {
		List<ResourceMetadata<ConnectionProvider>> list = new ArrayList<ResourceMetadata<ConnectionProvider>>();

		String lockDb = new File(TempFileManager.getBaseDir(), "lockdb").getAbsolutePath();
		LOG.info("Using '" + lockDb + "' for h2 lock database.");
		String url = "jdbc:h2:" + lockDb;

		ResourceLocation<ConnectionProvider> location = getSyntheticProvider("LOCK_DB", url, "SA", "");
		list.add(new LegacyConnectionProviderMetadata(workspace, location, this));

		return list;
	}

}
