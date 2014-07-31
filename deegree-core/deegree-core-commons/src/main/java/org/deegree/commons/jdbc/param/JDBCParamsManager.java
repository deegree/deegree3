//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.commons.jdbc.param;

import static java.sql.DriverManager.registerDriver;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.sql.Connection;
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

import org.deegree.commons.config.AbstractResourceManager;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.DefaultResourceManagerMetadata;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceManagerMetadata;
import org.deegree.commons.jdbc.DriverWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the {@link JDBCParams} resources in a {@link DeegreeWorkspace}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 */
@SuppressWarnings("unchecked")
public class JDBCParamsManager extends AbstractResourceManager<JDBCParams> {

    private static Logger LOG = LoggerFactory.getLogger( JDBCParamsManager.class );

    private JDBCParamsManagerMetadata metadata;

    @Override
    public void startup( DeegreeWorkspace workspace )
                            throws ResourceInitException {
        try {
            for ( Driver d : ServiceLoader.load( Driver.class, workspace.getModuleClassLoader() ) ) {
                registerDriver( new DriverWrapper( d ) );
                registerDriver( new DriverWrapper( new org.sqlite.JDBC() ) );
                LOG.info( "Found and loaded {}", d.getClass().getName() );
            }
        } catch ( SQLException e ) {
            LOG.debug( "Unable to load driver: {}", e.getLocalizedMessage() );
        }
        System.out.println( workspace );
        super.startup( workspace );
    }

    @Override
    public void initMetadata( DeegreeWorkspace workspace ) {
        metadata = new JDBCParamsManagerMetadata( workspace );
    }

    @Override
    public ResourceManagerMetadata<JDBCParams> getMetadata() {
        return metadata;
    }

    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[0];
    }

    @Override
    protected void add( JDBCParams params )
                            throws ResourceInitException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection( params.getUrl(), params.getUser(), params.getPassword() );
        } catch ( SQLException e ) {
            throw new ResourceInitException( e.getMessage() );
        } finally {
            if ( conn != null ) {
                try {
                    conn.close();
                } catch ( SQLException e ) {
                    // nothing to do
                }
            }
        }
    }

    static class JDBCParamsManagerMetadata extends DefaultResourceManagerMetadata<JDBCParams> {
        JDBCParamsManagerMetadata( DeegreeWorkspace workspace ) {
            super( "jdbc params", "jdbc/", JDBCParamsProvider.class, workspace );
        }
    }

    @Override
    public void shutdown() {
        Enumeration<Driver> e = DriverManager.getDrivers();
        while ( e.hasMoreElements() ) {
            Driver driver = e.nextElement();
            try {
                // no need to check for class loader, the driver manager does that already
                DriverManager.deregisterDriver( driver );
                DriverManager.deregisterDriver( new org.sqlite.JDBC() );
            } catch ( SQLException e1 ) {
                LOG.error( "Cannot unload driver: " + driver );
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

        // Oracle managed beans: Enterprise to the rescue
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
