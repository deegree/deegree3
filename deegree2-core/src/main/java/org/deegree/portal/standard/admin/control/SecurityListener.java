//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.portal.standard.admin.control;

import java.io.File;
import java.util.Properties;

import org.deegree.enterprise.control.ajax.AbstractListener;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.io.IODocument;
import org.deegree.io.JDBCConnection;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.drm.SecurityAccessManager;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class SecurityListener extends AbstractListener {

    private static ILogger LOG = LoggerFactory.getLogger( SecurityListener.class );

    protected SecurityAccessManager manager;

    protected static String secAdminPassword = "JOSE67";

    /**
     * @param driver
     * @param logon
     * @param user
     * @param password
     * @throws Exception
     */
    protected void setUp( )
                            throws Exception {
        if ( getInitParameter( "secAdminPassword" ) != null ) {
            secAdminPassword = getInitParameter( "secAdminPassword" );
        }
        String s = getHomePath()  + "WEB-INF/conf/igeoportal/securityDatabase.xml";
        LOG.logDebug( "path to securityDatabase.xml", s  );
        XMLFragment xml = new XMLFragment( new File( s ) );
        IODocument io = new IODocument( xml.getRootElement() );
        JDBCConnection jdbc = io.parseJDBCConnection();

        Properties properties = new Properties();
        properties.setProperty( "driver", jdbc.getDriver() );
        properties.setProperty( "url", jdbc.getURL() );
        properties.setProperty( "user", jdbc.getUser() );
        properties.setProperty( "password", jdbc.getPassword() );
        try {
            // has already been initialized
            manager = SecurityAccessManager.getInstance();
        } catch ( GeneralSecurityException e ) {
            LOG.logDebug( "sec DB login:", properties );
            SecurityAccessManager.initialize( "org.deegree.security.drm.SQLRegistry", properties, 60 * 1000 );
            manager = SecurityAccessManager.getInstance();
        }
    }

}
