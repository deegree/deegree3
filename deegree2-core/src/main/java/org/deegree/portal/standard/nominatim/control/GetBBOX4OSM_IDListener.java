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
import java.nio.charset.Charset;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.deegree.enterprise.control.ajax.AbstractListener;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;
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
public class GetBBOX4OSM_IDListener extends AbstractListener {
    
    private static ILogger LOG = LoggerFactory.getLogger( GetBBOX4OSM_IDListener.class );

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

        String osm_id = (String) event.getParameter().get( "OSM_ID" );

        HttpSession session = ( (HttpServletRequest) getRequest() ).getSession( true );
        Map<String, String> bboxes = (Map<String, String>) session.getAttribute( "NominatimBBOXES" );

        String result = "ERROR";
        String bbox = bboxes.get( osm_id );
        if ( bbox != null ) {
            String[] tmp = bbox.split( "," );
            String t = tmp[2] + ',' + tmp[0] + ',' + tmp[3] + ',' + tmp[1];
            ViewContext vc = (ViewContext) session.getAttribute( Constants.CURRENTMAPCONTEXT );
            Point[] points = vc.getGeneral().getBoundingBox();
            if ( !points[0].getCoordinateSystem().getPrefixedName().equalsIgnoreCase( "EPSG:4326" ) ) {
                try {
                    Envelope env = GeometryFactory.createEnvelope( t, CRSFactory.create( "EPSG:4326" ) );
                    GeoTransformer tr = new GeoTransformer( points[0].getCoordinateSystem() );
                    env = tr.transform( env, env.getCoordinateSystem() );
                    result = env.getMin().getX() + "," + env.getMin().getY() + "," + env.getMax().getX() + ","
                             + env.getMax().getY();
                } catch ( Exception e ) {
                    LOG.logError( e.getMessage(), e );
                    ExceptionBean eb = new ExceptionBean( getClass().getName(), e.getMessage() );
                    responseHandler.writeAndClose( true, eb );
                    return;
                }
            } else {
                result = t;
            }
        }

        // result page uses UTF-8 encoding
        String charEnc = Charset.defaultCharset().displayName();
        responseHandler.setContentType( "text/plain; charset=" + charEnc );
        responseHandler.writeAndClose( result );
    }

}
