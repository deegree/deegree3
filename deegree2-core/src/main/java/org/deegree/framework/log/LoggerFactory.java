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

// JDK 1.3
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.deegree.framework.util.BootLogger;

/**
 * The LoggerFactory is used to get a concrete logging service. The logging service can be provided
 * by the application server or a 3rd party logging service such as Apache log4j. The logging
 * service is configured by a set of Properties which are provided to the class init.
 * <p>
 * There are some global properties as well: <BR>
 * <UL>
 * <LI><B>log.class </B>: the logging class.
 * </UL>
 * To use the Log4J logging framework set this property: <code>
 * log.class=org.deegree_impl.log.Log4JLogger
 * </code>
 * Other supported logging facilites are the Java logging API with
 * <code>org.deegree_impl.log.JavaLogger</code> (default), and JBoss Logserver with
 * <code>org.deegree_impl.log.JBossLogger</code>.
 * <P>
 * <B>Example Code: </B> <code>
 * public class MyClass {<BR>
 *  private static ILogger logger = LoggerFactory.getLogger(this.getClass());<BR>
 *  ...<BR>
 *  public void doSomething() {<BR>
 *   logger.logDebug("have done something");<BR>
 *  }<BR>
 * </code>
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </A>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 * @see ILogger
 *
 */
public final class LoggerFactory {

    /** logging class name */
    private static String LOG_CLASS;

    /** default log handler, if not specified in resources */
    private static final String DEFAULT_LOG_CLS = "org.deegree.framework.log.JavaLogger";

    /**
     * Stores all named loggers.
     */
    private static final Map<String, ILogger> NAMED_LOGGER = Collections.synchronizedMap( new HashMap<String, ILogger>() );

    /**
     * Initialization done at class loading time
     */
    static {
        try {
            // fetch all configuration parameters
            Properties props = new Properties();
            InputStream is = LoggerService.class.getResourceAsStream( "/LoggerService.properties" );
            if ( is == null ) {
                is = LoggerService.class.getResourceAsStream( "LoggerService.properties" );
            }
            props.load( is );
            LOG_CLASS = props.getProperty( "log.class" );
            // try to load the logger class
            ILogger log = (ILogger) Class.forName( LOG_CLASS ).newInstance();
            log.init( props );
            is.close();
        } catch ( Throwable ex ) {
            LOG_CLASS = DEFAULT_LOG_CLS;
            BootLogger.logError( "Error while initializing " + LoggerFactory.class.getName() + " : " + ex.getMessage(),
                                 ex );
        } finally {
            BootLogger.logDebug( "Using Logging Class: " + LOG_CLASS );
        }
    }

    /**
     * Nothing to do, constructor is hidden
     */
    private LoggerFactory() {
        // constructor is hidden.
    }

    /**
     * Factory method to retrieve the instance of the concrete logging class. Return the named
     * logger for the given name.
     *
     * @param name
     *            of the logger
     *
     * @return the assigned logger
     */
    public static final ILogger getLogger( String name ) {
        if ( LOG_CLASS == null ) {
            return null;
        }

        ILogger logger = NAMED_LOGGER.get( name );

        if ( logger == null ) {
            try {
                logger = (ILogger) Class.forName( LOG_CLASS ).newInstance();
                logger.bindClass( name );
                NAMED_LOGGER.put( name, logger );
            } catch ( Throwable ex ) {
                BootLogger.logError( "Logging system is failing, shutting down?", null );
            }
        }

        return logger;
    }

    /**
     * Return the named logger for the given class
     *
     * @param name
     *            the class to be logged
     *
     * @return the assigned logger
     */
    public static final ILogger getLogger( Class<?> name ) {
        return LoggerFactory.getLogger( name.getName() );
    }
}
