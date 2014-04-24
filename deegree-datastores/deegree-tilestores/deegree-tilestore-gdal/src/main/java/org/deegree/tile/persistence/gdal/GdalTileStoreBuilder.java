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
package org.deegree.tile.persistence.gdal;

import java.util.Map;

import org.deegree.tile.TileDataSet;
import org.deegree.tile.persistence.GenericTileStore;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.gdal.jaxb.GdalTileStoreJaxb;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * This class is responsible for building geotiff tile stores.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * 
 * @since 3.4
 */
public class GdalTileStoreBuilder implements ResourceBuilder<TileStore> {

    private GdalTileStoreJaxb cfg;

    private Workspace workspace;

    private ResourceMetadata<TileStore> metadata;

    public GdalTileStoreBuilder( GdalTileStoreJaxb cfg, Workspace workspace, ResourceMetadata<TileStore> metadata ) {
        this.cfg = cfg;
        this.workspace = workspace;
        this.metadata = metadata;
    }

    @Override
    public TileStore build() {
        try {
            GdalTileDataSetMapBuilder builder = new GdalTileDataSetMapBuilder( workspace, metadata.getLocation(), cfg );

            Map<String, TileDataSet> map = builder.buildTileDataSetMap();
            return new GenericTileStore( map, metadata );
        } catch ( Exception e ) {
            e.printStackTrace();
            throw new ResourceInitException( "Unable to build GDALTileStore: " + e.getLocalizedMessage(), e );
        }
    }

}
