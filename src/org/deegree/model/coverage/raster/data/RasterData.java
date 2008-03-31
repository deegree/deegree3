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

/**
 * Interface for a 2 dimensional raster.
 * 
 * A raster is a rectangular grid of pixels with coordinates from the uppler left corner (0, 0) to the lower right
 * corner (width-1, height-1). A raster can contain multiple bands. A pixel stores one sample for every band. The sample
 * arrangement is determined by the interleaving type. The size of each sample is dertermined by the data type.
 * 
 * TODO: Only defines interface to byte and float data at the moment. Copy float methods for other data types.
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
     * Returns the data type of the raster.
     * 
     * @return data type of the raster
     */
    public DataType getDataType();

    /**
     * Returns the sample interleaving typ of the raster.
     * 
     * @return interleaving type of the raster
     */
    public InterleaveType getInterleaveType();

    /**
     * Returns the number of bands of the raster
     * 
     * @return the bands
     */
    public int getBands();

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
     * Returns a new RasterData with the same DataType and InterleaveType
     * 
     * @param width
     *            width of the new raster
     * @param height
     *            height of the new raster
     * @param bands
     *            number of bands
     * @return new empty raster
     */
    public RasterData createCompatibleRasterData( int width, int height, int bands );

    /**
     * Returns a new RasterData with the same size, DataType and InterleaveType
     * 
     * @param bands
     *            number of bands
     * @return new empty raster
     */
    public RasterData createCompatibleRasterData( int bands );

    /**
     * Returns a new RasterData with the same size, bands, DataType and InterleaveType
     * 
     * @return new empty raster
     */
    public RasterData createCompatibleRasterData();

    /**
     * Retruns a sample as byte array, regardless of the DataType. i.e. a FLOAT DataType results in a four byte array
     * 
     * @param x
     *            x position
     * @param y
     *            y position
     * @param band
     *            selected band
     * @return byte array with sample data
     */
    public byte[] getSample( int x, int y, int band );

    /**
     * Sets a sample with data from a byte array, regardless of the DataType. i.e. a float must be packed as a four byte
     * array
     * 
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @param band
     *            selected band
     * @param values
     *            array with the sample value
     */
    public void setSample( int x, int y, int band, byte[] values );

    /**
     * Sets a single pixel with byte values for each sample. The lenght of pixel array must be equal to the number of
     * bands.
     * 
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @param pixel
     *            array with one sample per band
     */
    public void setBytePixel( int x, int y, byte[] pixel );

    /**
     * Returns a byte array with all sample values from coordinate x/y. The lenght of the result array is equal to the
     * number of bands.
     * 
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @return sample array with sample values
     */
    public byte[] getBytePixel( int x, int y );

    /**
     * Sets a single pixel with float values for each sample. The lenght of pixel array must be equal to the number of
     * bands.
     * 
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @param pixel
     *            array with one sample per band
     */
    public void setFloatPixel( int x, int y, float[] pixel );

    /**
     * Returns a float array with all sample values from coordinate x/y. The lenght of the result array is equal to the
     * number of bands.
     * 
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @return sample array with sample values
     */
    public float[] getFloatPixel( int x, int y );

    /**
     * Returns a byte sample from coordinate x/y and selected band.
     * 
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @param band
     *            band number for sample
     * @return sample from selected coordinate and band
     */
    public byte getByteSample( int x, int y, int band );

    /**
     * Sets a single byte sample on coordinate x/y and selected band.
     * 
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @param band
     *            band number for sample
     * @param value
     *            new value for sample
     */
    public void setByteSample( int x, int y, int band, byte value );

    /**
     * Returns a float sample from coordinate x/y and selected band.
     * 
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @param band
     *            band number for sample
     * @return sample from selected coordinate and band
     */
    public float getFloatSample( int x, int y, int band );

    /**
     * Sets a single float sample on coordinate x/y and selected band.
     * 
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @param band
     *            band number for sample
     * @param value
     *            new value for sample
     */
    public void setFloatSample( int x, int y, int band, float value );

    /**
     * Returns a 2-D byte array for the specified rectangle.
     * 
     * When the raster contains multiple bands, the samples are interleaved with the current interleaving type ({@link #getInterleaveType()}.
     * 
     * @param x
     *            min x coordinate
     * @param y
     *            min y coordinate
     * @param width
     *            size of the rectangle
     * @param height
     *            size of the rectangle
     * @return selected samples
     */
    public byte[][] getBytes( int x, int y, int width, int height );

    /**
     * Returns a 2-D byte array for the specified rectangle.
     * 
     * @param x
     *            min x coordinate
     * @param y
     *            min y coordinate
     * @param width
     *            size of the rectangle
     * @param height
     *            size of the rectangle
     * @param band
     *            selected band
     * @return selected samples
     */
    public byte[][] getBytes( int x, int y, int width, int height, int band );

    /**
     * Sets values from a 2-D byte array in the specified rectangle.
     * 
     * @param x
     *            min x coordinate
     * @param y
     *            min y coordinate
     * @param width
     *            size of the rectangle
     * @param height
     *            size of the rectangle
     * @param band
     *            selected band
     * @param source
     *            float array with the input values
     */
    public void setBytes( int x, int y, int width, int height, int band, byte[][] source );

    /**
     * Returns a 2-D float array for the specified rectangle.
     * 
     * When the raster contains multiple bands, the samples are interleaved with the current interleaving type ({@link #getInterleaveType()}.
     * 
     * @param x
     *            min x coordinate
     * @param y
     *            min y coordinate
     * @param width
     *            size of the rectangle
     * @param height
     *            size of the rectangle
     * @return selected samples
     */
    public float[][] getFloats( int x, int y, int width, int height );

    /**
     * Returns a 2-D float array for the specified rectangle.
     * 
     * @param x
     *            min x coordinate
     * @param y
     *            min y coordinate
     * @param width
     *            size of the rectangle
     * @param height
     *            size of the rectangle
     * @param band
     *            selected band
     * @return selected samples
     */
    public float[][] getFloats( int x, int y, int width, int height, int band );

    /**
     * Sets values from a 2-D float array in the specified rectangle.
     * 
     * @param x
     *            min x coordinate
     * @param y
     *            min y coordinate
     * @param width
     *            size of the rectangle
     * @param height
     *            size of the rectangle
     * @param band
     *            selected band
     * @param source
     *            float array with the input values
     */
    public void setFloats( int x, int y, int width, int height, int band, float[][] source );

    /**
     * Returns new RasterData object for the specified rectangle The result may share the data with the source raster.
     * 
     * @param x
     *            min x coordinate
     * @param y
     *            min y coordinate
     * @param width
     *            size of the rectangle
     * @param height
     *            size of the rectangle
     * @return selected rectangle
     */
    public RasterData getSubset( int x, int y, int width, int height );

    /**
     * Returns new RasterData object for the specified rectangle The result may share the data with the source raster.
     * 
     * @param rasterRect
     *            rectangle for subset
     * @return selected subset
     */
    public RasterData getSubset( RasterRect rasterRect );

    public RasterData getSubset( int outWidth, int outHeight );

    /**
     * Returns new single-band RasterData object for the specified rectangle The result may share the data with the
     * source raster.
     * 
     * @param x
     *            min x coordinate
     * @param y
     *            min y coordinate
     * @param width
     *            size of the rectangle
     * @param height
     *            size of the rectangle
     * @param band
     *            selected band
     * @return selected rectangle
     */
    public RasterData getSubset( int x, int y, int width, int height, int band );

    /**
     * Sets the raster with values from sourceRaster. To limit the width and height use getSubset on the sourceRaster.
     * 
     * @param x
     *            min x coordinate
     * @param y
     *            min y coordinate
     * @param sourceRaster
     *            data source to copy
     */
    public void setSubset( int x, int y, RasterData sourceRaster );

    /**
     * Sets a single band of the raster with values from the first band of the sourceRaster.
     * 
     * To limit the width and height or to select another band use getSubset on the sourceRaster.
     * 
     * @param x
     *            min x coordinate
     * @param y
     *            min y coordinate
     * @param sourceRaster
     *            data source to copy
     */
    public void setSubset( int x, int y, int band, RasterData sourceRaster );

}
