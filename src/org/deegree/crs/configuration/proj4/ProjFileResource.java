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

package org.deegree.crs.configuration.proj4;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.deegree.crs.CRSCodeType;
import org.deegree.crs.configuration.resources.CRSResource;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.coordinatesystems.GeographicCRS;
import org.deegree.crs.exceptions.CRSConfigurationException;
import org.deegree.crs.transformations.Transformation;
import org.deegree.crs.transformations.helmert.Helmert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.deegree.crs.i18n.Messages;

/**
 * The <code>ProjFileResource</code> class TODO add class documentation here.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class ProjFileResource implements CRSResource<Map<String, String>> {

    private Map<CRSCodeType, Map<String, String>> idToParams = new HashMap<CRSCodeType, Map<String, String>>( 4000 );

    private static Logger LOG = LoggerFactory.getLogger( ProjFileResource.class );

    /**
     * @param provider
     * @param properties
     */
    public ProjFileResource( PROJ4CRSProvider provider, Properties properties ) {
        String fileName = properties.getProperty( "crs.configuration" );
        try {
            BufferedReader reader = new BufferedReader( new FileReader( fileName ) );
            String line = reader.readLine();
            Map<String, String> kvp = new HashMap<String, String>( 15 );
            int lineNumber = 1;
            while ( line != null ) {
                if ( line.startsWith( "#" ) ) {
                    // remove the '#' from the String.
                    if ( kvp.get( "comment" ) != null ) {
                        LOG.debug( "(Line: " + lineNumber + ") Multiple comments found, removing previous: "
                                      + kvp.get( "comment" ) );
                    }
                    kvp.put( "comment", line.substring( 1 ).trim() );
                } else {
                    String code = parseConfigString( line, Integer.toString( lineNumber ), kvp );
                    if ( code != null && !"".equals( code.trim() ) ) {
                        LOG.debug( "Found code: " + CRSCodeType.valueOf( code ) + " with following params: " + kvp );
                        idToParams.put( CRSCodeType.valueOf( code ), kvp );
                    }
                    kvp = new HashMap<String, String>( 15 );
                }
                line = reader.readLine();
                lineNumber++;
            }
            reader.close();

        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            LOG.error( "Could not open file: " + fileName, e );
            throw new CRSConfigurationException( e );
            // e.printStackTrace();
        }
    }

    public Helmert getWGS84Transformation( GeographicCRS sourceCRS ) {
        if ( sourceCRS == null ) {
            return null;
        }

        // convert codes to ids
        CRSCodeType[] codes = sourceCRS.getCodes();
        String[] ids = new String[ codes.length ];
        for ( int i = 0; i < ids.length; i++ )
            ids[i] = codes[i].getOriginal();

        for ( String id : ids ) {
            Map<String, String> params = null;
            try {
                params = getURIAsType( id );
            } catch ( IOException e ) {
                throw new CRSConfigurationException( e );
            }
            if ( params != null ) {
                return createWGS84ConversionInfo( sourceCRS, params );
            }
        }
        return null;
    }

    public Map<String, String> getURIAsType( String uri )
                            throws IOException {
        String tmpID = getIDCode( uri );
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Given id: " + uri + " converted into: " + tmpID );
        }
        Map<String, String> result = idToParams.get( tmpID );
        if ( result != null ) {
            result = new HashMap<String, String>( result );
        }
        return result;
    }

    /**
     * @return a set containing all available ids.
     */
    public Set<CRSCodeType> getAvailableCodes() {
        return idToParams.keySet();
    }

    /**
     * Creating the wgs84 aka BursaWolf conversion parameters. Either 3 or 7 parameters are supported.
     *
     * @param params
     *            to get the towgs84 param from
     * @return the conversion info from the params or <code>null<code> if no conversion info is available.
     * @throws CRSConfigurationException
     *             if the number of params are not 3 or 7.
     */
    private Helmert createWGS84ConversionInfo( CoordinateSystem sourceCRS, Map<String, String> params )
                            throws CRSConfigurationException {
        Helmert result = null;
        String tmpValue = params.remove( "towgs84" );
        if ( tmpValue != null && !"".equals( tmpValue.trim() ) ) {
            double[] values = null;
            String[] splitter = tmpValue.trim().split( "," );
            if ( splitter != null && splitter.length > 0 ) {
                values = new double[splitter.length];
                for ( int i = 0; i < splitter.length; ++i ) {
                    values[i] = Double.parseDouble( splitter[i] );
                }
            }
            if ( values != null ) {
                String description = "Handmade proj4 towgs84 definition (parsed from nad/epsg) used by crs with id: "
                                     + sourceCRS.getCode() + "identifier";
                String name = "Proj4 defined toWGS84 params";

                String id = Transformation.createFromTo( sourceCRS.getCode().getOriginal(), GeographicCRS.WGS84.getCode().getOriginal() );

                if ( values.length == 3 ) {
                    result = new Helmert( values[0], values[1], values[2], 0, 0, 0, 0, sourceCRS, GeographicCRS.WGS84,
                                          CRSCodeType.valueOf( id ), name, sourceCRS.getVersion(), description, sourceCRS.getAreaOfUse() );
                } else if ( values.length == 7 ) {
                    result = new Helmert( values[0], values[1], values[2], values[3], values[4], values[5], values[6],
                                          sourceCRS, GeographicCRS.WGS84, CRSCodeType.valueOf( id ), name, sourceCRS.getVersion(),
                                          description, sourceCRS.getAreaOfUse() );
                } else {
                    throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PROJ4_WGS84_PARAMS",
                                                                              sourceCRS.getCode() + "identifier",
                                                                              Integer.toString( values.length ) ) );
                }
            }
        }
        return result;
    }

    /**
     * Parses the configured proj4 parameters from the given String using a StreamTokenizer and saves them in the Map.
     *
     * @param params
     *            to be parsed
     * @param lineNumber
     *            in the config file.
     * @param kvp
     *            in which the key-value pairs will be saved.
     * @return the parsed Identifier or <code>null</code> if no identifier was found.
     * @throws IOException
     *             if the StreamTokenizer finds an error.
     * @throws CRSConfigurationException
     *             If the config was malformed.
     */
    private String parseConfigString( String params, String lineNumber, Map<String, String> kvp )
                            throws IOException, CRSConfigurationException {
        BufferedReader br = new BufferedReader( new StringReader( params ) );
        StreamTokenizer t = new StreamTokenizer( br );
        t.commentChar( '#' );
        t.ordinaryChars( '0', '9' );
        // t.ordinaryChars( '.', '.' );
        t.ordinaryChar( '.' );
        // t.ordinaryChars( '-', '-' );
        t.ordinaryChar( '-' );
        // t.ordinaryChars( '+', '+' );
        t.ordinaryChar( '+' );
        t.wordChars( '0', '9' );
        t.wordChars( '\'', '\'' );
        t.wordChars( '"', '"' );
        t.wordChars( '_', '_' );
        t.wordChars( '.', '.' );
        t.wordChars( '-', '-' );
        t.wordChars( '+', '+' );
        t.wordChars( ',', ',' );
        t.nextToken();
        String identifier = null;
        /**
         * PROJ4 type definitions have following format, <number> +proj .... So the first type must start with an '<'
         */
        if ( t.ttype == '<' ) {
            t.nextToken();
            // if the next value is not a word, e.g. a number (see t.wordChars('0','9')) it is the
            // wrong format.
            if ( t.ttype != StreamTokenizer.TT_WORD ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PROJ4_INVALID_ID", lineNumber,
                                                                          "An identifier (e.g. number)", "<",
                                                                          getTokenizerSymbolToString( t.ttype ) ) );
            }
            // it's a word so get the identifier.
            identifier = t.sval;
            //
            kvp.put( "identifier", identifier );
            t.nextToken();

            // check for closing bracket.
            if ( t.ttype != '>' ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PROJ4_MISSING_EXPECTED_CHAR",
                                                                          lineNumber, ">" ) );
            }
            t.nextToken();

            // get the parameters.
            while ( t.ttype != '<' ) {
                if ( t.ttype == '+' ) {
                    t.nextToken();
                }
                if ( t.ttype != StreamTokenizer.TT_WORD ) {
                    throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PROJ4_INVALID_ID",
                                                                              lineNumber, "A parameter", "+",
                                                                              getTokenizerSymbolToString( t.ttype ) ) );
                }
                String key = t.sval;
                if ( key != null && !"".equals( key ) ) {
                    if ( key.startsWith( "+" ) ) {
                        key = key.substring( 1 );
                    }
                    t.nextToken();
                    if ( t.ttype == '=' ) {
                        t.nextToken();
                        if ( t.ttype != StreamTokenizer.TT_WORD ) {
                            throw new CRSConfigurationException(
                                                                 Messages.getMessage(
                                                                                      "CRS_CONFIG_PROJ4_INVALID_ID",
                                                                                      lineNumber,
                                                                                      "A Value",
                                                                                      "=",
                                                                                      getTokenizerSymbolToString( t.ttype ) ) );
                        }
                        String value = t.sval;
                        LOG.debug( "Putting key: " + key + " with value: " + value );
                        kvp.put( key, value );
                        // take the next token.
                        t.nextToken();
                    }
                }
            }
            t.nextToken();
            if ( t.ttype != '>' ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PROJ4_MISSING_EXPECTED_CHAR",
                                                                          lineNumber, "<> (End of defintion)" ) );
            }
        } else {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PROJ4_MISSING_EXPECTED_CHAR",
                                                                      lineNumber, "< (Start of defintion)" ) );
        }
        br.close();
        return identifier;
    }

    /**
     * Creates a helpfull string of the given StreamTokenizer value.
     *
     * @param val
     *            an int gotten from streamTokenizer.ttype.
     * @return a human readable String.
     */
    private String getTokenizerSymbolToString( int val ) {
        String result = new String( "" + val );
        if ( val == StreamTokenizer.TT_EOF ) {
            result = "End of file";
        } else if ( val == StreamTokenizer.TT_EOL ) {
            result = "End of line";
        } else if ( val == StreamTokenizer.TT_NUMBER ) {
            result = "A number with value: " + val;
        }
        return result;
    }

    /**
     * removes any strings in front of the last number.
     *
     * @param id
     *            to be normalized.
     * @return the number of the id, or id if ':' or '#' is not found.
     */
    private String getIDCode( String id ) {
        if ( id == null || "".equals( id.trim() ) ) {
            return id;
        }
        int count = id.lastIndexOf( ":" );
        if ( count == -1 ) {
            count = id.lastIndexOf( "#" );
            if ( count == -1 ) {
                return id;
            }
        }
        return id.substring( count + 1 );
    }

    public Transformation getTransformation( CoordinateSystem sourceCRS, CoordinateSystem targetCRS ) {
        LOG.error( "Parsing of transformations is currently not supported for the Proj4 file resource." );
        return null;
    }

}
