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

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.layer.AbstractLayer;
import org.deegree.layer.LayerData;
import org.deegree.layer.LayerQuery;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.style.StyleRef;
import org.deegree.tile.Tile;
import org.deegree.tile.TileDataSet;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.deegree.commons.ows.exception.OWSException.NO_APPLICABLE_CODE;
import static org.deegree.layer.Utils.calcResolution;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A layer implementation based on a list of tile data sets.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */

public class TileLayer extends AbstractLayer {

    private static final Logger LOG = getLogger( TileLayer.class );

    // maps tile matrix set ids to tile data sets
    private final Map<String, TileDataSet> tileDataSets = new LinkedHashMap<String, TileDataSet>();

    // maps crs to tile matrix set ids
    private final Map<ICRS, String> coordinateSystems = new LinkedHashMap<ICRS, String>();

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
        double resolution = query.getResolution();
        Envelope env = query.getEnvelope();
        ICRS crs = env.getCoordinateSystem();
        String tds = coordinateSystems.get( crs );
        Iterator<Tile> tiles;
        if ( tds != null ) {
            tiles = retrieveTiles( resolution, env, tds );
        } else {
            tiles = retrieveTilesInSourceCrs( query, env, crs );
        }
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

    @Override
    public boolean isStyleApplicable( StyleRef style ) {
        return true;
    }

    /**
     * Retrieves tiles in an available source crs. For that a crs is chosen from the coordinateSystems map, the bounding
     * box and the resolution of the query are transformed into that crs and the matching tiles are returned.
     * 
     * @param query
     *            never <code>null</code>
     * @param env
     *            never <code>null</code>
     * @param crs
     *            never <code>null</code>
     * @return tile iterator
     * @throws OWSException
     */
    Iterator<Tile> retrieveTilesInSourceCrs( LayerQuery query, Envelope env, ICRS crs )
                            throws OWSException {
        LOG.debug( "There are no source tiles for coordinate system " + crs.getAlias() );
        Iterator<ICRS> coordinateSystemsIter = coordinateSystems.keySet().iterator();
        if ( coordinateSystemsIter.hasNext() ) {
            ICRS alternativeCrs = coordinateSystemsIter.next();
            return retrieveTilesInAlternativeCrs( query, env, alternativeCrs );
        } else {
            String msg = "Tile layer " + getMetadata().getName() + " does not offer the coordinate system "
                         + crs.getAlias();
            LOG.debug( msg );
            throw new OWSException( msg, OWSException.INVALID_CRS );
        }
    }

    private Iterator<Tile> retrieveTilesInAlternativeCrs( LayerQuery query, Envelope env, ICRS alternativeCrs )
                            throws OWSException {
        LOG.debug( "Using coordinate system " + alternativeCrs.getAlias() + " instead to retrieve source tiles" );
        String alternativeTds = coordinateSystems.get( alternativeCrs );
        Envelope alternativeEnv = retrieveTransformedEnvelope( env, alternativeCrs );
        double alternativeResolution = calcResolution( alternativeEnv, query.getWidth(), query.getHeight() );
        return retrieveTiles( alternativeResolution, alternativeEnv, alternativeTds );
    }

    private Envelope retrieveTransformedEnvelope( Envelope env, ICRS alternativeCrs )
                            throws OWSException {
        try {
            return new GeometryTransformer( alternativeCrs ).transform( env );
        } catch ( TransformationException e ) {
            String msg = "Could not transform bounding box to new coordinate system: " + e.getMessage();
            LOG.warn( msg );
            e.printStackTrace();
            throw new OWSException( "Tiles cannot be retrieved: " + msg, NO_APPLICABLE_CODE );
        } catch ( UnknownCRSException e ) {
            String msg = "Could not transform bounding box to new coordinate system as the CRS is not known: "
                         + e.getMessage();
            LOG.warn( msg );
            e.printStackTrace();
            throw new OWSException( "Tiles cannot be retrieved: " + msg, NO_APPLICABLE_CODE );
        }
    }

    private Iterator<Tile> retrieveTiles( double resolution, Envelope env, String tds ) {
        TileDataSet data = tileDataSets.get( tds );
        return data.getTiles( env, resolution );
    }

}