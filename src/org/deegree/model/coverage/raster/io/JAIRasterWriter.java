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
package org.deegree.model.coverage.raster.io;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.media.jai.DataBufferFloat;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;

import org.deegree.model.coverage.raster.data.ByteBufferRasterData;
import org.deegree.model.coverage.raster.data.DataType;
import org.deegree.model.coverage.raster.data.PixelInterleavedRasterData;
import org.deegree.model.coverage.raster.data.RasterData;

/**
 * This class implements a simple writer for raster files.
 * 
 * It is based on Java Advanced Imaging and saves RasterData objects as a raster image.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class JAIRasterWriter {

    /**
     * Saves a RasterData to TIFF file
     * 
     * @param rasterData
     *            RasterData to save
     * @param filename
     *            filename for output raster image
     */
    public static void rasterDataToImage( RasterData rasterData, String filename ) {
        JAIRasterWriter.rasterDataToImage( rasterData, filename, "TIFF" );
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
    public static void rasterDataToImage( RasterData sourceRaster, String filename, String format ) {

        ByteBufferRasterData raster = null;

        if ( sourceRaster instanceof ByteBufferRasterData ) {
            raster = ( (ByteBufferRasterData) sourceRaster );
        } else {
            throw new UnsupportedOperationException( "RasterData is not a ByteBufferBasedRaster" );
        }
        if ( raster instanceof PixelInterleavedRasterData ) {
            ;
        } else {
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
            if ( raster.isSubset() ) {
                throw new UnsupportedOperationException();
            }
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

        JAI.create( "filestore", result, filename, format, null );
    }
}
