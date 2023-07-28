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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.feature.persistence.simplesql;

import static org.deegree.commons.utils.CollectionUtils.map;

import java.util.LinkedList;

import org.deegree.commons.utils.CollectionUtils.Mapper;
import org.deegree.commons.utils.Pair;
import org.deegree.db.ConnectionProvider;
import org.deegree.db.ConnectionProviderProvider;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.simplesql.jaxb.SimpleSQLFeatureStoreConfig;
import org.deegree.feature.persistence.simplesql.jaxb.SimpleSQLFeatureStoreConfig.LODStatement;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * <code>SimpleSqlFeatureStoreBuilder</code>
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class SimpleSqlFeatureStoreBuilder implements ResourceBuilder<FeatureStore> {

	private ResourceMetadata<FeatureStore> metadata;

	private SimpleSQLFeatureStoreConfig config;

	private Workspace workspace;

	private static Mapper<Pair<Integer, String>, LODStatement> lodMapper = new Mapper<Pair<Integer, String>, LODStatement>() {
		public Pair<Integer, String> apply(LODStatement u) {
			return new Pair<Integer, String>(u.getAboveScale(), u.getValue());
		}
	};

	public SimpleSqlFeatureStoreBuilder(ResourceMetadata<FeatureStore> metadata, SimpleSQLFeatureStoreConfig config,
			Workspace workspace) {
		this.metadata = metadata;
		this.config = config;
		this.workspace = workspace;
	}

	@Override
	public FeatureStore build() {
		String connId = config.getConnectionPoolId();
		if (connId == null) {
			connId = config.getJDBCConnId();
		}
		String srs = config.getStorageCRS();
		String stmt = config.getSQLStatement();
		String name = config.getFeatureTypeName();
		String ns = config.getFeatureTypeNamespace();
		String prefix = config.getFeatureTypePrefix();
		String bbox = config.getBBoxStatement();
		LinkedList<Pair<Integer, String>> lods = map(config.getLODStatement(), lodMapper);

		ConnectionProvider prov = workspace.getResource(ConnectionProviderProvider.class, connId);

		return new SimpleSQLFeatureStore(prov, srs, stmt, name, ns, prefix, bbox, lods, metadata);
	}

}
