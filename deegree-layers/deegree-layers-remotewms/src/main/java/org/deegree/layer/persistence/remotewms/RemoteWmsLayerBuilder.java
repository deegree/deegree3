//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.layer.persistence.remotewms;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.deegree.commons.ows.metadata.Description;
import org.deegree.commons.ows.metadata.DescriptionConverter;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.geometry.metadata.SpatialMetadataConverter;
import org.deegree.layer.Layer;
import org.deegree.layer.config.ConfigUtils;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.layer.persistence.remotewms.jaxb.LayerType;
import org.deegree.layer.persistence.remotewms.jaxb.RemoteWMSLayers;
import org.deegree.layer.persistence.remotewms.jaxb.RequestOptionsType;
import org.deegree.protocol.wms.client.WMSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds remote wms layers from jaxb beans.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
class RemoteWmsLayerBuilder {

    private static final Logger LOG = LoggerFactory.getLogger( RemoteWmsLayerBuilder.class );

    private WMSClient client;

    private RemoteWMSLayers cfg;

    RemoteWmsLayerBuilder( WMSClient client, RemoteWMSLayers cfg ) {
        this.client = client;
        this.cfg = cfg;
    }

    Map<String, Layer> buildLayerMap() {
        Map<String, LayerMetadata> configured = collectConfiguredLayers();
        if ( configured.isEmpty() )
            return parseAllRemoteLayers();
        return collectConfiguredRemoteLayers( configured );
    }

    private Map<String, Layer> parseAllRemoteLayers() {
        Map<String, Layer> map = new LinkedHashMap<String, Layer>();

        RequestOptionsType opts = cfg.getRequestOptions();
        List<LayerMetadata> layers = client.getLayerTree().flattenDepthFirst();
        for ( LayerMetadata md : layers ) {
            if ( md.getName() != null ) {
                map.put( md.getName(), new RemoteWMSLayer( md.getName(), md, client, opts ) );
            }
        }
        return map;
    }

    private Map<String, Layer> collectConfiguredRemoteLayers( Map<String, LayerMetadata> configured ) {
        Map<String, Layer> map = new LinkedHashMap<String, Layer>();
        RequestOptionsType opts = cfg.getRequestOptions();
        List<LayerMetadata> layers = client.getLayerTree().flattenDepthFirst();
        for ( LayerMetadata md : layers ) {
            String name = md.getName();
            LayerMetadata confMd = configured.get( name );
            if ( confMd != null ) {
                confMd.merge( md );
                confMd.setStyles( md.getStyles() );
                confMd.setLegendStyles( md.getLegendStyles() );
                map.put( confMd.getName(), new RemoteWMSLayer( name, confMd, client, opts ) );
            }
        }
        return map;
    }

    private Map<String, LayerMetadata> collectConfiguredLayers() {
        Map<String, LayerMetadata> configured = new HashMap<String, LayerMetadata>();
        if ( cfg.getLayer() != null ) {
            for ( LayerType l : cfg.getLayer() ) {
                if ( !client.hasLayer( l.getOriginalName() ) ) {
                    LOG.warn( "Layer {} is not offered by the remote WMS.", l.getOriginalName() );
                    continue;
                }
                String name = l.getName();
                SpatialMetadata smd = SpatialMetadataConverter.fromJaxb( l.getEnvelope(), l.getCRS() );
                Description desc = null;
                if ( l.getDescription() != null ) {
                    desc = DescriptionConverter.fromJaxb( l.getDescription().getTitle(),
                                                          l.getDescription().getAbstract(),
                                                          l.getDescription().getKeywords() );
                }
                LayerMetadata md = new LayerMetadata( name, desc, smd );
                md.setMapOptions( ConfigUtils.parseLayerOptions( l.getLayerOptions() ) );
                configured.put( l.getOriginalName(), md );
            }
        }
        return configured;
    }

}