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
package org.deegree.portal.standard.wms.control;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCMember;
import org.deegree.enterprise.control.RPCMethodCall;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.KVP2Map;
import org.deegree.framework.util.StringTools;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Position;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OWSUtils;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.deegree.portal.context.GeneralExtension;
import org.deegree.portal.context.IOSettings;
import org.deegree.portal.context.LayerExtension;
import org.deegree.portal.context.ViewContext;

/**
 * This class is for highlighting polygons via a RPC request event from the client. A new WMS request will be created
 * with SLD. The SLD xml file will be created and saved. The WMS request for highlighting will be set.
 * 
 * <p>
 * ------------------------------------------------------------------------
 * </p>
 * 
 * @author <a href="mailto:k.lupp@web.de>Katharina Lupp</a>
 * @author last edited by: $Author: mays$
 * 
 * @version $Revision$, $Date: 27.05.2008 17:13:56$
 */
public class HighlightingListener extends AbstractMapListener {

    private static final ILogger LOG = LoggerFactory.getLogger( HighlightingListener.class );

    // private String sldUserStyle = "highlightingSLDUserStyle.xml";
    private String sldUserStyle = System.currentTimeMillis() + ".xml";

    /**
     * This method will be called by the <tt>MapListener</tt> if a highlighting action/event occurs.
     */
    @Override
    public void actionPerformed( FormEvent event ) {

        // default actions
        super.actionPerformed( event );
        RPCWebEvent rpc = (RPCWebEvent) event;
        RPCMethodCall mc = rpc.getRPCMethodCall();
        RPCParameter[] para = mc.getParameters();
        RPCStruct struct = (RPCStruct) para[0].getValue();

        HttpSession session = ( (HttpServletRequest) this.getRequest() ).getSession( true );
        ViewContext vc = (ViewContext) session.getAttribute( "DefaultMapContext" );

        try {
            GetMap wmsRequest = createRequest( para, vc );
            Position[] coords = getCoordinates( struct );
            String queryLayer = getQueryLayer( struct );
            Envelope env = wmsRequest.getBoundingBox();
            StringBuffer sld = createSLD( coords, env, queryLayer, vc );
            saveSLD( sld, vc );
            highlight( queryLayer, wmsRequest, vc );
        } catch ( Exception ex ) {
            LOG.logError( "Highlighting was not successful: ", ex );
        }

    }

    /**
     * creates the modified WMS Request including the URL for the SLD.
     */
    private GetMap createRequest( RPCParameter[] para, ViewContext vc ) {

        GeneralExtension ge = vc.getGeneral().getExtension();
        IOSettings ios = ge.getIOSettings();
        URL onlineResource = ios.getSLDDirectory().getOnlineResource();

        RPCStruct struct = (RPCStruct) para[1].getValue();
        RPCMember[] member = struct.getMembers();
        String request = (String) member[0].getValue();
        Map<String, String> getMR = KVP2Map.toMap( request );
        getMR.put( "ID", "id" );
        getMR.put( "FORMAT", "image/gif" );
        getMR.put( "TRANSPARENT", "true" );
        getMR.put( "SLD", onlineResource.toExternalForm() + "/" + sldUserStyle );
        GetMap modifiedRequest = null;
        try {
            modifiedRequest = GetMap.create( getMR );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return modifiedRequest;
    }

    /**
     * calculates the coordinates of the click event.
     */
    private Position[] getCoordinates( RPCStruct struct ) {

        String x = (String) struct.getMember( "X" ).getValue();
        String y = (String) struct.getMember( "Y" ).getValue();
        String[] tmpX = StringTools.toArray( x, ",", false );
        String[] tmpY = StringTools.toArray( y, ",", false );

        Position[] pos = new Position[tmpX.length];
        for ( int i = 0; i < tmpX.length; i++ ) {
            double xtmp = Double.parseDouble( tmpX[i] );
            double ytmp = Double.parseDouble( tmpY[i] );
            pos[i] = GeometryFactory.createPosition( xtmp, ytmp );
        }

        return pos;
    }

    /**
     * gets the layer to be highlighted.
     */
    private String getQueryLayer( RPCStruct struct ) {
        RPCMember queryLayers = struct.getMember( "queryLayers" );
        String queryLay = (String) queryLayers.getValue();
        int index = queryLay.indexOf( "|" );
        queryLay = queryLay.substring( 0, index );

        return queryLay;
    }

    /**
     * sets the request for highlighting.
     */
    private void highlight( String queryLayer, GetMap gmr, ViewContext vc )
                            throws OGCWebServiceException {

        URL wmsURL = vc.getLayerList().getLayer( queryLayer, null ).getServer().getOnlineResource();

        String s = OWSUtils.validateHTTPGetBaseURL( wmsURL.toExternalForm() );
        this.getRequest().setAttribute( "HighlightingRequest", s + gmr.getRequestParameter() );

    }

    /**
     * creates the SLD xml file.
     */
    private StringBuffer createSLD( Position[] coords, Envelope env, String queryLay, ViewContext vc ) {
        GeneralExtension ge = vc.getGeneral().getExtension();
        IOSettings ios = ge.getIOSettings();
        URL onlineResource = ios.getSLDDirectory().getOnlineResource();
        LayerExtension le = vc.getLayerList().getLayer( queryLay, null ).getExtension();

        WFSCapabilities capa = (WFSCapabilities) le.getDataService().getServer().getCapabilities();
        URL wfsurl = OWSUtils.getHTTPGetOperationURL( capa, GetFeature.class );

        StringBuffer sb = new StringBuffer( 10000 );

        sb.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
        sb.append( "<StyledLayerDescriptor version=\"1.0.0\" xmlns=\"http://www.opengis.net/sld\" " );
        sb.append( "xmlns:gml=\"http://www.opengis.net/gml\" xmlns:wfs=\"http://www.opengis.net/wfs\" " );
        sb.append( "xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" " );
        sb.append( "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" );
        sb.append( "<UserLayer><Name>MyLayer</Name><RemoteOWS>" );
        sb.append( "<Service>WFS</Service>" );
        sb.append( "<OnlineResource xmlns:xlink='http://www.w3.org/1999/xlink' xlink:type='simple' " );
        sb.append( "xlink:href='" + wfsurl.toExternalForm() + "'/>" );
        sb.append( "</RemoteOWS><LayerFeatureConstraints><FeatureTypeConstraint>" );
        sb.append( "<FeatureTypeName>" + queryLay + "</FeatureTypeName>" );
        sb.append( "<ogc:Filter><ogc:And>" );
        if ( coords.length > 1 )
            sb.append( "<ogc:Or>" );
        for ( int k = 0; k < coords.length; k++ ) {
            sb.append( "<ogc:Intersects><ogc:PropertyName>GEOM</ogc:PropertyName>" );
            sb.append( "<gml:Point><gml:coordinates>" + coords[k].getX() + "," );
            sb.append( coords[k].getY() + "</gml:coordinates></gml:Point>" );
            sb.append( "</ogc:Intersects>" );
        }
        if ( coords.length > 1 )
            sb.append( "</ogc:Or>" );
        sb.append( "<ogc:BBOX><ogc:PropertyName>GEOM</ogc:PropertyName>" );
        sb.append( "<gml:Box><gml:coord><gml:X>" + env.getMin().getX() );
        sb.append( "</gml:X><gml:Y>" + env.getMin().getY() + "</gml:Y>" );
        sb.append( "</gml:coord><gml:coord><gml:X>" + env.getMax().getX() );
        sb.append( "</gml:X><gml:Y>" + env.getMax().getY() + "</gml:Y>" );
        sb.append( "</gml:coord></gml:Box></ogc:BBOX></ogc:And>" );
        sb.append( "</ogc:Filter></FeatureTypeConstraint></LayerFeatureConstraints> " );

        sb.append( "<UserStyle><FeatureTypeStyle><Rule><MinScaleDenominator>0" );
        sb.append( "</MinScaleDenominator><MaxScaleDenominator>999999999" );
        sb.append( "</MaxScaleDenominator><PolygonSymbolizer><Fill><GraphicFill>" );
        sb.append( "<Graphic><ExternalGraphic><OnlineResource xmlns:xlink=" );
        sb.append( "'http://www.w3.org/1999/xlink' xlink:type='simple' xlink:href='" );
        sb.append( onlineResource.toExternalForm()  + "/pattern.gif' />" );
        sb.append( "<Format>image/gif</Format></ExternalGraphic></Graphic></GraphicFill>" );
        sb.append( "</Fill><Stroke><CssParameter name=\"stroke\">#FF0000</CssParameter>" );
        sb.append( "<CssParameter name=\"stroke-opacity\">1.0</CssParameter>" );
        sb.append( "<CssParameter name=\"stroke-width\">1.0</CssParameter>" );
        sb.append( "<CssParameter name=\"stroke-dasharray\">1</CssParameter>" );
        sb.append( "</Stroke></PolygonSymbolizer></Rule></FeatureTypeStyle>" );
        sb.append( "</UserStyle></UserLayer></StyledLayerDescriptor>" );

        return sb;
    }

    /**
     * saves the created SLD file.
     */
    private void saveSLD( StringBuffer sld, ViewContext vc )
                            throws IOException {
        GeneralExtension ge = vc.getGeneral().getExtension();
        IOSettings ios = ge.getIOSettings();
        String dir = ios.getSLDDirectory().getDirectoryName();
        OutputStreamWriter writer = new OutputStreamWriter( new FileOutputStream( dir + "/" + sldUserStyle ), "UTF-8" );
        writer.write( sld.toString() );
        writer.close();
    }

}
