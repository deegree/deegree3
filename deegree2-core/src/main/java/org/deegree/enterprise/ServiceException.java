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
package org.deegree.enterprise;

// JDK 1.3
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * The <code>ServiceException</code> class is used across all core framework services and is also
 * suitable for use by developers extending the framework using the framework SPI.
 *
 * Based on code published by Terren Suydam in JavaWorld
 *
 * @see <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip91.html">JavaWorld tip 91</a>
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </A>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ServiceException extends Exception implements java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    // the nested exception
    private Throwable nestedException;

    // String representation of stack trace - not transient!
    private String stackTraceString;

    /**
     * Convert a stack trace to a String so it can be serialized
     *
     * @param t
     *
     * @return  stack trace as String
     */
    public static String generateStackTraceString( Throwable t ) {
        StringWriter s = new StringWriter();

        t.printStackTrace( new PrintWriter( s ) );

        return s.toString();
    }

    /**
     * java.lang.Exception constructors
     */
    public ServiceException() {
    }

    /**
     * Constructor declaration
     *
     * @param msg
     *
     */
    public ServiceException( String msg ) {
        super( msg );
    }

    /**
     * additional c'tors - nest the exceptions, storing the stack trace
     *
     * @param nestedException
     */
    public ServiceException( Throwable nestedException ) {
        this.nestedException = nestedException;
        stackTraceString = generateStackTraceString( nestedException );
    }

    /**
     * Constructor declaration
     *
     *
     * @param msg
     * @param nestedException
     *
     *
     */
    public ServiceException( String msg, Throwable nestedException ) {
        this( msg );

        this.nestedException = nestedException;
        stackTraceString = generateStackTraceString( nestedException );
    }

    // methods

    /**
     * Method declaration
     *
     *
     * @return nestedException
     *
     */
    public Throwable getNestedException() {
        return nestedException;
    }

    /**
     * descend through linked-list of nesting exceptions, & output trace note that this displays the
     * 'deepest' trace first
     *
     * @return stack trace as String
     *
     */
    public String getStackTraceString() {

        // if there's no nested exception, there's no stackTrace
        if ( nestedException == null ) {
            return null;
        }

        StringBuffer traceBuffer = new StringBuffer();

        if ( nestedException instanceof ServiceException ) {
            traceBuffer.append( ( (ServiceException) nestedException ).getStackTraceString() );
            traceBuffer.append( " nested by:\n" );
        }

        traceBuffer.append( stackTraceString );

        return traceBuffer.toString();
    }

    // overrides Exception.getMessage()

    /**
     * Method declaration
     *
     *
     * @return message as String
     *
     */
    @Override
    public String getMessage() {

        // superMsg will contain whatever String was passed into the
        // constructor, and null otherwise.
        String superMsg = super.getMessage();

        // if there's no nested exception, do like we would always do
        if ( getNestedException() == null ) {
            return superMsg;
        }

        StringBuffer theMsg = new StringBuffer();

        // get the nested exception's message
        String nestedMsg = getNestedException().getMessage();

        if ( superMsg != null ) {
            theMsg.append( superMsg ).append( ": " ).append( nestedMsg );
        } else {
            theMsg.append( nestedMsg );
        }

        return theMsg.toString();
    }

    // overrides Exception.toString()

    /**
     *
     * @return String
     */
    @Override
    public String toString() {
        StringBuffer theMsg = new StringBuffer( super.toString() );

        if ( getNestedException() != null ) {
            theMsg.append( "; \n\t---> nested " ).append( getNestedException() );
        }

        return theMsg.toString();
    }

    /**
     * Method declaration
     *
     */
    @Override
    public void printStackTrace() {
        if ( this.getNestedException() != null ) {
            this.getNestedException().printStackTrace();
        } else {
            super.printStackTrace();
        }
    }

    /**
     * Method declaration
     *
     * @param inPrintStream
     */
    @Override
    public void printStackTrace( PrintStream inPrintStream ) {
        this.printStackTrace( new PrintWriter( inPrintStream ) );
    }

    /**
     * Method declaration
     *
     * @param inPrintWriter
     */
    @Override
    public void printStackTrace( PrintWriter inPrintWriter ) {
        if ( this.getNestedException() != null ) {
            this.getNestedException().printStackTrace( inPrintWriter );
        } else {
            super.printStackTrace( inPrintWriter );
        }
    }

}
