/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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
package org.deegree.tile.persistence.overlay;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.tile.TileDataSet;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreProvider;
import org.deegree.tile.persistence.overlay.jaxb.OverlayTileStoreConfig;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * {@link ResourceBuilder} for the {@link OverlayTileStore}.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
class OverlayTileStoreBuilder implements ResourceBuilder<TileStore> {

    private static final Logger LOG = getLogger( OverlayTileStoreMetadata.class );

    private final OverlayTileStoreConfig config;

    private final OverlayTileStoreMetadata metadata;

    private final Workspace workspace;

    /**
     * Creates a new {@link OverlayTileStoreBuilder} instance.
     * 
     * @param config
     *            JAXB configuration, must not be <code>null</code>
     * @param metadata
     *            resource metadata, must not be <code>null</code>
     * @param workspace
     *            workspace that the resource is part of, must not be <code>null</code>
     */
    OverlayTileStoreBuilder( final OverlayTileStoreConfig config, final OverlayTileStoreMetadata metadata,
                             final Workspace workspace ) {
        this.config = config;
        this.metadata = metadata;
        this.workspace = workspace;
    }

    @Override
    public TileStore build() {
        final Map<String, TileMatrixSet> idToMatrixSet = new HashMap<String, TileMatrixSet>();
        final Map<String, List<TileStore>> idToTileStore = new HashMap<String, List<TileStore>>();
        for ( final String tsId : config.getTileStoreId() ) {
            LOG.debug( "Processing TileStore: " + tsId );
            final TileStore tileStore = workspace.getResource( TileStoreProvider.class, tsId );
            for ( final String dsid : tileStore.getTileDataSetIds() ) {
                LOG.debug( "Processing TileDataSet: " + dsid );
                final TileDataSet tds = tileStore.getTileDataSet( dsid );
                final TileMatrixSet tms = tds.getTileMatrixSet();
                if ( idToMatrixSet.containsKey( dsid ) ) {
                    if ( !idToMatrixSet.get( dsid ).equals( tms ) ) {
                        throw new ResourceInitException( "TileDataSets have mismatching TileMatrixSets" );
                    }
                } else {
                    idToMatrixSet.put( dsid, tms );
                }
                if ( idToTileStore.containsKey( dsid ) ) {
                    idToTileStore.get( dsid ).add( tileStore );
                } else {
                    final ArrayList<TileStore> tileStoreList = new ArrayList<TileStore>();
                    tileStoreList.add( tileStore );
                    idToTileStore.put( dsid, tileStoreList );
                }
            }
        }
        return new OverlayTileStore( idToMatrixSet, idToTileStore, metadata );
    }
}
