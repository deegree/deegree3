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

import static org.deegree.commons.utils.StringUtils.isSet;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point3d;

import org.deegree.commons.uom.Length;
import org.deegree.cs.CRS;
import org.deegree.cs.CRSRegistry;
import org.deegree.cs.Transformer;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.exceptions.OutsideCRSDomainException;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.transformations.Transformation;
import org.deegree.cs.transformations.TransformationFactory;
import org.deegree.geometry.Geometry.GeometryType;
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
import org.deegree.geometry.multi.MultiGeometry.MultiGeometryType;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.OrientableCurve;
import org.deegree.geometry.primitive.OrientableSurface;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Solid;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.Tin;
import org.deegree.geometry.primitive.Curve.CurveType;
import org.deegree.geometry.primitive.GeometricPrimitive.PrimitiveType;
import org.deegree.geometry.primitive.Solid.SolidType;
import org.deegree.geometry.primitive.Surface.SurfaceType;
import org.deegree.geometry.primitive.patches.GriddedSurfacePatch;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.primitive.patches.Triangle;
import org.deegree.geometry.primitive.patches.GriddedSurfacePatch.GriddedSurfaceType;
import org.deegree.geometry.primitive.patches.PolygonPatch.PolygonPatchType;
import org.deegree.geometry.primitive.patches.SurfacePatch.SurfacePatchType;
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
import org.deegree.geometry.primitive.segments.LineStringSegment;
import org.deegree.geometry.primitive.segments.OffsetCurve;
import org.deegree.geometry.primitive.segments.CurveSegment.CurveSegmentType;
import org.deegree.geometry.standard.curvesegments.AffinePlacement;
import org.deegree.geometry.standard.points.PointsList;
import org.slf4j.Logger;

/**
 * 
 * Transforms a geometry defined in a {@link CoordinateSystem} into a geometry defined in another
 * {@link CoordinateSystem}
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GeometryTransformer extends Transformer {

    private static final Logger LOG = getLogger( GeometryTransformer.class );

    private static final GeometryFactory geomFactory = new GeometryFactory();

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
     * Creates a new GeometryTransformer object.
     * 
     * @param targetCRS
     * @throws IllegalArgumentException
     *             if the given parameter is null.
     * @throws UnknownCRSException
     */
    public GeometryTransformer( CRS targetCRS ) throws IllegalArgumentException, UnknownCRSException {
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
     * @param definedTransformation
     *            to use instead of the CRSFactory.
     * @throws IllegalArgumentException
     *             if the given parameter is null.
     */
    public GeometryTransformer( Transformation definedTransformation ) throws IllegalArgumentException {
        super( definedTransformation );
    }

    /**
     * transforms the coordinates of a deegree geometry to the target coordinate reference system.
     * 
     * @param <T>
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
    public <T extends Geometry> T transform( T geo )
                            throws TransformationException, IllegalArgumentException, UnknownCRSException {
        return transform( geo, null, false, null );
    }

    /**
     * transforms the coordinates of a deegree geometry to the target coordinate reference system. Optionally tests
     * whether the given Geometry lies within the source crs' valid domain.
     * 
     * @param geo
     *            to be transformed
     * @param testValidDomain
     *            true if the incoming geometry should be checked against the domain of validity of the CoordinateSystem
     *            it is defined in.
     * @return the same geometry in a different crs.
     * @throws TransformationException
     *             if the transformation between the source and target crs cannot be created.
     * @throws IllegalArgumentException
     *             if the coordinates system of the geometry is <code>null</code>
     * @throws UnknownCRSException
     */
    public Geometry transform( Geometry geo, boolean testValidDomain )
                            throws TransformationException, IllegalArgumentException, UnknownCRSException {
        return transform( geo, null, testValidDomain, null );
    }

    /**
     * Creates an envelope in the given CRS of the Area of Use (defined in WGS84) of the given Coordinate System.
     * 
     * @param sourceCRS
     *            to get the area of use from.
     * @return an Envelope with corresponding ordinates of the area of use BBox of the given Coordinate system, if the
     *         AreaOfUse was defined as -180,-90,180,90 BBox or if the envelope could not be transformed
     *         <code>null</code> will be returned.
     */
    public static Envelope createValidDomain( CoordinateSystem sourceCRS ) {
        double[] validDomain = sourceCRS.getValidDomain();
        if ( validDomain != null ) {
            return geomFactory.createEnvelope( new double[] { validDomain[0], validDomain[1] },
                                               new double[] { validDomain[2], validDomain[3] }, new CRS( sourceCRS ) );
        }
        return null;
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
        return transform( geo, CRSRegistry.lookup( sourceCRS ), false, null );
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
        return transform( geo, sourceCRS, false, null );
    }

    /**
     * transforms the coordinates of a deegree geometry to the target coordinate reference system.
     * 
     * @param <T>
     * 
     * @param geom
     *            to be transformed
     * @param sourceCRS
     *            the source CRS for the geometry. overwrites the CRS of the geometry.
     * @param testValidArea
     *            true if the geometry should be be checked of the valid area of the source crs.
     * @param toBeUsedTransformations
     * @return the same geometry in a different crs.
     * @throws IllegalArgumentException
     *             if the coordinates system of the geometry is <code>null</code>
     * @throws TransformationException
     *             if the transformation between the source and target crs cannot be created.
     */
    public <T extends Geometry> T transform( T geom, CoordinateSystem sourceCRS, boolean testValidArea,
                                             List<Transformation> toBeUsedTransformations )
                            throws IllegalArgumentException, TransformationException {
        Envelope sourceEnv = null;
        CoordinateSystem source = sourceCRS;
        if ( source == null ) {
            CRS gCRS = geom.getCoordinateSystem();
            if ( gCRS != null ) {
                try {
                    source = gCRS.getWrappedCRS();
                } catch ( UnknownCRSException e ) {
                    LOG.debug( "No sourceCRS was found in geometry: " + e.getLocalizedMessage(), e );
                    LOG.debug( "No sourceCRS was found in geometry: " + e.getLocalizedMessage() );
                }
            }
        }
        if ( testValidArea ) {
            sourceEnv = createValidDomain( source );
        }
        return transform( geom, createCRSTransformation( source, toBeUsedTransformations ), sourceEnv );
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
    private <T extends Geometry> T transform( T geo, Transformation trans, Envelope domainOfValidity )
                            throws TransformationException, IllegalArgumentException {
        if ( TransformationFactory.isIdentity( trans ) ) {
            return geo;
        }
        if ( domainOfValidity != null ) {
            if ( !insideValidDomain( domainOfValidity, geo ) ) {
                throw new OutsideCRSDomainException( "Geometry (gml:id="
                                                     + ( isSet( geo.getId() ) ? geo.getId() : "not set" )
                                                     + ")is outside the area of validity of the source CRS." );
            }
        }
        GeometryType geometryType = geo.getGeometryType();
        try {
            switch ( geometryType ) {
            case COMPOSITE_GEOMETRY: {
                @SuppressWarnings("unchecked")
                T transformedGeometry = (T) transform( (CompositeGeometry<?>) geo, trans );
                return transformedGeometry;
            }
            case ENVELOPE: {
                @SuppressWarnings("unchecked")
                T transformedGeometry = (T) transform( (Envelope) geo, trans );
                return transformedGeometry;
            }
            case MULTI_GEOMETRY: {
                @SuppressWarnings("unchecked")
                T transformedGeometry = (T) transform( (MultiGeometry<?>) geo, trans, domainOfValidity );
                return transformedGeometry;
            }
            case PRIMITIVE_GEOMETRY: {
                @SuppressWarnings("unchecked")
                T transformedGeometry = (T) transform( (GeometricPrimitive) geo, trans );
                return transformedGeometry;
            }
            }
        } catch ( GeometryException ge ) {
            throw new TransformationException( Messages.getMessage( "CRS_TRANSFORMATION_ERROR",
                                                                    geo.getCoordinateSystem().getName(),
                                                                    getTargetCRS().getCodes(), ge.getMessage() ), ge );
        }
        return null;
    }

    private GeometricPrimitive transform( GeometricPrimitive geom, Transformation trans )
                            throws TransformationException {
        GeometricPrimitive result = null;
        PrimitiveType type = geom.getPrimitiveType();
        switch ( type ) {
        case Curve:
            result = transform( (Curve) geom, trans );
            break;
        case Point:
            result = transform( (Point) geom, trans );
            break;
        case Solid:
            result = transform( (Solid) geom, trans );
            break;
        case Surface:
            result = transform( (Surface) geom, trans );
            break;
        }
        return result;
    }

    private CompositeGeometry<?> transform( CompositeGeometry<?> geom, Transformation trans )
                            throws TransformationException {
        List<GeometricPrimitive> transformed = new LinkedList<GeometricPrimitive>();
        for ( GeometricPrimitive gp : geom ) {
            GeometricPrimitive tGp = transform( gp, trans );
            transformed.add( tGp );
        }
        return geomFactory.createCompositeGeometry( geom.getId(), getWrappedTargetCRS(), transformed );
    }

    private MultiGeometry<?> transform( MultiGeometry<?> geom, Transformation trans, Envelope domainOfValidity )
                            throws TransformationException {
        MultiGeometry<?> result = null;

        final MultiGeometryType geometryType = geom.getMultiGeometryType();
        switch ( geometryType ) {
        case MULTI_CURVE:
            result = transform( (MultiCurve) geom, trans );
            break;
        case MULTI_GEOMETRY:
            List<Geometry> mg = new LinkedList<Geometry>();
            for ( Geometry geo : geom ) {
                Geometry tG = transform( geo, trans, domainOfValidity );
                mg.add( tG );
            }
            result = geomFactory.createMultiGeometry( geom.getId(), getWrappedTargetCRS(), mg );
            break;
        case MULTI_LINE_STRING:
            result = transform( (MultiLineString) geom, trans );
            break;
        case MULTI_POINT:
            result = transform( (MultiPoint) geom, trans );
            break;
        case MULTI_POLYGON:
            result = transform( (MultiPolygon) geom, trans );
            break;
        case MULTI_SOLID:
            result = transform( (MultiSolid) geom, trans );
            break;
        case MULTI_SURFACE:
            result = transform( (MultiSurface) geom, trans );
            break;
        }
        return result;
    }

    private MultiSolid transform( MultiSolid geom, Transformation trans )
                            throws TransformationException {
        List<Solid> transformedSolids = new LinkedList<Solid>();
        for ( Solid s : geom ) {
            Solid ts = transform( s, trans );
            transformedSolids.add( ts );
        }
        return geomFactory.createMultiSolid( geom.getId(), getWrappedTargetCRS(), transformedSolids );
    }

    private Solid transform( Solid solid, Transformation trans )
                            throws TransformationException {
        SolidType type = solid.getSolidType();
        Solid result = null;
        switch ( type ) {
        case CompositeSolid:
            result = transform( (CompositeSolid) solid, trans );
            break;
        case Solid:
            Surface ext = solid.getExteriorSurface();
            Surface tExt = transform( ext, trans );
            List<Surface> inter = solid.getInteriorSurfaces();
            List<Surface> tInter = new LinkedList<Surface>();
            if ( inter != null && !inter.isEmpty() ) {
                for ( Surface in : inter ) {
                    if ( in != null ) {
                        Surface tIn = transform( in, trans );
                        tInter.add( tIn );
                    }
                }
            }
            result = geomFactory.createSolid( solid.getId(), getWrappedTargetCRS(), tExt, tInter );
            break;
        }
        return result;

    }

    private CompositeSolid transform( CompositeSolid cSolid, Transformation trans )
                            throws TransformationException {
        List<Solid> tSolids = new ArrayList<Solid>( cSolid.size() );
        for ( Solid s : cSolid ) {
            tSolids.add( transform( s, trans ) );
        }
        return geomFactory.createCompositeSolid( cSolid.getId(), getWrappedTargetCRS(), tSolids );
    }

    private boolean insideValidDomain( Envelope validDomain, Geometry geom ) {
        boolean result = false;
        if ( validDomain != null && geom != null ) {
            Geometry inSource = geom;
            CRS sourceCRS = validDomain.getCoordinateSystem();
            if ( sourceCRS != null && geom.getCoordinateSystem() != null
                 && !sourceCRS.equals( geom.getCoordinateSystem() ) ) {
                try {
                    GeometryTransformer trans = new GeometryTransformer( sourceCRS.getWrappedCRS() );
                    inSource = trans.transform( geom );
                } catch ( IllegalArgumentException e ) {
                    if ( LOG.isDebugEnabled() ) {
                        LOG.debug( "No valid domain checking available: " + e.getMessage(), e );
                    }
                } catch ( TransformationException e ) {
                    if ( LOG.isDebugEnabled() ) {
                        LOG.debug( "No valid domain checking available: " + e.getMessage(), e );
                    }
                } catch ( UnknownCRSException e ) {
                    if ( LOG.isDebugEnabled() ) {
                        LOG.debug( "No valid domain checking available: " + e.getMessage(), e );
                    }
                }
            }
            try {
                result = validDomain.contains( inSource );
            } catch ( UnsupportedOperationException e ) {
                LOG.debug( "Could not determine valid domain because it is not supported: " + e.getMessage(), e );
                result = true;
            } catch ( Throwable t ) {
                LOG.debug( "No valid domain checking available: " + t.getMessage(), t );
                result = false;
            }
        }
        return result;
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

        double axis0Min = envelope.getMin().get0();
        double axis1Min = envelope.getMin().get1();
        double axis0Max = envelope.getMax().get0();
        double axis1Max = envelope.getMax().get1();

        double span0 = envelope.getSpan0();
        double span1 = envelope.getSpan1();

        double axis0Step = span0 / ( pointsPerSide + 1 );
        double axis1Step = span1 / ( pointsPerSide + 1 );

        List<Point3d> points = new ArrayList<Point3d>( pointsPerSide * 4 + 4 );
        double zValue = Double.NaN;
        if ( trans.getSourceCRS() != null ) {
            zValue = trans.getSourceCRS().getDimension() == 3 ? 1 : Double.NaN;
        }

        for ( int i = 0; i <= pointsPerSide + 1; i++ ) {
            points.add( new Point3d( axis0Min + i * axis0Step, axis1Min, zValue ) );
            points.add( new Point3d( axis0Min + i * axis0Step, axis1Max, zValue ) );
            points.add( new Point3d( axis0Min, axis1Min + i * axis1Step, zValue ) );
            points.add( new Point3d( axis0Max, axis1Min + i * axis1Step, zValue ) );
        }

        points = trans.doTransform( points );
        axis0Min = Double.MAX_VALUE;
        axis1Min = Double.MAX_VALUE;
        axis0Max = Double.NEGATIVE_INFINITY;
        axis1Max = Double.NEGATIVE_INFINITY;
        for ( Point3d p : points ) {
            axis0Min = Math.min( p.x, axis0Min );
            axis1Min = Math.min( p.y, axis1Min );
            axis0Max = Math.max( p.x, axis0Max );
            axis1Max = Math.max( p.y, axis1Max );
        }

        // pass the 3rd coordinate if exist and dimension of source and target CRS is 2
        double[] min;
        double[] max;
        if ( trans.getSourceCRS().getDimension() == 2 && trans.getTargetCRS().getDimension() == 2
             && !Double.isNaN( envelope.getMin().get2() ) && !Double.isNaN( envelope.getMax().get2() ) ) {
            min = new double[] { axis0Min, axis1Min, envelope.getMin().get2() };
            max = new double[] { axis0Max, axis1Max, envelope.getMax().get2() };
        } else {
            min = new double[] { axis0Min, axis1Min };
            max = new double[] { axis0Max, axis1Max };
        }

        return geomFactory.createEnvelope( min, max, new CRS( this.getTargetCRS() ) );
    }

    private LineString transform( LineString geo, Transformation trans )
                            throws TransformationException {
        LineStringSegment segment = (LineStringSegment) geo.getCurveSegments().get( 0 ); // only one for a line string?
        Points pos = segment.getControlPoints();
        pos = transform( pos, trans );
        return geomFactory.createLineString( geo.getId(), getWrappedTargetCRS(), pos );
    }

    /**
     * transforms the submitted curve to the target coordinate reference system
     * 
     * @throws TransformationException
     */
    private Curve transform( Curve curve, Transformation trans )
                            throws TransformationException {
        CurveType type = curve.getCurveType();
        Curve result = null;
        switch ( type ) {
        case CompositeCurve:
            result = transform( (CompositeCurve) curve, trans );
            break;
        case OrientableCurve:
        case Curve: {
            CurveSegment[] curveSegments = new CurveSegment[curve.getCurveSegments().size()];
            int i = 0;
            for ( CurveSegment segment : curve.getCurveSegments() ) {
                curveSegments[i++] = transform( segment, trans );
            }
            result = geomFactory.createCurve( curve.getId(), getWrappedTargetCRS(), curveSegments );
            if ( type == CurveType.OrientableCurve ) {
                result = geomFactory.createOrientableCurve( curve.getId(), result.getCoordinateSystem(), result,
                                                            ( (OrientableCurve) curve ).isReversed() );
            }
        }
            break;
        case LineString: {
            result = transform( (LineString) curve, trans );
        }
            break;
        case Ring:
            result = transform( (Ring) curve, trans );
            break;
        }
        return result;
    }

    private CompositeCurve transform( CompositeCurve cCurve, Transformation trans )
                            throws TransformationException {
        List<Curve> tCurves = new ArrayList<Curve>( cCurve.size() );
        for ( Curve c : cCurve ) {
            tCurves.add( transform( c, trans ) );
        }
        return geomFactory.createCompositeCurve( cCurve.getId(), getWrappedTargetCRS(), tCurves );
    }

    private CurveSegment transform( CurveSegment segment, Transformation trans )
                            throws TransformationException {
        CurveSegmentType segmentType = segment.getSegmentType();
        Points pos = null;
        CurveSegment transformedSegment = null;
        switch ( segmentType ) {
        case ARC:
            pos = ( (Arc) segment ).getControlPoints();
            pos = transform( pos, trans );
            transformedSegment = geomFactory.createArc( pos.get( 0 ), pos.get( 1 ), pos.get( 2 ) );
            break;
        case ARC_BY_BULGE:
            ArcByBulge abb = (ArcByBulge) segment;
            pos = abb.getControlPoints();
            pos = transform( pos, trans );
            transformedSegment = geomFactory.createArcByBulge( pos.get( 0 ), pos.get( 1 ), abb.getBulge(),
                                                               abb.getNormal() );
            break;
        case ARC_BY_CENTER_POINT:
            ArcByCenterPoint abc = (ArcByCenterPoint) segment;
            Point m = abc.getMidPoint();
            m = transform( m, trans );
            transformedSegment = geomFactory.createArcByCenterPoint( m, (Length) abc.getRadius( null ),
                                                                     abc.getStartAngle(), abc.getEndAngle() );
            break;
        case ARC_STRING:
            ArcString as = (ArcString) segment;
            pos = as.getControlPoints();
            pos = transform( pos, trans );
            transformedSegment = geomFactory.createArcString( pos );
            break;
        case ARC_STRING_BY_BULGE:
            ArcStringByBulge asbb = (ArcStringByBulge) segment;
            pos = asbb.getControlPoints();
            pos = transform( pos, trans );
            transformedSegment = geomFactory.createArcStringByBulge( pos, asbb.getBulges(), asbb.getNormals() );
            break;
        case BEZIER:
            Bezier b = (Bezier) segment;
            pos = b.getControlPoints();
            pos = transform( pos, trans );
            transformedSegment = geomFactory.createBezier( pos, b.getPolynomialDegree(), b.getKnot1(), b.getKnot2() );
            break;
        case BSPLINE:
            BSpline bs = (BSpline) segment;
            pos = bs.getControlPoints();
            pos = transform( pos, trans );
            transformedSegment = geomFactory.createBSpline( pos, bs.getPolynomialDegree(), bs.getKnots(),
                                                            bs.isPolynomial() );
            break;
        case CIRCLE:
            Circle c = (Circle) segment;
            Point c1 = c.getPoint1();
            c1 = transform( c1, trans );
            Point c2 = c.getPoint2();
            c2 = transform( c2, trans );
            Point c3 = c.getPoint3();
            c3 = transform( c3, trans );
            transformedSegment = geomFactory.createCircle( c1, c1, c3 );
            break;
        case CIRCLE_BY_CENTER_POINT:
            CircleByCenterPoint cbcp = (CircleByCenterPoint) segment;
            Point cbcpm = cbcp.getMidPoint();
            cbcpm = transform( cbcpm, trans );
            transformedSegment = geomFactory.createCircleByCenterPoint( cbcpm, (Length) cbcp.getRadius( null ),
                                                                        cbcp.getStartAngle() );
            break;
        case CLOTHOID:
            Clothoid cl = (Clothoid) segment;
            AffinePlacement ap = cl.getReferenceLocation();
            Point apl = ap.getLocation();
            Point tapl = transform( apl, trans );
            pos = ap.getRefDirections();
            pos = transform( pos, trans );
            AffinePlacement nap = new AffinePlacement( tapl, pos, ap.getInDimension(), ap.getOutDimension() );
            transformedSegment = geomFactory.createClothoid( nap, cl.getScaleFactor(), cl.getStartParameter(),
                                                             cl.getEndParameter() );
            break;
        case CUBIC_SPLINE:
            CubicSpline cs = (CubicSpline) segment;
            pos = cs.getControlPoints();
            pos = transform( pos, trans );
            transformedSegment = geomFactory.createCubicSpline( pos, cs.getVectorAtStart(), cs.getVectorAtEnd() );
            break;
        case GEODESIC:
            Geodesic g = (Geodesic) segment;
            Point gp1 = g.getPoint1();
            gp1 = transform( gp1, trans );
            Point gp2 = g.getPoint2();
            gp2 = transform( gp2, trans );
            transformedSegment = geomFactory.createGeodesic( gp1, gp2 );
            break;
        case GEODESIC_STRING:
            GeodesicString gs = (GeodesicString) segment;
            pos = gs.getControlPoints();
            pos = transform( pos, trans );
            transformedSegment = geomFactory.createGeodesicString( pos );
            break;
        case LINE_STRING_SEGMENT:
            LineStringSegment lss = (LineStringSegment) segment;
            pos = lss.getControlPoints();
            pos = transform( pos, trans );
            transformedSegment = geomFactory.createLineStringSegment( pos );
            break;
        case OFFSET_CURVE:
            OffsetCurve oc = (OffsetCurve) segment;
            Curve ocbc = oc.getBaseCurve();
            ocbc = transform( ocbc, trans );
            transformedSegment = geomFactory.createOffsetCurve( ocbc, oc.getDirection(), (Length) oc.getDistance( null ) );
            break;
        }
        return transformedSegment;
    }

    private MultiLineString transform( MultiLineString geo, Transformation trans )
                            throws TransformationException {
        List<LineString> lines = new ArrayList<LineString>( geo.size() );
        for ( LineString line : geo ) {
            lines.add( transform( line, trans ) );
        }
        return geomFactory.createMultiLineString( geo.getId(), getWrappedTargetCRS(), lines );
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
        return geomFactory.createMultiCurve( geo.getId(), getWrappedTargetCRS(), curves );
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
        return geomFactory.createMultiPoint( geo.getId(), getWrappedTargetCRS(), points );
    }

    /**
     * transforms the submitted multi surface to the target coordinate reference system
     * 
     * @throws TransformationException
     */
    private MultiSurface transform( MultiSurface multiSurface, Transformation trans )
                            throws TransformationException {
        List<Surface> surfaces = new ArrayList<Surface>( multiSurface.size() );
        for ( Surface surface : multiSurface ) {
            surfaces.add( transform( surface, trans ) );
        }
        return geomFactory.createMultiSurface( multiSurface.getId(), getWrappedTargetCRS(), surfaces );
    }

    private MultiPolygon transform( MultiPolygon multiPolygon, Transformation trans )
                            throws TransformationException {

        ArrayList<Polygon> polys = new ArrayList<Polygon>( multiPolygon.size() );
        for ( Polygon g : multiPolygon ) {
            polys.add( transform( g, trans ) );
        }
        return geomFactory.createMultiPolygon( multiPolygon.getId(), getWrappedTargetCRS(), polys );
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
                                                     getWrappedTargetCRS() ) );
            } else {
                // pass the 3rd coordinate if exist and dimension of source and target CRS is 2
                if ( trans.getSourceCRS().getDimension() == 2 && trans.getTargetCRS().getDimension() == 2 ) {
                    tmp.z = point.get2();
                }
                result.add( geomFactory.createPoint( point.getId(), new double[] { tmp.x, tmp.y, tmp.z },
                                                     getWrappedTargetCRS() ) );
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
            return geomFactory.createPoint( geo.getId(), new double[] { result.x, result.y }, getWrappedTargetCRS() );
        } else if ( trans.getSourceCRS().getDimension() == 2 && trans.getTargetCRS().getDimension() == 2 ) {
            // pass the 3rd coordinate if exist and dimension of source and target CRS is 2
            result.z = geo.get2();
        }
        return geomFactory.createPoint( geo.getId(), new double[] { result.x, result.y, result.z },
                                        getWrappedTargetCRS() );
    }

    /**
     * transforms the submitted surface to the target coordinate reference system
     * 
     * @throws TransformationException
     */
    @SuppressWarnings("unchecked")
    private Surface transform( Surface surface, Transformation trans )
                            throws TransformationException {
        Surface result = null;
        SurfaceType surfaceType = surface.getSurfaceType();
        String id = surface.getId();
        CRS nCRS = getWrappedTargetCRS();
        switch ( surfaceType ) {
        case CompositeSurface:
            result = transform( (CompositeSurface) surface, trans );
            break;
        case OrientableSurface:
            List<? extends SurfacePatch> osP = surface.getPatches();
            List<SurfacePatch> tosP = transform( osP, trans );
            Surface tOs = geomFactory.createSurface( id, tosP, nCRS );
            result = geomFactory.createOrientableSurface( id, nCRS, tOs, ( (OrientableSurface) surface ).isReversed() );
            break;
        case Polygon:
            result = transform( (Polygon) surface, trans );
            break;
        case PolyhedralSurface:
            // polyherdal surfaces only have polygon patches.
            List<PolygonPatch> phsP = (List<PolygonPatch>) surface.getPatches();
            List<PolygonPatch> tphsP = (List) transform( phsP, trans );
            result = geomFactory.createPolyhedralSurface( id, nCRS, tphsP );
            break;
        case Surface:
            List<? extends SurfacePatch> ssP = surface.getPatches();
            List<SurfacePatch> tssP = transform( ssP, trans );
            result = geomFactory.createSurface( id, tssP, nCRS );
            break;
        case Tin:
            result = transform( (Tin) surface, trans );
            break;
        case TriangulatedSurface:
            // rb: a triangluated surface consists only of triangles
            List<Triangle> tsP = (List<Triangle>) surface.getPatches();
            List<Triangle> ttsP = (List) transform( tsP, trans );
            result = geomFactory.createTriangulatedSurface( id, nCRS, ttsP );
            break;
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Tin transform( Tin tin, Transformation trans )
                            throws TransformationException {
        List<List<LineStringSegment>> stopLines = tin.getStopLines();
        List<List<LineStringSegment>> tStopLines = null;
        if ( stopLines != null ) {
            tStopLines = new ArrayList<List<LineStringSegment>>( stopLines.size() );
            for ( List<LineStringSegment> stopLine : stopLines ) {
                if ( stopLine != null ) {
                    List<LineStringSegment> tStopLine = new ArrayList<LineStringSegment>( stopLine.size() );
                    for ( LineStringSegment lss : stopLine ) {
                        if ( lss != null ) {
                            LineStringSegment tlss = (LineStringSegment) transform( lss, trans );
                            tStopLine.add( tlss );
                        }
                    }
                    tStopLines.add( tStopLine );
                }
            }
        }
        List<List<LineStringSegment>> breakLines = tin.getBreakLines();
        List<List<LineStringSegment>> tBreakLines = null;
        if ( breakLines != null ) {
            tBreakLines = new ArrayList<List<LineStringSegment>>( breakLines.size() );
            for ( List<LineStringSegment> breakLine : breakLines ) {
                if ( breakLine != null ) {
                    List<LineStringSegment> tBreakLine = new ArrayList<LineStringSegment>( breakLine.size() );
                    for ( LineStringSegment lss : breakLine ) {
                        if ( lss != null ) {
                            LineStringSegment tlss = (LineStringSegment) transform( lss, trans );
                            tBreakLine.add( tlss );
                        }
                    }
                    tBreakLines.add( tBreakLine );
                }
            }
        }
        Points cPoints = tin.getControlPoints();
        Points tcPoints = null;
        if ( cPoints != null ) {
            tcPoints = transform( cPoints, trans );
        }
        // rb: a triangluated surface consists only of triangles
        List<Triangle> patches = tin.getPatches();
        List<Triangle> tPatches = null;
        if ( patches != null ) {
            tPatches = (List) transform( patches, trans );
        }
        return geomFactory.createTin( tin.getId(), getWrappedTargetCRS(), tStopLines, tBreakLines,
                                      (Length) tin.getMaxLength( null ), tcPoints, tPatches );
    }

    private Polygon transform( Polygon polygon, Transformation trans )
                            throws TransformationException {
        Ring exterior = polygon.getExteriorRing();
        LinearRing tExteriorRing = transform( exterior, trans );
        List<Ring> interiorRings = polygon.getInteriorRings();
        List<Ring> tInteriorRings = new ArrayList<Ring>( interiorRings == null ? 0 : interiorRings.size() );
        if ( interiorRings != null && !interiorRings.isEmpty() ) {
            for ( Ring interior : interiorRings ) {
                if ( interior != null ) {
                    tInteriorRings.add( transform( interior, trans ) );
                }
            }
        }
        return geomFactory.createPolygon( polygon.getId(), getWrappedTargetCRS(), tExteriorRing, tInteriorRings );
    }

    private LinearRing transform( Ring ring, Transformation trans )
                            throws TransformationException {
        if ( ring != null ) {
            // TODO DefaultRing.getAsLineString currently returns an UnsupportedOpertionException
            // interior.getAsLineString().getControlPoints(),
            Points cP = ring.getControlPoints();
            Points tcP = transform( cP, trans );
            return geomFactory.createLinearRing( ring.getId(), getWrappedTargetCRS(), tcP );
        }
        return null;
    }

    private List<SurfacePatch> transform( List<? extends SurfacePatch> sPatches, Transformation trans )
                            throws TransformationException {
        List<SurfacePatch> result = new ArrayList<SurfacePatch>( sPatches.size() );
        for ( SurfacePatch patch : sPatches ) {
            SurfacePatchType pT = patch.getSurfacePatchType();
            SurfacePatch tsp = null;
            switch ( pT ) {
            case GRIDDED_SURFACE_PATCH:
                tsp = transform( (GriddedSurfacePatch) patch, trans );
                break;
            case POLYGON_PATCH:
                tsp = transform( (PolygonPatch) patch, trans );
                break;
            }
            result.add( tsp );
        }
        return result;
    }

    private CompositeSurface transform( CompositeSurface compositeSurface, Transformation trans )
                            throws TransformationException {
        List<Surface> tSurfaces = new ArrayList<Surface>( compositeSurface.size() );
        for ( Surface s : compositeSurface ) {
            Surface ts = transform( s, trans );
            tSurfaces.add( ts );
        }
        return geomFactory.createCompositeSurface( compositeSurface.getId(), getWrappedTargetCRS(), tSurfaces );
    }

    private PolygonPatch transform( PolygonPatch patch, Transformation trans )
                            throws TransformationException {

        Ring exterior = patch.getExteriorRing();
        LinearRing transformedExteriorRing = transform( exterior, trans );
        PolygonPatch result = null;
        PolygonPatchType type = patch.getPolygonPatchType();
        switch ( type ) {
        case POLYGON_PATCH:
            List<Ring> interiorRings = ( patch ).getInteriorRings();
            List<Ring> transformedInteriorRings = new ArrayList<Ring>( interiorRings == null ? 0 : interiorRings.size() );
            if ( interiorRings != null && !interiorRings.isEmpty() ) {
                for ( Ring interior : interiorRings ) {
                    if ( interior != null ) {
                        LinearRing lr = transform( interior, trans );
                        transformedInteriorRings.add( lr );
                    }
                }
            }
            result = geomFactory.createPolygonPatch( transformedExteriorRing, transformedInteriorRings );
            break;
        case RECTANGLE:
            result = geomFactory.createRectangle( transformedExteriorRing );
            break;
        case TRIANGLE:
            result = geomFactory.createTriangle( transformedExteriorRing );
            break;
        }

        return result;
    }

    private SurfacePatch transform( GriddedSurfacePatch patch, Transformation trans )
                            throws TransformationException {
        GriddedSurfaceType type = patch.getGriddedSurfaceType();
        GriddedSurfacePatch result = null;
        List<Points> rows = patch.getRows();
        if ( rows != null && !rows.isEmpty() ) {
            List<Points> tRows = new ArrayList<Points>( rows.size() );
            for ( Points row : rows ) {
                Points tRow = transform( row, trans );
                if ( tRow != null ) {
                    tRows.add( tRow );
                }
            }
            switch ( type ) {
            case CONE:
                result = geomFactory.createCone( tRows );
                break;
            case CYLINDER:
                result = geomFactory.createCylinder( tRows );
                break;
            case GRIDDED_SURFACE_PATCH:
                result = geomFactory.createGriddedSurfacePatch( tRows );
                break;
            case SPHERE:
                result = geomFactory.createSphere( tRows );
                break;
            }
        }
        return result;
    }

}
