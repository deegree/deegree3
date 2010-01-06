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
package org.deegree.coverage.raster;

import java.util.ArrayList;
import java.util.List;

import javax.media.jai.WarpPolynomial;
import javax.vecmath.Point3d;

import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.interpolation.Interpolation;
import org.deegree.coverage.raster.interpolation.InterpolationFactory;
import org.deegree.coverage.raster.interpolation.InterpolationType;
import org.deegree.crs.CRS;
import org.deegree.crs.Transformer;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.crs.transformations.Transformation;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class transforms raster to a taget coordinate system .
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class RasterTransformer extends Transformer {

    private static Logger LOG = LoggerFactory.getLogger( RasterTransformer.class );

    private final int polynomialOrder = 3;

    private final int refPointsGridSize = 10;

    private byte[] backgroundValue;

    // private CoordinateSystem srcCRS;

    /**
     * Creates a new RasterTransformer with the given target CRS.
     * 
     * @param targetCRS
     *            to transform incoming coordinates to.
     * @throws IllegalArgumentException
     *             if the given CoordinateSystem is <code>null</code>
     */
    public RasterTransformer( CoordinateSystem targetCRS ) throws IllegalArgumentException {
        super( targetCRS );
    }

    /**
     * Creates a new RasterTransformer with the given id as the target CRS.
     * 
     * @param targetCRS
     *            an identifier to which all incoming coordinates shall be transformed.
     * @throws UnknownCRSException
     *             if the given crs name could not be mapped to a valid (configured) crs.
     * @throws IllegalArgumentException
     *             if the given parameter is null.
     */
    public RasterTransformer( String targetCRS ) throws IllegalArgumentException, UnknownCRSException {
        super( targetCRS );
    }

    /**
     * Creates a transformed raster from a given source raster.
     * 
     * <p>
     * This method transforms the requested envelope and returns a new raster with the requested size. The source raster
     * can be larger than the requested envelope (like a large tiled raster), or smaller (the source raster nodata value
     * will be used outside the source raster).
     * </p>
     * <p>
     * If the dstEnvelope does not contain a CRS or the CRS is the same as the
     * {@link AbstractRaster#getCoordinateSystem()} only interpolation will be applied. If the requested size is the
     * same as the number of rows/columns of the source raster, only the subset of the given raster will be returned,
     * without interpolation being applied.
     * </p>
     * 
     * @param sourceRaster
     *            the source raster
     * @param dstEnvelope
     *            the requested envelope (already in the target crs)
     * @param dstWidth
     *            the requested raster size
     * @param dstHeight
     *            the requested raster size
     * @param interpolationType
     *            the type of the interpolation
     * @return the transformed raster
     * @throws TransformationException
     * @throws UnknownCRSException
     */
    public AbstractRaster transform( AbstractRaster sourceRaster, Envelope dstEnvelope, int dstWidth, int dstHeight,
                                     InterpolationType interpolationType )
                            throws TransformationException, UnknownCRSException {

        CoordinateSystem srcCRS = sourceRaster.getCoordinateSystem().getWrappedCRS();

        // get the (transformed) subraster which intersects with the given envelope.
        AbstractRaster source = getSubRaster( srcCRS, sourceRaster, dstEnvelope );
        if ( source.getColumns() == dstHeight && source.getRows() == dstWidth ) {
            // no need to interpolate.
            return source;
        }
        SimpleRaster simpleSourceRaster = source.getAsSimpleRaster();
        RasterData srcData = simpleSourceRaster.getReadOnlyRasterData();
        RasterGeoReference srcREnv = simpleSourceRaster.getRasterReference();

        if ( backgroundValue != null ) {
            srcData.setNoDataValue( backgroundValue );
        }

        // interpolation is needed.
        Interpolation interpolation = InterpolationFactory.getInterpolation( interpolationType, srcData );

        RasterRect rr = new RasterRect( 0, 0, dstWidth, dstHeight );
        RasterData dstData = srcData.createCompatibleWritableRasterData( rr, null );

        RasterGeoReference dstREnv = RasterGeoReference.create( sourceRaster.getRasterReference().getOriginLocation(),
                                                                dstEnvelope, dstWidth, dstHeight );

        // use warp to calculate the correct sample positions in the source raster.
        // the warp is a cubic polynomial function created of 100 points in the dstEnvelope. This function will map
        // points from the source crs to the target crs very accurate.
        WarpPolynomial warp = createWarp( dstWidth, dstHeight, srcCRS, srcREnv, dstREnv );
        warpTransform( warp, interpolation, dstData );

        return new SimpleRaster( dstData, dstEnvelope, dstREnv );
    }

    /**
     * @param warp
     * @param interpolation
     * @param dstData
     */
    private void warpTransform( WarpPolynomial warp, Interpolation interpolation, RasterData dstData ) {
        byte[] pixel = new byte[dstData.getBands() * dstData.getDataType().getSize()];
        float[] srcCoords = new float[dstData.getWidth() * 2];
        for ( int y = 0; y < dstData.getHeight(); y++ ) {
            // look-up the pixel positions in the source raster for every pixel in this row, the srcCoords will contain
            // the x,y ([2n],[2n+1]) values in the source raster (defined in the native CRS) for this row of pixels.
            warp.warpRect( 0, y, dstData.getWidth(), 1, srcCoords );
            for ( int x = 0; x < dstData.getWidth(); x++ ) {
                // get the interpolated pixel and set the value into the result raster
                interpolation.getPixel( srcCoords[x * 2], srcCoords[x * 2 + 1], pixel );
                dstData.setPixel( x, y, pixel );
            }
        }

    }

    /**
     * Create a new raster that contains all data we need for the transformation.
     * 
     * @throws IllegalArgumentException
     */
    private AbstractRaster getSubRaster( CoordinateSystem srcCRS, AbstractRaster sourceRaster, Envelope dstEnvelope )
                            throws TransformationException, IllegalArgumentException {
        Envelope dataEnv = dstEnvelope;
        if ( srcCRS != null && !srcCRS.equals( getTargetCRS() ) ) {

            GeometryTransformer srcTransf = new GeometryTransformer( srcCRS );

            // the envelope from which we need data
            Envelope workEnv = (Envelope) srcTransf.transform( dstEnvelope, getTargetCRS() );

            Envelope dataEnvelope = sourceRaster.getEnvelope();
            // the envelope from which we have data
            Geometry dataEnvGeom = workEnv.getIntersection( dataEnvelope );
            if ( dataEnvGeom == null ) {
                LOG.debug( "no intersection for " + sourceRaster + " and " + dstEnvelope );
                // todo create subclass of TransformationException
                throw new TransformationException( "no source data found" );

            }
            dataEnv = dataEnvGeom.getEnvelope();
        }

        AbstractRaster source;
        try {
            source = sourceRaster.getSubRaster( dataEnv );
        } catch ( IndexOutOfBoundsException ex ) {
            throw new TransformationException( "no source data found" );
        }
        // if ( LOG.isDebugEnabled() ) {
        // debugRasterFile( source );
        // }
        return source;
    }

    private WarpPolynomial createWarp( int dstWidth, int dstHeight, CoordinateSystem srcCRS,
                                       RasterGeoReference srcREnv, RasterGeoReference dstREnv )
                            throws TransformationException {
        int k = 0;
        // create/calculate reference points
        float dx = ( dstWidth - 1 ) / (float) ( refPointsGridSize - 1 );
        float dy = ( dstHeight - 1 ) / (float) ( refPointsGridSize - 1 );
        float[] srcCoords = new float[refPointsGridSize * refPointsGridSize * 2];
        float[] dstCoords = new float[refPointsGridSize * refPointsGridSize * 2];
        List<Point3d> points = new ArrayList<Point3d>( refPointsGridSize * refPointsGridSize );
        for ( int j = 0; j < refPointsGridSize; j++ ) {
            for ( int i = 0; i < refPointsGridSize; i++ ) {
                dstCoords[k] = i * dx;
                dstCoords[k + 1] = j * dy;
                double[] dstWCoords = dstREnv.getWorldCoordinate( (int) dstCoords[k], (int) dstCoords[k + 1] );
                points.add( new Point3d( dstWCoords[0], dstWCoords[1], Double.NaN ) );
                k += 2;
            }
        }
        List<Point3d> resultList = transformDstToSrc( srcCRS, points );

        k = 0;
        for ( Point3d point : resultList ) {
            double[] srcRCoords = srcREnv.getRasterCoordinateUnrounded( point.x, point.y );
            srcCoords[k] = (float) srcRCoords[0];
            srcCoords[k + 1] = (float) srcRCoords[1];
            k += 2;
        }

        // create a best fit polynomial for out grid
        WarpPolynomial warp = WarpPolynomial.createWarp( srcCoords, 0, dstCoords, 0, srcCoords.length, 1f, 1f, 1f, 1f,
                                                         polynomialOrder );
        return warp;
    }

    private List<Point3d> transformDstToSrc( CoordinateSystem srcCRS, List<Point3d> points )
                            throws TransformationException {
        // transform all grid points
        Transformation transform = createCRSTransformation( srcCRS );
        transform.inverse();
        return transform.doTransform( points );
    }

    /**
     * Transform a raster to the target coordinate system.
     * 
     * <p>
     * This method transforms the whole raster into the target CRS of this RasterTransformer. The size of the output
     * raster will be calculated, so that the pixels keep the aspect ratio (i.e. keep square pixels).
     * </p>
     * <p>
     * If the coordinate system of the source raster is <code>null</code> or equals the target crs, the source raster
     * will be returned unaltered.
     * </p>
     * 
     * 
     * @param sourceRaster
     *            the raster to be transformed
     * @param interpolationType
     * @return the transformed raster
     * @throws IllegalArgumentException
     * @throws TransformationException
     * @throws UnknownCRSException
     */
    public AbstractRaster transform( AbstractRaster sourceRaster, InterpolationType interpolationType )
                            throws IllegalArgumentException, TransformationException, UnknownCRSException {

        CRS srcCRS = sourceRaster.getCoordinateSystem();
        if ( srcCRS == null || srcCRS.equals( getTargetCRS() ) ) {
            return sourceRaster;
        }

        GeometryTransformer gt = new GeometryTransformer( getTargetCRS() );
        Envelope dstEnvelope = gt.transform( sourceRaster.getEnvelope(), srcCRS.getWrappedCRS() ).getEnvelope();

        int srcWidth = sourceRaster.getColumns();
        int srcHeight = sourceRaster.getRows();

        // calculate the new size, consider the aspect ratio to get square pixels
        double deltaX = dstEnvelope.getSpan0();
        double deltaY = dstEnvelope.getSpan1();
        double diagSize = Math.sqrt( deltaX * deltaX + deltaY * deltaY );
        // pixelSize for calculation of the new image size
        double pixelSize = diagSize / Math.sqrt( Math.pow( srcWidth, 2 ) + Math.pow( srcHeight, 2 ) );
        int dstHeight = (int) ( deltaY / pixelSize + 0.5 );
        int dstWidth = (int) ( deltaX / pixelSize + 0.5 );

        return transform( sourceRaster, dstEnvelope, dstWidth, dstHeight, interpolationType );

    }

    /**
     * Sets the background for the raster transformation.
     * 
     * @param backgroundValue
     */
    public void setBackgroundValue( byte[] backgroundValue ) {
        this.backgroundValue = backgroundValue;
    }

    // private void debugRasterFile( AbstractRaster source ) {
    // File tmpFile = null;
    // try {
    // tmpFile = File.createTempFile( "transform-src", ".tiff" );
    // LOG.debug( "writing the source raster of the transformation to " + tmpFile );
    // RasterFactory.saveRasterToFile( source, tmpFile );
    // } catch ( IOException e ) {
    // LOG.error( "couldn't write debug file " + tmpFile );
    // e.printStackTrace();
    // }
    // }

}
