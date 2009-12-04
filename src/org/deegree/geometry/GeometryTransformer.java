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
package org.deegree.geometry;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;

import org.deegree.crs.CRS;
import org.deegree.crs.Transformer;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.crs.transformations.Transformation;
import org.deegree.geometry.i18n.Messages;
import org.deegree.geometry.linearization.CurveLinearizer;
import org.deegree.geometry.linearization.NumPointsCriterion;
import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.multi.MultiSurface;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.primitive.segments.CurveSegment;
import org.deegree.geometry.primitive.segments.LineStringSegment;
import org.deegree.geometry.standard.points.PointsList;

/**
 * class for transforming deegree geometries to new coordinate reference systems.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GeometryTransformer extends Transformer {

    private static final GeometryFactory geomFactory = new GeometryFactory();

    private static final CurveLinearizer linearizer = new CurveLinearizer( geomFactory );
    
    // TODO make this configurable
    private static final NumPointsCriterion crit = new NumPointsCriterion( 100 );
    
    /**
     * Creates a new GeometryTransformer object.
     * 
     * @param targetCRS
     * @throws IllegalArgumentException
     *             if the given parameter is null.
     */
    public GeometryTransformer( CoordinateSystem targetCRS ) throws IllegalArgumentException {
        super( targetCRS );
    }

    /**
     * Creates a new GeometryTransformer object, with the given id as the target CRS.
     * 
     * @param targetCRS
     *            an identifier to which all other CRS's shall be transformed.
     * @throws UnknownCRSException
     *             if the given crs name could not be mapped to a valid (configured) crs.
     * @throws IllegalArgumentException
     *             if the given parameter is null.
     */
    public GeometryTransformer( String targetCRS ) throws UnknownCRSException, IllegalArgumentException {
        super( targetCRS );
    }

    /**
     * transforms the coordinates of a deegree geometry to the target coordinate reference system.
     * 
     * @param geo
     *            to be transformed
     * @return the same geometry in a different crs.
     * @throws TransformationException
     *             if the transformation between the source and target crs cannot be created.
     * @throws IllegalArgumentException
     *             if the coordinates system of the geometry is <code>null</code>
     * @throws UnknownCRSException
     */
    public Geometry transform( Geometry geo )
                            throws TransformationException, IllegalArgumentException, UnknownCRSException {
        if ( geo.getCoordinateSystem() == null ) {
            throw new IllegalArgumentException( Messages.getMessage( "CRS_GEOMETRY_HAS_NO_CRS" ) );
        }
        return transform( geo, createCRSTransformation( geo.getCoordinateSystem().getWrappedCRS() ) );
    }

    /**
     * transforms the coordinates of a deegree geometry to the target coordinate reference system.
     * 
     * @param geo
     *            to be transformed
     * @param sourceCRS
     *            the source CRS for the geometry. overwrites the CRS of the geometry.
     * @return the same geometry in a different crs.
     * @throws TransformationException
     *             if the transformation between the source and target crs cannot be created.
     * @throws IllegalArgumentException
     *             if the coordinates system of the geometry is <code>null</code>
     * @throws UnknownCRSException
     *             if the given CRS is not found
     */
    public Geometry transform( Geometry geo, String sourceCRS )
                            throws TransformationException, IllegalArgumentException, UnknownCRSException {
        return transform( geo, createCRSTransformation( sourceCRS ) );
    }

    /**
     * transforms the coordinates of a deegree geometry to the target coordinate reference system.
     * 
     * @param geo
     *            to be transformed
     * @param sourceCRS
     *            the source CRS for the geometry. overwrites the CRS of the geometry.
     * @return the same geometry in a different crs.
     * @throws TransformationException
     *             if the transformation between the source and target crs cannot be created.
     * @throws IllegalArgumentException
     *             if the coordinates system of the geometry is <code>null</code>
     */
    public Geometry transform( Geometry geo, CoordinateSystem sourceCRS )
                            throws TransformationException, IllegalArgumentException {
        return transform( geo, createCRSTransformation( sourceCRS ) );
    }

    /**
     * transforms the coordinates of a deegree geometry to the target coordinate reference system.
     * 
     * @param geo
     *            to be transformed
     * @return the same geometry in a different crs.
     * @throws TransformationException
     *             if the transformation between the source and target crs cannot be created.
     * @throws IllegalArgumentException
     *             if the coordinates system of the geometry is <code>null</code>
     */
    private Geometry transform( Geometry geo, Transformation trans )
                            throws TransformationException, IllegalArgumentException {
        Geometry transformedGeometry = null;
        try {
            if ( geo instanceof Point ) {
                transformedGeometry = transform( (Point) geo, trans );
            } else if ( geo instanceof LineString ) {
                transformedGeometry = transform( (LineString) geo, trans );
            } else if ( geo instanceof Curve ) {
                transformedGeometry = transform( (Curve) geo, trans );
            } else if ( geo instanceof Surface ) {
                transformedGeometry = transform( (Surface) geo, trans );
            } else if ( geo instanceof MultiPoint ) {
                transformedGeometry = transform( (MultiPoint) geo, trans );
            } else if ( geo instanceof MultiLineString ) {
                transformedGeometry = transform( (MultiLineString) geo, trans );
            } else if ( geo instanceof MultiCurve ) {
                transformedGeometry = transform( (MultiCurve) geo, trans );
            } else if ( geo instanceof MultiSurface ) {
                transformedGeometry = transform( (MultiSurface) geo, trans );
            } else if ( geo instanceof MultiPolygon ) {
                transformedGeometry = transform( (MultiPolygon) geo, trans );
            } else if ( geo instanceof Envelope ) {
                transformedGeometry = transform( (Envelope) geo, trans );
            } else {
                throw new IllegalArgumentException( "Unsupported geometry:" + geo.getClass().getName() );
            }
        } catch ( GeometryException ge ) {
            throw new TransformationException( Messages.getMessage( "CRS_TRANSFORMATION_ERROR",
                                                                    geo.getCoordinateSystem().getName(),
                                                                    getTargetCRS().getCodes(), ge.getMessage() ), ge );
        }
        return transformedGeometry;
    }

    private Geometry transform( Envelope envelope, Transformation trans )
                            throws TransformationException {
        return transform( envelope, trans, 20 );
    }

    /**
     * Transform the geometry of an envelope. It substitutes the envelope (two points) with a polygon with
     * <code>numPoints</code> points. The points are distributed evenly on the boundary of the envelope to provide a
     * more accurate transformed envelope. This is useful if the transformation rotates and distorts.
     * 
     * @param envelope
     * @param trans
     * @param numPoints
     * @return the transformed envelope
     * @throws TransformationException
     */
    private Envelope transform( Envelope envelope, Transformation trans, int numPoints )
                            throws TransformationException {
        int pointsPerSide;
        if ( numPoints < 4 ) {
            pointsPerSide = 0;
        } else {
            pointsPerSide = (int) Math.ceil( ( numPoints - 4 ) / 4.0 );
        }

        double x1 = envelope.getMin().get0();
        double y1 = envelope.getMin().get1();
        double x2 = envelope.getMax().get0();
        double y2 = envelope.getMax().get1();

        double width = envelope.getSpan0();
        double height = envelope.getSpan1();

        double xStep = width / ( pointsPerSide + 1 );
        double yStep = height / ( pointsPerSide + 1 );

        PrecisionModel precision = envelope.getPrecision();

        List<Point> points = new ArrayList<Point>( pointsPerSide * 4 + 4 );

        for ( int i = 0; i <= pointsPerSide + 1; i++ ) {
            points.add( geomFactory.createPoint( null, new double[] { x1 + i * xStep, y1 }, null ) );
            points.add( geomFactory.createPoint( null, new double[] { x1 + i * xStep, y2 }, null ) );
        }

        for ( int i = 1; i <= pointsPerSide; i++ ) {
            points.add( geomFactory.createPoint( null, new double[] { x1, y1 + i * yStep }, null ) );
            points.add( geomFactory.createPoint( null, new double[] { x2, y1 + i * yStep }, null ) );
        }

        MultiPoint envGeometry = geomFactory.createMultiPoint( null, envelope.getCoordinateSystem(), points );
        MultiPoint transformedEnvGeometry = transform( envGeometry, trans );

        return transformedEnvGeometry.getEnvelope();
    }

    private LineString transform( LineString geo, Transformation trans )
                            throws TransformationException {
        LineStringSegment segment = (LineStringSegment) geo.getCurveSegments().get( 0 ); // only one for a line string?
        Points pos = segment.getControlPoints();
        pos = transform( pos, trans );
        return geomFactory.createLineString( geo.getId(), new CRS( trans.getTargetCRS() ), pos );
    }

    /**
     * transforms the submitted curve to the target coordinate reference system
     * 
     * @throws TransformationException
     */
    private Curve transform( Curve geo, Transformation trans )
                            throws TransformationException {
        CurveSegment[] curveSegments = new CurveSegment[geo.getCurveSegments().size()];
        int i = 0;
        for ( CurveSegment segment : geo.getCurveSegments() ) {
            if ( !( segment instanceof LineStringSegment ) ) {
                // TODO is linearization here really a good idea?
                segment = linearizer.linearize( segment, crit );
            }
            Points pos = ( (LineStringSegment) segment ).getControlPoints();
            pos = transform( pos, trans );
            curveSegments[i++] = geomFactory.createLineStringSegment( pos );
        }
        return geomFactory.createCurve( geo.getId(), curveSegments, new CRS( trans.getTargetCRS() ) );
    }

    private MultiLineString transform( MultiLineString geo, Transformation trans )
                            throws TransformationException {
        List<LineString> lines = new ArrayList<LineString>( geo.size() );
        for ( LineString line : geo ) {
            lines.add( transform( line, trans ) );
        }
        return geomFactory.createMultiLineString( geo.getId(), new CRS( trans.getTargetCRS() ), lines );
    }

    /**
     * transforms the submitted multi curve to the target coordinate reference system
     * 
     * @throws TransformationException
     */
    private MultiCurve transform( MultiCurve geo, Transformation trans )
                            throws TransformationException {
        List<Curve> curves = new ArrayList<Curve>( geo.size() );
        for ( Curve curve : geo ) {
            curves.add( transform( curve, trans ) );
        }
        return geomFactory.createMultiCurve( geo.getId(), geo.getCoordinateSystem(), curves );
    }

    /**
     * transforms the submitted multi point to the target coordinate reference system
     * 
     * @throws TransformationException
     */
    private MultiPoint transform( MultiPoint geo, Transformation trans )
                            throws TransformationException {
        List<Point> points = new ArrayList<Point>( geo.size() );
        for ( Point p : geo ) {
            points.add( transform( p, trans ) );
        }
        return geomFactory.createMultiPoint( geo.getId(), geo.getCoordinateSystem(), points );
    }

    /**
     * transforms the submitted multi surface to the target coordinate reference system
     * 
     * @throws TransformationException
     */
    private MultiSurface transform( MultiSurface geo, Transformation trans )
                            throws TransformationException {
        List<Surface> surfaces = new ArrayList<Surface>( geo.size() );
        for ( Surface surface : geo ) {
            surfaces.add( transform( surface, trans ) );
        }
        return geomFactory.createMultiSurface( geo.getId(), geo.getCoordinateSystem(), surfaces );
    }

    // TODO: return a proper multi polygon instead
    private MultiSurface transform( MultiPolygon geo, Transformation trans )
                            throws TransformationException {
        ArrayList<Surface> polys = new ArrayList<Surface>( geo.size() );
        for ( Geometry g : geo ) {
            polys.add( (Surface) transform( g, trans ) );
        }
        return geomFactory.createMultiSurface( geo.getId(), geo.getCoordinateSystem(), polys );
    }

    /**
     * transforms the list of points
     * 
     * @throws TransformationException
     */
    private Points transform( Points points, Transformation trans )
                            throws TransformationException {

        List<Point> result = new ArrayList<Point>( points.size() );
        for ( Point point : points ) {
            Point3d coord = new Point3d( point.get0(), point.get1(), point.get2() );
            Point3d tmp = new Point3d( coord );

            tmp = trans.doTransform( coord );

            if ( Double.isNaN( point.get2() ) ) {
                result.add( geomFactory.createPoint( point.getId(), new double[] { tmp.x, tmp.y },
                                                     new CRS( trans.getTargetCRS() ) ) );
            } else {
                result.add( geomFactory.createPoint( point.getId(), new double[] { tmp.x, tmp.y, tmp.z },
                                                     new CRS( trans.getTargetCRS() ) ) );
            }
        }
        return new PointsList( result );
    }

    /**
     * transforms the submitted point to the target coordinate reference system
     * 
     * @throws TransformationException
     */
    private Point transform( Point geo, Transformation trans )
                            throws TransformationException {

        Point3d coord = new Point3d( geo.get0(), geo.get1(), geo.get2() );
        Point3d result = new Point3d( coord );

        result = trans.doTransform( coord );

        if ( Double.isNaN( geo.get2() ) ) {
            return geomFactory.createPoint( geo.getId(), new double[] { result.x, result.y },
                                            new CRS( trans.getTargetCRS() ) );
        }
        return geomFactory.createPoint( geo.getId(), new double[] { result.x, result.y, result.z },
                                        new CRS( trans.getTargetCRS() ) );
    }

    /**
     * transforms the submitted surface to the target coordinate reference system
     * 
     * @throws TransformationException
     */
    private Surface transform( Surface geo, Transformation trans )
                            throws TransformationException {

        List<SurfacePatch> patches = new ArrayList<SurfacePatch>( geo.getPatches().size() );
        for ( SurfacePatch patch : geo.getPatches() ) {
            if ( patch instanceof PolygonPatch ) {
                Ring exterior = ( (PolygonPatch) patch ).getExteriorRing();
                LinearRing transformedExteriorRing = null;
                if ( exterior != null ) {
                    transformedExteriorRing = geomFactory.createLinearRing(
                                                                            exterior.getId(),
                                                                            exterior.getCoordinateSystem(),
                                                                            transform(
                                                                                       exterior.getAsLineString().getControlPoints(),
                                                                                       trans ) );
                }
                List<Ring> interiorRings = ( (PolygonPatch) patch ).getInteriorRings();
                List<Ring> transformedInteriorRings = new ArrayList<Ring>( interiorRings.size() );
                for ( Ring interior : interiorRings ) {
                    transformedInteriorRings.add( geomFactory.createLinearRing(
                                                                                interior.getId(),
                                                                                interior.getCoordinateSystem(),
                                                                                transform(
                                                                                           interior.getAsLineString().getControlPoints(),
                                                                                           trans ) ) );
                }
                patches.add( geomFactory.createPolygonPatch( transformedExteriorRing, transformedInteriorRings ) );
            }
        }
        return geomFactory.createSurface( geo.getId(), patches, new CRS( trans.getTargetCRS() ) );
    }
}
