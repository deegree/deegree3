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

/**
 * A node of a {@link MultiresolutionMesh}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$
 */
public class Node {

    /** Size of binary representation (in bytes) */
    public final static int SIZE = 36;

    private final static int LOWEST_OUTGOING_OFFSET = 0;

    private final static int HIGHEST_OUTGOING_OFFSET = 4;

    private final static int LOWEST_INCOMING_OFFSET = 8;

    private final static int BBOX_OFFSET = 12;

    /** Id of the node. */
    public final int id;

    /** Lowest id of all arcs leaving from this node. */
    public final int lowestOutgoingArc;

    /** Highest id of all arcs leaving from this node. */
    public final int highestOutgoingArc;

    /** Lowest id of all arcs entering this node. */
    public final int lowestIncomingArc;

    /** Bounding box of this node. */
    public final float[][] bbox = new float[2][3];

    /**
     * Creates a new {@link Node} instance.
     * 
     * @param mt
     *            {@link MultiresolutionMesh} instance that the node is part of
     * @param id
     *            id of the node
     * @param buffer
     *            buffer that contains the binary representation of the node
     * @param baseOffset
     *            offset in the buffer
     */
    Node( MultiresolutionMesh mt, int id, ByteBuffer buffer, int baseOffset ) {
        this.id = id;
        lowestOutgoingArc = buffer.getInt( baseOffset + LOWEST_OUTGOING_OFFSET );
        highestOutgoingArc = buffer.getInt( baseOffset + HIGHEST_OUTGOING_OFFSET );
        lowestIncomingArc = buffer.getInt( baseOffset + LOWEST_INCOMING_OFFSET );
        bbox[0][0] = buffer.getFloat( baseOffset + BBOX_OFFSET + 0 );
        bbox[0][1] = buffer.getFloat( baseOffset + BBOX_OFFSET + 4 );
        bbox[0][2] = buffer.getFloat( baseOffset + BBOX_OFFSET + 8 );
        bbox[1][0] = buffer.getFloat( baseOffset + BBOX_OFFSET + 12 );
        bbox[1][1] = buffer.getFloat( baseOffset + BBOX_OFFSET + 16 );
        bbox[1][2] = buffer.getFloat( baseOffset + BBOX_OFFSET + 20 );
    }

    /**
     * Stores the information of a <code>Node</code> in the given <code>ByteBuffer</code>.
     * 
     * @param target
     *            buffer where the binary representation is written to
     * @param lowestOutgoingArc
     *            lowest id of all arcs leaving from this node
     * @param highestOutgoingArc
     *            highest id of all arcs leaving from this node
     * @param lowestIncomingArc
     *            lowest id of all arcs entering this node
     * @param bbox
     *            bounding box of this node
     */
    public static void store( ByteBuffer target, int lowestOutgoingArc, int highestOutgoingArc, int lowestIncomingArc,
                              float[][] bbox ) {
        target.putInt( lowestOutgoingArc );
        target.putInt( highestOutgoingArc );
        target.putInt( lowestIncomingArc );
        target.putFloat( bbox[0][0] );
        target.putFloat( bbox[0][1] );
        target.putFloat( bbox[0][2] );
        target.putFloat( bbox[1][0] );
        target.putFloat( bbox[1][1] );
        target.putFloat( bbox[1][2] );
    }
}
