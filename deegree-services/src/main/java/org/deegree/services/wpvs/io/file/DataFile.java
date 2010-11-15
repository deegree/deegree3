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

package org.deegree.services.wpvs.io.file;

import static org.deegree.commons.utils.memory.AllocatedHeapMemory.LONG_SIZE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.deegree.commons.index.PositionableModel;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.memory.AllocatedHeapMemory;
import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.services.wpvs.io.DataObjectInfo;
import org.deegree.services.wpvs.io.serializer.ObjectSerializer;

/**
 * The <code>DataFile</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * @param <T>
 *            to add to the file
 * 
 */
public class DataFile<T extends PositionableModel> {

    private final static GeometryFactory geomFac = new GeometryFactory();

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( DataFile.class );

    // private FileChannel channel;

    private HashMap<Long, DataObjectInfo<T>> cache;

    private HashMap<Long, DataObjectInfo<T>> addedData;

    private long writePos = LONG_SIZE;

    private final ObjectSerializer<T> serializer;

    private String fileName;

    private File dataFile;

    private boolean isNewFile;

    /**
     * @param dataFile
     * @param serializer
     *            to use for serialization.
     * @throws IOException
     */
    public DataFile( File dataFile, ObjectSerializer<T> serializer ) throws IOException {
        this.fileName = dataFile.getAbsolutePath();
        this.serializer = serializer;
        this.dataFile = dataFile;
        isNewFile = ( dataFile.length() == 0 ) || !dataFile.exists();
        if ( !isNewFile ) {
            writePos = initialWritePosition();
        }
        cache = new HashMap<Long, DataObjectInfo<T>>();
        addedData = new HashMap<Long, DataObjectInfo<T>>();

    }

    /**
     * @param position
     * @return the object from the file.
     * @throws IOException
     */
    public DataObjectInfo<T> get( long position )
                            throws IOException {
        DataObjectInfo<T> result = cache.get( position );
        if ( result == null && !isNewFile ) {
            RandomAccessFile f = new RandomAccessFile( dataFile, "r" );
            FileChannel channel = f.getChannel();

            channel.position( position );
            ByteBuffer buffer = serializer.allocateByteBuffer( channel );
            FileLock lock = channel.lock( position, position + buffer.capacity(), true );
            channel.read( buffer );
            lock.release();
            channel.close();
            f.close();

            buffer.rewind();
            String id = serializer.readID( buffer );
            long time = serializer.readTime( buffer );
            T object = serializer.read( buffer );
            result = new DataObjectInfo<T>( id, null, null, null, null, object, time );
            cache.put( position, result );
        }
        return result;
    }

    private long initialWritePosition()
                            throws IOException {
        RandomAccessFile f = new RandomAccessFile( dataFile, "r" );
        FileChannel channel = f.getChannel();
        channel.position( 0 );
        ByteBuffer buf = ByteBuffer.allocate( AllocatedHeapMemory.LONG_SIZE );
        channel.read( buf );
        buf.rewind();
        long result = buf.getLong();
        channel.close();
        f.close();
        return result;
    }

    /**
     * @param to
     * @param from
     * @return all objects from the file, puts nothing in the cache.
     * @throws IOException
     */
    Pair<Envelope, List<DataObjectInfo<T>>> readAllFromFile( long from, long to, Envelope datasetEnvelope, CRS baseCRS )
                            throws IOException {
        List<DataObjectInfo<T>> result = new ArrayList<DataObjectInfo<T>>();
        result.addAll( cache.values() );
        if ( !isNewFile ) {
            RandomAccessFile f = new RandomAccessFile( dataFile, "r" );
            FileChannel channel = f.getChannel();

            FileLock lock = channel.lock( from, to, true );
            ByteBuffer objectBuffer = ByteBuffer.allocate( (int) ( to - from ) );
            channel.position( from );
            channel.read( objectBuffer );
            lock.release();
            channel.close();
            f.close();
            objectBuffer.rewind();
            if ( from == 0 ) {
                objectBuffer.getLong();
            }

            // get the length of the written bytes of the object.
            long position = objectBuffer.position();
            int capacity = objectBuffer.capacity();
            while ( position < capacity ) {
                String id = serializer.readID( objectBuffer );
                long time = serializer.readTime( objectBuffer );
                T t = serializer.read( objectBuffer );
                if ( t != null ) {
                    datasetEnvelope = mergeEnvelopes( datasetEnvelope, t.getModelBBox(), baseCRS );
                    result.add( new DataObjectInfo<T>( id, null, null, null, null, t, time ) );
                } else {
                    LOG.error( "Skipping location, the given position does not contain data." );
                }
                position = objectBuffer.position();
            }
        }
        return new Pair<Envelope, List<DataObjectInfo<T>>>( datasetEnvelope, result );
    }

    /**
     * @param modelBBox
     * @return
     */
    private Envelope mergeEnvelopes( Envelope datasetEnv, float[] modelBBox, CRS baseCRS ) {
        if ( modelBBox == null ) {
            return datasetEnv;
        }
        int size = modelBBox.length;
        int dim = size / 2;
        int index = 0;
        double[] min = new double[3];
        double[] max = new double[3];
        min[0] = modelBBox[index++];
        min[1] = modelBBox[index++];
        min[2] = dim == 3 ? modelBBox[index++] : 0;
        max[0] = modelBBox[index++];
        max[1] = modelBBox[index++];
        max[2] = dim == 3 ? modelBBox[index++] : 0;
        Envelope env = geomFac.createEnvelope( min, max, baseCRS );
        if ( datasetEnv == null ) {
            datasetEnv = geomFac.createEnvelope( min, max, baseCRS );
        }
        return datasetEnv.merge( env );
    }

    /**
     * @param object
     * @return the position of the billboard in the data file.
     * @throws IOException
     */
    public long add( DataObjectInfo<T> object )
                            throws IOException {
        long wPos = getNextOpenWritePosition( serializer.sizeOfSerializedObject( object ) );
        if ( wPos == -1 ) {
            throw new IOException( "Could not get a position to write to. " );
        }
        cache.put( wPos, object );
        addedData.put( wPos, object );
        return wPos;
    }

    /**
     * Closes the file and writes all data to it.
     * 
     * @throws IOException
     */
    public void close()
                            throws IOException {
        if ( !addedData.isEmpty() ) {
            if ( !dataFile.exists() ) {
                LOG.info( "Creating the data file." );
                dataFile.createNewFile();
            }
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile( dataFile, "rw" );
            } catch ( FileNotFoundException e ) {
                throw new IOException( "Could not create index file because: " + e.getLocalizedMessage(), e );
            }

            FileChannel channel = raf.getChannel();
            if ( !channel.isOpen() ) {
                throw new IOException( "Could not open file: " + dataFile
                                       + " for writing all information will be lost." );
            }
            FileLock lock = channel.lock();
            SortedSet<Long> keys = new TreeSet<Long>( addedData.keySet() );
            for ( Long position : keys ) {
                DataObjectInfo<T> object = addedData.get( position );
                channel.position( position );
                ByteBuffer buffer = ByteBuffer.allocate( serializer.sizeOfSerializedObject( object ) );
                serializer.write( buffer, object );
                buffer.rewind();
                channel.write( buffer );
            }
            channel.position( 0 );
            ByteBuffer buffer = ByteBuffer.allocate( LONG_SIZE );
            buffer.putLong( writePos );
            buffer.rewind();
            channel.write( buffer );
            lock.release();
            channel.close();
            raf.close();
        }
    }

    /**
     * @param length
     * @return
     * @throws IOException
     */
    private long getNextOpenWritePosition( int length ) {
        long result = writePos;
        writePos += length;
        return result;
    }

    /**
     * @return the next write position.
     */
    public long getSize() {
        return writePos;
    }

    /**
     * @return the path of this datafile
     */
    public String getFileName() {
        return fileName;
    }

}
