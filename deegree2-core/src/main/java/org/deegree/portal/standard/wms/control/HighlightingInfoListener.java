// $HeadURL$
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
package org.deegree.portal.standard.wms.control;

import java.io.InputStreamReader;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCMember;
import org.deegree.enterprise.control.RPCMethodCall;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.NetWorker;
import org.deegree.framework.util.StringTools;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Position;
import org.deegree.portal.PortalException;
import org.deegree.portal.context.LayerExtension;
import org.deegree.portal.context.ViewContext;

/**
 * This class is for accessing informations about the highlighted polygons A new WFS GetFeature request will be created
 * and performed
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class HighlightingInfoListener extends AbstractMapListener {

    /**
     *
     * @param event
     */
    @Override
    public void actionPerformed( FormEvent event ) {

        // default actions
        super.actionPerformed( event );
        RPCWebEvent rpc = (RPCWebEvent) event;
        RPCMethodCall mc = rpc.getRPCMethodCall();
        RPCParameter[] para = mc.getParameters();
        RPCStruct struct = (RPCStruct) para[0].getValue();

        FeatureCollection fc = null;
        try {
            // get boundingbox of the request
            RPCMember member = struct.getMember( "boundingBox" );
            String tmp = (String) member.getValue();
            double[] box = StringTools.toArrayDouble( tmp, "," );
            // get coordinates for filtering from the request
            Position[] coords = getCoordinates( struct );
            // get layers/featuretypes to query
            String[] queryLayers = getQueryLayers( struct );
            // create WFS GetFeature request
            String request = createRequest( queryLayers, coords, box );
            // get responsible WFS URL
            URL url = getResponsibleWFS( queryLayers[0] );
            // get FeatureCollection from WFS
            fc = performGetFeature( request, url );
        } catch ( Exception ex ) {
            gotoErrorPage( "Invalid WCSCapabilityOperations: " + ex.toString() );
            ex.printStackTrace();

            return;
        }

        this.getRequest().setAttribute( "HIGHLIGHTINFO", fc );

    }

    /**
     * gets the layer to be highlighted.
     */
    private String[] getQueryLayers( RPCStruct struct ) {
        RPCMember mem = struct.getMember( "queryLayers" );
        String tmp = (String) mem.getValue();
        String[] queryLayers = StringTools.toArray( tmp, ",", false );
        for ( int i = 0; i < queryLayers.length; i++ ) {
            int index = queryLayers[i].indexOf( "|" );
            queryLayers[i] = queryLayers[i].substring( 0, index );
        }
        return queryLayers;
    }

    /**
     * returns the URL of the WFS that is responsible for accessing the data of the passed layer/featuretype
     *
     * @param queryLayer
     *            layer to determine the responsible WFS for data access
     */
    private URL getResponsibleWFS( String queryLayer )
                            throws PortalException {

        HttpSession session = ( (HttpServletRequest) this.getRequest() ).getSession( true );
        ViewContext vc = (ViewContext) session.getAttribute( "DefaultMapContext" );
        if ( vc.getLayerList().getLayer( queryLayer, null ) == null ) {
            throw new PortalException( "'" + queryLayer + "' is not known by the client!" );
        }
        LayerExtension le = vc.getLayerList().getLayer( queryLayer, null ).getExtension();
        URL wfsurl = null;
        if ( le.getDataService() == null ) {
            throw new PortalException( "no WFS registered in MapContext for requested layer. "
                                       + "Please contact your responsible administrator." );
        }
        if ( !le.getDataService().getServer().getService().equals( "ogc:WFS" ) ) {
            throw new PortalException( "The responsible services isn't a ogc:WFS; no "
                                       + "detail informations are available! " );
        }
        wfsurl = le.getDataService().getServer().getOnlineResource();

        return wfsurl;
    }

    /**
     * calculates the coordinates of the click event.
     */
    private Position[] getCoordinates( RPCStruct struct ) {

        String xs = (String) struct.getMember( "x" ).getValue();
        String ys = (String) struct.getMember( "y" ).getValue();
        double[] x = StringTools.toArrayDouble( xs, "," );
        double[] y = StringTools.toArrayDouble( ys, "," );

        Position[] pos = new Position[x.length];

        for ( int i = 0; i < pos.length; i++ ) {
            pos[i] = GeometryFactory.createPosition( x[i], y[i] );
        }

        return pos;
    }

    /**
     * creates a WFS GetFeature request from the passed layers (feature types) and the coordinates. The least are used
     * to create the filter conditions.
     *
     * @param queryLayers
     *            names of the layers/featuretypes that will be targeted by the request
     * @param coords
     *            coordinates to be used as filter conditions (intersect)
     * @param box
     *            relevant bounding box
     */
    private String createRequest( String[] queryLayers, Position[] coords, double[] box ) {

        StringBuffer sb = new StringBuffer( 5000 );
        sb.append( "<?xml version='1.0' encoding='UTF-8'?>" );
        sb.append( "<GetFeature xmlns='http://www.opengis.net/wfs' " );
        sb.append( "xmlns:ogc='http://www.opengis.net/ogc' " );
        sb.append( "xmlns:gml='http://www.opengis.net/gml' " );
        sb.append( "service='WFS' version='1.0.0' outputFormat='GML2'>" );
        sb.append( "<Query typeName='" + queryLayers[0] + "'>" );

        sb.append( "<ogc:Filter><ogc:And>" );
        if ( coords.length > 1 )
            sb.append( "<ogc:Or>" );

        for ( int k = 0; k < coords.length; k++ ) {
            sb.append( "<ogc:Intersects><ogc:PropertyName>GEOM</ogc:PropertyName>" );
            sb.append( "<gml:Point><gml:coordinates>" ).append( coords[k].getX() ).append( ',' );
            sb.append( coords[k].getY() ).append( "</gml:coordinates>" );
            sb.append( "</gml:Point></ogc:Intersects>" );
        }

        if ( coords.length > 1 )
            sb.append( "</ogc:Or>" );

        sb.append( "<ogc:BBOX><ogc:PropertyName>GEOM</ogc:PropertyName>" );
        sb.append( "<gml:Box><gml:coordinates>" ).append( box[0] ).append( ',' ).append( box[1] );
        sb.append( ' ' ).append( box[2] ).append( ',' ).append( box[3] );
        sb.append( "</gml:coordinates>" ).append( "</gml:Box></ogc:BBOX>" );
        sb.append( "</ogc:And></ogc:Filter></Query></GetFeature>" );

        return sb.toString();
    }

    /**
     * performs a GetFeature request against the responsible WFS
     */
    private FeatureCollection performGetFeature( String request, URL wfsURL )
                            throws PortalException {

        FeatureCollection fc = null;
        try {
            NetWorker nw = new NetWorker( CharsetUtils.getSystemCharset(), wfsURL, request );
            InputStreamReader isr = new InputStreamReader( nw.getInputStream(), CharsetUtils.getSystemCharset() );
            GMLFeatureCollectionDocument doc = new GMLFeatureCollectionDocument();
            doc.load( isr, wfsURL.toString() );
            fc = doc.parse();
        } catch ( Exception e ) {
            throw new PortalException( "couldn't perform GetFeature request", e );
        }

        return fc;
    }

}
