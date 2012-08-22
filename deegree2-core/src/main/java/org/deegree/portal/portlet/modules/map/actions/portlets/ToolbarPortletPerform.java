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

import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.jetspeed.portal.Portlet;
import org.apache.jetspeed.portal.PortletConfig;
import org.deegree.framework.util.BootLogger;
import org.deegree.framework.util.StringTools;
import org.deegree.i18n.Messages;
import org.deegree.portal.PortalException;
import org.deegree.portal.portlet.modules.actions.AbstractPortletPerform;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ToolbarPortletPerform extends AbstractPortletPerform {

    // init parameter of the portlet
    public static final String INIT_BUTTONS = "buttons";

    public static final String INIT_ORIENTATION = "orientation";

    public static final String INIT_COLS = "columns";

    public static final String INIT_ROWS = "rows";

    private String imageBase = "./igeoportal/images/";

    private static Properties props = new Properties();
    static {
        try {
            InputStream is = ToolbarPortletPerform.class.getResourceAsStream( "toolbar.properties" );
            props.load( is );
            is.close();
        } catch ( Exception e ) {
            BootLogger.logError( e.getMessage(), e );
        }
    }

    /**
     * private constructor
     *
     * @param request
     * @param portlet
     */
    private ToolbarPortletPerform( HttpServletRequest request, Portlet portlet ) {
        super( request, portlet );
    }

    /**
     * returns an instance of
     *
     * @see MapWindowPortletPerform
     * @param request
     * @param portlet
     * @return an instance of
     * @see MapWindowPortletPerform
     */
    static ToolbarPortletPerform getInstance( HttpServletRequest request, Portlet portlet ) {
        return new ToolbarPortletPerform( request, portlet );
    }

    /**
     * reads the init parameters of the ToolbarPortlet and writes them into the requests attributes.
     *
     * @throws PortalException
     *
     */
    void readInitParameter()
                            throws PortalException {
        PortletConfig pc = portlet.getPortletConfig();
        String tmp = pc.getInitParameter( INIT_BUTTONS );
        if ( tmp == null ) {
            throw new PortalException( "buttons init parameter must be set" );
        }
        String[] ar = StringTools.toArray( tmp, ";", false );
        String[][] buttonList = new String[ar.length][];
        for ( int i = 0; i < buttonList.length; i++ ) {
            buttonList[i] = getButtonSrc( ar[i] );
        }

        request.setAttribute( INIT_BUTTONS, buttonList );

        tmp = pc.getInitParameter( INIT_COLS );
        if ( tmp != null ) {
            request.setAttribute( INIT_COLS, new Integer( tmp ) );
        } else {
            request.setAttribute( INIT_COLS, new Integer( 99999 ) );
        }
        tmp = pc.getInitParameter( INIT_ROWS );
        if ( tmp != null ) {
            request.setAttribute( INIT_ROWS, new Integer( tmp ) );
        } else {
            request.setAttribute( INIT_ROWS, new Integer( 99999 ) );
        }

        tmp = pc.getInitParameter( INIT_ORIENTATION );
        if ( tmp == null || tmp.equals( "FLOW" ) ) {
            request.setAttribute( INIT_ORIENTATION, "FLOW" );
        } else if ( tmp.equals( "VERTICAL" ) ) {
            request.setAttribute( INIT_ORIENTATION, "VERTICAL" );
        } else if ( tmp.equals( "HORIZONTAL" ) ) {
            request.setAttribute( INIT_ORIENTATION, "HORIZONTAL" );
        } else {
            throw new PortalException( "init parameter orientation must be " + "VERTICAL, HORIZONTAL or FLOW" );
        }
        request.setAttribute( "PORTLETID", portlet.getID() );
    }

    /**
     *
     * @param button
     * @return
     */
    private String[] getButtonSrc( String button ) {
        String[] tmp = StringTools.toArray( button, "|", false );
        button = tmp[0];
        String[] src = new String[5];
        src[3] = button;
        src[4] = tmp[1];

        src[0] = imageBase + props.getProperty( button + ".released" );
        src[1] = imageBase + props.getProperty( button + ".pressed" );
        src[2] = props.getProperty( button + ".type" );

        if ( src[0] == null || src[1] == null || src[2] == null ) {
            throw new RuntimeException( Messages.getMessage( "IGEO_PORTLET_MISSING_TOOLBAR_BT", button ) );
        }

        return src;
    }

}
