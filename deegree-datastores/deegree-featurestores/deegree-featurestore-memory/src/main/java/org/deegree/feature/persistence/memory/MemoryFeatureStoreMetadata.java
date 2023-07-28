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

package org.deegree.feature.persistence.memory;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;
import static org.deegree.feature.persistence.memory.NewMemoryFeatureStoreProvider.CONFIG_SCHEMA;

import org.deegree.db.ConnectionProvider;
import org.deegree.db.ConnectionProviderProvider;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.memory.jaxb.MemoryFeatureStoreConfig;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceMetadata;
import org.deegree.workspace.standard.AbstractResourceProvider;
import org.deegree.workspace.standard.DefaultResourceIdentifier;

/**
 * <code>MemoryFeatureStoreMetadata</code>
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class MemoryFeatureStoreMetadata extends AbstractResourceMetadata<FeatureStore> {

	public MemoryFeatureStoreMetadata(Workspace workspace, ResourceLocation<FeatureStore> location,
			AbstractResourceProvider<FeatureStore> provider) {
		super(workspace, location, provider);
	}

	@Override
	public ResourceBuilder<FeatureStore> prepare() {
		try {
			MemoryFeatureStoreConfig config = (MemoryFeatureStoreConfig) unmarshall(
					"org.deegree.feature.persistence.memory.jaxb", CONFIG_SCHEMA, location.getAsStream(), workspace);
			dependencies
				.add(new DefaultResourceIdentifier<ConnectionProvider>(ConnectionProviderProvider.class, "LOCK_DB"));
			return new MemoryFeatureStoreBuilder(this, config, workspace);
		}
		catch (Exception e) {
			throw new ResourceInitException(e.getLocalizedMessage(), e);
		}
	}

}
