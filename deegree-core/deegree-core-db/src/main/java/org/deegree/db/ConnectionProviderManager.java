//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.db;

import static java.sql.DriverManager.deregisterDriver;
import static java.sql.DriverManager.getDrivers;
import static java.sql.DriverManager.registerDriver;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.ServiceLoader;

import org.deegree.commons.jdbc.DriverWrapper;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultResourceManager;
import org.deegree.workspace.standard.DefaultResourceManagerMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class ConnectionProviderManager extends DefaultResourceManager<ConnectionProvider> {

    private static final Logger LOG = LoggerFactory.getLogger( ConnectionProviderManager.class );

    public ConnectionProviderManager() {
        super( new DefaultResourceManagerMetadata<ConnectionProvider>( ConnectionProviderProvider.class,
                                                                       "database connections", "jdbc" ) );
    }

    @Override
    public void init( Workspace workspace ) {
        try {
            for ( Driver d : ServiceLoader.load( Driver.class, workspace.getModuleClassLoader() ) ) {
                registerDriver( new DriverWrapper( d ) );
                LOG.info( "Found and loaded {}", d.getClass().getName() );
            }
        } catch ( SQLException e ) {
            LOG.debug( "Unable to load driver: {}", e.getLocalizedMessage() );
        }
        super.init( workspace );
    }

    @Override
    public void destroy() {
        Enumeration<Driver> enumer = getDrivers();
        while ( enumer.hasMoreElements() ) {
            Driver d = enumer.nextElement();
            if ( d instanceof DriverWrapper ) {
                try {
                    deregisterDriver( d );
                } catch ( SQLException e ) {
                    LOG.debug( "Unable to deregister driver: {}", e.getLocalizedMessage() );
                }
            }
        }
    }

}
