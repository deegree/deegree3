/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.metadata.iso.persistence.memory;

import java.io.IOException;
import java.net.URL;

import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.metadata.persistence.MetadataStoreProvider;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * {@link MetadataStoreProvider} for the {@link ISOMemoryMetadataStore}.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class ISOMemoryMetadataStoreProvider extends MetadataStoreProvider {

	private final static String CONFIG_NAMESPACE = "http://www.deegree.org/datasource/metadata/iso19139/memory";

	private final static URL CONFIG_SCHEMA = ISOMemoryMetadataStore.class
		.getResource("/META-INF/schemas/datasource/metadata/iso19139/memory/memory.xsd");

	@Override
	public String[] getCreateStatements(SQLDialect dbType) throws IOException {
		return new String[0];
	}

	@Override
	public String[] getDropStatements(SQLDialect dbType) throws IOException {
		return new String[0];
	}

	@Override
	public String getNamespace() {
		return CONFIG_NAMESPACE;
	}

	@Override
	public ResourceMetadata<MetadataStore<? extends MetadataRecord>> createFromLocation(Workspace workspace,
			ResourceLocation<MetadataStore<? extends MetadataRecord>> location) {
		return new IsoMemoryMetadataStoreMetadata(workspace, location, this);
	}

	@Override
	public URL getSchema() {
		return CONFIG_SCHEMA;
	}

}
