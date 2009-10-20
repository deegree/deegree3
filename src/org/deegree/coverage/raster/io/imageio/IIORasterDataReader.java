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

import java.awt.image.BufferedImage;
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

    // io handles
    private File file;

    private InputStream inputStream;

    /* true if an mark supported input stream is the datasource */
    private final boolean resetableStream;

    private int width = -1;

    private int height = -1;

    private IIOMetadata metaData;

    private String format;

    private static Logger LOG = LoggerFactory.getLogger( IIORasterDataReader.class );

    private ImageReader reader;

    // flags which define if a read failed.
    private boolean retrievalOfReadersFailed = false;

    private boolean metadataReadFailed = false;

    private boolean heightReadFailed = false;

    private boolean widthReadFailed = false;

    private boolean imageReadFailed = false;

    /**
     * Create a IIORasterDataReader for given file
     * 
     * @param file
     *            file to read
     * @param format
     */
    public IIORasterDataReader( File file, String format ) {
        this.file = file;
        this.format = format;
        resetableStream = false;
    }

    /**
     * Create a IIORasterDataReader for given stream
     * 
     * @param stream
     *            stream to read
     * @param format
     */
    public IIORasterDataReader( InputStream stream, String format ) {
        this.inputStream = stream;
        this.resetableStream = ( inputStream != null && inputStream.markSupported() );
        this.format = format;
    }

    /**
     * Reads data and returns a new RasterData object
     * 
     * @return new RasterData
     */
    public ByteBufferRasterData read() {
        if ( !imageReadFailed && findReaderForIO() ) {
            try {
                BufferedImage img = reader.read( 0 );
                resetStream();
                return rasterDataFromImage( img );
            } catch ( IOException e ) {
                LOG.error( "couldn't open image:" + e.getMessage(), e );
                this.imageReadFailed = true;
            }
        }
        return null;
    }

    /**
     * 
     */
    private void resetStream() {
        // if ( resetableStream ) {
        // try {
        // inputStream.reset();
        // } catch ( IOException e ) {
        // // could not reset, but this should not happen.
        // }
        // }
        // if ( reader != null && reader.getInput() != null ) {
        // try {
        // ( (ImageInputStream) reader.getInput() ).seek( 0 );
        // } catch ( IOException e ) {
        // // could not reset.
        // }
        // }
    }

    /**
     * Returns the height of the raster associated with the reader
     * 
     * @return raster height
     */
    public int getWidth() {
        if ( width == -1 && !widthReadFailed && findReaderForIO() ) {
            try {
                width = reader.getWidth( 0 );
            } catch ( IOException e ) {
                LOG.debug( "couldn't open image for width:" + e.getMessage(), e );
                this.widthReadFailed = true;
            }
            resetStream();
        }
        return width;
    }

    /**
     * Returns the height of the raster associated with the reader
     * 
     * @return raster height
     */
    public int getHeight() {
        if ( height == -1 && !heightReadFailed && findReaderForIO() ) {
            try {
                height = reader.getHeight( 0 );
            } catch ( IOException e ) {
                LOG.debug( "couldn't open image for height:" + e.getMessage(), e );
                this.heightReadFailed = true;
            }
            resetStream();
        }
        return height;
    }

    /**
     * @return the raw metadata of the raster
     */
    IIOMetadata getMetaData() {
        // md == null,
        // 1. didn't read the metadata
        // 2. the reader couldn't read it, just do not try again.
        // 3.
        if ( metaData == null && !metadataReadFailed && findReaderForIO() ) {
            try {
                metaData = reader.getImageMetadata( 0 );
            } catch ( IOException e ) {
                LOG.debug( "couldn't open metadata:" + e.getMessage(), e );
                this.metadataReadFailed = true;
            }
            resetStream();
        }
        return metaData;
    }

    /**
     * Removes the internal references to the loaded raster to allow garbage collection of the raster.
     */
    public void close() {
        if ( reader != null && reader.getInput() != null ) {
            try {
                ( (ImageInputStream) reader.getInput() ).close();
            } catch ( IOException e ) {
                LOG.debug( "Could not close the imagestream, ignoring.", e );
            }
        }
    }

    /**
     * Create an Imagereader from the file or the inputstream.
     * 
     * @return true if the creation of the image reader was successful.
     */
    private boolean findReaderForIO() {
        if ( this.reader == null && !retrievalOfReadersFailed ) {
            try {
                ImageInputStream iis = null;
                if ( file != null ) {
                    iis = ImageIO.createImageInputStream( file );
                } else {
                    if ( resetableStream ) {
                        inputStream.mark( Integer.MAX_VALUE );
                    }
                    iis = ImageIO.createImageInputStream( inputStream );
                }
                Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName( format );
                if ( iter.hasNext() ) {
                    // use the first.
                    this.reader = iter.next();
                    reader.setInput( iis );
                    // done creating a reader.
                    return true;
                }
                LOG.error( "couldn't find ImageReader" );
                this.retrievalOfReadersFailed = true;
            } catch ( IOException e ) {
                LOG.debug( "Could not open an ImageStream for "
                           + ( ( file != null ) ? "file: " + file.getAbsolutePath() : "stream " ) + ", because: "
                           + e.getLocalizedMessage(), e );
                this.retrievalOfReadersFailed = true;
            }
        }
        return ( this.reader != null && !retrievalOfReadersFailed );
    }
}
