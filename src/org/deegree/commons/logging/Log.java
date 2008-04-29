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
package org.deegree.commons.logging;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This is a thin wrapper for {@link org.apache.commons.logging.Log}. It adds some methods to log direct into files.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author: $
 * 
 * @version $Revision: $, $Date: $
 * 
 */
public class Log implements org.apache.commons.logging.Log {

    org.apache.commons.logging.Log log;

    // package scoped, instantiated by LogFactory
    Log( org.apache.commons.logging.Log commonsLog ) {
        this.log = commonsLog;
    }

    /**
     * Logs the given {@link String} to the specified file if log level is set to <code>DEBUG</code>.
     * 
     * @param file
     *            file to log to
     * @param content
     *            text to be logged
     */
    void debugFile( File file, String content ) {
        if ( log.isDebugEnabled() ) {
            debug( "Writing debug file '" + file.getAbsolutePath() + "'." );
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter( new FileWriter( file ) );
                writer.write( content );
            } catch ( IOException e ) {
                error( "Could not write to debug file '" + file.getAbsolutePath() + "'.", e );
            } finally {
                if ( writer != null ) {
                    try {
                        writer.close();
                    } catch ( IOException e ) {
                        error( "Error closing debug file '" + file.getAbsolutePath() + "'.", e );
                    }
                }
            }
        }
    }

    /**
     * Logs the given {@link String} to a temporary file (created from specified prefix and suffix) if log level is set
     * to <code>DEBUG</code>.
     * 
     * @see File#createTempFile(String, String)
     * @param filePrefix
     *            prefix for the temp file name, must be at least three characters long
     * @param fileSuffix
     *            suffix for the temp file name, can be null (then ".tmp" is used)
     * @param content
     *            text to be logged
     */
    void debugTempFile( String filePrefix, String fileSuffix, String content ) {
        if ( log.isDebugEnabled() ) {
            try {
                File tmpFile = File.createTempFile( filePrefix, fileSuffix );
                debugFile( tmpFile, content );
            } catch ( IOException e ) {
                error( "Cannot create debug file for prefix '" + filePrefix + "' and suffix '" + fileSuffix + ".", e );
            }
        }
    }

    /**
     * Logs the given binary data to the specified file if log level is set to <code>DEBUG</code>.
     * 
     * @param file
     *            file to log to
     * @param data
     *            binary data to be logged
     */
    void debugBinaryFile( File file, byte[] data ) {
        if ( log.isDebugEnabled() ) {
            debug( "Writing binary debug file '" + file.getAbsolutePath() + "'." );
            BufferedOutputStream out = null;
            try {
                out = new BufferedOutputStream( new FileOutputStream( file ) );
                out.write( data );
            } catch ( IOException e ) {
                error( "Could not write to debug file '" + file.getAbsolutePath() + "'.", e );
            } finally {
                if ( out != null ) {
                    try {
                        out.close();
                    } catch ( IOException e ) {
                        error( "Error closing debug file '" + file.getAbsolutePath() + "'.", e );
                    }
                }
            }
        }
    }

    /**
     * Logs the given binary data to a temporary file (created from specified prefix and suffix) if log level is set to
     * <code>DEBUG</code>.
     * 
     * @see File#createTempFile(String, String)
     * @param filePrefix
     *            prefix for the temp file name, must be at least three characters long
     * @param fileSuffix
     *            suffix for the temp file name, can be null (then ".tmp" is used)
     * @param data
     *            binary data to be logged
     */
    void debugBinaryTempFile( String filePrefix, String fileSuffix, byte[] data ) {
        if ( log.isDebugEnabled() ) {
            try {
                File tmpFile = File.createTempFile( filePrefix, fileSuffix );
                debugBinaryFile( tmpFile, data );
            } catch ( IOException e ) {
                error( "Cannot create debug file for prefix '" + filePrefix + "' and suffix '" + fileSuffix + ".", e );
            }
        }
    }

    // ***********************************************************
    // --------------- wrapped logging methods -------------------
    // ***********************************************************

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.logging.Log#debug(java.lang.Object)
     */
    public void debug( Object message ) {
        log.debug( message );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.logging.Log#debug(java.lang.Object, java.lang.Throwable)
     */
    public void debug( Object message, Throwable t ) {
        log.debug( message, t );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.logging.Log#error(java.lang.Object)
     */
    public void error( Object message ) {
        log.error( message );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.logging.Log#error(java.lang.Object, java.lang.Throwable)
     */
    public void error( Object message, Throwable t ) {
        log.error( message, t );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.logging.Log#fatal(java.lang.Object)
     */
    public void fatal( Object message ) {
        log.fatal( message );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.logging.Log#fatal(java.lang.Object, java.lang.Throwable)
     */
    public void fatal( Object message, Throwable t ) {
        log.fatal( message, t );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.logging.Log#info(java.lang.Object)
     */
    public void info( Object message ) {
        log.info( message );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.logging.Log#info(java.lang.Object, java.lang.Throwable)
     */
    public void info( Object message, Throwable t ) {
        log.info( message, t );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.logging.Log#isDebugEnabled()
     */
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.logging.Log#isErrorEnabled()
     */
    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.logging.Log#isFatalEnabled()
     */
    public boolean isFatalEnabled() {
        return log.isFatalEnabled();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.logging.Log#isInfoEnabled()
     */
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.logging.Log#isTraceEnabled()
     */
    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.logging.Log#isWarnEnabled()
     */
    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.logging.Log#trace(java.lang.Object)
     */
    public void trace( Object message ) {
        log.trace( message );

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.logging.Log#trace(java.lang.Object, java.lang.Throwable)
     */
    public void trace( Object message, Throwable t ) {
        log.trace( message, t );

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.logging.Log#warn(java.lang.Object)
     */
    public void warn( Object message ) {
        log.warn( message );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.logging.Log#warn(java.lang.Object, java.lang.Throwable)
     */
    public void warn( Object message, Throwable t ) {
        log.warn( message, t );
    }

}
