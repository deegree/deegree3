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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.jetspeed.portal.Portlet;
import org.apache.jetspeed.portal.PortletSet;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.portal.PortalException;
import org.deegree.portal.context.ViewContext;
import org.deegree.portal.portlet.modules.actions.IGeoPortalPortletPerform;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class MapActionPortletPerform extends IGeoPortalPortletPerform {

    protected static final ILogger LOG = LoggerFactory.getLogger( MapActionPortletPerform.class );

    /**
     *
     * @param request
     * @param portlet
     * @param sc
     */
    public MapActionPortletPerform( HttpServletRequest request, Portlet portlet, ServletContext sc ) {
        super( request, portlet, sc );
    }

    @Override
    public void buildNormalContext()
                            throws PortalException {
        super.buildNormalContext();

        HttpSession ses = request.getSession();

        List<String> tmp = StringTools.toList( getInitParam( AVAILABLE_WMC ), ";", false );
        List<String[]> wmc = new ArrayList<String[]>( tmp.size() );
        for ( int i = 0; i < tmp.size(); i++ ) {
            String[] t = StringTools.toArray( tmp.get( i ), "|", false );
            wmc.add( t );
        }
        ses.setAttribute( AVAILABLE_WMC, wmc );
        if ( getNamedViewContext( wmc.get( 0 )[0] ) == null ) {
            // initial call - init parameter WMCs has not been read
            for ( int i = 0; i < wmc.size(); i++ ) {
                try {
                    if ( getNamedViewContext( wmc.get( i )[0] ) == null ) {
                        File f = new File( wmc.get( i )[1] );
                        if ( !f.isAbsolute() ) {
                            wmc.get( i )[1] = sc.getRealPath( wmc.get( i )[1] );
                        }
                        File file = new File( wmc.get( i )[1] );
                        LOG.logDebug( "write context to session: " + wmc.get( i )[0] );

                        setNameContext( wmc.get( i )[0], file.toURL() );
                    }
                } catch ( Exception e ) {
                    LOG.logError( e.getMessage(), e );
                    throw new PortalException( e.getMessage() );
                }
            }

        }
        // write map model id for each portlet that has registered an
        // initial WMC into the users session
        PortletSet ps = portlet.getPortletConfig().getPortletSet();
        Enumeration enm = ps.getPortlets();
        while ( enm.hasMoreElements() ) {
            Portlet port = (Portlet) enm.nextElement();
            String cntxId = port.getPortletConfig().getInitParameter( INIT_WMC );
            if ( cntxId != null ) {
                try {
                    if ( getCurrentViewContext( getInitParam( INIT_MAPPORTLETID ) ) == null ) {
                        ViewContext vc = getNamedViewContext( cntxId );
                        setCurrentMapContext( vc, getInitParam( INIT_MAPPORTLETID ) );
                        setCurrentMapContextName( getInitParam( INIT_MAPPORTLETID ), cntxId );
                    }
                } catch ( Throwable e ) {
                    //LOG.logError( e.getMessage(), e );
                    throw new PortalException( e.getMessage() );
                }
            }
        }

    }

}
