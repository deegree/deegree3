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
package org.deegree.coverage.raster.io.imageio;

import static org.deegree.coverage.raster.utils.RasterFactory.rasterDataFromImage;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.io.RasterDataReader;
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
        if ( img == null ) {
            openImageFile();
            getHeight();
            getWidth(); // cache size
        }

        RenderedImage img = this.img;
        this.img = null; // remove reference to img
        return rasterDataFromImage( img );
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
    public void close() {
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

    // /**
    // * @param first
    // * @return
    // */
    // public static RasterData rasterDataFromImage( BufferedImage first ) {
    // return rasterDataFromImage( first, null );
    // }
}
