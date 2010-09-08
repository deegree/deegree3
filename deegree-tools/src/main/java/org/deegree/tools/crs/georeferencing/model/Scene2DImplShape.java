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

import jj2000.j2k.NotImplementedError;

import org.deegree.commons.jdbc.ConnectionManager;
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
 * Generates a 2D BufferedImage from a ShapeFile.
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

            ConnectionManager.destroy();
            store = new ShapeFeatureStore( filePath, null, null, "http://www.deegree.org/app", "MyFeatureType", true,
                                           null );

            store.init();
            srs = store.getStorageSRS();

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
                imageBoundingbox.setCoordinateSystem( srs );
                sceneValues.setEnvelopeGeoref( imageBoundingbox );
                sceneValues.transformProportionGeorefPartialOrientation( imageBoundingbox );
                imageBoundingbox = sceneValues.getEnvelopeGeoref();
            }
        } catch ( FeatureStoreException e1 ) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        Java2DRenderer renderer = new Java2DRenderer( g, imageWidth, imageHeight, imageBoundingbox );
        Java2DTextRenderer textRenderer = new Java2DTextRenderer( renderer );

        Query query = new Query( schema.getFeatureTypes()[0].getName(), imageBoundingbox, null, -1, -1, -1 );
        double resolution = max( imageBoundingbox.getSpan0() / imageWidth, imageBoundingbox.getSpan1() / imageHeight );

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
        throw new NotImplementedError();
    }

    @Override
    public BufferedImage getGeneratedImage() {
        return generatedImage;
    }

    @Override
    public BufferedImage getPredictedImage() {

        return predictedImage;
    }

}
