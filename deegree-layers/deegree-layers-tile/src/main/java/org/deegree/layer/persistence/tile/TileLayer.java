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
package org.deegree.layer.persistence.tile;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.deegree.layer.AbstractLayer;
import org.deegree.layer.LayerData;
import org.deegree.layer.LayerQuery;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.tile.Tile;
import org.deegree.tile.TileDataSet;
import org.slf4j.Logger;

/**
 * A layer implementation based on a list of tile data sets.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */

public class TileLayer extends AbstractLayer {

    private static final Logger LOG = getLogger( TileLayer.class );

    // maps tile matrix set ids to tile data sets
    private Map<String, TileDataSet> tileDataSets = new LinkedHashMap<String, TileDataSet>();

    // maps crs to tile matrix set ids
    private Map<ICRS, String> coordinateSystems = new LinkedHashMap<ICRS, String>();

    public TileLayer( LayerMetadata md, List<TileDataSet> datasets ) {
        super( md );
        for ( TileDataSet tds : datasets ) {
            coordinateSystems.put( tds.getTileMatrixSet().getSpatialMetadata().getCoordinateSystems().get( 0 ),
                                   tds.getTileMatrixSet().getIdentifier() );
            tileDataSets.put( tds.getTileMatrixSet().getIdentifier(), tds );
        }
    }

    @Override
    public TileLayerData mapQuery( LayerQuery query, List<String> headers )
                            throws OWSException {
        Envelope env = query.getEnvelope();
        ICRS crs = env.getCoordinateSystem();

        String tds = coordinateSystems.get( crs );
        if ( tds == null ) {
            LOG.debug( "Tile layer {} does not offer the coordinate system {}.", getMetadata().getName(),
                       crs.getAlias() );
            return null;
        }
        TileDataSet data = tileDataSets.get( tds );

        Iterator<Tile> tiles = data.getTiles( env, query.getResolution() );
        return new TileLayerData( tiles );
    }

    @Override
    public LayerData infoQuery( LayerQuery query, List<String> headers )
                            throws OWSException {
        return null;
    }

    /**
     * @return the tile data set this layer has been configured with wrt the tile matrix set
     */
    public TileDataSet getTileDataSet( String tileMatrixSet ) {
        return tileDataSets.get( tileMatrixSet );
    }

    /**
     * @return all tile data sets this layer has been configured with
     */
    public Collection<TileDataSet> getTileDataSets() {
        return tileDataSets.values();
    }

}
