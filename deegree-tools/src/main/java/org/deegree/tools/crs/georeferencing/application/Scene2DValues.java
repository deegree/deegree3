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
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
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

    /**
     * new width and new height
     */
    private Point2d transformedRasterSpan;

    private Point2d convertedPixelToRasterPoint;

    private double sizeGeoRef;

    // private double sizeFootprint, oldSizeFootprint;

    private Point2d minPointRaster;

    private Point2d minPointPixel;

    private RasterIOOptions options;

    private CRS crs;

    private Point2d convertedRasterToPixelPoint;

    private double sizeGeoRefPolygon;

    private Rectangle dimensionFootprint;

    private Envelope envelopeFootprint;

    private GeometryFactory geom;

    public Scene2DValues( RasterIOOptions options, GeometryFactory geom ) {
        this.options = options;
        this.geom = geom;

    }

    /**
     * Converts the pixelPoint to a point with world coordinates.
     * 
     * @param pixelPoint
     * @return
     */
    public AbstractGRPoint getWorldPoint( AbstractGRPoint pixelPoint ) {
        switch ( pixelPoint.getPointType() ) {

        case GeoreferencedPoint:

            if ( subRaster != null ) {

                double[] worldPos;

                // determine the minX and the maxY position of the subRaster-envelope
                double getMinX = subRaster.getEnvelope().getMin().get0();
                double getMaxY = subRaster.getEnvelope().getMax().get1();

                // convert the requested point to rasterCoordinates
                double rasterPosX = pixelPoint.x * convertedPixelToRasterPoint.x;
                double rasterPosY = pixelPoint.y * convertedPixelToRasterPoint.y;

                // determine the minX and maxY values of the rasterEnvelope because they are absolute points
                double minWorldInRasterX = subRaster.getRasterReference().getOriginEasting();
                double maxWorldInRasterY = subRaster.getRasterReference().getOriginNorthing();

                // determine the delta between the absolute rasterPoint and the minX and maxY point of the
                // subRaster-envelope
                double deltaX = Math.abs( minWorldInRasterX - getMinX );
                double deltaY = Math.abs( maxWorldInRasterY - getMaxY );

                // get the worldCoordinates of the rasterPoints
                worldPos = subRaster.getRasterReference().getWorldCoordinate( rasterPosX, rasterPosY );
                // add/substract the delta of the worldRasterPoint
                worldPos[0] = worldPos[0] + deltaX;
                worldPos[1] = worldPos[1] - deltaY;

                return new GeoReferencedPoint( worldPos[0], worldPos[1] );

            }
        case FootprintPoint:

            // determine the minX and the maxY position of the envelope
            double getMinX = this.envelopeFootprint.getMin().get0();
            double getMaxY = this.envelopeFootprint.getMax().get1();

            double spanX = this.envelopeFootprint.getSpan0();
            double spanY = this.envelopeFootprint.getSpan1();

            double percentX = ( pixelPoint.getX() / dimensionFootprint.width ) * spanX;
            double percentY = ( pixelPoint.getY() / dimensionFootprint.height ) * spanY;

            return new FootprintPoint( getMinX + percentX, getMaxY - percentY );

            // return new FootprintPoint( worldPos[0], worldPos[1] );

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
    public int[] getPixelCoord( AbstractGRPoint abstractGRPoint ) {

        Point2d percentPoint;

        int pixelPointX;
        int pixelPointY;

        switch ( abstractGRPoint.getPointType() ) {

        case GeoreferencedPoint:

            percentPoint = computePercentWorld( subRaster.getEnvelope(), abstractGRPoint );

            pixelPointX = Math.round( (float) ( ( percentPoint.x * imageDimension.width ) ) );
            pixelPointY = Math.round( (float) ( ( ( 1 - percentPoint.y ) * imageDimension.height ) ) );
            return new int[] { pixelPointX, pixelPointY };
        case FootprintPoint:

            percentPoint = computePercentWorld( this.envelopeFootprint, abstractGRPoint );

            pixelPointX = Math.round( (float) ( ( percentPoint.x * dimensionFootprint.width ) ) );
            pixelPointY = Math.round( (float) ( ( ( 1 - percentPoint.y ) * dimensionFootprint.height ) ) );
            return new int[] { pixelPointX, pixelPointY };
        }

        return null;
    }

    private Point2d computePercentWorld( Envelope env, AbstractGRPoint abstractGRPoint ) {
        double spanX = env.getSpan0();
        double spanY = env.getSpan1();
        double mathX = -env.getMin().get0();
        double mathY = -env.getMin().get1();
        double deltaX = mathX + abstractGRPoint.getX();
        double deltaY = mathY + abstractGRPoint.getY();
        return new Point2d( deltaX / spanX, deltaY / spanY );

    }

    private Point2d computePercentPixel( Rectangle dimension, AbstractGRPoint abstractGRPoint ) {
        double spanX = dimension.width;
        double spanY = dimension.height;
        // double mathX = -env.getMin().get0();
        // double mathY = -env.getMin().get1();
        // double deltaX = 0 + ;
        // double deltaY = 0 + ;
        return new Point2d( abstractGRPoint.getX() / spanX, abstractGRPoint.getY() / spanY );
    }

    public Rectangle getImageDimension() {
        return imageDimension;
    }

    public void setImageDimension( Rectangle imageDimension ) {
        this.imageDimension = imageDimension;
    }

    public void setDimenstionFootpanel( Rectangle dimension ) {
        this.dimensionFootprint = dimension;
    }

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
        this.transformedRasterSpan = transformedBounds;
    }

    public Point2d getConvertedPixelToRasterPoint() {
        return convertedPixelToRasterPoint;
    }

    public void setConvertedPixelToRasterPoint( Point2d convertedPixelToRasterPoint ) {
        this.convertedPixelToRasterPoint = convertedPixelToRasterPoint;
    }

    public double getSizeGeoRef() {
        return sizeGeoRef;
    }

    // public double getSizeFootprint() {
    // return sizeFootprint;
    // }

    public RasterIOOptions getOptions() {
        return options;
    }

    public void setSizeGeoRef( boolean isZoomedIn, double resizing ) {

        double newSize = this.sizeGeoRef * ( 1 - resizing );
        if ( isZoomedIn == false ) {
            newSize = this.sizeGeoRef * ( 1 / ( 1 - resizing ) );
        }
        // BigDecimal b = new BigDecimal( newSize );
        // b = b.round( new MathContext( 16 ) );
        // this.size = b.floatValue();
        this.sizeGeoRef = newSize;

        System.out.println( "[Scene2DValues] newSizeGeoRef: " + this.sizeGeoRef );
    }

    // public void setSizeFootprint( double sizeFootprint ) {
    // this.sizeFootprint = sizeFootprint;
    // }

    public void moveEnvlopeFootprint( AbstractGRPoint mouseChange ) {
        System.out.println( "[Scene2DValues] Env before moving: " + this.envelopeFootprint );

        Point2d percent = computePercentPixel( dimensionFootprint, mouseChange );

        double changeX = envelopeFootprint.getSpan0() * percent.getX();
        double changeY = envelopeFootprint.getSpan1() * percent.getY();

        this.envelopeFootprint = geom.createEnvelope( envelopeFootprint.getMin().get0() + changeX,
                                                      envelopeFootprint.getMin().get1() - changeY,
                                                      envelopeFootprint.getMax().get0() + changeX,
                                                      envelopeFootprint.getMax().get1() - changeY,
                                                      envelopeFootprint.getCoordinateSystem() );
        System.out.println( "[Scene2DValues] Env after moving: " + this.envelopeFootprint );
    }

    public void computeEnvelopeFootprint( boolean isZoomedIn, double resizing, AbstractGRPoint mousePosition ) {

        double newSize = ( 1 - resizing );
        if ( isZoomedIn == false ) {

            newSize = ( 1 / ( 1 - resizing ) );

        }

        Envelope envTmp = geom.createEnvelope( envelopeFootprint.getMin().get0(), envelopeFootprint.getMin().get1(),
                                               envelopeFootprint.getMax().get0(), envelopeFootprint.getMax().get1(),
                                               envelopeFootprint.getCoordinateSystem() );

        FootprintPoint pCenter = (FootprintPoint) getWorldPoint( mousePosition );
        Point2d percentP = computePercentWorld( envTmp, pCenter );
        double spanX = envTmp.getSpan0() * newSize;
        double spanY = envTmp.getSpan1() * newSize;

        double percentSpanX = spanX * percentP.x;
        double percentSpanY = spanY * percentP.y;
        double percentSpanXPos = spanX - percentSpanX;
        double percentSpanYPos = spanY - percentSpanY;

        double minPointX = pCenter.getX() - percentSpanX;
        double minPointY = pCenter.getY() - percentSpanY;
        double maxPointX = pCenter.getX() + percentSpanXPos;
        double maxPointY = pCenter.getY() + percentSpanYPos;
        this.envelopeFootprint = geom.createEnvelope( minPointX, minPointY, maxPointX, maxPointY,
                                                      envTmp.getCoordinateSystem() );

        System.out.println( "[Scene2DValues] Env after resizing: " + this.envelopeFootprint );
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
        if ( sizeGeoRef == 0.0f ) {
            sizeGeoRef = 1.0f;
        }

        if ( ratio < 1 ) {
            // if < 1 then do orientation on h
            double newWidth = ( w / h ) * sizeGeoRef * rect.width;
            convertedPixelToRasterPoint = new Point2d( newWidth / w, rect.height * sizeGeoRef / h );
            convertedRasterToPixelPoint = new Point2d( w / newWidth, h / ( rect.height * sizeGeoRef ) );
            transformedRasterSpan = new Point2d( newWidth, rect.height * sizeGeoRef );
            return transformedRasterSpan;
        } else if ( ratio > 1 ) {
            // if > 1 then do orientation on w
            double newHeight = ( h / w ) * sizeGeoRef * rect.height;
            convertedPixelToRasterPoint = new Point2d( rect.width * sizeGeoRef / w, newHeight / h );
            convertedRasterToPixelPoint = new Point2d( w / ( rect.width * sizeGeoRef ), h / newHeight );
            transformedRasterSpan = new Point2d( rect.width * sizeGeoRef, newHeight );
            return transformedRasterSpan;
        }
        // if w = h then return 0
        transformedRasterSpan = new Point2d( rect.width * sizeGeoRef, rect.height * sizeGeoRef );

        return transformedRasterSpan;
    }

    public double getResolution() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Sets the minimum points to generate a georeferenced map
     * 
     * @param minPoint
     *            the pixel representation of the point, can be <Code>null</Code>.
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
        // System.out.println( "[Scene2DValues] minPixel: " + minPointPixel + " minRaster: " + minPointRaster );
    }

    /**
     * 
     * Sets the point as the centroid of the envelope.
     * 
     * @param xCoord
     *            x-coordiante in worldCoordinate-representation, not be <Code>null</Code>.
     * @param yCoord
     *            y-coordiante in worldCoordinate-representation, not be <Code>null</Code>.
     * @return AbstractGRPoint which is the translation needed to get the xCoord and yCoord as center of the scene.
     * 
     */
    public AbstractGRPoint setCentroidRasterEnvelopePosition( double xCoord, double yCoord ) {

        double halfSpanX = subRaster.getEnvelope().getSpan0() / 2;
        double halfSpanY = subRaster.getEnvelope().getSpan1() / 2;

        double x = xCoord - halfSpanX;
        double y = yCoord + halfSpanY;

        // get the worldPoint in pixelCoordinates
        int[] p = getPixelCoord( new GeoReferencedPoint( x, y ) );

        // set all the relevant parameters for generating the georefernced map
        setStartRasterEnvelopePosition( new Point2d( p[0], p[1] ) );

        return new GeoReferencedPoint( p[0], p[1] );
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

    public void setEnvelopeFootprint( Envelope createEnvelope ) {
        this.envelopeFootprint = createEnvelope;

    }

}
