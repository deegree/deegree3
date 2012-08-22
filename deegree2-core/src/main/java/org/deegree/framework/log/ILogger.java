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
package org.deegree.framework.log;

import java.io.File;
import java.util.Properties;

import org.deegree.framework.xml.XMLFragment;

/**
 * This interface specifies the log worker services.
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe</a>
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
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
    void bindClass( Class<?> name );

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
     * Logs the given text to a temporary file (created from specified prefix and suffix) if log level is set to
     * <code>LOG_DEBUG</code>.
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
     * Logs the given {@link XMLFragment} to a temporary file (created from specified prefix and suffix ".xml") if log
     * level is set to <code>LOG_DEBUG</code>.
     *
     * @param filePrefix
     *            prefix for the temp file name
     * @param fragment
     *            XMLFragment to be logged (will be pretty-printed)
     */
    void logDebugXMLFile( String filePrefix, XMLFragment fragment );

    /**
     * Logs the given binary data to the specified file if log level is set to <code>LOG_DEBUG</code>.
     *
     * @param file
     *            file to log to
     * @param data
     *            binary data to be logged
     */
    void logDebugBinaryFile( File file, byte[] data );

    /**
     * Logs the given binary data to a temporary file (created from specified prefix and suffix) if log level is set to
     * <code>LOG_DEBUG</code>.
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
     * Log error with exception, use the exceptions.getLocatizedMethod for the message
     *
     * @param e
     *            the exception to be logged
     */
    void logError( Throwable e );

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
    void logDebug( String message, Object... tracableObjects );

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
    void logInfo( String message, Object... tracableObjects );

    /**
     *
     * @param priority
     * @param message
     * @param ex
     */
    void log( int priority, String message, Throwable ex );

    /**
     *
     * @param priority
     * @param message
     * @param source
     * @param ex
     */
    void log( int priority, String message, Object source, Throwable ex );

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
