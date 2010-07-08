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
 * Helper for the {@link Controller} to handle operations in painting the scenes.
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

    private Rectangle dimensionGeoreference;

    /**
     * new width and new height
     */
    private Point2d transformedRasterSpan;

    private Point2d convertedPixelToRasterPoint;

    private double sizeGeoRef;

    private Point2d minPointRaster;

    private Point2d minPointPixel;

    private RasterIOOptions options;

    private CRS crs;

    private Rectangle dimensionFootprint;

    private Envelope envelopeFootprint;

    private GeometryFactory geom;

    /**
     * Creates a new instance of <Code>Scene2DValues</Code>
     * 
     * @param options
     * @param geom
     */
    public Scene2DValues( RasterIOOptions options, GeometryFactory geom ) {
        this.options = options;
        this.geom = geom;

    }

    /**
     * Converts the pixelPoint to a point with world coordinates.
     * 
     * @param pixelPoint
     *            , not <Code>null</Code>
     * @return an AbstractGRPoint in worldCoordinates
     */
    public AbstractGRPoint getWorldPoint( AbstractGRPoint pixelPoint ) {
        switch ( pixelPoint.getPointType() ) {

        case GeoreferencedPoint:

            if ( subRaster != null ) {

                double[] worldPos;

                // determine the minX and the maxY position of the subRaster-envelope
                double getMinX = subRaster.getEnvelope().getMin().get0();
                double getMaxY = subRaster.getEnvelope().getMax().get1();

                // determine the span of the envelope
                double spanX = this.subRaster.getEnvelope().getSpan0();
                double spanY = this.subRaster.getEnvelope().getSpan1();

                // determine the percentage of the requested point
                double percentX = ( pixelPoint.getX() / dimensionGeoreference.width ) * spanX;
                double percentY = ( pixelPoint.getY() / dimensionGeoreference.height ) * spanY;

                return new GeoReferencedPoint( getMinX + percentX, getMaxY - percentY );

            }
        case FootprintPoint:

            // determine the minX and the maxY position of the envelope
            double getMinX = this.envelopeFootprint.getMin().get0();
            double getMaxY = this.envelopeFootprint.getMax().get1();

            // determine the span of the envelope
            double spanX = this.envelopeFootprint.getSpan0();
            double spanY = this.envelopeFootprint.getSpan1();

            // determine the percentage of the requested point
            double percentX = ( pixelPoint.getX() / dimensionFootprint.width ) * spanX;
            double percentY = ( pixelPoint.getY() / dimensionFootprint.height ) * spanY;

            return new FootprintPoint( getMinX + percentX, getMaxY - percentY );

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

            pixelPointX = Math.round( (float) ( ( percentPoint.x * dimensionGeoreference.width ) ) );
            pixelPointY = Math.round( (float) ( ( ( 1 - percentPoint.y ) * dimensionGeoreference.height ) ) );
            return new int[] { pixelPointX, pixelPointY };
        case FootprintPoint:

            percentPoint = computePercentWorld( this.envelopeFootprint, abstractGRPoint );

            pixelPointX = Math.round( (float) ( ( percentPoint.x * dimensionFootprint.width ) ) );
            pixelPointY = Math.round( (float) ( ( ( 1 - percentPoint.y ) * dimensionFootprint.height ) ) );
            return new int[] { pixelPointX, pixelPointY };
        }

        return null;
    }

    /**
     * Computes the percent of the AbstractGRPoint relative to the Envelope in worldCoordinates.
     * 
     * @param env
     *            of the scene
     * @param abstractGRPoint
     *            to compute the percentage of
     * @return a Point2d which holds the percentage of the abstractGRPoint
     */
    private Point2d computePercentWorld( Envelope env, AbstractGRPoint abstractGRPoint ) {
        double spanX = env.getSpan0();
        double spanY = env.getSpan1();
        double mathX = -env.getMin().get0();
        double mathY = -env.getMin().get1();
        double deltaX = mathX + abstractGRPoint.getX();
        double deltaY = mathY + abstractGRPoint.getY();
        return new Point2d( deltaX / spanX, deltaY / spanY );

    }

    /**
     * Computes the percent of the AbstractGRPoint relative to the sceneDimension in PixelCoordinates.
     * 
     * @param dimension
     *            of the scene
     * @param abstractGRPoint
     *            to compute the percentage of
     * @return a Point2d which holds the percentage of the abstractGRPoint
     */
    private Point2d computePercentPixel( Rectangle dimension, AbstractGRPoint abstractGRPoint ) {
        double spanX = dimension.width;
        double spanY = dimension.height;
        return new Point2d( abstractGRPoint.getX() / spanX, abstractGRPoint.getY() / spanY );
    }

    public Rectangle getImageDimension() {
        return dimensionGeoreference;
    }

    public void setImageDimension( Rectangle imageDimension ) {
        this.dimensionGeoreference = imageDimension;
    }

    public void setDimenstionFootpanel( Rectangle dimension ) {
        this.dimensionFootprint = dimension;
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

    public RasterIOOptions getOptions() {
        return options;
    }

    /**
     * Computes the translation of the envelope for the georeferencing scene or the footprint scene.
     * 
     * @param mouseChange
     *            the translation of the scene in pixelCoordinates, not <Code>null</Code>
     */
    public void moveEnvelope( AbstractGRPoint mouseChange ) {
        Point2d percent;
        switch ( mouseChange.getPointType() ) {
        case GeoreferencedPoint:
            percent = computePercentPixel( dimensionGeoreference, mouseChange );

            this.subRaster = raster.getAsSimpleRaster().getSubRaster(
                                                                      createTranslatedEnv( subRaster.getEnvelope(),
                                                                                           percent ) );
            break;
        case FootprintPoint:

            percent = computePercentPixel( dimensionFootprint, mouseChange );
            this.envelopeFootprint = createTranslatedEnv( envelopeFootprint, percent );
            break;
        }

    }

    /**
     * Creates the envelope for translation.
     * 
     * @param env
     *            to be translated, not <Code>null</Code>
     * @param percent
     *            the percent of the point regarding to the envelope, not <Code>null</Code>
     * @return the envelope for translation.
     */
    private Envelope createTranslatedEnv( Envelope env, Point2d percent ) {
        double changeX = env.getSpan0() * percent.getX();
        double changeY = env.getSpan1() * percent.getY();

        return geom.createEnvelope( env.getMin().get0() + changeX, env.getMin().get1() - changeY, env.getMax().get0()
                                                                                                  + changeX,
                                    env.getMax().get1() - changeY, env.getCoordinateSystem() );
    }

    /**
     * Computes the zoom of the envelope for the georeferencing scene or the footprint scene.
     * 
     * @param isZoomedIn
     *            <i>true</i>, if one zoom in, otherwise <i>false</i>
     * @param resizing
     *            the factor the scene should be resized &rarr; <i>0&lt;resizing&lt;1</i>, could be <Code>null</Code>
     * @param mousePosition
     *            where the zoom should orient on, not <Code>null</Code>
     */
    public void computeZoomedEnvelope( boolean isZoomedIn, double resizing, AbstractGRPoint mousePosition ) {

        double newSize = ( 1 - resizing );
        if ( isZoomedIn == false ) {

            newSize = ( 1 / ( 1 - resizing ) );

        }
        AbstractGRPoint center;
        switch ( mousePosition.getPointType() ) {
        case GeoreferencedPoint:
            center = (GeoReferencedPoint) getWorldPoint( mousePosition );
            Envelope enve = createZoomedEnv( this.subRaster.getEnvelope(), newSize, center );
            this.subRaster = raster.getAsSimpleRaster().getSubRaster( enve );
            transformProportion( this.subRaster.getRasterReference().convertEnvelopeToRasterCRS( enve ) );
            break;
        case FootprintPoint:
            center = (FootprintPoint) getWorldPoint( mousePosition );
            this.envelopeFootprint = createZoomedEnv( envelopeFootprint, newSize, center );
            break;
        }
    }

    /**
     * Creates the envelope for zoom.
     * 
     * @param env
     *            to be zoomed, not <Code>null</Code>
     * @param newSize
     *            the absolute value to be resized, not <Code>null<Code>.
     * @param center
     *            in worldCoordinates where the zoom should orient on, not <Code>null</Code>
     * @return the zoomed envelope.
     */
    private Envelope createZoomedEnv( Envelope env, double newSize, AbstractGRPoint center ) {
        Point2d percentP = computePercentWorld( env, center );
        double spanX = env.getSpan0() * newSize;
        double spanY = env.getSpan1() * newSize;

        double percentSpanX = spanX * percentP.x;
        double percentSpanY = spanY * percentP.y;
        double percentSpanXPos = spanX - percentSpanX;
        double percentSpanYPos = spanY - percentSpanY;

        double minPointX = center.getX() - percentSpanX;
        double minPointY = center.getY() - percentSpanY;
        double maxPointX = center.getX() + percentSpanXPos;
        double maxPointY = center.getY() + percentSpanYPos;

        return geom.createEnvelope( minPointX, minPointY, maxPointX, maxPointY, env.getCoordinateSystem() );
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
        double w = dimensionGeoreference.width;
        double h = dimensionGeoreference.height;

        double ratio = w / h;
        if ( sizeGeoRef == 0.0f ) {
            sizeGeoRef = 1.0f;
        }

        if ( ratio < 1 ) {
            // if < 1 then do orientation on h
            double newWidth = ( w / h ) * sizeGeoRef * rect.width;
            convertedPixelToRasterPoint = new Point2d( newWidth / w, rect.height * sizeGeoRef / h );
            transformedRasterSpan = new Point2d( newWidth, rect.height * sizeGeoRef );
            return transformedRasterSpan;
        } else if ( ratio > 1 ) {
            // if > 1 then do orientation on w
            double newHeight = ( h / w ) * sizeGeoRef * rect.height;
            convertedPixelToRasterPoint = new Point2d( rect.width * sizeGeoRef / w, newHeight / h );
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
