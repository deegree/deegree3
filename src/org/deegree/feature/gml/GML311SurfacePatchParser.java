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
package org.deegree.feature.gml;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;

import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.surfacepatches.PolygonPatch;
import org.deegree.geometry.primitive.surfacepatches.Rectangle;
import org.deegree.geometry.primitive.surfacepatches.SurfacePatch;
import org.deegree.geometry.primitive.surfacepatches.Triangle;

/**
 * Handles the parsing of <code>gml:_SurfacePatch</code> elements, i.e concrete element declarations that are in the
 * substitution group of <code>gml:_SurfacePatch</code>.
 * <p>
 * This class handles all 6 concrete substitutions for <code>gml:_SurfacePatch</code> that are defined in GML 3.1.1:
 * <ul>
 * <li><code>Cone</code></li>
 * <li><code>Cylinder</code></li>
 * <li><code>PolygonPatch</code></li>
 * <li><code>Rectangle</code></li>
 * <li><code>Sphere</code></li>
 * <li><code>Triangle</code></li>
 * </ul>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
class GML311SurfacePatchParser extends GML311BaseParser {

    private GML311GeometryParser geometryParser;

    /**
     * @param geometryParser
     * @param geomFac
     */
    GML311SurfacePatchParser( GML311GeometryParser geometryParser, GeometryFactory geomFac ) {
        super( geomFac );
        this.geometryParser = geometryParser;
    }

    /**
     * Returns the object representation for a <code>gml:_SurfacePatch</code> element. Consumes all corresponding events
     * from the associated <code>XMLStream</code>.
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_SurfacePatch&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/gml:_SurfacePatch&gt;)
     * </li>
     * </ul>
     * <p>
     * This method handles all 6 concrete substitutions for <code>gml:_SurfacePatch</code> that are defined in GML
     * 3.1.1:
     * <ul>
     * <li><code>Cone</code></li>
     * <li><code>Cylinder</code></li>
     * <li><code>PolygonPatch</code></li>
     * <li><code>Rectangle</code></li>
     * <li><code>Sphere</code></li>
     * <li><code>Triangle</code></li>
     * </ul>
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the "gml:_SurfacePatch" has no <code>srsName</code>
     *            attribute
     * @return corresponding {@link SurfacePatch} object
     * @throws XMLParsingException
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    SurfacePatch parseSurfacePatch( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        SurfacePatch patch = null;

        if ( !GMLNS.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Invalid gml:_SurfacePatch element: " + xmlStream.getName()
                         + "' is not a GML geometry element. Not in the gml namespace.";
            throw new XMLParsingException( xmlStream, msg );
        }

        String name = xmlStream.getLocalName();
        if ( name.equals( "Cone" ) ) {
            patch = parseCone( xmlStream, defaultCRS );
        } else if ( name.equals( "Cylinder" ) ) {
            patch = parseCylinder( xmlStream, defaultCRS );
        } else if ( name.equals( "PolygonPatch" ) ) {
            patch = parsePolygonPatch( xmlStream, defaultCRS );
        } else if ( name.equals( "Rectangle" ) ) {
            patch = parseRectangle( xmlStream, defaultCRS );
        } else if ( name.equals( "Sphere" ) ) {
            patch = parseCone( xmlStream, defaultCRS );
        } else if ( name.equals( "Triangle" ) ) {
            patch = parseTriangle( xmlStream, defaultCRS );
        } else {
            String msg = "Invalid GML geometry: '" + xmlStream.getName()
                         + "' is not a valid substitution for '_SurfacePatch'.";
            throw new XMLParsingException( xmlStream, msg );
        }
        return patch;
    }

    private SurfacePatch parseCone( XMLStreamReaderWrapper xmlStream, CRS defaultCRS ) {
        // TODO Auto-generated method stub
        return null;
    }

    private SurfacePatch parseCylinder( XMLStreamReaderWrapper xmlStream, CRS defaultCRS ) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Returns the object representation of a <code>&lt;gml:PolygonPatch&gt;</code> element. Consumes all corresponding
     * events from the associated <code>XMLStream</code>.
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:PolygonPatch&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/gml:PolygonPatch&gt;)</li>
     * </ul>
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is propagated if no deeper <code>srsName</code> attribute is
     *            specified
     * @return corresponding {@link PolygonPatch} object
     * @throws XMLParsingException
     *             if a syntactical (or semantic) error is detected in the element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    PolygonPatch parsePolygonPatch( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        validateInterpolationAttribute( xmlStream, "planar" );

        Ring exteriorRing = null;
        List<Ring> interiorRings = new LinkedList<Ring>();

        // 0 or 1 exterior element (yes, 0 is possible -- see section 9.2.2.5 of GML spec)
        if ( xmlStream.nextTag() == START_ELEMENT ) {
            if ( xmlStream.getLocalName().equals( "exterior" ) ) {
                if ( xmlStream.nextTag() != START_ELEMENT ) {
                    String msg = "Error in 'gml:PolygonPatch' element. Expected a 'gml:_Ring' element.";
                    throw new XMLParsingException( xmlStream, msg );
                }
                exteriorRing = geometryParser.parseAbstractRing( xmlStream, defaultCRS );
                xmlStream.nextTag();
                xmlStream.require( END_ELEMENT, GMLNS, "exterior" );
                xmlStream.nextTag();
            }
        }

        // arbitrary number of interior elements
        while ( xmlStream.getEventType() == START_ELEMENT ) {
            if ( xmlStream.getLocalName().equals( "interior" ) ) {
                if ( xmlStream.nextTag() != START_ELEMENT ) {
                    String msg = "Error in 'gml:PolygonPatch' element. Expected a 'gml:_Ring' element.";
                    throw new XMLParsingException( xmlStream, msg );
                }
                interiorRings.add( geometryParser.parseAbstractRing( xmlStream, defaultCRS ) );
                xmlStream.nextTag();
                xmlStream.require( END_ELEMENT, GMLNS, "interior" );
            } else {
                String msg = "Error in 'gml:Polygon' element. Expected a 'gml:interior' element.";
                throw new XMLParsingException( xmlStream, msg );
            }
            xmlStream.nextTag();
        }
        xmlStream.require( END_ELEMENT, GMLNS, "PolygonPatch" );
        return geomFac.createPolygonPatch( exteriorRing, interiorRings );
    }

    /**
     * Returns the object representation of a <code>&lt;gml:Rectangle&gt;</code> element. Consumes all corresponding
     * events from the associated <code>XMLStream</code>.
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:Rectangle&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/gml:Rectangle&gt;)</li>
     * </ul>
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is propagated if no deeper <code>srsName</code> attribute is
     *            specified
     * @return corresponding {@link Rectangle} object
     * @throws XMLParsingException
     *             if a syntactical (or semantic) error is detected in the element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    private Rectangle parseRectangle( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {
        validateInterpolationAttribute( xmlStream, "planar" );

        xmlStream.nextTag();
        xmlStream.require( START_ELEMENT, GMLNS, "exterior" );
        if ( xmlStream.nextTag() != START_ELEMENT ) {
            String msg = "Error in 'gml:Rectangle' element. Expected a 'gml:LinearRing' element.";
            throw new XMLParsingException( xmlStream, msg );
        }

        LinearRing exteriorRing = geometryParser.parseLinearRing( xmlStream, defaultCRS );
        if ( exteriorRing.getControlPoints().size() != 5 ) {
            String msg = "Error in 'gml:Rectangle' element. Exterior ring must contain exactly five points, but contains "
                         + exteriorRing.getControlPoints().size();
            throw new XMLParsingException( xmlStream, msg );
        }

        xmlStream.nextTag();
        xmlStream.require( END_ELEMENT, GMLNS, "exterior" );
        xmlStream.nextTag();
        xmlStream.require( END_ELEMENT, GMLNS, "Rectangle" );

        return geomFac.createRectangle( exteriorRing );
    }

    /**
     * Returns the object representation of a <code>&lt;gml:Triangle&gt;</code> element. Consumes all corresponding
     * events from the associated <code>XMLStream</code>.
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:Triangle&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/gml:Triangle&gt;)</li>
     * </ul>
     * 
     * @param defaultCRS
     *            default srs for the geometry, this is propagated if no deeper <code>srsName</code> attribute is
     *            specified
     * @return corresponding {@link Triangle} object
     * @throws XMLParsingException
     *             if a syntactical (or semantic) error is detected in the element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    Triangle parseTriangle( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        validateInterpolationAttribute( xmlStream, "planar" );

        xmlStream.nextTag();
        xmlStream.require( START_ELEMENT, GMLNS, "exterior" );
        if ( xmlStream.nextTag() != START_ELEMENT ) {
            String msg = "Error in 'gml:Triangle' element. Expected a 'gml:LinearRing' element.";
            throw new XMLParsingException( xmlStream, msg );
        }

        LinearRing exteriorRing = geometryParser.parseLinearRing( xmlStream, defaultCRS );
        if ( exteriorRing.getControlPoints().size() != 4 ) {
            String msg = "Error in 'gml:Triangle' element. Exterior ring must contain exactly four points, but contains "
                         + exteriorRing.getControlPoints().size();
            throw new XMLParsingException( xmlStream, msg );
        }

        xmlStream.nextTag();
        xmlStream.require( END_ELEMENT, GMLNS, "exterior" );
        xmlStream.nextTag();
        xmlStream.require( END_ELEMENT, GMLNS, "Triangle" );

        return geomFac.createTriangle( exteriorRing );
    }

    private void validateInterpolationAttribute( XMLStreamReaderWrapper xmlStream, String expected )
                            throws XMLParsingException {
        String actual = xmlStream.getAttributeValue( null, "interpolation" );
        if ( actual != null && !expected.equals( actual ) ) {
            String msg = "Invalid value (='" + actual + "') for interpolation attribute in element '"
                         + xmlStream.getName() + "'. Must be '" + expected + "'.";
            throw new XMLParsingException( xmlStream, msg );
        }
    }
}
