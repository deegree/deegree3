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

package org.deegree.feature.gml;

import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;

import java.util.HashSet;
import java.util.Iterator;
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
import org.deegree.geometry.primitive.curvesegments.Arc;
import org.deegree.geometry.primitive.curvesegments.ArcByBulge;
import org.deegree.geometry.primitive.curvesegments.ArcByCenterPoint;
import org.deegree.geometry.primitive.curvesegments.ArcString;
import org.deegree.geometry.primitive.curvesegments.ArcStringByBulge;
import org.deegree.geometry.primitive.curvesegments.BSpline;
import org.deegree.geometry.primitive.curvesegments.Bezier;
import org.deegree.geometry.primitive.curvesegments.Circle;
import org.deegree.geometry.primitive.curvesegments.CircleByCenterPoint;
import org.deegree.geometry.primitive.curvesegments.Clothoid;
import org.deegree.geometry.primitive.curvesegments.CubicSpline;
import org.deegree.geometry.primitive.curvesegments.CurveSegment;
import org.deegree.geometry.primitive.curvesegments.Geodesic;
import org.deegree.geometry.primitive.curvesegments.GeodesicString;
import org.deegree.geometry.primitive.curvesegments.Knot;
import org.deegree.geometry.primitive.curvesegments.LineStringSegment;
import org.deegree.geometry.primitive.curvesegments.OffsetCurve;
import org.deegree.geometry.primitive.surfacepatches.Cone;
import org.deegree.geometry.primitive.surfacepatches.Cylinder;
import org.deegree.geometry.primitive.surfacepatches.GriddedSurfacePatch;
import org.deegree.geometry.primitive.surfacepatches.PolygonPatch;
import org.deegree.geometry.primitive.surfacepatches.Rectangle;
import org.deegree.geometry.primitive.surfacepatches.Sphere;
import org.deegree.geometry.primitive.surfacepatches.SurfacePatch;
import org.deegree.geometry.primitive.surfacepatches.Triangle;
import org.deegree.geometry.standard.curvesegments.AffinePlacement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exporter class for Geometries. TODO add more details
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: ionita $
 * 
 * @version $Revision: $, $Date: $
 */
public class GML311GeometryExporter {

    private static final Logger LOG = LoggerFactory.getLogger( GML311GeometryExporter.class );

    private XMLStreamWriter writer;

    private Set<String> exportedIds;

    public GML311GeometryExporter( XMLStreamWriter writer ) {
        this.writer = writer;
        exportedIds = new HashSet<String>();
    }

    public GML311GeometryExporter( XMLStreamWriter writer, Set<String> exportedIds ) {
        this.writer = writer;
        this.exportedIds = exportedIds;
    }

    public void export( Geometry geometry )
                            throws XMLStreamException {
        switch ( geometry.getGeometryType() ) {
        case COMPOSITE_GEOMETRY:
            if ( geometry instanceof CompositeCurve ) {
                export( (CompositeCurve) geometry );
            } else if ( geometry instanceof CompositeSurface ) {
                export( (CompositeSurface) geometry );
            } else if ( geometry instanceof CompositeSolid ) {
                export( (CompositeSolid) geometry );
            } else { // should be GeometricComplex
                export( (CompositeGeometry<GeometricPrimitive>) geometry );
            }
            break;
        case ENVELOPE:
            export( (Envelope) geometry );
            break;
        case MULTI_GEOMETRY:
            switch ( ( (MultiGeometry<Geometry>) geometry ).getMultiGeometryType() ) {
            case MULTI_CURVE:
                MultiCurve multiCurve = (MultiCurve) geometry;
                writer.writeStartElement( GMLNS, "MultiCurve" );
                if ( multiCurve.getId() != null )
                    writer.writeAttribute( GMLNS, "id", multiCurve.getId() );
                writer.writeAttribute( "srsName", multiCurve.getCoordinateSystem().getName() );
                writer.writeStartElement( GMLNS, "curveMembers" );
                Iterator<Curve> iterator = multiCurve.iterator();
                while ( iterator.hasNext() ) {
                    Object currentCurve = iterator.next();
                    if ( currentCurve instanceof CompositeCurve )
                        export( (CompositeCurve) currentCurve );
                    else if ( currentCurve instanceof Curve )
                        export( (Curve) currentCurve );
                }
                writer.writeEndElement();
                writer.writeEndElement();
                break;
            case MULTI_GEOMETRY:
                MultiGeometry<Geometry> multiGeometry = (MultiGeometry<Geometry>) geometry;
                writer.writeStartElement( GMLNS, "MultiGeometry" );
                if ( multiGeometry.getId() != null )
                    writer.writeAttribute( GMLNS, "id", multiGeometry.getId() );
                writer.writeAttribute( "srsName", multiGeometry.getCoordinateSystem().getName() );
                writer.writeStartElement( GMLNS, "geometryMembers" );
                Iterator<Geometry> iteratorG = multiGeometry.iterator();
                while ( iteratorG.hasNext() )
                    export( iteratorG.next() );
                writer.writeEndElement();
                writer.writeEndElement();
                break;
            case MULTI_LINE_STRING:
                MultiLineString multiLineString = (MultiLineString) geometry;
                writer.writeStartElement( GMLNS, "MultiLineString" );
                if ( multiLineString.getId() != null )
                    writer.writeAttribute( GMLNS, "id", multiLineString.getId() );
                writer.writeAttribute( "srsName", multiLineString.getCoordinateSystem().getName() );
                Iterator<LineString> iteratorLS = multiLineString.iterator();
                while ( iteratorLS.hasNext() ) {
                    writer.writeStartElement( GMLNS, "lineStringMember" );
                    export( iteratorLS.next() );
                    writer.writeEndElement();
                }
                writer.writeEndElement();
                break;
            case MULTI_POINT:
                MultiPoint multiPoint = (MultiPoint) geometry;
                writer.writeStartElement( GMLNS, "MultiPoint" );
                if ( multiPoint.getId() != null )
                    writer.writeAttribute( GMLNS, "id", multiPoint.getId() );
                writer.writeAttribute( "srsName", multiPoint.getCoordinateSystem().getName() );
                Iterator<Point> iteratorP = multiPoint.iterator();
                while ( iteratorP.hasNext() ) {
                    writer.writeStartElement( GMLNS, "pointMember" );
                    writer.writeStartElement( GMLNS, "Point" );
                    export( iteratorP.next() );
                    writer.writeEndElement();
                    writer.writeEndElement();
                }
                writer.writeEndElement();
                break;
            case MULTI_POLYGON:
                LOG.debug( "Exporting Geometry with ID " + geometry.getId() );
                MultiPolygon multiPolygon = (MultiPolygon) geometry;
                writer.writeStartElement( GMLNS, "MultiPolygon" );
                if ( multiPolygon.getId() != null )
                    writer.writeAttribute( GMLNS, "id", multiPolygon.getId() );
                writer.writeAttribute( "srsName", multiPolygon.getCoordinateSystem().getName() );
                Iterator<Polygon> iteratorPol = multiPolygon.iterator();
                while ( iteratorPol.hasNext() ) {
                    writer.writeStartElement( GMLNS, "polygonMember" );
                    export( iteratorPol.next() );
                    writer.writeEndElement();
                }
                writer.writeEndElement();
                break;
            case MULTI_SOLID:
                MultiSolid multiSolid = (MultiSolid) geometry;
                writer.writeStartElement( GMLNS, "MultiSolid" );
                if ( multiSolid.getId() != null )
                    writer.writeAttribute( GMLNS, "id", multiSolid.getId() );
                writer.writeAttribute( "srsName", multiSolid.getCoordinateSystem().getName() );
                writer.writeStartElement( GMLNS, "solidMembers" );
                Iterator<Solid> iterator3 = multiSolid.iterator();
                while ( iterator3.hasNext() ) {
                    Object currentSolid = iterator3.next();
                    if ( currentSolid instanceof CompositeSolid )
                        export( (CompositeSolid) currentSolid );
                    else if ( currentSolid instanceof Solid )
                        export( (Solid) currentSolid );
                }
                writer.writeEndElement();
                writer.writeEndElement();

                break;
            case MULTI_SURFACE:
                MultiSurface multiSurface = (MultiSurface) geometry;
                writer.writeStartElement( GMLNS, "MultiSurface" );
                if ( multiSurface.getId() != null )
                    writer.writeAttribute( GMLNS, "id", multiSurface.getId() );
                writer.writeAttribute( "srsName", multiSurface.getCoordinateSystem().getName() );
                writer.writeStartElement( GMLNS, "surfaceMembers" );
                Iterator<Surface> iterator2 = multiSurface.iterator();
                while ( iterator2.hasNext() ) {
                    Object currentSurface = iterator2.next();
                    if ( currentSurface instanceof CompositeSurface )
                        export( (CompositeSurface) currentSurface );
                    else if ( currentSurface instanceof Surface )
                        export( (Surface) currentSurface );
                }
                writer.writeEndElement();
                writer.writeEndElement();
                break;
            }
            break;
        case PRIMITIVE_GEOMETRY:
            switch ( ( (GeometricPrimitive) geometry ).getPrimitiveType() ) {
            case Curve:
                export( (Curve) geometry );
                break;
            case Point:
                writer.writeStartElement( GMLNS, "Point" );
                Point point = (Point) geometry;
                if ( point.getId() != null && exportedIds.contains( point.getId() ) ) {
                    writer.writeStartElement( GMLNS, "pointProperty" );
                    writer.writeAttribute( XLNNS, "href", "#" + point.getId() );
                    writer.writeEndElement();
                } else {
                    exportedIds.add( point.getId() );
                    if ( point.getId() != null )
                        writer.writeAttribute( GMLNS, "id", point.getId() );
                    writer.writeStartElement( GMLNS, "pos" );
                    double[] array = point.getAsArray();
                    for ( int i = 0; i < array.length; i++ )
                        writer.writeCharacters( String.valueOf( array[i] ) + " " );
                    writer.writeEndElement();
                }
                writer.writeEndElement();
                break;
            case Solid:
                export( (Solid) geometry );
                break;
            case Surface:
                export( (Surface) geometry );
                break;
            }
            break;
        }
    }

    public void export( Point point )
                            throws XMLStreamException {
        if ( point.getId() != null && exportedIds.contains( point.getId() ) ) {
            writer.writeEmptyElement( GMLNS, "pointProperty" );
            writer.writeAttribute( XLNNS, "href", "#" + point.getId() );
        } else {
            exportedIds.add( point.getId() );
            writer.writeStartElement( GMLNS, "pos" );
            double[] array = point.getAsArray();
            for ( int i = 0; i < array.length; i++ )
                writer.writeCharacters( String.valueOf( array[i] ) + " " );
            writer.writeEndElement();
        }
    }

    public void export( Curve curve )
                            throws XMLStreamException {
        switch ( curve.getCurveType() ) {
        case CompositeCurve:
            CompositeCurve compositeCurve = (CompositeCurve) curve;
            writer.writeStartElement( GMLNS, "CompositeCurve" );
            if ( compositeCurve.getId() != null )
                writer.writeAttribute( GMLNS, "id", compositeCurve.getId() );
            writer.writeAttribute( "srsName", compositeCurve.getCoordinateSystem().getName() );
            Iterator<Curve> iterator = compositeCurve.iterator();
            while ( iterator.hasNext() ) {
                writer.writeStartElement( GMLNS, "curveMember" );
                export( iterator.next() );
                writer.writeEndElement();
            }
            writer.writeEndElement();
            break;
        case Curve:
            writer.writeStartElement( GMLNS, "Curve" );
            if ( curve.getId() != null )
                writer.writeAttribute( GMLNS, "id", curve.getId() );
            writer.writeAttribute( "srsName", curve.getCoordinateSystem().getName() );
            writer.writeStartElement( GMLNS, "segments" );
            for ( CurveSegment curveSeg : curve.getCurveSegments() )
                export( curveSeg );
            writer.writeEndElement();
            writer.writeEndElement();
            break;
        case LineString:
            writer.writeStartElement( GMLNS, "LineString" );
            LineString lineString = (LineString) curve;
            if ( lineString.getId() != null )
                writer.writeAttribute( GMLNS, "id", lineString.getId() );
            int dim =  lineString.getCoordinateDimension();
            export( lineString.getControlPoints(), dim );
            writer.writeEndElement();
            break;
        case OrientableCurve:
            writer.writeStartElement( GMLNS, "OrientableCurve" );
            OrientableCurve orientableCurve = (OrientableCurve) curve;
            if ( orientableCurve.getId() != null )
                writer.writeAttribute( GMLNS, "id", orientableCurve.getId() );
            writer.writeAttribute( "orientation", orientableCurve.isReversed() ? "-" : "+" );
            Curve baseCurve = orientableCurve.getBaseCurve();
            if ( baseCurve.getId() != null && exportedIds.contains( baseCurve.getId() ) ) {
                writer.writeEmptyElement( GMLNS, "baseCurve" );
                writer.writeAttribute( XLNNS, "href", "#" + baseCurve.getId() );
                writer.writeEndElement();
            } else {
                writer.writeStartElement( GMLNS, "baseCurve" );
                export( baseCurve );
                writer.writeEndElement();
            }
            writer.writeEndElement();
            break;
        case Ring:
            export( (Ring) curve );
            break;
        }
    }

    public void export( Surface surface )
                            throws XMLStreamException {
        switch ( surface.getSurfaceType() ) {
        case CompositeSurface:
            CompositeSurface compSurface = (CompositeSurface) surface;
            writer.writeStartElement( GMLNS, "CompositeSurface" );
            if ( compSurface.getId() != null )
                writer.writeAttribute( GMLNS, "id", compSurface.getId() );
            writer.writeAttribute( "srsName", compSurface.getCoordinateSystem().getName() );
            Iterator<Surface> iterator = compSurface.iterator();
            while ( iterator.hasNext() ) {
                writer.writeStartElement( GMLNS, "surfaceMember" );
                export( iterator.next() );
                writer.writeEndElement();
            }
            writer.writeEndElement();
            break;
        case OrientableSurface:
            OrientableSurface orientableSurface = (OrientableSurface) surface;
            writer.writeStartElement( GMLNS, "OrientableSurface" );
            Surface baseSurface = orientableSurface.getBaseSurface();
            if ( baseSurface.getId() != null && exportedIds.contains( baseSurface.getId() ) ) {
                writer.writeEmptyElement( GMLNS, "baseSurface" );
                writer.writeAttribute( XLNNS, "href", "#" + baseSurface.getId() );
            } else {
                exportedIds.add( baseSurface.getId() );
                writer.writeStartElement( GMLNS, "baseSurface" );
                export( orientableSurface.getBaseSurface() );
                writer.writeEndElement();
            }
            writer.writeEndElement();
            break;
        case Polygon:
            Polygon polygon = (Polygon) surface;
            writer.writeStartElement( GMLNS, "Polygon" );
            if ( polygon.getId() != null )
                writer.writeAttribute( GMLNS, "id", polygon.getId() );
            writer.writeAttribute( "srsName", polygon.getCoordinateSystem().getName() );
            Ring exteriorRing = polygon.getExteriorRing();
            if ( exteriorRing.getId() != null && exportedIds.contains( exteriorRing.getId() ) ) {
                writer.writeEmptyElement( GMLNS, "exterior" );
                writer.writeAttribute( XLNNS, "href", "#" + exteriorRing.getId() );
            } else {
                exportedIds.add( exteriorRing.getId() );
                writer.writeStartElement( GMLNS, "exterior" );
                export( exteriorRing );
                writer.writeEndElement();
            }
            if ( polygon.getInteriorRings() != null ) {
                for ( Ring ring : polygon.getInteriorRings() ) {
                    if ( ring.getId() != null && exportedIds.contains( ring.getId() ) ) {
                        writer.writeEmptyElement( GMLNS, "interior" );
                        writer.writeAttribute( XLNNS, "href", "#" + ring.getId() );
                    } else {
                        exportedIds.add( ring.getId() );
                        writer.writeStartElement( GMLNS, "interior" );
                        export( ring );
                        writer.writeEndElement();
                    }
                }
            }
            writer.writeEndElement();
            break;
        case PolyhedralSurface:
            if ( surface.getId() != null && exportedIds.contains( surface.getId() ) ) {
                writer.writeEmptyElement( GMLNS, "PolyhedralSurface" );
                writer.writeAttribute( XLNNS, "href", "#" + surface.getId() );
            } else {
                exportedIds.add( surface.getId() );
                PolyhedralSurface polyhSurf = (PolyhedralSurface) surface;
                writer.writeStartElement( GMLNS, "PolyhedralSurface" );
                writer.writeStartElement( GMLNS, "polygonPatches" );
                for ( SurfacePatch surfacePatch : polyhSurf.getPatches() )
                    export( surfacePatch );
                writer.writeEndElement();
                writer.writeEndElement();
            }
            break;
        case Surface:
            writer.writeStartElement( GMLNS, "Surface" );
            writer.writeStartElement( GMLNS, "patches" );
            for ( SurfacePatch surfacePatch : surface.getPatches() )
                export( surfacePatch );
            writer.writeEndElement();
            writer.writeEndElement();
            break;
        case Tin:
            Tin tin = (Tin) surface;
            writer.writeStartElement( GMLNS, "Tin" );
            if ( tin.getId() != null )
                writer.writeAttribute( GMLNS, "id", tin.getId() );
            writer.writeAttribute( "srsName", tin.getCoordinateSystem().getName() );
            writer.writeStartElement( GMLNS, "trianglePatches" );
            for ( SurfacePatch sp : tin.getPatches() )
                export( sp );
            writer.writeEndElement();
            for ( List<LineStringSegment> lsSegments : tin.getStopLines() ) {
                writer.writeStartElement( GMLNS, "stopLines" );
                for ( LineStringSegment lsSeg : lsSegments )
                    export( lsSeg );
                writer.writeEndElement();
            }
            for ( List<LineStringSegment> lsSegments : tin.getBreakLines() ) {
                writer.writeStartElement( GMLNS, "breakLines" );
                for ( LineStringSegment lsSeg : lsSegments )
                    export( lsSeg );
                writer.writeEndElement();
            }
            writer.writeStartElement( GMLNS, "maxLength" );
            writer.writeAttribute( "uom", tin.getMaxLength().getUomUri() );
            writer.writeCharacters( String.valueOf( tin.getMaxLength().getValue() ) );
            writer.writeEndElement();
            writer.writeStartElement( GMLNS, "controlPoint" );
            int dim = tin.getCoordinateDimension();
            export( tin.getControlPoints(), dim );
            writer.writeEndElement();
            writer.writeEndElement();
            break;
        case TriangulatedSurface:
            writer.writeStartElement( GMLNS, "TriangulatedSurface" );
            if ( surface.getId() != null && exportedIds.contains( surface.getId() ) ) {
                writer.writeEmptyElement( GMLNS, "trianglePatches" );
                writer.writeAttribute( XLNNS, "href", "#" + surface.getId() );
            } else {
                exportedIds.add( surface.getId() );
                TriangulatedSurface triangSurface = (TriangulatedSurface) surface;
                writer.writeStartElement( GMLNS, "trianglePatches" );
                for ( SurfacePatch surfacePatch : triangSurface.getPatches() )
                    export( surfacePatch );
                writer.writeEndElement();
            }
            writer.writeEndElement();
            break;
        }
    }

    void export( Solid solid )
                            throws XMLStreamException {
        switch ( solid.getSolidType() ) {
        case Solid:
            writer.writeStartElement( GMLNS, "Solid" );
            if ( solid.getId() != null )
                writer.writeAttribute( GMLNS, "id", solid.getId() );
            writer.writeAttribute( "srsName", solid.getCoordinateSystem().getName() );
            Surface exSurface = solid.getExteriorSurface();
            writer.writeStartElement( GMLNS, "exterior" );
            export( exSurface );
            writer.writeEndElement();
            for ( Surface inSurface : solid.getInteriorSurfaces() ) {
                writer.writeStartElement( GMLNS, "interior" );
                export( inSurface );
                writer.writeEndElement();
            }
            writer.writeEndElement();
            break;
        case CompositeSolid:
            CompositeSolid compositeSolid = (CompositeSolid) solid;
            writer.writeStartElement( GMLNS, "CompositeSolid" );
            if ( compositeSolid.getId() != null )
                writer.writeAttribute( GMLNS, "id", solid.getId() );
            writer.writeAttribute( "srsName", compositeSolid.getCoordinateSystem().getName() );
            Iterator<Solid> iterator = compositeSolid.iterator();
            while ( iterator.hasNext() ) {
                Solid solidMember = iterator.next();
                if ( solidMember.getId() != null && exportedIds.contains( solidMember.getId() ) ) {
                    writer.writeEmptyElement( GMLNS, "solidMember" );
                    writer.writeAttribute( XLNNS, "href", "#" + solidMember.getId() );
                } else {
                    exportedIds.add( solidMember.getId() );
                    writer.writeStartElement( GMLNS, "solidMember" );
                    export( solidMember );
                    writer.writeEndElement();
                }
            }
            writer.writeEndElement();
            break;
        }
    }

    public void export( Ring ring )
                            throws XMLStreamException {
        switch ( ring.getRingType() ) {
        case Ring:
            writer.writeStartElement( GMLNS, "Ring" );
            if ( ring.getId() != null )
                writer.writeAttribute( GMLNS, "id", ring.getId() );
            writer.writeAttribute( "srsName", ring.getCoordinateSystem().getName() );
            for ( Curve c : ring.getMembers() ) {
                writer.writeStartElement( GMLNS, "curveMember" );
                export( c );
                writer.writeEndElement();
            }
            writer.writeEndElement();
            break;
        case LinearRing:
            LinearRing linearRing = (LinearRing) ring;
            writer.writeStartElement( GMLNS, "LinearRing" );
            if ( linearRing.getCoordinateSystem() != null )
                writer.writeAttribute( "srsName", linearRing.getCoordinateSystem().getName() );
            int dim = linearRing.getCoordinateDimension();
            export( linearRing.getControlPoints(), dim );
            writer.writeEndElement();
            break;
        }
    }

    public void export( CompositeCurve compositeCurve )
                            throws XMLStreamException {
        writer.writeStartElement( GMLNS, "CompositeCurve" );
        if ( compositeCurve.getId() != null )
            writer.writeAttribute( GMLNS, "id", compositeCurve.getId() );
        Iterator<Curve> iterator = compositeCurve.iterator();
        while ( iterator.hasNext() ) {
            writer.writeStartElement( GMLNS, "curveMember" );
            export( iterator.next() );
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    public void export( CompositeSurface compositeSurface )
                            throws XMLStreamException {
        writer.writeStartElement( GMLNS, "CompositeSurface" );
        if ( compositeSurface.getId() != null )
            writer.writeAttribute( GMLNS, "id", compositeSurface.getId() );
        writer.writeStartElement( GMLNS, "surfaceMember" );
        Iterator<Surface> iterator = compositeSurface.iterator();
        while ( iterator.hasNext() ) {
            export( iterator.next() );
        }
        writer.writeEndElement();
        writer.writeEndElement();
    }

    public void export( CompositeSolid compositeSolid )
                            throws XMLStreamException {
        writer.writeStartElement( GMLNS, "CompositeSolid" );
        if ( compositeSolid.getId() != null )
            writer.writeAttribute( GMLNS, "id", compositeSolid.getId() );
        writer.writeStartElement( GMLNS, "solidMember" );
        Iterator<Solid> iterator = compositeSolid.iterator();
        while ( iterator.hasNext() ) {
            export( iterator.next() );
        }
        writer.writeEndElement();
        writer.writeEndElement();
    }

    public void export( Envelope envelope )
                            throws XMLStreamException {
        writer.writeStartElement( GMLNS, "Envelope" );
        writer.writeStartElement( GMLNS, "lowerCorner" );
        double[] array = envelope.getMin().getAsArray();
        for ( int i = 0; i < array.length; i++ )
            writer.writeCharacters( String.valueOf( array[i] ) + " " );
        writer.writeEndElement();
        writer.writeStartElement( GMLNS, "upperCorner" );
        array = envelope.getMax().getAsArray();
        for ( int i = 0; i < array.length; i++ )
            writer.writeCharacters( String.valueOf( array[i] ) + " " );
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private void export( LineStringSegment lineStringSeg )
                            throws XMLStreamException {
        writer.writeStartElement( GMLNS, "LineStringSegment" );
        writer.writeAttribute( "interpolation", lineStringSeg.getInterpolation().toString() );
        int dim = lineStringSeg.getCoordinateDimension();
        export( lineStringSeg.getControlPoints(), dim );
        writer.writeEndElement();
    }

    void export( SurfacePatch surfacePatch )
                            throws XMLStreamException {
        switch ( surfacePatch.getSurfacePatchType() ) {
        case GRIDDED_SURFACE_PATCH:
            GriddedSurfacePatch gridded = (GriddedSurfacePatch) surfacePatch;
            switch ( gridded.getGriddedSurfaceType() ) {
            case GRIDDED_SURFACE_PATCH:
                // gml:_GriddedSurfacePatch is abstract; only future custom defined types will be treated
                break;
            case CONE:
                writer.writeStartElement( GMLNS, "Cone" );
                writer.writeAttribute( "horizontalCurveType", "circularArc3Points" );
                writer.writeAttribute( "verticalCurveType", "linear" );
                Cone cone = (Cone) surfacePatch;
                for ( int i = 0; i < cone.getNumRows(); i++ ) {
                    writer.writeStartElement( GMLNS, "row" );
                    export( cone.getRow( i ), 3 ); // srsDimension attribute in posList set to 3
                    writer.writeEndElement();
                }
                writer.writeStartElement( GMLNS, "rows" );
                writer.writeCharacters( String.valueOf( cone.getNumRows() ) );
                writer.writeEndElement();
                writer.writeStartElement( GMLNS, "columns" );
                writer.writeCharacters( String.valueOf( cone.getNumColumns() ) );
                writer.writeEndElement();
                writer.writeEndElement();
                break;
            case CYLINDER:
                writer.writeStartElement( GMLNS, "Cylinder" );
                writer.writeAttribute( "horizontalCurveType", "circularArc3Points" );
                writer.writeAttribute( "verticalCurveType", "linear" );
                Cylinder cylinder = (Cylinder) surfacePatch;
                for ( int i = 0; i < cylinder.getNumRows(); i++ ) {
                    writer.writeStartElement( GMLNS, "row" );
                    export( cylinder.getRow( i ), 3 ); // srsDimension attribute in posList set to 3
                    writer.writeEndElement();
                }
                writer.writeStartElement( GMLNS, "rows" );
                writer.writeCharacters( String.valueOf( cylinder.getNumRows() ) );
                writer.writeEndElement();
                writer.writeStartElement( GMLNS, "columns" );
                writer.writeCharacters( String.valueOf( cylinder.getNumColumns() ) );
                writer.writeEndElement();
                writer.writeEndElement();
                break;
            case SPHERE:
                writer.writeStartElement( GMLNS, "Sphere" );
                writer.writeAttribute( "horizontalCurveType", "circularArc3Points" );
                writer.writeAttribute( "verticalCurveType", "circularArc3Points" );
                Sphere sphere = (Sphere) surfacePatch;
                for ( int i = 0; i < sphere.getNumRows(); i++ ) {
                    writer.writeStartElement( GMLNS, "row" );
                    export( sphere.getRow( i ), 3 ); // srsDimension attribute in posList set to 3
                    writer.writeEndElement();
                }
                writer.writeStartElement( GMLNS, "rows" );
                writer.writeCharacters( String.valueOf( sphere.getNumRows() ) );
                writer.writeEndElement();
                writer.writeStartElement( GMLNS, "columns" );
                writer.writeCharacters( String.valueOf( sphere.getNumColumns() ) );
                writer.writeEndElement();
                writer.writeEndElement();
                break;
            }
            break;
        case POLYGON_PATCH:
            PolygonPatch polygonPatch = (PolygonPatch) surfacePatch;
            writer.writeStartElement( GMLNS, "PolygonPatch" );
            writer.writeStartElement( GMLNS, "exterior" );
            export( polygonPatch.getExteriorRing() );
            writer.writeEndElement();
            for ( Ring ring : polygonPatch.getInteriorRings() ) {
                writer.writeStartElement( GMLNS, "interior" );
                export( ring );
                writer.writeEndElement();
            }
            writer.writeEndElement();
            break;
        case RECTANGLE:
            Rectangle rectangle = (Rectangle) surfacePatch;
            writer.writeStartElement( GMLNS, "Rectangle" );
            writer.writeStartElement( GMLNS, "exterior" );
            export( rectangle.getExteriorRing() );
            writer.writeEndElement();
            writer.writeEndElement();
            break;
        case TRIANGLE:
            Triangle triangle = (Triangle) surfacePatch;
            writer.writeStartElement( GMLNS, "Triangle" );
            writer.writeStartElement( GMLNS, "exterior" );
            export( triangle.getExteriorRing() );
            writer.writeEndElement();
            writer.writeEndElement();
            break;
        }
    }

    void export( CompositeGeometry<GeometricPrimitive> geometryComplex )
                            throws XMLStreamException {
        writer.writeStartElement( GMLNS, "GeometricComplex" );
        if ( geometryComplex.getId() != null )
            writer.writeAttribute( GMLNS, "id", geometryComplex.getId() );
        writer.writeAttribute( "srsName", geometryComplex.getCoordinateSystem().getName() );
        Iterator<GeometricPrimitive> iterator = geometryComplex.iterator();
        while ( iterator.hasNext() ) {
            writer.writeStartElement( GMLNS, "element" );
            GeometricPrimitive member = iterator.next();
            export( member );
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    void export( CurveSegment curveSeg )
                            throws XMLStreamException {
        switch ( curveSeg.getSegmentType() ) {
        case ARC:
            writer.writeStartElement( GMLNS, "Arc" );
            Arc arc = (Arc) curveSeg;
            export( arc.getPoint1() );
            export( arc.getPoint2() );
            export( arc.getPoint3() );
            writer.writeEndElement();
            break;
        case ARC_BY_BULGE:
            writer.writeStartElement( GMLNS, "ArcByBulge" );
            ArcByBulge arcBulge = (ArcByBulge) curveSeg;
            export( arcBulge.getPoint1() );
            export( arcBulge.getPoint2() );
            writer.writeStartElement( GMLNS, "bulge" );
            writer.writeCharacters( String.valueOf( arcBulge.getBulge() ) );
            writer.writeEndElement();
            writer.writeStartElement( GMLNS, "normal" );
            writer.writeCharacters( String.valueOf( arcBulge.getNormal().getX() ) );
            writer.writeEndElement();
            writer.writeEndElement();
            break;
        case ARC_BY_CENTER_POINT:
            writer.writeStartElement( GMLNS, "ArcByCenterPoint" );
            ArcByCenterPoint arcCenterP = (ArcByCenterPoint) curveSeg;
            writer.writeAttribute( "interpolation", arcCenterP.getInterpolation().toString() );
            writer.writeAttribute( "numArc", "1" ); // TODO have a getNumArcs() method in ArcByCenterPoint ???
            export( arcCenterP.getMidPoint() );
            writer.writeStartElement( GMLNS, "radius" );
            writer.writeAttribute( "uom", arcCenterP.getRadius().getUomUri() );
            writer.writeCharacters( String.valueOf( arcCenterP.getRadius().getValue() ) );
            writer.writeEndElement();
            writer.writeStartElement( GMLNS, "startAngle" );
            writer.writeAttribute( "uom", arcCenterP.getStartAngle().getUomUri() );
            writer.writeCharacters( String.valueOf( arcCenterP.getStartAngle().getValue() ) );
            writer.writeEndElement();
            writer.writeStartElement( GMLNS, "endAngle" );
            writer.writeAttribute( "uom", arcCenterP.getEndAngle().getUomUri() );
            writer.writeCharacters( String.valueOf( arcCenterP.getEndAngle().getValue() ) );
            writer.writeEndElement();
            writer.writeEndElement();
            break;
        case ARC_STRING:
            writer.writeStartElement( GMLNS, "ArcString" );
            ArcString arcString = (ArcString) curveSeg;
            writer.writeAttribute( "interpolation", arcString.getInterpolation().toString() );
            writer.writeAttribute( "numArc", String.valueOf( arcString.getNumArcs() ) );
            int dim = arcString.getCoordinateDimension();
            export( arcString.getControlPoints(), dim );
            writer.writeEndElement();
            break;
        case ARC_STRING_BY_BULGE:
            writer.writeStartElement( GMLNS, "ArcStringByBulge" );
            ArcStringByBulge arcStringBulge = (ArcStringByBulge) curveSeg;
            writer.writeAttribute( "interpolation", arcStringBulge.getInterpolation().toString() );
            writer.writeAttribute( "numArc", String.valueOf( arcStringBulge.getNumArcs() ) );
            dim = arcStringBulge.getCoordinateDimension();
            export( arcStringBulge.getControlPoints(), dim );
            for ( double d : arcStringBulge.getBulges() ) {
                writer.writeStartElement( GMLNS, "bulge" );
                writer.writeCharacters( String.valueOf( d ) );
                writer.writeEndElement();
            }
            for ( Point p : arcStringBulge.getNormals() ) {
                writer.writeStartElement( GMLNS, "normal" );
                double[] array = p.getAsArray();
                int curveSegDim = curveSeg.getCoordinateDimension();
                for ( int i = 0; i < curveSegDim - 1; i++ )
                    writer.writeCharacters( String.valueOf( array[i] ) + " " );
                writer.writeEndElement();
            }
            writer.writeEndElement();
            break;
        case BEZIER:
            writer.writeStartElement( GMLNS, "Bezier" );
            Bezier bezier = (Bezier) curveSeg;
            writer.writeAttribute( "interpolation", bezier.getInterpolation().toString() );
            dim = bezier.getCoordinateDimension();
            export( bezier.getControlPoints(), dim );
            writer.writeStartElement( GMLNS, "degree" );
            writer.writeCharacters( String.valueOf( bezier.getPolynomialDegree() ) );
            writer.writeEndElement();
            export( bezier.getKnot1() );
            export( bezier.getKnot2() );
            writer.writeEndElement();
            break;
        case BSPLINE:
            writer.writeStartElement( GMLNS, "BSpline" );
            BSpline bSpline = (BSpline) curveSeg;
            writer.writeAttribute( "interpolation", bSpline.getInterpolation().toString() );
            dim = bSpline.getCoordinateDimension();
            export( bSpline.getControlPoints(), dim );
            writer.writeStartElement( GMLNS, "degree" );
            writer.writeCharacters( String.valueOf( bSpline.getPolynomialDegree() ) );
            writer.writeEndElement();
            for ( Knot knot : bSpline.getKnots() )
                export( knot );
            writer.writeEndElement();
            break;
        case CIRCLE:
            writer.writeStartElement( GMLNS, "Circle" );
            Circle circle = (Circle) curveSeg;
            writer.writeAttribute( "interpolation", circle.getInterpolation().toString() );
            dim = circle.getCoordinateDimension();
            export( circle.getControlPoints(), dim );
            writer.writeEndElement();
            break;
        case CIRCLE_BY_CENTER_POINT:
            writer.writeStartElement( GMLNS, "CircleByCenterPoint" );
            CircleByCenterPoint circleCenterP = (CircleByCenterPoint) curveSeg;
            writer.writeAttribute( "interpolation", circleCenterP.getInterpolation().toString() );
            writer.writeAttribute( "numArc", "1" );
            export( circleCenterP.getMidPoint() );
            writer.writeStartElement( GMLNS, "radius" );
            writer.writeAttribute( "uom", circleCenterP.getRadius().getUomUri() );
            writer.writeCharacters( String.valueOf( circleCenterP.getRadius().getValue() ) );
            writer.writeEndElement();
            writer.writeStartElement( GMLNS, "startAngle" );
            writer.writeAttribute( "uom", circleCenterP.getStartAngle().getUomUri() );
            writer.writeCharacters( String.valueOf( circleCenterP.getStartAngle().getValue() ) );
            writer.writeEndElement();
            writer.writeStartElement( GMLNS, "endAngle" );
            writer.writeAttribute( "uom", circleCenterP.getEndAngle().getUomUri() );
            writer.writeCharacters( String.valueOf( circleCenterP.getEndAngle().getValue() ) );
            writer.writeEndElement();
            writer.writeEndElement();
            break;
        case CLOTHOID:
            writer.writeStartElement( GMLNS, "Clothoid" );
            Clothoid clothoid = (Clothoid) curveSeg;
            writer.writeStartElement( GMLNS, "refLocation" );
            writer.writeStartElement( GMLNS, "AffinePlacement" );
            AffinePlacement affinePlace = clothoid.getReferenceLocation();
            writer.writeStartElement( GMLNS, "location" );
            double[] array = affinePlace.getLocation().getAsArray();
            for ( int i = 0; i < array.length; i++ )
                writer.writeCharacters( String.valueOf( array[i] ) + " " );
            writer.writeEndElement();
            for ( Point p : affinePlace.getRefDirections() ) {
                writer.writeStartElement( GMLNS, "refDirection" );
                array = p.getAsArray();
                for ( int i = 0; i < array.length; i++ )
                    writer.writeCharacters( String.valueOf( array[i] ) + " " );
                writer.writeEndElement();
            }
            writer.writeStartElement( GMLNS, "inDimension" );
            writer.writeCharacters( String.valueOf( affinePlace.getInDimension() ) );
            writer.writeEndElement();
            writer.writeStartElement( GMLNS, "outDimension" );
            writer.writeCharacters( String.valueOf( affinePlace.getOutDimension() ) );
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeStartElement( GMLNS, "scaleFactor" );
            writer.writeCharacters( String.valueOf( clothoid.getScaleFactor() ) );
            writer.writeEndElement();
            writer.writeStartElement( GMLNS, "startParameter" );
            writer.writeCharacters( String.valueOf( clothoid.getStartParameter() ) );
            writer.writeEndElement();
            writer.writeStartElement( GMLNS, "endParameter" );
            writer.writeCharacters( String.valueOf( clothoid.getEndParameter() ) );
            writer.writeEndElement();
            writer.writeEndElement();
            break;
        case CUBIC_SPLINE:
            writer.writeStartElement( GMLNS, "CubicSpline" );
            CubicSpline cubicSpline = (CubicSpline) curveSeg;
            writer.writeAttribute( "interpolation", cubicSpline.getInterpolation().toString() );
            dim = cubicSpline.getCoordinateDimension();
            export( cubicSpline.getControlPoints(), dim );
            writer.writeStartElement( GMLNS, "vectorAtStart" );
            array = cubicSpline.getVectorAtStart().getAsArray();
            for ( int i = 0; i < array.length; i++ )
                writer.writeCharacters( String.valueOf( array[i] ) + " " );
            writer.writeEndElement();
            writer.writeStartElement( GMLNS, "vectorAtEnd" );
            array = cubicSpline.getVectorAtEnd().getAsArray();
            for ( int i = 0; i < array.length; i++ )
                writer.writeCharacters( String.valueOf( array[i] ) + " " );
            writer.writeEndElement();
            writer.writeEndElement();
            break;
        case GEODESIC:
            writer.writeStartElement( GMLNS, "Geodesic" );
            Geodesic geodesic = (Geodesic) curveSeg;
            writer.writeAttribute( "interpolation", geodesic.getInterpolation().toString() );
            int geodesicDim = geodesic.getCoordinateDimension();
            export( geodesic.getControlPoints(), geodesicDim );
            writer.writeEndElement();
            break;
        case GEODESIC_STRING:
            writer.writeStartElement( GMLNS, "GeodesicString" );
            GeodesicString geodesicString = (GeodesicString) curveSeg;
            writer.writeAttribute( "interpolation", geodesicString.getInterpolation().toString() );
            dim = geodesicString.getCoordinateDimension();
            export( geodesicString.getControlPoints(), dim );
            writer.writeEndElement();
            break;
        case LINE_STRING_SEGMENT:
            export( (LineStringSegment) curveSeg );
            break;
        case OFFSET_CURVE:
            writer.writeStartElement( GMLNS, "OffsetCurve" );
            OffsetCurve offsetCurve = (OffsetCurve) curveSeg;

            Curve baseCurve = offsetCurve.getBaseCurve();
            if ( baseCurve.getId() != null && exportedIds.contains( baseCurve.getId() ) ) {
                writer.writeEmptyElement( GMLNS, "offsetBase" );
                writer.writeAttribute( GMLNS, "href", "#" + baseCurve.getId() );
            } else {
                writer.writeStartElement( GMLNS, "offsetBase" );
                export( baseCurve );
                writer.writeEndElement();
            }
            writer.writeStartElement( GMLNS, "distance" );
            writer.writeAttribute( "uom", offsetCurve.getDistance().getUomUri() );
            writer.writeCharacters( String.valueOf( offsetCurve.getDistance().getValue() ) );
            writer.writeEndElement();
            writer.writeStartElement( GMLNS, "refDirection" );
            export( offsetCurve.getDirection() );
            writer.writeEndElement();
            writer.writeEndElement();
            break;
        }
    }

    void export( Knot knot )
                            throws XMLStreamException {
        writer.writeStartElement( GMLNS, "knot" );
        writer.writeStartElement( GMLNS, "Knot" );
        writer.writeStartElement( GMLNS, "value" );
        writer.writeCharacters( String.valueOf( knot.getValue() ) );
        writer.writeEndElement();
        writer.writeStartElement( GMLNS, "multiplicity" );
        writer.writeCharacters( String.valueOf( knot.getMultiplicity() ) );
        writer.writeEndElement();
        writer.writeStartElement( GMLNS, "weight" );
        writer.writeCharacters( String.valueOf( knot.getWeight() ) );
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
    }

    void export( List<Point> points, int srsDimension )
                            throws XMLStreamException {
        boolean hasID = false; // see if there exists a point that has an ID
        for ( Point p : points )
            if ( p.getId() != null && p.getId().trim().length() > 0 ) {
                hasID = true;
                break;
            }
        if ( !hasID ) { // if not then use the <posList> element to export the points
            writer.writeStartElement( GMLNS, "posList" );
            writer.writeAttribute( "srsDimension", String.valueOf( srsDimension ) );
            for ( Point p : points ) {
                double[] array = p.getAsArray();
                for ( int i = 0; i < array.length; i++ )
                    writer.writeCharacters( String.valueOf( array[i] ) + " " );
            }
            writer.writeEndElement();
        } else { // if there are points with IDs, see whether an ID was already encountered
            for ( Point p : points )
                export( p );
        }
    }

}
