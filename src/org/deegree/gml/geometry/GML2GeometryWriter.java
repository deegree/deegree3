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
import org.deegree.geometry.io.CoordinateFormatter;
import org.deegree.geometry.io.DecimalCoordinateFormatter;
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
import org.deegree.gml.geometry.refs.GeometryReference;

/**
 * Exports a Geometry bean (that belongs to GML 2.1.*) via a {@link XMLStreamWriter}.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GML2GeometryWriter implements GMLGeometryWriter {

    private static final String GML21NS = "http://www.opengis.net/gml";

    private final XMLStreamWriter writer;

    private final Set<String> exportedIds;

    private final CoordinateFormatter formatter;

    /**
     * @param writer
     */
    public GML2GeometryWriter( XMLStreamWriter writer ) {
        this( writer, null, new HashSet<String>() );
    }

    /**
     * @param writer
     * @param formatter
     *            formatter to use for exporting coordinates, e.g. to limit the number of decimal places, may be
     *            <code>null</code> (use 5 decimal places)
     * @param exportedIds
     *            must not be null
     */
    public GML2GeometryWriter( XMLStreamWriter writer, CoordinateFormatter formatter, Set<String> exportedIds ) {
        this.writer = writer;
        this.exportedIds = exportedIds;
        if ( formatter == null ) {
            this.formatter = new DecimalCoordinateFormatter( 5 );
        } else {
            this.formatter = formatter;
        }
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
        } else if ( geometry instanceof MultiPoint ) {
            exportMultiPoint( (MultiPoint) geometry );
        } else if ( geometry instanceof MultiLineString ) {
            exportMultiLineString( (MultiLineString) geometry );
        } else if ( geometry instanceof MultiPolygon ) {
            exportMultiPolygon( (MultiPolygon) geometry );
        } else if ( geometry instanceof MultiGeometry<?> ) {
            exportMultiGeometry( (MultiGeometry<?>) geometry );
        }

    }

    /**
     * @param point
     * @throws XMLStreamException
     */
    @Override
    public void exportPoint( Point point )
                            throws XMLStreamException {
        writer.writeStartElement( "gml", "Point", GML21NS );

        if ( point.getId() != null ) {
            exportedIds.add( point.getId() );
            writer.writeAttribute( "gid", point.getId() );
        }
        if ( point.getCoordinateSystem().getName() != null ) {
            writer.writeAttribute( "srsName", point.getCoordinateSystem().getName() );
        }

        exportCoord( point );
        writer.writeEndElement(); // </gml:Point>
    }

    private void exportCoord( Point point )
                            throws XMLStreamException {
        writer.writeStartElement( "gml", "coord", GML21NS );
        writer.writeStartElement( "gml", "X", GML21NS );
        writer.writeCharacters( formatter.format( point.get0() ) );
        writer.writeEndElement();
        if ( point.getCoordinateDimension() > 1 ) {

            writer.writeStartElement( "gml", "Y", GML21NS );
            writer.writeCharacters( formatter.format( point.get1() ) );
            writer.writeEndElement();
            if ( point.getCoordinateDimension() > 2 ) {

                writer.writeStartElement( "gml", "Z", GML21NS );
                writer.writeCharacters( formatter.format( point.get2() ) );
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

        writer.writeStartElement( "gml", "Polygon", GML21NS );

        if ( polygon.getId() != null ) {
            exportedIds.add( polygon.getId() );
            writer.writeAttribute( "gid", polygon.getId() );
        }
        if ( polygon.getCoordinateSystem() != null && polygon.getCoordinateSystem().getName() != null ) {
            writer.writeAttribute( "srsName", polygon.getCoordinateSystem().getName() );
        }

        LinearRing outerRing = (LinearRing) polygon.getExteriorRing();
        if ( outerRing.getId() != null && exportedIds.contains( outerRing.getId() ) ) {
            writer.writeEmptyElement( "gml", "outerBoundaryIs", GML21NS );
            writer.writeAttribute( "xlink", XLNNS, "href", "#" + outerRing.getId() );

        } else {
            writer.writeStartElement( "gml", "outerBoundaryIs", GML21NS );
            exportLinearRing( outerRing );
            writer.writeEndElement(); // </gml:outerBoundaryIs>
        }

        List<Ring> rings = polygon.getInteriorRings();
        if ( rings != null ) {

            for ( Ring ring : rings ) {
                writer.writeStartElement( "gml", "innerBoundaryIs", GML21NS );
                if ( exportedIds.contains( ring.getId() ) ) {
                    writer.writeAttribute( "xlink", XLNNS, "href", "#" + ring.getId() );

                } else {
                    exportLinearRing( (LinearRing) ring ); // in GML 2.1 the interior rings are linear rings
                }
                writer.writeEndElement(); // </gml:innerBoundaryIs>
            }
        }
        writer.writeEndElement(); // </gml:Polygon>
    }

    /**
     * @param linearRing
     * @throws XMLStreamException
     */
    public void exportLinearRing( LinearRing linearRing )
                            throws XMLStreamException {
        writer.writeStartElement( "gml", "LinearRing", GML21NS );

        if ( linearRing.getId() != null ) {
            exportedIds.add( linearRing.getId() );
            writer.writeAttribute( "gid", linearRing.getId() );
        }
        if ( linearRing.getCoordinateSystem() != null && linearRing.getCoordinateSystem().getName() != null ) {
            writer.writeAttribute( "srsName", linearRing.getCoordinateSystem().getName() );
        }

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
        writer.writeStartElement( "gml", "LineString", GML21NS );

        if ( linearString.getId() != null ) {
            exportedIds.add( linearString.getId() );
            writer.writeAttribute( "gid", linearString.getId() );
        }
        if ( linearString.getCoordinateSystem().getName() != null ) {
            writer.writeAttribute( "srsName", linearString.getCoordinateSystem().getName() );
        }

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
        writer.writeStartElement( "gml", "Box", GML21NS );

        if ( envelope.getId() != null ) {
            exportedIds.add( envelope.getId() );
            writer.writeAttribute( "gid", envelope.getId() );
        }
        if ( envelope.getCoordinateSystem().getName() != null ) {
            writer.writeAttribute( "srsName", envelope.getCoordinateSystem().getName() );
        }

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

        if ( multiGeometry.getId() != null ) {
            writer.writeAttribute( "gid", multiGeometry.getId() );
        }
        if ( multiGeometry.getCoordinateSystem().getName() != null ) {
            writer.writeAttribute( "srsName", multiGeometry.getCoordinateSystem().getName() );
        }

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

        if ( multiPoint.getId() != null ) {
            writer.writeAttribute( "gid", multiPoint.getId() );
        }
        if ( multiPoint.getCoordinateSystem().getName() != null ) {
            writer.writeAttribute( "srsName", multiPoint.getCoordinateSystem().getName() );
        }

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

        if ( multiLineString.getId() != null ) {
            writer.writeAttribute( "gid", multiLineString.getId() );
        }
        if ( multiLineString.getCoordinateSystem().getName() != null ) {
            writer.writeAttribute( "srsName", multiLineString.getCoordinateSystem().getName() );
        }

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

        if ( multiPolygon.getId() != null ) {
            writer.writeAttribute( "gid", multiPolygon.getId() );
        }
        writer.writeAttribute( "srsName", multiPolygon.getCoordinateSystem().getName() );

        for ( Polygon polygon : multiPolygon ) {
            if ( polygon.getId() != null && exportedIds.contains( polygon.getId() ) ) {
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
    public void exportCompositeCurve( CompositeCurve compositeCurve ) {
        throw new UnsupportedOperationException(
                                                 "Cannot export CompositeCurve in GML 2.1 as this geometry is not supported in this version of GML." );
    }

    @Override
    public void exportCompositeGeometry( CompositeGeometry<GeometricPrimitive> geometryComplex ) {
        throw new UnsupportedOperationException(
                                                 "Cannot export CompositeGeometry in GML 2.1 as this geometry is not supported in this version of GML." );
    }

    @Override
    public void exportCompositeSolid( CompositeSolid compositeSolid ) {
        throw new UnsupportedOperationException(
                                                 "Cannot export CompositeSolid in GML 2.1 as this geometry is not supported in this version of GML." );
    }

    @Override
    public void exportCompositeSurface( CompositeSurface compositeSurface ) {
        throw new UnsupportedOperationException(
                                                 "Cannot export CompositeSurface in GML 2.1 as this geometry is not supported in this version of GML." );
    }

    @Override
    public void exportCurve( Curve curve ) {
        throw new UnsupportedOperationException(
                                                 "Cannot export Curve in GML2.1 as this geometry is not supported in this version of GML." );
    }

    @Override
    public void exportReference( GeometryReference<Geometry> geometryRef ) {
        throw new UnsupportedOperationException(
                                                 "Cannot export GeometryReference in GML2.1 as this geometry is not supported in this version of GML." );
    }

    @Override
    public void exportRing( Ring ring ) {
        throw new UnsupportedOperationException(
                                                 "Cannot export Ring in GML2.1 as this geometry is not supported in this version of GML." );
    }

    @Override
    public void exportSolid( Solid solid ) {
        throw new UnsupportedOperationException(
                                                 "Cannot export Solid in GML2.1 as this geometry is not supported in this version of GML." );
    }

    @Override
    public void exportSurface( Surface surface ) {
        throw new UnsupportedOperationException(
                                                 "Cannot export Surface in GML2.1 as this geometry is not supported in this version of GML." );
    }

    @Override
    public void exportTin( Tin tin ) {
        throw new UnsupportedOperationException(
                                                 "Cannot export Tin in GML2.1 as this geometry is not supported in this version of GML." );
    }

    @Override
    public void exportTriangulatedSurface( TriangulatedSurface triangSurface ) {
        throw new UnsupportedOperationException(
                                                 "Cannot export TriangulatedSurface in GML2.1 as this geometry is not supported in this version of GML." );
    }
}
