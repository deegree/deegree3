//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de

 ---------------------------------------------------------------------------*/
package org.deegree.commons.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class contains static utility methods for handling files and filenames.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author: $
 * 
 * @version $Revision: $, $Date: $
 * 
 */
public class FileUtils {

    private static Log LOG = LogFactory.getLog( FileUtils.class );

    /**
     * Returns the filename, without any extension. (Eg. /tmp/foo.txt -> /tmp/foo)
     * 
     * @param file
     * @return the basename
     */
    public static String getBasename( File file ) {
        return splitFilename( file ).first;
    }

    /**
     * Returns the file extension (Eg. /tmp/foo.txt -> txt)
     * 
     * @param file
     * @return the file extension
     */
    public static String getFileExtension( File file ) {
        return splitFilename( file ).second;
    }

    /**
     * Split a filename in basename and extension.
     * 
     * @param file
     * @return a StringPair with basename and extension
     */
    private static StringPair splitFilename( File file ) {
        String filename = file.getName();
        File path = file.getParentFile();
        int pos = filename.lastIndexOf( "." );
        if ( pos != -1 ) {
            String basename = filename.substring( 0, pos );
            String suffix = filename.substring( pos + 1 );
            if ( path != null ) {
                basename = path.getPath() + File.separator + basename;
            }
            return new StringPair( basename, suffix );
        }
        return new StringPair( file.getPath(), "" );
    }

    /**
     * Writes the given {@link String} to the specified file.
     * 
     * @param file
     *            file to write to
     * @param content
     */
    void writeFile( File file, String content ) {
        LOG.debug( "Writing debug file '" + file.getAbsolutePath() + "'." );
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter( new FileWriter( file ) );
            writer.write( content );
        } catch ( IOException e ) {
            LOG.error( "Could not write to debug file '" + file.getAbsolutePath() + "'.", e );
        } finally {
            if ( writer != null ) {
                try {
                    writer.close();
                } catch ( IOException e ) {
                    LOG.error( "Error closing debug file '" + file.getAbsolutePath() + "'.", e );
                }
            }
        }
    }

    /**
     * Writes the given {@link String} to a temporary file (created from specified prefix and suffix).
     * 
     * @see File#createTempFile(String, String)
     * @param filePrefix
     *            prefix for the temp file name, must be at least three characters long
     * @param fileSuffix
     *            suffix for the temp file name, can be null (then ".tmp" is used)
     * @param content
     */
    void writeTempFile( String filePrefix, String fileSuffix, String content ) {
        try {
            File tmpFile = File.createTempFile( filePrefix, fileSuffix );
            writeFile( tmpFile, content );
        } catch ( IOException e ) {
            LOG.error( "Cannot create debug file for prefix '" + filePrefix + "' and suffix '" + fileSuffix + ".", e );
        }
    }

    /**
     * Writes the given binary data to the specified file.
     * 
     * @param file
     *            file to write to
     * @param data
     *            binary data to be written
     */
    void writeBinaryFile( File file, byte[] data ) {
        LOG.debug( "Writing binary debug file '" + file.getAbsolutePath() + "'." );
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream( new FileOutputStream( file ) );
            out.write( data );
        } catch ( IOException e ) {
            LOG.error( "Could not write to debug file '" + file.getAbsolutePath() + "'.", e );
        } finally {
            if ( out != null ) {
                try {
                    out.close();
                } catch ( IOException e ) {
                    LOG.error( "Error closing debug file '" + file.getAbsolutePath() + "'.", e );
                }
            }
        }
    }

    /**
     * Writes the given binary data to a temporary file (created from specified prefix and suffix).
     * 
     * @see File#createTempFile(String, String)
     * @param filePrefix
     *            prefix for the temp file name, must be at least three characters long
     * @param fileSuffix
     *            suffix for the temp file name, can be null (then ".tmp" is used)
     * @param data
     *            binary data to be written
     */
    void writeBinaryTempFile( String filePrefix, String fileSuffix, byte[] data ) {
        try {
            File tmpFile = File.createTempFile( filePrefix, fileSuffix );
            writeBinaryFile( tmpFile, data );
        } catch ( IOException e ) {
            LOG.error( "Cannot create debug file for prefix '" + filePrefix + "' and suffix '" + fileSuffix + ".", e );
        }
    }
}
