/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.workspace.standard;

import static org.deegree.workspace.ResourceStates.ResourceState.Deactivated;
import static org.deegree.workspace.ResourceStates.ResourceState.Scanned;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.deegree.workspace.LocationHandler;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceManager;
import org.deegree.workspace.ResourceManagerMetadata;
import org.deegree.workspace.ResourceProvider;
import org.deegree.workspace.ResourceStates;
import org.deegree.workspace.ResourceStates.ResourceState;

/**
 * Default implementation of a location handler based on a standard workspace directory.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * 
 * @since 3.4
 */
public class DefaultLocationHandler implements LocationHandler {

    private Map<Class<? extends ResourceProvider<? extends Resource>>, List<ResourceLocation<? extends Resource>>> extraResources = new HashMap<Class<? extends ResourceProvider<? extends Resource>>, List<ResourceLocation<? extends Resource>>>();

    private File directory;

    private Map<Class<? extends ResourceProvider<? extends Resource>>, ResourceManager<? extends Resource>> managers;

    private ResourceStates states;

    /**
     * @param directory
     *            the workspace directory, may not be <code>null</code>
     * @param managers
     *            the resource managers, may not be <code>null</code>
     */
    public DefaultLocationHandler( File directory,
                                   Map<Class<? extends ResourceProvider<? extends Resource>>, ResourceManager<? extends Resource>> managers,
                                   ResourceStates states ) {
        this.directory = directory;
        this.managers = managers;
        this.states = states;
    }

    @Override
    public <T extends Resource> List<ResourceLocation<T>> findResourceLocations( ResourceManagerMetadata<T> metadata ) {
        List<ResourceLocation<T>> list = new ArrayList<ResourceLocation<T>>();

        if ( extraResources.get( metadata.getProviderClass() ) != null ) {
            list.addAll( (Collection) extraResources.get( metadata.getProviderClass() ) );
        }

        File dir = new File( directory, metadata.getWorkspacePath() );
        if ( !dir.isDirectory() ) {
            return list;
        }
        URI base = dir.getAbsoluteFile().toURI();
        for ( File f : FileUtils.listFiles( dir, new String[] { "xml", "ignore" }, true ) ) {
            URI uri = f.getAbsoluteFile().toURI();
            uri = base.relativize( uri );
            String p = uri.getPath();
            ResourceState state = null;
            if ( p.endsWith( "xml" ) ) {
                p = p.substring( 0, p.length() - 4 );
            } else {
                p = p.substring( 0, p.length() - 7 );
                state = ResourceState.Deactivated;
            }
            DefaultResourceIdentifier<T> identifier = new DefaultResourceIdentifier<T>( metadata.getProviderClass(), p );
            if ( state != null ) {
                states.setState( identifier, state );
            }
            list.add( new DefaultResourceLocation<T>( f, identifier ) );

        }
        return list;
    }

    @Override
    public void addExtraResource( ResourceLocation<? extends Resource> location ) {
        List<ResourceLocation<? extends Resource>> list = extraResources.get( location.getIdentifier().getProvider() );
        if ( list == null ) {
            list = new ArrayList<ResourceLocation<? extends Resource>>();
            extraResources.put( location.getIdentifier().getProvider(), list );
        }
        list.add( location );
    }

    @Override
    public <T extends Resource> ResourceLocation<T> persist( ResourceLocation<T> location ) {
        ResourceManager<?> mgr = managers.get( location.getIdentifier().getProvider() );
        File file = new File( directory, mgr.getMetadata().getWorkspacePath() );
        file = new File( file, location.getIdentifier().getId() + ".xml" );
        file.getParentFile().mkdirs();
        try {
            // copy to avoid persisting from same file
            File tmp = File.createTempFile( "config", ".xml" );
            FileUtils.copyInputStreamToFile( location.getAsStream(), tmp );
            tmp.renameTo( file );
            return new DefaultResourceLocation<T>( file, location.getIdentifier() );
        } catch ( Exception e ) {
            throw new ResourceException( "Could not persist resource location: " + e.getLocalizedMessage(), e );
        }
    }

    @Override
    public <T extends Resource> void delete( ResourceLocation<T> location ) {
        if ( location instanceof DefaultResourceLocation ) {
            DefaultResourceLocation<T> loc = (DefaultResourceLocation<T>) location;
            loc.getAsFile().delete();
        }
    }

    @Override
    public <T extends Resource> void activate( ResourceLocation<T> location ) {
        location.activate();
        states.setState( location.getIdentifier(), Scanned );
    }

    @Override
    public <T extends Resource> void deactivate( ResourceLocation<T> location ) {
        location.deactivate();
        states.setState( location.getIdentifier(), Deactivated );
    }

}
