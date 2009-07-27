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

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.utils.GraphvizDot;
import org.deegree.commons.utils.Pair;
import org.deegree.geometry.Envelope;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.PositionableModel;

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
public class QTree<T> extends SpatialIndex<T> {

    private float[] envelope;

    private float halfWidth;

    private float halfHeight;

    private QTree<T>[] children;

    private ArrayList<Entry<T>> objects = null;

    private final int numberOfObjects;

    private QTree( int numberOfObjects ) {
        if ( numberOfObjects < 1 ) {
            throw new IllegalArgumentException( "The number of objects per leaf may not be smaller than 1." );
        }
        this.numberOfObjects = numberOfObjects;
    }

    /**
     * @param validDomain
     * @param numberOfObjects
     *            each node will contain
     */
    public QTree( Envelope validDomain, int numberOfObjects ) {
        this( numberOfObjects );
        if ( validDomain == null ) {
            throw new IllegalArgumentException( "The envelope must be set." );
        }
        this.envelope = createEnvelope( validDomain );

        this.halfHeight = this.envelope[1] + ( .5f * (float) validDomain.getWidth() );
        this.halfWidth = this.envelope[0] + ( .5f * (float) validDomain.getHeight() );
    }

    /**
     * @param envelope
     * @param numberOfObjects
     *            each leaf node will contain
     */
    public QTree( float[] envelope, int numberOfObjects ) {
        this( numberOfObjects );
        this.envelope = envelope;
        this.halfHeight = this.envelope[1] + ( ( this.envelope[3] - this.envelope[1] ) * 0.5f );
        this.halfWidth = this.envelope[0] + ( ( this.envelope[2] - this.envelope[0] ) * 0.5f );
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
        if ( object != null && intersects( this.envelope, envelope ) ) {
            Entry<T> obj = new Entry<T>( envelope, object );
            if ( isLeaf() ) {
                addObject( obj );
            } else {
                List<QTree<T>> treeNodes = getLeafNodes( obj.entryEnv );
                for ( QTree<T> node : treeNodes ) {
                    if ( node != null ) {
                        node.addObject( obj );
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * @param object
     *            to remove
     * @return true if the object was inserted, false otherwise.
     */
    public boolean remove( T object ) {
        if ( object != null ) {
            return removeObject( object );
        }
        return false;
    }

    /**
     * @param object
     */
    private boolean removeObject( T object ) {
        boolean result = false;
        if ( isLeaf() ) {
            if ( objects != null ) {
                for ( Entry<T> e : objects ) {
                    if ( e != null && e.entryValue.equals( object ) ) {
                        result = objects.remove( e );
                    }
                }
                if ( objects.isEmpty() ) {
                    objects = null;
                }
            }
        } else {
            List<QTree<T>> leafNodes = getLeafNodes( this.envelope );
            boolean tR = false;
            for ( QTree<T> node : leafNodes ) {
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
     * 
     */
    private void merge() {
        if ( !isLeaf() ) {
            int size = 0;
            for ( int i = 0; i < children.length; ++i ) {
                QTree<T> n = children[i];
                if ( n != null ) {
                    if ( n.isLeaf() ) {
                        // the child is a leaf but does not contain any objects, no need to keep it in memory.
                        if ( n.objects == null ) {
                            children[i] = null;
                        }
                        size += n.size();
                    } else {
                        // no merging, but check other children if they can be deleted.
                        size = numberOfObjects + 1;
                    }
                }
            }
            if ( size <= numberOfObjects ) {
                // we can merge the children.
                objects = new ArrayList<Entry<T>>( size );
                for ( QTree<T> n : children ) {
                    if ( n != null && n.objects != null ) {
                        objects.addAll( n.objects );
                        n.objects = null;
                        n = null;
                    }
                }
                children = null;
            }
        }
    }

    /**
     * @return
     */
    private int size() {
        return ( objects == null ) ? 0 : objects.size();
    }

    /**
     * @param object
     * @param position
     */
    private void addObject( Entry<T> object ) {
        if ( objects == null ) {
            objects = new ArrayList<Entry<T>>( numberOfObjects );
        }
        objects.add( object );
        if ( objects.size() > numberOfObjects ) {
            split();
        }

    }

    /**
     * @param object
     * @param position
     */
    @SuppressWarnings("unchecked")
    private void split() {
        children = new QTree[4];
        for ( Entry<T> e : objects ) {
            List<QTree<T>> treeNodes = getLeafNodes( e.entryEnv );
            for ( QTree<T> node : treeNodes ) {
                node.addObject( e );
            }
        }
        objects = null;
    }

    /**
     * @param entryEnv
     * @return
     */
    private List<QTree<T>> getLeafNodes( float[] entryEnv ) {
        List<QTree<T>> list = new LinkedList<QTree<T>>();
        if ( !isLeaf() ) {
            int[] indizes = getIndizes( entryEnv );
            for ( int index : indizes ) {
                QTree<T> child = children[index];
                // No children in that area create a new one
                if ( child == null ) {
                    child = createNode( index );
                    children[index] = child;
                    list.add( child );
                }
                // traverse tree
                list.addAll( child.getLeafNodes( entryEnv ) );
            }
        } else {
            list.add( this );
        }
        return list;
    }

    /**
     * @return true if this is a leaf node
     */
    private boolean isLeaf() {
        return children == null;
    }

    /**
     * @param position
     * @return
     */
    private QTree<T> createNode( int index ) {
        float[] newEnv = Arrays.copyOf( this.envelope, this.envelope.length );
        switch ( index ) {
        case 0:// ll
            newEnv[2] = halfWidth;
            newEnv[3] = halfHeight;
            break;
        case 1:// lr
            newEnv[0] = halfWidth;
            newEnv[3] = halfHeight;
            break;
        case 2:// ul
            newEnv[1] = halfHeight;
            newEnv[2] = halfWidth;
            break;
        case 3:// ur
            newEnv[0] = halfWidth;
            newEnv[1] = halfHeight;
            break;
        }
        return new QTree<T>( newEnv, numberOfObjects );
    }

    /**
     * Calculate the indizes of the sons touching this envelope
     * 
     * @param envelope
     * @return the indizes of the sons touching the envelope
     */
    private int[] getIndizes( float[] envelope ) {
        int min = getIndex( envelope, 0 );
        int max = getIndex( envelope, 2 );
        return analyzeIndizes( envelope, min, max );
    }

    /**
     * @param envelope
     * @param min
     * @param max
     * @return the indizes of the son the given envelope touches.
     */
    private int[] analyzeIndizes( float[] envelope, int min, int max ) {
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

    /**
     * @return
     */
    private int getIndex( float[] position, int start ) {
        return ( ( ( position[start] < halfWidth ) ? 0 : 1 ) + ( ( position[start + 1] < halfHeight ) ? 0 : 2 ) );
    }

    // /**
    // *
    // * @param env
    // * to intersect with
    // * @return true if this node intersects with the given bbox
    // */
    // private boolean intersects( float[] env ) {
    // return ( this.envelope[0] >= env[0] && this.envelope[0] <= env[2] )
    // || ( this.envelope[2] <= env[2] && this.envelope[2] >= env[0] )
    // || ( this.envelope[1] >= env[1] && this.envelope[1] <= env[3] )
    // || ( this.envelope[3] <= env[3] && this.envelope[3] >= env[1] );
    // }

    private void getObjects( float[] envelope, List<Entry<T>> result ) {
        if ( intersects( this.envelope, envelope ) ) {
            if ( isLeaf() ) {
                if ( objects != null ) {
                    result.addAll( objects );
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

    private List<T> getEntryListAsResult( List<Entry<T>> aList ) {
        List<T> result = new ArrayList<T>( aList.size() );
        for ( Entry<T> e : aList ) {
            result.add( e.entryValue );
        }
        return result;
    }

    /**
     * @param env
     *            to get the objects for.
     * @return the objects which intersect with this node and or it's children, or the empty list.
     */
    @Override
    public List<T> query( Envelope env ) {
        List<Entry<T>> r = new LinkedList<Entry<T>>();
        getObjects( createEnvelope( env ), r );
        return getEntryListAsResult( r );
    }

    /**
     * @return the objects which intersect with this node and or it's children, or the empty list.
     */
    public List<T> getObjects() {
        List<Entry<T>> r = new LinkedList<Entry<T>>();
        getObjects( envelope, r );
        return getEntryListAsResult( r );
    }

    /**
     * 
     */
    public void clear() {
        if ( isLeaf() ) {
            if ( objects != null ) {
                objects.clear();
                objects = null;
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
     * The <code>Positionable</code> class wraps an object with its envelope
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author$
     * @version $Revision$, $Date$
     * 
     */
    private class Entry<T> {
        final float[] entryEnv;

        final T entryValue;

        /**
         * 
         */
        Entry( float[] envelope, T object ) {
            this.entryEnv = envelope;
            this.entryValue = object;
        }

    }

    /**
     * @param out
     * @param id
     * @param level
     * @param sonID
     * @throws IOException
     */
    public void outputAsDot( BufferedWriter out, String id, int level, int sonID )
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
            label += "(" + size() + ")";
            attList.add( GraphvizDot.getShapeDef( "box" ) );
        }
        attList.add( GraphvizDot.getLabelDef( label ) );
        attList.add( GraphvizDot.getFillColorDef( color ) );
        return attList;
    }

    @Override
    public void buildIndex( List<Pair<float[], T>> listOfObjects ) {
        for ( Pair<float[], T> p : listOfObjects ) {
            if ( p != null ) {
                insert( p.first, p.second );
            }
        }
    }

}
