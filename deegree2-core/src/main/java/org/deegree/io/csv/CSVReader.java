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

package org.deegree.io.csv;

import static java.io.StreamTokenizer.TT_EOF;
import static java.lang.Double.parseDouble;
import static java.util.Collections.unmodifiableList;
import static org.deegree.datatypes.Types.VARCHAR;
import static org.deegree.framework.log.LoggerFactory.getLogger;
import static org.deegree.io.mapinfoapi.MapInfoReader.whitespaceChars;
import static org.deegree.io.mapinfoapi.MapInfoReader.wordChars;
import static org.deegree.model.feature.FeatureFactory.createFeature;
import static org.deegree.model.feature.FeatureFactory.createFeatureCollection;
import static org.deegree.model.feature.FeatureFactory.createFeatureProperty;
import static org.deegree.model.feature.FeatureFactory.createFeatureType;
import static org.deegree.model.feature.FeatureFactory.createGeometryPropertyType;
import static org.deegree.model.feature.FeatureFactory.createSimplePropertyType;
import static org.deegree.model.spatialschema.GeometryFactory.createPoint;
import static org.deegree.model.spatialschema.WKTAdapter.wrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;

/**
 * <code>CSVReader</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class CSVReader {

    private static final ILogger LOG = getLogger( CSVReader.class );

    private File fileName;

    private int xcol = 0, ycol = 1, wkt = -1;

    private static URI APPNS;

    private List<String[]> header;

    private boolean ignoreFirstLine, parseGeometryProperty = true;

    static {
        try {
            APPNS = new URI( "http://www.deegree.org/app" );
        } catch ( URISyntaxException e ) {
            // yes, cannot happen
        }
    }

    /**
     * @param name
     * @param ignoreFirstLine
     * @throws IOException
     */
    public CSVReader( String name, boolean ignoreFirstLine ) throws IOException {
        this.ignoreFirstLine = ignoreFirstLine;

        fileName = new File( name ).getAbsoluteFile();

        header = new ArrayList<String[]>( 3 );

        BufferedReader in = new BufferedReader( new FileReader( name ) );
        String str = in.readLine();
        char separat = determineSeparator( str );
        do {
            List<String> lst = parseLine( str, separat );
            header.add( lst.toArray( new String[lst.size()] ) );
        } while ( ( ( str = in.readLine() ) != null ) && header.size() < 3 );
        in.close();
    }

    /**
     * @return max. the first three lines of the file (if there are three)
     */
    public List<String[]> getHeader() {
        return unmodifiableList( header );
    }

    /**
     * By default, a geometry property will be parsed. Set this to false to get "simple property only" features.
     *
     * @param parseGeometryProperty
     */
    public void setParseGeometryProperty( boolean parseGeometryProperty ) {
        this.parseGeometryProperty = parseGeometryProperty;
    }

    private static char determineSeparator( String s ) {
        // determine most likely separator
        int ccount = countChars( s, ',' );
        int scount = countChars( s, ';' );
        int tcount = countChars( s, '\t' );
        if ( ccount >= scount && ccount >= tcount ) {
            return ',';
        }
        if ( tcount >= ccount && tcount >= scount ) {
            return '\t';
        }
        if ( scount >= ccount && scount >= tcount ) {
            return ';';
        }
        return ',';
    }

    private static List<String> parseLine( String line, char separator )
                            throws IOException {
        String seps = ",;\t";
        for ( int i = 0; i < seps.length(); ++i ) {
            if ( line.startsWith( "" + seps.charAt( i ) ) ) {
                line = "\"\"" + line;
            }
            String dseps = "" + seps.charAt( i ) + seps.charAt( i );
            while ( line.indexOf( dseps ) != -1 ) {
                line = line.replace( dseps, seps.charAt( i ) + "\"\"" + seps.charAt( i ) );
            }
        }
        StreamTokenizer tok = getCSVFromStringTokenizer( line, separator );

        LinkedList<String> list = new LinkedList<String>();

        tok.nextToken();
        if ( tok.ttype == TT_EOF ) {
            return list;
        }
        while ( tok.ttype != TT_EOF ) {
            list.add( tok.sval );
            tok.nextToken();
        }

        return list;
    }

    /**
     * Also sets wkt to -1.
     *
     * @param x
     * @param y
     */
    public void setPointColumns( int x, int y ) {
        xcol = x;
        ycol = y;
        wkt = -1;
    }

    /**
     * @param wkt
     *            if -1, x/y will be used instead
     */
    public void setWKTColumn( int wkt ) {
        this.wkt = wkt;
    }

    /**
     * @param input
     * @param separator
     * @return a tokenizer with a stringreader as data input
     */
    public static StreamTokenizer getCSVFromStringTokenizer( String input, char separator ) {
        StreamTokenizer tok = new StreamTokenizer( new StringReader( input ) );

        tok.resetSyntax();
        tok.eolIsSignificant( true );
        tok.lowerCaseMode( true );
        tok.slashSlashComments( false );
        tok.slashStarComments( false );
        tok.wordChars( 'a', 'z' );
        tok.wordChars( 'A', 'Z' );
        tok.wordChars( '\u00a0', '\u00ff' );
        tok.wordChars( '0', '9' );
        wordChars( tok, ',', '\t', ';' );
        wordChars( tok, '.', '-', '_', ' ', '+', '/', '\\', '(', ')', '^' );
        tok.quoteChar( '"' );
        whitespaceChars( tok, '\n', '\r', '\f' );

        // reset separator
        whitespaceChars( tok, separator );

        return tok;
    }

    private static int countChars( String s, char c ) {
        int count = 0;
        for ( int i = 0; i < s.length(); ++i ) {
            if ( s.charAt( i ) == c ) {
                ++count;
            }
        }
        return count;
    }

    /**
     * @return a new feature collection
     * @throws IOException
     */
    public FeatureCollection parseFeatureCollection()
                            throws IOException {
        FeatureCollection fc = createFeatureCollection( "uniquemy_", 512 );
        QualifiedName geomName = new QualifiedName( "app:geometry", APPNS );
        QualifiedName featureName = new QualifiedName( "app:feature", APPNS );

        int counter = 0;

        BufferedReader in = new BufferedReader( new FileReader( fileName ) );
        String str = in.readLine();
        List<String> colNames = null;

        char separator = determineSeparator( str );
        if ( ignoreFirstLine ) {
            colNames = parseLine( str, separator );
            str = in.readLine();
        }
        outer: do {
            LOG.logDebug( "Trying to parse line ", str );
            List<String> vals = parseLine( str, separator );

            double x = 0, y = 0;
            Geometry wktGeom = null;
            LinkedList<FeatureProperty> fps = new LinkedList<FeatureProperty>();
            LinkedList<PropertyType> fpt = new LinkedList<PropertyType>();

            for ( int i = 0; i < vals.size(); ++i ) {

                if ( parseGeometryProperty && wkt == -1 && i == xcol ) {
                    try {
                        x = parseDouble( vals.get( i ) );
                    } catch ( NumberFormatException nfe ) {
                        // puh, CSV is an easy format? I think not...
                        try {
                            x = parseDouble( vals.get( i ).replace( ",", "." ) );
                        } catch ( NumberFormatException nfe2 ) {
                            LOG.logWarning( "Skipping line " + str );
                            continue outer;
                        }
                    }
                    continue;
                }
                if ( parseGeometryProperty && wkt == -1 && i == ycol ) {
                    if ( vals.get( i ).equals( "" ) ) {
                        y = 0; // this seems to be a sensible (Java-like) default
                    } else {
                        try {
                            y = parseDouble( vals.get( i ) );
                        } catch ( NumberFormatException nfe ) {
                            // puh, CSV is an easy format? I think not...
                            try {
                                y = parseDouble( vals.get( i ).replace( ",", "." ) );
                            } catch ( NumberFormatException nfe2 ) {
                                LOG.logWarning( "Skipping line " + str );
                                continue outer;
                            }
                        }
                    }
                    continue;
                }
                if ( parseGeometryProperty && wkt != -1 && i == wkt ) {
                    try {
                        wktGeom = wrap( vals.get( i ), null );
                    } catch ( GeometryException e ) {
                        LOG.logError( "Invalid WKT geometry", e );
                    }
                    if ( wktGeom == null ) {
                        LOG.logError( "Could not parse WKT geometry: " + vals.get( i ) );
                    }
                    continue;
                }

                String n;
                if ( ignoreFirstLine ) {
                    String coln = colNames.get( i );
                    n = "app:" + ( coln.trim().equals( "" ) ? "property" + i : coln );
                } else {
                    n = "app:property" + i;
                }
                n = n.replace( ' ', '_' );
                QualifiedName name = new QualifiedName( n, APPNS );
                fps.add( createFeatureProperty( name, vals.get( i ) ) );
                fpt.add( createSimplePropertyType( name, VARCHAR, true ) );
            }

            if ( parseGeometryProperty ) {
                if ( wkt != -1 && wktGeom != null ) {
                    fps.add( createFeatureProperty( geomName, wktGeom ) );
                } else {
                    fps.add( createFeatureProperty( geomName, createPoint( x, y, null ) ) );
                }
                fpt.add( createGeometryPropertyType( geomName, null, 1, 1 ) );
            }

            FeatureType tp = createFeatureType( featureName, false, fpt.toArray( new PropertyType[fpt.size()] ) );
            fc.add( createFeature( ++counter + "", tp, fps ) );
        } while ( ( ( str = in.readLine() ) != null ) );

        in.close();

        // makes sense (?)
        if ( fc.size() > 0 ) {
            fc.setFeatureType( fc.getFeature( 0 ).getFeatureType() );
        }

        return fc;
    }
}
