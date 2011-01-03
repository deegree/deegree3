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
package org.deegree.remoteows.wms;

import static java.util.Collections.singletonList;
import static org.deegree.commons.utils.math.MathUtils.round;
import static org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation.OUTER;
import static org.deegree.coverage.raster.interpolation.InterpolationType.BILINEAR;
import static org.deegree.coverage.raster.utils.RasterFactory.rasterDataFromImage;
import static org.deegree.coverage.raster.utils.RasterFactory.rasterDataToImage;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.deegree.commons.utils.Pair;
import org.deegree.coverage.raster.RasterTransformer;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.protocol.wms.Utils;
import org.deegree.protocol.wms.client.WMSClient111;
import org.deegree.remoteows.RemoteOWSStore;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class RemoteWMSStore implements RemoteOWSStore {

    private static final Logger LOG = getLogger( RemoteWMSStore.class );

    private WMSClient111 client;

    private Map<String, LayerOptions> layers;

    private List<String> layerOrder;

    private TreeSet<String> commonSRS;

    private LayerOptions options;

    /**
     * @param client
     * @param layers
     */
    public RemoteWMSStore( WMSClient111 client, List<String> layers, LayerOptions options ) {
        this.client = client;
        this.layerOrder = layers;
        this.options = options;

        for ( String l : layers ) {
            if ( commonSRS == null ) {
                commonSRS = new TreeSet<String>( client.getCoordinateSystems( l ) );
            } else {
                commonSRS.retainAll( client.getCoordinateSystems( l ) );
            }
        }
        LOG.debug( "Requestable srs common to all cascaded layers: " + commonSRS );
    }

    /**
     * @param client
     * @param layers
     */
    public RemoteWMSStore( WMSClient111 client, Map<String, LayerOptions> layers, List<String> layerOrder ) {
        this.client = client;
        this.layers = layers;
        this.layerOrder = layerOrder;
    }

    /**
     * @return true, if all layers can be requested in one request
     */
    public boolean isSimple() {
        return options != null;
    }

    private BufferedImage getMap( final String layer, final Envelope envelope, final int width, final int height,
                                  LayerOptions opts ) {
        CRS origCrs = envelope.getCoordinateSystem();
        String origCrsName = origCrs.getName();
        try {
            if ( ( !opts.alwaysUseDefaultCRS && client.getCoordinateSystems( layer ).contains( origCrsName ) )
                 || origCrsName.equals( opts.defaultCRS ) ) {
                LOG.trace( "Will request remote layer(s) in " + origCrsName );
                LinkedList<String> errors = new LinkedList<String>();
                Pair<BufferedImage, String> pair = client.getMap( new LinkedList<String>( singletonList( layer ) ),
                                                                  width, height, envelope, origCrs, opts.imageFormat,
                                                                  opts.transparent, false, -1, true, errors );
                LOG.debug( "Parameters that have been replaced for this request: " + errors );
                if ( pair.first == null ) {
                    LOG.debug( "Error from remote WMS: " + pair.second );
                }
                return pair.first;
            }

            // case: transform the bbox and image
            LOG.trace( "Will request remote layer(s) in {} and transform to {}", opts.defaultCRS, origCrsName );

            GeometryTransformer trans = new GeometryTransformer( opts.defaultCRS );
            Envelope bbox = trans.transform( envelope, origCrs.getWrappedCRS() );

            RasterTransformer rtrans = new RasterTransformer( origCrs.getWrappedCRS() );

            double scale = Utils.calcScaleWMS111( width, height, envelope,
                                                  envelope.getCoordinateSystem().getWrappedCRS() );
            double newScale = Utils.calcScaleWMS111( width, height, bbox, new CRS( opts.defaultCRS ).getWrappedCRS() );
            double ratio = scale / newScale;

            int newWidth = round( ratio * width );
            int newHeight = round( ratio * height );

            LinkedList<String> errors = new LinkedList<String>();
            Pair<BufferedImage, String> pair = client.getMap( layerOrder, newWidth, newHeight, bbox,
                                                              new CRS( opts.defaultCRS ), opts.imageFormat,
                                                              opts.transparent, false, -1, true, errors );

            LOG.debug( "Parameters that have been replaced for this request: {}", errors );
            if ( pair.first == null ) {
                LOG.debug( "Error from remote WMS: {}", pair.second );
                return null;
            }

            RasterGeoReference env = RasterGeoReference.create( OUTER, bbox, newWidth, newHeight );
            RasterData data = rasterDataFromImage( pair.first );
            SimpleRaster raster = new SimpleRaster( data, bbox, env );

            SimpleRaster transformed = rtrans.transform( raster, envelope, width, height, BILINEAR ).getAsSimpleRaster();

            return rasterDataToImage( transformed.getRasterData() );
        } catch ( IOException e ) {
            LOG.info( "Error when loading image from remote WMS: {}", e.getLocalizedMessage() );
            LOG.trace( "Stack trace", e );
        } catch ( UnknownCRSException e ) {
            LOG.warn( "Unable to find crs, this is not supposed to happen." );
            LOG.trace( "Stack trace", e );
        } catch ( TransformationException e ) {
            LOG.warn( "Unable to transform bbox from {} to {}", origCrsName, opts.defaultCRS );
            LOG.trace( "Stack trace", e );
        }
        return null;
    }

    /**
     * @param envelope
     * @param width
     * @param height
     * @return a singleton list if #isSimple returns true, or a list of one image per layer
     */
    public List<BufferedImage> getMap( final Envelope envelope, final int width, final int height ) {
        if ( options != null ) {
            CRS origCrs = envelope.getCoordinateSystem();
            String origCrsName = origCrs.getName();
            try {

                if ( ( !options.alwaysUseDefaultCRS && commonSRS.contains( origCrsName ) )
                     || origCrsName.equals( options.defaultCRS ) ) {
                    LOG.trace( "Will request remote layer(s) in " + origCrsName );
                    LinkedList<String> errors = new LinkedList<String>();
                    Pair<BufferedImage, String> pair = client.getMap( layerOrder, width, height, envelope, origCrs,
                                                                      options.imageFormat, options.transparent, false,
                                                                      -1, true, errors );
                    LOG.debug( "Parameters that have been replaced for this request: " + errors );
                    if ( pair.first == null ) {
                        LOG.debug( "Error from remote WMS: " + pair.second );
                    }
                    return singletonList( pair.first );
                }

                // case: transform the bbox and image
                LOG.trace( "Will request remote layer(s) in {} and transform to {}", options.defaultCRS, origCrsName );

                GeometryTransformer trans = new GeometryTransformer( options.defaultCRS );
                Envelope bbox = trans.transform( envelope, origCrs.getWrappedCRS() );

                RasterTransformer rtrans = new RasterTransformer( origCrs.getWrappedCRS() );

                double scale = Utils.calcScaleWMS111( width, height, envelope,
                                                      envelope.getCoordinateSystem().getWrappedCRS() );
                double newScale = Utils.calcScaleWMS111( width, height, bbox,
                                                         new CRS( options.defaultCRS ).getWrappedCRS() );
                double ratio = scale / newScale;

                int newWidth = round( ratio * width );
                int newHeight = round( ratio * height );

                LinkedList<String> errors = new LinkedList<String>();
                Pair<BufferedImage, String> pair = client.getMap( layerOrder, newWidth, newHeight, bbox,
                                                                  new CRS( options.defaultCRS ), options.imageFormat,
                                                                  options.transparent, false, -1, true, errors );

                LOG.debug( "Parameters that have been replaced for this request: {}", errors );
                if ( pair.first == null ) {
                    LOG.debug( "Error from remote WMS: {}", pair.second );
                    return null;
                }

                RasterGeoReference env = RasterGeoReference.create( OUTER, bbox, newWidth, newHeight );
                RasterData data = rasterDataFromImage( pair.first );
                SimpleRaster raster = new SimpleRaster( data, bbox, env );

                SimpleRaster transformed = rtrans.transform( raster, envelope, width, height, BILINEAR ).getAsSimpleRaster();

                return Collections.singletonList( rasterDataToImage( transformed.getRasterData() ) );

            } catch ( IOException e ) {
                LOG.info( "Error when loading image from remote WMS: {}", e.getLocalizedMessage() );
                LOG.trace( "Stack trace", e );
            } catch ( UnknownCRSException e ) {
                LOG.warn( "Unable to find crs, this is not supposed to happen." );
                LOG.trace( "Stack trace", e );
            } catch ( TransformationException e ) {
                LOG.warn( "Unable to transform bbox from {} to {}", origCrsName, options.defaultCRS );
                LOG.trace( "Stack trace", e );
            }
            return null;
        }

        ArrayList<BufferedImage> list = new ArrayList<BufferedImage>();
        for ( String l : layerOrder ) {
            list.add( getMap( l, envelope, width, height, layers.get( l ) ) );
        }
        return list;
    }

    /**
     * @return null, if cascaded WMS does not have a bbox for the layers in EPSG:4326
     */
    public Envelope getEnvelope() {
        return client.getBoundingBox( "EPSG:4326", layerOrder );
    }

    /**
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public static class LayerOptions {
        public boolean transparent = true, alwaysUseDefaultCRS = false;

        public String imageFormat = "image/png", defaultCRS = "EPSG:4326";
    }

}
