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
package org.deegree.rendering.r3d.multiresolution.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;

import org.deegree.commons.utils.nio.DirectByteBufferPool;
import org.deegree.commons.utils.nio.PooledByteBuffer;
import org.deegree.rendering.r3d.multiresolution.MeshFragmentData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class opens a channel to a file containing meshfragments.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$
 */
public class MeshFragmentDataReader {

    private static final Logger LOG = LoggerFactory.getLogger( MeshFragmentDataReader.class );

    // TODO not static
    private static final DirectByteBufferPool bufferPool = new DirectByteBufferPool( 300 * 1024 * 1024, 500 );

    private final FileChannel channel;

    /**
     * Construct access to a file containing mesh fragments.
     *
     * @param meshFragments
     * @throws FileNotFoundException
     */
    public MeshFragmentDataReader( File meshFragments ) throws FileNotFoundException {
        this.channel = new FileInputStream( meshFragments ).getChannel();
    }

    /**
     * Read meshdata from the file.
     *
     * @param fragmentId
     * @param offset
     * @param length
     * @return the actual data read from the file.
     * @throws IOException
     */
    public MeshFragmentData read( int fragmentId, long offset, int length )
                            throws IOException {

        PooledByteBuffer pooledByteBuffer = bufferPool.allocate( length );
        // PooledByteBuffer pooledByteBuffer = new PooledByteBuffer(length);
        ByteBuffer rawTileBuffer = pooledByteBuffer.getBuffer();
        // rawTileBuffer.order( ByteOrder.nativeOrder() );

        LOG.debug( "Reading mesh fragment with id " + fragmentId + " (offset: " + offset + ", length: " + length + ")." );
        long begin = System.currentTimeMillis();
        channel.read( rawTileBuffer, offset );
        long elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Reading took " + elapsed + " milliseconds." );

        rawTileBuffer.rewind();
        int numVertices = rawTileBuffer.getInt();

        // generate contained buffers
        rawTileBuffer.limit( rawTileBuffer.position() + numVertices * 4 * 3 );
        ByteBuffer verticesSlice = rawTileBuffer.slice();
        verticesSlice.order( ByteOrder.nativeOrder() );

        FloatBuffer vertexBuffer = verticesSlice.asFloatBuffer();
        rawTileBuffer.position( rawTileBuffer.position() + numVertices * 4 * 3 );

        FloatBuffer normalsBuffer = null;
        rawTileBuffer.limit( rawTileBuffer.position() + numVertices * 4 * 3 );
        ByteBuffer normalsSlice = rawTileBuffer.slice();
        normalsSlice.order( ByteOrder.nativeOrder() );
        normalsBuffer = normalsSlice.asFloatBuffer();
        rawTileBuffer.position( rawTileBuffer.position() + numVertices * 4 * 3 );

        rawTileBuffer.limit( length );
        Buffer indexBuffer = null;
        ByteBuffer indexSlice = rawTileBuffer.slice();
        indexSlice.order( ByteOrder.nativeOrder() );

        if ( numVertices <= 255 ) {
            indexBuffer = indexSlice;
        } else if ( numVertices <= 65535 ) {
            indexBuffer = indexSlice.asShortBuffer();
            indexBuffer.rewind();
        } else {
            indexBuffer = indexSlice.asIntBuffer();
        }
        return new MeshFragmentData( pooledByteBuffer, vertexBuffer, normalsBuffer, indexBuffer );
    }
}
