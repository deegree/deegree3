/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.feature.persistence.shape;

import java.net.URL;

import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * {@link FeatureStoreProvider} for the {@link ShapeFeatureStore}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class ShapeFeatureStoreProvider extends FeatureStoreProvider {

	private static final String CONFIG_NS = "http://www.deegree.org/datasource/feature/shape";

	static final String CONFIG_JAXB_PACKAGE = "org.deegree.feature.persistence.shape.jaxb";

	private static final URL CONFIG_SCHEMA = ShapeFeatureStoreProvider.class
		.getResource("/META-INF/schemas/datasource/feature/shape/shape.xsd");

	static class Mapping {

		String fieldname;

		String propname;

		boolean index;

		Mapping(String fieldname, String propname, boolean index) {
			this.fieldname = fieldname;
			this.propname = propname;
			this.index = index;
		}

	}

	@Override
	public String getNamespace() {
		return CONFIG_NS;
	}

	@Override
	public ResourceMetadata<FeatureStore> createFromLocation(Workspace workspace,
			ResourceLocation<FeatureStore> location) {
		return new ShapeFeatureStoreMetadata(workspace, location, this);
	}

	@Override
	public URL getSchema() {
		return CONFIG_SCHEMA;
	}

}