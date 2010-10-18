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

package org.deegree.rendering.r3d.opengl.rendering.model.geometry;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.FloatBuffer;

import org.deegree.commons.utils.memory.AllocatedHeapMemory;

import com.sun.opengl.util.BufferUtil;

/**
 * The <code>DirectGeometryBuffer</code> encapsulates the directbuffers (coordinate, normals and texture) for all
 * buildings defined in a scene.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 *
 */
public class DirectGeometryBuffer {

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( DirectGeometryBuffer.class );

    private FloatBuffer coordBuffer;

    private FloatBuffer normalBuffer;

    private FloatBuffer textureBuffer;

    /**
     * @param coordinateCapacity
     * @param textureCapacity
     */
    public DirectGeometryBuffer( int coordinateCapacity, int textureCapacity ) {
        LOG.info( "Allocating directbuffer for " + 2 * coordinateCapacity + " oordinates (normals and vertices): "
                  + ( 2 * coordinateCapacity * AllocatedHeapMemory.FLOAT_SIZE ) / 1048576 + " MB" );
        LOG.info( "Allocating directbuffer for " + textureCapacity + " texture oordinates: "
                  + ( textureCapacity * AllocatedHeapMemory.FLOAT_SIZE ) / 1048576 + " MB" );
        // LOG.info( "VM has total mb of direct memory: " + VM.maxDirectMemory() / 1048576 );

        coordBuffer = BufferUtil.newFloatBuffer( coordinateCapacity );
        normalBuffer = BufferUtil.newFloatBuffer( coordinateCapacity );
        textureBuffer = BufferUtil.newFloatBuffer( textureCapacity );
    }

    /**
     * @param position
     *            in the coordinates buffer.
     * @param limit
     *            the number of ordinates to copy
     * @return a copy of the direct coordinate floatbuffer, or <code>null</code> if the position /limit is out of
     *         range.
     */
    public FloatBuffer getCoords( int position, int limit ) {
        if ( position < 0 || position > coordBuffer.capacity() ) {
            return null;
        }
        FloatBuffer fb = coordBuffer.asReadOnlyBuffer();
        fb.position( position );
        fb.limit( position + limit );
        return fb.slice();
    }

    /**
     * @param position
     *            in the normal buffer.
     * @param limit
     *            the number of ordinates to copy
     * @return a copy of the direct normal floatbuffer, or <code>null</code> if the position /limit is out of range.
     */
    public FloatBuffer getNormals( int position, int limit ) {
        if ( position < 0 || position > normalBuffer.capacity() ) {
            return null;
        }
        FloatBuffer fb = normalBuffer.asReadOnlyBuffer();
        fb.position( position );
        fb.limit( position + limit );
        return fb.slice();
    }

    /**
     * @param position
     *            in the texture buffer.
     * @param limit
     *            the number of ordinates to copy
     * @return a copy of the direct texture floatbuffer, or <code>null</code> if the position/limit is out of range.
     */
    public FloatBuffer getTextureCoordinates( int position, int limit ) {
        if ( position < 0 || position > textureBuffer.capacity() ) {
            return null;
        }
        FloatBuffer fb = textureBuffer.asReadOnlyBuffer();
        fb.position( position );
        fb.limit( position + limit );
        return fb.slice();
    }

    /**
     * Add the given {@link FloatBuffer} to the direct coordinates buffer, if the direct coordinate buffer does not have
     * the capacity this method will return -1;
     *
     * @param coordBuffer
     *            to add to the coordinates direct buffer.
     * @return the position of the copy in the coordinate buffer or -1 if the given buffer could not be copied in the
     *         direct buffer.
     */
    public synchronized int addCoordinates( FloatBuffer coordBuffer ) {
        return addBuffer( this.coordBuffer, coordBuffer );
    }

    /**
     * Add the given {@link FloatBuffer} to the direct coordinates buffer, if the direct normal buffer does not have the
     * capacity this method will return -1;
     *
     * @param normalBuffer
     *            to add to the normal direct buffer.
     * @return the position of the copy in the coordinate buffer or -1 if the given buffer could not be copied in the
     *         direct buffer.
     */
    public synchronized int addNormals( FloatBuffer normalBuffer ) {
        return addBuffer( this.normalBuffer, normalBuffer );
    }

    /**
     * Add the given {@link FloatBuffer} to the direct texture buffer, if the direct texture buffer does not have the
     * capacity this method will return -1;
     *
     * @param textureBuffer
     *            to add to the texture direct buffer.
     * @return the position of the copy in the texture buffer or -1 if the given buffer could not be copied in the
     *         direct buffer.
     */
    public synchronized int addTexture( FloatBuffer textureBuffer ) {
        return addBuffer( this.textureBuffer, textureBuffer );
    }

    /**
     * Add the values from the given buffer to the directbuffer.
     *
     * @param directBuffer
     * @param newBuffer
     * @return
     */
    private int addBuffer( FloatBuffer directBuffer, FloatBuffer newBuffer ) {
        int oldPosition = directBuffer.position();
        if ( ( directBuffer.capacity() - oldPosition ) >= newBuffer.capacity() ) {
            directBuffer.put( newBuffer );
        } else {
            oldPosition = -1;
        }
        return oldPosition;
    }

    /**
     * Read the floats from the given {@link DataInputStream} to the direct coordinates buffer, if the direct coordinate
     * buffer does not have the capacity this method will return -1,-1;
     *
     * @param in
     *            the stream to add to the coordinates from.
     * @return the position [0] of the copy in the coordinate buffer and the number of ordinates [1] inserted. Both will
     *         have -1 if the given ordinates could not be copied in the direct buffer.
     * @throws IOException
     */
    public synchronized int[] readCoordinates( DataInputStream in )
                            throws IOException {
        return readFromStreamBuffer( this.coordBuffer, in );
    }

    /**
     * Read the floats from the given {@link DataInputStream} to the direct normal buffer, if the direct normal buffer
     * does not have the capacity this method will return -1,-1;
     *
     * @param in
     *            the stream to add to the coordinates from.
     * @return the position [0] of the copy in the normal buffer and the number of ordinates [1] inserted. Both will
     *         have -1 if the given ordinates could not be copied in the direct buffer.
     * @throws IOException
     */
    public synchronized int[] readNormals( DataInputStream in )
                            throws IOException {
        return readFromStreamBuffer( this.normalBuffer, in );
    }

    /**
     * Read the floats from the given {@link DataInputStream} to the direct texture buffer, if the direct texture buffer
     * does not have the capacity this method will return -1,-1;
     *
     * @param in
     *            the stream to add to the coordinates from.
     * @return the position [0] of the copy in the texture buffer and the number of ordinates [1] inserted. Both will
     *         have -1 if the given ordinates could not be copied in the direct buffer.
     * @throws IOException
     */
    public synchronized int[] readTextureOrdinates( DataInputStream in )
                            throws IOException {
        return readFromStreamBuffer( this.textureBuffer, in );
    }

    /**
     * @param coordBuffer2
     * @param in
     * @return
     * @throws IOException
     */
    private int[] readFromStreamBuffer( FloatBuffer directBuffer, DataInputStream in )
                            throws IOException {
        int numberOfValues = in.readInt();
        int position = -1;
        if ( numberOfValues != -1 ) {
            position = directBuffer.position();
            if ( ( directBuffer.capacity() - position ) >= numberOfValues ) {
                for ( int i = 0; i < numberOfValues; ++i ) {
                    directBuffer.put( in.readFloat() );
                }
            }
        }
        return new int[] { position, numberOfValues };
    }

    /**
     * Add the floats read from the given {@link ObjectInputStream} to the direct coordinate {@link FloatBuffer}, if
     * the direct coordinate buffer does not have the capacity this method will return -1. This method assumes that the
     * stream is positioned so, that the next value is an int, declaring the size of the number of floats to be read.
     *
     * @param in
     *            the stream to get the floats from.
     * @return the position of the copy in the texture buffer or -1 if the given buffer could not be copied in the
     *         direct buffer.
     * @throws IOException
     *             if the stream throws this exception while reading.
     */
    public synchronized int readCoordsFromStream( ObjectInputStream in )
                            throws IOException {
        int numberOfValues = in.readInt();
        int position = -1;
        if ( numberOfValues != -1 ) {
            position = coordBuffer.position();
            for ( int i = 0; i < numberOfValues; ++i ) {
                coordBuffer.put( in.readFloat() );
            }

        }
        return position;
    }

    /**
     * Add the floats read from the given {@link ObjectInputStream} to the direct normal {@link FloatBuffer}, if the
     * direct normal buffer does not have the capacity this method will return -1. This method assumes that the stream
     * is positioned so, that the next value is an int, declaring the size of the number of floats to be read.
     *
     * @param in
     *            the stream to get the floats from.
     * @return the position of the copy in the normal buffer or -1 if the given buffer could not be copied in the direct
     *         buffer.
     * @throws IOException
     *             if the stream throws this exception while reading.
     */
    public synchronized int readNormalsFromStream( ObjectInputStream in )
                            throws IOException {
        int numberOfValues = in.readInt();
        int position = -1;
        if ( numberOfValues != -1 ) {
            position = normalBuffer.position();
            for ( int i = 0; i < numberOfValues; ++i ) {
                normalBuffer.put( in.readFloat() );
            }
        }
        return position;
    }

    /**
     * Add the floats read from the given {@link ObjectInputStream} to the direct texture {@link FloatBuffer}, if the
     * direct texture buffer does not have the capacity this method will return -1. This method assumes that the stream
     * is positioned so, that the next value is an int, declaring the size of the number of floats to be read.
     *
     * @param in
     *            the stream to get the floats from.
     * @return the position of the copy in the texture buffer or -1 if the given buffer could not be copied in the
     *         direct buffer.
     * @throws IOException
     *             if the stream throws this exception while reading.
     */
    public synchronized int readTexCoordsFromStream( ObjectInputStream in )
                            throws IOException {
        return addFromStream( textureBuffer, in );
    }

    /**
     * Read from the given stream the values into the given direct buffer.
     *
     * @param directBuffer
     * @param in
     * @return
     * @throws IOException
     */
    private int addFromStream( FloatBuffer directBuffer, ObjectInputStream in )
                            throws IOException {
        int numberOfValues = in.readInt();
        int oldPosition = -1;
        if ( numberOfValues != -1 ) {
            oldPosition = directBuffer.position();
            if ( ( directBuffer.capacity() - oldPosition ) >= numberOfValues ) {
                for ( int i = 0; i < numberOfValues; ++i ) {
                    directBuffer.put( in.readFloat() );
                }
            }
        }
        return oldPosition;
    }

}
