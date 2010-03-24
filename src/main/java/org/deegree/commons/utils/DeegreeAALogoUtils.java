//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.commons.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides utility methods for outputting/logging the deegree 3 ascii art logo.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DeegreeAALogoUtils {

    private static Logger LOG = LoggerFactory.getLogger( DeegreeAALogoUtils.class );

    private static List<String> lines = new LinkedList<String>();

    private static String DEEGREE_AA_LOGO_FILE = "deegree_aa_logo.txt";

    static {
        try {
            InputStream is = DeegreeAALogoUtils.class.getResourceAsStream( DEEGREE_AA_LOGO_FILE );
            BufferedReader reader = new BufferedReader( new InputStreamReader( is, "UTF-8" ) );
            String line = null;
            while ( ( line = reader.readLine() ) != null ) {
                lines.add( line );
            }
        } catch ( Exception e ) {
            LOG.error( "Could not read deegree logo '" + DEEGREE_AA_LOGO_FILE + "'." );
        }
    }

    /**
     * Writes the logo to the given <code>Logger</code>, using the <code>Info</code> log level.
     * 
     * @param log
     *            <code>Logger</code> to write to
     */
    public static void logInfo( Logger log ) {
        for ( String line : lines ) {
            log.info( line );
        }
    }

    /**
     * Writes the logo to the given <code>Writer</code>.
     * 
     * @param writer
     *            <code>Writer</code> to write to
     */
    public static void print( Writer writer ) {
        try {
            for ( String line : lines ) {
                writer.write( line );
                writer.write( '\n' );
            }
        } catch ( IOException e ) {
            // be gentle and silent (it's not that important after all)
        }
    }

    /**
     * Writes the logo to the given <code>OutputStream</code>.
     * 
     * @param os
     *            <code>OutputStream</code> to write to
     */
    public static void print( OutputStream os ) {
        try {
            for ( String line : lines ) {
                os.write( line.getBytes() );
                os.write( '\n' );
            }
        } catch ( IOException e ) {
            // be gentle and silent (it's not that important after all)
        }
    }

    /**
     * Returns the logo as a single string, with newline separators.
     *
     * @return the logo as a single string
     */
    public static String getAsString () {
        StringBuffer sb = new StringBuffer();
        for ( String line : lines ) {
            sb.append (line);
            sb.append ('\n');
        }
        return sb.toString();
    }
}
