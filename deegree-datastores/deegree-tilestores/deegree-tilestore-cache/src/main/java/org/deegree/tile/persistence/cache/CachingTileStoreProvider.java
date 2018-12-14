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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.tile.persistence.cache;

import java.net.URL;

import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreProvider;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * SPI provider class for caching tile stores.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public class CachingTileStoreProvider extends TileStoreProvider {

    private static final URL SCHEMA = CachingTileStoreProvider.class.getResource( "/META-INF/schemas/datasource/tile/cache/3.4.0/cache.xsd" );

    @Override
    public String getNamespace() {
        return "http://www.deegree.org/datasource/tile/cache";
    }

    @Override
    public ResourceMetadata<TileStore> createFromLocation( Workspace workspace, ResourceLocation<TileStore> location ) {
        return new CachingTileStoreMetadata( workspace, location, this );
    }

    @Override
    public URL getSchema() {
        return SCHEMA;
    }

}
