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
package org.deegree.gml.geometry;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.patches.Cone;
import org.deegree.geometry.primitive.patches.Cylinder;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.Rectangle;
import org.deegree.geometry.primitive.patches.Sphere;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.primitive.patches.Triangle;

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
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
class GML3SurfacePatchReader extends GML3GeometryBaseReader {

    private GML3GeometryReader geometryParser;

    /**
     * @param geometryParser
     * @param geomFac
     * @param defaultCoordDim 
     */
    GML3SurfacePatchReader( GML3GeometryReader geometryParser, GeometryFactory geomFac, int defaultCoordDim ) {
        super( geometryParser.version, geomFac, defaultCoordDim );
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

        if ( !gmlNs.equals( xmlStream.getNamespaceURI() ) ) {
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
            patch = parseSphere( xmlStream, defaultCRS );
        } else if ( name.equals( "Triangle" ) ) {
            patch = parseTriangle( xmlStream, defaultCRS );
        } else {
            String msg = "Invalid GML geometry: '" + xmlStream.getName()
                         + "' is not a valid substitution for '_SurfacePatch'.";
            throw new XMLParsingException( xmlStream, msg );
        }
        return patch;
    }

    /**
     * Returns the object representation for a <code>gml:Cone</code> element.
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:Cone&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/gml:Cone&gt;)</li>
     * </ul>
     * <p>
     * 
     * @param xmlStream
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the "gml:Cone" has no <code>srsName</code>
     *            attribute
     * @return corresponding {@link Cone} object
     * @throws XMLStreamException
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    private Cone parseCone( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {
        validateAttribute( xmlStream, "horizontalCurveType", "circularArc3Points" );
        validateAttribute( xmlStream, "verticalCurveType", "linear" );

        xmlStream.nextTag();
        xmlStream.require( START_ELEMENT, gmlNs, "row" );

        List<Points> grid = new ArrayList<Points>();
        while ( xmlStream.getLocalName().equals( "row" ) ) {
            xmlStream.nextTag();
            List<Point> currentRow = new LinkedList<Point>();
            if ( xmlStream.getLocalName().equals( "posList" ) ) {
                currentRow = geometryParser.parsePosList( xmlStream, defaultCRS );
                xmlStream.nextTag();
            } else {
                while ( xmlStream.getLocalName().equals( "pos" ) || xmlStream.getLocalName().equals( "pointProperty" ) ) {
                    if ( xmlStream.getLocalName().equals( "pos" ) ) {
                        Point point = geometryParser.parsePoint( xmlStream, defaultCRS );
                        currentRow.add( point );
                    } else {
                        Point point = geometryParser.parsePointProperty( xmlStream, defaultCRS );
                        currentRow.add( point );
                    }
                    xmlStream.nextTag();
                }
            }
            grid.add( geomFac.createPoints( currentRow ) );
            xmlStream.require( END_ELEMENT, gmlNs, "row" );
            xmlStream.nextTag();
        }

        xmlStream.require( START_ELEMENT, gmlNs, "rows" );
        xmlStream.getElementTextAsPositiveInteger(); // redundant; one can determine it from the grid
        xmlStream.require( END_ELEMENT, gmlNs, "rows" );
        xmlStream.nextTag();
        xmlStream.require( START_ELEMENT, gmlNs, "columns" );
        xmlStream.getElementTextAsPositiveInteger(); // redundant; one can determine it from the grid
        xmlStream.require( END_ELEMENT, gmlNs, "columns" );
        xmlStream.nextTag();
        xmlStream.require( END_ELEMENT, gmlNs, "Cone" );

        return geomFac.createCone( grid );
    }

    /**
     * Returns the object representation for a <code>gml:Cylinder</code> element.
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:Cylidner&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/gml:Cylinder&gt;)</li>
     * </ul>
     * <p>
     * 
     * @param xmlStream
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the "gml:Cylinder" has no <code>srsName</code>
     *            attribute
     * @return corresponding {@link Cylinder} object
     * @throws XMLStreamException
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    private Cylinder parseCylinder( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {
        validateAttribute( xmlStream, "horizontalCurveType", "circularArc3Points" );
        validateAttribute( xmlStream, "verticalCurveType", "linear" );

        xmlStream.nextTag();
        xmlStream.require( START_ELEMENT, gmlNs, "row" );

        List<Points> grid = new ArrayList<Points>();
        while ( xmlStream.getLocalName().equals( "row" ) ) {
            xmlStream.nextTag();
            List<Point> currentRow = new LinkedList<Point>();
            if ( xmlStream.getLocalName().equals( "posList" ) ) {
                currentRow = geometryParser.parsePosList( xmlStream, defaultCRS );
                xmlStream.nextTag();
            } else {
                while ( xmlStream.getLocalName().equals( "pos" ) || xmlStream.getLocalName().equals( "pointProperty" ) ) {
                    if ( xmlStream.getLocalName().equals( "pos" ) ) {
                        Point point = geometryParser.parsePoint( xmlStream, defaultCRS );
                        currentRow.add( point );
                    } else {
                        Point point = geometryParser.parsePointProperty( xmlStream, defaultCRS );
                        currentRow.add( point );
                    }
                    xmlStream.nextTag();
                }
            }
            grid.add( geomFac.createPoints( currentRow ) );
            xmlStream.require( END_ELEMENT, gmlNs, "row" );
            xmlStream.nextTag();
        }

        xmlStream.require( START_ELEMENT, gmlNs, "rows" );
        xmlStream.getElementTextAsPositiveInteger(); // redundant; one can determine it from the grid
        xmlStream.require( END_ELEMENT, gmlNs, "rows" );
        xmlStream.nextTag();
        xmlStream.require( START_ELEMENT, gmlNs, "columns" );
        xmlStream.getElementTextAsPositiveInteger(); // redundant; one can determine it from the grid
        xmlStream.require( END_ELEMENT, gmlNs, "columns" );
        xmlStream.nextTag();
        xmlStream.require( END_ELEMENT, gmlNs, "Cylinder" );

        return geomFac.createCylinder( grid );
    }

    /**
     * Returns the object representation for a <code>gml:Sphere</code> element.
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:Sphere&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/gml:Sphere&gt;)</li>
     * </ul>
     * <p>
     * 
     * @param xmlStream
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the "gml:Sphere" has no <code>srsName</code>
     *            attribute
     * @return corresponding {@link Sphere} object
     * @throws XMLStreamException
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    private Sphere parseSphere( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {
        validateAttribute( xmlStream, "horizontalCurveType", "circularArc3Points" );
        validateAttribute( xmlStream, "verticalCurveType", "circularArc3Points" );

        xmlStream.nextTag();
        xmlStream.require( START_ELEMENT, gmlNs, "row" );

        List<Points> grid = new ArrayList<Points>();
        while ( xmlStream.getLocalName().equals( "row" ) ) {
            xmlStream.nextTag();
            List<Point> currentRow = new LinkedList<Point>();
            if ( xmlStream.getLocalName().equals( "posList" ) ) {
                currentRow = geometryParser.parsePosList( xmlStream, defaultCRS );
                xmlStream.nextTag();
            } else {
                while ( xmlStream.getLocalName().equals( "pos" ) || xmlStream.getLocalName().equals( "pointProperty" ) ) {
                    if ( xmlStream.getLocalName().equals( "pos" ) ) {
                        Point point = geometryParser.parsePoint( xmlStream, defaultCRS );
                        currentRow.add( point );
                    } else {
                        Point point = geometryParser.parsePointProperty( xmlStream, defaultCRS );
                        currentRow.add( point );
                    }
                    xmlStream.nextTag();
                }
            }
            grid.add( geomFac.createPoints( currentRow ) );
            xmlStream.require( END_ELEMENT, gmlNs, "row" );
            xmlStream.nextTag();
        }

        xmlStream.require( START_ELEMENT, gmlNs, "rows" );
        xmlStream.getElementTextAsPositiveInteger(); // redundant; one can determine it from the grid
        xmlStream.require( END_ELEMENT, gmlNs, "rows" );
        xmlStream.nextTag();
        xmlStream.require( START_ELEMENT, gmlNs, "columns" );
        xmlStream.getElementTextAsPositiveInteger(); // redundant; one can determine it from the grid
        xmlStream.require( END_ELEMENT, gmlNs, "columns" );
        xmlStream.nextTag();
        xmlStream.require( END_ELEMENT, gmlNs, "Sphere" );

        return geomFac.createSphere( grid );
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

        validateAttribute( xmlStream, "interpolation", "planar" );

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
                xmlStream.require( END_ELEMENT, gmlNs, "exterior" );
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
                xmlStream.require( END_ELEMENT, gmlNs, "interior" );
            } else {
                String msg = "Error in 'gml:Polygon' element. Expected a 'gml:interior' element.";
                throw new XMLParsingException( xmlStream, msg );
            }
            xmlStream.nextTag();
        }
        xmlStream.require( END_ELEMENT, gmlNs, "PolygonPatch" );
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
        validateAttribute( xmlStream, "interpolation", "planar" );

        xmlStream.nextTag();
        xmlStream.require( START_ELEMENT, gmlNs, "exterior" );
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
        xmlStream.require( END_ELEMENT, gmlNs, "exterior" );
        xmlStream.nextTag();
        xmlStream.require( END_ELEMENT, gmlNs, "Rectangle" );

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

        validateAttribute( xmlStream, "interpolation", "planar" );

        xmlStream.nextTag();
        xmlStream.require( START_ELEMENT, gmlNs, "exterior" );
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
        xmlStream.require( END_ELEMENT, gmlNs, "exterior" );
        xmlStream.nextTag();
        xmlStream.require( END_ELEMENT, gmlNs, "Triangle" );

        return geomFac.createTriangle( exteriorRing );
    }

    private void validateAttribute( XMLStreamReaderWrapper xmlStream, String attributeName, String expected ) {
        String actual = xmlStream.getAttributeValue( null, attributeName );
        if ( actual != null && !expected.equals( actual ) ) {
            String msg = "Invalid value (='" + actual + "') for " + attributeName + " attribute in element '"
                         + xmlStream.getName() + "'. Must be '" + expected + "'.";
            throw new XMLParsingException( xmlStream, msg );
        }
    }
}
