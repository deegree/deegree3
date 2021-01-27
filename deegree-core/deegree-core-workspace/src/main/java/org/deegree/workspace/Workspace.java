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
package org.deegree.workspace;

import java.util.List;

import org.deegree.workspace.graph.ResourceGraph;

/**
 * The central workspace interface.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * 
 * @since 3.4
 */
public interface Workspace {

    /**
     * Call this method (or #initAll) before anything else. Prepares the workspace so it can start initializing
     * resources.
     */
    void startup();

    /**
     * Completely initializes the workspace AND all of its resources.
     */
    void initAll();

    /**
     * Completely destroys the workspace AND all of its resources.
     */
    void destroy();

    /**
     * Destroys the resource with the given id and all resources that depend on it.
     * 
     * @param id
     *            never <code>null</code>
     */
    <T extends Resource> void destroy( ResourceIdentifier<T> id );

    /**
     * Destroys the resource with the given id and shuts down all resources that depend on it,
     * putting them into error state.
     *
     * @param id
     *            never <code>null</code>
     */
    <T extends Resource> void destroyAndShutdownDependents( ResourceIdentifier<T> id );

    /**
     * Used to obtain the class loader coupled with this workspace.
     * 
     * @return the module class loader, never <code>null</code>
     */
    ClassLoader getModuleClassLoader();

    /**
     * @return the dependency graph, never <code>null</code> after started up
     */
    ResourceGraph getDependencyGraph();

    /**
     * Scans and prepares all resources.
     * 
     * @return all prepared resources, never <code>null</code>
     */
    PreparedResources prepare();

    /**
     * Retrieves a single resource metadata.
     * 
     * @param providerClass
     *            never <code>null</code>
     * @param id
     *            never <code>null</code>
     * @return the metadata or <code>null</code>, if no such metadata exists
     */
    <T extends Resource> ResourceMetadata<T> getResourceMetadata( Class<? extends ResourceProvider<T>> providerClass,
                                                                  String id );

    /**
     * Retrieves a single resource.
     * 
     * @param providerClass
     *            never <code>null</code>
     * @param id
     *            never <code>null</code>
     * @return the resource or <code>null</code>, if no such resource exists
     */
    <T extends Resource> T getResource( Class<? extends ResourceProvider<T>> providerClass, String id );

    /**
     * Adds a single resource. Does no preparation.
     * 
     * @param location
     *            never <code>null</code>
     */
    <T extends Resource> void add( ResourceLocation<T> location );

    /**
     * Prepares a single resource.
     * 
     * @param id
     *            never <code>null</code>
     * @return the new resource builder, or <code>null</code>, if preparation failed
     */
    <T extends Resource> ResourceBuilder<T> prepare( ResourceIdentifier<T> id );

    /**
     * Builds and initializes a single resource.
     * 
     * @param id
     *            never <code>null</code>
     * @param prepared
     *            can be <code>null</code>
     * @return the new resource, or <code>null</code>, if building or initializing failed
     */
    <T extends Resource> T init( ResourceIdentifier<T> id, PreparedResources prepared );

    /**
     * Can be used to obtain an instance of a resource manager.
     * 
     * @param managerClass
     *            never <code>null</code>
     * @return the manager instance, or <code>null</code>, if there's no such manager
     */
    <T extends ResourceManager<? extends Resource>> T getResourceManager( Class<T> managerClass );

    /**
     * Returns all known resource managers.
     * 
     * @return never <code>null</code>
     */
    List<ResourceManager<? extends Resource>> getResourceManagers();

    /**
     * Can be used to obtain a list of resource ids for a specific resource type.
     * 
     * @param providerClass
     * @return
     */
    <T extends Resource> List<ResourceIdentifier<T>> getResourcesOfType( Class<? extends ResourceProvider<T>> providerClass );

    /**
     * Returns an error handler that contains information about errors when starting up resources.
     * 
     * @return never <code>null</code>
     */
    ErrorHandler getErrorHandler();

    /**
     * Returns the current resource states.
     * 
     * @return never <code>null</code>
     */
    ResourceStates getStates();

    /**
     * Can be used to obtain an Initializable object which has been loaded and started up during #startup.
     * 
     * @return null, if no such initializable has been loaded
     */
    <T extends Initializable> T getInitializable( Class<T> className );

    /**
     * Returns a location handler that can be used to persist and query for resource locations.
     * 
     * @return never <code>null</code>
     */
    LocationHandler getLocationHandler();

}
