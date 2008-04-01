//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
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
package org.deegree.model.coverage.raster.data;

import java.nio.ByteBuffer;

/**
 * This abstract class implements the RasterData interface for ByteBuffer based raster.
 * 
 * <p>It is based on java.nio.ByteBuffer and implements common get- and set-operations on the data. The different
 * InterleaveTypes are implemented by additional subclasses.
 * 
 * <p>get- and set-operations are implemented naive and access all data sample-wise. For efficiency subclasses should
 * overwrite methods that access more than one sample and leverage the knowledge of the internal storage format
 * (interleaving).
 * 
 * <p>TODO: Only implements access to byte and float data at the moment. Copy float methods for other data types and change
 * 'Float/float' to short, int, long or double. These types are supported by ByteBuffer and the according methods only
 * differ in the name of the type (eg. getFloat, getInt, getDouble,...). Opposed to the methods for bytes, which lack
 * the type in the method names (eg. only get()).
 * 
 * <p>Also this implementation is able to store a sub-view on another {@link ByteBufferRasterData}, resp. ByteBuffer. With
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

    /**
     * the width of the raster
     */
    protected int width;

    /**
     * the height of the raster
     */
    protected int height;

    /**
     * The width of the subset of the raster.
     * 
     * <p>
     * A ByteBufferRasterData can refer to a subset of another ByteBuffer so lazy evaluation/copying is possible. Though
     * it is not effective at the moment, getSubset always returns a copy.
     * 
     */
    protected int subWidth;

    /**
     * The height of the subset of the raster.
     * 
     * <p>
     * A ByteBufferRasterData can refer to a subset of another ByteBuffer so lazy evaluation/copying is possible. Though
     * it is not effective at the moment, getSubset always returns a copy.
     * 
     */
    protected int subHeight;

    /**
     * The x offset of the subset of the raster.
     */
    protected int x0 = 0;

    /**
     * The y offset of the subset of the raster.
     */
    protected int y0 = 0;

    /**
     * The index of the subband.
     */
    protected int subBand = -1;

    /**
     * true if this raster is a single banded subview of the raster.
     */
    protected boolean singleBand = false;

    /**
     * true if the raster is a subset of the original raster data.
     */
    protected boolean subset = false;

    /**
     * The number of bands in this raster.
     */
    protected int bands;

    /**
     * The {@link DataType} of this raster.
     */
    protected DataType dataType;

    /**
     * The NODATA value.
     */
    protected byte[] nodata;

    /**
     * The raster data itself.
     */
    ByteBuffer data;

    /**
     * Creates a new single-band ByteBufferRasterData instance.
     * 
     * @param dataType
     *            sample data type
     * @param width
     *            size of the raster
     * @param height
     *            size of the raster
     */
    protected ByteBufferRasterData( int width, int height, DataType dataType ) {
        this( width, height, 1, dataType );
    }

    /**
     * Creates a new ByteBufferRasterData instance.
     * 
     * @param dataType
     *            sample data type
     * @param width
     *            size of the raster
     * @param height
     *            size of the raster
     * @param bands
     *            number of bands
     */
    protected ByteBufferRasterData( int width, int height, int bands, DataType dataType ) {
        this.width = width;
        this.height = height;
        this.subWidth = width;
        this.subHeight = height;

        this.bands = bands;
        this.dataType = dataType;

        this.nodata = new byte[dataType.getSize()];
        this.data = createByteBuffer();
    }

    /**
     * Create a new {@link ByteBufferRasterData} that represents a subset of a larger {@link ByteBufferRasterData}.
     * 
     * @param x0
     *            The x offset of the subset.
     * @param y0
     *            The y offset of the subset.
     * @param subWidth
     *            The width of the subset.
     * @param subHeight
     *            The height of the subset.
     * @param width
     *            The width of the original raster.
     * @param height
     *            The height of the original raster.
     * @param bands
     *            The number of bands of the raster.
     * @param dataType
     *            The {@link DataType} of the raster.
     * @param data
     *            The original raster data.
     */
    protected ByteBufferRasterData( int x0, int y0, int subWidth, int subHeight, int width, int height, int bands,
                                    DataType dataType, ByteBuffer data ) {

        this.x0 = x0;
        this.y0 = y0;
        this.width = width;
        this.height = height;
        this.subWidth = subWidth;
        this.subHeight = subHeight;
        this.subset = true;

        this.bands = bands;
        this.dataType = dataType;

        this.nodata = new byte[dataType.getSize()];
        this.data = data;
    }

    /**
     * Set the raster to be a single banded subset.
     * 
     * @param band
     *            The index of the subband.
     */
    protected void setSubBand( int band ) {
        if ( band >= bands ) {
            throw new IndexOutOfBoundsException( "band not available" );
        }
        singleBand = true;
        subBand = band;
    }

    private ByteBuffer createByteBuffer() {
        return ByteBuffer.allocate( getBufferSize() );
    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#createCompatibleRasterData(int, int, int)
     */
    public abstract RasterData createCompatibleRasterData( int width, int height, int bands );

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#createCompatibleRasterData(int)
     */
    public RasterData createCompatibleRasterData( int bands ) {
        return createCompatibleRasterData( this.subWidth, this.subHeight, bands );
    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#createCompatibleRasterData()
     */
    public RasterData createCompatibleRasterData() {
        int bands;
        if ( singleBand ) {
            bands = 1;
        } else {
            bands = this.bands;
        }
        return createCompatibleRasterData( this.subWidth, this.subHeight, bands );
    }

    // returns the selected band
    private final int getBand( int b ) {
        if ( !singleBand ) {
            return b; // not a single-band subset, return requested band number
        } else if ( b == 0 ) { // else single-band subset
            return subBand; // return wrapped band
        } else {
            throw new IndexOutOfBoundsException( "band index out of bounds" );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#getBands()
     */
    public final int getBands() {
        if ( singleBand ) {
            return 1;
        } else {
            return bands;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#getWidth()
     */
    public final int getWidth() {
        return subWidth;
    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#getHeight()
     */
    public final int getHeight() {
        return subHeight;
    }

    /**
     * @return true if the raster is a subset.
     */
    public boolean isSubset() {
        return subset;
    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#getDataType()
     */
    public final DataType getDataType() {
        return dataType;
    }

    /**
     * Returns the size of the ByteBuffer in bytes.
     * 
     * @return size of the buffer
     */
    public final int getBufferSize() {
        return width * height * bands * dataType.getSize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#getInterleaveType()
     */
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
     */
    protected void checkBounds( int x, int y, int width, int height ) {
        if ( ( ( this.subWidth - x ) < width ) || ( ( this.subHeight - y ) < height ) || ( x < 0 ) || ( y < 0 ) ) {

            throw new IndexOutOfBoundsException( "request out of bounds" );
        }
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
        return ( y0 + y ) * getLineStride() + ( x0 + x ) * getPixelStride();
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
        return calculatePos( x, y ) + band * getBandStride(); // TODO
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

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#getSample(int, int, int)
     */
    public byte[] getSample( int x, int y, int band ) {
        int pos = calculatePos( x, y, getBand( band ) );
        if ( pos >= data.capacity() ) {
            return nodata;
        } else {
            byte[] result = new byte[dataType.getSize()];
            data.position( pos );
            data.get( result );
            return result;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#setSample(int, int, int, byte[])
     */
    public void setSample( int x, int y, int band, byte[] value ) {
        data.position( calculatePos( x, y, getBand( band ) ) );
        data.put( value );
    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#setByteSample(int, int, int, byte)
     */
    public void setByteSample( int x, int y, int band, byte value ) {
        int pos = calculatePos( x, y, band );
        data.put( pos, value );
    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#setFloatSample(int, int, int, float)
     */
    public void setFloatSample( int x, int y, int band, float value ) {
        int pos = calculatePos( x, y, band );
        data.putFloat( pos, value );
    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#setBytePixel(int, int, byte[])
     */
    public void setBytePixel( int x, int y, byte[] pixel ) {
        assert pixel.length == bands;
        for ( int band = 0; band < bands; band++ ) {
            data.put( calculatePos( x, y, band ), pixel[band] );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#setFloatPixel(int, int, float[])
     */
    public void setFloatPixel( int x, int y, float[] pixel ) {
        assert pixel.length == bands;
        for ( int band = 0; band < bands; band++ ) {
            data.putFloat( calculatePos( x, y, band ), pixel[band] );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#getByteSample(int, int, int)
     */
    public byte getByteSample( int x, int y, int band ) {
        return data.get( calculatePos( x, y, band ) );
    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#getFloatSample(int, int, int)
     */
    public float getFloatSample( int x, int y, int band ) {
        return data.getFloat( calculatePos( x, y, band ) );
    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#getBytes(int, int, int, int)
     */
    public abstract byte[][] getBytes( int x, int y, int width, int height );

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#getBytes(int, int, int, int, int)
     */
    public abstract byte[][] getBytes( int x, int y, int width, int height, int bands );

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#getFloats(int, int, int, int)
     */
    public abstract float[][] getFloats( int x, int y, int width, int height );

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#getFloats(int, int, int, int, int)
     */
    public abstract float[][] getFloats( int x, int y, int width, int height, int bands );

    /**
     * Returns a 2-D byte array for the specified rectangle.
     * 
     * When the raster contains multiple bands, the samples are interleaved with the passed interleaving type.
     * 
     * @param x
     *            min x coordinate
     * @param y
     *            min y coordinate
     * @param width
     *            size of the rectangle
     * @param height
     *            size of the rectangle
     * @param interleaving
     *            interleaving type for the result array
     * @return selected samples
     */
    public abstract byte[][] getBytes( int x, int y, int width, int height, InterleaveType interleaving );

    /**
     * Returns a 2-D byte array for the specified rectangle.
     * 
     * When the raster contains multiple bands, the samples are interleaved with the passed interleaving type.
     * 
     * @param x
     *            min x coordinate
     * @param y
     *            min y coordinate
     * @param width
     *            size of the rectangle
     * @param height
     *            size of the rectangle
     * @param interleaving
     *            interleaving type for the result array
     * @return selected samples
     */
    public abstract float[][] getFloats( int x, int y, int width, int height, InterleaveType interleaving );

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#getSubset(int, int, int, int)
     */
    public abstract ByteBufferRasterData getSubset( int x0, int y0, int width, int height );

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#getSubset(raster.org.deegree.model.raster.RasterRect)
     */
    public ByteBufferRasterData getSubset( RasterRect env ) {
        return this.getSubset( env.x, env.y, env.width, env.height );
    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#getSubset(int, int, int, int, int)
     */
    public ByteBufferRasterData getSubset( int x0, int y0, int width, int height, int band ) {
        ByteBufferRasterData result = getSubset( x0, y0, width, height );
        result.setSubBand( band );
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#setSubset(int, int, raster.org.deegree.model.raster.RasterData)
     */
    public void setSubset( int x0, int y0, RasterData raster ) {
        int width = Math.min( raster.getWidth(), subWidth - x0 );
        int height = Math.min( raster.getHeight(), subHeight - y0 );

        for ( int y = 0; y < height; y++ )
            for ( int x = 0; x < width; x++ )
                for ( int band = 0; band < this.bands; band++ ) {
                    setSample( x0 + x, y0 + y, band, raster.getSample( x, y, band ) );
                }
    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#setSubset(int, int, raster.org.deegree.model.raster.RasterData,
     *      int)
     */
    public void setSubset( int x0, int y0, int band, RasterData raster ) {
        int width = Math.min( raster.getWidth(), subWidth - x0 );
        int height = Math.min( raster.getHeight(), subHeight - y0 );

        for ( int y = 0; y < height; y++ )
            for ( int x = 0; x < width; x++ ) {
                setSample( x0 + x, y0 + y, band, raster.getSample( x, y, 0 ) );
            }
    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#getBytePixel(int, int)
     */
    public byte[] getBytePixel( int x, int y ) {
        byte[] result = new byte[bands];
        for ( int band = 0; band < bands; band++ ) {
            result[band] = getByteSample( x, y, band );
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#getFloatPixel(int, int)
     */
    public float[] getFloatPixel( int x, int y ) {
        float[] result = new float[bands];
        for ( int band = 0; band < bands; band++ ) {
            result[band] = getFloatSample( x, y, band );
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuilder result = new StringBuilder( 200 );
        result.append( "RasterData: type " + dataType + ", " );
        result.append( "size " + width + "x" + height + "(" + subWidth + "x" + subHeight + ")" );
        result.append( ", interleaving " + getInterleaveType() );

        return result.toString();
    }
}
