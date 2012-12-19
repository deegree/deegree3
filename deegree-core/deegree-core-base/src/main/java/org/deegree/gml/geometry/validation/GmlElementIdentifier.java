//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.gml.geometry.validation;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

/**
 * Identifies a GML element and it's position in a GML stream for providing location information.
 * 
 * @see GmlStreamGeometryValidator
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class GmlElementIdentifier {

    private QName elementName;

    private int lineNumber;

    private int columnNumber;

    /**
     * Creates a new {@link GmlElementIdentifier} for identifying the opening element that the given xml stream
     * currently points at.
     *
     * @param xmlStream
     *            must point at an open element event
     */
    public GmlElementIdentifier( XMLStreamReader xmlStream ) {
        if ( xmlStream.getEventType() != XMLStreamConstants.START_ELEMENT ) {
            String msg = "Cannot create GMLElementIdentifier. XMLStreamReader does not point at a START_ELEMENT event.";
            throw new IllegalArgumentException( msg );
        }
        elementName = xmlStream.getName();
        lineNumber  = xmlStream.getLocation().getLineNumber();
        columnNumber  = xmlStream.getLocation().getColumnNumber();
    }

    @Override
    public String toString () {
        String s = elementName + ", line: " + lineNumber + ", column: " + columnNumber;
        return s;
    }
}
