//$HeadURL: svn+ssh://aionita@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.protocol.wpvs.client;

import static org.deegree.commons.utils.ArrayUtils.join;
import static org.deegree.commons.utils.HttpUtils.IMAGE;
import static org.deegree.commons.utils.HttpUtils.XML;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.crs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.primitive.Point;

/**
 * The <code>WPVSClientD3</code> class supports the functionality of sending requests to the deegree3 WPVS
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author: ionita $
 * 
 * @version $Revision: $, $Date: $
 */
public class WPVSClientD3 {

    public enum Requests {
        GetView,
        GetCapabilities,
    }

    private static final NamespaceContext nsContext_d3;

    static {
        nsContext_d3 = new NamespaceContext();
        nsContext_d3.addNamespace( "ows", "http://www.opengis.net/ows/1.1" );
        nsContext_d3.addNamespace( "wpvs", "http://www.opengis.net/wpvs/1.0.0-pre" );
        nsContext_d3.addNamespace( "xlink", "http://www.w3.org/1999/xlink" );
        nsContext_d3.addNamespace( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
    }        

    private XMLAdapter capabilities;

    public WPVSClientD3( URL url ) {
        this( new XMLAdapter( url ) );
    }

    public WPVSClientD3( XMLAdapter capabilities ) {
        this.capabilities = capabilities;
    }
    
    /**
     * 
     * @return all datasets that are queryable
     */
    public List<String> getDatasets() {
        List<String> res = new LinkedList<String>();
        XPath xp = new XPath( "//wpvs:Dataset[@queryable='true']", nsContext_d3 );
        List<OMElement> datasets = capabilities.getElements( capabilities.getRootElement(), xp );
        for ( OMElement node : datasets ) {
            XPath xpName = new XPath( "wpvs:Name", nsContext_d3 );
            String name = capabilities.getNodeAsString( node, xpName, null );
            res.add( name );
        }
        return res;
    }
    
    /**
     * 
     * @return all elevation models defined
     */
    public List<String> getElevationModels() {
        List<String> res = new LinkedList<String>();
        XPath xp = new XPath( "//wpvs:ElevationModel", nsContext_d3 );
        List<OMElement> elModels = capabilities.getElements( capabilities.getRootElement(), xp );
        for ( OMElement node : elModels )
            res.add( node.getText() );
        return res;
    }

    /**
     * 
     * @param request the type of {@link Requests}
     * @return whether the request is defined in the getCapabilities xml file
     */
    public boolean isOperationSupported( Requests request ) {
        XPath xp = new XPath( "//ows:Operation[@name='" + request.name() + "']", nsContext_d3 );
        return capabilities.getElement( capabilities.getRootElement(), xp ) != null;
    }

    /**
     * 
     * @param request the type of {@link Requests}
     * @param get whether it is a Get or a Post request 
     * @return the url of the requested operation
     */
    public String getAddress( Requests request, boolean get ) {
        if ( !isOperationSupported( request ) ) {
            return null;
        }
        String xpathStr = "//ows:Operation[@name=\"" + request.name() + "\"]/ows:DCP/ows:HTTP/"
        + ( get ? "ows:Get" : "ows:Post" ) + "/@xlink:href";
        OMElement root = capabilities.getRootElement();
        String res = capabilities.getNodeAsString( root, new XPath( xpathStr, nsContext_d3 ), null );
        return res;
    }

    /**
     * 
     * @param dataSets
     * @param width
     * @param height
     * @param bbox
     * @param crs
     * @param format
     * @param poi
     * @param pitch
     * @param roll
     * @param yaw
     * @param elevationModel
     * @param distance
     * @param version
     * @param background
     * @param clipPlane
     * @param aov
     * @param scale
     * @return
     */
    public Pair<BufferedImage, String> getView( List<String> dataSets, int width, int height, Envelope bbox, CRS crs,
                                                String format, Point poi, int pitch, int roll, int yaw, 
                                                String elevationModel, int distance, String version, 
                                                String background, int clipPlane, int aov, double scale ) {
        String url = getAddress( Requests.GetView, true );
        url += "service=WPVS&request=GetView";
        url += "&BOUNDINGBOX=" + bbox.getMin().getX() + "," + bbox.getMin().getY() + "," + bbox.getMax().getX() + "," + bbox.getMax().getY();
        url += "&DATASETS=" + join( ",", dataSets );
        url += "&ELEVATIONMODEL=DEM";
        url += "&ROLL=" + roll;
        url += "&AOV=" + aov;
        url += "&FARCLIPPINGPLANE=" + clipPlane;
        url += "&CRS=" + crs.getName();
        url += "&WIDTH=" + width;
        url += "&HEIGHT=" + height;
        url += "&SCALE=" + scale;
        url += "&STYLES=default";
//        url += "&DATETIME=2007-03-21T12:00:00";
//        url += "&EXCEPTIONFORMAT=INIMAGE";
        url += "&VERSION=" + version;
        url += "&OUTPUTFORMAT=" + format;
//        url += "&background=" + background;
        url += "&POI=" + poi.getX() + "," + poi.getY() + "," + poi.getZ();
        url += "&YAW=" + yaw;
        url += "&PITCH=" + pitch;
        url += "&DISTANCE=" + distance;  
        System.out.println( "Generated GetView request: " + url );
        
        Pair<BufferedImage, String> res = new Pair<BufferedImage, String>();
        URL theUrl;
        try {
            theUrl = new URL( url );
            URLConnection conn = theUrl.openConnection();
            conn.connect();
            res.first = IMAGE.work( conn.getInputStream() );
            if ( res.first == null ) {
                conn = theUrl.openConnection();
                res.second = XML.work( conn.getInputStream() ).toString();
            }
        } catch ( MalformedURLException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return res;
    }    

}
