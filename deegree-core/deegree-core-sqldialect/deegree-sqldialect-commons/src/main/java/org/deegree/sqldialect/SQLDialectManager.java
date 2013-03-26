//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/deegree-core-base/src/main/java/org/deegree/sqldialect/SQLDialectManager.java $
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
package org.deegree.sqldialect;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.deegree.commons.config.AbstractBasicResourceManager;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceManagerMetadata;
import org.deegree.commons.config.ResourceProvider;
import org.deegree.commons.config.ResourceState;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.jdbc.ConnectionManager.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for {@link SQLDialect} instances.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: aschmitz $
 * 
 * @version $Revision: 31043 $, $Date: 2011-06-10 10:24:36 +0200 (Fr, 10. Jun 2011) $
 */
public class SQLDialectManager extends AbstractBasicResourceManager {

    private static final Logger LOG = LoggerFactory.getLogger( SQLDialectManager.class );

    private ServiceLoader<SQLDialectProvider> loader;

    private Map<Type, SQLDialectProvider> typeToDialectProvider;

    public SQLDialect create( final String connId )
                            throws ResourceInitException {

        final ConnectionManager mgr = workspace.getSubsystemManager( ConnectionManager.class );
        final Type connType = mgr.getType( connId );
        if ( connType == null ) {
            throw new ResourceInitException( "No JDBC connection with id '" + connId + "' defined." );
        }
        LOG.debug( "Connection type is {}.", connType );
        final SQLDialectProvider provider = typeToDialectProvider.get( connType );
        if ( provider != null ) {
            LOG.debug( "Found dialect provider {}", provider.getClass().getSimpleName() );
            return provider.create( connId, workspace );
        }
        throw new ResourceInitException( "No SQL dialect provider for connection type '" + connType + "' available." );
    }

    @SuppressWarnings("unchecked")
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { ConnectionManager.class };
    }

    public ResourceManagerMetadata<?> getMetadata() {
        return null;
    }

    public void shutdown() {
        if ( typeToDialectProvider != null ) {
            typeToDialectProvider.clear();
        }
        typeToDialectProvider = null;
    }

    public void startup( DeegreeWorkspace workspace )
                            throws ResourceInitException {
        loader = ServiceLoader.load( SQLDialectProvider.class, workspace.getModuleClassLoader() );
        this.workspace = workspace;
        initTypeToDialectProvider();
    }

    @Override
    public ResourceState activate( String id ) {
        return null;
    }

    @Override
    public ResourceState deactivate( String id ) {
        return null;
    }

    @Override
    protected ResourceProvider getProvider( URL file ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void remove( String id ) {
        // TODO Auto-generated method stub
    }

    private void initTypeToDialectProvider() {
        if ( typeToDialectProvider == null ) {
            typeToDialectProvider = new HashMap<Type, SQLDialectProvider>();
            try {
                for ( SQLDialectProvider provider : loader ) {
                    LOG.debug( "SQLDialectProvider: " + provider + ", db type: " + provider.getSupportedType() );
                    Type sqlType = provider.getSupportedType();
                    if ( typeToDialectProvider.containsKey( sqlType ) ) {
                        LOG.error( "Multiple SQLDialectProvider implementations for db type: '" + sqlType
                                   + "' on classpath -- omitting '" + provider.getClass().getName() + "'." );
                        continue;
                    }
                    typeToDialectProvider.put( sqlType, provider );
                }
            } catch ( Exception e ) {
                LOG.error( e.getMessage(), e );
            }
        }
    }

}