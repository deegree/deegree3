/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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
package org.deegree.db.datasource;

import java.net.URL;

import org.deegree.db.ConnectionProvider;
import org.deegree.db.ConnectionProviderProvider;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * {@link ConnectionProviderProvider} for the {@link DataSourceConnectionProvider}.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
public class DataSourceConnectionProviderProvider extends ConnectionProviderProvider {

	static final String CONFIG_NAMESPACE = "http://www.deegree.org/connectionprovider/datasource";

	static final URL SCHEMA_URL = DataSourceConnectionProviderProvider.class
		.getResource("/META-INF/schemas/connectionprovider/datasource/datasource.xsd");

	@Override
	public String getNamespace() {
		return CONFIG_NAMESPACE;
	}

	@Override
	public ResourceMetadata<ConnectionProvider> createFromLocation(Workspace workspace,
			ResourceLocation<ConnectionProvider> location) {
		return new DataSourceConnectionProviderMetadata(workspace, location, this);
	}

	@Override
	public URL getSchema() {
		return SCHEMA_URL;
	}

}
