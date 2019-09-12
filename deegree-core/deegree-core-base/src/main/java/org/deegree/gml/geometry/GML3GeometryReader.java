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
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipElement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.gml.GMLObjectType;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.uom.Length;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.cs.coordinatesystems.CRS;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.types.AppSchemaGeometryHierarchy;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.composite.CompositeCurve;
import org.deegree.geometry.composite.CompositeGeometry;
import org.deegree.geometry.composite.CompositeSolid;
import org.deegree.geometry.composite.CompositeSurface;
import org.deegree.geometry.i18n.Messages;
import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.multi.MultiSolid;
import org.deegree.geometry.multi.MultiSurface;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Curve.CurveType;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.OrientableCurve;
import org.deegree.geometry.primitive.OrientableSurface;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.PolyhedralSurface;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Ring.RingType;
import org.deegree.geometry.primitive.Solid;
import org.deegree.geometry.primitive.Solid.SolidType;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.Surface.SurfaceType;
import org.deegree.geometry.primitive.Tin;
import org.deegree.geometry.primitive.TriangulatedSurface;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.primitive.patches.Triangle;
import org.deegree.geometry.primitive.segments.CurveSegment;
import org.deegree.geometry.primitive.segments.LineStringSegment;
import org.deegree.geometry.refs.CurveReference;
import org.deegree.geometry.refs.GeometricPrimitiveReference;
import org.deegree.geometry.refs.GeometryReference;
import org.deegree.geometry.refs.LineStringReference;
import org.deegree.geometry.refs.PointReference;
import org.deegree.geometry.refs.PolygonReference;
import org.deegree.geometry.refs.SolidReference;
import org.deegree.geometry.refs.SurfaceReference;
import org.deegree.gml.GMLStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parser for geometry and geometry-related constructs from the GML 3 specification series (3.0/3.1/3.2).
 * <p>
 * Supports the following geometry elements:
 * <p>
 * <ul>
 * <li><code>Point</code></li>
 * <li><code>LineString</code></li>
 * <li><code>Curve</code></li>
 * <li><code>OrientableCurve</code></li>
 * <li><code>CompositeCurve</code></li>
 * <li><code>LinearRing</code></li>
 * <li><code>Ring</code></li>
 * <li><code>Polygon</code></li>
 * <li><code>Surface</code></li>
 * <li><code>CompositeSurface</code></li>
 * <li><code>OrientableSurface</code></li>
 * <li><code>PolyhedralSurface</code></li>
 * <li><code>Surface</code></li>
 * <li><code>Tin</code></li>
 * <li><code>TriangulatedSurface</code></li>
 * <li><code>Solid</code></li>
 * <li><code>CompositeSolid</code></li>
 * <li><code>GeometricComplex</code></li>
 * <li><code>MultiPoint</code></li>
 * <li><code>MultiLineString</code></li>
 * <li><code>MultiCurve</code></li>
 * <li><code>MultiPolygon</code></li>
 * <li><code>MultiSurface</code></li>
 * <li><code>MultiGeometry</code></li>
 * </p>
 * <p>
 * Additionally, parsing of <code>Envelope</code> elements is supported, {@link #parseEnvelope(XMLStreamReaderWrapper)}
 * and {@link #parseGeometryOrEnvelope(XMLStreamReaderWrapper)}.
 * </p>
 * <p>
 * TODO Currently unsupported are the elements from the <code>_ImplicitGeometry</code> substitution group, i.e.
 * <code>Grid</code> and <code>RectifiedGrid</code>).
 * </p>
 *
 * @author <a href="mailto:wanhoff@lat-lon.de">Jeronimo Wanhoff</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class GML3GeometryReader extends GML3GeometryBaseReader implements GMLGeometryReader {

    private static Logger LOG = LoggerFactory.getLogger( GML3GeometryReader.class );

    private static String GID = "gid";

    private static String GMLID = "id";

    private final GML3CurveSegmentReader curveSegmentParser;

    private final GML3SurfacePatchReader surfacePatchParser;

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

    // local name of "gml:GeometricComplex"
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
        implictGeometryElements.add( "Grid" );
        implictGeometryElements.add( "RectifiedGrid" );

        // "gml:GeometricComplex"
        complexElements.add( "GeometricComplex" );
    }

    /**
     * Creates a new {@link GML3GeometryReader} for the given {@link GMLStreamReader}.
     *
     * @param gmlStream
     *            gml stream reader, must not be <code>null</code>
     */
    public GML3GeometryReader( GMLStreamReader gmlStream ) {
        super( gmlStream );
        curveSegmentParser = new GML3CurveSegmentReader( this, gmlStream );
        surfacePatchParser = new GML3SurfacePatchReader( this, gmlStream );
    }

    @Override
    public boolean isGeometryElement( XMLStreamReader reader ) {
        if ( reader != null && reader.getEventType() == XMLStreamConstants.START_ELEMENT ) {
            QName elName = reader.getName();
            return isGeometryElement( elName );
        }
        return false;
    }

    @Override
    public boolean isGeometryOrEnvelopeElement( XMLStreamReader reader ) {
        if ( reader != null && reader.getEventType() == XMLStreamConstants.START_ELEMENT ) {
            QName elName = reader.getName();
            return isGeometryOrEnvelopeElement( elName );
        }
        return false;
    }

    /**
     * Returns whether the given element name denotes a GML 3.1.1 geometry element (a concrete element substitutable for
     * "gml:_Geometry").
     *
     * @param elName
     *            qualified element name to check
     * @return true, if the element is a GML 3.1.1 geometry element, false otherwise
     */
    public boolean isGeometryElement( QName elName ) {
        if ( schema != null && schema.getGeometryHierarchy() != null ) {
            GMLObjectType type = schema.getGeometryType( elName );
            return type != null;
        }
        if ( !gmlNs.equals( elName.getNamespaceURI() ) ) {
            return false;
        }
        String localName = elName.getLocalPart();
        return primitiveElements.contains( localName ) || aggregateElements.contains( localName )
               || complexElements.contains( localName ) || implictGeometryElements.contains( localName );
    }

    /**
     * Returns whether the given element name denotes a GML 3.1.1 geometry (a concrete element substitutable for
     * "gml:_Geometry") or envelope element.
     *
     * @param elName
     *            qualified element name to check
     * @return true, if the element is a GML 3.1.1 geometry or a GML 3.1.1 envelope element, false otherwise
     */
    public boolean isGeometryOrEnvelopeElement( QName elName ) {
        if ( elName.getLocalPart().equals( "Envelope" ) && gmlNs.equals( elName.getNamespaceURI() ) ) {
            return true;
        }
        return isGeometryElement( elName );
    }

    /**
     * Returns the object representation for the given <code>gml:_Geometry</code> element event that the cursor of the
     * associated <code>XMLStreamReader</code> points at.
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_Geometry&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/gml:_Geometry&gt;)</li>
     * </ul>
     * <p>
     * GML 3.1.1 specifies the following elements to be <b>directly</b> substitutable for <code>gml:_Geometry</code>:
     * <ul>
     * <li><code>_GeometricPrimitive</code></li>
     * <li><code>_Ring</code></li>
     * <li><code>_GeometricAggregate</code></li>
     * <li><code>GeometricComplex</code></li>
     * <li><code>_ImplicitGeometry</code></li>
     * </ul>
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_Geometry&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:_Geometry&gt;) afterwards
     * @return corresponding {@link Geometry} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_Geometry" element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    @Override
    public Geometry parse( XMLStreamReaderWrapper xmlStream )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {
        return parse( xmlStream, null );
    }

    /**
     * Returns the object representation for the given <code>gml:_Geometry</code> element event that the cursor of the
     * associated <code>XMLStreamReader</code> points at.
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_Geometry&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/gml:_Geometry&gt;)</li>
     * </ul>
     * <p>
     * GML 3.1.1 specifies the following elements to be <b>directly</b> substitutable for <code>gml:_Geometry</code>:
     * <ul>
     * <li><code>_GeometricPrimitive</code></li>
     * <li><code>_Ring</code></li>
     * <li><code>_GeometricAggregate</code></li>
     * <li><code>GeometricComplex</code></li>
     * <li><code>_ImplicitGeometry</code></li>
     * </ul>
     *
     * @param xmlStream
     *            cursor must point at the <code>jec</code> event (&lt;gml:_Geometry&gt;), points at the corresponding
     *            <code>END_ELEMENT</code> event (&lt;/gml:_Geometry&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Geometry} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_Geometry" element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    @Override
    public Geometry parse( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        Geometry geometry = null;
        AppSchemaGeometryHierarchy geomHierarchy = getGeometryHierarchy();

        if ( geomHierarchy != null ) {
            GMLObjectType type = schema.getGeometryType( xmlStream.getName() );
            if ( type == null ) {
                String msg = "Invalid geometry element: '" + xmlStream.getName()
                             + "'. Not defined in application/core schema in use.";
                throw new XMLParsingException( xmlStream, msg );
            }

            QName elName = xmlStream.getName();
            String name = xmlStream.getLocalName();
            if ( geomHierarchy.getPrimitiveElementNames().contains( elName ) ) {
                geometry = parseGeometricPrimitive( xmlStream, defaultCRS );
            } else if ( ringElements.contains( name ) ) {
                geometry = parseAbstractRing( xmlStream, defaultCRS );
            } else if ( aggregateElements.contains( name ) ) {
                geometry = parseGeometricAggregate( xmlStream, defaultCRS );
            } else if ( "GeometricComplex".equals( name ) ) {
                geometry = parseGeometricComplex( xmlStream, defaultCRS );
            } else if ( implictGeometryElements.contains( name ) ) {
                geometry = parseImplicitGeometry( xmlStream, defaultCRS );
            } else {
                String msg = "Invalid GML geometry: '" + xmlStream.getName()
                             + "' does not denote a well-known/application-schema defined GML geometry element.";
                throw new XMLParsingException( xmlStream, msg );
            }
        } else {
            if ( !gmlNs.equals( xmlStream.getNamespaceURI() ) ) {
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
            } else if ( "GeometricComplex".equals( name ) ) {
                geometry = parseGeometricComplex( xmlStream, defaultCRS );
            } else if ( implictGeometryElements.contains( name ) ) {
                geometry = parseImplicitGeometry( xmlStream, defaultCRS );
            } else {
                String msg = "Invalid GML geometry: '" + xmlStream.getName()
                             + "' does not denote a well-known GML geometry element.";
                throw new XMLParsingException( xmlStream, msg );
            }
        }
        return geometry;
    }

    /**
     * Returns the object representation for the given <code>gml:_Geometry/gml:Envelope</code> element event that the
     * cursor of the associated <code>XMLStreamReader</code> points at.
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_Geometry&gt; or
     * &lt;gml:Envelope&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/gml:_Geometry&gt; or
     * &lt;gml:Envelope&gt;)</li>
     * </ul>
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_Geometry&gt; or
     *            &lt;gml:Envelope&gt;), points at the corresponding <code>END_ELEMENT</code> event
     *            (&lt;/gml:_Geometry&gt; or &lt;gml:Envelope&gt;) afterwards
     * @return corresponding {@link Geometry} object
     * @throws XMLParsingException
     *             if the element is not a valid <code>gml:_Geometry</code> or <code>gml:Envelope</code> element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    public Geometry parseGeometryOrEnvelope( XMLStreamReaderWrapper xmlStream )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {
        return parseGeometryOrEnvelope( xmlStream, null );
    }

    /**
     * Returns the object representation for the given <code>gml:_Geometry/gml:Envelope</code> element event that the
     * cursor of the associated <code>XMLStreamReader</code> points at.
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_Geometry&gt; or
     * &lt;gml:Envelope&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/gml:_Geometry&gt; or
     * &lt;gml:Envelope&gt;)</li>
     * </ul>
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_Geometry&gt; or
     *            &lt;gml:Envelope&gt;), points at the corresponding <code>END_ELEMENT</code> event
     *            (&lt;/gml:_Geometry&gt; or &lt;gml:Envelope&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Geometry} object
     * @throws XMLParsingException
     *             if the element is not a valid <code>gml:_Geometry</code> or <code>gml:Envelope</code> element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    @Override
    public Geometry parseGeometryOrEnvelope( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        Geometry geometry = null;

        if ( !gmlNs.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Unexpected element: " + xmlStream.getName()
                         + "' is not a GML geometry or envelope element. Not in the gml namespace.";
            throw new XMLParsingException( xmlStream, msg );
        }

        if ( "Envelope".equals( xmlStream.getLocalName() ) ) {
            geometry = parseEnvelope( xmlStream, defaultCRS );
        } else {
            geometry = parse( xmlStream, defaultCRS );
        }
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
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_GeometricPrimitive&gt;), points at
     *            the corresponding <code>END_ELEMENT</code> event (&lt;/gml:_GeometricPrimitive&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link GeometricPrimitive} object
     * @throws XMLParsingException
     *             if the element is not a valid <code>gml:_GeometricPrimitive</code> element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    public GeometricPrimitive parseGeometricPrimitive( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        GeometricPrimitive primitive = null;
        AppSchemaGeometryHierarchy geomHierarchy = getGeometryHierarchy();

        if ( geomHierarchy != null ) {
            QName elName = xmlStream.getName();
            if ( geomHierarchy.getPointElementNames().contains( elName ) ) {
                primitive = parsePoint( xmlStream, defaultCRS );
            } else if ( geomHierarchy.getAbstractCurveSubstitutions().contains( elName ) ) {
                primitive = parseAbstractCurve( xmlStream, defaultCRS );
            } else if ( geomHierarchy.getRingElementNames().contains( elName ) ) {
                primitive = parseAbstractRing( xmlStream, defaultCRS );
            } else if ( geomHierarchy.getAbstractSurfaceElementNames().contains( elName ) ) {
                primitive = parseAbstractSurface( xmlStream, defaultCRS );
            } else if ( geomHierarchy.getSolidElementNames().contains( elName ) ) {
                primitive = parseAbstractSolid( xmlStream, defaultCRS );
            } else {
                String msg = "Invalid GML geometry: '" + xmlStream.getName()
                             + "' does not denote a well-known/application-schema defined GML geometry element.";
                throw new XMLParsingException( xmlStream, msg );
            }
        } else {
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
                             + "' is not a well-known GML primitive geometry element (gml:_GeometricPrimitive).";
                throw new XMLParsingException( xmlStream, msg );
            }
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
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_GeometricAggregate&gt;), points at
     *            the corresponding <code>END_ELEMENT</code> event (&lt;/gml:_GeometricAggregate&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link MultiGeometry} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_GeometricAggregate" element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    public MultiGeometry<? extends Geometry> parseGeometricAggregate( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        MultiGeometry<? extends Geometry> geometry = null;

        if ( !gmlNs.equals( xmlStream.getNamespaceURI() ) ) {
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
                         + "' is not a GML 3.1.1 aggregate geometry element (gml:_GeometricAggregate).";
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
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event, points at the corresponding
     *            <code>END_ELEMENT</code> event afterwards
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

        if ( !gmlNs.equals( xmlStream.getNamespaceURI() ) ) {
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
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_ImplicitGeometry&gt;), points at
     *            the corresponding <code>END_ELEMENT</code> event (&lt;/gml:_ImplicitGeometry&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Geometry} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_ImplicitGeometry" element
     * @throws XMLStreamException
     */
    public Geometry parseImplicitGeometry( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLParsingException, XMLStreamException {

        // Geometry geometry = null;

        if ( !gmlNs.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Invalid gml:_ImplicitGeometry element: " + xmlStream.getName()
                         + "' is not a GML geometry element. Not in the gml namespace.";
            throw new XMLParsingException( xmlStream, msg );
        }

        String name = xmlStream.getLocalName();
        if ( name.equals( "Grid" ) ) {
            String msg = "Parsing of 'gml:" + xmlStream.getLocalName() + "' elements is not supported.";
            throw new XMLParsingException( xmlStream, msg );
        } else if ( name.equals( "RectifiedGrid" ) ) {
            String msg = "Parsing of 'gml:" + xmlStream.getLocalName() + "' elements is not supported.";
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
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_Curve&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:_Curve&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Curve} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_Curve" element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    public Curve parseAbstractCurve( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        Curve curve = null;

        QName elName = xmlStream.getName();
        AppSchemaGeometryHierarchy geometryHierarchy = getGeometryHierarchy();
        if ( geometryHierarchy != null ) {
            if ( !geometryHierarchy.getAbstractCurveSubstitutions().contains( elName ) ) {
                String msg = "Invalid curve geometry element. '" + xmlStream.getName()
                             + "' is not defined in the active application schema.";
                throw new XMLParsingException( xmlStream, msg );
            }
            if ( geometryHierarchy.getCurveSubstitutions().contains( elName ) ) {
                curve = parseCurve( xmlStream, defaultCRS );
            } else if ( geometryHierarchy.getLineStringSubstitutions().contains( elName ) ) {
                curve = parseLineString( xmlStream, defaultCRS );
            } else if ( geometryHierarchy.getCompositeCurveSubstitutions().contains( elName ) ) {
                curve = parseCompositeCurve( xmlStream, defaultCRS );
            } else if ( geometryHierarchy.getOrientableCurveSubstitutions().contains( elName ) ) {
                curve = parseOrientableCurve( xmlStream, defaultCRS );
            } else {
                String msg = "Unhandled curve geometry element: '" + xmlStream.getName() + "'.";
                throw new XMLParsingException( xmlStream, msg );
            }
        } else {
            if ( !gmlNs.equals( xmlStream.getNamespaceURI() ) ) {
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
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_Ring&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:_Ring&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Ring} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_Ring" element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    public Ring parseAbstractRing( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        Ring ring = null;

        if ( !gmlNs.equals( xmlStream.getNamespaceURI() ) ) {
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
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_Surface&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:_Surface&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only use CoordinateSystem the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Surface} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_Surface" element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    public Surface parseAbstractSurface( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        Surface surface = null;

        QName elName = xmlStream.getName();
        AppSchemaGeometryHierarchy geometryHierarchy = getGeometryHierarchy();
        if ( geometryHierarchy != null ) {
            if ( !geometryHierarchy.getAbstractSurfaceElementNames().contains( elName ) ) {
                String msg = "Invalid surface geometry element. '" + xmlStream.getName()
                             + "' is not defined in the active application schema.";
                throw new XMLParsingException( xmlStream, msg );
            }
            if ( geometryHierarchy.getSurfaceSubstitutions().contains( elName ) ) {
                surface = parseSurface( xmlStream, defaultCRS );
            } else if ( "Polygon".equals( elName.getLocalPart() ) ) {
                surface = parsePolygon( xmlStream, defaultCRS );
            } else if ( geometryHierarchy.getCompositeSurfaceSubstitutions().contains( elName ) ) {
                surface = parseCompositeSurface( xmlStream, defaultCRS );
            } else {
                String msg = "Unhandled surface geometry element: '" + xmlStream.getName() + "'.";
                throw new XMLParsingException( xmlStream, msg );
            }
        } else {

            if ( !gmlNs.equals( xmlStream.getNamespaceURI() ) ) {
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
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:_Solid&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:_Solid&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Solid} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_Solid" element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    public Solid parseAbstractSolid( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        Solid solid = null;

        if ( !gmlNs.equals( xmlStream.getNamespaceURI() ) ) {
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
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:Point&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:Point&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is used if the "gml:Point" has no <code>srsName</code> attribute
     * @return corresponding {@link Point} object
     * @throws XMLParsingException
     *             if the element is not a valid <code>gml:Point</code> element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    public Point parsePoint( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        Point point = null;
        GMLObjectType type = getType( xmlStream );
        QName elName = xmlStream.getName();
        String gid = parseGeometryId( xmlStream );
        ICRS crs = determineActiveCRS( xmlStream, defaultCRS );
        List<Property> props = readStandardProperties( xmlStream, type, crs );

        // must contain one of the following child elements: "gml:pos", "gml:coordinates" or "gml:coord"
        if ( xmlStream.getEventType() == START_ELEMENT ) {
            String name = xmlStream.getLocalName();
            if ( "pos".equals( name ) ) {
                crs = determineActiveCRS( xmlStream, crs );
                double[] coords = parseDoubleList( xmlStream );
                point = geomFac.createPoint( gid, coords, crs );
            } else if ( "coordinates".equals( name ) ) {
                List<Point> points = parseCoordinates( xmlStream, crs );
                if ( points.size() != 1 ) {
                    String msg = "A gml:Point (or derived) element must contain exactly one tuple of coordinates.";
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
            String msg = "Error in 'gml:Point' element. Expected one of the following properties: 'gml:pos', 'gml:coordinates'"
                         + " or 'gml:coord'.";
            throw new XMLParsingException( xmlStream, msg );
        }

        nextElement( xmlStream );

        point.setType( type );

        props.addAll( readAdditionalProperties( xmlStream, type, crs ) );
        point.setProperties( props );

        idContext.addObject( point );

        xmlStream.require( END_ELEMENT, elName.getNamespaceURI(), elName.getLocalPart() );
        return point;
    }

    /**
     * Returns the object representation of a <code>gml:LineString</code> element. Consumes all corresponding events
     * from the given <code>XMLStream</code>.
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:LineString&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:LineString&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:LineString</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link LineString} object
     * @throws XMLParsingException
     *             if the element is not a valid <code>gml:LineString</code> element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public LineString parseLineString( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        String gid = parseGeometryId( xmlStream );
        ICRS crs = determineActiveCRS( xmlStream, defaultCRS );
        QName elName = xmlStream.getName();
        GMLObjectType type = getType( xmlStream );
        List<Property> props = readStandardProperties( xmlStream, type, crs );

        List<Point> points = null;
        if ( xmlStream.getEventType() == XMLStreamConstants.START_ELEMENT ) {
            String name = xmlStream.getLocalName();
            if ( "posList".equals( name ) ) {
                points = parsePosList( xmlStream, crs );
                xmlStream.nextTag();
            } else if ( "coordinates".equals( name ) ) {
                // deprecated since GML 3.1.0, only included for backward compatibility
                points = parseCoordinates( xmlStream, crs );
                xmlStream.nextTag();
            } else {
                points = new LinkedList<Point>();
                do {
                    if ( "pos".equals( name ) ) {
                        double[] coords = parseDoubleList( xmlStream );
                        // anonymous point (no registering necessary)
                        points.add( geomFac.createPoint( null, coords, crs ) );
                    } else if ( "pointProperty".equals( name ) || "pointRep".equals( name ) ) {
                        // pointRep has been deprecated since GML 3.1.0, only included for backward compatibility
                        points.add( parsePointProperty( xmlStream, crs ) );
                    } else if ( "coord".equals( name ) ) {
                        // deprecated since GML 3.0, only included for backward compatibility
                        double[] coords = parseCoordType( xmlStream );
                        // anonymous point (no registering necessary)
                        points.add( geomFac.createPoint( null, coords, crs ) );
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

        LineString lineString = geomFac.createLineString( gid, crs, geomFac.createPoints( points ) );
        lineString.setType( type );

        props.addAll( readAdditionalProperties( xmlStream, type, crs ) );
        lineString.setProperties( props );
        idContext.addObject( lineString );

        xmlStream.require( END_ELEMENT, elName.getNamespaceURI(), elName.getLocalPart() );
        return lineString;
    }

    /**
     * Returns the object representation of a <code>gml:Curve</code> element. Consumes all corresponding events from the
     * associated <code>XMLStream</code>.
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:Curve&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:Curve&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:Curve</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link Curve} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public Curve parseCurve( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        QName elName = xmlStream.getName();
        GMLObjectType type = getType( xmlStream );
        String gid = parseGeometryId( xmlStream );
        ICRS crs = determineActiveCRS( xmlStream, defaultCRS );
        List<Property> props = readStandardProperties( xmlStream, type, crs );

        xmlStream.require( XMLStreamConstants.START_ELEMENT, gmlNs, "segments" );
        List<CurveSegment> segments = new LinkedList<CurveSegment>();

        while ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
            segments.add( curveSegmentParser.parseCurveSegment( xmlStream, crs ) );
        }
        nextElement( xmlStream );

        Curve curve = geomFac.createCurve( gid, crs, segments.toArray( new CurveSegment[segments.size()] ) );
        curve.setType( type );

        props.addAll( readAdditionalProperties( xmlStream, type, crs ) );
        curve.setProperties( props );

        idContext.addObject( curve );
        xmlStream.require( END_ELEMENT, elName.getNamespaceURI(), elName.getLocalPart() );
        return curve;
    }

    /**
     * Returns the object representation of a <code>gml:OrientableCurve</code> element. Consumes all corresponding
     * events from the associated <code>XMLStream</code>.
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:OrientableCurve&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:OrientableCurve&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:OrientableCurve</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link OrientableCurve} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public OrientableCurve parseOrientableCurve( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, UnknownCRSException {

        QName elName = xmlStream.getName();
        GMLObjectType type = getType( xmlStream );
        String gid = parseGeometryId( xmlStream );
        boolean isReversed = !parseOrientation( xmlStream );
        ICRS crs = determineActiveCRS( xmlStream, defaultCRS );
        List<Property> props = readStandardProperties( xmlStream, type, crs );

        xmlStream.require( START_ELEMENT, gmlNs, "baseCurve" );
        Curve baseCurve = parseCurveProperty( xmlStream, crs );
        nextElement( xmlStream );

        OrientableCurve orientableCurve = geomFac.createOrientableCurve( gid, crs, baseCurve, isReversed );
        orientableCurve.setType( type );

        props.addAll( readAdditionalProperties( xmlStream, type, crs ) );
        orientableCurve.setProperties( props );

        idContext.addObject( orientableCurve );

        xmlStream.require( END_ELEMENT, elName.getNamespaceURI(), elName.getLocalPart() );
        return orientableCurve;
    }

    /**
     * Returns the object representation of a <code>gml:LinearRing</code> element. Consumes all corresponding events
     * from the associated <code>XMLStream</code>.
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:LinearRing&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:LinearRing&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:LinearRing</code> element has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link Ring} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public LinearRing parseLinearRing( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        QName elName = xmlStream.getName();
        GMLObjectType type = getType( xmlStream );
        String gid = parseGeometryId( xmlStream );
        ICRS crs = determineActiveCRS( xmlStream, defaultCRS );
        List<Property> props = readStandardProperties( xmlStream, type, crs );

        Points points = curveSegmentParser.parseControlPoints( xmlStream, crs );
        if ( points.size() < 4 ) {
            String msg = "Error in 'gml:LinearRing' element. Must specify at least four points.";
            throw new XMLParsingException( xmlStream, msg );
        }

        LinearRing linearRing = geomFac.createLinearRing( gid, crs, points );
        linearRing.setType( type );

        props.addAll( readAdditionalProperties( xmlStream, type, crs ) );
        linearRing.setProperties( props );

        idContext.addObject( linearRing );
        xmlStream.require( END_ELEMENT, elName.getNamespaceURI(), elName.getLocalPart() );
        return linearRing;
    }

    /**
     * Returns the object representation of a <code>gml:Ring</code> element. Consumes all corresponding events from the
     * given <code>XMLStream</code>.
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:Ring&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:Ring&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:Ring</code> element has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link Ring} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public Ring parseRing( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, UnknownCRSException {

        QName elName = xmlStream.getName();
        GMLObjectType type = getType( xmlStream );
        String gid = parseGeometryId( xmlStream );
        ICRS crs = determineActiveCRS( xmlStream, defaultCRS );
        List<Property> props = readStandardProperties( xmlStream, type, crs );

        List<Curve> memberCurves = new LinkedList<Curve>();

        while ( xmlStream.getEventType() == START_ELEMENT ) {
            // must be a 'gml:curveMember' element
            if ( !xmlStream.getLocalName().equals( "curveMember" ) ) {
                String msg = "Error in 'gml:Ring' element. Expected a 'gml:curveMember' element.";
                throw new XMLParsingException( xmlStream, msg );
            }
            memberCurves.add( parseCurveProperty( xmlStream, crs ) );
            xmlStream.require( END_ELEMENT, gmlNs, "curveMember" );
            xmlStream.nextTag();
        }
        Ring ring = geomFac.createRing( gid, crs, memberCurves );

        props.addAll( readAdditionalProperties( xmlStream, type, crs ) );
        ring.setType( type );
        ring.setProperties( props );

        idContext.addObject( ring );

        xmlStream.require( END_ELEMENT, elName.getNamespaceURI(), elName.getLocalPart() );
        return ring;
    }

    /**
     * Returns the object representation of a (&lt;gml:Polygon&gt;) element. Consumes all corresponding events from the
     * given <code>XMLStream</code>.
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:Polygon&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:Polygon&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the "gml:Polygon" has no <code>srsName</code>
     *            attribute
     * @return corresponding {@link Polygon} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public Polygon parsePolygon( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        QName elName = xmlStream.getName();
        GMLObjectType type = getType( xmlStream );
        String gid = parseGeometryId( xmlStream );
        ICRS crs = determineActiveCRS( xmlStream, defaultCRS );
        List<Property> props = readStandardProperties( xmlStream, type, crs );

        Ring exteriorRing = null;
        List<Ring> interiorRings = new LinkedList<Ring>();

        // NOTE: No need to check for xlink:href in the properties (AbstractRingPropertyType does not allow this).
        // 0 or 1 exterior/outerBoundaryIs element (yes, 0 is possible -- see section 9.2.2.5 of GML spec)
        if ( xmlStream.getEventType() == START_ELEMENT ) {
            if ( xmlStream.getLocalName().equals( "exterior" ) ) {
                if ( xmlStream.nextTag() != START_ELEMENT ) {
                    String msg = "Error in 'gml:Polygon' element. Expected a 'gml:_Ring' element.";
                    throw new XMLParsingException( xmlStream, msg );
                }
                exteriorRing = parseAbstractRing( xmlStream, crs );
                xmlStream.nextTag();
                xmlStream.require( END_ELEMENT, gmlNs, "exterior" );
                xmlStream.nextTag();
            } else if ( xmlStream.getLocalName().equals( "outerBoundaryIs" ) ) {
                if ( xmlStream.nextTag() != START_ELEMENT ) {
                    String msg = "Error in 'gml:Polygon' element. Expected a 'gml:LinearRing' element.";
                    throw new XMLParsingException( xmlStream, msg );
                }
                exteriorRing = parseLinearRing( xmlStream, crs );
                xmlStream.nextTag();
                xmlStream.require( END_ELEMENT, gmlNs, "outerBoundaryIs" );
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
                xmlStream.require( END_ELEMENT, gmlNs, "interior" );
            } else if ( xmlStream.getLocalName().equals( "innerBoundaryIs" ) ) {
                if ( xmlStream.nextTag() != START_ELEMENT ) {
                    String msg = "Error in 'gml:Polygon' element. Expected a 'gml:LinearRing' element.";
                    throw new XMLParsingException( xmlStream, msg );
                }
                interiorRings.add( parseLinearRing( xmlStream, crs ) );
                xmlStream.nextTag();
                xmlStream.require( END_ELEMENT, gmlNs, "innerBoundaryIs" );
            } else {
                String msg = "Error in 'gml:Polygon' element. Expected a 'gml:interior' or a 'gml:innerBoundaryIs' element, but found: '"
                             + xmlStream.getName() + "'.";
                throw new XMLParsingException( xmlStream, msg );
            }
            xmlStream.nextTag();
        }

        Polygon polygon = geomFac.createPolygon( gid, crs, exteriorRing, interiorRings );
        polygon.setType( type );

        props.addAll( readAdditionalProperties( xmlStream, type, crs ) );
        polygon.setProperties( props );

        idContext.addObject( polygon );

        xmlStream.require( END_ELEMENT, elName.getNamespaceURI(), elName.getLocalPart() );
        return polygon;
    }

    /**
     * Returns the object representation of a (&lt;gml:Surface&gt;) element. Consumes all corresponding events from the
     * given <code>XMLStream</code>.
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:Surface&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:Surface&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the "gml:Surface" has no <code>srsName</code>
     *            attribute
     * @return corresponding {@link Surface} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public Surface parseSurface( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, UnknownCRSException {

        QName elName = xmlStream.getName();
        GMLObjectType type = getType( xmlStream );
        String gid = parseGeometryId( xmlStream );
        ICRS crs = determineActiveCRS( xmlStream, defaultCRS );
        List<Property> props = readStandardProperties( xmlStream, type, crs );

        List<SurfacePatch> memberPatches = new LinkedList<SurfacePatch>();
        if ( xmlStream.getEventType() != START_ELEMENT || !gmlNs.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Surface requires a patches, trianglePatches or polygonPatches child element.";
            throw new XMLParsingException( xmlStream, msg );
        }
        String localName = xmlStream.getLocalName();
        if ( "patches".equals( localName ) ) {
            while ( xmlStream.nextTag() == START_ELEMENT ) {
                memberPatches.add( surfacePatchParser.parseSurfacePatch( xmlStream, crs ) );
            }
            xmlStream.require( END_ELEMENT, gmlNs, "patches" );
        } else if ( "trianglePatches".equals( localName ) ) {
            while ( xmlStream.nextTag() == START_ELEMENT ) {
                memberPatches.add( surfacePatchParser.parseTriangle( xmlStream, crs ) );
            }
            xmlStream.require( END_ELEMENT, gmlNs, "trianglePatches" );
        } else if ( "polygonPatches".equals( localName ) ) {
            while ( xmlStream.nextTag() == START_ELEMENT ) {
                memberPatches.add( surfacePatchParser.parsePolygonPatch( xmlStream, crs ) );
            }
            xmlStream.require( END_ELEMENT, gmlNs, "polygonPatches" );
        }
        nextElement( xmlStream );

        Surface surface = geomFac.createSurface( gid, memberPatches, crs );
        surface.setType( type );

        props.addAll( readAdditionalProperties( xmlStream, type, crs ) );
        surface.setProperties( props );

        idContext.addObject( surface );

        xmlStream.require( END_ELEMENT, elName.getNamespaceURI(), elName.getLocalPart() );
        return surface;
    }

    /**
     * Returns the object representation of a (&lt;gml:PolyhedralSurface&gt;) element. Consumes all corresponding events
     * from the given <code>XMLStream</code>.
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:PolyhedralSurface&gt;), points at
     *            the corresponding <code>END_ELEMENT</code> event (&lt;/gml:PolyhedralSurface&gt;) afterwards
     * @param defaultCRS
     *            default CoordinateSystem for the geometry, this is only used if the "gml:PolyhedralSurface" has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link PolyhedralSurface} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public PolyhedralSurface parsePolyhedralSurface( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, UnknownCRSException {

        QName elName = xmlStream.getName();
        GMLObjectType type = getType( xmlStream );
        String gid = parseGeometryId( xmlStream );
        ICRS crs = determineActiveCRS( xmlStream, defaultCRS );
        List<Property> props = readStandardProperties( xmlStream, type, crs );

        List<PolygonPatch> memberPatches = new LinkedList<PolygonPatch>();
        xmlStream.require( START_ELEMENT, gmlNs, "polygonPatches" );
        while ( xmlStream.nextTag() == START_ELEMENT ) {
            memberPatches.add( surfacePatchParser.parsePolygonPatch( xmlStream, crs ) );
        }
        xmlStream.require( END_ELEMENT, gmlNs, "polygonPatches" );
        nextElement( xmlStream );

        PolyhedralSurface polyhedralSurface = geomFac.createPolyhedralSurface( gid, crs, memberPatches );
        polyhedralSurface.setType( type );

        props.addAll( readAdditionalProperties( xmlStream, type, crs ) );
        polyhedralSurface.setProperties( props );

        idContext.addObject( polyhedralSurface );

        xmlStream.require( END_ELEMENT, elName.getNamespaceURI(), elName.getLocalPart() );
        return polyhedralSurface;
    }

    /**
     * Returns the object representation of a (&lt;gml:TriangulatedSurface&gt;) element. Consumes all corresponding
     * events from the given <code>XMLStream</code>.
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:TriangulatedSurface&gt;), points at
     *            the corresponding <code>END_ELEMENT</code> event (&lt;/gml:TriangulatedSurface&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the "gml:TriangulatedSurface" has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link TriangulatedSurface} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public TriangulatedSurface parseTriangulatedSurface( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, UnknownCRSException {

        QName elName = xmlStream.getName();
        GMLObjectType type = getType( xmlStream );
        String gid = parseGeometryId( xmlStream );
        ICRS crs = determineActiveCRS( xmlStream, defaultCRS );
        List<Property> props = readStandardProperties( xmlStream, type, crs );

        List<Triangle> memberPatches = new LinkedList<Triangle>();
        xmlStream.require( START_ELEMENT, gmlNs, "trianglePatches" );
        while ( xmlStream.nextTag() == START_ELEMENT ) {
            memberPatches.add( surfacePatchParser.parseTriangle( xmlStream, crs ) );
        }
        xmlStream.require( END_ELEMENT, gmlNs, "trianglePatches" );
        nextElement( xmlStream );

        TriangulatedSurface triangulatedSurface = geomFac.createTriangulatedSurface( gid, crs, memberPatches );
        triangulatedSurface.setType( type );

        props.addAll( readAdditionalProperties( xmlStream, type, crs ) );
        triangulatedSurface.setProperties( props );

        idContext.addObject( triangulatedSurface );

        xmlStream.require( END_ELEMENT, elName.getNamespaceURI(), elName.getLocalPart() );

        return triangulatedSurface;
    }

    /**
     * Returns the object representation of a (&lt;gml:Tin&gt;) element. Consumes all corresponding events from the
     * given <code>XMLStream</code>.
     * <p>
     * Note: GML 3.1.1 specifies both "gml:trianglePatches" and "gml:controlPoint" properties for "gml:Tin". This is
     * apparently redundant, and consequently (?) GML 3.2.1 only allows the controlPoint property here. This method
     * copes with this by only using the controlPoints for building the {@link Tin} object.
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:Tin&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:Tin&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the "gml:Tin" has no <code>srsName</code> attribute
     * @return corresponding {@link Tin} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public Tin parseTin( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        QName elName = xmlStream.getName();
        GMLObjectType type = getType( xmlStream );
        String gid = parseGeometryId( xmlStream );
        ICRS crs = determineActiveCRS( xmlStream, defaultCRS );
        List<Property> props = readStandardProperties( xmlStream, type, crs );

        List<Triangle> memberPatches = new LinkedList<Triangle>();
        xmlStream.require( START_ELEMENT, gmlNs, "trianglePatches" );
        while ( xmlStream.nextTag() == START_ELEMENT ) {
            // validate syntactically, but ignore the content for instantiating the geometry
            memberPatches.add( surfacePatchParser.parseTriangle( xmlStream, crs ) );
        }
        xmlStream.require( END_ELEMENT, gmlNs, "trianglePatches" );

        List<List<LineStringSegment>> stopLines = new LinkedList<List<LineStringSegment>>();
        if ( xmlStream.nextTag() == START_ELEMENT ) {
            while ( xmlStream.getLocalName().equals( "stopLines" ) ) {
                List<LineStringSegment> segments = new LinkedList<LineStringSegment>();
                while ( xmlStream.nextTag() == START_ELEMENT ) {
                    xmlStream.require( START_ELEMENT, gmlNs, "LineStringSegment" );
                    segments.add( curveSegmentParser.parseLineStringSegment( xmlStream, crs ) );
                    xmlStream.require( END_ELEMENT, gmlNs, "LineStringSegment" );
                }
                xmlStream.require( END_ELEMENT, gmlNs, "stopLines" );
                stopLines.add( segments );
                xmlStream.nextTag();
            }
        }

        List<List<LineStringSegment>> breakLines = new LinkedList<List<LineStringSegment>>();
        if ( xmlStream.getEventType() == START_ELEMENT ) {
            while ( xmlStream.getLocalName().equals( "breakLines" ) ) {
                List<LineStringSegment> segments = new LinkedList<LineStringSegment>();
                while ( xmlStream.nextTag() == START_ELEMENT ) {
                    xmlStream.require( START_ELEMENT, gmlNs, "LineStringSegment" );
                    segments.add( curveSegmentParser.parseLineStringSegment( xmlStream, crs ) );
                    xmlStream.require( END_ELEMENT, gmlNs, "LineStringSegment" );
                }
                xmlStream.require( END_ELEMENT, gmlNs, "breakLines" );
                breakLines.add( segments );
                xmlStream.nextTag();
            }
        }

        xmlStream.require( START_ELEMENT, gmlNs, "maxLength" );
        Length maxLength = parseLengthType( xmlStream );
        xmlStream.nextTag();

        List<Point> controlPoints = null;
        xmlStream.require( START_ELEMENT, gmlNs, "controlPoint" );
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
        nextElement( xmlStream );

        if ( controlPoints.size() < 3 ) {
            String msg = "Error in 'gml:Tin' element. Must specify three control points (=one triangle) at least.";
            throw new XMLParsingException( xmlStream, msg );
        }

        Tin tin = geomFac.createTin( gid, crs, stopLines, breakLines, maxLength, geomFac.createPoints( controlPoints ),
                                     memberPatches );
        tin.setType( type );

        props.addAll( readAdditionalProperties( xmlStream, type, crs ) );
        tin.setProperties( props );

        idContext.addObject( tin );

        xmlStream.require( END_ELEMENT, elName.getNamespaceURI(), elName.getLocalPart() );
        idContext.addObject( tin );
        return tin;
    }

    /**
     * Returns the object representation of a <code>gml:OrientableSurface</code> element. Consumes all corresponding
     * events from the associated <code>XMLStream</code>.
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:OrientableSurface&gt;), points at
     *            the corresponding <code>END_ELEMENT</code> event (&lt;/gml:OrientableSurface&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:OrientableSurface</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link OrientableSurface} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public OrientableSurface parseOrientableSurface( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        QName elName = xmlStream.getName();
        GMLObjectType type = getType( xmlStream );
        String gid = parseGeometryId( xmlStream );
        boolean isReversed = !parseOrientation( xmlStream );
        ICRS crs = determineActiveCRS( xmlStream, defaultCRS );
        List<Property> props = readStandardProperties( xmlStream, type, crs );

        xmlStream.require( XMLStreamConstants.START_ELEMENT, gmlNs, "baseSurface" );
        Surface baseSurface = parseSurfaceProperty( xmlStream, defaultCRS );
        xmlStream.require( XMLStreamConstants.END_ELEMENT, gmlNs, "baseSurface" );
        nextElement( xmlStream );

        OrientableSurface orientableSurface = geomFac.createOrientableSurface( gid, crs, baseSurface, isReversed );
        orientableSurface.setType( type );

        props.addAll( readAdditionalProperties( xmlStream, type, crs ) );
        orientableSurface.setProperties( props );

        idContext.addObject( orientableSurface );

        xmlStream.require( END_ELEMENT, elName.getNamespaceURI(), elName.getLocalPart() );
        return orientableSurface;
    }

    /**
     * Returns the object representation of a (&lt;gml:Solid&gt;) element. Consumes all corresponding events from the
     * given <code>XMLStream</code>.
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:Solid&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:Solid&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the "gml:Solid" has no <code>srsName</code>
     *            attribute
     * @return corresponding {@link Solid} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public Solid parseSolid( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        QName elName = xmlStream.getName();
        GMLObjectType type = getType( xmlStream );
        String gid = parseGeometryId( xmlStream );
        ICRS crs = determineActiveCRS( xmlStream, defaultCRS );
        List<Property> props = readStandardProperties( xmlStream, type, crs );

        Surface exteriorSurface = null;
        List<Surface> interiorSurfaces = new LinkedList<Surface>();

        // 0 or 1 exterior element (yes, 0 is possible -- see section 9.2.2.5 of GML spec)
        if ( xmlStream.getEventType() == START_ELEMENT ) {
            if ( xmlStream.getLocalName().equals( "exterior" ) ) {
                exteriorSurface = parseSurfaceProperty( xmlStream, crs );
                xmlStream.require( END_ELEMENT, gmlNs, "exterior" );
            }
            xmlStream.nextTag();
        }

        // arbitrary number of interior elements
        while ( xmlStream.getEventType() == START_ELEMENT ) {
            if ( xmlStream.getLocalName().equals( "interior" ) ) {
                interiorSurfaces.add( parseSurfaceProperty( xmlStream, crs ) );
                xmlStream.nextTag();
                xmlStream.require( END_ELEMENT, gmlNs, "interior" );
            } else {
                String msg = "Error in 'gml:Solid' element. Expected a 'gml:interior' element.";
                throw new XMLParsingException( xmlStream, msg );
            }
            xmlStream.nextTag();
        }
        Solid solid = geomFac.createSolid( gid, crs, exteriorSurface, interiorSurfaces );
        solid.setType( type );

        props.addAll( readAdditionalProperties( xmlStream, type, crs ) );
        solid.setProperties( props );

        idContext.addObject( solid );

        xmlStream.require( END_ELEMENT, elName.getNamespaceURI(), elName.getLocalPart() );
        return solid;
    }

    /**
     * Returns the object representation of a <code>gml:CompositeCurve</code> element. Consumes all corresponding events
     * from the associated <code>XMLStream</code>.
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:CompositeCurve&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:CompositeCurve&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:CompositeCurve</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link CompositeCurve} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public CompositeCurve parseCompositeCurve( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, UnknownCRSException {

        QName elName = xmlStream.getName();
        GMLObjectType type = getType( xmlStream );
        String gid = parseGeometryId( xmlStream );
        ICRS crs = determineActiveCRS( xmlStream, defaultCRS );
        List<Property> props = readStandardProperties( xmlStream, type, crs );

        List<Curve> memberCurves = new LinkedList<Curve>();

        do {
            xmlStream.require( START_ELEMENT, gmlNs, "curveMember" );
            memberCurves.add( parseCurveProperty( xmlStream, crs ) );
            xmlStream.require( END_ELEMENT, gmlNs, "curveMember" );
        } while ( xmlStream.nextTag() == START_ELEMENT );

        CompositeCurve curve = geomFac.createCompositeCurve( gid, crs, memberCurves );
        curve.setType( type );

        props.addAll( readAdditionalProperties( xmlStream, type, crs ) );
        curve.setProperties( props );

        idContext.addObject( curve );

        xmlStream.require( END_ELEMENT, elName.getNamespaceURI(), elName.getLocalPart() );
        return curve;
    }

    /**
     * Returns the object representation of a <code>gml:CompositeSurface</code> element. Consumes all corresponding
     * events from the associated <code>XMLStream</code>.
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:CompositeSurface&gt;), points at
     *            the corresponding <code>END_ELEMENT</code> event (&lt;/gml:CompositeSurface&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:CompositeSurface</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link CompositeSurface} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public CompositeSurface parseCompositeSurface( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        QName elName = xmlStream.getName();
        GMLObjectType type = getType( xmlStream );
        String gid = parseGeometryId( xmlStream );
        ICRS crs = determineActiveCRS( xmlStream, defaultCRS );
        List<Property> props = readStandardProperties( xmlStream, type, crs );

        List<Surface> memberSurfaces = new LinkedList<Surface>();

        do {
            xmlStream.require( START_ELEMENT, gmlNs, "surfaceMember" );
            memberSurfaces.add( parseSurfaceProperty( xmlStream, crs ) );
            xmlStream.require( END_ELEMENT, gmlNs, "surfaceMember" );
        } while ( xmlStream.nextTag() == START_ELEMENT );

        CompositeSurface compositeSurface = geomFac.createCompositeSurface( gid, crs, memberSurfaces );
        compositeSurface.setType( type );

        props.addAll( readAdditionalProperties( xmlStream, type, crs ) );
        compositeSurface.setProperties( props );

        idContext.addObject( compositeSurface );

        xmlStream.require( END_ELEMENT, elName.getNamespaceURI(), elName.getLocalPart() );
        return compositeSurface;
    }

    /**
     * Returns the object representation of a <code>gml:CompositeSolid</code> element. Consumes all corresponding events
     * from the associated <code>XMLStream</code>.
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:CompositeSolid&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:CompositeSolid&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:CompositeSolid</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link CompositeSolid} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public CompositeSolid parseCompositeSolid( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        QName elName = xmlStream.getName();
        GMLObjectType type = getType( xmlStream );
        String gid = parseGeometryId( xmlStream );
        ICRS crs = determineActiveCRS( xmlStream, defaultCRS );
        List<Property> props = readStandardProperties( xmlStream, type, crs );

        List<Solid> memberSolids = new LinkedList<Solid>();
        do {
            xmlStream.require( START_ELEMENT, gmlNs, "solidMember" );
            memberSolids.add( parseSolidProperty( xmlStream, crs ) );
            xmlStream.require( END_ELEMENT, gmlNs, "solidMember" );
        } while ( xmlStream.nextTag() == START_ELEMENT );

        CompositeSolid compositeSolid = geomFac.createCompositeSolid( gid, crs, memberSolids );
        compositeSolid.setType( type );

        props.addAll( readAdditionalProperties( xmlStream, type, crs ) );
        compositeSolid.setProperties( props );

        idContext.addObject( compositeSolid );

        xmlStream.require( END_ELEMENT, elName.getNamespaceURI(), elName.getLocalPart() );
        return compositeSolid;
    }

    /**
     * Returns the object representation of a <code>gml:GeometricComplex</code> element. Consumes all corresponding
     * events from the associated <code>XMLStream</code>.
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:GeometricComplex&gt;), points at
     *            the corresponding <code>END_ELEMENT</code> event (&lt;/gml:GeometricComplex&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:GeometricComplex</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link CompositeGeometry} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public CompositeGeometry<GeometricPrimitive> parseGeometricComplex( XMLStreamReaderWrapper xmlStream,
                                                                        ICRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        QName elName = xmlStream.getName();
        GMLObjectType type = getType( xmlStream );
        String gid = parseGeometryId( xmlStream );
        ICRS crs = determineActiveCRS( xmlStream, defaultCRS );
        List<Property> props = readStandardProperties( xmlStream, type, crs );

        List<GeometricPrimitive> memberSolids = new LinkedList<GeometricPrimitive>();

        do {
            xmlStream.require( START_ELEMENT, gmlNs, "element" );
            memberSolids.add( parseGeometricPrimitiveProperty( xmlStream, crs ) );
            xmlStream.require( END_ELEMENT, gmlNs, "element" );
        } while ( xmlStream.nextTag() == START_ELEMENT );

        CompositeGeometry<GeometricPrimitive> compositeGeometry = geomFac.createCompositeGeometry( gid, crs,
                                                                                                   memberSolids );
        compositeGeometry.setType( type );

        props.addAll( readAdditionalProperties( xmlStream, type, crs ) );
        compositeGeometry.setProperties( props );

        idContext.addObject( compositeGeometry );

        xmlStream.require( END_ELEMENT, elName.getNamespaceURI(), elName.getLocalPart() );
        return compositeGeometry;
    }

    /**
     * Returns the object representation of a <code>gml:MultiPoint</code> element. Consumes all corresponding events
     * from the associated <code>XMLStream</code>.
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:MultiPoint&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:MultiPoint&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:MultiPoint</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link MultiPoint} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public MultiPoint parseMultiPoint( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        QName elName = xmlStream.getName();
        GMLObjectType type = getType( xmlStream );
        String gid = parseGeometryId( xmlStream );
        ICRS crs = determineActiveCRS( xmlStream, defaultCRS );
        List<Property> props = readStandardProperties( xmlStream, type, crs );

        List<Point> members = new LinkedList<Point>();

        if ( xmlStream.isStartElement() ) {
            do {
                String localName = xmlStream.getLocalName();
                if ( localName.equals( "pointMember" ) ) {
                    members.add( parsePointProperty( xmlStream, crs ) );
                    xmlStream.require( END_ELEMENT, gmlNs, "pointMember" );
                } else if ( localName.equals( "pointMembers" ) ) {
                    while ( xmlStream.nextTag() == START_ELEMENT ) {
                        xmlStream.require( START_ELEMENT, gmlNs, "Point" );
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
            } while ( xmlStream.nextTag() == START_ELEMENT );
        }

        MultiPoint multiPoint = geomFac.createMultiPoint( gid, crs, members );
        multiPoint.setType( type );

        props.addAll( readAdditionalProperties( xmlStream, type, crs ) );
        multiPoint.setProperties( props );

        idContext.addObject( multiPoint );

        xmlStream.require( END_ELEMENT, elName.getNamespaceURI(), elName.getLocalPart() );
        return multiPoint;
    }

    /**
     * Returns the object representation of a <code>gml:MultiCurve</code> element. Consumes all corresponding events
     * from the associated <code>XMLStream</code>.
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:MultiCurve&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:MultiCurve&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:MultiCurve</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link MultiCurve} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public MultiCurve<?> parseMultiCurve( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        QName elName = xmlStream.getName();
        GMLObjectType type = getType( xmlStream );
        String gid = parseGeometryId( xmlStream );
        ICRS crs = determineActiveCRS( xmlStream, defaultCRS );
        List<Property> props = readStandardProperties( xmlStream, type, crs );

        List<Curve> members = new LinkedList<Curve>();

        if ( xmlStream.isStartElement() ) {
            do {
                String localName = xmlStream.getLocalName();
                if ( localName.equals( "curveMember" ) ) {
                    members.add( parseCurveProperty( xmlStream, crs ) );
                    xmlStream.require( END_ELEMENT, gmlNs, "curveMember" );
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
            } while ( xmlStream.nextTag() == START_ELEMENT );
        }

        MultiCurve<?> multiCurve = geomFac.createMultiCurve( gid, crs, members );
        multiCurve.setType( type );

        props.addAll( readAdditionalProperties( xmlStream, type, crs ) );
        multiCurve.setProperties( props );

        idContext.addObject( multiCurve );

        xmlStream.require( END_ELEMENT, elName.getNamespaceURI(), elName.getLocalPart() );
        return multiCurve;
    }

    /**
     * Returns the object representation of a <code>gml:MultiLineString</code> element. Consumes all corresponding
     * events from the associated <code>XMLStream</code>.
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:MultiLineString&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:MultiLineString&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:MultiLineString</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link MultiLineString} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public MultiLineString parseMultiLineString( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        QName elName = xmlStream.getName();
        GMLObjectType type = getType( xmlStream );
        String gid = parseGeometryId( xmlStream );
        ICRS crs = determineActiveCRS( xmlStream, defaultCRS );
        List<Property> props = readStandardProperties( xmlStream, type, crs );

        List<LineString> members = new LinkedList<LineString>();

        if ( xmlStream.isStartElement() ) {
            do {
                String localName = xmlStream.getLocalName();
                if ( localName.equals( "lineStringMember" ) ) {
                    members.add( parseLineStringProperty( xmlStream, crs ) );
                    xmlStream.require( END_ELEMENT, gmlNs, "lineStringMember" );
                } else {
                    String msg = "Invalid 'gml:MultiLineString' element: unexpected element '" + localName
                                 + "'. Expected 'lineStringMember'.";
                    throw new XMLParsingException( xmlStream, msg );
                }
            } while ( xmlStream.nextTag() == START_ELEMENT );
        }

        MultiLineString multiLineString = geomFac.createMultiLineString( gid, crs, members );
        multiLineString.setType( type );

        props.addAll( readAdditionalProperties( xmlStream, type, crs ) );
        multiLineString.setProperties( props );

        idContext.addObject( multiLineString );

        xmlStream.require( END_ELEMENT, elName.getNamespaceURI(), elName.getLocalPart() );
        return multiLineString;
    }

    /**
     * Returns the object representation of a <code>gml:MultiSurface</code> element. Consumes all corresponding events
     * from the associated <code>XMLStream</code>.
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:MultiSurface&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:MultiSurface&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:MultiSurface</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link MultiSurface} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public MultiSurface<?> parseMultiSurface( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        QName elName = xmlStream.getName();
        GMLObjectType type = getType( xmlStream );
        String gid = parseGeometryId( xmlStream );
        ICRS crs = determineActiveCRS( xmlStream, defaultCRS );
        List<Property> props = readStandardProperties( xmlStream, type, crs );

        List<Surface> members = new LinkedList<Surface>();

        if ( xmlStream.isStartElement() ) {
            do {
                String localName = xmlStream.getLocalName();
                if ( localName.equals( "surfaceMember" ) ) {
                    members.add( parseSurfaceProperty( xmlStream, crs ) );
                    xmlStream.require( END_ELEMENT, gmlNs, "surfaceMember" );
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
            } while ( xmlStream.nextTag() == START_ELEMENT );
        }

        MultiSurface<?> multiSurface = geomFac.createMultiSurface( gid, crs, members );
        multiSurface.setType( type );

        props.addAll( readAdditionalProperties( xmlStream, type, crs ) );
        multiSurface.setProperties( props );

        idContext.addObject( multiSurface );

        xmlStream.require( END_ELEMENT, elName.getNamespaceURI(), elName.getLocalPart() );
        return multiSurface;
    }

    /**
     * Returns the object representation of a <code>gml:MultiPolygon</code> element. Consumes all corresponding events
     * from the associated <code>XMLStream</code>.
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:MultiPolygon&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:MultiPolygon&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:MultiPolygon</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link MultiPolygon} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public MultiPolygon parseMultiPolygon( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        QName elName = xmlStream.getName();
        GMLObjectType type = getType( xmlStream );
        String gid = parseGeometryId( xmlStream );
        ICRS crs = determineActiveCRS( xmlStream, defaultCRS );
        List<Property> props = readStandardProperties( xmlStream, type, crs );

        List<Polygon> members = new LinkedList<Polygon>();

        if ( xmlStream.isStartElement() ) {
            do {
                String localName = xmlStream.getLocalName();
                if ( localName.equals( "polygonMember" ) ) {
                    members.add( parsePolygonProperty( xmlStream, crs ) );
                    xmlStream.require( END_ELEMENT, gmlNs, "polygonMember" );
                } else {
                    String msg = "Invalid 'gml:MultiPolygon' element: unexpected element '" + localName
                                 + "'. Expected 'polygonMember'.";
                    throw new XMLParsingException( xmlStream, msg );
                }
            } while ( xmlStream.nextTag() == START_ELEMENT );
        }

        MultiPolygon multiPolygon = geomFac.createMultiPolygon( gid, crs, members );
        multiPolygon.setType( type );

        props.addAll( readAdditionalProperties( xmlStream, type, crs ) );
        multiPolygon.setProperties( props );

        idContext.addObject( multiPolygon );

        xmlStream.require( END_ELEMENT, elName.getNamespaceURI(), elName.getLocalPart() );
        return multiPolygon;
    }

    /**
     * Returns the object representation of a <code>gml:MultiSolid</code> element. Consumes all corresponding events
     * from the associated <code>XMLStream</code>.
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:MultiSolid&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:MultiSolid&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:MultiSolid</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link MultiSolid} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public MultiSolid parseMultiSolid( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        QName elName = xmlStream.getName();
        GMLObjectType type = getType( xmlStream );
        String gid = parseGeometryId( xmlStream );
        ICRS crs = determineActiveCRS( xmlStream, defaultCRS );
        List<Property> props = readStandardProperties( xmlStream, type, crs );

        List<Solid> members = new LinkedList<Solid>();

        if ( xmlStream.isStartElement() ) {
            do {
                String localName = xmlStream.getLocalName();
                if ( localName.equals( "solidMember" ) ) {
                    members.add( parseSolidProperty( xmlStream, crs ) );
                    xmlStream.require( END_ELEMENT, gmlNs, "solidMember" );
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
            } while ( xmlStream.nextTag() == START_ELEMENT );
        }

        MultiSolid multiSolid = geomFac.createMultiSolid( gid, crs, members );
        multiSolid.setType( type );

        props.addAll( readAdditionalProperties( xmlStream, type, crs ) );
        multiSolid.setProperties( props );

        idContext.addObject( multiSolid );

        xmlStream.require( END_ELEMENT, elName.getNamespaceURI(), elName.getLocalPart() );
        return multiSolid;
    }

    /**
     * Returns the object representation of a <code>gml:MultiGeometry</code> element. Consumes all corresponding events
     * from the associated <code>XMLStream</code>.
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:MultiGeometry&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:MultiGeometry&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is only used if the <code>gml:MultiGeometry</code> has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link MultiGeometry} object
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public MultiGeometry<Geometry> parseMultiGeometry( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        QName elName = xmlStream.getName();
        GMLObjectType type = getType( xmlStream );
        String gid = parseGeometryId( xmlStream );
        ICRS crs = determineActiveCRS( xmlStream, defaultCRS );
        List<Property> props = readStandardProperties( xmlStream, type, crs );

        List<Geometry> members = new LinkedList<Geometry>();

        if ( xmlStream.isStartElement() ) {
            do {
                String localName = xmlStream.getLocalName();
                if ( localName.equals( "geometryMember" ) ) {
                    members.add( parseGeometryProperty( xmlStream, crs ) );
                    xmlStream.require( END_ELEMENT, gmlNs, "geometryMember" );
                } else if ( localName.equals( "geometryMembers" ) ) {
                    while ( xmlStream.nextTag() == START_ELEMENT ) {
                        members.add( parse( xmlStream, crs ) );
                    }
                    // geometryMembers may only occur once (and behind all surfaceMember) elements
                    xmlStream.nextTag();
                    break;
                } else {
                    String msg = "Invalid 'gml:MultiGeometry' element: unexpected element '" + localName
                                 + "'. Expected 'geometryMember' or 'geometryMembers'.";
                    throw new XMLParsingException( xmlStream, msg );
                }
            } while ( xmlStream.nextTag() == START_ELEMENT );
        }

        MultiGeometry<Geometry> multiGeometry = geomFac.createMultiGeometry( gid, crs, members );
        multiGeometry.setType( type );

        props.addAll( readAdditionalProperties( xmlStream, type, crs ) );
        multiGeometry.setProperties( props );

        idContext.addObject( multiGeometry );

        xmlStream.require( END_ELEMENT, elName.getNamespaceURI(), elName.getLocalPart() );
        return multiGeometry;
    }

    /**
     * Returns the object representation of a <code>gml:Envelope</code> element. Consumes all corresponding events from
     * the associated <code>XMLStream</code>.
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:Envelope&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:Envelope&gt;) afterwards
     * @return corresponding {@link Envelope} object
     * @throws XMLStreamException
     * @throws XMLParsingException
     */
    public Envelope parseEnvelope( XMLStreamReaderWrapper xmlStream )
                            throws XMLParsingException, XMLStreamException {
        return parseEnvelope( xmlStream, null );
    }

    /**
     * Returns the object representation of a <code>gml:Envelope</code> element. Consumes all corresponding events from
     * the associated <code>XMLStream</code>.
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:Envelope&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/gml:Envelope&gt;) afterwards
     * @param defaultCRS
     *            default CRS for the envelope, this is only used if the <code>gml:Envelope</code> has no
     *            <code>srsName</code> attribute itself
     * @return corresponding {@link Envelope} object
     * @throws XMLStreamException
     * @throws XMLParsingException
     */
    @Override
    public Envelope parseEnvelope( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLParsingException, XMLStreamException {

        ICRS crs = determineActiveCRS( xmlStream, defaultCRS );

        double[] lowerCorner = null;
        double[] upperCorner = null;

        // must contain exactly one of the following child elements: "gml:lowerCorner", "gml:coord", "gml:pos" or
        // "gml:coordinates"
        if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
            String name = xmlStream.getLocalName();
            if ( "lowerCorner".equals( name ) ) {
                lowerCorner = parseDoubleList( xmlStream );
                xmlStream.require( END_ELEMENT, gmlNs, "lowerCorner" );
                xmlStream.nextTag();
                xmlStream.require( START_ELEMENT, gmlNs, "upperCorner" );
                upperCorner = parseDoubleList( xmlStream );
                xmlStream.require( END_ELEMENT, gmlNs, "upperCorner" );
            } else if ( "coord".equals( name ) ) {
                lowerCorner = parseCoordType( xmlStream );
                xmlStream.require( END_ELEMENT, gmlNs, "coord" );
                xmlStream.nextTag();
                xmlStream.require( START_ELEMENT, gmlNs, "coord" );
                upperCorner = parseCoordType( xmlStream );
                xmlStream.require( END_ELEMENT, gmlNs, "coord" );
            } else if ( "pos".equals( name ) ) {
                // NOTE PLEASE: some envelopes only have the srsName on the pos, not on the envelope
                // this is a hack so the envelopes have a sensible crs set
                if ( crs == null ) {
                    crs = determineActiveCRS( xmlStream, defaultCRS );
                }
                lowerCorner = parseDoubleList( xmlStream );
                xmlStream.require( END_ELEMENT, gmlNs, "pos" );
                xmlStream.nextTag();
                xmlStream.require( START_ELEMENT, gmlNs, "pos" );
                upperCorner = parseDoubleList( xmlStream );
                xmlStream.require( END_ELEMENT, gmlNs, "pos" );
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
        xmlStream.require( END_ELEMENT, gmlNs, "Envelope" );
        return geomFac.createEnvelope( lowerCorner, upperCorner, crs );
    }

    /**
     * Returns the object representation of an element with type <code>gml:PointPropertyType</code> (such as
     * <code>gml:pointProperty</code>). Consumes all corresponding events from the given <code>XMLStream</code>.
     * <p>
     * The point value may be specified using an inline <code>gml:Point</code> element or using an
     * <code>xlink:href</code> attribute. In the latter case, a {@link PointReference} object is returned.
     * </p>
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event, points at the corresponding
     *            <code>END_ELEMENT</code> event afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is used if the contained "gml:Point" has no <code>srsName</code>
     *            attribute
     * @return corresponding {@link Point} object, this is a {@link PointReference} if the content is specified using
     *         xlink
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public Point parsePointProperty( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {
        Point point = null;
        String href = xmlStream.getAttributeValue( CommonNamespaces.XLNNS, "href" );
        if ( href != null && href.length() > 0 ) {
            LOG.debug( "Found geometry reference (xlink): '" + href + "'" );
            point = new PointReference( getResolver(), href, xmlStream.getSystemId() );
            idContext.addReference( (GeometryReference<?>) point );
            if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
                String msg = "Unexpected element '" + xmlStream.getName()
                             + "'. Point value has already been specified using xlink.";
                throw new XMLParsingException( xmlStream, msg );
            }
        } else if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
            // must be a 'gml:Point' element
            if ( !xmlStream.getLocalName().equals( "Point" ) ) {
                String msg = "Error in point property element. Expected a 'gml:Point' element.";
                throw new XMLParsingException( xmlStream, msg );
            }
            point = parsePoint( xmlStream, defaultCRS );
            xmlStream.nextTag();
        } else {
            String msg = "Error in point property element. Expected a 'gml:Point' element or an 'xlink:href' attribute.";
            throw new XMLParsingException( xmlStream, msg );
        }
        return point;
    }

    /**
     * Returns the object representation of an element with type <code>gml:LineStringPropertyType</code> (such as
     * <code>gml:lineStringProperty</code>). Consumes all corresponding events from the given <code>XMLStream</code>.
     * <p>
     * The LineString value may be specified using an inline <code>gml:LineString</code> element or using an
     * <code>xlink:href</code> attribute. In the latter case, a {@link LineStringReference} object is returned.
     * </p>
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event, points at the corresponding
     *            <code>END_ELEMENT</code> event afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is used if the contained "gml:LineString" has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link LineString} object, this is a {@link LineStringReference} if the content is
     *         specified using xlink
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public LineString parseLineStringProperty( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        LineString lineString = null;
        String href = xmlStream.getAttributeValue( CommonNamespaces.XLNNS, "href" );
        if ( href != null && href.length() > 0 ) {
            LOG.debug( "Found geometry reference (xlink): '" + href + "'" );
            lineString = new LineStringReference( getResolver(), href, xmlStream.getSystemId() );
            idContext.addReference( (GeometryReference<?>) lineString );
            if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
                String msg = "Unexpected element '" + xmlStream.getName()
                             + "'. LineString value has already been specified using xlink.";
                throw new XMLParsingException( xmlStream, msg );
            }
        } else if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
            // must be a 'gml:LineString' element
            if ( !xmlStream.getLocalName().equals( "LineString" ) ) {
                String msg = "Error in LineString property element. Expected a 'gml:LineString' element.";
                throw new XMLParsingException( xmlStream, msg );
            }
            lineString = parseLineString( xmlStream, defaultCRS );
            xmlStream.nextTag();
        } else {
            String msg = "Error in LineString property element. Expected a 'gml:LineString' element or an 'xlink:href' attribute.";
            throw new XMLParsingException( xmlStream, msg );
        }
        return lineString;
    }

    /**
     * Returns the object representation of an element with type <code>gml:CurvePropertyType</code> (such as
     * <code>gml:curveProperty</code>). Consumes all corresponding events from the given <code>XMLStream</code>.
     * <p>
     * The curve value may be specified using an inline <code>gml:_Curve</code> element or using an
     * <code>xlink:href</code> attribute. In the latter case, a {@link CurveReference} object is returned.
     * </p>
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event, points at the corresponding
     *            <code>END_ELEMENT</code> event afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is used if the contained "gml:_Curve" has no <code>srsName</code>
     *            attribute
     * @return corresponding {@link Curve} object, this is a {@link CurveReference} if the content is specified using
     *         xlink
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public Curve parseCurveProperty( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        Curve curve = null;
        String href = xmlStream.getAttributeValue( CommonNamespaces.XLNNS, "href" );
        if ( href != null && href.length() > 0 ) {
            LOG.debug( "Found geometry reference (xlink): '" + href + "'" );
            curve = new CurveReference<Curve>( getResolver(), href, xmlStream.getSystemId() );
            idContext.addReference( (GeometryReference<?>) curve );
            if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
                String msg = "Unexpected element '" + xmlStream.getName()
                             + "'. Curve value has already been specified using xlink.";
                throw new XMLParsingException( xmlStream, msg );
            }
        } else if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
            curve = parseAbstractCurve( xmlStream, defaultCRS );
            xmlStream.nextTag();
        } else {
            String msg = "Error in curve property element. Expected a 'gml:_Curve' element or an 'xlink:href' attribute.";
            throw new XMLParsingException( xmlStream, msg );
        }
        return curve;
    }

    /**
     * Returns the object representation of an element with type <code>gml:PolygonPropertyType</code> (such as
     * <code>gml:polygonProperty</code>). Consumes all corresponding events from the given <code>XMLStream</code>.
     * <p>
     * The polygon value may be specified using an inline <code>gml:Polygon</code> element or using an
     * <code>xlink:href</code> attribute. In the latter case, a {@link PolygonReference} object is returned.
     * </p>
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event, points at the corresponding
     *            <code>END_ELEMENT</code> event afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is used if the contained "gml:Polygon" has no <code>srsName</code>
     *            attribute
     * @return corresponding {@link Polygon} object, this is a {@link PolygonReference} if the content is specified
     *         using xlink
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public Polygon parsePolygonProperty( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        Polygon polygon = null;
        String href = xmlStream.getAttributeValue( CommonNamespaces.XLNNS, "href" );
        if ( href != null && href.length() > 0 ) {
            LOG.debug( "Found geometry reference (xlink): '" + href + "'" );
            polygon = new PolygonReference( getResolver(), href, xmlStream.getSystemId() );
            idContext.addReference( (GeometryReference<?>) polygon );
            if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
                String msg = "Unexpected element '" + xmlStream.getName()
                             + "'. Polygon value has already been specified using xlink.";
                throw new XMLParsingException( xmlStream, msg );
            }
        } else if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
            // must be a 'gml:Polygon' element
            if ( !xmlStream.getLocalName().equals( "Polygon" ) ) {
                String msg = "Error in polygon property element. Expected a 'gml:Polygon' element.";
                throw new XMLParsingException( xmlStream, msg );
            }
            polygon = parsePolygon( xmlStream, defaultCRS );
            xmlStream.nextTag();
        } else {
            String msg = "Error in Polygon property element. Expected a 'gml:Polygon' element or an 'xlink:href' attribute.";
            throw new XMLParsingException( xmlStream, msg );
        }
        return polygon;
    }

    /**
     * Returns the object representation of an element with type <code>gml:SurfacePropertyType</code> (such as
     * <code>gml:surfaceProperty</code>). Consumes all corresponding events from the given <code>XMLStream</code>.
     * <p>
     * The curve value may be specified using an inline <code>gml:_Surface</code> element or using an
     * <code>xlink:href</code> attribute. In the latter case, a {@link SurfaceReference} object is returned.
     * </p>
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event, points at the corresponding
     *            <code>END_ELEMENT</code> event afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is used if the contained "gml:_Surface" has no <code>srsName</code>
     *            attribute
     * @return corresponding {@link Surface} object, this is a {@link SurfaceReference} if the content is specified
     *         using xlink
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public Surface parseSurfaceProperty( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        Surface surface = null;
        String href = xmlStream.getAttributeValue( CommonNamespaces.XLNNS, "href" );
        if ( href != null && href.length() > 0 ) {
            LOG.debug( "Found geometry reference (xlink): '" + href + "'" );
            surface = new SurfaceReference<Surface>( getResolver(), href, xmlStream.getSystemId() );
            idContext.addReference( (GeometryReference<?>) surface );
            if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
                String msg = "Unexpected element '" + xmlStream.getName()
                             + "'. Surface value has already been specified using xlink.";
                throw new XMLParsingException( xmlStream, msg );
            }
        } else if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
            surface = parseAbstractSurface( xmlStream, defaultCRS );
            xmlStream.nextTag();
        } else {
            String msg = "Error in surface property element. Expected a 'gml:_Surface' element or an 'xlink:href' attribute.";
            throw new XMLParsingException( xmlStream, msg );
        }
        return surface;
    }

    /**
     * Returns the object representation of an element with type <code>gml:SolidPropertyType</code> (such as
     * <code>gml:solidProperty</code>). Consumes all corresponding events from the given <code>XMLStream</code>.
     * <p>
     * The solid value may be specified using an inline <code>gml:Solid</code> element or using an
     * <code>xlink:href</code> attribute. In the latter case, a {@link SolidReference} object is returned.
     * </p>
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event, points at the corresponding
     *            <code>END_ELEMENT</code> event afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is used if the contained "gml:Solid" has no <code>srsName</code>
     *            attribute
     * @return corresponding {@link Solid} object, this is a {@link SolidReference} if the content is specified using
     *         xlink
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public Solid parseSolidProperty( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        Solid solid = null;
        String href = xmlStream.getAttributeValue( CommonNamespaces.XLNNS, "href" );
        if ( href != null && href.length() > 0 ) {
            LOG.debug( "Found geometry reference (xlink): '" + href + "'" );
            solid = new SolidReference<Solid>( idContext, href, xmlStream.getSystemId() );
            idContext.addReference( (GeometryReference<?>) solid );
            if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
                String msg = "Unexpected element '" + xmlStream.getName()
                             + "'. Solid value has already been specified using xlink.";
                throw new XMLParsingException( xmlStream, msg );
            }
        } else if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
            // must be a 'gml:Solid' element
            if ( !xmlStream.getLocalName().equals( "Solid" ) ) {
                String msg = "Error in point property element. Expected a 'gml:Solid' element.";
                throw new XMLParsingException( xmlStream, msg );
            }
            solid = parseSolid( xmlStream, defaultCRS );
            xmlStream.nextTag();
        } else {
            String msg = "Error in solid property element. Expected a 'gml:Solid' element or an 'xlink:href' attribute.";
            throw new XMLParsingException( xmlStream, msg );
        }
        return solid;
    }

    /**
     * Returns the object representation of an element with type <code>gml:GeometricPrimitivePropertyType</code> (such
     * as <code>gml:element</code>). Consumes all corresponding events from the given <code>XMLStream</code>.
     * <p>
     * The solid value may be specified using an inline <code>gml:_GeometricPrimitive</code> element or using an
     * <code>xlink:href</code> attribute. In the latter case, a {@link GeometricPrimitiveReference} object is returned.
     * </p>
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event, points at the corresponding
     *            <code>END_ELEMENT</code> event afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is used if the contained "gml:_GeometricPrimitive" has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link GeometricPrimitive} object, this is a {@link GeometricPrimitiveReference} if the
     *         content is specified using xlink
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public GeometricPrimitive parseGeometricPrimitiveProperty( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        GeometricPrimitive primitive = null;
        String href = xmlStream.getAttributeValue( CommonNamespaces.XLNNS, "href" );
        if ( href != null && href.length() > 0 ) {
            LOG.debug( "Found geometry reference (xlink): '" + href + "'" );
            primitive = new GeometricPrimitiveReference<GeometricPrimitive>( idContext, href, xmlStream.getSystemId() );
            idContext.addReference( (GeometryReference<?>) primitive );
            if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
                String msg = "Unexpected element '" + xmlStream.getName()
                             + "'. GeometricPrimitive value has already been specified using xlink.";
                throw new XMLParsingException( xmlStream, msg );
            }
        } else if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
            primitive = parseGeometricPrimitive( xmlStream, defaultCRS );
            xmlStream.nextTag();
        } else {
            String msg = "Error in geometric primitive property element. Expected a 'gml:_GeometricPrimiitve' element or an 'xlink:href' attribute.";
            throw new XMLParsingException( xmlStream, msg );
        }
        return primitive;
    }

    /**
     * Returns the object representation of an element with type <code>gml:GeometryPropertyType</code> (such as
     * <code>gml:geometryMember</code>). Consumes all corresponding events from the given <code>XMLStream</code>.
     * <p>
     * The geometry value may be specified using an inline <code>gml:_Geometry</code> element or using an
     * <code>xlink:href</code> attribute. In the latter case, a {@link GeometryReference} object is returned.
     * </p>
     *
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event, points at the corresponding
     *            <code>END_ELEMENT</code> event afterwards
     * @param defaultCRS
     *            default CRS for the geometry, this is used if the contained "gml:_Geometry" has no
     *            <code>srsName</code> attribute
     * @return corresponding {@link Geometry} object, this is a {@link GeometryReference} if the content is specified
     *         using xlink
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public Geometry parseGeometryProperty( XMLStreamReaderWrapper xmlStream, ICRS defaultCRS )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        Geometry geometry = null;
        String href = xmlStream.getAttributeValue( CommonNamespaces.XLNNS, "href" );
        if ( href != null && href.length() > 0 ) {
            LOG.debug( "Found geometry reference (xlink): '" + href + "'" );
            geometry = new GeometryReference<Geometry>( idContext, href, xmlStream.getSystemId() );
            idContext.addReference( (GeometryReference<?>) geometry );
            if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
                String msg = "Unexpected element '" + xmlStream.getName()
                             + "'. Geometry value has already been specified using xlink.";
                throw new XMLParsingException( xmlStream, msg );
            }
        } else if ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT ) {
            geometry = parse( xmlStream, defaultCRS );
            xmlStream.nextTag();
        } else {
            String msg = "Error in geometry property element. Expected a 'gml:_Geometry' element or an 'xlink:href' attribute.";
            throw new XMLParsingException( xmlStream, msg );
        }
        return geometry;
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

        String gid = xmlStream.getAttributeValue( gmlNs, GMLID );
        if ( gid == null ) {
            gid = xmlStream.getAttributeValue( null, GID );
        }

        // Check that the geometry id has the correct form. "gid" and "gml:id" are both based
        // on the XML type "ID": http://www.w3.org/TR/xmlschema11-2/#NCName
        // Thus, they must match the NCName production rule. This means that they may not contain
        // a separating colon (onGMLly at the first position a colon is allowed) and must not
        // start with a digit.
        if ( gid != null && gid.length() > 0 && !gid.matches( "[^\\d][^:]+" ) ) {
            String msg = Messages.getMessage( "GML_INVALID_GEOMETRYID", gid );
            throw new IllegalArgumentException( msg );
        }
        return gid;
    }

    GML3CurveSegmentReader getCurveSegmentReader() {
        return curveSegmentParser;
    }

    GML3SurfacePatchReader getSurfacePatchReader() {
        return surfacePatchParser;
    }

    private AppSchemaGeometryHierarchy getGeometryHierarchy() {
        if ( schema != null ) {
            return schema.getGeometryHierarchy();
        }
        return null;
    }

    private GMLObjectType getType( XMLStreamReader xmlStream ) {

        GMLObjectType type = null;

        if ( schema != null && schema.getGMLSchema() != null ) {
            QName name = xmlStream.getName();
            type = schema.getGeometryType( name );
            if ( type == null ) {
                LOG.debug( "GML geometry element '" + name + "' is not defined in application schema!?" );
            }
        }
        return type;
    }

    private List<Property> readStandardProperties( XMLStreamReaderWrapper xmlStream, GMLObjectType type, ICRS crs )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        List<Property> props = new ArrayList<Property>();
        nextElement( xmlStream );
        while ( xmlStream.isStartElement() && isStandardProperty( xmlStream.getName() ) ) {
            if ( type != null ) {
                QName propName = xmlStream.getName();
                // TODO check order, cardinality and substitutable properties
                PropertyType pt = type.getPropertyDeclaration( propName );
                if ( pt == null ) {
                    String msg = "GML standard property element '" + propName
                                 + "' is not defined in application schema!?";
                    throw new XMLParsingException( xmlStream, msg );
                }
                Property prop = parseProperty( xmlStream, pt, crs );
                props.add( prop );
            } else {
                // handle without schema assistance -> skip
                skipElement( xmlStream );
            }
            nextElement( xmlStream );
        }
        return props;
    }

    private boolean isStandardProperty( QName name ) {
        if ( gmlNs.equals( name.getNamespaceURI() ) ) {
            String localName = name.getLocalPart();
            return "metaDataProperty".equals( localName ) || "description".equals( localName )
                   || "descriptionReference".equals( localName ) || "identifier".equals( localName )
                   || "name".equals( localName );
        }
        return false;
    }

    private List<Property> readAdditionalProperties( XMLStreamReaderWrapper xmlStream, GMLObjectType type, ICRS crs )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        List<Property> props = new ArrayList<Property>();
        if ( type != null ) {
            while ( xmlStream.isStartElement() ) {
                QName propName = xmlStream.getName();
                // TODO cope with order, cardinality and substitutable properties
                PropertyType pt = type.getPropertyDeclaration( propName );
                if ( pt == null ) {
                    String msg = "Geometry property element '" + propName + "' is not defined in application schema.";
                    throw new XMLParsingException( xmlStream, msg );
                }
                props.add( parseProperty( xmlStream, pt, crs ) );
                XMLStreamUtils.nextElement( xmlStream );
            }
        }
        return props;
    }
}
