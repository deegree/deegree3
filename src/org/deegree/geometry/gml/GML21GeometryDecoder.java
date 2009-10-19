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
package org.deegree.geometry.gml;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;

import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.primitive.Point;

/**
 * The GML 2.1 parser (name like that since it tries to comply to the latest changes in the newest subversions e.g.
 * 2.1.2) contains for now just the parsing of the "Box" (in GML 3.1 this is Envelope). Probably more differences to GML
 * 3.1 will be found which will result in more overridden methods.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class GML21GeometryDecoder extends GML311GeometryDecoder {

    @Override
    public Envelope parseEnvelope( XMLStreamReaderWrapper xmlStream )
                            throws XMLParsingException, XMLStreamException {
        return parseEnvelope( xmlStream, null );
    }

    @Override
    public Envelope parseEnvelope( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLParsingException, XMLStreamException {

        CRS crs = determineActiveCRS( xmlStream, defaultCRS );

        double[] lowerCorner = null;
        double[] upperCorner = null;

        // must contain exactly one of the following child elements: "gml:lowerCorner", "gml:coord", "gml:pos" or
        // "gml:coordinates"
        if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
            String name = xmlStream.getLocalName();
            if ( "coord".equals( name ) ) {
                lowerCorner = parseCoordType( xmlStream );
                xmlStream.require( END_ELEMENT, GMLNS, "coord" );
                xmlStream.nextTag();
                xmlStream.require( START_ELEMENT, GMLNS, "coord" );
                upperCorner = parseCoordType( xmlStream );
                xmlStream.require( END_ELEMENT, GMLNS, "coord" );
            } else if ( "coordinates".equals( name ) ) {
                List<Point> coords = parseCoordinates( xmlStream, crs );
                if ( coords.size() != 2 ) {
                    String msg = "Error in 'gml:Envelope' element, if 'gml:coordinates' is used, it must specify the coordinates of two points.";
                    throw new XMLParsingException( xmlStream, msg );
                }
                lowerCorner = coords.get( 0 ).getAsArray();
                upperCorner = coords.get( 1 ).getAsArray();
            } else {
                String msg = "Error in 'gml:Envelope' element. Expected either a 'gml:lowerCorner', 'gml:coord'"
                             + " 'gml:pos' or 'gml:coordinates' element, but found '" + name + "'.";
                throw new XMLParsingException( xmlStream, msg );
            }
        } else {
            String msg = "Error in 'gml:Envelope' element. Must contain one of the following child elements: 'gml:lowerCorner', 'gml:coord'"
                         + " 'gml:pos' or 'gml:coordinates'.";
            throw new XMLParsingException( xmlStream, msg );
        }
        xmlStream.nextTag();
        xmlStream.require( END_ELEMENT, GMLNS, "Envelope" );
        return geomFac.createEnvelope( lowerCorner, upperCorner, crs );
    }

}
