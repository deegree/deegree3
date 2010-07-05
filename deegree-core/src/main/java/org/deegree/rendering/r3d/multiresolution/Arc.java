//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/rendering/r3d/QualityModel.java $
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
package org.deegree.rendering.r3d.multiresolution;

import java.nio.ByteBuffer;

import org.deegree.geometry.Geometry;
import org.deegree.rendering.r3d.ViewFrustum;

/**
 * A directed arc of a {@link MultiresolutionMesh}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$
 */
public class Arc {

    // the Batched Multi-Triangulation DAG that this arc belongs to
    private MultiresolutionMesh mt;

    /** Size of binary representation (in bytes) */
    public final static int SIZE = 24;

    private final static int SOURCE_NODE_OFFSET = 0;

    private final static int DESTINATION_NODE_OFFSET = 4;

    private final static int LOWEST_PATCH_OFFSET = 8;

    private final static int HIGHEST_PATCH_OFFSET = 12;

    private final static int NEXT_ARC_SAME_DESTINATION_OFFSET = 16;

    private final static int GEOMETRIC_ERROR_OFFSET = 20;

    /** Id of the arc. */
    public final int id;

    /** Id of the source node. */
    public final int sourceNode;

    /** Id of the destination node. */
    public final int destinationNode;

    /** Lowest patch id in the label of the arc. */
    public final int lowestPatch;

    /** Highest patch id in the label of the arc. */
    public final int highestPatch;

    /** Id of the next arc with the same destination node (-1 if there is none). */
    public final int nextArcWithSameDestination;

    /** Geometric error (highest error of all fragments). */
    public final float geometricError;

    private MeshFragment[] fragments;

    /**
     * Creates a new {@link Arc} instance.
     * 
     * @param mt
     *            {@link MultiresolutionMesh} instance that the arc is part of
     * @param id
     *            id of the arc
     * @param buffer
     *            buffer that contains the binary representation of the arc
     * @param baseOffset
     *            offset in the buffer
     */
    Arc( MultiresolutionMesh mt, int id, ByteBuffer buffer, int baseOffset ) {
        this.mt = mt;
        this.id = id;
        sourceNode = buffer.getInt( baseOffset + SOURCE_NODE_OFFSET );
        destinationNode = buffer.getInt( baseOffset + DESTINATION_NODE_OFFSET );
        lowestPatch = buffer.getInt( baseOffset + LOWEST_PATCH_OFFSET );
        highestPatch = buffer.getInt( baseOffset + HIGHEST_PATCH_OFFSET );
        nextArcWithSameDestination = buffer.getInt( baseOffset + NEXT_ARC_SAME_DESTINATION_OFFSET );
        geometricError = buffer.getFloat( baseOffset + GEOMETRIC_ERROR_OFFSET );
    }

    /**
     * Stores the information of an {@link Arc} in the given <code>ByteBuffer</code>.
     * 
     * @param target
     *            buffer where the binary representation is written to
     * @param sourceNode
     *            id of the source node of the arc
     * @param destinationNode
     *            id of the destination node of the arc
     * @param lowestPatch
     *            lowest patch id in the label of the arc
     * @param highestPatch
     *            highest patch id in the label of the arc
     * @param nextArcWithSameDestination
     *            id of the next arc with the same destination node (-1 if there is none)
     * @param geometricError
     *            geometric error
     */
    public static void store( ByteBuffer target, int sourceNode, int destinationNode, int lowestPatch,
                              int highestPatch, int nextArcWithSameDestination, float geometricError ) {
        target.putInt( sourceNode );
        target.putInt( destinationNode );
        target.putInt( lowestPatch );
        target.putInt( highestPatch );
        target.putInt( nextArcWithSameDestination );
        target.putFloat( geometricError );
    }

    /**
     * Returns the fragments associated with this arc.
     * 
     * @return the fragments associated
     */
    public MeshFragment[] getFragments() {
        if ( fragments == null ) {
            fragments = new MeshFragment[highestPatch - lowestPatch + 1];
            for ( int i = 0; i < fragments.length; i++ ) {
                fragments[i] = mt.fragments[i + lowestPatch];
            }
        }
        return fragments;
    }

    /**
     * Determines if this arc interferes with the given {@link Geometry}.
     * 
     * @param roi
     *            geometry that is tested for interference
     * @param zScale
     *            scaling factor applied to z values of the mesh geometry (and bounding boxes)
     * @return true, if the arc interferes with the geometry, false otherwise
     */
    public boolean interferes( ViewFrustum roi, float zScale ) {
        float[][] nodeBBox = getBBox();
        nodeBBox[0][2] *= zScale;
        nodeBBox[1][2] *= zScale;
        return roi.intersects( nodeBBox );
    }

    /**
     * Returns the bounding box of the region that this arc represents.
     * 
     * @return the bounding box
     */
    public float[][] getBBox() {
        float[][] bbox = new float[2][3];
        bbox[0][0] = mt.fragments[lowestPatch].bbox[0][0];
        bbox[0][1] = mt.fragments[lowestPatch].bbox[0][1];
        bbox[0][2] = mt.fragments[lowestPatch].bbox[0][2];
        bbox[1][0] = mt.fragments[lowestPatch].bbox[1][0];
        bbox[1][1] = mt.fragments[lowestPatch].bbox[1][1];
        bbox[1][2] = mt.fragments[lowestPatch].bbox[1][2];
        for ( int patchId = lowestPatch + 1; patchId <= highestPatch; patchId++ ) {
            enlargeBBox( bbox, mt.fragments[patchId].bbox );
        }
        return bbox;
    }

    /**
     * Enlarges the given bounding box so that it includes the other bounding box.
     * 
     * @param bbox
     * @param bboxToInclude
     */
    private void enlargeBBox( float[][] bbox, float[][] bboxToInclude ) {
        if ( bbox[0][0] > bboxToInclude[0][0] ) {
            bbox[0][0] = bboxToInclude[0][0];
        }
        if ( bbox[0][1] > bboxToInclude[0][1] ) {
            bbox[0][1] = bboxToInclude[0][1];
        }
        if ( bbox[0][2] > bboxToInclude[0][2] ) {
            bbox[0][2] = bboxToInclude[0][2];
        }
        if ( bbox[1][0] < bboxToInclude[1][0] ) {
            bbox[1][0] = bboxToInclude[1][0];
        }
        if ( bbox[1][1] < bboxToInclude[1][1] ) {
            bbox[1][1] = bboxToInclude[1][1];
        }
        if ( bbox[1][2] < bboxToInclude[1][2] ) {
            bbox[1][2] = bboxToInclude[1][2];
        }
    }
}
