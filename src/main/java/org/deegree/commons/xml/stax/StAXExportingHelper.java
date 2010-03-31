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

package org.deegree.commons.xml.stax;

import static org.deegree.commons.utils.StringUtils.isSet;
import static org.slf4j.LoggerFactory.getLogger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class StAXExportingHelper {

    private static final Logger LOG = getLogger( StAXExportingHelper.class );

    public static void writeAttribute( XMLStreamWriter xmlStream, QName name, String value )
                            throws XMLStreamException {
        if ( name.getNamespaceURI() == null ) {
            xmlStream.writeAttribute( name.getLocalPart(), value );
        } else if ( name.getPrefix() == null ) {
            xmlStream.writeAttribute( name.getNamespaceURI(), name.getLocalPart(), value );
        } else {
            xmlStream.writeAttribute( name.getPrefix(), name.getNamespaceURI(), name.getLocalPart(), value );
        }
    }

    /**
     * Writes a start element from the given QName.
     * 
     * @param xmlStream
     * @param qName
     * @throws XMLStreamException
     */
    public static void writeStartElement( XMLStreamWriter xmlStream, QName qName )
                            throws XMLStreamException {
        if ( qName == null ) {
            throw new XMLStreamException( "The given qname may not be null" );
        }
        int id = isSet( qName.getNamespaceURI() ) ? 1 : 0;
        id += isSet( qName.getLocalPart() ) ? 2 : 0;
        id += isSet( qName.getPrefix() ) ? 4 : 0;
        switch ( id ) {
        case 0:
            throw new XMLStreamException( "The given qname may not be null or empty" );
        case 1:
        case 4:
        case 5:
            throw new XMLStreamException( "The given qname must have a local part." );
        case 2:
        case 6:
            xmlStream.writeStartElement( qName.getLocalPart() );
            break;
        case 3:
            xmlStream.writeStartElement( qName.getNamespaceURI(), qName.getLocalPart() );
            break;
        case 7:
            xmlStream.writeStartElement( qName.getPrefix(), qName.getLocalPart(), qName.getNamespaceURI() );
            break;
        }

    }
}
