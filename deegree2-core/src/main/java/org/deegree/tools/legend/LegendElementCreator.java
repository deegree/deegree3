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

package org.deegree.tools.legend;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.ImageUtils;
import org.deegree.framework.util.MimeTypeMapper;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.graphics.legend.LegendElement;
import org.deegree.graphics.legend.LegendException;
import org.deegree.graphics.legend.LegendFactory;
import org.deegree.graphics.sld.AbstractStyle;
import org.deegree.graphics.sld.NamedLayer;
import org.deegree.graphics.sld.SLDFactory;
import org.deegree.graphics.sld.StyledLayerDescriptor;
import org.deegree.graphics.sld.UserLayer;
import org.deegree.graphics.sld.UserStyle;

/**
 * This executable class is an application, which reads out an sld-document, creates the
 * corresponding legend-elements and saves them as an image. The class can be executed from the
 * console.
 *
 *
 * @author <a href="schaefer@lat-lon.de">Axel Schaefer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class LegendElementCreator {

    private static final ILogger LOG = LoggerFactory.getLogger( LegendElementCreator.class );

    String verbose_output = "";

    LecGUI lecgui = null;

    /**
     * @param sldfile
     * @param directory
     * @param format
     * @param color
     * @param width
     * @param height
     * @param title
     * @param lec
     * @throws LegendException
     *
     */
    public LegendElementCreator( String sldfile, String directory, String format, Color color, int width, int height,
                                 String title, LecGUI lec ) throws LegendException {

        this.lecgui = lec;

        StringBuffer sb = new StringBuffer( 100 );

        // read out the SLD
        HashMap stylemap = null;
        try {
            stylemap = loadSLD( sldfile );
        } catch ( IOException ioe ) {
            throw new LegendException( "An error (IOException) occured in processing the SLD-File:\n" + sldfile + "\n"
                                       + ioe );
        } catch ( XMLParsingException xmlpe ) {
            throw new LegendException( "An error (XMLParsingException) occured in parsing " + "the SLD-File:\n"
                                       + sldfile + "\n" + xmlpe.getMessage() );
        }

        // output
        LegendFactory lf = new LegendFactory();
        LegendElement le = null;
        BufferedImage buffi = null;

        Iterator iterator = stylemap.entrySet().iterator();
        String filename = null;
        AbstractStyle style = null;
        int i = 0;
        while ( iterator.hasNext() ) {
            i++;
            Map.Entry entry = (Map.Entry) iterator.next();
            filename = ( (String) entry.getKey() ).replace( ':', '_' );
            style = (AbstractStyle) entry.getValue();

            try {
                le = lf.createLegendElement( style, width, height, title );
                buffi = le.exportAsImage( MimeTypeMapper.toMimeType( format ) );
                saveImage( buffi, directory, filename, format, color );
                sb.append( "- Image " + filename + "." + format + " in " + directory + " saved.\n" );
            } catch ( LegendException lex ) {
                throw new LegendException( "An error (LegendException) occured during the creating\n"
                                           + "of the LegendElement " + filename + ":\n" + lex );
            } catch ( IOException ioex ) {
                throw new LegendException( "An error (IOException) occured during the creating/saving\n"
                                           + "of the output-image " + filename + ":\n" + ioex );
            } catch ( Exception ex ) {
                throw new LegendException( "A general error (Exception) occured during the creating/saving\n"
                                           + "of the output-image " + filename + ":\n" + ex );
            }

        }
        setVerboseOutput( sb.toString() );
    }

    /**
     *
     * @return verbose_output
     */
    public String getVerboseOutput() {
        return this.verbose_output;
    }

    /**
     * @param vo
     */
    public void setVerboseOutput( String vo ) {
        if ( vo != null ) {
            this.verbose_output = vo;
        }
    }

    /**
     * loads the sld-document, parses it an returns a HashMap containing the different styles.
     *
     * @param sldFile
     *            the file containing the StyledLayerDescriptor
     * @return HashMap containing the styles of the SLD.
     * @throws IOException
     *             if the SLD-document cant be read/found in the filesystem
     * @throws XMLParsingException
     *             if an error occurs during the parsing of the sld-document
     */
    static private HashMap loadSLD( String sldFile )
                            throws IOException, XMLParsingException {
        AbstractStyle[] styles = null;

        File file = new File( sldFile );
        StyledLayerDescriptor sld = SLDFactory.createSLD( file.toURL() );

        HashMap<String, AbstractStyle> map = new HashMap<String, AbstractStyle>();

        // NAMED LAYER
        NamedLayer[] namedlayers = sld.getNamedLayers();
        for ( int i = 0; i < namedlayers.length; i++ ) {
            styles = namedlayers[i].getStyles();
            for ( int j = 0; j < styles.length; j++ ) {
                if ( styles[j] instanceof UserStyle ) {
                    map.put( styles[j].getName(), styles[j] );
                }
            }
        }

        // USER LAYER
        UserLayer[] userLayers = sld.getUserLayers();
        for ( int k = 0; k < userLayers.length; k++ ) {
            styles = userLayers[k].getStyles();
            for ( int l = 0; l < styles.length; l++ ) {
                if ( styles[l] instanceof UserStyle ) {
                    map.put( styles[l].getName(), styles[l] );
                }
            }
        }
        return map;
    }

    /**
     * saves the resulting buffered Image from org.deegree.graphics.legend as an image.
     *
     * @param bi
     *            the BufferedImage from org.deegree.graphics.legend.*
     * @param outdir
     *            the output-directory (application-parameter)
     * @param filename
     *            the output-filename (from the styles of the SLD)
     * @param graphicsformat
     *            the output-graphicsformat (application-parameter)
     * @throws IOException
     *             if saving fails.
     * @throws Exception
     *             if the graphic-encoder can't be found.
     */
    private void saveImage( BufferedImage bi, String outdir, String filename, String graphicsformat, Color color )
                            throws LegendException, IOException, Exception {

        File file = new File( outdir, filename + "." + graphicsformat );
        FileOutputStream fos = new FileOutputStream( file );

        // PNG
        if ( graphicsformat.equalsIgnoreCase( "PNG" ) ) {

            BufferedImage outbi = new BufferedImage( bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB );
            Graphics g = outbi.getGraphics();
            g.drawImage( bi, 0, 0, color, null );
            ImageUtils.saveImage( outbi, fos, "png", 1 );
            // BMP
        } else if ( graphicsformat.equalsIgnoreCase( "BMP" ) ) {
            BufferedImage outbi = new BufferedImage( bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_3BYTE_BGR );

            Graphics g = outbi.getGraphics();
            // transparency
            if ( color == null ) {
                this.lecgui.addDebugInformation( "BMP-NOTIFY:\n"
                                                 + "Requested transparency (transp.) isn't available for BMP-images.\n"
                                                 + "Using default background color WHITE.\n" );
                color = Color.WHITE;
            }
            g.drawImage( bi, 0, 0, color, null );
            ImageUtils.saveImage( outbi, fos, "bmp", 1 );
            // GIF
        } else if ( graphicsformat.equalsIgnoreCase( "GIF" ) ) {
            BufferedImage outbi = new BufferedImage( bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB );
            Graphics g = outbi.getGraphics();
            g.drawImage( bi, 0, 0, color, null );
            ImageUtils.saveImage( outbi, fos, "gif", 1 );
            // JPEG
        } else if ( graphicsformat.equalsIgnoreCase( "JPEG" ) || graphicsformat.equalsIgnoreCase( "JPG" ) ) {
            BufferedImage outbi = new BufferedImage( bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB );
            Graphics g = outbi.getGraphics();

            // transparency
            if ( color == null ) {
                this.lecgui.addDebugInformation( "JPEG-NOTIFY:\n"
                                                 + "Requested transparency (transp.) isn't available for JPG-images.\n"
                                                 + "Using default background color WHITE.\n" );
                color = Color.WHITE;
            }

            g.drawImage( bi, 0, 0, color, null );
            ImageUtils.saveImage( outbi, fos, "jpeg", 1 );
            // TIFF
        } else if ( graphicsformat.equalsIgnoreCase( "TIFF" ) || graphicsformat.equalsIgnoreCase( "TIF" ) ) {
            BufferedImage outbi = new BufferedImage( bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_BYTE_BINARY );
            Graphics g = outbi.getGraphics();
            g.drawImage( bi, 0, 0, color, null );
            ImageUtils.saveImage( outbi, fos, "tif", 1 );
        } else {
            throw new Exception( "Can't save output image because no graphic-encoder found for:\n" + "filetype: '"
                                 + graphicsformat + "' for file: '" + file + "'" );
        }
        LOG.logInfo( "-- " + file + " saved." );
    }

    /**
     * main-method for testing purposes
     *
     * @param args
     */
    public static void main( String[] args ) {

        String sldfile = args[0];
        String directory = args[1];
        String format = "PNG";
        Color color = Color.WHITE;
        int width = 40;
        int height = 40;
        String title = "Mein Titel Platzhalter Texttexttext";
        LecGUI lec = null;

        try {
            new LegendElementCreator( sldfile, directory, format, color, width, height, title, lec );
        } catch ( LegendException e ) {
            e.printStackTrace();
        }

        LOG.logInfo( "...finished" );
    }

}
