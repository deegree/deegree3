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

import java.io.File;

import java.io.IOException;

import org.slf4j.Logger;

/**
 * This class contains static utility methods for writing files when a log is set to debug.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author: $
 * 
 * @version $Revision: $, $Date: $
 * 
 */
public class LogUtils {
    /**
     * Writes the given {@link String} to the specified file, if the log level is set to debug. *
     * 
     * @param log
     *            the log to check for the log level
     * @param file
     *            file to write to
     * @param content
     */
    public static void writeFile( Logger log, File file, String content ) {
        if ( log.isDebugEnabled() ) {
            log.debug( "Writing debug file '" + file.getAbsolutePath() + "'." );
            FileUtils.writeFile( file, content );
        }
    }

    /**
     * Writes the given {@link String} to a temporary file (created from specified prefix and suffix), if the log level
     * is set to debug.
     * 
     * @see File#createTempFile(String, String) *
     * @param log
     *            the log to check for the log level
     * @param filePrefix
     *            prefix for the temp file name, must be at least three characters long
     * @param fileSuffix
     *            suffix for the temp file name, can be null (then ".tmp" is used)
     * @param content
     */
    public static void writeTempFile( Logger log, String filePrefix, String fileSuffix, String content ) {
        if ( log.isDebugEnabled() ) {
            try {
                File file = File.createTempFile( filePrefix, fileSuffix );
                writeFile( log, file, content );
            } catch ( IOException e ) {
                log.error( "Cannot create temporary file for prefix '" + filePrefix + "' and suffix '" + fileSuffix
                           + ".", e );
            }
        }
    }

    /**
     * Writes the given binary data to the specified file, if the log level is set to debug.
     * 
     * @param log
     *            the log to check for the log level
     * @param file
     *            file to write to
     * @param data
     *            binary data to be written
     */
    public static void writeBinaryFile( Logger log, File file, byte[] data ) {
        if ( log.isDebugEnabled() ) {
            log.debug( "Writing binary debug file '" + file.getAbsolutePath() + "'." );
            FileUtils.writeBinaryFile( file, data );
        }
    }

    /**
     * Writes the given binary data to a temporary file (created from specified prefix and suffix), if the log level is
     * set to debug.
     * 
     * @see File#createTempFile(String, String) *
     * @param log
     *            the log to check for the log level
     * @param filePrefix
     *            prefix for the temp file name, must be at least three characters long
     * @param fileSuffix
     *            suffix for the temp file name, can be null (then ".tmp" is used)
     * @param data
     *            binary data to be written
     */
    public static void writeBinaryTempFile( Logger log, String filePrefix, String fileSuffix, byte[] data ) {
        if ( log.isDebugEnabled() ) {
            try {
                File file = File.createTempFile( filePrefix, fileSuffix );
                writeBinaryFile( log, file, data );
            } catch ( IOException e ) {
                log.error( "Cannot create temporary file for prefix '" + filePrefix + "' and suffix '" + fileSuffix
                           + ".", e );
            }
        }
    }
}
