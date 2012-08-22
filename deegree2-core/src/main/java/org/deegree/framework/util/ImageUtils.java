// $HeadURL$
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
package org.deegree.framework.util;

import static org.deegree.enterprise.WebUtils.enableProxyUsage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

import org.apache.batik.ext.awt.image.codec.ImageDecoderImpl;
import org.apache.batik.ext.awt.image.codec.PNGDecodeParam;
import org.apache.batik.ext.awt.image.codec.PNGImageDecoder;
import org.apache.batik.ext.awt.image.codec.tiff.TIFFDecodeParam;
import org.apache.batik.ext.awt.image.codec.tiff.TIFFImage;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

//import Acme.JPM.Encoders.GifEncoder;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.sun.media.jai.codec.BMPEncodeParam;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.MemoryCacheSeekableStream;
import com.sun.media.jai.codec.PNGEncodeParam;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codec.TIFFEncodeParam;

/**
 * Some utility methods for reading standard images
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ImageUtils {

    /**
     * reads an image from the passed <tt>URL</tt> using JAI mechanism
     *
     * @param url
     *            address of the image
     *
     * @return read image
     *
     * @throws IOException
     */
    public static BufferedImage loadImage( URL url )
                            throws IOException {
        if ( url.toExternalForm().startsWith( "file" ) ) {
            return loadImage( url.openStream() );
        }

        String uri = url.toExternalForm();
        HttpClient httpclient = new HttpClient();
        enableProxyUsage( httpclient, url );
        httpclient.getHttpConnectionManager().getParams().setSoTimeout( 25000 );
        GetMethod httpget = new GetMethod( uri );
        httpclient.executeMethod( httpget );
        return loadImage( httpget.getResponseBodyAsStream() );

    }

    /**
     * reads an image from the passed <tt>InputStream</tt> using JAI mechanism
     *
     * @param is
     *
     * @return read image
     *
     * @throws IOException
     */
    public static BufferedImage loadImage( InputStream is )
                            throws IOException {
        SeekableStream fss = new MemoryCacheSeekableStream( is );
        RenderedOp ro = JAI.create( "stream", fss );
        BufferedImage img = ro.getAsBufferedImage();
        fss.close();
        is.close();
        return img;
    }

    /**
     * reads an image from the passed file location using JAI mechanism
     *
     * @param fileName
     *
     * @return read imagey
     *
     * @throws IOException
     */
    public static BufferedImage loadImage( String fileName )
                            throws IOException {
        return loadImage( new File( fileName ) );
    }

    /**
     * reads an image from the passed file location using JAI mechanism
     *
     * @param file
     *
     * @return read image
     *
     * @throws IOException
     */
    public static BufferedImage loadImage( File file )
                            throws IOException {

        BufferedImage img = null;
        String tmp = file.getName().toLowerCase();
        if ( tmp.endsWith( ".tif" ) || tmp.endsWith( ".tiff" ) ) {
            InputStream is = file.toURL().openStream();
            org.apache.batik.ext.awt.image.codec.SeekableStream fss = new org.apache.batik.ext.awt.image.codec.MemoryCacheSeekableStream(
                                                                                                                                          is );
            TIFFImage tiff = new TIFFImage( fss, new TIFFDecodeParam(), 0 );
            img = PlanarImage.wrapRenderedImage( tiff ).getAsBufferedImage();
            fss.close();
        } else if ( tmp.endsWith( ".png" ) ) {
            InputStream is = file.toURL().openStream();
            ImageDecoderImpl dec = new PNGImageDecoder( is, new PNGDecodeParam() );
            img = PlanarImage.wrapRenderedImage( dec.decodeAsRenderedImage() ).getAsBufferedImage();
            is.close();
        } else {
            img = ImageIO.read( file );
        }

        return img;
    }

    /**
     * stores the passed image in the passed file name with defined quality
     *
     * @param image
     * @param fileName
     * @param quality
     *            just supported for jpeg (0..1)
     * @throws IOException
     */
    public static void saveImage( BufferedImage image, String fileName, float quality )
                            throws IOException {
        File file = new File( fileName );
        saveImage( image, file, quality );
    }

    /**
     * stores the passed image in the passed file with defined quality
     *
     * @param image
     * @param file
     * @param quality
     *            just supported for jpeg (0..1)
     * @throws IOException
     */
    public static void saveImage( BufferedImage image, File file, float quality )
                            throws IOException {
        int pos = file.getName().lastIndexOf( '.' );
        String ext = file.getName().substring( pos + 1, file.getName().length() ).toLowerCase();

        FileOutputStream fos = new FileOutputStream( file );
        saveImage( image, fos, ext, quality );

    }

    /**
     * write an image into the passed output stream. after writing the image the stream will be closed.
     *
     * @param image
     * @param os
     * @param format
     * @param quality
     * @throws IOException
     */
    public static void saveImage( BufferedImage image, OutputStream os, String format, float quality )
                            throws IOException {
        try {

            if ( "jpeg".equalsIgnoreCase( format ) || "jpg".equalsIgnoreCase( format ) ) {
                encodeJpeg( os, image, quality );
            } else if ( "tif".equalsIgnoreCase( format ) || "tiff".equalsIgnoreCase( format ) ) {
                encodeTiff( os, image );
            } else if ( "png".equalsIgnoreCase( format ) ) {
                encodePng( os, image );
            } else if ( "gif".equalsIgnoreCase( format ) ) {
                encodeGif( os, image );
            } else if ( "bmp".equalsIgnoreCase( format ) ) {
                encodeBmp( os, image );
            } else {
                throw new IOException( "invalid image format: " + format );
            }
        } catch ( IOException e ) {
            throw e;
        } finally {
            os.flush();
            os.close();
        }

    }

    /**
     *
     *
     * @param out
     * @param img
     *
     * @throws IOException
     */
    public static void encodeGif( OutputStream out, BufferedImage img )
                            throws IOException {
        ImageIO.write( img, "gif", out );
//        GifEncoder encoder = new GifEncoder( img, out );
//        encoder.encode();
    }

    /**
     *
     *
     * @param out
     * @param img
     *
     * @throws IOException
     */
    private static void encodeBmp( OutputStream out, BufferedImage img )
                            throws IOException {
        BMPEncodeParam encodeParam = new BMPEncodeParam();

        com.sun.media.jai.codec.ImageEncoder encoder = ImageCodec.createImageEncoder( "BMP", out, encodeParam );

        encoder.encode( img );
    }

    /**
     *
     *
     * @param out
     * @param img
     *
     * @throws IOException
     */
    private static void encodePng( OutputStream out, BufferedImage img )
                            throws IOException {
        PNGEncodeParam encodeParam = PNGEncodeParam.getDefaultEncodeParam( img );

        if ( encodeParam instanceof PNGEncodeParam.Palette ) {
            PNGEncodeParam.Palette p = (PNGEncodeParam.Palette) encodeParam;
            byte[] b = new byte[] { -127 };
            p.setPaletteTransparency( b );
        }

        com.sun.media.jai.codec.ImageEncoder encoder = ImageCodec.createImageEncoder( "PNG", out, encodeParam );
        encoder.encode( img.getData(), img.getColorModel() );
    }

    /**
     *
     *
     * @param out
     * @param img
     *
     * @throws IOException
     */
    private static void encodeTiff( OutputStream out, BufferedImage img )
                            throws IOException {
        TIFFEncodeParam encodeParam = new TIFFEncodeParam();

        com.sun.media.jai.codec.ImageEncoder encoder = ImageCodec.createImageEncoder( "TIFF", out, encodeParam );

        encoder.encode( img );
    }

    /**
     *
     *
     * @param out
     * @param img
     * @param quality
     *
     * @throws IOException
     */
    private static void encodeJpeg( OutputStream out, BufferedImage img, float quality )
                            throws IOException {

        // encode JPEG
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder( out );
        com.sun.image.codec.jpeg.JPEGEncodeParam jpegParams = encoder.getDefaultJPEGEncodeParam( img );
        jpegParams.setQuality( quality, false );
        encoder.setJPEGEncodeParam( jpegParams );

        encoder.encode( img );
    }

}
