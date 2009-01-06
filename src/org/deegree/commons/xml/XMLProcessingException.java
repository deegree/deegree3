//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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
package org.deegree.commons.xml;

/**
 * Base class for XML-related exceptions.
 * 
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class XMLProcessingException extends RuntimeException {

    private static final long serialVersionUID = -375766555263169888L;

    private String message = "org.deegree.xml.XMLParsingException";

    private String stackTrace = "<< is empty >>";

    /**
     * Creates a new instance of <code>XMLProcessingException</code> without detail message.
     */
    protected XMLProcessingException() {
        // nothing to do
    }

    /**
     * Constructs an instance of <code>XMLProcessingException</code> with the specified detail
     * message.
     * 
     * @param msg
     *            the detail message.
     */
    public XMLProcessingException( String msg ) {
        super();
        message = msg;

    }

    /**
     * Constructs an instance of <code>XMLProcessingException</code> with the specified cause.
     * 
     * @param cause
     *            the Throwable that caused this XMLParsingException
     * 
     */
    public XMLProcessingException( Throwable cause ) {
        super( cause );
    }

    /**
     * Constructs an instance of <code>XMLProcessingException</code> with the specified detail
     * message.
     * 
     * @param msg
     *            the detail message.
     * @param e 
     */
    public XMLProcessingException( String msg, Throwable e ) {
        this( msg );
        if ( e != null ) {
            StackTraceElement[] se = e.getStackTrace();
            StringBuffer sb = new StringBuffer( 1000 );
            for ( int i = 0; i < se.length; i++ ) {
                sb.append( se[i].getClassName() + " " );
                sb.append( se[i].getFileName() + " " );
                sb.append( se[i].getMethodName() + "(" );
                sb.append( se[i].getLineNumber() + ")\n" );
            }
            stackTrace = e.getMessage() + sb.toString();
        }
    }

    public String toString() {
        return this.getClass() + ": " + getMessage() + "\n" + stackTrace;
    }

    /**
     * 
     */
    public String getMessage() {
        return message;
    }

}