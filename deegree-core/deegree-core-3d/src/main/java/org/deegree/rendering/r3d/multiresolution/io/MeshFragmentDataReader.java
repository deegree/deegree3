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
import java.nio.ShortBuffer;
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
 */
public class MeshFragmentDataReader {

	private static final Logger LOG = LoggerFactory.getLogger(MeshFragmentDataReader.class);

	private final DirectByteBufferPool bufferPool;

	// = new DirectByteBufferPool( 1000 * 1024 * 1024, 2000, "static_mesh" );

	private final FileChannel channel;

	private ShortBuffer indexBuffer;

	private final int rowsPerMt;

	/**
	 * Construct access to a file containing mesh fragments.
	 * @param meshFragments
	 * @param directBufferPool
	 * @throws FileNotFoundException
	 */
	public MeshFragmentDataReader(File meshFragments, DirectByteBufferPool directBufferPool)
			throws FileNotFoundException {
		this.channel = new FileInputStream(meshFragments).getChannel();
		this.bufferPool = directBufferPool;
		this.rowsPerMt = -1;
	}

	/**
	 * Construct access to a file containing mesh fragments.
	 * @param meshFragments
	 * @param directBufferPool
	 * @throws FileNotFoundException
	 */
	public MeshFragmentDataReader(File meshFragments, DirectByteBufferPool directBufferPool, int rowsPerMt)
			throws FileNotFoundException {
		this.channel = new FileInputStream(meshFragments).getChannel();
		this.bufferPool = directBufferPool;
		this.rowsPerMt = rowsPerMt;
	}

	/**
	 * Read meshdata from the file.
	 * @param fragmentId
	 * @param offset
	 * @param length
	 * @return the actual data read from the file.
	 * @throws IOException
	 */
	public MeshFragmentData read(int fragmentId, long offset, int length) throws IOException {
		PooledByteBuffer pooledByteBuffer = bufferPool.allocate(length);
		// PooledByteBuffer pooledByteBuffer = new PooledByteBuffer(length);
		ByteBuffer rawTileBuffer = pooledByteBuffer.getBuffer();
		// rawTileBuffer.order( ByteOrder.nativeOrder() );

		LOG.debug("Reading mesh fragment with id " + fragmentId + " (offset: " + offset + ", length: " + length + ").");
		long begin = System.currentTimeMillis();
		channel.read(rawTileBuffer, offset);
		long elapsed = System.currentTimeMillis() - begin;
		LOG.debug("Reading took " + elapsed + " milliseconds.");

		rawTileBuffer.rewind();
		int numVertices = rawTileBuffer.getInt();

		// generate contained buffers
		rawTileBuffer.limit(rawTileBuffer.position() + numVertices * 4 * 3);
		ByteBuffer verticesSlice = rawTileBuffer.slice();
		verticesSlice.order(ByteOrder.nativeOrder());

		FloatBuffer vertexBuffer = verticesSlice.asFloatBuffer();
		rawTileBuffer.position(rawTileBuffer.position() + numVertices * 4 * 3);

		Buffer normalsBuffer = null;
		int bytesPerNormalComponent = 1;
		rawTileBuffer.limit(rawTileBuffer.position() + numVertices * bytesPerNormalComponent * 3);
		ByteBuffer normalsSlice = rawTileBuffer.slice();
		normalsSlice.order(ByteOrder.nativeOrder());
		if (bytesPerNormalComponent == 1) {
			normalsBuffer = normalsSlice;
		}
		else if (bytesPerNormalComponent == 2) {
			normalsBuffer = normalsSlice.asShortBuffer();
		}
		else if (bytesPerNormalComponent == 4) {
			normalsBuffer = normalsSlice.asFloatBuffer();
		}

		rawTileBuffer.position(rawTileBuffer.position() + numVertices * bytesPerNormalComponent * 3);

		Buffer indexBuffer = null;
		if (rowsPerMt == -1) {
			rawTileBuffer.limit(length);
			ByteBuffer indexSlice = rawTileBuffer.slice();
			indexSlice.order(ByteOrder.nativeOrder());

			if (numVertices <= 255) {
				indexBuffer = indexSlice;
			}
			else if (numVertices <= 65535) {
				indexBuffer = indexSlice.asShortBuffer();
				indexBuffer.rewind();
			}
			else {
				indexBuffer = indexSlice.asIntBuffer();
			}
		}
		else {
			indexBuffer = getIndexBuffer(rowsPerMt);
		}
		return new MeshFragmentData(pooledByteBuffer, vertexBuffer, normalsBuffer, indexBuffer);
	}

	private ShortBuffer getIndexBuffer(int rowsPerMt) {
		if (indexBuffer == null) {
			indexBuffer = generateMTVertexIds(rowsPerMt);
		}
		return indexBuffer;
	}

	private ShortBuffer generateMTVertexIds(int rowsPerTile) {

		int trianglesPerFragment = (4 * rowsPerTile) + (2 * (rowsPerTile - 1) * rowsPerTile);
		ByteBuffer buffer = bufferPool.allocate(trianglesPerFragment * 3 * Short.SIZE / 8).getBuffer();

		// build triangles
		int lastRowFirstVertexId = 0;
		int lastRowLastVertexId = 0;
		int firstVertexId = 2;

		for (int row = 1; row <= rowsPerTile; row++) {
			int rowLastVertexId = firstVertexId + row * 2;

			// build the two leftmost triangles
			buffer.putShort((short) (firstVertexId - 1));
			buffer.putShort((short) (firstVertexId + 1));
			buffer.putShort((short) (lastRowFirstVertexId));
			buffer.putShort((short) (firstVertexId - 1));
			buffer.putShort((short) (firstVertexId));
			buffer.putShort((short) (firstVertexId + 1));

			for (int i = 0; i < rowLastVertexId - firstVertexId - 2; i++) {

				int lastRowLeft = lastRowFirstVertexId + i;
				int lastRowRight = lastRowLeft + 1;
				int left = firstVertexId + i + 1;
				int right = left + 1;

				buffer.putShort((short) (lastRowLeft));
				buffer.putShort((short) (left));
				buffer.putShort((short) (lastRowRight));
				buffer.putShort((short) (right));
				buffer.putShort((short) (lastRowRight));
				buffer.putShort((short) (left));
			}
			// build the two rightmost triangles
			buffer.putShort((short) (rowLastVertexId + 1));
			buffer.putShort((short) (lastRowLastVertexId));
			buffer.putShort((short) (rowLastVertexId - 1));
			buffer.putShort((short) (rowLastVertexId + 1));
			buffer.putShort((short) (rowLastVertexId - 1));
			buffer.putShort((short) (rowLastVertexId));

			lastRowFirstVertexId = firstVertexId;
			lastRowLastVertexId = rowLastVertexId;
			firstVertexId = rowLastVertexId + 3;
		}
		buffer.rewind();
		return buffer.asShortBuffer();
	}

	/**
	 * @return the pool used to allocate direct buffers.
	 */
	public DirectByteBufferPool getDirectBufferPool() {
		return bufferPool;
	}

}