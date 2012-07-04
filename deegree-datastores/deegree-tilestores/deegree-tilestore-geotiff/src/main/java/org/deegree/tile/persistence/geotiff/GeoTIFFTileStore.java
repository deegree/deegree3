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
package org.deegree.tile.persistence.geotiff;

import static java.util.Collections.singletonList;
import static javax.imageio.ImageIO.createImageInputStream;
import static javax.imageio.ImageIO.getImageReadersBySuffix;
import static org.deegree.commons.utils.MapUtils.DEFAULT_PIXEL_SIZE;
import static org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation.CENTER;
import static org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation.OUTER;
import static org.slf4j.LoggerFactory.getLogger;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.utils.Pair;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.io.imageio.geotiff.GeoTiffIIOMetadataAdapter;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.tile.DefaultTileDataSet;
import org.deegree.tile.Tile;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreTransaction;
import org.slf4j.Logger;

/**
 * {@link TileStore} backed by a GeoTIFF/BigTIFF file.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */
public class GeoTIFFTileStore implements TileStore {

    private static final Logger LOG = getLogger( GeoTIFFTileStore.class );

    private final List<Pair<File, String>> files;

    private Map<String, TileDataSet> tileMatrixSets;

    public GeoTIFFTileStore( List<Pair<File, String>> files ) {
        this.files = files;
        this.tileMatrixSets = new LinkedHashMap<String, TileDataSet>();
    }

    @Override
    public void init( DeegreeWorkspace workspace )
                            throws ResourceInitException {
        ImageReader reader = null;
        ImageInputStream iis = null;
        try {
            for ( Pair<File, String> file : files ) {

                String fileName = file.first.getName();
                int pos = fileName.lastIndexOf( '.' );
                String tmsId;
                if ( pos != -1 ) {
                    tmsId = fileName.substring( 0, pos );
                } else {
                    tmsId = fileName;
                }

                ICRS crs = null;
                if ( file.second != null ) {
                    crs = CRSManager.getCRSRef( file.second );
                }

                Iterator<ImageReader> readers = getImageReadersBySuffix( "tiff" );
                while ( readers.hasNext() && !( reader instanceof TIFFImageReader ) ) {
                    reader = readers.next();
                }
                iis = createImageInputStream( file.first );
                // this is already checked in provider
                reader.setInput( iis, false, true );
                int num = reader.getNumImages( true );
                IIOMetadata md = reader.getImageMetadata( 0 );
                Envelope envelope = getEnvelope( md, reader.getWidth( 0 ), reader.getHeight( 0 ), crs );

                if ( envelope == null ) {
                    throw new ResourceInitException( "No envelope information could be read from GeoTIFF. "
                                                     + "Please add one to the GeoTIFF." );
                }

                LOG.debug( "Envelope from GeoTIFF was {}.", envelope );

                SpatialMetadata smd = new SpatialMetadata( envelope, singletonList( envelope.getCoordinateSystem() ) );

                List<TileDataLevel> matrices = new ArrayList<TileDataLevel>( num );

                for ( int i = 0; i < num; ++i ) {
                    int tw = reader.getTileWidth( i );
                    int th = reader.getTileHeight( i );
                    int width = reader.getWidth( i );
                    int height = reader.getHeight( i );
                    int numx = (int) Math.ceil( (double) width / (double) tw );
                    int numy = (int) Math.ceil( (double) height / (double) th );
                    double res = Math.max( envelope.getSpan0() / width, envelope.getSpan1() / height );
                    String id = Double.toString( res / DEFAULT_PIXEL_SIZE );
                    TileMatrix tmd = new TileMatrix( id, smd, tw, th, res, numx, numy );
                    GeoTIFFTileMatrix matrix = new GeoTIFFTileMatrix( tmd, file.first, i );
                    matrices.add( matrix );
                    LOG.debug( "Level {} has {}x{} tiles of {}x{} pixels, resolution is {}", new Object[] { i, numx,
                                                                                                           numy, tw,
                                                                                                           th, res } );
                }
                TileMatrixSet metadata = new TileMatrixSet( tmsId, "image/png", smd );
                TileDataSet tileMatrixSet = new DefaultTileDataSet( matrices, metadata );
                tileMatrixSets.put( tmsId, tileMatrixSet );
            }
        } catch ( Throwable e ) {
            throw new ResourceInitException( "Unable to create tile store.", e );
        } finally {
            try {
                if ( iis != null ) {
                    iis.close();
                }
                if ( reader != null ) {
                    reader.dispose();
                }
            } catch ( Throwable e ) {
                throw new ResourceInitException( "Unable to close image input stream, this should not happen.", e );
            }
        }
    }

    @Override
    public Collection<String> getTileMatrixSetIds() {
        return tileMatrixSets.keySet();
    }

    @Override
    public void destroy() {
        // nothing to destroy
    }

    @Override
    public Iterator<Tile> getTiles( String id, Envelope envelope, double resolution ) {
        return tileMatrixSets.get( id ).getTiles( envelope, resolution );
    }

    private static Envelope getEnvelope( IIOMetadata metaData, int width, int height, ICRS crs )
                            throws ResourceInitException {
        GeoTiffIIOMetadataAdapter geoTIFFMetaData = new GeoTiffIIOMetadataAdapter( metaData );
        try {
            if ( crs == null ) {
                int modelType = Integer.valueOf( geoTIFFMetaData.getGeoKey( GeoTiffIIOMetadataAdapter.GTModelTypeGeoKey ) );
                String epsgCode = null;
                if ( modelType == GeoTiffIIOMetadataAdapter.ModelTypeProjected ) {
                    epsgCode = geoTIFFMetaData.getGeoKey( GeoTiffIIOMetadataAdapter.ProjectedCSTypeGeoKey );
                } else if ( modelType == GeoTiffIIOMetadataAdapter.ModelTypeGeographic ) {
                    epsgCode = geoTIFFMetaData.getGeoKey( GeoTiffIIOMetadataAdapter.GeographicTypeGeoKey );
                }
                if ( epsgCode != null && epsgCode.length() != 0 ) {
                    try {
                        crs = CRSManager.lookup( "EPSG:" + epsgCode );
                    } catch ( UnknownCRSException e ) {
                        LOG.error( "No coordinate system found for EPSG:" + epsgCode );
                    }
                }
            }
            if ( crs == null ) {
                throw new ResourceInitException( "No CRS information could be read from GeoTIFF, "
                                                 + "and none was configured. Please configure a"
                                                 + " CRS or add one to the GeoTIFF." );
            }

            double[] tiePoints = geoTIFFMetaData.getModelTiePoints();
            double[] scale = geoTIFFMetaData.getModelPixelScales();
            if ( tiePoints != null && scale != null ) {

                RasterGeoReference rasterReference;
                if ( Math.abs( scale[0] - 0.5 ) < 0.001 ) { // when first pixel tie point is 0.5 -> center type
                    // rb: this might not always be right, see examples at
                    // http://www.remotesensing.org/geotiff/spec/geotiff3.html#3.2.1.
                    // search for PixelIsArea/PixelIsPoint to determine center/outer
                    rasterReference = new RasterGeoReference( CENTER, scale[0], -scale[1], tiePoints[3], tiePoints[4],
                                                              crs );
                } else {
                    rasterReference = new RasterGeoReference( OUTER, scale[0], -scale[1], tiePoints[3], tiePoints[4],
                                                              crs );
                }
                return rasterReference.getEnvelope( OUTER, width, height, crs );
            }

        } catch ( UnsupportedOperationException ex ) {
            LOG.debug( "couldn't read crs information in GeoTIFF" );
        }
        return null;
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
        throw new UnsupportedOperationException( "GeoTIFFTileStore does not support transactions." );
    }
}
