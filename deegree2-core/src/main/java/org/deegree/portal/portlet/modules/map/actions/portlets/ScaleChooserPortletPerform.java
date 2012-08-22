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

import java.awt.Rectangle;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.jetspeed.portal.Portlet;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.MapUtils;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;
import org.deegree.portal.PortalException;
import org.deegree.portal.context.ViewContext;
import org.deegree.portal.portlet.modules.actions.IGeoPortalPortletPerform;

/**
 * This Perform class takes care of changing the WMC's bounding box based on a scale paramter. The
 * parameter is passed in the request. The paramter name is defined by the static member
 * NEW_SCALE_VALUE
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */

public class ScaleChooserPortletPerform extends IGeoPortalPortletPerform {

    private static final ILogger LOG = LoggerFactory.getLogger( ScaleChooserPortletPerform.class );

    public static final String REQUESTED_SCALE_VALUE = "REQUESTED_SCALE_VALUE";

    public static final String AVAILABLE_SCALES = "AVAILABLE_SCALES";

    // TODO make pixel size a property
    public static final double DEFAULT_PIXEL_SIZE = 0.00028;

    /**
     * private constructor
     *
     * @param request
     * @param portlet
     * @param sc
     */
    ScaleChooserPortletPerform( HttpServletRequest request, Portlet portlet, ServletContext sc ) {
        super( request, portlet, sc );
    }

    public void buildNormalContext()
                            throws PortalException {
        super.buildNormalContext();
        readInitParameter();
    }

    /**
     * TODO reads the init parameters of the portlet and build the scale list
     *
     */
    private void readInitParameter() {

        HttpSession ses = request.getSession();
        if ( ses.getAttribute( AVAILABLE_SCALES ) == null ) {
            String list = getInitParam( AVAILABLE_SCALES );
            if ( list == null ) {
                list = "10000;25000;50000;100000;500000;1000000";
            }
            String[] tmp = list.split( ";" );

            ses.setAttribute( AVAILABLE_SCALES, tmp );
        }

    }

    /**
     * This method changes the scale of the current bounding box
     *
     * @throws PortalException
     *
     */
    void doChangeScale()
                            throws PortalException {

        String newScale = parameter.get( REQUESTED_SCALE_VALUE );

        if ( newScale == null ) {
            // throw new PortalException( "No scale available. Missing " + REQUESTED_SCALE_VALUE
            // + " parameter" );
            return;
        }

        request.setAttribute( REQUESTED_SCALE_VALUE, newScale );

        ViewContext vc = getCurrentViewContext( getInitParam( INIT_MAPPORTLETID ) );
        if ( vc == null ) {
            LOG.logDebug( "No VC with PID '" + getInitParam( INIT_MAPPORTLETID ) + "'" );
            throw new PortalException( "no valid view context available through users session" );
        }

        // rad need pars from ViewCOntext
        Point p0 = vc.getGeneral().getBoundingBox()[0];
        Point p1 = vc.getGeneral().getBoundingBox()[1];
        CoordinateSystem cs = p0.getCoordinateSystem();

        Rectangle window = vc.getGeneral().getWindow();

        Envelope env = GeometryFactory.createEnvelope( p0.getX(), p0.getY(), p1.getX(), p1.getY(), cs );

        try {
            double reqScale = Double.parseDouble( newScale );

            double currentScale = MapUtils.calcScale( window.width, window.height, env, cs, DEFAULT_PIXEL_SIZE );

            // calc new envelope
            env = MapUtils.scaleEnvelope( env, currentScale, reqScale );

            vc.getGeneral().setBoundingBox( env );
            if ( parameter.get( "MODE" ) != null ) {
                vc.getGeneral().getExtension().setMode( parameter.get( "MODE" ) );
            }

            List<Envelope> history = (List<Envelope>) request.getSession().getAttribute( SESSION_HISTORY );
            int p = (Integer) request.getSession().getAttribute( SESSION_HISTORYPOSITION );
            Envelope current = history.get( p );
            if ( current == null || !current.equals( env ) ) {
                p++;
                history.add( p, env );
                request.getSession().setAttribute( IGeoPortalPortletPerform.SESSION_HISTORYPOSITION, p );
            }

        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new PortalException( e.getMessage() );
        }
    }

}
