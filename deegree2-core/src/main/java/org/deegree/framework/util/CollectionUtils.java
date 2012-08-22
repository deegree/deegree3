//$HeadURL$
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

package org.deegree.framework.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * <code>CollectionUtils</code> contains some functionality missing in <code>Arrays</code> and <code>Collections</code>.
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CollectionUtils {

    /**
     * @param <T>
     * @param col
     *            may not contain null values
     * @param sep
     *            the separating string
     * @return a comma separated list of #toString values
     */
    public static <T> String collectionToString( Collection<T> col, String sep ) {
        StringBuilder sb = new StringBuilder( 512 );

        Iterator<T> iter = col.iterator();

        while ( iter.hasNext() ) {
            sb.append( iter.next() );
            if ( iter.hasNext() ) {
                sb.append( sep );
            }
        }

        return sb.toString();
    }

    /**
     * Wraps a for loop and the creation of a new list.
     * 
     * @param <T>
     * @param <U>
     * @param col
     * @param mapper
     * @return a list where the mapper has been applied to each element in the map
     */
    public static <T, U> LinkedList<T> map( U[] col, Mapper<T, U> mapper ) {
        LinkedList<T> list = new LinkedList<T>();

        for ( U u : col ) {
            list.add( mapper.apply( u ) );
        }

        return list;
    }

    /**
     * Wraps a for loop and the creation of a new list.
     * 
     * @param <T>
     * @param <U>
     * @param col
     * @param mapper
     * @return a list where the mapper has been applied to each element in the map
     */
    public static <T, U> LinkedList<T> map( Collection<U> col, Mapper<T, U> mapper ) {
        LinkedList<T> list = new LinkedList<T>();

        for ( U u : col ) {
            list.add( mapper.apply( u ) );
        }

        return list;
    }

    /**
     * @param <T>
     * @param array
     * @param obj
     * @return true, if the object is contained within the array
     */
    public static <T> boolean contains( T[] array, T obj ) {
        for ( T t : array ) {
            if ( obj == t ) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param <T>
     * @param col
     * @param obj
     * @return true, if the object is contained within the collection
     */
    public static <T> boolean contains( Collection<T> col, T obj ) {
        for ( T t : col ) {
            if ( obj == t ) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param <T>
     * @param col
     * @param obj
     * @return true, if an equal object is contained
     */
    public static <T> boolean containsEqual( Collection<T> col, T obj ) {
        for ( T t : col ) {
            if ( obj.equals( t ) ) {
                return true;
            }
        }

        return false;
    }

    /**
     * Attention: runs in n*n
     * 
     * @param <T>
     * @param col
     * @param other
     * @return true, if all elements in col have an equal in other
     */
    public static <T> boolean containsAllEqual( Collection<T> col, Collection<T> other ) {
        for ( T t : col ) {
            boolean contains = false;
            inner: for ( T u : other ) {
                if ( t.equals( u ) ) {
                    contains = true;
                    break inner;
                }
            }
            if ( !contains ) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param <T>
     * @param array
     * @param pred
     * @return the first object for which the predicate is true, or null
     */
    public static <T> T find( T[] array, Predicate<T> pred ) {
        for ( T t : array ) {
            if ( pred.eval( t ) ) {
                return t;
            }
        }

        return null;
    }

    /**
     * @param <T>
     * @param col
     * @param pred
     * @return the first object for which the predicate is true, or null
     */
    public static <T> T find( Collection<T> col, Predicate<T> pred ) {
        for ( T t : col ) {
            if ( pred.eval( t ) ) {
                return t;
            }
        }

        return null;
    }

    /**
     * @param <T>
     * @param col
     * @param pred
     * @return only those T, for which the pred is true
     */
    public static <T> LinkedList<T> filter( Collection<T> col, Predicate<T> pred ) {

        LinkedList<T> list = new LinkedList<T>();
        for ( T t : col ) {
            if ( pred.eval( t ) ) {
                list.add( t );
            }
        }

        return list;
    }

    /**
     * @param <T>
     * @param identity
     * @param col
     * @param folder
     * @return the folded value
     */
    public static <T> T fold( T identity, Collection<T> col, Folder<T> folder ) {
        if ( col.isEmpty() ) {
            return identity;
        }

        Iterator<T> i = col.iterator();

        T acc = i.next();

        while ( i.hasNext() ) {
            acc = folder.fold( acc, i.next() );
        }

        return acc;
    }

    /**
     * <code>Predicate</code> defines a boolean predicate function interface.
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     * @param <T>
     *            the type of the predicate function's argument
     */
    public static interface Predicate<T> {
        /**
         * @param t
         * @return true, if the predicate is satisfied
         */
        public boolean eval( T t );
    }

    /**
     * <code>Mapper</code> gives a name to a simple function.
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     * @param <T>
     *            the return type of the function
     * @param <U>
     *            the argument type of the function
     */
    public static interface Mapper<T, U> {
        /**
         * @param u
         * @return an implementation defined value
         */
        public T apply( U u );
    }

    /**
     * <code>Folder</code>
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     * @param <T>
     */
    public static interface Folder<T> {
        /**
         * @param t1
         * @param t2
         * @return the folded value
         */
        public T fold( T t1, T t2 );
    }

    /**
     *
     */
    public static final Mapper<String, Object> TOSTRINGS = new Mapper<String, Object>() {
        public String apply( Object u ) {
            return u.toString();
        }
    };

    /**
     * @param vals
     * @return the array as list
     */
    public static LinkedList<Integer> asList( int[] vals ) {
        LinkedList<Integer> list = new LinkedList<Integer>();
        for ( int i : vals ) {
            list.add( i );
        }
        return list;
    }

    /**
     * Not quite zip...
     * 
     * @param <T>
     * @param <U>
     * @param col
     * @return a map with the first pair components mapping to the second
     */
    public static <T, U> LinkedHashMap<T, U> unzip( Collection<Pair<T, U>> col ) {
        LinkedHashMap<T, U> map = new LinkedHashMap<T, U>( col.size() );
        for ( Pair<T, U> pair : col ) {
            map.put( pair.first, pair.second );
        }
        return map;
    }

    /**
     * Not quite unzip...
     * 
     * @param <T>
     * @param <U>
     * @param map
     * @return a list with the keys paired with their values
     */
    public static <T, U> LinkedList<Pair<T, U>> zip( Map<T, U> map ) {
        LinkedList<Pair<T, U>> list = new LinkedList<Pair<T, U>>();
        for ( T key : map.keySet() ) {
            list.add( new Pair<T, U>( key, map.get( key ) ) );
        }
        return list;
    }

    /**
     * @param <T>
     * @param ts
     * @return the last element or null
     */
    public static <T> T last( T[] ts ) {
        return ts == null || ts.length == 0 ? null : ts[ts.length - 1];
    }

}
