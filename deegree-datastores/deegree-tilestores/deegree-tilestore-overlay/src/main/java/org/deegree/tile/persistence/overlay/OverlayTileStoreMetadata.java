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

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreProvider;
import org.deegree.tile.persistence.overlay.jaxb.OverlayTileStoreConfig;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceMetadata;
import org.deegree.workspace.standard.DefaultResourceIdentifier;
import org.slf4j.Logger;

/**
 * {@link ResourceMetadata} for the {@link OverlayTileStore}.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
class OverlayTileStoreMetadata extends AbstractResourceMetadata<TileStore> {

    private static final Logger LOG = getLogger( OverlayTileStoreMetadata.class );

    private static final String JAXB_PACKAGE = "org.deegree.tile.persistence.overlay.jaxb";

    /**
     * Creates a new {@link OverlayTileStoreMetadata} instance.
     * 
     * @param config
     *            JAXB configuration, must not be <code>null</code>
     * @param metadata
     *            resource metadata, must not be <code>null</code>
     * @param workspace
     *            workspace that the resource is part of, must not be <code>null</code>
     */
    OverlayTileStoreMetadata( final Workspace workspace, final ResourceLocation<TileStore> location,
                              final OverlayTileStoreProvider provider ) {
        super( workspace, location, provider );
    }

    @Override
    public ResourceBuilder<TileStore> prepare() {
        try {
            final OverlayTileStoreConfig cfg = (OverlayTileStoreConfig) unmarshall( JAXB_PACKAGE, provider.getSchema(),
                                                                                    location.getAsStream(), workspace );
            final List<String> tileStoreIds = cfg.getTileStoreId();
            for ( final String tileStoreId : tileStoreIds ) {
                LOG.debug( "Adding dependency on TileStore: " + tileStoreId );
                dependencies.add( new DefaultResourceIdentifier<TileStore>( TileStoreProvider.class, tileStoreId ) );
            }
            return new OverlayTileStoreBuilder( cfg, this, workspace );
        } catch ( Exception e ) {
            throw new ResourceInitException( "Could not prepare OverlayTileStore: " + e.getLocalizedMessage(), e );
        }
    }
}

