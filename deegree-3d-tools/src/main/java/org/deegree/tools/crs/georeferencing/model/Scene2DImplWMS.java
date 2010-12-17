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
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point2d;

import jj2000.j2k.NotImplementedError;

import org.deegree.commons.utils.StringUtils;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.wms.client.WMSClient111;
import org.deegree.tools.crs.georeferencing.application.ParameterStore;
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

    private Scene2DValues sceneValues;

    private WMSClient111 wmsClient;

    private List<String> lays;

    private String format;

    private BufferedImage generatedImage;

    private BufferedImage predictedImage;

    private int imageWidth, imageHeight;

    private ParameterStore store;

    public Scene2DImplWMS( ParameterStore store, WMSClient111 wmsClient ) {
        this.store = store;
        this.wmsClient = wmsClient;
    }

    @Override
    public void init( Scene2DValues values ) {
        this.sceneValues = values;

        this.sceneValues.setEnvelopeGeoref( store.getBbox() );

        lays = getLayers( store.getLayers() );
        format = store.getFormat();

        imageWidth = store.getQor();
        imageHeight = store.getQor();

    }

    /**
     * The GetMap()-request to a WMSClient.
     */
    private BufferedImage generateMap( Envelope imageBoundingbox ) {
        BufferedImage i = null;
        try {

            // CRSConfiguration crsConfig = CRSConfiguration.getInstance(
            // "org.deegree.cs.configuration.deegree.xml.DeegreeCRSProvider" );
            // TransformationFactory fac = crsConfig.getTransformationFactory();
            // Transformation trans = null;
            // List<Point3d> l = null;
            // try {
            // trans = fac.createFromCoordinateSystems( imageBoundingbox.getCoordinateSystem().getWrappedCRS(),
            // GeographicCRS.WGS84 );
            // l = trans.doTransform( getBboxAsPoint3d( imageBoundingbox ) );
            // } catch ( TransformationException e ) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // } catch ( IllegalArgumentException e ) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // } catch ( UnknownCRSException e ) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            // double[] d = new double[4];
            // int counter = 0;
            // for ( Point3d p : l ) {
            // d[counter++] = p.x;
            // d[counter++] = p.y;
            // }
            //
            // Envelope envi = sceneValues.transformProportionGeorefPartialOrientation( geom.createEnvelope(
            // d[0],
            // d[1],
            // d[2],
            // d[3],
            // new CRS(
            // GeographicCRS.WGS84 ) ) );
            // System.out.println( "[Scene2dImplWMS] " + envi );
            i = wmsClient.getMap( lays, imageWidth, imageHeight, imageBoundingbox, sceneValues.getCrs(), format, true,
                                  false, -1, false, null ).first;

        } catch ( IOException e ) {
            e.printStackTrace();
        }

        return i;
    }

    @Override
    public BufferedImage generateSubImage( Rectangle bounds ) {
        sceneValues.transformAspectRatioGeorefPartial( sceneValues.getEnvelopeGeoref() );
        // sceneValues.transformProportionGeorefFullOrientation( store.getBbox() );
        return generatedImage = generateMap( sceneValues.getEnvelopeGeoref() );

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

    private List<String> getLayers( String layers ) {
        List<String> configuredLayers = new LinkedList<String>();
        // String layers = options.get( "RASTERIO_WMS_REQUESTED_LAYERS" );
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

}
