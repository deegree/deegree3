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

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

/**
 * Log service provided for Apache Commons Logging (JCL) log service.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 *
 * @author last edited by: UID=$Author$
 *
 * @version $Revision$, $Date$
 */
public class JCLLogger extends LoggerService {

    private Log log;

    private static String LOG4J_PROP_FILE = "log4j.properties";

    private static String LOG4JLOGGER_CLASS = "org.apache.commons.logging.impl.Log4JLogger";

    static {
        Log log = null;
        try {
            log = LogFactory.getLog( JCLLogger.class );
        } catch ( NoClassDefFoundError ncdfe ) {
            StringBuilder sb = new StringBuilder( 1024 );
            sb.append( " Could not instantiate the logger because(" );
            sb.append( ncdfe.getMessage() );
            sb.append( " to use the deegree logging framewormk, please put following libraries in your classpath:" );
            sb.append( "\n - $deegree-base$/lib/commons/commons-logging.jar" );
            sb.append( "\n - $deegree-base$/lib/log4j/log4j-1.2.9.jar" );
            System.out.println( sb.toString() );
            throw ncdfe;
        }

        // when log4j is used load log4j.properties from class path or package resource
        // for backwards compatiblity with old Log4JLogger
        if ( log.getClass().getName().equals( LOG4JLOGGER_CLASS ) ) {
            URL urlToLog4jProps = JCLLogger.class.getResource( "/" + LOG4J_PROP_FILE );
            if ( urlToLog4jProps == null ) {
                urlToLog4jProps = JCLLogger.class.getResource( LOG4J_PROP_FILE );
            }
            if ( urlToLog4jProps != null ) {
                PropertyConfigurator.configure( urlToLog4jProps );
                log.debug( "Log4J: found log4j.properties, initialized the Logger with configuration found in file "
                           + urlToLog4jProps );
            } else {
                // Set up a simple configuration that logs on the console.
                BasicConfigurator.configure();
                log.warn( "Log4J: No log4j.properties found, initialized Log4J with a BasicConfiguration." );
            }
        } else {
            try {
                @SuppressWarnings("unused")
                Class<?> log4jclass = Class.forName( LOG4JLOGGER_CLASS );
            } catch ( ClassNotFoundException e ) {
                log.warn( "Logging: Did not find complete version of Apache Commons Logging. Provide a propper commons-logging.jar in your lib/ directory for full logging functionality." );
            }
        }
    }

    public void bindClass( String name ) {
        this.log = LogFactory.getLog( name );
    }

    public void bindClass( Class<?> name ) {
        this.log = LogFactory.getLog( name );
    }

    public int getLevel() {
        if ( log.isDebugEnabled() || log.isTraceEnabled() ) {
            return ILogger.LOG_DEBUG;
        } else if ( log.isInfoEnabled() ) {
            return ILogger.LOG_INFO;
        } else if ( log.isWarnEnabled() ) {
            return ILogger.LOG_WARNING;
        } else { // log.isErrorEnabled() || log.isFatalEnabled()
            return ILogger.LOG_ERROR;
        }
    }

    public void setLevel( int level ) {
        log.error( "Can't change log level at runtime. " + "Use the appropriate properties file for configuration." );
    }

    public boolean isDebug() {
        return log.isDebugEnabled();
    }

    @Override
    public void logDebug( String message ) {
        log.debug( message );
    }

    @Override
    public void logDebug( String message, Throwable e ) {
        log.debug( message, e );
    }

    public void logDebug( String message, Object tracableObject ) {
        if ( log.isDebugEnabled() ) {
            log.debug( message + tracableObject );
        }
    }

    public void logDebug( String message, Object... tracableObjects ) {
        if ( log.isDebugEnabled() ) {
            log.debug( stringFromObjects( message, tracableObjects ) );
        }
    }

    public void logError( String message ) {
        log.error( message );
    }

    public void logError( String message, Throwable e ) {
        log.error( message, e );
    }

    @Override
    public void logInfo( String message ) {
        log.info( message );
    }

    @Override
    public void logInfo( String message, Throwable e ) {
        log.info( message, e );
    }

    public void logInfo( String message, Object tracableObject ) {
        log.info( message + ": " + tracableObject );
    }

    public void logInfo( String message, Object... tracableObject ) {
        if ( log.isInfoEnabled() ) {
            log.info( stringFromObjects( message, tracableObject ) );
        }
    }

    @Override
    public void logWarning( String message ) {
        log.warn( message );
    }

    @Override
    public void logWarning( String message, Throwable e ) {
        log.warn( message, e );
    }

    // not used?
    public void log( int priority, String message, Throwable ex ) {
        logDebug( message, ex );
    }

    // not used?
    public void log( int priority, String message, Object source, Throwable ex ) {
        logDebug( message, ex );
    }

    private String stringFromObjects( String message, Object... objects ) {
        StringBuilder sb = new StringBuilder( message );
        for ( Object part : objects ) {
            sb.append( ' ' ).append( part.toString() );
        }
        return sb.toString();
    }

    public void logError( Throwable e ) {
        if ( e != null ) {
            logError( e.getLocalizedMessage(), e );
        } else {
            logError( "No exception to log.", new NullPointerException( "No exception to log." ) );
        }
    }
}
