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

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.WebappResourceResolver;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.csw.CSWFactory;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueCapabilities;
import org.deegree.ogcwebservices.csw.manager.Manager_2_0_0;
import org.deegree.ogcwebservices.csw.manager.Manager_2_0_2;

/**
 * This class can be used to start up CSW harvesting thread when a servlet context will be
 * initialized and to free all assigned resources if it will be destroyed.<br>
 * For this it has to be registered to a servelt context making a &lt;listener&gt; entry to web.xml
 * 
 * <pre>
 *  &lt;web-app&gt;
 *   &lt;listener&gt;
 *       &lt;listener-class&gt;com.listeners.MyContextListener&lt;/listener-class&gt;
 *   &lt;/listener&gt;
 *   &lt;servlet&gt;
 *   ...
 *   &lt;/servlet&gt;
 *   &lt;servlet-mapping&gt;
 *    ...
 *   &lt;/servlet-mapping&gt;
 *  &lt;/web-app&gt;
 * &lt;pre&gt;
 * &#064;version $Revision$
 * &#064;author &lt;a href=&quot;mailto:poth@lat-lon.de&quot;&gt;Andreas Poth&lt;/a&gt;
 * &#064;author last edited by: $Author$
 * &#064;version 1.0. $Revision$, $Date$
 * 
 * @since 2.0
 * 
 */
public class CSWHarvestingContextListener implements ServletContextListener {

    private static final ILogger LOG = LoggerFactory.getLogger( CSWHarvestingContextListener.class );

    /**
     * @param event
     */
    public void contextDestroyed( ServletContextEvent event ) {
        // Manager.stopAllHarvester();
    }

    /**
     * @param event
     */
    public void contextInitialized( ServletContextEvent event ) {
        ServletContext sc = event.getServletContext();
        String s = sc.getInitParameter( "CSW.config" );
        if ( s == null ) {
            s = sc.getInitParameter( "csw.config" );
        }

        try {
            URL url = WebappResourceResolver.resolveFileLocation( s, sc, LOG );
            CSWFactory.setConfiguration( url );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new RuntimeException( Messages.getString( "CSWHarvestingContextListener.ONINIT" ) );
        }

        String version = "2.0.0";
        try {
            if ( CSWFactory.getService().getCapabilities() instanceof CatalogueCapabilities ) {
                CatalogueCapabilities caps = (CatalogueCapabilities) CSWFactory.getService().getCapabilities();
                List<String> versions = Arrays.asList( caps.getServiceIdentification().getServiceTypeVersions() );
                Collections.sort( versions );
                version = versions.get( versions.size() - 1 );
            }
        } catch ( OGCWebServiceException e ) {
            LOG.logError( Messages.getString( "CSWHarvestingContextListener.ONDETECTINGVERSION" ), e );
        }

        if ( "2.0.2".equals( version ) ) {
            Manager_2_0_2.startAllHarvester( version );
        } else {
            Manager_2_0_0.startAllHarvester( version );
        }
    }

}
