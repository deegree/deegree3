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

import org.deegree.rendering.r3d.multiresolution.MeshFragmentData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment me
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$
 */
public class MeshFragmentDataReader {

    private static final Logger LOG = LoggerFactory.getLogger( MeshFragmentDataReader.class );

    private FileChannel channel;

    public MeshFragmentDataReader( File meshFragments ) throws FileNotFoundException {
        this.channel = new FileInputStream( meshFragments ).getChannel();
    }

    public MeshFragmentData read( int fragmentId, long offset, int length )
                            throws IOException {

        ByteBuffer rawTileBuffer = ByteBuffer.allocateDirect( length );
        rawTileBuffer.order( ByteOrder.nativeOrder() );        
        
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
        return new MeshFragmentData( vertexBuffer, normalsBuffer, indexBuffer);
    }
}
