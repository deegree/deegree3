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
package org.deegree.model.crs;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.WarpPolynomial;
import javax.vecmath.Point3d;

import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.transformations.Transformation;
import org.deegree.crs.transformations.TransformationFactory;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.graphics.transformation.GeoTransform;
import org.deegree.graphics.transformation.WorldToScreenTransform;
import org.deegree.i18n.Messages;
import org.deegree.model.coverage.grid.AbstractGridCoverage;
import org.deegree.model.coverage.grid.GridCoverage;
import org.deegree.model.coverage.grid.ImageGridCoverage;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.spatialschema.Curve;
import org.deegree.model.spatialschema.CurveSegment;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.MultiCurve;
import org.deegree.model.spatialschema.MultiPoint;
import org.deegree.model.spatialschema.MultiSurface;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.Surface;
import org.deegree.model.spatialschema.SurfacePatch;
import org.deegree.ogcbase.OGCException;
import org.deegree.ogcwebservices.wcs.WCSException;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageOffering;
import org.deegree.ogcwebservices.wcs.describecoverage.DomainSet;

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
public class GeoTransformer {

    private static ILogger LOG_TRANSORM = LoggerFactory.getLogger( GeoTransformer.class.getCanonicalName()
                                                                   + ".TransformLogger" );

    private static ILogger LOG = LoggerFactory.getLogger( GeoTransformer.class );

    private CoordinateSystem targetCRS = null;

    private org.deegree.model.crs.CoordinateSystem targetCRSWrapper = null;

    private Transformation definedTransformation = null;

    /**
     * Creates a new GeoTransformer object.
     *
     * @param targetCRS
     * @throws InvalidParameterException
     *             if the given parameter is null.
     */
    public GeoTransformer( org.deegree.model.crs.CoordinateSystem targetCRS ) throws InvalidParameterException {
        if ( targetCRS == null ) {
            throw new InvalidParameterException( Messages.getMessage( "CRS_PARAMETER_NOT_NULL",
                                                                      "GeoTransformer(CoordinateSystem)", "targetCRS" ) );
        }
        this.targetCRS = targetCRS.getCRS();
        targetCRSWrapper = targetCRS;
    }

    /**
     * Creates a new GeoTransformer object.
     *
     * @param targetCRS
     * @throws InvalidParameterException
     *             if the given parameter is null.
     */
    public GeoTransformer( CoordinateSystem targetCRS ) {
        if ( targetCRS == null ) {
            throw new InvalidParameterException( Messages.getMessage( "CRS_PARAMETER_NOT_NULL",
                                                                      "GeoTransformer(CoordinateSystem)", "targetCRS" ) );
        }
        this.targetCRS = targetCRS;
    }

    /**
     * @param definedTransformation
     *            to use instead of the CRSFactory.
     */
    public GeoTransformer( Transformation definedTransformation ) {
        if ( definedTransformation == null ) {
            throw new InvalidParameterException( Messages.getMessage( "CRS_PARAMETER_NOT_NULL",
                                                                      "GeoTransformer(CRSTransformation)", "targetCRS" ) );
        }
        targetCRS = definedTransformation.getTargetCRS();
        targetCRSWrapper = new org.deegree.model.crs.CoordinateSystem( targetCRS );
        this.definedTransformation = definedTransformation;
    }

    /**
     * Creates a new GeoTransformer object, with the given id as the target CRS.
     *
     * @param targetCRS
     *            an identifier to which all other CRS's shall be transformed.
     * @throws UnknownCRSException
     *             if the given crs name could not be mapped to a valid (configured) crs.
     * @throws InvalidParameterException
     *             if the given parameter is null.
     */
    public GeoTransformer( String targetCRS ) throws UnknownCRSException, InvalidParameterException {
        this( CRSFactory.create( targetCRS ) );
    }

    /**
     * transforms a GridCoverage into another coordinate reference system.
     *
     * @param coverage
     *            grid coverage to definedTransformation
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
     *            interpolation method for warping the passed coverage. Can be <code>null</code>. In this case 'Nearest
     *            Neighbor' will be used as default
     * @return a transformed GridCoverage.
     * @throws CRSTransformationException
     *             if the gridCoverage could not be created or the transformation failed
     */
    public GridCoverage transform( AbstractGridCoverage coverage, Envelope targetBBOX, int dstWidth, int dstHeight,
                                   int refPointsGridSize, int degree, Interpolation interpolation )
                            throws CRSTransformationException {

        BufferedImage img = coverage.getAsImage( -1, -1 );
        Position min = coverage.getEnvelope().getMin();
        Position max = coverage.getEnvelope().getMax();

        // create transformation object to definedTransformation reference points
        // from the target CRS to the source (native) CRS
        org.deegree.model.crs.CoordinateSystem crs = coverage.getCoordinateReferenceSystem();

        Envelope sourceBBOX = GeometryFactory.createEnvelope( min.getX(), min.getY(), max.getX(), max.getY(), crs );

        img = transform( img, sourceBBOX, targetBBOX, dstWidth, dstHeight, refPointsGridSize, degree, interpolation );

        // create a new GridCoverage from the warp result.
        // because warping only can be performed on images the
        // resulting GridCoverage will be an instance of ImageGridCoverage
        CoverageOffering oldCO = coverage.getCoverageOffering();
        CoverageOffering coverageOffering = null;
        if ( oldCO != null ) {
            try {
                DomainSet ds = oldCO.getDomainSet();
                ds.getSpatialDomain().setEnvelops( new Envelope[] { targetBBOX } );
                coverageOffering = new CoverageOffering( oldCO.getName(), oldCO.getLabel(), oldCO.getDescription(),
                                                         oldCO.getMetadataLink(), oldCO.getLonLatEnvelope(),
                                                         oldCO.getKeywords(), ds, oldCO.getRangeSet(),
                                                         oldCO.getSupportedCRSs(), oldCO.getSupportedFormats(),
                                                         oldCO.getSupportedInterpolations(), oldCO.getExtension() );
            } catch ( WCSException e ) {
                throw new CRSTransformationException( Messages.getMessage( "CRS_CO_CREATION_ERROR",
                                                                           crs.getIdentifier(),
                                                                           targetCRS.getIdentifier(), e.getMessage() ),
                                                      e );
            } catch ( OGCException e ) {
                throw new CRSTransformationException( Messages.getMessage( "CRS_CO_CREATION_ERROR",
                                                                           crs.getIdentifier(),
                                                                           targetCRS.getIdentifier(), e.getMessage() ),
                                                      e );
            }
        }

        return new ImageGridCoverage( coverageOffering, targetBBOX, img );
    }

    /**
     * transforms an image into another coordinate reference system.
     *
     * @param img
     *            the image to definedTransformation
     * @param sourceBBOX
     *            envelope of the source image
     * @param targetBBOX
     *            envelope for the target image
     * @param dstWidth
     *            width of the output image in pixel
     * @param dstHeight
     *            height of the output image in pixel
     * @param refPointsGridSize
     *            size of the grid used to calculate polynoms coefficients. E.g. 2 -&lg; 4 points, 3 -&lg; 9 points ...<br>
     *            Must be &lg;= 2. Accuracy of coefficients increase with size of the grid. Speed decreases with size of
     *            the grid.
     * @param degree
     *            The degree of the polynomial is supplied as an argument.
     * @param interpolation
     *            interpolation method for warping the passed image. Can be <code>null</code>. In this case 'Nearest
     *            Neighbor' will be used as default
     * @return a transformed image.
     * @throws CRSTransformationException
     *             if the image could not be created or the transformation failed
     */
    public BufferedImage transform( BufferedImage img, Envelope sourceBBOX, Envelope targetBBOX, int dstWidth,
                                    int dstHeight, int refPointsGridSize, int degree, Interpolation interpolation )
                            throws CRSTransformationException {

        // create transformation object to definedTransformation reference points
        // from the target CRS to the source (native) CRS
        org.deegree.model.crs.CoordinateSystem crs = sourceBBOX.getCoordinateSystem();
        // org.deegree.model.crs.CoordinateSystem targetMCRS = new org.deegree.model.crs.CoordinateSystem( targetCRS,
        // null );

        GeoTransform sourceGT = new WorldToScreenTransform( sourceBBOX.getMin().getX(), sourceBBOX.getMin().getY(),
                                                            sourceBBOX.getMax().getX(), sourceBBOX.getMax().getY(), 0,
                                                            0, img.getWidth() - 1, img.getHeight() - 1 );
        GeoTransform targetGT = new WorldToScreenTransform( targetBBOX.getMin().getX(), targetBBOX.getMin().getY(),
                                                            targetBBOX.getMax().getX(), targetBBOX.getMax().getY(), 0,
                                                            0, dstWidth - 1, dstHeight - 1 );

        // create/calculate reference points
        float dx = ( dstWidth - 1 ) / (float) ( refPointsGridSize - 1 );
        float dy = ( dstHeight - 1 ) / (float) ( refPointsGridSize - 1 );
        float[] srcCoords = new float[refPointsGridSize * refPointsGridSize * 2];
        float[] targetCoords = new float[refPointsGridSize * refPointsGridSize * 2];
        int k = 0;

        GeoTransformer sourceCoordGT = new GeoTransformer( crs );
        for ( int i = 0; i < refPointsGridSize; i++ ) {
            for ( int j = 0; j < refPointsGridSize; j++ ) {
                targetCoords[k] = i * dx;
                targetCoords[k + 1] = j * dy;
                double x = targetGT.getSourceX( targetCoords[k] );
                double y = targetGT.getSourceY( targetCoords[k + 1] );
                Point point = GeometryFactory.createPoint( x, y, this.targetCRSWrapper );
                point = (Point) sourceCoordGT.transform( point );
                srcCoords[k] = (float) sourceGT.getDestX( point.getX() );
                srcCoords[k + 1] = (float) sourceGT.getDestY( point.getY() );
                // LOG.logDebug( String.format( "%.4f %.4f -> %.4f %.4f ",
                // srcCoords[k], srcCoords[k+1],
                // targetCoords[k], targetCoords[k+1]) );
                k += 2;
            }
        }

        // create warp object from reference points and desired interpolation
        WarpPolynomial warp = WarpPolynomial.createWarp( srcCoords, 0, targetCoords, 0, srcCoords.length, 1f, 1f, 1f,
                                                         1f, degree );

        if ( interpolation == null ) {
            interpolation = new InterpolationNearest();
        }

        // Create and perform the warp operation.
        ParameterBlock pb = new ParameterBlock();
        pb.addSource( img );
        pb.add( warp );
        pb.add( interpolation );
        pb.add( new double[] { 0 } );

        // Limit output size, otherwise the result will be a bit larger
        // (the polynomial warp will overlap the correct image border)
        ImageLayout layout = new ImageLayout();
        layout.setMinX( 0 );
        layout.setMinY( 0 );
        layout.setWidth( dstWidth );
        layout.setHeight( dstHeight );
        RenderingHints rh = new RenderingHints( JAI.KEY_IMAGE_LAYOUT, layout );

        return JAI.create( "warp", pb, rh ).getAsBufferedImage();
    }

    /**
     * transforms a GridCoverage into another coordinate reference system.
     *
     * @param coverage
     *            grid coverage to definedTransformation
     * @param refPointsGridSize
     *            size of the grid used to calculate polynoms coefficients. E.g. 2 -&lg; 4 points, 3 -&lg; 9 points ...<br>
     *            Must be &lg;= 2. Accuracy of coefficients increase with size of the grid. Speed decreases with size of
     *            the grid.
     * @param degree
     *            The degree of the polynomial is supplied as an argument.
     * @param interpolation
     *            interpolation method for warping the passed coverage. Can be <code>null</code>. In this case 'Nearest
     *            Neighbor' will be used as default
     * @return a transformed GridCoverage.
     * @throws CRSTransformationException
     *             if the gridCoverage could not be created or the transformation failed
     */
    @Deprecated
    public GridCoverage transform( AbstractGridCoverage coverage, int refPointsGridSize, int degree,
                                   Interpolation interpolation )
                            throws CRSTransformationException {

        BufferedImage img = coverage.getAsImage( -1, -1 );
        Position min = coverage.getEnvelope().getMin();
        Position max = coverage.getEnvelope().getMax();

        // create transformation object to definedTransformation reference points
        // from the source (native) CRS to the target CRS
        org.deegree.model.crs.CoordinateSystem crs = coverage.getCoordinateReferenceSystem();
        Envelope sourceBBOX = GeometryFactory.createEnvelope( min.getX(), min.getY(), max.getX(), max.getY(), crs );
        Envelope targetBBOX = transform( sourceBBOX, crs );

        GeoTransform sourceGT = new WorldToScreenTransform( sourceBBOX.getMin().getX(), sourceBBOX.getMin().getY(),
                                                            sourceBBOX.getMax().getX(), sourceBBOX.getMax().getY(), 0,
                                                            0, img.getWidth() - 1, img.getHeight() - 1 );
        GeoTransform targetGT = new WorldToScreenTransform( targetBBOX.getMin().getX(), targetBBOX.getMin().getY(),
                                                            targetBBOX.getMax().getX(), targetBBOX.getMax().getY(), 0,
                                                            0, img.getWidth() - 1, img.getHeight() - 1 );

        // create/calculate reference points
        float dx = img.getWidth() / (float) ( refPointsGridSize - 1 );
        float dy = img.getHeight() / (float) ( refPointsGridSize - 1 );
        float[] srcCoords = new float[refPointsGridSize * refPointsGridSize * 2];
        float[] targetCoords = new float[refPointsGridSize * refPointsGridSize * 2];
        int k = 0;
        for ( int i = 0; i < refPointsGridSize; i++ ) {
            for ( int j = 0; j < refPointsGridSize; j++ ) {
                srcCoords[k] = i * dx;
                srcCoords[k + 1] = j * dy;
                double x = sourceGT.getSourceX( srcCoords[k] );
                double y = sourceGT.getSourceY( srcCoords[k + 1] );
                Point point = GeometryFactory.createPoint( x, y, crs );
                point = (Point) transform( point );
                targetCoords[k] = (float) targetGT.getDestX( point.getX() );
                targetCoords[k + 1] = (float) targetGT.getDestY( point.getY() );
                k += 2;
            }
        }

        // create warp object from reference points and desired interpolation
        WarpPolynomial warp = WarpPolynomial.createWarp( srcCoords, 0, targetCoords, 0, srcCoords.length, 1f, 1f, 1f,
                                                         1f, degree );

        if ( interpolation == null ) {
            interpolation = new InterpolationNearest();
        }

        // Create and perform the warp operation.
        ParameterBlock pb = new ParameterBlock();
        pb.addSource( img );
        pb.add( warp );
        pb.add( interpolation );

        img = JAI.create( "warp", pb ).getAsBufferedImage();

        // create a new GridCoverage from the warp result.
        // because warping only can be performed on images the
        // resulting GridCoverage will be an instance of ImageGridCoverage
        CoverageOffering oldCO = coverage.getCoverageOffering();
        CoverageOffering coverageOffering = null;
        if ( oldCO != null ) {
            try {
                DomainSet ds = oldCO.getDomainSet();
                ds.getSpatialDomain().setEnvelops( new Envelope[] { targetBBOX } );
                coverageOffering = new CoverageOffering( oldCO.getName(), oldCO.getLabel(), oldCO.getDescription(),
                                                         oldCO.getMetadataLink(), oldCO.getLonLatEnvelope(),
                                                         oldCO.getKeywords(), ds, oldCO.getRangeSet(),
                                                         oldCO.getSupportedCRSs(), oldCO.getSupportedFormats(),
                                                         oldCO.getSupportedInterpolations(), oldCO.getExtension() );
            } catch ( WCSException e ) {
                throw new CRSTransformationException( Messages.getMessage( "CRS_CO_CREATION_ERROR",
                                                                           crs.getIdentifier(),
                                                                           targetCRS.getIdentifier(), e.getMessage() ),
                                                      e );
            } catch ( OGCException e ) {
                throw new CRSTransformationException( Messages.getMessage( "CRS_CO_CREATION_ERROR",
                                                                           crs.getIdentifier(),
                                                                           targetCRS.getIdentifier(), e.getMessage() ),
                                                      e );
            }
        }
        return new ImageGridCoverage( coverageOffering, sourceBBOX, img );
    }

    /**
     * Transforms a <code>Envelope</code> to the target crs of the <code>GeoTransformer</code> instance
     *
     * @param envelope
     *            to definedTransformation
     * @param sourceCRS
     *            CRS of the envelope
     * @return the transformed envelope
     * @throws CRSTransformationException
     */
    public Envelope transform( Envelope envelope, org.deegree.model.crs.CoordinateSystem sourceCRS )
                            throws CRSTransformationException {

        Point min = GeometryFactory.createPoint( envelope.getMin().getX(), envelope.getMin().getY(), sourceCRS );
        Point max = GeometryFactory.createPoint( envelope.getMax().getX(), envelope.getMax().getY(), sourceCRS );
        min = (Point) transform( min );
        max = (Point) transform( max );
        // create bounding box with coordinates
        return GeometryFactory.createEnvelope( min.getX(), min.getY(), max.getX(), max.getY(), targetCRSWrapper );
    }

    /**
     * Transforms a <code>Envelope</code> to the target crs of the <code>GeoTransformer</code> instance
     *
     * This transformation takes rotation and distortion into account when regardDistortion is true. Otherwise the
     * transformed envelope may not contain the whole input envelope.
     *
     * @param envelope
     *            to definedTransformation
     * @param sourceCRS
     *            CRS of the envelope
     * @param regardDistortion
     *
     * @return the transformed envelope
     * @throws CRSTransformationException
     * @throws UnknownCRSException
     */
    public Envelope transform( Envelope envelope, String sourceCRS, boolean regardDistortion )
                            throws CRSTransformationException, UnknownCRSException {
        org.deegree.model.crs.CoordinateSystem crs = CRSFactory.create( sourceCRS );
        return transform( envelope, crs, regardDistortion );
    }

    /**
     * Transforms a <code>Envelope</code> to the target crs of the <code>GeoTransformer</code> instance
     *
     * This transformation takes rotation and distortion into account when regardDistortion is true. Otherwise the
     * transformed envelope may not contain the whole input envelope.
     *
     * @param envelope
     *            to definedTransformation
     * @param sourceCRS
     *            CRS of the envelope
     * @param regardDistortion
     *
     * @return the transformed envelope
     * @throws CRSTransformationException
     */
    public Envelope transform( Envelope envelope, org.deegree.model.crs.CoordinateSystem sourceCRS,
                               boolean regardDistortion )
                            throws CRSTransformationException {

        if ( !regardDistortion ) {
            return transform( envelope, sourceCRS );
        }
        double x1 = envelope.getMin().getX();
        double y1 = envelope.getMin().getY();
        double x2 = envelope.getMax().getX();
        double y2 = envelope.getMax().getY();

        double width = envelope.getWidth();
        double height = envelope.getHeight();

        // definedTransformation with 8 points instead of only the min and max point
        double[] coords = new double[] { x1, y1, x1, y2, x2, y2, x2, y1, x1, y1 + height, x1 + width, y1, x2,
                                        y1 + height, x1 + width, y2 };

        Geometry envelopeGeom = null;
        try {
            envelopeGeom = GeometryFactory.createCurve( coords, 2, sourceCRS );
        } catch ( GeometryException e ) {
            throw new CRSTransformationException( Messages.getMessage( "CRS_TRANSFORMATION_ERROR",
                                                                       sourceCRS.getIdentifier(),
                                                                       targetCRS.getIdentifier(), e.getMessage() ), e );
        }
        envelopeGeom = transform( envelopeGeom );

        return envelopeGeom.getEnvelope();

    }

    /**
     *
     * @param sourceCRS
     *            in which the given points are referenced.
     * @param points
     *            to definedTransformation.
     * @return a list of transformed point3d's or an empty list if something went wrong, never <code>null</code>
     * @throws CRSTransformationException
     *             if no transformation could be created for the given source and target crs.
     * @throws IllegalArgumentException
     *             if the sourceCRS is <code>null</code>
     */
    public List<Point3d> transform( org.deegree.model.crs.CoordinateSystem sourceCRS, List<Point3d> points )
                            throws CRSTransformationException, IllegalArgumentException {
        if ( points == null || points.size() == 0 ) {
            return new ArrayList<Point3d>();
        }
        if ( sourceCRS == null ) {
            throw new IllegalArgumentException( Messages.getMessage( "CRS_NO_SOURCE_CRS" ) );
        }
        CoordinateSystem sCRS = sourceCRS.getCRS();
        Transformation trans = checkOrCreateTransformation( sCRS );

        List<Point3d> result = new ArrayList<Point3d>( points.size() );
        TransformationException exception = null;
        try {
            result = trans.doTransform( points );
        } catch ( TransformationException te ) {
            List<Point3d> tResult = te.getTransformedPoints();
            if ( tResult != null && tResult.size() > 0 ) {
                result = tResult;
            }
            exception = te;
        }

        if ( LOG_TRANSORM.isDebug() || LOG.isDebug() ) {
            Map<Integer, String> errorMessages = null;
            if ( exception != null ) {
                errorMessages = exception.getTransformErrors();
            }
            for ( int i = 0; i < points.size(); ++i ) {
                StringBuilder sb = new StringBuilder( 1000 );
                Point3d coord = points.get( i );
                Point3d resultCoord = result.get( i );
                if ( resultCoord == null ) {
                    resultCoord = new Point3d( coord );
                }
                sb.append( trans.getSourceCRS().getIdentifier() );
                sb.append( ";" );
                sb.append( coord.x );
                sb.append( ";" );
                sb.append( coord.y );
                sb.append( ";" );
                if ( trans.getSourceDimension() == 3 ) {
                    sb.append( coord.z );
                    sb.append( ";" );
                }
                sb.append( trans.getTargetCRS().getIdentifier() );
                sb.append( ";" );
                sb.append( resultCoord.x );
                sb.append( ";" );
                sb.append( resultCoord.y );
                sb.append( ";" );
                if ( trans.getTargetDimension() == 3 ) {
                    sb.append( resultCoord.z );
                    sb.append( ";" );
                }
                String successString = "Success";
                if ( errorMessages != null ) {
                    String tmp = errorMessages.get( new Integer( i ) );
                    if ( tmp != null && !"".equals( tmp.trim() ) ) {
                        successString = tmp;
                    }
                }
                sb.append( successString );
                LOG_TRANSORM.logDebug( sb.toString() );
                // LOG.logDebug( sb.toString() );
            }
        }
        if ( result == null ) {
            result = new ArrayList<Point3d>();
        }
        return result;
    }

    /**
     * Transforms a <code>Envelope</code> to the target crs of the <code>GeoTransformer</code> instance
     *
     * @param envelope
     *            to definedTransformation
     * @param sourceCRS
     *            CRS of the envelope
     * @return the transformed envelope
     * @throws CRSTransformationException
     *             if the transformation did not succeed.
     * @throws UnknownCRSException
     *             if the given string is unknown to the CRSProvider
     */
    public Envelope transform( Envelope envelope, String sourceCRS )
                            throws CRSTransformationException, UnknownCRSException {

        org.deegree.model.crs.CoordinateSystem crs = CRSFactory.create( sourceCRS );
        return transform( envelope, crs );
    }

    /**
     * transforms all geometries contained within the passed {@link Feature} into the target CRS of a GeoTransformer
     * instance. If a geometry was transformed the {@link Feature#setEnvelopesUpdated()} method will be called.
     *
     * @param feature
     * @return the transformed geometries in the given Feature.
     * @throws CRSTransformationException
     */
    public Feature transform( Feature feature )
                            throws CRSTransformationException {
        if ( feature != null ) {
            FeatureProperty[] featureProperties = feature.getProperties();
            if ( featureProperties != null ) {
                for ( FeatureProperty fp : featureProperties ) {
                    if ( fp != null ) {
                        Object value = fp.getValue();
                        if ( value != null ) {
                            if ( value instanceof Geometry ) {
                                Geometry geom = (Geometry) value;
                                if ( !targetCRSWrapper.equals( geom.getCoordinateSystem() ) ) {
                                    fp.setValue( transform( geom ) );
                                    feature.setEnvelopesUpdated();
                                }
                            } else if ( value instanceof Feature ) {
                                transform( (Feature) value );
                            }
                        }
                    }
                }
            }
        }
        return feature;
    }

    /**
     * transforms all geometries contained within the passed {@link FeatureCollection} into the target CRS of a
     * GeoTransformer instance.
     *
     * @param fc
     *            the collection to definedTransformation
     * @return the transformed geometries in the FeatureCollection
     * @throws CRSTransformationException
     *             if the transformation cannot be created or processed.
     */
    public FeatureCollection transform( FeatureCollection fc )
                            throws CRSTransformationException {
        for ( int i = 0; i < fc.size(); i++ ) {
            transform( fc.getFeature( i ) );
        }
        // signal that the envelope properties might have been updated.
        fc.setEnvelopesUpdated();
        return fc;
    }

    /**
     * transforms the coordinates of a deegree geometry to the target coordinate reference system.
     *
     * @param geo
     *            to be transformed
     * @return the same geometry in a different crs.
     * @throws CRSTransformationException
     *             if the transformation between the source and target crs cannot be created.
     * @throws IllegalArgumentException
     *             if the coordinates system of the geometry is <code>null</code>
     */
    public Geometry transform( Geometry geo )
                            throws CRSTransformationException, IllegalArgumentException {

        if ( geo.getCoordinateSystem() == null ) {
            throw new IllegalArgumentException( Messages.getMessage( "CRS_GEOMETRY_HAS_NO_CRS" ) );
        }

        return transform( geo, geo.getCoordinateSystem().getCRS() );
    }

    /**
     * @param geo
     * @param sourceCRS
     * @return a transformed geometry
     * @throws CRSTransformationException
     */
    public Geometry transform( Geometry geo, CoordinateSystem sourceCRS )
                            throws CRSTransformationException {
        Transformation trans = checkOrCreateTransformation( sourceCRS );

        try {
            if ( geo instanceof Point ) {
                geo = transform( (Point) geo, trans );
            } else if ( geo instanceof Curve ) {
                geo = transform( (Curve) geo, trans );
            } else if ( geo instanceof Surface ) {
                geo = transform( (Surface) geo, trans );
            } else if ( geo instanceof MultiPoint ) {
                geo = transform( (MultiPoint) geo, trans );
            } else if ( geo instanceof MultiCurve ) {
                geo = transform( (MultiCurve) geo, trans );
            } else if ( geo instanceof MultiSurface ) {
                geo = transform( (MultiSurface) geo, trans );
            }
        } catch ( GeometryException ge ) {
            throw new CRSTransformationException( Messages.getMessage( "CRS_TRANSFORMATION_ERROR",
                                                                       sourceCRS.getIdentifier(),
                                                                       targetCRSWrapper.getIdentifier(),
                                                                       ge.getMessage() ), ge );
        }
        return geo;
    }

    /**
     * transforms the submitted curve to the target coordinate reference system
     *
     * @throws GeometryException
     */
    private Geometry transform( Curve geo, Transformation trans )
                            throws GeometryException {
        CurveSegment[] newcus = new CurveSegment[geo.getNumberOfCurveSegments()];
        for ( int i = 0; i < geo.getNumberOfCurveSegments(); i++ ) {
            CurveSegment cus = geo.getCurveSegmentAt( i );
            Position[] pos = cus.getPositions();
            pos = transform( pos, trans );
            newcus[i] = GeometryFactory.createCurveSegment( pos, targetCRSWrapper );
        }
        return GeometryFactory.createCurve( newcus );
    }

    /**
     * transforms the submitted multi curve to the target coordinate reference system
     *
     * @throws GeometryException
     */
    private Geometry transform( MultiCurve geo, Transformation trans )
                            throws GeometryException {
        Curve[] curves = new Curve[geo.getSize()];
        for ( int i = 0; i < geo.getSize(); i++ ) {
            curves[i] = (Curve) transform( geo.getCurveAt( i ), trans );
        }
        return GeometryFactory.createMultiCurve( curves );
    }

    /**
     * transforms the submitted multi point to the target coordinate reference system
     */
    private Geometry transform( MultiPoint geo, Transformation trans ) {
        Point[] points = new Point[geo.getSize()];
        for ( int i = 0; i < geo.getSize(); i++ ) {
            points[i] = (Point) transform( geo.getPointAt( i ), trans );
        }
        return GeometryFactory.createMultiPoint( points );
    }

    /**
     * transforms the submitted multi surface to the target coordinate reference system
     *
     * @throws GeometryException
     */
    private Geometry transform( MultiSurface geo, Transformation trans )
                            throws GeometryException {
        Surface[] surfaces = new Surface[geo.getSize()];
        for ( int i = 0; i < geo.getSize(); i++ ) {
            surfaces[i] = (Surface) transform( geo.getSurfaceAt( i ), trans );
        }
        return GeometryFactory.createMultiSurface( surfaces );
    }

    /**
     * transforms the submitted point to the target coordinate reference system
     */
    private Geometry transform( Point geo, Transformation trans ) {
        Point3d coord = geo.getPosition().getAsPoint3d();
        Point3d result = new Point3d( coord );
        TransformationException exception = null;
        try {
            result = trans.doTransform( coord );
        } catch ( TransformationException te ) {
            List<Point3d> tResult = te.getTransformedPoints();
            if ( tResult != null && tResult.size() > 0 ) {
                result = tResult.get( 0 );
            }
            exception = te;
        }

        if ( LOG_TRANSORM.isDebug() || LOG.isDebug() ) {
            StringBuilder sb = new StringBuilder( trans.getSourceCRS().getIdentifier() );
            sb.append( ";" );
            sb.append( coord.x );
            sb.append( ";" );
            sb.append( coord.y );
            sb.append( ";" );
            if ( trans.getSourceDimension() == 3 ) {
                sb.append( coord.z );
                sb.append( ";" );
            }
            sb.append( trans.getTargetCRS().getIdentifier() );
            sb.append( ";" );
            sb.append( result.x );
            sb.append( ";" );
            sb.append( result.y );
            sb.append( ";" );
            if ( trans.getTargetDimension() == 3 ) {
                sb.append( result.z );
                sb.append( ";" );
            }
            if ( exception != null ) {
                Map<Integer, String> messages = exception.getTransformErrors();
                if ( !messages.isEmpty() ) {
                    Set<Integer> keys = messages.keySet();
                    for ( Integer key : keys ) {
                        sb.append( messages.get( key ) );
                    }
                }
            } else {
                sb.append( "Success" );
            }
            LOG_TRANSORM.logDebug( sb.toString() );
            // LOG.logDebug( sb.toString() );
        }
        return GeometryFactory.createPoint( result.x, result.y, ( targetCRS.getDimension() == 3 ) ? result.z
                                                                                                 : Double.NaN,
                                            targetCRSWrapper );
    }

    /**
     * transforms an array of Positions to the target coordinate reference system
     */
    private Position[] transform( Position[] pos, Transformation trans ) {

        Position[] newpos = new Position[pos.length];
        // boolean srcRad = trans.getSourceCRS().getUnits().equals( Unit.DEGREE );
        // boolean targetRad = trans.getTargetCRS().getUnits().equals( Unit.DEGREE );

        List<Point3d> coords = new ArrayList<Point3d>( pos.length );
        for ( Position p : pos ) {
            // if ( srcRad ) {
            // coords.add( new Point3d( Math.toRadians( p.getX() ), Math.toRadians( p.getY() ),
            // p.getZ() ) );
            // } else {
            coords.add( new Point3d( p.getAsPoint3d() ) );
            // }
        }
        // List<Point3d> result = trans.doTransform( coords );
        List<Point3d> result = new ArrayList<Point3d>( coords.size() );
        TransformationException exception = null;
        try {
            result = trans.doTransform( coords );
        } catch ( TransformationException te ) {
            List<Point3d> tResult = te.getTransformedPoints();
            if ( tResult != null && tResult.size() > 0 ) {
                result = tResult;
            }
            exception = te;
        }

        if ( LOG_TRANSORM.isDebug() || LOG.isDebug() ) {
            Map<Integer, String> errorMessages = null;
            if ( exception != null ) {
                errorMessages = exception.getTransformErrors();
            }

            for ( int i = 0; i < coords.size(); ++i ) {
                StringBuilder sb = new StringBuilder( 1000 );
                Point3d coord = coords.get( i );
                Point3d resultCoord = result.get( i );
                if ( resultCoord == null ) {
                    resultCoord = new Point3d( coord );
                }
                sb.append( trans.getSourceCRS().getIdentifier() );
                sb.append( ";" );
                sb.append( coord.x );
                sb.append( ";" );
                sb.append( coord.y );
                sb.append( ";" );
                if ( trans.getSourceDimension() == 3 ) {
                    sb.append( coord.z );
                    sb.append( ";" );
                }
                sb.append( trans.getTargetCRS().getIdentifier() );
                sb.append( ";" );
                sb.append( resultCoord.x );
                sb.append( ";" );
                sb.append( resultCoord.y );
                sb.append( ";" );
                if ( trans.getTargetDimension() == 3 ) {
                    sb.append( resultCoord.z );
                    sb.append( ";" );
                }
                String successString = "Success";
                if ( errorMessages != null ) {
                    String tmp = errorMessages.get( new Integer( i ) );
                    if ( tmp != null && !"".equals( tmp.trim() ) ) {
                        successString = tmp;
                    }
                }
                sb.append( successString );
                LOG_TRANSORM.logDebug( sb.toString() );
                // LOG.logDebug( sb.toString() );
            }

        }
        for ( int i = 0; i < result.size(); ++i ) {
            Point3d p = result.get( i );
            if ( p != null && i < newpos.length ) {
                newpos[i] = GeometryFactory.createPosition( p );
            }
        }
        return newpos;
    }

    /**
     * transforms the submitted surface to the target coordinate reference system
     *
     * @throws GeometryException
     *             if the surface cannot be retrieved or if the surface patch could not be created.
     */
    private Geometry transform( Surface geo, Transformation trans )
                            throws GeometryException {
        int cnt = geo.getNumberOfSurfacePatches();
        SurfacePatch[] patches = new SurfacePatch[cnt];

        for ( int i = 0; i < cnt; i++ ) {
            SurfacePatch p = geo.getSurfacePatchAt( i );
            Position[] ex = p.getExteriorRing();
            ex = transform( ex, trans );

            Position[][] innerRings = p.getInteriorRings();
            Position[][] transformedInnerRings = null;

            if ( innerRings != null ) {
                transformedInnerRings = new Position[innerRings.length][];

                for ( int k = 0; k < innerRings.length; k++ ) {
                    transformedInnerRings[k] = transform( innerRings[k], trans );
                }
            }

            patches[i] = GeometryFactory.createSurfacePatch( ex, transformedInnerRings, p.getInterpolation(),
                                                             this.targetCRSWrapper );
        }

        // at the moment only polygons made of one patch are supported
        return GeometryFactory.createSurface( patches[0] );
    }

    /**
     * Simple method to check for the CRS transformation to use.
     *
     * @param sourceCRS
     * @throws CRSTransformationException
     */
    private synchronized Transformation checkOrCreateTransformation(
                                                                     org.deegree.crs.coordinatesystems.CoordinateSystem sourceCRS )
                            throws CRSTransformationException {
        if ( definedTransformation == null
             || !( definedTransformation.getSourceCRS().equals( sourceCRS ) && definedTransformation.getTargetCRS().equals(
                                                                                                                            targetCRS ) ) ) {
            try {
                definedTransformation = TransformationFactory.getInstance().createFromCoordinateSystems( sourceCRS,
                                                                                                         targetCRS );
            } catch ( TransformationException e ) {
                throw new CRSTransformationException( e );
            }
        }
        return definedTransformation;
    }
}
