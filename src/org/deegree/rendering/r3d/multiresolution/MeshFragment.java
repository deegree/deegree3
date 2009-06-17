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
package org.deegree.rendering.r3d.multiresolution;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.deegree.rendering.r3d.multiresolution.io.MeshFragmentDataReader;

/**
 * Encapsulates the bounding box and approximation error for a fragment of a {@link MultiresolutionMesh} and provides
 * access to the actual geometry data.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$
 */
public class MeshFragment implements Comparable<MeshFragment> {

    /**
     * Size of binary representation (in bytes).
     *
     * NOTE: This is just the meta information of the patch, not the geometry data.
     */
    public static int SIZE = 40;

    /**
     * the id of the mesh fragment
     */
    public final int id;

    /**
     * The bbox of the frament
     */
    public final float[][] bbox = new float[2][3];

    /**
     * The geometric error.
     */
    public final float error;

    private long blobPosition;

    private int length;

    private MeshFragmentDataReader patchReader;

    MeshFragment( int id, ByteBuffer buffer, MeshFragmentDataReader patchReader ) {
        this.id = id;
        this.patchReader = patchReader;
        this.bbox[0][0] = buffer.getFloat();
        this.bbox[0][1] = buffer.getFloat();
        this.bbox[0][2] = buffer.getFloat();
        this.bbox[1][0] = buffer.getFloat();
        this.bbox[1][1] = buffer.getFloat();
        this.bbox[1][2] = buffer.getFloat();
        this.error = buffer.getFloat();
        this.blobPosition = buffer.getLong();
        this.length = buffer.getInt();
    }

    /**
     * @return offset of in the file.
     */
    public long getOffset() {
        return blobPosition;
    }

    /**
     * @return the last byte position.
     */
    public long getLastByteOffset() {
        return blobPosition + length - 1;
    }

    /**
     * Retrieves the actual geometry data of the {@link MeshFragment}.
     * <p>
     * NOTE: Calling this method usually involves I/O and memory allocation operations and the caller should probably
     * incorporate caching mechanisms to reduce the number of calls.
     * </p>
     *
     * @return the actual geometry data of the fragment
     * @throws IOException
     */
    public MeshFragmentData loadData()
                            throws IOException {
        return patchReader.read( id, blobPosition, length );
    }

    /**
     * Save this fragment to the given ByteBuffer.
     *
     * @param target
     * @param minX
     * @param minY
     * @param minZ
     * @param maxX
     * @param maxY
     * @param maxZ
     * @param error
     * @param blobPosition
     * @param length
     */
    public static void store( ByteBuffer target, float minX, float minY, float minZ, float maxX, float maxY,
                              float maxZ, float error, long blobPosition, int length ) {
        target.putFloat( minX );
        target.putFloat( minY );
        target.putFloat( minZ );
        target.putFloat( maxX );
        target.putFloat( maxY );
        target.putFloat( maxZ );
        target.putFloat( error );
        target.putLong( blobPosition );
        target.putInt( length );
    }

    /**
     * Save this fragment to the given byte buffer.
     *
     * @param target
     * @param bbox
     * @param error
     * @param blobPosition
     * @param length
     */
    public static void store( ByteBuffer target, float[][] bbox, float error, long blobPosition, int length ) {
        target.putFloat( bbox[0][0] );
        target.putFloat( bbox[0][1] );
        target.putFloat( bbox[0][2] );
        target.putFloat( bbox[1][0] );
        target.putFloat( bbox[1][1] );
        target.putFloat( bbox[1][2] );
        target.putFloat( error );
        target.putLong( blobPosition );
        target.putInt( length );
    }

    @Override
    public String toString() {
        return "blobPosition: " + blobPosition + ", length: " + length;
    }

    @Override
    public int compareTo( MeshFragment o ) {
        if ( id < o.id ) {
            return -1;
        }
        if ( id > o.id ) {
            return 1;
        }
        return 0;
    }
}
