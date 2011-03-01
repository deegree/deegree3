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

import static org.apache.commons.io.IOCase.INSENSITIVE;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class AbstractResourceManager<T extends Resource> implements ExtendedResourceManager<T> {

    private static final Logger LOG = getLogger( AbstractResourceManager.class );

    private HashMap<String, ExtendedResourceProvider<T>> nsToProvider = new HashMap<String, ExtendedResourceProvider<T>>();

    private HashMap<String, T> resources = new HashMap<String, T>();

    /**
     * @return all managed resources
     */
    public Collection<T> getAll() {
        return resources.values();
    }

    public T create( String id, URL configUrl )
                            throws WorkspaceInitializationException {
        ResourceManagerMetadata<T> md = getMetadata();

        if ( md == null ) {
            throw new WorkspaceInitializationException( "Creating from config file is not supported." );
        }

        String namespace = null;
        try {
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( configUrl.openStream() );
            StAXParsingHelper.nextElement( xmlReader );
            namespace = xmlReader.getNamespaceURI();
        } catch ( Exception e ) {
            String msg = "Error determining configuration namespace for file '" + configUrl + "'";
            LOG.error( msg );
            throw new WorkspaceInitializationException( msg );
        }
        LOG.debug( "Config namespace: '" + namespace + "'" );
        ExtendedResourceProvider<T> provider = nsToProvider.get( namespace );
        if ( provider == null ) {
            String msg = "No {} provider for namespace '{}' (file: '{}') registered. Skipping it.";
            LOG.error( msg, new Object[] { md.getName(), namespace, configUrl } );
            throw new WorkspaceInitializationException( "Creation of " + md.getName()
                                                        + " via configuration file failed." );
        }
        T resource = provider.create( configUrl );

        resources.put( id, resource );
        return resource;
    }

    public T get( String id ) {
        return resources.get( id );
    }

    public void shutdown() {
        for ( T t : resources.values() ) {
            t.destroy();
        }
        resources.clear();
        nsToProvider.clear();
    }

    public void startup( DeegreeWorkspace workspace )
                            throws WorkspaceInitializationException {
        ResourceManagerMetadata<T> md = getMetadata();
        if ( md != null ) {
            for ( ResourceProvider p : md.getResourceProviders() ) {
                nsToProvider.put( p.getConfigNamespace(), (ExtendedResourceProvider<T>) p );
            }
            System.out.println( nsToProvider );

            File dir = new File( workspace.getLocation(), md.getPath() );
            String name = md.getName();
            if ( !dir.exists() ) {
                LOG.info( "No '{}' directory -- skipping initialization of {}.", md.getPath(), name );
                return;
            }
            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "Setting up {}.", name );
            LOG.info( "--------------------------------------------------------------------------------" );

            File[] configFiles = dir.listFiles( (FilenameFilter) new SuffixFileFilter( ".xml", INSENSITIVE ) );
            for ( File configFile : configFiles ) {
                String fileName = configFile.getName();
                // 4 is the length of ".xml"
                String id = fileName.substring( 0, fileName.length() - 4 );
                LOG.info( "Setting up {} '{}' from file '{}'...", new Object[] { name, id, fileName } );
                try {
                    T resource = create( id, configFile.toURI().toURL() );
                    resource.init( workspace );
                    // TODO explicitly check for workspace init exception here? remove checked exception altogether?
                } catch ( Exception e ) {
                    LOG.error( "Error creating {}: {}", new Object[] { name, e.getMessage(), e } );
                }
            }
            LOG.info( "" );
        }
    }

}
