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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.utils.StringUtils;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.protocol.wms.client.WMSClient;
import org.deegree.remoteows.RemoteOWS;
import org.deegree.remoteows.RemoteOWSManager;
import org.deegree.remoteows.wms.RemoteWMS;
import org.deegree.tile.DefaultTileMatrixSet;
import org.deegree.tile.Tile;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixMetadata;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.TileMatrixSetMetadata;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.remotewms.jaxb.RemoteWMSTileStoreJAXB;
import org.deegree.tile.persistence.remotewms.jaxb.RemoteWMSTileStoreJAXB.RequestParams;
import org.deegree.tile.persistence.remotewms.jaxb.RemoteWMSTileStoreJAXB.TilePyramid;
import org.slf4j.Logger;

/**
 * {@link TileStore} that is backed by a remote WMS instance.
 * <p>
 * The WMS protocol support is limited to what the {@link RemoteWMS} class supports.
 * </p>
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$
 */
public class RemoteWMSTileStore implements TileStore {

    private static final Logger LOG = getLogger( RemoteWMSTileStore.class );

    private final String remoteWmsId;

    private final String crs;

    private final List<String> layers;

    private final List<String> styles;

    private final String format;

//    private final TileMatrixSet tileMatrixSet;

    private WMSClient client;

    public RemoteWMSTileStore( RemoteWMSTileStoreJAXB config ) throws UnknownCRSException {
        this.remoteWmsId = config.getRemoteWMSId();
        this.crs = config.getCRS();
        RequestParams requestParams = config.getRequestParams();
        layers = splitNullSafe( requestParams.getLayers() );
        styles = splitNullSafe( requestParams.getStyles() );
        format = requestParams.getFormat();
        LOG.info( "remote WMS id: " + remoteWmsId );
        LOG.info( "crs: " + crs );
        LOG.info( "request layers: " + layers );
        LOG.info( "request styles: " + styles );
        LOG.info( "request format: " + format );

//        TilePyramid pyramidConfig = config.getTilePyramid();
//        int tileWidth = pyramidConfig.getTileWidth().intValue();
//        int tileHeight = pyramidConfig.getTileHeight().intValue();
//        double minScaleDenominator = pyramidConfig.getMinScaleDenominator();
//        int levels = pyramidConfig.getNumLevels().intValue();
//        // TOD
//        SpatialMetadata spatialMetadata = new SpatialMetadata( null, Collections.singletonList( CRSManager.lookup( crs ) ));
//        tileMatrixSet = buildTileMatrixSet( spatialMetadata, tileWidth, tileHeight, minScaleDenominator, levels );
    }


    private List<String> splitNullSafe( String csv ) {
        if ( csv == null ) {
            return emptyList();
        }
        String[] tokens = StringUtils.split( csv, "," );
        return asList( tokens );
    }

    
    private TileMatrixSet buildTileMatrixSet( SpatialMetadata spatialMetadata, int tileWidth, int tileHeight,
                                              double minScaleDenominator, int levels ) {

        List<TileMatrix> matrices = new ArrayList<TileMatrix>( levels );
        for ( int i = 1; i < levels; i++ ) {
//            double worldWidth = 
//            int numTilesX = calc
//            String levelId = Double.toString( minScaleDenominator );
//            TileMatrixMetadata matrixMetadata = new TileMatrixMetadata( levelId, spatialMetadata, tileWidth,
//                                                                        tileHeight, resolution, numTilesX, numTilesY );
//            TileMatrix matrix = new RemoteWMSTileMatrix( matrixMetadata, this );
//            minScaleDenominator *= 2.0;
        }
        return new DefaultTileMatrixSet( matrices, new TileMatrixSetMetadata( format, spatialMetadata.getEnvelope().getCoordinateSystem() ) );
    }

    @Override
    public void init( DeegreeWorkspace workspace )
                            throws ResourceInitException {

        RemoteOWSManager mgr = workspace.getSubsystemManager( RemoteOWSManager.class );
        RemoteOWS store = mgr.get( remoteWmsId );
        if ( !( store instanceof RemoteWMS ) ) {
            String msg = "The remote WMS with id " + remoteWmsId + " is not available or not of type WMS.";
            throw new ResourceInitException( msg );
        }

        client = ( (RemoteWMS) store ).getClient();
        List<LayerMetadata> reportedLayers = client.getLayerTree().flattenDepthFirst();
        for ( LayerMetadata layerMd : reportedLayers ) {
            LOG.info( layerMd.getName() );
        }
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public SpatialMetadata getMetadata() {
        Envelope bbox = client.getBoundingBox( crs, layers );
        return new SpatialMetadata( bbox, singletonList( bbox.getCoordinateSystem() ) );
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
