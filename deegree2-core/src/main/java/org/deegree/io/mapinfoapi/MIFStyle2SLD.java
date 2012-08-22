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

package org.deegree.io.mapinfoapi;

import static java.awt.Color.black;
import static java.awt.Color.decode;
import static java.awt.Color.white;
import static java.awt.Font.TRUETYPE_FONT;
import static java.awt.Font.createFont;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.io.File.createTempFile;
import static java.lang.Integer.parseInt;
import static java.lang.Integer.toHexString;
import static java.lang.Math.sqrt;
import static javax.imageio.ImageIO.write;
import static javax.media.jai.JAI.create;
import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_HEIGHT;
import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_WIDTH;
import static org.deegree.framework.log.LoggerFactory.getLogger;
import static org.deegree.framework.xml.XMLTools.appendElement;
import static org.deegree.framework.xml.XMLTools.getNodes;
import static org.deegree.framework.xml.XMLTools.getRequiredElement;
import static org.deegree.ogcbase.CommonNamespaces.GMLNS;
import static org.deegree.ogcbase.CommonNamespaces.GML_PREFIX;
import static org.deegree.ogcbase.CommonNamespaces.OGCNS;
import static org.deegree.ogcbase.CommonNamespaces.OGC_PREFIX;
import static org.deegree.ogcbase.CommonNamespaces.SLDNS;
import static org.deegree.ogcbase.CommonNamespaces.SLD_PREFIX;
import static org.deegree.ogcbase.CommonNamespaces.XLINK_PREFIX;
import static org.deegree.ogcbase.CommonNamespaces.XLNNS;
import static org.deegree.ogcbase.CommonNamespaces.XSINS;
import static org.deegree.ogcbase.CommonNamespaces.XSI_PREFIX;
import static org.deegree.ogcbase.CommonNamespaces.getNamespaceContext;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.media.jai.RenderedOp;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.graphics.Theme;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.sun.media.jai.codec.MemoryCacheSeekableStream;

/**
 * <code>MIFStyle2SLD</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class MIFStyle2SLD {

    private static final NamespaceContext nsContext = getNamespaceContext();

    private static final ILogger LOG = getLogger( MIFStyle2SLD.class );

    private Font symbolFont;

    private static File BRUSHES, POINTS;

    private static final double SQRT2 = sqrt( 2 );

    static {
        // copy the brushes.zip to temp dir
        // necessary, because we cannot directly extract a singe entry from jar-enclosed zip
        try {
            BRUSHES = File.createTempFile( "brushes", ".zip" );
            BRUSHES.deleteOnExit();

            InputStream in = MIFStyle2SLD.class.getResourceAsStream( "brushes.zip" );
            if ( in == null ) {
                in = Theme.class.getResourceAsStream( "fillpatterns.zip" );
            }
            FileOutputStream out = new FileOutputStream( BRUSHES );

            byte[] buf = new byte[16384];

            int read;
            if ( in != null ) {
                while ( ( read = in.read( buf ) ) != -1 ) {
                    out.write( buf, 0, read );
                }
                in.close();
            }
            out.close();

        } catch ( IOException e ) {
            LOG.logError( "Could not find the brushes zip file", e );
        }
        // same for more complicated points
        try {
            POINTS = File.createTempFile( "points", ".zip" );
            POINTS.deleteOnExit();

            InputStream in = MIFStyle2SLD.class.getResourceAsStream( "points.zip" );
            if ( in == null ) {
                in = Theme.class.getResourceAsStream( "points.zip" );
            }
            FileOutputStream out = new FileOutputStream( POINTS );

            byte[] buf = new byte[16384];

            int read;
            if ( in != null ) {
                while ( ( read = in.read( buf ) ) != -1 ) {
                    out.write( buf, 0, read );
                }
                in.close();
            }
            out.close();
        } catch ( IOException e ) {
            LOG.logError( "Could not find the points zip file", e );
        }
    }

    /**
     * @param symbolFont
     * @throws FontFormatException
     * @throws IOException
     */
    public MIFStyle2SLD( String symbolFont ) throws FontFormatException, IOException {
        this.symbolFont = createFont( TRUETYPE_FONT, new File( symbolFont ) );
    }

    /**
     * @param symbolFont
     * @throws FontFormatException
     * @throws IOException
     */
    public MIFStyle2SLD( URL symbolFont ) throws FontFormatException, IOException {
        this.symbolFont = createFont( TRUETYPE_FONT, symbolFont.openStream() );
    }

    /**
     * @param name
     *            the layer name
     * @return an empty SLD document
     */
    public static XMLFragment getSLDTemplate( String name ) {
        XMLFragment doc = new XMLFragment( new QualifiedName( SLD_PREFIX, "StyledLayerDescriptor", SLDNS ) );
        Element root = doc.getRootElement();
        root.setAttribute( "version", "1.0.0" );
        root.setAttribute( "xmlns:app", "http://www.deegree.org/app" );
        root.setAttribute( "xmlns:" + XLINK_PREFIX, XLNNS.toASCIIString() );
        root.setAttribute( "xmlns:" + OGC_PREFIX, OGCNS.toASCIIString() );
        root.setAttribute( "xmlns:" + GML_PREFIX, GMLNS.toASCIIString() );
        root.setAttribute( "xmlns:" + XSI_PREFIX, XSINS.toASCIIString() );
        root.setAttribute( XSI_PREFIX + ":schemaLocation",
                           SLDNS.toASCIIString() + " http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd" );

        Element e = appendElement( root, SLDNS, SLD_PREFIX + ":NamedLayer" );
        appendElement( e, SLDNS, SLD_PREFIX + ":Name", "default:" + name );
        e = appendElement( e, SLDNS, SLD_PREFIX + ":UserStyle" );
        appendElement( e, SLDNS, SLD_PREFIX + ":Name", "default:" + name );
        appendElement( e, SLDNS, SLD_PREFIX + ":Title", "default:" + name );
        appendElement( e, SLDNS, SLD_PREFIX + ":IsDefault", "1" );
        e = appendElement( e, SLDNS, SLD_PREFIX + ":FeatureTypeStyle" );
        appendElement( e, SLDNS, SLD_PREFIX + ":Name", "default:" + name );

        String str = doc.getAsString();

        // wrap it, so the namespace bindings are correct (is there a better way to add them to the
        // root element?)
        try {
            return new XMLFragment( new StringReader( str ), "http://www.systemid.org" );
        } catch ( SAXException ex ) {
            // eat it, it must be parseable
        } catch ( IOException ex ) {
            // eat it, it must be parseable
        }

        return null;
    }

    /**
     * @param id
     * @param rule
     */
    public static void appendIDFilter( String id, Element rule ) {
        appendElement( rule, SLDNS, SLD_PREFIX + ":Name", id );
        Element e = appendElement( rule, OGCNS, OGC_PREFIX + ":Filter" );
        Element or = appendElement( e, OGCNS, OGC_PREFIX + ":Or" );
        e = appendElement( or, OGCNS, OGC_PREFIX + ":PropertyIsLike" );
        e.setAttribute( "wildCard", "*" );
        e.setAttribute( "escape", "\\" );
        e.setAttribute( "singleChar", "?" );
        appendElement( e, OGCNS, OGC_PREFIX + ":PropertyName", "app:styleid" );
        appendElement( e, OGCNS, OGC_PREFIX + ":Literal", id );

        // it could be part of a combined (polygon) style
        e = appendElement( or, OGCNS, OGC_PREFIX + ":PropertyIsLike" );
        e.setAttribute( "wildCard", "*" );
        e.setAttribute( "escape", "\\" );
        e.setAttribute( "singleChar", "?" );
        appendElement( e, OGCNS, OGC_PREFIX + ":PropertyName", "app:styleid" );
        appendElement( e, OGCNS, OGC_PREFIX + ":Literal", "*_" + id );

        e = appendElement( or, OGCNS, OGC_PREFIX + ":PropertyIsLike" );
        e.setAttribute( "wildCard", "*" );
        e.setAttribute( "escape", "\\" );
        e.setAttribute( "singleChar", "?" );
        appendElement( e, OGCNS, OGC_PREFIX + ":PropertyName", "app:styleid" );
        appendElement( e, OGCNS, OGC_PREFIX + ":Literal", id + "_*" );
    }

    /**
     * @param map
     * @param doc
     * @throws DOMException
     * @throws IOException
     * @throws XMLParsingException
     * @throws SAXException
     */
    public void insertSymbolStyle( Map<String, String> map, XMLFragment doc )
                            throws DOMException, IOException, XMLParsingException, SAXException {

        if ( map.size() != 4 && map.size() != 5 ) {
            LOG.logWarning( "Symbol style not supported yet: " + map );
            return;
        }

        // custom bitmap symbol style
        if ( map.size() == 5 ) {
            int size = parseInt( map.get( "size" ) );

            if ( map.get( "filename" ) == null ) {
                LOG.logWarning( "Symbol style not supported yet: " + map );
                return;
            }

            File file = new File( map.get( "filename" ) );
            if ( !file.exists() ) {
                LOG.logWarning( "The specified symbol does not exist: " + file.toString() );
                return;
            }

            Element e = getRequiredElement( doc.getRootElement(), ".//" + SLD_PREFIX + ":FeatureTypeStyle", nsContext );
            e = appendElement( e, SLDNS, SLD_PREFIX + ":Rule" );
            appendIDFilter( map.get( "styleid" ), e );
            e = appendElement( e, SLDNS, SLD_PREFIX + ":PointSymbolizer" );
            e = appendElement( e, SLDNS, SLD_PREFIX + ":Graphic" );
            appendElement( e, SLDNS, SLD_PREFIX + ":Size", "" + size );
            e = appendElement( e, SLDNS, SLD_PREFIX + ":ExternalGraphic" );
            Element o = appendElement( e, SLDNS, SLD_PREFIX + ":OnlineResource" );
            o.setAttributeNS( XLNNS.toASCIIString(), XLINK_PREFIX + ":href", file.toURI().toURL().toExternalForm() );
            o.setAttributeNS( XLNNS.toASCIIString(), XLINK_PREFIX + ":type", "simple" );
            appendElement( e, SLDNS, SLD_PREFIX + ":Format", "image/bmp" );

            return;
        }

        Color c = decode( map.get( "color" ) );

        int size = parseInt( map.get( "size" ) );
        int symbol = parseInt( map.get( "shape" ) );

        BufferedImage img = null;

        switch ( symbol ) {
        case 31:
            // don't do anything
            return;
        case 32:
            img = symbolFromTwoChars( symbolFont, (char) 61473, (char) 61479, size, c, black );
            break;
        case 33:
            img = symbolFromTwoChars( symbolFont, (char) 61474, (char) 61480, size, c, black );
            break;
        case 34:
            img = symbolFromTwoChars( symbolFont, (char) 61475, (char) 61481, size, c, black );
            break;
        case 35:
            img = symbolFromTwoChars( symbolFont, (char) 61476, (char) 61482, size, c, black );
            break;
        case 36:
            img = symbolFromTwoChars( symbolFont, (char) 61477, (char) 61483, size, c, black );
            break;
        case 37:
            img = symbolFromTwoChars( symbolFont, (char) 61478, (char) 61484, size, c, black );
            break;
        case 38:
        case 39:
        case 40:
        case 41:
        case 42:
        case 43:
        case 49:
        case 50:
        case 51:
        case 52:
        case 53:
        case 54:
        case 55:
        case 56:
        case 57:
        case 58:
        case 60:
        case 61:
        case 62:
        case 63:
        case 64:
            img = symbolFromFont( symbolFont, (char) ( 61473 + symbol - 32 ), size, c );
            break;
        case 44:
        case 45:
        case 46:
        case 47:
        case 48:
        case 65:
        case 66:
        case 67: {
            ZipFile zip = new ZipFile( POINTS );
            ZipEntry entry = zip.getEntry( symbol + ".svg" );
            XMLFragment svg = new XMLFragment( new InputStreamReader( zip.getInputStream( entry ) ),
                                               "http://www.systemid.org" );
            zip.close();
            updateSVGColors( svg, toHexColor( c ), toHexColor( c ) );
            img = renderSVGImage( svg, size );
            break;
        }
        case 59: {
            ZipFile zip = new ZipFile( POINTS );
            ZipEntry entry = zip.getEntry( symbol + ".svg" );
            XMLFragment svg = new XMLFragment( new InputStreamReader( zip.getInputStream( entry ) ),
                                               "http://www.systemid.org" );
            zip.close();
            updateSVGColors( svg, toHexColor( c ), toHexColor( c ) );
            img = renderSVGImage( svg, size );
            break;
        }
        }

        if ( img == null ) {
            img = new BufferedImage( 1, 1, TYPE_INT_ARGB );
        }

        File symbolFile = createTempFile( "mmsvg", ".png" );
        write( img, "png", symbolFile );
        symbolFile.deleteOnExit();

        Element e = getRequiredElement( doc.getRootElement(), ".//" + SLD_PREFIX + ":FeatureTypeStyle", nsContext );
        e = appendElement( e, SLDNS, SLD_PREFIX + ":Rule" );
        appendIDFilter( map.get( "styleid" ), e );
        e = appendElement( e, SLDNS, SLD_PREFIX + ":PointSymbolizer" );
        e = appendElement( e, SLDNS, SLD_PREFIX + ":Graphic" );
        e = appendElement( e, SLDNS, SLD_PREFIX + ":ExternalGraphic" );
        Element o = appendElement( e, SLDNS, SLD_PREFIX + ":OnlineResource" );
        o.setAttributeNS( XLNNS.toASCIIString(), XLINK_PREFIX + ":href", symbolFile.toURI().toURL().toExternalForm() );
        o.setAttributeNS( XLNNS.toASCIIString(), XLINK_PREFIX + ":type", "simple" );
        appendElement( e, SLDNS, SLD_PREFIX + ":Format", "image/png" );
    }

    private static void appendSimpleLine( Element rule, String cssPattern, int width, Color c ) {
        Element e = appendElement( rule, SLDNS, SLD_PREFIX + ":LineSymbolizer" );
        e = appendElement( e, SLDNS, SLD_PREFIX + ":Stroke" );
        appendElement( e, SLDNS, SLD_PREFIX + ":CssParameter", toHexColor( c ) ).setAttribute( "name", "stroke" );
        appendElement( e, SLDNS, SLD_PREFIX + ":CssParameter", "" + width ).setAttribute( "name", "stroke-width" );
        if ( cssPattern != null ) {
            appendElement( e, SLDNS, SLD_PREFIX + ":CssParameter", cssPattern ).setAttribute( "name",
                                                                                              "stroke-dasharray" );
        }
    }

    private static void appendImageLine( Element rule, String image, int width, Color c )
                            throws MalformedURLException, IOException, SAXException, XMLParsingException {
        XMLFragment svg = new XMLFragment( MIFStyle2SLD.class.getResource( "lines/" + image ) );
        updateSVGColors( svg, toHexColor( c ), toHexColor( c ) );
        BufferedImage img = renderSVGImage( svg, 0 );
        File svgFile = createTempFile( "mmsvg", ".png" );
        write( img, "png", svgFile );
        svgFile.deleteOnExit();

        Element e = appendElement( rule, SLDNS, SLD_PREFIX + ":LineSymbolizer" );
        e = appendElement( e, SLDNS, SLD_PREFIX + ":Stroke" );
        appendElement( e, SLDNS, SLD_PREFIX + ":CssParameter", "0" ).setAttribute( "name", "stroke-width" );
        appendElement( e, SLDNS, SLD_PREFIX + ":CssParameter", "0" ).setAttribute( "name", "stroke-opacity" );
        e = appendElement( e, SLDNS, SLD_PREFIX + ":GraphicStroke" );
        e = appendElement( e, SLDNS, SLD_PREFIX + ":Graphic" );
        appendElement( e, SLDNS, SLD_PREFIX + ":Size", "" + width );
        e = appendElement( e, SLDNS, SLD_PREFIX + ":ExternalGraphic" );
        Element o = appendElement( e, SLDNS, SLD_PREFIX + ":OnlineResource" );
        o.setAttributeNS( XLNNS.toASCIIString(), XLINK_PREFIX + ":href", svgFile.toURI().toURL().toExternalForm() );
        o.setAttributeNS( XLNNS.toASCIIString(), XLINK_PREFIX + ":type", "simple" );
        appendElement( e, SLDNS, SLD_PREFIX + ":Format", "image/png" );
    }

    /**
     * @param pattern
     *            a pattern like "1 2 1 2"
     * @param mult
     * @return the new pattern with each value multiplied by mult
     */
    public static String multiplyPattern( String pattern, int mult ) {
        String[] elems = pattern.split( "[ ]" );
        StringBuffer sb = new StringBuffer( pattern.length() );

        for ( int i = 0; i < elems.length; ++i ) {
            int k = mult * parseInt( elems[i] );
            sb.append( k );
            if ( i < elems.length - 1 ) {
                sb.append( " " );
            }
        }

        return sb.toString();
    }

    /**
     * @param map
     * @param doc
     * @throws XMLParsingException
     * @throws SAXException
     * @throws IOException
     * @throws MalformedURLException
     */
    public static void insertPenStyle( Map<String, String> map, XMLFragment doc )
                            throws MalformedURLException, IOException, SAXException, XMLParsingException {
        int pattern = parseInt( map.get( "pattern" ) );
        Color c = decode( map.get( "color" ) );
        int width = parseInt( map.get( "width" ) );
        if ( width > 10 ) {
            width = width * 10 + 10;
            width = (int) ( width / 72d / SQRT2 );
            LOG.logDebug( "Calculated pixel width from points", width );
        }

        Element e = getRequiredElement( doc.getRootElement(), ".//" + SLD_PREFIX + ":FeatureTypeStyle", nsContext );
        Element rule = appendElement( e, SLDNS, SLD_PREFIX + ":Rule" );
        appendIDFilter( map.get( "styleid" ), rule );

        switch ( pattern ) {
        case 1:
            break;
        case 2:
            appendSimpleLine( rule, null, width, c );
            break;
        case 3:
            appendSimpleLine( rule, multiplyPattern( "1 1", width ), width, c );
            break;
        case 4:
            appendSimpleLine( rule, multiplyPattern( "2 2", width ), width, c );
            break;
        case 5:
            appendSimpleLine( rule, multiplyPattern( "3 1", width ), width, c );
            break;
        case 6:
            appendSimpleLine( rule, multiplyPattern( "5 1", width ), width, c );
            break;
        case 7:
            appendSimpleLine( rule, multiplyPattern( "10 3", width ), width, c );
            break;
        case 8:
            appendSimpleLine( rule, multiplyPattern( "20 5", width ), width, c );
            break;
        case 9:
            appendSimpleLine( rule, multiplyPattern( "5 5", width ), width, c );
            break;
        case 10:
            appendSimpleLine( rule, multiplyPattern( "1 5", width ), width, c );
            break;
        case 11:
            appendSimpleLine( rule, multiplyPattern( "3 5", width ), width, c );
            break;
        case 12:
            appendSimpleLine( rule, multiplyPattern( "7 7", width ), width, c );
            break;
        case 13:
            appendSimpleLine( rule, multiplyPattern( "10 10", width ), width, c );
            break;
        case 14:
            appendSimpleLine( rule, multiplyPattern( "9 3 1 3", width ), width, c );
            break;
        case 15:
            appendSimpleLine( rule, multiplyPattern( "12 2 1 2", width ), width, c );
            break;
        case 16:
            appendSimpleLine( rule, multiplyPattern( "12 2 2 2", width ), width, c );
            break;
        case 17:
            appendSimpleLine( rule, multiplyPattern( "20 10 5 10", width ), width, c );
            break;
        case 18:
            appendSimpleLine( rule, multiplyPattern( "20 4 4 4 4 4", width ), width, c );
            break;
        case 19:
            appendSimpleLine( rule, multiplyPattern( "20 4 4 4 4 4 4 4", width ), width, c );
            break;
        case 20:
            appendSimpleLine( rule, multiplyPattern( "9 3 1 3 1 3", width ), width, c );
            break;
        case 21:
            appendSimpleLine( rule, multiplyPattern( "12 3 1 3 1 3", width ), width, c );
            break;
        case 22:
            appendSimpleLine( rule, multiplyPattern( "12 3 1 3 1 3 1 3", width ), width, c );
            break;
        case 23:
            appendSimpleLine( rule, multiplyPattern( "5 1 1 1", width ), width, c );
            break;
        case 24:
            appendSimpleLine( rule, multiplyPattern( "5 1 1 1 1 1", width ), width, c );
            break;
        case 25:
            appendSimpleLine( rule, multiplyPattern( "9 1 1 1 3 1 1 1", width ), width, c );
            break;
        case 26: {
            appendSimpleLine( rule, null, width, c );
            appendSimpleLine( rule, multiplyPattern( "1 8", width ), width * 5, c );
            break;
        }
        case 27: {
            appendSimpleLine( rule, null, width, c );
            appendSimpleLine( rule, multiplyPattern( "1 10", width ), width * 5, c );
            break;
        }
        case 28: {
            appendSimpleLine( rule, null, width, c );
            appendSimpleLine( rule, multiplyPattern( "1 15", width ), width * 5, c );
            break;
        }
        case 29: {
            appendSimpleLine( rule, null, width, c );
            appendImageLine( rule, "29.svg", width * 16, c );
            break;
        }
        case 30: {
            appendSimpleLine( rule, null, width, c );
            appendImageLine( rule, "30.svg", width * 16, c );
            break;
        }
        case 31: {
            appendSimpleLine( rule, null, width, c );
            appendSimpleLine( rule, multiplyPattern( "1 1 12", width ), width * 5, c );
            break;
        }
        case 32: {
            appendSimpleLine( rule, multiplyPattern( "6 3 5 0", width ), width, c );
            appendSimpleLine( rule, multiplyPattern( "1 13", width ), width * 5, c );
            break;
        }
        case 33: {
            appendSimpleLine( rule, multiplyPattern( "7 3 4 0", width ), width, c );
            appendSimpleLine( rule, multiplyPattern( "1 1 1 11", width ), width * 5, c );
            break;
        }
        case 34: {
            appendSimpleLine( rule, multiplyPattern( "0 1 10 2", width ), width, c );
            appendImageLine( rule, "34.svg", width * 26, c );
            break;
        }
        case 35: {
            appendSimpleLine( rule, multiplyPattern( "0 1 10 2", width ), width, c );
            appendImageLine( rule, "35.svg", width * 26, c );
            break;
        }
        case 36: {
            appendImageLine( rule, "36.svg", width * 11, c );
            break;
        }
        case 37: {
            appendSimpleLine( rule, multiplyPattern( "10 3", width ), width, c );
            appendSimpleLine( rule, multiplyPattern( "1 8 1 3", width ), width * 5, c );
            break;
        }
        case 38: {
            appendSimpleLine( rule, null, width, c );
            appendImageLine( rule, "38-39.svg", width * 20, c );
            break;
        }
        case 39: {
            appendSimpleLine( rule, multiplyPattern( "0 3 10 7", width ), width, c );
            appendImageLine( rule, "38-39.svg", width * 20, c );
            break;
        }
        case 40: {
            appendSimpleLine( rule, multiplyPattern( "10 3 3 3", width ), width, c );
            appendSimpleLine( rule, multiplyPattern( "0 14 1 4", width ), width * 5, c );
            break;
        }
        case 41: {
            appendSimpleLine( rule, multiplyPattern( "0 5 4 1", width ), width, c );
            appendSimpleLine( rule, multiplyPattern( "4 1 0 5", width ), width * 3, c );
            break;
        }
        case 42: {
            appendSimpleLine( rule, multiplyPattern( "0 5 4 1 4 1", width ), width, c );
            appendSimpleLine( rule, multiplyPattern( "5 10", width ), width * 3, c );
            break;
        }
        case 43: {
            appendSimpleLine( rule, multiplyPattern( "0 5 4 1 4 1 4 1", width ), width, c );
            appendSimpleLine( rule, multiplyPattern( "5 15", width ), width * 3, c );
            break;
        }
        case 44: {
            appendSimpleLine( rule, multiplyPattern( "0 5 4 1 4 1 4 1 4 1", width ), width, c );
            appendSimpleLine( rule, multiplyPattern( "5 20", width ), width * 3, c );
            break;
        }
        case 45: {
            appendSimpleLine( rule, null, width, c );
            appendSimpleLine( rule, multiplyPattern( "4 15", width ), width * 3, c );
            break;
        }
        case 46: {
            appendSimpleLine( rule, multiplyPattern( "1 3", width ), width * 4, c );
            break;
        }
        case 47: {
            appendImageLine( rule, "47.svg", width * 7, c );
            break;
        }
        case 48: {
            appendSimpleLine( rule, null, width, c );
            appendImageLine( rule, "48.svg", width * 5, c );
            break;
        }
        case 49: {
            appendSimpleLine( rule, null, width, c );
            appendImageLine( rule, "49.svg", width * 5, c );
            break;
        }
        case 50: {
            appendSimpleLine( rule, null, width, c );
            appendImageLine( rule, "50.svg", width * 6, c );
            break;
        }
        case 51: {
            appendSimpleLine( rule, null, width, c );
            appendImageLine( rule, "51.svg", width * 6, c );
            break;
        }
        case 52: {
            appendSimpleLine( rule, multiplyPattern( "0 3 10 1", width ), width, c );
            appendImageLine( rule, "52.svg", width * 14, c );
            break;
        }
        case 53: {
            appendSimpleLine( rule, multiplyPattern( "0 3 10 1", width ), width, c );
            appendImageLine( rule, "53.svg", width * 14, c );
            break;
        }
        case 54: {
            appendSimpleLine( rule, null, width, c );
            appendImageLine( rule, "54.svg", width * 11, c );
            break;
        }
        case 55: {
            appendSimpleLine( rule, null, width, c );
            appendImageLine( rule, "55.svg", width * 11, c );
            break;
        }
        case 56: {
            appendSimpleLine( rule, null, width, c );
            appendImageLine( rule, "56.svg", width * 16, c );
            break;
        }
        case 57:
        case 58: {
            appendImageLine( rule, pattern + ".svg", width * 11, c );
            break;
        }
        case 63: {
            appendSimpleLine( rule, null, width + 1, c );
            appendSimpleLine( rule, null, width, white );
            break;
        }
        case 65: {
            appendSimpleLine( rule, null, width + 1, black );
            appendSimpleLine( rule, null, width, c );
            break;
        }
        case 67: {
            appendSimpleLine( rule, null, width + 1, c );
            appendSimpleLine( rule, null, width, black );
            break;
        }
        case 68: {
            appendSimpleLine( rule, multiplyPattern( "12 3", width ), width + 1, c );
            appendSimpleLine( rule, null, width, white );
            break;
        }
        case 69: {
            appendSimpleLine( rule, null, width + 1, c );
            appendSimpleLine( rule, null, width, white );
            appendSimpleLine( rule, "1 " + 20 * width, width * 5, c );
            break;
        }
        case 70: {
            appendSimpleLine( rule, multiplyPattern( "15 15", width ), width, c );
            appendSimpleLine( rule, multiplyPattern( "0 15 15 0", width ), width, black );
            break;
        }
        case 71: {
            appendSimpleLine( rule, multiplyPattern( "17 23", width ), width, c );
            appendSimpleLine( rule, multiplyPattern( "0 20 17 3", width ), width, black );
            break;
        }
        case 72: {
            appendSimpleLine( rule, multiplyPattern( "25 5", width ), width + 1, black );
            appendSimpleLine( rule, null, width, c );
            break;
        }
        case 73: {
            appendSimpleLine( rule, null, width + 1, c );
            appendSimpleLine( rule, multiplyPattern( "12 12", width ), width, white );
            break;
        }
        case 74: {
            appendSimpleLine( rule, null, width + 1, black );
            appendSimpleLine( rule, multiplyPattern( "12 12", width ), width, c );
            break;
        }
        case 75: {
            appendSimpleLine( rule, null, width + 1, black );
            appendSimpleLine( rule, multiplyPattern( "12 12", width ), width, c );
            appendSimpleLine( rule, multiplyPattern( "0 12 12 0", width ), width, white );
            break;
        }
        case 76: {
            appendSimpleLine( rule, null, width + 1, black );
            appendSimpleLine( rule, multiplyPattern( "25 25", width ), width, c );
            appendSimpleLine( rule, multiplyPattern( "0 25 25 0", width ), width, white );
            break;
        }
        case 77: {
            appendSimpleLine( rule, null, width + 1, c );
            appendSimpleLine( rule, multiplyPattern( "12 12", width ), width, black );
            appendSimpleLine( rule, multiplyPattern( "0 12 12 0", width ), width, white );
            break;
        }
        case 81: {
            appendSimpleLine( rule, null, width, black );
            appendImageLine( rule, "81-85.svg", width * 10, c );
            break;
        }
        case 82: {
            appendImageLine( rule, "81-85.svg", width * 10, c );
            break;
        }
        case 83: {
            appendSimpleLine( rule, multiplyPattern( "0 2 1 7", width ), width, black );
            appendImageLine( rule, "81-85.svg", width * 10, c );
            break;
        }
        case 84: {
            appendSimpleLine( rule, null, width * 5, black );
            appendSimpleLine( rule, null, width * 5 - 1, white );
            appendImageLine( rule, "81-85.svg", width * 10, c );
            break;
        }
        case 85: {
            appendSimpleLine( rule, null, width * 6 + 1, black );
            appendSimpleLine( rule, null, width * 6, white );
            appendImageLine( rule, "81-85.svg", width * 10, c );
            break;
        }
        case 89: {
            appendSimpleLine( rule, null, width, black );
            appendSimpleLine( rule, multiplyPattern( "5 5", width ), width * 5, c );
            break;
        }
        case 90: {
            appendSimpleLine( rule, multiplyPattern( "5 5", width ), width * 5, c );
            break;
        }
        case 91: {
            appendSimpleLine( rule, multiplyPattern( "0 7 1 2", width ), width, black );
            appendSimpleLine( rule, multiplyPattern( "5 5", width ), width * 5, c );
            break;
        }
        case 92: {
            appendSimpleLine( rule, null, width * 5, black );
            appendSimpleLine( rule, null, width * 5 - 1, white );
            appendSimpleLine( rule, multiplyPattern( "5 5", width ), width * 5, c );
            break;
        }
        case 93: {
            appendSimpleLine( rule, null, width * 6 + 1, black );
            appendSimpleLine( rule, null, width * 6, white );
            appendSimpleLine( rule, multiplyPattern( "5 5", width ), width * 5, c );
            break;
        }
        case 97: {
            appendSimpleLine( rule, null, width, black );
            appendImageLine( rule, "97-101.svg", width * 10, c );
            break;
        }
        case 98: {
            appendImageLine( rule, "97-101.svg", width * 10, c );
            break;
        }
        case 99: {
            appendSimpleLine( rule, multiplyPattern( "0 2 1 7", width ), width, black );
            appendImageLine( rule, "97-101.svg", width * 10, c );
            break;
        }
        case 100: {
            appendSimpleLine( rule, null, width * 5, black );
            appendSimpleLine( rule, null, width * 5 - 1, white );
            appendImageLine( rule, "97-101.svg", width * 10, c );
            break;
        }
        case 101: {
            appendSimpleLine( rule, null, width * 6 + 1, black );
            appendSimpleLine( rule, null, width * 6, white );
            appendImageLine( rule, "97-101.svg", width * 10, c );
            break;
        }
        case 105: {
            appendSimpleLine( rule, null, width, black );
            appendImageLine( rule, "105-109.svg", width * 10, c );
            break;
        }
        case 106: {
            appendImageLine( rule, "105-109.svg", width * 10, c );
            break;
        }
        case 107: {
            appendSimpleLine( rule, multiplyPattern( "0 2 1 7", width ), width, black );
            appendImageLine( rule, "105-109.svg", width * 10, c );
            break;
        }
        case 108: {
            appendSimpleLine( rule, null, width * 5, black );
            appendSimpleLine( rule, null, width * 5 - 1, white );
            appendImageLine( rule, "105-109.svg", width * 10, c );
            break;
        }
        case 109: {
            appendSimpleLine( rule, null, width * 6 + 1, black );
            appendSimpleLine( rule, null, width * 6, white );
            appendImageLine( rule, "105-109.svg", width * 10, c );
            break;
        }
        case 114: {
            appendSimpleLine( rule, null, width * 3, c );
            appendImageLine( rule, "114.svg", width * 20, c );
            break;
        }
        case 115: {
            appendSimpleLine( rule, null, width * 3, c );
            appendImageLine( rule, "115.svg", width * 20, c );
            break;
        }
        case 116: {
            appendSimpleLine( rule, null, width * 3, c );
            appendImageLine( rule, "116.svg", width * 20, c );
            break;
        }
        case 117: {
            appendSimpleLine( rule, null, width * 2, c );
            appendImageLine( rule, "117.svg", width * 10, c );
            break;
        }
        case 118: {
            appendSimpleLine( rule, null, width * 5 + 1, black );
            appendSimpleLine( rule, null, width * 5, white );
            appendSimpleLine( rule, multiplyPattern( "1 5", width ), width * 10, c );
            break;
        }
        default:
            LOG.logWarning( "Ignoring fancy pen style, as it cannot be mapped to SLD." );
            break;
        }

    }

    private static final DecimalFormat formatter = new DecimalFormat( "000" );

    /**
     * @param map
     * @param doc
     * @param name
     * @throws SAXException
     * @throws IOException
     * @throws MalformedURLException
     * @throws XMLParsingException
     */
    public static void insertBrushStyle( Map<String, String> map, XMLFragment doc, String name )
                            throws MalformedURLException, IOException, SAXException, XMLParsingException {
        int pattern = parseInt( map.get( "pattern" ) );
        Color fore = decode( map.get( "forecolor" ) );
        Color back = null;
        if ( map.get( "backcolor" ) != null ) {
            back = decode( map.get( "backcolor" ) );
        }

        if ( pattern == 1 ) {
            return;
        }

        if ( pattern == 2 ) {
            Element e = getRequiredElement( doc.getRootElement(), ".//" + SLD_PREFIX + ":FeatureTypeStyle", nsContext );
            e = appendElement( e, SLDNS, SLD_PREFIX + ":Rule" );
            appendIDFilter( map.get( "styleid" ), e );
            appendElement( e, SLDNS, SLD_PREFIX + ":Name", "default:" + name );
            e = appendElement( e, SLDNS, SLD_PREFIX + ":PolygonSymbolizer" );
            e = appendElement( e, SLDNS, SLD_PREFIX + ":Fill" );
            appendElement( e, SLDNS, SLD_PREFIX + ":CssParameter", toHexColor( fore ) ).setAttribute( "name", "fill" );
            return;
        }

        ZipFile zip = new ZipFile( MIFStyle2SLD.BRUSHES );
        ZipEntry entry = zip.getEntry( formatter.format( pattern ) + ".svg" );

        XMLFragment svg = null;
        try {
            svg = new XMLFragment( new InputStreamReader( zip.getInputStream( entry ), "UTF-8" ),
                                   "http://www.systemid.org" );
            zip.close();
        } catch ( NullPointerException npe ) {
            LOG.logWarning( "Could not find brush symbol for brush number " + pattern );
            return;
        }

        updateFillPatternSVG( svg, toHexColor( fore ), back == null ? null : toHexColor( back ) );

        BufferedImage img = renderSVGImage( svg, 0 );
        File svgFile = createTempFile( "mmsvg", ".png" );
        write( img, "png", svgFile );
        svgFile.deleteOnExit();

        Element e = getRequiredElement( doc.getRootElement(), ".//" + SLD_PREFIX + ":FeatureTypeStyle", nsContext );
        e = appendElement( e, SLDNS, SLD_PREFIX + ":Rule" );
        appendIDFilter( map.get( "styleid" ), e );
        e = appendElement( e, SLDNS, SLD_PREFIX + ":PolygonSymbolizer" );
        e = appendElement( e, SLDNS, SLD_PREFIX + ":Fill" );
        e = appendElement( e, SLDNS, SLD_PREFIX + ":GraphicFill" );
        e = appendElement( e, SLDNS, SLD_PREFIX + ":Graphic" );
        e = appendElement( e, SLDNS, SLD_PREFIX + ":ExternalGraphic" );
        Element o = appendElement( e, SLDNS, SLD_PREFIX + ":OnlineResource" );
        o.setAttributeNS( XLNNS.toASCIIString(), XLINK_PREFIX + ":href", svgFile.toURI().toURL().toExternalForm() );
        o.setAttributeNS( XLNNS.toASCIIString(), XLINK_PREFIX + ":type", "simple" );
        appendElement( e, SLDNS, SLD_PREFIX + ":Format", "image/png" );
    }

    /**
     * @param map
     * @param doc
     * @throws XMLParsingException
     */
    public static void insertTextStyle( Map<String, String> map, XMLFragment doc )
                            throws XMLParsingException {
        String fontName = map.get( "fontname" );
        int styles = parseInt( map.get( "style" ) );
        String style = ( styles & 2 ) == 2 ? "italic" : "normal";
        String weight = ( styles & 1 ) == 1 ? "bold" : "normal";
        boolean halo = ( styles & 256 ) == 256;
        Color fore = decode( map.get( "forecolor" ) );
        Color back = null;
        if ( map.get( "backcolor" ) != null ) {
            back = decode( map.get( "backcolor" ) );
        }

        Element e = getRequiredElement( doc.getRootElement(), ".//" + SLD_PREFIX + ":FeatureTypeStyle", nsContext );
        e = appendElement( e, SLDNS, SLD_PREFIX + ":Rule" );
        appendIDFilter( map.get( "styleid" ), e );
        Element symbolizer = appendElement( e, SLDNS, SLD_PREFIX + ":TextSymbolizer" );
        e = appendElement( symbolizer, SLDNS, SLD_PREFIX + ":Label" );
        appendElement( e, OGCNS, OGC_PREFIX + ":PropertyName", "app:text_geometry" );
        e = appendElement( symbolizer, SLDNS, SLD_PREFIX + ":Font" );
        appendElement( e, SLDNS, SLD_PREFIX + ":CssParameter", fontName ).setAttribute( "name", "font-family" );
        appendElement( e, SLDNS, SLD_PREFIX + ":CssParameter", style ).setAttribute( "name", "font-style" );
        appendElement( e, SLDNS, SLD_PREFIX + ":CssParameter", weight ).setAttribute( "name", "font-weight" );
        appendElement( e, SLDNS, SLD_PREFIX + ":CssParameter", toHexColor( fore ) ).setAttribute( "name", "font-color" );
        e = appendElement( e, SLDNS, SLD_PREFIX + ":CssParameter", "92" ); // TODO max font size?
        e.setAttribute( "name", "font-size" );
        // Element div = appendElement( e, OGCNS, OGC_PREFIX + ":Div" );
        // appendElement( div, OGCNS, OGC_PREFIX + ":Literal", map.get( "ratio" ) );
        // appendElement( div, OGCNS, OGC_PREFIX + ":PropertyName", "app:$SCALE" );
        if ( halo ) {
            e = appendElement( symbolizer, SLDNS, SLD_PREFIX + ":Halo" );
            appendElement( e, SLDNS, SLD_PREFIX + ":Radius", "2" );
            e = appendElement( e, SLDNS, SLD_PREFIX + ":Fill" );
            e = appendElement( e, SLDNS, SLD_PREFIX + ":CssParameter", back == null ? "#ffffff" : toHexColor( back ) );
            e.setAttribute( "name", "fill" );
        }
        e = appendElement( symbolizer, SLDNS, SLD_PREFIX + ":Fill" );
        appendElement( e, SLDNS, SLD_PREFIX + ":CssParameter", toHexColor( fore ) ).setAttribute( "name", "fill" );

        e = appendElement( symbolizer, SLDNS, SLD_PREFIX + ":BoundingBox" );
        Element m = appendElement( e, SLDNS, SLD_PREFIX + ":Minx" );
        appendElement( m, OGCNS, OGC_PREFIX + ":PropertyName", "app:text_minx" );
        m = appendElement( e, SLDNS, SLD_PREFIX + ":Miny" );
        appendElement( m, OGCNS, OGC_PREFIX + ":PropertyName", "app:text_miny" );
        m = appendElement( e, SLDNS, SLD_PREFIX + ":Maxx" );
        appendElement( m, OGCNS, OGC_PREFIX + ":PropertyName", "app:text_maxx" );
        m = appendElement( e, SLDNS, SLD_PREFIX + ":Maxy" );
        appendElement( m, OGCNS, OGC_PREFIX + ":PropertyName", "app:text_maxy" );
    }

    /**
     * @param styles
     * @param name
     *            the layer name
     * @return a SLD document with temporary file references for point symbols
     */
    public XMLFragment getStyle( Map<String, HashSet<HashMap<String, String>>> styles, String name ) {
        XMLFragment doc = getSLDTemplate( name );

        HashSet<HashMap<String, String>> symbols = styles.get( "symbol" );
        if ( LOG.isDebug() ) {
            LOG.logDebug( "Found " + ( symbols == null ? "no" : symbols.size() ) + " symbol styles." );
        }
        if ( symbols != null ) {
            for ( Map<String, String> map : symbols ) {
                try {
                    insertSymbolStyle( map, doc );
                } catch ( DOMException e ) {
                    LOG.logError( "Unknown error", e );
                } catch ( IOException e ) {
                    LOG.logError( "Unknown error", e );
                } catch ( XMLParsingException e ) {
                    LOG.logError( "Unknown error", e );
                } catch ( SAXException e ) {
                    LOG.logError( "Unknown error", e );
                }
            }
        }

        HashSet<HashMap<String, String>> pens = styles.get( "pen" );
        if ( LOG.isDebug() ) {
            LOG.logDebug( "Found " + ( pens == null ? "no" : pens.size() ) + " pen styles." );
        }
        if ( pens != null ) {
            for ( Map<String, String> map : pens ) {
                try {
                    insertPenStyle( map, doc );
                } catch ( MalformedURLException e ) {
                    LOG.logError( "Unknown error", e );
                } catch ( IOException e ) {
                    LOG.logError( "Unknown error", e );
                } catch ( SAXException e ) {
                    LOG.logError( "Unknown error", e );
                } catch ( XMLParsingException e ) {
                    LOG.logError( "Unknown error", e );
                }
            }
        }

        HashSet<HashMap<String, String>> brushes = styles.get( "brush" );
        if ( LOG.isDebug() ) {
            LOG.logDebug( "Found " + ( brushes == null ? "no" : brushes.size() ) + " brushes." );
        }
        if ( brushes != null ) {
            for ( Map<String, String> map : brushes ) {
                try {
                    insertBrushStyle( map, doc, name );
                } catch ( MalformedURLException e ) {
                    LOG.logError( "Unknown error", e );
                } catch ( IOException e ) {
                    LOG.logError( "Unknown error", e );
                } catch ( SAXException e ) {
                    LOG.logError( "Unknown error", e );
                } catch ( XMLParsingException e ) {
                    LOG.logError( "Unknown error", e );
                }
            }
        }

        HashSet<HashMap<String, String>> texts = styles.get( "text" );
        if ( LOG.isDebug() ) {
            LOG.logDebug( "Found " + ( texts == null ? "no" : texts.size() ) + " texts." );
        }
        if ( texts != null ) {
            for ( Map<String, String> map : texts ) {
                try {
                    insertTextStyle( map, doc );
                } catch ( XMLParsingException e ) {
                    LOG.logError( "Unknown error", e );
                }
            }
        }

        if ( LOG.isDebug() ) {
            LOG.logDebug( "Generated SLD document", doc.getAsPrettyString() );
        }

        return doc;
    }

    /**
     * @param doc
     * @param size
     * @return an SVG image with black colors overwritten with the given colors
     */
    public static BufferedImage renderSVGImage( XMLFragment doc, int size ) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream( size * size * 4 );
        TranscoderOutput output = new TranscoderOutput( bos );

        PNGTranscoder trc = new PNGTranscoder();
        try {
            Element root = doc.getRootElement();
            TranscoderInput input = new TranscoderInput( root.getOwnerDocument() );
            if ( size > 0 ) {
                trc.addTranscodingHint( KEY_HEIGHT, new Float( size ) );
                trc.addTranscodingHint( KEY_WIDTH, new Float( size ) );
            }
            trc.transcode( input, output );
            bos.close();
            ByteArrayInputStream is = new ByteArrayInputStream( bos.toByteArray() );
            MemoryCacheSeekableStream mcss = new MemoryCacheSeekableStream( is );
            RenderedOp rop = create( "stream", mcss );
            return rop.getAsBufferedImage();
        } catch ( TranscoderException e ) {
            LOG.logError( "Unknown error", e );
        } catch ( MalformedURLException e ) {
            LOG.logError( "Unknown error", e );
        } catch ( IOException e ) {
            LOG.logError( "Unknown error", e );
        }

        return null;
    }

    /**
     * @param col
     * @return a #rrggbb string
     */
    public static String toHexColor( Color col ) {
        if ( col == null ) {
            col = black;
        }
        String scol = toHexString( col.getRGB() & 0xffffff );
        while ( scol.length() < 6 ) {
            scol = "0" + scol;
        }

        return "#" + scol;
    }

    /**
     * @param doc
     * @param stroke
     * @param fill
     * @throws XMLParsingException
     */
    public static void updateSVGColors( XMLFragment doc, String fill, String stroke )
                            throws XMLParsingException {
        List<Node> ns = getNodes( doc.getRootElement(), ".//@style", nsContext );
        for ( Node n : ns ) {
            String v = n.getTextContent();
            v = v.replace( "fill:#000000", "fill:" + fill );
            v = v.replace( "fill:black", "fill:" + fill );
            v = v.replace( "stroke:#000000", "stroke:" + stroke );
            v = v.replace( "stroke:black", "stroke:" + stroke );
            n.setTextContent( v );
        }
    }

    /**
     * @param doc
     * @param foreground
     * @param background
     * @throws XMLParsingException
     */
    public static void updateFillPatternSVG( XMLFragment doc, String foreground, String background )
                            throws XMLParsingException {
        List<Node> ns = getNodes( doc.getRootElement(), ".//@style", nsContext );
        for ( Node n : ns ) {
            String v = n.getTextContent();
            v = v.replace( "fill:#000000", "fill:" + foreground );
            v = v.replace( "fill:black", "fill:" + foreground );
            v = v.replace( "stroke:#000000", "stroke:" + foreground );
            v = v.replace( "stroke:black", "stroke:" + foreground );
            if ( background != null ) {
                v = v.replace( "fill:none", "fill:" + background );
            }
            n.setTextContent( v );
        }
    }

    /**
     * @param font
     * @param theChar
     * @param size
     * @param color
     * @return an image the char has been written onto
     */
    public static BufferedImage symbolFromFont( Font font, char theChar, int size, Color color ) {
        if ( font.canDisplay( theChar ) ) {
            BufferedImage img = new BufferedImage( size, size, TYPE_INT_ARGB );
            Graphics2D g = img.createGraphics();
            g.setFont( font.deriveFont( (float) size ) );
            g.setColor( color );
            g.drawString( theChar + "", 0, size );
            g.dispose();
            return img;
        }

        return null;
    }

    /**
     * @param font
     * @param theChar1
     * @param theChar2
     * @param size
     * @param color1
     * @param color2
     * @return an image with the second char written over the first one
     */
    public static BufferedImage symbolFromTwoChars( Font font, char theChar1, char theChar2, int size, Color color1,
                                                    Color color2 ) {
        if ( font.canDisplay( theChar1 ) && font.canDisplay( theChar2 ) ) {
            BufferedImage img = new BufferedImage( size, size, TYPE_INT_ARGB );
            Graphics2D g = img.createGraphics();
            g.setFont( font.deriveFont( (float) size ) );
            g.setColor( color1 );
            g.drawString( theChar1 + "", 0, size );
            g.setColor( color2 );
            g.drawString( theChar2 + "", 0, size );
            g.dispose();
            return img;
        }

        return null;
    }

}
