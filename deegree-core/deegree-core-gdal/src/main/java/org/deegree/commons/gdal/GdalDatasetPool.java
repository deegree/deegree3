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
package org.deegree.commons.gdal;

import static java.util.Collections.synchronizedMap;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.deegree.commons.gdal.pool.KeyedResourceFactory;
import org.deegree.commons.gdal.pool.LimitedKeyedResourcePool;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.slf4j.Logger;

/**
 * Pool for <code>GdalDataset</code> objects that limits the number of open datasets.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
public class GdalDatasetPool {

	private static final Logger LOG = getLogger(GdalDatasetPool.class);

	private final LimitedKeyedResourcePool<GdalDataset> pool;

	private final Map<File, ICRS> gdalFileToCrs = synchronizedMap(new HashMap<File, ICRS>());

	private final Map<File, Envelope> gdalFileToEnvelope = synchronizedMap(new HashMap<File, Envelope>());

	GdalDatasetPool(final int maxOpen) {
		final KeyedResourceFactory<GdalDataset> factory = new GdalDatasetFactory(this);
		pool = new LimitedKeyedResourcePool<GdalDataset>(factory, maxOpen);
	}

	public void addDataset(File file, ICRS crs) throws IOException {
		gdalFileToCrs.put(file, crs);
		GdalDataset dataset = null;
		try {
			dataset = new GdalDataset(file, crs);
			gdalFileToEnvelope.put(file, dataset.getEnvelope());
		}
		catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
		finally {
			if (dataset != null) {
				dataset.close();
			}
		}
	}

	public ICRS getCrs(File file) {
		return gdalFileToCrs.get(file);
	}

	public Envelope getEnvelope(File gdalFile) {
		return gdalFileToEnvelope.get(gdalFile);
	}

	public GdalDataset borrow(File file) throws NoSuchElementException, IllegalStateException, Exception {
		return pool.borrow(file.getCanonicalFile().toString());
	}

	public void returnDataset(GdalDataset dataset) {
		pool.returnObject(dataset);
	}

	void shutdown() {
		try {
			pool.close();
		}
		catch (Exception e) {
			LOG.error("Error closing KeyedObjectPool: " + e.getMessage());
		}
	}

}
