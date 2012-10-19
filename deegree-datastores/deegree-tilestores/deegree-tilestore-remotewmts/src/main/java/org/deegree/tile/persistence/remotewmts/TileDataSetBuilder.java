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
package org.deegree.tile.persistence.remotewmts;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.deegree.commons.config.ResourceInitException;
import org.deegree.protocol.wmts.client.WMTSClient;
import org.deegree.tile.DefaultTileDataSet;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.persistence.remotewmts.jaxb.RemoteWMTSTileStoreJAXB.TileDataSet.RequestParams;
import org.deegree.tile.tilematrixset.TileMatrixSetManager;

/**
 * Builds a tile data set from jaxb.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
class TileDataSetBuilder {

    private org.deegree.tile.persistence.remotewmts.jaxb.RemoteWMTSTileStoreJAXB.TileDataSet tileDataSetConfig;

    private WMTSClient client;

    private String outputFormat;

    private TileMatrixSetManager tileMatrixSetManager;

    TileDataSetBuilder( org.deegree.tile.persistence.remotewmts.jaxb.RemoteWMTSTileStoreJAXB.TileDataSet tileDataSetConfig,
                        WMTSClient client, String outputFormat, TileMatrixSetManager tileMatrixSetManager ) {
        this.tileDataSetConfig = tileDataSetConfig;
        this.client = client;
        this.outputFormat = outputFormat;
        this.tileMatrixSetManager = tileMatrixSetManager;
    }

    TileDataSet buildTileDataSet()
                            throws ResourceInitException {

        if ( outputFormat.startsWith( "image/" ) ) {
            outputFormat = outputFormat.substring( 6 );
        }

        RequestParams requestParams = tileDataSetConfig.getRequestParams();
        String requestTileMatrixSetId = requestParams.getTileMatrixSet();
        String workspaceTileMatrixSetId = tileDataSetConfig.getTileMatrixSetId();
        if ( workspaceTileMatrixSetId == null ) {
            workspaceTileMatrixSetId = requestTileMatrixSetId;
        }
        TileMatrixSet localTileMatrixSet = getLocalTileMatrixSet( requestTileMatrixSetId, workspaceTileMatrixSetId );
        TileMatrixSet remoteTileMatrixSet = getRemoteTileMatrixSet( requestTileMatrixSetId );

        List<TileDataLevel> dataLevels = buildTileDataLevels( localTileMatrixSet, remoteTileMatrixSet, requestParams );
        return new DefaultTileDataSet( dataLevels, localTileMatrixSet, "image/" + outputFormat );
    }

    private TileMatrixSet getLocalTileMatrixSet( String requestTileMatrixSetId, String workspaceTileMatrixSetId )
                            throws ResourceInitException {
        String tileMatrixSetId = workspaceTileMatrixSetId;
        if ( tileMatrixSetId == null ) {
            tileMatrixSetId = requestTileMatrixSetId;
        }

        TileMatrixSet tileMatrixSet = tileMatrixSetManager.get( tileMatrixSetId );
        if ( tileMatrixSet == null ) {
            String msg = "No local TileMatrixSet definition with identifier '" + tileMatrixSetId + "' available.";
            throw new ResourceInitException( msg );
        }
        return tileMatrixSet;
    }

    private TileMatrixSet getRemoteTileMatrixSet( String tileMatrixSetId )
                            throws ResourceInitException {
        TileMatrixSet tileMatrixSet = null;
        try {
            tileMatrixSet = client.getTileMatrixSet( tileMatrixSetId );
        } catch ( XMLStreamException e ) {
            if ( tileMatrixSet == null ) {
                String msg = "No remote TileMatrixSet definition with identifier '" + tileMatrixSetId + "' available.";
                throw new ResourceInitException( msg );
            }
        }
        return tileMatrixSet;
    }

    private List<TileDataLevel> buildTileDataLevels( TileMatrixSet localTileMatrixSet,
                                                     TileMatrixSet remoteTileMatrixSet, RequestParams requestParams ) {
        String layer = requestParams.getLayer();
        String style = requestParams.getStyle();
        String format = requestParams.getFormat();
        String remoteTileMatrixSetId = remoteTileMatrixSet.getIdentifier();

        List<TileDataLevel> dataLevels = new ArrayList<TileDataLevel>();
        List<TileMatrix> localTileMatrices = localTileMatrixSet.getTileMatrices();
        List<TileMatrix> remoteTileMatrices = remoteTileMatrixSet.getTileMatrices();
        int numMatrices = remoteTileMatrices.size();
        if ( localTileMatrices.size() < numMatrices ) {
            numMatrices = localTileMatrices.size();
        }
        for ( int i = 0; i < numMatrices; i++ ) {
            TileMatrix localTileMatrix = localTileMatrices.get( i );
            TileMatrix remoteTileMatrix = remoteTileMatrices.get( i );
            String remoteTileMatrixId = remoteTileMatrix.getIdentifier();
            TileDataLevel level = buildTileDataLevel( localTileMatrix, remoteTileMatrixSetId, remoteTileMatrixId,
                                                      layer, style, format );
            dataLevels.add( level );
        }
        return dataLevels;
    }

    private TileDataLevel buildTileDataLevel( TileMatrix tileMatrix, String remoteTileMatrixSetId,
                                              String remoteTileMatrixId, String layer, String style, String format ) {
        return new RemoteWMTSTileDataLevel( tileMatrix, remoteTileMatrixSetId, remoteTileMatrixId, format, layer,
                                            style, client, outputFormat );
    }

}
