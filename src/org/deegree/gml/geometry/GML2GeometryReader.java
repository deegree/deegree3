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
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.i18n.Messages;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.gml.GMLDocumentIdContext;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.geometry.refs.GeometryReference;
import org.deegree.gml.geometry.refs.LineStringReference;
import org.deegree.gml.geometry.refs.PointReference;
import org.deegree.gml.geometry.refs.PolygonReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GML2GeometryReader implements GMLGeometryReader {

    private static Logger LOG = LoggerFactory.getLogger( GML2GeometryReader.class );

    private static String GID = "gid";

    private static final String GML21NS = CommonNamespaces.GMLNS;

    private static final QName GML_X = new QName( GML21NS, "X" );

    private static final QName GML_Y = new QName( GML21NS, "Y" );

    private static final QName GML_Z = new QName( GML21NS, "Z" );

    private GeometryFactory geomFac;

    private GMLDocumentIdContext idContext;

    /**
     * 
     */
    public GML2GeometryReader() {
        this( new GeometryFactory(), new GMLDocumentIdContext( GMLVersion.GML_2 ) );
    }

    /**
     * @param geomFac
     * @param idContext
     */
    public GML2GeometryReader( GeometryFactory geomFac, GMLDocumentIdContext idContext ) {
        this.geomFac = geomFac;
        this.idContext = idContext;
    }

    /**
     * Returns the {@link GMLDocumentIdContext} that keeps track of objects, identifieres and references.
     * 
     * @return the {@link GMLDocumentIdContext}, never <code>null</code>
     */
    public GMLDocumentIdContext getDocumentIdContext() {
        return idContext;
    }

    /**
     * @param xmlStream
     * @return
     * @throws XMLStreamException
     */
    @Override
    public Geometry parse( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {
        return parse( xmlStream, null );
    }

    /**
     * @param xmlStream
     * @param defaultCRS
     * @return
     * @throws XMLStreamException
     */
    public Geometry parse( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException {
        Geometry geometry = null;

        if ( !GML21NS.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Invalid gml:_Geometry element: " + xmlStream.getName()
                         + "' is not a GML geometry element. Not in the gml namespace.";
            throw new XMLParsingException( xmlStream, msg );
        }

        String name = xmlStream.getLocalName();
        if ( name.equals( "Point" ) ) {
            geometry = parsePoint( xmlStream, defaultCRS );
        } else if ( name.equals( "Polygon" ) ) {
            geometry = parsePolygon( xmlStream, defaultCRS );
        } else if ( name.equals( "LinearRing" ) ) {
            geometry = parseLinearRing( xmlStream, defaultCRS );
        } else if ( name.equals( "LineString" ) ) {
            geometry = parseLineString( xmlStream, defaultCRS );
        } else if ( name.equals( "Box" ) ) {
            geometry = parseEnvelope( xmlStream, defaultCRS );
        } else if ( name.equals( "MultiGeometry" ) ) {
            geometry = parseMultiGeometry( xmlStream, defaultCRS );
        } else if ( name.equals( "MultiPoint" ) ) {
            geometry = parseMultiPoint( xmlStream, defaultCRS );
        } else if ( name.equals( "MultiLineString" ) ) {
            geometry = parseMultiLineString( xmlStream, defaultCRS );
        } else if ( name.equals( "MultiPolygon" ) ) {
            geometry = parseMultiPolygon( xmlStream, defaultCRS );
        } else {
            String msg = "Invalid GML geometry: '" + xmlStream.getName()
                         + "' does not denote a GML 3.1.1 geometry element.";
            throw new XMLParsingException( xmlStream, msg );
        }
        return geometry;

    }

    /**
     * @param xmlStream
     * @return
     * @throws XMLParsingException
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    public Geometry parseGeometryOrBox( XMLStreamReaderWrapper xmlStream )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {
        return parseGeometryOrBox( xmlStream, null );
    }

    /**
     * @param xmlStream
     * @param defaultCRS
     * @return
     * @throws XMLParsingException
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    public Geometry parseGeometryOrBox( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        Geometry geometry = null;

        if ( !GMLNS.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Unexpected element: " + xmlStream.getName()
                         + "' is not a GML geometry or envelope element. Not in the gml namespace.";
            throw new XMLParsingException( xmlStream, msg );
        }

        if ( "Box".equals( xmlStream.getLocalName() ) ) {
            geometry = parseEnvelope( xmlStream, defaultCRS );
        } else {
            geometry = parse( xmlStream, defaultCRS );
        }
        return geometry;
    }

    /**
     * @param xmlStream
     * @return
     * @throws XMLStreamException
     */
    public MultiPolygon parseMultiPolygon( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {
        return parseMultiPolygon( xmlStream, null );
    }

    /**
     * @param xmlStream
     * @param defaultCRS
     * @return
     * @throws XMLStreamException
     */
    public MultiPolygon parseMultiPolygon( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException {
        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );
        xmlStream.nextTag();

        List<Polygon> members = new LinkedList<Polygon>();

        if ( xmlStream.isStartElement() ) {
            do {
                String localName = xmlStream.getLocalName();
                if ( localName.equals( "polygonMember" ) ) {
                    members.add( parsePolygonProperty( xmlStream, crs ) );
                    xmlStream.require( END_ELEMENT, GML21NS, "polygonMember" );
                } else {
                    String msg = "Invalid 'gml:MultiPolygon' element: unexpected element '" + localName
                                 + "'. Expected 'polygonMember'.";
                    throw new XMLParsingException( xmlStream, msg );
                }
            } while ( xmlStream.nextTag() == START_ELEMENT );
        }
        xmlStream.require( END_ELEMENT, GML21NS, "MultiPolygon" );
        MultiPolygon multiPolygon = geomFac.createMultiPolygon( gid, crs, members );
        idContext.addObject( multiPolygon );
        return multiPolygon;
    }

    private Polygon parsePolygonProperty( XMLStreamReaderWrapper xmlStream, CRS crs )
                            throws XMLStreamException {
        Polygon polygon = null;
        String href = xmlStream.getAttributeValue( CommonNamespaces.XLNNS, "href" );
        if ( href != null && href.length() > 0 ) {
            LOG.debug( "Found geometry reference (xlink): '" + href + "'" );
            polygon = new PolygonReference( idContext, href, xmlStream.getSystemId() );
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
            polygon = parsePolygon( xmlStream, crs );
            xmlStream.nextTag();
        } else {
            String msg = "Error in Polygon property element. Expected a 'gml:Polygon' element or an 'xlink:href' attribute.";
            throw new XMLParsingException( xmlStream, msg );
        }
        return polygon;
    }

    /**
     * @param xmlStream
     * @return
     * @throws XMLStreamException
     */
    public MultiLineString parseMultiLineString( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {
        return parseMultiLineString( xmlStream, null );
    }

    /**
     * @param xmlStream
     * @param defaultCRS
     * @return
     * @throws XMLStreamException
     */
    public MultiLineString parseMultiLineString( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException {
        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );
        xmlStream.nextTag();

        List<LineString> members = new LinkedList<LineString>();

        if ( xmlStream.isStartElement() ) {
            do {
                String localName = xmlStream.getLocalName();
                if ( localName.equals( "lineStringMember" ) ) {
                    members.add( parseLineStringProperty( xmlStream, crs ) );
                    xmlStream.require( END_ELEMENT, GML21NS, "lineStringMember" );
                } else {
                    String msg = "Invalid 'gml:MultiLineString' element: unexpected element '" + localName
                                 + "'. Expected 'lineStringMember'.";
                    throw new XMLParsingException( xmlStream, msg );
                }
            } while ( xmlStream.nextTag() == START_ELEMENT );
        }

        xmlStream.require( END_ELEMENT, GML21NS, "MultiLineString" );
        MultiLineString multiLineString = geomFac.createMultiLineString( gid, crs, members );
        idContext.addObject( multiLineString );
        return multiLineString;
    }

    private LineString parseLineStringProperty( XMLStreamReaderWrapper xmlStream, CRS crs )
                            throws XMLStreamException {
        LineString lineString = null;
        String href = xmlStream.getAttributeValue( CommonNamespaces.XLNNS, "href" );
        if ( href != null && href.length() > 0 ) {
            LOG.debug( "Found geometry reference (xlink): '" + href + "'" );
            lineString = new LineStringReference( idContext, href, xmlStream.getSystemId() );
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
            lineString = parseLineString( xmlStream, crs );
            xmlStream.nextTag();
        } else {
            String msg = "Error in LineString property element. Expected a 'gml:LineString' element or an 'xlink:href' attribute.";
            throw new XMLParsingException( xmlStream, msg );
        }
        return lineString;
    }

    /**
     * @param xmlStream
     * @return
     * @throws XMLStreamException
     */
    public MultiPoint parseMultiPoint( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {
        return parseMultiPoint( xmlStream, null );
    }

    /**
     * @param xmlStream
     * @param defaultCRS
     * @return
     * @throws XMLStreamException
     */
    public MultiPoint parseMultiPoint( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException {
        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );
        xmlStream.nextTag();

        List<Point> members = new LinkedList<Point>();

        if ( xmlStream.isStartElement() ) {
            do {
                String localName = xmlStream.getLocalName();
                if ( localName.equals( "pointMember" ) ) {
                    members.add( parsePointProperty( xmlStream, crs ) );
                    xmlStream.require( END_ELEMENT, GML21NS, "pointMember" );
                } else {
                    String msg = "Invalid 'gml:MultiPoint' element: unexpected element '" + localName
                                 + "'. Expected 'pointMember' ";
                    throw new XMLParsingException( xmlStream, msg );
                }
            } while ( xmlStream.nextTag() == START_ELEMENT );
        }
        xmlStream.require( END_ELEMENT, GML21NS, "MultiPoint" );
        MultiPoint multiPoint = geomFac.createMultiPoint( gid, crs, members );
        idContext.addObject( multiPoint );
        return multiPoint;
    }

    private Point parsePointProperty( XMLStreamReaderWrapper xmlStream, CRS crs )
                            throws XMLStreamException {
        Point point = null;
        String href = xmlStream.getAttributeValue( CommonNamespaces.XLNNS, "href" );
        if ( href != null && href.length() > 0 ) {
            LOG.debug( "Found geometry reference (xlink): '" + href + "'" );
            point = new PointReference( idContext, href, xmlStream.getSystemId() );
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
            point = parsePoint( xmlStream, crs );
            xmlStream.nextTag();
        } else {
            String msg = "Error in point property element. Expected a 'gml:Point' element or an 'xlink:href' attribute.";
            throw new XMLParsingException( xmlStream, msg );
        }
        return point;
    }

    /**
     * @param xmlStream
     * @return
     * @throws XMLStreamException
     */
    public MultiGeometry<Geometry> parseMultiGeometry( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {
        return parseMultiGeometry( xmlStream, null );
    }

    /**
     * @param xmlStream
     * @param defaultCRS
     * @return
     * @throws XMLStreamException
     */
    public MultiGeometry<Geometry> parseMultiGeometry( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException {
        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );
        xmlStream.nextTag();

        List<Geometry> members = new LinkedList<Geometry>();

        if ( xmlStream.isStartElement() ) {
            do {
                String localName = xmlStream.getLocalName();
                if ( localName.equals( "geometryMember" ) ) {
                    members.add( parseGeometryProperty( xmlStream, crs ) );
                    xmlStream.require( END_ELEMENT, GML21NS, "geometryMember" );
                } else {
                    String msg = "Invalid 'gml:MultiGeometry' element: unexpected element '" + localName
                                 + "'. Expected 'geometryMember'.";
                    throw new XMLParsingException( xmlStream, msg );
                }
            } while ( xmlStream.nextTag() == START_ELEMENT );
        }
        xmlStream.require( END_ELEMENT, GML21NS, "MultiGeometry" );
        MultiGeometry<Geometry> multiGeometry = geomFac.createMultiGeometry( gid, crs, members );
        idContext.addObject( multiGeometry );
        return multiGeometry;
    }

    private Geometry parseGeometryProperty( XMLStreamReaderWrapper xmlStream, CRS crs )
                            throws XMLStreamException {
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
            geometry = parse( xmlStream, crs );
            xmlStream.nextTag();
        } else {
            String msg = "Error in geometry property element. Expected a 'gml:_Geometry' element or an 'xlink:href' attribute.";
            throw new XMLParsingException( xmlStream, msg );
        }
        return geometry;
    }

    /**
     * @param xmlStream
     * @return
     * @throws XMLStreamException
     */
    public Envelope parseEnvelope( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {
        return parseEnvelope( xmlStream, null );
    }

    /**
     * @param xmlStream
     * @param defaultCRS
     * @return
     * @throws XMLStreamException
     */
    public Envelope parseEnvelope( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException {
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );
        xmlStream.nextTag();

        List<Point> points = null;
        if ( xmlStream.getEventType() == XMLStreamConstants.START_ELEMENT ) {
            String name = xmlStream.getLocalName();
            if ( "coordinates".equals( name ) ) {
                points = parseCoordinates( xmlStream, crs );
                xmlStream.nextTag();
            } else {
                points = new LinkedList<Point>();
                do {
                    if ( "coord".equals( name ) ) {
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

        if ( points == null || points.size() != 2 ) {
            String msg = "Error in 'gml:Box' element. Must consist of exactly two points.";
            throw new XMLParsingException( xmlStream, msg );
        }

        Envelope envelope = geomFac.createEnvelope( points.get( 0 ).getAsArray(), points.get( 1 ).getAsArray(), crs );
        idContext.addObject( envelope );
        return envelope;
    }

    /**
     * @param xmlStream
     * @return
     * @throws XMLStreamException
     */
    public LineString parseLineString( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {
        return parseLineString( xmlStream, null );
    }

    /**
     * @param xmlStream
     * @param defaultCRS
     * @return
     * @throws XMLStreamException
     */
    public LineString parseLineString( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException {
        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );
        xmlStream.nextTag();

        List<Point> points = null;
        if ( xmlStream.getEventType() == XMLStreamConstants.START_ELEMENT ) {
            String name = xmlStream.getLocalName();
            if ( "coordinates".equals( name ) ) {
                points = parseCoordinates( xmlStream, crs );
                xmlStream.nextTag();
            } else {
                points = new LinkedList<Point>();
                do {
                    if ( "coord".equals( name ) ) {
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

        if ( points == null || points.size() < 2 ) {
            String msg = "Error in 'gml:LineString' element. Must consist of two points at least.";
            throw new XMLParsingException( xmlStream, msg );
        }
        LineString lineString = geomFac.createLineString( gid, crs, geomFac.createPoints( points ) );
        idContext.addObject( lineString );
        return lineString;
    }

    /**
     * @param xmlStream
     * @return
     * @throws XMLStreamException
     */
    public Polygon parsePolygon( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {
        return parsePolygon( xmlStream, null );
    }

    /**
     * @param xmlStream
     * @param defaultCRS
     * @return
     * @throws XMLStreamException
     */
    public Polygon parsePolygon( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException {
        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );
        xmlStream.nextTag();

        Ring exteriorRing = null;
        List<Ring> interiorRings = new LinkedList<Ring>();

        if ( xmlStream.getEventType() == START_ELEMENT ) {
            if ( xmlStream.getLocalName().equals( "outerBoundaryIs" ) ) {
                if ( xmlStream.nextTag() != START_ELEMENT ) {
                    String msg = "Error in 'gml:Polygon' element. Expected a 'gml:LinearRing' element.";
                    throw new XMLParsingException( xmlStream, msg );
                }
                exteriorRing = parseLinearRing( xmlStream, crs );
                xmlStream.nextTag();
                xmlStream.require( END_ELEMENT, GML21NS, "outerBoundaryIs" );
                xmlStream.nextTag();
            }
        }

        // arbitrary number of interior/innerBoundaryIs elements
        while ( xmlStream.getEventType() == START_ELEMENT ) {
            if ( xmlStream.getLocalName().equals( "innerBoundaryIs" ) ) {
                if ( xmlStream.nextTag() != START_ELEMENT ) {
                    String msg = "Error in 'gml:Polygon' element. Expected a 'gml:LinearRing' element.";
                    throw new XMLParsingException( xmlStream, msg );
                }
                interiorRings.add( parseLinearRing( xmlStream, crs ) );
                xmlStream.nextTag();
                xmlStream.require( END_ELEMENT, GML21NS, "innerBoundaryIs" );
            } else {
                String msg = "Error in 'gml:Polygon' element. Expected a 'gml:innerBoundaryIs' element, but found: '"
                             + xmlStream.getName() + "'.";
                throw new XMLParsingException( xmlStream, msg );
            }
            xmlStream.nextTag();
        }
        xmlStream.require( END_ELEMENT, GML21NS, "Polygon" );
        Polygon polygon = geomFac.createPolygon( gid, crs, exteriorRing, interiorRings );
        idContext.addObject( polygon );
        return polygon;

    }

    /**
     * @param xmlStream
     * @return
     * @throws XMLStreamException
     */
    public Ring parseLinearRing( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {
        return parseLinearRing( xmlStream, null );
    }

    /**
     * @param xmlStream
     * @param defaultCRS
     * @return
     * @throws XMLStreamException
     */
    public Ring parseLinearRing( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException {
        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );
        xmlStream.nextTag();

        Points points = parseControlPoints( xmlStream, crs );
        if ( points.size() < 4 ) {
            String msg = "Error in 'gml:LinearRing' element. Must specify at least four points.";
            throw new XMLParsingException( xmlStream, msg );
        }
        xmlStream.require( END_ELEMENT, GML21NS, "LinearRing" );
        LinearRing linearRing = geomFac.createLinearRing( gid, crs, points );
        idContext.addObject( linearRing );
        return linearRing;
    }

    private Points parseControlPoints( XMLStreamReaderWrapper xmlStream, CRS crs )
                            throws XMLStreamException {
        List<Point> controlPoints = null;

        if ( xmlStream.getEventType() == XMLStreamConstants.START_ELEMENT ) {
            String name = xmlStream.getLocalName();
            if ( "coordinates".equals( name ) ) {
                controlPoints = parseCoordinates( xmlStream, crs );
                xmlStream.nextTag();
            } else {
                controlPoints = new LinkedList<Point>();
                do {
                    name = xmlStream.getLocalName();
                    if ( "coord".equals( name ) ) {
                        double[] coords = parseCoordType( xmlStream );
                        // anonymous point (no registering necessary)
                        controlPoints.add( geomFac.createPoint( null, coords, crs ) );
                    } else {
                        break;
                    }
                } while ( xmlStream.nextTag() == XMLStreamConstants.START_ELEMENT );
            }
        }
        return geomFac.createPoints( controlPoints );
    }

    /**
     * @param xmlStream
     * @return
     * @throws XMLStreamException
     */
    public Point parsePoint( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {
        return parsePoint( xmlStream, null );
    }

    /**
     * @param xmlStream
     * @param defaultCRS
     * @return
     * @throws XMLStreamException
     */
    public Point parsePoint( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLStreamException {
        Point point = null;
        String gid = parseGeometryId( xmlStream );
        CRS crs = determineActiveCRS( xmlStream, defaultCRS );
        xmlStream.nextTag();

        // must contain one of the following child elements: "gml:pos", "gml:coordinates" or "gml:coord"
        if ( xmlStream.getEventType() == START_ELEMENT ) {
            String name = xmlStream.getLocalName();
            if ( "coordinates".equals( name ) ) {
                List<Point> points = parseCoordinates( xmlStream, crs );
                if ( points.size() != 1 ) {
                    String msg = "A gml:Point element must contain exactly one tuple of coordinates.";
                    throw new XMLParsingException( xmlStream, msg );
                }
                point = points.get( 0 );
            } else if ( "coord".equals( name ) ) {
                double[] coords = parseCoordType( xmlStream );
                point = geomFac.createPoint( gid, coords, crs );

            } else {
                String msg = "Error in 'gml:Point' element. Expected either a 'gml:coordinates'"
                             + " or a 'gml:coord' element, but found '" + name + "'.";
                throw new XMLParsingException( xmlStream, msg );
            }
        } else {
            String msg = "Error in 'gml:Point' element. Must contain one of the following child elements: 'gml:pos', 'gml:coordinates'"
                         + " or 'gml:coord'.";
            throw new XMLParsingException( xmlStream, msg );
        }
        xmlStream.nextTag();
        xmlStream.require( END_ELEMENT, GML21NS, "Point" );
        idContext.addObject( point );
        return point;
    }

    /**
     * @param xmlStream
     * @return
     * @throws XMLStreamException
     */
    protected double[] parseCoordType( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {

        int event = xmlStream.nextTag();

        // must be a 'gml:X' element
        if ( event != XMLStreamConstants.START_ELEMENT || !GML_X.equals( xmlStream.getName() ) ) {
            String msg = "Invalid 'gml:coords' element. Must contain an 'gml:X' element.";
            throw new XMLParsingException( xmlStream, msg );
        }
        double x = xmlStream.getElementTextAsDouble();
        event = xmlStream.nextTag();
        if ( event == XMLStreamConstants.END_ELEMENT ) {
            return new double[] { x };
        }

        // must be a 'gml:Y' element
        if ( event != XMLStreamConstants.START_ELEMENT || !GML_Y.equals( xmlStream.getName() ) ) {
            String msg = "Invalid 'gml:coords' element. Second child element must be a 'gml:Y' element.";
            throw new XMLParsingException( xmlStream, msg );
        }
        double y = xmlStream.getElementTextAsDouble();
        event = xmlStream.nextTag();
        if ( event == XMLStreamConstants.END_ELEMENT ) {
            return new double[] { x, y };
        }

        // must be a 'gml:Z' element
        if ( event != XMLStreamConstants.START_ELEMENT || !GML_Z.equals( xmlStream.getName() ) ) {
            String msg = "Invalid 'gml:coords' element. Third child element must be a 'gml:Z' element.";
            throw new XMLParsingException( xmlStream, msg );
        }
        double z = xmlStream.getElementTextAsDouble();

        event = xmlStream.nextTag();
        if ( event != XMLStreamConstants.END_ELEMENT ) {
            xmlStream.skipElement();
        }
        return new double[] { x, y, z };
    }

    private List<Point> parseCoordinates( XMLStreamReaderWrapper xmlStream, CRS crs )
                            throws XMLStreamException {

        String decimalSeparator = xmlStream.getAttributeValueWDefault( "decimal", "." );
        if ( !".".equals( decimalSeparator ) ) {
            String msg = "Currently, only '.' is supported as decimal separator.";
            throw new XMLParsingException( xmlStream, msg );
        }

        String coordinateSeparator = xmlStream.getAttributeValueWDefault( "cs", "," );
        String tupleSeparator = xmlStream.getAttributeValueWDefault( "ts", " " );

        String text = xmlStream.getElementText();

        List<String> tuples = new LinkedList<String>();
        StringTokenizer tupleTokenizer = new StringTokenizer( text, tupleSeparator + "\n" );
        while ( tupleTokenizer.hasMoreTokens() ) {
            String newToken = tupleTokenizer.nextToken();
            if ( newToken != null && newToken.trim().length() > 0 ) {
                tuples.add( newToken );
            }
        }

        List<Point> points = new ArrayList<Point>( tuples.size() );
        for ( int i = 0; i < tuples.size(); i++ ) {
            StringTokenizer coordinateTokenizer = new StringTokenizer( tuples.get( i ), coordinateSeparator );
            List<String> tokens = new ArrayList<String>();
            while ( coordinateTokenizer.hasMoreTokens() ) {
                tokens.add( coordinateTokenizer.nextToken() );
            }
            double[] tuple = new double[tokens.size()];
            for ( int j = 0; j < tuple.length; j++ ) {
                try {
                    tuple[j] = Double.parseDouble( tokens.get( j ) );
                } catch ( NumberFormatException e ) {
                    String msg = "Value '" + tokens.get( j ) + "' cannot be parsed as a double.";
                    throw new XMLParsingException( xmlStream, msg );
                }
            }
            points.add( geomFac.createPoint( null, tuple, crs ) );
        }
        return points;
    }

    private CRS determineActiveCRS( XMLStreamReaderWrapper xmlStream, CRS defaultCRS ) {
        CRS activeCRS = defaultCRS;
        String srsName = xmlStream.getAttributeValue( null, "srsName" );
        if ( !( srsName == null || srsName.length() == 0 ) ) {
            if ( defaultCRS == null || !srsName.equals( defaultCRS.getName() ) ) {
                activeCRS = new CRS( srsName );
            }
        }
        return activeCRS;
    }

    private String parseGeometryId( XMLStreamReaderWrapper xmlStream ) {
        String gid = xmlStream.getAttributeValue( null, GID );

        // Check that the geometry id has the correct form. "gid" and "gml:id" are both based
        // on the XML type "ID": http://www.w3.org/TR/xmlschema11-2/#NCName
        // Thus, they must match the NCName production rule. This means that they may not contain
        // a separating colon (only at the first position a colon is allowed) and must not
        // start with a digit.
        if ( gid != null && gid.length() > 0 && !gid.matches( "[^\\d][^:]+" ) ) {
            String msg = Messages.getMessage( "GML_INVALID_GEOMETRYID", gid );
            throw new IllegalArgumentException( msg );
        }
        return gid;
    }

}
