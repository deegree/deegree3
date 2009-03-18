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
package org.deegree.model.coverage.raster.data.nio;

import java.nio.ByteBuffer;

import org.deegree.model.coverage.raster.data.BandType;
import org.deegree.model.coverage.raster.data.DataType;
import org.deegree.model.coverage.raster.data.InterleaveType;
import org.deegree.model.coverage.raster.data.RasterData;
import org.deegree.model.coverage.raster.geom.RasterRect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This abstract class implements the RasterData interface for ByteBuffer based raster.
 * 
 * <p>
 * It is based on java.nio.ByteBuffer and implements common get- and set-operations on the data. The different
 * InterleaveTypes are implemented by additional subclasses.
 * 
 * <p>
 * get- and set-operations are implemented naive and access all data sample-wise. For efficiency subclasses should
 * overwrite methods that access more than one sample and leverage the knowledge of the internal storage format
 * (interleaving).
 * 
 * <p>
 * TODO: Only implements access to byte and float data at the moment. Copy float methods for other data types and change
 * 'Float/float' to short, int, long or double. These types are supported by ByteBuffer and the according methods only
 * differ in the name of the type (eg. getFloat, getInt, getDouble,...). Opposed to the methods for bytes, which lack
 * the type in the method names (eg. only get()).
 * 
 * <p>
 * Also this implementation is able to store a sub-view on another {@link ByteBufferRasterData}, resp. ByteBuffer. With
 * this feature you are able to create subsets without copying the data. Though the current deegree SimpleRaster
 * implementation makes no use of it.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public abstract class ByteBufferRasterData implements RasterData {

    private static Logger LOG = LoggerFactory.getLogger( ByteBufferRasterData.class );

    /**
     * Offset in samples to a sub data raster.
     */
    protected RasterRect view;

    /**
     * the width of the raster
     */
    protected int rasterWidth;

    /**
     * the height of the raster
     */
    protected int rasterHeight;

    /**
     * The number of bands in this raster.
     */
    protected int bands;

    /**
     * The {@link DataType} of this raster.
     */
    protected DataType dataType;

    /**
     * The {@link BandType} of this raster.
     */
    protected BandType[] bandsTypes;

    /**
     * The NODATA value.
     */
    protected byte[] nodata;

    /**
     * The raster data itself.
     */
    protected ByteBuffer data;

    // private int view.x;

    // private int view.y;

    /**
     * Creates a new ByteBufferRasterData instance.
     * 
     * @param env
     *            the raster rectangle defining the sample domain of this raster data.
     * @param rasterWidth
     *            width of the raster data
     * @param rasterHeight
     *            height of the raster data
     * @param dataType
     *            sample data type
     * @param bands
     *            number of bands
     */
    protected ByteBufferRasterData( RasterRect env, int rasterWidth, int rasterHeight, BandType[] bands,
                                    DataType dataType ) {
        this( env, rasterWidth, rasterHeight, bands, dataType, true );
    }

    /**
     * Creates a new ByteBufferRasterData instance.
     * 
     * @param env
     *            the raster rectangle defining the sample domain of this raster data.
     * @param rasterWidth
     *            width of the raster data
     * @param rasterHeight
     *            height of the raster data
     * @param dataType
     *            sample data type
     * @param bands
     *            number of bands
     * @param init
     *            true if the ByteBuffer should be initialized
     */
    protected ByteBufferRasterData( RasterRect env, int rasterWidth, int rasterHeight, BandType[] bands,
                                    DataType dataType, boolean init ) {
        view = env;
        this.rasterWidth = rasterWidth;
        this.rasterHeight = rasterHeight;

        this.bandsTypes = bands;
        this.bands = bandsTypes.length;
        this.dataType = dataType;

        this.nodata = new byte[dataType.getSize() * this.bands];
        if ( init ) {
            initByteBuffer();
        }
    }

    /**
     * Initialize the internal ByteBuffer
     */
    protected void initByteBuffer() {
        this.data = createByteBuffer();
    }

    private ByteBuffer createByteBuffer() {
        return ByteBuffer.allocate( getBufferSize() );
    }

    /**
     * Implementation should create a view of this raster data.
     * 
     * @param env
     * @param bands
     * @return a view or new raster data object, backed by a {@link java.nio.ByteBuffer}
     */
    public abstract ByteBufferRasterData createCompatibleRasterData( RasterRect env, BandType[] bands );

    /**
     * @return ByteBufferRasterData with unset data
     */
    protected abstract ByteBufferRasterData createCompatibleEmptyRasterData();

    public ByteBufferRasterData createCompatibleRasterData( int width, int height ) {
        return createCompatibleRasterData( new RasterRect( view.x, view.y, width, height ), bandsTypes );
    }

    public ByteBufferRasterData createCompatibleRasterData( RasterRect env ) {
        return createCompatibleRasterData( env, bandsTypes );
    }

    public ByteBufferRasterData createCompatibleRasterData( BandType[] bands ) {
        return createCompatibleRasterData( new RasterRect( view.x, view.y, rasterWidth, rasterHeight ), bands );
    }

    public ByteBufferRasterData createCompatibleRasterData() {
        return createCompatibleRasterData( new RasterRect( view.x, view.y, rasterWidth, rasterHeight ), bandsTypes );
    }

    @Override
    public RasterData asReadOnly() {
        ByteBufferRasterData copy = createCompatibleEmptyRasterData();
        copy.data = this.getByteBuffer().asReadOnlyBuffer();
        return copy;
    }

    public final int getBands() {
        return bands;
    }

    public final BandType[] getBandTypes() {
        return bandsTypes;
    }

    public final int getWidth() {
        return view.width;
    }

    public final int getHeight() {
        return view.height;
    }

    public final DataType getDataType() {
        return dataType;
    }

    /**
     * Returns the size of the ByteBuffer in bytes.
     * 
     * @return size of the buffer
     */
    public final int getBufferSize() {
        return rasterWidth * rasterHeight * bands * dataType.getSize();
    }

    public abstract InterleaveType getInterleaveType();

    /**
     * Retruns the byte offset to the next pixel.
     * 
     * @return byte offset to next pixel
     */
    public abstract int getPixelStride();

    /**
     * Returns the byte offset to the next sample of the same pixel.
     * 
     * @return byte offset to sample in the next band (same pixel)
     */
    public abstract int getBandStride();

    /**
     * Returns the byte offset to the next row (same column, same sample)
     * 
     * @return byte offset to next row (same column, same sample)
     */
    public abstract int getLineStride();

    /**
     * @return The internal ByteBuffer.
     */
    public ByteBuffer getByteBuffer() {
        return data;
    }

    /**
     * Checks whether a given rect is inside the raster, throws an exception if outside.
     * 
     * @param x
     *            The x position of the rect.
     * @param y
     *            The y position of the rect.
     * @param width
     *            The width of the rect.
     * @param height
     *            The height of the rect.
     * @throws IndexOutOfBoundsException
     *             if the given rect is outside the raster
     */
    protected final void checkBoundsEx( int x, int y, int width, int height )
                            throws IndexOutOfBoundsException {
        if ( !checkBounds( x, y, width, height ) ) {
            throw new IndexOutOfBoundsException( "request out of bounds" );
        }
    }

    /**
     * Checks whether a given rect is inside the raster.
     * 
     * @param x
     *            The x position of the rect.
     * @param y
     *            The y position of the rect.
     * @param width
     *            The width of the rect.
     * @param height
     *            The height of the rect.
     * @return <code>true</code> if the given rect is inside the raster, else <code>false</code>
     */
    protected final boolean checkBounds( int x, int y, int width, int height ) {
        if ( ( ( view.x + x + width ) > this.rasterWidth ) || ( ( view.y + y + height ) > this.rasterHeight )
             || ( view.x + x < 0 ) || ( view.y + y < 0 ) ) {
            return false;
        }
        return true;
    }

    /**
     * Calculates the position of a pixel in the ByteBuffer.
     * 
     * This method calculates the position of a pixel and returns the offset to this pixel in bytes. Use this method for
     * direct access to ByteBuffers.
     * 
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @return byte offset to the pixel with the specified coordinate
     */
    public final int calculatePos( int x, int y ) {
        return ( ( view.y + y ) * getLineStride() ) + ( ( view.x + x ) * getPixelStride() );
    }

    /**
     * Calculates the position of a sample in the ByteBuffer.
     * 
     * This method calculates the position of a pixel and returns the offset to this pixel in bytes. Use this method for
     * direct access to ByteBuffers.
     * 
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @param band
     *            band index of the sample
     * @return byte offset to the sample with the specified coordinate
     */
    public final int calculatePos( int x, int y, int band ) {
        return ( ( view.y + y ) * getLineStride() ) + ( ( view.x + x ) * getPixelStride() ) + ( band * getBandStride() );
    }

    /**
     * Calculates the position of a pixel in a view (FloatBuffer, etc.) of the ByteBuffer.
     * 
     * This method considers different sample sizes (eg. byte, float) and returns the position in sample stides (not
     * byte strides). Use this method to get proper positions for ByteBuffer views like FloatBuffer, ShortBuffer, etc..
     * 
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @return offset to the pixel with the specified coordinates
     */
    public final int calculateViewPos( int x, int y ) {
        return calculatePos( x, y ) / dataType.getSize();// TODO
    }

    /**
     * Calculates the position of a sample in a view (FloatBuffer, etc.) of the ByteBuffer.
     * 
     * This method considers different sample sizes (eg. byte, float) and returns the position in sample stides (not
     * byte strides). Use this method to get proper positions for ByteBuffer-views like FloatBuffer, ShortBuffer, etc..
     * 
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @param band
     *            band index of the sample
     * @return offset to the sample with the specified coordinates
     */
    public final int calculateViewPos( int x, int y, int band ) {
        return calculatePos( x, y, band ) / dataType.getSize();// TODO
    }

    public byte[] getNullPixel( byte[] result ) {
        if ( result == null ) {
            result = new byte[nodata.length];
        }
        System.arraycopy( nodata, 0, result, 0, result.length );
        return result;
    }

    public void setNullPixel( byte[] values ) {
        if ( values.length % getDataType().getSize() != 0 ) {
            LOG.error( "invalid null pixel values" );
            return;
        }
        if ( values.length == nodata.length ) {
            System.arraycopy( values, 0, nodata, 0, nodata.length );
        } else {
            for ( int b = 0; b < getBands(); b++ ) {
                System.arraycopy( values, 0, nodata, getDataType().getSize() * b, getDataType().getSize() );
            }
        }
    }

    public byte[] getSample( int x, int y, int band, byte[] result ) {
        if ( result == null ) {
            result = new byte[dataType.getSize()];
        }
        int pos = calculatePos( x, y, band );

        data.position( pos );
        data.get( result );
        return result;
    }

    public void setSample( int x, int y, int band, byte[] value ) {
        data.position( calculatePos( x, y, band ) );
        data.put( value );
    }

    public byte getByteSample( int x, int y, int band ) {
        return data.get( calculatePos( x, y, band ) );
    }

    public short getShortSample( int x, int y, int band ) {
        return data.getShort( calculatePos( x, y, band ) );
    }

    public float getFloatSample( int x, int y, int band ) {
        return data.getFloat( calculatePos( x, y, band ) );
    }

    public void setByteSample( int x, int y, int band, byte value ) {
        data.put( calculatePos( x, y, band ), value );
    }

    public void setShortSample( int x, int y, int band, short value ) {
        data.putShort( calculatePos( x, y, band ), value );
    }

    public void setFloatSample( int x, int y, int band, float value ) {
        data.putFloat( calculatePos( x, y, band ), value );
    }

    public void setPixel( int x, int y, byte[] result ) {
        int sampleSize = getDataType().getSize();
        for ( int b = 0; b < getBands(); b++ ) {
            data.position( calculatePos( x, y, b ) );
            data.put( result, b * sampleSize, sampleSize );
        }
    }

    public byte[] getPixel( int x, int y, byte[] result ) {
        int numBands = getBands();
        int sampleSize = getDataType().getSize();
        if ( result == null ) {
            result = new byte[numBands * sampleSize];
        }
        if ( 0 > x || x >= rasterWidth || 0 > y || y >= rasterWidth ) {
            System.arraycopy( nodata, 0, result, 0, result.length );
            return result;
        }
        for ( int b = 0; b < numBands; b++ ) {
            data.position( calculatePos( x, y, b ) );
            data.get( result, b * sampleSize, sampleSize );
        }

        return result;
    }

    public byte[] getBytePixel( int x, int y, byte[] result ) {
        if ( result == null ) {
            result = new byte[getBands()];
        }
        for ( int band = 0; band < getBands(); band++ ) {
            result[band] = getByteSample( x, y, band );
        }
        return result;
    }

    public short[] getShortPixel( int x, int y, short[] result ) {
        if ( result == null ) {
            result = new short[getBands()];
        }
        for ( int band = 0; band < getBands(); band++ ) {
            result[band] = getShortSample( x, y, band );
        }
        return result;
    }

    public float[] getFloatPixel( int x, int y, float[] result ) {
        if ( result == null ) {
            result = new float[getBands()];
        }
        for ( int band = 0; band < getBands(); band++ ) {
            result[band] = getFloatSample( x, y, band );
        }
        return result;
    }

    public void setBytePixel( int x, int y, byte[] pixel ) {
        for ( int band = 0; band < bands; band++ ) {
            data.put( calculatePos( x, y, band ), pixel[band] );
        }
    }

    public void setShortPixel( int x, int y, short[] pixel ) {
        for ( int band = 0; band < bands; band++ ) {
            data.putShort( calculatePos( x, y, band ), pixel[band] );
        }
    }

    public void setFloatPixel( int x, int y, float[] pixel ) {
        for ( int band = 0; band < bands; band++ ) {
            data.putFloat( calculatePos( x, y, band ), pixel[band] );
        }
    }

    public byte[] getBytes( int x, int y, int width, int height, int band, byte[] result ) {
        checkBoundsEx( x, y, width, height );
        if ( result == null ) {
            result = new byte[width * height];
        }
        for ( int i = 0; i < height; i++ ) {
            for ( int j = 0; j < width; j++ ) {
                result[( 2 * i ) + j] = data.get( calculatePos( x + j, y + i, band ) );
            }
        }
        return result;
    }

    public short[] getShorts( int x, int y, int width, int height, int band, short[] result ) {
        checkBoundsEx( x, y, width, height );
        if ( result == null ) {
            result = new short[width * height];
        }
        for ( int i = 0; i < height; i++ ) {
            for ( int j = 0; j < width; j++ ) {
                result[( 2 * i ) + j] = data.getShort( calculatePos( x + j, y + i, band ) );
            }
        }
        return result;
    }

    public float[] getFloats( int x, int y, int width, int height, int band, float[] result ) {
        checkBoundsEx( x, y, width, height );
        if ( result == null ) {
            result = new float[width * height];
        }
        for ( int i = 0; i < height; i++ ) {
            for ( int j = 0; j < width; j++ ) {
                result[( 2 * i ) + j] = data.getFloat( calculatePos( x + j, y + i, band ) );
            }
        }
        return result;
    }

    public void setBytes( int x, int y, int width, int height, int band, byte[] values ) {
        for ( int i = 0; i < height; i++ ) {
            for ( int j = 0; j < width; j++ ) {
                data.put( calculatePos( x + j, y + i, band ), values[( 2 * i ) + j] );
            }
        }
    }

    public void setShorts( int x, int y, int width, int height, int band, short[] values ) {
        for ( int i = 0; i < height; i++ ) {
            for ( int j = 0; j < width; j++ ) {
                data.putShort( calculatePos( x + j, y + i, band ), values[( 2 * i ) + j] );
            }
        }
    }

    public void setFloats( int x, int y, int width, int height, int band, float[] values ) {
        for ( int i = 0; i < height; i++ ) {
            for ( int j = 0; j < width; j++ ) {
                data.putFloat( calculatePos( x + j, y + i, band ), values[( 2 * i ) + j] );
            }
        }
    }

    public ByteBufferRasterData getSubset( int x0, int y0, int width, int height ) {
        ByteBufferRasterData result = createCompatibleRasterData( width, height );
        // result.setSubset( 0, 0, width, height, this, x0, y0 );
        return result;
    }

    public ByteBufferRasterData getSubset( RasterRect sampleDomain ) {
        // return this.getSubset( env.x, env.y, env.width, env.height );
        // return createCompatibleRasterData( sampleDomain );
        ByteBufferRasterData result = createCompatibleRasterData( new RasterRect( view.x + sampleDomain.x,
                                                                                  view.y + sampleDomain.y,
                                                                                  sampleDomain.width,
                                                                                  sampleDomain.height ) );
        result.data = this.data.asReadOnlyBuffer();
        return result;

    }

    public ByteBufferRasterData getSubset( int x0, int y0, int width, int height, int band ) {
        ByteBufferRasterData result = (ByteBufferRasterData) createCompatibleWritableRasterData(
                                                                                                 new RasterRect(
                                                                                                                 view.x,
                                                                                                                 view.y,
                                                                                                                 width,
                                                                                                                 height ),
                                                                                                 new BandType[] { bandsTypes[band] } );
        // result.data = this.data.asReadOnlyBuffer();
        result.setSubset( 0, 0, width, height, 0, this, band, x0, y0 );
        return result;
    }

    public void setSubset( int x0, int y0, int width, int height, RasterData sourceRaster ) {
        setSubset( x0, y0, width, height, sourceRaster, 0, 0 );
    }

    public void setSubset( int x0, int y0, int width, int height, int dstBand, RasterData sourceRaster, int srcBand ) {
        setSubset( x0, y0, width, height, dstBand, sourceRaster, srcBand, 0, 0 );
    }

    public void setSubset( int x0, int y0, int width, int height, RasterData sourceRaster, int xOffset, int yOffset ) {
        // clamp to maximum possible size
        int subWidth = min( this.rasterWidth - x0, width, sourceRaster.getWidth() );
        int subHeight = min( this.rasterHeight - y0, height, sourceRaster.getHeight() );

        byte[] tmp = new byte[getDataType().getSize()];
        for ( int y = 0; y < subHeight; y++ ) {
            for ( int x = 0; x < subWidth; x++ ) {
                for ( int band = 0; band < this.bands; band++ ) {
                    tmp = sourceRaster.getSample( x + xOffset, y + yOffset, band, tmp );
                    setSample( x0 + x, y0 + y, band, tmp );
                }
            }
        }
    }

    public void setSubset( int x0, int y0, int width, int height, int dstBand, RasterData sourceRaster, int srcBand,
                           int xOffset, int yOffset ) {
        // clamp to maximum possible size
        int subWidth = min( this.rasterWidth - x0, width, sourceRaster.getWidth() );
        int subHeight = min( this.rasterHeight - y0, height, sourceRaster.getHeight() );

        byte[] tmp = new byte[getDataType().getSize()];
        for ( int y = 0; y < subHeight; y++ ) {
            for ( int x = 0; x < subWidth; x++ ) {
                tmp = sourceRaster.getSample( x + xOffset, y + yOffset, srcBand, tmp );
                setSample( x0 + x, y0 + y, dstBand, tmp );
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append( "RasterData: type " + dataType + ", " );
        result.append( "size " + rasterWidth + "x" + rasterHeight + "(" + rasterWidth + "x" + rasterHeight + ")" );
        result.append( ", interleaving " + getInterleaveType() );

        return result.toString();
    }

    /**
     * Returns the smallest value of all <code>int</code>s.
     * 
     * @param sizes
     * @return the smalles value
     */
    protected final int min( int... sizes ) {
        int result = Math.min( sizes[0], sizes[1] );
        int i = 2;
        while ( i < sizes.length ) {
            result = Math.min( result, sizes[i] );
            i++;
        }
        return result;
    }
}
