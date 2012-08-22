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

import static java.io.StreamTokenizer.TT_EOF;
import static java.io.StreamTokenizer.TT_EOL;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.nio.charset.Charset.defaultCharset;
import static java.nio.charset.Charset.forName;
import static org.deegree.datatypes.Types.BOOLEAN;
import static org.deegree.datatypes.Types.DATE;
import static org.deegree.datatypes.Types.DOUBLE;
import static org.deegree.datatypes.Types.FLOAT;
import static org.deegree.datatypes.Types.GEOMETRY;
import static org.deegree.datatypes.Types.INTEGER;
import static org.deegree.datatypes.Types.VARCHAR;
import static org.deegree.model.feature.FeatureFactory.createFeature;
import static org.deegree.model.feature.FeatureFactory.createFeatureCollection;
import static org.deegree.model.feature.FeatureFactory.createFeatureProperty;
import static org.deegree.model.feature.FeatureFactory.createFeatureType;
import static org.deegree.model.feature.FeatureFactory.createGeometryPropertyType;
import static org.deegree.model.feature.FeatureFactory.createSimplePropertyType;
import static org.deegree.model.spatialschema.GeometryFactory.createPoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.Pair;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.spatialschema.Curve;
import org.deegree.model.spatialschema.MultiPoint;
import org.deegree.model.spatialschema.MultiSurface;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.Surface;

/**
 * <code>MapInfoReader</code> is a new implementation of a reader for the MID/MIF format, based on
 * the MapInfo 8.5 documentation.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class MapInfoReader {

    private static final ILogger LOG = LoggerFactory.getLogger( MapInfoReader.class );

    private static final URI APPNS;

    private StreamTokenizer mif;

    private StreamTokenizer mid;

    private FeatureType featureType;

    private char delimiter;

    private File mifFile;

    private File midFile;

    private FeatureCollection featureCollection;

    private LinkedList<Feature> features;

    private MIFGeometryParser parser;

    private MIFStyleParser styleParser;

    private HashMap<String, HashSet<HashMap<String, String>>> styles;

    private boolean featureTypeParsed, featuresParsed;

    static {
        URI u = null;
        try {
            u = new URI( "http://www.deegree.org/app" );
        } catch ( URISyntaxException e ) {
            // eat it
        }
        APPNS = u;
    }

    /**
     * @param baseName
     *            the base name of the file(s), may also end with .mif/.mid
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     */
    public MapInfoReader( String baseName ) throws FileNotFoundException, UnsupportedEncodingException {
        if ( baseName.toLowerCase().endsWith( ".mid" ) || baseName.toLowerCase().endsWith( ".mif" ) ) {
            baseName = baseName.substring( 0, baseName.length() - 4 );
            LOG.logDebug( "Reading base name of ", baseName );
        }

        midFile = new File( baseName + ".mid" );
        if ( !midFile.exists() ) {
            midFile = new File( baseName + ".MID" );
        }
        mifFile = new File( baseName + ".mif" );
        if ( !mifFile.exists() ) {
            mifFile = new File( baseName + ".MIF" );
        }

        mif = getMIFTokenizer( mifFile, null );
        mid = getMIDTokenizer( midFile, null );
    }

    /**
     * Was that so hard?
     *
     * @param chars
     * @param tok
     */
    public static void wordChars( StreamTokenizer tok, char... chars ) {
        for ( char c : chars ) {
            tok.wordChars( c, c );
        }
    }

    /**
     * Was that so hard?
     *
     * @param chars
     * @param tok
     */
    public static void quoteChars( StreamTokenizer tok, char... chars ) {
        for ( char c : chars ) {
            tok.quoteChar( c );
        }
    }

    /**
     * Was that so hard?
     *
     * @param chars
     * @param tok
     */
    public static void whitespaceChars( StreamTokenizer tok, char... chars ) {
        for ( char c : chars ) {
            tok.whitespaceChars( c, c );
        }
    }

    // applies heuristics to work around "try what your mapinfo says" in the documentation
    private static Charset getCharset( String charset ) {
        if ( charset == null ) {
            charset = defaultCharset().name();
            LOG.logDebug( "Parsing with default charset " + charset + " until charset directive is found." );
        } else if ( charset.equals( "WindowsLatin1" ) ) {
            LOG.logDebug( "Parsing with charset " + charset + ", interpreting it as iso-8859-1." );
            charset = "iso-8859-1";
        } else {
            LOG.logDebug( "Parsing with unknown charset " + charset + ", hoping Java knows the name. " );
        }

        return forName( charset );
    }

    /**
     * @param file
     * @param charset
     * @return a stream tokenizer with some specific settings for reading mif files
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public static StreamTokenizer getMIFTokenizer( File file, String charset )
                            throws FileNotFoundException, UnsupportedEncodingException {
        charset = getCharset( charset ).name();

        StreamTokenizer tok = new StreamTokenizer( new InputStreamReader( new FileInputStream( file ), charset ) );

        tok.resetSyntax();
        tok.eolIsSignificant( false );
        tok.lowerCaseMode( true );
        tok.slashSlashComments( false );
        tok.slashStarComments( false );
        tok.wordChars( 'a', 'z' );
        tok.wordChars( 'A', 'Z' );
        tok.wordChars( '\u00a0', '\u00ff' );
        tok.wordChars( '0', '9' );
        wordChars( tok, '.', '-', '_' );
        quoteChars( tok, '\'', '"' );
        whitespaceChars( tok, ' ', '\n', '\r', '\f', '\t', '(', ')', ',' );

        return tok;
    }

    /**
     * @param file
     * @param charset
     * @return a stream tokenizer for reading MIF files
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public static StreamTokenizer getMIDTokenizer( File file, String charset )
                            throws FileNotFoundException, UnsupportedEncodingException {
        charset = getCharset( charset ).name();

        StreamTokenizer tok = new StreamTokenizer( new InputStreamReader( new FileInputStream( file ), charset ) );

        tok.resetSyntax();
        tok.eolIsSignificant( true );
        tok.lowerCaseMode( true );
        tok.slashSlashComments( false );
        tok.slashStarComments( false );
        tok.wordChars( 'a', 'z' );
        tok.wordChars( 'A', 'Z' );
        tok.wordChars( '\u00a0', '\u00ff' );
        tok.wordChars( '0', '9' );
        wordChars( tok, '.', '-', '_' );
        tok.quoteChar( '"' );
        whitespaceChars( tok, ' ', '\n', '\r', '\f', '\t', '(', ')', ',' );

        return tok;
    }

    /**
     * @param mif
     * @return a list of tokens, without the comma
     * @throws IOException
     */
    public static LinkedList<String> parseCommaList( StreamTokenizer mif )
                            throws IOException {
        LinkedList<String> list = new LinkedList<String>();

        mif.ordinaryChar( ',' );
        mif.nextToken();

        list.add( mif.sval );

        while ( mif.nextToken() == ',' ) {
            mif.nextToken();
            list.add( mif.sval );
        }

        whitespaceChars( mif, ',' );

        return list;
    }

    /**
     * @throws IOException
     */
    public void parseFeatureType()
                            throws IOException {
        // don't parse it twice
        if ( featureTypeParsed ) {
            return;
        }

        delimiter = '\t';

        mid.ordinaryChar( delimiter );

        LinkedList<PropertyType> propertyTypes = new LinkedList<PropertyType>();
        propertyTypes.add( createGeometryPropertyType( new QualifiedName( "app", "geometry", APPNS ), null, 0, 1 ) );

        while ( mif.nextToken() != TT_EOF ) {
            if ( mif.sval.equals( "version" ) ) {
                mif.nextToken();
                LOG.logDebug( "File version is " + mif.sval );
                if ( mif.sval.compareTo( "650" ) > 0 ) {
                    LOG.logWarning( "Parsing an unknown version of " + mif.sval + "." );
                }
                continue;
            }

            if ( mif.sval.equals( "charset" ) ) {
                mif.nextToken();
                String charset = mif.sval;
                // get new tokenizer, skip everything until the charset and continue
                mif = getMIFTokenizer( mifFile, charset );
                mid = getMIDTokenizer( midFile, charset );
                mid.ordinaryChar( delimiter );
                mif.nextToken();
                while ( !mif.sval.equals( "charset" ) ) {
                    mif.nextToken();
                }
                mif.nextToken();
                continue;
            }

            if ( mif.sval.equals( "delimiter" ) ) {
                mif.nextToken();
                delimiter = mif.sval.charAt( 0 );
                mid.ordinaryChar( delimiter );
                continue;
            }

            if ( mif.sval.equals( "unique" ) ) {
                mif.nextToken();
                LOG.logWarning( "Ignoring unique directive." );
                continue;
            }

            if ( mif.sval.equals( "index" ) ) {
                LOG.logWarning( "Ignoring but parsing index directive." );
                mif.nextToken();
                while ( true ) {
                    try {
                        parseInt( mif.sval );
                        mif.nextToken();
                    } catch ( NumberFormatException e ) {
                        mif.pushBack();
                        break;
                    }
                }

                continue;
            }

            if ( mif.sval.equals( "coordsys" ) ) {
                LOG.logWarning( "Ignoring coordsys directive." );
                mif.nextToken();
                if ( mif.sval.equals( "window" ) ) {
                    mif.nextToken(); // window_id
                    continue;
                }

                if ( mif.sval.equals( "table" ) ) {
                    mif.nextToken(); // tablename
                    continue;
                }

                if ( mif.sval.equals( "layout" ) ) {
                    mif.nextToken(); // Units
                    mif.nextToken(); // paperunitname
                    continue;
                }

                if ( mif.sval.equals( "nonearth" ) ) {
                    mif.nextToken(); // Units or Affine
                    if ( mif.sval.equals( "affine" ) ) {
                        mif.nextToken(); // Units
                        mif.nextToken(); // unitname
                        mif.nextToken(); // A
                        mif.nextToken(); // B
                        mif.nextToken(); // C
                        mif.nextToken(); // D
                        mif.nextToken(); // E
                        mif.nextToken(); // F

                        mif.nextToken(); // Units
                    }
                    mif.nextToken(); // unitname
                    mif.nextToken(); // Bounds
                    mif.nextToken(); // minx
                    mif.nextToken(); // miny
                    mif.nextToken(); // maxx
                    mif.nextToken(); // maxy
                    continue;
                }

                if ( mif.sval.equals( "earth" ) ) {
                    mif.nextToken();
                    if ( mif.sval.equals( "projection" ) ) {
                        parseCommaList( mif );
                    }

                    if ( !mif.sval.equals( "affine" ) && !mif.sval.equals( "bounds" ) ) {
                        mif.pushBack();
                    }

                    if ( mif.sval.equals( "affine" ) ) {
                        mif.nextToken(); // Units
                        mif.nextToken(); // unitname
                        mif.nextToken(); // A
                        mif.nextToken(); // B
                        mif.nextToken(); // C
                        mif.nextToken(); // D
                        mif.nextToken(); // E
                        mif.nextToken(); // F
                    }

                    if ( mif.sval.equals( "bounds" ) ) {
                        mif.nextToken(); // minx
                        mif.nextToken(); // miny
                        mif.nextToken(); // maxx
                        mif.nextToken(); // maxy
                    }

                    continue;
                }
            }

            if ( mif.sval.equals( "transform" ) ) {
                mif.nextToken(); // four transformation parameters
                mif.nextToken();
                mif.nextToken();
                mif.nextToken();
            }

            if ( mif.sval.equals( "columns" ) ) {
                mif.nextToken();
                int cnt = parseInt( mif.sval );
                for ( int i = 0; i < cnt; ++i ) {
                    mif.lowerCaseMode( false );
                    mif.nextToken();
                    String name = mif.sval;
                    mif.lowerCaseMode( true );
                    mif.nextToken();
                    String type = mif.sval;
                    if ( type.equals( "integer" ) ) {
                        propertyTypes.add( createSimplePropertyType( new QualifiedName( "app", name, APPNS ), INTEGER,
                                                                     true ) );
                    }
                    if ( type.equals( "smallint" ) ) {
                        propertyTypes.add( createSimplePropertyType( new QualifiedName( "app", name, APPNS ), INTEGER,
                                                                     true ) );
                    }
                    if ( type.equals( "float" ) ) {
                        propertyTypes.add( createSimplePropertyType( new QualifiedName( "app", name, APPNS ), FLOAT,
                                                                     true ) );
                    }
                    if ( type.equals( "date" ) ) {
                        propertyTypes.add( createSimplePropertyType( new QualifiedName( "app", name, APPNS ), DATE,
                                                                     true ) );
                    }
                    if ( type.equals( "logical" ) ) {
                        propertyTypes.add( createSimplePropertyType( new QualifiedName( "app", name, APPNS ), BOOLEAN,
                                                                     true ) );
                    }
                    if ( type.equals( "char" ) ) {
                        mif.nextToken(); // size, is ignored (using varchar)
                        propertyTypes.add( createSimplePropertyType( new QualifiedName( "app", name, APPNS ), VARCHAR,
                                                                     true ) );
                    }
                    if ( type.equals( "decimal" ) ) {
                        // specifications are ignored, just using double
                        mif.nextToken(); // width
                        mif.nextToken(); // decimals
                        propertyTypes.add( createSimplePropertyType( new QualifiedName( "app", name, APPNS ), DOUBLE,
                                                                     true ) );
                    }
                }
                continue;
            }

            if ( mif.sval.equals( "data" ) ) {
                propertyTypes.add( createSimplePropertyType( new QualifiedName( "app", "styleid", APPNS ), VARCHAR,
                                                             true ) );
                propertyTypes.add( createSimplePropertyType( new QualifiedName( "app", "text_geometry", APPNS ),
                                                             VARCHAR, true ) );
                propertyTypes.add( createSimplePropertyType( new QualifiedName( "app", "text_minx", APPNS ), VARCHAR,
                                                             true ) );
                propertyTypes.add( createSimplePropertyType( new QualifiedName( "app", "text_miny", APPNS ), VARCHAR,
                                                             true ) );
                propertyTypes.add( createSimplePropertyType( new QualifiedName( "app", "text_maxx", APPNS ), VARCHAR,
                                                             true ) );
                propertyTypes.add( createSimplePropertyType( new QualifiedName( "app", "text_maxy", APPNS ), VARCHAR,
                                                             true ) );

                featureType = createFeatureType( new QualifiedName( "app", "someName", APPNS ), false,
                                                 propertyTypes.toArray( new PropertyType[propertyTypes.size()] ) );

                featureTypeParsed = true;
                return;
            }

            LOG.logWarning( "Spurious token: " + mif.sval );

        }
    }

    /**
     * @throws IOException
     */
    public void parseFeatures()
                            throws IOException {
        if ( featuresParsed ) {
            return;
        }

        parseFeatureType();

        mif.pushBack();
        while ( mif.nextToken() != TT_EOF ) {

            if ( mif.sval.equals( "data" ) ) {
                mif.nextToken();

                getFeatures();

                featureCollection = createFeatureCollection( "parsedFeatureCollection",
                                                             features.toArray( new Feature[features.size()] ) );
                featuresParsed = true;
            }

            LOG.logWarning( "Spurious token: " + mif.sval );

        }

    }

    private Pair<FeatureProperty, HashMap<String, HashMap<String, String>>> parseGeometry( CoordinateSystem crs,
                                                                                           QualifiedName name )
                            throws IOException {
        if ( mif.sval.equals( "none" ) ) {
            mif.nextToken();
            LOG.logWarning( "A null geometry was found." );
            return null;
        }

        HashMap<String, HashMap<String, String>> map = new HashMap<String, HashMap<String, String>>();
        Pair<FeatureProperty, HashMap<String, HashMap<String, String>>> pair;
        pair = new Pair<FeatureProperty, HashMap<String, HashMap<String, String>>>();
        pair.second = map;

        if ( mif.sval.equals( "point" ) ) {
            Point p = parser.parsePoint();

            HashMap<String, String> symbol = styleParser.parseSymbol();
            if ( symbol != null ) {
                map.put( "symbol", symbol );
            }

            pair.first = createFeatureProperty( name, p );

            return pair;
        }

        if ( mif.sval.equals( "multipoint" ) ) {
            MultiPoint mp = parser.parseMultipoint();

            HashMap<String, String> symbol = styleParser.parseSymbol();
            if ( symbol != null ) {
                map.put( "symbol", symbol );
            }

            pair.first = createFeatureProperty( name, mp );

            return pair;
        }

        if ( mif.sval.equals( "line" ) ) {
            Curve l = parser.parseLine();

            HashMap<String, String> pen = styleParser.parsePen();
            if ( pen != null ) {
                map.put( "pen", pen );
            }

            pair.first = createFeatureProperty( name, l );

            return pair;
        }

        if ( mif.sval.equals( "pline" ) ) {
            Curve c = parser.parsePLine();

            HashMap<String, String> pen = styleParser.parsePen();
            if ( pen != null ) {
                map.put( "pen", pen );
            }

            if ( mif.sval != null && mif.sval.equals( "smooth" ) ) {
                LOG.logWarning( "Smoothing is not supported, since it uses proprietary ad-hoc algorithms." );
                mif.nextToken();
            }

            pair.first = createFeatureProperty( name, c );

            return pair;
        }

        if ( mif.sval.equals( "region" ) ) {
            MultiSurface ms = parser.parseRegion();

            HashMap<String, String> pen = styleParser.parsePen();
            if ( pen != null ) {
                map.put( "pen", pen );
            }

            HashMap<String, String> brush = styleParser.parseBrush();
            if ( brush != null ) {
                map.put( "brush", brush );
            }

            if ( mif.sval != null && mif.sval.equals( "center" ) ) {
                LOG.logWarning( "Custom centroid settings are not supported." );
                mif.nextToken();
                mif.nextToken();
                mif.nextToken();
            }

            pair.first = createFeatureProperty( name, ms );

            return pair;
        }

        if ( mif.sval.equals( "arc" ) ) {
            parser.parseArc();

            HashMap<String, String> pen = styleParser.parsePen();
            if ( pen != null ) {
                map.put( "pen", pen );
            }

            return null;
        }

        if ( mif.sval.equals( "roundrect" ) ) {
            parser.parseRoundRect();

            HashMap<String, String> pen = styleParser.parsePen();
            if ( pen != null ) {
                map.put( "pen", pen );
            }

            HashMap<String, String> brush = styleParser.parseBrush();
            if ( brush != null ) {
                map.put( "brush", brush );
            }

            return null;
        }

        if ( mif.sval.equals( "ellipse" ) ) {
            parser.parseEllipse();

            HashMap<String, String> pen = styleParser.parsePen();
            if ( pen != null ) {
                map.put( "pen", pen );
            }

            HashMap<String, String> brush = styleParser.parseBrush();
            if ( brush != null ) {
                map.put( "brush", brush );
            }

            return null;
        }

        if ( mif.sval.equals( "rect" ) ) {
            Surface s = parser.parseRect();

            HashMap<String, String> pen = styleParser.parsePen();
            if ( pen != null ) {
                map.put( "pen", pen );
            }

            HashMap<String, String> brush = styleParser.parseBrush();
            if ( brush != null ) {
                map.put( "brush", brush );
            }

            pair.first = createFeatureProperty( name, s );

            return pair;
        }

        if ( mif.sval.equals( "collection" ) ) {
            LOG.logDebug( "Parsing collection..." );
            LOG.logWarning( "Collections are not understood and will be ignored. This will break the parsing!" );
            mif.nextToken();
            return null;
        }

        if ( mif.sval.equals( "text" ) ) {
            LOG.logDebug( "Parsing text..." );
            LOG.logWarning( "Text geometries will be parsed as points." );

            mif.nextToken();
            String text = mif.sval;
            mif.nextToken();

            double x1 = parseDouble( mif.sval );
            mif.nextToken();
            double y1 = parseDouble( mif.sval );
            mif.nextToken();

            double x2 = parseDouble( mif.sval );
            mif.nextToken();
            double y2 = parseDouble( mif.sval );
            mif.nextToken();

            HashMap<String, String> style = styleParser.parseText();
            if ( style == null ) {
                style = new HashMap<String, String>();
            }

            style.put( "minx", "" + x1 );
            style.put( "miny", "" + y1 );
            style.put( "maxx", "" + x2 );
            style.put( "maxy", "" + y2 );
            style.put( "text", text );
            map.put( "text", style );

            Point p = createPoint( x1, y1, crs );

            pair.first = createFeatureProperty( name, p );

            return pair;
        }

        LOG.logWarning( "Unknown construct: " + mif.sval );
        mif.nextToken();

        return null;
    }

    // mif stream is kept at beginning of next geometry
    private void getFeatures()
                            throws IOException {
        CoordinateSystem crs = null; // TODO

        final QualifiedName styleName = new QualifiedName( "app", "styleid", APPNS );
        final QualifiedName textName = new QualifiedName( "app", "text_geometry", APPNS );
        final QualifiedName minxName = new QualifiedName( "app", "text_minx", APPNS );
        final QualifiedName minyName = new QualifiedName( "app", "text_miny", APPNS );
        final QualifiedName maxxName = new QualifiedName( "app", "text_maxx", APPNS );
        final QualifiedName maxyName = new QualifiedName( "app", "text_maxy", APPNS );
        int styleNum = 0;

        int id = 0;

        features = new LinkedList<Feature>();

        DateFormat df = new SimpleDateFormat( "yyyymmdd" );

        parser = new MIFGeometryParser( mif, crs );
        styleParser = new MIFStyleParser( mif, this.mifFile.getParentFile() );

        styles = new HashMap<String, HashSet<HashMap<String, String>>>();

        while ( mif.ttype != TT_EOF ) {
            LinkedList<FeatureProperty> properties = new LinkedList<FeatureProperty>();

            PropertyType[] ps = featureType.getProperties();
            // skip the styleid, text, and minx, miny, maxx, maxy for the text
            for ( int i = 0; i < ps.length - 6; ++i ) {
                String field = null;
                if ( i != 0 ) { // 0 is the geometry
                    StringBuffer sb = new StringBuffer();
                    while ( mid.nextToken() != delimiter && mid.ttype != TT_EOL && mid.ttype != TT_EOF ) {
                        sb.append( mid.sval );
                    }
                    field = sb.toString();
                }

                switch ( ps[i].getType() ) {
                case GEOMETRY: {
                    Pair<FeatureProperty, HashMap<String, HashMap<String, String>>> pair;
                    pair = parseGeometry( crs, ps[i].getName() );
                    if ( pair != null && pair.first != null ) {
                        properties.add( pair.first );

                        String usedStyle = null;

                        // update styles map
                        for ( String key : pair.second.keySet() ) {
                            HashMap<String, String> ss;
                            ss = pair.second.get( key );

                            if ( ss != null ) {
                                if ( key.equals( "text" ) ) {
                                    String minx = ss.remove( "minx" );
                                    String miny = ss.remove( "miny" );
                                    String maxx = ss.remove( "maxx" );
                                    String maxy = ss.remove( "maxy" );
                                    String text = ss.remove( "text" );
                                    properties.add( createFeatureProperty( textName, text ) );
                                    properties.add( createFeatureProperty( minxName, minx ) );
                                    properties.add( createFeatureProperty( minyName, miny ) );
                                    properties.add( createFeatureProperty( maxxName, maxx ) );
                                    properties.add( createFeatureProperty( maxyName, maxy ) );
                                }

                                if ( styles.get( key ) == null ) {
                                    HashSet<HashMap<String, String>> set = new HashSet<HashMap<String, String>>();
                                    set.add( ss );
                                    styles.put( key, set );
                                    ss.put( "styleid", "" + styleNum );
                                    if ( usedStyle == null ) {
                                        usedStyle = "" + styleNum++;
                                    } else {
                                        usedStyle = usedStyle + "_" + styleNum++;
                                    }
                                } else {
                                    HashSet<HashMap<String, String>> set = styles.get( key );
                                    if ( !set.contains( ss ) ) {
                                        // check for same style, but different ID
                                        boolean found = false;
                                        String styleid = null;
                                        for ( HashMap<String, String> m : set ) {
                                            HashMap<String, String> woid = new HashMap<String, String>( m );
                                            woid.remove( "styleid" );
                                            if ( woid.equals( ss ) ) {
                                                found = true;
                                                styleid = m.get( "styleid" );
                                                break;
                                            }
                                        }
                                        if ( !found ) {
                                            set.add( ss );
                                            ss.put( "styleid", "" + styleNum );
                                            if ( usedStyle == null ) {
                                                usedStyle = "" + styleNum++;
                                            } else {
                                                usedStyle = usedStyle + "_" + styleNum++;
                                            }
                                        } else {
                                            if ( usedStyle == null ) {
                                                usedStyle = "" + styleid;
                                            } else {
                                                usedStyle = usedStyle + "_" + styleid;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if ( usedStyle != null ) {
                            properties.add( createFeatureProperty( styleName, usedStyle ) );
                        }
                    }
                    continue;
                }
                case INTEGER: {
                    Integer val = Integer.valueOf( field );
                    properties.add( createFeatureProperty( ps[i].getName(), val ) );
                    continue;
                }
                case FLOAT: {
                    Float val = Float.valueOf( field );
                    properties.add( createFeatureProperty( ps[i].getName(), val ) );
                    continue;
                }
                case DOUBLE: {
                    Double val = Double.valueOf( field );
                    properties.add( createFeatureProperty( ps[i].getName(), val ) );
                    continue;
                }
                case DATE: {
                    Date val = null;
                    try {
                        val = df.parse( field );
                        properties.add( createFeatureProperty( ps[i].getName(), val ) );
                    } catch ( ParseException e ) {
                        // ignore it
                        LOG.logWarning( "A date value could not be parsed." );
                    }
                    continue;
                }
                case BOOLEAN: {
                    Boolean val = Boolean.valueOf( field );
                    properties.add( createFeatureProperty( ps[i].getName(), val ) );
                    continue;
                }
                case VARCHAR: {
                    String val = field;
                    properties.add( createFeatureProperty( ps[i].getName(), val ) );
                    continue;
                }
                }
            }

            features.add( createFeature( "" + id++, featureType, properties ) );

        }

    }

    /**
     * @return the feature collection (null if it has not been parsed yet)
     */
    public FeatureCollection getFeatureCollection() {
        return featureCollection;
    }

    /**
     * @return the styles (null, if they've not been parsed yet)
     */
    public HashMap<String, HashSet<HashMap<String, String>>> getStyles() {
        return styles;
    }

    /**
     * @return the feature type, or null, if it has not been parsed yet
     */
    public FeatureType getFeatureType() {
        return featureType;
    }

    /**
     * @return a list of geometry errors
     */
    public LinkedList<String> getErrors() {
        return parser.errors;
    }

}
