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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.deegree.services.wpvs.io.serializer.ObjectSerializer;

/**
 * The <code>IndexFile</code> class TODO add class documentation here.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 *
 */
public class IndexFile {

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( IndexFile.class );

    private static final byte POSITION_SIZE = Long.SIZE / 8;

    private Map<String, PositionBlob> cache;

    private Map<String, PositionBlob> addedData = new HashMap<String, PositionBlob>();

    private LinkedList<Long> freePositions = new LinkedList<Long>();

    private int maxIDSize = 0;

    private boolean newFile = false;

    private long nextFilePosition = LONG_SIZE;

    private File file;

    /**
     * @param file
     * @throws IOException
     */
    IndexFile( File file ) throws IOException {
        cache = new HashMap<String, PositionBlob>();
        this.file = file;
        newFile = ( file.length() == 0 ) || !file.exists();
        if ( !newFile ) {
            fillRegistry();
        }

    }

    /**
     * @throws IOException
     *
     */
    private void fillRegistry()
                            throws IOException {
        RandomAccessFile f = new RandomAccessFile( file, "r" );
        FileChannel channel = f.getChannel();
        channel.position( 0 );
        ByteBuffer fileBuffer = ByteBuffer.allocate( (int) channel.size() );
        channel.read( fileBuffer );
        channel.close();

        fileBuffer.rewind();
        nextFilePosition = initialWritePosition( fileBuffer );
        long currentPosition = fileBuffer.position();
        int capacity = fileBuffer.capacity();
        while ( currentPosition < capacity ) {
            String fileID = ObjectSerializer.readString( fileBuffer );
            long dataPosition = fileBuffer.getLong();
            cache.put( fileID, new PositionBlob( fileID, dataPosition, currentPosition ) );
            currentPosition = fileBuffer.position();

        }
        f.close();
    }

    private long initialWritePosition( ByteBuffer fileBuffer ) {
        return fileBuffer.getLong();
    }

    /**
     * @return all positions from the currently available data objects.
     */
    public List<Long> getPositions() {
        List<Long> result = new ArrayList<Long>( cache.size() );
        for ( PositionBlob bp : cache.values() ) {
            result.add( bp.dataPosition );
        }
        for ( PositionBlob bp : addedData.values() ) {
            result.add( bp.dataPosition );
        }
        Collections.sort( result );
        return result;
    }

    /**
     * @param uuid
     * @return the position
     */
    public long getPositionForId( String uuid ) {
        long result = -1;
        if ( !cache.isEmpty() ) {
            PositionBlob pb = cache.get( uuid );
            if ( pb != null ) {
                result = pb.dataPosition;
            }
        }
        return result;
    }

    void close()
                            throws IOException {
        if ( !addedData.isEmpty() ) {
            // FileOutputStream fout = new FileOutputStream( file, false );
            if ( !file.exists() ) {
                LOG.info( "Creating the index file." );
                file.createNewFile();
            }
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile( file, "rw" );
            } catch ( FileNotFoundException e ) {
                throw new IOException( "Could not create index file because: " + e.getLocalizedMessage(), e );
            }

            FileChannel channel = raf.getChannel();
            if ( !channel.isOpen() ) {
                throw new IOException( "Could not open file: " + file + " for writing all information will be lost." );
            }
            // channel.position( channel.size() );
            for ( PositionBlob pb : addedData.values() ) {
                channel.position( pb.filePosition );
                int size = ObjectSerializer.sizeOfString( pb.id );
                ByteBuffer bb = ByteBuffer.allocate( size + POSITION_SIZE );
                // bb.limit( size + POSITION_SIZE );
                ObjectSerializer.writeString( bb, pb.id );
                bb.putLong( pb.dataPosition );
                bb.rewind();
                channel.write( bb );
            }
            ByteBuffer bb = ByteBuffer.allocate( LONG_SIZE );
            bb.putLong( nextFilePosition );
            bb.rewind();
            channel.position( 0 );
            channel.write( bb );
            channel.close();
            raf.close();
        }

        // } else {
        // LOG.error( "Updating the index file is currently not supported. " );
        // }
    }

    boolean addId( String id, long position ) {
        if ( !cache.containsKey( id ) ) {
            PositionBlob pb = new PositionBlob( id, position, getNextWritePosition( id ) );
            addedData.put( id, pb );
            cache.put( id, pb );
        }

        return cache.containsKey( id );
    }

    private long getNextWritePosition( String id ) {
        long result = nextFilePosition;
        if ( freePositions.isEmpty() ) {
            int size = ObjectSerializer.sizeOfString( id );
            maxIDSize = maxIDSize < size ? size : maxIDSize;
            nextFilePosition += ( size + POSITION_SIZE );
        } else {
            result = freePositions.pop();
        }
        return result;
    }

    private class PositionBlob {
        String id;

        long dataPosition;

        long filePosition;

        /**
         * @param id
         * @param dataPosition
         * @param filePosition
         */
        PositionBlob( String id, long dataPosition, long filePosition ) {
            this.id = id;
            this.dataPosition = dataPosition;
            this.filePosition = filePosition;
        }

        @Override
        public boolean equals( Object other ) {
            if ( other != null && other instanceof PositionBlob ) {
                final PositionBlob that = (PositionBlob) other;
                return this.id.equals( that.id ) && this.dataPosition == that.dataPosition;
            }
            return false;
        }

    }
}
