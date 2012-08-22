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
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCMember;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.MapUtils;
import org.deegree.framework.util.Parameter;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;
import org.deegree.portal.Constants;
import org.deegree.portal.PortalException;
import org.deegree.portal.context.ContextException;
import org.deegree.portal.context.Frontend;
import org.deegree.portal.context.GUIArea;
import org.deegree.portal.context.General;
import org.deegree.portal.context.GeneralExtension;
import org.deegree.portal.context.Module;
import org.deegree.portal.context.ViewContext;
import org.deegree.portal.context.XMLFactory;
import org.xml.sax.SAXException;

/**
 * Convert the current view context to a full screen mode. It hides all the modules except the map and the toolbar. It
 * also keeps whatever changes were made in the original context to the full screen context
 * 
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date: 1 Jun 2007 10:12:29$
 */
public class FullScreenListener extends AbstractContextListener {

    private int width = 0;

    private Map<String, String> modulesMap = null;

    /**
     * the path to where the Web Map Context files are found
     */
    public static final String CONTEXTPATH = "WEB-INF/conf/igeoportal";

    private String xslFileName = null;

    private HttpSession session = null;

    /**
     * A <code>String</code> defining the name of the xsl file that defines the transformation from a context to html.
     * This must be placed, together with the map context xml and helper xsl files, under
     * <code>${context-home}/WEB-INF/conf/igeoportal/</code>.
     */
    protected static final String DEFAULT_CTXT2HTML = "WEB-INF/conf/igeoportal/context2HTML.xsl";

    private static final ILogger LOG = LoggerFactory.getLogger( ResetContextListener.class );

    private static final String DISPLAYED_MODULES = "DISPLAYED_MODULES";

    private static final String NORMALSCREEN_MAPCONTEXT = "NormalScreenMapContext";

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.enterprise.control.WebListener#actionPerformed(org.deegree.enterprise.control.FormEvent)
     */
    @Override
    public void actionPerformed( FormEvent event ) {

        RPCWebEvent rpc = (RPCWebEvent) event;

        try {
            validate( rpc );
            validateCntxtProperties();
            initialize();
        } catch ( PortalException e ) {
            LOG.logError( e.getMessage(), e );
            e.printStackTrace();
            gotoErrorPage( e.getMessage() );
            return;
        }

        try {
            ViewContext fullScreenContext = setToFullScreen( rpc );
            setCurrentContext( fullScreenContext );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            e.printStackTrace();
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

        RPCMember bboxRPC = struct.getMember( "boundingBox" );
        validateBBox( bboxRPC );
    }

    /**
     * This method checks the context.properties file to make sure that all fields needed are available
     * 
     */
    protected void validateCntxtProperties()
                            throws PortalException {

        String displayedModules = ContextMessages.getString( DISPLAYED_MODULES );
        if ( displayedModules == null ) {
            throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_ERROR_CONFIG_FILE",
                                                            ContextMessages.getName(), "DISPLAYED_MODULES" ) );
        }

        if ( displayedModules.indexOf( "MANDATORY_MODULE_MAP" ) < 0 ) {
            throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_ERROR_CONFIG_FILE",
                                                            ContextMessages.getName(), "MANDATORY_MODULE_MAP" ) );
        }

        if ( displayedModules.indexOf( "MANDATORY_MODULE_TOOLBAR" ) < 0 ) {
            throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_ERROR_CONFIG_FILE",
                                                            ContextMessages.getName(), "MANDATORY_MODULE_TOOLBAR" ) );
        }

        String mapName = ContextMessages.getString( "MANDATORY_MODULE_MAP" );
        if ( mapName == null ) {
            throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_MISSING_MAND_PARAM",
                                                            ContextMessages.getName(), "MANDATORY_MODULE_MAP" ) );
        }

        String toolbarName = ContextMessages.getString( "MANDATORY_MODULE_TOOLBAR" );
        if ( toolbarName == null ) {
            throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_MISSING_MAND_PARAM",
                                                            ContextMessages.getName(), "MANDATORY_MODULE_TOOLBAR" ) );
        }

        int width = Integer.parseInt( ContextMessages.getString( "MAP_WIDTH" ) );
        if ( width <= 0 ) {
            throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_ERROR_CONFIG_FILE",
                                                            ContextMessages.getName(), "MAP_WIDTH" ) );
        }

        String normalScreen = ContextMessages.getString( "NORMAL_SIZE_SCREEN" );
        if ( normalScreen == null ) {
            throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_ERROR_CONFIG_FILE",
                                                            ContextMessages.getName(), "NORMAL_SIZE_SCREEN" ) );
        }
    }

    /**
     * Initializes the global variables
     * 
     * @throws PortalException
     */
    protected void initialize()
                            throws PortalException {

        try {
            xslFileName = "file://" + getHomePath() + DEFAULT_CTXT2HTML;

            width = Integer.parseInt( ContextMessages.getString( "MAP_WIDTH" ) );
            session = ( (HttpServletRequest) this.getRequest() ).getSession();

            modulesMap = loadModulesNames( DISPLAYED_MODULES );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage() );
            throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_ERROR_INITIALIZE", e ) );
        }
    }

    /**
     * Applies the modules read from the context.properties file to the current context using the data from the session
     * on the defaultContext
     * 
     * @param rpc
     * @return the view context for full screen
     * @throws ParserConfigurationException
     * @throws PortalException
     * @throws ContextException
     */
    protected ViewContext setToFullScreen( RPCWebEvent rpc )
                            throws ParserConfigurationException, PortalException, ContextException {

        RPCStruct struct = extractRPCStruct( rpc, 0 );
        ViewContext tempContext = applyLayers( struct );

        try {
            XMLFragment xmlActualContext = XMLFactory.export( tempContext );
            // Saving the original Context
            session.setAttribute( NORMALSCREEN_MAPCONTEXT, xmlActualContext );
        } catch ( ParserConfigurationException e ) {
            LOG.logError( e.getMessage() );
            throw new ParserConfigurationException( Messages.getMessage( "IGEO_STD_CNTXT_ERROR_TRANSFORM" ) );
        }
        return createFullScreenContext( struct, tempContext );
    }

    /**
     * Takes in viewContext converts it to a html of the viewcontext and display it
     * 
     * @param vc
     *            the Viewcontext after convert
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws SAXException
     * @throws MalformedURLException
     * @throws IOException
     */
    protected void setCurrentContext( ViewContext vc )
                            throws ParserConfigurationException, TransformerException, SAXException,
                            MalformedURLException, IOException {

        try {
            XMLFragment xml = XMLFactory.export( vc );
            String newHtml = transformToHtmlMapContext( xml, xslFileName );
            session.setAttribute( ContextSwitchListener.NEW_CONTEXT_HTML, newHtml );
        } catch ( ParserConfigurationException e ) {
            LOG.logError( e.getMessage() );
            throw new ParserConfigurationException( Messages.getMessage( "IGEO_STD_CNTXT_ERROR_TRANSFORM" ) );
        }
    }

    /**
     * Gets the current map context from the session and applies the current layers to it.
     * 
     * @param struct
     * @return the viewContext after applying the current layers to it
     * @throws PortalException
     */
    private ViewContext applyLayers( RPCStruct struct )
                            throws PortalException {

        ViewContext tempContext = null;
        try {
            String sessionId = (String) session.getAttribute( "SESSIONID" );
            ViewContext vc = (ViewContext) session.getAttribute( Constants.CURRENTMAPCONTEXT );
            tempContext = vc.clone( null, sessionId );

            // Applying the layer setting to the current context
            RPCMember[] layerList = ( (RPCStruct) struct.getMember( "layerList" ).getValue() ).getMembers();
            changeLayerList( tempContext, layerList );

        } catch ( PortalException e ) {
            // throw new PortalException( Messages.getMessage(
            // "IGEO_STD_CNTXT_ERROR_EMPTY_LAYERLIST" ) );
            throw e;
        } catch ( Exception e ) {
            LOG.logError( e.getMessage() );
            throw new PortalException( Messages.getMessage( "IGEO_STD_ERROR_UNKNOWN", e ) );
        }
        return tempContext;
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
     * Extracts shown modules names from the context.properties file
     * 
     * @param selectedModules
     *            The modules names key as written in the context.properties file
     * @return Map contains all the used modules
     */
    private Map<String, String> loadModulesNames( String selectedModules ) {

        String modulesNames = ContextMessages.getString( selectedModules );
        Map<String, String> modules = new HashMap<String, String>();
        String[] tmpNames = modulesNames.split( ";" );
        for ( int i = 0; i < tmpNames.length; i++ ) {
            String value = ContextMessages.getString( tmpNames[i] );
            if ( tmpNames[i] != null && value != null ) {
                modules.put( tmpNames[i], value );
            }
        }
        return modules;
    }

    /**
     * Sets the given context to FullScreen mode.
     * 
     * @param struct
     *            the rpc struct containing the current bbox envelope
     * @param vc
     *            view context with the layers
     * @return The context transformed to the fullScreen size
     * @throws ContextException
     * @throws PortalException
     */
    protected ViewContext createFullScreenContext( RPCStruct struct, ViewContext vc )
                            throws ContextException, PortalException {

        RPCStruct bBoxStruct = (RPCStruct) struct.getMember( "boundingBox" ).getValue();
        Envelope envelope = extractBBox( bBoxStruct, null );

        String sessionID = (String) session.getAttribute( "SESSIONID" );
        ViewContext fullscreenContext = null;
        try {
            fullscreenContext = vc.clone( null, sessionID );
        } catch ( Exception e ) {
            throw new ContextException( "The view context couldn't be cloned" );
        }
        General general = fullscreenContext.getGeneral();
        GeneralExtension extension = general.getExtension();
        Frontend frontEnd = extension.getFrontend();

        // The bbox values are changed here
        Point[] points = general.getBoundingBox();
        // if there are no 2 points, then nothing happens
        if ( points.length == 2 ) {
            Rectangle rect = general.getWindow();
            rect.width = width; // setting new window width
            CoordinateSystem crs = points[0].getCoordinateSystem();
            Envelope bbox = MapUtils.ensureAspectRatio( envelope, rect.width, rect.height );

            Point[] newPoints = new Point[2];
            newPoints[0] = GeometryFactory.createPoint( bbox.getMin(), crs );
            newPoints[1] = GeometryFactory.createPoint( bbox.getMax(), crs );

            general.setBoundingBox( newPoints );
        } else {
            throw new ContextException( Messages.getMessage( "IGEO_STD_CNTXT_ERROR_VIEWCONTEXT", "bounding box" ) );
        }

        // Here removing all the side modules
        List<GUIArea> contextAreas = new LinkedList<GUIArea>();
        // Makign sure we fetched the names right, otherwise we don't have the names to compare with

        // Adding GuiAreas to a list to do a for loop
        // instead of accessing them individually
        GUIArea curArea = null;
        if ( ( curArea = frontEnd.getNorth() ) != null ) {
            contextAreas.add( curArea );
        }
        if ( ( curArea = frontEnd.getSouth() ) != null ) {
            contextAreas.add( curArea );
        }
        if ( ( curArea = frontEnd.getEast() ) != null ) {
            contextAreas.add( curArea );
        }
        if ( ( curArea = frontEnd.getWest() ) != null ) {
            contextAreas.add( curArea );
        }
        if ( ( curArea = frontEnd.getCenter() ) != null ) {
            contextAreas.add( curArea );
        }

        Iterator<GUIArea> it = contextAreas.iterator();

        // We now know for sure, that will find the map and the toolbar
        // since we already called validateCntxtProperties()
        boolean mapFound = false;
        boolean toolbarFound = false;
        while ( it.hasNext() ) {
            GUIArea area = it.next();
            boolean found = false;
            Module[] modules = area.getModules();
            for ( int i = 0; i < modules.length; i++ ) {
                // If the module name was in the list of shownModules then keep it
                if ( modulesMap.containsValue( modules[i].getName() ) ) {
                    found = true;
                    if ( modules[i].getName().compareTo( modulesMap.get( "MANDATORY_MODULE_MAP" ) ) == 0 ) {
                        // The map view or the tool bar has been found
                        modules[i].setWidth( Integer.toString( width ) );
                        mapFound = true;
                    }
                    if ( modules[i].getName().compareTo( modulesMap.get( "MANDATORY_MODULE_TOOLBAR" ) ) == 0 ) {
                        modules[i].setWidth( Integer.toString( width ) );
                        toolbarFound = true;
                    }
                    continue;
                }
                modules[i].setHidden( true );
            }
            if ( !found ) {
                area.setArea( 0 );
                area.setHidden( true );
            }
        }
        if ( !mapFound ) {
            throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_MISSING_MODULE",
                                                            modulesMap.get( "MANDATORY_MODULE_MAP" ) ) );
        }
        if ( !toolbarFound ) {
            throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_MISSING_MODULE",
                                                            modulesMap.get( "MANDATORY_MODULE_TOOLBAR" ) ) );
        }

        // Changing the full screen button to normal size screen button
        Module toolBar = frontEnd.getCenter().getModule( "Toolbar" );
        if ( toolBar != null ) {
            String[] removedBtns = ContextMessages.getString( "REMOVED_BUTTONS" ).split( ";" );

            // Remove only the parameters that exist in the hash map
            String[] names = toolBar.getParameter().getParameterNames();
            for ( int i = 0; i < names.length; i++ ) {
                for ( int j = 0; j < removedBtns.length; j++ ) {
                    if ( names[i].indexOf( removedBtns[j] ) >= 0 ) {
                        toolBar.getParameter().removeParameter( names[i] );
                    }
                }
            }
        }

        Parameter param = new Parameter( ContextMessages.getString( "NORMAL_SIZE_SCREEN" ), "PushButton" );
        toolBar.addParameter( param );

        return fullscreenContext;
    }
}
