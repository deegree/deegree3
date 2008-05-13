//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: klaus.greve@uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.model.geometry;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;

import org.deegree.model.crs.Transformer;
import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.crs.exceptions.TransformationException;
import org.deegree.model.crs.exceptions.UnknownCRSException;
import org.deegree.model.crs.transformations.coordinate.CRSTransformation;
import org.deegree.model.geometry.multi.MultiCurve;
import org.deegree.model.geometry.multi.MultiPoint;
import org.deegree.model.geometry.multi.MultiSurface;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.CurveSegment;
import org.deegree.model.geometry.primitive.Envelope;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Surface;
import org.deegree.model.geometry.primitive.SurfacePatch;
import org.deegree.model.i18n.Messages;

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

    private static final GeometryFactory geomFactory = GeometryFactoryCreator.getInstance().getGeometryFactory();

    //private static Log LOG = LogFactory.getLog( GeometryTransformer.class );

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
     */
    public Geometry transform( Geometry geo )
                            throws TransformationException, IllegalArgumentException {
        if ( geo.getCoordinateSystem() == null ) {
            throw new IllegalArgumentException( Messages.getMessage( "CRS_GEOMETRY_HAS_NO_CRS" ) );
        }
        return transform( geo, createCRSTransformation( geo.getCoordinateSystem() ) );
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
    private Geometry transform( Geometry geo, CRSTransformation trans )
                            throws TransformationException, IllegalArgumentException {
        Geometry transformedGeometry = null;
        try {
            if ( geo instanceof Point ) {
                transformedGeometry = transform( (Point) geo, trans );
            } else if ( geo instanceof Curve ) {
                transformedGeometry = transform( (Curve) geo, trans );
            } else if ( geo instanceof Surface ) {
                transformedGeometry = transform( (Surface) geo, trans );
            } else if ( geo instanceof MultiPoint ) {
                transformedGeometry = transform( (MultiPoint) geo, trans );
            } else if ( geo instanceof MultiCurve ) {
                transformedGeometry = transform( (MultiCurve) geo, trans );
            } else if ( geo instanceof MultiSurface ) {
                transformedGeometry = transform( (MultiSurface) geo, trans );
            } else if ( geo instanceof Envelope ) {
                transformedGeometry = transform( (Envelope) geo, trans );
            } else {
                throw new IllegalArgumentException( "Unspupported geometry:" + geo.getClass().getName() );
            }
        } catch ( GeometryException ge ) {
            throw new TransformationException( Messages.getMessage( "CRS_TRANSFORMATION_ERROR",
                                                                    geo.getCoordinateSystem().getIdentifier(),
                                                                    getTargetCRS().getIdentifier(), ge.getMessage() ),
                                               ge );
        }
        return transformedGeometry;
    }

    private Geometry transform( Envelope envelope, CRSTransformation trans )
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
    private Envelope transform( Envelope envelope, CRSTransformation trans, int numPoints )
                            throws TransformationException {
        int pointsPerSide;
        if ( numPoints < 4 ) {
            pointsPerSide = 0;
        } else {
            pointsPerSide = (int) Math.ceil( ( numPoints - 4 ) / 4.0 );
        }

        double x1 = envelope.getMin().getX();
        double y1 = envelope.getMin().getY();
        double x2 = envelope.getMax().getX();
        double y2 = envelope.getMax().getY();

        double width = envelope.getWidth();
        double height = envelope.getHeight();

        double xStep = width / ( pointsPerSide - 1 );
        double yStep = height / ( pointsPerSide - 1 );

        double precision = envelope.getPrecision();

        List<Point> points = new ArrayList<Point>( pointsPerSide * 4 + 4 );

        for ( int i = 0; i <= pointsPerSide + 1; i++ ) {
            points.add( geomFactory.createPoint( new double[] { x1 + i * xStep, y1 }, precision, null ) );
            points.add( geomFactory.createPoint( new double[] { x1 + i * xStep, y2 }, precision, null ) );
        }

        for ( int i = 1; i <= pointsPerSide; i++ ) {
            points.add( geomFactory.createPoint( new double[] { x1, y1 + i * yStep }, precision, null ) );
            points.add( geomFactory.createPoint( new double[] { x2, y1 + i * yStep }, precision, null ) );
        }

        MultiPoint envGeometry = geomFactory.createMultiPoint( points );
        MultiPoint transformedEnvGeometry = transform( envGeometry, trans );

        return transformedEnvGeometry.getEnvelope();
    }

    /**
     * transforms the submitted curve to the target coordinate reference system
     * 
     * @throws TransformationException
     */
    private Curve transform( Curve geo, CRSTransformation trans )
                            throws TransformationException {
        CurveSegment[] curveSegments = new CurveSegment[geo.getCurveSegments().size()];
        int i = 0;
        for ( CurveSegment segment : geo.getCurveSegments() ) {
            List<Point> pos = segment.getPoints();
            pos = transform( pos, trans );
            curveSegments[i++] = geomFactory.createCurveSegment( pos );
        }
        return geomFactory.createCurve( curveSegments, geo.getOrientation(), trans.getTargetCRS() );
    }

    /**
     * transforms the submitted multi curve to the target coordinate reference system
     * 
     * @throws TransformationException
     */
    private MultiCurve transform( MultiCurve geo, CRSTransformation trans )
                            throws TransformationException {
        List<Curve> curves = new ArrayList<Curve>( geo.getNumberOfGeometries() );
        for ( int i = 0; i < geo.getNumberOfGeometries(); i++ ) {
            curves.add( transform( geo.getGeometryAt( i ), trans ) );
        }
        return geomFactory.createMultiCurve( curves );
    }

    /**
     * transforms the submitted multi point to the target coordinate reference system
     * 
     * @throws TransformationException
     */
    private MultiPoint transform( MultiPoint geo, CRSTransformation trans )
                            throws TransformationException {
        List<Point> points = new ArrayList<Point>( geo.getNumberOfGeometries() );
        for ( Point p : geo.getGeometries() ) {
            points.add( transform( p, trans ) );
        }
        return geomFactory.createMultiPoint( points );
    }

    /**
     * transforms the submitted multi surface to the target coordinate reference system
     * 
     * @throws TransformationException
     */
    private MultiSurface transform( MultiSurface geo, CRSTransformation trans )
                            throws TransformationException {
        List<Surface> surfaces = new ArrayList<Surface>( geo.getNumberOfGeometries() );
        for ( int i = 0; i < geo.getNumberOfGeometries(); i++ ) {
            surfaces.add( transform( geo.getGeometryAt( i ), trans ) );
        }
        return geomFactory.createMultiSurface( surfaces );
    }

    /**
     * transforms the list of points
     * 
     * @throws TransformationException
     */
    private List<Point> transform( List<Point> points, CRSTransformation trans )
                            throws TransformationException {

        List<Point> result = new ArrayList<Point>( points.size() );
        for ( Point point : points ) {
            Point3d coord = new Point3d( point.getX(), point.getY(), point.getZ() );
            Point3d tmp = new Point3d( coord );

            tmp = trans.doTransform( coord );

            if ( Double.isNaN( point.getZ() ) ) {
                result.add( geomFactory.createPoint( new double[] { tmp.x, tmp.y }, point.getPrecision(),
                                                     trans.getTargetCRS() ) );
            }
            result.add( geomFactory.createPoint( new double[] { tmp.x, tmp.y, tmp.z }, point.getPrecision(),
                                                 trans.getTargetCRS() ) );
        }
        return result;
    }

    /**
     * transforms the submitted point to the target coordinate reference system
     * 
     * @throws TransformationException
     */
    private Point transform( Point geo, CRSTransformation trans )
                            throws TransformationException {

        Point3d coord = new Point3d( geo.getX(), geo.getY(), geo.getZ() );
        Point3d result = new Point3d( coord );

        result = trans.doTransform( coord );

        if ( Double.isNaN( geo.getZ() ) ) {
            return geomFactory.createPoint( new double[] { result.x, result.y }, geo.getPrecision(),
                                            trans.getTargetCRS() );
        }
        return geomFactory.createPoint( new double[] { result.x, result.y, result.z }, geo.getPrecision(),
                                        trans.getTargetCRS() );
    }

    /**
     * transforms the submitted surface to the target coordinate reference system
     * 
     * @throws TransformationException
     */
    private Surface transform( Surface geo, CRSTransformation trans )
                            throws TransformationException {

        List<SurfacePatch> patches = new ArrayList<SurfacePatch>( geo.getPatches().size() );
        for ( SurfacePatch patch : geo.getPatches() ) {
            List<Curve> boundaries = new ArrayList<Curve>( patch.getBoundary().size() );
            for ( Curve ring : patch.getBoundary() ) {
                boundaries.add( transform( ring, trans ) );
            }
            patches.add( geomFactory.createSurfacePatch( boundaries ) );
        }

        return geomFactory.createSurface( patches, trans.getTargetCRS() );
    }

}
