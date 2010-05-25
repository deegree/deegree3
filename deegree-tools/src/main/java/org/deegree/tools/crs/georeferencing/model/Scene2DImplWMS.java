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
package org.deegree.tools.crs.georeferencing.model;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point2d;

import org.deegree.commons.utils.StringUtils;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.protocol.wms.client.WMSClient111;

/**
 * 
 * Generates a 2D BufferedImage from a WMS request.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Scene2DImplWMS implements Scene2D {

    private AbstractRaster raster;

    private RasterIOOptions options;

    private WMSClient111 wmsClient;

    private CRS srs;

    private List<String> lays;

    private String format;

    private Envelope imageBoundingbox;

    private Envelope holeRequestBoundingbox;

    private Point2d onePixel;

    private GeometryFactory geometryFactory;

    private BufferedImage generatedImage;

    private Envelope sightWindowBoundingbox;

    private Envelope predictedBoundingbox;

    private BufferedImage predictedImage;

    private int panelWidth;

    private int panelHeight;

    /**
     * The GetMap()-request to a WMSClient.
     * 
     * @param panelWidth
     * @param panelHeight
     * @param sightWindowMinX
     * @param sightWindowMaxX
     * @param sightWindowMinY
     * @param sightWindowMaxY
     * @return
     */
    private BufferedImage generateMap( Envelope imageBoundingbox ) {

        lays = getLayers( options );
        format = options.get( "RASTERIO_WMS_DEFAULT_FORMAT" );
        srs = options.getCRS();
        panelWidth = Integer.parseInt( options.get( "RASTERIO_WMS_MAX_WIDTH" ) );
        panelHeight = Integer.parseInt( options.get( "RASTERIO_WMS_MAX_HEIGHT" ) );
        try {
            BufferedImage img = wmsClient.getMap( lays, panelWidth, panelHeight, imageBoundingbox, srs, format, true,
                                                  false, 1000, false, null ).first;
            return img;

        } catch ( IOException e ) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Determines the ratio the boundingbox has to orient on. If there is a mismatch between the width and height this
     * should influence the display of the image returned by the WMS to prevent any deformation. <li>pos - orientation
     * on width because width is larger</li> <li>neg - orientation on hight because hight is larger</li> <li>other -
     * orientation on width/hight because they are even</li>
     * 
     * @param panelBounds
     *            the rectangle bounds, not <Code>null</Code>
     * @return an positive, negative or even integer
     */
    private int determineProportion( RasterRect panelBounds ) {
        double w = panelBounds.width;
        double h = panelBounds.height;

        double ratio = w / h;

        if ( ratio < 1 ) {
            // if < 1 then do orientation on h
            return -1;
        } else if ( ratio > 1 ) {
            // if > 1 then do orientation on w
            return 1;
        }
        // if w = h then return 0
        return 0;
    }

    @Override
    public BufferedImage generateImage( RasterIOOptions options ) {

        this.options = options;
        URL url = null;
        try {
            String g = options.get( "RASTER_URL" );
            url = new URL( options.get( "RASTER_URL" ) );

            InputStream in;

            in = url.openStream();

            raster = RasterFactory.loadRasterFromStream( in, options );
            in.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        wmsClient = new WMSClient111( url );

        // generatedImage = RasterFactory.imageFromRaster( raster );
        // try {
        // RasterFactory.saveRasterToFile( raster, new File( "/tmp/out.png" ) );
        // } catch ( IOException e ) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        generatedImage = generateMap( raster.getEnvelope() );

        return generatedImage;

    }

    @Override
    public BufferedImage generatePredictedImage( Rectangle predictedBounds, Envelope envelope ) {

        // RasterRect rect = new RasterRect( 0, 0, predictedBounds.width * 2, predictedBounds.height * 2 );
        //
        // Envelope env = geometryFactory.createEnvelope( envelope.getMin().get0(), envelope.getMin().get1(),
        // envelope.getMin().get0() + envelope.getSpan0() * 2,
        // envelope.getMin().get1() + envelope.getSpan1() * 2,
        // envelope.getCoordinateSystem() );

        double panelWidth = predictedBounds.getWidth();
        double panelHeight = predictedBounds.getHeight();

        // predictedImage = generateMap( rect.width, rect.height, env );
        // predictedImage = generateMap( panelWidth, panelHeight, envelope );

        return predictedImage;

    }

    // /**
    // *
    // *
    // * @param sceneBounds
    // * @return the boundingbox of the image that should be displayed
    // */
    // private Envelope generateImageBoundingbox( RasterRect sceneBounds ) {
    // int proportion = determineProportion( sceneBounds );
    //
    // double panelWidth = sceneBounds.width;
    // double panelHeight = sceneBounds.height;
    // if ( sightWindowBoundingbox != null ) {
    //
    // double minX = sightWindowBoundingbox.getMin().get0();
    // double minY = sightWindowBoundingbox.getMin().get1();
    // double maxX = sightWindowBoundingbox.getMax().get0();
    // double maxY = sightWindowBoundingbox.getMax().get1();
    //
    // if ( proportion == 0 ) {
    // // do nothing
    //
    // } else if ( proportion < 0 ) {
    //
    // double newWidth = ( panelWidth / panelHeight ) * ( maxY - minY );
    //
    // imageBoundingbox = geometryFactory.createEnvelope( minX, minY, ( minX + newWidth ), maxY, srs );
    //
    // } else {
    // double newHeight = ( panelHeight / panelWidth ) * ( maxX - minX );
    //
    // imageBoundingbox = geometryFactory.createEnvelope( minX, minY, maxX, ( minY + newHeight ), srs );
    //
    // }
    // onePixel = normalizeImageBoundingbox( sceneBounds, imageBoundingbox );
    //
    // }
    // System.out.println( "SCENE2DIMPL - generateImagebbox: " + imageBoundingbox );
    // return imageBoundingbox;
    //
    // }

    /**
     * Based on bounds from an upper component this method normalizes the bounds of the upper component regarding to an
     * envelope to one pixel.
     * <p>
     * Sets the relation between panelBounds as the rectangle of the panel and bbox as the envelope of the requested
     * image.
     * 
     * @param panelBounds
     *            the rectangle bounds, not <Code>null</Code>
     * @param bbox
     *            the boundingbox, not <Code>null</Code>
     * @return a point, not <Code>null</Code>
     */
    private Point2d normalizeImageBoundingbox( RasterRect panelBounds, Envelope bbox ) {

        double w = panelBounds.width;
        double h = panelBounds.height;
        double oneX = bbox.getSpan0() / w;
        double oneY = bbox.getSpan1() / h;
        return new Point2d( oneX, oneY );
    }

    @Override
    public void changeImageBoundingbox( Point2d change ) {
        double envStartPosX = imageBoundingbox.getMin().get0() + change.getX() * onePixel.getX();
        double envStartPosY = imageBoundingbox.getMin().get1() - change.getY() * onePixel.getY();

        double envEndPosX = imageBoundingbox.getMax().get0() + change.getX() * onePixel.getX();
        double envEndPosY = imageBoundingbox.getMax().get1() - change.getY() * onePixel.getY();

        System.out.println( "OnePixel: " + onePixel + " -- Change: " + change );
        System.out.println( "  start: " + envStartPosX + ", " + envStartPosY + " end: " + envEndPosX + ", "
                            + envEndPosY );

        imageBoundingbox = geometryFactory.createEnvelope( envStartPosX, envStartPosY, envEndPosX, envEndPosY, srs );

    }

    @Override
    public void changePredictionBoundingbox( Point2d change ) {
        double rising = 1;
        Envelope env;
        if ( predictedBoundingbox != null ) {
            env = predictedBoundingbox;

        } else {
            env = imageBoundingbox;

        }
        double envStartPosX = env.getMin().get0() + ( change.getX() * rising ) * onePixel.getX();
        double envStartPosY = env.getMin().get1() - ( change.getY() * rising ) * onePixel.getY();

        double envEndPosX = env.getMax().get0() + ( change.getX() * rising ) * onePixel.getX();
        double envEndPosY = env.getMax().get1() - ( change.getY() * rising ) * onePixel.getY();

        System.out.println( "  startPrediction: " + envStartPosX + ", " + envStartPosY + " endPrediction: "
                            + envEndPosX + ", " + envEndPosY );

        predictedBoundingbox = geometryFactory.createEnvelope( envStartPosX, envStartPosY, envEndPosX, envEndPosY, srs );
    }

    @Override
    public Envelope getImageBoundingbox() {
        return imageBoundingbox;
    }

    @Override
    public void reset() {
        imageBoundingbox = null;
        predictedBoundingbox = null;
        sightWindowBoundingbox = null;
        onePixel = null;
        generatedImage = null;
        predictedImage = null;
    }

    @Override
    public void setSightWindowBoundingbox( Envelope sigthWindowBoundingbox ) {
        this.sightWindowBoundingbox = sigthWindowBoundingbox;

    }

    @Override
    public Envelope getHoleRequestBoundingbox() {

        return holeRequestBoundingbox;
    }

    @Override
    public Envelope getPredictionBoundingbox() {

        return predictedBoundingbox;
    }

    @Override
    public BufferedImage getGeneratedImage() {
        return generatedImage;
    }

    @Override
    public BufferedImage getPredictedImage() {
        return predictedImage;
    }

    @Override
    public Point2d getOnePixel() {
        return onePixel;
    }

    /**
     * @param options
     */
    private List<String> getLayers( RasterIOOptions options ) {
        List<String> configuredLayers = new LinkedList<String>();
        String layers = options.get( "RASTERIO_WMS_REQUESTED_LAYERS" );
        if ( StringUtils.isSet( layers ) ) {
            String[] layer = layers.split( "," );
            for ( String l : layer ) {
                configuredLayers.add( l );
            }
        }
        if ( configuredLayers.isEmpty() ) {
            List<String> namedLayers = this.wmsClient.getNamedLayers();
            if ( namedLayers != null ) {
                configuredLayers.addAll( namedLayers );
            }
        }
        return configuredLayers;
    }

}
