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
package org.deegree.framework.util;

/**
 * The BootLogger is designed to be used internally by the framework manager and components at
 * start-up time, i.e. when the main logging service has not yet been initialized.
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe</A>
 *
 * @author last edited by $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class BootLogger {
    /**
     * if the System property <code>framework.boot.debug</code> is defined and set to true then
     * more verbose messages will be printed to stdout.
     */
    private static boolean debug = false;

    static {
        try {
            debug = Boolean.getBoolean( "framework.boot.debug" );
        } catch ( Exception exc ) {
            System.out.println( "Error retrieving system property framework.boot.debug" );
            exc.printStackTrace();
        }
    }

    /**
     * @param debug true if debugging.
     */
    public static void setDebug( boolean debug ) {
        BootLogger.debug = debug;
    }

    /**
     * @return true if the level is debugging
     */
    public static boolean getDebug() {
        return debug;
    }

    /**
     * currently wraps around <Code>System.out.println</code> to print out the passed in message
     * if the debug flag is set to true.
     * @param inMessage the message to the standard out
     */
    public static void logDebug( String inMessage ) {
        if ( debug ) {
            System.out.println( inMessage );
        }
    }

    /**
     * currently wraps around <Code>System.err.println</code> to print out the passed in error
     * message.
     * @param inMessage to output to error out
     * @param ex cause
     */
    public static void logError( String inMessage, Throwable ex ) {
        System.err.println( inMessage );
        if ( ex != null )
            ex.printStackTrace( System.err );
    }

    /**
     * currently wraps around <Code>System.out.println</code> to print out the passed in error
     * message.
     * @param inMessage to log to standard out
     */
    public static void log( String inMessage ) {
        System.out.println( inMessage );
    }
}
