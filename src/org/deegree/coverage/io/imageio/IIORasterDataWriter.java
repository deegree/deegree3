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
package org.deegree.coverage.io.imageio;

import java.awt.image.BufferedImage;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.deegree.coverage.raster.data.DataType;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.data.nio.PixelInterleavedRasterData;

/**
 *
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class IIORasterDataWriter {

    /**
     * Saves a RasterData to file.
     *
     * The format must be supported by JAI (i.e. BMP, JPEG, PNG, PNM, TIFF)
     *
     * @param sourceRaster
     *            RasterData to save
     * @param file
     *            file for output raster image
     * @param format
     *            format for output raster
     * @throws IOException
     */
    public static void saveRasterDataToFile( RasterData sourceRaster, File file, String format )
                            throws IOException {
        BufferedImage result = rasterDataToImage( sourceRaster );
        boolean written = ImageIO.write( result, format, file );
        if ( !written ) {
            throw new IOException( "could't find ImageIO writer for " + format );
        }
    }

    /**
     * Saves a RasterData to stream.
     *
     * The format must be supported by JAI (i.e. BMP, JPEG, PNG, PNM, TIFF)
     *
     * @param sourceRaster
     *            RasterData to save
     * @param out
     *            stream for output raster image
     * @param format
     *            format for output raster
     * @throws IOException
     */
    public static void saveRasterDataToStream( RasterData sourceRaster, OutputStream out, String format )
                            throws IOException {
        BufferedImage result = rasterDataToImage( sourceRaster );
        boolean written = ImageIO.write( result, format, out );
        out.flush();

        if ( !written ) {
            throw new IOException( "could't find ImageIO writer for " + format );
        }

    }

    /**
     * Convert RasterData into a BufferedImage
     *
     * @param sourceRaster
     *            the source RasterData
     * @return a BufferedImage
     */
    public static BufferedImage rasterDataToImage( RasterData sourceRaster ) {

        ByteBufferRasterData raster = null;

        if ( !( sourceRaster instanceof ByteBufferRasterData ) ) {
            throw new UnsupportedOperationException( "RasterData is not a ByteBufferBasedRaster" );
        }

        raster = ( (ByteBufferRasterData) sourceRaster );
        if ( !( raster instanceof PixelInterleavedRasterData ) ) {
            throw new UnsupportedOperationException( "RasterData is not pixel interleaved" );
        }

        int bands = raster.getBands();
        int bandOffset = raster.getBandStride();

        int[] bandOffsets = new int[bands];
        for ( int i = 0; i < bandOffsets.length; i++ ) {
            bandOffsets[i] = bandOffset * i;
        }

        int width = raster.getWidth();
        int height = raster.getHeight();
        DataType type = raster.getDataType();

        SampleModel sm = new PixelInterleavedSampleModel( DataType.toDataBufferType( type ), width, height,
                                                          raster.getPixelStride(), raster.getLineStride(), bandOffsets );

        WritableRaster outputRaster = Raster.createWritableRaster( sm, null );
        BufferedImage result = null;

        if ( type == DataType.BYTE ) {
            int outputType;
            if ( bands == 1 ) {
                outputType = BufferedImage.TYPE_BYTE_GRAY;
            } else if ( bands == 3 ) {
                outputType = BufferedImage.TYPE_3BYTE_BGR;
            } else if ( bands == 4 ) {
                outputType = BufferedImage.TYPE_4BYTE_ABGR;
            } else {
                throw new UnsupportedOperationException( "Could not save raster with " + bands + "  bands" );
            }

            byte[] buf = new byte[raster.getPixelStride() * width];
            ByteBuffer src = raster.getByteBuffer();
            for ( int i = 0; i < height; i++ ) {
                int pos = raster.calculatePos( 0, i );
                src.position( pos );
                src.get( buf );
                outputRaster.setDataElements( 0, i, width, 1, buf );
            }
            result = new BufferedImage( width, height, outputType );
            result.setData( outputRaster );
        } else {
            throw new UnsupportedOperationException( "DataType not supported (" + type + ")" );
        }

        return result;
    }

}
