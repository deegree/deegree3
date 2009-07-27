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
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.deegree.coverage.raster.data.BandType;
import org.deegree.coverage.raster.data.DataType;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.io.RasterDataReader;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.data.nio.PixelInterleavedRasterData;
import org.deegree.coverage.raster.geom.RasterRect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class IIORasterDataReader implements RasterDataReader {

    private File file;

    private RenderedImage img = null;

    private int width = -1;

    private int height = -1;

    private IIOMetadata metaData;

    private boolean metaDataRead = false;

    private String format;

    private InputStream inputStream;

    private static Logger LOG = LoggerFactory.getLogger( IIORasterDataReader.class );

    private boolean errorOnLoading = false;

    /**
     * Create a JAIRasterReader for given file
     *
     * @param file
     *            file to read
     * @param format
     */
    public IIORasterDataReader( File file, String format ) {
        this.file = file;
        this.format = format;
    }

    /**
     * Create a JAIRasterReader for given stream
     *
     * @param stream
     *            stream to read
     * @param format
     */
    public IIORasterDataReader( InputStream stream, String format ) {
        this.inputStream = stream;
        this.format = format;
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
        return IIORasterDataReader.rasterDataFromImage( img );
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
     * @return the raw metadata of the raster
     */
    IIOMetadata getMetaData() {
        if ( !metaDataRead ) {
            openImageFile();
        }
        return metaData;
    }

    /**
     * Removes the internal references to the loaded raster to allow garbage collection of the raster.
     */
    void close() {
        img = null;
    }

    private void openImageFile() {
        if ( img == null && !errorOnLoading ) {
            try {
                openImageStream( imageStreamFromFileOrStream() );
            } catch ( IOException e ) {
                LOG.error( "could't open image from "
                           + ( ( file != null ) ? "file: " + file.getAbsolutePath() : "stream " ) + ", because: "
                           + e.getMessage() );
                // e.printStackTrace();
                errorOnLoading = true;
            }
        }
    }

    private ImageInputStream imageStreamFromFileOrStream()
                            throws IOException {
        if ( file != null ) {
            return ImageIO.createImageInputStream( file );
        }
        return ImageIO.createImageInputStream( inputStream );
    }

    private void openImageStream( ImageInputStream stream )
                            throws IOException {
        if ( stream == null ) {
            throw new IOException( "Imagestream was null." );
        }
        Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName( format );
        if ( !iter.hasNext() ) {
            LOG.error( "couldn't find ImageReader" );
            return;
        }
        ImageReader reader = iter.next();
        try {
            reader.setInput( stream );
            try {
                metaDataRead = true;
                metaData = reader.getImageMetadata( 0 );
            } catch ( IOException e ) {
                LOG.error( "couldn't open metadata:" + e.getMessage() );
            }
            // com.sun.imageio.plugins.png.PNGMetadata md = (com.sun.imageio.plugins.png.PNGMetadata) metaData;
            img = reader.read( 0 );
        } catch ( IOException e ) {
            LOG.error( "couldn't open image:" + e.getMessage() );
        }

    }

    /**
     * @param img
     * @return the rasterdata object from the image or <code>null</code> if the given img is <code>null</code>
     */
    public static ByteBufferRasterData rasterDataFromImage( RenderedImage img ) {
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

    // /**
    // * @param first
    // * @return
    // */
    // public static RasterData rasterDataFromImage( BufferedImage first ) {
    // return rasterDataFromImage( first, null );
    // }
}
