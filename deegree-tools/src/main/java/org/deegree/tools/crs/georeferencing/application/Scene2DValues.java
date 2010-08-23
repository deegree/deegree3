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
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint.PointType;

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

    private Rectangle dimensionFootprint;

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

    private Envelope envelopeFootprint;

    private GeometryFactory geom;

    private double ratio;

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

                // determine the minX and the maxY position of the subRaster-envelope
                double getMinX = subRaster.getEnvelope().getMin().get0();
                double getMaxY = subRaster.getEnvelope().getMax().get1();

                // determine the percentage of the requested point
                double percentX = getWorldDimension( pixelPoint ).getX();
                double percentY = getWorldDimension( pixelPoint ).getY();

                return new GeoReferencedPoint( getMinX + percentX, getMaxY - percentY );

            }
        case FootprintPoint:

            // determine the minX and the maxY position of the envelope
            double getMinX = this.envelopeFootprint.getMin().get0();
            double getMaxY = this.envelopeFootprint.getMax().get1();

            // determine the percentage of the requested point
            double percentX = getWorldDimension( pixelPoint ).getX();
            double percentY = getWorldDimension( pixelPoint ).getY();

            return new FootprintPoint( getMinX + percentX, getMaxY - percentY );

        }

        return null;
    }

    /**
     * Converts the pixelDimension to a dimension with world coordinates.
     * 
     * @param dimension
     *            , not <Code>null</Code>.
     * @return an AbstractGRPoint in worldCoordinates.
     */
    private AbstractGRPoint getWorldDimension( AbstractGRPoint dimension ) {
        switch ( dimension.getPointType() ) {

        case GeoreferencedPoint:

            if ( subRaster != null ) {

                // determine the span of the envelope
                double spanX = this.subRaster.getEnvelope().getSpan0();
                double spanY = this.subRaster.getEnvelope().getSpan1();

                // if(){
                //                    
                // }
                // determine the percentage of the requested point
                double percentX = ( dimension.getX() / dimensionGeoreference.width ) * spanX;
                double percentY = ( dimension.getY() / dimensionGeoreference.height ) * spanY;

                return new GeoReferencedPoint( percentX, percentY );

            }
        case FootprintPoint:

            // determine the span of the envelope
            double spanX = this.envelopeFootprint.getSpan0();
            double spanY = this.envelopeFootprint.getSpan1();

            // determine the percentage of the requested point
            double percentX = ( dimension.getX() / dimensionFootprint.width ) * spanX;
            double percentY = ( dimension.getY() / dimensionFootprint.height ) * spanY;

            return new FootprintPoint( percentX, percentY );

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
            // transformPropFoot();
            pixelPointX = new Double( ( percentPoint.x * dimensionFootprint.width ) ).intValue();
            pixelPointY = new Double( ( 1 - percentPoint.y ) * dimensionFootprint.height ).intValue();
            return new int[] { pixelPointX, pixelPointY };
        }

        return null;
    }

    /**
     * Sets the points <i>xCoord</i> and <i>yCoord</i> as the centroid of the envelope with specified spans in x- and
     * y-direction. These parameters will be corrected if there is a mismatch regarding to the proportion of the
     * requested envelope.
     * 
     * @param xCoord
     *            x-coordiante in worldCoordinate-representation, not be <Code>null</Code>.
     * @param yCoord
     *            y-coordiante in worldCoordinate-representation, not be <Code>null</Code>.
     * @param spanX
     *            x-dimension that should be the width of the envelope, if not specified use
     *            {@link #setCentroidWorldEnvelopePosition(double, double)} instead.
     * @param spanY
     *            y-dimension that should be the height of the envelope, if not specified use
     *            {@link #setCentroidWorldEnvelopePosition(double, double)} instead.
     */
    public void setCentroidWorldEnvelopePosition( double xCoord, double yCoord, double spanX, double spanY,
                                                  PointType type ) {

        double halfSpanXWorld;
        double halfSpanYWorld;
        switch ( type ) {
        case GeoreferencedPoint:
            if ( spanX != -1 && spanY != -1 ) {
                if ( ratio < 1 ) {
                    halfSpanYWorld = spanY / 2;
                    halfSpanXWorld = halfSpanYWorld * transformedRasterSpan.x / transformedRasterSpan.y;

                } else {

                    halfSpanXWorld = spanX / 2;
                    halfSpanYWorld = halfSpanXWorld * transformedRasterSpan.y / transformedRasterSpan.x;

                }

            } else {
                halfSpanXWorld = this.subRaster.getEnvelope().getSpan0() / 2;
                halfSpanYWorld = this.subRaster.getEnvelope().getSpan1() / 2;

            }

            double minX = xCoord - halfSpanXWorld;
            double minY = yCoord - halfSpanYWorld;
            double maxX = xCoord + halfSpanXWorld;
            double maxY = yCoord + halfSpanYWorld;
            Envelope enve = geom.createEnvelope( minX, minY, maxX, maxY, crs );
            this.subRaster = raster.getAsSimpleRaster().getSubRaster( enve );
            System.out.println( "[Scene2DValues] subRaster: " + subRaster );
            rasterRect = this.subRaster.getRasterReference().convertEnvelopeToRasterCRS( enve );
            break;
        case FootprintPoint:
            double minXF = xCoord;
            double minYF = yCoord;
            double maxYF = minYF - spanY;
            double maxXF = minXF + spanX;

            transformProportionFootprint( geom.createEnvelope( minXF, maxYF, maxXF, minYF,
                                                               envelopeFootprint.getCoordinateSystem() ) );
            break;
        }
    }

    /**
     * 
     * Sets the point as the centroid of the envelope.
     * 
     * @param xCoord
     *            x-coordiante in worldCoordinate-representation, not be <Code>null</Code>.
     * @param yCoord
     *            y-coordiante in worldCoordinate-representation, not be <Code>null</Code>.
     * 
     */
    public void setCentroidWorldEnvelopePosition( double xCoord, double yCoord, PointType type ) {
        this.setCentroidWorldEnvelopePosition( xCoord, yCoord, -1, -1, type );
    }

    /**
     * Resizes the envelope in world coordinates to the specified size. The specified centroid will be the
     * centerposition of the new envelope.
     * 
     * @param centroid
     *            the centroid position in pixel coordinates, not <Code>null</Code>.
     * 
     * @param dimension
     *            the width and height of the envelope in pixel coordinates, not <Code>null</Code>.
     */
    public void setCentroidRasterEnvelopePosition( AbstractGRPoint centroid, AbstractGRPoint dimension ) {
        AbstractGRPoint center = getWorldPoint( centroid );
        AbstractGRPoint dim = getWorldDimension( dimension );

        this.setCentroidWorldEnvelopePosition( center.getX(), center.getY(), dim.getX(), dim.getY(),
                                               center.getPointType() );
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
        if ( envelopeFootprint != null ) {
            transformPropFoot();
        }
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

    public Point2d generateTransformedBounds() {
        if ( rasterRect != null ) {
            return transformProportion( rasterRect );
        }
        return null;
    }

    public Point2d getTransformedBounds() {
        return this.transformedRasterSpan;
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
            System.out.println( "[Scene2DValues] Subrasterzoomed " + subRaster + " SPAN: " + enve.getSpan0() + ", "
                                + enve.getSpan1() );
            rasterRect = this.subRaster.getRasterReference().convertEnvelopeToRasterCRS( enve );
            // transformProportion( rasterRect );
            break;
        case FootprintPoint:
            center = (FootprintPoint) getWorldPoint( mousePosition );
            this.envelopeFootprint = createZoomedEnv( envelopeFootprint, newSize, center );
            System.out.println( "[Scene2DValues] envZoomed " + envelopeFootprint );
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

        ratio = w / h;
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

    /**
     * Transforms the ratio of the dimensions of the footprintPanel. <br>
     * It takes the ratio between the dimension -width and -height and the ratio between the envelope -width and
     * -height.
     */
    private void transformPropFoot() {
        double wE = envelopeFootprint.getSpan0();
        double hE = envelopeFootprint.getSpan1();
        double w = dimensionFootprint.width;
        double h = dimensionFootprint.height;
        double ratioEnv = wE / hE;
        double ratio = w / h;

        System.out.println( "[Scene2DValues] bevore " + dimensionFootprint + " " + ratio );
        if ( ratio < 1 ) {

            dimensionFootprint.height = new Double( dimensionFootprint.height * ratio / ratioEnv ).intValue();
        } else if ( ratio > 1 ) {

            dimensionFootprint.width = new Double( dimensionFootprint.width * ratioEnv / ratio ).intValue();

        }
        System.out.println( "[Scene2DValues] after " + dimensionFootprint + " " + envelopeFootprint );

    }

    private void transformProportionFootprint( Envelope env ) {
        double wE = env.getSpan0();
        double hE = env.getSpan1();
        double w = dimensionFootprint.width;
        double h = dimensionFootprint.height;
        double ratioEnv = wE / hE;
        double ratio = w / h;
        double rW = w / wE;
        double rH = h / hE;

        if ( rW < rH ) {
            double minX = env.getMin().get0();
            double minY = env.getMin().get1();
            double newHeight = minY - ( env.getSpan0() * h / w );

            this.envelopeFootprint = geom.createEnvelope( minX, minY, minX + env.getSpan0(), newHeight,
                                                          env.getCoordinateSystem() );
        } else if ( rW > rH ) {
            double minX = env.getMin().get0();
            double minY = env.getMin().get1();
            double newWidth = minX + ( env.getSpan1() * w / h );

            this.envelopeFootprint = geom.createEnvelope( minX, minY, newWidth, minY + env.getSpan1(),
                                                          env.getCoordinateSystem() );

        }
        // TODO what if equals?
        System.out.println( "[Scene2DValues] after " + dimensionFootprint + " " + envelopeFootprint );

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
        System.out.println( "[Scene2DValues] minPixel: " + minPointPixel + " minRaster: " + minPointRaster );
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
