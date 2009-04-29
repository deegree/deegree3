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
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.crs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryFactoryCreator;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.standard.primitive.DefaultPoint;

/**
 * <code>WPVSClient</code> currently issues getView requests to the WPVS from deegree2. Prior to running
 * this utility, have deegree-wpvs set up in Apache Tomcat ( which here is set to use port 8080).
 * You can download the deegree2 version of WPVS from the deegree download page: 
 * {@link http://www.deegree.org/deegree/portal/media-type/html/user/anon/page/default.psml/js_pane/download}
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author: ionita $
 * 
 * @version $Revision: $, $Date: $
 * 
 */
public class WPVSClient {

    public enum Requests {
        GetView,
        GetCapabilities,
    }

    private static final NamespaceContext nsContext;

    static {
        nsContext = new NamespaceContext();
        nsContext.addNamespace( "ows", "http://www.opengis.net/ows" );
        nsContext.addNamespace( "wpvs", "http://www.opengis.net/wpvs" );
        nsContext.addNamespace( "", "http://www.opengis.net/wpvs" );
        nsContext.addNamespace( "gml", "http://www.opengis.net/gml" );
        nsContext.addNamespace( "ogc", "http://www.opengis.net/ogc" );
        nsContext.addNamespace( "ows", "http://www.opengis.net/ows" );
        nsContext.addNamespace( "wfs", "http://www.opengis.net/wfs" );
        nsContext.addNamespace( "xlink", "http://www.w3.org/1999/xlink" );
        nsContext.addNamespace( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );        
    }

    private XMLAdapter capabilities;

    public WPVSClient( URL url ) {
        this( new XMLAdapter( url ) );
    }

    public WPVSClient( XMLAdapter capabilities ) {
        this.capabilities = capabilities;
    }
    
    /**
     * Get all datasets that are queryable
     */
    public List<String> getDatasets() {
        List<String> res = new LinkedList<String>();
        XPath xp = new XPath( "//wpvs:Dataset[@queryable=\"1\"]", nsContext );
        List<OMElement> datasets = capabilities.getElements( capabilities.getRootElement(), xp );
        for ( OMElement node : datasets ) {
            XPath xpName = new XPath( "wpvs:Name", nsContext );
            String name = capabilities.getNodeAsString( node, xpName, null );
            res.add( name );
        }
        return res;
    }
    
    public List<String> getElevationModels() {
        List<String> res = new LinkedList<String>();
        XPath xp = new XPath( "//wpvs:ElevationModel", nsContext );
        List<OMElement> elModels = capabilities.getElements( capabilities.getRootElement(), xp );
        for ( OMElement node : elModels )
            res.add( node.getText() );
        return res;
    }

    public boolean isOperationSupported( Requests request ) {
        XPath xp = new XPath( "//ows:Operation[@name='" + request.name() + "']", nsContext );
        return capabilities.getElement( capabilities.getRootElement(), xp ) != null;
    }

    public String getAddress( Requests request, boolean get ) {
        if ( !isOperationSupported( request ) ) {
            return null;
        }
        String xpathStr = "//ows:Operation[@name=\"" + request.name() + "\"]/ows:DCP/ows:HTTP/"
        + ( get ? "ows:Get" : "ows:Post" ) + "/@xlink:href";
        OMElement root = capabilities.getRootElement();
        String res = capabilities.getNodeAsString( root, new XPath( xpathStr, nsContext ), null );
        return res;
    }

    public Pair<BufferedImage, String> getView( List<String> dataSets, int width, int height, Envelope bbox, CRS crs,
                                                String format, Point poi, int pitch, int roll, int yaw, 
                                                String elevationModel, int distance, String version, 
                                                String background, int clipPlane, int aov, double scale ) {
        String url = getAddress( Requests.GetView, true );
        url += "?request=GetView";
        url += "&BOUNDINGBOX=" + bbox.getMin().getX() + "," + bbox.getMin().getY() + "," + bbox.getMax().getX() + "," + bbox.getMax().getY();
        url += "&DATASETS=" + join( ",", dataSets );
        url += "&ELEVATIONMODEL=" + elevationModel;
        url += "&ROLL=" + roll;
        url += "&AOV=" + aov;
        url += "&FARCLIPPINGPLANE=" + clipPlane;
        url += "&CRS=" + crs.getName();
        url += "&WIDTH=" + width;
        url += "&HEIGHT=" + height;
        url += "&SCALE=" + scale;
        url += "&STYLES=default";
        url += "&DATETIME=2007-03-21T12:00:00";
        url += "&EXCEPTIONFORMAT=INIMAGE";
        url += "&SPLITTER=QUAD";
        url += "&VERSION=" + version;
        url += "&OUTPUTFORMAT=" + format;
        url += "&background=" + background;
        url += "&POI=" + poi.getX() + "," + poi.getY() + "," + poi.getZ();
        url += "&YAW=" + yaw;
        url += "&PITCH=" + pitch;
        url += "&DISTANCE=" + distance;  

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

    public static void main( String args[] ) throws IOException {
        WPVSClient client = new WPVSClient( new URL( "http://localhost:8080/deegree-wpvs/services?REQUEST=GetCapabilities&version=1.0&service=WPVS" ) );
        GeometryFactory geomFac = GeometryFactoryCreator.getInstance().getGeometryFactory();
        Envelope bbox = geomFac.createEnvelope( 423750, 4512700, 425500, 4513900, new CRS( "EPSG:26912" ) );
        Point poi = new DefaultPoint( "poi", new CRS( "EPSG:26912" ), new double[] { 424750.0, 4513400.0, 50 } );
        Pair<BufferedImage, String> response = client.getView( client.getDatasets(), 
                                                               1200/*maxWidth:1200*/, 1000/*maxHeight:1000*/, 
                                                               bbox, new CRS( "EPSG:26912" ),
                                                               "image/jpg", poi, 35, 10/*not supported*/, 45, 
                                                               client.getElevationModels().get( 0 ), 4000, 
                                                               "1.0.0", "sunset", 10000, 70, 1.0 );
        ImageIO.write( response.first, "jpg", new File( "/tmp/out.jpg" ) );
    }

}
