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
package org.deegree.commons.utils.kvp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class contains convenience methods for working with key-value pair maps (e.g. from OGC KVP requests).
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class KVPUtils {

    /**
     * Returns the value of the key. Throws an exception if the kvp map doesn't contain the key.
     * 
     * @param param
     *            the key-value map
     * @param key
     * @return the value
     * @throws MissingParameterException
     *             if the kvp map doesn't contain the key
     */
    public static String getRequired( Map<String, String> param, String key )
                            throws MissingParameterException {
        String value = param.get( key );
        if ( value == null || value.length() == 0) {
            throw new MissingParameterException( "Required parameter '" + key + "' is missing.", key );
        }
        return value;
    }

    /**
     * @param param
     * @param key
     * @return the parsed value as an integer
     * @throws MissingParameterException
     *             if the kvp map doesn't contain the key
     * @throws InvalidParameterValueException
     *             if the value is not an integer
     */
    public static int getRequiredInt( Map<String, String> param, String key )
                            throws MissingParameterException, InvalidParameterValueException {
        String value = getRequired( param, key );
        try {
            return Integer.parseInt( value );
        } catch ( NumberFormatException e ) {
            throw new InvalidParameterValueException( "The value of parameter '" + key
                                                      + "' must be an integer, but was '" + value + "'." );
        }
    }

    /**
     * @param param
     * @param key
     * @return the parsed value as a double
     * @throws MissingParameterException
     *             if the kvp map doesn't contain the key
     * @throws InvalidParameterValueException
     *             if the value is not a double
     */
    public static double getRequiredDouble( Map<String, String> param, String key )
                            throws MissingParameterException, InvalidParameterValueException {
        String value = getRequired( param, key );
        try {
            return Double.parseDouble( value );
        } catch ( NumberFormatException e ) {
            throw new InvalidParameterValueException( "The value of parameter '" + key
                                                      + "' must be a double, but was '" + value + "'." );
        }
    }

    /**
     * @param param
     * @param key
     * @param defaultValue
     *            to be used if missing.
     * @return the parsed value as a double or if the given key was not found or the value was not a number, the default
     *         value.
     */
    public static double getDefaultDouble( Map<String, String> param, String key, double defaultValue ) {
        double result = defaultValue;
        String value = param.get( key );
        if ( value != null ) {
            try {
                result = Double.parseDouble( value );
            } catch ( NumberFormatException e ) {
                // wanted.
            }
        }
        return result;
    }

    /**
     * Returns the value of the key or the default value if the kvp map doesn't contain the key.
     * 
     * @param param
     *            the key-value map
     * @param key
     * @param defaultValue
     * @return the value
     */
    public static String getDefault( Map<String, String> param, String key, String defaultValue ) {
        String value = param.get( key );
        if ( value == null ) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Return a list with all values for a key. Values are splitted if they contain multiple comma delimited values.
     * 
     * @param param
     *            the key-value map
     * @param key
     * @return a list with all values
     */
    public static List<String> splitAll( Map<String, String> param, String key ) {
        List<String> result = new LinkedList<String>();
        String value = param.get( key );
        if ( value != null ) {
            for ( String part : value.split( "," ) ) {
                result.add( part );
            }
        }
        return result;
    }

    /**
     * Returns the specified parameter from a KVP map as a boolean value.
     * 
     * @param kvpParams
     *            KVP map
     * @param paramName
     *            name of the parameter
     * 
     * @param defaultValue
     *            returned when the specified parameter is not present in the map (=null)
     * @return the specified parameter value as a boolean value
     * @throws InvalidParameterValueException
     *             if the parameter value is neither null nor "true" nor "false"
     */
    public static boolean getBoolean( Map<String, String> kvpParams, String paramName, boolean defaultValue )
                            throws InvalidParameterValueException {
        boolean result = defaultValue;
        String booleanString = kvpParams.get( paramName );
        if ( booleanString != null ) {
            if ( booleanString.equals( "true" ) ) {
                result = true;
            } else if ( booleanString.equals( "false" ) ) {
                result = false;
            } else {
                String msg = "Parameter '" + paramName + "' must either be 'true' or 'false', but is '" + booleanString
                             + "'.";
                throw new InvalidParameterValueException( msg );
            }
        }
        return result;
    }

    /**
     * Returns the specified parameter from a KVP map as an integer value.
     * 
     * @param kvpParams
     *            KVP map
     * @param paramName
     *            name of the parameter
     * @param defaultValue
     *            returned when the specified parameter is not present in the map (=null)
     * @return the specified parameter value as an integer value
     * @throws InvalidParameterValueException
     *             if the parameter value does not denote an integer
     */
    public static int getInt( Map<String, String> kvpParams, String paramName, int defaultValue )
                            throws InvalidParameterValueException {
        int result = defaultValue;
        String s = kvpParams.get( paramName );
        if ( s != null ) {
            try {
                result = Integer.parseInt( s );
            } catch ( NumberFormatException e ) {
                throw new InvalidParameterValueException( "The value of parameter '" + paramName
                                                          + "' must be an integer, but was '" + s + "'." );
            }
        }
        return result;
    }

    /**
     * Reads a text file with KVP content into a map.
     * <p>
     * Example contents:
     * 
     * <pre>
     * SERVICE=WFS
     * VERSION=1.1.0
     * REQUEST=DescribeFeatureType
     * TYPENAME=TreesA_1M
     * </pre>
     * 
     * What this method does:
     * <ul>
     * <li>Every line is split around the '=' character, the first part is used as the key, the second part as the value
     * (if the line doesn't contain a '=', it is ignored).</li>
     * <li>Keys are uppercased.</li>
     * <li>Values are URL decoded.</li>
     * </ul>
     * </p>
     * 
     * @param url
     *            url of the text file
     * @return map with the contents of the file, keys are uppercased
     * @throws IOException
     *             if the the file cannot be loaded
     */
    public static Map<String, String> readFileIntoMap( URL url )
                            throws IOException {
        BufferedReader reader = new BufferedReader( new InputStreamReader( url.openStream() ) );

        String line = null;
        Map<String, String> params = new HashMap<String, String>();
        while ( ( line = reader.readLine() ) != null ) {
            if ( line.contains( "=" ) ) {
                String[] parts = line.split( "=" );
                if ( parts[0].equalsIgnoreCase( "FILTER" ) ) {
                    params.put( "FILTER", URLDecoder.decode( line.substring( 7 ), "UTF-8" ) );
                } else if ( parts[0].equalsIgnoreCase( "NAMESPACE" ) ) {
                    params.put( "NAMESPACE", line.substring( 10 ) );
                } else if ( parts.length == 2 ) {
                    params.put( parts[0].toUpperCase(), URLDecoder.decode( parts[1], "UTF-8" ) );
                } else {
                    throw new IllegalArgumentException();
                }
            }
        }
        return params;
    }
}
