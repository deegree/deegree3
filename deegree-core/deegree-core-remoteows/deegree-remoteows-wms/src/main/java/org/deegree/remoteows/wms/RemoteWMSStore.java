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

import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR;
import static java.lang.Math.abs;
import static java.util.Collections.singletonList;
import static org.deegree.commons.utils.math.MathUtils.round;
import static org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation.OUTER;
import static org.deegree.coverage.raster.interpolation.InterpolationType.BILINEAR;
import static org.deegree.coverage.raster.utils.RasterFactory.rasterDataFromImage;
import static org.deegree.coverage.raster.utils.RasterFactory.rasterDataToImage;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.utils.Pair;
import org.deegree.coverage.raster.RasterTransformer;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.FeatureCollection;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.layer.Utils;
import org.deegree.remoteows.RemoteOWSStore;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@Deprecated
public class RemoteWMSStore implements RemoteOWSStore {

    private static final Logger LOG = getLogger( RemoteWMSStore.class );

    private OldWMSClient111 client;

    private Map<String, LayerOptions> layers;

    private List<String> layerOrder;

    private TreeSet<String> commonSRS;

    private LayerOptions options;

    /**
     * @param client
     * @param layers
     */
    public RemoteWMSStore( OldWMSClient111 client, List<String> layers, LayerOptions options ) {
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
    public RemoteWMSStore( OldWMSClient111 client, Map<String, LayerOptions> layers, List<String> layerOrder ) {
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
                                  LayerOptions opts, Map<String, String> originalParameters ) {
        ICRS origCrs = envelope.getCoordinateSystem();
        String origCrsName = origCrs.getAlias();
        Map<String, String> extraParams = new HashMap<String, String>();
        handleParameters( extraParams, originalParameters, opts.defaultParametersGetMap, opts.hardParametersGetMap );
        try {
            if ( ( !opts.alwaysUseDefaultCRS && client.getCoordinateSystems( layer ).contains( origCrsName ) )
                 || origCrsName.equals( opts.defaultCRS ) ) {
                LOG.trace( "Will request remote layer(s) in " + origCrsName );
                LinkedList<String> errors = new LinkedList<String>();
                Pair<BufferedImage, String> pair = client.getMap( singletonList( layer ), width, height, envelope,
                                                                  origCrs, opts.imageFormat, opts.transparent, false,
                                                                  -1, true, errors, extraParams );
                LOG.debug( "Parameters that have been replaced for this request: " + errors );
                if ( pair.first == null ) {
                    LOG.debug( "Error from remote WMS: " + pair.second );
                }
                return pair.first;
            }

            // case: transform the bbox and image
            LOG.trace( "Will request remote layer(s) in {} and transform to {}", opts.defaultCRS, origCrsName );

            GeometryTransformer trans = new GeometryTransformer( opts.defaultCRS );
            Envelope bbox = trans.transform( envelope, origCrs );

            RasterTransformer rtrans = new RasterTransformer( origCrs );

            double scale = Utils.calcScaleWMS111( width, height, envelope, envelope.getCoordinateSystem() );
            double newScale = Utils.calcScaleWMS111( width, height, bbox, CRSManager.getCRSRef( opts.defaultCRS ) );
            double ratio = scale / newScale;

            int newWidth = abs( round( ratio * width ) );
            int newHeight = abs( round( ratio * height ) );

            LinkedList<String> errors = new LinkedList<String>();
            Pair<BufferedImage, String> pair = client.getMap( singletonList( layer ), newWidth, newHeight, bbox,
                                                              CRSManager.getCRSRef( opts.defaultCRS ),
                                                              opts.imageFormat, opts.transparent, false, -1, true,
                                                              errors, extraParams );

            LOG.debug( "Parameters that have been replaced for this request: {}", errors );
            if ( pair.first == null ) {
                LOG.debug( "Error from remote WMS: {}", pair.second );
                return null;
            }

            // hack to ensure correct raster transformations. 4byte_abgr seems to be working best with current api
            if ( pair.first.getType() != TYPE_4BYTE_ABGR ) {
                BufferedImage img = new BufferedImage( pair.first.getWidth(), pair.first.getHeight(), TYPE_4BYTE_ABGR );
                Graphics2D g = img.createGraphics();
                g.drawImage( pair.first, 0, 0, null );
                g.dispose();
                pair.first = img;
            }

            RasterGeoReference env = RasterGeoReference.create( OUTER, bbox, newWidth, newHeight );
            RasterData data = rasterDataFromImage( pair.first );
            SimpleRaster raster = new SimpleRaster( data, bbox, env, null );

            SimpleRaster transformed = rtrans.transform( raster, envelope, width, height, BILINEAR ).getAsSimpleRaster();

            return rasterDataToImage( transformed.getRasterData() );
        } catch ( IOException e ) {
            LOG.info( "Error when loading image from remote WMS: {}", e.getLocalizedMessage() );
            LOG.trace( "Stack trace", e );
        } catch ( Throwable e ) {
            LOG.warn( "Unable to transform bbox from {} to {}", origCrsName, opts.defaultCRS );
            LOG.trace( "Stack trace", e );
        }
        return null;
    }

    private static void handleParameters( Map<String, String> map, Map<String, String> originals,
                                          Map<String, String> defaults, Map<String, String> hards ) {
        // handle default params
        for ( String def : defaults.keySet() ) {
            String key = def.toUpperCase();
            if ( originals.containsKey( key ) ) {
                map.put( key, originals.get( key ) );
            } else {
                map.put( def, defaults.get( def ) );
            }
        }
        // handle preset params
        for ( Entry<String, String> e : hards.entrySet() ) {
            if ( map.containsKey( e.getKey().toLowerCase() ) ) {
                map.put( e.getKey().toLowerCase(), e.getValue() );
            } else
                map.put( e.getKey(), e.getValue() );
        }
    }

    /**
     * @param envelope
     * @param width
     * @param height
     * @param originalParameters
     *            if not null, will be used to copy some optional/vendor parameters according to the configuration
     * @return a singleton list if #isSimple returns true, or a list of one image per layer
     */
    public List<BufferedImage> getMap( final Envelope envelope, final int width, final int height,
                                       final Map<String, String> originalParameters ) {
        Map<String, String> extraParameters = new HashMap<String, String>();
        if ( options != null ) {
            handleParameters( extraParameters, originalParameters, options.defaultParametersGetMap,
                              options.hardParametersGetMap );

            ICRS origCrs = envelope.getCoordinateSystem();
            String origCrsName = origCrs.getAlias();
            try {

                if ( ( !options.alwaysUseDefaultCRS && commonSRS.contains( origCrsName ) )
                     || origCrsName.equals( options.defaultCRS ) ) {
                    LOG.trace( "Will request remote layer(s) in " + origCrsName );
                    LinkedList<String> errors = new LinkedList<String>();
                    Pair<BufferedImage, String> pair = client.getMap( layerOrder, width, height, envelope, origCrs,
                                                                      options.imageFormat, options.transparent, false,
                                                                      -1, true, errors, extraParameters );
                    LOG.debug( "Parameters that have been replaced for this request: " + errors );
                    if ( pair.first == null ) {
                        LOG.debug( "Error from remote WMS: " + pair.second );
                    }
                    return singletonList( pair.first );
                }

                // case: transform the bbox and image
                LOG.trace( "Will request remote layer(s) in {} and transform to {}", options.defaultCRS, origCrsName );

                GeometryTransformer trans = new GeometryTransformer( options.defaultCRS );
                Envelope bbox = trans.transform( envelope, origCrs );

                RasterTransformer rtrans = new RasterTransformer( origCrs );

                double scale = Utils.calcScaleWMS111( width, height, envelope, envelope.getCoordinateSystem() );
                double newScale = Utils.calcScaleWMS111( width, height, bbox, CRSManager.getCRSRef( options.defaultCRS ) );
                double ratio = scale / newScale;

                int newWidth = abs( round( ratio * width ) );
                int newHeight = abs( round( ratio * height ) );

                LinkedList<String> errors = new LinkedList<String>();
                Pair<BufferedImage, String> pair = client.getMap( layerOrder, newWidth, newHeight, bbox,
                                                                  CRSManager.getCRSRef( options.defaultCRS ),
                                                                  options.imageFormat, options.transparent, false, -1,
                                                                  true, errors, extraParameters );

                LOG.debug( "Parameters that have been replaced for this request: {}", errors );
                if ( pair.first == null ) {
                    LOG.debug( "Error from remote WMS: {}", pair.second );
                    return null;
                }

                RasterGeoReference env = RasterGeoReference.create( OUTER, bbox, newWidth, newHeight );
                // hack to ensure correct raster transformations. 4byte_abgr seems to be working best with current api
                if ( pair.first.getType() != TYPE_4BYTE_ABGR ) {
                    BufferedImage img = new BufferedImage( pair.first.getWidth(), pair.first.getHeight(),
                                                           TYPE_4BYTE_ABGR );
                    Graphics2D g = img.createGraphics();
                    g.drawImage( pair.first, 0, 0, null );
                    g.dispose();
                    pair.first = img;
                }
                RasterData data = rasterDataFromImage( pair.first );
                SimpleRaster raster = new SimpleRaster( data, bbox, env, null );

                SimpleRaster transformed = rtrans.transform( raster, envelope, width, height, BILINEAR ).getAsSimpleRaster();

                return Collections.singletonList( rasterDataToImage( transformed.getRasterData() ) );

            } catch ( IOException e ) {
                LOG.info( "Error when loading image from remote WMS: {}", e.getLocalizedMessage() );
                LOG.trace( "Stack trace", e );
            } catch ( Throwable e ) {
                LOG.warn( "Unable to transform bbox from {} to {}", origCrsName, options.defaultCRS );
                LOG.trace( "Stack trace", e );
            }
            return null;
        }

        ArrayList<BufferedImage> list = new ArrayList<BufferedImage>();
        for ( String l : layerOrder ) {
            list.add( getMap( l, envelope, width, height, layers.get( l ), originalParameters ) );
        }
        return list;
    }

    /**
     * @param envelope
     * @param width
     * @param height
     * @param x
     * @param y
     * @param count
     * @return null, if reading the feature collection failed
     */
    public FeatureCollection getFeatureInfo( Envelope envelope, int width, int height, int x, int y, int count,
                                             Map<String, String> originalParameters ) {
        // TODO implement for !isSimple()
        try {
            Map<String, String> extraParams = new HashMap<String, String>();
            handleParameters( extraParams, originalParameters, options.defaultParametersGetFeatureInfo,
                              options.hardParametersGetFeatureInfo );
            return client.getFeatureInfo( layerOrder, width, height, x, y, envelope, envelope.getCoordinateSystem(),
                                          count, extraParams );
        } catch ( Throwable e ) {
            LOG.info( "Error when loading features from remote WMS: {}", e.getLocalizedMessage() );
            LOG.trace( "Stack trace", e );
        }
        return null;
    }

    /**
     * @return null, if cascaded WMS does not have a bbox for the layers in EPSG:4326
     */
    public Envelope getEnvelope() {
        Envelope bbox = client.getBoundingBox( "EPSG:4326", layerOrder );
        if ( bbox == null ) {
            bbox = client.getLatLonBoundingBox( layerOrder );
        }
        if ( bbox == null ) {
            bbox = client.getBoundingBox( client.getCoordinateSystems( layerOrder.get( 0 ) ).getFirst(), layerOrder );
            if ( bbox != null ) {
                try {
                    bbox = new GeometryTransformer( "EPSG:4326" ).transform( bbox );
                } catch ( Throwable e ) {
                    LOG.info( "Cannot transform bounding box from {} to EPSG:4326.", bbox.getCoordinateSystem() );
                    LOG.trace( "Stack trace: ", e );
                }
            }
        }
        if ( bbox == null ) {
            LOG.info( "Could not determine bounding box from remote WMS for remote WMS store." );
        }
        return bbox;
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

        public Map<String, String> defaultParametersGetMap = new HashMap<String, String>();

        public Map<String, String> hardParametersGetMap = new HashMap<String, String>();

        public Map<String, String> defaultParametersGetFeatureInfo = new HashMap<String, String>();

        public Map<String, String> hardParametersGetFeatureInfo = new HashMap<String, String>();
    }

    @Override
    public void destroy() {
        client = null; // necessary?
    }

    @Override
    public void init( DeegreeWorkspace workspace )
                            throws ResourceInitException {
        // TODO move construction here
    }

}
