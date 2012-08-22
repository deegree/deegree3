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

package org.deegree.portal.portlet.enterprise;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.KVP2Map;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.portal.context.ViewContext;
import org.deegree.portal.context.XMLFactory;
import org.deegree.portal.portlet.modules.actions.IGeoPortalPortletPerform;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ContextSaveServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 8301108158529824880L;
    private static final ILogger LOG = LoggerFactory.getLogger( ContextSaveServlet.class );

    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response )
                            throws ServletException, IOException {
        this.doPost( request, response );
    }

    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response )
                            throws ServletException, IOException {

        Map<String, String> parameter = KVP2Map.toMap( request );

        String mm = parameter.get( "MAPPORTLET" );
        LOG.logDebug( "parameter: " + parameter );
        IGeoPortalPortletPerform igeo = new IGeoPortalPortletPerform( request, null, getServletContext() );
        ViewContext vc = igeo.getCurrentViewContext( mm );

        String filename = parameter.get( "FILENAME" );
        String user = parameter.get( "USER" );
        File dir = new File( getServletContext().getRealPath( Messages.getString( "WMCBasePath" ) + user ) );

        if ( !dir.exists() ) {
            // create user director if not exits
            dir.mkdir();
        }

        filename = StringTools.concat( 150, Messages.getString( "WMCBasePath" ), user, '/', filename );
        filename = getServletContext().getRealPath( filename );
        try {
            saveDocument( vc, filename );
        } catch ( ParserConfigurationException e ) {
            LOG.logError( e.getMessage(), e );
            request.setAttribute( "MESSAGE", Messages.getString( "ContextSaveServlet.2" ) );
            request.getRequestDispatcher( "/igeoportal/error.jsp" ).forward( request, response );
            return;
        } catch ( FileNotFoundException e ) {
            LOG.logError( e.getMessage(), e );
            request.setAttribute( "MESSAGE", Messages.getString( "ContextSaveServlet.3" ) );
            request.getRequestDispatcher( "/igeoportal/error.jsp" ).forward( request, response );
            return;
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
            request.setAttribute( "MESSAGE", Messages.getString( "ContextSaveServlet.4" ) );
            request.getRequestDispatcher( "/igeoportal/error.jsp" ).forward( request, response );
            return;
        }
        request.setAttribute( "MESSAGE", Messages.getString( "ContextSaveServlet.5" ) );
        request.getRequestDispatcher( "/igeoportal/message.jsp" ).forward( request, response );
    }

    /**
     *
     * @param vc
     * @param filename
     * @throws ParserConfigurationException
     * @throws IOException
     */
    private void saveDocument( ViewContext vc, String filename )
                            throws ParserConfigurationException, IOException {

        /*
         * // removes all layers that are just for highlighting selected features Layer[] layers =
         * vc.getLayerList().getLayers(); for ( int i = 0; i < layers.length; i++ ) { if (
         * layers[i].getTitle().startsWith( SelectFeaturesPerform.SELECTEDLAYERNAME ) ) {
         * vc.getLayerList().removeLayer( layers[i].getName(), null ); } }
         */

        XMLFragment xml = XMLFactory.export( vc );
        FileOutputStream fos = new FileOutputStream( filename );
        xml.write( fos );
        fos.close();

    }

}
