//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.coverage.raster.interpolation;

import java.util.ArrayList;
import java.util.List;

import javax.media.jai.WarpPolynomial;
import javax.vecmath.Point3d;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterRect;

/**
 * Interpolates a given raster to a given width .
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class RasterInterpolater {

    private final InterpolationType interpolationType;

    private final int polynomialOrder = 3;

    private final int refPointsGridSize = 10;

    /**
     * @param interpolationType
     */
    public RasterInterpolater( InterpolationType interpolationType ) {
        this.interpolationType = interpolationType;
    }

    /**
     * Interpolates the given raster to retrieve a raster with the given width and height.
     * 
     * @param sourceRaster
     *            the raster to get an interpolation version from
     * @param dstWidth
     *            the width (columns) of the resulting raster
     * @param dstHeight
     *            the height (rows) of the resulting raster
     * @return the interpolated raster
     */
    public AbstractRaster interPolate( AbstractRaster sourceRaster, int dstWidth, int dstHeight ) {
        SimpleRaster simpleSourceRaster = sourceRaster.getAsSimpleRaster();
        RasterData srcData = simpleSourceRaster.getReadOnlyRasterData();
        RasterGeoReference srcREnv = simpleSourceRaster.getRasterReference();

        // interpolation is needed.
        Interpolation interpolation = InterpolationFactory.getInterpolation( interpolationType, srcData );

        RasterRect rr = new RasterRect( 0, 0, dstWidth, dstHeight );
        RasterData dstData = srcData.createCompatibleWritableRasterData( rr, null );

        RasterGeoReference dstREnv = RasterGeoReference.create( sourceRaster.getRasterReference().getOriginLocation(),
                                                                sourceRaster.getEnvelope(), dstWidth, dstHeight );

        // use warp to calculate the correct sample positions in the source raster.
        // the warp is a cubic polynomial function created of 100 points in the dstEnvelope. This function will map
        // points from the source crs to the target crs very accurate.
        WarpPolynomial warp = createWarp( dstWidth, dstHeight, srcREnv, dstREnv );
        warpTransform( warp, interpolation, dstData );

        return new SimpleRaster( dstData, sourceRaster.getEnvelope(), dstREnv );
    }

    /**
     * @param warp
     * @param interpolation
     * @param dstData
     */
    private void warpTransform( WarpPolynomial warp, Interpolation interpolation, RasterData dstData ) {
        byte[] pixel = new byte[dstData.getBands() * dstData.getDataType().getSize()];
        float[] srcCoords = new float[dstData.getColumns() * 2];
        for ( int y = 0; y < dstData.getRows(); y++ ) {
            // look-up the pixel positions in the source raster for every pixel in this row, the srcCoords will contain
            // the x,y ([2n],[2n+1]) values in the source raster (defined in the native CRS) for this row of pixels.
            warp.warpRect( 0, y, dstData.getColumns(), 1, srcCoords );
            for ( int x = 0; x < dstData.getColumns(); x++ ) {
                // get the interpolated pixel and set the value into the result raster
                interpolation.getPixel( srcCoords[x * 2], srcCoords[x * 2 + 1], pixel );
                dstData.setPixel( x, y, pixel );
            }
        }

    }

    private WarpPolynomial createWarp( int dstWidth, int dstHeight, RasterGeoReference srcREnv,
                                       RasterGeoReference dstREnv ) {
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
        // List<Point3d> resultList = transformDstToSrc( srcCRS, points );

        k = 0;
        for ( Point3d point : points ) {
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
}
