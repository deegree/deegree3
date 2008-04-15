//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/src/org/deegree/framework/util/BootLogger.java $
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
package org.deegree.commons.logging;

/**
 * The BootLogger is designed to be used internally by the framework manager and components at
 * start-up time, i.e. when the main logging service has not yet been initialized.
 * 
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe</A>
 * 
 * @author last edited by $Author: apoth $
 * 
 * @version $Revision: 9339 $, $Date: 2007-12-27 13:31:52 +0100 (Do, 27 Dez 2007) $
 * @deprecated
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

    public static void setDebug( boolean debug ) {
        BootLogger.debug = debug;
    }

    public static boolean getDebug() {
        return debug;
    }

    /**
     * currently wraps around <Code>System.out.println</code> to print out the passed in message
     * if the debug flag is set to true.
     */
    public static void logDebug( String inMessage ) {
        if ( debug ) {
            System.out.println( inMessage );
        }
    }

    /**
     * currently wraps around <Code>System.err.println</code> to print out the passed in error
     * message.
     */
    public static void logError( String inMessage, Throwable ex ) {
        System.err.println( inMessage );
        if ( ex != null )
            ex.printStackTrace( System.err );
    }

    /**
     * currently wraps around <Code>System.out.println</code> to print out the passed in error
     * message.
     */
    public static void log( String inMessage ) {
        System.out.println( inMessage );
    }
}