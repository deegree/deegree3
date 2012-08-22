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
package org.deegree.framework.xml;

import org.deegree.ogcwebservices.OGCWebServiceException;

/**
 * This exception is thrown when a syntactic or semantic error has been encountered during the parsing process of an XML
 * document.
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @version $Revision$
 */
public class XMLParsingException extends Exception {

    private static final long serialVersionUID = -375766555263169888L;

    private String stackTrace = "<< is empty >>";

    private OGCWebServiceException wrapped;

    /**
     * Creates a new instance of <code>XMLParsingException</code> without detail message.
     */
    XMLParsingException() {
        super( "org.deegree.xml.XMLParsingException" );
        // package protected constructor
    }

    /**
     * Workaround to beat the "no API change" policy.
     *
     * @param wrapException
     *            ignored parameter, to avoid clashing with the other constructor taking a Throwable.
     * @param wrapped
     */
    public XMLParsingException( boolean wrapException, OGCWebServiceException wrapped ) {
        this.wrapped = wrapped;
    }

    /**
     * Constructs an instance of <code>XMLParsingException</code> with the specified detail message.
     *
     * @param msg
     *            the detail message
     */
    public XMLParsingException( String msg ) {
        super( msg );
    }

    /**
     * Constructs an instance of <code>XMLParsingException</code> with the specified cause.
     *
     * @param cause
     *            the Throwable that caused this XMLParsingException
     *
     */
    public XMLParsingException( Throwable cause ) {
        super( "org.deegree.xml.XMLParsingException", cause );
    }

    /**
     * Constructs an instance of <code>XMLParsingException</code> with the specified detail message.
     *
     * @param msg
     *            the detail message.
     * @param e
     *            the cause of the exception to be thrown.
     */
    public XMLParsingException( String msg, Throwable e ) {
        this( msg );
        if ( e != null ) {
            StackTraceElement[] se = e.getStackTrace();
            StringBuffer sb = new StringBuffer();
            for ( int i = 0; i < se.length; i++ ) {
                sb.append( se[i].getClassName() + " " );
                sb.append( se[i].getFileName() + " " );
                sb.append( se[i].getMethodName() + "(" );
                sb.append( se[i].getLineNumber() + ")\n" );
            }
            stackTrace = e.getMessage() + sb.toString();
        }
    }

    /**
     * @return the wrapped Throwable
     */
    public OGCWebServiceException getWrapped() {
        return wrapped;
    }

    @Override
    public String toString() {
        return getMessage() + "\n" + stackTrace;
    }

}
