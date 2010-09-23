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
package org.deegree.client.sos.fileutils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class ReplaceClientLink {

    private static final String REPLACED_LINE = "[<a href=\"console/client/client.html\" target=\"_blank\">Generic OGC client</a>]";

    private static final String NEW_LINE = "[<a href=\"client/sos/index.html\" target=\"_blank\">SOS webclient</a>]";

    private static final String CLASSPATH_ROOT = ReplaceClientLink.class.getResource( "/" ).getPath();

    private static final String PROJECT_ROOT = new File( new File( CLASSPATH_ROOT ).getParent() ).getParent();

    private static final String CONSOLE_PATH = PROJECT_ROOT + "/src/main/webapp/console.xhtml";

    private static final String NEW_CONSOLE_PATH = PROJECT_ROOT + "/src/main/webapp/newconsole.xhtml";

    public static void main( String[] args )
                            throws IOException {

        File consoleFile = new File( CONSOLE_PATH );
        File newconsoleFile = new File( NEW_CONSOLE_PATH );
        BufferedReader reader = new BufferedReader( new FileReader( consoleFile ) );
        FileWriter writer = new FileWriter( newconsoleFile );

        String line = reader.readLine();
        while ( line != null ) {
            if ( REPLACED_LINE.equals( line.trim() ) ) {
                writer.write( NEW_LINE );
            } else {
                writer.write( line );
            }
            line = reader.readLine();
        }

        writer.close();
        reader.close();
    }
}
