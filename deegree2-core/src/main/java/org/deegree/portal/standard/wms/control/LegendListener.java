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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

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
import org.deegree.framework.util.IDGenerator;
import org.deegree.framework.util.ImageUtils;
import org.deegree.ogcwebservices.InconsistentRequestException;
import org.deegree.ogcwebservices.wms.operation.GetLegendGraphic;
import org.deegree.portal.context.GeneralExtension;
import org.deegree.portal.context.IOSettings;
import org.deegree.portal.context.ViewContext;

/**
 * will be called if the client forces a dynamic legend.
 * 
 * 
 * @author <a href="mailto:lupp@lat-lon.de">Katharina Lupp</a>
 * @version $Revision$ $Date$
 */
public class LegendListener extends AbstractMapListener {

    private static final ILogger LOG = LoggerFactory.getLogger( LegendListener.class );

    /**
     * the method will be called if a zoomout action/event occurs.
     */
    public void actionPerformed( FormEvent event ) {

        super.actionPerformed( event );

        RPCWebEvent rpc = (RPCWebEvent) event;
        RPCMethodCall mc = rpc.getRPCMethodCall();
        RPCParameter[] para = mc.getParameters();
        RPCStruct struct = (RPCStruct) para[0].getValue();
        HttpSession session = ( (HttpServletRequest) this.getRequest() ).getSession( true );
        ViewContext vc = (ViewContext) session.getAttribute( "DefaultMapContext" );

        Map<String, String>[] model = createWMSRequestModel( struct );

        try {
            GetLegendGraphic legendParam = getLegendRequestParameter();
            Map<String, Object> symbols = setLegend( legendParam, model );
            Rectangle rect = calcLegendSize( symbols );
            BufferedImage bi = new BufferedImage( rect.width + 30, rect.height + 50, BufferedImage.TYPE_INT_RGB );
            bi = drawSymbolsToBI( symbols, bi );
            saveImage( vc, bi );
        } catch ( Exception e ) {
            LOG.logError( "Error occurred in PrintListener: ", e );
        }

    }

    private Rectangle calcLegendSize( Map<String, Object> map ) {

        String[] layers = (String[]) map.get( "NAMES" );
        BufferedImage[] legs = (BufferedImage[]) map.get( "IMAGES" );

        int w = 0;
        int h = 0;
        for ( int i = 0; i < layers.length; i++ ) {
            h += legs[i].getHeight() + 6;
            Graphics g = legs[i].getGraphics();
            Rectangle2D rect = g.getFontMetrics().getStringBounds( layers[i], g );
            g.dispose();
            if ( rect.getWidth() > w ) {
                w = (int) rect.getWidth();
            }
        }
        w += 50;

        return new Rectangle( w, h );
    }

    private BufferedImage drawSymbolsToBI( Map<String, Object> map, BufferedImage bi ) {

        Graphics g = bi.getGraphics();
        g.setColor( Color.WHITE );
        g.fillRect( 1, 1, bi.getWidth() - 2, bi.getHeight() - 2 );

        String[] layers = (String[]) map.get( "NAMES" );
        BufferedImage[] legs = (BufferedImage[]) map.get( "IMAGES" );
        int h = 5;
        for ( int i = layers.length - 1; i >= 0; i-- ) {
            g.drawImage( legs[i], 20, h, null );
            g.setColor( Color.BLACK );
            if ( legs[i].getHeight() < 50 ) {
                g.drawString( layers[i], 30 + legs[i].getWidth(), h + (int) ( legs[i].getHeight() / 1.2 ) );
            }
            h += legs[i].getHeight() + 5;
        }
        g.dispose();
        return bi;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String>[] createWMSRequestModel( RPCStruct struct ) {

        RPCMember[] member = struct.getMembers();

        Map<String, String>[] getMR = new HashMap[member.length];
        for ( int i = 0; i < member.length; i++ ) {
            String request = (String) member[i].getValue();
            getMR[i] = toMap( request );
            StringTokenizer st = new StringTokenizer( request, "?" );
            getMR[i].put( "URL", st.nextToken() );
        }
        return getMR;
    }

    private void saveImage( ViewContext vc, BufferedImage bg ) {

        GeneralExtension ge = vc.getGeneral().getExtension();
        IOSettings ios = ge.getIOSettings();
        String dir = ios.getPrintDirectory().getDirectoryName();
        String format = "jpeg";
        long l = IDGenerator.getInstance().generateUniqueID();
        String file = "legend" + l + '.' + format;
        try {
            FileOutputStream fos = new FileOutputStream( dir + "/" + file );

            ImageUtils.saveImage( bg, fos, format, 1 );

            fos.close();
        } catch ( Exception e ) {
            LOG.logError( "Error occurred in saving legend image: ", e );
        }
        int pos = dir.lastIndexOf( '/' );
        String access = "./" + dir.substring( pos + 1, dir.length() ) + "/" + file;
        this.getRequest().setAttribute( "DYNLEGENDIMAGE", access );

    }

    private GetLegendGraphic getLegendRequestParameter()
                            throws InconsistentRequestException {

        HashMap<String, String> legend = toMap( "VERSION=1.1.1&REQUEST=GetLegendGraphic&FORMAT=image/jpeg&WIDTH=50&HEIGHT=50&"
                                                + "EXCEPTIONS=application/vnd.ogc.se_inimage&LAYER=europe:major_rivers&STYLE=default&"
                                                + "SLD=file:///styles.xml" );
        legend.put( "ID", "1" );
        return GetLegendGraphic.create( legend );

    }

    /**
     * creates legend
     */
    private Map<String, Object> setLegend( GetLegendGraphic glr, Map<String, String>[] model )
                            throws MalformedURLException, IOException {

        ArrayList<String> list1 = new ArrayList<String>();
        ArrayList<BufferedImage> list2 = new ArrayList<BufferedImage>();

        StringTokenizer st = null;
        String format = glr.getFormat();
        if ( format.equals( "image/jpg" ) )
            format = "image/jpeg";
        String legendURL = "";
        int lgHeight = 0;
        for ( int i = 0; i < model.length; i++ ) {

            String style = (String) model[i].get( "STYLE" );
            if ( style != null ) {
                st = new StringTokenizer( style, "," );
                style = st.nextToken();
            } else
                style = "default";
            st = new StringTokenizer( (String) model[i].get( "LAYERS" ), "," );

            while ( st.hasMoreTokens() ) {
                String layer = st.nextToken();
                legendURL = setLegendURL( layer, style, format, glr, model[0] );
                lgHeight = lgHeight + 30;
                BufferedImage legendGraphic = ImageUtils.loadImage( new URL( legendURL ) );
                list1.add( layer );
                list2.add( legendGraphic );
            }
        }

        String[] layers = list1.toArray( new String[list1.size()] );
        BufferedImage[] legs = list2.toArray( new BufferedImage[list2.size()] );
        Map<String, Object> map = new HashMap<String, Object>();
        map.put( "NAMES", layers );
        map.put( "IMAGES", legs );
        return map;
    }

    private String setLegendURL( String layer, String style, String format, GetLegendGraphic glr,
                                 Map<String, String> model ) {

        StringBuffer sb = new StringBuffer( 500 );
        sb.append( model.get( "URL" ) ).append( '?' );
        sb.append( "&VERSION=" ).append( glr.getVersion() );
        sb.append( "&REQUEST=GetLegendGraphic" );
        sb.append( "&FORMAT=" ).append( format );
        sb.append( "&WIDTH=15" );
        sb.append( "&HEIGHT=15&EXCEPTIONS=application/vnd.ogc.se_inimage" );
        sb.append( "&LAYER=" ).append( layer );
        sb.append( "&STYLE=" ).append( style );

        return sb.toString();
    }

}
