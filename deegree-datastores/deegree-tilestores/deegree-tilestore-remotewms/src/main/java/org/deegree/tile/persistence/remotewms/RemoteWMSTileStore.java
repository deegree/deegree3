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
import static org.deegree.commons.utils.MapUtils.DEFAULT_PIXEL_SIZE;
import static org.deegree.cs.components.Unit.METRE;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.utils.StringUtils;
import org.deegree.commons.utils.math.MathUtils;
import org.deegree.cs.components.IAxis;
import org.deegree.cs.components.IUnit;
import org.deegree.cs.components.Unit;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.geometry.metadata.SpatialMetadataConverter;
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
import org.deegree.tile.persistence.TileStoreTransaction;
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

    private final RemoteWMSTileStoreJAXB config;

    private final String remoteWmsId;

    private final List<String> layers;

    private final List<String> styles;

    private final String format;

    private WMSClient client;

    private TileMatrixSet tileMatrixSet;

    private SpatialMetadata spatialMetadata;

    /**
     * Creates a new {@link RemoteWMSTileStore} instance.
     * 
     * @param config
     *            configuration, must not be <code>null</code>
     * @throws UnknownCRSException
     */
    public RemoteWMSTileStore( RemoteWMSTileStoreJAXB config ) throws UnknownCRSException {
        this.config = config;
        this.remoteWmsId = config.getRemoteWMSId();
        RequestParams requestParams = config.getRequestParams();
        layers = splitNullSafe( requestParams.getLayers() );
        styles = splitNullSafe( requestParams.getStyles() );
        format = requestParams.getFormat();
        LOG.debug( "remote WMS id: " + remoteWmsId );
        LOG.debug( "crs: " + config.getCRS() );
        LOG.debug( "request layers: " + layers );
        LOG.debug( "request styles: " + styles );
        LOG.debug( "request format: " + format );
    }

    private List<String> splitNullSafe( String csv ) {
        if ( csv == null ) {
            return emptyList();
        }
        String[] tokens = StringUtils.split( csv, "," );
        return asList( tokens );
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

        ICRS crs = null;
        try {
            crs = CRSManager.lookup( config.getCRS() );
        } catch ( UnknownCRSException e ) {
            throw new ResourceInitException( "Configured CRS ('" + config.getCRS() + "') is not known." );
        }

        tileMatrixSet = buildTileMatrixSet( crs, config );
    }

    private TileMatrixSet buildTileMatrixSet( ICRS crs, RemoteWMSTileStoreJAXB config ) {
        TilePyramid pyramidConfig = config.getTilePyramid();
        int tileWidth = pyramidConfig.getTileWidth().intValue();
        int tileHeight = pyramidConfig.getTileHeight().intValue();
        double minScaleDenominator = pyramidConfig.getMinScaleDenominator();
        int levels = pyramidConfig.getNumLevels().intValue();
        if ( config.getEnvelope() == null ) {
            Envelope bbox = client.getBoundingBox( config.getCRS(), layers );
            spatialMetadata = new SpatialMetadata( bbox, singletonList( crs ) );
        } else {
            spatialMetadata = SpatialMetadataConverter.fromJaxb( config.getEnvelope(), config.getCRS() );
        }
        return buildTileMatrixSet( spatialMetadata, tileWidth, tileHeight, minScaleDenominator, levels );
    }

    private TileMatrixSet buildTileMatrixSet( SpatialMetadata smd, int tileWidth, int tileHeight,
                                              double scaleDenominator, int levels ) {

        List<TileMatrix> matrices = new ArrayList<TileMatrix>( levels );
        Envelope bbox = smd.getEnvelope();
        double span0 = bbox.getSpan0();
        double span1 = bbox.getSpan1();

        for ( int i = 0; i < levels; i++ ) {
            String id = Double.toString( scaleDenominator );
            double res = calcWorldResolution( scaleDenominator, bbox );
            int numX = MathUtils.round( Math.ceil( span0 / ( res * tileWidth ) ) );
            int numY = MathUtils.round( Math.ceil( span1 / ( res * tileHeight ) ) );

            TileMatrixMetadata md = new TileMatrixMetadata( id, smd, tileWidth, tileHeight, res, numX, numY );

            TileMatrix m = new RemoteWMSTileMatrix( md, this, format, layers, styles );
            matrices.add( m );

            scaleDenominator *= 2;
        }
        return new DefaultTileMatrixSet( matrices, new TileMatrixSetMetadata( remoteWmsId, format,
                                                                              bbox.getCoordinateSystem() ) );
    }

    /**
     * Calculates the resolution (side length of a pixel in world coordinates).
     * 
     * @param scaleDenominator
     *            (factor for transforming a screen to a world length)
     * @return resolution of a pixel in world coordinates
     */
    private double calcWorldResolution( double scaleDenominator, Envelope bbox ) {
        ICRS crs = bbox.getCoordinateSystem();
        IUnit unit = null;
        for ( IAxis axis : crs.getAxis() ) {
            IUnit axisUnit = axis.getUnits();
            if ( unit != null && !unit.equals( axisUnit ) ) {
                String msg = "Unable to calculate world resolution. CRS (" + crs.getAlias()
                             + ") uses axes with different UOMs.";
                throw new IllegalArgumentException( msg );
            }
            unit = axisUnit;
        }
        if ( unit == null ) {
            String msg = "Unable to calculate world resolution. CRS (" + crs.getAlias() + ") has no axes!?";
            throw new IllegalArgumentException( msg );
        }
        if ( unit.getBaseType().equals( METRE ) ) {
            double factor = unit.convert( 1.0, Unit.METRE );
            return factor * scaleDenominator * DEFAULT_PIXEL_SIZE;
        }
        String msg = "Unable to calculate world resolution. Cannot convert units of CRS (" + crs.getAlias()
                     + ") to meters.";
        throw new IllegalArgumentException( msg );
    }

    @Override
    public void destroy() {
        // nothing to do
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
    public Iterator<Tile> getTiles( Envelope envelope, double resolution ) {
        return tileMatrixSet.getTiles( envelope, resolution );
    }

    @Override
    public Tile getTile( String tileMatrix, int x, int y ) {
        TileMatrix m = tileMatrixSet.getTileMatrix( tileMatrix );
        if ( m == null ) {
            return null;
        }
        return m.getTile( x, y );
    }

    /**
     * Returns a {@link WMSClient} instance for accessing the remote WMS instance.
     * 
     * @return WMS client, never <code>null</code>
     */
    WMSClient getClient() {
        return client;
    }

    @Override
    public TileStoreTransaction acquireTransaction() {
        throw new UnsupportedOperationException( "RemoteWMSTileStore does not support transactions." );
    }
}
