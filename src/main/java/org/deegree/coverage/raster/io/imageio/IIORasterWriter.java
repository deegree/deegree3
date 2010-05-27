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
package org.deegree.coverage.raster.io.imageio;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import org.deegree.commons.utils.FileUtils;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.RasterWriter;
import org.deegree.coverage.raster.io.WorldFileAccess;
import org.deegree.coverage.raster.io.imageio.geotiff.GeoTiffWriter;
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
public class IIORasterWriter implements RasterWriter {

    private static final Logger LOG = LoggerFactory.getLogger( IIORasterWriter.class );

    private static final Set<String> SUPPORTED_TYPES;

    static {
        SUPPORTED_TYPES = new HashSet<String>();

        String[] writerFormatNames = ImageIO.getWriterFormatNames();
        if ( writerFormatNames != null ) {
            for ( String format : writerFormatNames ) {
                if ( format != null && !"".equals( format.trim() ) && !format.contains( " " ) ) {
                    SUPPORTED_TYPES.add( format.toLowerCase() );
                }
            }
        }
    }

    @Override
    public void write( AbstractRaster raster, File file, RasterIOOptions options )
                            throws IOException {
        LOG.debug( "writing " + file + " with ImageIO" );
        String format = options != null ? options.get( RasterIOOptions.OPT_FORMAT ) : null;
        if ( format == null ) {
            format = FileUtils.getFileExtension( file );
        }

        LOG.debug( "Writing raster with width: {} height: {}", raster.getColumns(), raster.getRows() );
        if ( "geotiff".equals( format ) ) {
            GeoTiffWriter.save( raster, file );
        } else {
            IIORasterDataWriter.saveRasterDataToFile( raster.getAsSimpleRaster().getRasterData(), file, format );
            RasterGeoReference rasterReference = raster.getRasterReference();
            WorldFileAccess.writeWorldFile( rasterReference, file );
        }
    }

    @Override
    public void write( AbstractRaster raster, OutputStream out, RasterIOOptions options )
                            throws IOException {
        LOG.debug( "writing to stream with ImageIO" );
        String format = options != null ? options.get( RasterIOOptions.OPT_FORMAT ) : null;
        if ( "geotiff".equals( format ) ) {
            GeoTiffWriter.save( raster, out );
        } else {
            IIORasterDataWriter.saveRasterDataToStream( raster.getAsSimpleRaster().getRasterData(), out, format );
        }
    }

    @Override
    public boolean canWrite( AbstractRaster raster, RasterIOOptions options ) {
        String format = options.get( RasterIOOptions.OPT_FORMAT );
        if ( "geotiff".equalsIgnoreCase( format ) ) {
            format = "tiff";
        }
        if ( ImageIO.getImageWritersBySuffix( format ).hasNext() ) {
            return true;
        }
        return false;
    }

    @Override
    public Set<String> getSupportedFormats() {
        return SUPPORTED_TYPES;
    }

}
