//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.commons.index;

import java.util.Collection;
import java.util.List;

import org.deegree.commons.utils.Pair;
import org.deegree.geometry.Envelope;

/**
 * The <code>SpatialIndex</code> defines basic methods for the adding, removing and querying of a spatial index.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * @param <T>
 *            the type returned by the query
 * 
 */
public abstract class SpatialIndex<T> {

    /**
     * Query the spatial index with the given envelope and return all objects which intersect with the given
     * boundingbox.
     * 
     * @param envelope
     *            to intersect
     * @return the list of intersecting objects.
     */
    public abstract Collection<T> query( Envelope envelope );

    /**
     * Create the spatial index from the given list of envelope, objects tuples.
     * 
     * @param listOfObjects
     *            to be inserted into the spatial index.
     */
    public abstract void insertBulk( List<Pair<Envelope, T>> listOfObjects );

    /**
     * Removes all objects from this spatial index.
     */
    public abstract void clear();

    /**
     * Add the given object to the spatial index using the given boundingbox
     * 
     * @param envelope
     *            of the object
     * @param object
     *            to insert
     * @return true if the object could be inserted.
     * @throws UnsupportedOperationException
     *             if the implementation does not support inserting single objects
     */
    public abstract boolean insert( Envelope envelope, T object );

    /**
     * Removes the given object from this spatial index, using the objects' equals method.
     * 
     * @param object
     *            to be removed
     * @return true if the removal was successful, false otherwise
     * @throws UnsupportedOperationException
     *             if the implementation does not support removal of objects
     */
    public abstract boolean remove( T object );

    /**
     * Tests whether one point lies in the given bbox
     * 
     * @param box
     * @param maxOffset
     *            the offset within the bbox where the max point starts.
     * @param x
     * @param y
     * @return
     */
    private static final boolean contains( final float[] box, final int maxOffset, final float x, final float y ) {
        return box[0] <= x && x <= box[maxOffset] && box[1] <= y && y <= box[maxOffset + 1];
    }

    /**
     * tests whether a bbox is overlapping another without actually being inside it.
     * 
     * @param box1
     * @param box2
     * @param maxOffset
     *            the offset within the bbox where the max point starts.
     * @return
     */
    private static final boolean noEdgeOverlap( final float[] box1, final float[] box2, final int maxOffset ) {
        return box1[0] <= box2[0] && box2[1] <= box1[1] && box2[maxOffset] <= box1[maxOffset]
               && box1[maxOffset + 1] <= box2[maxOffset + 1];
    }

    /**
     * Test if two envelopes intersect, bbox must be defined as float[4]=min[0],min[1];max[0],max[1]
     * 
     * @param box1
     *            the first envelope
     * @param box2
     *            the second envelope
     * @param maxOffset
     *            the offset within the bbox where the max point starts.
     * @return true if the given boxes intersects with each other.
     */
    protected boolean intersects( final float[] box1, final float[] box2, int maxOffset ) {
        return contains( box2, maxOffset, box1[0], box1[maxOffset + 1] )
               || contains( box2, maxOffset, box1[0], box1[1] )
               || contains( box2, maxOffset, box1[maxOffset], box1[maxOffset + 1] )
               || contains( box2, maxOffset, box1[maxOffset], box1[1] )
               || contains( box1, maxOffset, box2[0], box2[maxOffset + 1] )
               || contains( box1, maxOffset, box2[0], box2[1] )
               || contains( box1, maxOffset, box2[maxOffset], box2[maxOffset + 1] )
               || contains( box1, maxOffset, box2[maxOffset], box2[1] ) || noEdgeOverlap( box1, box2, maxOffset )
               || noEdgeOverlap( box2, box1, maxOffset );

    }

    /**
     * Creates float array out of an envelope (Geometry).
     * 
     * @param validDomain
     * @return a float[] representation of the given envelope
     */
    public static final float[] createEnvelope( Envelope validDomain ) {
        int dim = validDomain.getCoordinateDimension();
        double[] env = validDomain.getMin().getAsArray();

        if ( !( dim == 3 || dim == 2 ) ) {
            throw new IllegalArgumentException( "The envelope must be 2 or 3 dimensional." );
        }
        float[] envelope = new float[dim * 2];
        int index = 0;
        envelope[index++] = (float) env[0];
        envelope[index++] = (float) env[1];
        if ( dim == 3 ) {
            envelope[index++] = (float) env[2];
        }
        env = validDomain.getMax().getAsArray();
        envelope[index++] = (float) env[0];
        envelope[index++] = (float) env[1];
        if ( dim == 3 ) {
            envelope[index] = (float) env[2];
        }
        return envelope;
    }

}