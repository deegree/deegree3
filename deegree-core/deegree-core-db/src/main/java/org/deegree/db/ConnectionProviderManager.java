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

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.ServiceLoader;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.deegree.commons.jdbc.DriverWrapper;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultResourceManager;
import org.deegree.workspace.standard.DefaultResourceManagerMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource manager for connection providers.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class ConnectionProviderManager extends DefaultResourceManager<ConnectionProvider> {

    private static final Logger LOG = LoggerFactory.getLogger( ConnectionProviderManager.class );

    private Workspace workspace;

    public ConnectionProviderManager() {
        super( new DefaultResourceManagerMetadata<ConnectionProvider>( ConnectionProviderProvider.class,
                                                                       "database connections", "jdbc" ) );
    }

    @Override
    public void startup( Workspace workspace ) {
        this.workspace = workspace;
        try {
            for ( Driver d : ServiceLoader.load( Driver.class, workspace.getModuleClassLoader() ) ) {
                registerDriver( new DriverWrapper( d ) );
                LOG.info( "Found and loaded {}", d.getClass().getName() );
            }
        } catch ( SQLException e ) {
            LOG.debug( "Unable to load driver: {}", e.getLocalizedMessage() );
        }
        super.startup( workspace );
    }

    @Override
    public void shutdown() {
        // unload drivers
        Enumeration<Driver> enumer = getDrivers();
        while ( enumer.hasMoreElements() ) {
            Driver d = enumer.nextElement();
            try {
                deregisterDriver( d );
            } catch ( SQLException e ) {
                LOG.debug( "Unable to deregister driver: {}", e.getLocalizedMessage() );
            }
        }

        // manually remove drivers via reflection if loaded by module class loader (else the driver manager won't let us
        // remove them)
        // Yes, this is DangerousStuff.
        try {
            List<Object> toRemove = new ArrayList<Object>();
            Field f = DriverManager.class.getDeclaredField( "registeredDrivers" );
            f.setAccessible( true );
            List<?> list = (List<?>) f.get( null );
            ListIterator<?> iter = list.listIterator();
            while ( iter.hasNext() ) {
                Object o = iter.next();
                if ( o.getClass().getClassLoader() == workspace.getModuleClassLoader()
                     || o.getClass().getClassLoader() == null ) {
                    // iter.remove not supported by used list
                    toRemove.add( o );
                }
            }
            for ( Object o : toRemove ) {
                list.remove( o );
            }
        } catch ( Exception ex ) {
            // well...
        }

        // Oracle managed beans: Enterprise to the rescue (but expect classloader leaks)
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            final Hashtable<String, String> keys = new Hashtable<String, String>();
            keys.put( "type", "diagnosability" );
            keys.put( "name", cl.getClass().getName() + "@" + Integer.toHexString( cl.hashCode() ).toLowerCase() );
            mbs.unregisterMBean( new ObjectName( "com.oracle.jdbc", keys ) );
        } catch ( Exception ex ) {
            // perhaps no oracle, or other classloader
        }
        cl = workspace.getModuleClassLoader();
        try {
            final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            final Hashtable<String, String> keys = new Hashtable<String, String>();
            keys.put( "type", "diagnosability" );
            keys.put( "name", cl.getClass().getName() + "@" + Integer.toHexString( cl.hashCode() ).toLowerCase() );
            mbs.unregisterMBean( new ObjectName( "com.oracle.jdbc", keys ) );
        } catch ( Exception ex ) {
            // perhaps no oracle, or other classloader
        }
    }

}
