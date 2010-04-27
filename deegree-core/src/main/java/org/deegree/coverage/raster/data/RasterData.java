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
package org.deegree.coverage.raster.data;

import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.data.info.InterleaveType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.geom.RasterRect;

/**
 * Interface for a 2 dimensional raster.
 * <p>
 * A raster is a rectangular grid of pixels with coordinates from the uppler left corner (0, 0) to the lower right
 * corner (width-1, height-1). A raster can contain multiple bands. A pixel stores one sample for every band. The sample
 * arrangement is determined by the interleaving type ({@link InterleaveType}). The size of each sample is dertermined
 * by the data type ({@link DataType}).
 * 
 * <p>
 * Most get-methods accept an array as a parameter for performance reasons. This parameter is used to store the result.
 * The same array will be returned by the method, filled with requested values. A new array will be created, filled and
 * returned if this parameter is <code>null</code>.
 * 
 * <p>
 * TODO: Only defines interface to byte, short and float data at the moment. Copy float methods for other data types.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 * @see DataType
 * @see InterleaveType
 * 
 */
public interface RasterData {

    /**
     * Integer defining the RGB byte bands
     */
    public static final int TYPE_BYTE_RGB = -100;

    /**
     * Integer defining the RGBA byte bands with.
     */
    public static final int TYPE_BYTE_RGBA = -101;

    /**
     * Returns the data type of the raster.
     * 
     * @return data type of the raster
     */
    public DataType getDataType();

    /**
     * Returns the number of bands of the raster
     * 
     * @return the bands
     */
    public int getBands();

    /**
     * Returns an encapsulation of all available information of this raster data.
     * 
     * @return available information of this raster data.
     */
    public RasterDataInfo getDataInfo();

    /**
     * Returns the height of the raster
     * 
     * @return the height
     */
    public int getHeight();

    /**
     * Returns the width of the raster
     * 
     * @return the width
     */
    public int getWidth();

    /**
     * Returns the no data values for this raster, or the view of this rasters bands.
     * 
     * @param result
     *            an array to put the values into or <code>null</code>
     * @return the <code>result</code> array or a new array, if the <code>result</code> array is <code>null</code>
     */
    public byte[] getNullPixel( byte[] result );

    /**
     * Sets the no data values for this raster
     * 
     * @param values
     *            an array with the null values
     */
    public void setNoDataValue( byte[] values );

    /**
     * Returns a new RasterData with the same DataType and InterleaveType
     * 
     * @param sampleDomain
     *            the raster rectangle defining the sample domain of this raster data.
     * @param bands
     *            indices to the requested bands
     * @return new empty raster
     */
    public RasterData createCompatibleRasterData( RasterRect sampleDomain, BandType[] bands );

    /**
     * Create a writable compatible raster with the height and width of the given sample domain.
     * 
     * @param sampleDomain
     *            the raster rectangle defining the sample domain of this raster data.
     * @param bands
     *            indices to the requested bands if <code>null</code> all bands will be available.
     * @return new empty raster
     */
    public RasterData createCompatibleWritableRasterData( RasterRect sampleDomain, BandType[] bands );

    /**
     * Returns a view as a new RasterData with the same DataType and InterleaveType but valid only for the given rect.
     * 
     * @param sampleDomain
     *            the raster rectangle defining the sample domain of this raster data.
     * 
     * @return new raster backed by the readonly data of original raster.
     */
    public ByteBufferRasterData createCompatibleRasterData( RasterRect sampleDomain );

    /**
     * Returns a new RasterData with the same DataType and InterleaveType and all bands
     * 
     * @param width
     *            width of the new raster
     * @param height
     *            height of the new raster
     * @return new empty raster
     */
    public RasterData createCompatibleRasterData( int width, int height );

    /**
     * Returns a new RasterData with the same size, DataType and InterleaveType
     * 
     * @param bands
     *            definitions of the new rasterdata.
     * @return new empty raster
     */
    public RasterData createCompatibleRasterData( BandType[] bands );

    /**
     * Returns a new RasterData with the same size, bands, DataType and InterleaveType
     * 
     * @return new empty raster
     */
    public RasterData createCompatibleRasterData();

    /**
     * Return a read-only version of this RasterData. The result shares the same data as the original.
     * 
     * @return a read-only RasterData
     */
    public RasterData asReadOnly();

    /**
     * Returns a sample as byte array, regardless of the DataType. i.e. a FLOAT DataType results in a four byte array
     * 
     * @param x
     * @param y
     * @param band
     *            selected band
     * @param result
     *            an array to put the values into or <code>null</code>
     * @return the <code>result</code> array or a new array, if the <code>result</code> array is <code>null</code>
     */
    public byte[] getSample( int x, int y, int band, byte[] result );

    /**
     * Sets a sample with data from a byte array, regardless of the DataType. i.e. a float must be packed as a four byte
     * array
     * 
     * @param x
     * @param y
     * @param band
     *            selected band
     * @param values
     *            array with the sample value
     */
    public void setSample( int x, int y, int band, byte[] values );

    /**
     * Returns a pixel as byte array, regardless of the DataType. i.e. a FLOAT DataType results in a four byte array.
     * 
     * @param x
     * @param y
     * @param result
     *            a byte array to put the values into or <code>null</code>
     * @return the <code>result</code> array or a new array, if the <code>result</code> array is <code>null</code>
     */
    public byte[] getPixel( int x, int y, byte[] result );

    /**
     * Sets a pixel with data from a byte array, regardless of the DataType. i.e. a float sample must be packed as a
     * four byte array.
     * 
     * @param x
     * @param y
     * @param pixel
     */
    public void setPixel( int x, int y, byte[] pixel );

    /*******************************************************************************************************************
     * pixel getter and setter
     ******************************************************************************************************************/

    /**
     * Returns a byte array with all sample values from coordinate x/y. The length of the array is equal to the number
     * of bands.
     * 
     * @param x
     * @param y
     * @param result
     *            an array to put the values into or <code>null</code>
     * @return the <code>result</code> array or a new array, if the <code>result</code> array is <code>null</code>
     */
    public byte[] getBytePixel( int x, int y, byte[] result );

    /**
     * Returns a double array with all sample values from coordinate x/y. The length of the array is equal to the number
     * of bands.
     * 
     * @param x
     * @param y
     * @param result
     *            an array to put the values into or <code>null</code>
     * @return the <code>result</code> array or a new array, if the <code>result</code> array is <code>null</code>
     */
    public double[] getDoublePixel( int x, int y, double[] result );

    /**
     * Returns a float array with all sample values from coordinate x/y. The length of the array is equal to the number
     * of bands.
     * 
     * @param x
     * @param y
     * @param result
     *            an array to put the values into or <code>null</code>
     * @return the <code>result</code> array or a new array, if the <code>result</code> array is <code>null</code>
     */
    public float[] getFloatPixel( int x, int y, float[] result );

    /**
     * Returns an integer array with all sample values from coordinate x/y. The length of the array is equal to the
     * number of bands.
     * 
     * @param x
     * @param y
     * @param result
     *            an array to put the values into or <code>null</code>
     * @return the <code>result</code> array or a new array, if the <code>result</code> array is <code>null</code>
     */
    public int[] getIntPixel( int x, int y, int[] result );

    /**
     * Returns a short array with all sample values from coordinate x/y. The length of the array is equal to the number
     * of bands.
     * 
     * @param x
     * @param y
     * @param result
     *            an array to put the values into or <code>null</code>
     * @return the <code>result</code> array or a new array, if the <code>result</code> array is <code>null</code>
     */
    public short[] getShortPixel( int x, int y, short[] result );

    /**
     * Sets a single pixel with byte values for each sample. The length of pixel array must be equal to the number of
     * bands.
     * 
     * @param x
     * @param y
     * @param pixel
     *            array with one sample per band
     */
    public void setBytePixel( int x, int y, byte[] pixel );

    /**
     * Sets a single pixel with double values for each sample. The length of pixel array must be equal to the number of
     * bands.
     * 
     * @param x
     * @param y
     * @param pixel
     *            array with one sample per band
     */
    public void setDoublePixel( int x, int y, double[] pixel );

    /**
     * Sets a single pixel with float values for each sample. The length of pixel array must be equal to the number of
     * bands.
     * 
     * @param x
     * @param y
     * @param pixel
     *            array with one sample per band
     */
    public void setFloatPixel( int x, int y, float[] pixel );

    /**
     * Sets a single pixel with integer values for each sample. The length of pixel array must be equal to the number of
     * bands.
     * 
     * @param x
     * @param y
     * @param pixel
     *            array with one sample per band
     */
    public void setIntPixel( int x, int y, int[] pixel );

    /**
     * Sets a single pixel with short values for each sample. The length of pixel array must be equal to the number of
     * bands.
     * 
     * @param x
     * @param y
     * @param pixel
     *            array with one sample per band
     */
    public void setShortPixel( int x, int y, short[] pixel );

    /*******************************************************************************************************************
     * sample getter and setter
     ******************************************************************************************************************/

    /**
     * Returns a byte sample from coordinate x/y and selected band.
     * 
     * @param x
     * @param y
     * @param band
     *            band number for sample
     * @return sample from selected coordinate and band
     */
    public byte getByteSample( int x, int y, int band );

    /**
     * Returns a double sample from coordinate x/y and selected band.
     * 
     * @param x
     * @param y
     * @param band
     *            band number for sample
     * @return sample from selected coordinate and band
     */
    public double getDoubleSample( int x, int y, int band );

    /**
     * Returns a float sample from coordinate x/y and selected band.
     * 
     * @param x
     * @param y
     * @param band
     *            band number for sample
     * @return sample from selected coordinate and band
     */
    public float getFloatSample( int x, int y, int band );

    /**
     * Returns an integer sample from coordinate x/y and selected band.
     * 
     * @param x
     * @param y
     * @param band
     *            band number for sample
     * @return sample from selected coordinate and band
     */
    public int getIntSample( int x, int y, int band );

    /**
     * Returns a short sample from coordinate x/y and selected band.
     * 
     * @param x
     * @param y
     * @param band
     *            band number for sample
     * @return sample from selected coordinate and band
     */
    public short getShortSample( int x, int y, int band );

    /**
     * Sets a single byte sample on coordinate x/y and selected band.
     * 
     * @param x
     * @param y
     * @param band
     *            band number for sample
     * @param value
     *            new value for sample
     */
    public void setByteSample( int x, int y, int band, byte value );

    /**
     * Sets a single double sample on coordinate x/y and selected band.
     * 
     * @param x
     * @param y
     * @param band
     *            band number for sample
     * @param value
     *            new value for sample
     */
    public void setDoubleSample( int x, int y, int band, double value );

    /**
     * Sets a single float sample on coordinate x/y and selected band.
     * 
     * @param x
     * @param y
     * @param band
     *            band number for sample
     * @param value
     *            new value for sample
     */
    public void setFloatSample( int x, int y, int band, float value );

    /**
     * Sets a single integer sample on coordinate x/y and selected band.
     * 
     * @param x
     * @param y
     * @param band
     *            band number for sample
     * @param value
     *            new value for sample
     */
    public void setIntSample( int x, int y, int band, int value );

    /**
     * Sets a single short sample on coordinate x/y and selected band.
     * 
     * @param x
     * @param y
     * @param band
     *            band number for sample
     * @param value
     *            new value for sample
     */
    public void setShortSample( int x, int y, int band, short value );

    /*******************************************************************************************************************
     * getter and setter for rectangles
     ******************************************************************************************************************/

    /**
     * Gets values from the specified rectangle and band. The result is stored row-ordered in a single array (e.g.
     * {x0y0, x1y0, x2y0,...,x0y1...})
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     * @param band
     * @param result
     *            an array to put the values into or <code>null</code>
     * @return the <code>result</code> array or a new array, if the <code>result</code> array is <code>null</code>
     */
    public byte[] getBytes( int x, int y, int width, int height, int band, byte[] result );

    /**
     * Gets values from the specified rectangle and band. The result is stored row-ordered in a single array (e.g.
     * {x0y0, x1y0, x2y0,...,x0y1...})
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     * @param band
     * @param result
     *            an array to put the values into or <code>null</code>
     * @return the <code>result</code> array or a new array, if the <code>result</code> array is <code>null</code>
     */
    public double[] getDoubles( int x, int y, int width, int height, int band, double[] result );

    /**
     * Gets values from the specified rectangle and band. The result is stored row-ordered in a single array (e.g.
     * {x0y0, x1y0, x2y0,...,x0y1...})
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     * @param band
     * @param result
     *            an array to put the values into or <code>null</code>
     * @return the <code>result</code> array or a new array, if the <code>result</code> array is <code>null</code>
     */
    public float[] getFloats( int x, int y, int width, int height, int band, float[] result );

    /**
     * Gets values from the specified rectangle and band. The result is stored row-ordered in a single array (e.g.
     * {x0y0, x1y0, x2y0,...,x0y1...})
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     * @param band
     * @param result
     *            an array to put the values into or <code>null</code>
     * @return the <code>result</code> array or a new array, if the <code>result</code> array is <code>null</code>
     */
    public int[] getInts( int x, int y, int width, int height, int band, int[] result );

    /**
     * Gets values from the specified rectangle and band. The result is stored row-ordered in a single array (e.g.
     * {x0y0, x1y0, x2y0,...,x0y1...})
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     * @param band
     * @param result
     *            an array to put the values into or <code>null</code>
     * @return the <code>result</code> array or a new array, if the <code>result</code> array is <code>null</code>
     */
    public short[] getShorts( int x, int y, int width, int height, int band, short[] result );

    /**
     * Sets values from the array to the specified rectangle and band. The values must be stored row-ordered in a single
     * array (e.g. {x0y0, x1y0, x2y0,...,x0y1...})
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     * @param band
     * @param values
     *            the samples to put into the raster
     */
    public void setBytes( int x, int y, int width, int height, int band, byte[] values );

    /**
     * Sets values from the array to the specified rectangle and band. The values must be stored row-ordered in a single
     * array (e.g. {x0y0, x1y0, x2y0,...,x0y1...})
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     * @param band
     * @param values
     *            the samples to put into the raster
     */
    public void setDoubles( int x, int y, int width, int height, int band, double[] values );

    /**
     * Sets values from the array to the specified rectangle and band. The values must be stored row-ordered in a single
     * array (e.g. {x0y0, x1y0, x2y0,...,x0y1...})
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     * @param band
     * @param values
     *            the samples to put into the raster
     */
    public void setFloats( int x, int y, int width, int height, int band, float[] values );

    /**
     * Sets values from the array to the specified rectangle and band. The values must be stored row-ordered in a single
     * array (e.g. {x0y0, x1y0, x2y0,...,x0y1...})
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     * @param band
     * @param values
     *            the samples to put into the raster
     */
    public void setInts( int x, int y, int width, int height, int band, int[] values );

    /**
     * Sets values from the array to the specified rectangle and band. The values must be stored row-ordered in a single
     * array (e.g. {x0y0, x1y0, x2y0,...,x0y1...})
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     * @param band
     * @param values
     *            the samples to put into the raster
     */
    public void setShorts( int x, int y, int width, int height, int band, short[] values );

    /*******************************************************************************************************************
     * RasterData getter and setter
     ******************************************************************************************************************/

    /**
     * Returns new RasterData object for the specified rectangle.
     * 
     * @param rasterRect
     *            rectangle for subset
     * @return selected subset
     */
    public RasterData getSubset( RasterRect rasterRect );

    /**
     * Returns new single-band RasterData object for the specified rectangle.
     * 
     * @param rasterRect
     *            rectangle for subset
     * @param bands
     *            selected band
     * @return selected rectangle
     */
    public RasterData getSubset( RasterRect rasterRect, BandType[] bands );

    /**
     * Sets the raster with values from sourceRaster.
     * 
     * @param x
     *            insert position
     * @param y
     *            insert position
     * @param width
     *            width of the subset
     * @param height
     *            height of the subset
     * @param sourceRaster
     *            data source to copy
     */
    public void setSubset( int x, int y, int width, int height, RasterData sourceRaster );

    /**
     * Sets the raster with values from sourceRaster.
     * 
     * @param x
     *            insert position
     * @param y
     *            insert position
     * @param width
     *            width of the subset
     * @param height
     *            height of the subset
     * @param xOffset
     *            x offset in the source raster
     * @param yOffset
     *            y offset in the source raster
     * @param sourceRaster
     *            data source to copy
     */
    public void setSubset( int x, int y, int width, int height, RasterData sourceRaster, int xOffset, int yOffset );

    /**
     * Sets a single band of the raster with values from one band of the sourceRaster.
     * 
     * @param x
     *            insert position
     * @param y
     *            insert position
     * @param width
     *            width of the subset
     * @param height
     *            height of the subset
     * @param dstBand
     *            the index of the destination band
     * @param sourceRaster
     *            data source to copy
     * @param srcBand
     *            the index of the source band of <code>sourceRaster</code>
     */
    public void setSubset( int x, int y, int width, int height, int dstBand, RasterData sourceRaster, int srcBand );

    /**
     * Sets a single band of the raster with values from one band of the sourceRaster.
     * 
     * @param x
     *            insert position
     * @param y
     *            insert position
     * @param width
     *            width of the subset
     * @param height
     *            height of the subset
     * @param dstBand
     *            the index of the destination band
     * @param sourceRaster
     *            data source to copy
     * @param srcBand
     *            the index of the source band of <code>sourceRaster</code>
     * @param xOffset
     *            x offset in the source raster
     * @param yOffset
     *            y offset in the source raster
     */
    public void setSubset( int x, int y, int width, int height, int dstBand, RasterData sourceRaster, int srcBand,
                           int xOffset, int yOffset );

    /**
     * Try to dispose all allocated memory of this data object.
     */
    public void dispose();

}
