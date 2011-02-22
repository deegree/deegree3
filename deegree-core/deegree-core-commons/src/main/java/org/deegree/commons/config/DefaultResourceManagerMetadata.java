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

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DefaultResourceManagerMetadata<T extends Resource> implements ResourceManagerMetadata<T> {

    private final String name;

    private final String path;

    private final ArrayList<ExtendedResourceProvider<T>> providers = new ArrayList<ExtendedResourceProvider<T>>();

    // public DefaultResourceManagerMetadata( String name, String path,
    // ServiceLoader<? extends ExtendedResourceProvider<T>> loader ) {
    // this.name = name;
    // this.path = path;
    // for ( ExtendedResourceProvider<T> p : loader ) {
    // providers.add( p );
    // }
    // }

    public DefaultResourceManagerMetadata( String name, String path, Class<? extends ExtendedResourceProvider<T>> clz,
                                           DeegreeWorkspace workspace ) {
        this.name = name;
        this.path = path;
        for ( ExtendedResourceProvider<T> p : ServiceLoader.load( clz, workspace.getModuleClassLoader() ) ) {
            providers.add( p );
        }
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public List<? extends ExtendedResourceProvider<T>> getResourceProviders() {
        return providers;
    }

}
