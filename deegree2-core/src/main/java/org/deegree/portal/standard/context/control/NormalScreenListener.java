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

package org.deegree.portal.standard.context.control;

import java.awt.Rectangle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;

import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCMember;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.MapUtils;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.i18n.Messages;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;
import org.deegree.portal.Constants;
import org.deegree.portal.PortalException;
import org.deegree.portal.context.ViewContext;
import org.deegree.portal.context.WebMapContextFactory;
import org.deegree.portal.context.XMLFactory;

/**
 * This class converts the fullScreen to normalScreen view. It loads the old view from the httpSession and applies to it
 * the manipulations the user did in fullScreen view
 *
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author: elmasri$
 *
 * @version $Revision$, $Date: 1 Jun 2007 13:50:40$
 */

public class NormalScreenListener extends AbstractContextListener {

    private static final ILogger LOG = LoggerFactory.getLogger( ContextSwitchListener.class );

    protected static final String DEFAULT_CTXT2HTML = "WEB-INF/conf/igeoportal/context2HTML.xsl";

    private static final String NORMALSCREEN_MAPCONTEXT = "NormalScreenMapContext";

    private static String xslFilename = null;

    private HttpSession session = null;

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.enterprise.control.AbstractListener#actionPerformed(org.deegree.enterprise.control.FormEvent)
     */
    @Override
    public void actionPerformed( FormEvent event ) {

        RPCWebEvent rpc = (RPCWebEvent) event;

        try {
            validate( rpc );
            initialize();
        } catch ( PortalException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            gotoErrorPage( e.getMessage() );
            return;
        }

        try {
            ViewContext vc = setToNormalScreen( rpc );
            setCurrentContext( vc );
        } catch ( Exception e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            gotoErrorPage( e.getMessage() );
            return;
        }

    }

    /**
     * ValidateRPC looks in the RPCStruct for all needed elements and gotoErrorPage in case an element is found or
     * wronglz formated, this is useful so that we won't need to check later for anz variables, simply get them and
     * start Working
     *
     * @param rpc
     * @throws PortalException
     */
    protected void validate( RPCWebEvent rpc )
                            throws PortalException {

        RPCStruct struct = extractRPCStruct( rpc, 0 );

        RPCMember layerListRPC = struct.getMember( "layerList" );
        validateLayerList( layerListRPC );

        RPCMember bboxRpc = struct.getMember( "boundingBox" );
        validateBBox( bboxRpc );
    }

    /**
     * Initializes the global variables of the class
     *
     * @throws PortalException
     */
    protected void initialize()
                            throws PortalException {
        try {
            session = ( (HttpServletRequest) this.getRequest() ).getSession();
            xslFilename = "file://" + getHomePath() + DEFAULT_CTXT2HTML;
        } catch ( Exception e ) {
            LOG.logError( e.getMessage() );
            throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_ERROR_INITIALIZE", e ) );
        }
    }

    /**
     *
     * @param rpc
     *            The RPCWebEvent that contains a struct with the necessary information about the current context
     * @return the viewcontext with the current changes
     */
    protected ViewContext setToNormalScreen( RPCWebEvent rpc )
                            throws PortalException {

        RPCStruct struct = extractRPCStruct( rpc, 0 );
        ViewContext vc = getViewContextFromSession();

        RPCMember[] layerList = ( (RPCStruct) struct.getMember( "layerList" ).getValue() ).getMembers();
        if ( layerList != null ) {
            try {
                changeLayerList( vc, layerList );
            } catch ( PortalException e ) {
                LOG.logError( e.getMessage() );
                throw new PortalException( e.getMessage() );
            }
        }

        RPCStruct bboxStruct = (RPCStruct) struct.getMember( "boundingBox" ).getValue();
        Envelope envelope = extractBBox( bboxStruct, vc.getGeneral().getBoundingBox()[0].getCoordinateSystem() );
        // HINT: Please don't remove. Its another mechanism to set the map to normal view
        // RPCStruct mapSizeRPC = (RPCStruct) struct.getMember( "mapSize" ).getValue();
        // Point mapSize = getMapSize( mapSizeRPC, envelope.getCoordinateSystem() );
        // setNormalSizeScreen( vc, envelope, mapSize );
        vc = createNormalScreenContext( vc, envelope );

        return vc;
    }

    /**
     * Takes in viewContext converts it to a html of the viewcontext and display it
     *
     * @param vc
     *            the ViewContext after convert
     */
    protected void setCurrentContext( ViewContext vc )
                            throws ParserConfigurationException, PortalException {

        // doing the transformation
        try {
            XMLFragment xml = XMLFactory.export( vc );
            String newHtml = transformToHtmlMapContext( xml, xslFilename );
            session.setAttribute( ContextSwitchListener.NEW_CONTEXT_HTML, newHtml );
        } catch ( ParserConfigurationException e ) {
            LOG.logError( e.getMessage() );
            throw new ParserConfigurationException( Messages.getMessage( "IGEO_STD_CNTXT_ERROR_TRANSFORM" ) );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage() );
            throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_ERROR_PARSE_XSL", xslFilename ) );
        }
    }

    /**
     * Validates the data in the struct to make sure it contains a valid layerList
     *
     * @param layerListRPCMember
     * @throws PortalException
     */
    protected void validateLayerList( RPCMember layerListRPCMember )
                            throws PortalException {

        if ( layerListRPCMember == null || layerListRPCMember.getValue() == null ) {
            throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_MISSING_RPC_MEMBER", "layerList" ) );
        }

        RPCMember[] layerList = null;
        try {
            layerList = ( (RPCStruct) layerListRPCMember.getValue() ).getMembers();
        } catch ( Exception e ) {
            LOG.logError( e.getMessage() );
            throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_WRONG_RPC_MEMBER_VALUE", "layerList" ) );
        }

        if ( layerList == null || layerList.length < 1 ) {
            throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_ERROR_EMPTY_LAYERLIST", "layerList" ) );
        }
    }

    /**
     * Validates the data in the struct to make sure it contains a valid bbox
     *
     * @param bboxRPCMember
     * @throws PortalException
     */
    protected void validateBBox( RPCMember bboxRPCMember )
                            throws PortalException {

        if ( bboxRPCMember == null || bboxRPCMember.getValue() == null ) {
            throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_MISSING_RPC_MEMBER", "boundingBox" ) );
        }
        RPCStruct bboxStruct = (RPCStruct) bboxRPCMember.getValue();
        Double minx = null;
        Double miny = null;
        Double maxx = null;
        Double maxy = null;

        RPCMember mem = bboxStruct.getMember( Constants.RPC_BBOXMINX );
        if ( mem != null ) {
            minx = (Double) mem.getValue();
        }
        mem = bboxStruct.getMember( Constants.RPC_BBOXMINY );
        if ( mem != null ) {
            miny = (Double) mem.getValue();
        }

        mem = bboxStruct.getMember( Constants.RPC_BBOXMAXX );
        if ( mem != null ) {
            maxx = (Double) mem.getValue();
        }

        mem = bboxStruct.getMember( Constants.RPC_BBOXMAXY );
        if ( mem != null ) {
            maxy = (Double) mem.getValue();
        }

        if ( minx == null || maxx == null || miny == null || maxy == null ) {
            throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_WRONG_RPC_MEMBER_VALUE", "boundingBox" ) );
        }
        if ( minx >= maxx || miny >= maxy ) {
            throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_ERROR_BBOX_BOUNDARIES" ) );
        }
    }

    /**
     * Extracts the Xml of NORMALSCREEN_MAPCONTEXT from the session and transforms it to a ViewContext
     *
     * @return The stored actual context
     * @throws PortalException
     */
    protected ViewContext getViewContextFromSession()
                            throws PortalException {

        try {
            XMLFragment xml = (XMLFragment) session.getAttribute( NORMALSCREEN_MAPCONTEXT );
            String sessionId = (String) session.getAttribute( "SESSIONID" );
            return WebMapContextFactory.createViewContext( xml, null, sessionId );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage() );
            throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_ERROR_CREATE_VC" ) );
        }
    }

    /**
     * Extracts the width and height of the map and returns a point with the coordinates system Please don't delete it.
     * It is used with the commneted code in setNormalSizeScreen()
     *
     * @param mapSize
     * @param crs
     * @return
     */
    // private Point getMapSize( RPCStruct mapSize, CoordinateSystem crs ) {
    //
    // Double width = (Double) mapSize.getMember( "width" ).getValue();
    // Double height = (Double) mapSize.getMember( "height" ).getValue();
    //
    // return GeometryFactory.createPoint( width, height, crs );
    // }

    /**
     * This Method adapt the normal size screen context with the new changes applied in the full screen view, ex.
     * changing zoom , or add new layers
     *
     * @param vc
     * @param newbBox
     */
    protected ViewContext createNormalScreenContext( ViewContext vc, Envelope newbBox )
                            throws PortalException {

        //         void setNormalSizeScreen( ViewContext vc, Envelope newbBox, Point mapSize ) {
        //         // HINT: Please don't delete.
        //         // This is another mechanism (still not 100% correct) to resize the map
        //         // Since I have the doubt that the function ensureAspectRatio() can be unprecise in some cases It
        //         // happens with ensureAspectRatio that the map is
        //         // shifted a little bit upwards, and not exactly centerd in the middle of the mapView
        //         // This is the middle point of the full screen map
        //
        //         double midX = ( newbBox.getMax().getX() - newbBox.getMin().getX() )/2 ; double midY = (
        //         newbBox.getMax().getY() - newbBox.getMin().getY() )/2;
        //
        //         // This is the middle point of the normal size map
        //         Point [] points = vc.getGeneral().getBoundingBox();
        //         General general = vc.getGeneral();
        //
        //         Envelope env = GeometryFactory.createEnvelope( points[0].getX(), points[0].getY(), points[1].getX(), points[1].getY(), points[0].getCoordinateSystem());
        //         double fullSizeScale = MapUtils.calcScale((int)mapSize.getX(), (int)mapSize.getY(), newbBox, newbBox.getCoordinateSystem(), 1 );
        //         double normalSizeScale = MapUtils.calcScale( general.getWindow().width, general.getWindow().height, env, env.getCoordinateSystem(), 1 );
        //         Envelope scaledNormalSize = MapUtils.scaleEnvelope( env, normalSizeScale, fullSizeScale );
        //         double halfWidth = scaledNormalSize.getWidth()/2;
        //         double halfHeight = scaledNormalSize.getHeight()/2;

        // Min point
        Point[] newPoints = new Point[2];

        //        newPoints[0] = GeometryFactory.createPoint( midX - halfWidth, midY - halfHeight,
        //        newbBox.getCoordinateSystem() );
        //        // Max Point
        //        newPoints[1] = GeometryFactory.createPoint( midX + halfWidth, midY + halfHeight, newbBox.getCoordinateSystem());
        //
        //        newPoints[0] = GeometryFactory.createPoint( scaledNormalSize.getMin().getX(),
        //        scaledNormalSize.getMin().getY(), scaledNormalSize.getCoordinateSystem() );
        //        newPoints[1] = GeometryFactory.createPoint( scaledNormalSize.getMax().getX(), scaledNormalSize.getMax().getY(), scaledNormalSize.getCoordinateSystem() );

        Rectangle rect = vc.getGeneral().getWindow();
        Envelope env1 = MapUtils.ensureAspectRatio( newbBox, rect.getWidth(), rect.getHeight() );

        // changeBBox( vc, env1);

        // remove rest of method!
        newPoints[0] = GeometryFactory.createPoint( env1.getMin().getX(), env1.getMin().getY(),
                                                    env1.getCoordinateSystem() );
        newPoints[1] = GeometryFactory.createPoint( env1.getMax().getX(), env1.getMax().getY(),
                                                    env1.getCoordinateSystem() );

        try {
            vc.getGeneral().setBoundingBox( newPoints );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage() );
            throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_ERROR_SET_BBOX" ) );
        }
        return vc;
    }

}
