/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.tile.tilematrixset.geotiff;

import static java.util.Collections.singletonList;
import static javax.imageio.ImageIO.createImageInputStream;
import static javax.imageio.ImageIO.getImageReadersBySuffix;
import static org.deegree.commons.utils.MapUtils.DEFAULT_PIXEL_SIZE;
import static org.deegree.tile.persistence.geotiff.GeoTiffUtils.getEnvelopeAndCrs;
import static org.slf4j.LoggerFactory.getLogger;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReader;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.cs.refs.coordinatesystem.CRSRef;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.tilematrixset.geotiff.jaxb.GeoTIFFTileMatrixSetConfig;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;
import org.slf4j.Logger;

/**
 * This class is responsible for building geotiff based tile matrix sets.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * 
 * @since 3.4
 */
public class GeoTiffTileMatrixSetBuilder implements ResourceBuilder<TileMatrixSet> {

    private static final Logger LOG = getLogger( GeoTiffTileMatrixSetBuilder.class );

    private GeoTIFFTileMatrixSetConfig cfg;

    private ResourceMetadata<TileMatrixSet> metadata;

    public GeoTiffTileMatrixSetBuilder( GeoTIFFTileMatrixSetConfig cfg, ResourceMetadata<TileMatrixSet> metadata ) {
        this.cfg = cfg;
        this.metadata = metadata;
    }

    @Override
    public TileMatrixSet build() {
        ImageReader reader = null;
        ImageInputStream iis = null;

        try {
            ICRS crs = null;
            if ( cfg.getStorageCRS() != null ) {
                try {
                    CRSRef ref = CRSManager.getCRSRef( cfg.getStorageCRS() );
                    ref.getReferencedObject();
                    crs = ref;
                } catch ( ReferenceResolvingException e ) {
                    throw new ResourceInitException( "Invalid CRS: " + cfg.getStorageCRS() );
                }
            }

            ImageIO.scanForPlugins();
            
            Iterator<ImageReader> readers = getImageReadersBySuffix( "tiff" );
            while ( readers.hasNext() && !( reader instanceof TIFFImageReader ) ) {
                reader = readers.next();
            }

            File file = metadata.getLocation().resolveToFile( cfg.getFile() );

            if ( !file.exists() ) {
                throw new ResourceInitException( "The file " + file + " does not exist." );
            }

            iis = createImageInputStream( file );
            // this is already checked in provider
            reader.setInput( iis, false, true );
            int num = reader.getNumImages( true );
            IIOMetadata md = reader.getImageMetadata( 0 );
            Envelope envelope = getEnvelopeAndCrs( md, reader.getWidth( 0 ), reader.getHeight( 0 ), crs );

            if ( envelope == null ) {
                throw new ResourceInitException( "No envelope information could be read from GeoTIFF. "
                                                 + "Please add one to the GeoTIFF." );
            }

            LOG.debug( "Envelope from GeoTIFF was {}.", envelope );

            SpatialMetadata smd = new SpatialMetadata( envelope, singletonList( envelope.getCoordinateSystem() ) );

            List<TileMatrix> matrices = new ArrayList<TileMatrix>();

            for ( int i = 0; i < num; ++i ) {
                int tw = reader.getTileWidth( i );
                int th = reader.getTileHeight( i );
                int width = reader.getWidth( i );
                int height = reader.getHeight( i );
                int numx = (int) Math.ceil( (double) width / (double) tw );
                int numy = (int) Math.ceil( (double) height / (double) th );
                double res = Math.max( envelope.getSpan0() / width, envelope.getSpan1() / height );
                String id = Double.toString( res / DEFAULT_PIXEL_SIZE );
                TileMatrix tmd = new TileMatrix( id, smd, BigInteger.valueOf( tw ), BigInteger.valueOf( th ), res,
                                                 BigInteger.valueOf( numx ), BigInteger.valueOf( numy ) );
                matrices.add( tmd );
                LOG.debug( "Level {} has {}x{} tiles of {}x{} pixels, resolution is {}", new Object[] { i, numx, numy,
                                                                                                       tw, th, res } );
            }

            return new TileMatrixSet( file.getName().substring( 0, file.getName().length() - 4 ), null, matrices, smd,
                                      metadata );
        } catch ( Exception e ) {
            throw new ResourceInitException( "Could not create tile matrix set. Reason: " + e.getLocalizedMessage(), e );
        }
    }

}
