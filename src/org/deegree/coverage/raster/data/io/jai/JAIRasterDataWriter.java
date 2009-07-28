//$HeadURL:svn+ssh://otonnhofer@svn.wald.intevation.org/deegree/deegree3/model/trunk/src/org/deegree/model/coverage/raster/implementation/io/JAIRasterWriter.java $
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
package org.deegree.coverage.raster.data.io.jai;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.media.jai.DataBufferFloat;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;

import org.deegree.coverage.raster.data.DataType;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.data.nio.PixelInterleavedRasterData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a simple writer for raster files.
 *
 * It is based on Java Advanced Imaging and saves RasterData objects as a raster image.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author:otonnhofer $
 *
 * @version $Revision:10872 $, $Date:2008-04-01 15:41:48 +0200 (Tue, 01 Apr 2008) $
 */
public class JAIRasterDataWriter {

    private static Logger LOG = LoggerFactory.getLogger( JAIRasterDataWriter.class );

    /**
     * Saves a RasterData to TIFF file
     *
     * @param rasterData
     *            RasterData to save
     * @param filename
     *            filename for output raster image
     */
    public static void saveRasterDataToFile( RasterData rasterData, String filename ) {
        JAIRasterDataWriter.saveRasterDataToFile( rasterData, filename, "TIFF" );
    }

    /**
     * Saves a RasterData to file.
     *
     * The format must be supported by JAI (i.e. BMP, JPEG, PNG, PNM, TIFF)
     *
     * @param sourceRaster
     *            RasterData to save
     * @param filename
     *            filename for output raster image
     * @param format
     *            format for output raster
     */
    public static void saveRasterDataToFile( RasterData sourceRaster, String filename, String format ) {
        BufferedImage result = rasterDataToImage( sourceRaster );
        try {
            // jai_imageio registers ImageWrite with support for more formats (eg. JPEG 2000)
            LOG.debug( "trying to write: " + filename + " with format: " + format );
            JAI.create( "ImageWrite", result, filename, format, null );
        } catch ( IllegalArgumentException ex ) {
            // else use build in "filestore"
            JAI.create( "filestore", result, filename, format, null );
        }
    }

    /**
     * Saves a RasterData to stream.
     *
     * The format must be supported by JAI (i.e. BMP, JPEG, PNG, PNM, TIFF)
     *
     * @param sourceRaster
     *            RasterData to save
     * @param stream
     *            stream for output raster image
     * @param format
     *            format for output raster
     */
    public static void saveRasterDataToStream( RasterData sourceRaster, OutputStream stream, String format ) {
        BufferedImage result = rasterDataToImage( sourceRaster );
        try {
            // jai_imageio registers ImageWrite with support for more formats (eg. JPEG 2000)
            LOG.debug( "trying to write to stream with format: " + format );
            JAI.create( "ImageWrite", result, stream, format, null );
        } catch ( IllegalArgumentException ex ) {
            // else use build in "filestore"
            JAI.create( "encode", result, stream, format, null );
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
                src.position( raster.calculatePos( 0, i ) );
                src.get( buf );
                outputRaster.setDataElements( 0, i, width, 1, buf );
            }
            result = new BufferedImage( width, height, outputType );
            result.setData( outputRaster );
        } else if ( type == DataType.FLOAT && bands == 1 ) {
            DataBuffer dataBuffer = new DataBufferFloat( width * height );
            sm = RasterFactory.createBandedSampleModel( DataBuffer.TYPE_FLOAT, width, height, 1 );
            ColorModel cm = PlanarImage.createColorModel( sm );
            outputRaster = RasterFactory.createWritableRaster( sm, dataBuffer, null );

            float[] buf = new float[width];
            raster.getByteBuffer().rewind();
            FloatBuffer src = raster.getByteBuffer().asFloatBuffer();
            for ( int i = 0; i < height; i++ ) {
                src.position( raster.calculateViewPos( 0, i ) );
                src.get( buf );
                outputRaster.setDataElements( 0, i, width, 1, buf );
            }

            result = new BufferedImage( cm, outputRaster, false, null );

        } else if ( type == DataType.USHORT ) {
            throw new UnsupportedOperationException( "DataType not supported (" + type + ")" );
            // TODO
            // short[] buf = new short[width];
            // raster.getByteBuffer().rewind();
            // ShortBuffer src = raster.getByteBuffer().asShortBuffer();
            // for (int i = 0; i < height; i++) {
            // src.position(raster.calculateViewPos(0, i));
            // src.get(buf);
            // outputRaster.setDataElements(0, i, width, 1, buf);
            // }
            // outputType = BufferedImage.TYPE_USHORT_GRAY;
        } else {
            throw new UnsupportedOperationException( "DataType not supported (" + type + ")" );
        }

        return result;
    }
}
