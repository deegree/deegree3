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
package org.deegree.portal.portlet.enterprise;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.KVP2Map;
import org.deegree.framework.util.StringPair;
import org.deegree.framework.util.StringTools;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;
import org.deegree.portal.PortalUtils;
import org.deegree.portal.context.ContextException;
import org.deegree.portal.context.Layer;
import org.deegree.portal.context.LayerList;
import org.deegree.portal.context.Node;
import org.deegree.portal.context.ViewContext;
import org.deegree.portal.portlet.modules.actions.IGeoPortalPortletPerform;

/**
 * The servlet will be used to update a map model (Web Map Context) in the background (using an
 * invisible iframe) from an iGeoPortal Portlet Edition
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
public class WMCUpdateServlet extends HttpServlet {

    private static final long serialVersionUID = 2927537039728672671L;

    private static final ILogger LOG = LoggerFactory.getLogger( WMCUpdateServlet.class );

    @Override
    public void init()
                            throws ServletException {
        super.init();
    }

    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response )
                            throws ServletException, IOException {

        doPost( request, response );
    }

    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response )
                            throws ServletException, IOException {

        Map<String, String> parameter = KVP2Map.toMap( request );

        String mm = parameter.get( "MAPPORTLET" );
        String nodeValue = parameter.get( "NODE" );
        String action = parameter.get( "ACTION" );

        LOG.logDebug( "parameter: " + parameter );

        IGeoPortalPortletPerform igeo = new IGeoPortalPortletPerform( request, null, getServletContext() );

        try {
            // Handle ajax/iframe requests.
            // All arguments are passed via a parameter map, see the handelXxxXxx
            // static methods for the accepted parameters.

            // The parameter NODE identifies the affected node/layer:
            // nodes are passed as 'node-xx' where xx is the number/id
            // layers are passed as 'layername|serveraddress'
            if ( action != null && mm != null && nodeValue != null ) {
                ViewContext vc = igeo.getCurrentViewContext( mm );

                // send parts of a layer tree
                if ( action.equalsIgnoreCase( "TREEDATA" ) ) {
                    // writes into response stream
                    handleTreeData( parameter, vc, response );
                    return;
                }

                // handle actions on nodes/layers
                boolean repaintMap = true;
                if ( action.equalsIgnoreCase( "MOVENODE" ) ) {
                    repaintMap = handleMoveNode( parameter, vc );
                } else if ( action.equalsIgnoreCase( "ADDNODE" ) ) {
                    String nextNodeID = handleAddNode( parameter, vc );
                    request.setAttribute( "NEXTNODEID", nextNodeID );
                    repaintMap = false;
                } else if ( action.equalsIgnoreCase( "REMOVENODE" ) ) {
                    handleRemoveNode( parameter, vc );
                } else if ( action.equalsIgnoreCase( "RENAMENODE" ) ) {
                    handleRenameNode( parameter, vc );
                    repaintMap = false;
                } else if ( action.equalsIgnoreCase( "SETVISIBILITY" ) ) {
                    handleSetVisibility( parameter, vc );
                }
                if ( repaintMap ) {
                    // if the visible layers changed,
                    // send new baserequests to client ( see igeoportal/enterprise.jsp)
                    request.setAttribute( "UPDATE_BASEREQUESTS", true );
                    request.setAttribute( "VIEWCONTEXT", vc );
                }
            } else if ( mm != null ) {
                // update bbox
                ViewContext vc = igeo.getCurrentViewContext( mm );

                String bbox = parameter.get( IGeoPortalPortletPerform.PARAM_BBOX );

                if ( bbox != null && vc != null ) {
                    double[] coords = StringTools.toArrayDouble( bbox, "," );
                    CoordinateSystem crs = vc.getGeneral().getBoundingBox()[0].getCoordinateSystem();
                    Point[] pt = new Point[2];
                    pt[0] = GeometryFactory.createPoint( coords[0], coords[1], crs );
                    pt[1] = GeometryFactory.createPoint( coords[2], coords[3], crs );
                    try {
                        vc.getGeneral().setBoundingBox( pt );
                    } catch ( ContextException should_never_happen ) {
                        //nottin
                    }
                    List<Envelope> history = (List<Envelope>) request.getSession().getAttribute(
                                                                                                 IGeoPortalPortletPerform.SESSION_HISTORY );
                    int p = (Integer) request.getSession().getAttribute(
                                                                         IGeoPortalPortletPerform.SESSION_HISTORYPOSITION );
                    Envelope current = history.get( p );
                    Envelope env = GeometryFactory.createEnvelope( coords[0], coords[1], coords[2], coords[3], null );
                    if ( current == null || !current.equals( env ) ) {
                        p++;
                        history.add( p, env );
                        request.getSession().setAttribute( IGeoPortalPortletPerform.SESSION_HISTORYPOSITION, p );
                    }
                }

                if ( bbox != null && parameter.get( "LAYERS" ) != null ) {
                    // just change layerlist if the request contains a BBOX parameter
                    // and at least one layer because other wise it will be the initial call;
                    Layer[] layers = vc.getLayerList().getLayers();
                    String ly =  parameter.get( "LAYERS" );
                    StringBuffer sb = new StringBuffer( 100 );
                    for ( int i = 0; i < layers.length; i++ ) {
                        sb.append( layers[i].getName() ).append( '|' );
                        sb.append( layers[i].getServer().getOnlineResource() );
                        if ( ly.indexOf( sb.toString() ) > -1 ) {
                            layers[i].setHidden( false );
                        } else {
                            layers[i].setHidden( true );
                        }
                        sb.delete( 0, sb.length() );
                    }
                    igeo.setCurrentMapContext( vc, mm );
                }
            } else {
                System.out.println( "no mapmodel defined in request; ensure that parameter 'MAPPORTLET' is defined!" );
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            LOG.logError( "could not update WMC: " + e.getMessage(), e );
        }
        request.setAttribute( "ACTION", getInitParameter( "ACTION" ) );

        request.getRequestDispatcher( "igeoportal/enterprise.jsp" ).forward( request, response );
    }

    /**
     * moves a node/layer to a new position used parameters: NODE, BEFORENODE, PARENTNODE
     *
     * @return true if moved layer/node was visible and a repaint is necessary
     */
    private static boolean handleMoveNode( Map<String, String> parameter, ViewContext vc ) {
        int nodeID = getNodeID( parameter, "NODE" );
        boolean visibleLayer;
        if ( nodeID != -1 ) { // move a node
            int beforeNodeID = getNodeID( parameter, "BEFORENODE" );
            int parentNodeID = getNodeID( parameter, "PARENTNODE" );
            vc.getLayerList().moveNodes( nodeID, beforeNodeID );
            Node node = vc.getGeneral().getExtension().getLayerTreeRoot().getNode( nodeID );
            Node parent;
            if ( parentNodeID == -1 ) {
                parent = vc.getGeneral().getExtension().getLayerTreeRoot();
            } else {
                parent = vc.getGeneral().getExtension().getLayerTreeRoot().getNode( parentNodeID );
            }
            node.getParent().removeNode( nodeID );
            node.setParent( parent );
            int beforeNodeIndex = parent.getIndex( beforeNodeID );
            if ( beforeNodeIndex == -1 ) {
                parent.appendNode( node );
            } else {
                parent.insertNode( node, beforeNodeIndex );
            }
            visibleLayer = PortalUtils.hasNodeVisibleLayers( vc, nodeID );
        } else { // move a layer with layer/servername
            StringPair layerName = getLayer( parameter, "NODE" );
            StringPair beforeLayerName = getLayer( parameter, "BEFORENODE" );
            int parentNodeId = getNodeID( parameter, "PARENTNODE" );
            Layer layer = vc.getLayerList().getLayer( layerName.first, layerName.second );
            layer.getExtension().setParentNodeId( parentNodeId );
            Layer beforeLayer = null;
            if ( beforeLayerName.second != "" ) {
                beforeLayer = vc.getLayerList().getLayer( beforeLayerName.first, beforeLayerName.second );
            }
            vc.getLayerList().move( layer, beforeLayer );
            visibleLayer = !layer.isHidden();
        }
        return visibleLayer;
    }

    /**
     * rename a node used parameters: NODE and NODETITLE
     */
    private static void handleRenameNode( Map<String, String> parameter, ViewContext vc )
                            throws ContextException {
        int nodeID = getNodeID( parameter, "NODE" );
        String title = parameter.get( "NODETITLE" );
        if ( title != null ) {
            if ( nodeID != -1 ) {
                vc.getGeneral().getExtension().getLayerTreeRoot().getNode( nodeID ).setTitle( title );
            }
        }
    }

    /**
     * removes a layer/node. when a node is removed, all child nodes/layers will be removed too used
     * parameters: NODE
     */
    private static void handleRemoveNode( Map<String, String> parameter, ViewContext vc ) {
        int nodeID = getNodeID( parameter, "NODE" );
        if ( nodeID != -1 ) {
            vc.getLayerList().removeLayers( nodeID );
            vc.getGeneral().getExtension().getLayerTreeRoot().removeNode( nodeID );
        } else {
            StringPair layer = getLayer( parameter, "NODE" );
            vc.getLayerList().removeLayer( layer.first, layer.second );
        }
    }

    /**
     * adds a new node to the context used parameters: NODE, NODETITLE, PARENTNODE
     *
     * @return id for the next new node
     */
    private static String handleAddNode( Map<String, String> parameter, ViewContext vc )
                            throws ContextException {
        int nodeID = getNodeID( parameter, "NODE" );
        int parentNodeID = getNodeID( parameter, "PARENTNODE" );
        String nodeTitle = parameter.get( "NODETITLE" );
        if ( nodeTitle == null ) {
            nodeTitle = "";
        }
        Node root = vc.getGeneral().getExtension().getLayerTreeRoot();
        if ( parentNodeID != -1 ) {
            root = root.getNode( parentNodeID );
        }
        Node newNode = new Node( nodeID, root, nodeTitle, true, true );
        root.insertNode( newNode, 0 );
        String nextNodeID = "node-" + ( root.getMaxNodeId() + 1 );
        return nextNodeID;
    }

    /**
     * sets visibility for layers or complete nodes used parameters: HIDDEN (boolean) and NODE
     */
    private static void handleSetVisibility( Map<String, String> parameter, ViewContext vc ) {
        boolean hidden = Boolean.parseBoolean( parameter.get( "HIDDEN" ) );
        int nodeID = getNodeID( parameter, "NODE" );
        if ( nodeID != -1 ) { // change complete node
            PortalUtils.setVisibility( vc, nodeID, hidden );
        } else { // change single layer
            StringPair layerName = getLayer( parameter, "NODE" );
            Layer layer = vc.getLayerList().getLayer( layerName.first, layerName.second );
            layer.setHidden( hidden );
        }
    }

    /**
     * sends layer/node data for requested node as JSON to response stream used parameters: NODE
     */
    private static void handleTreeData( Map<String, String> parameter, ViewContext vc, HttpServletResponse response )
                            throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append( '[' );

        LayerList layerList = vc.getLayerList();
        String nodeValue = parameter.get( "NODE" );
        // root node
        if ( nodeValue.equals( "/" ) ) {
            // return all nodes for root
            Node[] nodes = vc.getGeneral().getExtension().getLayerTreeRoot().getNodes();
            for ( Node n : nodes ) {
                boolean hidden = !PortalUtils.hasNodeVisibleLayers( vc, n.getId() );
                sb.append( String.format( "{'text': '%s', 'iconCls': 'folder', 'id':'node-%s', "
                                          + "'leaf': false, 'checked': %s, 'expanded': true},", n.getTitle(),
                                          n.getId(), hidden ? "false" : "true" ) );
            }
        } else {
            int nodeID = getNodeID( parameter, "NODE" );
            if ( nodeID != -1 ) {
                // first add the nodes...
                Node[] nodes = vc.getGeneral().getExtension().getLayerTreeRoot().getNode( nodeID ).getNodes();
                for ( Node n : nodes ) {
                    boolean hidden = !PortalUtils.hasNodeVisibleLayers( vc, n.getId() );
                    sb.append( String.format( "{'text': '%s', 'iconCls': 'folder', 'id':'node-%s', "
                                              + "'leaf': false, 'checked': %s},", n.getTitle(), n.getId(),
                                              hidden ? "false" : "true" ) );
                }
                // ...then all layers for this node
                for ( Layer layer : layerList.getLayersByNodeId( nodeID ) ) {
                    if ( layer.getServer().getCapabilities() != null ) {
                        URL s = null;
                        if ( layer.getStyleList().getCurrentStyle().getLegendURL() != null ) {
                            s = layer.getStyleList().getCurrentStyle().getLegendURL().getOnlineResource();
                        }
                        sb.append( String.format(
                                                  "{'text': '%s', 'id':'%s', 'leaf': true, 'checked': %s, 'img': '%s'},",
                                                  layer.getTitle(), layer.getName() + '|'
                                                                    + layer.getServer().getOnlineResource(),
                                                  layer.isHidden() ? "false" : "true", s ) );
                    }
                }
            }
        }
        sb.append( ']' );
        String fixedDemoOutput = sb.toString();
        response.setContentType( "application/json" );
        response.setCharacterEncoding( "utf-8" );
        response.getWriter().write( fixedDemoOutput );
    }

    /**
     * extracts the numeric node id from the node id sting (eg. (String)node-42 -> (int)42)
     *
     * @param parameter
     *            the parameter map
     * @param paramName
     *            key of the parameter
     * @return the node id or -1, if not found
     */
    private static int getNodeID( Map<String, String> parameter, String paramName ) {
        String paramValue = parameter.get( paramName.toUpperCase() );
        int nodeID = -1;
        if ( paramValue != null && paramValue.startsWith( "node-" ) ) {
            nodeID = Integer.parseInt( paramValue.substring( paramValue.lastIndexOf( '-' ) + 1 ) );
        }
        return nodeID;
    }

    /**
     * extracts the layer and servername from a node id
     *
     * @param parameter
     *            the parameter map
     * @param paramName
     *            key of the parameter
     * @return the layer (first) and servername (second)
     */
    private static StringPair getLayer( Map<String, String> parameter, String paramName ) {
        String paramValue = parameter.get( paramName.toUpperCase() );
        StringPair result = new StringPair();
        if ( paramValue != null ) {
            String[] values = paramValue.split( "\\|", 2 );
            result.first = values[0];
            if ( values.length == 2 ) {
                result.second = values[1];
            } else {
                result.second = "";
            }
        }
        return result;
    }

}
