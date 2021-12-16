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
package org.deegree.workspace;

import java.util.HashMap;
import java.util.Map;

/**
 * This class can be used to manage the states of the various resources.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * 
 * @since 3.4
 */
public class ResourceStates {

    private Map<ResourceIdentifier<? extends Resource>, ResourceState> map;

    public ResourceStates() {
        map = new HashMap<ResourceIdentifier<? extends Resource>, ResourceState>();
    }

    /**
     * @param id
     *            may not be <code>null</code>
     * @param state
     *            may not be <code>null</code>
     */
    public void setState( ResourceIdentifier<? extends Resource> id, ResourceState state ) {
        map.put( id, state );
    }

    /**
     * @param id
     *            may not be <code>null</code>
     */
    public void remove( ResourceIdentifier<? extends Resource> id ) {
        map.remove( id );
    }

    /**
     * @param id
     *            may not be <code>null</code>
     * @return will return <code>null</code> if the state is not known
     */
    public ResourceState getState( ResourceIdentifier<? extends Resource> id ) {
        return map.get( id );
    }

    /**
     * Enum listing possible resource states.
     * 
     * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
     * 
     * @since 3.4
     */
    public static enum ResourceState {
        /**
         * The resource has been scanned, but not prepared.
         */
        Scanned, /**
         * The resource has been prepared, but not built.
         */
        Prepared, /**
         * The resource has been built, but not initialized.
         */
        Built, /**
         * The resource has been fully initialized.
         */
        Initialized, /**
         * The resource is deactivated, and will not be started up automatically when initializing the
         * workspace.
         */
        Deactivated,
        /**
         * Scanning, preparing, building or initializing failed with an error.
         */
        Error
    }

}
