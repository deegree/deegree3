//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/rendering/r3d/QualityModel.java $
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
package org.deegree.model.multiresolution;

import java.nio.ByteBuffer;

/**
 * A node of a {@link MultiresolutionMesh}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$
 */
public class Node {

    // the Batched Multi-Triangulation that this node belongs to
    // private BatchedMT mt;

    /** Size of binary representation (in bytes) */
    public static int SIZE = 40;

    private static int LOWEST_OUTGOING_OFFSET = 0;

    private static int HIGHEST_OUTGOING_OFFSET = 4;

    private static int LOWEST_INCOMING_OFFSET = 8;

    private static int GEOMETRY_ERROR_OFFSET = 12;

    private static int BBOX_OFFSET = 16;

    /** Id of the node. */
    public final int id;

    /** Lowest id of all arcs leaving from this node. */
    public final int lowestOutgoingArc;

    /** Highest id of all arcs leaving from this node. */
    public final int highestOutgoingArc;

    /** Lowest id of all arcs entering this node. */
    public final int lowestIncomingArc;

    /** Geometry error associated with the patches of this node. */
    public final float geometryError;

    /** Bounding box of this node. */
    public final float[][] bbox = new float[2][3];

    /**
     * Creates a new {@link Node} instance.
     * 
     * @param mt
     *                {@link MultiresolutionMesh} instance that the node is part of
     * @param id
     *                id of the node
     * @param buffer
     *                buffer that contains the binary representation of the node
     * @param baseOffset
     *                offset in the buffer
     */
    Node(MultiresolutionMesh mt, int id, ByteBuffer buffer, int baseOffset) {
        // this.mt = mt;
        this.id = id;
        lowestOutgoingArc = buffer.getInt(baseOffset + LOWEST_OUTGOING_OFFSET);
        highestOutgoingArc = buffer.getInt(baseOffset + HIGHEST_OUTGOING_OFFSET);
        lowestIncomingArc = buffer.getInt(baseOffset + LOWEST_INCOMING_OFFSET);
        geometryError = buffer.getFloat(baseOffset + GEOMETRY_ERROR_OFFSET);
        bbox[0][0] = buffer.getFloat(baseOffset + BBOX_OFFSET + 0);
        bbox[0][1] = buffer.getFloat(baseOffset + BBOX_OFFSET + 4);
        bbox[0][2] = buffer.getFloat(baseOffset + BBOX_OFFSET + 8);
        bbox[1][0] = buffer.getFloat(baseOffset + BBOX_OFFSET + 12);
        bbox[1][1] = buffer.getFloat(baseOffset + BBOX_OFFSET + 16);
        bbox[1][2] = buffer.getFloat(baseOffset + BBOX_OFFSET + 20);
    }

    /**
     * Stores the information of a <code>Node</code> in the given <code>ByteBuffer</code>.
     * 
     * @param target
     *                buffer where the binary representation is written to
     * @param lowestOutgoingArc
     *                lowest id of all arcs leaving from this node
     * @param highestOutgoingArc
     *                highest id of all arcs leaving from this node
     * @param lowestIncomingArc
     *                lowest id of all arcs entering this node
     * @param error
     *                geometry error associated with the patches of this node
     * @param bbox
     *                bounding box of this node
     */
    public static void store(ByteBuffer target, int lowestOutgoingArc, int highestOutgoingArc,
            int lowestIncomingArc, float error, float[][] bbox) {
        target.putInt(lowestOutgoingArc);
        target.putInt(highestOutgoingArc);
        target.putInt(lowestIncomingArc);
        target.putFloat(error);
        target.putFloat(bbox[0][0]);
        target.putFloat(bbox[0][1]);
        target.putFloat(bbox[0][2]);
        target.putFloat(bbox[1][0]);
        target.putFloat(bbox[1][1]);
        target.putFloat(bbox[1][2]);
    }
}
