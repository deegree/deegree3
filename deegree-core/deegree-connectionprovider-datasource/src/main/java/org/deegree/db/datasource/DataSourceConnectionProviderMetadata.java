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

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;
import static org.deegree.db.datasource.DataSourceConnectionProviderProvider.SCHEMA_URL;
import static org.slf4j.LoggerFactory.getLogger;

import org.deegree.db.ConnectionProvider;
import org.deegree.db.ConnectionProviderProvider;
import org.deegree.db.datasource.jaxb.DataSourceConnectionProvider;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceMetadata;
import org.slf4j.Logger;

/**
 * {@link ConnectionProviderMetadata} for the {@link DataSourceConnectionProvider}.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
class DataSourceConnectionProviderMetadata extends AbstractResourceMetadata<ConnectionProvider> {

	private static final Logger LOG = getLogger(DataSourceConnectionProviderMetadata.class);

	private static final String JAXB_PACKAGE = "org.deegree.db.datasource.jaxb";

	DataSourceConnectionProviderMetadata(final Workspace workspace, final ResourceLocation<ConnectionProvider> location,
			final ConnectionProviderProvider provider) {
		super(workspace, location, provider);
	}

	@Override
	public ResourceBuilder<ConnectionProvider> prepare() {
		try {
			final Object cfg = unmarshall(JAXB_PACKAGE, SCHEMA_URL, location.getAsStream(), workspace);
			return new DataSourceConnectionProviderBuilder((DataSourceConnectionProvider) cfg, this, workspace);
		}
		catch (Exception e) {
			LOG.error(e.getLocalizedMessage(), e);
			throw new ResourceInitException(e.getLocalizedMessage(), e);
		}
	}

}
