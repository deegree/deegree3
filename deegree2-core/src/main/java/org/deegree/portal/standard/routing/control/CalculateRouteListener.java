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

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpException;
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
import org.deegree.model.crs.CRSTransformationException;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.WKTAdapter;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.portal.Constants;
import org.deegree.portal.cataloguemanager.model.ExceptionBean;
import org.deegree.portal.context.ViewContext;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CalculateRouteListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( CalculateRouteListener.class );

    private RequestBean requestBean;

    private static NamespaceContext nsc = CommonNamespaces.getNamespaceContext();
    static {
        nsc.addNamespace( "kml", URI.create( "http://earth.google.com/kml/2.0" ) );
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.enterprise.control.ajax.AbstractListener#actionPerformed(org.deegree.enterprise.control.ajax.WebEvent
     * , org.deegree.enterprise.control.ajax.ResponseHandler)
     */
    @SuppressWarnings("unchecked")
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {

        HttpSession session = ( (HttpServletRequest) getRequest() ).getSession( true );
        ViewContext vc = (ViewContext) session.getAttribute( Constants.CURRENTMAPCONTEXT );
        CoordinateSystem modelCrs = vc.getGeneral().getBoundingBox()[0].getCoordinateSystem();

        // address of the YOURS (Yet another OpenStreetMap Route Service) web application
        String address = getInitParameter( "address" );
        LOG.logDebug( "request: ", event.getParameter() );
        requestBean = new RequestBean( event.getParameter(), modelCrs );

        // result of a query - if no exception occurs - will be a KML document
        XMLFragment xml;
        try {
            xml = performQuery( responseHandler, address );
        } catch ( Exception e ) {
            ExceptionBean eb = new ExceptionBean( getClass().getName(), e.getMessage() );
            responseHandler.writeAndClose( true, eb );
            return;
        }

        RouteBean route = null;
        try {
            String xpath = "kml:Document/kml:Folder/kml:Placemark/kml:LineString/kml:coordinates";
            String coords = XMLTools.getNodeAsString( xml.getRootElement(), xpath, nsc, null );
            if ( coords == null || coords.trim().length() < 6) {
                // this for example will happen if you try to find a route from somewhere in the middle
                // of the sahara to the middle of the atlantic ocean
                LOG.logWarning( "no route found for defined start-, end- and way points" );
                ExceptionBean eb = new ExceptionBean( getClass().getName(),
                                                      "no route found for defined start-, end- and way points" );
                responseHandler.writeAndClose( true, eb );
                return;
            }
            xpath = "kml:Document/kml:description";
            String description = XMLTools.getNodeAsString( xml.getRootElement(), xpath, nsc, null );
            xpath = "kml:Document/kml:distance";
            double distance = XMLTools.getNodeAsDouble( xml.getRootElement(), xpath, nsc, -1 );
            // create response be to be serialized as JSON object
            route = new RouteBean( modelCrs, coords, description, distance, requestBean.getStartText(),
                                   requestBean.getEndText() );
            // store route geometry in the user's session to be used with deegree SessionWMS
            // for rendering a route
            session.setAttribute( "TEMP_WMS_GEOMETRY", WKTAdapter.wrap( route.getCoordinates(), modelCrs ) );
        } catch ( Exception e ) {
            LOG.logError( e );
            throw new RuntimeException( e );
        }

        String charEnc = getRequest().getCharacterEncoding();
        if ( charEnc == null ) {
            charEnc = Charset.defaultCharset().displayName();
        }
        responseHandler.setContentType( "application/json; charset=" + charEnc );
        responseHandler.writeAndClose( false, route );
    }

    private XMLFragment performQuery( ResponseHandler responseHandler, String address )
                            throws HttpException, IOException, UnsupportedEncodingException {
        StringBuilder query = new StringBuilder( 500 );
        query.append( "format=kml&layer=mapnik&instructions=1" );
        query.append( "&flat=" ).append( requestBean.getStartWGS84().y );
        query.append( "&flon=" ).append( requestBean.getStartWGS84().x );
        query.append( "&tlat=" ).append( requestBean.getEndWGS84().y );
        query.append( "&tlon=" ).append( requestBean.getEndWGS84().x );
        query.append( "&v=" ).append( requestBean.getTransportation() );
        query.append( "&fast=" ).append( requestBean.isFastest() );

        LOG.logDebug( "YOURS query: ", address + "?" + query );

        HttpMethod method = HttpUtils.performHttpGet( address, query.toString(), 60000, null, null, null );

        // get and parse the result that must be an XML document
        // YOURS routing service always returns result in UTF-8 encoding
        InputStreamReader isr = new InputStreamReader( method.getResponseBodyAsStream(), "UTF-8" );
        XMLFragment xml = new XMLFragment();
        try {
            xml.load( isr, address );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new RuntimeException( e );
        } finally {
            isr.close();
        }

        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( xml.getAsPrettyString() );
        }
        return xml;
    }

    /**
     * 
     * TODO add class documentation here
     * 
     * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    private class RequestBean {

        private String transportation;

        private short fastest;

        private String startText;

        private String endText;

        private Point2D.Double startWGS84;

        private Point2D.Double endWGS84;

        private Point2D.Double[] wayPointsWGS84;

        /**
         * 
         * @param param
         * @param modelCrs
         * @throws CRSTransformationException
         * @throws IllegalArgumentException
         */
        RequestBean( Map<String, Object> param, CoordinateSystem modelCrs ) {
            transportation = (String) param.get( "transportation" );
            if ( (Boolean) param.get( "fastest" ) ) {
                fastest = 1;
            }
            startText = (String) param.get( "startText" );
            endText = (String) param.get( "endText" );

            GeoTransformer gt = new GeoTransformer( CRSFactory.EPSG_4326 );

            double x = ( (Number) param.get( "startWGS84x" ) ).doubleValue();
            double y = ( (Number) param.get( "startWGS84y" ) ).doubleValue();
            startWGS84 = new Point2D.Double( x, y );

            x = ( (Number) param.get( "endWGS84x" ) ).doubleValue();
            y = ( (Number) param.get( "endWGS84y" ) ).doubleValue();
            endWGS84 = new Point2D.Double( x, y );

            x = ( (Number) param.get( "startLocalx" ) ).doubleValue();
            y = ( (Number) param.get( "startLocaly" ) ).doubleValue();
            if ( startWGS84.x < -8999999999d ) {
                Point p = GeometryFactory.createPoint( x, y, modelCrs );
                try {
                    p = (Point) gt.transform( p );
                } catch ( Exception e ) {
                    throw new RuntimeException( e );
                }
                startWGS84 = new Point2D.Double( p.getX(), p.getY() );
            }

            x = ( (Number) param.get( "endLocalx" ) ).doubleValue();
            y = ( (Number) param.get( "endLocaly" ) ).doubleValue();
            if ( endWGS84.x < -8999999999d ) {
                Point p = GeometryFactory.createPoint( x, y, modelCrs );
                try {
                    p = (Point) gt.transform( p );
                } catch ( Exception e ) {
                    throw new RuntimeException( e );
                }
                endWGS84 = new Point2D.Double( p.getX(), p.getY() );
            }
        }

        /**
         * @return the transportation
         */
        public String getTransportation() {
            return transportation;
        }

        /**
         * @return the fastest
         */
        public short isFastest() {
            return fastest;
        }

        /**
         * @return the startText
         */
        public String getStartText() {
            return startText;
        }

        /**
         * @return the endText
         */
        public String getEndText() {
            return endText;
        }

        /**
         * @return the startWGS84
         */
        public Point2D.Double getStartWGS84() {
            return startWGS84;
        }

        /**
         * @return the endWGS84
         */
        public Point2D.Double getEndWGS84() {
            return endWGS84;
        }

        /**
         * @return the wayPoints
         */
        public Point2D.Double[] getWayPointsWGS84() {
            return wayPointsWGS84;
        }

    }

}
