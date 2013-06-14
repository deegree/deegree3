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

/**
 * Base interface for all resource providers. This is the base interface whose implementations/subinterfaces are used
 * for SPI.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * 
 * @since 3.4
 */
public interface ResourceProvider<T extends Resource> {

    /**
     * @return the namespace of the provider implementation, never <code>null</code>
     */
    String getNamespace();

    /**
     * The initial step of resource initialization. It is responsible for creating metadata objects, but not for
     * preparing them.
     * 
     * @param workspace
     *            never <code>null</code>
     * @param location
     *            never <code>null</code>
     * @return the metadata object, never <code>null</code>
     */
    ResourceMetadata<T> read( Workspace workspace, ResourceLocation<T> location );

    /**
     * Can be used to provide additional, 'synthetic' resources which are not found by the workspace.
     * 
     * @param workspace
     *            never <code>null</code>
     * @return a list of additional resource metadata, never <code>null</code>
     */
    List<ResourceMetadata<T>> getAdditionalResources( Workspace workspace );

}
