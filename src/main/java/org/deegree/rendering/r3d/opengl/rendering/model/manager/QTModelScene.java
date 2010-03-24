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

import static org.deegree.geometry.utils.GeometryUtils.createEnvelope;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.vecmath.Point3d;

import org.deegree.commons.index.PositionableModel;
import org.deegree.commons.index.QTree;
import org.deegree.commons.utils.math.VectorUtils;
import org.deegree.commons.utils.math.Vectors3f;
import org.deegree.geometry.Envelope;
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
public class QTModelScene<T extends PositionableModel> extends QTree<T> {

    // the most significant error of a node.
    private float maxError = Float.MIN_VALUE;

    private double maxPixelError;

    /**
     * Create son node.
     * 
     * @param numberOfObjects
     * @param envelope
     * @param depth
     */
    private QTModelScene( int numberOfObjects, float[] envelope, byte depth, double maxPixelError ) {
        super( numberOfObjects, envelope, depth );
        this.maxPixelError = maxPixelError;
    }

    /**
     * 
     * @param validDomain
     * @param numberOfObjects
     * @param maxPixelError
     */
    public QTModelScene( Envelope validDomain, int numberOfObjects, double maxPixelError ) {
        super( createEnvelope( validDomain ), numberOfObjects );
        this.maxPixelError = maxPixelError;
    }

    /**
     * This method uses the envelope of the object instead of the given envelope;
     * 
     * @param envelope
     *            of the object
     * @param object
     *            to insert
     * @return true if the object was inserted, false otherwise.
     */
    @Override
    public boolean insert( float[] envelope, T object ) {
        if ( envelope == null || object == null ) {
            return false;
        }
        return insert( object );
    }

    /**
     * @param object
     *            to insert
     * @return true if the object was inserted, false otherwise.
     */
    public boolean insert( T object ) {
        if ( object == null ) {
            return false;
        }
        this.maxError = Math.max( object.getErrorScalar(), maxError );
        return super.insert( object.getModelBBox(), object );
    }

    private void getObjects( ViewParams viewParams, float[] eye, Set<T> result ) {
        if ( viewParams.getViewFrustum().intersects( getEnvelope() ) ) {
            if ( hasCoveringObjects() ) {
                for ( Entry<T> obj : objectsCoveringEnv ) {
                    result.add( obj.entryValue );
                }
            }
            if ( isLeaf() ) {
                if ( leafObjects != null ) {
                    double distance = VectorUtils.getDistance( envelope, eye );

                    double estimatePixel = viewParams.estimatePixelSizeForSpaceUnit( distance );
                    double estError = estimatePixel * maxError;
                    if ( distance <= 1E-10 || ( estError > maxPixelError ) ) {
                        for ( Entry<T> obj : leafObjects ) {
                            distance = Vectors3f.distance( eye, 0, obj.entryEnv, 0 );
                            double estPixelSize = viewParams.estimatePixelSizeForSpaceUnit( distance );
                            boolean noPixelError = ( obj.entryValue.getErrorScalar() * estPixelSize ) > maxPixelError;
                            boolean intersects = viewParams.getViewFrustum().intersects( obj.entryEnv );
                            if ( noPixelError && intersects ) {
                                result.add( obj.entryValue );
                            }

                        }
                    }
                }

            } else {
                for ( QTree<T> n : children ) {
                    if ( n != null ) {
                        ( (QTModelScene<T>) n ).getObjects( viewParams, eye, result );
                    }
                }
            }
        }
    }

    @Override
    protected QTModelScene<T> createNode( int son ) {
        float[] newEnv = bboxForSon( son );
        return new QTModelScene<T>( numberOfObjects, newEnv, (byte) ( currentDepth + 1 ), maxPixelError );
    }

    /**
     * @param viewParams
     *            to get the objects for.
     * @return the objects which intersect with the given view parameters and or it's children, or the empty list.
     */
    public Set<T> getObjects( ViewParams viewParams ) {
        Set<T> result = new HashSet<T>();
        Point3d e = viewParams.getViewFrustum().getEyePos();
        float[] eye = new float[] { (float) e.x, (float) e.y, (float) e.z };
        getObjects( viewParams, eye, result );
        return result;
    }

    /**
     * @param object
     * @return true if this tree contains the given object
     */
    public boolean contains( PositionableModel object ) {
        if ( object != null && intersects( envelope, object.getModelBBox(), getMaxOffset() ) ) {
            List<T> r = getObjects( object.getModelBBox() );
            if ( r != null && !r.isEmpty() ) {
                return r.contains( object );
            }
        }
        return false;
    }

    /**
     * @return the configured max pixel error.
     */
    public final double getMaxPixelError() {
        return maxPixelError;
    }
}
