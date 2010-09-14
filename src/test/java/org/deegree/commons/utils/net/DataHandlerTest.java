//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.commons.utils.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLConnection;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DataHandlerTest extends TestCase {

    private static String retrieveAll( DURL url )
                            throws IOException {
        InputStreamReader is = null;
        try {
            URLConnection conn = url.getURL().openConnection();
            String enc = conn.getContentEncoding();
            if ( enc != null ) {
                is = new InputStreamReader( conn.getInputStream(), enc );
            } else {
                is = new InputStreamReader( conn.getInputStream(), "US-ASCII" );
            }
            BufferedReader in = new BufferedReader( is );
            StringBuilder sb = new StringBuilder();
            String s;
            while ( ( s = in.readLine() ) != null ) {
                sb.append( s );
            }
            return sb.toString();
        } finally {
            if ( is != null ) {
                try {
                    is.close();
                } catch ( IOException e ) {
                    // probably already closed
                }
            }
        }
    }

    /**
     * @throws IOException
     */
    public void testAll()
                            throws IOException {
        assertEquals( retrieveAll( new DURL( "data:text/plain;charset=utf-8;base64,QnVpbGRpbmdGZWF0dXJl" ) ),
                      "BuildingFeature" );
    }

    /**
     * @throws IOException
     */
    public void testOnlyCharset()
                            throws IOException {
        assertEquals( retrieveAll( new DURL( "data:;charset=utf-8;base64,QnVpbGRpbmdGZWF0dXJl" ) ), "BuildingFeature" );
    }

    /**
     * @throws IOException
     */
//    public void testNoEncoding()
//                            throws IOException {
//        assertEquals( retrieveAll( new DURL( "data:text/plain;charset=utf-8,mytest%c3%a1accent" ) ), "mytestáaccent" );
//    }

    /**
     * @throws IOException
     */
    public void testOnlyEncoding()
                            throws IOException {
        assertEquals( retrieveAll( new DURL( "data:;base64,QnVpbGRpbmdGZWF0dXJl" ) ), "BuildingFeature" );
    }

    /**
     * @throws IOException
     */
    public void testOnlyData()
                            throws IOException {
        assertEquals( retrieveAll( new DURL( "data:,mytestnoaccent" ) ), "mytestnoaccent" );
    }

    /**
     * 
     */
    public void testInvalidEncoding() {
        try {
            retrieveAll( new DURL( "data:nix,mytest%c3%a1accent" ) );
        } catch ( IOException e ) {
            String msg = "The 'nix' encoding is not supported by the data URL. Only base64 and the default"
                         + " of url-encoded is allowed.";
            assertEquals( e.getMessage(), msg );
            return;
        }
        fail( "Expected an exception." );
    }

}
