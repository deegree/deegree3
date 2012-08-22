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

package org.deegree.enterprise.servlet;

import static org.deegree.framework.log.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.Enumeration;
import java.util.TreeMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deegree.framework.log.ILogger;

/**
 * To configure the <code>RedirectServlet</code>, have the tomcat match everything (/*) for this
 * servlet. Add the following init parameters:
 *
 * <ul>
 * <li>defaultLocation: set it to the location to be used if no rule matches</li>
 * <li>rule&lt;xxx&gt: set the value to a String to match, followed by ",", followed by
 * the String to replace it with</li>
 * </ul>
 *
 * Using a log4j logging level of debug will output all redirections that take place.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class RedirectServlet extends HttpServlet {

    private static final long serialVersionUID = -8507813259962496365L;

    private static final ILogger LOG = getLogger( RedirectServlet.class );

    private ServletConfig config;

    private TreeMap<String, String> redirections;

    private TreeMap<String, String> redirectionOrder;

    private String defaultLocation;

    @Override
    public void destroy() {
        config = null;
    }

    @Override
    public ServletConfig getServletConfig() {
        return config;
    }

    @Override
    public String getServletInfo() {
        return "Simple redirection servlet";
    }

    @Override
    public void init( ServletConfig config )
                            throws ServletException {
        this.config = config;
        redirections = new TreeMap<String, String>();
        redirectionOrder = new TreeMap<String, String>();

        Enumeration<?> enumer = config.getInitParameterNames();
        while ( enumer.hasMoreElements() ) {
            String name = (String) enumer.nextElement();
            if ( name.equalsIgnoreCase( "defaultlocation" ) ) {
                defaultLocation = config.getInitParameter( name );
            }

            if ( name.toLowerCase().startsWith( "rule" ) ) {
                String[] r = config.getInitParameter( name ).split( "," );
                redirections.put( r[0], r[1] );
                redirectionOrder.put( name.toLowerCase(), r[0] );
                if ( LOG.isDebug() ) {
                    LOG.logDebug( "Adding rule " + r[0] + " -> " + r[1] );
                }
            }
        }

        if ( defaultLocation == null ) {
            LOG.logWarning( "No default location given. I hope your rules match all possible URLs." );
        }
    }

    @Override
    public void service( ServletRequest request, ServletResponse response )
                            throws ServletException, IOException {
        HttpServletRequest r = (HttpServletRequest) request;

        String req = r.getRequestURL().toString() + ( r.getQueryString() == null ? "" : ( "?" + r.getQueryString() ) );

        String location = defaultLocation;

        // use the first match
        for ( String rule : redirectionOrder.keySet() ) {
            String p = redirectionOrder.get( rule );
            if ( req.indexOf( p ) != -1 ) {
                LOG.logDebug( "Rule with key " + p + " matches." );
                location = req.replace( p, redirections.get( p ) );
                break;
            }
        }

        if ( LOG.isDebug() ) {
            LOG.logDebug( "Redirecting " + req + " to " + location );
        }

        // do the redirect
        HttpServletResponse httpResponse = ( (HttpServletResponse) response );
        httpResponse.setHeader( "Location", location );
        httpResponse.setStatus( 302 );
    }

}
