//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
 ----------------------------------------------------------------------------*/
package org.deegree.tile.persistence.filesystem;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Iterator;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.tile.Tile;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreTransaction;
import org.deegree.tile.persistence.filesystem.jaxb.FileSystemTileStoreJAXB;
import org.slf4j.Logger;

/**
 * {@link TileStore} that is backed by a directory hierarchy of image tiles.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FileSystemTileStore implements TileStore {

    private static final Logger LOG = getLogger( FileSystemTileStore.class );

    private final FileSystemTileStoreJAXB config;

    private TileMatrixSet tileMatrixSet;

    private SpatialMetadata spatialMetadata;

    /**
     * Creates a new {@link FileSystemTileStore} instance.
     * 
     * @param config
     *            JAXB configuration, must not be <code>null</code>
     */
    public FileSystemTileStore( FileSystemTileStoreJAXB config ) {
        this.config = config;
    }

    @Override
    public void init( DeegreeWorkspace workspace )
                            throws ResourceInitException {
        // TODO
    }

    @Override
    public void destroy() {
        // nothing to destroy
    }

    @Override
    public Iterator<Tile> getTiles( Envelope envelope, double resolution ) {
        return tileMatrixSet.getTiles( envelope, resolution );
    }

    @Override
    public SpatialMetadata getMetadata() {
        return spatialMetadata;
    }

    @Override
    public TileMatrixSet getTileMatrixSet() {
        return tileMatrixSet;
    }

    @Override
    public Tile getTile( String tileMatrix, int x, int y ) {
        TileMatrix tm = tileMatrixSet.getTileMatrix( tileMatrix );
        if ( tm == null ) {
            return null;
        }
        return tm.getTile( x, y );
    }

    @Override
    public TileStoreTransaction acquireTransaction() {
        return new FileSystemTileStoreTransaction( this );
    }
}
