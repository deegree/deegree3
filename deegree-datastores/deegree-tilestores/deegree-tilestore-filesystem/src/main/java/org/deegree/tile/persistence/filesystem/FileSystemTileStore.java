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

import static org.deegree.commons.utils.MapUtils.DEFAULT_PIXEL_SIZE;
import static org.deegree.cs.components.Unit.METRE;
import static org.deegree.geometry.metadata.SpatialMetadataConverter.fromJaxb;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.utils.MapUtils;
import org.deegree.commons.utils.math.MathUtils;
import org.deegree.cs.components.IAxis;
import org.deegree.cs.components.IUnit;
import org.deegree.cs.components.Unit;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.geometry.metadata.jaxb.EnvelopeType;
import org.deegree.tile.DefaultTileDataSet;
import org.deegree.tile.Tile;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreTransaction;
import org.deegree.tile.persistence.filesystem.jaxb.FileSystemTileStoreJAXB;
import org.deegree.tile.persistence.filesystem.jaxb.FileSystemTileStoreJAXB.TilePyramid;
import org.deegree.tile.persistence.filesystem.layout.TileCacheDiskLayout;

/**
 * {@link TileStore} that is backed by image files on the file system.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FileSystemTileStore implements TileStore {

    // private static final Logger LOG = getLogger( FileSystemTileStore.class );

    private Map<String, TileDataSet> tileMatrixSets;

    /**
     * Creates a new {@link FileSystemTileStore} instance.
     * 
     * @param config
     *            JAXB configuration, must not be <code>null</code>
     * @param configUrl
     * @throws ResourceInitException
     */
    public FileSystemTileStore( FileSystemTileStoreJAXB config, URL configUrl ) throws ResourceInitException {
        tileMatrixSets = new HashMap<String, TileDataSet>();
        Iterator<Object> iter = config.getCRSAndEnvelopeAndTilePyramid().iterator();
        while ( iter.hasNext() ) {
            String crss = (String) iter.next();
            EnvelopeType env = (EnvelopeType) iter.next();
            TilePyramid p = (TilePyramid) iter.next();
            org.deegree.tile.persistence.filesystem.jaxb.FileSystemTileStoreJAXB.TileCacheDiskLayout lay;
            lay = (org.deegree.tile.persistence.filesystem.jaxb.FileSystemTileStoreJAXB.TileCacheDiskLayout) iter.next();

            File parent = null;
            try {
                parent = new File( configUrl.toURI() ).getParentFile();
                File baseDir = new File( lay.getLayerDirectory() );
                if ( !baseDir.isAbsolute() ) {
                    baseDir = new File( parent, lay.getLayerDirectory() );
                }
                String layerName = baseDir.getName();
                if ( p.getIdentifier() != null ) {
                    layerName = p.getIdentifier();
                }
                TileCacheDiskLayout layout = new TileCacheDiskLayout( baseDir, lay.getFileType() );

                TileDataSet tms = buildTileMatrixSet( layerName, p, env, crss, layout );
                tileMatrixSets.put( layerName, tms );
                layout.setTileMatrixSet( tms );
            } catch ( Throwable e ) {
                throw new ResourceInitException( e.getLocalizedMessage(), e );
            }
        }
    }

    @Override
    public Collection<String> getTileMatrixSetIds() {
        return tileMatrixSets.keySet();
    }

    private TileDataSet buildTileMatrixSet( String id, TilePyramid pyramidConfig, EnvelopeType envelope, String crs,
                                              DiskLayout layout ) {
        int tileWidth = pyramidConfig.getTileWidth().intValue();
        int tileHeight = pyramidConfig.getTileHeight().intValue();
        double minScaleDenominator = pyramidConfig.getMinScaleDenominator();
        int levels = pyramidConfig.getNumLevels().intValue();
        SpatialMetadata spatialMetadata = fromJaxb( envelope, crs );
        return buildTileMatrixSet( id, spatialMetadata, tileWidth, tileHeight, minScaleDenominator, levels, layout );
    }

    private TileDataSet buildTileMatrixSet( String layerName, SpatialMetadata smd, int tileWidth, int tileHeight,
                                              double scaleDenominator, int levels, DiskLayout layout ) {

        List<TileDataLevel> matrices = new ArrayList<TileDataLevel>( levels );
        Envelope bbox = smd.getEnvelope();
        double span0 = bbox.getSpan0();
        double span1 = bbox.getSpan1();

        for ( int i = 0; i < levels; i++ ) {
            String id = Double.toString( scaleDenominator );
            double res = calcWorldResolution( scaleDenominator, bbox );
            int numX = MathUtils.round( Math.ceil( span0 / ( res * tileWidth ) ) );
            int numY = MathUtils.round( Math.ceil( span1 / ( res * tileHeight ) ) );

            TileMatrix md = new TileMatrix( id, smd, tileWidth, tileHeight, res, numX, numY );

            TileDataLevel m = new FileSystemTileMatrix( md, layout );
            matrices.add( m );

            scaleDenominator *= 2;
        }
        String format = layout.getFileType();
        if ( !format.startsWith( "image" ) ) {
            format = "image/" + format;
        }

        return new DefaultTileDataSet( matrices, new TileMatrixSet( layerName, format, smd ) );
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
        if ( unit.equals( Unit.DEGREE ) ) {
            return MapUtils.calcResFromScale( scaleDenominator );
        }

        String msg = "Unable to calculate world resolution. Cannot convert units of CRS (" + crs.getAlias()
                     + ") to meters.";
        throw new IllegalArgumentException( msg );
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
    public Iterator<Tile> getTiles( String id, Envelope envelope, double resolution ) {
        return tileMatrixSets.get( id ).getTiles( envelope, resolution );
    }

    @Override
    public SpatialMetadata getMetadata( String id ) {
        return tileMatrixSets.get( id ).getMetadata().getSpatialMetadata();
    }

    @Override
    public TileDataSet getTileMatrixSet( String id ) {
        return tileMatrixSets.get( id );
    }

    @Override
    public Tile getTile( String tmsId, String tileMatrix, int x, int y ) {
        TileDataLevel tm = tileMatrixSets.get( tmsId ).getTileMatrix( tileMatrix );
        if ( tm == null ) {
            return null;
        }
        return tm.getTile( x, y );
    }

    @Override
    public TileStoreTransaction acquireTransaction( String id ) {
        return new FileSystemTileStoreTransaction( id, this );
    }
}
