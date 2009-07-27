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

import java.util.List;

import org.deegree.commons.utils.Pair;
import org.deegree.geometry.Envelope;

/**
 * The <code>SpatialIndex</code> defines basic methods for the adding and querying of a spatial index.
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
    public abstract List<T> query( Envelope envelope );

    /**
     * Create the spatial index from the given list of envelope, objects tuples.
     * 
     * @param listOfObjects
     *            to be inserted into the spatial index.
     */
    public abstract void buildIndex( List<Pair<float[], T>> listOfObjects );

    /**
     * Add the given object to the spatial index using the given boundingbox
     * 
     * @param envelope
     *            of the object
     * @param object
     *            to insert
     * @return true if the object could be inserted.
     */
    public abstract boolean insert( float[] envelope, T object );

    private static final boolean contained( final float[] box, final float x, final float y ) {
        return box[0] <= x && x <= box[2] && box[1] <= y && y <= box[3];
    }

    private static final boolean noEdgeOverlap( final float[] box1, final float[] box2 ) {
        return box1[0] <= box2[0] && box2[2] <= box1[2] && box2[1] <= box1[1] && box1[3] <= box2[3];
    }

    /**
     * Test if two envelopes intersect, bbox must be defined as float[4]=min[0],min[1];max[0],max[1]
     * 
     * @param box1
     *            the first envelope
     * @param box2
     *            the second envelope
     * @return true if the given boxes intersects with eachother.
     */
    protected boolean intersects( final float[] box1, final float[] box2 ) {
        return contained( box2, box1[0], box1[3] ) || contained( box2, box1[0], box1[1] )
               || contained( box2, box1[2], box1[3] ) || contained( box2, box1[2], box1[1] )
               || contained( box1, box2[0], box2[3] ) || contained( box1, box2[0], box2[1] )
               || contained( box1, box2[2], box2[3] ) || contained( box1, box2[2], box2[1] )
               || noEdgeOverlap( box1, box2 ) || noEdgeOverlap( box2, box1 );

    }

    /**
     * Creates float array out of an envelope (Geometry).
     * 
     * @param validDomain
     * @return a float[] representation of the given envelope
     */
    protected float[] createEnvelope( Envelope validDomain ) {
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