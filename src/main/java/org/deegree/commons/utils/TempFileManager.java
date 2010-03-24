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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages temporary file system resources for deegree.
 * <p>
 * This class ensures that each running deegree application (e.g. a service webapp or a command line application) uses a
 * separate directory for storing temporary resources. For webapps, the webapp context name is used to build a unique
 * directory.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class TempFileManager {

    private static final Logger LOG = LoggerFactory.getLogger( TempFileManager.class );

    private static File baseDir;

    /**
     * Initializes the {@link TempFileManager} to use the given String for creating unique temporary file names.
     * 
     * @param contextId
     *            string to use for the unique file names, must not be <code>null</code>
     */
    public static synchronized void init( String contextId ) {
        if ( baseDir != null ) {
            String msg = "Cannot initialize TempResourceManager for context '" + contextId + "'. Already initialized";
            throw new RuntimeException( msg );
        }
        // encode it, so no invalid characters are used for the directory name
        try {
            // quirk to get nicer looking names for the common contextId=webapp case
            if ( contextId.startsWith( "/" ) && contextId.length() > 1 ) {
                contextId = contextId.substring( 1 );
            }
            String safeName = URLEncoder.encode( contextId, "UTF-8" );
            String tmpDir = System.getProperty( "java.io.tmpdir" );
            baseDir = new File( tmpDir, "deegree-" + safeName );
            if ( !baseDir.exists() ) {
                baseDir.mkdir();
            } else if ( !baseDir.isDirectory() ) {
                String msg = "'" + baseDir + "' does not denote a directory. Please delete it and restart.";
                throw new RuntimeException( msg );
            }
        } catch ( UnsupportedEncodingException e ) {
            LOG.error( "Internal error: Cannot encode '" + contextId + "' for creating unique tempdir." );
        }
        LOG.info( "Using '" + baseDir + "' for storing temporary files." );
    }

    /**
     * Initializes the {@link TempFileManager} to use a random String for creating unique temporary file names.
     */
    public static synchronized void init() {
        String contextId = UUID.randomUUID().toString();
        init( contextId );
    }

    /**
     * Returns the base directory to be used for storing temporary files.
     * 
     * @return the base directory, never <code>null</code>
     */
    public static synchronized File getBaseDir() {
        if ( baseDir == null ) {
            init();
        }
        return baseDir;
    }
}
