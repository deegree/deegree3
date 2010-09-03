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

package org.deegree.commons.utils.nio;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.deegree.commons.utils.memory.AllocatedHeapMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.opengl.util.BufferUtil;

/**
 * The <code>BufferSerializer</code> class TODO add class documentation here.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 *
 */
public class BufferSerializer {
    private final static Logger LOG = LoggerFactory.getLogger( BufferSerializer.class );

    /**
     * Writes a buffer to the given output stream by checking the instance of the buffer and writing the native values
     * appropriately. Because of the ObjectOutputStream is not seekable, the serialized buffer will be corrupted if an
     * {@link IOException} occurs. The 'size' integer will in this case not be set to -1 and will not contain the
     * correct number of 'native-types' to read.
     *
     * @param buffer
     * @param out
     * @throws IOException
     */
    public static void writeBufferToStream( Buffer buffer, DataOutputStream out )
                            throws IOException {
        if ( out == null ) {
            LOG.info( "Given ObjectOutputStream is null, this is not allowed." );
            return;
        }

        if ( buffer == null || buffer.capacity() == 0 ) {
            out.writeInt( -1 );
            return;
        }
        buffer.rewind();
        out.writeInt( buffer.capacity() );
        try {
            if ( buffer instanceof ByteBuffer ) {
                writeBuffer( (ByteBuffer) buffer, out );
            } else if ( buffer instanceof FloatBuffer ) {
                writeBuffer( (FloatBuffer) buffer, out );
            } else if ( buffer instanceof DoubleBuffer ) {
                writeBuffer( (DoubleBuffer) buffer, out );
            } else if ( buffer instanceof IntBuffer ) {
                writeBuffer( (IntBuffer) buffer, out );
            } else if ( buffer instanceof LongBuffer ) {
                writeBuffer( (LongBuffer) buffer, out );
            } else if ( buffer instanceof CharBuffer ) {
                writeBuffer( (CharBuffer) buffer, out );
            } else {
                LOG.warn( "Not able to serialize Buffers of given type: " + buffer.getClass().getCanonicalName() );
            }
        } catch ( IOException ie ) {
            LOG.error( "An exception occurred while writing the buffer, the output will be corrupted and should be re-serialized." );
            throw ie;
        }
    }

    /**
     * @param byteBuffer
     * @param out
     * @throws IOException
     */
    private static void writeBuffer( DoubleBuffer buffer, DataOutputStream out )
                            throws IOException {
        for ( int i = 0; i < buffer.capacity(); ++i ) {
            out.writeDouble( buffer.get( i ) );
        }
    }

    /**
     * @param buffer
     * @param out
     * @throws IOException
     */
    private static void writeBuffer( IntBuffer buffer, DataOutputStream out )
                            throws IOException {
        for ( int i = 0; i < buffer.capacity(); ++i ) {
            out.writeInt( buffer.get( i ) );
        }
    }

    /**
     * @param buffer
     * @param out
     * @throws IOException
     */
    private static void writeBuffer( LongBuffer buffer, DataOutputStream out )
                            throws IOException {
        for ( int i = 0; i < buffer.capacity(); ++i ) {
            out.writeLong( buffer.get( i ) );
        }
    }

    /**
     * @param buffer
     * @param out
     * @throws IOException
     */
    private static void writeBuffer( CharBuffer buffer, DataOutputStream out )
                            throws IOException {
        for ( int i = 0; i < buffer.capacity(); ++i ) {
            out.writeChar( buffer.get( i ) );
        }
    }

    /**
     * @param buffer
     * @param out
     * @throws IOException
     */
    private static void writeBuffer( FloatBuffer buffer, DataOutputStream out )
                            throws IOException {
        for ( int i = 0; i < buffer.capacity(); ++i ) {
            out.writeFloat( buffer.get( i ) );
        }
    }

    /**
     * @param buffer
     * @param out
     * @throws IOException
     */
    private static void writeBuffer( ByteBuffer buffer, DataOutputStream out )
                            throws IOException {
        for ( int i = 0; i < buffer.capacity(); ++i ) {
            out.writeByte( buffer.get( i ) );
        }

    }

    /**
     * Reads a number of floats from the stream and fills a direct, native FloatBuffer with them.
     * <p>
     * The callee has to ensure that this method is called in it's right time, so that the next read to the stream gives
     * an int and the read(s) thereafter will result in the number of values denoted by the first int value. Using this
     * class will result in a correct order. The following code will result in correct reading and writing of the
     * buffers:<code>
     *
     * FloatBuffer myFloatBuffer;
     *
     * private void writeObject( ObjectOutputStream out ){
     *    BufferSerializer.writeBufferToStream( out, myFloatBuffer );
     * }
     *
     * private void readObject( ObjectInputStream in ){
     *    myFloatBuffer = BufferSerializer.readFloatBufferFromStream( in );
     * }
     * </code>
     * </p>
     *
     * @param in
     * @param direct
     * @return the floatBuffer of <code>null</code> if no indication of the number of floats to read has been made.
     * @throws IOException
     */
    public static FloatBuffer readFloatBufferFromStream( DataInputStream in, boolean direct )
                            throws IOException {
        int numberOfValues = in.readInt();
        FloatBuffer result = null;
        if ( numberOfValues != -1 ) {
            if ( direct ) {
                ByteBuffer bb = ByteBuffer.allocateDirect( numberOfValues * AllocatedHeapMemory.FLOAT_SIZE );
                bb.order( ByteOrder.nativeOrder() );
                // result = BufferUtil.newFloatBuffer( numberOfValues );
                result = bb.asFloatBuffer();
            } else {
                result = FloatBuffer.allocate( numberOfValues );
            }
            result.clear();
            for ( int i = 0; i < numberOfValues; ++i ) {
                try {
                    result.put( in.readFloat() );
                } catch ( IOException e ) {
                    result.clear();
                    throw e;
                }
            }
            result.rewind();
        }

        return result;
    }

    /**
     * Reads a number of bytes from the stream and fills a direct, native ByteBuffer with them.
     * <p>
     * The callee has to ensure that this method is called in it's right time, so that the next read to the stream gives
     * an int and the read(s) thereafter will result in the number of values denoted by the first int value. Using this
     * class will result in a correct order. The following code will result in correct reading and writing of the
     * buffers:<code>
     *
     * ByteBuffer myByteBuffer;
     *
     * private void writeObject( ObjectOutputStream out ){
     *    BufferSerializer.writeBufferToStream( out, myByteBuffer );
     * }
     *
     * private void readObject( ObjectInputStream in ){
     *    myByteBuffer = BufferSerializer.readByteBufferFromStream( in );
     * }
     * </code>
     * </p>
     *
     * @param in
     * @param direct
     * @return the floatBuffer of <code>null</code> if no indication of the number of floats to read has been made.
     * @throws IOException
     */
    public static ByteBuffer readByteBufferFromStream( DataInputStream in, boolean direct )
                            throws IOException {
        int numberOfValues = in.readInt();
        ByteBuffer result = null;
        if ( numberOfValues != -1 ) {
            if ( direct ) {
                result = BufferUtil.newByteBuffer( numberOfValues );
            } else {
                result = ByteBuffer.allocate( numberOfValues );
            }
            result.clear();
            for ( int i = 0; i < numberOfValues; ++i ) {
                try {
                    result.put( in.readByte() );
                } catch ( IOException e ) {
                    result.clear();
                    throw e;
                }
            }
            result.rewind();
        }
        return result;
    }
}
