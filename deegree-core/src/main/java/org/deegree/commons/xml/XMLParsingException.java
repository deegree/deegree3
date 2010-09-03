// $HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/commons/xml/XMLAdapter.java $
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
package org.deegree.commons.xml;

import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;

/**
 * Thrown when a syntactic or semantic error has been encountered during the parsing process in an {@link XMLAdapter}.
 * <p>
 * Helps to determine the error in the XML document by returning file name and position (column, line, character offset)
 * information in {@link #getMessage()} when they are available.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class XMLParsingException extends XMLProcessingException {

    private static final long serialVersionUID = 2428868104304736218L;

    private String msg;

    private XMLErrorPosition errorPosition;

    @Deprecated
    public XMLParsingException( String msg ) {
        this.msg = msg;
        errorPosition = null;
    }

    /**
     * Creates a new exception for a parsing error that occured in a StAX-based parsing method.
     * 
     * @param xmlReader
     *            {@link XMLStreamReader} that encountered the erroneous event
     * @param msg
     *            error information that explains the problem
     */
    public XMLParsingException( XMLStreamReader xmlReader, String msg ) {
        this.msg = msg;
        this.errorPosition = new XMLErrorPosition( xmlReader );
    }

    /**
     * Creates a new exception for a parsing error that occured in an AXIOM-based parsing method.
     * 
     * @param origin
     *            {@link XMLAdapter} that determined the error (usually use <code>this</code>)
     * @param erroneousElement
     *            element that contains the error
     * @param msg
     *            error information that explains the problem
     */
    public XMLParsingException( XMLAdapter origin, OMElement erroneousElement, String msg ) {
        this.msg = msg;
        this.errorPosition = new XMLErrorPosition( origin, erroneousElement );
    }

    @Override
    public String getMessage() {
        return "Error in XML document ("
               + ( ( errorPosition != null ) ? errorPosition.getAsMessage() : "Unknown error position" ) + "): " + msg;
    }
}
