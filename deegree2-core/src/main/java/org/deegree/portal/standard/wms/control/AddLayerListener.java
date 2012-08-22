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
package org.deegree.portal.standard.wms.control;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.deegree.enterprise.control.ajax.AbstractListener;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.HttpUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.ogcbase.BaseURL;
import org.deegree.ogcbase.ImageURL;
import org.deegree.ogcwebservices.getcapabilities.MetadataURL;
import org.deegree.ogcwebservices.wms.capabilities.LegendURL;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocument;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocumentFactory;
import org.deegree.portal.Constants;
import org.deegree.portal.context.Format;
import org.deegree.portal.context.FormatList;
import org.deegree.portal.context.Layer;
import org.deegree.portal.context.LayerExtension;
import org.deegree.portal.context.LayerGroup;
import org.deegree.portal.context.LayerList;
import org.deegree.portal.context.MMLayer;
import org.deegree.portal.context.MapModel;
import org.deegree.portal.context.Server;
import org.deegree.portal.context.Style;
import org.deegree.portal.context.StyleList;
import org.deegree.portal.context.ViewContext;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class AddLayerListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( AddLayerListener.class );

    /**
     * structure of incoming data:
     * <p>
     * {action=addLayer, layerGroup={bgColor=0xFFFFFF, transparency=true, id=deegree wms,
     * serviceURL=http:*demo.deegree.org/deegree-wms/services, format=image/gif, serviceType=OGC:WMS 1.1.1,
     * serviceName=deegree wms, layers=[{dsResource=null, visible=false, wmsName=deegree wms,dsGeomType=null,
     * changed=true, styleName=default, title=Energy Resources, selected=false, metadataURL=, minScale=0.0,
     * name=EnergyResources, legendURL=null, dsFeatureType=null, maxScale=1000000.0, sldRef=null, queryable=true},
     * {dsResource=null, visible=false, wmsName=deegree wms, dsGeomType=null, changed=true, styleName=default,
     * title=Airports of Utah, selected=false, metadataURL=, minScale=0.0, name=Airports, legendURL=null,
     * dsFeatureType=null, maxScale=1000000.0, sldRef=null, queryable=true}]} }
     * </p>
     */
    @SuppressWarnings("unchecked")
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {
        ViewContext vc = (ViewContext) event.getSession().getAttribute( Constants.CURRENTMAPCONTEXT );
        LayerList layerList = vc.getLayerList();
        MapModel mapModel = vc.getGeneral().getExtension().getMapModel();
        List<String> ids = new ArrayList<String>();
        try {
            Map<String, Object> parameter = event.getParameter();
            Map<String, Object> layerGroup = (Map<String, Object>) parameter.get( "layerGroup" );
            String[] srs = new String[] { (String) parameter.get( "srs" ) };
            String wmsName = (String) layerGroup.get( "id" );
            String serviceType = (String) layerGroup.get( "serviceType" );
            String[] service = StringTools.toArray( serviceType, " ", false );
            String format = (String) layerGroup.get( "format" );
            Format frmt = new Format( format, true );
            FormatList formatList = new FormatList( new Format[] { frmt } );
            List<?> layers = (List<?>) layerGroup.get( "layers" );
            Collections.reverse( layers );
            // read capabilities from WMS where new layers are defined and create Server instance
            // which is the same for all new layers
            String serviceURL = (String) layerGroup.get( "serviceURL" );
            String sessionID = (String) event.getSession().getAttribute( "SESSIONID" );
            WMSCapabilities capabilities = readCapabilities( serviceURL, service[1], sessionID );
            Server server = new Server( wmsName, service[1], service[0], new URL( serviceURL ), capabilities );
            LayerGroup lg = null;
            if ( mapModel != null && (Boolean) parameter.get( "createLayerGroup" ) ) {
                // add a new group for added layers
                lg = addLayerGroup( mapModel, wmsName );
            } else {
                lg = mapModel.getLayerGroups().get( 0 );
            }
            for ( Object object : layers ) {
                Map<String, Object> layer = (Map<String, Object>) object;
                org.deegree.ogcwebservices.wms.capabilities.Layer wmsLayer = capabilities.getLayer( (String) layer.get( "name" ) );
                // if no legend URL is defined try reading it from WMS capabilities
                ImageURL legendURL = null;
                if ( layer.get( "legendURL" ) != null ) {
                    legendURL = new ImageURL( 25, 25, format, new URL( (String) layer.get( "legendURL" ) ) );
                } else {
                    legendURL = readLegendURL( wmsLayer, layer );
                }
                Style style = new Style( (String) layer.get( "styleName" ), (String) layer.get( "styleName" ),
                                         (String) layer.get( "abstract" ), legendURL, true );
                StyleList styleList = new StyleList( new Style[] { style } );

                // if no meta data URL is defined try reading it from WMS capabilities
                BaseURL metadataURL = null;
                String tmp = (String) layer.get( "metadataURL" );
                if ( tmp != null && tmp.length() > 4 ) {
                    metadataURL = new BaseURL( "text/xml", new URL( tmp ) );
                } else {
                    metadataURL = readMetadataURL( wmsLayer );
                }
                LayerExtension extension = new LayerExtension();
                // ensures that each layer part of WMC layer list has one unique identifier
                extension.setIdentifier( UUID.randomUUID().toString() );
                ids.add( extension.getIdentifier() );
                // read scale hints from WMS capabilities
                extension.setMinScaleHint( wmsLayer.getScaleHint().getMin() );
                extension.setMaxScaleHint( wmsLayer.getScaleHint().getMax() );

                Layer lay = new Layer( server, (String) layer.get( "name" ), (String) layer.get( "title" ),
                                       (String) layer.get( "abstract" ), srs, null, metadataURL, formatList, styleList,
                                       (Boolean) layer.get( "queryable" ), !(Boolean) layer.get( "visible" ), extension );
                layerList.addLayer( lay );
                if ( mapModel != null ) {
                    // because a MapModel is optional and only must exist if layer tree module is
                    // included, we have to check availability of a MapModel
                    addToMapModel( lay, mapModel, lg );
                }
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            responseHandler.writeAndClose( "ERROR: could not add layer(s) to map model: "
                                           + StringTools.stackTraceToString( e ) );
        }
        responseHandler.writeAndClose( false, ids );
    }

    private BaseURL readMetadataURL( org.deegree.ogcwebservices.wms.capabilities.Layer wmsLayer ) {
        BaseURL metadataURL = null;
        MetadataURL[] metadataURLs = wmsLayer.getMetadataURL();
        if ( metadataURLs != null && metadataURLs.length > 0 ) {
            metadataURL = new BaseURL( metadataURLs[0].getFormat(), metadataURLs[0].getOnlineResource() );
        }
        return metadataURL;
    }

    private ImageURL readLegendURL( org.deegree.ogcwebservices.wms.capabilities.Layer wmsLayer,
                                    Map<String, Object> layer ) {
        ImageURL legendURL = null;
        org.deegree.ogcwebservices.wms.capabilities.Style wmsStyles[] = wmsLayer.getStyles();
        for ( org.deegree.ogcwebservices.wms.capabilities.Style style : wmsStyles ) {
            if ( style.getName().equalsIgnoreCase( "default" ) || style.getName().toLowerCase().startsWith( "default:" ) ) {
                LegendURL[] legendURLs = style.getLegendURL();
                if ( legendURLs != null && legendURLs.length > 0 ) {
                    legendURL = new ImageURL( legendURLs[0].getWidth(), legendURLs[0].getHeight(),
                                              legendURLs[0].getFormat(), legendURLs[0].getOnlineResource() );
                    break;
                }
            }
        }
        return legendURL;
    }

    private LayerGroup addLayerGroup( MapModel mapModel, String wmsName )
                            throws Exception {
        LayerGroup lg = new LayerGroup( UUID.randomUUID().toString(), wmsName, false, true, null, mapModel );
        // add to root layer group
        mapModel.insert( lg, mapModel.getLayerGroups().get( 0 ), null, true );
        return lg;
    }

    /**
     * update {@link MapModel} with new layer by inserting it at first position
     * 
     * @param lay
     * @param mapModel
     * @throws Exception
     */
    private void addToMapModel( Layer lay, MapModel mapModel, LayerGroup parent )
                            throws Exception {
        MMLayer layer = new MMLayer( lay.getExtension().getIdentifier(), null, mapModel, lay );
        mapModel.insert( layer, parent, null, true );
    }

    /**
     * @param serviceURL
     * @param version
     * @param sessionID
     * @return WMS capabilities
     * @throws Exception
     */
    private WMSCapabilities readCapabilities( String serviceURL, String version, String sessionID )
                            throws Exception {
        String s = "service=WMS&request=GetCapabilities";
        if ( version != null ) {
            s += ( "&version=" + version );
        }
        if ( sessionID != null ) {
            s += ( "&sessionID=" + sessionID );
        }
        LOG.logDebug( "service URL: ", serviceURL );
        LOG.logDebug( "request: ", s );
        XMLFragment xml = new XMLFragment();
        InputStream is = HttpUtils.performHttpGet( serviceURL, s, timeout, null, null, null ).getResponseBodyAsStream();
        xml.load( is, serviceURL );
        WMSCapabilitiesDocument doc = WMSCapabilitiesDocumentFactory.getWMSCapabilitiesDocument( xml.getRootElement() );
        return (WMSCapabilities) doc.parseCapabilities();
    }

}
