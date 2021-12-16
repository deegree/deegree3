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

import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReader;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.deegree.coverage.raster.cache.ByteBufferPool;
import org.deegree.coverage.raster.cache.RasterCache;
import org.deegree.coverage.raster.data.container.BufferResult;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.data.info.InterleaveType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.io.RasterDataReader;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.utils.RasterFactory;
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

    private static final boolean CACHE_DISABLED;

    static {
        CACHE_DISABLED = "false".equalsIgnoreCase( System.getProperty( "deegree.raster.cache.iioreader", "true" ));
        if ( !CACHE_DISABLED ) {
            LoggerFactory.getLogger( IIORasterDataReader.class ).info( "IIORaster cache is disabled for file based resources via system property!" );
        }
    }

    // io handles
    private File file;

    private final Object LOCK = new Object();

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

    private boolean rdiReadFailed = false;

    private boolean heightReadFailed = false;

    private boolean widthReadFailed = false;

    private boolean imageReadFailed = false;

    private RasterIOOptions options;

    private RasterDataInfo rdi;

    private final int imageIndex;
    
    private final boolean forceNoCache;

    /**
     * Create a IIORasterDataReader for given file
     * 
     * @param file
     *            file to read
     * @param options
     *            with values.
     */
    public IIORasterDataReader( File file, RasterIOOptions options, int imageIndex ) {
        this( options, false, imageIndex, CACHE_DISABLED || RasterCache.hasNoCacheFile( file, imageIndex ) );
        this.file = file;
    }

    /**
     * Create a IIORasterDataReader for given stream
     * 
     * @param stream
     *            stream to read
     * @param options
     *            with values
     */
    public IIORasterDataReader( InputStream stream, RasterIOOptions options, int imageIndex ) {
        this( options, ( stream != null && stream.markSupported() ), imageIndex, false );
        this.inputStream = stream;
    }

    private IIORasterDataReader( RasterIOOptions options, boolean resetableStream, int imageIndex, boolean forceNoCache ) {
        this.imageIndex = imageIndex;
        this.format = options.get( RasterIOOptions.OPT_FORMAT );
        this.options = options;
        this.resetableStream = resetableStream;
        this.forceNoCache = forceNoCache;
    }

    /**
     * Reads data and returns a new RasterData object
     * 
     * @return new RasterData
     */
    @Override
    public ByteBufferRasterData read() {
        BufferedImage result = null;
        synchronized ( LOCK ) {
            if ( !imageReadFailed && findReaderForIO() ) {
                try {
                    result = reader.read( imageIndex );
                    resetStream();
                    return RasterFactory.rasterDataFromImage( result, options );
                } catch ( IOException e ) {
                    LOG.error( "couldn't open image:" + e.getMessage(), e );
                    this.imageReadFailed = true;
                }
            }
        }
        return rasterDataFromImage( result, options );
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
    public int getColumns() {
        synchronized ( LOCK ) {
            if ( width == -1 && !widthReadFailed && findReaderForIO() ) {
                try {
                    width = reader.getWidth( imageIndex );
                } catch ( IOException e ) {
                    LOG.debug( "couldn't open image for width:" + e.getMessage(), e );
                    this.widthReadFailed = true;
                }
                resetStream();
            }
        }
        return width;
    }

    /**
     * Returns the height of the raster associated with the reader
     * 
     * @return raster height
     */
    public int getRows() {
        synchronized ( LOCK ) {
            if ( height == -1 && !heightReadFailed && findReaderForIO() ) {
                try {
                    height = reader.getHeight( imageIndex );
                } catch ( IOException e ) {
                    LOG.debug( "couldn't open image for height:" + e.getMessage(), e );
                    this.heightReadFailed = true;
                }
                resetStream();
            }
        }
        return height;
    }

    /**
     * @return the raw metadata of the raster
     */
    protected IIOMetadata getMetaData() {
        // md == null,
        // 1. didn't read the metadata
        // 2. the reader couldn't read it, just do not try again.
        // 3.
        synchronized ( LOCK ) {
            if ( metaData == null && !metadataReadFailed && findReaderForIO() ) {
                try {
                    metaData = reader.getImageMetadata( 0 );
                } catch ( IOException e ) {
                    LOG.debug( "couldn't open metadata:" + e.getMessage(), e );
                    this.metadataReadFailed = true;
                }
                resetStream();
            }
        }
        return metaData;
    }

    /**
     * @return the raster data info object describing the data to be read.
     */
    public RasterDataInfo getRasterDataInfo() {
        synchronized ( LOCK ) {
            if ( rdi == null && !rdiReadFailed && findReaderForIO() ) {
                try {
                    Iterator<ImageTypeSpecifier> imageTypes = reader.getImageTypes( imageIndex );
                    while ( imageTypes.hasNext() && rdi == null ) {
                        ImageTypeSpecifier its = imageTypes.next();
                        if ( its != null ) {
                            BufferedImage bi = its.createBufferedImage( 2, 2 );
                            BandType[] bands = BandType.fromBufferedImageType( bi.getType(), its.getNumBands(),
                                                                               bi.getRaster().getSampleModel() );
                            DataType type = DataType.fromDataBufferType( its.getSampleModel().getDataType() );

                            if ( type != DataType.FLOAT && type != DataType.DOUBLE && type != DataType.BYTE
                                 && bands.length > 1 ) {
                                type = DataType.BYTE;
                                boolean alphaLast = ( bands.length == 4 ) && ( bands[3] == BandType.ALPHA );
                                bands[0] = BandType.RED;
                                bands[1] = BandType.GREEN;
                                bands[2] = BandType.BLUE;
                                if ( bands.length == 4 && !alphaLast ) {
                                    bands[3] = BandType.ALPHA;
                                }
                            }
                            rdi = new RasterDataInfo( bands, type, InterleaveType.PIXEL );
                        }
                    }

                } catch ( IOException e ) {
                    LOG.debug( "couldn't create a raster data info object:" + e.getMessage(), e );
                    rdiReadFailed = true;
                }
            }
        }
        return rdi;
    }

    /**
     * Removes the internal references to the loaded raster to allow garbage collection of the raster.
     */
    @Override
    public void close() {
        synchronized ( LOCK ) {
            if ( reader != null && reader.getInput() != null ) {
                try {
                    ( (ImageInputStream) reader.getInput() ).close();
                } catch ( IOException e ) {
                    LOG.debug( "Could not close the imagestream, ignoring.", e );
                }
            }
        }
    }

    /**
     * Create an Imagereader from the file or the inputstream.
     * 
     * @return true if the creation of the image reader was successful.
     */
    private boolean findReaderForIO() {
        synchronized ( LOCK ) {
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
                    while ( iter.hasNext() && !( reader instanceof TIFFImageReader ) ) {
                        this.reader = iter.next();
                    }
                    if ( reader != null ) {
                        reader.setInput( iis );
                        // done creating a reader.
                        return true;
                    }
                    LOG.error( "couldn't find ImageReader" );
                    this.retrievalOfReadersFailed = true;
                } catch ( IOException e ) {
                    LOG.debug( "Could not open an ImageStream for "
                                                       + ( ( file != null ) ? "file: " + file.getAbsolutePath()
                                                                           : "stream " ) + ", because: "
                                                       + e.getLocalizedMessage(), e );
                    this.retrievalOfReadersFailed = true;
                }
            }
            return ( this.reader != null && !retrievalOfReadersFailed );
        }
    }

    /**
     * needed by the raster reader,
     * 
     * @return will return the file if any.
     */
    protected File file() {
        return ( file != null ) ? file : null;
    }

    /**
     * 
     * @return true if the imageio thinks the file can be accessed easily
     */
    boolean shouldCreateCacheFile() {
        if ( forceNoCache ) {
            LOG.debug( "cache for reader with file {} and index {} disabbled by no-cache-file/system-property", file, imageIndex );
            return false;
        }
        
        boolean result = true;
        try {
            synchronized ( LOCK ) {
                if ( findReaderForIO() ) {
                    result = !reader.isRandomAccessEasy( imageIndex );
                }
            }
        } catch ( IOException e ) {
            LOG.debug( "Could not get easy access information from the imagereader, using configured value for using cache: "
                                               + result, e );
        }
        return result;
    }

    /**
     * @param rect
     * @param resultBuffer
     * @return the buffer result containing the result buffer (instantiated if the given one was null) and the rect it
     *         is valid for.
     */
    public BufferResult read( RasterRect rect, ByteBuffer resultBuffer ) {
        BufferResult result = null;
        ImageReadParam rp = new ImageReadParam();
        Rectangle dataRect = new Rectangle( 0, 0, getColumns(), getRows() );
        Rectangle intersection = dataRect.intersection( new Rectangle( rect.x, rect.y, rect.width, rect.height ) );
        if ( intersection.width > 0 && intersection.height > 0 ) {
            rp.setSourceRegion( intersection );
            try {
                BufferedImage img = null;
                synchronized ( LOCK ) {
                    if ( findReaderForIO() ) {
                        img = reader.read( imageIndex, rp );
                    }
                }
                if ( img != null ) {
                    Raster raster = img.getRaster();
                    if ( resultBuffer == null ) {
                        RasterDataInfo rdi = getRasterDataInfo();

                        resultBuffer = ByteBufferPool.allocate( rdi.bands * rdi.dataSize * intersection.width
                                                                * intersection.height, false );
                    }

                    // DataBuffer buffer = raster.getDataBuffer();
                    int imgDataType = raster.getSampleModel().getDataType();
                    DataType type = DataType.fromDataBufferType( imgDataType );
                    RasterFactory.rasterToByteBuffer( raster, 0, 0, img.getWidth(), img.getHeight(), type, resultBuffer );
                    result = new BufferResult( new RasterRect( intersection ), resultBuffer );
                }
            } catch ( IOException e ) {
                LOG.debug( "Could not read the given rect: " + rect + " because: " + e.getLocalizedMessage(), e );
            }
        }
        return result;
    }

    /**
     * @return true if random access is easy for the image io reader.
     */
    public boolean getReadTiles() {
        boolean result = false;
        synchronized ( LOCK ) {
            if ( findReaderForIO() ) {
                try {
                    result = reader != null ? reader.isRandomAccessEasy( imageIndex ) : false;
                } catch ( IOException e ) {
                    // just do nothing.
                }
            }
        }

        return result;
    }

    /**
     * 
     */
    public void dispose() {
        // only close the inputstream if it was read from a file.
        if ( file != null ) {
            synchronized ( LOCK ) {
                if ( reader != null && file != null ) {
                    reader.dispose();
                    if ( reader.getInput() != null ) {
                        Object input = reader.getInput();
                        if ( input instanceof ImageInputStream ) {
                            try {
                                ( (ImageInputStream) input ).close();
                            } catch ( IOException e ) {
                                LOG.debug( "Error while disposing: ", e );
                            }
                        }
                    }
                }
                // set the reader to null.
                reader = null;
            }
        }
    }
}
