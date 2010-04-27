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
package org.deegree.coverage.raster.io.jai;

import static org.deegree.coverage.raster.utils.RasterFactory.rasterDataFromImage;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.media.jai.JAI;

import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.io.RasterDataReader;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.SeekableStream;

/**
 * This class implements a simple reader for raster files.
 * 
 * It is based on Java Advanced Imaging and it returns RasterData objects.
 * 
 * rb: This class needs heavy refactoring, maybe enable tiling for supporting files, than streams would not work any
 * more, but using the renderOp would.
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

    private RasterIOOptions options;

    /**
     * Create a JAIRasterReader for given file
     * 
     * @param file
     *            file to read
     * @param options
     *            holding information
     */
    public JAIRasterDataReader( File file, RasterIOOptions options ) {
        this( options );
        this.file = file;
    }

    /**
     * Create a JAIRasterReader for given stream
     * 
     * @param stream
     *            stream to read
     * @param options
     *            holding information
     */
    public JAIRasterDataReader( InputStream stream, RasterIOOptions options ) {
        this( options );
        this.inputStream = stream;
    }

    /**
     * @param options
     */
    private JAIRasterDataReader( RasterIOOptions options ) {
        this.options = options;
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
        return rasterDataFromImage( img, options );

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

    /**
     * 
     * @return the file containing the data.
     */
    public File file() {
        return file;
    }

    /**
     * 
     */
    public void dispose() {
        // nothing to do.
    }

    /**
     * @return the raster data info.
     */
    public RasterDataInfo getRasterDataInfo() {
        throw new UnsupportedOperationException( "Getting rdi is currently not supported." );
    }

}
