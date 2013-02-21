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

import java.util.Set;

import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.ResourceProvider;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class DefaultResourceMetadata<T extends Resource> implements ResourceMetadata<T> {

    private ResourceLocation<T> location;

    private ResourceProvider<T> provider;

    public DefaultResourceMetadata( ResourceLocation<T> location, ResourceProvider<T> provider ) {
        this.location = location;
        this.provider = provider;
    }

    @Override
    public ResourceLocation<T> getLocation() {
        return location;
    }

    @Override
    public void init() {

    }

    @Override
    public ResourceIdentifier<T> getIdentifier() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResourceProvider<T> getProvider() {
        return provider;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.workspace.ResourceMetadata#getDependencies()
     */
    @Override
    public Set<ResourceMetadata<? extends Resource>> getDependencies() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.workspace.ResourceMetadata#getRelatedResources()
     */
    @Override
    public Set<ResourceMetadata<? extends Resource>> getRelatedResources() {
        // TODO Auto-generated method stub
        return null;
    }

}
