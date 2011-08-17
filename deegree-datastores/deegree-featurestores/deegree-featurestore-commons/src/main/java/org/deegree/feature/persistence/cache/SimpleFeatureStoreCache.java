//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/src/main/java/org/deegree/feature/persistence/cache/FeatureStoreCache.java $
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

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.deegree.feature.persistence.FeatureStore;
import org.deegree.gml.GMLObject;

/**
 * {@link FeatureStoreCache} that uses a Java's {@link SoftReference} as eviction strategy and allows to limit the maximum
 * number of cached objects.
 * 
 * @see FeatureStore
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 22192 $, $Date: 2010-01-25 20:00:06 +0100 (Mo, 25 Jan 2010) $
 */
public class SimpleFeatureStoreCache implements FeatureStoreCache {

    private static final int DEFAULT_LIMIT = 10000;

	private final Map<String, SoftReference<GMLObject>> idToObject;

    /**
     * Creates a new {@link SimpleFeatureStoreCache} instance that allows to store a default number of entries. 
     */
    public SimpleFeatureStoreCache() {
    	this (DEFAULT_LIMIT);
	}
	
    /**
     * Creates a new {@link SimpleFeatureStoreCache} instance that allows to store the specified number of entries.
     * 
     * @param maxEntries
     *      maximum number of cached objects
     */
    public SimpleFeatureStoreCache( int maxEntries ) {
        idToObject = Collections.synchronizedMap( new CacheMap( maxEntries ) );
    }

	@Override
    public GMLObject get( String id ) {
        SoftReference<GMLObject> ref = idToObject.get( id );
        if ( ref == null ) {
            return null;
        }
        return ref.get();
    }

    @Override
    public void add( GMLObject obj ) {
        idToObject.put( obj.getId(), new SoftReference<GMLObject>( obj ) );
    }

    @Override
    public void remove( String id ) {
        idToObject.remove( id );
    }

    @Override
    public void clear() {
        idToObject.clear();
    }

    private class CacheMap extends LinkedHashMap<String, SoftReference<GMLObject>> {

        private static final long serialVersionUID = 6368164113834314158L;

        private final int maxEntries;

        private CacheMap( int maxEntries ) {
            this.maxEntries = maxEntries;
        }

        @Override
        protected boolean removeEldestEntry( Map.Entry<String, SoftReference<GMLObject>> eldest ) {
            return size() > maxEntries;
        }
    }
}
