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

import static javax.imageio.ImageIO.createImageInputStream;
import static javax.imageio.ImageIO.getImageReadersBySuffix;
import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;
import static org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation.CENTER;
import static org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation.OUTER;
import static org.slf4j.LoggerFactory.getLogger;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReader;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.io.imageio.geotiff.GeoTiffIIOMetadataAdapter;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.deegree.tile.DefaultTileDataSet;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.persistence.GenericTileStore;
import org.deegree.tile.persistence.TileStoreProvider;
import org.deegree.tile.persistence.geotiff.jaxb.GeoTIFFTileStoreJAXB;
import org.deegree.tile.tilematrixset.TileMatrixSetManager;
import org.slf4j.Logger;

/**
 * The <code>GeoTIFFTileStoreProvider</code> provides a <code>TileMatrixSet</code> out of a GeoTIFF file (tiled
 * BIGTIFF).
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public class GeoTIFFTileStoreProvider implements TileStoreProvider {

    private static final Logger LOG = getLogger( GeoTIFFTileStoreProvider.class );

    private static final URL SCHEMA = GeoTIFFTileStoreProvider.class.getResource( "/META-INF/schemas/datasource/tile/geotiff/3.2.0/geotiff.xsd" );

    private DeegreeWorkspace workspace;

    @Override
    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    @Override
    public GenericTileStore create( URL configUrl )
                            throws ResourceInitException {
        try {
            GeoTIFFTileStoreJAXB cfg = (GeoTIFFTileStoreJAXB) unmarshall( "org.deegree.tile.persistence.geotiff.jaxb",
                                                                          SCHEMA, configUrl, workspace );
            Iterator<ImageReader> readers = getImageReadersBySuffix( "tiff" );
            ImageReader reader = null;
            while ( readers.hasNext() && !( reader instanceof TIFFImageReader ) ) {
                reader = readers.next();
            }

            if ( reader == null ) {
                throw new ResourceInitException( "No TIFF reader was found for imageio." );
            }

            Map<String, TileDataSet> map = new HashMap<String, TileDataSet>();
            for ( GeoTIFFTileStoreJAXB.TileDataSet tds : cfg.getTileDataSet() ) {
                String id = tds.getIdentifier();
                if ( id == null ) {
                    id = new File( tds.getFile() ).getName();
                }

                File file = new File( configUrl.toURI().resolve( tds.getFile() ) );

                if ( !file.exists() ) {
                    LOG.warn( "The file {} does not exist, skipping.", file );
                    continue;
                }

                ImageInputStream iis = createImageInputStream( file );
                reader.setInput( iis, false, true );
                IIOMetadata md = reader.getImageMetadata( 0 );
                Envelope envelope = getEnvelope( md, reader.getWidth( 0 ), reader.getHeight( 0 ), null );

                if ( envelope == null ) {
                    throw new ResourceInitException( "No envelope information could be read from GeoTIFF. "
                                                     + "Please add one to the GeoTIFF." );
                }

                LOG.debug( "Envelope from GeoTIFF was {}.", envelope );

                map.put( id, buildTileDataSet( tds, configUrl, envelope ) );
            }
            return new GenericTileStore( map );
        } catch ( Throwable e ) {
            throw new ResourceInitException( "Unable to create tile store.", e );
        }
    }

    private TileDataSet buildTileDataSet( GeoTIFFTileStoreJAXB.TileDataSet cfg, URL configUrl, Envelope envelope )
                            throws ResourceInitException, URISyntaxException {
        TileMatrixSetManager mgr = workspace.getSubsystemManager( TileMatrixSetManager.class );
        String filename = cfg.getFile();
        String format = cfg.getImageFormat();
        String tmsId = cfg.getTileMatrixSetId();

        File file = new File( configUrl.toURI().resolve( filename ) );

        TileMatrixSet tms = mgr.get( tmsId );
        if ( tms == null ) {
            throw new ResourceInitException( "The tile matrix set with id " + tmsId + " was not available." );
        }

        List<TileDataLevel> levels = new ArrayList<TileDataLevel>();
        double x = envelope.getMin().get0() - tms.getSpatialMetadata().getEnvelope().getMin().get0();
        double y = envelope.getMax().get1() - tms.getSpatialMetadata().getEnvelope().getMax().get1();

        int idx = 0;
        for ( TileMatrix tm : tms.getTileMatrices() ) {
            int xoff = (int) Math.round( x / tm.getTileWidth() );
            int yoff = (int) Math.round( y / tm.getTileHeight() );
            int numx = (int) Math.round( envelope.getSpan0() / tm.getTileWidth() );
            int numy = (int) Math.round( envelope.getSpan1() / tm.getTileHeight() );
            levels.add( new GeoTIFFTileDataLevel( tm, file, idx++, xoff, yoff, numx, numy ) );
        }

        return new DefaultTileDataSet( levels, tms, format );
    }

    private static Envelope getEnvelope( IIOMetadata metaData, int width, int height, ICRS crs )
                            throws ResourceInitException {
        GeoTiffIIOMetadataAdapter geoTIFFMetaData = new GeoTiffIIOMetadataAdapter( metaData );
        try {
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

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] {};
    }

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/datasource/tile/geotiff";
    }

    @Override
    public URL getConfigSchema() {
        return SCHEMA;
    }

    @Override
    public List<File> getTileStoreDependencies( File config ) {
        return Collections.<File> emptyList();
    }

}
