//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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

package org.deegree.services.wms.model.layers;

import static java.util.Arrays.asList;
import static org.deegree.commons.utils.math.MathUtils.round;
import static org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation.OUTER;
import static org.deegree.coverage.raster.interpolation.InterpolationType.BILINEAR;
import static org.deegree.coverage.raster.utils.RasterFactory.rasterDataFromImage;
import static org.deegree.coverage.raster.utils.RasterFactory.rasterDataToImage;
import static org.deegree.cs.coordinatesystems.GeographicCRS.WGS84;
import static org.deegree.services.i18n.Messages.get;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.TreeSet;

import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.coverage.raster.RasterTransformer;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.protocol.wms.Utils;
import org.deegree.protocol.wms.client.WMSClient111;
import org.deegree.protocol.wms.raster.jaxb.WMSDataSourceType;
import org.deegree.rendering.r2d.se.unevaluated.Style;
import org.deegree.services.jaxb.wms.AbstractLayerType;
import org.deegree.services.wms.controller.ops.GetFeatureInfo;
import org.deegree.services.wms.controller.ops.GetMap;
import org.slf4j.Logger;

/**
 * <code>RemoteWMSLayer</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(info = "logs in case of an IO error when sending response", warn = "logs problems with CRS", debug = "logs information about interaction with the remote WMS, errors from remote WMS etc.", trace = "logs details of interaction with remote WMS, stack traces")
public class RemoteWMSLayer extends Layer {

    private static final Logger LOG = getLogger( RemoteWMSLayer.class );

    private WMSClient111 client;

    private LinkedList<String> layers;

    private TreeSet<String> commonSRS;

    private String defaultSRS;

    /**
     * @param layer
     * @param parent
     * @param datasource
     * @param adapter
     * @throws MalformedURLException
     */
    public RemoteWMSLayer( AbstractLayerType layer, Layer parent, WMSDataSourceType datasource, XMLAdapter adapter )
                            throws MalformedURLException {
        super( layer, parent );
        client = new WMSClient111( adapter.resolve( datasource.getCapabilitiesDocumentLocation().getLocation() ) );
        if ( datasource.getCapabilitiesDocumentLocation().getRefreshTime() != -1 ) {
            client.refreshCapabilities();
        }

        layers = new LinkedList<String>( asList( datasource.getRequestedLayers().split( "," ) ) );
        ListIterator<String> i = layers.listIterator();
        while ( i.hasNext() ) {
            i.set( i.next().trim() );
        }

        setBbox( client.getLatLonBoundingBox( layers ) );

        commonSRS = null;
        for ( String l : layers ) {
            if ( commonSRS == null ) {
                commonSRS = new TreeSet<String>( client.getCoordinateSystems( l ) );
            } else {
                commonSRS.retainAll( client.getCoordinateSystems( l ) );
            }
        }
        LOG.debug( "Requestable srs common to all cascaded layers: " + commonSRS );

        defaultSRS = datasource.getDefaultSRS();
        defaultSRS = defaultSRS == null ? "EPSG:4326" : defaultSRS;
        if ( !commonSRS.contains( defaultSRS ) ) {
            defaultSRS = commonSRS.first();
            // try to set the srs to the first advertised srs, as WGS84 is unlikely to be correct
            try {
                Envelope bbox = client.getBoundingBox( defaultSRS, layers );
                GeometryTransformer trans = new GeometryTransformer( WGS84 );
                setBbox( trans.transform( bbox ) );
            } catch ( UnknownCRSException e ) {
                LOG.warn( "The coordinate system " + defaultSRS + " could not be found." );
            } catch ( TransformationException e ) {
                LOG.debug( get( "WMS.CANNOT_TRANSFORM_BBOX_FROM", defaultSRS ) );
            }
        }
    }

    @Override
    public LinkedList<String> paintMap( Graphics2D g, GetMap gm, Style style ) {
        Pair<BufferedImage, LinkedList<String>> pair = paintMap( gm, style );
        if ( pair.first != null ) {
            g.drawImage( pair.first, 0, 0, null );
        }
        return pair.second;
    }

    @Override
    public Pair<BufferedImage, LinkedList<String>> paintMap( GetMap gm, Style style ) {
        CRS origCrs = gm.getCoordinateSystem();
        String origCrsName = origCrs.getName();
        try {

            if ( commonSRS.contains( origCrsName ) ) {
                LOG.trace( "Will request remote layer(s) in " + origCrsName );
                LinkedList<String> errors = new LinkedList<String>();
                Pair<BufferedImage, String> pair = client.getMap( layers, gm.getWidth(), gm.getHeight(),
                                                                  gm.getBoundingBox(), origCrs, gm.getFormat(),
                                                                  gm.getTransparent(), false, 100, true, errors );
                LOG.debug( "Parameters that have been replaced for this request: " + errors );
                if ( pair.first == null ) {
                    LOG.debug( "Error from remote WMS: " + pair.second );
                }
                return new Pair<BufferedImage, LinkedList<String>>( pair.first, new LinkedList<String>() );
            }

            // case: transform the bbox and image
            LOG.trace( "Will request remote layer(s) in " + defaultSRS + " and transform to " + origCrsName );

            GeometryTransformer trans = new GeometryTransformer( defaultSRS );
            Envelope bbox = (Envelope) trans.transform( gm.getBoundingBox(), origCrs.getWrappedCRS() );

            RasterTransformer rtrans = new RasterTransformer( origCrs.getWrappedCRS() );

            int width = gm.getWidth();
            int height = gm.getHeight();
            double scale = Utils.calcScaleWMS111( width, height, gm.getBoundingBox(), null );
            double newScale = Utils.calcScaleWMS111( width, height, bbox, null );
            double ratio = scale / newScale;

            width = round( ratio * width );
            height = round( ratio * height );

            LinkedList<String> errors = new LinkedList<String>();
            Pair<BufferedImage, String> pair = client.getMap( layers, width, height, bbox,
                                                              new CRS( trans.getTargetCRS() ), gm.getFormat(),
                                                              gm.getTransparent(), false, -1, true, errors );

            LOG.debug( "Parameters that have been replaced for this request: " + errors );
            if ( pair.first == null ) {
                LOG.debug( "Error from remote WMS: " + pair.second );
                return null;
            }

            RasterGeoReference env = RasterGeoReference.create( OUTER, bbox, width, height );
            RasterData data = rasterDataFromImage( pair.first );
            SimpleRaster raster = new SimpleRaster( data, bbox, env );

            SimpleRaster transformed = rtrans.transform( raster, gm.getBoundingBox(), gm.getWidth(), gm.getHeight(),
                                                         BILINEAR ).getAsSimpleRaster();

            return new Pair<BufferedImage, LinkedList<String>>( rasterDataToImage( transformed.getRasterData() ),
                                                                new LinkedList<String>() );

        } catch ( IOException e ) {
            LOG.info( get( "WMS.IO_ERROR_REMOTE_WMS", e.getLocalizedMessage() ) );
            LOG.trace( "Stack trace", e );
        } catch ( UnknownCRSException e ) {
            LOG.warn( "Unable to find crs, this is not supposed to happen. Trace: " + e );
        } catch ( TransformationException e ) {
            LOG.warn( "Unable to transform bbox from " + origCrsName + " to " + defaultSRS );
        }

        return null;
    }

    @Override
    public Pair<FeatureCollection, LinkedList<String>> getFeatures( GetFeatureInfo fi, Style style ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FeatureType getFeatureType() {
        return null;
    }

}
