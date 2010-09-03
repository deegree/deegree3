//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.protocol.ows;

import static org.deegree.commons.xml.CommonNamespaces.OWS_11_NS;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.protocol.ows.exception.OWSException;

/**
 * The <code>OWSExceptionReader</code> class parses the OWSException report.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class OWSExceptionReader {

    /**
     * @param reader
     * @return true if the current element in the reader is an <code>ows:ExceptionReport</code>, false otherwise
     */
    public static boolean isException( XMLStreamReader reader ) {
        return ( new QName( OWS_11_NS, "ExceptionReport" ).equals( reader.getName() ) );
    }

    /**
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;ows:ExceptionReport&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event
     * (&lt;/ows:ExceptionReport&gt;)</li>
     * </ul>
     * 
     * @param reader
     * 
     * @return the parsed {@link OWSException}
     */
    public static OWSException parseException( XMLStreamReader reader ) {
        String code = null;
        String locator = null;
        String message = null;
        try {
            StAXParsingHelper.nextElement( reader ); // "ExceptionReport"
            code = reader.getAttributeValue( null, "exceptionCode" );
            locator = reader.getAttributeValue( null, "locator" );
            StAXParsingHelper.nextElement( reader ); // "Exception"
            message = reader.getElementText();
        } catch ( XMLStreamException e ) {
            throw new RuntimeException( "Error parsing OWSExceptionReport: " + e.getMessage() );
        }
        return new OWSException( message, code, locator );
    }
}