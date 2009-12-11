//$HeadURL$
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
package org.deegree.feature.persistence.cache;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.deegree.feature.persistence.FeatureStore;
import org.deegree.gml.GMLObject;

/**
 * Cache for persistent {@link GMLObject} instances stored in a {@link FeatureStore}.
 * 
 * @see FeatureStore
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FeatureStoreCache {

    private final Map<String, GMLObject> idToObject;

    /**
     * @param maxEntries
     */
    @SuppressWarnings("synthetic-access")
    public FeatureStoreCache( int maxEntries ) {
        idToObject = Collections.synchronizedMap( new CacheMap( maxEntries ) );
    }

    /**
     * Returns the object with the specified id (if it exists in the cache).
     * 
     * @param id
     *            id of the object
     * @return the object with the specified id, or <code>null</code> if it is not present in the cache
     */
    public GMLObject get( String id ) {
        return idToObject.get( id );
    }

    /**
     * Adds the given object to the cache.
     * 
     * @param obj
     *            object, must not be <code>null</code>
     */
    public void add( GMLObject obj ) {
        idToObject.put( obj.getId(), obj );
    }

    /**
     * Removes the object with the specified id from the cache (if it exists).
     * 
     * @param id
     *            id of the object, must not be <code>null</code>
     */
    public void remove( String id ) {
        idToObject.remove( id );
    }

    /**
     * Clears the cache, removing all objects.
     */
    public void clear() {
        idToObject.clear();
    }

    private class CacheMap extends LinkedHashMap<String, GMLObject> {

        private static final long serialVersionUID = 6368164113834314158L;

        private final int maxEntries;

        private CacheMap( int maxEntries ) {
            this.maxEntries = maxEntries;
        }

        @Override
        protected boolean removeEldestEntry( Map.Entry<String, GMLObject> eldest ) {
            return size() > maxEntries;
        }
    }
}
