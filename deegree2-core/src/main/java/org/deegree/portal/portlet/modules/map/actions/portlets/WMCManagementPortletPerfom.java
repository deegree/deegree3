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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.jetspeed.portal.Portlet;
import org.deegree.enterprise.WebUtils;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.ConvenienceFileFilter;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.portal.PortalException;
import org.deegree.portal.context.ContextException;
import org.deegree.portal.context.General;
import org.deegree.portal.context.ViewContext;
import org.deegree.portal.context.WMCMetadataSurrogate;
import org.deegree.portal.context.WebMapContextFactory;
import org.deegree.portal.context.XMLFactory;
import org.deegree.portal.portlet.modules.actions.AbstractPortletPerform;
import org.deegree.portal.portlet.modules.actions.IGeoPortalPortletPerform;
import org.deegree.security.drm.model.User;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Perform class for saving WMCs
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */

public class WMCManagementPortletPerfom extends IGeoPortalPortletPerform {

    private static final ILogger LOG = LoggerFactory.getLogger( WMCManagementPortletPerfom.class );

    public static final String TITLE = "TITLE";

    public static final String DESCRIPTION = "DESCRIPTION";

    public static final String KEYWORDS = "KEYWORDS";

    public static final String FILENAME = "FILENAME";

    private static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    private static final List<WMCMetadataSurrogate> sharedContextList = new ArrayList<WMCMetadataSurrogate>( 50 );

    /**
     * constructor
     *
     * @param request
     * @param portlet
     * @param sc
     */
    WMCManagementPortletPerfom( HttpServletRequest request, Portlet portlet, ServletContext sc ) {
        super( request, portlet, sc );
    }

    /**
     * Saves the current context as a user context to a file in
     * "WEB-INF/wmc/$USER_DIR/$mapPortletID$_CURRENTWMC.xml".
     *
     * @param userName
     * @throws Exception
     */
    void doSavecontext( String userName )
                            throws Exception {

        saveContext( userName, getMapPortletID() + "_" + AbstractPortletPerform.CURRENT_WMC );

    }

    /**
     * Saves the current context as a user context to a file named by the user
     *
     * @param userName
     * @throws Exception
     */
    void doSavenamedcontext( String userName )
                            throws Exception {

        saveContext( userName, parameter.get( "CONTEXTNAME" ) );

    }

    /**
     *
     * @param userName
     * @param cntxtName
     * @throws PortalException
     * @throws ParserConfigurationException
     * @throws IOException
     */
    private void saveContext( String userName, String cntxtName )
                            throws PortalException, ParserConfigurationException, IOException {
        LOG.logDebug( "Preparing context save." );
        ViewContext vc = getCurrentViewContext( getInitParam( INIT_MAPPORTLETID ) );
        if ( vc == null ) {
            String msg = Messages.getMessage( "IGEO_PORTLET_NO_VC_AVAILABLE", getInitParam( INIT_MAPPORTLETID ) );
            LOG.logDebug( msg );
            throw new PortalException( msg );
        }

        File dir = new File( sc.getRealPath( "WEB-INF/wmc/" + userName ) );
        LOG.logDebug( "Found directory '" + dir.getAbsolutePath() + "' for user '" + userName + "'." );

        HttpSession ses = request.getSession( false );

        LOG.logDebug( "Session is '" + ses + "'." );

        // create user director if not exists
        System.out.println( dir );
        if ( !dir.exists() ) {
            System.out.println( 555 );
            dir.mkdir();
        }

        if ( ses != null ) {
            storeContext( dir, cntxtName, vc );
        }
    }

    /**
     * Saves the current context as a shared context to a file in "WEB-INF/wmc/shared".
     *
     * @param userName
     * @throws Exception
     */
    void doSaveshared( String userName )
                            throws Exception {

        ViewContext vc = getCurrentViewContext( getInitParam( INIT_MAPPORTLETID ) );
        if ( vc == null ) {
            String msg = Messages.getMessage( "IGEO_PORTLET_NO_VC_AVAILABLE", getInitParam( INIT_MAPPORTLETID ) );
            throw new PortalException( msg );
        }

        updateVC( userName, vc );
        String filename = "wmc_" + String.valueOf( System.currentTimeMillis() );

        File dir = new File( sc.getRealPath( "WEB-INF/wmc/shared/" ) );

        HttpSession ses = request.getSession( false );
        if ( ses != null && dir.exists() ) {
            storeContext( dir, filename, vc );
        }

        sharedContextList.add( WMCMetadataSurrogate.createFromWMC( filename + ".xml", vc ) );
    }

    /**
     * Updates the WMC using the info available inteh request. Currently title, abstract, keywords
     * and user (author) are update. The first three come from the request parameters TITLE,
     * ABSTRACT and KEYWORDS, and the user name come from the session.
     *
     * @param userName
     * @param vc
     * @throws ContextException
     * @throws PortalException
     */
    private void updateVC( String userName, ViewContext vc )
                            throws ContextException, PortalException {

        String title = parameter.get( TITLE );
        String _abstract = parameter.get( DESCRIPTION );
        String keywords = parameter.get( KEYWORDS );

        General general = vc.getGeneral();
        if ( userName != null && userName.length() > 0 ) {
            general.getContactInformation().setIndividualName( new String[] { userName } );
        }
        String charset = WebUtils.readCharsetFromContentType( request );
        try {
            if ( title != null ) {
                general.setTitle( URLDecoder.decode( title, charset ) );
            }
            if ( _abstract != null ) {
                general.setAbstract( URLDecoder.decode( _abstract, charset ) );
            }
            if ( keywords != null ) {
                keywords = URLDecoder.decode( keywords, charset );
                String kwords[] = keywords.split( "," );
                general.setKeywords( kwords );
            }
        } catch ( UnsupportedEncodingException e ) {
            LOG.logError( e.getMessage(), e );
            String msg = Messages.getMessage( "IGEO_PORTLET_CONEXTMNG_DECODING", charset );
            throw new PortalException( msg, e );
        }

    }

    /**
     *
     * @param dir
     * @param name
     * @param context
     * @throws ParserConfigurationException
     * @throws IOException
     */
    private void storeContext( File dir, String name, ViewContext context )
                            throws ParserConfigurationException, IOException {

        XMLFragment xml = XMLFactory.export( context );

        File file = new File( dir.getAbsolutePath() + '/' + name + ".xml" );

        LOG.logDebug( "Saving context to '" + file.getAbsolutePath() + "'." );

        String charset = WebUtils.readCharsetFromContentType( request );
        OutputStreamWriter osw = new OutputStreamWriter( new FileOutputStream( file ), charset );
        xml.write( osw );
        osw.close();
    }

    /**
     * Loads a context from a file pointed to by the request parameter "wmc_filename". The file must
     * be in the "WEB-INF/wmc/shared/" directory.
     *
     * @throws Exception
     */
    void doLoadcontext()
                            throws Exception {

        String wmcFilename = request.getParameter( "wmc_filename" );
        File file = new File( sc.getRealPath( "WEB-INF/wmc/shared/" + wmcFilename ) );
        loadContext( file );

    }

    /**
     * Loads a context from a file pointed to by the request parameter "wmc_filename". The file must
     * be in the "WEB-INF/wmc/$USER_NAME$/" directory.
     *
     * @param userName
     *            name of the user who loads a context
     * @throws Exception
     */
    void doLoadnamedcontext( String userName )
                            throws Exception {

        String wmcFilename = request.getParameter( "wmc_filename" );
        File file = new File( sc.getRealPath( "WEB-INF/wmc/" + userName + '/' + wmcFilename ) );
        loadContext( file );

    }

    /**
     * loads a context
     *
     * @param file
     *            context file name
     * @throws IOException
     * @throws XMLParsingException
     * @throws ContextException
     * @throws SAXException
     * @throws UnknownCRSException
     * @throws MalformedURLException
     */
    private void loadContext( File file )
                            throws IOException, XMLParsingException, ContextException, SAXException,
                            UnknownCRSException, MalformedURLException {
        User u = null;
        String session = parameter.get( "PARAM_SESSIONID" );

        String msg = StringTools.concat( 300, "Loading context from '", file.getAbsolutePath(), "' using user '", u,
                                         "' and sessionID '", session, "'." );
        LOG.logDebug( msg );

        ViewContext vc = WebMapContextFactory.createViewContext( file.toURI().toURL(), u, session );

        setCurrentMapContext( vc, getInitParam( INIT_MAPPORTLETID ) );
    }

    /**
     *
     * @return shared context list
     */
    public static List<WMCMetadataSurrogate> getSharedContextList() {
        return sharedContextList;
    }

    @Override
    public void buildNormalContext()
                            throws PortalException {
        super.buildNormalContext();

        // TODO
        // remove this from buildNormalContext()
        if ( sharedContextList.size() == 0 ) {
            String s = sc.getRealPath( "WEB-INF/wmc/shared/" );
            File directory = new File( s );

            ConvenienceFileFilter cff = new ConvenienceFileFilter( false, "XML" );
            File[] wmcFiles = directory.listFiles( cff );

            for ( int i = 0; i < wmcFiles.length; i++ ) {
                try {
                    XMLFragment xml = new XMLFragment( wmcFiles[i].toURI().toURL() );
                    Element root = xml.getRootElement();
                    String xpath = "cntxt:General/cntxt:ContactInformation/cntxt:ContactPersonPrimary/cntxt:ContactPerson[0]";
                    String author = XMLTools.getNodeAsString( root, xpath, nsContext, "-" );
                    xpath = "cntxt:General/cntxt:Title";
                    String title = XMLTools.getNodeAsString( root, xpath, nsContext, "-" );
                    String abstract_ = XMLTools.getNodeAsString( root, "cntxt:General/cntxt:Abstract", nsContext, "-" );
                    xpath = "cntxt:General/cntxt:KeywordList/cntxt:Keyword";
                    String[] keywords = XMLTools.getNodesAsStrings( root, xpath, nsContext );

                    WMCMetadataSurrogate wmcs = new WMCMetadataSurrogate( wmcFiles[i].getName(), author, title,
                                                                          abstract_, keywords );
                    sharedContextList.add( wmcs );
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
            }
        }
    }

}
