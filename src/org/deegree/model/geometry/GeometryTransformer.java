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

import javax.media.jai.Interpolation;
import javax.vecmath.Point3d;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.Transformer;
import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.crs.exceptions.TransformationException;
import org.deegree.model.crs.exceptions.UnknownCRSException;
import org.deegree.model.crs.transformations.coordinate.CRSTransformation;
import org.deegree.model.geometry.multi.MultiCurve;
import org.deegree.model.geometry.multi.MultiPoint;
import org.deegree.model.geometry.multi.MultiSurface;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.Envelope;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Surface;
import org.deegree.model.i18n.Messages;

/**
 * class for transforming deegree geometries to new coordinate reference systems.
 * 
 * <p>
 * ------------------------------------------------------------
 * </p>
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GeometryTransformer extends Transformer {

    private static final GeometryFactory geomFactory = GeometryFactoryCreator.getInstance().getGeometryFactory();

    private static Log LOG = LogFactory.getLog( GeometryTransformer.class );

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
     * transforms a GridCoverage into another coordinate reference system.
     * 
     * @param coverage
     *            grid coverage to transform
     * @param targetBBOX
     *            envelope for the target coverage
     * @param dstWidth
     *            width of the output coverage in pixel
     * @param dstHeight
     *            height of the output coverage in pixel
     * @param refPointsGridSize
     *            size of the grid used to calculate polynoms coefficients. E.g. 2 -&lg; 4 points, 3 -&lg; 9 points ...<br>
     *            Must be &lg;= 2. Accuracy of coefficients increase with size of the grid. Speed decreases with size of
     *            the grid.
     * @param degree
     *            The degree of the polynomial is supplied as an argument.
     * @param interpolation
     *            interpolation method for warping the passed coverage. Can be <code>null</code>. In this case
     *            'Nearest Neighbor' will be used as default
     * @return a transformed GridCoverage.
     * @throws TransformationException
     *             if the gridCoverage could not be created or the transformation failed
     */
    // public GridCoverage transform( AbstractGridCoverage coverage, Envelope targetBBOX, int dstWidth, int dstHeight,
    // int refPointsGridSize, int degree, Interpolation interpolation )
    public Object transform( Object coverage, Envelope targetBBOX, int dstWidth, int dstHeight, int refPointsGridSize,
                             int degree, Interpolation interpolation )
                            throws TransformationException {
        throw new UnsupportedOperationException( "Currently not adapted to deegree 3" );
        // BufferedImage img = coverage.getAsImage( -1, -1 );
        // PT_CoordinatePoint min = coverage.getEnvelope().minCP;
        // PT_CoordinatePoint max = coverage.getEnvelope().maxCP;
        //
        // // create transformation object to transform reference points
        // // from the target CRS to the source (native) CRS
        // CoordinateSystem crs = coverage.getCoordinateReferenceSystem();
        // CoordinateSystem targetMCRS = new CoordinateSystem( targetCRS, null );
        //
        // Envelope sourceBBOX = GeometryFactory.createEnvelope( min.ord[0], min.ord[1], max.ord[0], max.ord[1], crs );
        //
        // GeoTransform sourceGT = new WorldToScreenTransform( sourceBBOX.getMin().getX(), sourceBBOX.getMin().getY(),
        // sourceBBOX.getMax().getX(), sourceBBOX.getMax().getY(), 0,
        // 0, img.getWidth() - 1, img.getHeight() - 1 );
        // GeoTransform targetGT = new WorldToScreenTransform( targetBBOX.getMin().getX(), targetBBOX.getMin().getY(),
        // targetBBOX.getMax().getX(), targetBBOX.getMax().getY(), 0,
        // 0, dstWidth - 1, dstHeight - 1 );
        //
        // // create/calculate reference points
        // float dx = ( dstWidth - 1 ) / (float) ( refPointsGridSize - 1 );
        // float dy = ( dstHeight - 1 ) / (float) ( refPointsGridSize - 1 );
        // float[] srcCoords = new float[refPointsGridSize * refPointsGridSize * 2];
        // float[] targetCoords = new float[refPointsGridSize * refPointsGridSize * 2];
        // int k = 0;
        //
        // GeometryTransformer sourceCoordGT = new GeometryTransformer( crs );
        // for ( int i = 0; i < refPointsGridSize; i++ ) {
        // for ( int j = 0; j < refPointsGridSize; j++ ) {
        // targetCoords[k] = i * dx;
        // targetCoords[k + 1] = j * dy;
        // double x = targetGT.getSourceX( targetCoords[k] );
        // double y = targetGT.getSourceY( targetCoords[k + 1] );
        // Point point = GeometryFactory.createPoint( x, y, targetMCRS );
        // point = (Point) sourceCoordGT.transform( point );
        // srcCoords[k] = (float) sourceGT.getDestX( point.getX() );
        // srcCoords[k + 1] = (float) sourceGT.getDestY( point.getY() );
        // // LOG.debug( String.format( "%.4f %.4f -> %.4f %.4f ",
        // // srcCoords[k], srcCoords[k+1],
        // // targetCoords[k], targetCoords[k+1]) );
        // k += 2;
        // }
        // }
        //
        // // create warp object from reference points and desired interpolation
        // WarpPolynomial warp = WarpPolynomial.createWarp( srcCoords, 0, targetCoords, 0, srcCoords.length, 1f, 1f, 1f,
        // 1f, degree );
        //
        // if ( interpolation == null ) {
        // interpolation = new InterpolationNearest();
        // }
        //
        // // Create and perform the warp operation.
        // ParameterBlock pb = new ParameterBlock();
        // pb.addSource( img );
        // pb.add( warp );
        // pb.add( interpolation );
        //
        // // Limit output size, otherwise the result will be a bit larger
        // // (the polynomial warp will overlap the correct image border)
        // ImageLayout layout = new ImageLayout();
        // layout.setMinX( 0 );
        // layout.setMinY( 0 );
        // layout.setWidth( dstWidth );
        // layout.setHeight( dstHeight );
        // RenderingHints rh = new RenderingHints( JAI.KEY_IMAGE_LAYOUT, layout );
        //
        // img = JAI.create( "warp", pb, rh ).getAsBufferedImage();
        //
        // // create a new GridCoverage from the warp result.
        // // because warping only can be performed on images the
        // // resulting GridCoverage will be an instance of ImageGridCoverage
        // CoverageOffering oldCO = coverage.getCoverageOffering();
        // CoverageOffering coverageOffering = null;
        // if ( oldCO != null ) {
        // try {
        // DomainSet ds = oldCO.getDomainSet();
        // ds.getSpatialDomain().setEnvelops( new Envelope[] { targetBBOX } );
        // coverageOffering = new CoverageOffering( oldCO.getName(), oldCO.getLabel(), oldCO.getDescription(),
        // oldCO.getMetadataLink(), oldCO.getLonLatEnvelope(),
        // oldCO.getKeywords(), ds, oldCO.getRangeSet(),
        // oldCO.getSupportedCRSs(), oldCO.getSupportedFormats(),
        // oldCO.getSupportedInterpolations(), oldCO.getExtension() );
        // } catch ( WCSException e ) {
        // throw new TransformationException( Messages.getMessage( "CRS_CO_CREATION_ERROR",
        // crs.getIdentifier(),
        // targetCRS.getIdentifier(), e.getMessage() ),
        // e );
        // } catch ( OGCException e ) {
        // throw new TransformationException( Messages.getMessage( "CRS_CO_CREATION_ERROR",
        // crs.getIdentifier(),
        // targetCRS.getIdentifier(), e.getMessage() ),
        // e );
        // }
        // }
        //
        // return new ImageGridCoverage( coverageOffering, targetBBOX, img );
    }

    /**
     * Transforms a <code>Envelope</code> to the target crs of the <code>GeometryTransformer</code> instance
     * 
     * @param envelope
     *            to transform
     * @param sourceCRS
     *            CRS of the envelope
     * @return the transformed envelope
     * @throws TransformationException
     */
    public Envelope transform( Envelope envelope, CoordinateSystem sourceCRS )
                            throws TransformationException {
        throw new UnsupportedOperationException( "Currently not adapted to deegree 3" );
        // Point min = GeometryFactory.createPoint( envelope.getMin().getX(), envelope.getMin().getY(), sourceCRS );
        // Point max = GeometryFactory.createPoint( envelope.getMax().getX(), envelope.getMax().getY(), sourceCRS );
        // min = (Point) transform( min );
        // max = (Point) transform( max );
        // // create bounding box with coordinates
        // return GeometryFactory.createEnvelope( min.getX(), min.getY(), max.getX(), max.getY(), targetCRSWrapper );
    }

    /**
     * Transforms a <code>Envelope</code> to the target crs of the <code>GeometryTransformer</code> instance
     * 
     * This transformation takes rotation and distortion into account when regardDistortion is true. Otherwise the
     * transformed envelope may not contain the whole input envelope.
     * 
     * @param envelope
     *            to transform
     * @param sourceCRS
     *            CRS of the envelope
     * @param regardDistortion
     * 
     * @return the transformed envelope
     * @throws TransformationException
     * @throws UnknownCRSException
     */
    public Envelope transform( Envelope envelope, String sourceCRS, boolean regardDistortion )
                            throws TransformationException, UnknownCRSException {
        CoordinateSystem crs = CRSFactory.create( sourceCRS );
        return transform( envelope, crs, regardDistortion );
    }

    /**
     * Transforms a <code>Envelope</code> to the target crs of the <code>GeometryTransformer</code> instance
     * 
     * This transformation takes rotation and distortion into account when regardDistortion is true. Otherwise the
     * transformed envelope may not contain the whole input envelope.
     * 
     * @param envelope
     *            to transform
     * @param sourceCRS
     *            CRS of the envelope
     * @param regardDistortion
     * 
     * @return the transformed envelope
     * @throws TransformationException
     */
    public Envelope transform( Envelope envelope, CoordinateSystem sourceCRS, boolean regardDistortion )
                            throws TransformationException {

        if ( !regardDistortion ) {
            throw new UnsupportedOperationException( "Currently not adapted to deegree 3" );

        }
        double x1 = envelope.getMin().getX();
        double y1 = envelope.getMin().getY();
        double x2 = envelope.getMax().getX();
        double y2 = envelope.getMax().getY();

        double width = envelope.getWidth();
        double height = envelope.getHeight();

        GeometryFactory geomFactory = GeometryFactoryCreator.getInstance().getGeometryFactory();
        List<Point> points = new ArrayList<Point>();
        points.add( geomFactory.createPoint( new double[] { x1, y1 }, sourceCRS ) );
        points.add( geomFactory.createPoint( new double[] { x1, y2 }, sourceCRS ) );
        points.add( geomFactory.createPoint( new double[] { x2, y2 }, sourceCRS ) );
        points.add( geomFactory.createPoint( new double[] { x2, y1 }, sourceCRS ) );
        points.add( geomFactory.createPoint( new double[] { x1, y1 + height }, sourceCRS ) );
        points.add( geomFactory.createPoint( new double[] { x1 + width, y1 }, sourceCRS ) );
        points.add( geomFactory.createPoint( new double[] { x2, y1 + height }, sourceCRS ) );
        points.add( geomFactory.createPoint( new double[] { x1 + width, y2 }, sourceCRS ) );
        // transform with 8 points instead of only the min and max point
        // double[] coords = new double[] { x1, y1, x1, y2, x2, y2, x2, y1, x1, y1 + height, x1 + width, y1, x2,
        // y1 + height, x1 + width, y2 };

        Geometry envelopeGeom = null;
        try {
            envelopeGeom = GeometryFactoryCreator.getInstance().getGeometryFactory().createMultiPoint( points );
        } catch ( GeometryException e ) {
            throw new TransformationException( Messages.getMessage( "CRS_TRANSFORMATION_ERROR",
                                                                    sourceCRS.getIdentifier(),
                                                                    getTargetCRS().getIdentifier(), e.getMessage() ), e );
        }
        envelopeGeom = transform( envelopeGeom );

        return envelopeGeom.getEnvelope();

    }

    /**
     * Transforms a <code>Envelope</code> to the target crs of the <code>GeometryTransformer</code> instance
     * 
     * @param envelope
     *            to transform
     * @param sourceCRS
     *            CRS of the envelope
     * @return the transformed envelope
     * @throws TransformationException
     *             if the transformation did not succeed.
     * @throws UnknownCRSException
     *             if the given string is unknown to the CRSProvider
     */
    public Envelope transform( Envelope envelope, String sourceCRS )
                            throws TransformationException, UnknownCRSException {

        org.deegree.model.crs.coordinatesystems.CoordinateSystem crs = CRSFactory.create( sourceCRS );
        return transform( envelope, crs );
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
        CRSTransformation trans = createCRSTransformation( geo.getCoordinateSystem() );
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
            }
        } catch ( GeometryException ge ) {
            throw new TransformationException( Messages.getMessage( "CRS_TRANSFORMATION_ERROR",
                                                                    geo.getCoordinateSystem().getIdentifier(),
                                                                    getTargetCRS().getIdentifier(), ge.getMessage() ),
                                               ge );
        }
        return transformedGeometry;
    }

    /**
     * transforms the submitted curve to the target coordinate reference system
     * 
     * @throws GeometryException
     */
    private Curve transform( Curve geo, CRSTransformation trans )
                            throws GeometryException {
        throw new UnsupportedOperationException( "Currently not adapted to deegree 3" );
        // CurveSegment[] newcus = new CurveSegment[geo.getCurveSegments().size()];
        // for( CurveSegment segment : geo.getCurveSegments() ){
        // //for ( int i = 0; i < geo.getCurveSegments().size(); i++ ) {
        // //CurveSegment cus = geo.getCurveSegmentAt( i );
        // Position[] pos = segment.getPositions();
        // pos = transform( pos, trans );
        // newcus[i] = GeometryFactory.createCurveSegment( pos, targetCRSWrapper );
        // }
        // return GeometryFactory.createCurve( newcus );
    }

    /**
     * transforms the submitted multi curve to the target coordinate reference system
     * 
     * @throws GeometryException
     */
    private MultiCurve transform( MultiCurve geo, CRSTransformation trans )
                            throws GeometryException {
        throw new UnsupportedOperationException( "Currently not adapted to deegree 3" );
        // Curve[] curves = new Curve[geo.getSize()];
        // for ( int i = 0; i < geo.getSize(); i++ ) {
        // curves[i] = (Curve) transform( geo.getCurveAt( i ), trans );
        // }
        // return GeometryFactory.createMultiCurve( curves );
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
     * @throws GeometryException
     */
    private MultiSurface transform( MultiSurface geo, CRSTransformation trans )
                            throws GeometryException {
        throw new UnsupportedOperationException( "Currently not adapted to deegree 3" );
        // Surface[] surfaces = new Surface[geo.getSize()];
        // for ( int i = 0; i < geo.getSize(); i++ ) {
        // surfaces[i] = (Surface) transform( geo.getSurfaceAt( i ), trans );
        // }
        // return GeometryFactory.createMultiSurface( surfaces );
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
     * transforms an array of Positions to the target coordinate reference system
     */
    // private Position[] transform( Position[] pos, CRSTransformation trans ) {
    private Object[] transform( Object[] pos, CRSTransformation trans ) {
        throw new UnsupportedOperationException( "Currently not adapted to deegree 3" );
        // Position[] newpos = new Position[pos.length];
        // // boolean srcRad = trans.getSourceCRS().getUnits().equals( Unit.DEGREE );
        // // boolean targetRad = trans.getTargetCRS().getUnits().equals( Unit.DEGREE );
        //
        // List<Point3d> coords = new ArrayList<Point3d>( pos.length );
        // for ( Position p : pos ) {
        // // if ( srcRad ) {
        // // coords.add( new Point3d( Math.toRadians( p.getX() ), Math.toRadians( p.getY() ),
        // // p.getZ() ) );
        // // } else {
        // coords.add( p.getAsPoint3d() );
        // // }
        // }
        // // List<Point3d> result = trans.doTransform( coords );
        // List<Point3d> result = new ArrayList<Point3d>( coords.size() );
        // TransformationException exception = null;
        // try {
        // result = trans.doTransform( coords );
        // } catch ( TransformationException te ) {
        // List<Point3d> tResult = te.getTransformedPoints();
        // if ( tResult != null && tResult.size() > 0 ) {
        // result = tResult;
        // }
        // exception = te;
        // }
        //
        // if ( LOG_TRANSORM.isDebug() || LOG.isDebugEnabled() ) {
        // Map<Integer, String> errorMessages = null;
        // if ( exception != null ) {
        // errorMessages = exception.getTransformErrors();
        // }
        //
        // for ( int i = 0; i < coords.size(); ++i ) {
        // StringBuilder sb = new StringBuilder( 1000 );
        // Point3d coord = coords.get( i );
        // Point3d resultCoord = result.get( i );
        // if ( resultCoord == null ) {
        // resultCoord = new Point3d( coord );
        // }
        // sb.append( trans.getSourceCRS().getIdentifier() );
        // sb.append( ";" );
        // sb.append( coord.x );
        // sb.append( ";" );
        // sb.append( coord.y );
        // sb.append( ";" );
        // if ( trans.getSourceDimension() == 3 ) {
        // sb.append( coord.z );
        // sb.append( ";" );
        // }
        // sb.append( trans.getTargetCRS().getIdentifier() );
        // sb.append( ";" );
        // sb.append( resultCoord.x );
        // sb.append( ";" );
        // sb.append( resultCoord.y );
        // sb.append( ";" );
        // if ( trans.getTargetDimension() == 3 ) {
        // sb.append( resultCoord.z );
        // sb.append( ";" );
        // }
        // String successString = "Success";
        // if ( errorMessages != null ) {
        // String tmp = errorMessages.get( new Integer( i ) );
        // if ( tmp != null && !"".equals( tmp.trim() ) ) {
        // successString = tmp;
        // }
        // }
        // sb.append( successString );
        // LOG_TRANSORM.logDebug( sb.toString() );
        // // LOG.debug( sb.toString() );
        // }
        //
        // }
        // for ( int i = 0; i < result.size(); ++i ) {
        // Point3d p = result.get( i );
        // if ( p != null && i < newpos.length ) {
        // newpos[i] = GeometryFactory.createPosition( p );
        // }
        // }
        // return newpos;
    }

    /**
     * transforms the submitted surface to the target coordinate reference system
     * 
     * @throws GeometryException
     *             if the surface cannot be retrieved or if the surface patch could not be created.
     */
    private Geometry transform( Surface geo, CRSTransformation trans )
                            throws GeometryException {
        throw new UnsupportedOperationException( "Currently not adapted to deegree 3" );
        // int cnt = geo.getNumberOfSurfacePatches();
        // SurfacePatch[] patches = new SurfacePatch[cnt];
        //
        // for ( int i = 0; i < cnt; i++ ) {
        // SurfacePatch p = geo.getSurfacePatchAt( i );
        // Position[] ex = p.getExteriorRing();
        // ex = transform( ex, trans );
        //
        // Position[][] innerRings = p.getInteriorRings();
        // Position[][] transformedInnerRings = null;
        //
        // if ( innerRings != null ) {
        // transformedInnerRings = new Position[innerRings.length][];
        //
        // for ( int k = 0; k < innerRings.length; k++ ) {
        // transformedInnerRings[k] = transform( innerRings[k], trans );
        // }
        // }
        //
        // patches[i] = GeometryFactory.createSurfacePatch( ex, transformedInnerRings, p.getInterpolation(),
        // this.targetCRSWrapper );
        // }
        //
        // // at the moment only polygons made of one patch are supported
        // return GeometryFactory.createSurface( patches[0] );
    }

}
