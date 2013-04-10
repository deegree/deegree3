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
package org.deegree.workspace.standard;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceManager;
import org.deegree.workspace.ResourceManagerMetadata;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.ResourceProvider;
import org.deegree.workspace.Workspace;
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
public class DefaultResourceManager<T extends Resource> implements ResourceManager<T> {

    private static Logger LOG = LoggerFactory.getLogger( DefaultResourceManager.class );

    private ResourceManagerMetadata<T> metadata;

    private Map<ResourceIdentifier<T>, ResourceMetadata<T>> metadataMap;

    private Map<String, ResourceProvider<T>> nsToProvider;

    public DefaultResourceManager( ResourceManagerMetadata<T> metadata ) {
        this.metadata = metadata;
    }

    @Override
    public void find( Workspace workspace ) {
        nsToProvider = new HashMap<String, ResourceProvider<T>>();
        metadataMap = new HashMap<ResourceIdentifier<T>, ResourceMetadata<T>>();

        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Scanning for {}.", metadata.getName() );
        LOG.info( "--------------------------------------------------------------------------------" );

        // load providers
        Iterator<? extends ResourceProvider<T>> iter = ServiceLoader.load( metadata.getProviderClass(),
                                                                           workspace.getModuleClassLoader() ).iterator();
        while ( iter.hasNext() ) {
            ResourceProvider<T> prov = iter.next();
            nsToProvider.put( prov.getNamespace(), prov );
            try {
                for ( ResourceMetadata<T> md : prov.getAdditionalResources( workspace ) ) {
                    metadataMap.put( md.getIdentifier(), md );
                }
            } catch ( Exception e ) {
                LOG.error( "Unable to obtain additional resources from {}: {}", prov.getClass().getSimpleName(),
                           e.getLocalizedMessage() );
                LOG.trace( "Stack trace:", e );
            }
        }

        List<ResourceLocation<T>> list = workspace.findResourceLocations( metadata );

        read( list, workspace );
    }

    private void read( List<ResourceLocation<T>> list, Workspace workspace ) {
        for ( ResourceLocation<T> loc : list ) {
            try {
                ResourceProvider<T> prov = nsToProvider.get( loc.getNamespace() );
                if ( prov != null ) {
                    LOG.info( "Scanning resource {} with provider {}.", loc, prov.getClass().getSimpleName() );
                    ResourceMetadata<T> md = prov.read( workspace, loc );
                    metadataMap.put( md.getIdentifier(), md );
                } else {
                    LOG.warn( "Not scanning resource {}, no provider found for namespace {}.", loc, loc.getNamespace() );
                }
            } catch ( Exception e ) {
                LOG.error( "Unable to scan resource {}: {}.", loc.getIdentifier(), e.getLocalizedMessage() );
                LOG.trace( "Stack trace:", e );
            }
        }
    }

    @Override
    public ResourceManagerMetadata<T> getMetadata() {
        return metadata;
    }

    @Override
    public Collection<ResourceMetadata<T>> getResourceMetadata() {
        return metadataMap.values();
    }

    @Override
    public void shutdown() {
        // nothing to do
    }

}
