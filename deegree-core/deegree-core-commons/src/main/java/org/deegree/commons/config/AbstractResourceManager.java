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
import static org.deegree.commons.config.ResourceState.StateType.created;
import static org.deegree.commons.config.ResourceState.StateType.deactivated;
import static org.deegree.commons.config.ResourceState.StateType.init_error;
import static org.deegree.commons.config.ResourceState.StateType.init_ok;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.utils.FileUtils;
import org.deegree.commons.xml.stax.XMLStreamUtils;
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

    protected final HashMap<String, ExtendedResourceProvider<T>> nsToProvider = new HashMap<String, ExtendedResourceProvider<T>>();

    protected String name = this.getClass().getSimpleName();

    /**
     * Called when a new {@link Resource} has been successfully initialized.
     * 
     * @param resource
     * @throws ResourceInitException
     */
    protected void add( T resource )
                            throws ResourceInitException {
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

    @Override
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
        return resource;
    }

    @Override
    public T get( String id ) {
        ResourceState<T> state = getState( id );
        if ( state != null ) {
            return state.getResource();
        }
        return null;
    }

    @Override
    public void shutdown() {
        for ( ResourceState<T> state : getStates() ) {
            try {
                T resource = state.getResource();
                if ( resource != null ) {
                    remove( resource );
                    resource.destroy();
                }
            } catch ( Throwable t ) {
                LOG.error( t.getMessage(), t );
            }
        }
        idToState.clear();
        nsToProvider.clear();
    }

    /**
     * Override this if you need a custom order when starting up.
     * 
     * @return the list of files to process upon startup
     */
    public List<File> getFiles() {
        return FileUtils.findFilesForExtensions( dir, true, "xml,ignore" );
    }

    @Override
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

            List<File> files = getFiles();

            for ( File configFile : files ) {
                try {
                    ResourceState<T> state = processResourceConfig( configFile );
                    if ( state == null ) {
                        continue;
                    }
                    idToState.put( state.getId(), state );
                } catch ( Throwable t ) {
                    LOG.error( "Could not create resource: {}", t.getLocalizedMessage() );
                    if ( t.getCause() != null ) {
                        LOG.error( "Cause was: {}", t.getCause().getLocalizedMessage() );
                    }
                    LOG.trace( "Stack trace:", t );
                }
            }
            LOG.info( "" );
        }
    }

    @Override
    public ResourceState<T> getState( String id ) {
        return super.getState( id );
    }

    @Override
    protected ExtendedResourceProvider<T> getProvider( File file ) {
        String namespace = null;
        FileInputStream is = null;
        try {
            is = new FileInputStream( file );
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( is );
            XMLStreamUtils.nextElement( xmlReader );
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
        idToState.remove( id );
    }

    /**
     * Processes the given resource configuration file and returns the resulting resource state.
     * <p>
     * This method does not update the resource / state maps.
     * </p>
     * 
     * @param configFile
     *            configuration file, must not be <code>null</code>
     * @return resource state, can be null if resource manager does not want to handle the config
     * @throws IOException
     *             if the resource filename is invalid / could not be processed
     */
    protected ResourceState<T> processResourceConfig( File configFile )
                            throws IOException {

        LOG.debug( "Processing file '{}'", configFile );

        ResourceState<T> state = null;

        String dirName = dir.getCanonicalPath();
        String fileName = configFile.getCanonicalPath().substring( dirName.length() );

        ResourceProvider provider = getProvider( configFile );

        if ( fileName.startsWith( File.separator ) ) {
            fileName = fileName.substring( 1 );
        }
        if ( fileName.endsWith( ".xml" ) ) {
            // 4 is the length of ".xml"
            String id = fileName.substring( 0, fileName.length() - 4 );
            LOG.info( "Setting up {} '{}' from file '{}'...", new Object[] { name, id, fileName } );
            if ( provider != null ) {
                try {
                    T resource = create( id, configFile.toURI().toURL() );
                    state = new ResourceState<T>( id, configFile, provider, created, resource, null );
                    resource.init( workspace );
                    state = new ResourceState<T>( id, configFile, provider, init_ok, resource, null );
                    add( resource );
                } catch ( ResourceInitException e ) {
                    LOG.error( "Could not create resource {}: {}", name, e.getLocalizedMessage() );
                    if ( e.getCause() != null ) {
                        LOG.error( "Cause was: {}", e.getCause().getLocalizedMessage() );
                    }
                    LOG.trace( "Stack trace:", e );
                    state = new ResourceState<T>( id, configFile, provider, init_error, null, e );
                } catch ( Throwable t ) {
                    LOG.error( "Could not create resource {}: {}", name, t.getLocalizedMessage() );
                    if ( t.getCause() != null ) {
                        LOG.error( "Cause was: {}", t.getCause().getLocalizedMessage() );
                    }
                    LOG.trace( "Stack trace:", t );
                    state = new ResourceState<T>( id, configFile, provider, init_error, null,
                                                  new ResourceInitException( t.getMessage(), t ) );
                }
            } else {
                String msg = "No suitable resource provider available.";
                ResourceInitException e = new ResourceInitException( msg );
                state = new ResourceState<T>( id, configFile, provider, init_error, null, e );
            }
        } else {
            // 7 is the length of ".ignore"
            String id = fileName.substring( 0, fileName.length() - 7 );
            state = new ResourceState<T>( id, configFile, provider, deactivated, null, null );
        }
        return state;
    }

    @Override
    public ResourceState<T> activate( String id ) {

        ResourceState<T> state = getState( id );
        if ( state != null && state.getType() == deactivated ) {
            File oldFile = state.getConfigLocation();
            File newFile = new File( dir, id + ".xml" );
            try {
                moveFile( oldFile, newFile );
            } catch ( Throwable t ) {
                LOG.error( t.getMessage(), t );
                String msg = "Renaming of file '" + oldFile + "' to '" + newFile + "' failed. Activation of resource '"
                             + id + "' failed.";
                ResourceInitException e = new ResourceInitException( msg, t );
                state = new ResourceState<T>( id, oldFile, state.getProvider(), init_error, null, e );
            }
            try {
                state = processResourceConfig( newFile );
            } catch ( IOException e ) {
                state = new ResourceState<T>( id, oldFile, state.getProvider(), init_error, null,
                                              new ResourceInitException( e.getMessage(), e ) );
            }
            idToState.put( id, state );
        }
        return state;
    }

    @Override
    public ResourceState<T> deactivate( String id ) {

        ResourceState<T> state = getState( id );
        if ( state != null && state.getType() != deactivated ) {
            File oldFile = state.getConfigLocation();
            File newFile = new File( dir, id + ".ignore" );
            try {
                moveFile( oldFile, newFile );

                T resource = state.getResource();
                if ( resource != null ) {
                    try {
                        remove( resource );
                        resource.destroy();
                    } catch ( Throwable t ) {
                        LOG.error( t.getMessage(), t );
                    }
                }
                state = new ResourceState<T>( id, newFile, state.getProvider(), deactivated, null, null );
            } catch ( Throwable t ) {
                LOG.error( t.getMessage(), t );
                String msg = "Renaming of file '" + oldFile + "' to '" + newFile
                             + "' failed. Deactivation of resource '" + id + "' failed.";
                ResourceInitException e = new ResourceInitException( msg, t );
                state = new ResourceState<T>( id, oldFile, state.getProvider(), init_error, state.getResource(), e );
            }
            idToState.put( id, state );
        }
        return state;
    }
}