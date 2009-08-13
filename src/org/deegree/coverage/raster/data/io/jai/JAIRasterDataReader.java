//$HeadURL:svn+ssh://otonnhofer@svn.wald.intevation.org/deegree/deegree3/model/trunk/src/org/deegree/model/coverage/raster/implementation/io/JAIRasterReader.java $
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
import java.awt.image.ComponentColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Hashtable;

import javax.media.jai.JAI;

import org.deegree.coverage.raster.data.BandType;
import org.deegree.coverage.raster.data.DataType;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.io.RasterDataReader;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.data.nio.PixelInterleavedRasterData;
import org.deegree.coverage.raster.geom.RasterRect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.SeekableStream;

/**
 * This class implements a simple reader for raster files.
 * 
 * It is based on Java Advanced Imaging and it returns RasterData objects.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author:otonnhofer $
 * 
 * @version $Revision:10872 $, $Date:2008-04-01 15:41:48 +0200 (Tue, 01 Apr 2008) $
 * 
 */
public class JAIRasterDataReader implements RasterDataReader {

    private static final Logger LOG = LoggerFactory.getLogger( JAIRasterDataReader.class );

    private File file;

    private InputStream inputStream;

    private RenderedImage img = null;

    private int width = -1;

    private int height = -1;

    private boolean errorOnLoading = false;

    /**
     * Create a JAIRasterReader for given file
     * 
     * @param file
     *            file to read
     */
    public JAIRasterDataReader( File file ) {
        this.file = file;
    }

    /**
     * Create a JAIRasterReader for given stream
     * 
     * @param stream
     *            stream to read
     */
    public JAIRasterDataReader( InputStream stream ) {
        this.inputStream = stream;
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
        this.img = null; // remove reference to img
        return JAIRasterDataReader.rasterDataFromImage( img );

    }

    /**
     * Retruns the width of the raster associated with the reader
     * 
     * @return raster width
     */
    public int getWidth() {
        if ( width == -1 ) {
            openImageFile();
            if ( !errorOnLoading ) {
                width = img.getWidth();
            }
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
            if ( !errorOnLoading ) {
                height = img.getHeight();
            }
        }
        return height;
    }

    /**
     * Removes the internal references to the loaded raster to allow garbage collection of the raster.
     */
    public void close() {
        img = null;
    }

    private void openImageFile() {
        if ( img == null ) {
            try {
                img = openImageStream( imageStreamFromFileOrStream() );
            } catch ( IOException e ) {
                LOG.error( "could't open image from "
                           + ( ( file != null ) ? "file: " + file.getAbsolutePath() : "stream " ) + ", because: "
                           + e.getMessage() );
                // e.printStackTrace();
                errorOnLoading = true;
            }
        }
    }

    private SeekableStream imageStreamFromFileOrStream()
                            throws IOException {
        if ( file != null ) {
            return new FileSeekableStream( file );
        }
        return SeekableStream.wrapInputStream( inputStream, false );
    }

    private static RenderedImage openImageStream( SeekableStream stream )
                            throws IOException {
        if ( stream == null ) {
            throw new IOException( "Imagestream was null." );
        }
        try {
            // jai_imageio registers ImageRead with support for more formats (eg. JPEG 2000)
            return JAI.create( "ImageRead", stream );
        } catch ( IllegalArgumentException ex ) {
            // else use build in "fileload"
            return JAI.create( "stream", stream );
        }
    }

    private static ByteBufferRasterData rasterDataFromImage( RenderedImage img ) {
        ByteBufferRasterData result = null;
        if ( img != null ) {
            Raster raster = img.getData();

            int x = 0, y = 0;
            int width = raster.getWidth();
            int height = raster.getHeight();

            BandType[] bandTypes = determineBandTypes( img );

            DataType type = DataType.fromDataBufferType( raster.getSampleModel().getDataType() );
            result = new PixelInterleavedRasterData( new RasterRect( 0, 0, width, height ), width, height, bandTypes,
                                                     type );

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
        }
        return result;
    }

    /**
     * @param img
     *            to get the bands for.
     * @return
     */
    private static BandType[] determineBandTypes( RenderedImage img ) {

        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        String[] keys = img.getPropertyNames();
        if ( keys != null ) {
            for ( int i = 0; i < keys.length; i++ ) {
                properties.put( keys[i], img.getProperty( keys[i] ) );
            }
        }
        ColorModel cm = img.getColorModel();
        WritableRaster raster = cm.createCompatibleWritableRaster( (byte) 1, (byte) 1 );
        int imageType = new BufferedImage( cm, raster, cm.isAlphaPremultiplied(), properties ).getType();

        if ( imageType == BufferedImage.TYPE_CUSTOM ) {
            int numBands = raster.getNumBands();
            // try a little more
            if ( ( cm instanceof ComponentColorModel )
                 && ( raster.getSampleModel() instanceof PixelInterleavedSampleModel ) ) {
                PixelInterleavedSampleModel csm = (PixelInterleavedSampleModel) raster.getSampleModel();
                int[] nBits = ( (ComponentColorModel) cm ).getComponentSize();
                boolean is8bit = true;
                for ( int i = 0; i < numBands; i++ ) {
                    if ( nBits[i] != 8 ) {
                        is8bit = false;
                        break;
                    }
                }
                if ( is8bit ) {
                    int[] offs = csm.getBandOffsets();
                    if ( numBands == 3 ) {
                        if ( offs[0] == numBands - 3 && offs[1] == numBands - 2 && offs[2] == numBands - 1 ) {
                            imageType = RasterData.TYPE_BYTE_RGB;
                        }
                    } else if ( numBands == 4 ) {
                        if ( offs[0] == numBands - 4 && offs[1] == numBands - 3 && offs[2] == numBands - 2
                             && offs[3] == numBands - 1 ) {
                            imageType = RasterData.TYPE_BYTE_RGBA;
                        }
                    }
                }
            }
        }
        return BandType.fromBufferedImageType( imageType, raster.getNumBands() );
    }
}
