//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.commons.config;

import static org.apache.commons.io.FileUtils.moveFile;
import static org.deegree.commons.config.ResourceState.StateType.deactivated;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.config.ResourceState.StateType;
import org.deegree.commons.utils.FileUtils;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class AbstractResourceManager<T extends Resource> extends AbstractBasicResourceManager
                                                                                                      implements
                                                                                                      ExtendedResourceManager<T> {

    private static final Logger LOG = getLogger( AbstractResourceManager.class );

    private final HashMap<String, ExtendedResourceProvider<T>> nsToProvider = new HashMap<String, ExtendedResourceProvider<T>>();

    private final HashMap<String, T> idToResource = new HashMap<String, T>();

    private String name = this.getClass().getSimpleName();

    /**
     * Called when a new {@link Resource} has been successfully initialized.
     * 
     * @param resource
     */
    protected void add( T resource ) {
        // nothing to do
    }

    /**
     * Called when a formerly active {@link Resource} is going to be destroyed.
     * 
     * @param resource
     */
    protected void remove( T resource ) {
        // nothing to do
    }

    /**
     * @return all managed resources
     */
    public Collection<T> getAll() {
        return idToResource.values();
    }

    public T create( String id, URL configUrl )
                            throws ResourceInitException {

        ResourceManagerMetadata<T> md = getMetadata();
        if ( md == null ) {
            throw new ResourceInitException( "Internal error: No metadata for resource manager class "
                                             + this.getClass().getName() + " available." );
        }

        ExtendedResourceProvider<T> provider;
        try {
            provider = getProvider( new File( configUrl.toURI() ) );
        } catch ( URISyntaxException e ) {
            LOG.error( e.getMessage(), e );
            throw new ResourceInitException( e.getMessage() );
        }
        if ( provider == null ) {
            String msg = "No {} provider for file: '{}' found. Skipping it.";
            LOG.error( msg, new Object[] { md.getName(), configUrl } );
            throw new ResourceInitException( "Creation of " + md.getName() + " via configuration file failed: " + msg );
        }
        T resource = provider.create( configUrl );
        add( resource );

        idToResource.put( id, resource );
        return resource;
    }

    public T get( String id ) {
        return idToResource.get( id );
    }

    public void shutdown() {
        for ( T t : idToResource.values() ) {
            remove( t );
            if ( t != null ) {
                t.destroy();
            }
        }
        idToResource.clear();
        nsToProvider.clear();
    }

    public void startup( DeegreeWorkspace workspace )
                            throws ResourceInitException {
        this.workspace = workspace;
        ResourceManagerMetadata<T> md = getMetadata();
        if ( md != null ) {
            for ( ResourceProvider p : md.getResourceProviders() ) {
                ( (ExtendedResourceProvider<?>) p ).init( workspace );
                nsToProvider.put( p.getConfigNamespace(), (ExtendedResourceProvider<T>) p );
            }

            dir = new File( workspace.getLocation(), md.getPath() );
            name = md.getName();
            if ( !dir.exists() ) {
                LOG.info( "No '{}' directory -- skipping initialization of {}.", md.getPath(), name );
                return;
            }
            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "Setting up {}.", name );
            LOG.info( "--------------------------------------------------------------------------------" );

            List<File> files = FileUtils.findFilesForExtensions( dir, true, "xml,ignore" );
            try {
                String dirName = dir.getCanonicalPath();
                for ( File configFile : files ) {
                    ResourceProvider provider = getProvider( configFile );
                    if ( provider != null ) {
                        String fileName = configFile.getCanonicalPath().substring( dirName.length() );
                        if ( fileName.startsWith( File.separator ) ) {
                            fileName = fileName.substring( 1 );
                        }
                        if ( fileName.endsWith( ".xml" ) ) {
                            // 4 is the length of ".xml"
                            String id = fileName.substring( 0, fileName.length() - 4 );
                            LOG.info( "Setting up {} '{}' from file '{}'...", new Object[] { name, id, fileName } );
                            try {
                                T resource = create( id, configFile.toURI().toURL() );
                                idToState.put( id,
                                               new ResourceState( id, configFile, provider, StateType.created, null ) );
                                resource.init( workspace );
                                idToState.put( id,
                                               new ResourceState( id, configFile, provider, StateType.init_ok, null ) );
                                add( resource );
                            } catch ( ResourceInitException e ) {
                                idToState.put( id,
                                               new ResourceState( id, configFile, provider, StateType.init_error, e ) );
                                LOG.error( "Error creating {}: {}", new Object[] { name, e.getMessage(), e } );
                                LOG.trace( "Stack trace: ", e );
                            } catch ( Throwable t ) {
                                idToState.put( id, new ResourceState( id, configFile, provider, StateType.init_error,
                                                                      new ResourceInitException( t.getMessage(), t ) ) );
                                LOG.error( "Error creating {}: {}", new Object[] { name, t.getMessage(), t } );
                                LOG.trace( "Stack trace: ", t );
                            }
                        } else {
                            // 7 is the length of ".ignore"
                            String id = fileName.substring( 0, fileName.length() - 7 );
                            idToState.put( id,
                                           new ResourceState( id, configFile, provider, StateType.deactivated, null ) );
                        }
                    } else {
                        LOG.info( "No provider for file '" + configFile + "' found. Ignoring." );
                    }
                }
            } catch ( IOException e ) {
                e.printStackTrace();
            }
            LOG.info( "" );
        }
    }

    @Override
    protected ExtendedResourceProvider<T> getProvider( File file ) {
        String namespace = null;
        FileInputStream is = null;
        try {
            is = new FileInputStream( file );
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( is );
            StAXParsingHelper.nextElement( xmlReader );
            namespace = xmlReader.getNamespaceURI();
            LOG.debug( "Config namespace: '" + namespace + "'" );
            xmlReader.close();
            return nsToProvider.get( namespace );
        } catch ( Throwable e ) {
            String msg = "Error determining configuration namespace for file '" + file + "'";
            LOG.error( msg );
        } finally {
            IOUtils.closeQuietly( is );
        }
        return null;
    }

    @Override
    protected void remove( String id ) {
        idToResource.remove( id );
        idToState.remove( id );
    }

    @Override
    public void activate( String id )
                            throws ResourceInitException {

        ResourceState state = getState( id );
        if ( state != null && state.getType() == deactivated ) {
            File oldFile = state.getConfigLocation();
            File newFile = new File( dir, id + ".xml" );
            try {
                moveFile( oldFile, newFile );
            } catch ( IOException e ) {
                e.printStackTrace();
                String msg = "Renaming of file '" + oldFile + "' to '" + newFile + "' failed. Activation of resource '"
                             + id + "' failed.";
                throw new ResourceInitException( msg );
            }

            String fileName = newFile.getName();
            // 4 is the length of ".xml"
            ResourceProvider provider = getProvider( newFile );
            LOG.info( "Setting up {} '{}' from file '{}'...", new Object[] { name, id, fileName } );
            try {
                T resource = create( id, newFile.toURI().toURL() );
                idToState.put( id, new ResourceState( id, newFile, provider, StateType.created, null ) );
                resource.init( workspace );
                idToState.put( id, new ResourceState( id, newFile, provider, StateType.init_ok, null ) );
                add( resource );
            } catch ( ResourceInitException e ) {
                idToState.put( id, new ResourceState( id, newFile, provider, StateType.init_error, e ) );
                LOG.error( "Error creating {}: {}", new Object[] { name, e.getMessage(), e } );
                LOG.trace( "Stack trace: ", e );
            } catch ( Throwable t ) {
                idToState.put( id, new ResourceState( id, newFile, provider, StateType.init_error,
                                                      new ResourceInitException( t.getMessage(), t ) ) );
                LOG.error( "Error creating {}: {}", new Object[] { name, t.getMessage(), t } );
                LOG.trace( "Stack trace: ", t );
            }
        }
    }

    @Override
    public void deactivate( String id )
                            throws ResourceInitException {

        ResourceState state = getState( id );
        if ( state != null && state.getType() != deactivated ) {
            File oldFile = state.getConfigLocation();
            File newFile = new File( dir, id + ".ignore" );
            try {
                moveFile( oldFile, newFile );
            } catch ( IOException e ) {
                e.printStackTrace();
                String msg = "Renaming of file '" + oldFile + "' to '" + newFile
                             + "' failed. Deactivation of resource '" + id + "' failed.";
                throw new ResourceInitException( msg );
            }

            T resource = idToResource.get( id );
            if ( resource != null ) {
                remove( resource );
                try {
                    resource.destroy();
                } catch ( Throwable t ) {
                    t.printStackTrace();
                }
            }
            ResourceProvider provider = getProvider( newFile );
            idToState.put( id, new ResourceState( id, newFile, provider, StateType.deactivated, null ) );
        }
    }
}