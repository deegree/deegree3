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

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.jetspeed.portal.Portlet;
import org.apache.turbine.util.RunData;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;
import org.deegree.portal.context.ContextException;
import org.deegree.portal.context.Layer;
import org.deegree.portal.context.ViewContext;
import org.deegree.portal.context.WebMapContextFactory;
import org.deegree.security.drm.model.User;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class IGeoPortalPortletPerform extends AbstractPortletPerform {

    private static final ILogger LOG = LoggerFactory.getLogger( IGeoPortalPortletPerform.class );

    /**
     * A String denoting BBOX
     */
    public static final String PARAM_BBOX = "BBOX";

    /**
     * A String denoting LAYER
     */
    public static final String PARAM_LAYER = "LAYER";

    /**
     * A String denoting STYLE
     */
    public static final String PARAM_STYLE = "STYLE";

    /**
     * A String denoting MAPMODE
     */
    public static final String PARAM_MAPMODE = "MAPMODE";

    /**
     * A String denoting SESSIONID
     */
    public static final String PARAM_SESSIONID = "SESSIONID";

    /**
     * A String denoting HISTORY
     */
    public static final String SESSION_HISTORY = "HISTORY";

    /**
     * A String denoting HISTORYPOSITION
     */
    public static final String SESSION_HISTORYPOSITION = "HISTORYPOSITION";

    protected ServletContext sc = null;

    protected static Map<String, ViewContext> vcMap = new HashMap<String, ViewContext>( 100 );


    /**
     * @param request
     * @param portlet
     * @param sc context
     */
    public IGeoPortalPortletPerform( HttpServletRequest request, Portlet portlet, ServletContext sc ) {
        super( request, portlet );
        this.sc = sc;
    }

    /**
     * updates the view context with the current bounding box, the list of visible layers and the
     * current map mode
     *
     */
    public void updateContext() {

        // update the view contexts bounding box with the current one.
        setBoundingBoxFromBBOXParam();

        // update the view contexts list of visible layers
        setLayers();

        // update the map mode
        setMode();

    }

    /**
     * sets layers of the view context as visible or invisble depending on the incoming request
     *
     */
    public void setLayers() {

        if ( portlet != null && parameter.get( PARAM_BBOX ) != null && parameter.get( "LAYERS" ) != null ) {
            // just change layerlist if the request contains a BBOX parameter
            // and at least one layer because other wise it will be the initial call;
            ViewContext vc = getCurrentViewContext( portlet.getID() );
            Layer[] layers = vc.getLayerList().getLayers();
            String ly = parameter.get( "LAYERS" );
            StringBuffer sb = new StringBuffer( 100 );
            for ( int i = 0; i < layers.length; i++ ) {
                sb.append( layers[i].getName() ).append( '|' );
                sb.append( layers[i].getServer().getOnlineResource() );
                if ( ly.indexOf( sb.toString() ) > -1 ) {
                    layers[i].setHidden( false );
                } else {
                    layers[i].setHidden( true );
                }
                sb.delete( 0, sb.length() );
            }

            setCurrentMapContext( vc, portlet.getID() );
        }

    }

    /**
     * writes the current map mode (if set) into the users WMC.
     *
     */
    public void setMode() {
        String mm = parameter.get( PARAM_MAPMODE );
        if ( mm != null ) {
            ViewContext vc = getCurrentViewContext( portlet.getID() );
            vc.getGeneral().getExtension().setMode( mm );
            setCurrentMapContext( vc, portlet.getID() );
        }
    }

    /**
     * sets a new bounding box of the map read from the the request object passed when initialized
     * an instance of <code>MapWindowPortletPerfom</code>
     *
     */
    public void setBoundingBoxFromBBOXParam() {
        String bbox = parameter.get( PARAM_BBOX );
        setBoundingBox( bbox );
    }

    /**
     * sets a new bounding box of the map read from the the request object passed when initialized
     * an instance of <code>MapWindowPortletPerfom</code>
     *
     * @param env
     *            new bounding box
     */
    public void setBoundingBox( Envelope env ) {

        if ( portlet != null ) {
            ViewContext vc = getCurrentViewContext( portlet.getID() );
            if ( vc != null ) {
                CoordinateSystem crs = vc.getGeneral().getBoundingBox()[0].getCoordinateSystem();
                Point[] pt = new Point[2];
                pt[0] = GeometryFactory.createPoint( env.getMin().getX(), env.getMin().getY(), crs );
                pt[1] = GeometryFactory.createPoint( env.getMax().getX(), env.getMax().getY(), crs );
                try {
                    vc.getGeneral().setBoundingBox( pt );
                } catch ( ContextException should_never_happen ) {
                    //nottin
                }


                List<Envelope> history = (List<Envelope>) request.getSession().getAttribute( SESSION_HISTORY );
                int p = (Integer) request.getSession().getAttribute( SESSION_HISTORYPOSITION );
                Envelope current = history.get( p );
                if ( current == null || !current.equals( env ) ) {
                    p++;
                    history.add( p, env );
                    request.getSession().setAttribute( IGeoPortalPortletPerform.SESSION_HISTORYPOSITION, p );
                }

            }
            setCurrentMapContext( vc, portlet.getID() );
        }
    }

    /**
     * the method expects a string with four comma seperated coordinate values. The created box will
     * be written
     *
     * @param bbox
     */
    public void setBoundingBox( String bbox ) {

        if ( bbox != null ) {
            double[] coords = StringTools.toArrayDouble( bbox, "," );
            Envelope env = GeometryFactory.createEnvelope( coords[0], coords[1], coords[2], coords[3], null );
            setBoundingBox( env );
        }
    }

    /**
     * returns the current <@link ViewContext> read from the portlet session.
     *
     * @param session
     * @param pid
     *            MapWindowPortlet id
     *
     * @return the current ViewContext read from the portlet session.
     */
    public static ViewContext getCurrentViewContext( HttpSession session, String pid ) {
        return (ViewContext) session.getAttribute( pid + '_' + CURRENT_WMC );
    }

    /**
     * returns the current <@link ViewContext> read from the portlet session.
     *
     * @param pid
     *            MapWindowPortlet id
     *
     * @return the current ViewContext read from the portlet session.
     */
    public ViewContext getCurrentViewContext( String pid ) {
        return getCurrentViewContext( request.getSession(), pid );
    }

    /**
     * sets the current MapContext to the users session
     *
     * @param session
     * @param vc
     * @param pid
     */
    public static void setCurrentMapContext( HttpSession session, ViewContext vc, String pid ) {
        session.setAttribute( pid + '_' + CURRENT_WMC, vc );
    }

    /**
     * sets the current MapContext to the users session
     *
     * @param vc
     * @param pid
     */
    public void setCurrentMapContext( ViewContext vc, String pid ) {
        setCurrentMapContext( request.getSession(), vc, pid );
    }

    /**
     * writes the name of the current WMC into the users session
     *
     * @param session
     * @param pid
     * @param name
     */
    public static void setCurrentMapContextName( HttpSession session, String pid, String name ) {
        session.setAttribute( pid + '_' + CURRENT_WMC_NAME, name );
    }

    /**
     * writes the name of the current WMC into the users session
     *
     * @param pid
     * @param name
     */
    public void setCurrentMapContextName( String pid, String name ) {
        setCurrentMapContextName( request.getSession(), pid, name );
    }

    /**
     * returns the name of the current WMC into the users session
     *
     * @param session
     * @param pid
     * @return the name of the current WMC into the users session
     */
    public static String getCurrentMapContextName( HttpSession session, String pid ) {
        return (String) session.getAttribute( pid + '_' + CURRENT_WMC_NAME );
    }

    /**
     * returns the name of the current WMC into the users session
     *
     * @param pid
     * @return the name of the current WMC into the users session
     */
    public String getCurrentMapContextName( String pid ) {
        return getCurrentMapContextName( request.getSession(), pid );
    }

    /**
     * returns an instance of <@link ViewContext> read from the portlet session. If no instance is
     * available <code>null</code> will be returned.
     *
     * @param session
     * @param name
     *            map context name/id
     *
     * @return an instance of ViewContext read from the portlet session. If no instance is available
     *         <code>null</code> will be returned.
     */
    public ViewContext getNamedViewContext( HttpSession session, String name ) {
        URL url = (URL) session.getAttribute( SESSION_VIEWCONTEXT + name );
        ViewContext vc = null;
        try {
            if ( url != null ) {

                RunData rundata = (RunData) request.getAttribute( "data" );
                if ( rundata != null ) {
                    org.apache.turbine.om.security.User tu = rundata.getUser();

                    String username = tu.getUserName();
                    String password = tu.getPassword();
                    String firstname = tu.getFirstName();
                    String lastname = tu.getLastName();
                    String email = tu.getEmail();

                    User user = new User( 0, username, password, firstname, lastname, email, null );
                    // User(int id, String name, String password, String
                    // firstName,
                    // String lastName, String emailAddress, SecurityRegistry
                    // registry)
                    String key = url.toExternalForm() + '|' + session.getId();
                    if ( vcMap.get( key ) != null ) {
                        vc = vcMap.get( key );
                    } else {
                        vc = WebMapContextFactory.createViewContext( url, user, null );
                        vcMap.put( key, vc );
                    }
                } else {
                    String key = url.toExternalForm();
                    if ( vcMap.get( key ) != null ) {
                        vc = vcMap.get( key );
                    } else {
                        vc = WebMapContextFactory.createViewContext( url, null, null );
                        vcMap.put( key, vc );
                    }
                }
            }
        } catch ( Exception e ) {
            // should never happen
            LOG.logError( e.getMessage(), e );
        }

        return vc;
    }

    /**
     * returns an instance of <@link ViewContext> read from the portlet session. If no instance is
     * available <code>null</code> will be returned.
     *
     * @param name
     *            map context name/id
     *
     * @return instance of ViewContext read from the portlet session. If no instance is available
     *         <code>null</code> will be returned.
     */
    public ViewContext getNamedViewContext( String name ) {
        return getNamedViewContext( request.getSession(), name );
    }

    /**
     * writes the URL to a WMC with a assigend name into a users session
     *
     * @param session
     * @param name
     * @param url
     */
    public static void setNameContext( HttpSession session, String name, URL url ) {
        session.setAttribute( SESSION_VIEWCONTEXT + name, url );
    }

    /**
     * writes the URL to a WMC with a assigend name into a users session
     *
     * @param name
     * @param url
     */
    public void setNameContext( String name, URL url ) {
        setNameContext( request.getSession(), name, url );
    }

}
