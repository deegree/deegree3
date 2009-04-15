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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.types.Length;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.i18n.Messages;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryFactoryCreator;
import org.deegree.geometry.composite.CompositeCurve;
import org.deegree.geometry.composite.CompositeGeometry;
import org.deegree.geometry.composite.CompositeSolid;
import org.deegree.geometry.composite.CompositeSurface;
import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.multi.MultiSolid;
import org.deegree.geometry.multi.MultiSurface;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.OrientableCurve;
import org.deegree.geometry.primitive.OrientableSurface;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.PolyhedralSurface;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Solid;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.Tin;
import org.deegree.geometry.primitive.TriangulatedSurface;
import org.deegree.geometry.primitive.Curve.CurveType;
import org.deegree.geometry.primitive.Ring.RingType;
import org.deegree.geometry.primitive.Solid.SolidType;
import org.deegree.geometry.primitive.Surface.SurfaceType;
import org.deegree.geometry.primitive.curvesegments.CurveSegment;
import org.deegree.geometry.primitive.curvesegments.LineStringSegment;
import org.deegree.geometry.primitive.surfacepatches.PolygonPatch;
import org.deegree.geometry.primitive.surfacepatches.SurfacePatch;
import org.deegree.geometry.primitive.surfacepatches.Triangle;
import org.deegree.geometry.refs.PointReference;
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

    private final GML311SurfacePatchParser surfacePatchParser;

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

    // local names of "gml:GeometricComplex", "gml:CompositeCurve", "gml:CompositeSurface" and "gml:CompositeSolid"
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
        solidElements.add( "CompositeSolid" );
        solidElements.add( "Solid" );

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

    public GML311GeometryParser( GeometryFactory geomFac, GMLIdContext idContext ) {
        super( geomFac, idContext );
        curveSegmentParser = new GML311CurveSegmentParser( this, geomFac, idContext );
        surfacePatchParser = new GML311SurfacePatchParser( this, geomFac, idContext );
    }

    public GML311GeometryParser() {
        this (GeometryFactoryCreator.getInstance().getGeometryFactory(), new GMLIdContext());
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
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Geometry} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_Geometry" element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    public Geometry parseGeometry( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        Geometry geometry = null;

        if ( !GMLNS.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Invalid gml:_Geometry element: " + xmlStream.getName()
                         + "' is not a GML geometry element. Not in the gml namespace.";
            throw new XMLParsingException( xmlStream, msg );
        }

        String name = xmlStream.getLocalName();
        if ( primitiveElements.contains( name ) ) {
            geometry = parseGeometricPrimitive( xmlStream, defaultCRS );
        } else if ( ringElements.contains( name ) ) {
            geometry = parseAbstractRing( xmlStream, defaultCRS );
        } else if ( aggregateElements.contains( name ) ) {
            geometry = parseGeometricAggregate( xmlStream, defaultCRS );
        } else if ( complexElements.contains( name ) ) {
            geometry = parseAbstractGeometricComplex( xmlStream, defaultCRS );
        } else if ( implictGeometryElements.contains( name ) ) {
            geometry = parseImplicitGeometry( xmlStream, defaultCRS );
        } else {
            String msg = "Invalid GML geometry: '" + xmlStream.getName() + "' is not a GML geometry element.";
            throw new XMLParsingException( xmlStream, msg );
        }
        return geometry;
    }

    public boolean isGeometry( QName elName ) {
        if ( !GMLNS.equals( elName.getNamespaceURI() ) ) {
            return false;
        }
        String localName = elName.getLocalPart();
        return primitiveElements.contains( localName ) || ringElements.contains( localName )
               || aggregateElements.contains( localName ) || complexElements.contains( localName )
               || implictGeometryElements.contains( localName );
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
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Geometry} object
     * @throws XMLParsingException
     *             if the element is not a valid <code>gml:_GeometricPrimitive</code> element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    public GeometricPrimitive parseGeometricPrimitive( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        GeometricPrimitive primitive = null;

        if ( !GMLNS.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Invalid gml:_Geometry element: " + xmlStream.getName()
                         + "' is not a GML geometry element. Not in the gml namespace.";
            throw new XMLParsingException( xmlStream, msg );
        }

        String name = xmlStream.getLocalName();
        if ( name.equals( "Point" ) ) {
            primitive = parsePoint( xmlStream, defaultCRS );
        } else if ( curveElements.contains( name ) ) {
            primitive = parseAbstractCurve( xmlStream, defaultCRS );
        } else if ( ringElements.contains( name ) ) {
            primitive = parseAbstractRing( xmlStream, defaultCRS );
        } else if ( surfaceElements.contains( name ) ) {
            primitive = parseAbstractSurface( xmlStream, defaultCRS );
        } else if ( solidElements.contains( name ) ) {
            primitive = parseAbstractSolid( xmlStream, defaultCRS );
        } else {
            String msg = "Invalid GML geometry: '" + xmlStream.getName()
                         + "' is not a GML primitive geometry element (gml:_GeometricPrimitive).";
            throw new XMLParsingException( xmlStream, msg );
        }

        return primitive;
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
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Geometry} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_GeometricAggregate" element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    public Geometry parseGeometricAggregate( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        Geometry geometry = null;

        if ( !GMLNS.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Invalid gml:_GeometricAggregate element: " + xmlStream.getName()
                         + "' is not a GML geometry element. Not in the gml namespace.";
            throw new XMLParsingException( xmlStream, msg );
        }

        String name = xmlStream.getLocalName();
        if ( name.equals( "MultiCurve" ) ) {
            geometry = parseMultiCurve( xmlStream, defaultCRS );
        } else if ( name.equals( "MultiGeometry" ) ) {
            geometry = parseMultiGeometry( xmlStream, defaultCRS );
        } else if ( name.equals( "MultiLineString" ) ) {
            geometry = parseMultiLineString( xmlStream, defaultCRS );
        } else if ( name.equals( "MultiPoint" ) ) {
            geometry = parseMultiPoint( xmlStream, defaultCRS );
        } else if ( name.equals( "MultiPolygon" ) ) {
            geometry = parseMultiPolygon( xmlStream, defaultCRS );
        } else if ( name.equals( "MultiSolid" ) ) {
            geometry = parseMultiSolid( xmlStream, defaultCRS );
        } else if ( name.equals( "MultiSurface" ) ) {
            geometry = parseMultiSurface( xmlStream, defaultCRS );
        } else {
            String msg = "Invalid GML geometry: '" + xmlStream.getName()
                         + "' is not a GML aggregate geometry element (gml:_GeometricAggregate).";
            throw new XMLParsingException( xmlStream, msg );
        }
        return geometry;
    }

    /**
     * Returns the object representation for the given <code>gml:_Geometry</code> event that represents a geometric
     * complex (either <code>gml:CompositeCurve</code>, <code>gml:CompositeSolid</code>,
     * <code>gml:CompositeSurface</code> or <code>gml:GeometricComplex</code>), that the cursor of the given
     * <code>XMLStreamReader</code> points at.
     * <p>
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
     * @param defaultCRS
     *            default crs for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Geometry} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_Geometry" element with geometric complex semantic
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    public Geometry parseAbstractGeometricComplex( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        Geometry geometry = null;

        if ( !GMLNS.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Invalid gml:_GeometricComplex element: " + xmlStream.getName()
                         + "' is not a GML geometry element. Not in the gml namespace.";
            throw new XMLParsingException( xmlStream, msg );
        }

        String name = xmlStream.getLocalName();
        if ( name.equals( "CompositeCurve" ) ) {
            geometry = parseCompositeCurve( xmlStream, defaultCRS );
        } else if ( name.equals( "CompositeSolid" ) ) {
            geometry = parseCompositeSolid( xmlStream, defaultCRS );
        } else if ( name.equals( "CompositeSurface" ) ) {
            geometry = parseCompositeSurface( xmlStream, defaultCRS );
        } else if ( name.equals( "GeometricComplex" ) ) {
            geometry = parseGeometricComplex( xmlStream, defaultCRS );
        } else {
            String msg = "Invalid GML geometry: '" + xmlStream.getName()
                         + "' is not a (supported) GML geometry element.";
            throw new XMLParsingException( xmlStream, msg );
        }
        return geometry;
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
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Geometry} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_ImplicitGeometry" element
     * @throws XMLStreamException
     */
    public Geometry parseImplicitGeometry( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLParsingException, XMLStreamException {

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
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Geometry} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_Curve" element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    public Curve parseAbstractCurve( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        Curve curve = null;

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
            curve = parseCurve( xmlStream, defaultCRS );
            break;
        }
        case LineString: {
            curve = parseLineString( xmlStream, defaultCRS );
            break;
        }
        case CompositeCurve: {
            curve = parseCompositeCurve( xmlStream, defaultCRS );
            break;
        }
        case OrientableCurve: {
            curve = parseOrientableCurve( xmlStream, defaultCRS );
            break;
        }
        default:
            // cannot happen by construction
        }
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
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Ring} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_Ring" element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    public Ring parseAbstractRing( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        Ring ring = null;

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
            ring = parseLinearRing( xmlStream, defaultCRS );
            break;
        }
        case Ring: {
            ring = parseRing( xmlStream, defaultCRS );
            break;
        }
        default:
            // cannot happen by construction
        }
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
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Geometry} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_Ring" element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    public Surface parseAbstractSurface( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        Surface surface = null;

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
        case CompositeSurface: {
            surface = parseCompositeSurface( xmlStream, defaultCRS );
            break;
        }
        case OrientableSurface: {
            surface = parseOrientableSurface( xmlStream, defaultCRS );
            break;
        }
        case Polygon: {
            surface = parsePolygon( xmlStream, defaultCRS );
            break;
        }
        case PolyhedralSurface: {
            surface = parsePolyhedralSurface( xmlStream, defaultCRS );
            break;
        }
        case Surface: {
            surface = parseSurface( xmlStream, defaultCRS );
            break;
        }
        case TriangulatedSurface: {
            surface = parseTriangulatedSurface( xmlStream, defaultCRS );
            break;
        }
        case Tin: {
            surface = parseTin( xmlStream, defaultCRS );
            break;
        }
        default:
            // cannot happen by construction
        }
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
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Geometry} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_Ring" element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    public Solid parseAbstractSolid( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        Solid solid = null;

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
        case Solid: {
            solid = parseSolid( xmlStream, defaultCRS );
            break;
        }
        case CompositeSolid: {
            solid = parseCompositeSolid( xmlStream, defaultCRS );
            break;
        }
        default: {
            // cannot happen by construction
        }
        }
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
     * @param defaultCRS
     *            default CRS for the geometry, this is used if the "gml:Point" has no <code>srsName</code> attribute
     * @return corresponding {@link Point} object
     * @throws XMLParsingException
     *             if the element is not a valid <code>gml:Point</code> element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    public Point parsePoint( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        Point point = null;
        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );

        // must contain one of the following child elements: "gml:pos", "gml:coordinates" or "gml:coord"
        if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
            String name = xmlStream.getLocalName();
            if ( "pos".equals( name ) ) {
                double[] coords = parseDoubleList( xmlStream );
                point = geomFac.createPoint( gid, coords, crs );
            } else if ( "coordinates".equals( name ) ) {
                List<Point> points = parseCoordinates( xmlStream, crs );
                if ( points.size() != 1 ) {
                    String msg = "A gml:Point element must contain exactly one tuple of coordinates.";
                    throw new XMLParsingException( xmlStream, msg );
                }
                point = points.get( 0 );
            } else if ( "coord".equals( name ) ) {
                // deprecated since GML 3.0, only included for backward compatibility
                double[] coords = parseCoordType( xmlStream );
                point = geomFac.createPoint( gid, coords, crs );

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
        xmlStream.nextTag();
        xmlStream.require( END_ELEMENT, GMLNS, "Point" );
        return point;
    }

    /**
     * TODO handled xlinked content ("xlink:href")
     * 
     * @param defaultCRS
     * 
     * @return
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public Point parsePointProperty( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        Point point = null;
        String href = xmlStream.getAttributeValue( CommonNamespaces.XLNNS, "href" );
        if ( href != null && href.length() > 0 ) {
            LOG.info( "Found geometry reference: '" + href + "'" );
            point = new PointReference( href );
            // TODO check if href + Point element are present
        } else if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
            // must be a 'gml:Point' element
            if ( !xmlStream.getLocalName().equals( "Point" ) ) {
                String msg = "Error in 'gml:pointProperty' element. Expected a 'gml:Point' element.";
                throw new XMLParsingException( xmlStream, msg );
            }
            point = parsePoint( xmlStream, defaultCRS );
        } else {
            String msg = "Error in 'gml:pointProperty' element. Expected a 'gml:Point' element or an 'xlink:href' attribute.";
            throw new XMLParsingException( xmlStream, msg );
        }
        xmlStream.nextTag();
        return point;
    }

    /**
     * Returns the object representation of a <code>gml:LineString</code> element. Consumes all corresponding events
     * from the given <code>XMLStream</code>.
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:LineString</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link Curve} object
     * @throws XMLParsingException
     *             if the element is not a valid <code>gml:Curve</code> element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public LineString parseLineString( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );

        List<Point> points = null;
        if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
            String name = xmlStream.getLocalName();
            if ( "posList".equals( name ) ) {
                points = parsePosList( xmlStream, crs );
                xmlStream.nextTag();
            } else if ( "coordinates".equals( name ) ) {
                points = parseCoordinates( xmlStream, crs );
                xmlStream.nextTag();
            } else {
                points = new LinkedList<Point>();
                do {
                    if ( "pos".equals( name ) ) {
                        double[] coords = parseDoubleList( xmlStream );
                        points.add( geomFac.createPoint( gid, coords, crs ) );
                    } else if ( "pointProperty".equals( name ) || "pointRep".equals( name ) ) {
                        points.add( parsePointProperty( xmlStream, crs ) );
                    } else if ( "coord".equals( name ) ) {
                        // deprecated since GML 3.0, only included for backward compatibility
                        double[] coords = parseCoordType( xmlStream );
                        points.add( geomFac.createPoint( gid, coords, crs ) );
                    } else {
                        String msg = "Error in 'gml:LineString' element.";
                        throw new XMLParsingException( xmlStream, msg );
                    }
                } while ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT );
            }
        }

        if ( points.size() < 2 ) {
            String msg = "Error in 'gml:LineString' element. Must consist of two points at least.";
            throw new XMLParsingException( xmlStream, msg );
        }
        return geomFac.createLineString( gid, crs, points );
    }

    /**
     * Returns the object representation of a <code>gml:Curve</code> element. Consumes all corresponding events from the
     * associated <code>XMLStream</code>.
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:Curve</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link Curve} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     * @throws XMLParsingException
     */
    public Curve parseCurve( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );

        xmlStream.nextTag();
        xmlStream.require( XMLStreamConstants.START_ELEMENT, GMLNS, "segments" );
        List<CurveSegment> segments = new LinkedList<CurveSegment>();

        while ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
            segments.add( curveSegmentParser.parseCurveSegment( xmlStream, crs ) );
        }

        xmlStream.nextTag();
        xmlStream.require( END_ELEMENT, GMLNS, "Curve" );
        return geomFac.createCurve( gid, segments.toArray( new CurveSegment[segments.size()] ), crs );
    }

    /**
     * Returns the object representation of a <code>gml:OrientableCurve</code> element. Consumes all corresponding
     * events from the associated <code>XMLStream</code>.
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:OrientableCurve</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link OrientableCurve} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public OrientableCurve parseOrientableCurve( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, UnknownCRSException {

        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );
        boolean isReversed = !parseOrientation( xmlStream );

        xmlStream.nextTag();
        xmlStream.require( XMLStreamConstants.START_ELEMENT, GMLNS, "baseCurve" );
        xmlStream.nextTag();
        Curve baseCurve = parseAbstractCurve( xmlStream, crs );
        xmlStream.nextTag();
        xmlStream.require( XMLStreamConstants.END_ELEMENT, GMLNS, "baseCurve" );
        xmlStream.nextTag();
        xmlStream.require( END_ELEMENT, GMLNS, "OrientableCurve" );

        return geomFac.createOrientableCurve( gid, crs, baseCurve, isReversed );
    }

    /**
     * Returns the object representation of a <code>gml:LinearRing</code> element. Consumes all corresponding events
     * from the associated <code>XMLStream</code>.
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:LinearRing</code> element has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link Ring} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     * @throws XMLParsingException
     */
    public LinearRing parseLinearRing( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );

        List<Point> points = curveSegmentParser.parseControlPoints( xmlStream, crs );
        if ( points.size() < 4 ) {
            String msg = "Error in 'gml:LinearRing' element. Must specify at least four points.";
            throw new XMLParsingException( xmlStream, msg );
        }
        xmlStream.require( END_ELEMENT, GMLNS, "LinearRing" );
        return geomFac.createLinearRing( gid, crs, points );
    }

    /**
     * Returns the object representation of a <code>gml:Ring</code> element. Consumes all corresponding events from the
     * given <code>XMLStream</code>.
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:Ring</code> element has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link Ring} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public Ring parseRing( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, UnknownCRSException {

        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );

        List<Curve> memberCurves = new LinkedList<Curve>();

        while ( xmlStream.nextTag() == START_ELEMENT ) {
            // must be a 'gml:curveMember' element
            if ( !xmlStream.getLocalName().equals( "curveMember" ) ) {
                String msg = "Error in 'gml:Ring' element. Expected a 'gml:curveMember' element.";
                throw new XMLParsingException( xmlStream, msg );
            }
            if ( xmlStream.nextTag() != START_ELEMENT ) {
                String msg = "Error in 'gml:Ring' element. Expected a 'gml:_Curve' element.";
                throw new XMLParsingException( xmlStream, msg );
            }
            memberCurves.add( parseAbstractCurve( xmlStream, crs ) );
            xmlStream.nextTag();
            xmlStream.require( END_ELEMENT, GMLNS, "curveMember" );
        }
        xmlStream.require( END_ELEMENT, GMLNS, "Ring" );
        return geomFac.createRing( gid, crs, memberCurves );
    }

    /**
     * Returns the object representation of a (&lt;gml:Polygon&gt;) element. Consumes all corresponding events from the
     * given <code>XMLStream</code>.
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the "gml:Polygon" has no <code>srsName</code>
     *            attribute
     * @return corresponding {@link Polygon} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     * @throws XMLParsingException
     */
    public Polygon parsePolygon( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );

        Ring exteriorRing = null;
        List<Ring> interiorRings = new LinkedList<Ring>();

        // 0 or 1 exterior/outerBoundaryIs element (yes, 0 is possible -- see section 9.2.2.5 of GML spec)
        if ( xmlStream.nextTag() == START_ELEMENT ) {
            if ( xmlStream.getLocalName().equals( "exterior" ) ) {
                if ( xmlStream.nextTag() != START_ELEMENT ) {
                    String msg = "Error in 'gml:Polygon' element. Expected a 'gml:_Ring' element.";
                    throw new XMLParsingException( xmlStream, msg );
                }
                exteriorRing = parseAbstractRing( xmlStream, crs );
                xmlStream.nextTag();
                xmlStream.require( END_ELEMENT, GMLNS, "exterior" );
                xmlStream.nextTag();
            } else if ( xmlStream.getLocalName().equals( "outerBoundaryIs" ) ) {
                if ( xmlStream.nextTag() != START_ELEMENT ) {
                    String msg = "Error in 'gml:Polygon' element. Expected a 'gml:LinearRing' element.";
                    throw new XMLParsingException( xmlStream, msg );
                }
                exteriorRing = parseLinearRing( xmlStream, crs );
                xmlStream.nextTag();
                xmlStream.require( END_ELEMENT, GMLNS, "outerBoundaryIs" );
                xmlStream.nextTag();
            }
        }

        // arbitrary number of interior/innerBoundaryIs elements
        while ( xmlStream.getEventType() == START_ELEMENT ) {
            if ( xmlStream.getLocalName().equals( "interior" ) ) {
                if ( xmlStream.nextTag() != START_ELEMENT ) {
                    String msg = "Error in 'gml:Polygon' element. Expected a 'gml:_Ring' element.";
                    throw new XMLParsingException( xmlStream, msg );
                }
                interiorRings.add( parseAbstractRing( xmlStream, crs ) );
                xmlStream.nextTag();
                xmlStream.require( END_ELEMENT, GMLNS, "interior" );
            } else if ( xmlStream.getLocalName().equals( "innerBoundaryIs" ) ) {
                if ( xmlStream.nextTag() != START_ELEMENT ) {
                    String msg = "Error in 'gml:Polygon' element. Expected a 'gml:LinearRing' element.";
                    throw new XMLParsingException( xmlStream, msg );
                }
                interiorRings.add( parseLinearRing( xmlStream, crs ) );
                xmlStream.nextTag();
                xmlStream.require( END_ELEMENT, GMLNS, "innerBoundaryIs" );
            } else {
                String msg = "Error in 'gml:Polygon' element. Expected a 'gml:interior' or a 'gml:innerBoundaryIs' element, but found: '"
                             + xmlStream.getName() + "'.";
                throw new XMLParsingException( xmlStream, msg );
            }
            xmlStream.nextTag();
        }
        xmlStream.require( END_ELEMENT, GMLNS, "Polygon" );
        return geomFac.createPolygon( gid, crs, exteriorRing, interiorRings );
    }

    /**
     * Returns the object representation of a (&lt;gml:Surface&gt;) element. Consumes all corresponding events from the
     * given <code>XMLStream</code>.
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the "gml:Surface" has no <code>srsName</code>
     *            attribute
     * @return corresponding {@link Surface} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public Surface parseSurface( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, UnknownCRSException {

        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );

        List<SurfacePatch> memberPatches = new LinkedList<SurfacePatch>();
        xmlStream.nextTag();
        xmlStream.require( START_ELEMENT, GMLNS, "patches" );
        while ( xmlStream.nextTag() == START_ELEMENT ) {
            memberPatches.add( surfacePatchParser.parseSurfacePatch( xmlStream, crs ) );
        }
        xmlStream.require( END_ELEMENT, GMLNS, "patches" );
        xmlStream.nextTag();
        xmlStream.require( END_ELEMENT, GMLNS, "Surface" );
        return geomFac.createSurface( gid, memberPatches, crs );
    }

    /**
     * Returns the object representation of a (&lt;gml:PolyhedralSurface&gt;) element. Consumes all corresponding events
     * from the given <code>XMLStream</code>.
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the "gml:PolyhedralSurface" has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link PolyhedralSurface} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public PolyhedralSurface parsePolyhedralSurface( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, UnknownCRSException {

        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );

        List<PolygonPatch> memberPatches = new LinkedList<PolygonPatch>();
        xmlStream.nextTag();
        xmlStream.require( START_ELEMENT, GMLNS, "polygonPatches" );
        while ( xmlStream.nextTag() == START_ELEMENT ) {
            memberPatches.add( surfacePatchParser.parsePolygonPatch( xmlStream, crs ) );
        }
        xmlStream.require( END_ELEMENT, GMLNS, "polygonPatches" );
        xmlStream.nextTag();
        xmlStream.require( END_ELEMENT, GMLNS, "PolyhedralSurface" );
        return geomFac.createPolyhedralSurface( gid, crs, memberPatches );
    }

    /**
     * Returns the object representation of a (&lt;TriangulatedSurface&gt;) element. Consumes all corresponding events
     * from the given <code>XMLStream</code>.
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the "gml:TriangulatedSurface" has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link TriangulatedSurface} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public TriangulatedSurface parseTriangulatedSurface( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, UnknownCRSException {

        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );

        List<Triangle> memberPatches = new LinkedList<Triangle>();
        xmlStream.nextTag();
        xmlStream.require( START_ELEMENT, GMLNS, "trianglePatches" );
        while ( xmlStream.nextTag() == START_ELEMENT ) {
            memberPatches.add( surfacePatchParser.parseTriangle( xmlStream, crs ) );
        }
        xmlStream.require( END_ELEMENT, GMLNS, "trianglePatches" );
        xmlStream.nextTag();
        xmlStream.require( END_ELEMENT, GMLNS, "TriangulatedSurface" );
        return geomFac.createTriangulatedSurface( gid, crs, memberPatches );
    }

    /**
     * Returns the object representation of a (&lt;Tin&gt;) element. Consumes all corresponding events from the given
     * <code>XMLStream</code>.
     * <p>
     * Note: GML 3.1.1 specifies both "gml:trianglePatches" and "gml:controlPoint" properties for "gml:Tin". This is
     * apparently redundant, and consequently (?) GML 3.2.1 only allows the controlPoint property here. This method
     * copes with this by only using the controlPoints for building the {@link Tin} object.
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the "gml:Tin" has no <code>srsName</code> attribute
     * @return corresponding {@link Tin} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     * @throws XMLParsingException
     */
    public Tin parseTin( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );

        xmlStream.nextTag();
        xmlStream.require( START_ELEMENT, GMLNS, "trianglePatches" );
        while ( xmlStream.nextTag() == START_ELEMENT ) {
            // validate syntactically, but ignore the content for instantiating the geometry
            surfacePatchParser.parseTriangle( xmlStream, crs );
        }
        xmlStream.require( END_ELEMENT, GMLNS, "trianglePatches" );

        List<List<LineStringSegment>> stopLines = new LinkedList<List<LineStringSegment>>();
        if ( xmlStream.nextTag() == START_ELEMENT ) {
            while ( xmlStream.getLocalName().equals( "stopLines" ) ) {
                List<LineStringSegment> segments = new LinkedList<LineStringSegment>();
                while ( xmlStream.nextTag() == START_ELEMENT ) {
                    xmlStream.require( START_ELEMENT, GMLNS, "LineStringSegment" );
                    segments.add( curveSegmentParser.parseLineStringSegment( xmlStream, crs ) );
                    xmlStream.require( END_ELEMENT, GMLNS, "LineStringSegment" );
                }
                xmlStream.require( END_ELEMENT, GMLNS, "stopLines" );
                stopLines.add( segments );
                xmlStream.nextTag();
            }
        }

        List<List<LineStringSegment>> breakLines = new LinkedList<List<LineStringSegment>>();
        if ( xmlStream.getEventType() == START_ELEMENT ) {
            while ( xmlStream.getLocalName().equals( "breakLines" ) ) {
                List<LineStringSegment> segments = new LinkedList<LineStringSegment>();
                while ( xmlStream.nextTag() == START_ELEMENT ) {
                    xmlStream.require( START_ELEMENT, GMLNS, "LineStringSegment" );
                    segments.add( curveSegmentParser.parseLineStringSegment( xmlStream, crs ) );
                    xmlStream.require( END_ELEMENT, GMLNS, "LineStringSegment" );
                }
                xmlStream.require( END_ELEMENT, GMLNS, "breakLines" );
                breakLines.add( segments );
                xmlStream.nextTag();
            }
        }

        xmlStream.require( START_ELEMENT, GMLNS, "maxLength" );
        Length maxLength = parseLengthType( xmlStream );
        xmlStream.nextTag();

        List<Point> controlPoints = null;
        xmlStream.require( START_ELEMENT, GMLNS, "controlPoint" );
        if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
            String name = xmlStream.getLocalName();
            if ( "posList".equals( name ) ) {
                controlPoints = parsePosList( xmlStream, crs );
                xmlStream.nextTag();
            } else {
                controlPoints = new LinkedList<Point>();
                do {
                    if ( "pos".equals( name ) ) {
                        double[] coords = parseDoubleList( xmlStream );
                        controlPoints.add( geomFac.createPoint( gid, coords, crs ) );
                    } else if ( "pointProperty".equals( name ) ) {
                        controlPoints.add( parsePointProperty( xmlStream, crs ) );
                    } else {
                        String msg = "Error in 'gml:Tin' element.";
                        throw new XMLParsingException( xmlStream, msg );
                    }
                } while ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT );
            }
        }
        xmlStream.nextTag();

        if ( controlPoints.size() < 3 ) {
            String msg = "Error in 'gml:Tin' element. Must specify three control points (=one triangle) at least.";
            throw new XMLParsingException( xmlStream, msg );
        }

        xmlStream.require( END_ELEMENT, GMLNS, "Tin" );
        return geomFac.createTin( gid, crs, stopLines, breakLines, maxLength, controlPoints );
    }

    /**
     * Returns the object representation of a <code>gml:OrientableSurface</code> element. Consumes all corresponding
     * events from the associated <code>XMLStream</code>.
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:OrientableSurface</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link OrientableSurface} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     * @throws XMLParsingException
     */
    public OrientableSurface parseOrientableSurface( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );
        boolean isReversed = !parseOrientation( xmlStream );

        xmlStream.nextTag();
        xmlStream.require( XMLStreamConstants.START_ELEMENT, GMLNS, "baseSurface" );
        xmlStream.nextTag();
        Surface baseSurface = parseAbstractSurface( xmlStream, crs );
        xmlStream.nextTag();
        xmlStream.require( XMLStreamConstants.END_ELEMENT, GMLNS, "baseSurface" );
        xmlStream.nextTag();
        xmlStream.require( END_ELEMENT, GMLNS, "OrientableSurface" );

        return geomFac.createOrientableSurface( gid, crs, baseSurface, isReversed );
    }

    /**
     * Returns the object representation of a (&lt;gml:Solid&gt;) element. Consumes all corresponding events from the
     * given <code>XMLStream</code>.
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the "gml:Solid" has no <code>srsName</code>
     *            attribute
     * @return corresponding {@link Solid} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     * @throws XMLParsingException
     */
    public Solid parseSolid( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );

        Surface exteriorSurface = null;
        List<Surface> interiorSurfaces = new LinkedList<Surface>();

        // 0 or 1 exterior element (yes, 0 is possible -- see section 9.2.2.5 of GML spec)
        if ( xmlStream.nextTag() == START_ELEMENT ) {
            if ( xmlStream.getLocalName().equals( "exterior" ) ) {
                if ( xmlStream.nextTag() != START_ELEMENT ) {
                    String msg = "Error in 'gml:Solid' element. Expected a 'gml:_Surface' element.";
                    throw new XMLParsingException( xmlStream, msg );
                }
                exteriorSurface = parseAbstractSurface( xmlStream, crs );
                xmlStream.nextTag();
                xmlStream.require( END_ELEMENT, GMLNS, "exterior" );
            }
            xmlStream.nextTag();
        }

        // arbitrary number of interior elements
        while ( xmlStream.getEventType() == START_ELEMENT ) {
            if ( xmlStream.getLocalName().equals( "interior" ) ) {
                if ( xmlStream.nextTag() != START_ELEMENT ) {
                    String msg = "Error in 'gml:Solid' element. Expected a 'gml:_Surface' element.";
                    throw new XMLParsingException( xmlStream, msg );
                }
                interiorSurfaces.add( parseAbstractSurface( xmlStream, crs ) );
                xmlStream.nextTag();
                xmlStream.require( END_ELEMENT, GMLNS, "interior" );
            } else {
                String msg = "Error in 'gml:Solid' element. Expected a 'gml:interior' element.";
                throw new XMLParsingException( xmlStream, msg );
            }
            xmlStream.nextTag();
        }
        xmlStream.require( END_ELEMENT, GMLNS, "Solid" );
        return geomFac.createSolid( gid, crs, exteriorSurface, interiorSurfaces );
    }

    /**
     * Returns the object representation of a <code>gml:CompositeCurve</code> element. Consumes all corresponding events
     * from the associated <code>XMLStream</code>.
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:CompositeCurve</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link CompositeCurve} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public CompositeCurve parseCompositeCurve( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, UnknownCRSException {

        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );

        List<Curve> memberCurves = new LinkedList<Curve>();

        while ( xmlStream.nextTag() == START_ELEMENT ) {
            // must be a 'gml:curveMember' element
            if ( !xmlStream.getLocalName().equals( "curveMember" ) ) {
                String msg = "Error in 'gml:CompositeCurve' element. Expected a 'gml:curveMember' element.";
                throw new XMLParsingException( xmlStream, msg );
            }
            if ( xmlStream.nextTag() != START_ELEMENT ) {
                String msg = "Error in 'gml:CompositeCurve' element. Expected a 'gml:_Curve' element.";
                throw new XMLParsingException( xmlStream, msg );
            }
            memberCurves.add( parseAbstractCurve( xmlStream, crs ) );
            xmlStream.nextTag();
            xmlStream.require( END_ELEMENT, GMLNS, "curveMember" );
        }
        xmlStream.require( END_ELEMENT, GMLNS, "CompositeCurve" );
        return geomFac.createCompositeCurve( gid, crs, memberCurves );
    }

    /**
     * Returns the object representation of a <code>gml:CompositeSurface</code> element. Consumes all corresponding
     * events from the associated <code>XMLStream</code>.
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:CompositeSurface</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link CompositeSurface} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     * @throws XMLParsingException
     */
    public CompositeSurface parseCompositeSurface( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );

        List<Surface> memberSurfaces = new LinkedList<Surface>();

        while ( xmlStream.nextTag() == START_ELEMENT ) {
            // must be a 'gml:surfaceMember' element
            if ( !xmlStream.getLocalName().equals( "surfaceMember" ) ) {
                String msg = "Error in 'gml:CompositeSurface' element. Expected a 'gml:surfaceMember' element.";
                throw new XMLParsingException( xmlStream, msg );
            }
            if ( xmlStream.nextTag() != START_ELEMENT ) {
                String msg = "Error in 'gml:CompositeSurface' element. Expected a 'gml:_Surface' element.";
                throw new XMLParsingException( xmlStream, msg );
            }
            memberSurfaces.add( parseAbstractSurface( xmlStream, crs ) );
            xmlStream.nextTag();
            xmlStream.require( END_ELEMENT, GMLNS, "surfaceMember" );
        }
        xmlStream.require( END_ELEMENT, GMLNS, "CompositeSurface" );
        return geomFac.createCompositeSurface( gid, crs, memberSurfaces );
    }

    /**
     * Returns the object representation of a <code>gml:CompositeSolid</code> element. Consumes all corresponding events
     * from the associated <code>XMLStream</code>.
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:CompositeSolid</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link CompositeSolid} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     * @throws XMLParsingException
     */
    public CompositeSolid parseCompositeSolid( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );

        List<Solid> memberSolids = new LinkedList<Solid>();

        while ( xmlStream.nextTag() == START_ELEMENT ) {
            // must be a 'gml:solidMember' element
            if ( !xmlStream.getLocalName().equals( "solidMember" ) ) {
                String msg = "Error in 'gml:CompositeSolid' element. Expected a 'gml:solidMember' element.";
                throw new XMLParsingException( xmlStream, msg );
            }
            if ( xmlStream.nextTag() != START_ELEMENT ) {
                String msg = "Error in 'gml:CompositeSolid' element. Expected a 'gml:_Solid' element.";
                throw new XMLParsingException( xmlStream, msg );
            }
            memberSolids.add( parseAbstractSolid( xmlStream, crs ) );
            xmlStream.nextTag();
            xmlStream.require( END_ELEMENT, GMLNS, "solidMember" );
        }
        xmlStream.require( END_ELEMENT, GMLNS, "CompositeSolid" );
        return geomFac.createCompositeSolid( gid, crs, memberSolids );
    }

    /**
     * Returns the object representation of a <code>gml:CompositeGeometry</code> element. Consumes all corresponding
     * events from the associated <code>XMLStream</code>.
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:CompositeGeometry</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link CompositeGeometry} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public CompositeGeometry<GeometricPrimitive> parseGeometricComplex( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );

        List<GeometricPrimitive> memberSolids = new LinkedList<GeometricPrimitive>();

        while ( xmlStream.nextTag() == START_ELEMENT ) {
            // must be a 'gml:element' element
            if ( !xmlStream.getLocalName().equals( "element" ) ) {
                String msg = "Error in 'gml:GeometricComplex' element. Expected a 'gml:element' element.";
                throw new XMLParsingException( xmlStream, msg );
            }
            if ( xmlStream.nextTag() != START_ELEMENT ) {
                String msg = "Error in 'gml:GeometricComplex' element. Expected a 'gml:element' element.";
                throw new XMLParsingException( xmlStream, msg );
            }
            memberSolids.add( parseGeometricPrimitive( xmlStream, crs ) );
            xmlStream.nextTag();
            xmlStream.require( END_ELEMENT, GMLNS, "element" );
        }
        xmlStream.require( END_ELEMENT, GMLNS, "GeometricComplex" );
        return geomFac.createCompositeGeometry( gid, crs, memberSolids );
    }

    /**
     * Returns the object representation of a <code>gml:MultiPoint</code> element. Consumes all corresponding events
     * from the associated <code>XMLStream</code>.
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:MultiPoint</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link MultiPoint} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     * @throws XMLParsingException
     */
    public MultiPoint parseMultiPoint( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );

        List<Point> members = new LinkedList<Point>();

        while ( xmlStream.nextTag() == START_ELEMENT ) {
            String localName = xmlStream.getLocalName();
            if ( localName.equals( "pointMember" ) ) {
                xmlStream.nextTag();
                xmlStream.require( START_ELEMENT, GMLNS, "Point" );
                members.add( parsePoint( xmlStream, crs ) );
                xmlStream.require( END_ELEMENT, GMLNS, "Point" );
                xmlStream.nextTag();
            } else if ( localName.equals( "pointMembers" ) ) {
                while ( xmlStream.nextTag() == START_ELEMENT ) {
                    xmlStream.require( START_ELEMENT, GMLNS, "Point" );
                    members.add( parsePoint( xmlStream, crs ) );
                }
                // pointMembers may only occur once (and behind all pointMember) elements
                xmlStream.nextTag();
                break;
            } else {
                String msg = "Invalid 'gml:MultiPoint' element: unexpected element '" + localName
                             + "'. Expected 'pointMember' or 'pointMembers'.";
                throw new XMLParsingException( xmlStream, msg );
            }
        }
        xmlStream.require( END_ELEMENT, GMLNS, "MultiPoint" );
        return geomFac.createMultiPoint( gid, crs, members );
    }

    /**
     * Returns the object representation of a <code>gml:MultiCurve</code> element. Consumes all corresponding events
     * from the associated <code>XMLStream</code>.
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:MultiCurve</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link MultiCurve} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     * @throws XMLParsingException
     */
    public MultiCurve parseMultiCurve( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );

        List<Curve> members = new LinkedList<Curve>();

        while ( xmlStream.nextTag() == START_ELEMENT ) {
            String localName = xmlStream.getLocalName();
            if ( localName.equals( "curveMember" ) ) {
                if ( xmlStream.nextTag() != START_ELEMENT ) {
                    String msg = "Invalid 'gml:MultiCurve' element: expected a 'gml:_Curve' element.";
                    throw new XMLParsingException( xmlStream, msg );
                }
                members.add( parseAbstractCurve( xmlStream, crs ) );
                xmlStream.nextTag();
            } else if ( localName.equals( "curveMembers" ) ) {
                while ( xmlStream.nextTag() == START_ELEMENT ) {
                    members.add( parseAbstractCurve( xmlStream, crs ) );
                }
                // curveMembers may only occur once (and behind all curveMember) elements
                xmlStream.nextTag();
                break;
            } else {
                String msg = "Invalid 'gml:MultiCurve' element: unexpected element '" + localName
                             + "'. Expected 'curveMember' or 'curveMembers'.";
                throw new XMLParsingException( xmlStream, msg );
            }
        }
        xmlStream.require( END_ELEMENT, GMLNS, "MultiCurve" );
        return geomFac.createMultiCurve( gid, crs, members );
    }

    /**
     * Returns the object representation of a <code>gml:MultiLineString</code> element. Consumes all corresponding
     * events from the associated <code>XMLStream</code>.
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:MultiLineString</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link MultiLineString} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public MultiLineString parseMultiLineString( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );

        List<LineString> members = new LinkedList<LineString>();

        while ( xmlStream.nextTag() == START_ELEMENT ) {
            String localName = xmlStream.getLocalName();
            if ( localName.equals( "lineStringMember" ) ) {
                xmlStream.nextTag();
                xmlStream.require( START_ELEMENT, GMLNS, "LineString" );
                members.add( parseLineString( xmlStream, crs ) );
                xmlStream.nextTag();
            } else {
                String msg = "Invalid 'gml:MultiLineString' element: unexpected element '" + localName
                             + "'. Expected 'lineStringMember'.";
                throw new XMLParsingException( xmlStream, msg );
            }
        }
        xmlStream.require( END_ELEMENT, GMLNS, "MultiLineString" );
        return geomFac.createMultiLineString( gid, crs, members );
    }

    /**
     * Returns the object representation of a <code>gml:MultiSurface</code> element. Consumes all corresponding events
     * from the associated <code>XMLStream</code>.
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:MultiSurface</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link MultiSurface} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public MultiSurface parseMultiSurface( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );

        List<Surface> members = new LinkedList<Surface>();

        while ( xmlStream.nextTag() == START_ELEMENT ) {
            String localName = xmlStream.getLocalName();
            if ( localName.equals( "surfaceMember" ) ) {
                if ( xmlStream.nextTag() != START_ELEMENT ) {
                    String msg = "Invalid 'gml:MultiSurface' element: expected a 'gml:_Surface' element.";
                    throw new XMLParsingException( xmlStream, msg );
                }
                members.add( parseAbstractSurface( xmlStream, crs ) );
                xmlStream.nextTag();
            } else if ( localName.equals( "surfaceMembers" ) ) {
                while ( xmlStream.nextTag() == START_ELEMENT ) {
                    members.add( parseAbstractSurface( xmlStream, crs ) );
                }
                // surfaceMembers may only occur once (and behind all surfaceMember) elements
                xmlStream.nextTag();
                break;
            } else {
                String msg = "Invalid 'gml:MultiSurface' element: unexpected element '" + localName
                             + "'. Expected 'surfaceMember' or 'surfaceMembers'.";
                throw new XMLParsingException( xmlStream, msg );
            }
        }
        xmlStream.require( END_ELEMENT, GMLNS, "MultiSurface" );
        return geomFac.createMultiSurface( gid, crs, members );
    }

    /**
     * Returns the object representation of a <code>gml:MultiPolygon</code> element. Consumes all corresponding events
     * from the associated <code>XMLStream</code>.
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:MultiPolygon</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link MultiPolygon} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public MultiPolygon parseMultiPolygon( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );

        List<Polygon> members = new LinkedList<Polygon>();

        while ( xmlStream.nextTag() == START_ELEMENT ) {
            String localName = xmlStream.getLocalName();
            if ( localName.equals( "polygonMember" ) ) {
                xmlStream.nextTag();
                xmlStream.require( START_ELEMENT, GMLNS, "Polygon" );
                members.add( parsePolygon( xmlStream, crs ) );
                xmlStream.nextTag();
            } else {
                String msg = "Invalid 'gml:MultiPolygon' element: unexpected element '" + localName
                             + "'. Expected 'polygonMember'.";
                throw new XMLParsingException( xmlStream, msg );
            }
        }
        xmlStream.require( END_ELEMENT, GMLNS, "MultiPolygon" );
        return geomFac.createMultiPolygon( gid, crs, members );
    }

    /**
     * Returns the object representation of a <code>gml:MultiSolid</code> element. Consumes all corresponding events
     * from the associated <code>XMLStream</code>.
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:MultiSolid</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link MultiSolid} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public MultiSolid parseMultiSolid( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );

        List<Solid> members = new LinkedList<Solid>();

        while ( xmlStream.nextTag() == START_ELEMENT ) {
            String localName = xmlStream.getLocalName();
            if ( localName.equals( "solidMember" ) ) {
                if ( xmlStream.nextTag() != START_ELEMENT ) {
                    String msg = "Invalid 'gml:MultiSolid' element: expected a 'gml:_Solid' element.";
                    throw new XMLParsingException( xmlStream, msg );
                }
                members.add( parseAbstractSolid( xmlStream, crs ) );
                xmlStream.nextTag();
            } else if ( localName.equals( "solidMembers" ) ) {
                while ( xmlStream.nextTag() == START_ELEMENT ) {
                    members.add( parseAbstractSolid( xmlStream, crs ) );
                }
                // solidMembers may only occur once (and behind all surfaceMember) elements
                xmlStream.nextTag();
                break;
            } else {
                String msg = "Invalid 'gml:MultiSolid' element: unexpected element '" + localName
                             + "'. Expected 'solidMember' or 'solidMembers'.";
                throw new XMLParsingException( xmlStream, msg );
            }
        }
        xmlStream.require( END_ELEMENT, GMLNS, "MultiSolid" );
        return geomFac.createMultiSolid( gid, crs, members );
    }

    /**
     * Returns the object representation of a <code>gml:MultiGeometry</code> element. Consumes all corresponding events
     * from the associated <code>XMLStream</code>.
     * 
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:MultiGeometry</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link MultiGeometry} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public MultiGeometry<Geometry> parseMultiGeometry( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );

        List<Geometry> members = new LinkedList<Geometry>();

        while ( xmlStream.nextTag() == START_ELEMENT ) {
            String localName = xmlStream.getLocalName();
            if ( localName.equals( "geometryMember" ) ) {
                if ( xmlStream.nextTag() != START_ELEMENT ) {
                    String msg = "Invalid 'gml:MultiGeometry' element: expected a 'gml:_Geometry' element.";
                    throw new XMLParsingException( xmlStream, msg );
                }
                members.add( parseGeometry( xmlStream, crs ) );
                xmlStream.nextTag();
            } else if ( localName.equals( "geometryMembers" ) ) {
                while ( xmlStream.nextTag() == START_ELEMENT ) {
                    members.add( parseGeometry( xmlStream, crs ) );
                }
                // geometryMembers may only occur once (and behind all surfaceMember) elements
                xmlStream.nextTag();
                break;
            } else {
                String msg = "Invalid 'gml:MultiGeometry' element: unexpected element '" + localName
                             + "'. Expected 'geometryMember' or 'geometryMembers'.";
                throw new XMLParsingException( xmlStream, msg );
            }
        }
        xmlStream.require( END_ELEMENT, GMLNS, "MultiGeometry" );
        return geomFac.createMultiGeometry( gid, crs, members );
    }

    /**
     * Returns the object representation of a <code>gml:Envelope</code> element. Consumes all corresponding events from
     * the associated <code>XMLStream</code>.
     * 
     * @param defaultCRS
     *            default CRS for the envelope, this is only used if the <code>gml:Envelope</code> has no
     *            <code>srsName</code> attribute itself
     * @return corresponding {@link Envelope} object
     * @throws XMLStreamException
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    public Envelope parseEnvelope( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        CRS crs = determineActiveCRS( xmlStream, defaultCRS );

        double[] lowerCorner = null;
        double[] upperCorner = null;

        // must contain exactly one of the following child elements: "gml:lowerCorner", "gml:coord", "gml:pos" or
        // "gml:coordinates"
        if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
            String name = xmlStream.getLocalName();
            if ( "lowerCorner".equals( name ) ) {
                lowerCorner = parseDoubleList( xmlStream );
                xmlStream.require( END_ELEMENT, GMLNS, "lowerCorner" );
                xmlStream.nextTag();
                xmlStream.require( START_ELEMENT, GMLNS, "upperCorner" );
                upperCorner = parseDoubleList( xmlStream );
                xmlStream.require( END_ELEMENT, GMLNS, "upperCorner" );
            } else if ( "coord".equals( name ) ) {
                lowerCorner = parseCoordType( xmlStream );
                xmlStream.require( END_ELEMENT, GMLNS, "coord" );
                xmlStream.nextTag();
                xmlStream.require( START_ELEMENT, GMLNS, "coord" );
                upperCorner = parseCoordType( xmlStream );
                xmlStream.require( END_ELEMENT, GMLNS, "coord" );
            } else if ( "pos".equals( name ) ) {
                lowerCorner = parseDoubleList( xmlStream );
                xmlStream.require( END_ELEMENT, GMLNS, "pos" );
                xmlStream.nextTag();
                xmlStream.require( START_ELEMENT, GMLNS, "pos" );
                upperCorner = parseDoubleList( xmlStream );
                xmlStream.require( END_ELEMENT, GMLNS, "pos" );
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

    /**
     * Parses the geometry id attribute from the geometry <code>START_ELEMENT</code> event that the given
     * <code>XMLStreamReader</code> points to.
     * <p>
     * Looks after 'gml:id' (GML 3) first, if no such attribute is present, the 'gid' (GML 2) attribute is used.
     * 
     * @return the geometry id, or "" (empty string) if neither a 'gml:id' nor a 'gid' attribute is present
     */
    private String parseGeometryId( XMLStreamReaderWrapper xmlStream ) {

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
