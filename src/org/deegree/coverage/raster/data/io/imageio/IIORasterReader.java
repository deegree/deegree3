//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.coverage.raster.data.io.imageio;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.container.RasterDataContainer;
import org.deegree.coverage.raster.data.container.RasterDataContainerFactory;
import org.deegree.coverage.raster.data.io.WorldFileAccess;
import org.deegree.coverage.raster.geom.RasterReference;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.crs.CRS;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.geometry.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class IIORasterReader implements RasterReader {

    private File file;

    private static final Set<String> SUPPORTED_TYPES;

    private static Logger LOG = LoggerFactory.getLogger( IIORasterReader.class );

    static {
        SUPPORTED_TYPES = new HashSet<String>();

        String[] readerFormatNames = ImageIO.getReaderFormatNames();
        if ( readerFormatNames != null ) {
            for ( String format : readerFormatNames ) {
                if ( format != null && !"".equals( format.trim() ) && !format.contains( " " ) ) {
                    SUPPORTED_TYPES.add( format.toLowerCase() );
                }
            }
        }
        // SUPPORTED_TYPES.add( IIORasterReader.class.getCanonicalName() );
        // SUPPORTED_TYPES.add( "iio" );
        // SUPPORTED_TYPES.add( "imageio" );
        // SUPPORTED_TYPES.add( "iio-reader" );
        // String[] types = new String[] { "jpg", "jpeg", "png", "tif", "tiff", "jp2", "gif" };
        //
        // for ( String type : types ) {
        // Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix( type );
        // if ( iter != null && iter.hasNext() ) {
        // SUPPORTED_TYPES.add( type.toLowerCase() );
        // LOG.debug( "register ImageReader for " + type );
        // } else {
        // LOG.error( "no ImageReader for " + type + " found" );
        // }
        // }
    }

    public boolean canLoad( File filename ) {
        // TODO Auto-generated method stub
        return true;
    }

    public AbstractRaster load( File file, RasterIOOptions options )
                            throws IOException {
        LOG.debug( "reading " + file + " with ImageIO" );
        this.file = file;
        IIORasterDataReader reader = new IIORasterDataReader( file, options.get( RasterIOOptions.OPT_FORMAT ) );
        AbstractRaster r = loadFromReader( reader, options );
        return r;
    }

    @Override
    public AbstractRaster load( InputStream stream, RasterIOOptions options )
                            throws IOException {
        IIORasterDataReader reader = new IIORasterDataReader( stream, options.get( RasterIOOptions.OPT_FORMAT ) );
        return loadFromReader( reader, options );
    }

    private AbstractRaster loadFromReader( IIORasterDataReader reader, RasterIOOptions options ) {

        int width = reader.getWidth();
        int height = reader.getHeight();

        RasterReference rasterReference;

        MetaDataReader metaDataReader = new MetaDataReader( reader.getMetaData() );
        CoordinateSystem crs = metaDataReader.getCRS();
        rasterReference = metaDataReader.getRasterReference();

        if ( rasterReference == null ) {
            if ( options.hasEnvelope() ) {
                rasterReference = options.getEnvelope();
            } else {
                rasterReference = new RasterReference( 0.5, height - 0.5, 1.0, -1.0 );
                if ( options.readWorldFile() ) {
                    try {
                        if ( file != null ) {
                            rasterReference = WorldFileAccess.readWorldFile( file, RasterReference.Type.CENTER );
                        }
                    } catch ( IOException e ) {
                        //
                    }
                }
            }
        }

        reader.close();

        CRS readCRS = crs == null ? null : new CRS( crs );
        Envelope envelope = rasterReference.getEnvelope( width, height, readCRS );

        // RasterDataContainer source = RasterDataContainerFactory.withDefaultLoadingPolicy( reader );
        RasterDataContainer source = RasterDataContainerFactory.withLoadingPolicy( reader, options.getLoadingPolicy() );
        return new SimpleRaster( source, envelope, rasterReference );
    }

    @Override
    public Set<String> getSupportedFormats() {
        return SUPPORTED_TYPES;
    }

}
