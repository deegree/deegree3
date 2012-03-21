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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.tile.persistence.remotewms;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Iterator;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.remoteows.wms.RemoteWMS;
import org.deegree.tile.Tile;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.persistence.TileStore;
import org.slf4j.Logger;

/**
 * {@link TileStore} that is backed by a remote WMS instance.
 * <p>
 * The WMS protocol support is limited to what the {@link RemoteWMS} class supports (currently, this is only 1.1.1).
 * </p>
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$
 */
public class RemoteWMSTileStore implements TileStore {

    private static final Logger LOG = getLogger( RemoteWMSTileStore.class );

    @Override
    public void init( DeegreeWorkspace workspace )
                            throws ResourceInitException {
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub
    }

    @Override
    public SpatialMetadata getMetadata() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TileMatrixSet getTileMatrixSet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<Tile> getTiles( Envelope envelope, double resolution ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Tile getTile( String tileMatrix, int x, int y ) {
        // TODO Auto-generated method stub
        return null;
    }
}
