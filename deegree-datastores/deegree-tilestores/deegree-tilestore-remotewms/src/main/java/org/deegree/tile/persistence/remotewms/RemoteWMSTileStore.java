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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.utils.MapUtils;
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
import org.deegree.tile.persistence.remotewms.jaxb.RemoteWMSTileStoreJAXB.TileMatrixSet.RequestParams;
import org.deegree.tile.persistence.remotewms.jaxb.RemoteWMSTileStoreJAXB.TileMatrixSet.TilePyramid;

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

    // private static final Logger LOG = getLogger( RemoteWMSTileStore.class );

    private final RemoteWMSTileStoreJAXB config;

    private Map<String, TileMatrixSet> tileMatrixSets;

    /**
     * Creates a new {@link RemoteWMSTileStore} instance.
     * 
     * @param config
     *            configuration, must not be <code>null</code>
     */
    public RemoteWMSTileStore( RemoteWMSTileStoreJAXB config ) {
        this.config = config;
        tileMatrixSets = new HashMap<String, TileMatrixSet>();
    }

    private static List<String> splitNullSafe( String csv ) {
        if ( csv == null ) {
            return emptyList();
        }
        String[] tokens = StringUtils.split( csv, "," );
        return asList( tokens );
    }

    @Override
    public void init( DeegreeWorkspace workspace )
                            throws ResourceInitException {

        for ( RemoteWMSTileStoreJAXB.TileMatrixSet config : this.config.getTileMatrixSet() ) {

            String remoteWmsId = config.getRemoteWMSId();

            RemoteOWSManager mgr = workspace.getSubsystemManager( RemoteOWSManager.class );
            RemoteOWS store = mgr.get( remoteWmsId );
            if ( !( store instanceof RemoteWMS ) ) {
                String msg = "The remote WMS with id " + remoteWmsId + " is not available or not of type WMS.";
                throw new ResourceInitException( msg );
            }

            WMSClient client = ( (RemoteWMS) store ).getClient();

            ICRS crs = null;
            try {
                crs = CRSManager.lookup( config.getCRS() );
            } catch ( UnknownCRSException e ) {
                throw new ResourceInitException( "Configured CRS ('" + config.getCRS() + "') is not known." );
            }

            TileMatrixSet tileMatrixSet = buildTileMatrixSet( crs, config, client );
            tileMatrixSets.put( tileMatrixSet.getMetadata().getIdentifier(), tileMatrixSet );
        }
    }

    @Override
    public Collection<String> getTileMatrixSetIds() {
        return tileMatrixSets.keySet();
    }

    private static TileMatrixSet buildTileMatrixSet( ICRS crs, RemoteWMSTileStoreJAXB.TileMatrixSet config,
                                                     WMSClient client ) {
        RequestParams requestParams = config.getRequestParams();
        List<String> layers = splitNullSafe( requestParams.getLayers() );
        List<String> styles = splitNullSafe( requestParams.getStyles() );
        String format = requestParams.getFormat();
        TilePyramid pyramidConfig = config.getTilePyramid();
        String outputFormat = pyramidConfig.getImageFormat();
        int tileWidth = pyramidConfig.getTileWidth().intValue();
        int tileHeight = pyramidConfig.getTileHeight().intValue();
        double minScaleDenominator = pyramidConfig.getMinScaleDenominator();
        int levels = pyramidConfig.getNumLevels().intValue();
        SpatialMetadata spatialMetadata;
        if ( config.getEnvelope() == null ) {
            Envelope bbox = client.getBoundingBox( config.getCRS(), layers );
            spatialMetadata = new SpatialMetadata( bbox, singletonList( crs ) );
        } else {
            spatialMetadata = SpatialMetadataConverter.fromJaxb( config.getEnvelope(), config.getCRS() );
        }
        return buildTileMatrixSet( pyramidConfig.getIdentifier(), spatialMetadata, tileWidth, tileHeight,
                                   minScaleDenominator, levels, layers, styles, format, client, outputFormat );
    }

    private static TileMatrixSet buildTileMatrixSet( String tmsId, SpatialMetadata smd, int tileWidth, int tileHeight,
                                                     double scaleDenominator, int levels, List<String> layers,
                                                     List<String> styles, String format, WMSClient client,
                                                     String outputFormat ) {

        if ( outputFormat != null && outputFormat.startsWith( "image/" ) ) {
            outputFormat = outputFormat.substring( 6 );
        }

        List<TileMatrix> matrices = new ArrayList<TileMatrix>( levels );
        Envelope bbox = smd.getEnvelope();
        double span0 = bbox.getSpan0();
        double span1 = bbox.getSpan1();

        for ( int i = 0; i < levels; i++ ) {
            String id = Double.toString( scaleDenominator );
            double res = calcResolution( scaleDenominator, bbox );
            int numX = MathUtils.round( Math.ceil( span0 / ( res * tileWidth ) ) );
            int numY = MathUtils.round( Math.ceil( span1 / ( res * tileHeight ) ) );

            TileMatrixMetadata md = new TileMatrixMetadata( id, smd, tileWidth, tileHeight, res, numX, numY );

            TileMatrix m = new RemoteWMSTileMatrix( md, format, layers, styles, client, outputFormat );
            matrices.add( m );

            scaleDenominator *= 2;
        }
        String f = outputFormat != null ? "image/" + outputFormat : format;
        return new DefaultTileMatrixSet( matrices, new TileMatrixSetMetadata( tmsId, f, smd ) );
    }

    /**
     * Calculates the resolution (side length of a pixel in world coordinates).
     * 
     * @param scaleDenominator
     *            (factor for transforming a screen to a world length)
     * @return resolution of a pixel in world coordinates
     */
    private static double calcResolution( double scaleDenominator, Envelope bbox ) {
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
        if ( unit.equals( Unit.DEGREE ) ) {
            return MapUtils.calcResFromScale( scaleDenominator );
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
    public SpatialMetadata getMetadata( String id ) {
        return tileMatrixSets.get( id ).getMetadata().getSpatialMetadata();
    }

    @Override
    public TileMatrixSet getTileMatrixSet( String id ) {
        return tileMatrixSets.get( id );
    }

    @Override
    public Iterator<Tile> getTiles( String id, Envelope envelope, double resolution ) {
        return tileMatrixSets.get( id ).getTiles( envelope, resolution );
    }

    @Override
    public Tile getTile( String tmsId, String tileMatrix, int x, int y ) {
        TileMatrix m = tileMatrixSets.get( tmsId ).getTileMatrix( tileMatrix );
        if ( m == null ) {
            return null;
        }
        return m.getTile( x, y );
    }

    @Override
    public TileStoreTransaction acquireTransaction( String id ) {
        throw new UnsupportedOperationException( "RemoteWMSTileStore does not support transactions." );
    }
}
