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
package org.deegree.portal;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.model.spatialschema.Point;
import org.deegree.ogcwebservices.OWSUtils;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.deegree.portal.context.Format;
import org.deegree.portal.context.GeneralExtension;
import org.deegree.portal.context.Layer;
import org.deegree.portal.context.LayerExtension;
import org.deegree.portal.context.LayerList;
import org.deegree.portal.context.Node;
import org.deegree.portal.context.ViewContext;

/**
 *
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class PortalUtils {

    private static final ILogger LOG = LoggerFactory.getLogger( PortalUtils.class );

    /**
     * returns a comma seperated list of layers marked as sensible for feature info requestes
     *
     * @param vc
     * @return a comma seperated list of layers marked as sensible for feature info requestes
     */
    public static List<String> getFeatureInfoLayers( ViewContext vc ) {
        List<String> list = new ArrayList<String>();
        LayerList layerList = vc.getLayerList();
        Layer[] layers = layerList.getLayers();
        for ( int i = 0; i < layers.length; i++ ) {
            if ( layers[i].getExtension() != null && layers[i].getExtension().isSelectedForQuery() ) {
                list.add( layers[i].getName() );
            }
        }
        return list;
    }

    /**
     * returns a comma separated list of visible layers
     *
     * @param vc
     * @return a comma separated list of visible layers
     */
    public static List<String[]> getVisibleLayers( ViewContext vc ) {
        List<String[]> list = new ArrayList<String[]>();
        LayerList layerList = vc.getLayerList();
        Layer[] layers = layerList.getLayers();
        for ( int i = 0; i < layers.length; i++ ) {
            if ( !layers[i].isHidden() ) {
                String[] s = new String[2];
                s[0] = layers[i].getName();
                s[1] = layers[i].getServer().getOnlineResource().toExternalForm();
                list.add( s );
            }
        }
        return list;
    }

    /**
     * returns true if at least one layer of the passed server is visible
     *
     * @param vc
     * @param serverTitle
     * @return <code>true</code> if at least one layer of the passed server is visible
     */
    public static boolean hasServerVisibleLayers( ViewContext vc, String serverTitle ) {
        LayerList layerList = vc.getLayerList();
        Layer[] layers = layerList.getLayers();
        for ( int i = 0; i < layers.length; i++ ) {
            if ( layers[i].getServer().getTitle().equals( serverTitle ) && !layers[i].isHidden() ) {
                return true;
            }
        }
        return false;
    }

    /**
     * returns true if at least one layer within the node is visible
     *
     * @param vc
     * @param nodeID
     * @return <code>true</code> if at least one layer of the passed server is visible
     */
    public static boolean hasNodeVisibleLayers( ViewContext vc, int nodeID ) {
        Layer[] layers = vc.getLayerList().getLayersByNodeId( nodeID );
        for ( Layer layer : layers ) {
            if ( !layer.isHidden() ) {
                return true;
            }
        }
        Node[] nodes = vc.getGeneral().getExtension().getLayerTreeRoot().getNode( nodeID ).getNodes();
        for ( Node node : nodes ) {
            if ( hasNodeVisibleLayers( vc, node.getId() ) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * set visibility for a node and all child nodes/layers
     *
     * @param vc
     * @param nodeID
     * @param hidden
     */
    public static void setVisibility( ViewContext vc, int nodeID, boolean hidden ) {
        Layer[] layers = vc.getLayerList().getLayersByNodeId( nodeID );
        for ( Layer layer : layers ) {
            layer.setHidden( hidden );
        }
        Node[] nodes = vc.getGeneral().getExtension().getLayerTreeRoot().getNode( nodeID ).getNodes();
        for ( Node node : nodes ) {
            setVisibility( vc, node.getId(), hidden );
        }
    }

    /**
     * creates the GetMap basic requests required by the JSP page assigned to the MapViewPortlet.
     *
     * @param vc
     * @param user
     * @param password
     * @param sessionID
     * @return list of GetMap requests
     */
    public static String[] createBaseRequests( ViewContext vc, String user, String password, String sessionID ) {

        Layer[] layers = vc.getLayerList().getLayers();
        List<String> list = new ArrayList<String>( layers.length );
        int i = layers.length - 1;
        try {
            while ( i >= 0 ) {
                GeneralExtension gExt = vc.getGeneral().getExtension();
                Point[] bbox = vc.getGeneral().getBoundingBox();
                StringBuffer sb = new StringBuffer( 1000 );
                URL url = OWSUtils.getHTTPGetOperationURL( layers[i].getServer().getCapabilities(), GetMap.class );
                if ( url != null ) {
                    String href = url.toExternalForm();

                    sb.append( OWSUtils.validateHTTPGetBaseURL( href ) );
                    sb.append( "SRS=" ).append( bbox[0].getCoordinateSystem().getIdentifier() );
                    if ( "1.0.0".equals( layers[i].getServer().getVersion() ) ) {
                        sb.append( "&REQUEST=map" );
                        sb.append( "&exceptions=INIMAGE" );
                        sb.append( "&WMTVER=" ).append( layers[i].getServer().getVersion() );
                    } else {
                        sb.append( "&REQUEST=GetMap" );
                        sb.append( "&exceptions=application/vnd.ogc.se_inimage" );
                        sb.append( "&VERSION=" ).append( layers[i].getServer().getVersion() );
                    }
                    sb.append( "&transparent=" ).append( true );
                    if ( gExt == null ) {
                        sb.append( "&BGCOLOR=0xFFFFFF" );
                    } else {
                        sb.append( "&BGCOLOR=" ).append( gExt.getBgColor() );
                    }

                    Format format = layers[i].getFormatList().getCurrentFormat();
                    sb.append( "&FORMAT=" ).append( format.getName() );
                    StringBuffer styles = new StringBuffer( 1000 );
                    styles.append( "&STYLES=" );
                    StringBuffer lyrs = new StringBuffer( 1000 );
                    lyrs.append( "&LAYERS=" );
                    String title = layers[i].getServer().getOnlineResource().toExternalForm();
                    int authentication = -1;
                    while ( i >= 0 && title.equals( layers[i].getServer().getOnlineResource().toExternalForm() ) ) {
                        if ( !layers[i].isHidden() ) {
                            if ( authentication < 0 ) {
                                // just evaluate if not has already been loaded
                                authentication = layers[i].getExtension().getAuthentication();
                            }
                            lyrs.append( URLEncoder.encode( layers[i].getName(), "UTF-8" ) ).append( ',' );
                            String style = layers[i].getStyleList().getCurrentStyle().getName();
                            if ( style.equalsIgnoreCase( "DEFAULT" ) ) {
                                style = "";
                            }
                            styles.append( style ).append( ',' );
                        }
                        LayerExtension le = layers[i].getExtension();
                        Iterator<String> iterator = le.getVendorspecificParameterNames();
                        while ( iterator.hasNext() ) {
                            sb.append( '&' );
                            String name = iterator.next();
                            String value = le.getVendorspecificParameter( name );
                            sb.append( name ).append( '=' ).append( value );
                        }
                        i--;
                    }
                    if ( authentication == LayerExtension.USERPASSWORD ) {
                        if ( user != null ) {
                            sb.append( "&user=" ).append( URLEncoder.encode( user, CharsetUtils.getSystemCharset() ) );
                        }
                        if ( password != null ) {
                            sb.append( "&password=" );
                            sb.append( URLEncoder.encode( password, CharsetUtils.getSystemCharset() ) );
                        }
                    } else if ( authentication == LayerExtension.SESSIONID ) {
                        if ( sessionID != null ) {
                            sb.append( "&sessionID=" );
                            sb.append( URLEncoder.encode( sessionID, CharsetUtils.getSystemCharset() ) );
                        }
                    }
                    String s1 = lyrs.substring( 0, lyrs.length() - 1 );
                    String s2 = styles.substring( 0, styles.length() - 1 );
                    sb.append( s1 ).append( s2 );

                    if ( s1.length() > 7 ) {
                        // ensure that a request will just be created if
                        // at least one layer of a group is visible
                        list.add( sb.toString() );
                    }
                } else {
                    LOG.logWarning( "no service available for layer: " + layers[i--].getName() );
                }

            }
        } catch ( Exception shouldneverhappen ) {
            shouldneverhappen.printStackTrace();
        }

        return list.toArray( new String[list.size()] );

    }

    /**
     * creates the GetMap basic requests required by the JSP page assigned to the MapViewPortlet.
     *
     * @param vc
     * @return the GetMap basic requests
     */
    public static String[] createBaseRequests( ViewContext vc ) {

        return createBaseRequests( vc, null, null, null );

    }

}
