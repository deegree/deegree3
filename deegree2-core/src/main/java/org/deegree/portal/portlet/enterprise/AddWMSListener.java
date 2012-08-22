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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.deegree.enterprise.control.AbstractListener;
import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.ogcbase.BaseURL;
import org.deegree.ogcbase.ImageURL;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.getcapabilities.MetadataURL;
import org.deegree.ogcwebservices.wms.capabilities.LegendURL;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocument;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocumentFactory;
import org.deegree.portal.PortalException;
import org.deegree.portal.context.ContextException;
import org.deegree.portal.context.Format;
import org.deegree.portal.context.FormatList;
import org.deegree.portal.context.Layer;
import org.deegree.portal.context.LayerExtension;
import org.deegree.portal.context.Node;
import org.deegree.portal.context.Server;
import org.deegree.portal.context.Style;
import org.deegree.portal.context.StyleList;
import org.deegree.portal.context.ViewContext;
import org.deegree.portal.context.XMLFactory;
import org.deegree.portal.standard.wms.control.GetWMSLayerListener;

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
public class AddWMSListener extends AbstractListener {

    private static ILogger LOG = LoggerFactory.getLogger( GetWMSLayerListener.class );

    /**
     * @see org.deegree.enterprise.control.WebListener#actionPerformed(org.deegree.enterprise.control.FormEvent)
     */
    @SuppressWarnings("unchecked")
    @Override
    public synchronized void actionPerformed( FormEvent event ) {

        RPCWebEvent rpc = (RPCWebEvent) event;
        try {
            validate( rpc );
        } catch ( Exception e ) {
            gotoErrorPage( "Not a valid RPC for AddWMSListener\n" + e.getMessage() );
        }

        HttpSession session = ( (HttpServletRequest) this.getRequest() ).getSession();
        Enumeration<String> en = (Enumeration<String>)session.getAttributeNames();
        try {
            while ( en.hasMoreElements() ) {
                String key = en.nextElement();
                Object o = session.getAttribute( key );
                synchronized ( session ) {
                    if ( o != null && o instanceof ViewContext ) {
                        appendWMS( rpc, (ViewContext) o );
                    }
                }
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            e.printStackTrace();
        }
    }

    /**
     * appends the selected layers of a WMS to the passed <code>ViewContext</code>
     *
     * @param context
     * @throws ContextException
     * @throws MalformedURLException
     * @throws PortalException
     * @throws InvalidCapabilitiesException
     */
    private void appendWMS( RPCWebEvent rpc, ViewContext context )
                            throws MalformedURLException, ContextException, PortalException,
                            InvalidCapabilitiesException {

        RPCStruct struct = (RPCStruct) rpc.getRPCMethodCall().getParameters()[0].getValue();
        URL url = new URL( (String) struct.getMember( "WMSURL" ).getValue() );
        String name = (String) struct.getMember( "WMSNAME" ).getValue();
        String version = (String) struct.getMember( "WMSVERSION" ).getValue();
        String layers = (String) struct.getMember( "LAYERS" ).getValue();
        String formatName = (String) struct.getMember( "FORMAT" ).getValue();
        boolean useAuthentification = false;
        if ( struct.getMember( "useAuthentification" ) != null ) {
            String tmp = (String) struct.getMember( "useAuthentification" ).getValue();
            useAuthentification = "true".equalsIgnoreCase( tmp );
        }

        List<String> list = StringTools.toList( layers, ";", true );

        WMSCapabilitiesDocument capa = null;
        try {
            StringBuffer sb = new StringBuffer( 500 );
            if ( "1.0.0".equals( version ) ) {
                sb.append( url.toExternalForm() ).append( "?request=capabilities&service=WMS" );
            } else {
                sb.append( url.toExternalForm() ).append( "?request=GetCapabilities&service=WMS" );
            }
            if ( useAuthentification ) {
                HttpSession session = ( (HttpServletRequest) getRequest() ).getSession();
                String user = ( (org.apache.jetspeed.om.security.BaseJetspeedUser) session.getAttribute( "turbine.user" ) ).getUserName();
                String password = ( (org.apache.jetspeed.om.security.BaseJetspeedUser) session.getAttribute( "turbine.user" ) ).getPassword();
                if ( !"anon".equals( user ) ) {
                    sb.append( "&user=" ).append( user );
                    sb.append( "&password=" ).append( password );
                }
            }
            LOG.logDebug( "GetCapabilites for added WMS", sb.toString() );
            capa = WMSCapabilitiesDocumentFactory.getWMSCapabilitiesDocument( new URL( sb.toString() ) );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            String msg = null;
            if ( "1.0.0".equals( version ) ) {
                msg = StringTools.concat( 500, "could not load WMS capabilities from: ",
                                          new URL( url.toExternalForm() + "?request=capabilities&service=WMS" ),
                                          "; reason: ", e.getMessage() );
            } else {
                msg = StringTools.concat( 500, "could not load WMS capabilities from: ",
                                          new URL( url.toExternalForm() + "?request=GetCapabilities&service=WMS" ),
                                          "; reason: ", e.getMessage() );
            }
            throw new PortalException( msg );
        }
        WMSCapabilities capabilities = (WMSCapabilities) capa.parseCapabilities();
        String rootTitle = capabilities.getLayer().getTitle();

        // ----------------------------------------------------------------------------
        // stuff required by layerlist tree view
        Node root = context.getGeneral().getExtension().getLayerTreeRoot();
        // check if Node width this title already exists
        Node[] nodes = root.getNodes();
        int newNodeId = -1;
        for ( int j = 0; j < nodes.length; j++ ) {
            if ( nodes[j].getTitle().equals( rootTitle ) ) {
                newNodeId = nodes[j].getId();
                break;
            }
        }
        if ( newNodeId == -1 ) {
            newNodeId = root.getMaxNodeId() + 1;
            Node newNode = new Node( newNodeId, root, rootTitle, true, false );
            Node[] newNodes = new Node[nodes.length + 1];
            newNodes[0] = newNode;
            for ( int j = 0; j < nodes.length; j++ ) {
                newNodes[j + 1] = nodes[j];
            }

            root.setNodes( newNodes );
        }
        // ----------------------------------------------------------------------------
        for ( int i = 0; i < list.size(); i++ ) {
            String[] lay = StringTools.toArray( list.get( i ), "|", false );
            Server server = new Server( name, version, "OGC:WMS", url, capabilities );
            String srs = context.getGeneral().getBoundingBox()[0].getCoordinateSystem().getPrefixedName();
            Format format = new Format( formatName, true );
            FormatList fl = new FormatList( new Format[] { format } );
            // read available styles from WMS capabilities and add them
            // to the WMC layer
            org.deegree.ogcwebservices.wms.capabilities.Layer wmslay = capabilities.getLayer( lay[0] );
            org.deegree.ogcwebservices.wms.capabilities.Style[] wmsstyles = wmslay.getStyles();
            Style[] styles = null;
            if ( wmsstyles == null || wmsstyles.length == 0 ) {
                // a wms capabilities layer may offeres one or more styles for
                // a layer but it don't have to. But WMC must have at least one
                // style for each layer; So we set a default style in the case
                // a wms does not declares one
                styles = new Style[1];
                styles[0] = new Style( "", "default", "", null, true );
            } else {
                styles = new Style[wmsstyles.length];
                for ( int j = 0; j < styles.length; j++ ) {
                    boolean isDefault = wmsstyles[j].getName().toLowerCase().indexOf( "default" ) > -1
                                        || wmsstyles[j].getName().trim().length() == 0;
                    ImageURL legendURL = null;
                    LegendURL[] lUrl = wmsstyles[j].getLegendURL();
                    if ( lUrl != null && lUrl.length > 0 ) {
                        legendURL = new ImageURL( lUrl[0].getWidth(), lUrl[0].getHeight(), lUrl[0].getFormat(),
                                                  lUrl[0].getOnlineResource() );
                    }
                    styles[j] = new Style( wmsstyles[j].getName(), wmsstyles[j].getTitle(), wmsstyles[j].getAbstract(),
                                           legendURL, isDefault );
                }
            }

            StyleList styleList = new StyleList( styles );
            BaseURL mdUrl = null;
            MetadataURL[] mdUrls = wmslay.getMetadataURL();
            if ( mdUrls != null && mdUrls.length == 1 && mdUrls[0] != null ) {
                mdUrl = mdUrls[0];
            }

            int authentication = LayerExtension.NONE;
            if ( useAuthentification ) {
                authentication = LayerExtension.USERPASSWORD;
            }
            LayerExtension lex = new LayerExtension( null, false, wmslay.getScaleHint().getMin(),
                                                     wmslay.getScaleHint().getMax(), false, authentication, newNodeId,
                                                     false, null );
            Layer newLay = new Layer( server, lay[0], lay[1], null, new String[] { srs }, null, mdUrl, fl, styleList,
                                      wmslay.isQueryable(), false, lex );
            if ( context.getLayerList().getLayer( newLay.getName(), server.getOnlineResource().toExternalForm() ) == null ) {
                context.getLayerList().addLayerToTop( newLay );
            }

        }
        try {
            XMLFragment xml = XMLFactory.export( context );
            System.out.println( xml.getAsPrettyString() );
        } catch ( Exception e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * validates the incomming RPC
     *
     * @param rpc
     */
    private void validate( RPCWebEvent rpc ) {
        // TODO Auto-generated method stub

    }

}
