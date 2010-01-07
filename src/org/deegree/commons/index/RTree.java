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

package org.deegree.commons.index;

import static java.lang.Math.min;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.deegree.commons.utils.Pair;
import org.deegree.geometry.Envelope;
import org.slf4j.Logger;

/**
 * <code>RTree</code> Query will return the Objects of the index
 * 
 * <p>
 * The bulk insertion mechanism is an implementation of STRTree:
 * 
 * Scott T. Leutenegger, Jeffrey M. Edgington, Mario A. Lopez: STR: A SIMPLE AND EFFICIENT ALGORITHM FOR R-TREE PACKING
 * (1997)
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * @param <T>
 *            of objects the tree will hold.
 * 
 */
public class RTree<T> extends SpatialIndex<T> {

    private static final Logger LOG = getLogger( RTree.class );

    private Entry<T>[] root;

    private float[] bbox;

    private int maxNumberOfObjects = 128;

    // rb: output the warning
    boolean outputWarning = true;

    /**
     * @param rootEnvelope
     *            of this rtree.
     * @param numberOfObjects
     *            each rectangle shall hold before splitting.
     */
    public RTree( Envelope rootEnvelope, int numberOfObjects ) {
        if ( rootEnvelope == null ) {
            throw new NullPointerException( "The root envelope may not be null" );
        }
        this.bbox = createEnvelope( rootEnvelope );
        if ( numberOfObjects > 0 ) {
            this.maxNumberOfObjects = numberOfObjects;
        }
        this.root = null;
    }

    /**
     * Rereads this RTree from a serialized file to which is should point.
     * 
     * @param is
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public RTree( InputStream is ) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream( new BufferedInputStream( is ) );
        maxNumberOfObjects = in.readInt();
        bbox = (float[]) in.readObject();
        root = (Entry[]) in.readObject();
        in.close();
    }

    private LinkedList<T> query( final float[] bbox, Entry<T>[] node ) {

        LinkedList<T> list = new LinkedList<T>();

        for ( Entry<T> e : node ) {
            if ( intersects( bbox, e.bbox, 2 ) ) {
                if ( e.next == null ) {
                    list.add( e.entryValue );
                    // rb: uncommented
                    if ( ( list.size() >= maxNumberOfObjects * 10 && outputWarning ) ) {
                        outputWarning = false;
                        LOG.warn(
                                  "Collecting features should stop because {} features were loaded, which was 10 times larger then the maxNumberOfObjects {} (which is currently hardcoded). Continue filling the list though.",
                                  list.size(), this.maxNumberOfObjects );
                        // return list;
                    }
                } else {
                    list.addAll( query( bbox, e.next ) );
                    if ( ( list.size() >= maxNumberOfObjects * 10 ) && outputWarning ) {
                        outputWarning = false;
                        LOG.warn(
                                  "Collecting features should stop because {} features were loaded, which was 10 times larger then the maxNumberOfObjects {} (which is currently hardcoded). Continue filling the list though.",
                                  list.size(), this.maxNumberOfObjects );
                        // return list;
                    }
                }
            }
        }

        return list;
    }

    /**
     * @param env
     * @return a list of objects intersecting the given boundingbox.
     */
    @Override
    public LinkedList<T> query( Envelope env ) {
        if ( root != null ) {
            final float[] bbox = new float[] { (float) env.getMin().get0(), (float) env.getMin().get1(),
                                              (float) env.getMax().get0(), (float) env.getMax().get1() };

            if ( intersects( bbox, this.bbox, 2 ) ) {
                return query( bbox, root );
            }
        }
        return new LinkedList<T>();
    }

    private TreeMap<Float, LinkedList<Pair<float[], ?>>> sortEnvelopes( Collection<Pair<Envelope, ?>> rects, int byIdx ) {
        TreeMap<Float, LinkedList<Pair<float[], ?>>> map = new TreeMap<Float, LinkedList<Pair<float[], ?>>>();

        for ( Pair<Envelope, ?> p : rects ) {
            float[] env = createEnvelope( p.first );
            float d = env[byIdx] + ( env[byIdx + 2] - env[byIdx] ) / 2;
            if ( !map.containsKey( d ) ) {
                map.put( d, new LinkedList<Pair<float[], ?>>() );
            }
            map.get( d ).add( new Pair<float[], Object>( env, p.second ) );
        }

        return map;
    }

    private TreeMap<Float, LinkedList<Pair<float[], ?>>> sort( Collection<Pair<float[], ?>> rects, int byIdx ) {
        TreeMap<Float, LinkedList<Pair<float[], ?>>> map = new TreeMap<Float, LinkedList<Pair<float[], ?>>>();

        for ( Pair<float[], ?> p : rects ) {
            float d = p.first[byIdx] + ( p.first[byIdx + 2] - p.first[byIdx] ) / 2;
            if ( !map.containsKey( d ) ) {
                map.put( d, new LinkedList<Pair<float[], ?>>() );
            }
            map.get( d ).add( p );
        }

        return map;
    }

    private LinkedList<LinkedList<Pair<float[], ?>>> slice( TreeMap<Float, LinkedList<Pair<float[], ?>>> map, int limit ) {
        LinkedList<LinkedList<Pair<float[], ?>>> list = new LinkedList<LinkedList<Pair<float[], ?>>>();

        LinkedList<Pair<float[], ?>> cur = new LinkedList<Pair<float[], ?>>();
        Iterator<LinkedList<Pair<float[], ?>>> iter = map.values().iterator();
        LinkedList<Pair<float[], ?>> l = iter.next();
        while ( iter.hasNext() || l.size() > 0 ) {
            if ( cur.size() == limit ) {
                list.add( cur );
                cur = new LinkedList<Pair<float[], ?>>();
            }
            if ( l.isEmpty() ) {
                l = iter.next();
            }
            cur.add( l.poll() );
        }
        list.add( cur );

        return list;
    }

    /**
     * Builds the index from the given objects with their envelope.
     * 
     * @param listOfObjects
     */
    @SuppressWarnings("unchecked")
    @Override
    public void insertBulk( List<Pair<Envelope, T>> listOfObjects ) {
        // rb: dirty cast because the ? will not accept T... m*rf*king generics.
        root = buildTree( (List) listOfObjects );
    }

    @SuppressWarnings("unchecked")
    private Entry<T>[] buildTree( List<Pair<Envelope, ?>> rects ) {
        if ( rects.size() <= maxNumberOfObjects ) {
            Entry<T>[] node = new Entry[rects.size()];
            for ( int i = 0; i < rects.size(); ++i ) {
                node[i] = new Entry<T>();
                node[i].bbox = createEnvelope( rects.get( i ).first );
                if ( rects.get( i ).second instanceof Entry[] ) {
                    node[i].next = (Entry[]) rects.get( i ).second;
                } else {
                    node[i].entryValue = (T) rects.get( i ).second;
                }
            }
            return node;
        }

        LinkedList<LinkedList<Pair<float[], ?>>> slices = slice( sortEnvelopes( rects, 0 ), maxNumberOfObjects
                                                                                            * maxNumberOfObjects );
        ArrayList<Pair<float[], ?>> newRects = new ArrayList<Pair<float[], ?>>();

        for ( LinkedList<Pair<float[], ?>> slice : slices ) {
            TreeMap<Float, LinkedList<Pair<float[], ?>>> map = sort( slice, 1 );

            Iterator<LinkedList<Pair<float[], ?>>> iter = map.values().iterator();
            LinkedList<Pair<float[], ?>> list = iter.next();
            int idx = 0;
            while ( idx < slice.size() ) {
                Entry<T>[] node = new Entry[min( maxNumberOfObjects, slice.size() - idx )];
                float[] bbox = null;
                for ( int i = 0; i < maxNumberOfObjects; ++i, ++idx ) {
                    if ( idx < slice.size() ) {
                        if ( list.isEmpty() ) {
                            list = iter.next();
                        }
                        Pair<float[], ?> p = list.poll();
                        node[i] = new Entry<T>();
                        node[i].bbox = p.first;
                        if ( p.second instanceof Entry[] ) {
                            node[i].next = (Entry[]) p.second;
                        } else {
                            node[i].entryValue = (T) p.second;
                        }
                        if ( bbox == null ) {
                            bbox = new float[] { p.first[0], p.first[1], p.first[2], p.first[3] };
                        } else {
                            for ( int k = 0; k < 2; ++k ) {
                                if ( bbox[k] > p.first[k] ) {
                                    bbox[k] = p.first[k];
                                }
                            }
                            for ( int k = 2; k < 4; ++k ) {
                                if ( bbox[k] < p.first[k] ) {
                                    bbox[k] = p.first[k];
                                }
                            }
                        }
                    }
                }
                newRects.add( new Pair<float[], Entry[]>( bbox, node ) );
            }
        }

        return buildFromFloat( newRects );
    }

    /**
     * RB: this method is a duplicate from buildTree, but used only with float arrays. (Sorry no time to refactor
     * buildtree).
     * 
     * @param rects
     * @return
     */
    @SuppressWarnings("unchecked")
    private Entry<T>[] buildFromFloat( List<Pair<float[], ?>> rects ) {
        if ( rects.size() <= maxNumberOfObjects ) {
            Entry<T>[] node = new Entry[rects.size()];
            for ( int i = 0; i < rects.size(); ++i ) {
                node[i] = new Entry<T>();
                node[i].bbox = rects.get( i ).first;
                if ( rects.get( i ).second instanceof Entry[] ) {
                    node[i].next = (Entry[]) rects.get( i ).second;
                } else {
                    node[i].entryValue = (T) rects.get( i ).second;
                }
            }
            return node;
        }

        LinkedList<LinkedList<Pair<float[], ?>>> slices = slice( sort( rects, 0 ), maxNumberOfObjects
                                                                                   * maxNumberOfObjects );
        ArrayList<Pair<float[], ?>> newRects = new ArrayList<Pair<float[], ?>>();

        for ( LinkedList<Pair<float[], ?>> slice : slices ) {
            TreeMap<Float, LinkedList<Pair<float[], ?>>> map = sort( slice, 1 );

            Iterator<LinkedList<Pair<float[], ?>>> iter = map.values().iterator();
            LinkedList<Pair<float[], ?>> list = iter.next();
            int idx = 0;
            while ( idx < slice.size() ) {
                Entry<T>[] node = new Entry[min( maxNumberOfObjects, slice.size() - idx )];
                float[] bbox = null;
                for ( int i = 0; i < maxNumberOfObjects; ++i, ++idx ) {
                    if ( idx < slice.size() ) {
                        if ( list.isEmpty() ) {
                            list = iter.next();
                        }
                        Pair<float[], ?> p = list.poll();
                        node[i] = new Entry<T>();
                        node[i].bbox = p.first;
                        if ( p.second instanceof Entry[] ) {
                            node[i].next = (Entry[]) p.second;
                        } else {
                            node[i].entryValue = (T) p.second;
                        }
                        if ( bbox == null ) {
                            bbox = new float[] { p.first[0], p.first[1], p.first[2], p.first[3] };
                        } else {
                            for ( int k = 0; k < 2; ++k ) {
                                if ( bbox[k] > p.first[k] ) {
                                    bbox[k] = p.first[k];
                                }
                            }
                            for ( int k = 2; k < 4; ++k ) {
                                if ( bbox[k] < p.first[k] ) {
                                    bbox[k] = p.first[k];
                                }
                            }
                        }
                    }
                }
                newRects.add( new Pair<float[], Entry[]>( bbox, node ) );
            }
        }

        return buildFromFloat( newRects );
    }

    /**
     * @param output
     * @throws IOException
     */
    public void write( RandomAccessFile output )
                            throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream( bos );
        out.writeInt( maxNumberOfObjects );
        out.writeObject( bbox );
        out.writeObject( root );
        out.close();
        output.write( bos.toByteArray() );
        // output.close();
    }

    static class Entry<T> implements Serializable {
        private static final long serialVersionUID = -4272761420705520561L;

        float[] bbox;

        T entryValue;

        Entry<T>[] next;
    }

    @Override
    public void clear() {
        this.root = null;
    }

    @Override
    public boolean remove( T object ) {
        throw new UnsupportedOperationException( "Deletion of a single object should be implemented" );
    }

    @Override
    public boolean insert( Envelope envelope, T object ) {
        throw new UnsupportedOperationException( "Inserting of a single object should be implemented" );
    }

}
