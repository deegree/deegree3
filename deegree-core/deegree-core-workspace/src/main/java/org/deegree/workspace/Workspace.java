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
import java.util.TreeMap;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
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
     * Used to obtain the class loader coupled with this workspace.
     * 
     * @return the module class loader, never <code>null</code>
     */
    ClassLoader getModuleClassLoader();

    // TODO think about how to clean up this mess
    void scan();

    <T extends Resource> void scan( ResourceLocation<T> location );

    TreeMap<ResourceMetadata<? extends Resource>, ResourceBuilder<? extends Resource>> prepare();

    <T extends Resource> ResourceBuilder<T> prepare( ResourceIdentifier<T> id );

    <T extends Resource> T init( ResourceIdentifier<T> id,
                                 TreeMap<ResourceMetadata<? extends Resource>, ResourceBuilder<? extends Resource>> metadataToBuilder );

    <T extends Resource> T init( ResourceBuilder<T> builder );

    void addExtraResource( ResourceLocation<? extends Resource> location );

    <T extends ResourceManager<? extends Resource>> T getResourceManager( Class<T> managerClass );

    <T extends Resource> List<ResourceLocation<T>> findResourceLocations( ResourceManagerMetadata<T> metadata );

    <T extends Resource> ResourceMetadata<T> getResourceMetadata( Class<? extends ResourceProvider<T>> providerClass,
                                                                  String id );

    <T extends Resource> T getResource( Class<? extends ResourceProvider<T>> providerClass, String id );

    <T extends Resource> List<ResourceIdentifier<T>> getResourcesOfType( Class<? extends ResourceProvider<T>> providerClass );

    List<ResourceMetadata<? extends Resource>> getResourceMetadata();

}
