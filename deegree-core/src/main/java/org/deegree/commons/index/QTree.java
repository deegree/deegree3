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

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deegree.commons.utils.GraphvizDot;
import org.deegree.commons.utils.Pair;

/**
 * The <code>QTree</code> is a quadtree based organization of a scene containing {@link PositionableModel}s.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * @param <T>
 *            a positionable
 * 
 */
public class QTree<T> extends SpatialIndex<T> implements Serializable {

    private static final long serialVersionUID = 4203959065145481646L;

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( QTree.class );

    /** the envelope of this tree */
    protected final float[] envelope;

    /** the children of this node */
    protected QTree<T>[] children;

    /** Objects partially (or totally) contained in this node (if this node is a leaf). */
    protected ArrayList<Entry<T>> leafObjects = null;

    /** The objects which totally cover this qtree node */
    protected List<Entry<T>> objectsCoveringEnv = null;

    private final static float SPLIT_CRITERIA_EPSILON = 1E-4f;

    /** the number of object this tree can hold in a leaf */
    protected final int numberOfObjects;

    /** the current depth of this node */
    protected final byte currentDepth;

    private final static byte MAX_DEPTH = 25;

    /** denoting lower left son */
    protected final static byte LOWER_LEFT = 0;

    /** denoting lower right son */
    protected final static byte LOWER_RIGHT = 1;

    /** denoting upper left son */
    protected final static byte UP_LEFT = 2;

    /** denoting upper right son */
    protected final static byte UP_RIGHT = 3;

    private final int maxOffset;

    private int objectsInLeaf = 0;

    /**
     * Create son node.
     * 
     * @param numberOfObjects
     * @param envelope
     * @param depth
     */
    protected QTree( int numberOfObjects, float[] envelope, byte depth ) {
        if ( numberOfObjects < 1 ) {
            throw new IllegalArgumentException( "The number of objects per leaf may not be smaller than 1." );
        }
        this.numberOfObjects = numberOfObjects;
        this.envelope = envelope;
        this.currentDepth = depth;
        // if the dimension == 3, the max point will start at position 3;
        maxOffset = envelope.length / 2;
    }

    /**
     * @param validDomain
     * @param numberOfObjects
     *            each node will contain
     */
    public QTree( float[] validDomain, int numberOfObjects ) {
        this( numberOfObjects, validDomain, (byte) 0 );
        if ( validDomain == null ) {
            throw new IllegalArgumentException( "The envelope must be set." );
        }
    }

    /**
     * @return the envelope
     */
    public final float[] getEnvelope() {
        return Arrays.copyOf( envelope, envelope.length );
    }

    /**
     * @return the maxOffset
     */
    public final int getMaxOffset() {
        return maxOffset;
    }

    /**
     * @return the half of the width added to min x
     */
    protected final float getHalfWidth() {
        return ( this.envelope[0] + ( ( this.envelope[maxOffset] - this.envelope[0] ) * 0.5f ) );
    }

    /**
     * @return the half of the height added to the min y
     */
    protected final float getHalfHeight() {
        return ( this.envelope[1] + ( ( this.envelope[maxOffset + 1] - this.envelope[1] ) * 0.5f ) );
    }

    /**
     * @param envelope
     *            of the object
     * @param object
     *            to insert
     * @return true if the object was inserted, false otherwise.
     */
    @Override
    public boolean insert( float[] envelope, T object ) {
        if ( object != null ) {
            if ( intersects( this.envelope, envelope, maxOffset ) ) {
                Entry<T> obj = new Entry<T>( envelope, object );
                if ( isLeaf() ) {
                    addObject( obj );
                } else {
                    List<QTree<T>> treeNodes = getObjectNodes( obj.entryEnv );
                    for ( QTree<T> node : treeNodes ) {
                        if ( node != null ) {
                            node.addObject( obj );
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * @param object
     *            to remove
     * @return true if the object was inserted, false otherwise.
     */
    @Override
    public boolean remove( T object ) {
        if ( object != null ) {
            return removeObject( object );
        }
        return false;
    }

    @Override
    public String toString() {
        return getEnvString()
               + ": "
               + ( ( isLeaf() ) ? " is a leaf with " + leafObjects.size() + " leafObjects."
                               : ( " is a node with: " + ( ( children[0] == null ) ? "" : " ll," )
                                   + ( ( children[1] == null ) ? "" : " lr," )
                                   + ( ( children[2] == null ) ? "" : " ul," ) + ( ( children[3] == null ) ? ""
                                                                                                          : " ur," ) ) );

    }

    private String getEnvString() {
        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < envelope.length; ++i ) {
            sb.append( envelope[i] );
            if ( ( i + 1 ) < envelope.length ) {
                sb.append( ", " );
            }
        }
        return sb.toString();
    }

    /**
     * @param object
     */
    private boolean removeObject( T object ) {
        boolean result = false;
        if ( objectsCoveringEnv != null ) {
            result = removeFromCovering( object );
        }
        // none of the totally covering objects matched the given object, try the subtrees.
        if ( !result ) {
            if ( isLeaf() ) {
                result = removeFromLeaf( object );
            } else {
                result = removeFromSubTree( object );
            }
        }
        return result;
    }

    /**
     * remove the given object from the subtree of this node.
     * 
     * @param object
     *            to remove
     * @return true if the object could be removed from one of the subtrees.
     */
    private boolean removeFromSubTree( T object ) {
        // only usable for not leafs;
        boolean result = false;

        if ( !isLeaf() ) {
            boolean tR = false;
            for ( QTree<T> node : children ) {
                if ( node != null ) {
                    tR = node.removeObject( object );
                    if ( tR && !result ) {
                        result = true;
                    }
                }
            }
            if ( result ) {
                merge();
            }
        }

        return result;
    }

    /**
     * Try to remove the given object from the set of objects which cover this nodes whole envelope
     * 
     * @param object
     *            to be removed
     * @return true if the object was in the list of the objects covering this nodes' total area.
     */
    private boolean removeFromCovering( T object ) {
        boolean result = false;
        if ( objectsCoveringEnv != null ) {
            Iterator<Entry<T>> it = objectsCoveringEnv.iterator();
            while ( it.hasNext() && !result ) {
                Entry<T> e = it.next();
                if ( e != null && e.entryValue.equals( object ) ) {
                    result = objectsCoveringEnv.remove( e );
                }
            }
            if ( objectsCoveringEnv.isEmpty() ) {
                objectsCoveringEnv = null;
            }
        }
        return result;
    }

    /**
     * Try to remove the given object from this nodes (which is a leaf) objects.
     * 
     * @param object
     *            to be removed
     * @return true if the given object was in this leaf and it could be removed.
     */
    private boolean removeFromLeaf( T object ) {
        boolean result = false;
        if ( leafObjects != null ) {
            for ( int i = 0; i < leafObjects.size() && !result; ++i ) {
                Entry<T> e = leafObjects.get( i );
                if ( e != null && e.entryValue.equals( object ) ) {
                    result = leafObjects.remove( e );
                    if ( leafObjects.isEmpty() || !hasDuplicateLocation( e.entryEnv ) ) {
                        objectsInLeaf--;
                    }
                }
            }
            if ( leafObjects.isEmpty() ) {
                if ( objectsInLeaf > 0 ) {
                    LOG.error( "No more objects in leaf, but the counter says there should be." );
                }
                leafObjects = null;
                objectsInLeaf = 0;
            }
        }
        return result;
    }

    /**
     * 
     */
    private void merge() {
        if ( !isLeaf() ) {
            Set<Entry<T>> mergableObjects = validateChildrenSize();
            if ( mergableObjects != null ) {
                // calculate the size of the set, after removing duplicates
                ArrayList<Entry<T>> objects = new ArrayList<Entry<T>>( mergableObjects );
                int size = mergableObjects.size() - duplicateEnvelopes( objects );
                if ( size <= numberOfObjects ) {
                    // we can merge the children.
                    leafObjects = objects;
                    // leafObjects.addAll( mergableObjects );
                    for ( QTree<T> n : children ) {
                        if ( n != null ) {
                            n.objectsCoveringEnv = null;
                            n.leafObjects = null;
                            n = null;
                        }
                    }
                    children = null;
                }
            }
        }
    }

    private Set<Entry<T>> validateChildrenSize() {
        boolean possibleMerge = true;
        Set<Entry<T>> childrenInLeaf = new HashSet<Entry<T>>();
        if ( !isLeaf() ) {
            for ( int i = 0; i < children.length; ++i ) {
                QTree<T> n = children[i];
                if ( n != null ) {
                    if ( n.isLeaf() ) {
                        // if one of the other children wasn't a leaf, don't bother getting the objects
                        if ( possibleMerge ) {
                            n.getObjects( n.envelope, childrenInLeaf );
                        }
                        // the child is a leaf but does not contain any leafObjects, no need to keep it in memory.
                        if ( n.leafObjects == null && n.objectsCoveringEnv == null ) {
                            children[i] = null;
                        }
                    } else {
                        // no merging, but check other children if they can be deleted.
                        possibleMerge = false;
                    }
                }
            }
        }
        return possibleMerge ? childrenInLeaf : null;
    }

    /**
     * @return the number of duplicate envelopes in the leaf objects which should be subtracted from the size();
     */
    private final int duplicateEnvelopes( List<Entry<T>> objectList ) {
        List<List<T>> equalEnvelope = new ArrayList<List<T>>( objectList.size() );
        for ( int i = 0; i < objectList.size(); ++i ) {
            Entry<T> firstE = objectList.get( i );
            float[] first = firstE.entryEnv;
            boolean checked = false;
            for ( int listI = 0; listI < equalEnvelope.size() && !checked; ++listI ) {
                List<T> l = equalEnvelope.get( listI );
                if ( l != null ) {
                    checked = l.contains( firstE.entryValue );
                }
            }
            if ( !checked ) {
                LinkedList<T> checkList = new LinkedList<T>();

                for ( int j = i + 1; j < objectList.size(); ++j ) {
                    float[] second = objectList.get( j ).entryEnv;
                    double minD = calcDist( first, second, 0, maxOffset );
                    double maxD = calcDist( first, second, maxOffset, maxOffset );

                    // if min and max have distance 0, then just count as one because they are equals, this might
                    // prevent a stack overflow
                    if ( ( minD < SPLIT_CRITERIA_EPSILON && maxD < SPLIT_CRITERIA_EPSILON ) ) {
                        checkList.add( objectList.get( j ).entryValue );
                    }
                }
                if ( !checkList.isEmpty() ) {
                    checkList.add( firstE.entryValue );
                    equalEnvelope.add( checkList );
                }
            }
        }
        int result = 0;
        for ( List<T> l : equalEnvelope ) {
            if ( l != null && !l.isEmpty() ) {
                result += ( l.size() - 1 );
            }
        }

        return result;
    }

    /**
     * @return the number of all objects in this node, e.g. the leafObjects and the objects covering the entire
     *         envelope.
     */
    private final int totalSize() {
        return size()
               + ( ( objectsCoveringEnv == null ) ? 0
                                                 : ( objectsCoveringEnv.size() - duplicateEnvelopes( objectsCoveringEnv ) ) );

    }

    private final int size() {
        return ( leafObjects == null ) ? 0 : objectsInLeaf;// ( leafObjects.size() - duplicateEnvelopes( leafObjects )
        // );
    }

    /**
     * @param object
     */
    private void addObject( Entry<T> object ) {
        if ( objectCoversEnvelope( object.entryEnv ) ) {
            if ( objectsCoveringEnv == null ) {
                objectsCoveringEnv = new LinkedList<Entry<T>>();
            }
            objectsCoveringEnv.add( object );
        } else {
            if ( leafObjects == null ) {
                leafObjects = new ArrayList<Entry<T>>( numberOfObjects );
            }
            if ( !hasDuplicateLocation( object.entryEnv ) ) {
                objectsInLeaf++;
            }
            leafObjects.add( object );
            if ( splitCriteria() ) {
                split();
            }
        }
        if ( leafObjects != null && leafObjects.size() < objectsInLeaf ) {
            LOG.error( "leaf counter (" + objectsInLeaf + ") is larger then actual objects in leaf: "
                       + leafObjects.size() );
        }

    }

    private final boolean hasDuplicateLocation( final float[] objectEnvelope ) {
        for ( Entry<T> obj : leafObjects ) {
            float[] second = obj.entryEnv;
            double minD = calcDist( objectEnvelope, second, 0, maxOffset );
            double maxD = calcDist( objectEnvelope, second, maxOffset, maxOffset );

            // if min and max have distance 0, then just count as one because they are equals, this might
            // prevent a stack overflow
            if ( ( minD < SPLIT_CRITERIA_EPSILON && maxD < SPLIT_CRITERIA_EPSILON ) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if the number of objects in the leaf without the duplicate envelopes are larger than the allowed
     *         number of objects in a leaf
     */
    private boolean splitCriteria() {
        return ( size() > numberOfObjects ) && ( ( currentDepth + 1 ) < MAX_DEPTH );
    }

    /**
     * Distance between the two 2d points starting at index (0 == min, maxOffset == max )
     * 
     * @param first
     * @param second
     */
    private final static double calcDist( float[] first, float[] second, int index, int dim ) {
        double d = 0;
        for ( int i = 0; i < dim; ++i ) {
            d += ( first[index + i] - second[index + i] ) * ( first[index + i] - second[index + i] );
        }
        return Math.sqrt( d );
    }

    /**
     * @return true if the object covers this entire quad node
     */
    private final boolean objectCoversEnvelope( float[] entryEnv ) {
        return ( entryEnv[0] <= this.envelope[0] && entryEnv[1] <= this.envelope[1] )
               && ( entryEnv[maxOffset] >= this.envelope[maxOffset] && entryEnv[maxOffset + 1] >= this.envelope[maxOffset + 1] );
    }

    @SuppressWarnings("unchecked")
    private final void split() {
        children = new QTree[4];
        for ( Entry<T> e : leafObjects ) {
            List<QTree<T>> treeNodes = getObjectNodes( e.entryEnv );
            for ( QTree<T> node : treeNodes ) {
                node.addObject( e );
            }
        }
        objectsInLeaf = 0;
        leafObjects = null;
    }

    /**
     * @param entryEnv
     * @return the nodes the given envelope will intersect.
     */
    final protected List<QTree<T>> getObjectNodes( float[] entryEnv ) {
        List<QTree<T>> list = new LinkedList<QTree<T>>();
        // if the object covers the whole of this node
        if ( objectCoversEnvelope( entryEnv ) ) {
            list.add( this );
        } else {
            if ( !isLeaf() ) {
                int[] indizes = getIndizes( entryEnv );
                for ( int index : indizes ) {
                    QTree<T> child = children[index];
                    // No children in that area create a new one
                    if ( child == null ) {
                        child = createNode( index );
                        children[index] = child;
                        list.add( child );
                    } else {
                        // traverse tree
                        list.addAll( child.getObjectNodes( entryEnv ) );
                    }
                }
            } else {
                list.add( this );
            }
        }
        return list;
    }

    /**
     * @return true if this is a leaf node
     */
    protected final boolean isLeaf() {
        return children == null;
    }

    /**
     * @param son
     *            one of {@link QTree#LOWER_LEFT},{@link QTree#LOWER_RIGHT},{@link QTree#UP_LEFT},{@link QTree#UP_RIGHT}
     * @return a new QTree created from the given index.
     */
    protected QTree<T> createNode( int son ) {
        float[] newEnv = bboxForSon( son );
        return new QTree<T>( numberOfObjects, newEnv, (byte) ( currentDepth + 1 ) );
    }

    /**
     * 
     * @param son
     *            one of {@link QTree#LOWER_LEFT},{@link QTree#LOWER_RIGHT},{@link QTree#UP_LEFT},{@link QTree#UP_RIGHT}
     * @return the new envelope for the given son.
     */
    protected final float[] bboxForSon( int son ) {
        float[] newEnv = Arrays.copyOf( this.envelope, this.envelope.length );
        switch ( son ) {
        case LOWER_LEFT:// ll
            newEnv[maxOffset] = getHalfWidth();
            newEnv[maxOffset + 1] = getHalfHeight();
            break;
        case LOWER_RIGHT:// lr
            newEnv[0] = getHalfWidth();
            newEnv[maxOffset + 1] = getHalfHeight();
            break;
        case UP_LEFT:// ul
            newEnv[1] = getHalfHeight();
            newEnv[maxOffset] = getHalfWidth();
            break;
        case UP_RIGHT:// ur
            newEnv[0] = getHalfWidth();
            newEnv[1] = getHalfHeight();
            break;
        }
        return newEnv;
    }

    /**
     * Calculate the indices of the sons touching this envelope
     * 
     * @param envelope
     * @return the indices of the sons touching the envelope
     */
    private final int[] getIndizes( float[] envelope ) {
        int min = getIndex( envelope, 0 );
        int max = getIndex( envelope, maxOffset );
        return analyzeIndizes( envelope, min, max );
    }

    /**
     * @param envelope
     * @param min
     *            index of the intersecting node the min point
     * @param max
     *            index of the intersecting node the max point
     * @return the indices of the sons with which the given envelope intersects.
     */
    private final static int[] analyzeIndizes( float[] envelope, int min, int max ) {
        if ( min == max ) {
            return new int[] { min };
        }
        if ( min == 0 ) {
            // min == ll
            if ( max == 3 ) {
                // max == ur
                // all sons intersect given envelope.
                return new int[] { 0, 1, 2, 3 };
            }

        }
        return new int[] { min, max };
    }

    private final int getIndex( float[] position, int start ) {
        return ( ( ( position[start] < getHalfWidth() ) ? 0 : 1 ) + ( ( position[start + 1] < getHalfHeight() ) ? 0 : 2 ) );
    }

    private final void getObjects( float[] envelope, Set<Entry<T>> result ) {
        if ( intersects( this.envelope, envelope, maxOffset ) ) {
            if ( hasCoveringObjects() ) {
                result.addAll( objectsCoveringEnv );
            }
            if ( isLeaf() ) {
                if ( leafObjects != null ) {
                    for ( Entry<T> e : leafObjects ) {
                        if ( intersects( envelope, e.entryEnv, maxOffset ) && !result.contains( e.entryValue ) ) {
                            result.add( e );
                        }
                    }
                }
            } else {
                for ( QTree<T> n : children ) {
                    if ( n != null ) {
                        n.getObjects( envelope, result );
                    }
                }
            }
        }
    }

    /**
     * @return true if this node has objects fitting the total region of space.
     */
    protected final boolean hasCoveringObjects() {
        return objectsCoveringEnv != null;
    }

    private final List<T> getEntrySetAsResult( Set<Entry<T>> set ) {
        List<T> result = new ArrayList<T>( set.size() );
        for ( Entry<T> e : set ) {
            result.add( e.entryValue );
        }
        return result;
    }

    /**
     * @param env
     *            to get the leafObjects for.
     * @return the leafObjects which intersect with this node and or it's children, or the empty list.
     */
    @Override
    public List<T> query( float[] env ) {
        Set<Entry<T>> r = new HashSet<Entry<T>>();
        getObjects( env, r );
        return getEntrySetAsResult( r );
    }

    /**
     * Convenience method to retrieve the objects intersecting with the given envelope.
     * 
     * @param envelope
     * @return a List with all objects intersecting with the given envelope.
     */
    protected List<T> getObjects( float[] envelope ) {
        Set<Entry<T>> r = new HashSet<Entry<T>>();
        getObjects( envelope, r );
        return getEntrySetAsResult( r );
    }

    /**
     * @return the leafObjects which intersect with this node and or it's children, or the empty list.
     */
    public List<T> getObjects() {
        Set<Entry<T>> r = new HashSet<Entry<T>>();
        getObjects( envelope, r );
        return getEntrySetAsResult( r );
    }

    /**
     * 
     */
    @Override
    public void clear() {
        if ( isLeaf() ) {
            if ( leafObjects != null ) {
                leafObjects.clear();
                leafObjects = null;
            }
        } else {
            for ( QTree<T> n : children ) {
                if ( n != null ) {
                    n.clear();
                }
            }
            children = null;
        }
    }

    /**
     * 
     * The <code>Entry</code> class wraps an object with its envelope
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author$
     * @version $Revision$, $Date$
     * @param <ET>
     * 
     */
    protected final class Entry<ET> implements Serializable {

        private static final long serialVersionUID = -1957657299823750733L;

        /** the envelope of the object */
        public final float[] entryEnv;

        /** the actual object */
        public final ET entryValue;

        /**
         * 
         */
        Entry( float[] envelope, ET object ) {
            this.entryEnv = envelope;
            this.entryValue = object;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals( Object other ) {
            if ( other != null && other instanceof Entry ) {
                final Entry<T> that = (Entry<T>) other;
                return testEnv( that.entryEnv ) && this.entryValue.equals( that.entryValue );
            }
            return false;
        }

        /**
         * Implementation as proposed by Joshua Block in Effective Java (Addison-Wesley 2001), which supplies an even
         * distribution and is relatively fast. It is created from field <b>f</b> as follows:
         * <ul>
         * <li>boolean -- code = (f ? 0 : 1)</li>
         * <li>byte, char, short, int -- code = (int)f</li>
         * <li>long -- code = (int)(f ^ (f &gt;&gt;&gt;32))</li>
         * <li>float -- code = Float.floatToIntBits(f);</li>
         * <li>double -- long l = Double.doubleToLongBits(f); code = (int)(l ^ (l &gt;&gt;&gt; 32))</li>
         * <li>all Objects, (where equals(&nbsp;) calls equals(&nbsp;) for this field) -- code = f.hashCode(&nbsp;)</li>
         * <li>Array -- Apply above rules to each element</li>
         * </ul>
         * <p>
         * Combining the hash code(s) computed above: result = 37 * result + code;
         * </p>
         * 
         * @return (int) ( result >>> 32 ) ^ (int) result;
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            // the 2nd millionth prime, :-)
            long result = 32452843;
            result = result * 37 + Arrays.hashCode( entryEnv );
            result = result * 37 + entryValue.hashCode();
            return (int) ( result >>> 32 ) ^ (int) result;
        }

        private boolean testEnv( float[] otherEnv ) {
            if ( otherEnv == null ) {
                return entryEnv == null;
            }
            boolean result = entryEnv != null;
            if ( result ) {
                result = ( entryEnv.length == otherEnv.length );
                for ( int i = 0; i < entryEnv.length && result; ++i ) {
                    result = ( Math.abs( entryEnv[i] - otherEnv[i] ) < 1E-11 );
                }
            }
            return result;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder( "{min(" );
            for ( int i = 0; i < getMaxOffset(); ++i ) {
                sb.append( entryEnv[i] );
                if ( ( i + 1 ) < getMaxOffset() ) {
                    sb.append( "," );
                }
            }
            sb.append( "), max(" );
            for ( int i = 0; i < getMaxOffset(); ++i ) {
                sb.append( entryEnv[getMaxOffset() + i] );
                if ( ( i + 1 ) < getMaxOffset() ) {
                    sb.append( "," );
                }
            }
            sb.append( "), value:" + entryValue + "}" );

            return sb.toString();
        }
    }

    /**
     * @param out
     * @param id
     * @param level
     * @param sonID
     * @throws IOException
     */
    public void outputAsDot( Writer out, String id, int level, int sonID )
                            throws IOException {
        if ( isLeaf() ) {
            GraphvizDot.writeVertex( id, getDotVertex( level, sonID, true ), out );
        } else {
            GraphvizDot.writeVertex( id, getDotVertex( level, sonID, false ), out );
            for ( int i = 0; i < 4; ++i ) {
                QTree<T> child = children[i];
                if ( child != null ) {
                    String newID = id + i;
                    child.outputAsDot( out, newID, level + 1, i );
                    GraphvizDot.writeEdge( id, newID, null, out );
                }
            }
        }
    }

    private List<String> getDotVertex( int level, int quad, boolean isLeaf ) {
        List<String> attList = new ArrayList<String>();
        String label = level + "-";
        String color = null;
        switch ( quad ) {
        case -1:
            label = "root";
            color = "cyan";
            break;
        case 0:
            label = "ll";
            color = "green";
            break;
        case 1:
            label = "lr";
            color = "red";
            break;
        case 2:
            label = "ul";
            color = "blue";
            break;
        case 3:
            label = "ur";
            color = "orange";
            break;
        }
        if ( isLeaf ) {
            StringBuilder sb = new StringBuilder();
            StringBuilder s = getCoveringAsDot();
            if ( s != null ) {
                sb.append( s );
            }
            if ( leafObjects != null ) {
                // int i = 0;
                // sb.append( "L{" );
                // for ( Entry<T> t : leafObjects ) {
                // sb.append( t.entryValue ).append( ( ++i < leafObjects.size() ? "," : "" ) );
                // }
                // sb.append( "}" );
            }
            label += "(" + ( totalSize() ) + ":[" + sb.toString() + "])";
            attList.add( GraphvizDot.getShapeDef( "box" ) );
        } else {
            if ( objectsCoveringEnv != null ) {
                StringBuilder sb = new StringBuilder();
                StringBuilder s = getCoveringAsDot();
                if ( s != null ) {
                    sb.append( s );
                }
                label += "(" + size() + ":[" + sb.toString() + "])";
            }
        }
        attList.add( GraphvizDot.getLabelDef( label ) );
        attList.add( GraphvizDot.getFillColorDef( color ) );
        return attList;
    }

    private StringBuilder getCoveringAsDot() {
        StringBuilder sb = null;
        // if ( objectsCoveringEnv != null ) {
        // sb = new StringBuilder();
        // int i = 0;
        // sb.append( "C{" );
        // for ( Entry<T> t : objectsCoveringEnv ) {
        // sb.append( t.entryValue ).append( ( ++i < objectsCoveringEnv.size() ? "," : "" ) );
        // }
        // sb.append( "}" );
        // }
        return sb;
    }

    @Override
    public void insertBulk( List<Pair<float[], T>> listOfObjects ) {
        for ( Pair<float[], T> p : listOfObjects ) {
            if ( p != null ) {
                insert( p.first, p.second );
            }
        }
    }

}
