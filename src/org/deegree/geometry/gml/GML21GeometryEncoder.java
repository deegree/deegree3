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

import static org.deegree.commons.xml.CommonNamespaces.XLNNS;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.composite.CompositeCurve;
import org.deegree.geometry.composite.CompositeGeometry;
import org.deegree.geometry.composite.CompositeSolid;
import org.deegree.geometry.composite.CompositeSurface;
import org.deegree.geometry.gml.refs.GeometryReference;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Solid;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.Tin;
import org.deegree.geometry.primitive.TriangulatedSurface;

/**
 * Exports a Geometry bean (that belongs to GML 2.1.*) via a {@link XMLStreamWriter}.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class GML21GeometryEncoder implements GMLGeometryEncoder {

    private static final String GML21NS = "http://www.opengis.net/gml";

    private XMLStreamWriter writer;

    private Set<String> exportedIds;

    /**
     * @param writer
     */
    public GML21GeometryEncoder( XMLStreamWriter writer ) {
        new GML21GeometryEncoder( writer, new HashSet<String>() );
    }

    /**
     * @param writer
     * @param exportedIds
     *            must not be null
     */
    public GML21GeometryEncoder( XMLStreamWriter writer, Set<String> exportedIds ) {
        this.writer = writer;
        this.exportedIds = exportedIds;
    }

    /**
     * @param geometry
     * @throws XMLStreamException
     */
    @Override
    public void export( Geometry geometry )
                            throws XMLStreamException {

        if ( geometry instanceof Point ) {
            exportPoint( (Point) geometry );
        } else if ( geometry instanceof Polygon ) {
            exportPolygon( (Polygon) geometry );
        } else if ( geometry instanceof LinearRing ) {
            exportLinearRing( (LinearRing) geometry );
        } else if ( geometry instanceof LineString ) {
            exportLineString( (LineString) geometry );
        } else if ( geometry instanceof Envelope ) {
            exportEnvelope( (Envelope) geometry );
        } else if ( geometry instanceof MultiGeometry<?> ) {
            exportMultiGeometry( (MultiGeometry<?>) geometry );
        } else if ( geometry instanceof MultiPoint ) {
            exportMultiPoint( (MultiPoint) geometry );
        } else if ( geometry instanceof MultiLineString ) {
            exportMultiLineString( (MultiLineString) geometry );
        } else if ( geometry instanceof MultiPolygon ) {
            exportMultiPolygon( (MultiPolygon) geometry );
        }
    }

    /**
     * @param point
     * @throws XMLStreamException
     */
    @Override
    public void exportPoint( Point point )
                            throws XMLStreamException {

        exportedIds.add( point.getId() );

        writer.writeStartElement( "gml", "Point", GML21NS );

        writer.writeAttribute( null, "gid", point.getId() );
        writer.writeAttribute( null, "srsName", point.getCoordinateSystem().getName() );

        exportCoord( point );
        writer.writeEndElement(); // </gml:Point>
    }

    private void exportCoord( Point point )
                            throws XMLStreamException {
        writer.writeStartElement( "coord", GML21NS );

        writer.writeStartElement( "gml", "X", GML21NS );
        writer.writeCharacters( String.valueOf( point.get0() ) );
        writer.writeEndElement();
        if ( point.getCoordinateDimension() > 1 ) {

            writer.writeStartElement( "gml", "Y", GML21NS );
            writer.writeCharacters( String.valueOf( point.get1() ) );
            writer.writeEndElement();
            if ( point.getCoordinateDimension() > 2 ) {

                writer.writeStartElement( "gml", "Z", GML21NS );
                writer.writeCharacters( String.valueOf( point.get2() ) );
                writer.writeEndElement();
            }
        }
        writer.writeEndElement(); // </gml:coord>
    }

    /**
     * @param polygon
     * @throws XMLStreamException
     */
    public void exportPolygon( Polygon polygon )
                            throws XMLStreamException {

        if ( exportedIds.contains( polygon.getId() ) ) {
            writer.writeEmptyElement( "gml", "Polygon", GML21NS );
            writer.writeAttribute( XLNNS, "href", "#" + polygon.getId() );
        } else {

            exportedIds.add( polygon.getId() );

            writer.writeStartElement( "gml", "Polygon", GML21NS );

            writer.writeAttribute( null, "gid", polygon.getId() );
            writer.writeAttribute( null, "srsName", polygon.getCoordinateSystem().getName() );

            LinearRing outerRing = (LinearRing) polygon.getExteriorRing();
            if ( exportedIds.contains( outerRing.getId() ) ) {
                writer.writeEmptyElement( "gml", "outerBoundaryIs", GML21NS );
                writer.writeAttribute( "xlink", XLNNS, "href", "#" + outerRing.getId() );

            } else {
                writer.writeStartElement( "gml", "outerBoundaryIs", GML21NS );
                exportLinearRing( outerRing );
                writer.writeEndElement(); // </gml:outerBoundaryIs>
            }

            List<Ring> rings = polygon.getInteriorRings();
            if ( rings != null ) {
                writer.writeStartElement( "gml", "innerBoundaryIs", GML21NS );
                for ( Ring ring : rings ) {
                    if ( exportedIds.contains( ring.getId() ) ) {
                        writer.writeAttribute( "xlink", XLNNS, "href", "#" + ring.getId() );
                    } else {
                        exportLinearRing( (LinearRing) ring ); // in GML 2.1 the interior rings are linear rings
                    }
                }
                writer.writeEndElement(); // </gml:innerBoundaryIs>
            }
            writer.writeEndElement(); // </gml:Polygon>
        }
    }

    /**
     * @param linearRing
     * @throws XMLStreamException
     */
    public void exportLinearRing( LinearRing linearRing )
                            throws XMLStreamException {
        exportedIds.add( linearRing.getId() );

        writer.writeStartElement( "gml", "LinearRing", GML21NS );

        writer.writeAttribute( null, "gid", linearRing.getId() );
        writer.writeAttribute( null, "srsName", linearRing.getCoordinateSystem().getName() );

        for ( Point point : linearRing.getControlPoints() ) {
            exportCoord( point );
        }
        writer.writeEndElement(); // </gml:LinearRing>
    }

    /**
     * @param linearString
     * @throws XMLStreamException
     */
    public void exportLineString( LineString linearString )
                            throws XMLStreamException {
        exportedIds.add( linearString.getId() );

        writer.writeStartElement( "gml", "LineString", GML21NS );

        writer.writeAttribute( null, "gid", linearString.getId() );
        writer.writeAttribute( null, "srsName", linearString.getCoordinateSystem().getName() );

        for ( Point point : linearString.getControlPoints() ) {
            exportCoord( point );
        }
        writer.writeEndElement(); // </gml:LineString>
    }

    /**
     * @param envelope
     * @throws XMLStreamException
     */
    @Override
    public void exportEnvelope( Envelope envelope )
                            throws XMLStreamException {
        exportedIds.add( envelope.getId() );

        writer.writeStartElement( "gml", "Box", GML21NS );

        writer.writeAttribute( "gml", GML21NS, "gid", envelope.getId() );
        writer.writeAttribute( "gml", GML21NS, "srsName", envelope.getCoordinateSystem().getName() );

        Point min = envelope.getMin();
        exportCoord( min );

        Point max = envelope.getMax();
        exportCoord( max );

        writer.writeEndElement(); // </gml:Box>
    }

    /**
     * @param multiGeometry
     * @throws XMLStreamException
     */
    @Override
    public void exportMultiGeometry( MultiGeometry<? extends Geometry> multiGeometry )
                            throws XMLStreamException {
        writer.writeStartElement( "gml", "MultiGeometry", GML21NS );

        writer.writeAttribute( null, "gid", multiGeometry.getId() );
        writer.writeAttribute( null, "srsName", multiGeometry.getCoordinateSystem().getName() );

        for ( Geometry geom : multiGeometry ) {
            if ( exportedIds.contains( geom.getId() ) ) {
                writer.writeEmptyElement( "gml", "geometryMember", GML21NS );
                writer.writeAttribute( "xlink", XLNNS, "href", "#" + geom.getId() );

            } else {
                writer.writeStartElement( "gml", "geometryMember", GML21NS );
                export( geom );
                writer.writeEndElement();
            }
        }
        writer.writeEndElement(); // </gml:MultiGeometry>
    }

    /**
     * @param multiPoint
     * @throws XMLStreamException
     */
    public void exportMultiPoint( MultiPoint multiPoint )
                            throws XMLStreamException {
        writer.writeStartElement( "gml", "MultiPoint", GML21NS );

        writer.writeAttribute( null, "gid", multiPoint.getId() );
        writer.writeAttribute( null, "srsName", multiPoint.getCoordinateSystem().getName() );

        for ( Point point : multiPoint ) {
            if ( exportedIds.contains( point.getId() ) ) {
                writer.writeEmptyElement( "gml", "pointMember", GML21NS );
                writer.writeAttribute( "xlink", XLNNS, "href", "#" + point.getId() );

            } else {
                writer.writeStartElement( "gml", "pointMember", GML21NS );
                exportPoint( point );
                writer.writeEndElement();
            }
        }
        writer.writeEndElement(); // </gml:MultiPoint>
    }

    /**
     * @param multiLineString
     * @throws XMLStreamException
     */
    public void exportMultiLineString( MultiLineString multiLineString )
                            throws XMLStreamException {
        writer.writeStartElement( "gml", "MultiLineString", GML21NS );

        writer.writeAttribute( null, "gid", multiLineString.getId() );
        writer.writeAttribute( null, "srsName", multiLineString.getCoordinateSystem().getName() );

        for ( LineString lineString : multiLineString ) {
            if ( exportedIds.contains( lineString.getId() ) ) {
                writer.writeEmptyElement( "gml", "lineStringMember", GML21NS );
                writer.writeAttribute( "xlink", XLNNS, "href", "#" + lineString.getId() );

            } else {
                writer.writeStartElement( "gml", "lineStringMember", GML21NS );
                exportLineString( lineString );
                writer.writeEndElement();
            }
        }
        writer.writeEndElement(); // </gml:MultiLineString>
    }

    /**
     * @param multiPolygon
     * @throws XMLStreamException
     */
    public void exportMultiPolygon( MultiPolygon multiPolygon )
                            throws XMLStreamException {
        writer.writeStartElement( "gml", "MultiPolygon", GML21NS );

        writer.writeAttribute( null, "gid", multiPolygon.getId() );
        writer.writeAttribute( null, "srsName", multiPolygon.getCoordinateSystem().getName() );

        for ( Polygon polygon : multiPolygon ) {
            if ( exportedIds.contains( polygon.getId() ) ) {
                writer.writeEmptyElement( "gml", "polygonMember", GML21NS );
                writer.writeAttribute( "xlink", XLNNS, "href", "#" + polygon.getId() );

            } else {
                writer.writeStartElement( "gml", "polygonMember", GML21NS );
                exportPolygon( polygon );
                writer.writeEndElement();
            }
        }
        writer.writeEndElement(); // </gml:MultiPolygon>
    }

    @Override
    public void exportCompositeCurve( CompositeCurve compositeCurve )
                            throws Exception {
        // TODO throw a suitable exception
        throw new Exception(
                             "Cannot export CompositeCurve in GML 2.1 as this geometry is not supported in this version of GML." );
    }

    @Override
    public void exportCompositeGeometry( CompositeGeometry<GeometricPrimitive> geometryComplex )
                            throws Exception {
        // TODO throw a suitable exception
        throw new Exception(
                             "Cannot export CompositeGeometry in GML 2.1 as this geometry is not supported in this version of GML." );
    }

    @Override
    public void exportCompositeSolid( CompositeSolid compositeSolid )
                            throws Exception {
        // TODO throw a suitable exception
        throw new Exception(
                             "Cannot export CompositeSolid in GML 2.1 as this geometry is not supported in this version of GML." );
    }

    @Override
    public void exportCompositeSurface( CompositeSurface compositeSurface )
                            throws Exception {
        // TODO throw a suitable exception
        throw new Exception(
                             "Cannot export CompositeSurface in GML 2.1 as this geometry is not supported in this version of GML." );
    }

    @Override
    public void exportCurve( Curve curve )
                            throws Exception {
        // TODO throw a suitable exception
        throw new Exception( "Cannot export Curve in GML2.1 as this geometry is not supported in this version of GML." );
    }

    @Override
    public void exportReference( GeometryReference<Geometry> geometryRef )
                            throws Exception {
        // TODO throw a suitable exception
        throw new Exception(
                             "Cannot export GeometryReference in GML2.1 as this geometry is not supported in this version of GML." );
    }

    @Override
    public void exportRing( Ring ring )
                            throws Exception {
        // TODO throw a suitable exception
        throw new Exception( "Cannot export Ring in GML2.1 as this geometry is not supported in this version of GML." );
    }

    @Override
    public void exportSolid( Solid solid )
                            throws Exception {
        // TODO throw a suitable exception
        throw new Exception( "Cannot export Solid in GML2.1 as this geometry is not supported in this version of GML." );
    }

    @Override
    public void exportSurface( Surface surface )
                            throws Exception {
        // TODO throw a suitable exception
        throw new Exception(
                             "Cannot export Surface in GML2.1 as this geometry is not supported in this version of GML." );
    }

    @Override
    public void exportTin( Tin tin )
                            throws Exception {
        // TODO throw a suitable exception
        throw new Exception( "Cannot export Tin in GML2.1 as this geometry is not supported in this version of GML." );
    }

    @Override
    public void exportTriangulatedSurface( TriangulatedSurface triangSurface )
                            throws Exception {
        // TODO throw a suitable exception
        throw new Exception(
                             "Cannot export TriangulatedSurface in GML2.1 as this geometry is not supported in this version of GML." );
    }

}
