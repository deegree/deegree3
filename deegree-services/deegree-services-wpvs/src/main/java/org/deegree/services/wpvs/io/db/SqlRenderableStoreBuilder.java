/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.services.wpvs.io.db;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.util.List;

import org.deegree.db.ConnectionProvider;
import org.deegree.db.ConnectionProviderProvider;
import org.deegree.rendering.r3d.jaxb.renderable.RenderableSQLStoreConfig;
import org.deegree.rendering.r3d.opengl.rendering.model.texture.TexturePool;
import org.deegree.rendering.r3d.persistence.RenderableStore;
import org.deegree.services.wpvs.io.ModelBackend;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for building sql renderable stores.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class SqlRenderableStoreBuilder implements ResourceBuilder<RenderableStore> {

	private static final Logger LOG = LoggerFactory.getLogger(SqlRenderableStoreBuilder.class);

	private RenderableSQLStoreConfig config;

	private ResourceMetadata<RenderableStore> metadata;

	private Workspace workspace;

	public SqlRenderableStoreBuilder(RenderableSQLStoreConfig config, ResourceMetadata<RenderableStore> metadata,
			Workspace workspace) {
		this.config = config;
		this.metadata = metadata;
		this.workspace = workspace;
	}

	@Override
	public RenderableStore build() {
		RenderableStore rs = null;
		try {

			String connId = config.getJDBCConnId();
			ConnectionProvider prov = workspace.getResource(ConnectionProviderProvider.class, connId);
			Connection connection = prov.getConnection();
			connection.close();

			rs = new PostgisBackend(connId,
					(config.isIsBillboard() ? ModelBackend.Type.TREE : ModelBackend.Type.BUILDING), workspace,
					metadata);
			// instantiate the texture dir
			List<String> tDirs = config.getTextureDirectory();
			for (String tDir : tDirs) {
				if (tDir != null) {
					File tD = resolveFile(tDir, false, null);
					TexturePool.addTexturesFromDirectory(tD);
				}
			}

		}
		catch (Exception e) {
			throw new ResourceInitException(e.getLocalizedMessage(), e);
		}
		return rs;
	}

	private File resolveFile(String fileName, boolean required, String msg) {
		URI resolve = resolveURI(fileName);
		if (resolve == null) {
			if (required) {
				throw new IllegalArgumentException(msg);
			}
			return null;
		}
		return new File(resolve);
	}

	private URI resolveURI(String fileName) {
		URI resolve = null;
		try {
			URL url = metadata.getLocation().resolveToUrl(fileName);
			resolve = url.toURI();
		}
		catch (URISyntaxException e) {
			LOG.warn("Error while resolving url for file: " + fileName + ".");
		}
		return resolve;
	}

}
