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
package org.deegree.portal.portlet.modules.map.actions.portlets;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.jetspeed.portal.Portlet;
import org.apache.jetspeed.portal.PortletSet;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.model.spatialschema.Point;
import org.deegree.portal.PortalException;
import org.deegree.portal.context.ContextException;
import org.deegree.portal.context.ViewContext;
import org.deegree.portal.context.WebMapContextFactory;
import org.deegree.portal.portlet.modules.actions.IGeoPortalPortletPerform;
import org.deegree.security.drm.model.User;

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
public class SelectWMCPortletPerform extends IGeoPortalPortletPerform {

    private static ILogger LOG = LoggerFactory.getLogger( SelectWMCPortletPerform.class );

    private static String INIT_KEEP_BBOX = "keep_bbox";

    /**
     *
     * @param request
     * @param portlet
     * @param sc
     */
    public SelectWMCPortletPerform( HttpServletRequest request, Portlet portlet, ServletContext sc ) {
        super( request, portlet, sc );

    }

    @Override
    public void buildNormalContext()
                            throws PortalException {
        super.buildNormalContext();

        if ( request.getSession().getAttribute( CURRENT_WMC ) == null ) {
            List<String[]> wmc = (List<String[]>) request.getSession().getAttribute( AVAILABLE_WMC );
            request.getSession().setAttribute( CURRENT_WMC, wmc.get( 0 )[1] );
        }
    }

    /**
     * selects the current context of a MapWindowPortlet
     *
     * @throws PortalException
     *
     */
    void doSelectwmc()
                            throws PortalException {

        String cntxid = parameter.get( "WMCID" );

        PortletSet ps = portlet.getPortletConfig().getPortletSet();
        String mapid = ps.getPortletByName( "iGeoPortal:MapActionPortlet" ).getID();
        Portlet port = ps.getPortletByID( mapid );
        port.getPortletConfig().setInitParameter( INIT_WMC, cntxid );

        String mwinid = getInitParam( INIT_MAPPORTLETID );
        port = ps.getPortletByID( mwinid );
        port.getPortletConfig().setInitParameter( INIT_WMC, cntxid );

        request.setAttribute( PARAM_MAPPORTLET, mwinid );

        ViewContext vc = null;
        if ( "true".equals( getInitParam( INIT_KEEP_BBOX ) ) ) {
            // get old current context to read its bounding box
            vc = getCurrentViewContext( parameter.get( "MAPPORTLET" ) );
            Point[] currentEnv = vc.getGeneral().getBoundingBox();

            // get new current context to set its bounding box with value of the
            // old current context bounding box to keep viewing area
            vc = getNamedViewContext( cntxid );
            try {
                vc.getGeneral().setBoundingBox( currentEnv );
            } catch ( ContextException e ) {
                LOG.logError( e.getMessage(), e );
                throw new PortalException( e.getMessage(), e );
            }
        } else {
            vc = getNamedViewContext( cntxid );
        }

        setCurrentMapContext( vc, getInitParam( INIT_MAPPORTLETID ) );
        setCurrentMapContextName( getInitParam( INIT_MAPPORTLETID ), cntxid );

    }

    /**
     * loads a context defined by its absolut path passed in URL encoded parameter 'FILENAME'
     *
     * @throws PortalException
     */
    void doLoadwmc( User user )
                            throws PortalException {

        String filename = null;
        try {
            filename = URLDecoder.decode( parameter.get( "FILENAME" ), CharsetUtils.getSystemCharset() );
        } catch ( UnsupportedEncodingException e1 ) {
            e1.printStackTrace();
        }
        File file = new File( filename );
        String cntxid = file.getName();

        PortletSet ps = portlet.getPortletConfig().getPortletSet();
        String mapid = ps.getPortletByName( "iGeoPortal:MapActionPortlet" ).getID();
        Portlet port = ps.getPortletByID( mapid );
        port.getPortletConfig().setInitParameter( INIT_WMC, cntxid );

        String mwinid = getInitParam( INIT_MAPPORTLETID );
        port = ps.getPortletByID( mwinid );
        port.getPortletConfig().setInitParameter( INIT_WMC, cntxid );

        request.setAttribute( PARAM_MAPPORTLET, mwinid );

        ViewContext vc = null;
        try {
            vc = WebMapContextFactory.createViewContext( file.toURI().toURL(), user, null );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            return;
        }

        setCurrentMapContext( vc, getInitParam( INIT_MAPPORTLETID ) );
        setCurrentMapContextName( getInitParam( INIT_MAPPORTLETID ), cntxid );

    }

}
