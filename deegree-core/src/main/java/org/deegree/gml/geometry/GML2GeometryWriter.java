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

import static org.deegree.commons.xml.CommonNamespaces.XLNNS;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.cs.CRS;
import org.deegree.cs.CoordinateTransformer;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
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
import org.deegree.geometry.points.Points;
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
import org.deegree.geometry.standard.points.PointsArray;
import org.deegree.gml.geometry.refs.GeometryReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates GML 2.1 representations from {@link Geometry} objects.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GML2GeometryWriter implements GMLGeometryWriter {

    private static final Logger LOG = LoggerFactory.getLogger( GML2GeometryWriter.class );

    private static final String GML21NS = "http://www.opengis.net/gml";

    private final XMLStreamWriter writer;

    private final Set<String> exportedIds;

    private final CoordinateFormatter formatter;

    private CoordinateTransformer transformer;

    private double[] transformedOrdinates;

    private GeometryTransformer geoTransformer;

    private CRS outputCRS;

    /**
     * Creates a new {@link GML2GeometryWriter} instance.
     * 
     * @param writer
     *            the {@link XMLStreamWriter} that is used to serialize the GML, must not be <code>null</code>
     */
    public GML2GeometryWriter( XMLStreamWriter writer ) {
        this( writer, null, null, null );
    }

    /**
     * Creates a new {@link GML2GeometryWriter} instance.
     * 
     * @param writer
     *            the {@link XMLStreamWriter} that is used to serialize the GML, must not be <code>null</code>
     * @param outputCrs
     *            crs used for exported geometries, may be <code>null</code> (in that case, the crs of the geometries is
     *            used)
     * @param formatter
     *            formatter to use for exporting coordinates, e.g. to limit the number of decimal places, may be
     *            <code>null</code> (use 5 decimal places)
     * @param exportedIds
     *            may be <code>null</code>
     */
    public GML2GeometryWriter( XMLStreamWriter writer, CRS outputCrs, CoordinateFormatter formatter,
                               Set<String> exportedIds ) {
        this.writer = writer;
        this.outputCRS = outputCrs;
        if ( exportedIds == null ) {
            this.exportedIds = new HashSet<String>();
        } else {
            this.exportedIds = exportedIds;
        }
        if ( outputCrs != null ) {
            try {
                CoordinateSystem crs = outputCrs.getWrappedCRS();
                transformer = new CoordinateTransformer( crs );
                transformedOrdinates = new double[crs.getDimension()];
                geoTransformer = new GeometryTransformer( crs );
            } catch ( Exception e ) {
                LOG.debug( "Could not create transformer for CRS '" + outputCrs + "': " + e.getMessage()
                           + ". Encoding will fail if a transformation is actually necessary." );
            }
        }
        if ( formatter == null ) {
            this.formatter = new DecimalCoordinateFormatter( 5 );
        } else {
            this.formatter = formatter;
        }
    }

    /**
     * @param geometry
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    @Override
    public void export( Geometry geometry )
                            throws XMLStreamException, TransformationException, UnknownCRSException {

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
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    @Override
    public void exportPoint( Point point )
                            throws XMLStreamException, TransformationException, UnknownCRSException {

        startGeometry( "Point", point );
        exportCoord( point );
        writer.writeEndElement(); // </gml:Point>
    }

    private void exportCoord( Point point )
                            throws XMLStreamException, TransformationException, UnknownCRSException {

        double[] ords = getTransformedCoordinate( point.getCoordinateSystem(), point.getAsArray() );

        writer.writeStartElement( "gml", "coord", GML21NS );
        writer.writeStartElement( "gml", "X", GML21NS );
        writer.writeCharacters( formatter.format( ords[0] ) );
        writer.writeEndElement();
        if ( ords.length > 1 ) {
            writer.writeStartElement( "gml", "Y", GML21NS );
            writer.writeCharacters( formatter.format( ords[1] ) );
            writer.writeEndElement();
            if ( ords.length > 2 ) {
                writer.writeStartElement( "gml", "Z", GML21NS );
                writer.writeCharacters( formatter.format( ords[2] ) );
                writer.writeEndElement();
            }
        }
        writer.writeEndElement(); // </gml:coord>
    }

    private void exportCoordinates( Points points )
                            throws XMLStreamException, TransformationException, UnknownCRSException {

        writer.writeStartElement( "gml", "coordinates", GML21NS );
        writer.writeAttribute( "decimal", "." );
        writer.writeAttribute( "cs", "," );
        writer.writeAttribute( "ts", " " );
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for ( Point point : points ) {
            if ( !first ) {
                sb.append( " " );
            }
            double[] ords = getTransformedCoordinate( point.getCoordinateSystem(), point.getAsArray() );
            sb.append( formatter.format( ords[0] ) );
            for ( int i = 1; i < ords.length; i++ ) {
                sb.append( "," );
                sb.append( formatter.format( ords[i] ) );
            }
            first = false;
        }
        writer.writeCharacters( sb.toString() );
        writer.writeEndElement();
    }

    /**
     * @param polygon
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void exportPolygon( Polygon polygon )
                            throws XMLStreamException, TransformationException, UnknownCRSException {

        startGeometry( "Polygon", polygon );

        Ring outerRing = polygon.getExteriorRing();
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
                    exportLinearRing( ring ); // in GML 2.1 the interior rings are linear rings
                }
                writer.writeEndElement(); // </gml:innerBoundaryIs>
            }
        }
        writer.writeEndElement(); // </gml:Polygon>
    }

    /**
     * @param linearRing
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void exportLinearRing( Ring linearRing )
                            throws XMLStreamException, TransformationException, UnknownCRSException {

        startGeometry( "LinearRing", linearRing );
        exportCoordinates( linearRing.getControlPoints() );
        writer.writeEndElement(); // </gml:LinearRing>
    }

    /**
     * @param curve
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void exportLineString( Curve curve )
                            throws XMLStreamException, TransformationException, UnknownCRSException {

        startGeometry( "LineString", curve );
        exportCoordinates( curve.getControlPoints() );
        writer.writeEndElement(); // </gml:LineString>
    }

    /**
     * @param envelope
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    @Override
    public void exportEnvelope( Envelope envelope )
                            throws XMLStreamException, TransformationException, UnknownCRSException {

        writer.writeStartElement( "gml", "Box", GML21NS );
        if ( envelope.getCoordinateSystem().getName() != null ) {
            writer.writeAttribute( "srsName", envelope.getCoordinateSystem().getName() );
        }
        exportCoordinates( new PointsArray( envelope.getMin(), envelope.getMax() ) );
        writer.writeEndElement(); // </gml:Box>
    }

    /**
     * @param multiGeometry
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    @Override
    public void exportMultiGeometry( MultiGeometry<? extends Geometry> multiGeometry )
                            throws XMLStreamException, TransformationException, UnknownCRSException {

        startGeometry( "MultiGeometry", multiGeometry );
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
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void exportMultiPoint( MultiPoint multiPoint )
                            throws XMLStreamException, TransformationException, UnknownCRSException {

        startGeometry( "MultiPoint", multiPoint );

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
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void exportMultiLineString( MultiLineString multiLineString )
                            throws XMLStreamException, TransformationException, UnknownCRSException {
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
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void exportMultiPolygon( MultiPolygon multiPolygon )
                            throws XMLStreamException, TransformationException, UnknownCRSException {
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

    private double[] getTransformedCoordinate( CRS inputCRS, double[] inputCoordinate )
                            throws TransformationException, UnknownCRSException {
        if ( inputCRS != null && outputCRS != null && !inputCRS.equals( outputCRS ) ) {
            if ( transformer == null ) {
                throw new UnknownCRSException( outputCRS.getName() );
            }
            double[] out = transformer.transform( inputCRS.getWrappedCRS(), inputCoordinate, transformedOrdinates );
            return out;
        }
        return inputCoordinate;
    }

    private Envelope getTransformedEnvelope( Envelope env )
                            throws TransformationException, UnknownCRSException {
        CRS inputCRS = env.getCoordinateSystem();
        if ( inputCRS != null && outputCRS != null && !inputCRS.equals( outputCRS ) ) {
            if ( transformer == null ) {
                throw new UnknownCRSException( outputCRS.getName() );
            }
            return (Envelope) geoTransformer.transform( env );
        }
        return env;
    }

    private void startGeometry( String localName, Geometry geometry )
                            throws XMLStreamException {

        writer.writeStartElement( "gml", localName, GML21NS );

        if ( geometry.getId() != null ) {
            exportedIds.add( geometry.getId() );
            writer.writeAttribute( "gid", geometry.getId() );
        }

        if ( outputCRS != null ) {
            writer.writeAttribute( "srsName", outputCRS.getName() );
        } else if ( geometry.getCoordinateSystem() != null ) {
            writer.writeAttribute( "srsName", geometry.getCoordinateSystem().getName() );
        }
    }
}
