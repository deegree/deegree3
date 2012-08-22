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

import static java.net.URLDecoder.decode;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;

/**
 * offeres utility method for transformating a key-value-pair encoded request to a <tt>Map</tt>
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version 1.0. $Revision$, $Date$
 * 
 * @since 2.0
 */
public class KVP2Map {

    private static final ILogger LOG = LoggerFactory.getLogger( KVP2Map.class );

    /**
     * Transforms a String/KVPs like it is used for HTTP-GET request to a Map.
     * 
     * TODO: Check if the trim () call may cause side effects. It is currently used to eliminate possible new line
     * characters at the end of the string, that occured using the <code>GenericClient</code>.
     * 
     * @param kvp
     *            key-value-pair encoded request
     * @return created Map
     */
    public static Map<String, String> toMap( String kvp ) {

        StringTokenizer st = new StringTokenizer( kvp.trim(), "&?" );
        HashMap<String, String> map = new HashMap<String, String>();

        while ( st.hasMoreTokens() ) {
            String s = st.nextToken();
            if ( s != null ) {
                int pos = s.indexOf( '=' );

                if ( pos > -1 ) {
                    String s1 = s.substring( 0, pos );
                    String s2 = s.substring( pos + 1, s.length() );
                    map.put( s1.toUpperCase(), s2 );
                }
            }
        }

        return map;

    }

    /**
     * @param iterator
     *            Enumeration containing KVP encoded parameters
     * @return created Map
     */
    public static Map<String, String> toMap( Enumeration<String> iterator ) {
        HashMap<String, String> map = new HashMap<String, String>();

        while ( iterator.hasMoreElements() ) {
            String s = iterator.nextElement();
            if ( s != null ) {
                int pos = s.indexOf( '=' );

                if ( pos > -1 ) {
                    String s1 = s.substring( 0, pos );
                    String s2 = s.substring( pos + 1, s.length() );
                    map.put( s1.toUpperCase(), s2 );
                }
            }
        }

        return map;
    }

    /**
     * @see #toMap(HttpServletRequest)
     * @param request
     * @return a Map which contains kvp's from the given request
     */
    public static Map<String, String> toMap( HttpServletRequest request ) {
        return toMap( (ServletRequest)request );
    }
    
    /**
     * returns the parameters of a <tt>HttpServletRequest</tt> as <tt>Map</tt>. (HINT: all the keys get changed to upper
     * case)
     * 
     * @param request
     * @return a Map which contains kvp's from the given request
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> toMap( ServletRequest request ) {
        Map<String, String> result = null;
        // encoding heuristics for URL encoding
        // if %c3 is found (a sign of UTF-8 encoding) parse it manually, setting the encoding right
        String queryString = ( (HttpServletRequest) request ).getQueryString();
        if ( queryString != null && queryString.toLowerCase().indexOf( "%c3" ) != -1 ) {
            result = new TreeMap<String, String>();
            try {
                for ( String kv : queryString.split( "&" ) ) {
                    String[] pair = kv.split( "=", 2 );
                    if ( pair.length == 2 ) {
                        result.put( decode( pair[0], "UTF-8" ).toUpperCase(), decode( pair[1], "UTF-8" ) );
                    }
                }
            } catch ( UnsupportedEncodingException e ) {
                LOG.logError( "Unknown error", e );
            }
        } else {
            // according to javax.servlet.* documentation, the type is correct
            Map<String, String[]> map = request.getParameterMap();
            result = new HashMap<String, String>();
            for ( String key : map.keySet() ) {
                String[] tmp = map.get( key );
                for ( int i = 0; i < tmp.length; i++ ) {
                    tmp[i] = tmp[i].trim();
                }
                result.put( key.toUpperCase(), StringTools.arrayToString( tmp, ',' ) );
            }

        }
        return result;
    }

}
