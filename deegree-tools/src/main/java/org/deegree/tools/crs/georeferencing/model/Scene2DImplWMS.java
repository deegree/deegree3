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
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.wms.client.WMSClient111;
import org.deegree.tools.crs.georeferencing.application.Scene2DValues;

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

    private SimpleRaster subRaster;

    private Scene2DValues sceneValues;

    private SimpleRaster predictedRaster;

    private RasterGeoReference ref;

    private RasterDataInfo rasterDataInfo;

    private RasterData rasterData;

    private RasterRect rasterRect;

    private SimpleRaster simpleMapRaster;

    private RasterIOOptions options;

    private WMSClient111 wmsClient;

    private CRS srs;

    private List<String> lays;

    private String format;

    private BufferedImage generatedImage;

    private BufferedImage predictedImage;

    // private double size;

    private int imageWidth, imageHeight;

    @Override
    public void init( RasterIOOptions options, Scene2DValues values ) {

        this.options = options;
        URL url = null;
        this.sceneValues = values;

        try {
            url = new URL( options.get( "RASTER_URL" ) );

            InputStream in = url.openStream();
            raster = RasterFactory.loadRasterFromStream( in, options );
            in.close();
            SimpleRaster ra = raster.getAsSimpleRaster().getSubRaster(
                                                                       Double.parseDouble( options.get( "LEFT_LOWER_X" ) ),
                                                                       Double.parseDouble( options.get( "LEFT_LOWER_Y" ) ),
                                                                       Double.parseDouble( options.get( "RIGHT_UPPER_X" ) ),
                                                                       Double.parseDouble( options.get( "RIGHT_UPPER_Y" ) ) );
            this.sceneValues.setRaster( ra );
            ref = ra.getRasterReference();
            this.sceneValues.setRasterGeoRef( ref );
            rasterRect = ref.convertEnvelopeToRasterCRS( ra.getEnvelope() );
            this.sceneValues.setRasterRect( rasterRect );
            this.sceneValues.setCrs( raster.getCoordinateSystem() );

        } catch ( IOException e ) {
            e.printStackTrace();
        }

        wmsClient = new WMSClient111( url );

        lays = getLayers( options );
        format = options.get( "RASTERIO_WMS_DEFAULT_FORMAT" );
        srs = options.getCRS();
        imageWidth = Integer.parseInt( options.get( "RASTERIO_WMS_MAX_WIDTH" ) );
        imageHeight = Integer.parseInt( options.get( "RASTERIO_WMS_MAX_HEIGHT" ) );

    }

    /**
     * The GetMap()-request to a WMSClient.
     * 
     * @param imageWidth
     * @param iamgeHeight
     * @param sightWindowMinX
     * @param sightWindowMaxX
     * @param sightWindowMinY
     * @param sightWindowMaxY
     * @return
     */
    private BufferedImage generateMap( Envelope imageBoundingbox ) {
        BufferedImage i = null;
        try {
            i = wmsClient.getMap( lays, imageWidth, imageHeight, imageBoundingbox, srs, format, true, false, -1, false,
                                  null ).first;

        } catch ( IOException e ) {
            e.printStackTrace();
        }

        return i;
    }

    @Override
    public BufferedImage generateSubImage( Rectangle bounds ) {
        Point2d transformedBounds = sceneValues.generateTransformedBounds();
        System.out.println( "[Scene2DImplWMS] transformedBounds: " + transformedBounds );
        if ( sceneValues.getMinPointRaster() == null ) {
            sceneValues.setMinPointRaster( new Point2d( rasterRect.x, rasterRect.y ) );

        }

        Point2d min = sceneValues.getMinPointRaster();

        double maxX = min.x + transformedBounds.x;
        double maxY = min.y + transformedBounds.y;

        // transform to get the boundingbox coordinates
        double[] worldCoordLeftLower = ref.getWorldCoordinate( min.x, maxY );
        double[] worldCoordRightUpper = ref.getWorldCoordinate( maxX, min.y );

        subRaster = raster.getAsSimpleRaster().getSubRaster( worldCoordLeftLower[0], worldCoordLeftLower[1],
                                                             worldCoordRightUpper[0], worldCoordRightUpper[1] );
        subRaster.setCoordinateSystem( raster.getCoordinateSystem() );
        sceneValues.setSubRaster( subRaster );
        rasterData = subRaster.getRasterData();
        System.out.println( "[Scene2DImplWMS] subRaster: " + subRaster );
        return generatedImage = generateMap( subRaster.getEnvelope() );

    }

    @Override
    public BufferedImage generateSubImageFromRaster( AbstractRaster raster ) {
        // Point2d transformedBounds = sceneValues.getTransformedBounds();
        // System.out.println( "transformedBounds: " + transformedBounds );
        // if ( sceneValues.getMinPointRaster() == null ) {
        // sceneValues.setMinPointRaster( new Point2d( rasterRect.x, rasterRect.y ) );
        //
        // }
        //
        // Point2d min = sceneValues.getMinPointRaster();
        //
        // double maxX = min.x + transformedBounds.x;
        // double maxY = min.y + transformedBounds.y;
        //
        // // transform to get the boundingbox coordinates
        // double[] worldCoordLeftLower = ref.getWorldCoordinate( min.x, maxY );
        // double[] worldCoordRightUpper = ref.getWorldCoordinate( maxX, min.y );

        // subRaster = raster.getAsSimpleRaster().getSubRaster( worldCoordLeftLower[0], worldCoordLeftLower[1],
        // worldCoordRightUpper[0], worldCoordRightUpper[1] );
        // subRaster.setCoordinateSystem( raster.getCoordinateSystem() );
        // sceneValues.setSubRaster( subRaster );
        // rasterData = subRaster.getRasterData();
        // System.out.println( "subRaster: " + subRaster );
        return generatedImage = generateMap( raster.getEnvelope() );

    }

    @Override
    public void generatePredictedImage( Point2d changePoint ) {

        double minX = changePoint.x * 2 * ref.getResolutionX();
        double minY = changePoint.y * 2 * ref.getResolutionY();
        // Point2d predictedBounds = new Point2d( transformedBounds.x * 2, transformedBounds.y * 2 );
        // double maxX = minX + predictedBounds.x;
        // double maxY = minY + predictedBounds.y;
        // double minX = 0;
        // double minY = 0;
        // double[] max = ref.getWorldCoordinate( raster.getColumns(), raster.getRows() );

        // System.out.println( ref.getSize( raster.getEnvelope() ) );
        // System.out.println( max[0] + " " + max[1] );

        // transform to get the boundingbox coordinates
        // double[] worldCoordLeftLower = ref.getWorldCoordinate( minX, maxY );
        // double[] worldCoordRightUpper = ref.getWorldCoordinate( maxX, minY );
        //
        // SimpleRaster predRaster = raster.getAsSimpleRaster().getSubRaster( worldCoordLeftLower[0],
        // worldCoordLeftLower[1],
        // worldCoordRightUpper[0],
        // worldCoordRightUpper[1] );
        // System.out.println( "predictedRaster: " + predRaster );
        // System.out.println( "world: " + worldCoordLeftLower[0] + " " + worldCoordLeftLower[1] + " "
        // + worldCoordRightUpper[0] + " " + worldCoordRightUpper[1] );

    }

    @Override
    public BufferedImage getGeneratedImage() {
        return generatedImage;
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

    @Override
    public BufferedImage getPredictedImage() {

        return predictedImage;
    }

    // @Override
    // public void generatePredictedImage( SimpleRaster coverage ) {
    // imageAround = new BufferedImage[4];
    // int c = coverage.getColumns();
    // int r = coverage.getRows();
    // GeometryFactory geoFac = new GeometryFactory();
    // // Grid
    // double[] leftUpperCornerRaster = new double[] { 0 - c, 0 - r };
    // double[] leftUpperCornerWorld = ref.getWorldCoordinate( leftUpperCornerRaster[0], leftUpperCornerRaster[1] );
    //
    // double[] rightUpperCornerRaster = new double[] { c * 2, 0 - r };
    // double[] rightUpperCornerWorld = ref.getWorldCoordinate( rightUpperCornerRaster[0], rightUpperCornerRaster[1] );
    //
    // double[] leftMiddleUpperCornerRaster = new double[] { 0 - c, 0 };
    // double[] leftMiddleUpperCornerWorld = ref.getWorldCoordinate( leftMiddleUpperCornerRaster[0],
    // leftMiddleUpperCornerRaster[1] );
    //
    // double[] rightMiddleUpperCornerRaster = new double[] { c * 2, 0 };
    // double[] rightMiddleUpperCornerWorld = ref.getWorldCoordinate( rightMiddleUpperCornerRaster[0],
    // rightMiddleUpperCornerRaster[1] );
    //
    // double[] leftMiddleLowerCornerRaster = new double[] { 0 - c, r };
    // double[] leftMiddleLowerCornerWorld = ref.getWorldCoordinate( leftMiddleLowerCornerRaster[0],
    // leftMiddleLowerCornerRaster[1] );
    //
    // double[] rightMiddleLowerCornerRaster = new double[] { c * 2, r };
    // double[] rightMiddleLowerCornerWorld = ref.getWorldCoordinate( rightMiddleLowerCornerRaster[0],
    // rightMiddleLowerCornerRaster[1] );
    //
    // double[] leftLowerCornerRaster = new double[] { 0 - c, r * 2 };
    // double[] leftLowerCornerWorld = ref.getWorldCoordinate( leftLowerCornerRaster[0], leftLowerCornerRaster[1] );
    //
    // double[] rightLowerCornerRaster = new double[] { c * 2, r * 2 };
    // double[] rightLowerCornerWorld = ref.getWorldCoordinate( rightLowerCornerRaster[0], rightLowerCornerRaster[1] );
    //
    // double[] leftUpperMiddleCornerRaster = new double[] { 0, 0 - r };
    // double[] leftUpperMiddleCornerWorld = ref.getWorldCoordinate( leftUpperMiddleCornerRaster[0],
    // leftUpperMiddleCornerRaster[1] );
    //
    // double[] rightUpperMiddleCornerRaster = new double[] { c, 0 - r };
    // double[] rightUpperMiddleCornerWorld = ref.getWorldCoordinate( rightUpperMiddleCornerRaster[0],
    // rightUpperMiddleCornerRaster[1] );
    //
    // double[] leftLowerMiddleCornerRaster = new double[] { 0, r * 2 };
    // double[] leftLowerMiddleCornerWorld = ref.getWorldCoordinate( leftLowerMiddleCornerRaster[0],
    // leftLowerMiddleCornerRaster[1] );
    //
    // double[] rightLowerMiddleCornerRaster = new double[] { c, r * 2 };
    // double[] rightLowerMiddleCornerWorld = ref.getWorldCoordinate( rightLowerMiddleCornerRaster[0],
    // rightLowerMiddleCornerRaster[1] );
    //
    // Envelope env1 = geoFac.createEnvelope( leftMiddleUpperCornerWorld, rightUpperCornerWorld, ref.getCrs() );
    // Envelope env2 = geoFac.createEnvelope( leftLowerCornerWorld, leftUpperMiddleCornerWorld, ref.getCrs() );
    // Envelope env3 = geoFac.createEnvelope( leftLowerCornerWorld, rightMiddleLowerCornerWorld, ref.getCrs() );
    // Envelope env4 = geoFac.createEnvelope( rightLowerMiddleCornerWorld, rightUpperCornerWorld, ref.getCrs() );
    //
    // imageAround[0] = generateMap( env1 );
    // imageAround[1] = generateMap( env2 );
    // imageAround[2] = generateMap( env3 );
    // imageAround[3] = generateMap( env4 );
    //
    // if ( coverage.getEnvelope().intersects( env4 ) ) {
    // System.out.println( "bin intersected mit env4" );
    // System.out.println( coverage.getEnvelope() );
    // System.out.println( env4 );
    // BufferedImage img = imageAround[3];
    // RasterFactory.imageFromRaster( coverage );
    // }
    //
    // }

}
