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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deegree.commons.index.PositionableModel;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.memory.AllocatedHeapMemory;
import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;
import org.deegree.services.wpvs.io.DataObjectInfo;
import org.deegree.services.wpvs.io.ModelBackendInfo;
import org.deegree.services.wpvs.io.serializer.ObjectSerializer;

/**
 * The <code>ModelFile</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * @param <P>
 * 
 */
class ModelFile<P extends PositionableModel> {
    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( ModelFile.class );

    private final IndexFile index;

    private final DataFile<P> data;

    private ModelBackendInfo info;

    private final File infoFile;

    private Envelope datasetEnvelope;

    /**
     * @param prototypeIndex
     * @param treeData
     * @param serializer
     * @throws IOException
     */
    ModelFile( IndexFile index, DataFile<P> data, File infoFile ) throws IOException {
        this.data = data;
        this.index = index;
        this.infoFile = infoFile;
        this.info = readBackendInfo( infoFile );
        datasetEnvelope = this.info.getDatasetEnvelope();
    }

    /**
     * @param file
     * @return
     * @throws IOException
     */
    private ModelBackendInfo readBackendInfo( File file )
                            throws IOException {

        if ( !file.exists() || ( file.length() == 0 ) ) {
            return new ModelBackendInfo();
        }

        RandomAccessFile raf = new RandomAccessFile( file, "r" );
        FileChannel channel = raf.getChannel();
        ByteBuffer bb = ByteBuffer.allocate( (int) raf.length() );
        channel.read( bb );
        channel.close();
        raf.close();
        bb.rewind();

        return new ModelBackendInfo( bb.getInt(), bb.getInt(), readEnvelope( bb ) );
    }

    private Envelope readEnvelope( ByteBuffer bb ) {
        Envelope result = null;
        if ( ( bb.position() + ( 6 * ( AllocatedHeapMemory.DOUBLE_SIZE ) ) ) < bb.capacity() ) {
            double[] min = new double[] { bb.getDouble(), bb.getDouble(), bb.getDouble() };
            double[] max = new double[] { bb.getDouble(), bb.getDouble(), bb.getDouble() };
            String crs = ObjectSerializer.readString( bb );
            result = new GeometryFactory().createEnvelope( min, max, new CRS( crs ) );
        }
        return result;
    }

    /**
     * @return the prototypeIndex
     */
    public final IndexFile getIndexFile() {
        return index;
    }

    /**
     * @return the treeData
     */
    public final DataFile<P> getDataFile() {
        return data;
    }

    /**
     * Add the given billboard with the given id.
     * 
     * @param id
     * @param time
     * @param object
     * @return true if the object was inserted truly.
     * @throws IOException
     */
    boolean add( DataObjectInfo<P> object )
                            throws IOException {
        boolean result = true;
        long indexInData = index.getPositionForId( object.getUuid() );
        long dataPosition = 0;
        if ( indexInData == -1 ) {
            // just do an add
            dataPosition = data.add( object );
            if ( dataPosition != -1 ) {
                if ( object.getData() instanceof WorldRenderableObject ) {
                    info.addOrdinates( object );
                }
                index.addId( object.getUuid(), dataPosition );
                Envelope objEnv = object.getEnvelope();
                if ( objEnv != null ) {
                    if ( this.datasetEnvelope == null ) {
                        this.datasetEnvelope = objEnv;
                    } else {
                        this.datasetEnvelope = this.datasetEnvelope.merge( objEnv );
                    }
                }
            } else {
                result = false;
                LOG.error( "Could not add the given object to the file." );
            }
        } else {
            LOG.error( "Updating is not supported for file backend, not adding object with id: " + object.getUuid() );
            result = false;
        }
        return result;
    }

    /**
     * Returns true if the id is available in the filebackend.
     * 
     * @param id
     *            to check
     * @return true if the id is already present in the filebackend.
     */
    boolean contains( String id ) {
        return index.getPositionForId( id ) != -1;
    }

    /**
     * Writes added data to the files and closes all files.
     * 
     * @throws IOException
     */
    void close()
                            throws IOException {
        index.close();
        data.close();
        writeBackendInfo();
    }

    /**
     * @throws IOException
     * 
     */
    private void writeBackendInfo()
                            throws IOException {
        if ( info.getOrdinateCount() > 0 || info.getTextureOrdinateCount() > 0 ) {
            if ( !infoFile.exists() ) {
                LOG.info( "Creating the info file." );
                infoFile.createNewFile();
            }
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile( infoFile, "rw" );
            } catch ( FileNotFoundException e ) {
                throw new IOException( "Could not create infofile because: " + e.getLocalizedMessage(), e );
            }
            FileChannel channel = raf.getChannel();
            int bytes = 2 * AllocatedHeapMemory.INT_SIZE;
            if ( this.datasetEnvelope != null ) {
                // min and max in 3d space
                bytes += AllocatedHeapMemory.DOUBLE_SIZE * 3 * 2;
                if ( this.datasetEnvelope.getCoordinateSystem() != null ) {
                    bytes += ObjectSerializer.sizeOfString( this.datasetEnvelope.getCoordinateSystem().getName() );
                }
            }
            ByteBuffer bb = ByteBuffer.allocate( bytes );
            bb.putInt( info.getOrdinateCount() );
            bb.putInt( info.getTextureOrdinateCount() );
            // write the envelope
            if ( this.datasetEnvelope != null ) {
                double[] min = this.datasetEnvelope.getMin().getAsArray();
                double[] max = this.datasetEnvelope.getMax().getAsArray();
                for ( double d : min ) {
                    bb.putDouble( d );
                }
                for ( double d : max ) {
                    bb.putDouble( d );
                }
                if ( this.datasetEnvelope.getCoordinateSystem() != null ) {
                    ObjectSerializer.writeString( bb, this.datasetEnvelope.getCoordinateSystem().getName() );
                }
            }

            bb.rewind();
            channel.write( bb, 0 );
            channel.close();
            raf.close();
        }
    }

    List<DataObjectInfo<P>> readAllFromFile( CRS baseCRS )
                            throws IOException {
        List<DataObjectInfo<P>> result = new ArrayList<DataObjectInfo<P>>();

        List<Long> positions = index.getPositions();

        long size = data.getSize();

        List<Pair<Long, Long>> createBatches = createBatches( positions, size );

        int i = 0;

        boolean updateOldInfoFile = this.datasetEnvelope == null;
        Envelope dsEnv = null;
        for ( Pair<Long, Long> pair : createBatches ) {
            LOG.info( "Deserialized " + ( ++i ) + " of " + createBatches.size() + " from file: " + data.getFileName() );
            Pair<Envelope, List<DataObjectInfo<P>>> fromFile = data.readAllFromFile( pair.first, pair.second, dsEnv,
                                                                                     baseCRS );
            if ( fromFile != null ) {
                result.addAll( fromFile.second );
                dsEnv = fromFile.first;
            } else {
                LOG.warn( "Could not retrieve data from positions: " + pair.first + " till: " + pair.second
                          + " from file: " + data.getFileName() );
            }
        }
        if ( updateOldInfoFile && dsEnv != null ) {
            double[] min = dsEnv.getMin().getAsArray();
            double[] max = dsEnv.getMax().getAsArray();
            double[] tMin = Arrays.copyOf( min, min.length );
            double[] tMax = Arrays.copyOf( max, max.length );
            // tMin[0] += ( -toLocal[0] );
            // tMin[1] += ( -toLocal[1] );
            // tMax[0] += ( -toLocal[0] );
            // tMax[1] += ( -toLocal[1] );
            this.datasetEnvelope = new GeometryFactory().createEnvelope( tMin, tMax, baseCRS );

            this.info.setDatasetEnvelope( this.datasetEnvelope );
            this.writeBackendInfo();
        }
        return result;
    }

    private List<Pair<Long, Long>> createBatches( List<Long> positions, long dataSize ) {
        long step = 25 * 1048576; // mb
        long begin = 0;
        long end = 0;
        long nextStep = step;
        List<Pair<Long, Long>> result = new java.util.LinkedList<Pair<Long, Long>>();
        for ( Long position : positions ) {
            end = position;
            if ( end > nextStep ) {
                result.add( new Pair<Long, Long>( begin, end ) );
                begin = end;
                nextStep += step;
            }
            if ( end > dataSize ) {
                LOG.warn( "The data position: " + end + " is larger than the size of the datafile: " + dataSize
                          + " this is strange." );
            }
        }
        result.add( new Pair<Long, Long>( begin, dataSize ) );
        return result;

    }

    /**
     * @param uuid
     * @return
     * @throws IOException
     */

    Object getObject( String uuid )
                            throws IOException {
        long position = index.getPositionForId( uuid );
        if ( position != -1 ) {
            data.get( position );
        }
        return null;
    }

    /**
     * @return the info file
     */
    public ModelBackendInfo getBackendInfo() {
        return info;
    }

}
