// $HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/commons/xml/XMLAdapter.java $
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

    /**
     * Creates a new exception for a parsing error that occured in a StAX-based parsing method.
     * 
     * @param origin
     *            {@link XMLAdapter} that determined the error (usually use <code>this</code>)
     * @param xmlStream
     *            {@link XMLStreamReader} that points at the erroneous event
     * @param msg
     *            error information that explains the problem
     */
    public XMLParsingException( XMLAdapter origin, XMLStreamReader xmlStream, String msg ) {
        this.msg = msg;
        this.errorPosition = new XMLErrorPosition( origin, xmlStream );
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
        return errorPosition.getAsMessage() + ": " + msg;
    }
}
