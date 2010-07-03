//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.tools.crs.georeferencing.application;

import java.awt.Rectangle;

import javax.vecmath.Point2d;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.cs.CRS;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint;
import org.deegree.tools.crs.georeferencing.model.points.FootprintPoint;
import org.deegree.tools.crs.georeferencing.model.points.GeoReferencedPoint;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Scene2DValues {

    private AbstractRaster raster;

    private SimpleRaster subRaster;

    private RasterRect rasterRect;

    // private Point2d imageMargin;

    //
    private Rectangle imageDimension;

    //
    // private Point2d imageStartPosition;

    private Point2d transformedBounds;

    private Point2d convertedPixelToRasterPoint;

    private float size;

    private Point2d minPointRaster;

    private Point2d minPointPixel;

    private RasterIOOptions options;

    private CRS crs;

    private Point2d convertedRasterToPixelPoint;

    public Scene2DValues( RasterIOOptions options ) {
        this.options = options;
    }

    /**
     * Converts the pixelPoint to a point with world coordinates.
     * 
     * @param pixelPoint
     * @return
     */
    public AbstractGRPoint getWorldPoint( AbstractGRPoint pixelPoint ) {
        if ( subRaster != null ) {

            double[] worldPos;
            if ( minPointPixel == null ) {
                minPointPixel = new Point2d( 0.0, 0.0 );
            }
            // double pixelPosX = imageStartPosition.x - minPointPixel.x - pixelPoint.x;
            // double pixelPosY = imageStartPosition.y - minPointPixel.y - pixelPoint.y;
            double pixelPosX = minPointPixel.x - pixelPoint.x;
            double pixelPosY = minPointPixel.y - pixelPoint.y;
            double rasterPosX = -( pixelPosX * convertedPixelToRasterPoint.x );
            double rasterPosY = -( pixelPosY * convertedPixelToRasterPoint.y );
            // System.out.println( "pos: " + minPointPixel );
            // System.out.println( "rasta: " + rasterPosX + " " + rasterPosY );

            worldPos = subRaster.getRasterReference().getWorldCoordinate( rasterPosX, rasterPosY );

            switch ( pixelPoint.getPointType() ) {

            case GeoreferencedPoint:
                return new GeoReferencedPoint( worldPos[0], worldPos[1] );
            case FootprintPoint:
                return new FootprintPoint( worldPos[0], worldPos[1] );

            }

        }

        return null;
    }

    /**
     * In this method firstly there is a computation from the span of the raster-envelope, after that the point is
     * calculated relative to the min-point of the raster-envelope, after that the percent is computed and finally
     * multiplicated with the imagedimension.
     * 
     * @param abstractGRPoint
     *            the worldCoordinatePoint which should be translated back to pixelCoordinates, not be <Code>null</Code>
     * @return an integer array with x, y - coordinates, z is not implemented yet.
     */
    public int[] getPixelCoordinatePolygon( AbstractGRPoint abstractGRPoint ) {

        double spanX = subRaster.getEnvelope().getSpan0();
        double spanY = subRaster.getEnvelope().getSpan1();
        double mathX = -subRaster.getEnvelope().getMin().get0();
        double mathY = -subRaster.getEnvelope().getMin().get1();
        double pointWorldX = mathX + abstractGRPoint.getX();
        double pointWorldY = mathY + abstractGRPoint.getY();
        double percentX = pointWorldX / spanX;
        double percentY = pointWorldY / spanY;
        // int pixelPointX = Math.round( (float) ( ( percentX * imageDimension.width ) + imageStartPosition.getX() + (
        // imageMargin.getX() * 2 ) ) );
        // int pixelPointY = Math.round( (float) ( ( ( 1 - percentY ) * imageDimension.height )
        // + imageStartPosition.getY() - imageMargin.getY() ) );
        int pixelPointX = Math.round( (float) ( ( percentX * imageDimension.width ) ) );
        int pixelPointY = Math.round( (float) ( ( ( 1 - percentY ) * imageDimension.height ) ) );

        return new int[] { pixelPointX, pixelPointY };

    }

    public int[] getPixelCoord( AbstractGRPoint abstractGRPoint ) {

        double spanX = subRaster.getEnvelope().getSpan0();
        double spanY = subRaster.getEnvelope().getSpan1();
        double mathX = -subRaster.getEnvelope().getMin().get0();
        double mathY = -subRaster.getEnvelope().getMin().get1();
        double deltaX = mathX + abstractGRPoint.getX();
        double deltaY = mathY + abstractGRPoint.getY();
        double percentX = deltaX / spanX;
        double percentY = deltaY / spanY;
        // int pixelPointX = Math.round( (float) ( ( percentX * imageDimension.width ) + imageStartPosition.getX() ) );
        // int pixelPointY = Math.round( (float) ( ( ( 1 - percentY ) * imageDimension.height ) +
        // imageStartPosition.getY() ) );

        int pixelPointX = Math.round( (float) ( ( percentX * imageDimension.width ) ) );
        int pixelPointY = Math.round( (float) ( ( ( 1 - percentY ) * imageDimension.height ) ) );

        // System.out.println( "[Scene2DValues] percent: " + percentX + " " + percentY + " = " + deltaY + " " + spanY );
        return new int[] { pixelPointX, pixelPointY };
    }

    // public void setImageMargin( Point2d imageMargin ) {
    // this.imageMargin = imageMargin;
    //
    // }

    public Rectangle getImageDimension() {
        return imageDimension;
    }

    public void setImageDimension( Rectangle imageDimension ) {
        this.imageDimension = imageDimension;
    }

    // public Point2d getImageStartPosition() {
    // return imageStartPosition;
    // }
    //
    // public void setImageStartPosition( Point2d imageStartPosition ) {
    // this.imageStartPosition = imageStartPosition;
    // }
    //
    // public Point2d getImageMargin() {
    // return imageMargin;
    // }

    public AbstractRaster getRaster() {
        return raster;
    }

    public void setRaster( AbstractRaster raster ) {
        this.raster = raster;
    }

    public SimpleRaster getSubRaster() {
        return subRaster;
    }

    public void setSubRaster( SimpleRaster subRaster ) {
        this.subRaster = subRaster;
    }

    public Point2d getTransformedBounds() {
        if ( rasterRect != null ) {
            return transformProportion( rasterRect );
        }
        return null;
    }

    public void setTransformedBounds( Point2d transformedBounds ) {
        this.transformedBounds = transformedBounds;
    }

    public Point2d getConvertedPixelToRasterPoint() {
        return convertedPixelToRasterPoint;
    }

    public void setConvertedPixelToRasterPoint( Point2d convertedPixelToRasterPoint ) {
        this.convertedPixelToRasterPoint = convertedPixelToRasterPoint;
    }

    public float getSize() {
        return size;
    }

    public RasterIOOptions getOptions() {
        return options;
    }

    public void setSize( float resolution ) {
        this.size = Float.parseFloat( ( options.get( "RESOLUTION" ) ) ) * resolution;
    }

    /**
     * Determines the ratio the boundingbox has to orient on. If there is a mismatch between the width and height this
     * should influence the display of the image returned by the WMS to prevent any deformation. <li>pos - orientation
     * on width because width is larger</li> <li>neg - orientation on hight because hight is larger</li> <li>other -
     * orientation on width/hight because they are even</li>
     * <p>
     * Additionally it generates a converted point that represents a rasterPoint converted from a pixelPoint
     * 
     * @param panelBounds
     *            the rectangle bounds, not <Code>null</Code>
     * @return an positive, negative or even integer
     */
    private Point2d transformProportion( RasterRect rect ) {
        double w = imageDimension.width;
        double h = imageDimension.height;

        double ratio = w / h;
        if ( size == 0.0f ) {
            size = 1.0f;
        }

        if ( ratio < 1 ) {
            // if < 1 then do orientation on h
            double newWidth = ( w / h ) * size * rect.width;
            convertedPixelToRasterPoint = new Point2d( newWidth / w, rect.height * size / h );
            convertedRasterToPixelPoint = new Point2d( w / newWidth, h / ( rect.height * size ) );
            transformedBounds = new Point2d( newWidth, rect.height * size );
            return transformedBounds;
        } else if ( ratio > 1 ) {
            // if > 1 then do orientation on w
            double newHeight = ( h / w ) * size * rect.height;
            convertedPixelToRasterPoint = new Point2d( rect.width * size / w, newHeight / h );
            convertedRasterToPixelPoint = new Point2d( w / ( rect.width * size ), h / newHeight );
            transformedBounds = new Point2d( rect.width * size, newHeight );
            return transformedBounds;
        }
        // if w = h then return 0
        transformedBounds = new Point2d( rect.width * size, rect.height * size );

        return transformedBounds;
    }

    public double getResolution() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * 
     * @param minPoint
     *            the pixel representation of the point
     */
    public void setStartRasterEnvelopePosition( Point2d minPoint ) {
        if ( minPointPixel == null ) {
            minPointPixel = new Point2d( 0.0, 0.0 );
        }

        double minRX = this.minPointRaster.x + ( minPoint.x * convertedPixelToRasterPoint.x );
        double minRY = this.minPointRaster.y + ( minPoint.y * convertedPixelToRasterPoint.y );

        double minPX = this.minPointPixel.x + minPoint.x;
        double minPY = this.minPointPixel.y + minPoint.y;

        this.minPointRaster = new Point2d( minRX, minRY );
        this.minPointPixel = new Point2d( minPX, minPY );
        // System.out.println( "minPixel: " + minPointPixel + " minRaster: " + minPointRaster );
    }

    public Point2d getMinPointRaster() {

        return minPointRaster;
    }

    public void setMinPointRaster( Point2d min ) {
        this.minPointRaster = min;
    }

    public void setRasterRect( RasterRect rasterRect ) {
        this.rasterRect = rasterRect;

    }

    public Point2d getMinPointPixel() {
        return minPointPixel;
    }

    public void setMinPointPixel( Point2d minPointPixel ) {
        this.minPointPixel = minPointPixel;
    }

    public CRS getCrs() {
        return crs;
    }

    public void setCrs( CRS crs ) {
        this.crs = crs;
    }

}
