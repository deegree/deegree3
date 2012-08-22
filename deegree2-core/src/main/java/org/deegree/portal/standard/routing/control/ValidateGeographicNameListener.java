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
package org.deegree.portal.standard.routing.control;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpMethod;
import org.deegree.enterprise.control.ajax.AbstractListener;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.HttpUtils;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.portal.Constants;
import org.deegree.portal.cataloguemanager.model.ExceptionBean;
import org.deegree.portal.context.ViewContext;
import org.w3c.dom.Node;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ValidateGeographicNameListener extends AbstractListener {

    private static ILogger LOG = LoggerFactory.getLogger( ValidateGeographicNameListener.class );

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.enterprise.control.ajax.AbstractListener#actionPerformed(org.deegree.enterprise.control.ajax.WebEvent
     * , org.deegree.enterprise.control.ajax.ResponseHandler)
     */
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {

        // 1.read out request and configuration parameters

        // address of the nominatim web application
        String address = getInitParameter( "address" );
        // location name we will send to nominatim
        String queryString = (String) event.getParameter().get( "QUERYSTRING" );
        // we use the bounding box of the current map to limit nominatim search area
        // the will make searches faster and reduce number of useless matches
        String searchBox = null;
        try {
            searchBox = getSearchBox();
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            ExceptionBean eb = new ExceptionBean( getClass().getName(), e.getMessage() );
            responseHandler.writeAndClose( true, eb );
            return;
        }

        // 2.create a q request against Nominatim gazetteer service
        queryString = "q=" + URLEncoder.encode( queryString, "UTF-8" )
                      + "&bounded=1&osm_type=N&format=xml&limit=50&viewbox=" + searchBox;
        LOG.logDebug( "Nominatim search query: ", address + "?" + queryString );

        // 3.perform request against Nominatim gazetteer service
        HttpMethod method = HttpUtils.performHttpGet( address, queryString, 60000, null, null, null );

        // 4.receive the result and parse it as an XML document
        InputStreamReader isr = new InputStreamReader( method.getResponseBodyAsStream(), "UTF-8" );
        XMLFragment xml = new XMLFragment();
        try {
            xml.load( isr, address );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            ExceptionBean eb = new ExceptionBean( getClass().getName(), e.getMessage() );
            responseHandler.writeAndClose( true, eb );
            return;
        } finally {
            isr.close();
        }

        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( xml.getAsPrettyString() );
        }

        // 5. parse the content of the result document and create response to the client
        //
        // extract required information from the nominatim result XML. This is:
        // - names of the locations (we possibly have more than one matching location)
        // - bounding box of the locations
        // Form the bounding box we can calculate its center that will be used for
        // displaying the location and calculating a route
        List<String[]> result = new ArrayList<String[]>( 50 );
        HttpSession session = ( (HttpServletRequest) getRequest() ).getSession( true );
        ViewContext vc = (ViewContext) session.getAttribute( Constants.CURRENTMAPCONTEXT );
        CoordinateSystem modelCrs = vc.getGeneral().getBoundingBox()[0].getCoordinateSystem();
        GeoTransformer gt = new GeoTransformer( modelCrs );
        try {
            NamespaceContext nsc = CommonNamespaces.getNamespaceContext();
            List<Node> nodes = XMLTools.getNodes( xml.getRootElement(), "place", nsc );
            for ( Node node : nodes ) {
                String place = XMLTools.getNodeAsString( node, "@display_name", nsc, "" );
                // get location of an object in geographic and map CRS
                double lat = XMLTools.getNodeAsDouble( node, "@lat", nsc, 0 );
                double lon = XMLTools.getNodeAsDouble( node, "@lon", nsc, 0 );
                // location coordinates mst be available in:
                // - WGS84 for route calculation
                // - in CRS of current map model for displaying in the client
                Point point = GeometryFactory.createPoint( lon, lat, CRSFactory.EPSG_4326 );
                Point pointMap = point;
                if ( !modelCrs.equals( CRSFactory.EPSG_4326 ) ) {
                    pointMap = (Point) gt.transform( point );
                }
                String p = point.getX() + " " + point.getY();
                String pm = pointMap.getX() + " " + pointMap.getY();
                // add location to the result array
                result.add( new String[] { place, p, pm } );
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            ExceptionBean eb = new ExceptionBean( getClass().getName(), e.getMessage() );
            responseHandler.writeAndClose( true, eb );
            return;
        }

        // 6.return response back to the client
        String charEnc = getRequest().getCharacterEncoding();
        if ( charEnc == null ) {
            charEnc = Charset.defaultCharset().displayName();
        }
        responseHandler.setContentType( "application/json; charset=" + charEnc );
        responseHandler.writeAndClose( false, result );
    }

    /**
     * @return search box for limiting query
     * @throws Exception
     */
    private String getSearchBox()
                            throws Exception {
        HttpSession session = ( (HttpServletRequest) getRequest() ).getSession( true );
        ViewContext vc = (ViewContext) session.getAttribute( Constants.CURRENTMAPCONTEXT );
        Point[] points = vc.getGeneral().getBoundingBox();
        Point p1 = points[0];
        Point p2 = points[1];
        if ( !points[0].getCoordinateSystem().getPrefixedName().equalsIgnoreCase( "EPSG:4326" ) ) {
            GeoTransformer tr = new GeoTransformer( "EPSG:4326" );
            p1 = (Point) tr.transform( p1 );
            p2 = (Point) tr.transform( p2 );
        }
        return "" + p1.getX() + ',' + p2.getY() + ',' + p2.getX() + ',' + p1.getY();
    }
}
