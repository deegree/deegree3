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
package org.deegree.commons.xml;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;

/**
 * Encapsulates information to locate the cause {@link XMLParsingException}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
class XMLErrorPosition {

    private String systemId;

    private int lineNumber;

    private int columnNumber;

    private int characterOffset;

    /**
     * @param xmlReader
     */
    XMLErrorPosition( XMLStreamReader xmlReader ) {

        if ( xmlReader instanceof XMLStreamReaderWrapper ) {
            systemId = ( (XMLStreamReaderWrapper) xmlReader ).getSystemId();
        } else {
            systemId = "unknown source";
        }

        Location location = xmlReader.getLocation();
        lineNumber = location.getLineNumber();
        columnNumber = location.getColumnNumber();
        characterOffset = location.getCharacterOffset();
    }

    /**
     * @param origin
     * @param erroneousElement
     */
    XMLErrorPosition( XMLAdapter origin, OMElement erroneousElement ) {
        systemId = origin.getSystemId() == null ? "" : origin.getSystemId().toString();
        lineNumber = erroneousElement == null ? -1 : erroneousElement.getLineNumber();
        // no column number or character offset information available
        columnNumber = -1;
        characterOffset = -1;
    }

    /**
     * Returns the location information in a human readable form.
     * 
     * @return
     */
    String getAsMessage() {
        String s = systemId != null && !"".equals( systemId ) ? ( "file '" + systemId + "', " ) : "";
        s += "line: " + lineNumber;
        if ( columnNumber != -1 ) {
            s += ", column: " + columnNumber;
        }
        if ( characterOffset != -1 ) {
            s += ", character offset: " + characterOffset;
        }
        return s;
    }

    @Override
    public String toString() {
        return getAsMessage();
    }
}
