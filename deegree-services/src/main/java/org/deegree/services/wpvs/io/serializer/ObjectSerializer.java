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

package org.deegree.services.wpvs.io.serializer;

import static org.deegree.commons.utils.memory.AllocatedHeapMemory.INT_SIZE;
import static org.deegree.commons.utils.memory.AllocatedHeapMemory.LONG_SIZE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.deegree.commons.index.PositionableModel;
import org.deegree.commons.utils.memory.AllocatedHeapMemory;
import org.deegree.geometry.GeometryFactory;
import org.deegree.services.wpvs.io.DataObjectInfo;

/**
 * The <code>ObjectSerializer</code> class TODO add class documentation here.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * @param <T>
 *
 */
public abstract class ObjectSerializer<T extends PositionableModel> {

    static GeometryFactory geomFac = new GeometryFactory();

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( ObjectSerializer.class );

    /**
     * Write the given {@link PositionableModel} to the buffer.
     *
     * @param buffer
     * @param object
     */
    public abstract void write( ByteBuffer buffer, DataObjectInfo<T> object );

    /**
     * Create a {@link PositionableModel} by reading it from the buffer.
     *
     * @param buffer
     * @return the instantiated {@link PositionableModel}
     */
    public abstract T read( ByteBuffer buffer );

    /**
     * Returns the size of the object after serialization, without the header information, just the fields.
     *
     * @param object
     *            to get the size from
     *
     * @return the size of the object after serialization, without the header information.
     */
    public abstract int serializedObjectSize( DataObjectInfo<T> object );

    /**
     * Writes the default header, consisting of the totalSize, id and the currentTimeMillis().
     *
     * @param buffer
     * @param object
     *            to write the header from.
     */
    public void writeHeader( ByteBuffer buffer, DataObjectInfo<T> object ) {
        int headerSize = getHeaderSize( object.getUuid() );
        // size of the header.
        buffer.putInt( headerSize );
        // size of the object
        buffer.putInt( serializedObjectSize( object ) );
        // the time
        buffer.putLong( object.getTime() );
        // the uuid
        writeString( buffer, object.getUuid() );
    }

    /**
     * Skips the header.
     *
     * @param buffer
     */
    public void skipHeader( ByteBuffer buffer ) {
        int startPosition = buffer.position();
        int headerSize = buffer.getInt();
        buffer.position( startPosition + headerSize );
    }

    /**
     * Allocates a byte buffer to hold the total object.
     *
     * @param channel
     * @return an allocated bytebuffer large enough to deserialize the next object.
     * @throws IOException
     */
    public ByteBuffer allocateByteBuffer( FileChannel channel )
                            throws IOException {
        ByteBuffer header = ByteBuffer.allocate( INT_SIZE );
        long position = channel.position();
        channel.read( header );
        header.rewind();
        // the header size
        int length = header.getInt();
        // read the total size of the serialized object.
        length += header.getInt();
        channel.position( position );
        return ByteBuffer.allocate( length );
    }

    /**
     * Returns the size of the object after serialization, with the header information and the fields.
     *
     * @param object
     *            to get the size from
     *
     * @return the size of the object after serialization, with the header information.
     */
    public int sizeOfSerializedObject( DataObjectInfo<T> object ) {
        return serializedObjectSize( object ) + getHeaderSize( object.getUuid() );
    }

    /**
     * Read the id of this object and reset the position to it's start position.
     *
     * @param buffer
     *            to read from
     * @return the id of the object or 0 if the id could not be read.
     */
    public String readID( ByteBuffer buffer ) {
        int startPos = buffer.position();
        // headersize, objectsize, time
        buffer.position( startPos + ( 2 * INT_SIZE ) + LONG_SIZE );
        // id
        String result = readString( buffer );
        buffer.position( startPos );
        return result;

    }

    /**
     * Read the creation time of this object and reset the position to it's start position.
     *
     * @param buffer
     *            to read from
     * @return the creation time of the object or 0 if the time could not be read.
     */
    public long readTime( ByteBuffer buffer ) {
        int startPos = buffer.position();
        // headersize, objectsize
        buffer.position( startPos + ( AllocatedHeapMemory.INT_SIZE * 2 ) );
        // time
        long result = buffer.getLong();
        buffer.position( startPos );
        return result;
    }

    /**
     * @param uuid
     * @return the size of the header starting from the position of the object.
     */
    public int getHeaderSize( String uuid ) {
        // headersize, length, id, time.
        return AllocatedHeapMemory.INT_SIZE + getHeaderFieldsSize( uuid );
    }

    /**
     * @return the size of the header without the length
     */
    private int getHeaderFieldsSize( String uuid ) {
        // sizeofobject, time, id
        return AllocatedHeapMemory.INT_SIZE + LONG_SIZE + sizeOfString( uuid );
    }

    /**
     * Writes a string to the buffer, by using UTF-8.
     *
     * @param buffer
     * @param string
     */
    public static void writeString( ByteBuffer buffer, String string ) {
        byte[] chars;
        try {
            chars = string.getBytes( "UTF-8" );
        } catch ( UnsupportedEncodingException e ) {
            chars = string.getBytes();
        }
        buffer.putInt( chars.length );
        buffer.put( chars );
    }

    /**
     * Reads a string from the buffer, using UTF-8.
     *
     * @param buffer
     * @return string
     */
    public static String readString( ByteBuffer buffer ) {
        int stringSize = buffer.getInt();
        byte[] idBuffer = new byte[stringSize];
        buffer.get( idBuffer );
        String result;
        try {
            result = new String( idBuffer, "UTF-8" );
        } catch ( UnsupportedEncodingException e ) {
            result = new String( idBuffer );
        }
        return result;
    }

    /**
     * Return the size of the given string in UTF-8 plus the length index
     *
     * @param string
     * @return the size of the given string in UTF-8, or in the default platform encoding.
     */
    public static int sizeOfString( String string ) {
        byte[] chars;
        try {
            chars = string.getBytes( "UTF-8" );
        } catch ( UnsupportedEncodingException e ) {
            chars = string.getBytes();
        }
        return AllocatedHeapMemory.INT_SIZE + chars.length;
    }

    /**
     * Read the size of the serialized object and reset the position to it's start position.
     *
     * @param buffer
     *            to read from
     * @return the size of the object.
     */
    public int readObjectSize( ByteBuffer buffer ) {
        int startPos = buffer.position();
        // headersize
        buffer.position( startPos + INT_SIZE );
        // objectSize
        int result = buffer.getInt();
        buffer.position( startPos );
        return result;
    }

    /**
     * Serializes an object using the standard serialization mechanism, {@link ObjectOutputStream}
     *
     * @param doi
     *            to be serialized with the {@link ObjectOutputStream}
     * @return the byte array containing the serialized object.
     */
    public byte[] serializeObject( DataObjectInfo<T> doi ) {
        T dm = doi.getData();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream output = new ObjectOutputStream( baos );
            output.writeObject( dm );
            output.close();
            baos.close();
        } catch ( IOException e ) {
            LOG.error( "Error while serializing object because: " + e.getLocalizedMessage(), e );
        }
        return baos.toByteArray();
    }

    /**
     * Deserialize an object from the given byte array.
     *
     * @param buffer
     *            containing bytes to deserialize.
     * @return the deserialized object of type T.
     */
    @SuppressWarnings("unchecked")
    public T deserializeDataObject( byte[] buffer ) {
        T result = null;
        if ( buffer != null ) {
            try {
                ObjectInputStream objectIn = new ObjectInputStream( new ByteArrayInputStream( buffer ) );
                result = (T) objectIn.readObject();
            } catch ( ClassCastException e ) {
                LOG.error( "Error while deserializing object because: " + e.getLocalizedMessage(), e );
            } catch ( IOException e ) {
                LOG.error( "Error while deserializing object because: " + e.getLocalizedMessage(), e );
            } catch ( ClassNotFoundException e ) {
                LOG.error( "Error while deserializing object because: " + e.getLocalizedMessage(), e );
            } catch ( Throwable e ) {
                LOG.error( "Error while deserializing object because: " + e.getLocalizedMessage(), e );
            }
        }
        return result;

    }

}
