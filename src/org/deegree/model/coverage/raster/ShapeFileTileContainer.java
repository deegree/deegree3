//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/
package org.deegree.model.coverage.raster;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deegree.model.geometry.primitive.Envelope;
import org.deegree.io.dbaseapi.DBaseException;
import org.deegree.io.shpapi.HasNoDBaseFileException;
import org.deegree.io.shpapi.ShapeFile;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureProperty;

import org.deegree.model.legacy.GeometryConverter;

/**
 * This TileContainer gets tiles from a tileindex shapefile.
 * 
 * ShapeFileTileContainer opens a shape file for access to tiles.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 * 
 */
public class ShapeFileTileContainer implements TileContainer {

    private ShapeFile shapeFile;

    private RasterEnvelope rasterEnvelope;

    private Envelope envelope;

    private String baseDir;

    private static final String LOCATION_FIELD_NAME = "LOCATION";

    private static Log log = LogFactory.getLog( ShapeFileTileContainer.class );

    private static Cache cache;

    private final static String CACHENAME = "ShapeFileTileContainerCache";

    static {
        CacheManager manager = CacheManager.create();
        // see ehcachel.xml for ShapeFileTileContainerCache configuration
        cache = manager.getCache( CACHENAME );
    }

    /**
     * Create new ShapeFileTileContainer with tile index as shape file.
     * 
     * @param basename
     *            filename of the tileindex shape file (without .shp)
     * @throws IOException
     */
    public ShapeFileTileContainer( String basename ) throws IOException {
        this.baseDir = new File( basename ).getParent();
        this.shapeFile = new ShapeFile( basename );
        this.initializeEnvelopes();
    }

    /**
     * Initialize Envelope and RasterEnvelope. Reads one tile for calculation of the RasterEnvelope (size/resolution)
     * 
     * @throws IOException
     */
    private void initializeEnvelopes()
                            throws IOException {
        this.envelope = GeometryConverter.fromLegacy( shapeFile.getFileMBR() );

        Feature tileFeature;
        try {
            tileFeature = shapeFile.getFeatureByRecNo( 1 );
        } catch ( HasNoDBaseFileException e ) {
            e.printStackTrace();
            throw new IOException();
        } catch ( DBaseException e ) {
            e.printStackTrace();
            throw new IOException();
        }
        String filename = getLocationProperty( tileFeature );
        AbstractRaster tile = RasterFactory.createRasterFromFile( filename );

        this.rasterEnvelope = tile.getRasterEnvelope().createSubEnvelope( getEnvelope() );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.raster.TileContainer#getEnvelope()
     */
    public Envelope getEnvelope() {
        return envelope;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.raster.TileContainer#getRasterEnvelope()
     */
    public RasterEnvelope getRasterEnvelope() {
        return rasterEnvelope;
    }

    /**
     * Returns absolut pathname for feature property LOCATION.
     */
    private String getLocationProperty( Feature feature ) {
        FeatureProperty[] properties = feature.getProperties();

        String fileName = null;
        for ( FeatureProperty property : properties ) {
            if ( property.getName().getLocalName().equalsIgnoreCase( LOCATION_FIELD_NAME ) ) {
                fileName = (String) property.getValue();
                break;
            }
        }
        return baseDir + File.separator + fileName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.raster.TileContainer#getTiles(org.deegree.model.geometry.primitive.Envelope)
     */
    public List<AbstractRaster> getTiles( Envelope env ) {
        List<AbstractRaster> result = null;
        try {
            int[] geoNumbers = shapeFile.getGeoNumbersByRect( GeometryConverter.toLegacy( env ) );
            if ( log.isDebugEnabled() ) {
                log.debug( "read " + geoNumbers.length + " tiles from shape file" );
            }
            result = new ArrayList<AbstractRaster>( geoNumbers.length );
            for ( int recNo : geoNumbers ) {
                Feature feature = shapeFile.getFeatureByRecNo( recNo );
                result.add( getSimpleRasterFromFeature( feature ) );
            }
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( HasNoDBaseFileException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( DBaseException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if ( result == null ) {
            result = new ArrayList<AbstractRaster>( 0 );
        }
        return result;
    }

    /**
     * Get SimpleRaster from Feature from shapefile tileindex.
     */
    private SimpleRaster getSimpleRasterFromFeature( Feature feature ) {
        SimpleRaster result;

        String tileFileName = getLocationProperty( feature );

        // Because the tile objects (SimpleRaster) are created on each getTiles call
        // CachedRasterDataContainer for RasterData is noneffective, do caching on
        // SimpleRaster level.
        Element elem = cache.get( tileFileName );
        if ( elem == null ) {
            if ( log.isDebugEnabled() ) {
                log.trace( "Creating new SimpleRaster" );
            }
            Envelope tileEnv = GeometryConverter.fromLegacy( feature.getGeometryPropertyValues()[0].getEnvelope() );
            RasterEnvelope tileREnv = getRasterEnvelope().createSubEnvelope( tileEnv );
            int[] size = tileREnv.getSize( tileEnv );
            // Do not use Cached CachePolicy here, otherwise RasterData will be cached twice
            // once in CachedRasterDataContainer and again here as a part of the SimpleRaster.
            result = RasterFactory.createRasterFromFile( tileFileName, tileEnv, size[0], size[1],
                                                         RasterFactory.CachePolicy.LAZY );
            elem = new Element( tileFileName, result );
            cache.put( elem );
        } else {
            if ( log.isDebugEnabled() ) {
                log.trace( "Loading SimpleRaster from cache" );
            }
            result = (SimpleRaster) elem.getObjectValue();
        }
        return result;
    }

}
