/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.feature.persistence.cache;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.Envelope;

/**
 * Cache for {@link Envelope}s of {@link FeatureType}s stored in a {@link FeatureStore}.
 *
 * @see FeatureStore
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public interface BBoxCache {

	/**
	 * Returns the cached envelope for features of the specified type.
	 * @param ftName name of the feature type, must not be <code>null</code>
	 * @return the envelope (using the storage CRS), or <code>null</code>, if the feature
	 * type does not have an envelope (no geometry properties or no instances)
	 * @throws IllegalArgumentException if the cache does not contain information on the
	 * specified feature type
	 */
	Envelope get(QName ftName);

	/**
	 * Updates the envelope for the specified type.
	 * @param ftName name of the feature type, must not be <code>null</code>
	 * @param bbox new envelope (using the storage CRS), or <code>null</code>, if the
	 * feature type does not have an envelope (no geometry properties or no instances)
	 */
	void set(QName ftName, Envelope bbox);

	/**
	 * Returns true, if the the specified feature type is known to the cache.
	 * @param ftName name of the feature type, must not be <code>null</code>
	 * @return true, if the feature type is known, false otherwise
	 */
	boolean contains(QName ftName);

	/**
	 * Ensures that the cache is persisted.
	 * @throws IOException
	 */
	void persist() throws IOException;

}
