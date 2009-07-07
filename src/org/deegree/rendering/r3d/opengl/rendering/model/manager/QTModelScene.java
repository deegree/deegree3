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

package org.deegree.rendering.r3d.opengl.rendering.model.manager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point3d;

import org.deegree.commons.utils.GraphvizDot;
import org.deegree.commons.utils.math.VectorUtils;
import org.deegree.commons.utils.math.Vectors3f;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.primitive.Point;
import org.deegree.rendering.r3d.ViewParams;

/**
 * The <code>QTModelScene</code> is a quadtree based organization of a scene containing {@link PositionableModel}s.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * @param <T>
 *            a positionable
 *
 */
public class QTModelScene<T extends PositionableModel> {

    private float[] min;

    private float[] max;

    private float halfWidth;

    private float halfHeight;

    private QTModelScene<T>[] children;

    private ArrayList<T> objects = null;

    private final int numberOfObjects;

    // the most significant error of a node.
    private float maxError = Float.MIN_VALUE;

    private double maxPixelError;

    private QTModelScene( int numberOfObjects, double maxPixelError ) {
        if ( numberOfObjects < 1 ) {
            throw new IllegalArgumentException( "The number of objects per leaf may not be smaller than 1." );
        }
        this.maxPixelError = maxPixelError;
        this.numberOfObjects = numberOfObjects;
    }

    /**
     * @param validDomain
     * @param numberOfObjects
     *            each node will contain
     * @param maxPixelError
     */
    public QTModelScene( Envelope validDomain, int numberOfObjects, double maxPixelError ) {
        this( numberOfObjects, maxPixelError );
        if ( validDomain == null ) {
            throw new IllegalArgumentException( "The envelope must be set." );
        }
        double[] env = validDomain.getMin().getAsArray();
        this.min = new float[] { (float) env[0], (float) env[1], (float) ( ( env.length == 3 ) ? env[2] : 0 ) };
        env = validDomain.getMax().getAsArray();
        this.max = new float[] { (float) env[0], (float) env[1], (float) ( ( env.length == 3 ) ? env[2] : 0 ) };
        this.halfHeight = min[1] + ( .5f * (float) validDomain.getWidth() );
        this.halfWidth = min[0] + ( .5f * (float) validDomain.getHeight() );
    }

    /**
     * @param min
     * @param max
     * @param numberOfObjects
     *            each leaf node will contain
     * @param maxPixelError
     */
    public QTModelScene( float[] min, float[] max, int numberOfObjects, double maxPixelError ) {
        this( numberOfObjects, maxPixelError );
        this.min = min;
        this.max = max;
        this.halfHeight = min[1] + ( ( max[1] - min[1] ) * 0.5f );
        this.halfWidth = min[0] + ( ( max[0] - min[0] ) * 0.5f );
    }

    /**
     * @param object
     *            to insert
     * @return true if the object was inserted, false otherwise.
     */
    public boolean add( T object ) {
        if ( object != null && liesWithin( object.getPosition() ) ) {
            if ( isLeaf() ) {
                addObject( object );
            } else {
                getLeafNode( object.getPosition() ).addObject( object );
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
    public boolean remove( PositionableModel object ) {
        if ( object != null && liesWithin( object.getPosition() ) ) {
            return removeObject( object );
        }
        return false;
    }

    /**
     * @param object
     */
    private boolean removeObject( PositionableModel object ) {
        boolean result = false;
        if ( isLeaf() ) {
            if ( objects != null ) {
                result = objects.remove( object );
                if ( objects.isEmpty() ) {
                    objects = null;
                }
            }
        } else {
            QTModelScene<T> n = getChild( object.getPosition() );
            if ( n != null ) {
                result = n.removeObject( object );
                if ( result ) {
                    merge();
                }
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
                QTModelScene<T> n = children[i];
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
                objects = new ArrayList<T>( size );
                for ( QTModelScene<T> n : children ) {
                    if ( n != null && n.objects != null ) {
                        maxError = Math.max( maxError, n.maxError );
                        min[2] = Math.min( n.min[2], min[2] );
                        max[2] = Math.max( n.max[2], max[2] );
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
    private void addObject( T object ) {
        if ( objects == null ) {
            objects = new ArrayList<T>( numberOfObjects );
        }
        objects.add( object );
        maxError = Math.max( object.getErrorScalar(), maxError );
        min[2] = Math.min( object.getGroundLevel(), min[2] );
        max[2] = Math.max( min[2] + object.getObjectHeight(), max[2] );
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
        children = new QTModelScene[4];
        for ( T p : objects ) {
            getLeafNode( p.getPosition() ).addObject( p );
        }
        objects = null;
        maxError = Float.MIN_VALUE;

    }

    /**
     * @return true if this is a leaf node
     */
    public boolean isLeaf() {
        return children == null;
    }

    /**
     * @param position
     * @return the node which contains this position or <code>null</code> if the given position lies not within this
     *         node.
     */
    public QTModelScene<T> getNode( float[] position ) {
        if ( liesWithin( position ) ) {
            return getLeafNode( position );
        }
        return null;
    }

    /**
     * @param position
     *            to get a child for
     * @return the child for the position or <code>null</code> if the given node does not exist.
     */
    private QTModelScene<T> getChild( float[] position ) {
        QTModelScene<T> result = null;
        if ( !isLeaf() ) {
            int index = getIndex( position );
            result = children[index];
        }
        return result;
    }

    /**
     * Method without checking lies within.
     *
     * @return the leafnode which contains the given position.
     */
    private QTModelScene<T> getLeafNode( float[] position ) {
        if ( !isLeaf() ) {
            int index = getIndex( position );
            QTModelScene<T> child = children[index];
            // No children in that area create a new one
            if ( child == null ) {
                child = createNode( index );
                children[index] = child;
                return child;
            }
            // traverse tree
            return child.getLeafNode( position );
        }
        return this;
    }

    /**
     * @param position
     * @return
     */
    private QTModelScene<T> createNode( int index ) {
        float[] mi = new float[3];
        float[] ma = new float[3];
        switch ( index ) {
        case 0:
            mi = min;
            ma[0] = halfWidth;
            ma[1] = halfHeight;
            break;
        case 1:
            mi[0] = halfWidth;
            mi[1] = mi[1];
            ma[0] = max[0];
            ma[1] = halfHeight;
            break;
        case 2:
            mi[0] = min[0];
            mi[1] = halfHeight;
            ma[0] = halfWidth;
            ma[1] = max[1];
            break;
        case 3:
            mi[0] = halfWidth;
            mi[1] = halfHeight;
            ma = max;
            break;
        }
        return new QTModelScene<T>( mi, ma, numberOfObjects, maxPixelError );
    }

    /**
     * @return
     */
    private int getIndex( float[] position ) {
        return ( ( ( position[0] < halfWidth ) ? 0 : 1 ) + ( ( position[1] < halfHeight ) ? 0 : 2 ) );
    }

    /**
     * @param position
     * @return
     */
    private boolean liesWithin( float[] position ) {
        return min[0] <= position[0] && max[0] >= position[0] && min[1] < position[1] && max[1] > position[1];
    }

    /**
     *
     * @param bbMin
     * @param bbMax
     * @return true if this node intersects with the given bbox
     */
    private boolean intersects( float[] bbMin, float[] bbMax ) {
        return ( min[0] >= bbMin[0] && min[0] <= bbMax[0] ) || ( max[0] <= bbMax[0] && max[0] >= bbMin[0] )
               || ( min[1] >= bbMin[1] && min[1] <= bbMax[1] ) || ( max[1] <= bbMax[1] && max[1] >= bbMin[1] );
    }

    private void getObjects( float[] bbMin, float[] bbMax, List<T> result, Comparator<T> comparator ) {
        if ( intersects( bbMin, bbMax ) ) {
            if ( isLeaf() ) {
                if ( objects != null ) {
                    if ( comparator != null ) {
                        Collections.sort( objects, comparator );
                    }

                    result.addAll( objects );
                }
            } else {
                for ( QTModelScene<T> n : children ) {
                    if ( n != null ) {
                        n.getObjects( bbMin, bbMax, result, comparator );
                    }
                }
            }
        }
    }

    private void getObjects( ViewParams viewParams, float[] eye, List<T> result, Comparator<T> comparator ) {
        float[][] bbox = new float[][] { min, max };
        if ( viewParams.getViewFrustum().intersects( bbox ) ) {
            if ( isLeaf() ) {
                if ( objects != null ) {
                    double distance = VectorUtils.getDistance( bbox, eye );
                    double estimatePixel = viewParams.estimatePixelSizeForSpaceUnit( distance );
                    double estError = estimatePixel * maxError;
                    if ( distance <= 1E-10 || ( estError > maxPixelError ) ) {
                        if ( comparator != null ) {
                            Collections.sort( objects, comparator );
                        }

                        for ( T obj : objects ) {
                            distance = Vectors3f.distance( eye, obj.getPosition() );
                            double estPixelSize = viewParams.estimatePixelSizeForSpaceUnit( distance );
                            boolean noPixelError = ( obj.getErrorScalar() * estPixelSize ) > maxPixelError;
                            float[][] objBBox = obj.getModelBBox();
                            boolean intersects = viewParams.getViewFrustum().intersects( objBBox );
                            if ( noPixelError && intersects ) {
                                result.add( obj );
                            }

                        }
                    }
                }

            } else {
                for ( QTModelScene<T> n : children ) {
                    if ( n != null ) {
                        n.getObjects( viewParams, eye, result, comparator );
                    }
                }
            }
        }
    }

    /**
     * @param env
     *            to get the objects for.
     * @return the objects which intersect with this node and or it's children, or the empty list.
     */
    public List<T> getObjects( Envelope env ) {
        List<T> result = new LinkedList<T>();

        Point min = env.getMin();
        Point max = env.getMax();
        int dim = min.getCoordinateDimension();
        getObjects( new float[] { (float) min.get( 0 ), (float) min.get( 1 ),
                                 ( ( dim == 2 ) ? 0 : (float) min.get( 2 ) ) },
                    new float[] { (float) max.get( 0 ), (float) max.get( 1 ),
                                 ( ( dim == 2 ) ? 0 : (float) max.get( 2 ) ) }, result, null );
        return result;
    }

    /**
     * @param env
     *            to get the objects for.
     * @param comparator
     *            to be used for sorting the objects in each leaf, not the result as a total. If <code>null</code> no
     *            sorting of the objects will be done.
     * @return the objects which intersect with this node and or it's children, or the empty list.
     */
    public List<T> getObjects( Envelope env, Comparator<T> comparator ) {
        List<T> result = new LinkedList<T>();

        Point min = env.getMin();
        Point max = env.getMax();
        int dim = min.getCoordinateDimension();
        getObjects( new float[] { (float) min.get( 0 ), (float) min.get( 1 ),
                                 ( ( dim == 2 ) ? 0 : (float) min.get( 2 ) ) },
                    new float[] { (float) max.get( 0 ), (float) max.get( 1 ),
                                 ( ( dim == 2 ) ? 0 : (float) max.get( 2 ) ) }, result, comparator );
        return result;
    }

    /**
     * @param viewParams
     *            to get the objects for.
     * @param comparator
     *            to be used for sorting the objects in each leaf, not the result as a total. If <code>null</code> no
     *            sorting of the objects will be done.
     * @return the objects which intersect with the given view parameters and or it's children, or the empty list.
     */
    public List<T> getObjects( ViewParams viewParams, Comparator<T> comparator ) {
        List<T> result = new LinkedList<T>();
        Point3d e = viewParams.getViewFrustum().getEyePos();
        float[] eye = new float[] { (float) e.x, (float) e.y, (float) e.z };
        getObjects( viewParams, eye, result, comparator );
        return result;
    }

    /**
     * @return the objects which intersect with this node and or it's children, or the empty list.
     */
    public List<T> getObjects() {
        List<T> result = new LinkedList<T>();
        getObjects( min, max, result, null );
        return result;
    }

    /**
     * @param comparator
     *            to be used for sorting the objects in each leaf, not the result as a total. If <code>null</code> no
     *            sorting of the objects will be done.
     * @return the objects which intersect with this node and or it's children, or the empty list.
     */
    public List<T> getObjects( Comparator<T> comparator ) {
        List<T> result = new LinkedList<T>();
        getObjects( min, max, result, comparator );
        return result;
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
            for ( QTModelScene<T> n : children ) {
                if ( n != null ) {
                    n.clear();
                }
            }
            children = null;
        }
    }

    /**
     * @param object
     * @return true if this tree contains the given object
     */
    public boolean contains( PositionableModel object ) {
        boolean result = false;
        if ( object != null && liesWithin( object.getPosition() ) ) {
            QTModelScene<T> n = getLeafNode( object.getPosition() );
            if ( n.objects != null ) {
                result = n.objects.contains( object );
            }
        }
        return result;
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
                QTModelScene<T> child = children[i];
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

    /**
     * @return the configured max pixel error.
     */
    public final double getMaxPixelError() {
        return maxPixelError;
    }
}
