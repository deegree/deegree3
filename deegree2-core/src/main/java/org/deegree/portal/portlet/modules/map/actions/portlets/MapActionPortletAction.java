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

import java.lang.reflect.Constructor;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.jetspeed.modules.actions.portlets.JspPortletAction;
import org.apache.jetspeed.portal.Portlet;
import org.apache.turbine.om.security.User;
import org.apache.turbine.util.RunData;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.KVP2Map;
import org.deegree.portal.PortalException;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class MapActionPortletAction extends JspPortletAction {

    private ILogger LOG = LoggerFactory.getLogger( MapActionPortletAction.class );

    /**
     * @throws Exception
     *
     */
    @Override
    protected void buildNormalContext( Portlet portlet, RunData data )
                            throws Exception {

        try {
            MapActionPortletPerform mapp = new MapActionPortletPerform( data.getRequest(), portlet,
                                                                        data.getServletContext() );
            mapp.buildNormalContext();
            data.getRequest().setAttribute( "User", data.getUser().getUserName() );
            data.getRequest().setAttribute( "Password", data.getUser().getPassword() );
        } catch ( Exception e ) {
            throw e;
        }

    }

    /**
     * Changes the CRS of the underlying ViewContext. The actual action is implemented by the
     * <code>CRSChooserPortletPerform</code>.
     *
     * @param data
     * @param portlet
     * @throws PortalException
     */
    public void doCrschoose( RunData data, Portlet portlet )
                            throws PortalException {

        try {
            Portlet port = portlet.getPortletConfig().getPortletSet().getPortletByName( "iGeoPortal:CRSChooserPortlet" );
            CRSChooserPortletPerform ccpp = new CRSChooserPortletPerform( data.getRequest(), port,
                                                                          data.getServletContext() );
            ccpp.doCRSChange();
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new PortalException( e.getMessage() );
        }
    }

    /**
     * returns the MapWindowPortlet targeted by the current action
     *
     * @param rundata
     * @param portlet
     * @return the MapWindowPortlet targeted by the current action
     */
    private Portlet getMapWindowPortlet( RunData rundata, Portlet portlet ) {
        Map map = KVP2Map.toMap( rundata.getRequest() );
        String id = (String) map.get( "MAPPORTLET" );
        Portlet port = portlet.getPortletConfig().getPortletSet().getPortletByID( id );
        return port;
    }

    /**
     *
     * @param data
     * @param portlet
     * @throws Exception
     */
    public void doFeatureinfo( RunData data, Portlet portlet )
                            throws Exception {

        try {
            HttpServletRequest req = data.getRequest();
            req.setAttribute( "$U$", data.getUser().getUserName() );
            req.setAttribute( "$P$", data.getUser().getPassword() );
            Portlet port = getMapWindowPortlet( data, portlet );
            FeatureInfoPortletPerform fipp = new FeatureInfoPortletPerform( req, port, data.getServletContext() );
            fipp.doGetFeatureInfo();
        } catch ( Exception e ) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @param data
     * @param portlet
     * @throws Exception
     */
    public void doFeatureinfoForward( RunData data, Portlet portlet )
                            throws Exception {

        try {
            Portlet port = getMapWindowPortlet( data, portlet );
            String className = portlet.getPortletConfig().getInitParameter( "performingClass" );
            Class[] classes = new Class[3];
            classes[0] = data.getRequest().getClass();
            classes[1] = port.getClass();
            classes[2] = data.getRequest().getClass();
            Object[] o = new Object[3];
            o[0] = data.getRequest();
            o[1] = portlet;
            o[2] = data.getRequest();

            Class clss = Class.forName( className );
            Constructor constructor = clss.getConstructor( classes );
            constructor.newInstance( o );

            FeatureInfoForwardPortletPerform fifpp = (FeatureInfoForwardPortletPerform) constructor.newInstance( o );
            fifpp.doGetFeatureInfo();
        } catch ( Exception e ) {
            e.printStackTrace();
        }

    }

    /**
     * sets a new bounding box for the current ViewContext.
     *
     * @param data
     * @param portlet
     */
    public void doSetboundingbox( RunData data, Portlet portlet ) {
        try {
            Portlet port = getMapWindowPortlet( data, portlet );
            MapWindowPortletPerform mwpp = new MapWindowPortletPerform( data.getRequest(), port,
                                                                        data.getServletContext() );
            mwpp.setBoundingBoxFromBBOXParam();
            mwpp.setLayers();
            mwpp.setCurrentFILayer();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * sets the maps boundingbox to the defined home boundingbox
     *
     * @param data
     * @param portlet
     */
    public void doSethomeboundingbox( RunData data, Portlet portlet ) {
        try {
            Portlet port = getMapWindowPortlet( data, portlet );
            MapWindowPortletPerform mwpp = new MapWindowPortletPerform( data.getRequest(), port,
                                                                        data.getServletContext() );
            mwpp.setHomeBoundingbox();
            // mwpp.setLayers( port.getID() );
            // mwpp.setCurrentFILayer();
        } catch ( Exception e ) {
            e.printStackTrace();
        }

    }

    /**
     * sets a new size of the map (pixel)
     *
     * @param rundata
     * @param portlet
     * @throws Exception
     */
    public void doSetmapsize( RunData rundata, Portlet portlet )
                            throws Exception {
        try {
            Portlet port = getMapWindowPortlet( rundata, portlet );
            MapWindowPortletPerform mwpp = new MapWindowPortletPerform( rundata.getRequest(), port,
                                                                        rundata.getServletContext() );
            mwpp.setMapSize();
            mwpp.setBoundingBoxFromBBOXParam();
            mwpp.setLayers();
            mwpp.setCurrentFILayer();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * Performs a repaint of the current ViewContext by regenerating the assigend OWS requests. In opposite to
     * <code>doActualizeViewContext(RunData, Portlet)</code> no parameters from the client are solved to actualize the
     * current ViewContext.
     *
     * @param rundata
     * @param portlet
     * @throws Exception
     */
    public void doRepaint( RunData rundata, Portlet portlet )
                            throws Exception {
        try {
            Portlet port = getMapWindowPortlet( rundata, portlet );
            if ( port != null ) {
                MapWindowPortletPerform mwpp = new MapWindowPortletPerform( rundata.getRequest(), port,
                                                                            rundata.getServletContext() );
                mwpp.setBoundingBoxFromBBOXParam();
                mwpp.setLayers();
                mwpp.setCurrentFILayer();
                mwpp.setMode();
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * performs a zoomIn or a zoomOut on the current map model (ViewContext)
     *
     * @param rundata
     * @param portlet
     * @throws Exception
     */
    public void doZoom( RunData rundata, Portlet portlet )
                            throws Exception {
        try {
            Portlet port = getMapWindowPortlet( rundata, portlet );
            MapWindowPortletPerform mwpp = new MapWindowPortletPerform( rundata.getRequest(), port,
                                                                        rundata.getServletContext() );
            mwpp.zoom();
            mwpp.setLayers();
            mwpp.setCurrentFILayer();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * performs a panning on the current map model (ViewContext)
     *
     * @param rundata
     * @param portlet
     * @throws Exception
     */
    public void doPan( RunData rundata, Portlet portlet )
                            throws Exception {
        try {
            Portlet port = getMapWindowPortlet( rundata, portlet );
            MapWindowPortletPerform mwpp = new MapWindowPortletPerform( rundata.getRequest(), port,
                                                                        rundata.getServletContext() );
            mwpp.pan();
            mwpp.setLayers();
            mwpp.setCurrentFILayer();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * performs a recentering on the current map model (ViewContext)
     *
     * @param rundata
     * @param portlet
     * @throws Exception
     */
    public void doRecenter( RunData rundata, Portlet portlet )
                            throws Exception {
        try {
            Portlet port = getMapWindowPortlet( rundata, portlet );
            MapWindowPortletPerform mwpp = new MapWindowPortletPerform( rundata.getRequest(), port,
                                                                        rundata.getServletContext() );
            mwpp.recenter();
            mwpp.setLayers();
            mwpp.setCurrentFILayer();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * sets layers of the view context as visible or invisble depending on the incoming request
     *
     * @param rundata
     * @param portlet
     *
     * @throws Exception
     */
    public void doSetlayers( RunData rundata, Portlet portlet )
                            throws Exception {
        try {
            Portlet port = getMapWindowPortlet( rundata, portlet );
            MapWindowPortletPerform mwpp = new MapWindowPortletPerform( rundata.getRequest(), port,
                                                                        rundata.getServletContext() );
            mwpp.setLayers();
            mwpp.setBoundingBoxFromBBOXParam();
            mwpp.setCurrentFILayer();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * moves the layer passed through by the HTTP request up for one position
     *
     * @param rundata
     * @param portlet
     * @throws Exception
     */
    public void doMoveup( RunData rundata, Portlet portlet )
                            throws Exception {
        try {
            Portlet port = getMapWindowPortlet( rundata, portlet );
            MapWindowPortletPerform mwpp = new MapWindowPortletPerform( rundata.getRequest(), port,
                                                                        rundata.getServletContext() );
            mwpp.setLayers();
            mwpp.moveUp();
            mwpp.setBoundingBoxFromBBOXParam();
            mwpp.setCurrentFILayer();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * moves the layer passed through by the HTTP request down for one position
     *
     * @param rundata
     * @param portlet
     * @throws Exception
     */
    public void doMovedown( RunData rundata, Portlet portlet )
                            throws Exception {
        try {
            Portlet port = getMapWindowPortlet( rundata, portlet );
            MapWindowPortletPerform mwpp = new MapWindowPortletPerform( rundata.getRequest(), port,
                                                                        rundata.getServletContext() );
            mwpp.setLayers();
            mwpp.moveDown();
            mwpp.setBoundingBoxFromBBOXParam();
            mwpp.setCurrentFILayer();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * move the map view (just bounding box) to the next entry in the history
     *
     * @param rundata
     * @param portlet
     * @throws Exception
     */
    public void doHistoryforward( RunData rundata, Portlet portlet )
                            throws Exception {
        try {
            Portlet port = getMapWindowPortlet( rundata, portlet );
            MapWindowPortletPerform mwpp = new MapWindowPortletPerform( rundata.getRequest(), port,
                                                                        rundata.getServletContext() );
            mwpp.setLayers();
            mwpp.setCurrentFILayer();
            mwpp.doHistoryforward();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * move the map view (just bounding box) to the previous entry in the history
     *
     * @param rundata
     * @param portlet
     * @throws Exception
     */
    public void doHistorybackward( RunData rundata, Portlet portlet )
                            throws Exception {
        try {
            Portlet port = getMapWindowPortlet( rundata, portlet );
            MapWindowPortletPerform mwpp = new MapWindowPortletPerform( rundata.getRequest(), port,
                                                                        rundata.getServletContext() );
            mwpp.setLayers();
            mwpp.setCurrentFILayer();
            mwpp.doHistorybackward();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param rundata
     * @param portlet
     * @throws Exception
     */
    public void doAddows( RunData rundata, Portlet portlet )
                            throws Exception {
        try {
            Portlet port = getMapWindowPortlet( rundata, portlet );
            MapWindowPortletPerform mwpp = new MapWindowPortletPerform( rundata.getRequest(), port,
                                                                        rundata.getServletContext() );
            mwpp.doAddows();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param rundata
     * @param portlet
     * @throws Exception
     */
    public void doRemoveows( RunData rundata, Portlet portlet )
                            throws Exception {
        try {
            Portlet port = getMapWindowPortlet( rundata, portlet );
            MapWindowPortletPerform mwpp = new MapWindowPortletPerform( rundata.getRequest(), port,
                                                                        rundata.getServletContext() );
            mwpp.doRemoveows();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * selects the current context of a MapWindowPortlet
     *
     * @param data
     * @param portlet
     * @throws PortalException
     */
    public void doSelectwmc( RunData data, Portlet portlet )
                            throws PortalException {
        try {
            Portlet port = portlet.getPortletConfig().getPortletSet().getPortletByName( "iGeoPortal:SelectWMCPortlet" );
            SelectWMCPortletPerform swp = new SelectWMCPortletPerform( data.getRequest(), port,
                                                                       data.getServletContext() );
            swp.doSelectwmc();
        } catch ( Exception e ) {
            e.printStackTrace();
            LOG.logError( e.getMessage(), e );
            throw new PortalException( e.getMessage() );
        }
    }

    /**
     * selects the current context of a MapWindowPortlet
     *
     * @param data
     * @param portlet
     * @throws PortalException
     */
    public void doLoadwmc( RunData data, Portlet portlet )
                            throws PortalException {
        try {
            Portlet port = portlet.getPortletConfig().getPortletSet().getPortletByName( "iGeoPortal:SelectWMCPortlet" );
            SelectWMCPortletPerform swp = new SelectWMCPortletPerform( data.getRequest(), port,
                                                                       data.getServletContext() );
            User user = data.getUser();
            org.deegree.security.drm.model.User du = null;
            if ( !"anon".equals( user.getUserName() ) ) {
                du = new org.deegree.security.drm.model.User( 1, user.getUserName(), user.getPassword(),
                                                              user.getFirstName(), user.getLastName(), user.getEmail(),
                                                              null );
            }
            swp.doLoadwmc( du );
        } catch ( Exception e ) {
            e.printStackTrace();
            LOG.logError( e.getMessage(), e );
            throw new PortalException( e.getMessage() );
        }
    }

    /**
     * Changes the scale.
     *
     * @param data
     * @param portlet
     * @throws PortalException
     */
    public void doChangescale( RunData data, Portlet portlet )
                            throws PortalException {
        try {
            Portlet port = portlet.getPortletConfig().getPortletSet().getPortletByName(
                                                                                        "iGeoPortal:ScaleChooserPortlet" );
            ScaleChooserPortletPerform swp = new ScaleChooserPortletPerform( data.getRequest(), port,
                                                                             data.getServletContext() );
            swp.doChangeScale();
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new PortalException( e.getMessage() );
        }
    }
}
