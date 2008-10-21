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

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.GeometryFactory;
import org.deegree.model.geometry.AbstractSolid.SolidType;
import org.deegree.model.geometry.AbstractSurface.SurfaceType;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.CurveSegment;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Ring;
import org.deegree.model.geometry.primitive.Solid;
import org.deegree.model.geometry.primitive.Surface;
import org.deegree.model.geometry.primitive.SurfacePatch;
import org.deegree.model.geometry.primitive.Curve.CurveType;
import org.deegree.model.geometry.primitive.Curve.Orientation;
import org.deegree.model.geometry.primitive.Ring.RingType;
import org.deegree.model.geometry.primitive.curvesegments.LineStringSegment;
import org.deegree.model.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parser for geometry and geometry-related constructs from the GML 3.1.1 specification.
 * <p>
 * Supports all abstract (8) and concrete (24) geometry elements:
 * <ul>
 * <ul>
 * </p>
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

    // local names of all concrete elements substitutable for "gml:_Curve"
    private static final Set<String> curveElements = new HashSet<String>();

    // local names of all concrete elements substitutable for "gml:_Ring"
    private static final Set<String> ringElements = new HashSet<String>();

    // local names of all concrete elements substitutable for "gml:_Surface"
    private static final Set<String> surfaceElements = new HashSet<String>();

    // local names of all concrete elements substitutable for "gml:_Solid"
    private static final Set<String> solidElements = new HashSet<String>();

    // local names of all concrete elements substitutable for "gml:_GeometricPrimitive"
    private static final Set<String> primitiveElements = new HashSet<String>();

    // local names of all concrete elements substitutable for "gml:_GeometricAggregate"
    private static final Set<String> aggregateElements = new HashSet<String>();

    // local names of all concrete elements substitutable for "gml:_ImplicitGeometry"
    private static final Set<String> implictGeometryElements = new HashSet<String>();

    // local names of all concrete elements substitutable for "gml:_GeometricComplex"
    private static final Set<String> complexElements = new HashSet<String>();

    static {

        // substitutions for "gml:_Curve"
        curveElements.add( "CompositeCurve" );
        curveElements.add( "Curve" );
        curveElements.add( "OrientableCurve" );
        curveElements.add( "LineString" );

        // substitutions for "gml:_Ring"
        ringElements.add( "LinearRing" );
        ringElements.add( "Ring" );

        // substitutions for "gml:_Surface"
        surfaceElements.add( "CompositeSurface" );
        surfaceElements.add( "OrientableSurface" );
        surfaceElements.add( "Polygon" );
        surfaceElements.add( "PolyhedralSurface" );
        surfaceElements.add( "Surface" );
        surfaceElements.add( "Tin" );
        surfaceElements.add( "TriangulatedSurface" );

        // substitutions for "gml:_Solid"
        surfaceElements.add( "CompositeSolid" );
        surfaceElements.add( "Solid" );

        // substitutions for "gml:_GeometricPrimitive"
        primitiveElements.add( "Point" );
        primitiveElements.addAll( curveElements );
        primitiveElements.addAll( ringElements );
        primitiveElements.addAll( surfaceElements );
        primitiveElements.addAll( solidElements );

        // substitutions for "gml:_GeometricAggregate"
        aggregateElements.add( "MultiCurve" );
        aggregateElements.add( "MultiGeometry" );
        aggregateElements.add( "MultiLineString" );
        aggregateElements.add( "MultiPoint" );
        aggregateElements.add( "MultiPolygon" );
        aggregateElements.add( "MultiSolid" );
        aggregateElements.add( "MultiSurface" );

        // substitutions for "gml:_ImplicitGeometry"
        aggregateElements.add( "Grid" );
        aggregateElements.add( "RectifiedGrid" );

        // GeometricComplex elements
        // Note: only for technical reasons (XML Schema does not support multiple inheritance), there is no substitution
        // group "gml:_GeometricComplex"
        complexElements.add( "CompositeCurve" );
        complexElements.add( "CompositeSolid" );
        complexElements.add( "CompositeSurface" );
        complexElements.add( "GeometricComplex" );
    }

    public GML311GeometryParser( GeometryFactory geomFac, XMLStreamReaderWrapper xmlStream ) {
        super( geomFac, xmlStream );
        curveSegmentParser = new GML311CurveSegmentParser( this, geomFac, xmlStream );
    }

    /**
     * Returns the object representation for the given <code>gml:_Geometry</code> element event that the cursor of the
     * associated <code>XMLStreamReader</code> points at.
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_Geometry&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/gml:_Geometry&gt;)</li>
     * </ul>
     * <p>
     * GML 3.1.1 specifies the following abstract elements to be <b>directly</b> substitutable for
     * <code>gml:_Geometry</code>:
     * <ul>
     * <li><code>_GeometricPrimitive</code></li>
     * <li><code>_Ring</code></li>
     * <li><code>_GeometricAggregate</code></li>
     * <li><code>_GeometricComplex</code></li>
     * <li><code>_ImplicitGeometry</code></li>
     * </ul>
     * <p>
     * Note: for technical reasons (XML Schema cannot express multiple inheritance) the abstract substitution group
     * <code>gml:_GeometricComplex</code> is not strictly defined in the GML schemas, but it is described in the
     * comments.
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
        if ( primitiveElements.contains( name ) ) {
            geometry = parseGeometricPrimitive( defaultSrsName );
        } else if ( ringElements.contains( name ) ) {
            geometry = parseAbstractRing( defaultSrsName );
        } else if ( aggregateElements.contains( name ) ) {
            geometry = parseGeometricAggregate( defaultSrsName );
        } else if ( complexElements.contains( name ) ) {
            geometry = parseAbstractGeometricComplex( defaultSrsName );
        } else if ( implictGeometryElements.contains( name ) ) {
            geometry = parseImplicitGeometry( defaultSrsName );
        } else {
            String msg = "Invalid GML geometry: '" + xmlStream.getName() + "' is not a GML geometry element.";
            throw new XMLParsingException( xmlStream, msg );
        }

        LOG.debug( " - parsing gml:_Geometry (end): " + xmlStream.getCurrentEventInfo() );
        return geometry;
    }

    /**
     * Returns the object representation for the given <code>gml:_GeometricPrimitive</code> element event that the
     * cursor of the associated <code>XMLStreamReader</code> points at.
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_GeometricPrimitive&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event
     * (&lt;/gml:_GeometricPrimitive&gt;)</li>
     * </ul>
     * <p>
     * GML 3.1.1 specifies the following elements to be <b>directly</b> substitutable for
     * <code>gml:_GeometricPrimitive</code>:
     * <ul>
     * <li><code>Point</code></li>
     * <li><code>_Curve</code></li>
     * <li><code>_Surface</code></li>
     * <li><code>_Solid</code></li>
     * </ul>
     * 
     * @param defaultSrsName
     *            default srs for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Geometry} object
     * @throws XMLParsingException
     *             if the element is not a valid <code>gml:_GeometricPrimitive</code> element
     * @throws XMLStreamException
     */
    public Geometry parseGeometricPrimitive( String defaultSrsName )
                            throws XMLParsingException, XMLStreamException {

        LOG.debug( " - parsing gml:_GeometricPrimitive (begin): " + xmlStream.getCurrentEventInfo() );

        Geometry geometry = null;

        if ( !GMLNS.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Invalid gml:_Geometry element: " + xmlStream.getName()
                         + "' is not a GML geometry element. Not in the gml namespace.";
            throw new XMLParsingException( xmlStream, msg );
        }

        String name = xmlStream.getLocalName();
        if ( name.equals( "Point" ) ) {
            geometry = parsePoint( defaultSrsName );
        } else if ( curveElements.contains( name ) ) {
            geometry = parseAbstractCurve( defaultSrsName );
        } else if ( ringElements.contains( name ) ) {
            geometry = parseAbstractRing( defaultSrsName );
        } else if ( surfaceElements.contains( name ) ) {
            geometry = parseAbstractSurface( defaultSrsName );
        } else if ( solidElements.contains( name ) ) {
            geometry = parseAbstractSolid( defaultSrsName );
        } else {
            String msg = "Invalid GML geometry: '" + xmlStream.getName()
                         + "' is not a GML primitive geometry element (gml:_GeometricPrimitive).";
            throw new XMLParsingException( xmlStream, msg );
        }

        LOG.debug( " - parsing gml:_GeometricPrimitive (end): " + xmlStream.getCurrentEventInfo() );
        return geometry;
    }

    /**
     * Returns the object representation for the given <code>gml:_GeometricAggregate</code> element event that the
     * cursor of the given <code>XMLStreamReader</code> points at.
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_GeometricAggregate&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event
     * (&lt;/gml:_GeometricAggregate&gt;)</li>
     * </ul>
     * <p>
     * GML 3.1.1 specifies the following concrete elements to be substitutable for <code>gml:_GeometricAggregate</code>:
     * <ul>
     * <li><code>MultiCurve</code></li>
     * <li><code>MultiGeometry</code></li>
     * <li><code>MultiLineString</code></li>
     * <li><code>MultiPoint</code></li>
     * <li><code>MultiPolygon</code></li>
     * <li><code>MultiSolid</code></li>
     * <li><code>MultiSurface</code></li>
     * </ul>
     * 
     * @param defaultSrsName
     *            default srs for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Geometry} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_GeometricAggregate" element
     * @throws XMLStreamException
     */
    public Geometry parseGeometricAggregate( String defaultSrsName )
                            throws XMLParsingException, XMLStreamException {

        LOG.debug( " - parsing gml:_GeometricAggregate (begin): " + xmlStream.getCurrentEventInfo() );

        Geometry geometry = null;

        if ( !GMLNS.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Invalid gml:_GeometricAggregate element: " + xmlStream.getName()
                         + "' is not a GML geometry element. Not in the gml namespace.";
            throw new XMLParsingException( xmlStream, msg );
        }

        String name = xmlStream.getLocalName();
        if ( name.equals( "MultiCurve" ) ) {
        } else if ( name.equals( "MultiGeometry" ) ) {
        } else if ( name.equals( "MultiLineString" ) ) {
        } else if ( name.equals( "MultiPoint" ) ) {
        } else if ( name.equals( "MultiPolygon" ) ) {
        } else if ( name.equals( "MultiSolid" ) ) {
        } else if ( name.equals( "MultiSurface" ) ) {
        } else {
            String msg = "Invalid GML geometry: '" + xmlStream.getName()
                         + "' is not a GML aggregate geometry element (gml:_GeometricAggregate).";
            throw new XMLParsingException( xmlStream, msg );
        }

        LOG.debug( " - parsing gml:_GeometricAggregate (end): " + xmlStream.getCurrentEventInfo() );
        return geometry;
    }

    /**
     * Returns the object representation for the given <code>gml:_Geometry</code> event with geometric complex semantic
     * (either <code>gml:CompositeCurve</code>, <code>gml:CompositeSolid</code>, <code>gml:CompositeSurface</code> or
     * <code>gml:GeometricComplex</code>), that the cursor of the given <code>XMLStreamReader</code> points at.
     * <p>
     * 
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event</li>
     * </ul>
     * <p>
     * GML 3.1.1 specifies the following four concrete geometry elements with an geometry complex semantic</code>:
     * <ul>
     * <li><code>CompositeCurve</code></li>
     * <li><code>CompositeSolid</code></li>
     * <li><code>CompositeSurface</code></li>
     * <li><code>GeometricComplex</code></li>
     * </ul>
     * <p>
     * NOTE: For technical reasons (XML Schema does not support multiple inheritance), there is no substitution group
     * <code>gml:_GeometricComplex</code> defined in the GML schemas. However, it is described in the comments.
     * 
     * @param defaultSrsName
     *            default srs for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Geometry} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_Geometry" element with geometric complex semantic
     * @throws XMLStreamException
     */
    public Geometry parseAbstractGeometricComplex( String defaultSrsName )
                            throws XMLParsingException, XMLStreamException {

        LOG.debug( " - parsing gml:_GeometricComplex (begin): " + xmlStream.getCurrentEventInfo() );

        Geometry geometry = null;

        if ( !GMLNS.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Invalid gml:_GeometricComplex element: " + xmlStream.getName()
                         + "' is not a GML geometry element. Not in the gml namespace.";
            throw new XMLParsingException( xmlStream, msg );
        }

        String name = xmlStream.getLocalName();
        if ( name.equals( "ComplexCurve" ) ) {
            String msg = "Parsing of 'gml:" + xmlStream.getLocalName() + "' elements is not implemented yet.";
            throw new XMLParsingException( xmlStream, msg );
        } else if ( name.equals( "ComplexSolid" ) ) {
            String msg = "Parsing of 'gml:" + xmlStream.getLocalName() + "' elements is not implemented yet.";
            throw new XMLParsingException( xmlStream, msg );
        } else if ( name.equals( "ComplexSurface" ) ) {
            String msg = "Parsing of 'gml:" + xmlStream.getLocalName() + "' elements is not implemented yet.";
            throw new XMLParsingException( xmlStream, msg );
        } else if ( name.equals( "GeometricComplex" ) ) {
            String msg = "Parsing of 'gml:" + xmlStream.getLocalName() + "' elements is not implemented yet.";
            throw new XMLParsingException( xmlStream, msg );
        } else {
            String msg = "Invalid GML geometry: '" + xmlStream.getName()
                         + "' is not a (supported) GML geometry element.";
            throw new XMLParsingException( xmlStream, msg );
        }

        // LOG.debug( " - parsing gml:_GeometricComplex (end): " + xmlStream.getCurrentEventInfo() );
        // return geometry;
    }

    /**
     * Returns the object representation for the given <code>gml:_ImplicitGeometry</code> element event that the cursor
     * of the associated <code>XMLStreamReader</code> points at.
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_ImplicitGeometry&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event
     * (&lt;/gml:_GeometricAggregate&gt;)</li>
     * </ul>
     * <p>
     * GML 3.1.1 specifies the following two concrete elements to be substitutable for
     * <code>gml:_ImplicitGeometry</code>:
     * <ul>
     * <li><code>Grid</code></li>
     * <li><code>RectifiedGrid</code></li>
     * </ul>
     * 
     * @param defaultSrsName
     *            default srs for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Geometry} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_ImplicitGeometry" element
     * @throws XMLStreamException
     */
    public Geometry parseImplicitGeometry( String defaultSrsName )
                            throws XMLParsingException, XMLStreamException {

        LOG.debug( " - parsing gml:_ImplicitGeometry (begin): " + xmlStream.getCurrentEventInfo() );

        Geometry geometry = null;

        if ( !GMLNS.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Invalid gml:_ImplicitGeometry element: " + xmlStream.getName()
                         + "' is not a GML geometry element. Not in the gml namespace.";
            throw new XMLParsingException( xmlStream, msg );
        }

        String name = xmlStream.getLocalName();
        if ( name.equals( "Grid" ) ) {
            String msg = "Parsing of 'gml:" + xmlStream.getLocalName() + "' elements is not implemented yet.";
            throw new XMLParsingException( xmlStream, msg );
        } else if ( name.equals( "RectifiedGrid" ) ) {
            String msg = "Parsing of 'gml:" + xmlStream.getLocalName() + "' elements is not implemented yet.";
            throw new XMLParsingException( xmlStream, msg );
        } else {
            String msg = "Invalid GML geometry: '" + xmlStream.getName() + "' is not a GML geometry element.";
            throw new XMLParsingException( xmlStream, msg );
        }

        // LOG.debug( " - parsing gml:_ImplicitGeometry (end): " + xmlStream.getCurrentEventInfo() );
        // return geometry;
    }

    /**
     * Returns the object representation for the given <code>gml:_Curve</code> element event that the cursor of the
     * associated <code>XMLStreamReader</code> points at.
     * <p>
     * GML 3.1.1 specifies the following elements to be substitutable for <code>gml:_Curve</code>:
     * <ul>
     * <li><code>CompositeCurve</code></li>
     * <li><code>Curve</code></li>
     * <li><code>LineString</code></li>
     * <li><code>OrientableCurve</code></li>
     * </ul>
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

        Curve curve = null;

        LOG.debug( " - parsing gml:_Curve (begin): " + xmlStream.getCurrentEventInfo() );

        if ( !GMLNS.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Invalid gml:_Curve element: " + xmlStream.getName()
                         + "' is not a GML geometry element. Not in the gml namespace.";
            throw new XMLParsingException( xmlStream, msg );
        }

        CurveType type = null;
        try {
            type = CurveType.valueOf( xmlStream.getLocalName() );
        } catch ( IllegalArgumentException e ) {
            String msg = "Invalid GML geometry: '" + xmlStream.getName()
                         + "' is not a valid substitution for 'gml:_Curve'.";
            throw new XMLParsingException( xmlStream, msg );
        }

        switch ( type ) {
        case Curve: {
            curve = parseCurve( defaultSrsName );
            break;
        }
        case LineString: {
            curve = parseLineString( defaultSrsName );
            break;
        }
        case CompositeCurve:
        case OrientableCurve: {
            String msg = "Parsing of 'gml:" + xmlStream.getLocalName() + "' elements is not implemented yet.";
            throw new XMLParsingException( xmlStream, msg );
        }
        default:
            // cannot happen by construction
        }

        LOG.debug( " - parsing gml:_Curve (end): " + xmlStream.getCurrentEventInfo() );
        return curve;
    }

    /**
     * Returns the object representation of a (&lt;gml:_Ring&gt;) element. Consumes all corresponding events from the
     * associated <code>XMLStream</code>.
     * <p>
     * GML 3.1.1 specifies the following elements to be substitutable for <code>gml:_Ring</code>:
     * <ul>
     * <li><code>LinearRing</code></li>
     * <li><code>Ring</code></li>
     * </ul>
     * 
     * @param defaultSrsName
     *            default srs for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Ring} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_Ring" element
     * @throws XMLStreamException
     */
    public Ring parseAbstractRing( String defaultSrsName )
                            throws XMLParsingException, XMLStreamException {

        Ring ring = null;

        LOG.debug( " - parsing gml:_Ring (begin): " + xmlStream.getCurrentEventInfo() );

        if ( !GMLNS.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Invalid gml:_Ring element: " + xmlStream.getName()
                         + "' is not a GML geometry element. Not in the gml namespace.";
            throw new XMLParsingException( xmlStream, msg );
        }

        RingType type = null;
        try {
            type = RingType.valueOf( xmlStream.getLocalName() );
        } catch ( IllegalArgumentException e ) {
            String msg = "Invalid GML geometry: '" + xmlStream.getName()
                         + "' is not a valid substitution for 'gml:_Ring'.";
            throw new XMLParsingException( xmlStream, msg );
        }

        switch ( type ) {
        case LinearRing: {
            ring = parseLinearRing( defaultSrsName );
        }
        case Ring: {
            ring = parseRing( defaultSrsName );
            break;
        }
        default:
            // cannot happen by construction
        }

        LOG.debug( " - parsing gml:_Ring (end): " + xmlStream.getCurrentEventInfo() );
        return ring;
    }

    /**
     * Returns the object representation of a (&lt;gml:_Surface&gt;) element. Consumes all corresponding events from the
     * associated <code>XMLStream</code>.
     * <p>
     * GML 3.1.1 specifies the following seven elements to be substitutable for <code>gml:_Surface</code>:
     * <ul>
     * <li><code>Polygon</code></li>
     * <li><code>CompositeSurface</code></li>
     * <li><code>OrientableSurface</code></li>
     * <li><code>PolyhedralSurface</code></li>
     * <li><code>Surface</code></li>
     * <li><code>Tin</code></li>
     * <li><code>TriangulatedSurface</code></li>
     * </ul>
     * 
     * @param defaultSrsName
     *            default srs for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Geometry} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_Ring" element
     * @throws XMLStreamException
     */
    public Surface parseAbstractSurface( String defaultSrsName )
                            throws XMLParsingException, XMLStreamException {

        Surface surface = null;

        LOG.debug( " - parsing gml:_Surface (begin): " + xmlStream.getCurrentEventInfo() );

        if ( !GMLNS.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Invalid gml:_Surface element: " + xmlStream.getName()
                         + "' is not a GML geometry element. Not in the gml namespace.";
            throw new XMLParsingException( xmlStream, msg );
        }

        SurfaceType type = null;
        try {
            type = SurfaceType.valueOf( xmlStream.getLocalName() );
        } catch ( IllegalArgumentException e ) {
            String msg = "Invalid GML geometry: '" + xmlStream.getName()
                         + "' is not a valid substitution for 'gml:_Surface'.";
            throw new XMLParsingException( xmlStream, msg );
        }

        switch ( type ) {
        case Polygon: {
            surface = parsePolygon( defaultSrsName );
            break;
        }
        case CompositeSurface:
        case OrientableSurface:
        case PolyhedralSurface:
        case Surface:
        case Tin:
        case TriangulatedSurface: {
            String msg = "Parsing of 'gml:" + xmlStream.getLocalName() + "' elements is not implemented yet.";
            throw new XMLParsingException( xmlStream, msg );
        }
        default:
            // cannot happen by construction
        }

        LOG.debug( " - parsing gml:_Surface (end): " + xmlStream.getCurrentEventInfo() );
        return surface;
    }

    /**
     * Returns the object representation of a <code>gml:_Solid</code> element. Consumes all corresponding events from
     * the associated <code>XMLStream</code>.
     * <p>
     * GML 3.1.1 specifies the following two elements to be substitutable for <code>gml:_Solid</code> elements:
     * <ul>
     * <li><code>CompositeSolid</code></li>
     * <li><code>Solid</code></li>
     * </ul>
     * 
     * @param defaultSrsName
     *            default srs for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Geometry} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_Ring" element
     * @throws XMLStreamException
     */
    public Solid parseAbstractSolid( String defaultSrsName )
                            throws XMLParsingException, XMLStreamException {

        Solid solid = null;

        LOG.debug( " - parsing gml:_Solid(begin): " + xmlStream.getCurrentEventInfo() );

        if ( !GMLNS.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Invalid gml:_Surface element: " + xmlStream.getName()
                         + "' is not a GML geometry element. Not in the gml namespace.";
            throw new XMLParsingException( xmlStream, msg );
        }

        SolidType type = null;
        try {
            type = SolidType.valueOf( xmlStream.getLocalName() );
        } catch ( IllegalArgumentException e ) {
            String msg = "Invalid GML geometry: '" + xmlStream.getName()
                         + "' is not a valid substitution for 'gml:_Solid'.";
            throw new XMLParsingException( xmlStream, msg );
        }

        switch ( type ) {
        case CompositeSolid:
        case Solid: {
            String msg = "Parsing of 'gml:" + xmlStream.getLocalName() + "' elements is not implemented yet.";
            throw new XMLParsingException( xmlStream, msg );
        }
        default: {
            // cannot happen by construction
        }
        }

        LOG.debug( " - parsing gml:_Solid (end): " + xmlStream.getCurrentEventInfo() );
        return solid;
    }

    /**
     * Returns the object representation of a (&lt;gml:Point&gt;) element. Consumes all corresponding events from the
     * associated <code>XMLStream</code>.
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:Point&gt;)</li>
     * <li>Postcondition: cursor points at the next event after the <code>END_ELEMENT</code> (&lt;/gml:Point&gt;)</li>
     * </ul>
     * 
     * @param defaultSrsName
     *            default srs for the geometry, this is used if the "gml:Point" has no <code>srsName</code> attribute
     * @return corresponding {@link Point} object
     * @throws XMLParsingException
     *             if the element is not a valid <code>gml:Point</code> element
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
     * Returns the object representation of a <code>gml:LineString</code> element. Consumes all corresponding events
     * from the given <code>XMLStream</code>.
     * 
     * @param defaultSrsName
     *            default srs for the geometry, this is only used if the <code>gml:LineString</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link Curve} object
     * @throws XMLParsingException
     *             if the element is not a valid <code>gml:Curve</code> element
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
     * Returns the object representation of a <code>gml:Curve</code> element. Consumes all corresponding events from the
     * associated <code>XMLStream</code>.
     * 
     * @param defaultSrsName
     *            default srs for the geometry, this is only used if the <code>gml:Curve</code> has no
     *            <code>srsName</code> attribute
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
     * Returns the object representation of a <code>gml:LinearRing</code> element. Consumes all corresponding events
     * from the associated <code>XMLStream</code>.
     * 
     * @param defaultSrsName
     *            default srs for the geometry, this is only used if the <code>gml:LinearRing</code> element has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link Ring} object
     * @throws XMLStreamException
     * @throws XMLParsingException
     */
    public Ring parseLinearRing( String defaultSrsName )
                            throws XMLStreamException {

        String gid = parseGeometryId();
        String srsName = determineCurrentSrsName( defaultSrsName );

        List<Point> points = curveSegmentParser.parseControlPoints( srsName );
        if ( points.size() < 4 ) {
            String msg = "Error in 'gml:LinearRing' element. Must specify at least four points.";
            throw new XMLParsingException( xmlStream, msg );
        }
        xmlStream.require( END_ELEMENT, GMLNS, "LinearRing" );

        LineStringSegment segment = geomFac.createLineStringSegment( points );
        Curve curve = geomFac.createCurve( null, new CurveSegment[] { segment }, Orientation.positive,
                                           lookupCRS( srsName ) );
        List<Curve> memberCurves = new ArrayList<Curve>( 1 );
        memberCurves.add( curve );
        return geomFac.createRing( gid, lookupCRS( srsName ), memberCurves );
    }

    /**
     * Returns the object representation of a <code>gml:Ring</code> element. Consumes all corresponding events from the
     * given <code>XMLStream</code>.
     * 
     * @param defaultSrsName
     *            default srs for the geometry, this is only used if the <code>gml:Ring</code> element has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link Ring} object
     * @throws XMLStreamException
     * @throws XMLParsingException
     */
    public Ring parseRing( String defaultSrsName )
                            throws XMLStreamException {

        String gid = parseGeometryId();
        String srsName = determineCurrentSrsName( defaultSrsName );

        List<Curve> memberCurves = new LinkedList<Curve>();

        while ( xmlStream.nextTag() == START_ELEMENT ) {
            // must be a 'gml:curveMember' element
            if ( !xmlStream.getLocalName().equals( "curveMember" ) ) {
                String msg = "Error in 'gml:Ring' element. Expected a 'gml:curveMembers' element.";
                throw new XMLParsingException( xmlStream, msg );
            }
            if ( xmlStream.nextTag() != START_ELEMENT ) {
                String msg = "Error in 'gml:Ring' element. Expected a 'gml:_Curve' element.";
                throw new XMLParsingException( xmlStream, msg );
            }
            memberCurves.add( parseAbstractCurve( srsName ) );
            xmlStream.nextTag();
            xmlStream.require( END_ELEMENT, GMLNS, "curveMember" );
        }
        xmlStream.require( END_ELEMENT, GMLNS, "Ring" );
        return geomFac.createRing( gid, lookupCRS( srsName ), memberCurves );
    }

    /**
     * Returns the object representation of a (&lt;gml:Polygon&gt;) element. Consumes all corresponding events from the
     * given <code>XMLStream</code>.
     * 
     * @param defaultSrsName
     *            default srs for the geometry, this is only used if the "gml:Polygon" has no <code>srsName</code>
     *            attribute
     * @return corresponding {@link Surface} object
     * @throws XMLStreamException
     * @throws XMLParsingException
     */
    public Surface parsePolygon( String defaultSrsName )
                            throws XMLStreamException {

        String gid = parseGeometryId();
        String srsName = determineCurrentSrsName( defaultSrsName );

        SurfacePatch exterior = null;
        List<SurfacePatch> interior = new LinkedList<SurfacePatch>();

        // 0 or 1 exterior element (yes, 0 is possible -- see section 9.2.2.5 of GML spec)
        if ( xmlStream.nextTag() == START_ELEMENT ) {
            if ( xmlStream.getLocalName().equals( "exterior" ) ) {
                xmlStream.nextTag();

                xmlStream.nextTag();
                xmlStream.require( END_ELEMENT, GMLNS, "exterior" );
            }
        }

        // arbitrary number of interior elements
        while ( xmlStream.getEventType() == START_ELEMENT ) {
            if ( !xmlStream.getLocalName().equals( "interior" ) ) {
                String msg = "Error in 'gml:Polygon' element. Expected a 'gml:interior' element.";
                throw new XMLParsingException( xmlStream, msg );
            }
            xmlStream.nextTag();
        }

        xmlStream.require( END_ELEMENT, GMLNS, "Polygon" );
        return null;
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
