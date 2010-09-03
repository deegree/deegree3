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

package org.deegree.commons.utils;

import static java.nio.charset.Charset.forName;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.deegree.commons.utils.log.LoggingNotes;
import org.slf4j.Logger;

/**
 * <code>EncodingGuesser</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(debug = "logs information about dynamically guessing the encoding of strings/files when such information is not present")
public class EncodingGuesser {

    private static final Logger LOG = getLogger( EncodingGuesser.class );

    private static final HashMap<Charset, LinkedList<HashSet<Integer>>> ENCODINGS = new HashMap<Charset, LinkedList<HashSet<Integer>>>();

    static {
        LinkedList<HashSet<Integer>> list = new LinkedList<HashSet<Integer>>();
        HashSet<Integer> set = new HashSet<Integer>();
        set.add( 196 ); // German
        set.add( 214 );
        set.add( 220 );
        set.add( 223 );
        set.add( 228 );
        set.add( 246 );
        set.add( 252 );
        list.add( new HashSet<Integer>( set ) );
        set.clear(); // Dutch
        set.add( 235 );
        set.add( 232 );
        set.add( 233 );
        list.add( new HashSet<Integer>( set ) );
        // TODO add more languages

        ENCODINGS.put( forName( "iso-8859-1" ), new LinkedList<HashSet<Integer>>( list ) );

        list.clear();
        set.clear(); // German
        set.add( 129 );
        set.add( 132 );
        set.add( 142 );
        set.add( 148 );
        set.add( 153 );
        set.add( 154 );
        set.add( 225 );
        list.add( new HashSet<Integer>( set ) );

        ENCODINGS.put( forName( "cp850" ), new LinkedList<HashSet<Integer>>( list ) );

        list.clear();
        set.clear(); // German
        set.add( 132 );
        set.add( 150 );
        set.add( 156 );
        set.add( 159 );
        set.add( 164 );
        set.add( 182 );
        set.add( 188 );
        set.add( 195 );
        list.add( new HashSet<Integer>( set ) );
        // TODO add more languages

        ENCODINGS.put( forName( "utf8" ), new LinkedList<HashSet<Integer>>( list ) );
    }

    /**
     * @param map
     * @return the guessed charset, or null
     * @throws UnsupportedEncodingException
     *             if you don't have i18n.jar from Sun's JDK, I guess
     */
    public static Charset guess( Map<Integer, Integer> map )
                            throws UnsupportedEncodingException {
        if ( map.isEmpty() ) {
            return forName( "ascii" );
        }

        TreeMap<Double, Charset> ratios = new TreeMap<Double, Charset>();

        for ( Charset charset : ENCODINGS.keySet() ) {
            for ( HashSet<Integer> set : ENCODINGS.get( charset ) ) {
                int sum = 0;
                for ( Integer i : set ) {
                    if ( map.containsKey( i ) ) {
                        sum += map.get( i );
                    }
                }

                int otherSum = 0;
                for ( Integer i : map.keySet() ) {
                    if ( !set.contains( i ) ) {
                        otherSum += map.get( i );
                    }
                }

                LOG.debug( "For encoding '" + charset.displayName() + "' we have " + sum + " matches, and " + otherSum
                           + " mismatches." );

                if ( otherSum == 0 ) {
                    return charset;
                }

                if ( sum != 0 ) {
                    ratios.put( (double) otherSum / (double) sum, charset );
                }
            }
        }

        if ( ratios.size() > 0 ) {
            Entry<Double, Charset> first = ratios.firstEntry();
            Double key = first.getKey();
            Charset val = first.getValue();
            LOG.debug( "Guessing encoding '" + val.displayName() + "' with a mismatch ratio of " + key );
            LOG.debug( "Map was:" );
            for ( Integer i : map.keySet() ) {
                LOG.debug( i + " (" + new String( new byte[] { i.byteValue() }, "iso-8859-1" ) + "): " + map.get( i ) );
            }

            return val;
        }

        LOG.debug( "Unknown encoding:" );
        for ( Integer i : map.keySet() ) {
            LOG.debug( i + " (" + new String( new byte[] { i.byteValue() }, "iso-8859-1" ) + "): " + map.get( i ) );
        }

        return null;
    }

    /**
     * @param in
     * @return the guessed encoding, or null, if none was determined
     * @throws IOException
     * @throws UnsupportedEncodingException
     *             if you don't have i18n.jar from Sun's JDK, I guess
     */
    public static Charset guess( InputStream in )
                            throws IOException {
        TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>();

        int b;
        while ( ( b = in.read() ) != -1 ) {
            if ( b < 128 ) {
                continue;
            }

            if ( map.containsKey( b ) ) {
                map.put( b, map.get( b ) + 1 );
            } else {
                map.put( b, 1 );
            }
        }

        return guess( map );
    }

    /**
     * @param bs
     * @return the guessed encoding, or null
     * @throws UnsupportedEncodingException
     *             if you don't have i18n.jar from Sun's JDK, I guess
     */
    public static Charset guess( byte[] bs )
                            throws UnsupportedEncodingException {
        TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>();

        for ( byte b : bs ) {
            if ( b < 128 ) {
                continue;
            }

            if ( map.containsKey( (int) b ) ) {
                map.put( (int) b, map.get( b ) + 1 );
            } else {
                map.put( (int) b, 1 );
            }
        }

        return guess( map );
    }

}
