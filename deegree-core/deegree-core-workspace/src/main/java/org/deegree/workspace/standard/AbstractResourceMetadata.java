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

import java.util.HashSet;
import java.util.Set;

import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
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
public abstract class AbstractResourceMetadata<T extends Resource> implements ResourceMetadata<T> {

    private static final Logger LOG = LoggerFactory.getLogger( AbstractResourceMetadata.class );

    protected Workspace workspace;

    protected ResourceLocation<T> location;

    protected AbstractResourceProvider<T> provider;

    protected Set<ResourceMetadata<? extends Resource>> dependencies = new HashSet<ResourceMetadata<? extends Resource>>();

    public AbstractResourceMetadata( Workspace workspace, ResourceLocation<T> location,
                                     AbstractResourceProvider<T> provider ) {
        this.workspace = workspace;
        this.location = location;
        this.provider = provider;
    }

    @Override
    public ResourceLocation<T> getLocation() {
        return location;
    }

    @Override
    public ResourceIdentifier<T> getIdentifier() {
        return location.getIdentifier();
    }

    @Override
    public AbstractResourceProvider<T> getProvider() {
        return provider;
    }

    @Override
    public Set<ResourceMetadata<? extends Resource>> getDependencies() {
        return new HashSet<ResourceMetadata<? extends Resource>>( dependencies );
    }

    @Override
    public Set<ResourceMetadata<? extends Resource>> getRelatedResources() {
        Set<ResourceMetadata<? extends Resource>> set = new HashSet<ResourceMetadata<? extends Resource>>();
        Set<ResourceMetadata<? extends Resource>> deps = getDependencies();
        set.addAll( set );
        for ( ResourceMetadata<? extends Resource> md : deps ) {
            set.addAll( md.getRelatedResources() );
        }
        return set;
    }

    @Override
    public int compareTo( ResourceMetadata<? extends Resource> o ) {
        Set<ResourceMetadata<? extends Resource>> deps = new HashSet<ResourceMetadata<? extends Resource>>();
        collectDependencies( deps, getDependencies() );
        if ( deps.contains( o ) ) {
            return 1;
        }
        deps = new HashSet<ResourceMetadata<? extends Resource>>();
        collectDependencies( deps, o.getDependencies() );
        if ( deps.contains( this ) ) {
            return -1;
        }

        // else compare the identifiers
        return getIdentifier().compareTo( (ResourceIdentifier) o.getIdentifier() );
    }

    private void collectDependencies( Set<ResourceMetadata<? extends Resource>> visited,
                                      Set<ResourceMetadata<? extends Resource>> deps ) {
        for ( ResourceMetadata<? extends Resource> md : deps ) {
            if ( visited.contains( md ) ) {
                LOG.error( "Circular dependencies chain detected when loading resource {}!", getIdentifier() );
                return;
            }
            visited.add( md );
            collectDependencies( visited, md.getDependencies() );
        }
    }

    @Override
    public int hashCode() {
        return location.getIdentifier().hashCode();
    }

    @Override
    public boolean equals( Object obj ) {
        if ( !( obj instanceof ResourceMetadata ) ) {
            return false;
        }
        return location.getIdentifier().equals( ( (ResourceMetadata) obj ).getLocation().getIdentifier() );
    }

    @Override
    public String toString() {
        return location.getIdentifier().toString();
    }

}
