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
package org.deegree.portal.standard.nominatim.control;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpMethod;
import org.deegree.enterprise.control.ajax.AbstractListener;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.HttpUtils;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.crs.GeoTransformer;
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
public class SearchNominatimListener extends AbstractListener {

    private static ILogger LOG = LoggerFactory.getLogger( SearchNominatimListener.class );

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.enterprise.control.ajax.AbstractListener#actionPerformed(org.deegree.enterprise.control.ajax.WebEvent
     * , org.deegree.enterprise.control.ajax.ResponseHandler)
     */
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {

        String address = getInitParameter( "address" );
        String queryString = (String) event.getParameter().get( "QUERYSTRING" );
        String searchBox = null;
        try {
            searchBox = getSearchBox();
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            ExceptionBean eb = new ExceptionBean( getClass().getName(), e.getMessage() );
            responseHandler.writeAndClose( true, eb );
            return;
        }

        String charEnc = getRequest().getCharacterEncoding();
        if ( charEnc == null ) {
            charEnc = Charset.defaultCharset().displayName();
        }
        queryString = "q=" + URLEncoder.encode( queryString, "UTF-8" ) + "&format=xml&limit=100&viewbox=" + searchBox;
        LOG.logInfo( "Nominatim search query: ", address + "?" + queryString );

        HttpMethod method = HttpUtils.performHttpGet( address, queryString, 60000, null, null, null );
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
        List<String[]> result = new ArrayList<String[]>( 100 );
        Map<String, String> bboxes = new HashMap<String, String>( 100 );
        try {
            List<Node> nodes = XMLTools.getNodes( xml.getRootElement(), "place", CommonNamespaces.getNamespaceContext() );
            for ( Node node : nodes ) {
                String place = XMLTools.getNodeAsString( node, "@display_name", CommonNamespaces.getNamespaceContext(),
                                                         "" );
                String osm_id = XMLTools.getNodeAsString( node, "@osm_id", CommonNamespaces.getNamespaceContext(), "" );
                result.add( new String[] { place, osm_id } );

                String boundingbox = XMLTools.getNodeAsString( node, "@boundingbox",
                                                               CommonNamespaces.getNamespaceContext(), "" );
                bboxes.put( osm_id, boundingbox );
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            ExceptionBean eb = new ExceptionBean( getClass().getName(), e.getMessage() );
            responseHandler.writeAndClose( true, eb );
            return;
        }
        HttpSession session = ( (HttpServletRequest) getRequest() ).getSession( true );
        session.setAttribute( "NominatimBBOXES", bboxes );

        // result page uses UTF-8 encoding
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
