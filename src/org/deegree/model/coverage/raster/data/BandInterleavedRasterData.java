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
import java.nio.FloatBuffer;

/**
 * This class implements a band-interleaved, ByteBuffer-based RasterData.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class BandInterleavedRasterData extends ByteBufferRasterData {

    /**
     * Creates a new BandInterleavedRasterData with given size, number of bands and data type
     * 
     * @param width
     *            width of new raster
     * @param height
     *            height of new raster
     * @param bands
     *            number of bands
     * @param type
     *            DataType of raster samples
     */
    public BandInterleavedRasterData( int width, int height, int bands, DataType type ) {
        super( width, height, bands, type );
    }

    /**
     * Create a new {@link BandInterleavedRasterData} that represents a subset of a larger
     * {@link BandInterleavedRasterData}.
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
    protected BandInterleavedRasterData( int x0, int y0, int subWidth, int subHeight, int width, int height, int bands,
                                         DataType dataType, ByteBuffer data ) {
        super( x0, y0, subWidth, subHeight, width, height, bands, dataType, data );
    }

    @Override
    public BandInterleavedRasterData createCompatibleRasterData( int width, int height, int bands ) {
        return new BandInterleavedRasterData( width, height, bands, this.dataType );
    }

    @Override
    public final int getBandStride() {
        return width * getPixelStride() * height;
    }

    @Override
    public final int getLineStride() {
        return width * getPixelStride();
    }

    @Override
    public final int getPixelStride() {
        return dataType.getSize();
    }

    @Override
    public final InterleaveType getInterleaveType() {
        return InterleaveType.BAND;
    }

    @Override
    public BandInterleavedRasterData getSubset( int x0, int y0, int width, int height ) {
        int w = Math.min( width, subWidth - x0 );
        int h = Math.min( height, subHeight - y0 );
        checkBounds( x0, y0, width, height );
        BandInterleavedRasterData result = createCompatibleRasterData( w, h, bands );
        BandInterleavedRasterData subset = new BandInterleavedRasterData( this.x0 + x0, this.y0 + y0, w, h, this.width,
                                                                          this.height, this.bands, this.dataType,
                                                                          this.data );
        result.setSubset( 0, 0, subset );
        return result;
    }

    @Override
    public byte[][] getBytes( int x, int y, int width, int height, int band ) {
        byte[][] result = new byte[height][width];

        checkBounds( x, y, width, height );

        for ( int i = 0; i < height; i++ ) {
            data.position( calculatePos( x, y + i, band ) );
            data.get( result[i], 0, result[i].length );
        }
        return result;
    }

    @Override
    public byte[][] getBytes( int x, int y, int width, int height ) {
        byte[][] result = new byte[height * bands][width];

        checkBounds( x, y, width, height );

        for ( int b = 0; b < bands; b++ ) {
            for ( int i = 0; i < height; i++ ) {
                data.position( calculatePos( x, y + i, b ) );
                data.get( result[i + height * b], 0, result[i].length );
            }
        }
        return result;
    }

    @Override
    public float[][] getFloats( int x, int y, int width, int height, int band ) {
        float[][] result = new float[height][width];
        FloatBuffer bufView = data.asFloatBuffer();

        checkBounds( x, y, width, height );

        for ( int i = 0; i < height; i++ ) {
            bufView.position( calculateViewPos( x, y + i, band ) );
            bufView.get( result[i], 0, result[i].length );
        }
        return result;
    }

    @Override
    public float[][] getFloats( int x, int y, int width, int height ) {
        float[][] result = new float[height * bands][width];
        FloatBuffer bufView = data.asFloatBuffer();

        checkBounds( x, y, width, height );

        for ( int b = 0; b < bands; b++ ) {
            for ( int i = 0; i < height; i++ ) {
                bufView.position( calculateViewPos( x, y + i, b ) );
                bufView.get( result[i + height * b], 0, result[i].length );
            }
        }
        return result;
    }

    @Override
    public byte[][] getBytes( int x, int y, int width, int height, InterleaveType interleaving ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float[][] getFloats( int x, int y, int width, int height, InterleaveType interleaving ) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#setBytes(int, int, int, int, int, byte[][])
     */
    public void setBytes( int x, int y, int width, int height, int band, byte[][] source ) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see raster.org.deegree.model.raster.RasterData#setFloats(int, int, int, int, int, float[][])
     */
    public void setFloats( int x, int y, int width, int height, int band, float[][] source ) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.coverage.raster.data.RasterData#getSubset(int, int)
     */
    public RasterData getSubset( int outWidth, int outHeight ) {
        throw new UnsupportedOperationException();
    }

}
