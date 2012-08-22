//$$Header: $$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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

package org.deegree.graphics.charts;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A HashMp with keys and values. The difference here is that elements are inserted using a queue structure instead of a
 * hash function, so that first come first served
 *
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author: elmasri$
 *
 * @version $Revision$, $Date: 28 Mar 2008 11:04:49$
 * @param <K>
 * @param <V>
 */
public class QueuedMap<K, V> implements Map<K, V> {

    /**
     *
     */
    private static final long serialVersionUID = 6497684950402691072L;

    private Map<K, V> map = null;

    private LinkedHashSet<K> list = new LinkedHashSet<K>();

    /**
     *
     */
    public QueuedMap() {
        map = new TreeMap<K, V>();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#clear()
     */
    public void clear() {
        list.clear();
        map.clear();

    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey( Object key ) {
        return map.containsKey( key );
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue( Object value ) {
        return map.containsValue( value );
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#entrySet()
     */
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#get(java.lang.Object)
     */
    public V get( Object key ) {
        return map.get( key );
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#keySet()
     */
    public Set<K> keySet() {
        Set<K> set = new TreeSet<K>();
        set.addAll( list );
        return set;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public V put( K key, V value ) {
        list.remove( key );
        list.add( key );
        return map.put( key, value );
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll( Map<? extends K, ? extends V> t ) {
        list.addAll( t.keySet() );
        map.putAll( t );

    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#remove(java.lang.Object)
     */
    public V remove( Object key ) {
        list.remove( key );
        return map.remove( key );
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#size()
     */
    public int size() {
        return map.size();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#values()
     */
    public Collection<V> values() {
        return map.values();
    }
}
