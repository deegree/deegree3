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
package org.deegree.portal.portlet.modules.actions;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.jetspeed.portal.Portlet;
import org.apache.jetspeed.portal.PortletSet;
import org.deegree.framework.util.KVP2Map;
import org.deegree.portal.PortalException;

/**
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public abstract class AbstractPortletPerform {

    /**
     * define 'mapPortletID'
     */
    public static final String INIT_MAPPORTLETID = "mapPortletID";

    /**
     * define 'wmc'
     */
    public static final String INIT_WMC = "wmc";

    /**
     * define 'availableWMC'
     */
    public static final String AVAILABLE_WMC = "availableWMC";

    // attributes stored in the users session
    /**
     * define 'HOME'
     */
    public static final String SESSION_HOME = "HOME";

    /**
     * define 'CURRENTWMC'
     */
    public static final String CURRENT_WMC = "CURRENTWMC";

    /**
     * define 'CURRENTWMCNAME'
     */
    public static final String CURRENT_WMC_NAME = "CURRENTWMCNAME";

    /**
     * define 'VIEWCONTEXT'
     */
    public static final String SESSION_VIEWCONTEXT = "VIEWCONTEXT";

    /**
     * define 'CURRENTFILAYER'
     */
    public static final String SESSION_CURRENTFILAYER = "CURRENTFILAYER";

    /**
     * define 'MAPPORTLET'
     */
    public static final String PARAM_MAPPORTLET = "MAPPORTLET";

    /**
     * define 'MAPACTIONPORTLET'
     */
    public static final String PARAM_MAPACTION = "MAPACTIONPORTLET";

    /**
     * define 'MODE'
     */
    public static final String PARAM_MODE = "MODE";

    protected Portlet portlet = null;

    protected HttpServletRequest request = null;

    protected Map<String, String> parameter = null;

    /**
     * @param portlet
     * @param request
     */
    public AbstractPortletPerform( HttpServletRequest request, Portlet portlet ) {
        this.portlet = portlet;
        this.request = request;
        parameter = KVP2Map.toMap( request );
        if ( portlet != null ) {
            setMapActionPortletID();
        }
    }

    /**
     * returns the value of the passed init parameter. This method shal be used to hide functional
     * implementation from concrete portlet implementation.
     *
     * @param name
     * @return the value of the passed init parameter.
     */
    protected String getInitParam( String name ) {
        if ( portlet != null ) {
            return portlet.getPortletConfig().getInitParameter( name );
        }
        return null;
    }

    /**
     * returns the ID of the mapmodel assigned to a portlet. First the method tries to read it from
     * the portlets initparameter. If not present it returns the ID of the first
     * iGeoPortal:MapWindowPortlet it finds.
     *
     * @return the ID of the mapmodel assigned to a portlet. First the method tries to read it from
     *         the portlets initparameter. If not present it returns the ID of the first
     *         iGeoPortal:MapWindowPortlet it finds.
     */
    protected String getMapPortletID() {
        String mmid = portlet.getPortletConfig().getInitParameter( INIT_MAPPORTLETID );
        if ( mmid == null ) {
            PortletSet ps = portlet.getPortletConfig().getPortletSet();
            Portlet port = ps.getPortletByName( "iGeoPortal:MapWindowPortlet" );
            mmid = port.getID();
        }
        return mmid;
    }

    /**
     * this method will be called each time a portlet will be repainted. It determines all portlets
     * visible in a page and writes a Map with the portlets name as key and the portlets ID as value
     * into the forwarded HttpRequest object
     *
     * @throws PortalException
     */
    public void buildNormalContext()
                            throws PortalException {

        request.setAttribute( "PORTLETID", portlet.getID() );
        Map<String, String> map = new HashMap<String, String>();
        Enumeration enume = portlet.getPortletConfig().getPortletSet().getPortlets();
        while ( enume.hasMoreElements() ) {
            Portlet p = (Portlet) enume.nextElement();
            map.put( p.getName(), p.getID() );
        }
        request.setAttribute( "PORTLETS", map );
        request.setAttribute( PARAM_MAPPORTLET, getInitParam( INIT_MAPPORTLETID ) );
    }

    protected String readMapContextID_() {
        String pid = null;

        String cntxID = null;
        if ( portlet != null ) {
            cntxID = (String) request.getSession().getAttribute( portlet.getID() + "_" + CURRENT_WMC );
            if ( cntxID == null ) {
                pid = portlet.getPortletConfig().getInitParameter( AbstractPortletPerform.INIT_MAPPORTLETID );
            }
        }

        if ( cntxID == null ) {
            if ( pid == null ) {
                cntxID = getInitParam( INIT_WMC );
            } else {
                Portlet port = portlet.getPortletConfig().getPortletSet().getPortletByID( pid );
                if ( port == null ) {
                    // try to access first MapWindowPortlet found in a page
                    pid = getMapPortletID();
                    port = portlet.getPortletConfig().getPortletSet().getPortletByID( pid );
                }
                if ( port != null ) {
                    cntxID = port.getPortletConfig().getInitParameter( INIT_WMC );
                }
            }
        }
        return cntxID;
    }

    /**
     *
     *
     */
    private void setMapActionPortletID() {
        Portlet port = portlet.getPortletConfig().getPortletSet().getPortletByName( "iGeoPortal:MapActionPortlet" );
        if ( port != null ) {
            request.setAttribute( PARAM_MAPACTION, port.getID() );
        }
    }
}
