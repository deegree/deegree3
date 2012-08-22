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
package org.deegree.portal.standard.security.control;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deegree.enterprise.control.RequestDispatcher;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.io.IODocument;
import org.deegree.io.JDBCConnection;
import org.deegree.security.drm.SecurityAccessManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class SecurityRequestDispatcher extends RequestDispatcher {

    private static final long serialVersionUID = -5980332315176739271L;

    private static String SECURITY_CONFIG = "Security.configFile";

    private boolean supportManyServices = false;

    /**
     * This method initializes the servlet.
     *
     * @param cfg
     *            the servlet configuration
     * @throws ServletException
     *             an exception
     */
    @Override
    public void init( ServletConfig cfg )
                            throws ServletException {
        super.init( cfg );

        try {
            // config file -> DOM
            String s = getInitParameter( SECURITY_CONFIG );
            File file = new File( s );
            if ( !file.isAbsolute() ) {
                file = new File( getServletContext().getRealPath( s ) );
            }
            URL url = file.toURL();
            Reader reader = new InputStreamReader( url.openStream() );
            Document doc = XMLTools.parse( reader );
            Element element = doc.getDocumentElement();
            reader.close();

            // extract configuration information from DOM
            String readWriteTimeoutString = XMLTools.getStringValue( "readWriteTimeout", null, element, "600" );
            int readWriteTimeout = Integer.parseInt( readWriteTimeoutString );

            String registryClass = XMLTools.getRequiredStringValue( "registryClass", null, element );
            Element registryConfig = (Element) XMLTools.getRequiredNode( element, "registryConfig", null );

            // required: <connection>
            NamespaceContext nsc = new NamespaceContext();
            nsc.addNamespace( "jdbc", new URI( "http://www.deegree.org/jdbc" ) );
            element = (Element) XMLTools.getRequiredNode( registryConfig, "jdbc:JDBCConnection", nsc );
            IODocument xml = new IODocument( element );
            JDBCConnection jdbc = xml.parseJDBCConnection();

            Properties properties = new Properties();

            // required: <driver>
            properties.put( "driver", jdbc.getDriver() );
            // required: <logon>
            properties.put( "url", jdbc.getURL() );
            // required: <user>
            properties.put( "user", jdbc.getUser() );
            // required: <password>
            properties.put( "password", jdbc.getPassword() );

            if ( !SecurityAccessManager.isInitialized() ) {
                SecurityAccessManager.initialize( registryClass, properties, readWriteTimeout * 1000 );
            }

            Enumeration<?> enumer = cfg.getInitParameterNames();
            while ( enumer.hasMoreElements() ) {
                String key = (String) enumer.nextElement();
                if ( key.equalsIgnoreCase( "supportmanyservices" ) ) {
                    supportManyServices = cfg.getInitParameter( key ).equalsIgnoreCase( "true" );
                }
            }

        } catch ( Exception e ) {
            throw new ServletException( Messages.getMessage( "IGEO_STD_SEC_FAIL_INIT_SEC_DISPATCHER", e.getMessage() ) );
        }

    }

    @Override
    protected void service( HttpServletRequest request, HttpServletResponse response )
                            throws ServletException, IOException {
        if ( supportManyServices ) {
            request.setAttribute( "supportManyServices", "true" );
        }

        super.service( request, response );
    }

}
