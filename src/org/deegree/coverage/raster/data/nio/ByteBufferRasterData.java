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
package org.deegree.coverage.raster.data.nio;

import java.nio.ByteBuffer;

import org.deegree.coverage.raster.data.DataView;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterRect;

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

    /**
     * The view of this data
     */
    protected DataView view;

    /**
     * the width of the raster
     */
    protected int rasterWidth;

    /**
     * the height of the raster
     */
    protected int rasterHeight;

    /**
     * the raster data info of the original data, not the view.
     */
    protected RasterDataInfo dataInfo;

    /**
     * The raster data itself.
     */
    protected ByteBuffer data;

    /**
     * ==================== Abstract method declarations ====================
     */

    /**
     * Implementation should create a view of this raster data.
     * 
     * @param view
     *            the new view on this data
     * 
     * @return a view or new raster data object, backed by a {@link java.nio.ByteBuffer}
     */
    protected abstract ByteBufferRasterData createCompatibleRasterData( DataView view );

    /**
     * @return ByteBufferRasterData with unset data
     */
    protected abstract ByteBufferRasterData createCompatibleEmptyRasterData();

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
     * ==================== Implementation ====================
     */

    /**
     * Creates a new ByteBufferRasterData instance.
     * 
     * @param view
     *            the raster rectangle defining the sample domain of this raster data.
     * @param rasterWidth
     *            width of the raster data
     * @param rasterHeight
     *            height of the raster data
     * @param originalDataInfo
     *            containing information about this raster data object
     * @param init
     *            true if the ByteBuffer should be initialized
     */
    protected ByteBufferRasterData( DataView view, int rasterWidth, int rasterHeight, RasterDataInfo originalDataInfo,
                                    boolean init ) {
        this.view = view;
        this.rasterWidth = rasterWidth;
        this.rasterHeight = rasterHeight;

        this.dataInfo = originalDataInfo;
        if ( init ) {
            initByteBuffer();
        }
    }

    /**
     * Creates a new ByteBufferRasterData instance.
     * 
     * @param view
     *            the raster rectangle defining the sample domain of this raster data.
     * @param rasterWidth
     *            width of the raster data
     * @param rasterHeight
     *            height of the raster data
     * @param originalDataInfo
     *            containing information about this raster data object
     */
    protected ByteBufferRasterData( DataView view, int rasterWidth, int rasterHeight, RasterDataInfo originalDataInfo ) {
        this( view, rasterWidth, rasterHeight, originalDataInfo, true );
    }

    /**
     * Initialize the internal ByteBuffer
     */
    protected void initByteBuffer() {
        this.data = createByteBuffer();
    }

    private ByteBuffer createByteBuffer() {
        return ByteBuffer.allocate( getRequiredBufferSize() );
        // return ByteBuffer.allocateDirect( getBufferSize() );
    }

    /**
     * Returns the needed size of the ByteBuffer in bytes.
     * 
     * @return size of the buffer
     */
    public final int getRequiredBufferSize() {
        // data.capacity() can not be used if the ByteBuffer was not yet instantiated.
        return rasterWidth * rasterHeight * dataInfo.bands * dataInfo.dataSize;
    }

    public ByteBufferRasterData createCompatibleRasterData( int width, int height ) {
        // use the offset from the view and the new width and height (shouldn't they be checked for size?)
        return createCompatibleRasterData( new DataView( view.x, view.y, width, height, dataInfo ) );
    }

    public ByteBufferRasterData createCompatibleRasterData( RasterRect env ) {
        // create a new view using the given rectangle, the info will be the same.
        return createCompatibleRasterData( new DataView( env, dataInfo ) );
    }

    @Override
    public RasterData createCompatibleRasterData( RasterRect sampleDomain, BandType[] bands ) {
        // create a view taken using the given rectangle and the given bands
        return createCompatibleRasterData( new DataView( sampleDomain, createRasterDataInfo( bands ), dataInfo ) );
    }

    public ByteBufferRasterData createCompatibleRasterData( BandType[] bands ) {
        // create a new view from the given bands
        return createCompatibleRasterData( new DataView( view, createRasterDataInfo( bands ), dataInfo ) );
    }

    public ByteBufferRasterData createCompatibleRasterData() {
        // just use the view on the data
        return createCompatibleRasterData( view );
    }

    @Override
    public RasterData asReadOnly() {
        ByteBufferRasterData copy = createCompatibleEmptyRasterData();
        copy.data = this.getByteBuffer().asReadOnlyBuffer();
        return copy;
    }

    public DataType getDataType() {
        return view.dataInfo.dataType;
    }

    public int getBands() {
        return view.dataInfo.bands;
    }

    public RasterDataInfo getDataInfo() {
        return view.dataInfo;
    }

    public byte[] getNullPixel( byte[] result ) {
        return view.dataInfo.getNoDataPixel( result );
    }

    public void setNullPixel( byte[] values ) {
        view.dataInfo.setNoDataPixel( values );
    }

    public final int getWidth() {
        return view.width;
    }

    public final int getHeight() {
        return view.height;
    }

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
     * @param bandOfView
     *            band index of the sample
     * @return byte offset to the sample with the specified coordinate
     */
    public final int calculatePos( int x, int y, int bandOfView ) {
        return ( ( view.y + y ) * getLineStride() ) + ( ( view.x + x ) * getPixelStride() )
               + ( view.getBandOffset( bandOfView ) * getBandStride() );
    }

    /**
     * Calculates the position of a pixel in a view (FloatBuffer, etc.) of the ByteBuffer.
     * 
     * This method considers different sample sizes (eg. byte, float) and returns the position in sample strides (not
     * byte strides). Use this method to get proper positions for ByteBuffer views like FloatBuffer, ShortBuffer, etc..
     * 
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @return offset to the pixel with the specified coordinates
     */
    public final int calculateViewPos( int x, int y ) {
        return calculatePos( x, y ) / dataInfo.dataSize;// TODO
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
        return calculatePos( x, y, band ) / dataInfo.dataSize;// TODO
    }

    public byte[] getBytes( int x, int y, int width, int height, int band, byte[] result ) {
        checkBoundsEx( x, y, width, height );
        if ( result == null || result.length < ( width * height ) ) {
            result = new byte[width * height];
        }
        for ( int h = 0; h < height; h++ ) {
            for ( int w = 0; w < width; w++ ) {
                result[( 2 * h ) + w] = data.get( calculatePos( x + w, y + h, band ) );
            }
        }
        return result;
    }

    public byte[] getBytePixel( int x, int y, byte[] result ) {
        if ( result == null || result.length < view.dataInfo.bands ) {
            result = new byte[view.dataInfo.bands];
        }
        for ( int band = 0; band < view.dataInfo.bands; band++ ) {
            result[band] = getByteSample( x, y, band );
        }
        return result;
    }

    public byte getByteSample( int x, int y, int band ) {
        return data.get( calculatePos( x, y, band ) );
    }

    public double[] getDoubles( int x, int y, int width, int height, int band, double[] result ) {
        checkBoundsEx( x, y, width, height );
        if ( result == null || result.length < ( width * height ) ) {
            result = new double[width * height];
        }
        for ( int h = 0; h < height; h++ ) {
            for ( int w = 0; w < width; w++ ) {
                result[( 2 * h ) + w] = data.getDouble( calculatePos( x + w, y + h, band ) );
            }
        }
        return result;
    }

    public double[] getDoublePixel( int x, int y, double[] result ) {
        if ( result == null || result.length < view.dataInfo.bands ) {
            result = new double[view.dataInfo.bands];
        }
        for ( int band = 0; band < view.dataInfo.bands; band++ ) {
            result[band] = getDoubleSample( x, y, band );
        }
        return result;
    }

    public double getDoubleSample( int x, int y, int band ) {
        return data.getDouble( calculatePos( x, y, band ) );
    }

    public float[] getFloats( int x, int y, int width, int height, int band, float[] result ) {
        checkBoundsEx( x, y, width, height );
        if ( result == null || result.length < ( width * height ) ) {
            result = new float[width * height];
        }
        for ( int h = 0; h < height; h++ ) {
            for ( int w = 0; w < width; w++ ) {
                result[( 2 * h ) + w] = data.getFloat( calculatePos( x + w, y + h, band ) );
            }
        }
        return result;
    }

    public float[] getFloatPixel( int x, int y, float[] result ) {
        if ( result == null || result.length < view.dataInfo.bands ) {
            result = new float[view.dataInfo.bands];
        }
        for ( int band = 0; band < view.dataInfo.bands; band++ ) {
            result[band] = getFloatSample( x, y, band );
        }
        return result;
    }

    public float getFloatSample( int x, int y, int band ) {
        return data.getFloat( calculatePos( x, y, band ) );
    }

    public int[] getInts( int x, int y, int width, int height, int band, int[] result ) {
        checkBoundsEx( x, y, width, height );
        if ( result == null || result.length < ( width * height ) ) {
            result = new int[width * height];
        }
        for ( int h = 0; h < height; h++ ) {
            for ( int w = 0; w < width; w++ ) {
                result[( 2 * h ) + w] = data.getInt( calculatePos( x + w, y + h, band ) );
            }
        }
        return result;
    }

    public int[] getIntPixel( int x, int y, int[] result ) {
        if ( result == null || result.length < view.dataInfo.bands ) {
            result = new int[view.dataInfo.bands];
        }
        for ( int band = 0; band < view.dataInfo.bands; band++ ) {
            result[band] = getIntSample( x, y, band );
        }
        return result;
    }

    public int getIntSample( int x, int y, int band ) {
        return data.getInt( calculatePos( x, y, band ) );
    }

    public byte[] getPixel( int x, int y, byte[] result ) {
        // operates on the view.
        int numBands = view.dataInfo.bands;
        // datasize should be equal between original data and the view
        int sampleSize = view.dataInfo.dataSize;
        if ( result == null || result.length < ( numBands * sampleSize ) ) {
            result = new byte[numBands * sampleSize];
        }

        // null pixel
        if ( 0 > x || x >= rasterWidth || 0 > y || y >= rasterWidth ) {
            System.arraycopy( view.dataInfo.noDataPixel, 0, result, 0, result.length );
            return result;
        }

        // copy per band on the view.
        for ( int b = 0; b < numBands; b++ ) {
            data.position( calculatePos( x, y, b ) );
            data.get( result, b * sampleSize, sampleSize );
        }

        return result;
    }

    public byte[] getSample( int x, int y, int band, byte[] result ) {
        if ( result == null || result.length < dataInfo.dataSize ) {
            result = new byte[dataInfo.dataSize];
        }
        int pos = calculatePos( x, y, band );

        data.position( pos );
        data.get( result, 0, dataInfo.dataSize );
        return result;
    }

    public short[] getShorts( int x, int y, int width, int height, int band, short[] result ) {
        checkBoundsEx( x, y, width, height );
        if ( result == null || result.length < ( width * height ) ) {
            result = new short[width * height];
        }
        for ( int h = 0; h < height; h++ ) {
            for ( int w = 0; w < width; w++ ) {
                result[( 2 * h ) + w] = data.getShort( calculatePos( x + w, y + h, band ) );
            }
        }
        return result;
    }

    public short[] getShortPixel( int x, int y, short[] result ) {
        if ( result == null || result.length < view.dataInfo.bands ) {
            result = new short[view.dataInfo.bands];
        }
        for ( int band = 0; band < view.dataInfo.bands; band++ ) {
            result[band] = getShortSample( x, y, band );
        }
        return result;
    }

    public short getShortSample( int x, int y, int band ) {
        return data.getShort( calculatePos( x, y, band ) );
    }

    public ByteBufferRasterData getSubset( RasterRect sampleDomain ) {
        // return this.getSubset( env.x, env.y, env.width, env.height );
        // return createCompatibleRasterData( sampleDomain );
        ByteBufferRasterData result = createCompatibleRasterData( new DataView( view.x + sampleDomain.x,
                                                                                view.y + sampleDomain.y,
                                                                                sampleDomain.width,
                                                                                sampleDomain.height, dataInfo ) );
        result.data = this.data.asReadOnlyBuffer();
        return result;

    }

    public ByteBufferRasterData getSubset( RasterRect sampleDomain, BandType[] bands ) {
        ByteBufferRasterData result = createCompatibleRasterData( new DataView( view.x + sampleDomain.x,
                                                                                view.y + sampleDomain.y,
                                                                                sampleDomain.width,
                                                                                sampleDomain.height,
                                                                                createRasterDataInfo( bands ), dataInfo ) );
        result.data = this.data.asReadOnlyBuffer();

        // RASTERRECT viewRect = new RasterRect( ( view.x + rasterRect.x ), ( view.y + rasterRect.y ), rasterRect.width,
        // rasterRect.height );
        // ByteBufferRasterData result = (ByteBufferRasterData) createCompatibleWritableRasterData( viewRect, bands );
        // // result.data = this.data.asReadOnlyBuffer();
        // result.setSubset( 0, 0, rasterRect.width, rasterRect.height, 0, this, bands, view.x + rasterRect.x,
        // view.y + rasterRect.y );
        return result;
    }

    public void setBytes( int x, int y, int width, int height, int band, byte[] values ) {
        if ( values != null && values.length >= ( width * height ) ) {
            for ( int h = 0; h < height; h++ ) {
                for ( int w = 0; w < width; w++ ) {
                    data.put( calculatePos( x + w, y + h, band ), values[( 2 * h ) + w] );
                }
            }
        }
    }

    public void setBytePixel( int x, int y, byte[] pixel ) {
        for ( int band = 0; band < view.dataInfo.bands; band++ ) {
            data.put( calculatePos( x, y, band ), pixel[band] );
        }
    }

    public void setByteSample( int x, int y, int band, byte value ) {
        data.put( calculatePos( x, y, band ), value );
    }

    public void setDoubles( int x, int y, int width, int height, int band, double[] values ) {
        if ( values != null && values.length >= ( width * height ) ) {
            for ( int h = 0; h < height; h++ ) {
                for ( int w = 0; w < width; w++ ) {
                    data.putDouble( calculatePos( x + w, y + h, band ), values[( 2 * h ) + w] );
                }
            }
        }
    }

    public void setDoublePixel( int x, int y, double[] pixel ) {
        for ( int band = 0; band < view.dataInfo.bands; band++ ) {
            data.putDouble( calculatePos( x, y, band ), pixel[band] );
        }
    }

    public void setDoubleSample( int x, int y, int band, double value ) {
        data.putDouble( calculatePos( x, y, band ), value );
    }

    public void setFloats( int x, int y, int width, int height, int band, float[] values ) {
        if ( values != null && values.length >= ( width * height ) ) {
            for ( int h = 0; h < height; h++ ) {
                for ( int w = 0; w < width; w++ ) {
                    data.putFloat( calculatePos( x + w, y + h, band ), values[( 2 * h ) + w] );
                }
            }
        }
    }

    public void setFloatPixel( int x, int y, float[] pixel ) {
        for ( int band = 0; band < view.dataInfo.bands; band++ ) {
            data.putFloat( calculatePos( x, y, band ), pixel[band] );
        }
    }

    public void setFloatSample( int x, int y, int band, float value ) {
        data.putFloat( calculatePos( x, y, band ), value );
    }

    public void setPixel( int x, int y, byte[] result ) {
        // operates on the view
        if ( result != null && result.length == ( view.dataInfo.dataSize * view.dataInfo.bands ) ) {
            int sampleSize = view.dataInfo.dataSize;
            for ( int b = 0; b < view.dataInfo.bands; b++ ) {
                data.position( calculatePos( x, y, b ) );
                data.put( result, b * sampleSize, sampleSize );
            }
        }
    }

    public void setInts( int x, int y, int width, int height, int band, int[] values ) {
        if ( values != null && values.length >= ( width * height ) ) {
            for ( int h = 0; h < height; h++ ) {
                for ( int w = 0; w < width; w++ ) {
                    data.putInt( calculatePos( x + w, y + h, band ), values[( 2 * h ) + w] );
                }
            }
        }
    }

    public void setIntPixel( int x, int y, int[] pixel ) {
        for ( int band = 0; band < view.dataInfo.bands; band++ ) {
            data.putInt( calculatePos( x, y, band ), pixel[band] );
        }
    }

    public void setIntSample( int x, int y, int band, int value ) {
        data.putInt( calculatePos( x, y, band ), value );
    }

    public void setShorts( int x, int y, int width, int height, int band, short[] values ) {
        if ( values != null && values.length >= ( width * height ) ) {
            for ( int h = 0; h < height; h++ ) {
                for ( int w = 0; w < width; w++ ) {
                    data.putShort( calculatePos( x + w, y + h, band ), values[( 2 * h ) + w] );
                }
            }
        }
    }

    public void setShortPixel( int x, int y, short[] pixel ) {
        for ( int band = 0; band < view.dataInfo.bands; band++ ) {
            data.putShort( calculatePos( x, y, band ), pixel[band] );
        }
    }

    public void setShortSample( int x, int y, int band, short value ) {
        data.putShort( calculatePos( x, y, band ), value );
    }

    public void setSample( int x, int y, int band, byte[] value ) {
        if ( value == null || value.length < view.dataInfo.dataSize ) {
            return;
        }
        data.position( calculatePos( x, y, band ) );
        data.put( value, 0, view.dataInfo.dataSize );
    }

    public void setSubset( int x0, int y0, int width, int height, RasterData sourceRaster ) {
        setSubset( x0, y0, width, height, sourceRaster, 0, 0 );
    }

    public void setSubset( int x0, int y0, int width, int height, int dstBand, RasterData sourceRaster, int srcBand ) {
        setSubset( x0, y0, width, height, dstBand, sourceRaster, srcBand, 0, 0 );
    }

    public void setSubset( int dstX, int dstY, int width, int height, RasterData srcRaster, int srcX, int srcY ) {

        // clamp to maximum possible size
        // int subWidth = min( this.rasterWidth - dstX, width, srcRaster.getWidth() );
        // int subHeight = min( this.rasterHeight - dstY, height, srcRaster.getHeight() );

        int subWidth = clampSize( getWidth(), dstX, srcRaster.getWidth(), srcX, width );
        int subHeight = clampSize( getHeight(), dstY, srcRaster.getHeight(), srcY, height );

        if ( subHeight <= 0 || subWidth <= 0 ) {
            return;
        }

        byte[] tmp = new byte[dataInfo.dataSize];
        for ( int y = 0; y < subHeight; ++y ) {
            for ( int x = 0; x < subWidth; ++x ) {
                for ( int band = 0; band < this.view.dataInfo.bands; band++ ) {
                    srcRaster.getSample( x + srcX, y + srcY, band, tmp );
                    setSample( dstX + x, dstY + y, band, tmp );
                }
            }
        }
    }

    public void setSubset( int dstX, int dstY, int width, int height, int dstBand, RasterData srcRaster, int srcBand,
                           int srcX, int srcY ) {
        // clamp to maximum possible size
        // int subWidth = min( this.rasterWidth - dstX, width, srcRaster.getWidth() );
        // int subHeight = min( this.rasterHeight - dstY, height, srcRaster.getHeight() );
        int subWidth = clampSize( getWidth(), dstX, srcRaster.getWidth(), srcX, width );
        int subHeight = clampSize( getHeight(), dstY, srcRaster.getHeight(), srcY, height );

        if ( subHeight <= 0 || subWidth <= 0 ) {
            return;
        }

        byte[] tmp = new byte[dataInfo.dataSize];
        for ( int y = 0; y < subHeight; y++ ) {
            for ( int x = 0; x < subWidth; x++ ) {
                srcRaster.getSample( x + srcX, y + srcY, srcBand, tmp );
                setSample( dstX + x, dstY + y, dstBand, tmp );
            }
        }
    }

    /**
     * Clamp to the minimal size of the given values.
     * 
     * @param dstSize
     *            e.g. getWidth() or getHeight() of the destination raster.
     * @param dstOrdinate
     *            e.g srcX, srcY, the location to put the data in destination raster.
     * @param srcSize
     *            e.g. getWidth() or getHeight() of the source raster. *
     * @param srcOrdinate
     *            e.g dstX, dstY, the location to read the data from the source raster.
     * @param copySize
     *            e.g. width/height of rectangle to put data in this raster.
     * @return the minimal value
     */
    protected static final int clampSize( int dstSize, int dstOrdinate, int srcSize, int srcOrdinate, int copySize ) {
        return min( dstSize - dstOrdinate, srcSize - srcOrdinate, copySize );
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append( "RasterData: type " + dataInfo.dataType + ", " );
        result.append( "size " + rasterWidth + "x" + rasterHeight + "(" + rasterWidth + "x" + rasterHeight + ")" );
        result.append( ", interleaving " + dataInfo.interleaveType );

        return result.toString();
    }

    /**
     * Returns the smallest value of all <code>int</code>s.
     * 
     * @param sizes
     * @return the smallest value
     */
    protected static final int min( int... sizes ) {
        int result = Math.min( sizes[0], sizes[1] );
        int i = 2;
        while ( i < sizes.length ) {
            result = Math.min( result, sizes[i] );
            i++;
        }
        return result;
    }

    /**
     * Create the raster data info object for the given bands, if empty or
     * <code>null<code> the current dataInfo will be used.
     * 
     * @param bands
     * @return a new raster data info object with for the given bands.
     */
    protected RasterDataInfo createRasterDataInfo( BandType[] bands ) {
        if ( bands == null || bands.length == 0 ) {
            return dataInfo;
        }
        byte[] noPD = dataInfo.getNoDataPixel( bands );
        return new RasterDataInfo( noPD, bands, dataInfo.dataType, dataInfo.interleaveType );
    }
}
