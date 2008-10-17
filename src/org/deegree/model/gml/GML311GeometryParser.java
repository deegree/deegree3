//$HeadURL$
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
package org.deegree.model.gml;

import static org.deegree.commons.xml.CommonNamespaces.GMLNS;

import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.GeometryFactory;
import org.deegree.model.geometry.composite.CompositeCurve;
import org.deegree.model.geometry.composite.CompositeSurface;
import org.deegree.model.geometry.multi.MultiCurve;
import org.deegree.model.geometry.multi.MultiGeometry;
import org.deegree.model.geometry.multi.MultiPoint;
import org.deegree.model.geometry.multi.MultiSurface;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.CurveSegment;
import org.deegree.model.geometry.primitive.Envelope;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Surface;
import org.deegree.model.geometry.primitive.Curve.Orientation;
import org.deegree.model.geometry.primitive.curvesegments.LineStringSegment;
import org.deegree.model.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GML311GeometryParser extends GML311BaseParser {

    private static Logger LOG = LoggerFactory.getLogger( GML311GeometryParser.class );

    private static String FID = "gid";

    private static String GMLID = "id";

    private final GML311CurveSegmentParser curveSegmentParser;

    public GML311GeometryParser( GeometryFactory geomFac, XMLStreamReaderWrapper xmlStream ) {
        super( geomFac, xmlStream );
        curveSegmentParser = new GML311CurveSegmentParser( this, geomFac, xmlStream );
    }

    /**
     * Returns the object representation for the given <code>gml:_Geometry</code> element event that the cursor of the
     * given <code>XMLStreamReader</code> points at.
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_Geometry&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/gml:_Geometry&gt;)</li>
     * </ul>
     * <p>
     * Currently, the following geometry elements are supported:
     * <table border="1">
     * <tr>
     * <th>Geometry element name<br>
     * (in gml namespace)</th>
     * <th>Return type<br>
     * (deegree {@link Geometry})</th>
     * </tr>
     * <tr>
     * <td align="center">Box/Envelope</td>
     * <td align="center">{@link Envelope}</td>
     * </tr>
     * <tr>
     * <td align="center">CompositeSurface</td>
     * <td align="center">{@link CompositeSurface}</td>
     * </tr>
     * <tr>
     * <td align="center">Curve</td>
     * <td align="center">{@link Curve}</td>
     * </tr>
     * <tr>
     * <td align="center">LinearRing</td>
     * <td align="center">{@link Curve}</td>
     * </tr>
     * <tr>
     * <td align="center">LineString</td>
     * <td align="center">{@link Curve}</td>
     * </tr>
     * <tr>
     * <td align="center">MultiCurve</td>
     * <td align="center">{@link MultiCurve}</td>
     * </tr>
     * <tr>
     * <td align="center">MultiGeometry</td>
     * <td align="center">{@link MultiGeometry}</td>
     * </tr>
     * <tr>
     * <td align="center">MultiLineString</td>
     * <td align="center">{@link MultiCurve}</td>
     * </tr>
     * <tr>
     * <td align="center">MultiPoint</td>
     * <td align="center">{@link MultiPoint}</td>
     * </tr>
     * <tr>
     * <td align="center">MultiPolygon</td>
     * <td align="center">{@link MultiSurface}</td>
     * </tr>
     * <tr>
     * <td align="center">MultiSurface</td>
     * <td align="center">{@link MultiSurface}</td>
     * </tr>
     * <tr>
     * <td align="center">Point/Center</td>
     * <td align="center">{@link Point}</td>
     * </tr>
     * <tr>
     * <td align="center">Polygon</td>
     * <td align="center">{@link Surface}</td>
     * </tr>
     * <tr>
     * <td align="center">Ring</td>
     * <td align="center">{@link Curve}</td>
     * </tr>
     * <tr>
     * <td align="center">Surface</td>
     * <td align="center">{@link Surface}</td>
     * </tr>
     * </table>
     * 
     * @param defaultSrsName
     *            default srs for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Geometry} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_Geometry" element
     * @throws XMLStreamException
     */
    public Geometry parseGeometry( String defaultSrsName )
                            throws XMLParsingException, XMLStreamException {

        LOG.debug( " - parsing gml:_Geometry (begin): " + xmlStream.getCurrentEventInfo() );

        Geometry geometry = null;

        if ( !GMLNS.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Invalid gml:_Geometry element: " + xmlStream.getName()
                         + "' is not a GML geometry element. Not in the gml namespace.";
            throw new XMLParsingException( xmlStream, msg );
        }

        String name = xmlStream.getLocalName();
        if ( name.equals( "Point" ) ) {
            geometry = parsePoint( defaultSrsName );
        } else {
            String msg = "Invalid GML geometry: '" + xmlStream.getName()
                         + "' is not a (supported) GML geometry element.";
            throw new XMLParsingException( xmlStream, msg );
        }

        LOG.debug( " - parsing gml:_Geometry (end): " + xmlStream.getCurrentEventInfo() );
        return geometry;
    }

    /**
     * Returns the object representation for the given <code>gml:_Curve</code> element event that the cursor of the
     * given <code>XMLStreamReader</code> points at.
     * <p>
     * The following concrete substitutions for <code>gml:_Curve</code> are defined:
     * <table border="1">
     * <tr>
     * <th>Geometry element name<br>
     * (in gml namespace)</th>
     * <th>Return type<br>
     * (deegree {@link Geometry})</th>
     * </tr>
     * <tr>
     * <td align="center">LineString</td>
     * <td align="center">{@link Curve}</td>
     * </tr>
     * <tr>
     * <td align="center">Curve</td>
     * <td align="center">{@link Curve}</td>
     * </tr>
     * <tr>
     * <td align="center">CompositeCurve</td>
     * <td align="center">{@link CompositeCurve}</td>
     * </tr>
     * <tr>
     * <td align="center">OrientableCurve</td>
     * <td align="center">{@link CompositeCurve}</td>
     * </tr>
     * </table>
     * 
     * @param defaultSrsName
     *            default srs for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Geometry} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_Curve" element
     * @throws XMLStreamException
     */
    public Curve parseAbstractCurve( String defaultSrsName )
                            throws XMLParsingException, XMLStreamException {

        LOG.debug( " - parsing gml:_Curve (begin): " + xmlStream.getCurrentEventInfo() );

        Curve curve = null;

        if ( !GMLNS.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Invalid gml:_Curve element: " + xmlStream.getName()
                         + "' is not a GML geometry element. Not in the gml namespace.";
            throw new XMLParsingException( xmlStream, msg );
        }

        String name = xmlStream.getLocalName();
        if ( name.equals( "LineString" ) ) {
            curve = parseLineString( defaultSrsName );
        } else if ( name.equals( "Curve" ) ) {
            String msg = "Parsing of 'gml:Curve' elements is not implemented yet.";
            throw new XMLParsingException( xmlStream, msg );
        } else if ( name.equals( "CompositeCurve" ) ) {
            String msg = "Parsing of 'gml:CompositeCurve' elements is not implemented yet.";
            throw new XMLParsingException( xmlStream, msg );
        } else if ( name.equals( "OrientableCurve" ) ) {
            String msg = "Parsing of 'gml:OrientableCurve' elements is not implemented yet.";
            throw new XMLParsingException( xmlStream, msg );
        } else {
            String msg = "Invalid GML geometry: '" + xmlStream.getName()
                         + "' is not a valid substitution for '_Curve'.";
            throw new XMLParsingException( xmlStream, msg );
        }

        LOG.debug( " - parsing gml:_Curve (end): " + xmlStream.getCurrentEventInfo() );
        return curve;
    }

    /**
     * Returns the object representation of a (&lt;gml:Point&gt;) element. Consumes all corresponding events from the
     * given <code>XMLStream</code>.
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:Point&gt;)</li>
     * <li>Postcondition: cursor points at the next event after the <code>END_ELEMENT</code> (&lt;/gml:Point&gt;)</li>
     * </ul>
     * 
     * @param defaultSrsName
     *            default srs for the geometry, this is used if the "gml:Point" has no <code>srsName</code> attribute
     * @return corresponding {@link Point} object
     * @throws XMLParsingException
     * @throws XMLStreamException
     */
    public Point parsePoint( String defaultSrsName )
                            throws XMLParsingException, XMLStreamException {

        Point point = null;
        String gid = parseGeometryId();
        String srsName = determineCurrentSrsName( defaultSrsName );

        // must contain exactly one of the following child elements: "gml:pos", "gml:coordinates" or "gml:coord"
        if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
            String name = xmlStream.getLocalName();
            if ( "pos".equals( name ) ) {
                double[] coords = parseDoubleList();
                point = geomFac.createPoint( gid, coords, lookupCRS( srsName ) );
            } else if ( "coordinates".equals( name ) ) {
                List<Point> points = parseCoordinates( srsName );
                if ( points.size() != 1 ) {
                    String msg = "A gml:Point element must contain exactly one tuple of coordinates.";
                    throw new XMLParsingException( xmlStream, msg );
                }
                point = points.get( 0 );
            } else if ( "coord".equals( name ) ) {
                // deprecated since GML 3.0, only included for backward compatibility
                double[] coords = parseCoordType();
                point = geomFac.createPoint( gid, coords, lookupCRS( srsName ) );
            } else {
                String msg = "Error in 'gml:Point' element. Expected either a 'gml:pos', 'gml:coordinates'"
                             + " or a 'gml:coord' element, but found '" + name + "'.";
                throw new XMLParsingException( xmlStream, msg );
            }
        } else {
            String msg = "Error in 'gml:Point' element. Must contain one of the following child elements: 'gml:pos', 'gml:coordinates'"
                         + " or 'gml:coord'.";
            throw new XMLParsingException( xmlStream, msg );
        }
        // ensure that the stream points to the "gml:Point" end element event
        xmlStream.skipElement();
        return point;
    }

    /**
     * TODO handled xlinked content ("xlink:href")
     * 
     * @param defaultSrsName
     * 
     * @return
     * @throws XMLStreamException
     */
    public Point parsePointProperty( String defaultSrsName )
                            throws XMLStreamException {
        Point point = null;
        if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
            // must be a 'gml:Point' element
            if ( !xmlStream.getLocalName().equals( "Point" ) ) {
                String msg = "Error in 'gml:pointProperty' element. Expected a 'gml:Point' element.";
                throw new XMLParsingException( xmlStream, msg );
            }
            point = parsePoint( defaultSrsName );
        } else {
            String msg = "Error in 'gml:pointProperty' element. Expected a 'gml:Point' element.";
            throw new XMLParsingException( xmlStream, msg );
        }
        xmlStream.skipElement();
        return point;
    }

    /**
     * Returns the object representation of a (&lt;gml:LineString&gt;) element. Consumes all corresponding events from
     * the given <code>XMLStream</code>.
     *
     * @param defaultSrsName
     *            default srs for the geometry, this is only used if the "gml:LineString" has no <code>srsName</code>
     *            attribute
     * @return corresponding {@link Curve} object
     * @throws XMLParsingException
     * @throws XMLStreamException
     */
    public Curve parseLineString( String defaultSrsName )
                            throws XMLStreamException {

        String gid = parseGeometryId();
        String srsName = determineCurrentSrsName( defaultSrsName );

        List<Point> points = null;
        if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
            String name = xmlStream.getLocalName();
            if ( "posList".equals( name ) ) {
                points = parsePosList( srsName );
                xmlStream.skipElement();
            } else if ( "coordinates".equals( name ) ) {
                points = parseCoordinates( srsName );
                xmlStream.skipElement();
            } else {
                points = new LinkedList<Point>();
                do {
                    if ( "pos".equals( name ) ) {
                        double[] coords = parseDoubleList();
                        points.add( geomFac.createPoint( gid, coords, lookupCRS( srsName ) ) );
                    } else if ( "pointProperty".equals( name ) || "pointRep".equals( name ) ) {
                        points.add( parsePointProperty( srsName ) );
                    } else {
                        String msg = "Error in 'gml:LineString' element.";
                        throw new XMLParsingException( xmlStream, msg );
                    }
                } while ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT );
                if ( xmlStream.getEventType() != XMLStreamConstants.END_ELEMENT ) {
                    // ensure that the stream points to the "gml:LineString" end element event
                    xmlStream.skipElement();
                }
            }
        }

        if ( points.size() < 2 ) {
            String msg = "Error in 'gml:LineString' element. Must consist of two points at least.";
            throw new XMLParsingException( xmlStream, msg );
        }

        LineStringSegment singleSegment = geomFac.createLineStringSegment( points );
        return geomFac.createCurve( gid, new CurveSegment[] { singleSegment }, Orientation.positive,
                                    lookupCRS( srsName ) );
    }

    /**
     * Returns the object representation of a (&lt;gml:Curve&gt;) element. Consumes all corresponding events from the
     * given <code>XMLStream</code>.
     * 
     * @param defaultSrsName
     *            default srs for the geometry, this is only used if the "gml:Curve" has no <code>srsName</code>
     *            attribute
     * @return corresponding {@link Curve} object
     * @throws XMLStreamException
     * @throws XMLParsingException
     */
    public Curve parseCurve( String defaultSrsName )
                            throws XMLStreamException {

        String gid = parseGeometryId();
        String srsName = determineCurrentSrsName( defaultSrsName );

        xmlStream.nextTag();
        xmlStream.require( XMLStreamConstants.START_ELEMENT, GMLNS, "segments" );
        List<CurveSegment> segments = new LinkedList<CurveSegment>();

        while ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
            segments.add( curveSegmentParser.parseCurveSegment( srsName ) );
        }

        xmlStream.skipElement();
        return geomFac.createCurve( gid, segments.toArray( new CurveSegment[segments.size()] ), Orientation.positive,
                                    lookupCRS( srsName ) );
    }

    /**
     * Parses the geometry id attribute from the geometry <code>START_ELEMENT</code> event that the given
     * <code>XMLStreamReader</code> points to.
     * <p>
     * Looks after 'gml:id' (GML 3) first, if no such attribute is present, the 'gid' (GML 2) attribute is used.
     * 
     * @return the geometry id, or "" (empty string) if neither a 'gml:id' nor a 'gid' attribute is present
     */
    private String parseGeometryId() {

        String gid = xmlStream.getAttributeValue( GMLNS, GMLID );
        if ( gid == null ) {
            gid = xmlStream.getAttributeValue( null, FID );
        }

        // Check that the geometry id has the correct form. "gid" and "gml:id" are both based
        // on the XML type "ID": http://www.w3.org/TR/xmlschema11-2/#NCName
        // Thus, they must match the NCName production rule. This means that they may not contain
        // a separating colon (only at the first position a colon is allowed) and must not
        // start with a digit.
        if ( gid != null && gid.length() > 0 && !gid.matches( "[^\\d][^:]+" ) ) {
            String msg = Messages.getMessage( "ERROR_INVALID_FEATUREID", gid );
            throw new IllegalArgumentException( msg );
        }
        return gid;
    }
}
