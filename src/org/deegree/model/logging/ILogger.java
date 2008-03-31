//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/src/org/deegree/framework/log/ILogger.java $
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
 53115 Bonn
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
package org.deegree.model.logging;

import java.io.File;
import java.util.Properties;

/**
 * This interface specifies the log worker services.
 * 
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe</a>
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: apoth $
 * 
 * @version $Revision: 10660 $, $Date: 2008-03-24 22:39:54 +0100 (Mo, 24 Mrz 2008) $
 */
public interface ILogger {

    /** Debug log level */
    int LOG_DEBUG = 0;

    /** Info log level */
    int LOG_INFO = 1;

    /** Warning log level */
    int LOG_WARNING = 2;

    /** Fatal error log level */
    int LOG_ERROR = 3;

    /**
     * 
     * @param props
     */
    void init( Properties props );

    /**
     * 
     * @param name
     */
    void bindClass( String name );

    /**
     * 
     * @param name
     */
    void bindClass( Class name );

    /**
     * 
     * @param message
     */
    void logDebug( String message );

    /**
     * Logs the given text to the specified file if log level is set to <code>LOG_DEBUG</code>.
     * 
     * @param file
     *            file to log to
     * @param content
     *            text to be logged
     */
    void logDebugFile( File file, String content );

    /**
     * Logs the given text to a temporary file (created from specified prefix and suffix) if log
     * level is set to <code>LOG_DEBUG</code>.
     * 
     * @see File#createTempFile(String, String)
     * @param filePrefix
     *            prefix for the temp file name
     * @param fileSuffix
     *            suffix for the temp file name, can be null (then ".tmp" is used)
     * @param content
     *            text to be logged
     */
    void logDebugFile( String filePrefix, String fileSuffix, String content );
  

    /**
     * Logs the given binary data to the specified file if log level is set to
     * <code>LOG_DEBUG</code>.
     * 
     * @param file
     *            file to log to
     * @param data
     *            binary data to be logged
     */
    void logDebugBinaryFile( File file, byte[] data );

    /**
     * Logs the given binary data to a temporary file (created from specified prefix and suffix) if
     * log level is set to <code>LOG_DEBUG</code>.
     * 
     * @see File#createTempFile(String, String)
     * @param filePrefix
     *            prefix for the temp file name
     * @param fileSuffix
     *            suffix for the temp file name, can be null (then ".tmp" is used)
     * @param data
     *            binary data to be logged
     */
    void logDebugBinaryFile( String filePrefix, String fileSuffix, byte[] data );

    /**
     * 
     * @param message
     */
    void logInfo( String message );

    /**
     * 
     * @param message
     */
    void logWarning( String message );

    /**
     * Log error message.
     * 
     * @param message
     *            the log message
     */
    void logError( String message );

    /**
     * 
     * @param message
     * @param e
     */
    void logDebug( String message, Throwable e );

    /**
     * 
     * @param message
     * @param e
     */
    void logInfo( String message, Throwable e );

    /**
     * 
     * @param message
     * @param e
     */
    void logWarning( String message, Throwable e );

    /**
     * Log error with exception
     * 
     * @param message
     *            the log message
     * @param e
     *            the exception to be logged
     */
    void logError( String message, Throwable e );

    /**
     * 
     * @param message
     * @param tracableObject
     */
    void logDebug( String message, Object tracableObject );

    /**
     * 
     * @param message
     * @param tracableObjects
     */
    void logDebug( String message, Object... tracableObject );

    /**
     * 
     * @param message
     * @param tracableObject
     */
    void logInfo( String message, Object tracableObject );

    /**
     * 
     * @param message
     * @param tracableObjects
     */
    void logInfo( String message, Object... tracableObject );   

    /**
     * sets the debug level
     * 
     * @param level
     * 
     */
    void setLevel( int level );

    /**
     * @return the debug level
     * 
     */
    int getLevel();

    /**
     * Debugging log is enabled.
     * 
     * @return <code>true</code> if the log level is DEBUG, otherwise <code>false</code>
     */
    boolean isDebug();
}