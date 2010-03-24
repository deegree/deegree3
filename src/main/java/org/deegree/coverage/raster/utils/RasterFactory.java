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
package org.deegree.coverage.raster.utils;

import static java.awt.image.BufferedImage.TYPE_USHORT_GRAY;

import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.ServiceLoader;
import java.util.Set;

import org.deegree.commons.utils.FileUtils;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.cache.ByteBufferPool;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.RasterDataFactory;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.data.info.InterleaveType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.data.nio.PixelInterleavedRasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.RasterIOProvider;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.coverage.raster.io.RasterWriter;
import org.deegree.geometry.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class reads and writes raster files. The actual raster loading and writing is handled by {@link RasterReader}
 * and {@link RasterWriter} implementations.
 * 
 * TODO use the new, not yet implemented, configuration framework to allow customization of the IO classes
 * 
 * @version $Revision$
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 */

public class RasterFactory {
    private static class ThreadLocalServiceLoader extends ThreadLocal<ServiceLoader<RasterIOProvider>> {
        @Override
        public ServiceLoader<RasterIOProvider> initialValue() {
            return ServiceLoader.load( RasterIOProvider.class );
        }
    }

    @SuppressWarnings("synthetic-access")
    private static ThreadLocalServiceLoader serviceLoader = new ThreadLocalServiceLoader();

    private static Logger log = LoggerFactory.getLogger( RasterFactory.class );

    /**
     * Load a raster from a file.
     * 
     * @param filename
     *            the filename of the raster
     * @return the loaded raster as an AbstractRaster
     * @throws IOException
     */
    public static AbstractRaster loadRasterFromFile( File filename )
                            throws IOException {
        RasterIOOptions options = RasterIOOptions.forFile( filename );
        return loadRasterFromFile( filename, options );
    }

    /**
     * Load a raster from a file.
     * 
     * @param filename
     *            the filename of the raster
     * @param options
     * @return the loaded raster as an AbstractRaster
     * @throws IOException
     */
    public static AbstractRaster loadRasterFromFile( File filename, RasterIOOptions options )
                            throws IOException {
        RasterReader reader = getRasterReader( filename, options );
        if ( reader == null ) {
            log.error( "couldn't find raster reader for " + filename );
            throw new IOException( "couldn't find raster reader" );
        }
        return reader.load( filename, options );
    }

    /**
     * Load a raster from a stream.
     * 
     * @param in
     * @param options
     *            map with options for the raster writer
     * @return the loaded raster as an AbstractRaster
     * @throws IOException
     */
    public static AbstractRaster loadRasterFromStream( InputStream in, RasterIOOptions options )
                            throws IOException {
        RasterReader reader = getRasterReader( options );
        if ( reader == null ) {
            log.error( "couldn't find raster reader for stream" );
            throw new IOException( "couldn't find raster reader for stream (" + options + ")" );
        }

        return reader.load( in, options );
    }

    /**
     * Save a raster to a file.
     * 
     * @param raster
     * @param filename
     * @throws IOException
     */
    public static void saveRasterToFile( AbstractRaster raster, File filename )
                            throws IOException {
        saveRasterToFile( raster, filename, new RasterIOOptions() );
    }

    /**
     * Save a raster to a file.
     * 
     * @param raster
     * @param filename
     * @param options
     *            map with options for the raster writer
     * @throws IOException
     */
    public static void saveRasterToFile( AbstractRaster raster, File filename, RasterIOOptions options )
                            throws IOException {
        if ( !options.contains( RasterIOOptions.OPT_FORMAT ) ) {
            String format = FileUtils.getFileExtension( filename );
            options.add( RasterIOOptions.OPT_FORMAT, format );
        }
        RasterWriter writer = getRasterWriter( raster, options );
        if ( writer == null ) {
            log.error( "couldn't find raster writer for " + filename );
            throw new IOException( "couldn't find raster writer" );
        }

        writer.write( raster, filename, options );
    }

    /**
     * Save a raster to a stream.
     * 
     * @param raster
     * @param out
     * @param options
     *            map with options for the raster writer
     * @throws IOException
     */
    public static void saveRasterToStream( AbstractRaster raster, OutputStream out, RasterIOOptions options )
                            throws IOException {
        RasterWriter writer = getRasterWriter( raster, options );
        if ( writer == null ) {
            log.error( "couldn't find raster writer for stream" );
            throw new IOException( "couldn't find raster writer" );
        }

        writer.write( raster, out, options );
    }

    private static ServiceLoader<RasterIOProvider> getRasterIOLoader() {
        return serviceLoader.get();
    }

    private static RasterReader getRasterReader( File filename, RasterIOOptions options ) {
        for ( RasterIOProvider reader : getRasterIOLoader() ) {
            String format = options.get( RasterIOOptions.OPT_FORMAT );
            RasterReader possibleReader = reader.getRasterReader( format );
            if ( possibleReader != null && possibleReader.canLoad( filename ) ) {
                return possibleReader;
            }
        }
        return null;
    }

    private static RasterReader getRasterReader( RasterIOOptions options ) {
        for ( RasterIOProvider reader : getRasterIOLoader() ) {
            String format = options.get( RasterIOOptions.OPT_FORMAT );
            RasterReader possibleReader = reader.getRasterReader( format );
            if ( possibleReader != null ) {
                return possibleReader;
            }
        }
        return null;
    }

    private static RasterWriter getRasterWriter( AbstractRaster raster, RasterIOOptions options ) {
        for ( RasterIOProvider writer : getRasterIOLoader() ) {
            String format = options.get( RasterIOOptions.OPT_FORMAT );
            RasterWriter possibleWriter = writer.getRasterWriter( format );
            // TODO
            if ( possibleWriter != null && possibleWriter.canWrite( raster, options ) ) {
                return possibleWriter;
            }
        }
        return null;
    }

    /**
     * Find all RasterIOLoaders and retrieve all the (image) formats they support on writing.
     * 
     * @return a set of supported writable mime-types.
     */
    public static Set<String> getAllSupportedWritingFormats() {
        Set<String> result = new LinkedHashSet<String>();
        for ( RasterIOProvider reader : getRasterIOLoader() ) {
            Set<String> formats = reader.getRasterWriterFormats();
            if ( formats != null && !formats.isEmpty() ) {
                result.addAll( formats );
            }
        }
        return result;
    }

    /**
     * Creates a simple raster from a given {@link BufferedImage} and sets the geo reference to the given envelope.
     * 
     * @param image
     *            to get as a raster
     * @param envelope
     *            of the raster
     * @param originLocation
     *            the mapped location of the world coordinate origin on the upper left raster coordinate.
     * @return a geo referenced AbstractRaster.
     */
    public static AbstractRaster createRasterFromImage( RenderedImage image, Envelope envelope,
                                                        RasterGeoReference.OriginLocation originLocation ) {
        return createRasterFromImage( image, envelope, originLocation, null );
    }

    /**
     * Creates a simple raster from a given {@link BufferedImage} and sets the geo reference to the given envelope.
     * 
     * @param image
     *            to get as a raster
     * @param envelope
     *            of the raster
     * @param originLocation
     *            the mapped location of the world coordinate origin on the upper left raster coordinate.
     * @param options
     *            with information about the image and resulting raster.
     * @return a geo referenced AbstractRaster.
     */
    public static AbstractRaster createRasterFromImage( RenderedImage image, Envelope envelope,
                                                        RasterGeoReference.OriginLocation originLocation,
                                                        RasterIOOptions options ) {
        RasterGeoReference ref = RasterGeoReference.create( originLocation, envelope, image.getWidth(),
                                                            image.getHeight() );
        RasterIOOptions opts = options;
        if ( opts == null ) {
            opts = new RasterIOOptions( ref );
        } else {
            RasterGeoReference defRef = options.getRasterGeoReference();
            if ( defRef != null ) {
                ref = defRef;
            } else {
                options.setRasterGeoReference( ref );
            }
        }
        ByteBufferRasterData rasterDataFromImage = rasterDataFromImage( image, opts );

        return new SimpleRaster( rasterDataFromImage, envelope, ref );

    }

    /**
     * Creates a buffered image from a given {@link AbstractRaster}, note creating an image might result in a incorrect
     * view of the raster.
     * 
     * @param raster
     *            to create the image from
     * 
     * @return a {@link BufferedImage} created from the given raster.
     */
    public static BufferedImage imageFromRaster( AbstractRaster raster ) {
        if ( raster != null ) {
            SimpleRaster sr = raster.getAsSimpleRaster();
            return rasterDataToImage( sr.getRasterData() );
        }
        return null;
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

        // operate on the view of the given raster
        RasterDataInfo view = raster.getDataInfo();

        BandType[] bandTypes = view.getBandInfo();
        int bands = view.bands();

        // the datasize in bytes
        int bandOffset = view.dataSize;

        int[] bandOffsets = new int[bands];
        for ( int i = 0; i < bandOffsets.length; i++ ) {
            bandOffsets[i] = bandOffset * i;
        }

        // all operate on the view.
        int width = raster.getWidth();
        int height = raster.getHeight();
        DataType type = raster.getDataType();

        // pixel stride returns the rasters file, so remodulate that
        // raster.getPixelStride()
        int pixelStride = bandOffset * bands;

        // the line stride operates on the original size as well, recreate this.
        // raster.getLineStride()
        int lineStride = pixelStride * width;

        // the sample model is large enough for the view.
        SampleModel sm = null;

        WritableRaster outputRaster = null;
        BufferedImage result = null;
        int outputType = BufferedImage.TYPE_CUSTOM;
        switch ( type ) {
        case BYTE:
            int outputBands = 1;
            if ( bands == 1 ) {
                outputType = BufferedImage.TYPE_BYTE_GRAY;
            } else if ( bands == 2 || bands == 3 ) {
                if ( bands == 2 ) {
                    pixelStride++;
                    lineStride = pixelStride * width;
                    bandOffsets = new int[] { 0, 1, 2 };

                }
                outputType = BufferedImage.TYPE_INT_RGB;
                outputBands = 3;
            } else if ( bands == 4 ) {
                outputType = BufferedImage.TYPE_INT_ARGB;
                outputBands = 4;
            } else {
                throw new UnsupportedOperationException( "Could not save raster with bands: " + bandTypes );
            }
            sm = new PixelInterleavedSampleModel( DataType.toDataBufferType( type ), width, height, pixelStride,
                                                  lineStride, bandOffsets );
            outputRaster = Raster.createWritableRaster( sm, null );
            // if the number of bands is original, use line copying (it's faster)
            // if ( pixelStride == raster.getPixelStride() && bands >= 3 ) {
            // byte[] buf = new byte[raster.getPixelStride() * width];
            // ByteBuffer src = raster.getByteBuffer();
            // for ( int i = 0; i < height; i++ ) {
            // int pos = raster.calculatePos( 0, i );
            // if ( pos == -1 ) {// outside the buffer.
            // raster.getNullPixel( buf );
            // } else {
            // src.position( pos );
            // src.get( buf );
            // }
            // outputRaster.setDataElements( 0, i, width, 1, buf );
            // }
            // } else {
            // the view has changed the bands, so copy pixel for pixel.

            byte[] buf = new byte[bandOffset * bands];
            byte[] output = new byte[outputBands];
            for ( int y = 0; y < height; y++ ) {
                for ( int x = 0; x < width; x++ ) {
                    raster.getPixel( x, y, buf );
                    outputRaster.setDataElements( x, y, mapToRGB( output, buf, view.bandInfo ) );
                }
            }
            // }
            break;
        case DOUBLE:
            sm = new BandedSampleModel( DataBuffer.TYPE_DOUBLE, width, height, bands );
            outputRaster = Raster.createWritableRaster( sm, null );
            double[] dbuf = new double[pixelStride];
            for ( int y = 0; y < height; y++ ) {
                for ( int x = 0; x < width; x++ ) {
                    raster.getDoublePixel( x, y, dbuf );
                    outputRaster.setDataElements( x, y, dbuf );
                }
            }
            break;
        case INT:
            sm = new BandedSampleModel( DataBuffer.TYPE_INT, width, height, bands );
            outputRaster = Raster.createWritableRaster( sm, null );

            // one pixel at a time.
            int[] ibuf = new int[bands];
            for ( int y = 0; y < height; y++ ) {
                for ( int x = 0; x < width; x++ ) {
                    raster.getIntPixel( x, y, ibuf );
                    outputRaster.setDataElements( x, y, ibuf );
                }
            }
            break;
        case FLOAT:
            // float[] dataBuffer = new float[bands * width * height];
            // float[] floatValues = new float[bands];
            // for ( int y = 0; y < height; y++ ) {
            // for ( int x = 0; x < width; x++ ) {
            // raster.getFloatPixel( x, y, floatValues );
            // System.arraycopy( floatValues, 0, dataBuffer, ( ( y * width ) + x ) * bands, bands );
            //
            // }
            // }
            // set data does not work.. it will round to int :-)
            SampleModel fm = new BandedSampleModel( DataBuffer.TYPE_FLOAT, raster.getWidth(), raster.getHeight(), bands );

            byte[] noData = sourceRaster.getNullPixel( null );
            FloatBuffer f = ByteBuffer.wrap( noData ).asFloatBuffer();

            DataBuffer db = new RawDataBufferFloat( raster.getByteBuffer(), f.get( 0 ), raster.getBytebufferDomain(),
                                                    raster.getView() );
            // new DataBufferFloat( 12 );
            WritableRaster wr = Raster.createWritableRaster( fm, db, null );
            BufferedImage floatImage = new BufferedImage( new FloatColorModel( noData ), wr, false, null );
            return floatImage;
            // break;
        case SHORT:
        case USHORT:
            outputType = TYPE_USHORT_GRAY;
            sm = new BandedSampleModel( DataBuffer.TYPE_SHORT, width, height, bands );
            outputRaster = Raster.createWritableRaster( sm, null );
            short[] sbuf = new short[pixelStride];
            for ( int y = 0; y < height; y++ ) {
                for ( int x = 0; x < width; x++ ) {
                    raster.getShortPixel( x, y, sbuf );
                    outputRaster.setDataElements( x, y, sbuf );
                }
            }
            break;
        default:
            throw new UnsupportedOperationException( "DataType not supported (" + type + ")" );

        }
        result = new BufferedImage( width, height, outputType );
        result.setData( outputRaster );
        return result;
    }

    /**
     * @param bandsFromRaster
     * @param bandInfo
     */
    private static byte[] mapToRGB( byte[] outputBands, byte[] bandsFromRaster, BandType[] bandInfo ) {
        if ( bandInfo.length == 1 ) {
            outputBands[0] = bandsFromRaster[0];
        } else {
            int add = outputBands.length == 4 ? 1 : 0;
            // ABGR
            for ( int i = 0; i < bandInfo.length; ++i ) {
                byte value = bandsFromRaster[i];
                switch ( bandInfo[i] ) {
                case ALPHA:
                    outputBands[0] = value;
                    break;
                case RED:
                    outputBands[add] = value;
                    break;
                case GREEN:
                    outputBands[1 + add] = value;
                    break;
                case BLUE:
                    outputBands[2 + add] = value;
                    break;
                default:
                    outputBands[i] = value;
                }
            }
        }
        return outputBands;
    }

    /**
     * Creates a buffered image from the given raster data. The options can be used to modify the outcome of the
     * buffered image.
     * 
     * @param img
     * @param options
     *            which can hold information about the image read, may be <code>null</code>
     * @return the rasterdata object from the image or <code>null</code> if the given img is <code>null</code>
     */
    public static ByteBufferRasterData rasterDataFromImage( RenderedImage img, RasterIOOptions options ) {
        ByteBufferRasterData result = null;
        if ( img != null ) {

            Raster raster = img.getData();

            int x = 0, y = 0;
            int width = raster.getWidth();
            int height = raster.getHeight();

            BandType[] bandTypes = determineBandTypes( img );

            int imgDataType = raster.getSampleModel().getDataType();

            DataType type = DataType.fromDataBufferType( imgDataType );
            if ( img instanceof BufferedImage ) {
                // buffered images pack the pixels into the given datatype, so if datatype is int, the
                // org.deegree.coverage.raster.data datatype is still Byte (4 bytes in an int value).
                if ( type != DataType.FLOAT && type != DataType.DOUBLE && type != DataType.BYTE && bandTypes.length > 1 ) {
                    type = DataType.BYTE;
                    boolean alphaLast = ( bandTypes.length == 4 ) && ( bandTypes[3] == BandType.ALPHA );

                    bandTypes[0] = BandType.RED;
                    bandTypes[1] = BandType.GREEN;
                    bandTypes[2] = BandType.BLUE;
                    if ( bandTypes.length == 4 && !alphaLast ) {
                        bandTypes[3] = BandType.ALPHA;
                    }
                }
            }
            byte[] noData = options != null ? options.getNoDataValue() : null;
            // rb: are we sure it is always pixel interleaved?
            RasterDataInfo rdi = new RasterDataInfo( noData, bandTypes, type, InterleaveType.PIXEL );

            ByteBuffer byteBuffer = ByteBufferPool.allocate( rdi.bands * width * height * rdi.dataSize, false );
            if ( type == DataType.BYTE && imgDataType == DataBuffer.TYPE_INT || imgDataType == DataBuffer.TYPE_SHORT
                 || imgDataType == DataBuffer.TYPE_USHORT ) {
                // hack for the INT_ARGB etc. etc.
                // the bytes are _packed_ in an int/short/ushort *sigh*
                Object pixels = raster.getDataElements( x, y, width, height, null );

                ColorModel cm = img.getColorModel();

                if ( imgDataType == DataBuffer.TYPE_INT ) {
                    copyIntValues( (int[]) pixels, cm, byteBuffer, rdi );
                } else {
                    copyShortValues( (short[]) pixels, cm, byteBuffer, rdi );
                }

            } else if ( type == DataType.BYTE && ( imgDataType != DataBuffer.TYPE_BYTE ) ) {
                throw new UnsupportedOperationException(
                                                         "The image databuffer type could not be converted to the coverage raster api type." );
            } else {
                rasterToByteBuffer( raster, 0, 0, width, height, type, byteBuffer );
            }

            RasterGeoReference geoRef = options == null ? null : options.getRasterGeoReference();
            if ( geoRef == null ) {
                // create a default geoRef
                geoRef = new RasterGeoReference( OriginLocation.OUTER, 1, -1, 0, 0 );
            }
            result = RasterDataFactory.createRasterData( width, height, rdi, geoRef, byteBuffer, true, null );
            // CacheRasterReader reader = new CacheRasterReader( byteBuffer, width, height, null, rdi, geoRef );
            byteBuffer = null;
            // result = new PixelInterleavedRasterData( new RasterRect( 0, 0, width, height ), width, height, reader,
            // rdi );

        }

        return result;
    }

    /**
     * Create a byte buffer from the given raster, the Bytebuffer will be filled row order, that is, pixel(0,0);(0,1)
     * 
     * @param imageRaster
     * @param x
     * @param y
     * @param width
     * @param height
     * @param type
     * @param byteBuffer
     * @return the byte buffer filled with data from the image raster.
     */
    public static ByteBuffer rasterToByteBuffer( Raster imageRaster, int x, int y, int width, int height,
                                                 DataType type, ByteBuffer byteBuffer ) {
        if ( byteBuffer == null ) {
            int cap = imageRaster.getHeight() * imageRaster.getWidth() * imageRaster.getNumBands() * type.getSize();
            // what's the size of a sample
            byteBuffer = ByteBufferPool.allocate( cap, false );
        }

        // rb: check the bounds?
        byteBuffer.clear();
        switch ( type ) {
        case BYTE:
            // DataBufferByte data = ( (DataBufferByte) imageRaster.getDataBuffer() );
            // int banks = data.getNumBanks();
            byteBuffer.put( (byte[]) imageRaster.getDataElements( x, y, width, height, null ) );
            // rb: because the getDataElements uses the bankofset of the data
            // for ( int bank = 0; bank < banks; ++bank ) {
            // byte[] bs = data.getData( bank );
            // byteBuffer.put( bs );
            // }
            break;
        case DOUBLE:
            DoubleBuffer dbuf = byteBuffer.asDoubleBuffer();
            dbuf.put( (double[]) imageRaster.getDataElements( x, y, width, height, null ) );
            break;
        case INT:
            IntBuffer ibuf = byteBuffer.asIntBuffer();
            ibuf.put( (int[]) imageRaster.getDataElements( x, y, width, height, null ) );
            break;
        case FLOAT:
            FloatBuffer fbuf = byteBuffer.asFloatBuffer();
            fbuf.put( (float[]) imageRaster.getDataElements( x, y, width, height, null ) );
            break;
        case SHORT:
        case USHORT:
            ShortBuffer sbuf = byteBuffer.asShortBuffer();
            sbuf.put( (short[]) imageRaster.getDataElements( x, y, width, height, null ) );
            break;
        default:
            throw new UnsupportedOperationException( "DataType not supported (" + type + ")" );
        }
        return byteBuffer;
    }

    /**
     * @param pixels
     * @param cm
     * @param byteBuffer
     * @param rdi
     */
    private static void copyIntValues( int[] pixels, ColorModel cm, ByteBuffer byteBuffer, RasterDataInfo rdi ) {
        byteBuffer.clear();
        byte[] res = new byte[rdi.bands()];
        for ( int i = 0; i < pixels.length; ++i ) {
            int pixel = pixels[i];
            res[0] = (byte) cm.getRed( pixel );
            res[1] = (byte) cm.getGreen( pixel );
            res[2] = (byte) cm.getBlue( pixel );
            if ( res.length == 4 ) {
                res[3] = (byte) cm.getAlpha( i );
            }
            byteBuffer.put( res );
            // System.out.println( "pixel(" + pixels.length + "): " + i + "| color: " + ( res[0] & 0xff ) + ","
            // + ( res[1] & 0xff ) + "," + ( res[2] & 0xff ) );
            // System.out.println( "pos(" + byteBuffer.position() + "): " + i + "| color: " + ( res[0] & 0xff ) + ","
            // + ( res[1] & 0xff ) + "," + ( res[2] & 0xff ) );
        }
    }

    /**
     * @param pixels
     * @param cm
     * @param byteBuffer
     * @param rdi
     */
    private static void copyShortValues( short[] pixels, ColorModel cm, ByteBuffer byteBuffer, RasterDataInfo rdi ) {
        byte[] res = new byte[rdi.bands()];
        for ( int i = 0; i < pixels.length; ++i ) {
            short pixel = pixels[i];
            res[0] = (byte) cm.getRed( pixel );
            res[1] = (byte) cm.getGreen( pixel );
            res[2] = (byte) cm.getBlue( pixel );
            if ( res.length == 4 ) {
                res[3] = (byte) cm.getAlpha( i );
            }
            byteBuffer.put( res );
        }
    }

    /**
     * @param img
     *            to get the bands for.
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

    /**
     * Creates a buffered image from the given raster data by calling the
     * {@link RasterFactory#rasterDataFromImage(RenderedImage, RasterIOOptions)} method without any options. *
     * 
     * @param img
     * @return the rasterdata object from the image or <code>null</code> if the given img is <code>null</code>
     */
    public static ByteBufferRasterData rasterDataFromImage( BufferedImage img ) {
        return rasterDataFromImage( img, null );
    }

    /**
     * Creates a new Raster data object from the given world envelope, a raster reference and the data info object
     * (holding information about type, size etc...). If any of the parameters are <code>null</code> null will be
     * returned.
     * 
     * @param rdi
     * @param worldEnvelope
     *            describing the raster data.
     * @param rasterGeoReference
     *            the raster geo reference defining the resolution of the raster.
     * @return a raster data object according to the given parameters.
     */
    public static SimpleRaster createEmptyRaster( RasterDataInfo rdi, Envelope worldEnvelope,
                                                  RasterGeoReference rasterGeoReference ) {
        return createEmptyRaster( rdi, worldEnvelope, rasterGeoReference, null, false, null );
    }

    /**
     * Creates a new Raster data object from the given world envelope, a raster reference and the data info object
     * (holding information about type, size etc...). If any of the parameters are <code>null</code> null will be
     * returned.
     * 
     * @param rdi
     * @param worldEnvelope
     *            describing the raster data.
     * @param rasterGeoReference
     *            the raster geo reference defining the resolution of the raster.
     * @param reader
     *            to lazely instantiate the data from, may be <code>null</code>, if the raster should not be backed by
     *            any data.
     * @param addToCache
     *            true if the raster data of the given raster should be added to the cache.
     * @param options
     *            containing information about the raster caching.
     * @return a raster data object according to the given parameters.
     */
    public static SimpleRaster createEmptyRaster( RasterDataInfo rdi, Envelope worldEnvelope,
                                                  RasterGeoReference rasterGeoReference, RasterReader reader,
                                                  boolean addToCache, RasterIOOptions options ) {
        SimpleRaster result = null;
        if ( rdi != null && rasterGeoReference != null && worldEnvelope != null ) {
            RasterRect rasterRect = null;
            if ( reader == null ) {
                rasterRect = rasterGeoReference.convertEnvelopeToRasterCRS( worldEnvelope );
            } else {
                rasterRect = new RasterRect( 0, 0, reader.getWidth(), reader.getHeight() ); //
            }
            ByteBufferRasterData data = RasterDataFactory.createRasterData( rasterRect, rdi, reader, addToCache,
                                                                            options );
            // rasterGeoReference.convertEnvelopeToRasterCRS(
            // worldEnvelope );
            // switch ( rdi.interleaveType ) {
            // case BAND:
            // data = new BandInterleavedRasterData( rasterRect, rasterRect.width, rasterRect.height, reader, rdi );
            // break;
            // case LINE:
            // data = new LineInterleavedRasterData( rasterRect, rasterRect.width, rasterRect.height, reader, rdi );
            // break;
            // case PIXEL:
            // data = new PixelInterleavedRasterData( rasterRect, rasterRect.width, rasterRect.height, reader, rdi );
            // break;
            // }
            result = new SimpleRaster( data, worldEnvelope, rasterGeoReference );
        }
        return result;
    }
}
