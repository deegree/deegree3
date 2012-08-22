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
package org.deegree.model.spatialschema;

import static org.deegree.model.spatialschema.GeometryFactory.createCurveSegment;
import static org.deegree.ogcwebservices.wfs.configuration.WFSDeegreeParams.getSwitchAxes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.deegree.crs.coordinatesystems.GeographicCRS;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.filterencoding.Function;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.InvalidGMLException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Adapter class for converting GML geometries to deegree geometries and vice versa. Some logical problems result from
 * the fact that an envelope isn't a geometry according to ISO 19107 (where the deegree geometry model is based on) but
 * according to GML2/3 specification it is.<br>
 * So if the wrap(..) method is called with an envelope a <tt>Surface</tt> will be returned representing the envelops
 * shape. To export an <tt>Envelope</tt> to a GML box/envelope two specialized export methods are available.<BR>
 * The export method(s) doesn't return a DOM element as one may expect but a <tt>StringBuffer</tt>. This is done because
 * the transformation from deegree geometries to GML mainly is required when a GML representation of a geometry shall be
 * serialized to a file or to a network connection. For both cases the string representation is required and it is
 * simply faster to create the string directly instead of first creating a DOM tree that after this must be serialized
 * to a string.<BR>
 * In future version geometries will be serialized to a stream.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GMLGeometryAdapter {

    private static final ILogger LOG = LoggerFactory.getLogger( GMLGeometryAdapter.class );

    private static final NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    private static Map<String, CoordinateSystem> crsMap = new HashMap<String, CoordinateSystem>();

    private static final String COORD = CommonNamespaces.GML_PREFIX + ":coord";

    private static final String COORDINATES = CommonNamespaces.GML_PREFIX + ":coordinates";

    private static final String POS = CommonNamespaces.GML_PREFIX + ":pos";

    private static final String POSLIST = CommonNamespaces.GML_PREFIX + ":posList";

    private static Properties arcProperties;

    /**
     * This differs from GeographiCRS.WGS84 in the identifiers (equals fails for WGS84.equals(EPSG4326)).
     */
    public static CoordinateSystem EPSG4326;

    static {
        try {
            EPSG4326 = CRSFactory.create( "EPSG:4326" );
        } catch ( UnknownCRSException e ) {
            LOG.logDebug( "Unknown error", e );
            EPSG4326 = new org.deegree.model.crs.CoordinateSystem( GeographicCRS.WGS84 );
        }
    }

    static {
        if ( GMLGeometryAdapter.arcProperties == null ) {
            GMLGeometryAdapter.initialize();
        }
    }

    private static void initialize() {

        arcProperties = new Properties();

        try {
            // initialize mappings with mappings from "arc.properties" file in this package
            InputStream is = GMLGeometryAdapter.class.getResourceAsStream( "arc.properties" );
            try {
                arcProperties.load( is );
            } catch ( IOException e ) {
                LOG.logError( e.getMessage(), e );
            }

            // override mappings with mappings from "arc.properties" file in root package
            is = Function.class.getResourceAsStream( "/arc.properties" );
            if ( is != null ) {
                Properties props = new Properties();
                props.load( is );
                Iterator<?> iter = props.keySet().iterator();
                while ( iter.hasNext() ) {
                    String key = (String) iter.next();
                    String value = props.getProperty( key );
                    arcProperties.put( key, value );
                }
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
        }
    }

    /**
     * Returns true if the given localName != <code>null</code> or empty and matches one of the following:
     * <ul>
     * <li>Box (alternative name: Envelope)</li>
     * <li>CompositeSurface</li>
     * <li>Curve</li>
     * <li>LinearRing</li>
     * <li>LineString</li>
     * <li>MultiCurve</li>
     * <li>MultiGeometry</li>
     * <li>MultiLineString</li>
     * <li>MultiPoint</li>
     * <li>MultiPolygon</li>
     * <li>MultiSurface</li>
     * <li>Point (alternative name: Center)</li>
     * <li>Polygon</li>
     * <li>Ring</li>
     * <li>Surface</li>
     * </ul>
     * 
     * @param localName
     *            name to check
     * @return true if localName equals (ignore-case) one of the above strings
     */
    public static boolean isGeometrieSupported( String localName ) {
        if ( localName == null || "".equals( localName.trim() ) ) {
            return false;
        }
        return "Point".equalsIgnoreCase( localName ) || "Center".equalsIgnoreCase( localName )
               || "LineString".equalsIgnoreCase( localName ) || "Polygon".equalsIgnoreCase( localName )
               || "MultiPoint".equalsIgnoreCase( localName ) || "MultiLineString".equalsIgnoreCase( localName )
               || "MultiPolygon".equalsIgnoreCase( localName ) || "Box".equalsIgnoreCase( localName )
               || "Envelope".equalsIgnoreCase( localName ) || "Curve".equalsIgnoreCase( localName )
               || "Surface".equalsIgnoreCase( localName ) || "MultiCurve".equalsIgnoreCase( localName )
               || "MultiSurface".equalsIgnoreCase( localName ) || "CompositeSurface".equalsIgnoreCase( localName )
               || "MultiGeometry".equalsIgnoreCase( localName ) || "LinearRing".equalsIgnoreCase( localName )
               || "Ring".equalsIgnoreCase( localName );
    }

    /**
     * Parses the given DOM element of a GML geometry and returns a corresponding deegree {@link Geometry} object.
     * <p>
     * Notice that GML boxes will be converted to surfaces because in ISO 19107 envelopes are no geometries. Rings are
     * returned as {@link Curve} objects.
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
     * @param element
     *            gml geometry element
     * @param srsName
     *            default SRS for the geometry
     * @return corresponding {@link Geometry} instance
     * @throws GeometryException
     *             if type unsupported or parsing / conversion failed
     */
    public static Geometry wrap( Element element, String srsName )
                            throws GeometryException {

        Geometry geometry = null;
        try {
            String name = element.getLocalName();
            if ( ( name.equals( "Point" ) ) || ( name.equals( "Center" ) ) ) {
                geometry = wrapPoint( element, srsName );
            } else if ( name.equals( "LineString" ) ) {
                geometry = wrapLineString( element, srsName );
            } else if ( name.equals( "Polygon" ) ) {
                geometry = wrapPolygon( element, srsName );
            } else if ( name.equals( "MultiPoint" ) ) {
                geometry = wrapMultiPoint( element, srsName );
            } else if ( name.equals( "MultiLineString" ) ) {
                geometry = wrapMultiLineString( element, srsName );
            } else if ( name.equals( "MultiPolygon" ) ) {
                geometry = wrapMultiPolygon( element, srsName );
            } else if ( name.equals( "Box" ) || name.equals( "Envelope" ) ) {
                geometry = wrapBoxAsSurface( element, srsName );
            } else if ( name.equals( "Curve" ) ) {
                geometry = wrapCurveAsCurve( element, srsName );
            } else if ( name.equals( "Surface" ) ) {
                geometry = wrapSurfaceAsSurface( element, srsName );
            } else if ( name.equals( "MultiCurve" ) ) {
                geometry = wrapMultiCurveAsMultiCurve( element, srsName );
            } else if ( name.equals( "MultiSurface" ) ) {
                geometry = wrapMultiSurfaceAsMultiSurface( element, srsName );
            } else if ( name.equals( "CompositeSurface" ) ) {
                geometry = wrapCompositeSurface( element, srsName );
            } else if ( name.equals( "MultiGeometry" ) ) {
                geometry = wrapMultiGeometry( element, srsName );
            } else if ( name.equals( "LinearRing" ) ) {
                geometry = wrapLinearRing( element, srsName );
            } else if ( name.equals( "Ring" ) ) {
                geometry = wrapRing( element, srsName );
            } else {
                throw new GeometryException( "Not a supported geometry type: " + name );
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new GeometryException( StringTools.stackTraceToString( e ) );
        }

        return geometry;
    }

    /**
     * Converts the given string representation of a GML geometry object to a corresponding deegree {@link Geometry}.
     * Notice that GML Boxes will be converted to Surfaces because in ISO 19107 Envelopes are no geometries.
     * 
     * @see #wrap(Element, String)
     * 
     * @param gml
     * @param srsName
     *            default SRS for the geometry (may be overwritten in geometry elements)
     * @return corresponding geometry object
     * @throws GeometryException
     * @throws XMLParsingException
     */
    public static Geometry wrap( String gml, String srsName )
                            throws GeometryException, XMLParsingException {
        StringReader sr = new StringReader( gml );
        Document doc = null;
        try {
            doc = XMLTools.parse( sr );
        } catch ( Exception e ) {
            LOG.logError( "could not parse: '" + gml + "' as GML/XML", e );
            throw new XMLParsingException( "could not parse: '" + gml + "' as GML/XML: " + e.getMessage() );
        }
        return wrap( doc.getDocumentElement(), srsName );
    }

    /**
     * Returns an instance of {@link Envelope} created from the passed <code>gml:Box</code> or <code>gml:Envelope</code>
     * element.
     * 
     * @param element
     *            <code>gml:Box</code> or <code>gml:Envelope</code> element
     * @param srsName
     *            default SRS for the geometry
     * @return instance of <code>Envelope</code>
     * @throws XMLParsingException
     * @throws InvalidGMLException
     * @throws UnknownCRSException
     */
    public static Envelope wrapBox( Element element, String srsName )
                            throws XMLParsingException, InvalidGMLException, UnknownCRSException {

        srsName = findSrsName( element, srsName );

        boolean swap = swap( srsName );

        Position[] bb = null;
        List<Element> nl = XMLTools.getElements( element, COORD, nsContext );
        if ( nl != null && nl.size() > 0 ) {
            bb = new Position[2];
            bb[0] = createPositionFromCoord( nl.get( 0 ), swap );
            bb[1] = createPositionFromCoord( nl.get( 1 ), swap );
        } else {
            nl = XMLTools.getElements( element, COORDINATES, nsContext );
            if ( nl != null && nl.size() > 0 ) {
                bb = createPositionFromCoordinates( nl.get( 0 ), swap );
            } else {
                nl = XMLTools.getElements( element, POS, nsContext );
                if ( nl != null && nl.size() > 0 ) {
                    bb = new Position[2];
                    bb[0] = createPositionFromPos( nl.get( 0 ), swap );
                    bb[1] = createPositionFromPos( nl.get( 1 ), swap );
                } else {
                    Element lowerCorner = (Element) XMLTools.getRequiredNode( element, "gml:lowerCorner", nsContext );
                    Element upperCorner = (Element) XMLTools.getRequiredNode( element, "gml:upperCorner", nsContext );
                    bb = new Position[2];
                    bb[0] = createPositionFromCorner( lowerCorner, swap );
                    bb[1] = createPositionFromCorner( upperCorner, swap );
                }
            }
        }
        Envelope box = GeometryFactory.createEnvelope( bb[0], bb[1], getCRS( srsName ) );
        return box;
    }

    /**
     * Returns an instance of {@link Curve} created from the passed element.
     * <p>
     * The element must be substitutable for <code>gml:_Curve</code>. Currently, the following concrete elements are
     * supported:
     * <ul>
     * <li><code>gml:Curve</code></li>
     * <li><code>gml:LineString</code>
     * <li>
     * </ul>
     * 
     * @param element
     *            must be substitutable for the abstract element <code>gml:_Curve</code>
     * @param srsName
     * @return curve instance
     * @throws UnknownCRSException
     * @throws GeometryException
     * @throws XMLParsingException
     * @throws InvalidGMLException
     */
    private static Curve wrapAbstractCurve( Element element, String srsName )
                            throws XMLParsingException, GeometryException, UnknownCRSException, InvalidGMLException {
        Curve curve = null;
        String localName = element.getLocalName();
        if ( "Curve".equals( localName ) ) {
            curve = wrapCurveAsCurve( element, srsName );
        } else if ( "LineString".equals( localName ) ) {
            curve = wrapLineString( element, srsName );
        } else {
            String msg = "'" + localName + "' is not a valid or supported substitution for 'gml:_Curve'.";
            throw new XMLParsingException( msg );
        }
        return curve;
    }

    /**
     * Returns an instance of {@link Curve} created from the passed <code>gml:Curve</code> element.
     * 
     * @param element
     *            <code>gml:Curve</code> element
     * @param srsName
     *            default SRS for the geometry
     * @return corresponding Curve instance
     * @throws XMLParsingException
     * @throws GeometryException
     * @throws UnknownCRSException
     */
    public static Curve wrapCurveAsCurve( Element element, String srsName )
                            throws XMLParsingException, GeometryException, UnknownCRSException {

        srsName = findSrsName( element, srsName );
        Element segmentsElement = (Element) XMLTools.getRequiredNode( element, "gml:segments", nsContext );
        ElementList childElements = XMLTools.getChildElements( segmentsElement );
        CurveSegment[] segments = new CurveSegment[childElements.getLength()];
        for ( int i = 0; i < childElements.getLength(); i++ ) {
            segments[i] = parseAbstractCurveSegment( childElements.item( i ), srsName );
        }
        return GeometryFactory.createCurve( segments, getCRS( srsName ) );
    }

    /**
     * Parses the given element as a {@link CurveSegment}.
     * <p>
     * The element must be substitutable for <code>gml:_CurveSegment</code>. Currently, the following concrete elements
     * are supported:
     * <ul>
     * <li><code>gml:Arc</code></li>
     * <li><code>gml:Circle</code>
     * <li><code>gml:LineStringSegment</code>
     * <li>
     * </ul>
     * 
     * @param element
     *            must be substitutable for the abstract element <code>gml:_CurveSegment</code>
     * @param srsName
     * @return curve instance
     * @throws GeometryException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    private static CurveSegment parseAbstractCurveSegment( Element element, String srsName )
                            throws GeometryException, XMLParsingException, UnknownCRSException {
        CurveSegment segment = null;
        String localName = element.getLocalName();
        if ( "Arc".equals( localName ) ) {
            segment = parseArc( element, srsName );
        } else if ( "Circle".equals( localName ) ) {
            segment = parseCircle( element, srsName );
        } else if ( "LineStringSegment".equals( localName ) ) {
            segment = parseLineStringSegment( element, srsName );
        } else {
            String msg = "'" + localName + "' is not a valid or supported substitution for 'gml:_CurveSegment'.";
            throw new GeometryException( msg );
        }
        return segment;
    }

    /**
     * Parses the given <code>gml:Arc</code> element as a {@link CurveSegment}.
     * 
     * @param element
     * @param srsName
     * @return the CurveSegment created from the given element
     * @throws GeometryException
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    private static CurveSegment parseArc( Element element, String srsName )
                            throws GeometryException, XMLParsingException, UnknownCRSException {

        srsName = findSrsName( element, srsName );
        Position[] pos = null;
        try {
            pos = createPositions( element, srsName );
        } catch ( Exception e ) {
            throw new GeometryException( "Error parsing gml:Arc: " + e.getMessage() );
        }
        if ( pos.length != 3 ) {
            throw new GeometryException( "A gml:Arc must be described by exactly three points" );
        }
        int count = Integer.parseInt( arcProperties.getProperty( "defaultNumPoints" ) );
        Position[] linearizedPos = LinearizationUtil.linearizeArc( pos[0], pos[1], pos[2], count );
        return GeometryFactory.createCurveSegment( linearizedPos, getCRS( srsName ) );
    }

    /**
     * Parses the given <code>gml:Circle</code> element as a {@link CurveSegment}.
     * 
     * @param element
     * @param srsName
     * @return the CurveSegment created from the given element
     * @throws GeometryException
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    private static CurveSegment parseCircle( Element element, String srsName )
                            throws GeometryException, XMLParsingException, UnknownCRSException {

        srsName = findSrsName( element, srsName );
        Position[] pos;
        try {
            pos = createPositions( element, srsName );
        } catch ( Exception e ) {
            throw new GeometryException( "Error parsing gml:Circle: " + e.getMessage() );
        }
        if ( pos.length != 3 ) {
            throw new GeometryException( "A gml:Circle element must be described by exactly three points." );
        }
        int count = Integer.parseInt( arcProperties.getProperty( "defaultNumPoints" ) );
        Position[] linearizedPos = LinearizationUtil.linearizeCircle( pos[0], pos[1], pos[2], count );
        return GeometryFactory.createCurveSegment( linearizedPos, getCRS( srsName ) );
    }

    /**
     * Parses the given <code>gml:LineStringSegment</code> element as a {@link CurveSegment}.
     * 
     * @param element
     * @param srsName
     * @return the curve segment created from the given element
     * @throws GeometryException
     * @throws XMLParsingException
     */
    private static CurveSegment parseLineStringSegment( Element element, String srsName )
                            throws GeometryException, XMLParsingException {

        srsName = findSrsName( element, srsName );
        CurveSegment segment = null;
        try {
            Position[] pos = createPositions( element, srsName );
            segment = createCurveSegment( pos, getCRS( srsName ) );
        } catch ( Exception e ) {
            LOG.logError( "Real error:", e );
            throw new GeometryException( "Error parsing LineStringSegment: " + e.getMessage() );
        }
        return segment;
    }

    /**
     * Parses the given element as a {@link Surface}.
     * <p>
     * The element must be substitutable for <code>gml:_Surface</code>. Currently, the following concrete elements are
     * supported:
     * <ul>
     * <li><code>gml:Surface</code></li>
     * <li><code>gml:Polygon</code>
     * <li>
     * </ul>
     * 
     * @param element
     *            must be substitutable for the abstract element <code>gml:_Surface</code>
     * @param srsName
     * @return corresponding surface instance
     * @throws UnknownCRSException
     * @throws GeometryException
     * @throws XMLParsingException
     * @throws InvalidGMLException
     */
    private static Surface wrapAbstractSurface( Element element, String srsName )
                            throws XMLParsingException, GeometryException, UnknownCRSException, InvalidGMLException {
        Surface surface = null;
        String localName = element.getLocalName();
        if ( "Polygon".equals( localName ) ) {
            surface = wrapPolygon( element, srsName );
        } else if ( "Surface".equals( localName ) ) {
            surface = wrapSurfaceAsSurface( element, srsName );
        } else {
            String msg = "'" + localName + "' is not a valid or supported substitution for 'gml:_Surface'.";
            throw new XMLParsingException( msg );
        }
        return surface;
    }

    /**
     * Returns an instance of {@link Surface} created from the passed <code>gml:Surface</code> element.
     * 
     * @param element
     * @param srsName
     *            default SRS for the geometry
     * @return Surface
     * @throws XMLParsingException
     * @throws GeometryException
     * @throws UnknownCRSException
     */
    public static Surface wrapSurfaceAsSurface( Element element, String srsName )
                            throws XMLParsingException, GeometryException, UnknownCRSException {

        srsName = findSrsName( element, srsName );

        Element patches = extractPatches( element );
        List<Element> polygonList = XMLTools.getRequiredElements( patches, "gml:Polygon | gml:PolygonPatch", nsContext );

        SurfacePatch[] surfacePatches = new SurfacePatch[polygonList.size()];

        for ( int i = 0; i < polygonList.size(); i++ ) {
            Curve exteriorRing = null;
            Element polygon = polygonList.get( i );
            try {
                Element exterior = (Element) XMLTools.getNode( polygon, "gml:exterior | gml:outerBounderyIs", nsContext );
                if ( exterior != null ) {
                    exteriorRing = parseRing( srsName, exterior );
                } else {
                    String msg = Messages.getMessage( "GEOM_SURFACE_NO_EXTERIOR_RING" );
                    throw new XMLParsingException( msg );
                }

                List<Element> interiorList = XMLTools.getElements( polygon, "gml:interior | gml:outerBounderyIs",
                                                                   nsContext );
                Curve[] interiorRings = null;
                if ( interiorList != null && interiorList.size() > 0 ) {

                    interiorRings = new Curve[interiorList.size()];

                    for ( int j = 0; j < interiorRings.length; j++ ) {
                        Element interior = interiorList.get( j );
                        interiorRings[j] = parseRing( srsName, interior );
                    }
                }
                surfacePatches[i] = GeometryFactory.createSurfacePatch( exteriorRing, interiorRings, getCRS( srsName ) );
            } catch ( InvalidGMLException e ) {
                LOG.logError( e.getMessage(), e );
                throw new XMLParsingException( "Error parsing the polygon element '" + polygon.getNodeName()
                                               + "' to create a surface geometry." );
            }

        }
        Surface surface = null;
        try {
            surface = GeometryFactory.createSurface( surfacePatches, getCRS( srsName ) );
        } catch ( GeometryException e ) {
            throw new GeometryException( "Error creating a surface from '" + surfacePatches.length + "' polygons." );
        }
        return surface;
    }

    /**
     * Determines the relevant srsName attribute for a geometry element.
     * <p>
     * Strategy:
     * <nl>
     * <li>Check if the geometry element has a srsName attribute, or any ancestor. If an srsName is found this way, it
     * is used.</li>
     * <li>If no srsName was found, but an srsName was given as parameter to the method, it is returned.</li>
     * <li>If no srsName was found *and* no srsName was given as parameter to the method, the whole DOM tree is searched
     * for for an srsName attribute.</li>
     * </nl>
     * 
     * @param element
     *            geometry element
     * @param srsName
     *            default value that is used when the element (or an ancestor element) does not have an own srsName
     *            attribute
     * @return the srs name
     * @throws XMLParsingException
     */
    private static String findSrsName( Element element, String srsName )
                            throws XMLParsingException {

        Node elem = element;
        String tmp = null;
        while ( tmp == null && elem != null ) {
            tmp = XMLTools.getNodeAsString( elem, "@srsName", nsContext, null );
            elem = elem.getParentNode();
        }

        if ( tmp != null ) {
            srsName = tmp;
        } else if ( srsName == null ) {
            // TODO do this is a better way!!!
            srsName = XMLTools.getNodeAsString( element, "//@srsName", nsContext, null );
        }
        return srsName;
    }

    /**
     * Returns the {@link CoordinateSystem} corresponding to the passed crs name.
     * 
     * @param name
     *            name of the crs or null (not specified)
     * @return CoordinateSystem corresponding to the given crs name or null if name is null
     * @throws UnknownCRSException
     */
    private static CoordinateSystem getCRS( String name )
                            throws UnknownCRSException {

        if ( name == null ) {
            return null;
        }

        if ( name.length() > 2 ) {
            if ( name.startsWith( "http://www.opengis.net/gml/srs/" ) ) {
                // as declared in the GML 2.1.1 specification
                // http://www.opengis.net/gml/srs/epsg.xml#4326
                int p = name.lastIndexOf( "/" );

                if ( p >= 0 ) {
                    name = name.substring( p, name.length() );
                    p = name.indexOf( "." );

                    String s1 = name.substring( 1, p ).toUpperCase();
                    p = name.indexOf( "#" );

                    String s2 = name.substring( p + 1, name.length() );
                    name = s1 + ":" + s2;
                }
            }
        }

        CoordinateSystem crs = crsMap.get( name );
        if ( crs == null ) {
            crs = CRSFactory.create( name );
            crsMap.put( name, crs );
        }
        return crs;
    }

    /**
     * Parses a ring element; this may be a gml:LinearRing or a gml:Ring.
     * 
     * @param srsName
     * @param parent
     *            parent of a gml:LinearRing or gml:Ring
     * @return curves of a ring
     * @throws XMLParsingException
     * @throws InvalidGMLException
     * @throws GeometryException
     * @throws UnknownCRSException
     */
    private static Curve parseRing( String srsName, Element parent )
                            throws XMLParsingException, InvalidGMLException, GeometryException, UnknownCRSException {

        List<CurveSegment> curveMembers = null;
        Element ring = (Element) XMLTools.getNode( parent, "gml:LinearRing", nsContext );
        if ( ring != null ) {
            Position[] exteriorRing = createPositions( ring, srsName );
            curveMembers = new ArrayList<CurveSegment>();
            curveMembers.add( GeometryFactory.createCurveSegment( exteriorRing, getCRS( srsName ) ) );
        } else {
            List<Node> members = XMLTools.getRequiredNodes( parent, "gml:Ring/gml:curveMember/child::*", nsContext );
            curveMembers = new ArrayList<CurveSegment>( members.size() );
            for ( Node node : members ) {
                Curve curve = (Curve) wrap( (Element) node, srsName );
                CurveSegment[] tmp = curve.getCurveSegments();
                for ( int i = 0; i < tmp.length; i++ ) {
                    curveMembers.add( tmp[i] );
                }
            }
        }
        CurveSegment[] cs = curveMembers.toArray( new CurveSegment[curveMembers.size()] );
        return GeometryFactory.createCurve( cs );
    }

    /**
     * Returns a {@link Point} instance created from the passed <code>gml:Point</code> element.
     * 
     * @param element
     *            <code>gml:Point</code> element
     * @param srsName
     *            default SRS for the geometry
     * @return instance of Point
     * @throws XMLParsingException
     * @throws InvalidGMLException
     * @throws UnknownCRSException
     */
    public static Point wrapPoint( Element element, String srsName )
                            throws XMLParsingException, InvalidGMLException, UnknownCRSException {

        srsName = findSrsName( element, srsName );

        boolean swap = swap( srsName );

        Position[] bb = null;
        List<Element> nl = XMLTools.getElements( element, COORD, nsContext );
        if ( nl != null && nl.size() > 0 ) {
            bb = new Position[1];
            bb[0] = createPositionFromCoord( nl.get( 0 ), swap );
        } else {
            nl = XMLTools.getElements( element, COORDINATES, nsContext );
            if ( nl != null && nl.size() > 0 ) {
                bb = createPositionFromCoordinates( nl.get( 0 ), swap );
            } else {
                nl = XMLTools.getElements( element, POS, nsContext );
                bb = new Position[1];
                bb[0] = createPositionFromPos( nl.get( 0 ), swap );
            }
        }
        return GeometryFactory.createPoint( bb[0], getCRS( srsName ) );
    }

    /**
     * Returns a {@link Curve} instance created from the passed <code>gml:LineString</code> element.
     * 
     * @param element
     *            <code>gml:LineString</code> element
     * @param srsName
     *            default SRS for the geometry
     * @return instance of Curve
     * @throws XMLParsingException
     * @throws GeometryException
     * @throws InvalidGMLException
     * @throws UnknownCRSException
     */
    public static Curve wrapLineString( Element element, String srsName )
                            throws XMLParsingException, GeometryException, InvalidGMLException, UnknownCRSException {

        srsName = findSrsName( element, srsName );
        Position[] pos = createPositions( element, srsName );
        return GeometryFactory.createCurve( pos, getCRS( srsName ) );
    }

    /**
     * Parses the given <code>gml:Polygon</code> element as a {@link Surface}.
     * 
     * @param element
     *            <code>gml:Polygon</code> element
     * @param srsName
     *            default SRS for the geometry
     * @return corresponding Surface instance
     * @throws XMLParsingException
     * @throws GeometryException
     * @throws InvalidGMLException
     * @throws UnknownCRSException
     */
    public static Surface wrapPolygon( Element element, String srsName )
                            throws XMLParsingException, GeometryException, InvalidGMLException, UnknownCRSException {

        srsName = findSrsName( element, srsName );

        // get positions for outer boundary
        Element exteriorRingElement = XMLTools.getRequiredElement( element, "gml:exterior/*|gml:outerBoundaryIs/*",
                                                                   nsContext );
        Curve exteriorRing = wrapAbstractRing( exteriorRingElement, srsName );
        Position[] exteriorPositions = getCurvePositions( exteriorRing );

        // get positions for inner boundaries
        List<Element> interiorRingElements = XMLTools.getElements( element, "gml:interior/*|gml:innerBoundaryIs/*",
                                                                   nsContext );
        Position[][] interiorPositions = new Position[interiorRingElements.size()][];
        for ( int i = 0; i < interiorPositions.length; i++ ) {
            Curve interiorRing = wrapAbstractRing( interiorRingElements.get( i ), srsName );
            interiorPositions[i] = getCurvePositions( interiorRing );
        }

        SurfaceInterpolation si = new SurfaceInterpolationImpl();
        return GeometryFactory.createSurface( exteriorPositions, interiorPositions, si, getCRS( srsName ) );
    }

    private static Position[] getCurvePositions( Curve curve )
                            throws GeometryException {
        Position[] positions = null;
        CurveSegment[] segments = curve.getCurveSegments();
        if ( segments.length == 1 ) {
            positions = segments[0].getPositions();
        } else {
            int numPositions = 0;
            for ( int i = 0; i < segments.length; i++ ) {
                numPositions += segments[i].getNumberOfPoints();
            }
            positions = new Position[numPositions];
            int positionId = 0;
            for ( int i = 0; i < segments.length; i++ ) {
                Position[] segmentPositions = segments[i].getPositions();
                for ( int j = 0; j < segmentPositions.length; j++ ) {
                    positions[positionId++] = segmentPositions[j];
                }
            }
        }
        return positions;
    }

    /**
     * Returns an instance of {@link Curve} created from the passed element.
     * <p>
     * The element must be substitutable for <code>gml:_Ring</code>. Currently, the following concrete elements are
     * supported:
     * <ul>
     * <li><code>gml:LinearRing</code></li>
     * <li><code>gml:Ring</code>
     * <li>
     * </ul>
     * 
     * @param element
     *            must be substitutable for the abstract element <code>gml:_Ring</code>
     * @param srsName
     * @return curve instance
     * @throws UnknownCRSException
     * @throws GeometryException
     * @throws XMLParsingException
     * @throws InvalidGMLException
     */
    private static Curve wrapAbstractRing( Element element, String srsName )
                            throws XMLParsingException, GeometryException, UnknownCRSException, InvalidGMLException {
        Curve curve = null;
        String localName = element.getLocalName();
        if ( "LinearRing".equals( localName ) ) {
            curve = wrapLinearRing( element, srsName );
        } else if ( "Ring".equals( localName ) ) {
            curve = wrapRing( element, srsName );
        } else {
            String msg = "'" + localName + "' is not a valid or supported substitution for 'gml:_Ring'.";
            throw new XMLParsingException( msg );
        }
        return curve;
    }

    /**
     * Parses the given <code>gml:LinearRing</code> element as a {@link Curve}.
     * 
     * @param element
     * @param srsName
     * @return a polygon containing the ring
     * @throws XMLParsingException
     * @throws GeometryException
     * @throws InvalidGMLException
     * @throws UnknownCRSException
     */
    public static Curve wrapLinearRing( Element element, String srsName )
                            throws XMLParsingException, GeometryException, InvalidGMLException, UnknownCRSException {

        srsName = findSrsName( element, srsName );
        Position[] pos = createPositions( element, srsName );
        return GeometryFactory.createCurve( pos, getCRS( srsName ) );
    }

    /**
     * Parses the given <code>gml:Ring</code> element as a {@link Curve}.
     * 
     * @param element
     *            <code>gml:Ring</code> element
     * @param srsName
     *            default SRS for the geometry
     * @return corresponding Curve instance
     * @throws XMLParsingException
     * @throws GeometryException
     * @throws UnknownCRSException
     * @throws InvalidGMLException
     */
    public static Curve wrapRing( Element element, String srsName )
                            throws XMLParsingException, GeometryException, UnknownCRSException, InvalidGMLException {

        srsName = findSrsName( element, srsName );
        Element curveElement = (Element) XMLTools.getRequiredNode( element, "gml:curveMember/*", nsContext );
        return wrapAbstractCurve( curveElement, srsName );
    }

    /**
     * Returns a {@link MultiPoint} instance created from the passed <code>gml:MultiPoint</code> element.
     * 
     * @param element
     *            <code>gml:MultiPoint</code> element
     * @param srsName
     *            default SRS for the geometry
     * @return instance of MultiPoint
     * @throws XMLParsingException
     * @throws InvalidGMLException
     * @throws UnknownCRSException
     */
    public static MultiPoint wrapMultiPoint( Element element, String srsName )
                            throws XMLParsingException, InvalidGMLException, UnknownCRSException {

        srsName = findSrsName( element, srsName );

        // gml:pointMember
        List<Point> pointList = new ArrayList<Point>();
        List<Element> listPointMember = XMLTools.getElements( element, "gml:pointMember", nsContext );
        if ( listPointMember != null ) {
            for ( int i = 0; i < listPointMember.size(); i++ ) {
                Element pointMember = listPointMember.get( i );
                Element point = XMLTools.getElement( pointMember, "gml:Point", nsContext );
                pointList.add( wrapPoint( point, srsName ) );
            }
        }

        // gml:pointMembers
        Element pointMembers = XMLTools.getElement( element, "gml:pointMembers", nsContext );
        if ( pointMembers != null ) {
            List<Element> pointElems = XMLTools.getElements( pointMembers, "gml:Point", nsContext );
            for ( int j = 0; j < pointElems.size(); j++ ) {
                pointList.add( wrapPoint( pointElems.get( j ), srsName ) );
            }
        }

        Point[] points = new Point[pointList.size()];
        return GeometryFactory.createMultiPoint( pointList.toArray( points ), getCRS( srsName ) );
    }

    /**
     * Returns a {@link MultiCurve} instance created from the passed <code>gml:MultiLineString</code> element.
     * 
     * @param element
     *            <code>gml:MultiLineString</code> element
     * @param srsName
     *            default SRS for the geometry
     * @return instance of MultiCurve
     * @throws XMLParsingException
     * @throws GeometryException
     * @throws InvalidGMLException
     * @throws UnknownCRSException
     */
    public static MultiCurve wrapMultiLineString( Element element, String srsName )
                            throws XMLParsingException, GeometryException, InvalidGMLException, UnknownCRSException {

        srsName = findSrsName( element, srsName );

        // ElementList el = XMLTools.getChildElements( "lineStringMember", CommonNamespaces.GMLNS,
        // element );
        List<Element> el = XMLTools.getElements( element, CommonNamespaces.GML_PREFIX + ":lineStringMember", nsContext );
        Curve[] curves = new Curve[el.size()];
        for ( int i = 0; i < curves.length; i++ ) {
            curves[i] = wrapLineString( XMLTools.getFirstChildElement( el.get( i ) ), srsName );
        }
        return GeometryFactory.createMultiCurve( curves, getCRS( srsName ) );
    }

    /**
     * Parses the given <code>gml:MultiCurve</code> element as a {@link MultiCurve}.
     * 
     * @param element
     *            <code>gml:MultiCurve</code> element
     * @param srsName
     *            default SRS for the geometry
     * @return corresponding <code>MultiCurve</code> instance
     * @throws XMLParsingException
     * @throws GeometryException
     * @throws UnknownCRSException
     * @throws InvalidGMLException
     */
    public static MultiCurve wrapMultiCurveAsMultiCurve( Element element, String srsName )
                            throws XMLParsingException, GeometryException, UnknownCRSException, InvalidGMLException {

        srsName = findSrsName( element, srsName );

        // gml:curveMember
        List<Element> curveMemberElements = XMLTools.getElements( element, "gml:curveMember", nsContext );
        List<Curve> curves = new ArrayList<Curve>();
        if ( curveMemberElements.size() > 0 ) {
            for ( int i = 0; i < curveMemberElements.size(); i++ ) {
                Element curveMemberElement = curveMemberElements.get( i );
                Element curveElement = XMLTools.getFirstChildElement( curveMemberElement );
                Curve curve = wrapAbstractCurve( curveElement, srsName );
                curves.add( curve );
            }
        }
        // gml:curveMembers
        Element curveMembersElement = (Element) XMLTools.getNode( element, "gml:curveMembers", nsContext );
        if ( curveMembersElement != null ) {
            ElementList curveElements = XMLTools.getChildElements( curveMembersElement );
            for ( int i = 0; i < curveElements.getLength(); i++ ) {
                Curve curve = wrapAbstractCurve( curveElements.item( i ), srsName );
                curves.add( curve );
            }
        }
        return GeometryFactory.createMultiCurve( curves.toArray( new Curve[curves.size()] ), getCRS( srsName ) );
    }

    /**
     * Parses the given <code>gml:MultiSurface</code> element as a {@link MultiSurface}.
     * 
     * @param element
     *            <code>gml:MultiSurface</code> element
     * @param srsName
     *            default SRS for the geometry
     * @return corresponding <code>MultiSurface</code> instance
     * @throws XMLParsingException
     * @throws GeometryException
     * @throws UnknownCRSException
     * @throws InvalidGMLException
     */
    public static MultiSurface wrapMultiSurfaceAsMultiSurface( Element element, String srsName )
                            throws XMLParsingException, GeometryException, UnknownCRSException, InvalidGMLException {

        srsName = findSrsName( element, srsName );

        // gml:surfaceMember
        List<Element> surfaceMemberElements = XMLTools.getElements( element, "gml:surfaceMember", nsContext );
        List<Surface> surfaces = new ArrayList<Surface>();
        if ( surfaceMemberElements.size() > 0 ) {
            for ( int i = 0; i < surfaceMemberElements.size(); i++ ) {
                Element surfaceMemberElement = surfaceMemberElements.get( i );
                Element surfaceElement = XMLTools.getFirstChildElement( surfaceMemberElement );
                Surface surface = wrapAbstractSurface( surfaceElement, srsName );
                surfaces.add( surface );
            }
        }
        // gml:surfaceMembers
        Element surfaceMembersElement = (Element) XMLTools.getNode( element, "gml:surfaceMembers", nsContext );
        if ( surfaceMembersElement != null ) {
            ElementList surfaceElements = XMLTools.getChildElements( surfaceMembersElement );
            for ( int i = 0; i < surfaceElements.getLength(); i++ ) {
                Surface surface = wrapAbstractSurface( surfaceElements.item( i ), srsName );
                surfaces.add( surface );
            }
        }
        return GeometryFactory.createMultiSurface( surfaces.toArray( new Surface[surfaces.size()] ), getCRS( srsName ) );
    }

    /**
     * Returns a {@link MultiSurface} instance created from the passed <code>gml:MultiPolygon</code> element.
     * 
     * @param element
     *            <code>gml:MultiPolygon</code> element
     * @param srsName
     *            default SRS for the geometry
     * @return instance of MultiCurve
     * 
     * @throws XMLParsingException
     * @throws GeometryException
     * @throws InvalidGMLException
     * @throws UnknownCRSException
     */
    public static MultiSurface wrapMultiPolygon( Element element, String srsName )
                            throws XMLParsingException, GeometryException, InvalidGMLException, UnknownCRSException {

        srsName = findSrsName( element, srsName );

        // ElementList el = XMLTools.getChildElements( "polygonMember", CommonNamespaces.GMLNS,
        // element );
        List<Element> el = XMLTools.getElements( element, CommonNamespaces.GML_PREFIX + ":polygonMember", nsContext );
        Surface[] surfaces = new Surface[el.size()];
        for ( int i = 0; i < surfaces.length; i++ ) {
            surfaces[i] = wrapPolygon( XMLTools.getFirstChildElement( el.get( i ) ), srsName );
        }
        return GeometryFactory.createMultiSurface( surfaces, getCRS( srsName ) );
    }

    /**
     * Creates an instance of {@link MultiGeometry} from the passed <code>gml:MultiGeometry</code> element.
     * 
     * @param element
     *            <code>gml:MultiGeometry</code> element
     * @param srsName
     *            default SRS for the geometry
     * @return MultiSurface
     * @throws XMLParsingException
     * @throws GeometryException
     * @throws UnknownCRSException
     * @throws GeometryException
     */
    public static MultiGeometry wrapMultiGeometry( Element element, String srsName )
                            throws XMLParsingException, UnknownCRSException, GeometryException {

        srsName = findSrsName( element, srsName );
        List<Geometry> memberGeometries = new ArrayList<Geometry>();

        // gml:geometryMember
        List<Element> geometryMemberElements = XMLTools.getElements( element, "gml:geometryMember", nsContext );
        if ( geometryMemberElements != null ) {
            for ( Element geometryMemberElement : geometryMemberElements ) {
                Element geometryElement = XMLTools.getFirstChildElement( geometryMemberElement );
                memberGeometries.add( wrap( geometryElement, srsName ) );
            }
        }

        // gml:geometryMembers
        Element surfaceMembers = (Element) XMLTools.getNode( element, "gml:geometryMembers", nsContext );
        if ( surfaceMembers != null ) {
            ElementList geometryElements = XMLTools.getChildElements( surfaceMembers );
            for ( int i = 0; i < geometryElements.getLength(); i++ ) {
                Element geometryElement = geometryElements.item( i );
                memberGeometries.add( wrap( geometryElement, srsName ) );
            }
        }
        return GeometryFactory.createMultiGeometry( memberGeometries.toArray( new Geometry[memberGeometries.size()] ),
                                                    getCRS( srsName ) );
    }

    /**
     * Returns a <code>Surface</code> created from the given <code>gml:Box</code> element. This method is useful because
     * an Envelope that would normally be created from a Box isn't a geometry in context of ISO 19107.
     * 
     * @param element
     *            <code>gml:Box</code> element
     * @param srsName
     * @return instance of <code>Surface</code>
     * @throws XMLParsingException
     * @throws GeometryException
     * @throws InvalidGMLException
     * @throws UnknownCRSException
     */
    public static Surface wrapBoxAsSurface( Element element, String srsName )
                            throws XMLParsingException, GeometryException, InvalidGMLException, UnknownCRSException {
        Envelope env = wrapBox( element, srsName );
        return GeometryFactory.createSurface( env, env.getCoordinateSystem() );
    }

    /**
     * Returns an instance of {@link CompositeSurface} created from the passed <code>gml:CompositeSurface</code>
     * element.
     * 
     * TODO
     * 
     * @param element
     * @param srsName
     *            default SRS for the geometry
     * @return CompositeSurface
     */
    public static CompositeSurface wrapCompositeSurface( Element element, String srsName ) {
        throw new UnsupportedOperationException(
                                                 "#wrapCompositeSurface(Element) is not implemented as yet. Work in Progress." );
    }

    /**
     * Extract the <gml:patches> node from a <gml:Surface> element.
     * 
     * @param surface
     * @return Element
     * @throws XMLParsingException
     */
    private static Element extractPatches( Element surface )
                            throws XMLParsingException {
        Element patches = null;
        try {
            patches = (Element) XMLTools.getRequiredNode( surface, "gml:patches", nsContext );
        } catch ( XMLParsingException e ) {
            throw new XMLParsingException( "Error retrieving the patches element from the surface element." );
        }
        return patches;
    }

    private static Position createPositionFromCorner( Element corner, boolean swap )
                            throws InvalidGMLException {

        String tmp = XMLTools.getAttrValue( corner, null, "dimension", null );
        int dim = 0;
        if ( tmp != null ) {
            dim = Integer.parseInt( tmp );
        }
        tmp = XMLTools.getStringValue( corner );
        double[] vals = StringTools.toArrayDouble( tmp, ", " );
        if ( dim != 0 ) {
            if ( vals.length != dim ) {
                throw new InvalidGMLException( "dimension must be equal to the number of coordinate values defined "
                                               + "in pos element." );
            }
        } else {
            dim = vals.length;
        }

        Position pos = null;
        if ( dim == 3 ) {
            pos = GeometryFactory.createPosition( vals[0], vals[1], vals[2] );
        } else {
            pos = GeometryFactory.createPosition( vals[swap ? 1 : 0], vals[swap ? 0 : 1] );
        }

        return pos;

    }

    /**
     * returns an instance of Position created from the passed coord
     * 
     * @param element
     *            &lt;coord&gt;
     * 
     * @return instance of <tt>Position</tt>
     * 
     * @throws XMLParsingException
     */
    private static Position createPositionFromCoord( Element element, boolean swap )
                            throws XMLParsingException {

        Position pos = null;
        // Element elem = XMLTools.getRequiredChildElement( "X", CommonNamespaces.GMLNS, element );
        Element elem = XMLTools.getRequiredElement( element, CommonNamespaces.GML_PREFIX + ":X", nsContext );
        double x = Double.parseDouble( XMLTools.getStringValue( elem ) );
        // elem = XMLTools.getRequiredChildElement( "Y", CommonNamespaces.GMLNS, element );
        elem = XMLTools.getRequiredElement( element, CommonNamespaces.GML_PREFIX + ":Y", nsContext );
        double y = Double.parseDouble( XMLTools.getStringValue( elem ) );
        // elem = XMLTools.getChildElement( "Z", CommonNamespaces.GMLNS, element );
        elem = XMLTools.getElement( element, CommonNamespaces.GML_PREFIX + ":Z", nsContext );

        if ( elem != null ) {
            double z = Double.parseDouble( XMLTools.getStringValue( elem ) );
            // no swapping for 3D
            pos = GeometryFactory.createPosition( new double[] { x, y, z } );
        } else {
            pos = GeometryFactory.createPosition( new double[] { swap ? y : x, swap ? x : y } );
        }
        return pos;
    }

    /**
     * returns an array of Positions created from the passed coordinates
     * 
     * @param element
     *            <coordinates>
     * 
     * @return instance of <tt>Position[]</tt>
     */
    private static Position[] createPositionFromCoordinates( Element element, boolean swap ) {

        Position[] points = null;
        // fixing the failure coming from the usage of the xmltools.getAttrib method
        String ts = XMLTools.getAttrValue( element, null, "ts", " " );

        // not used because javas current decimal seperator will be used
        // String ds = XMLTools.getAttrValue( element, null, "decimal", "." );
        String cs = XMLTools.getAttrValue( element, null, "cs", "," );

        String value = XMLTools.getStringValue( element ).trim();

        // first tokenizer, tokens the tuples
        StringTokenizer tuple = new StringTokenizer( value, ts );
        points = new Position[tuple.countTokens()];
        int i = 0;
        while ( tuple.hasMoreTokens() ) {
            String s = tuple.nextToken();
            // second tokenizer, tokens the coordinates
            StringTokenizer coort = new StringTokenizer( s, cs );
            double[] p = new double[coort.countTokens()];

            int idx;
            for ( int k = 0; k < p.length; k++ ) {
                idx = swap ? ( k % 2 == 0 ? k + 1 : k - 1 ) : k;
                s = coort.nextToken();
                p[idx] = Double.parseDouble( s );
            }

            points[i++] = GeometryFactory.createPosition( p );
        }

        return points;
    }

    /**
     * creates a <tt>Point</tt> from the passed <pos> element containing a GML pos.
     * 
     * @param element
     * @return created <tt>Point</tt>
     * @throws InvalidGMLException
     */
    private static Position createPositionFromPos( Element element, boolean swap )
                            throws InvalidGMLException {

        String tmp = XMLTools.getAttrValue( element, null, "dimension", null );
        int dim = 0;
        if ( tmp != null ) {
            dim = Integer.parseInt( tmp );
        } else {
            tmp = element.getAttribute( "srsDimension" );
            if ( tmp != null && !"".equals( tmp.trim() ) ) {
                dim = Integer.parseInt( tmp );
            }
        }
        tmp = XMLTools.getStringValue( element );
        double[] vals = StringTools.toArrayDouble( tmp, "\t\n\r\f ," );
        if ( vals != null ) {
            if ( dim != 0 ) {
                if ( vals.length != dim ) {
                    throw new InvalidGMLException(
                                                   "The dimension of a position must be equal to the number of coordinate values defined in the pos element." );
                }
            } else {
                dim = vals.length;
            }
        } else {
            throw new InvalidGMLException( "The given element {" + element.getNamespaceURI() + "}"
                                           + element.getLocalName()
                                           + " does not contain any coordinates, this may not be!" );
        }

        Position pos = null;
        if ( dim == 3 ) {
            // TODO again I guess no swapping for 3D
            pos = GeometryFactory.createPosition( vals[0], vals[1], vals[2] );
        } else {
            pos = GeometryFactory.createPosition( vals[swap ? 1 : 0], vals[swap ? 0 : 1] );
        }

        return pos;
    }

    /**
     * 
     * @param element
     * @return Position
     * @throws InvalidGMLException
     * @throws XMLParsingException
     */
    private static Position[] createPositionFromPosList( Element element, String srsName )
                            throws InvalidGMLException, XMLParsingException {

        srsName = findSrsName( element, srsName );
        String srsDimension = XMLTools.getAttrValue( element, null, "srsDimension", null );
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            XMLFragment doc = new XMLFragment( element );
            LOG.logDebug( doc.getAsPrettyString() );
        }

        boolean swap = swap( srsName );

        int dim = 0;
        if ( srsDimension != null ) {
            dim = Integer.parseInt( srsDimension );
        }

        if ( dim == 0 ) {
            if ( srsName == null ) {
                dim = 2;
            } else {
                try {
                    dim = CRSFactory.create( srsName ).getCRS().getDimension();
                } catch ( UnknownCRSException e ) {
                    LOG.logError( e.getMessage(), e );
                    dim = 2;
                }
            }
        }

        String axisLabels = XMLTools.getAttrValue( element, null, "gml:axisAbbrev", null );
        String uomLabels = XMLTools.getAttrValue( element, null, "uomLabels", null );

        if ( srsName == null ) {
            if ( srsDimension != null ) {
                String msg = "Attribute srsDimension cannot be defined unless attribute srsName has been defined.";
                throw new InvalidGMLException( msg );
            }
            if ( axisLabels != null ) {
                String msg = "Attribute axisLabels cannot be defined unless attribute srsName has been defined.";
                throw new InvalidGMLException( msg );
            }
        }
        if ( axisLabels == null ) {
            if ( uomLabels != null ) {
                String msg = "Attribute uomLabels cannot be defined unless attribute axisLabels has been defined.";
                throw new InvalidGMLException( msg );
            }
        }
        String tmp = XMLTools.getStringValue( element );
        double[] values = StringTools.toArrayDouble( tmp, "\t\n\r\f ," );
        int size = values.length / dim;
        LOG.logDebug( "Number of points = ", size );
        LOG.logDebug( "Size of the original array: ", values.length );
        LOG.logDebug( "Dimension: ", dim );

        if ( values.length < 4 ) {
            if ( values.length == 2 ) {
                values = new double[] { values[swap ? 1 : 0], values[swap ? 0 : 1], values[swap ? 1 : 0],
                                       values[swap ? 0 : 1] };
                size = 2;
            } else {
                for ( double d : values ) {
                    System.out.println( "value: " + d );
                }
                throw new InvalidGMLException( "A point list must have minimum 2 coordinate tuples. Here only '" + size
                                               + "' are defined." );
            }
        }
        double positions[][] = new double[size][dim];
        int a = 0, b = 0;
        for ( int i = 0; i < values.length; i++ ) {
            if ( b == dim ) {
                a++;
                b = 0;
            }
            positions[a][b] = values[i];
            b++;
        }

        Position[] position = new Position[positions.length];
        for ( int i = 0; i < positions.length; i++ ) {
            double[] vals = positions[i];
            if ( dim == 3 ) {
                // TODO no swapping for 3D I guess?
                position[i] = GeometryFactory.createPosition( vals[0], vals[1], vals[2] );
            } else {
                position[i] = GeometryFactory.createPosition( vals[swap ? 1 : 0], vals[swap ? 0 : 1] );
            }
        }
        return position;
    }

    /**
     * creates an array of <tt>Position</tt>s from the <coordinates> or <pos> Elements located as children under the
     * passed parent element.
     * <p>
     * example:<br>
     * 
     * <pre>
     * 
     * &lt;gml:Box&gt; &lt;gml:coordinates cs=&quot;,&quot; decimal=&quot;.&quot; ts=&quot; &quot;&gt;0,0
     * 4000,4000&lt;/gml:coordinates&gt; &lt;/gml:Box&gt;
     * 
     * </pre>
     * 
     * </p>
     * 
     * @param parent
     * @param srsName
     * @return the positions of the given element.
     * @throws XMLParsingException
     * @throws InvalidGMLException
     */
    private static Position[] createPositions( Element parent, String srsName )
                            throws XMLParsingException, InvalidGMLException {

        srsName = findSrsName( parent, srsName );

        boolean swap = swap( srsName );

        List<Element> nl = XMLTools.getElements( parent, COORDINATES, nsContext );
        Position[] pos = null;
        if ( nl != null && nl.size() > 0 ) {
            pos = createPositionFromCoordinates( nl.get( 0 ), swap );
        } else {
            nl = XMLTools.getElements( parent, POS, nsContext );
            if ( nl != null && nl.size() > 0 ) {
                pos = new Position[nl.size()];
                for ( int i = 0; i < pos.length; i++ ) {
                    pos[i] = createPositionFromPos( nl.get( i ), swap );
                }
            } else {
                Element posList = (Element) XMLTools.getRequiredNode( parent, POSLIST, nsContext );
                if ( posList != null ) {
                    pos = createPositionFromPosList( posList, srsName );
                }
            }
        }
        return pos;
    }

    /**
     * Writes the GML representation of the given {@link Geometry} to the given {@link OutputStream}.
     * <p>
     * Currently, the {@link Geometry} realizations are handled:
     * <ul>
     * <li>SurfacePatch
     * <li>LineString
     * <li>Point
     * <li>Curve
     * <li>Surface
     * <li>MultiPoint
     * <li>MultiCurve
     * <li>MultiSurface
     * <li>MultiGeometry
     * </ul>
     * 
     * @param geometry
     *            geometry to be exported
     * @param os
     *            target {@link OutputStream}
     * @return a {@link PrintWriter} created from the {@link OutputStream}
     * @throws GeometryException
     */
    public static PrintWriter export( Geometry geometry, OutputStream os )
                            throws GeometryException {
        return export( geometry, new PrintWriter( os ) );
    }

    /**
     * @param geometry
     * @param os
     * @param id
     * @return a {@link PrintWriter} created from the {@link OutputStream}
     * @throws GeometryException
     */
    public static PrintWriter export( Geometry geometry, OutputStream os, String id )
                            throws GeometryException {
        return export( geometry, new PrintWriter( os ), id );
    }

    /**
     * Writes the GML representation of the given {@link Geometry} to the given {@link PrintWriter}.
     * <p>
     * Currently, the {@link Geometry} realizations are handled:
     * <ul>
     * <li>SurfacePatch
     * <li>LineString
     * <li>Point
     * <li>Curve
     * <li>Surface
     * <li>MultiPoint
     * <li>MultiCurve
     * <li>MultiSurface
     * <li>MultiGeometry
     * </ul>
     * 
     * @param geometry
     *            geometry to be exported
     * @param pw
     *            target {@link PrintWriter}
     * @return same as input {@link PrintWriter}
     * @throws GeometryException
     */
    public static PrintWriter export( Geometry geometry, PrintWriter pw )
                            throws GeometryException {
        return export( geometry, pw, null );
    }

    /**
     * @param geometry
     * @param pw
     * @param id
     * @return same as input {@link PrintWriter}
     * @throws GeometryException
     */
    public static PrintWriter export( Geometry geometry, PrintWriter pw, String id )
                            throws GeometryException {
        if ( geometry instanceof SurfacePatch ) {
            geometry = new SurfaceImpl( (SurfacePatch) geometry );
        } else if ( geometry instanceof LineString ) {
            geometry = new CurveImpl( (LineString) geometry );
        }
        if ( id == null ) {
            id = "";
        } else {
            id = "gml:id='" + id + "'";
        }

        if ( geometry instanceof Point ) {
            exportPoint( (Point) geometry, pw, id );
        } else if ( geometry instanceof Curve ) {
            exportCurve( (Curve) geometry, pw, id );
        } else if ( geometry instanceof Surface ) {
            exportSurface( (Surface) geometry, pw, id );
        } else if ( geometry instanceof MultiPoint ) {
            exportMultiPoint( (MultiPoint) geometry, pw, id );
        } else if ( geometry instanceof MultiCurve ) {
            exportMultiCurve( (MultiCurve) geometry, pw, id );
        } else if ( geometry instanceof MultiSurface ) {
            exportMultiSurface( (MultiSurface) geometry, pw, id );
        } else if ( geometry instanceof MultiGeometry ) {
            exportMultiGeometry( (MultiGeometry) geometry, pw, id );
        }
        pw.flush();
        return pw;
    }

    /**
     * Creates a GML representation from the passed <code>Geometry</code>.
     * 
     * @param geometry
     * @return a string buffer containing the XML
     * @throws GeometryException
     */
    public static StringBuffer export( Geometry geometry )
                            throws GeometryException {
        return export( geometry, (String) null );
    }

    /**
     * @param geometry
     * @param id
     * @return a string buffer containing the XML
     * @throws GeometryException
     */
    public static StringBuffer export( Geometry geometry, String id )
                            throws GeometryException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream( 5000 );
        export( geometry, bos, id );
        return new StringBuffer( new String( bos.toByteArray() ) );
    }

    /**
     * creates a GML representation from the passed <tt>Envelope</tt>. This method is required because in ISO 19107
     * Envelops are no geometries.
     * 
     * @param envelope
     * @return the stringbuffer filled with the envelope.
     */
    public static StringBuffer exportAsBox( Envelope envelope ) {

        StringBuffer sb = new StringBuffer( "<gml:Box xmlns:gml='http://www.opengis.net/gml'" );

        if ( envelope.getCoordinateSystem() != null ) {
            sb.append( " srsName='" ).append( envelope.getCoordinateSystem().getIdentifier() ).append( "'" );
        }
        sb.append( ">" );

        boolean swap = swap( envelope );

        sb.append( "<gml:coordinates cs=\",\" decimal=\".\" ts=\" \">" );
        sb.append( swap ? envelope.getMin().getY() : envelope.getMin().getX() ).append( ',' );
        sb.append( swap ? envelope.getMin().getX() : envelope.getMin().getY() );
        int dim = envelope.getMax().getCoordinateDimension();
        if ( dim == 3 ) {
            sb.append( ',' ).append( envelope.getMin().getZ() );
        }
        sb.append( ' ' ).append( swap ? envelope.getMax().getY() : envelope.getMax().getX() );
        sb.append( ',' ).append( swap ? envelope.getMax().getX() : envelope.getMax().getY() );
        if ( dim == 3 ) {
            sb.append( ',' ).append( envelope.getMax().getZ() );
        }
        sb.append( "</gml:coordinates></gml:Box>" );

        return sb;
    }

    /**
     * creates a GML representation from the passed <tt>Envelope</tt>. This method is required because in ISO 19107
     * Envelops are no geometries.
     * 
     * @param envelope
     * @return the String representation of the given envelope
     */
    public static StringBuffer exportAsEnvelope( Envelope envelope ) {

        StringBuffer sb = new StringBuffer( "<gml:Envelope " );
        if ( envelope.getCoordinateSystem() != null ) {
            sb.append( "srsName='" ).append( envelope.getCoordinateSystem().getIdentifier() ).append( "' " );
        }

        boolean swap = swap( envelope );

        sb.append( "xmlns:gml='http://www.opengis.net/gml'>" );
        sb.append( "<gml:coordinates cs=\",\" decimal=\".\" ts=\" \">" );
        sb.append( swap ? envelope.getMin().getY() : envelope.getMin().getX() ).append( ',' );
        sb.append( swap ? envelope.getMin().getX() : envelope.getMin().getY() );
        int dim = envelope.getMax().getCoordinateDimension();
        if ( dim == 3 ) {
            sb.append( ',' ).append( envelope.getMin().getZ() );
        }
        sb.append( ' ' ).append( swap ? envelope.getMax().getY() : envelope.getMax().getX() );
        sb.append( ',' ).append( swap ? envelope.getMax().getX() : envelope.getMax().getY() );
        if ( dim == 3 ) {
            sb.append( ',' ).append( envelope.getMax().getZ() );
        }
        sb.append( "</gml:coordinates></gml:Envelope>" );

        return sb;
    }

    /**
     * creates a GML expression of a point geometry
     * 
     * @param point
     *            point geometry
     * 
     */
    private static void exportPoint( Point point, PrintWriter pw, String id ) {

        String crs = null;
        if ( point.getCoordinateSystem() != null ) {
            crs = point.getCoordinateSystem().getIdentifier();
        }

        boolean swap = swap( point );

        String srs = null;
        if ( crs != null ) {
            srs = "<gml:Point srsName=\"" + crs + "\" " + id + ">";
        } else {
            srs = "<gml:Point " + id + ">";
        }
        pw.println( srs );
        pw.print( appendPos( point.getPosition(), point.getCoordinateSystem(), swap ) );
        pw.print( "</gml:Point>" );

    }

    /**
     * creates a GML expression of a curve geometry
     * 
     * @param o
     *            curve geometry
     * 
     * 
     * @throws GeometryException
     */
    private static void exportCurve( Curve o, PrintWriter pw, String id )
                            throws GeometryException {

        String crs = null;
        if ( o.getCoordinateSystem() != null ) {
            crs = o.getCoordinateSystem().getIdentifier();
        }

        boolean swap = swap( o );

        String srs = null;
        if ( crs != null ) {
            srs = "<gml:Curve srsName=\"" + crs + "\" " + id + ">";
        } else {
            srs = "<gml:Curve " + id + ">";
        }
        pw.println( srs );
        pw.println( "<gml:segments>" );

        int curveSegments = o.getNumberOfCurveSegments();
        for ( int i = 0; i < curveSegments; i++ ) {
            pw.print( "<gml:LineStringSegment>" );
            CurveSegment segment = o.getCurveSegmentAt( i );
            Position[] p = segment.getAsLineString().getPositions();
            pw.print( appendPosList( p, o.getCoordinateDimension(), swap ) );
            pw.println( "</gml:LineStringSegment>" );
        }
        pw.println( "</gml:segments>" );
        pw.print( "</gml:Curve>" );

    }

    private static StringBuilder appendPosList( Position[] p, int coordinateDimension, boolean swap ) {
        StringBuilder sb = new StringBuilder();
        if ( p != null && p.length > 0 ) {
            sb.append( "<gml:posList" );
            if ( coordinateDimension > 0 ) {
                sb.append( " srsDimension='" ).append( coordinateDimension );
                sb.append( "' count='" ).append( p.length ).append( "'" );
            }
            sb.append( ">" );
            for ( int i = 0; i < p.length; ++i ) {
                sb.append( swap ? ( p[i].getY() + " " + p[i].getX() ) : ( p[i].getX() + " " + p[i].getY() ) );
                if ( coordinateDimension == 3 ) {
                    sb.append( " " ).append( p[i].getZ() );
                }
                if ( i < p.length - 1 ) {
                    sb.append( " " );
                }
            }
            sb.append( "</gml:posList>" );
        }
        return sb;
    }

    private static StringBuilder appendPos( Position pos, CoordinateSystem crs, boolean swap ) {
        StringBuilder sb = new StringBuilder();
        if ( pos != null ) {
            sb.append( "<gml:pos" );
            int dimension = pos.getCoordinateDimension();
            if ( dimension > 0 ) {
                if ( crs != null ) {
                    int tmp = crs.getDimension();
                    if ( dimension != tmp ) {
                        sb.append( " srsDimension='" ).append( dimension ).append( "'" );
                        sb.append( " srsName='" ).append( crs.getIdentifier() ).append( "'" );
                    }
                }
            }
            sb.append( ">" );
            sb.append( swap ? ( pos.getY() + " " + pos.getX() ) : ( pos.getX() + " " + pos.getY() ) );
            if ( dimension == 3 ) {
                sb.append( " " ).append( pos.getZ() );
            }
            sb.append( "</gml:pos>" );
        }
        return sb;
    }

    /**
     * @throws GeometryException
     */
    private static void exportSurface( Surface surface, PrintWriter pw, String id )
                            throws GeometryException {

        String crs = null;
        if ( surface.getCoordinateSystem() != null ) {
            crs = surface.getCoordinateSystem().getIdentifier().replace( ' ', ':' );
        }

        boolean swap = swap( surface );

        String srs = null;
        if ( crs != null ) {
            srs = "<gml:Surface srsName='" + crs + "\' " + id + ">";
        } else {
            srs = "<gml:Surface " + id + ">";
        }
        pw.println( srs );
        int patches = surface.getNumberOfSurfacePatches();
        pw.println( "<gml:patches>" );
        for ( int i = 0; i < patches; i++ ) {
            pw.println( "<gml:PolygonPatch>" );
            SurfacePatch patch = surface.getSurfacePatchAt( i );
            printExteriorRing( surface, pw, patch, swap );
            printInteriorRing( surface, pw, patch, swap );
            pw.println( "</gml:PolygonPatch>" );
        }
        pw.println( "</gml:patches>" );
        pw.print( "</gml:Surface>" );

    }

    /**
     * @param surface
     * @param pw
     * @param patch
     */
    private static void printInteriorRing( Surface surface, PrintWriter pw, SurfacePatch patch, boolean swap ) {
        // interior rings
        Position[][] ip = patch.getInteriorRings();
        if ( ip != null ) {
            for ( int j = 0; j < ip.length; j++ ) {
                pw.println( "<gml:interior>" );
                pw.println( "<gml:LinearRing>" );
                if ( surface.getCoordinateSystem() != null ) {
                    pw.print( appendPosList( ip[j], surface.getCoordinateDimension(), swap ) );
                } else {
                    pw.print( appendPosList( ip[j], 0, swap ) );
                }
                pw.println( "</gml:LinearRing>" );
                pw.println( "</gml:interior>" );
            }
        }
    }

    /**
     * @param surface
     * @param pw
     * @param patch
     */
    private static void printExteriorRing( Surface surface, PrintWriter pw, SurfacePatch patch, boolean swap ) {
        // exterior ring
        pw.print( "<gml:exterior>" );
        pw.print( "<gml:LinearRing>" );
        if ( surface.getCoordinateSystem() != null ) {
            pw.print( appendPosList( patch.getExteriorRing(), surface.getCoordinateDimension(), swap ) );
        } else {
            pw.print( appendPosList( patch.getExteriorRing(), 0, swap ) );
        }
        pw.print( "</gml:LinearRing>" );
        pw.print( "</gml:exterior>" );
    }

    /**
     * @param mp
     */
    private static void exportMultiPoint( MultiPoint mp, PrintWriter pw, String id ) {

        String crs = null;
        if ( mp.getCoordinateSystem() != null ) {
            crs = mp.getCoordinateSystem().getIdentifier();
        }

        boolean swap = swap( mp );

        String srs = null;
        if ( crs != null ) {
            srs = "<gml:MultiPoint srsName=\"" + crs + "\" " + id + ">";
        } else {
            srs = "<gml:MultiPoint " + id + ">";
        }
        pw.println( srs );
        pw.println( "<gml:pointMembers>" );
        for ( int i = 0; i < mp.getSize(); i++ ) {
            pw.println( "<gml:Point>" );
            pw.print( appendPos( mp.getPointAt( i ).getPosition(), mp.getCoordinateSystem(), swap ) );
            pw.println( "</gml:Point>" );
        }
        pw.println( "</gml:pointMembers>" );
        pw.print( "</gml:MultiPoint>" );

    }

    /**
     * @param multiCurve
     * @throws GeometryException
     */
    private static void exportMultiCurve( MultiCurve multiCurve, PrintWriter pw, String id )
                            throws GeometryException {

        String crs = null;
        if ( multiCurve.getCoordinateSystem() != null ) {
            crs = multiCurve.getCoordinateSystem().getIdentifier().replace( ' ', ':' );
        }

        boolean swap = swap( multiCurve );

        String srs = null;
        if ( crs != null ) {
            srs = "<gml:MultiCurve srsName=\"" + crs + "\" " + id + ">";
        } else {
            srs = "<gml:MultiCurve " + id + ">";
        }
        pw.println( srs );

        Curve[] curves = multiCurve.getAllCurves();
        pw.println( "<gml:curveMembers>" );
        for ( int i = 0; i < curves.length; i++ ) {
            Curve curve = curves[i];
            pw.println( "<gml:Curve>" );
            pw.println( "<gml:segments>" );
            int numberCurveSegments = curve.getNumberOfCurveSegments();
            for ( int j = 0; j < numberCurveSegments; j++ ) {
                pw.println( "<gml:LineStringSegment>" );
                CurveSegment curveSegment = curve.getCurveSegmentAt( j );
                Position[] p = curveSegment.getAsLineString().getPositions();
                pw.print( appendPosList( p, curve.getCoordinateDimension(), swap ) );
                pw.println( "</gml:LineStringSegment>" );
            }
            pw.println( "</gml:segments>" );
            pw.println( "</gml:Curve>" );
        }
        pw.println( "</gml:curveMembers>" );
        pw.print( "</gml:MultiCurve>" );
    }

    /**
     * @param multiSurface
     * @throws GeometryException
     */
    private static void exportMultiSurface( MultiSurface multiSurface, PrintWriter pw, String id )
                            throws GeometryException {

        String crs = null;
        if ( multiSurface.getCoordinateSystem() != null ) {
            crs = multiSurface.getCoordinateSystem().getIdentifier().replace( ' ', ':' );
        }
        String srs = null;
        if ( crs != null ) {
            srs = "<gml:MultiSurface srsName=\"" + crs + "\" " + id + ">";
        } else {
            srs = "<gml:MultiSurface " + id + ">";
        }
        pw.println( srs );

        Surface[] surfaces = multiSurface.getAllSurfaces();

        pw.println( "<gml:surfaceMembers>" );
        for ( int i = 0; i < surfaces.length; i++ ) {
            Surface surface = surfaces[i];
            exportSurface( surface, pw, "" );
        }
        pw.println( "</gml:surfaceMembers>" );
        // substitution as requested in issue
        // http://wald.intevation.org/tracker/index.php?func=detail&aid=477&group_id=27&atid=212
        // can be removed if it was inserted correctly
        // pw.println( "<gml:surfaceMembers>" );
        // for ( int i = 0; i < surfaces.length; i++ ) {
        // Surface surface = surfaces[i];
        // pw.println( "<gml:Surface>" );
        // pw.println( "<gml:patches>" );
        // pw.println( "<gml:Polygon>" );
        // int numberSurfaces = surface.getNumberOfSurfacePatches();
        // for ( int j = 0; j < numberSurfaces; j++ ) {
        // SurfacePatch surfacePatch = surface.getSurfacePatchAt( j );
        // printExteriorRing( surface, pw, surfacePatch );
        // printInteriorRing( surface, pw, surfacePatch );
        // }
        // pw.println( "</gml:Polygon>" );
        // pw.println( "</gml:patches>" );
        // pw.println( "</gml:Surface>" );
        // }
        // pw.println( "</gml:surfaceMembers>" );
        pw.print( "</gml:MultiSurface>" );

    }

    /**
     * Exports the given {@link MultiGeometry} as a <code>gml:MultiGeometry</code> element.
     * 
     * @param multiGeometry
     * @param pw
     * @throws GeometryException
     */
    public static void exportMultiGeometry( MultiGeometry multiGeometry, PrintWriter pw )
                            throws GeometryException {
        exportMultiGeometry( multiGeometry, pw, null );
    }

    /**
     * Exports the given {@link MultiGeometry} as a <code>gml:MultiGeometry</code> element.
     * 
     * @param multiGeometry
     * @param pw
     * @param id
     * @throws GeometryException
     */
    public static void exportMultiGeometry( MultiGeometry multiGeometry, PrintWriter pw, String id )
                            throws GeometryException {

        String crs = null;
        if ( multiGeometry.getCoordinateSystem() != null ) {
            crs = multiGeometry.getCoordinateSystem().getIdentifier().replace( ' ', ':' );
        }

        // opening tag
        if ( crs != null ) {
            pw.print( "<gml:MultiGeometry srsName=\"" + crs + "\" " + id + ">" );
        } else {
            pw.print( "<gml:MultiGeometry " + id + ">" );
        }

        Geometry[] memberGeometries = multiGeometry.getAll();
        if ( memberGeometries.length > 0 ) {
            pw.print( "<gml:geometryMembers>" );
            for ( Geometry geometry : memberGeometries ) {
                export( geometry, pw );
            }
            pw.print( "</gml:geometryMembers>" );
        }
        pw.print( "</gml:MultiGeometry>" );

    }

    // helpers that determine whether to swap x/y coordinates
    private static boolean swap( Geometry geom ) {
        return ( geom.getCoordinateSystem() == null || geom.getCoordinateSystem().equals( EPSG4326 ) )
               && getSwitchAxes();
    }

    /**
     * @param env
     * @return true, if configuration and environment say yes to swapping
     */
    public static boolean swap( Envelope env ) {
        return ( env.getCoordinateSystem() == null || env.getCoordinateSystem().equals( EPSG4326 ) ) && getSwitchAxes();
    }

    private static boolean swap( String srsName ) {
        // TODO use real crs instance just to verify the name? Too expensive?
        return ( srsName == null || srsName.contains( "4326" ) ) && getSwitchAxes();
    }

}
