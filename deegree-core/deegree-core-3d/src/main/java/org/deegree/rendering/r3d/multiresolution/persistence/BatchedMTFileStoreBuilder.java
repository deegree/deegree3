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
package org.deegree.rendering.r3d.multiresolution.persistence;

import java.net.URL;

import org.deegree.cs.persistence.CRSManager;
import org.deegree.rendering.r3d.jaxb.batchedmt.BatchedMTFileStoreConfig;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;

/**
 * This class is responsible for building batched MT stores.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class BatchedMTFileStoreBuilder implements ResourceBuilder<BatchedMTStore> {

	private BatchedMTFileStoreConfig config;

	private ResourceMetadata<BatchedMTStore> metadata;

	public BatchedMTFileStoreBuilder(BatchedMTFileStoreConfig config, ResourceMetadata<BatchedMTStore> metadata) {
		this.config = config;
		this.metadata = metadata;
	}

	@Override
	public BatchedMTStore build() {
		BatchedMTStore bs = null;
		try {
			CRSManager.getCRSRef(config.getCrs());
			URL dir = metadata.getLocation().resolveToUrl(config.getDirectory());
			int maxDirectMem = config.getMaxDirectMemory().intValue();
			bs = new BatchedMTFileStore(dir, maxDirectMem, metadata);

		}
		catch (Exception e) {
			throw new ResourceInitException("Unable to build batched MT store: " + e.getLocalizedMessage(), e);
		}
		return bs;
	}

}
