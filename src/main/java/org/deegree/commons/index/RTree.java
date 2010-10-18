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

import static org.slf4j.LoggerFactory.getLogger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.deegree.commons.utils.Pair;
import org.slf4j.Logger;

/**
 * <code>RTree</code>
 * 
 * NB: When creating node entry arrays i.e. new RTree.NodeEntry[bigM + 1], be sure to always use dimension bigM + 1, as
 * this length is expected (otherwise NullPointerExceptions will pop up).
 * 
 * <p>
 * The bulk insertion mechanism is an implementation of STRTree:
 * 
 * Scott T. Leutenegger, Jeffrey M. Edgington, Mario A. Lopez: STR: A SIMPLE AND EFFICIENT ALGORITHM FOR R-TREE PACKING
 * (1997)
 * 
 * <p>
 * The <code>insert<code> method (more precisely <code>chooseSubtree</code> and <code>split</code>) have been
 * implemented after the paper:
 * 
 * Norbert Beckmann, Bernhard Seeger: A Revised R*-tree in Comparison with Related Index Structures (2009)
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * @param <T>
 *            of objects the tree will hold.
 * 
 */
public class RTree<T> extends SpatialIndex<T> {

    private static final Logger LOG = getLogger( RTree.class );

    private static final double EPS5 = 1E-5;

    protected NodeEntry[] root;

    /* entire bbox of the tree */
    private float[] rootbbox;

    /* default value, usually an actual one is passed to the constructor */
    private int bigM = 128;

    private int smallm;

    /* --------------------------- */

    /* internal constants */
    private final double ASYM = 1.0;

    private final double S = 0.5;

    // rb: output the warning
    boolean outputWarning = true;

    private boolean extraFlag;

    private List<NodeEntry> removedNodeEntries;

    /**
     * @param rootEnvelope
     *            of this rtree.
     * @param numberOfObjects
     *            each rectangle shall hold before splitting.
     */
    public RTree( float[] rootEnvelope, int numberOfObjects ) {
        if ( rootEnvelope == null ) {
            throw new NullPointerException( "The root envelope may not be null" );
        }
        this.rootbbox = Arrays.copyOf( rootEnvelope, rootEnvelope.length );
        if ( numberOfObjects > 0 ) {
            this.bigM = numberOfObjects;
        }
        this.smallm = ( bigM / 5 ) == 0 ? 1 : ( bigM / 5 );
        this.root = null;
    }

    /**
     * @param <T>
     * @param storagePath
     *            absolute path to the file
     * @return a new instance
     * @throws IOException
     */
    public static <T> RTree<T> loadFromDisk( String storagePath )
                            throws IOException {
        RTree<T> tree = null;
        File storageFile = new File( storagePath );

        DataInputStream dis = new DataInputStream( new FileInputStream( storageFile ) );
        float[] loadedBbox = loadBboxFromDisk( dis );
        int loadedBigM = dis.readInt();

        tree = new RTree<T>( loadedBbox, loadedBigM );

        tree.root = tree.loadNodeFromDisk( dis );

        dis.close();

        return tree;
    }

    /**
     * @param dis
     * @throws IOException
     */
    private static float[] loadBboxFromDisk( DataInputStream dis )
                            throws IOException {
        float xmin = dis.readFloat();
        float ymin = dis.readFloat();
        float xmax = dis.readFloat();
        float ymax = dis.readFloat();
        return new float[] { xmin, ymin, xmax, ymax };
    }

    /**
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    private NodeEntry[] loadNodeFromDisk( DataInputStream dis )
                            throws IOException {
        // the number of non-null entries in this node
        int n = dis.readInt();

        NodeEntry[] node = null;
        try {
            node = new RTree.NodeEntry[bigM + 1];
        } catch ( OutOfMemoryError e ) {
            LOG.error( "Out of memory when reading rtree." );
            throw new IOException( "out of memory" );
        }

        for ( int i = 0; i < n; i++ ) {
            node[i] = new RTree.NodeEntry();
            node[i].bbox = loadBboxFromDisk( dis );
            Long value = dis.readLong();
            if ( Long.MIN_VALUE == value ) {
                node[i].entryValue = null;
                node[i].next = loadNodeFromDisk( dis );
            } else {
                node[i].entryValue = (T) value;
            }
        }

        return node;
    }

    private LinkedList<T> query( final float[] bbox, NodeEntry[] node ) {

        LinkedList<T> list = new LinkedList<T>();

        for ( NodeEntry e : node ) {
            if ( e != null && intersects( bbox, e.bbox, 2 ) ) {
                if ( e.next == null ) {
                    list.add( e.entryValue );
                    // rb: uncommented
                    if ( ( list.size() >= bigM * 10 && outputWarning ) ) {
                        outputWarning = false;
                        // LOG.warn(
                        // "Collecting features should stop because {} features were loaded, which was 10 times larger then the maxNumberOfObjects {} (which is currently hardcoded). Continue filling the list though.",
                        // list.size(), this.maxNumberOfObjects );
                        // return list;
                    }
                } else {
                    list.addAll( query( bbox, e.next ) );
                    if ( ( list.size() >= bigM * 10 ) && outputWarning ) {
                        outputWarning = false;
                        // LOG.warn(
                        // "Collecting features should stop because {} features were loaded, which was 10 times larger then the maxNumberOfObjects {} (which is currently hardcoded). Continue filling the list though.",
                        // list.size(), this.maxNumberOfObjects );
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
    public LinkedList<T> query( float[] env ) {
        if ( root != null ) {
            // final float[] bbox = new float[] { env.getMin().get0(), env.getMin().get1(),
            // env.getMax().get0(), env.getMax().get1() };

            if ( intersects( env, this.rootbbox, 2 ) ) {
                return query( env, root );
            }
        }

        LOG.debug( "Querying either a null tree or with a envelope that does not intersect it... returning." );
        return new LinkedList<T>();
    }

    @SuppressWarnings("unchecked")
    private TreeMap<Float, LinkedList<Pair<float[], ?>>> sortEnvelopes( Collection<Pair<float[], ?>> rects, int byIdx ) {
        TreeMap<Float, LinkedList<Pair<float[], ?>>> map = new TreeMap<Float, LinkedList<Pair<float[], ?>>>();

        for ( Pair<float[], ?> p : rects ) {
            float[] env = p.first;
            float d = env[byIdx] + ( env[byIdx + 2] - env[byIdx] ) / 2;
            if ( !map.containsKey( d ) ) {
                map.put( d, new LinkedList<Pair<float[], ?>>() );
            }
            map.get( d ).add( new Pair( env, p.second ) );
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
    public void insertBulk( List<Pair<float[], T>> listOfObjects ) {
        root = buildTree( (List) listOfObjects );
    }

    @SuppressWarnings("unchecked")
    private NodeEntry[] buildTree( List<Pair<float[], ?>> rects ) {
        if ( rects.size() <= bigM ) {
            // all of them are leaves
            NodeEntry[] node = new RTree.NodeEntry[bigM + 1];
            for ( int i = 0; i < rects.size(); ++i ) {
                node[i] = new RTree.NodeEntry();
                node[i].bbox = rects.get( i ).first;// createEnvelope( rects.get( i ).first );
                // if ( rects.get( i ).second instanceof NodeEntry[] ) {
                // node[i].next = (NodeEntry[]) rects.get( i ).second;
                // } else {
                node[i].entryValue = (T) rects.get( i ).second;
                // }
            }
            return node;
        }

        LinkedList<LinkedList<Pair<float[], ?>>> slices = slice( sortEnvelopes( rects, 0 ), bigM * bigM );
        ArrayList<Pair<float[], NodeEntry[]>> newRects = new ArrayList<Pair<float[], NodeEntry[]>>();

        for ( LinkedList<Pair<float[], ?>> slice : slices ) {
            TreeMap<Float, LinkedList<Pair<float[], ?>>> map = sort( slice, 1 );

            Iterator<LinkedList<Pair<float[], ?>>> iter = map.values().iterator();
            LinkedList<Pair<float[], ?>> list = iter.next();
            int idx = 0;
            while ( idx < slice.size() ) {
                NodeEntry[] node = new RTree.NodeEntry[bigM + 1];
                float[] bbox = null;
                for ( int i = 0; i < bigM; ++i, ++idx ) {
                    if ( idx < slice.size() ) {
                        if ( list.isEmpty() ) {
                            list = iter.next();
                        }
                        Pair<float[], ?> p = list.poll();
                        node[i] = new RTree.NodeEntry();
                        node[i].bbox = p.first;
                        if ( p.second instanceof RTree.NodeEntry[] ) {
                            node[i].next = (NodeEntry[]) p.second;
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
                newRects.add( new Pair<float[], NodeEntry[]>( bbox, node ) );
            }
        }

        return buildFromFloat( (List) newRects );
    }

    /**
     * RB: this method is a duplicate from buildTree, but used only with float arrays. (Sorry no time to refactor
     * buildtree).
     * 
     * @param rects
     */
    @SuppressWarnings("unchecked")
    private NodeEntry[] buildFromFloat( List<Pair<float[], ?>> rects ) {
        if ( rects.size() <= bigM ) {
            NodeEntry[] node = new RTree.NodeEntry[bigM + 1];
            for ( int i = 0; i < rects.size(); ++i ) {
                node[i] = new RTree.NodeEntry();
                node[i].bbox = rects.get( i ).first;
                if ( rects.get( i ).second instanceof RTree.NodeEntry[] ) {
                    node[i].next = (NodeEntry[]) rects.get( i ).second;
                } else {
                    node[i].entryValue = (T) rects.get( i ).second;
                }
            }
            return node;
        }

        LinkedList<LinkedList<Pair<float[], ?>>> slices = slice( sort( rects, 0 ), bigM * bigM );
        ArrayList<Pair<float[], NodeEntry[]>> newRects = new ArrayList<Pair<float[], NodeEntry[]>>();

        for ( LinkedList<Pair<float[], ?>> slice : slices ) {
            TreeMap<Float, LinkedList<Pair<float[], ?>>> map = sort( slice, 1 );

            Iterator<LinkedList<Pair<float[], ?>>> iter = map.values().iterator();
            LinkedList<Pair<float[], ?>> list = iter.next();
            int idx = 0;
            while ( idx < slice.size() ) {
                NodeEntry[] node = new RTree.NodeEntry[bigM + 1];
                float[] bbox = null;
                for ( int i = 0; i < bigM; ++i, ++idx ) {
                    if ( idx < slice.size() ) {
                        if ( list.isEmpty() ) {
                            list = iter.next();
                        }
                        Pair<float[], ?> p = list.poll();
                        node[i] = new RTree.NodeEntry();
                        node[i].bbox = p.first;
                        if ( p.second instanceof RTree.NodeEntry[] ) {
                            node[i].next = (NodeEntry[]) p.second;
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
                newRects.add( new Pair<float[], NodeEntry[]>( bbox, node ) );
            }
        }

        return buildFromFloat( (List) newRects );
    }

    @Override
    public void clear() {
        this.root = null;
    }

    @Override
    public boolean remove( T object ) {
        if ( root == null ) {
            LOG.error( "The tree is empty. Nothing to remove." );
            return false;
        }
        TraceCell[] trace = findLeafWithValue( object, root );
        if ( trace != null ) {
            removeFromArray( trace[0].node, trace[0].index );
            removedNodeEntries = new ArrayList<NodeEntry>();
            boolean remove = false;
            int nnl = notNullLength( trace[0].node );
            if ( nnl < smallm ) {
                remove = true;
                for ( int j = 0; j < nnl; j++ ) {
                    removedNodeEntries.add( trace[0].node[j] );
                }
            }
            condenseTree( trace, 1, remove );

            return true;
        }
        // the object to-be-removed was not found
        return false;
    }

    /**
     * Returns number of entries in the array that are not null.
     * 
     * @param node
     */
    private int notNullLength( NodeEntry[] node ) {
        for ( int i = 0; i < node.length; i++ ) {
            if ( node[i] == null ) {
                return i;
            }
        }
        return node.length;
    }

    /**
     * Extract and remove the specified index from the array
     * 
     * @param node
     *            the array
     * @param index
     *            the index
     */
    private void removeFromArray( Object[] node, int index ) {
        for ( int i = index; i < node.length - 1; i++ ) {
            node[i] = node[i + 1];
        }
        node[node.length - 1] = null;
    }

    /**
     * Adjust the ancestors of the leaf node that just shrinked.
     * 
     * @param trace
     *            the descendence path starting with the leaf node
     * @param traceIndex
     *            the current index in the trace
     * @param removed
     *            boolean, indicates whether the child node has been deleted (having < smallm entries) or not
     */
    private void condenseTree( TraceCell[] trace, int traceIndex, boolean removed ) {
        if ( traceIndex < trace.length ) {
            int entryIndex = trace[traceIndex].index;
            NodeEntry[] entries = trace[traceIndex].node;
            if ( removed ) {
                // removed child node, parent entry has to be removed
                removeFromArray( entries, entryIndex );

                if ( notNullLength( entries ) < smallm ) {
                    for ( int i = 0; i < entries.length; i++ ) {
                        if ( entries[i] != null ) {
                            addOrphanedEntries( entries[i] );
                            entries[i] = null;
                        }
                    }
                    condenseTree( trace, traceIndex + 1, true );

                } else {
                    condenseTree( trace, traceIndex + 1, false );
                    // insert orphaned nodes
                    for ( NodeEntry orphaned : removedNodeEntries ) {
                        insertNode( orphaned.bbox, orphaned.entryValue, trace[traceIndex].node );
                    }
                }
            } else {
                // adjust bounding box after one child entry has been removed
                trace[traceIndex].node[entryIndex].bbox = mbb( copyBoxesFromRange( trace[traceIndex - 1].node, 0,
                                                                                   trace[traceIndex - 1].node.length ) );
                condenseTree( trace, traceIndex + 1, false );
            }

        } else {
            if ( removed ) {
                // insert orphaned nodes into the empty tree
                for ( NodeEntry orphaned : removedNodeEntries ) {
                    insert( orphaned.bbox, orphaned.entryValue );
                }
            }
        }
    }

    /**
     * @param entry
     */
    private void addOrphanedEntries( NodeEntry entry ) {
        if ( entry.next == null ) {
            removedNodeEntries.add( entry );
            return;
        }
        NodeEntry[] nextLevelEntries = entry.next;
        for ( int i = 0; i < nextLevelEntries.length; i++ ) {
            if ( nextLevelEntries[i] != null ) {
                addOrphanedEntries( nextLevelEntries[i] );
            }
        }
    }

    /**
     * Returns an array of {@TraceCell} i.e. that contains a pairs of (node, index of the entry in the node)
     * 
     * @param object
     * @param entries
     */
    @SuppressWarnings("unchecked")
    private TraceCell[] findLeafWithValue( T object, NodeEntry[] entries ) {
        if ( entries[0].next == null ) {
            // leaf node, try to find object in one of the entries
            for ( int i = 0; i < entries.length; i++ ) {
                if ( entries[i] != null && entries[i].entryValue.equals( object.toString() ) ) {
                    return new RTree.TraceCell[] { new TraceCell( entries, i ) };
                }
            }
            return null;
        }

        // non-leaf nodes, continue depth-first search
        for ( int i = 0; i < entries.length; i++ ) {
            if ( entries[i] != null ) {
                TraceCell[] furtherTrace = findLeafWithValue( object, entries[i].next );
                if ( furtherTrace != null ) {
                    // add this node to the trace
                    TraceCell[] updatedResult = new RTree.TraceCell[furtherTrace.length + 1];
                    System.arraycopy( furtherTrace, 0, updatedResult, 0, furtherTrace.length );
                    updatedResult[furtherTrace.length] = new TraceCell( entries, i );
                    return updatedResult;
                }
            }
        }
        return null;
    }

    /**
     * @param bbox2
     */
    private double calculateArea( float[] bbox2 ) {
        return ( bbox2[2] - bbox2[0] ) * ( bbox2[3] - bbox2[1] );
    }

    /**
     * @return extra flag read from a file (used for hacking around buggy shp files)
     */
    public boolean getExtraFlag() {
        return extraFlag;
    }

    // class Node<T> {
    //
    // }

    class NodeEntry implements Serializable {
        private static final long serialVersionUID = -4272761420705520561L;

        float[] bbox;

        T entryValue;

        NodeEntry[] next;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean insert( float[] insertBox, T object ) {
        if ( root == null || hasNullEntries( root ) ) {
            NodeEntry newEntry = new NodeEntry();
            newEntry.bbox = insertBox;
            newEntry.entryValue = object;
            newEntry.next = null;

            root = new RTree.NodeEntry[bigM + 1];
            root[0] = newEntry;

            return true;
        }

        insertNode( insertBox, object, root );
        return true;
    }

    /**
     * Persist the r-tree to the given location.
     * 
     * @param storagePath
     */
    public void writeTreeToDisk( String storagePath ) {
        try {
            DataOutputStream output = new DataOutputStream( new FileOutputStream( new File( storagePath ) ) );
            writeBboxToDisk( output, this.rootbbox );
            output.writeInt( this.bigM );
            writeNodeToDisk( root, output );
            output.close();
        } catch ( IOException e ) {
            throw new RuntimeException( "Error while writing tree to disk. " + e.getMessage() );
        }
    }

    /**
     * @throws IOException
     */
    private void writeNodeToDisk( NodeEntry[] node, DataOutputStream output )
                            throws IOException {
        // write how many entries in the node are not-null
        output.writeInt( lastNotNull( node ) + 1 );
        for ( int i = 0; i < bigM; i++ ) {
            if ( node[i] != null ) {
                writeBboxToDisk( output, node[i].bbox );
                if ( node[i].entryValue != null ) {
                    output.writeLong( (Long) node[i].entryValue );
                } else {
                    output.writeLong( Long.MIN_VALUE );
                }

                if ( node[i].next != null ) {
                    writeNodeToDisk( node[i].next, output );
                }
            }
        }

    }

    /**
     * @throws IOException
     */
    private void writeBboxToDisk( DataOutputStream output, float[] boundingBox )
                            throws IOException {
        output.writeFloat( boundingBox[0] );
        output.writeFloat( boundingBox[1] );
        output.writeFloat( boundingBox[2] );
        output.writeFloat( boundingBox[3] );
    }

    @SuppressWarnings("unchecked")
    private boolean hasNullEntries( NodeEntry[] array ) {
        NodeEntry[] nullArray = new RTree.NodeEntry[bigM + 1];
        Arrays.fill( nullArray, null );
        return Arrays.equals( nullArray, array );
    }

    /*
     * Insert an object of value type T and bbox insertBox among the entries of the current node.
     */
    @SuppressWarnings("unchecked")
    private void insertNode( float[] insertBox, T object, NodeEntry[] entries ) {
        List<TraceCell> trace = new ArrayList<TraceCell>();
        NodeEntry[] leafNode = chooseLeaf( insertBox, entries, trace );

        NodeEntry newEntry = new RTree.NodeEntry();
        newEntry.bbox = insertBox;
        newEntry.entryValue = object;
        newEntry.next = null;

        int index = lastNotNull( leafNode );
        leafNode[index + 1] = newEntry;

        if ( leafNode[bigM] != null ) {
            int splitIndex = split( leafNode, insertBox, object );

            NodeEntry[] addedNode = new RTree.NodeEntry[bigM + 1];
            Arrays.fill( addedNode, null );
            System.arraycopy( leafNode, splitIndex + 1, addedNode, 0, bigM - splitIndex );
            Arrays.fill( leafNode, splitIndex + 1, bigM + 1, null );

            adjustTree( leafNode, addedNode, trace, trace.size() - 1 );

        } else {
            adjustTree( leafNode, null, trace, trace.size() - 1 );
        }
    }

    /**
     * Adjust the tree bottom-up after insertion, enlarging the bboxes of parent nodes and splitting the nodes when
     * needed.
     * 
     * @param leftEntries
     * @param rightEntries
     *            may be null, in case no split was performed a level lower
     * @param trace
     * @param traceIndex
     */
    @SuppressWarnings("unchecked")
    private void adjustTree( NodeEntry[] leftEntries, NodeEntry[] rightEntries, List<TraceCell> trace, int traceIndex ) {
        if ( rightEntries != null ) { // children have been split

            // get the parent of the original child
            NodeEntry[] parent;
            int parentIndex;
            if ( traceIndex < 0 ) {
                parent = new RTree.NodeEntry[bigM + 1];
                parent[0] = new RTree.NodeEntry();
                root = parent;
                parentIndex = 0;
            } else {
                parent = trace.get( traceIndex ).node;
                parentIndex = trace.get( traceIndex ).index;
            }

            // update the bbox of the parent after the child entries changed due to insert/split
            int nLeftEntries = lastNotNull( leftEntries );
            parent[parentIndex].bbox = mbb( copyBoxesFromRange( leftEntries, 0, nLeftEntries + 1 ) );
            parent[parentIndex].next = leftEntries;

            NodeEntry newEntry = new RTree.NodeEntry();
            int nRightEntries = lastNotNull( rightEntries );
            newEntry.bbox = mbb( copyBoxesFromRange( rightEntries, 0, nRightEntries + 1 ) );
            newEntry.next = rightEntries;

            addAfterLast( newEntry, parent, parentIndex );

            if ( parent[bigM] != null ) {
                int splitIndex = split( parent, newEntry.bbox, null );

                NodeEntry[] addedNode = new RTree.NodeEntry[bigM + 1];
                Arrays.fill( addedNode, null );
                System.arraycopy( parent, splitIndex + 1, addedNode, 0, bigM - splitIndex );
                Arrays.fill( parent, splitIndex + 1, bigM + 1, null );

                adjustTree( parent, addedNode, trace, traceIndex - 1 );
            } else {

                adjustTree( parent, null, trace, traceIndex - 1 );
            }
        } else {

            if ( traceIndex < 0 ) {
                return;
            }

            NodeEntry[] parent = trace.get( traceIndex ).node;
            int parentIndex = trace.get( traceIndex ).index;

            int nLeftEntries = -1;
            for ( int i = 0; i < bigM + 1; i++ ) {
                if ( leftEntries[i] == null ) {
                    nLeftEntries = i;
                    break;
                }
            }

            // update the bbox of the parent after the child entries changed due to insert/split
            parent[parentIndex].bbox = mbb( copyBoxesFromRange( leftEntries, 0, nLeftEntries ) );

            adjustTree( parent, null, trace, traceIndex - 1 );
        }
    }

    /**
     * Add newEntry after the last not-null entry in the array, starting from index + 1.
     * 
     * @param newEntry
     * @param array
     * @param index
     */
    private void addAfterLast( NodeEntry newEntry, NodeEntry[] array, int index ) {
        for ( int i = index + 1; i < bigM + 1; i++ ) {
            if ( array[i] == null ) {
                array[i] = newEntry;
                break;
            }
        }
    }

    private int lastNotNull( NodeEntry[] array ) {
        int res = -1;
        for ( int i = bigM - 1; i >= 0; i-- ) {
            if ( array[i] != null ) {
                res = i;
                break;
            }
        }
        return res;
    }

    /**
     * Find the right leaf node in which to insert
     * 
     * @param insertBox
     * @param entries
     */
    private NodeEntry[] chooseLeaf( float[] insertBox, NodeEntry[] entries, List<TraceCell> trace ) {
        if ( entries[0].next == null ) {
            return entries;
        }
        int index = chooseSubtree( entries, insertBox );
        trace.add( new TraceCell( entries, index ) );

        return chooseLeaf( insertBox, entries[index].next, trace );
    }

    /**
     * Choose the index at which the entries shall be split when inserting insertBox.
     * 
     * @param entries
     * @param insertBox
     * @param object
     */
    private int split( NodeEntry[] entries, float[] insertBox, T object ) {
        if ( entries[0].next == null ) {
            // leaf node
            splitAxis( entries );
        }

        double minValue = Double.MAX_VALUE;
        int splitIndex = -1;
        for ( int i = smallm - 1; i < bigM + 1 - smallm; i++ ) {
            float[][] leftSide = copyBoxesFromRange( entries, 0, i + 1 );
            float[][] rightSide = copyBoxesFromRange( entries, i + 1, bigM + 1 );

            double currentVal;
            if ( !intersects( mbb( leftSide ), mbb( rightSide ), 2 ) ) {
                currentVal = wgFunction( leftSide, rightSide ) * wfFunction( i );
            } else {
                currentVal = wgFunction( leftSide, rightSide ) / wfFunction( i );
            }
            if ( currentVal < minValue ) {
                minValue = currentVal;
                splitIndex = i;
            }
        }
        return splitIndex;
    }

    /**
     * @param i
     */
    private double wfFunction( int i ) {
        double result;
        double miu = ( 1 - 2 * smallm / ( bigM + 1 ) ) * ASYM;
        double xi = 2 * i / ( bigM + 1 ) - 1;
        double sigma = S * ( 1 + Math.abs( miu ) );
        double y1 = Math.exp( -1 / ( S * S ) );
        double ys = 1 / ( 1 - y1 );
        double expr = Math.exp( -( ( xi - miu ) / sigma ) * ( ( xi - miu ) / sigma ) );
        result = ( ys * expr - y1 );
        return result;
    }

    /**
     * Calculate the value of the goal function when the splitting at position i.
     * 
     * @param leftSide
     * @param rightSide
     */
    private double wgFunction( float[][] leftSide, float[][] rightSide ) {
        double result;
        try {
            result = calculatePerimeter( calculateIntersection( mbb( leftSide ), mbb( rightSide ) ) );
        } catch ( NoOverlapException e ) {
            float[][] all = new float[leftSide.length + rightSide.length][];

            // Arrays.copyOf does not work
            for ( int i = 0; i < leftSide.length; i++ ) {
                all[i] = leftSide[i];
            }
            System.arraycopy( rightSide, 0, all, leftSide.length, rightSide.length );
            result = calculatePerimeter( mbb( leftSide ) ) + calculatePerimeter( mbb( rightSide ) )
                     - calculatePerimMax( all );
        }
        return result;
    }

    /**
     * Determines the axis on which the sorting should be done before splitting. This also returns the entries sorted
     * accordingly.
     * 
     * This is determined by summing up the perimeters for each split and then choosing the axis that has the smallest
     * perimeter sum.
     * 
     * @param entries
     * @return -1 when the X axis wins, 1 when Y axis wins
     */
    private int splitAxis( NodeEntry[] entries ) {
        NodeEntry[] entriesOrderX = Arrays.copyOf( entries, bigM + 1 );
        sortEntriesByX( entriesOrderX );
        double perimX = 0.0;
        for ( int i = smallm - 1; i <= bigM + 1 - smallm; i++ ) {
            float[][] boxes = copyBoxesFromRange( entries, 0, i + 1 );
            perimX += calculatePerimeter( mbb( boxes ) );

            boxes = copyBoxesFromRange( entries, i, bigM + 1 );
            perimX += calculatePerimeter( mbb( boxes ) );
        }

        NodeEntry[] entriesOrderY = Arrays.copyOf( entries, bigM + 1 );
        sortEntriesByY( entriesOrderY );
        double perimY = 0.0;
        for ( int i = smallm; i <= bigM + 1 - smallm; i++ ) {
            float[][] boxes = copyBoxesFromRange( entries, 0, i + 1 );
            perimY += calculatePerimeter( mbb( boxes ) );

            boxes = copyBoxesFromRange( entries, i, bigM + 1 );
            perimY += calculatePerimeter( mbb( boxes ) );
        }
        if ( perimX < perimY ) {
            for ( int i = 0; i < bigM + 1; i++ ) {
                entries[i] = entriesOrderX[i];
            }
            return -1;
        }

        // otherwise y wins
        for ( int i = 0; i < bigM + 1; i++ ) {
            entries[i] = entriesOrderY[i];
        }
        return 1;
    }

    /**
     * Calculate "maximum perimeter" constant for a node to be split: 2 times the perimeter of the minimum bounding box
     * of all bigM + 1 bboxes minus the shortest axis length
     * 
     * @param boxes
     */
    private double calculatePerimMax( float[][] boxes ) {
        float[] overfilledBox = mbb( boxes );
        double result = 2 * calculatePerimeter( overfilledBox );

        double xLength = 2 * ( overfilledBox[2] - overfilledBox[0] );
        double yLength = 2 * ( overfilledBox[3] - overfilledBox[1] );

        if ( xLength < yLength ) {
            return result - xLength;
        }
        return result - yLength;
    }

    /**
     * Copies the bboxes of entries with the indexes i1..i2-1
     * 
     * @param entries
     * @param i1
     * @param i2
     * @return an array of bboxes (each containing a float array)
     */
    private float[][] copyBoxesFromRange( NodeEntry[] entries, int i1, int i2 ) {
        float[][] boxes = new float[i2 - i1][];
        for ( int j = i1; j < i2; j++ ) {
            if ( entries[j] != null ) {
                boxes[j - i1] = entries[j].bbox;
            }
        }
        return boxes;
    }

    /**
     * Sort the rectangles by their minimum Y then by their maximum Y
     * 
     * @param entriesOrderY
     */
    private void sortEntriesByY( NodeEntry[] entriesOrderY ) {
        Arrays.sort( entriesOrderY, new Comparator<RTree<T>.NodeEntry>() {
            public int compare( RTree<T>.NodeEntry a, RTree<T>.NodeEntry b ) {
                if ( a.bbox[1] < b.bbox[1] ) {
                    return -1;
                } else if ( a.bbox[1] > b.bbox[1] ) {
                    return 1;
                } else {
                    return 0;
                }
            }
        } );
        Arrays.sort( entriesOrderY, new Comparator<RTree<T>.NodeEntry>() {
            public int compare( RTree<T>.NodeEntry a, RTree<T>.NodeEntry b ) {
                if ( a.bbox[3] < b.bbox[3] ) {
                    return -1;
                } else if ( a.bbox[3] > b.bbox[3] ) {
                    return 1;
                } else {
                    return 0;
                }
            }
        } );
    }

    /**
     * Sort the entries' bboxes by their minimum X then by their maximum X
     * 
     * @param entriesOrderX
     */
    private void sortEntriesByX( NodeEntry[] entriesOrderX ) {
        Arrays.sort( entriesOrderX, new Comparator<RTree<T>.NodeEntry>() {
            public int compare( RTree<T>.NodeEntry a, RTree<T>.NodeEntry b ) {
                if ( a.bbox[0] < b.bbox[0] ) {
                    return -1;
                } else if ( a.bbox[0] > b.bbox[0] ) {
                    return 1;
                } else {
                    return 0;
                }
            }
        } );
        Arrays.sort( entriesOrderX, new Comparator<RTree<T>.NodeEntry>() {
            public int compare( RTree<T>.NodeEntry a, RTree<T>.NodeEntry b ) {
                if ( a.bbox[2] < b.bbox[2] ) {
                    return -1;
                } else if ( a.bbox[2] > b.bbox[2] ) {
                    return 1;
                } else {
                    return 0;
                }
            }
        } );
    }

    /**
     * Decide which subtree is better to insert in
     * 
     * @param entries
     * @param insertBox
     * @return the index of the <code>entries</code> in which it is best to insert the new node
     */
    @SuppressWarnings("unchecked")
    private int chooseSubtree( NodeEntry[] entries, float[] insertBox ) {
        int n = bigM;
        for ( int i = bigM; i >= 0; i-- ) {
            if ( entries[i] != null ) {
                n = i + 1;
                break;
            }
        }

        // if there are entries that do not need enlargement to accommodate the new object then select the one with the
        // minimum area
        int selectEntry = -1;
        double minimumArea = Double.MAX_VALUE;
        for ( int i = 1; i < n; i++ ) {
            if ( calculateEnlargement( entries[i].bbox, insertBox ) < EPS5 ) {
                if ( calculateArea( entries[i].bbox ) < minimumArea ) {
                    selectEntry = i;
                    minimumArea = calculateArea( entries[i].bbox );
                }
            }
        }
        if ( selectEntry != -1 ) {
            return selectEntry;
        }

        // calculate the change in perimeter that the new rectangle wound bring to every entry and then sort the values
        double[] deltaPerim = new double[n];
        for ( int i = 0; i < n; i++ ) {
            deltaPerim[i] = calculatePerimeter( mbbIncludeInsertBox( entries[i].bbox, insertBox ) )
                            - calculatePerimeter( entries[i].bbox );
        }

        ArrayEncapsInsert[] array = new RTree.ArrayEncapsInsert[n];
        for ( int i = 0; i < n; i++ ) {
            array[i] = new ArrayEncapsInsert();
            array[i].nodeEntry = entries[i];
            array[i].value = deltaPerim[i];
            array[i].origIndex = i;
        }
        Arrays.sort( array, new Comparator<ArrayEncapsInsert>() {
            public int compare( ArrayEncapsInsert a, ArrayEncapsInsert b ) {
                return a.value < b.value ? -1 : ( a.value == b.value ? 0 : 1 );
            }
        } );

        double perimOverlap = 0.0;
        for ( int i = 1; i < n; i++ ) {
            perimOverlap += calculatePerimOverlap( array[0].nodeEntry.bbox, array[i].nodeEntry.bbox, insertBox );
        }
        if ( perimOverlap < EPS5 ) {
            return array[0].origIndex;
        }

        double maxOverlap = Double.MIN_VALUE;
        int selectIndex = -1;
        for ( int i = 1; i < n; i++ ) {
            if ( calculatePerimOverlap( array[0].nodeEntry.bbox, array[i].nodeEntry.bbox, insertBox ) > maxOverlap ) {
                maxOverlap = calculatePerimOverlap( array[0].nodeEntry.bbox, array[i].nodeEntry.bbox, insertBox );
                selectIndex = i;
            }
        }

        // consider the first selectIndex+1 entries in the remaining steps
        boolean success = false;
        Set<Integer> cand = new HashSet<Integer>();
        int c = -1;
        double[] overlap = new double[selectIndex + 1];
        checkComp( 0, selectIndex + 1, array, insertBox, success, cand, c, overlap );
        if ( success ) {
            return array[c].origIndex;
        }
        double minOverlap = Double.MAX_VALUE;
        selectIndex = -1;
        for ( int i : cand ) {
            if ( overlap[i] < minOverlap ) {
                minOverlap = overlap[i];
                selectIndex = i;
            }
        }
        return array[selectIndex].origIndex;
    }

    /**
     * @param i
     */
    private void checkComp( int i, int p, ArrayEncapsInsert[] array, float[] insertBox, boolean success,
                            Set<Integer> cand, int c, double[] overlap ) {
        cand.add( i );
        for ( int j = 0; j < p; j++ ) {
            if ( j != i ) {
                double currentOverlap = calculateAreaOverlap( array[i].nodeEntry.bbox, array[j].nodeEntry.bbox,
                                                              insertBox );
                overlap[j] += currentOverlap;
                if ( currentOverlap > 0 && !cand.contains( j ) ) {
                    checkComp( j, p, array, insertBox, success, cand, c, overlap );
                    if ( success ) {
                        break;
                    }
                }
            }
        }
        if ( overlap[i] < EPS5 ) {
            c = i;
            success = true;
        }
    }

    /**
     * @param insertBox
     */
    private double calculateAreaOverlap( float[] box1, float[] box2, float[] insertBox ) {
        double area1;
        try {
            area1 = calculateArea( calculateIntersection( mbbIncludeInsertBox( box1, insertBox ), box2 ) )
                    - calculateArea( calculateIntersection( box1, box2 ) );
        } catch ( NoOverlapException e ) {
            area1 = 0.0;
        }
        double area2;
        try {
            area2 = calculateArea( calculateIntersection( box1, box2 ) );
        } catch ( NoOverlapException e ) {
            area2 = 0.0;
        }
        return area1 - area2;
    }

    /**
     * 
     * The <code>ArrayEncapsInsert</code> class encapsulates a triplet (entry, value, origIndex). Its primary usage
     * benefit is the storage of the original index, after the entries have been sorted.
     * 
     * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
     * 
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     * 
     */
    class ArrayEncapsInsert {
        NodeEntry nodeEntry;

        double value;

        int origIndex;
    }

    /**
     * The <code>TraceCell</code> class encapsulates a pair of (entries array, index in this array). Its use is in
     * recording the trace of entries in nodes, from leaf to root, in the context of removal operation. It has been
     * preferred to using a Pair for clarity of syntax reasons.
     * 
     * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
     * 
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     * 
     */
    class TraceCell {
        NodeEntry[] node;

        int index;

        public TraceCell( NodeEntry[] node, int index ) {
            this.node = node;
            this.index = index;
        }
    }

    /**
     * @param insertBox
     */
    private double calculatePerimOverlap( float[] box1, float[] box2, float[] insertBox ) {
        double perim1;
        try {
            perim1 = calculatePerimeter( calculateIntersection( mbbIncludeInsertBox( box1, insertBox ), box2 ) );
        } catch ( NoOverlapException e ) {
            perim1 = 0.0;
        }
        double perim2;
        try {
            perim2 = calculatePerimeter( calculateIntersection( box1, box2 ) );
        } catch ( NoOverlapException e ) {
            perim2 = 0.0;
        }
        return perim1 - perim2;
    }

    /**
     * @param box2
     * @throws NoOverlapException
     */
    private float[] calculateIntersection( float[] box1, float[] box2 )
                            throws NoOverlapException {
        float interXmin = box1[0];
        if ( box2[0] > interXmin ) {
            interXmin = box2[0];
        }
        float interXmax = box1[2];
        if ( box2[2] < interXmax ) {
            interXmax = box2[2];
        }
        float interYmin = box1[1];
        if ( box2[1] > interYmin ) {
            interYmin = box2[1];
        }
        float interYmax = box1[3];
        if ( box2[3] < interYmax ) {
            interYmax = box2[3];
        }

        if ( interXmin > interXmax || interYmin > interYmax ) {
            throw new NoOverlapException( "Areas " + Arrays.toString( box1 ) + " and " + Arrays.toString( box2 )
                                          + " do not intersect!" );
        }
        return new float[] { interXmin, interYmin, interXmax, interYmax };
    }

    private double calculatePerimeter( float[] box ) {
        return box[2] - box[0] + box[3] - box[1];
    }

    /**
     * Determines the bounding box that fits both the to-be-inserted bbox and the array of entry boxes
     * 
     * @param insertBox
     * @param entryBoxes
     */
    private float[] mbbIncludeInsertBox( float[] insertBox, float[]... entryBoxes ) {
        float minx = insertBox[0];
        float miny = insertBox[1];
        float maxx = insertBox[2];
        float maxy = insertBox[3];

        for ( int i = 0; i < entryBoxes.length; i++ ) {
            if ( entryBoxes[i][0] < minx ) {
                minx = entryBoxes[i][0];
            }
            if ( entryBoxes[i][1] < miny ) {
                miny = entryBoxes[i][1];
            }
            if ( maxx < entryBoxes[i][2] ) {
                maxx = entryBoxes[i][2];
            }
            if ( maxy < entryBoxes[i][3] ) {
                maxy = entryBoxes[i][3];
            }
        }
        return new float[] { minx, miny, maxx, maxy };
    }

    /**
     * Determines the bounding box that fits the array of entry boxes
     * 
     * @param entryBoxes
     */
    private float[] mbb( float[]... entryBoxes ) {
        float minx = entryBoxes[0][0];
        float miny = entryBoxes[0][1];
        float maxx = entryBoxes[0][2];
        float maxy = entryBoxes[0][3];

        for ( int i = 1; i < entryBoxes.length; i++ ) {
            if ( entryBoxes[i] != null ) {
                if ( entryBoxes[i][0] < minx ) {
                    minx = entryBoxes[i][0];
                }
                if ( entryBoxes[i][1] < miny ) {
                    miny = entryBoxes[i][1];
                }
                if ( maxx < entryBoxes[i][2] ) {
                    maxx = entryBoxes[i][2];
                }
                if ( maxy < entryBoxes[i][3] ) {
                    maxy = entryBoxes[i][3];
                }
            }
        }
        return new float[] { minx, miny, maxx, maxy };
    }

    /**
     * Calculate how much does the bbox of an entry needs to be enlarged so that it contains the bbox of the objext to
     * be inserted
     * 
     * @param entryBbox
     *            the bbox of the entry already in the tree
     * @param insertBbox
     *            the bbox of object to be inserted
     * @return the area of enlargement
     */
    private double calculateEnlargement( float[] entryBbox, float[] insertBbox ) {
        return calculateArea( mbbIncludeInsertBox( entryBbox, insertBbox ) ) - calculateArea( entryBbox );
    }

}
