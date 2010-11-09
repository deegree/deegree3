//$HeadURL: svn+ssh://aionita@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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
import static org.deegree.gml.GMLVersion.GML_30;
import static org.deegree.gml.GMLVersion.GML_32;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.multi.MultiSolid;
import org.deegree.geometry.multi.MultiSurface;
import org.deegree.geometry.points.Points;
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
import org.deegree.geometry.primitive.patches.Cone;
import org.deegree.geometry.primitive.patches.Cylinder;
import org.deegree.geometry.primitive.patches.GriddedSurfacePatch;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.Rectangle;
import org.deegree.geometry.primitive.patches.Sphere;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.primitive.patches.Triangle;
import org.deegree.geometry.primitive.segments.Arc;
import org.deegree.geometry.primitive.segments.ArcByBulge;
import org.deegree.geometry.primitive.segments.ArcByCenterPoint;
import org.deegree.geometry.primitive.segments.ArcString;
import org.deegree.geometry.primitive.segments.ArcStringByBulge;
import org.deegree.geometry.primitive.segments.BSpline;
import org.deegree.geometry.primitive.segments.Bezier;
import org.deegree.geometry.primitive.segments.Circle;
import org.deegree.geometry.primitive.segments.CircleByCenterPoint;
import org.deegree.geometry.primitive.segments.Clothoid;
import org.deegree.geometry.primitive.segments.CubicSpline;
import org.deegree.geometry.primitive.segments.CurveSegment;
import org.deegree.geometry.primitive.segments.Geodesic;
import org.deegree.geometry.primitive.segments.GeodesicString;
import org.deegree.geometry.primitive.segments.Knot;
import org.deegree.geometry.primitive.segments.LineStringSegment;
import org.deegree.geometry.primitive.segments.OffsetCurve;
import org.deegree.geometry.standard.curvesegments.AffinePlacement;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.geometry.refs.GeometryReference;
import org.deegree.gml.props.GMLStdProps;
import org.deegree.gml.props.GMLStdPropsWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates GML 3 (3.0/3.1/3.2) representations from {@link Geometry} objects.
 * <p>
 * This class is not thread-safe.
 * </p>
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: ionita $
 * 
 * @version $Revision: $, $Date: $
 */
public class GML3GeometryWriter implements GMLGeometryWriter {

    private static final Logger LOG = LoggerFactory.getLogger( GML3GeometryWriter.class );

    private final GMLVersion version;

    private final String gmlNs;

    private final XMLStreamWriter writer;

    private final Set<String> exportedIds;

    private final CRS outputCRS;

    private final CoordinateFormatter formatter;

    private CoordinateTransformer transformer;

    private GeometryTransformer geoTransformer;

    // this object is used for every coordinate conversion, hence this class is not Thread-safe
    private double[] transformedOrdinates;

    private final boolean exportSf;

    private final GMLStdPropsWriter stdPropsWriter;

    /**
     * Creates a new {@link GML3GeometryWriter} instance, with no default crs, the default formatter and no simple
     * feature support.
     * 
     * @param version
     *            either {@link GMLVersion#GML_30}, {@link GMLVersion#GML_31} or {@link GMLVersion#GML_32}
     * @param writer
     *            the {@link XMLStreamWriter} that is used to serialize the GML, must not be <code>null</code>
     */
    public GML3GeometryWriter( GMLVersion version, XMLStreamWriter writer ) {
        this( version, writer, null, null, false, null );
    }

    /**
     * Creates a new {@link GML3GeometryWriter} instance.
     * 
     * @param version
     *            either {@link GMLVersion#GML_30}, {@link GMLVersion#GML_31} or {@link GMLVersion#GML_32}
     * @param writer
     *            the {@link XMLStreamWriter} that is used to serialize the GML, must not be <code>null</code>
     * @param outputCrs
     *            crs used for exported geometries, may be <code>null</code> (in that case, the crs of the geometries is
     *            used)
     * @param formatter
     *            formatter to use for exporting coordinates, e.g. to limit the number of decimal places, may be
     *            <code>null</code> (use 5 decimal places)
     * @param exportSf
     *            if true, the generated GML must conform to the GML-SF profile (only simple geometries are used and
     *            they are exported without id attributes)
     * @param exportedIds
     *            for the creation of xlinks, may be <code>null</code>
     */
    public GML3GeometryWriter( GMLVersion version, XMLStreamWriter writer, CRS outputCrs,
                               CoordinateFormatter formatter, boolean exportSf, Set<String> exportedIds ) {
        this.version = version;
        this.gmlNs = version.getNamespace();
        this.writer = writer;
        this.outputCRS = outputCrs;
        // TODO
        this.exportSf = false;
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
            this.formatter = new DecimalCoordinateFormatter( 8 );
        } else {
            this.formatter = formatter;
        }
        if ( exportedIds == null ) {
            this.exportedIds = new HashSet<String>();
        } else {
            this.exportedIds = exportedIds;
        }
        this.stdPropsWriter = new GMLStdPropsWriter( version, writer );
    }

    /**
     * Exporting a geometry via the XMLStreamWriter given when the class was constructed
     * 
     * @param geometry
     *            the {@link Geometry} to be exported
     * @throws XMLStreamException
     *             if an error occured writing to the xml stream
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    @SuppressWarnings("unchecked")
    @Override
    public void export( Geometry geometry )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        // TODO properly
        if ( geometry instanceof GeometryReference<?> ) {
            exportReference( (GeometryReference) geometry );
            return;
        }

        switch ( geometry.getGeometryType() ) {
        case COMPOSITE_GEOMETRY:
            exportCompositeGeometry( (CompositeGeometry<GeometricPrimitive>) geometry );
            break;
        case ENVELOPE:
            exportEnvelope( (Envelope) geometry );
            break;
        case MULTI_GEOMETRY:
            exportMultiGeometry( (MultiGeometry<? extends Geometry>) geometry );
            break;
        case PRIMITIVE_GEOMETRY:
            switch ( ( (GeometricPrimitive) geometry ).getPrimitiveType() ) {
            case Curve:
                exportCurve( (Curve) geometry );
                break;
            case Point:
                exportPoint( (Point) geometry );
                break;
            case Solid:
                exportSolid( (Solid) geometry );
                break;
            case Surface:
                exportSurface( (Surface) geometry );
                break;
            }
            break;
        }
    }

    @Override
    public void exportReference( GeometryReference<Geometry> geometryRef )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        export( geometryRef.getReferencedObject() );
    }

    /**
     * Exporting a multi-geometry via the XMLStreamWriter given when the class was constructed
     * 
     * @param geometry
     *            a {@link MultiGeometry} object
     * @throws XMLStreamException
     *             if an error occured writing to the xml stream
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    @Override
    public void exportMultiGeometry( MultiGeometry<? extends Geometry> geometry )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        switch ( geometry.getMultiGeometryType() ) {
        case MULTI_CURVE:
            MultiCurve multiCurve = (MultiCurve) geometry;
            startGeometry( "MultiCurve", geometry );
            for ( Curve curve : multiCurve ) {
                writer.writeStartElement( "gml", "curveMember", gmlNs );
                if ( !exportSf && curve.getId() != null && exportedIds.contains( curve.getId() ) ) {
                    writer.writeAttribute( XLNNS, "href", "#" + curve.getId() );
                } else if ( curve instanceof CompositeCurve ) {
                    exportCompositeCurve( (CompositeCurve) curve );
                } else {
                    exportCurve( curve );
                }
                writer.writeEndElement();
            }
            writer.writeEndElement(); // MultiCurve
            break;
        case MULTI_LINE_STRING:
            MultiLineString multiLineString = (MultiLineString) geometry;
            startGeometry( "MultiLineString", geometry );
            for ( LineString ls : multiLineString ) {
                writer.writeStartElement( gmlNs, "lineStringMember" );
                if ( !exportSf && ls.getId() != null && exportedIds.contains( ls.getId() ) ) {
                    writer.writeAttribute( XLNNS, "href", "#" + ls.getId() );
                } else {
                    exportCurve( ls ); // LineString is a type of Curve
                }
                writer.writeEndElement();
            }
            writer.writeEndElement(); // MultiLineString
            break;
        case MULTI_POINT:
            MultiPoint multiPoint = (MultiPoint) geometry;
            startGeometry( "MultiPoint", geometry );
            for ( Point point : multiPoint ) {
                writer.writeStartElement( gmlNs, "pointMember" );
                if ( !exportSf && point.getId() != null && exportedIds.contains( point.getId() ) ) {
                    writer.writeAttribute( XLNNS, "href", "#" + point.getId() );
                } else {
                    export( point );
                }
                writer.writeEndElement();
            }
            writer.writeEndElement(); // MultiPoint
            break;
        case MULTI_POLYGON:
            LOG.debug( "Exporting Geometry with ID " + geometry.getId() );
            MultiPolygon multiPolygon = (MultiPolygon) geometry;
            startGeometry( "MultiPolygon", geometry );
            for ( Polygon pol : multiPolygon ) {
                writer.writeStartElement( gmlNs, "polygonMember" );
                if ( !exportSf && pol.getId() != null && exportedIds.contains( pol.getId() ) ) {
                    writer.writeAttribute( XLNNS, "href", "#" + pol.getId() );
                } else {
                    exportSurface( pol );
                }

                writer.writeEndElement(); // polygonMember
            }
            writer.writeEndElement();
            break;
        case MULTI_SOLID:
            MultiSolid multiSolid = (MultiSolid) geometry;
            startGeometry( "MultiSolid", geometry );
            for ( Solid solid : multiSolid ) {
                writer.writeStartElement( gmlNs, "solidMember" );
                if ( !exportSf && solid.getId() != null && exportedIds.contains( solid.getId() ) ) {
                    writer.writeAttribute( XLNNS, "href", "#" + solid.getId() );
                } else if ( solid instanceof CompositeSolid ) {
                    exportCompositeSolid( (CompositeSolid) solid );
                } else {
                    exportSolid( solid );
                }
                writer.writeEndElement(); // solidMember
            }
            writer.writeEndElement();
            break;
        case MULTI_SURFACE:
            MultiSurface multiSurface = (MultiSurface) geometry;
            startGeometry( "MultiSurface", geometry );
            for ( Surface surface : multiSurface ) {
                writer.writeStartElement( gmlNs, "surfaceMember" );
                if ( !exportSf && surface.getId() != null && exportedIds.contains( surface.getId() ) ) {
                    writer.writeAttribute( XLNNS, "href", "#" + surface.getId() );
                } else if ( surface instanceof CompositeSurface ) {
                    exportCompositeSurface( (CompositeSurface) surface );
                } else {
                    exportSurface( surface );
                }

                writer.writeEndElement(); // surfaceMember
            }
            writer.writeEndElement();
            break;
        case MULTI_GEOMETRY:
            // it is the case that we export a general MultiGeometry
            startGeometry( "MultiGeometry", geometry );
            for ( Geometry geometryMember : geometry ) {
                writer.writeStartElement( gmlNs, "geometryMember" );
                if ( !exportSf && geometryMember.getId() != null && exportedIds.contains( geometryMember.getId() ) ) {
                    writer.writeAttribute( XLNNS, "href", "#" + geometryMember.getId() );
                } else {
                    export( geometryMember );
                }

                writer.writeEndElement(); // geometryMember
            }
            writer.writeEndElement();
            break;
        }
    }

    /**
     * Exporting a point via the XMLStreamWriter given when the class was constructed
     * 
     * @param point
     *            a {@link Point} object
     * @throws XMLStreamException
     *             if an error occured writing to the xml stream
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    @Override
    public void exportPoint( Point point )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        startGeometry( "Point", point );
        exportAsPos( point );
        writer.writeEndElement();
    }

    private void exportAsPos( Point point )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        writer.writeStartElement( gmlNs, "pos" );
        double[] ordinates = getTransformedCoordinate( point.getCoordinateSystem(), point.getAsArray() );
        writer.writeCharacters( formatter.format( ordinates[0] ) );
        for ( int i = 1; i < ordinates.length; i++ ) {
            writer.writeCharacters( " " + formatter.format( ordinates[i] ) );
        }
        writer.writeEndElement();
    }

    /**
     * Exporting a curve via the XMLStreamWriter given when the class was constructed
     * 
     * @param curve
     *            a {@link Curve} object
     * @throws XMLStreamException
     *             if an error occured writing to the xml stream
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    @Override
    public void exportCurve( Curve curve )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        switch ( curve.getCurveType() ) {
        case CompositeCurve:
            exportCompositeCurve( (CompositeCurve) curve );
            break;

        case Curve:
            startGeometry( "Curve", curve );

            writer.writeStartElement( gmlNs, "segments" );
            for ( CurveSegment curveSeg : curve.getCurveSegments() ) {
                exportCurveSegment( curveSeg );
            }
            writer.writeEndElement(); // segments
            writer.writeEndElement(); // Curve
            break;

        case LineString:
            LineString lineString = (LineString) curve;

            startGeometry( "LineString", lineString );
            int dim = lineString.getCoordinateDimension();
            export( lineString.getControlPoints(), dim );
            writer.writeEndElement();
            break;

        case OrientableCurve:
            OrientableCurve orientableCurve = (OrientableCurve) curve;

            startGeometry( "OrientableCurve", orientableCurve );

            writer.writeAttribute( "orientation", orientableCurve.isReversed() ? "-" : "+" );

            Curve baseCurve = orientableCurve.getBaseCurve();
            if ( baseCurve.getId() != null && exportedIds.contains( baseCurve.getId() ) ) {
                writer.writeEmptyElement( gmlNs, "baseCurve" );
                writer.writeAttribute( XLNNS, "href", "#" + baseCurve.getId() );
                writer.writeEndElement();
            } else {
                writer.writeStartElement( gmlNs, "baseCurve" );
                exportCurve( baseCurve );
                writer.writeEndElement();
            }
            writer.writeEndElement();
            break;

        case Ring:
            exportRing( (Ring) curve );
            break;
        }
    }

    /**
     * Exporting a surface via the XMLStreamWriter given when the class was constructed
     * 
     * @param surface
     *            a {@link Surface} object
     * @throws XMLStreamException
     *             if an error occured writing to the xml stream
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    @Override
    public void exportSurface( Surface surface )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        switch ( surface.getSurfaceType() ) {
        case CompositeSurface: {
            exportCompositeSurface( (CompositeSurface) surface );
            break;
        }
        case OrientableSurface: {
            exportOrientableSurface( (OrientableSurface) surface );
            break;
        }
        case Polygon: {
            exportPolygon( (Polygon) surface );
            break;
        }
        case PolyhedralSurface: {
            exportPolyhedralSurface( (PolyhedralSurface) surface );
            break;
        }
        case Surface: {
            startGeometry( "Surface", surface );
            writer.writeStartElement( gmlNs, "patches" );
            for ( SurfacePatch surfacePatch : surface.getPatches() ) {
                exportSurfacePatch( surfacePatch );
            }
            writer.writeEndElement();
            writer.writeEndElement();
            break;
        }
        case Tin: {
            exportTin( (Tin) surface );
            break;
        }
        case TriangulatedSurface: {
            exportTriangulatedSurface( (TriangulatedSurface) surface );
            break;
        }
        }
    }

    /**
     * Exporting a triangulated surface via the XMLStreamWriter given when the class was constructed
     * 
     * @param triangSurface
     *            a {@link TriangulatedSurface} object
     * @throws XMLStreamException
     *             if an error occured writing to the xml stream
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    @Override
    public void exportTriangulatedSurface( TriangulatedSurface triangSurface )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        writer.writeStartElement( gmlNs, "TriangulatedSurface" );

        if ( triangSurface.getId() != null && exportedIds.contains( triangSurface.getId() ) ) {
            writer.writeEmptyElement( gmlNs, "trianglePatches" );
            writer.writeAttribute( XLNNS, "href", "#" + triangSurface.getId() );
        } else {
            exportedIds.add( triangSurface.getId() );

            writer.writeStartElement( gmlNs, "trianglePatches" );
            for ( SurfacePatch surfacePatch : triangSurface.getPatches() )
                exportSurfacePatch( surfacePatch );
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    /**
     * Exporting a tin via the XMLStreamWriter given when the class was constructed
     * 
     * @param tin
     *            a {@link Tin} object
     * @throws XMLStreamException
     *             if an error occured writing to the xml stream
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    @Override
    public void exportTin( Tin tin )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        startGeometry( "Tin", tin );

        writer.writeStartElement( gmlNs, "trianglePatches" );
        for ( SurfacePatch sp : tin.getPatches() ) {
            exportSurfacePatch( sp );
        }
        writer.writeEndElement();

        for ( List<LineStringSegment> lsSegments : tin.getStopLines() ) {
            writer.writeStartElement( gmlNs, "stopLines" );
            for ( LineStringSegment lsSeg : lsSegments ) {
                exportLineStringSegment( lsSeg );
            }
            writer.writeEndElement();
        }

        for ( List<LineStringSegment> lsSegments : tin.getBreakLines() ) {
            writer.writeStartElement( gmlNs, "breakLines" );
            for ( LineStringSegment lsSeg : lsSegments ) {
                exportLineStringSegment( lsSeg );
            }
            writer.writeEndElement();
        }

        writer.writeStartElement( gmlNs, "maxLength" );
        writer.writeAttribute( "uom", tin.getMaxLength( null ).getUomUri() );
        writer.writeCharacters( String.valueOf( tin.getMaxLength( null ).getValue() ) );
        writer.writeEndElement();

        writer.writeStartElement( gmlNs, "controlPoint" );
        int dim = tin.getCoordinateDimension();
        export( tin.getControlPoints(), dim );
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void exportPolyhedralSurface( PolyhedralSurface polyhSurf )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        if ( polyhSurf.getId() != null && exportedIds.contains( polyhSurf.getId() ) ) {
            writer.writeEmptyElement( gmlNs, "PolyhedralSurface" );
            writer.writeAttribute( XLNNS, "href", "#" + polyhSurf.getId() );

        } else {
            exportedIds.add( polyhSurf.getId() );
            writer.writeStartElement( gmlNs, "PolyhedralSurface" );
            writer.writeStartElement( gmlNs, "polygonPatches" );
            for ( SurfacePatch surfacePatch : polyhSurf.getPatches() )
                exportSurfacePatch( surfacePatch );
            writer.writeEndElement();
            writer.writeEndElement();
        }
    }

    private void exportPolygon( Polygon polygon )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        startGeometry( "Polygon", polygon );

        Ring exteriorRing = polygon.getExteriorRing();
        if ( exteriorRing.getId() != null && exportedIds.contains( exteriorRing.getId() ) ) {
            writer.writeEmptyElement( gmlNs, "exterior" );
            writer.writeAttribute( XLNNS, "href", "#" + exteriorRing.getId() );
        } else {
            exportedIds.add( exteriorRing.getId() );
            writer.writeStartElement( gmlNs, "exterior" );
            exportRing( exteriorRing );
            writer.writeEndElement();
        }

        if ( polygon.getInteriorRings() != null ) {
            for ( Ring ring : polygon.getInteriorRings() ) {
                if ( ring.getId() != null && exportedIds.contains( ring.getId() ) ) {
                    writer.writeEmptyElement( gmlNs, "interior" );
                    writer.writeAttribute( XLNNS, "href", "#" + ring.getId() );
                } else {
                    exportedIds.add( ring.getId() );
                    writer.writeStartElement( gmlNs, "interior" );
                    exportRing( ring );
                    writer.writeEndElement();
                }
            }
        }
        writer.writeEndElement();
    }

    private void exportOrientableSurface( OrientableSurface orientableSurface )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        startGeometry( "OrientableSurface", orientableSurface );

        Surface baseSurface = orientableSurface.getBaseSurface();
        if ( baseSurface.getId() != null && exportedIds.contains( baseSurface.getId() ) ) {
            writer.writeEmptyElement( gmlNs, "baseSurface" );
            writer.writeAttribute( XLNNS, "href", "#" + baseSurface.getId() );
        } else {
            exportedIds.add( baseSurface.getId() );
            writer.writeStartElement( gmlNs, "baseSurface" );
            exportSurface( orientableSurface.getBaseSurface() );
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    /**
     * Exporting a solid via the XMLStreamWriter given when the class was constructed
     * 
     * @param solid
     *            a {@link Solid} object
     * @throws XMLStreamException
     *             if an error occured writing to the xml stream
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    @Override
    public void exportSolid( Solid solid )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        switch ( solid.getSolidType() ) {

        case Solid:
            startGeometry( "Solid", solid );

            Surface exSurface = solid.getExteriorSurface();
            writer.writeStartElement( gmlNs, "exterior" );
            exportSurface( exSurface );
            writer.writeEndElement();

            for ( Surface inSurface : solid.getInteriorSurfaces() ) {
                writer.writeStartElement( gmlNs, "interior" );
                exportSurface( inSurface );
                writer.writeEndElement();
            }
            writer.writeEndElement();
            break;

        case CompositeSolid:
            exportCompositeSolid( (CompositeSolid) solid );
            break;
        }
    }

    /**
     * Exporting a ring via the XMLStreamWriter given when the class was constructed
     * 
     * @param ring
     *            a {@link Ring} object
     * @throws XMLStreamException
     *             if an error occured writing to the xml stream
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    @Override
    public void exportRing( Ring ring )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        switch ( ring.getRingType() ) {
        case Ring:
            if ( GML_32 != version ) {
                startGeometry( "Ring", ring );
            } else {
                // in GML 3.2, a Ring is not a Geometry object
                writer.writeStartElement( "gml", "Ring", gmlNs );
            }

            for ( Curve c : ring.getMembers() ) {
                writer.writeStartElement( gmlNs, "curveMember" );
                exportCurve( c );
                writer.writeEndElement();
            }
            writer.writeEndElement();
            break;
        case LinearRing:
            LinearRing linearRing = (LinearRing) ring;

            if ( GML_32 != version ) {
                startGeometry( "LinearRing", linearRing );
            } else {
                // in GML 3.2, a LinearRing is not a Geometry object
                writer.writeStartElement( "gml", "LinearRing", gmlNs );
            }

            int dim = linearRing.getCoordinateDimension();
            export( linearRing.getControlPoints(), dim );
            writer.writeEndElement();
            break;
        }
    }

    /**
     * Exporting a composite curve via the XMLStreamWriter given when the class was constructed
     * 
     * @param compositeCurve
     *            the {@link CompositeCurve} object
     * @throws XMLStreamException
     * @throws XMLStreamException
     *             if an error occured writing to the xml stream
     * @throws TransformationException
     * @throws UnknownCRSException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    @Override
    public void exportCompositeCurve( CompositeCurve compositeCurve )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        startGeometry( "CompositeCurve", compositeCurve );

        for ( Curve curve : compositeCurve ) {
            writer.writeStartElement( "gml", "curveMember", gmlNs );
            exportCurve( curve );
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    /**
     * Exporting a composite surface via the XMLStreamWriter given when the class was constructed
     * 
     * @param compositeSurface
     *            the {@link CompositeSurface} object
     * @throws XMLStreamException
     *             if an error occured writing to the xml stream
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    @Override
    public void exportCompositeSurface( CompositeSurface compositeSurface )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        startGeometry( "CompositeSurface", compositeSurface );

        for ( Surface surface : compositeSurface ) {
            writer.writeStartElement( "gml", "surfaceMember", gmlNs );
            exportSurface( surface );
            writer.writeEndElement();
        }

        writer.writeEndElement();
    }

    /**
     * Exporting a composite solid via the XMLStreamWriter given when the class was constructed
     * 
     * @param compositeSolid
     *            the {@link CompositeSolid} object
     * @throws XMLStreamException
     *             if an error occured writing to the xml stream
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    @Override
    public void exportCompositeSolid( CompositeSolid compositeSolid )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        startGeometry( "CompositeSolid", compositeSolid );

        for ( Solid solidMember : compositeSolid ) {
            if ( solidMember.getId() != null && exportedIds.contains( solidMember.getId() ) ) {
                writer.writeEmptyElement( gmlNs, "solidMember" );
                writer.writeAttribute( XLNNS, "href", "#" + solidMember.getId() );
            } else {
                exportedIds.add( solidMember.getId() );
                writer.writeStartElement( gmlNs, "solidMember" );
                exportSolid( solidMember );
                writer.writeEndElement();
            }
        }
        writer.writeEndElement();
    }

    /**
     * Exporting an {@link Envelope} via the XMLStreamWriter given when the class was constructed
     * 
     * @param envelope
     *            the envelope object
     * @throws XMLStreamException
     *             if an error occured writing to the xml stream
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    @Override
    public void exportEnvelope( Envelope envelope )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        Envelope env = getTransformedEnvelope( envelope );

        // Envelope does not have gml:id (actually it is not a gml:Geometry element)
        writer.writeStartElement( "gml", "Envelope", gmlNs );
        if ( outputCRS != null ) {
            writer.writeAttribute( "srsName", outputCRS.getName() );
        } else if ( envelope.getCoordinateSystem() != null ) {
            writer.writeAttribute( "srsName", envelope.getCoordinateSystem().getName() );
        }

        if ( version == GML_30 ) {
            writer.writeStartElement( "gml", "pos", gmlNs );
        } else {
            writer.writeStartElement( "gml", "lowerCorner", gmlNs );
        }
        double[] ordinates = env.getMin().getAsArray();
        writer.writeCharacters( formatter.format( ordinates[0] ) );
        for ( int i = 1; i < ordinates.length; i++ ) {
            writer.writeCharacters( " " + formatter.format( ordinates[i] ) );
        }
        writer.writeEndElement();

        if ( version == GML_30 ) {
            writer.writeStartElement( "gml", "pos", gmlNs );
        } else {
            writer.writeStartElement( "gml", "upperCorner", gmlNs );
        }
        ordinates = env.getMax().getAsArray();
        writer.writeCharacters( formatter.format( ordinates[0] ) );
        for ( int i = 1; i < ordinates.length; i++ ) {
            writer.writeCharacters( " " + formatter.format( ordinates[i] ) );
        }
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private void exportLineStringSegment( LineStringSegment lineStringSeg )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        writer.writeStartElement( gmlNs, "LineStringSegment" );

        writer.writeAttribute( "interpolation", "linear" );
        int dim = lineStringSeg.getCoordinateDimension();
        export( lineStringSeg.getControlPoints(), dim );
        writer.writeEndElement();
    }

    /**
     * Exporting a {@link SurfacePatch} via the XMLStreamWriter given when the class was constructed
     * 
     * @param surfacePatch
     *            a surface patch object
     * @throws XMLStreamException
     *             if an error occured writing to the xml stream
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    protected void exportSurfacePatch( SurfacePatch surfacePatch )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        switch ( surfacePatch.getSurfacePatchType() ) {

        case GRIDDED_SURFACE_PATCH:
            GriddedSurfacePatch gridded = (GriddedSurfacePatch) surfacePatch;

            switch ( gridded.getGriddedSurfaceType() ) {

            case GRIDDED_SURFACE_PATCH:
                // gml:_GriddedSurfacePatch is abstract; only future custom defined types will be treated
                break;

            case CONE:
                exportCone( (Cone) surfacePatch );
                break;

            case CYLINDER:
                exportCylinder( (Cylinder) surfacePatch );
                break;

            case SPHERE:
                exportSphere( (Sphere) surfacePatch );
                break;
            }
            break;

        case POLYGON_PATCH:
            exportPolygonPatch( (PolygonPatch) surfacePatch );
            break;
        }
    }

    private void exportTriangle( Triangle triangle )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        writer.writeStartElement( gmlNs, "Triangle" );

        writer.writeStartElement( gmlNs, "exterior" );
        exportRing( triangle.getExteriorRing() );
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void exportRectangle( Rectangle rectangle )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        writer.writeStartElement( gmlNs, "Rectangle" );

        writer.writeStartElement( gmlNs, "exterior" );
        exportRing( rectangle.getExteriorRing() );
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void exportPolygonPatch( PolygonPatch polygonPatch )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        switch ( polygonPatch.getPolygonPatchType() ) {
        case POLYGON_PATCH:
            writer.writeStartElement( gmlNs, "PolygonPatch" );

            writer.writeStartElement( gmlNs, "exterior" );
            exportRing( polygonPatch.getExteriorRing() );
            writer.writeEndElement();

            for ( Ring ring : polygonPatch.getInteriorRings() ) {
                writer.writeStartElement( gmlNs, "interior" );
                exportRing( ring );
                writer.writeEndElement();
            }
            writer.writeEndElement();
            break;
        case TRIANGLE:
            exportTriangle( (Triangle) polygonPatch );
            break;
        case RECTANGLE:
            exportRectangle( (Rectangle) polygonPatch );
            break;
        }

    }

    private void exportSphere( Sphere sphere )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        writer.writeStartElement( gmlNs, "Sphere" );
        writer.writeAttribute( "horizontalCurveType", "circularArc3Points" );
        writer.writeAttribute( "verticalCurveType", "circularArc3Points" );

        for ( int i = 0; i < sphere.getNumRows(); i++ ) {
            writer.writeStartElement( gmlNs, "row" );
            export( sphere.getRow( i ), 3 ); // srsDimension attribute in posList set to 3
            writer.writeEndElement();
        }

        writer.writeStartElement( gmlNs, "rows" );
        writer.writeCharacters( String.valueOf( sphere.getNumRows() ) );
        writer.writeEndElement();

        writer.writeStartElement( gmlNs, "columns" );
        writer.writeCharacters( String.valueOf( sphere.getNumColumns() ) );
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void exportCylinder( Cylinder cylinder )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        writer.writeStartElement( gmlNs, "Cylinder" );
        writer.writeAttribute( "horizontalCurveType", "circularArc3Points" );
        writer.writeAttribute( "verticalCurveType", "linear" );

        for ( int i = 0; i < cylinder.getNumRows(); i++ ) {
            writer.writeStartElement( gmlNs, "row" );
            export( cylinder.getRow( i ), 3 ); // srsDimension attribute in posList set to 3
            writer.writeEndElement();
        }

        writer.writeStartElement( gmlNs, "rows" );
        writer.writeCharacters( String.valueOf( cylinder.getNumRows() ) );
        writer.writeEndElement();

        writer.writeStartElement( gmlNs, "columns" );
        writer.writeCharacters( String.valueOf( cylinder.getNumColumns() ) );
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void exportCone( Cone cone )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        writer.writeStartElement( gmlNs, "Cone" );
        writer.writeAttribute( "horizontalCurveType", "circularArc3Points" );
        writer.writeAttribute( "verticalCurveType", "linear" );

        for ( int i = 0; i < cone.getNumRows(); i++ ) {
            writer.writeStartElement( gmlNs, "row" );
            export( cone.getRow( i ), 3 ); // srsDimension attribute in posList set to 3
            writer.writeEndElement();
        }

        writer.writeStartElement( gmlNs, "rows" );
        writer.writeCharacters( String.valueOf( cone.getNumRows() ) );
        writer.writeEndElement();

        writer.writeStartElement( gmlNs, "columns" );
        writer.writeCharacters( String.valueOf( cone.getNumColumns() ) );
        writer.writeEndElement();

        writer.writeEndElement();
    }

    /**
     * Exporting a composite geometry via the XMLStreamWriter given when the class was constructed
     * 
     * @param geometryComplex
     *            the {@link CompositeGeometry} object
     * @throws XMLStreamException
     *             if an error occured writing to the xml stream
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    @Override
    public void exportCompositeGeometry( CompositeGeometry<GeometricPrimitive> geometryComplex )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        startGeometry( "GeometricComplex", geometryComplex );

        for ( GeometricPrimitive gp : geometryComplex ) {
            writer.writeStartElement( "gml", "element", gmlNs );
            export( gp );
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    /**
     * Exporting a curve segment via the XMLStreamWriter given when the class was constructed
     * 
     * @param curveSeg
     *            a {@link CurveSegment} object
     * @throws XMLStreamException
     *             if an error occured writing to the xml stream
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    protected void exportCurveSegment( CurveSegment curveSeg )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        switch ( curveSeg.getSegmentType() ) {

        case ARC:
            exportArc( (Arc) curveSeg );
            break;

        case ARC_BY_BULGE:
            exportArcByBulge( (ArcByBulge) curveSeg );
            break;

        case ARC_BY_CENTER_POINT:
            exportArcByCenterPoint( (ArcByCenterPoint) curveSeg );
            break;

        case ARC_STRING:
            exportArcString( (ArcString) curveSeg );
            break;

        case ARC_STRING_BY_BULGE:
            exportArcStringByBulge( (ArcStringByBulge) curveSeg );
            break;

        case BEZIER:
            exportBezier( (Bezier) curveSeg );
            break;

        case BSPLINE:
            exportBSpline( (BSpline) curveSeg );
            break;

        case CIRCLE:
            exportCircle( (Circle) curveSeg );
            break;

        case CIRCLE_BY_CENTER_POINT:
            exportCircleByCenterPoint( (CircleByCenterPoint) curveSeg );
            break;

        case CLOTHOID:
            exportClothoid( (Clothoid) curveSeg );
            break;

        case CUBIC_SPLINE:
            exportCubicSpline( (CubicSpline) curveSeg );
            break;

        case GEODESIC:
            exportGeodesic( (Geodesic) curveSeg );
            break;

        case GEODESIC_STRING:
            exportGeodesicString( (GeodesicString) curveSeg );
            break;

        case LINE_STRING_SEGMENT:
            exportLineStringSegment( (LineStringSegment) curveSeg );
            break;

        case OFFSET_CURVE:
            exportOffsetCurve( (OffsetCurve) curveSeg );
            break;
        }
    }

    private void exportOffsetCurve( OffsetCurve offsetCurve )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        writer.writeStartElement( "gml", "OffsetCurve", gmlNs );

        Curve baseCurve = offsetCurve.getBaseCurve();
        if ( baseCurve.getId() != null && exportedIds.contains( baseCurve.getId() ) ) {
            writer.writeEmptyElement( gmlNs, "offsetBase" );
            writer.writeAttribute( "gml", gmlNs, "href", "#" + baseCurve.getId() );
        } else {
            writer.writeStartElement( "gml", "offsetBase", gmlNs );
            exportCurve( baseCurve );
            writer.writeEndElement();
        }

        writer.writeStartElement( "gml", "distance", gmlNs );
        writer.writeAttribute( "uom", offsetCurve.getDistance( null ).getUomUri() );
        writer.writeCharacters( String.valueOf( offsetCurve.getDistance( null ).getValue() ) );
        writer.writeEndElement();

        writer.writeStartElement( "gml", "refDirection", gmlNs );
        exportAsPos( offsetCurve.getDirection() );
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void exportGeodesicString( GeodesicString geodesicString )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        writer.writeStartElement( "gml", "GeodesicString", gmlNs );
        writer.writeAttribute( "interpolation", "geodesic" );

        int dim = geodesicString.getCoordinateDimension();
        export( geodesicString.getControlPoints(), dim );
        writer.writeEndElement();
    }

    private void exportGeodesic( Geodesic geodesic )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        writer.writeStartElement( "gml", "Geodesic", gmlNs );
        writer.writeAttribute( "interpolation", "geodesic" );

        int geodesicDim = geodesic.getCoordinateDimension();
        export( geodesic.getControlPoints(), geodesicDim );
        writer.writeEndElement();
    }

    private void exportCubicSpline( CubicSpline cubicSpline )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        writer.writeStartElement( "gml", "CubicSpline", gmlNs );
        writer.writeAttribute( "interpolation", "cubicSpline" );
        int dim = cubicSpline.getCoordinateDimension();
        export( cubicSpline.getControlPoints(), dim );

        writer.writeStartElement( "gml", "vectorAtStart", gmlNs );
        double[] array = cubicSpline.getVectorAtStart().getAsArray();
        for ( int i = 0; i < array.length; i++ ) {
            writer.writeCharacters( String.valueOf( array[i] ) + " " );
        }
        writer.writeEndElement();

        writer.writeStartElement( "gml", "vectorAtEnd", gmlNs );
        array = cubicSpline.getVectorAtEnd().getAsArray();
        for ( int i = 0; i < array.length; i++ )
            writer.writeCharacters( String.valueOf( array[i] ) + " " );
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void exportClothoid( Clothoid clothoid )
                            throws XMLStreamException {

        writer.writeStartElement( "gml", "Clothoid", gmlNs );
        writer.writeStartElement( "gml", "refLocation", gmlNs );
        writer.writeStartElement( "gml", "AffinePlacement", gmlNs );

        AffinePlacement affinePlace = clothoid.getReferenceLocation();
        writer.writeStartElement( "gml", "location", gmlNs );
        double[] array = affinePlace.getLocation().getAsArray();
        for ( int i = 0; i < array.length; i++ ) {
            writer.writeCharacters( String.valueOf( array[i] ) + " " );
        }
        writer.writeEndElement();

        for ( Point p : affinePlace.getRefDirections() ) {
            writer.writeStartElement( "gml", "refDirection", gmlNs );
            array = p.getAsArray();
            for ( int i = 0; i < array.length; i++ )
                writer.writeCharacters( String.valueOf( array[i] ) + " " );
            writer.writeEndElement();
        }

        writer.writeStartElement( "gml", "inDimension", gmlNs );
        writer.writeCharacters( String.valueOf( affinePlace.getInDimension() ) );
        writer.writeEndElement();

        writer.writeStartElement( "gml", "outDimension", gmlNs );
        writer.writeCharacters( String.valueOf( affinePlace.getOutDimension() ) );
        writer.writeEndElement();

        writer.writeEndElement(); // AffinePlacement
        writer.writeEndElement(); // refLocation

        writer.writeStartElement( "gml", "scaleFactor", gmlNs );
        writer.writeCharacters( String.valueOf( clothoid.getScaleFactor() ) );
        writer.writeEndElement();

        writer.writeStartElement( "gml", "startParameter", gmlNs );
        writer.writeCharacters( String.valueOf( clothoid.getStartParameter() ) );
        writer.writeEndElement();

        writer.writeStartElement( "gml", "endParameter", gmlNs );
        writer.writeCharacters( String.valueOf( clothoid.getEndParameter() ) );
        writer.writeEndElement();

        writer.writeEndElement(); // Clothoid
    }

    private void exportCircleByCenterPoint( CircleByCenterPoint circleCenterP )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        writer.writeStartElement( "gml", "CircleByCenterPoint", gmlNs );

        writer.writeAttribute( "interpolation", "circularArcCenterPointWithRadius" );
        writer.writeAttribute( "numArc", "1" );

        exportAsPos( circleCenterP.getMidPoint() );

        writer.writeStartElement( "gml", "radius", gmlNs );
        writer.writeAttribute( "uom", circleCenterP.getRadius( null ).getUomUri() );
        writer.writeCharacters( String.valueOf( circleCenterP.getRadius( null ).getValue() ) );
        writer.writeEndElement();

        writer.writeStartElement( "gml", "startAngle", gmlNs );
        writer.writeAttribute( "uom", circleCenterP.getStartAngle().getUomUri() );
        writer.writeCharacters( String.valueOf( circleCenterP.getStartAngle().getValue() ) );
        writer.writeEndElement();

        writer.writeStartElement( "gml", "endAngle", gmlNs );
        writer.writeAttribute( "uom", circleCenterP.getEndAngle().getUomUri() );
        writer.writeCharacters( String.valueOf( circleCenterP.getEndAngle().getValue() ) );
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void exportCircle( Circle circle )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        writer.writeStartElement( "gml", "Circle", gmlNs );

        writer.writeAttribute( "interpolation", "circularArc3Points" );

        int dim = circle.getCoordinateDimension();
        export( circle.getControlPoints(), dim );
        writer.writeEndElement();
    }

    private void exportBSpline( BSpline bSpline )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        writer.writeStartElement( "gml", "BSpline", gmlNs );

        writer.writeAttribute( "interpolation", "polynomialSpline" );

        int dim = bSpline.getCoordinateDimension();
        export( bSpline.getControlPoints(), dim );

        writer.writeStartElement( "gml", "degree", gmlNs );
        writer.writeCharacters( String.valueOf( bSpline.getPolynomialDegree() ) );
        writer.writeEndElement();

        for ( Knot knot : bSpline.getKnots() ) {
            exportKnot( knot );
        }
        writer.writeEndElement();
    }

    private void exportBezier( Bezier bezier )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        writer.writeStartElement( "gml", "Bezier", gmlNs );

        writer.writeAttribute( "interpolation", "polynomialSpline" );

        int dim = bezier.getCoordinateDimension();
        export( bezier.getControlPoints(), dim );

        writer.writeStartElement( "gml", "degree", gmlNs );
        writer.writeCharacters( String.valueOf( bezier.getPolynomialDegree() ) );
        writer.writeEndElement();

        exportKnot( bezier.getKnot1() );
        exportKnot( bezier.getKnot2() );
        writer.writeEndElement();
    }

    private void exportArcStringByBulge( ArcStringByBulge arcStringBulge )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        writer.writeStartElement( "gml", "ArcStringByBulge", gmlNs );

        writer.writeAttribute( "interpolation", "circularArc2PointWithBulge" );
        writer.writeAttribute( "numArc", String.valueOf( arcStringBulge.getNumArcs() ) );

        int dim = arcStringBulge.getCoordinateDimension();
        export( arcStringBulge.getControlPoints(), dim );

        for ( double d : arcStringBulge.getBulges() ) {
            writer.writeStartElement( "gml", "bulge", gmlNs );
            writer.writeCharacters( String.valueOf( d ) );
            writer.writeEndElement();
        }

        for ( Point p : arcStringBulge.getNormals() ) {
            writer.writeStartElement( "gml", "normal", gmlNs );
            double[] array = p.getAsArray();
            int curveSegDim = arcStringBulge.getCoordinateDimension();
            for ( int i = 0; i < curveSegDim - 1; i++ )
                writer.writeCharacters( String.valueOf( array[i] ) + " " );
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void exportArcString( ArcString arcString )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        writer.writeStartElement( "gml", "ArcString", gmlNs );

        writer.writeAttribute( "interpolation", "circularArc3Points" );
        writer.writeAttribute( "numArc", String.valueOf( arcString.getNumArcs() ) );

        int dim = arcString.getCoordinateDimension();
        export( arcString.getControlPoints(), dim );

        writer.writeEndElement();
    }

    private void exportArcByCenterPoint( ArcByCenterPoint arcCenterP )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        writer.writeStartElement( "gml", "ArcByCenterPoint", gmlNs );
        writer.writeAttribute( "interpolation", "circularArcCenterPointWithRadius" );
        writer.writeAttribute( "numArc", "1" ); // TODO have a getNumArcs() method in ArcByCenterPoint ???

        exportAsPos( arcCenterP.getMidPoint() );

        writer.writeStartElement( "gml", "radius", gmlNs );
        writer.writeAttribute( "uom", arcCenterP.getRadius( null ).getUomUri() );
        writer.writeCharacters( String.valueOf( arcCenterP.getRadius( null ).getValue() ) );
        writer.writeEndElement();

        writer.writeStartElement( "gml", "startAngle", gmlNs );
        writer.writeAttribute( "uom", arcCenterP.getStartAngle().getUomUri() );
        writer.writeCharacters( String.valueOf( arcCenterP.getStartAngle().getValue() ) );
        writer.writeEndElement();

        writer.writeStartElement( "gml", "endAngle", gmlNs );
        writer.writeAttribute( "uom", arcCenterP.getEndAngle().getUomUri() );
        writer.writeCharacters( String.valueOf( arcCenterP.getEndAngle().getValue() ) );
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void exportArcByBulge( ArcByBulge arcBulge )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        writer.writeStartElement( "gml", "ArcByBulge", gmlNs );
        exportAsPos( arcBulge.getPoint1() );
        exportAsPos( arcBulge.getPoint2() );

        writer.writeStartElement( "gml", "bulge", gmlNs );
        writer.writeCharacters( String.valueOf( arcBulge.getBulge() ) );
        writer.writeEndElement();

        writer.writeStartElement( "gml", "normal", gmlNs );
        writer.writeCharacters( String.valueOf( arcBulge.getNormal().get0() ) );
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void exportArc( Arc arc )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        writer.writeStartElement( "gml", "Arc", gmlNs );
        export( arc.getControlPoints(), -1 );
        // exportAsPos( arc.getPoint1() );
        // exportAsPos( arc.getPoint2() );
        // exportAsPos( arc.getPoint3() );
        writer.writeEndElement();
    }

    private void exportKnot( Knot knot )
                            throws XMLStreamException {
        writer.writeStartElement( "gml", "knot", gmlNs );

        writer.writeStartElement( "gml", "Knot", gmlNs );

        writer.writeStartElement( "gml", "value", gmlNs );
        writer.writeCharacters( String.valueOf( knot.getValue() ) );
        writer.writeEndElement();

        writer.writeStartElement( "gml", "multiplicity", gmlNs );
        writer.writeCharacters( String.valueOf( knot.getMultiplicity() ) );
        writer.writeEndElement();

        writer.writeStartElement( "gml", "weight", gmlNs );
        writer.writeCharacters( String.valueOf( knot.getWeight() ) );
        writer.writeEndElement();

        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void export( Points points, int srsDimension )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        boolean hasID = false; // see if there exists a point that has an ID
        for ( Point p : points ) {
            if ( p.getId() != null && p.getId().trim().length() > 0 ) {
                hasID = true;
                break;
            }
        }
        if ( !hasID ) { // if not then use the <posList> element to export the points
            if ( version != GML_30 ) {
                writer.writeStartElement( "gml", "posList", gmlNs );

                // TODO CITE
                // writer.writeAttribute( "srsDimension", String.valueOf( srsDimension ) );
                boolean first = true;
                for ( Point p : points ) {
                    double[] ordinates = getTransformedCoordinate( p.getCoordinateSystem(), p.getAsArray() );
                    for ( int i = 0; i < ordinates.length; i++ ) {
                        if ( !first ) {
                            writer.writeCharacters( " " + formatter.format( ordinates[i] ) );
                        } else {
                            writer.writeCharacters( formatter.format( ordinates[i] ) );
                            first = false;
                        }
                    }
                }
                writer.writeEndElement();
            } else {
                for ( Point p : points ) {
                    exportAsPos( p );
                }
            }
        } else { // if there are points with IDs, see whether an ID was already encountered
            for ( Point point : points ) {
                writer.writeStartElement( "gml", "pointProperty", gmlNs );
                if ( point.getId() != null && exportedIds.contains( point.getId() ) ) {
                    writer.writeAttribute( XLNNS, "href", "#" + point.getId() );
                } else {
                    export( point );
                }
                writer.writeEndElement();
            }
        }
    }

    private void startGeometry( String localName, Geometry geometry )
                            throws XMLStreamException {

        writer.writeStartElement( "gml", localName, gmlNs );

        if ( !exportSf && geometry.getId() != null ) {
            exportedIds.add( geometry.getId() );
            writer.writeAttribute( "gml", gmlNs, "id", geometry.getId() );
        } else if ( version == GML_32 && geometry.getId() == null ) {
            // in GML 3.2, a gml:id is required for every geometry
            writer.writeAttribute( "gml", gmlNs, "id", "GEOMETRY_" + generateNewId() );
        }

        if ( outputCRS != null ) {
            writer.writeAttribute( "srsName", outputCRS.getName() );
        } else if ( geometry.getCoordinateSystem() != null ) {
            writer.writeAttribute( "srsName", geometry.getCoordinateSystem().getName() );
        }

        GMLStdProps props = geometry.getGMLProperties();
        if ( props != null ) {
            stdPropsWriter.write( props );
        }
    }

    private String generateNewId() {
        return UUID.randomUUID().toString();
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
}
