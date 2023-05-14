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
package org.deegree.metadata.iso.persistence.memory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.iso.persistence.memory.jaxb.ISOMemoryMetadataStoreConfig;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;

/**
 * Responsible for building memory iso stores.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class IsoMemoryMetadataStoreBuilder implements ResourceBuilder<MetadataStore<? extends MetadataRecord>> {

	private ISOMemoryMetadataStoreConfig config;

	private ResourceMetadata<MetadataStore<? extends MetadataRecord>> metadata;

	public IsoMemoryMetadataStoreBuilder(ISOMemoryMetadataStoreConfig config,
			ResourceMetadata<MetadataStore<? extends MetadataRecord>> metadata) {
		this.config = config;
		this.metadata = metadata;
	}

	@Override
	public MetadataStore<? extends MetadataRecord> build() {
		List<File> recordDirectories = new ArrayList<File>();
		File insertDirectory = null;
		try {
			List<String> isoRecordDirectories = config.getISORecordDirectory();
			for (String isoRecordDirectory : isoRecordDirectories) {
				recordDirectories.add(metadata.getLocation().resolveToFile(isoRecordDirectory));
			}
			if (config.getInsertDirectory() != null) {
				insertDirectory = metadata.getLocation().resolveToFile(config.getInsertDirectory());
			}
			else {
				insertDirectory = recordDirectories.get(0);
			}

			return new ISOMemoryMetadataStore(recordDirectories, insertDirectory, metadata);
		}
		catch (Exception e) {
			String msg = "Error setting up iso memory meatadata store from configuration: " + e.getMessage();
			throw new ResourceInitException(msg, e);
		}
	}

}
