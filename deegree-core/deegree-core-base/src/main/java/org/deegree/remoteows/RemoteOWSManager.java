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
package org.deegree.remoteows;

import static java.io.File.separator;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceManagerMetadata;
import org.deegree.commons.config.ResourceProvider;
import org.deegree.commons.config.ResourceState;
import org.deegree.commons.utils.ProxyUtils;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class RemoteOWSManager implements ResourceManager {

    private static final Logger LOG = getLogger( RemoteOWSManager.class );

    private ServiceLoader<RemoteOWSProvider> serviceLoader = ServiceLoader.load( RemoteOWSProvider.class );

    Map<String, RemoteOWSProvider> providers = new HashMap<String, RemoteOWSProvider>();

    private Map<String, RemoteOWSStore> stores = new HashMap<String, RemoteOWSStore>();

    /**
     * 
     */
    public RemoteOWSManager() {
        for ( RemoteOWSProvider p : serviceLoader ) {
            providers.put( p.getConfigNamespace(), p );
            if ( p.getCapabilitiesNamespaces() != null ) {
                for ( String ns : p.getCapabilitiesNamespaces() ) {
                    providers.put( ns, p );
                }
            }
        }
    }

    public void startup( DeegreeWorkspace workspace ) {
        File dir = new File( workspace.getLocation(), "datasources" + separator + "remoteows" );
        if ( !dir.exists() ) {
            LOG.info( "No 'datasources/remoteows' directory -- skipping initialization of remote OWS stores." );
            return;
        }
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Setting up remote OWS stores." );
        LOG.info( "--------------------------------------------------------------------------------" );

        File[] configFiles = dir.listFiles( (FilenameFilter) new SuffixFileFilter( ".xml" ) );
        for ( File conf : configFiles ) {
            String fileName = conf.getName();
            // 4 is the length of ".xml"
            String storeId = fileName.substring( 0, fileName.length() - 4 );
            LOG.info( "Setting up remote OWS store '" + storeId + "' from file '" + fileName + "'..." + "" );
            if ( stores.containsKey( storeId ) ) {
                LOG.warn( "Skipping loading of store with id {}, it was already loaded.", storeId );
                continue;
            }
            try {
                RemoteOWSStore store = create( conf.toURI().toURL() );
                stores.put( storeId, store );
            } catch ( Exception e ) {
                LOG.warn( "Error creating remote OWS store: {}", e.getMessage() );
                LOG.trace( "Stack trace:", e );
            }
        }
        LOG.info( "" );
    }

    /**
     * @param configURL
     * @return null, if namespace could not be determined, or no fitting provider was found
     */
    public RemoteOWSStore create( URL configURL ) {
        String namespace = null;
        try {
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( configURL.openStream() );
            StAXParsingHelper.nextElement( xmlReader );
            namespace = xmlReader.getNamespaceURI();
            xmlReader.close();
        } catch ( Exception e ) {
            LOG.warn( "Error '{}' while determining configuration namespace for file '{}', skipping it.",
                      e.getLocalizedMessage(), configURL );
            LOG.trace( "Stack trace:", e );
            return null;
        }
        LOG.debug( "Config namespace: '{}'", namespace );
        RemoteOWSProvider provider = providers.get( namespace );
        if ( provider == null ) {
            LOG.warn( "No remote OWS store provider for namespace '{}' (file: '{}') registered. Skipping it.",
                      namespace, configURL );
            return null;
        }
        return provider.create( configURL );
    }

    public RemoteOWSStore get( String id ) {
        return stores.get( id );
    }

    @SuppressWarnings("unchecked")
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { ProxyUtils.class };
    }

    public void shutdown() {
        stores.clear();
    }

    public ResourceManagerMetadata getMetadata() {
        return new ResourceManagerMetadata() {
            public String getName() {
                return "remote OWS stores";
            }

            public String getPath() {
                return "datasources/remoteows/";
            }

            public List<ResourceProvider> getResourceProviders() {
                return new LinkedList<ResourceProvider>( providers.values() );
            }
        };
    }

    @Override
    public ResourceState getState( String id ) {
        // TODO
        return null;
    }
}