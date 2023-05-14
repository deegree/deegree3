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
package org.deegree.services.wps.provider.jrxml.contentprovider.map;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.wms.client.WMSClient;
import org.deegree.protocol.wms.ops.GetMap;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.provider.jrxml.jaxb.map.Layer;
import org.deegree.services.wps.provider.jrxml.jaxb.map.WMSDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.media.jai.codec.MemoryCacheSeekableStream;
import com.sun.media.jai.codec.SeekableStream;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * 
 */
public class WMSOrderedDatasource extends OrderedDatasource<WMSDatasource> {

    private static final Logger LOG = LoggerFactory.getLogger( WMSOrderedDatasource.class );

    List<Layer> layers;

    public WMSOrderedDatasource( WMSDatasource datasource, List<Layer> layers ) {
        super( datasource );
        this.layers = layers;
    }

    public WMSOrderedDatasource( WMSDatasource datasource, List<Layer> layers, int min, int max ) {
        super( datasource, min, max );
        this.layers = layers;
    }

    @Override
    BufferedImage getImage( int width, int height, Envelope bbox )
                            throws ProcessletException {
        LOG.debug( "create map image for WMS datasource '{}'", datasource.getName() );
        try {
            String user = datasource.getAuthentification() != null ? datasource.getAuthentification().getUser() : null;
            String passw = datasource.getAuthentification() != null ? datasource.getAuthentification().getPassword()
                                                                   : null;
            // TODO: version
            if ( !"1.1.1".equals( datasource.getVersion() ) ) {
                throw new ProcessletException( "WMS version " + datasource.getVersion()
                                               + " is not yet supported. Supported values are: 1.1.1" );
            }
            String capUrl = datasource.getUrl() + "?request=GetCapabilities&service=WMS&version=1.1.1";
            WMSClient wmsClient = new WMSClient( new URL( capUrl ), 5, 60, user, passw );
            List<Pair<String, String>> layerToStyle = new ArrayList<Pair<String, String>>();
            for ( Layer l : layers ) {
                String style = l.getStyle() != null ? l.getStyle().getNamedStyle() : null;
                layerToStyle.add( new Pair<String, String>( l.getName(), style ) );
            }
            GetMap gm = new GetMap( layerToStyle, width, height, bbox, "image/png", true );
            Pair<BufferedImage, String> map = wmsClient.getMap( gm, null, 60, false );
            if ( map.first == null )
                throw new ProcessletException( "Could not get map from datasource " + datasource.getName() + ": "
                                               + map.second );
            return map.first;
        } catch ( MalformedURLException e ) {
            String msg = "could not resolve wms url " + datasource.getUrl() + "!";
            LOG.error( msg, e );
            throw new ProcessletException( msg );
        } catch ( Exception e ) {
            String msg = "could not get map: " + e.getMessage();
            LOG.error( msg, e );
            throw new ProcessletException( msg );
        }
    }

    private BufferedImage loadImage( String request )
                            throws IOException {
        LOG.debug( "try to load image from request " + request );
        InputStream is = null;
        SeekableStream fss = null;
        try {
            URLConnection conn = new URL( request ).openConnection();
            is = conn.getInputStream();

            if ( LOG.isDebugEnabled() ) {
                File logFile = File.createTempFile( "loadedImg", "response" );
                OutputStream logStream = new FileOutputStream( logFile );

                byte[] buffer = new byte[1024];
                int read = 0;
                while ( ( read = is.read( buffer ) ) != -1 ) {
                    logStream.write( buffer, 0, read );
                }
                logStream.close();

                is = new FileInputStream( logFile );
                LOG.debug( "response can be found at " + logFile );
            }

            try {
                fss = new MemoryCacheSeekableStream( is );
                RenderedOp ro = JAI.create( "stream", fss );
                return ro.getAsBufferedImage();
            } catch ( Exception e ) {
                LOG.info( "could not load image!" );
                return null;
            }
        } finally {
            IOUtils.closeQuietly( fss );
            IOUtils.closeQuietly( is );
        }
    }

    @Override
    Map<String, List<String>> getLayerList() {
        HashMap<String, List<String>> layerList = new HashMap<String, List<String>>();
        ArrayList<String> layerNames = new ArrayList<String>( layers.size() );
        for ( Layer layer : layers ) {
            layerNames.add( layer.getTitle() != null ? layer.getTitle() : layer.getName() );
        }
        layerList.put( datasource.getName(), layerNames );
        return layerList;
    }

    @Override
    public List<Pair<String, BufferedImage>> getLegends( int width )
                            throws ProcessletException {
        List<Pair<String, BufferedImage>> legends = new ArrayList<Pair<String, BufferedImage>>();
        for ( Layer layer : layers ) {
            String layerName = layer.getName();
            try {
                BufferedImage bi = null;
                org.deegree.style.se.unevaluated.Style style = getStyle( layer.getStyle() );
                if ( style != null ) {
                    bi = getLegendImg( style, width );
                } else {
                    String legendUrl = getLegendUrl( layer );
                    LOG.debug( "Try to load legend image from WMS {}: ", legendUrl );
                    try {
                        bi = loadImage( legendUrl );
                    } catch ( IOException e ) {
                        String msg = "Could not create legend for layer: " + layerName;
                        LOG.error( msg );
                        throw new ProcessletException( msg );
                    }
                }
                legends.add( new Pair<String, BufferedImage>( layerName, bi ) );
            } catch ( Exception e ) {
                String dsName = datasource.getName() != null ? datasource.getName() : datasource.getUrl();
                LOG.info( "Could not create legend image for datasource '" + dsName + "', layer " + layerName + ".", e );
            }
        }
        return legends;
    }

    String getLegendUrl( Layer layer ) {
        String url = datasource.getUrl();
        StringBuilder sb = new StringBuilder();
        sb.append( url );
        if ( !url.endsWith( "?" ) )
            sb.append( '?' );
        sb.append( "REQUEST=GetLegendGraphic&" );
        sb.append( "SERVICE=WMS&" );
        sb.append( "VERSION=" ).append( datasource.getVersion() ).append( '&' );
        sb.append( "LAYER=" ).append( layer.getName() ).append( '&' );
        sb.append( "TRANSPARENT=" ).append( "TRUE" ).append( '&' );
        sb.append( "FORMAT=" ).append( "image/png" ).append( '&' );
        if ( layer.getStyle() != null && layer.getStyle().getNamedStyle() != null ) {
            sb.append( "STYLE=" ).append( layer.getStyle().getNamedStyle() );
        }
        return sb.toString();
    }

}
