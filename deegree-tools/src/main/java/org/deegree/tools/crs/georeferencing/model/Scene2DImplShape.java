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

import static java.lang.Math.max;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.vecmath.Point2d;

import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.shape.ShapeFeatureStore;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.wms.Utils;
import org.deegree.rendering.r2d.Java2DRenderer;
import org.deegree.rendering.r2d.Java2DTextRenderer;
import org.deegree.services.wms.model.layers.Layer;
import org.deegree.tools.crs.georeferencing.application.Scene2DValues;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Scene2DImplShape implements Scene2D {

    private Scene2DValues sceneValues;

    private CRS srs;

    private BufferedImage generatedImage;

    private BufferedImage predictedImage;

    private int imageWidth, imageHeight;

    private FeatureStore store;

    private Graphics2D g;

    private String filePath;

    private ApplicationSchema schema;

    public Scene2DImplShape( String filePath, Graphics2D g ) {
        this.filePath = filePath;
        this.g = g;
    }

    @Override
    public void init( Scene2DValues values ) {
        this.sceneValues = values;

        try {

            store = new ShapeFeatureStore( filePath, null, null, "http://www.deegree.org/app", "MyFeatureType", true,
                                           null );

            store.init();

        } catch ( FeatureStoreException e1 ) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

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
        BufferedImage i = new BufferedImage( imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB );

        g = i.createGraphics();
        schema = store.getSchema();
        try {
            if ( imageBoundingbox == null ) {
                imageBoundingbox = store.getEnvelope( schema.getFeatureTypes()[0].getName() );
                sceneValues.setEnvelopeGeoref( imageBoundingbox );
            }
        } catch ( FeatureStoreException e1 ) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        Java2DRenderer renderer = new Java2DRenderer( g, imageWidth, imageHeight, imageBoundingbox );
        Java2DTextRenderer textRenderer = new Java2DTextRenderer( renderer );

        Query query = new Query( schema.getFeatureTypes()[0].getName(), imageBoundingbox, null, -1, -1, -1 );
        double resolution = max( imageBoundingbox.getSpan0() / imageWidth, imageBoundingbox.getSpan1() / imageHeight );
        srs = store.getStorageSRS();
        try {
            FeatureResultSet fs = store.query( query );
            for ( Feature f : fs ) {
                Layer.render( f, null, renderer, textRenderer, Utils.calcScaleWMS130( imageWidth, imageHeight,
                                                                                      imageBoundingbox,
                                                                                      srs.getWrappedCRS() ), resolution );
            }
        } catch ( FeatureStoreException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( FilterEvaluationException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( UnknownCRSException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        g.dispose();

        return i;
    }

    @Override
    public BufferedImage generateSubImage( Rectangle bounds ) {
        imageWidth = new Double( bounds.getWidth() ).intValue();
        imageHeight = new Double( bounds.getHeight() ).intValue();

        return generatedImage = generateMap( null );

    }

    @Override
    public BufferedImage generateSubImageFromRaster( Envelope env ) {
        return generatedImage = generateMap( env );

    }

    @Override
    public void generatePredictedImage( Point2d changePoint ) {

        // double minX = changePoint.x * 2 * ref.getResolutionX();
        // double minY = changePoint.y * 2 * ref.getResolutionY();
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

    // /**
    // * @param options
    // */
    // private List<String> getLayers( RasterIOOptions options ) {
    // List<String> configuredLayers = new LinkedList<String>();
    // String layers = options.get( "RASTERIO_WMS_REQUESTED_LAYERS" );
    // if ( StringUtils.isSet( layers ) ) {
    // String[] layer = layers.split( "," );
    // for ( String l : layer ) {
    //
    // configuredLayers.add( l );
    // }
    // }
    // if ( configuredLayers.isEmpty() ) {
    // List<String> namedLayers = this.wmsClient.getNamedLayers();
    // if ( namedLayers != null ) {
    //
    // configuredLayers.addAll( namedLayers );
    // }
    // }
    //
    // return configuredLayers;
    // }

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
