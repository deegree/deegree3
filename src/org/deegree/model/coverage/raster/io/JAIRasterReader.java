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

import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.media.jai.JAI;

import org.deegree.model.coverage.raster.RasterReader;
import org.deegree.model.coverage.raster.data.ByteBufferRasterData;
import org.deegree.model.coverage.raster.data.DataType;
import org.deegree.model.coverage.raster.data.PixelInterleavedRasterData;

/**
 * This class implements a simple reader for raster files.
 * 
 * It is based on Java Advanced Imaging and it returns RasterData objects.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 * 
 */
public class JAIRasterReader implements RasterReader {
    private String filename;

    private RenderedImage img = null;

    private int width = -1;

    private int height = -1;

    /**
     * Create a JAIRasterReader for given file
     * 
     * @param filename
     *            file to read
     */
    public JAIRasterReader( String filename ) {
        this.filename = filename;
    }

    /**
     * Reads data and returns a new RasterData object
     * 
     * @return new RasterData
     */
    public ByteBufferRasterData read() {
        openImageFile();
        getHeight();
        getWidth(); // cache size

        RenderedImage img = this.img;
        this.img = null; // free img
        return JAIRasterReader.rasterDataFromImage( img );

    }

    /**
     * Retruns the width of the raster associated with the reader
     * 
     * @return raster width
     */
    public int getWidth() {
        if ( width == -1 ) {
            openImageFile();
            width = img.getWidth();
        }
        return width;
    }

    /**
     * Retruns the height of the raster associated with the reader
     * 
     * @return raster height
     */
    public int getHeight() {
        if ( height == -1 ) {
            openImageFile();
            height = img.getHeight();
        }
        return height;
    }

    public void close() {
        img = null;
    }

    private void openImageFile() {
        if ( img == null ) {
            img = JAIRasterReader.openImageFile( filename );
        }
    }

    private static RenderedImage openImageFile( String filename ) {
        return (RenderedImage) JAI.create( "fileload", filename );
    }

    /**
     * Reads data from given file and returns a new RasterData object.
     * 
     * @param filename
     *            file to read
     * @return new RasterData
     */
    public static ByteBufferRasterData rasterDataFromImageFile( String filename ) {
        RenderedImage img = openImageFile( filename );
        return rasterDataFromImage( img );
    }

    private static ByteBufferRasterData rasterDataFromImage( RenderedImage img ) {
        ByteBufferRasterData result = null;

        Raster raster = img.getData();

        int x = 0, y = 0;
        int width = raster.getWidth();
        int height = raster.getHeight();

        int bands = raster.getNumBands();

        DataType type = DataType.fromDataBufferType( raster.getSampleModel().getDataType() );
        result = new PixelInterleavedRasterData( width, height, bands, type );

        if ( type == DataType.BYTE ) {
            result.getByteBuffer().put( (byte[]) raster.getDataElements( x, y, width, height, null ) );
        } else if ( type == DataType.FLOAT ) {
            FloatBuffer buf = result.getByteBuffer().asFloatBuffer();
            buf.put( (float[]) raster.getDataElements( x, y, width, height, null ) );
        } else if ( type == DataType.USHORT ) {
            ShortBuffer buf = result.getByteBuffer().asShortBuffer();
            buf.put( (short[]) raster.getDataElements( x, y, width, height, null ) );
        } else {
            throw new UnsupportedOperationException( "DataType not supported (" + type + ")" );
        }

        return result;
    }
}