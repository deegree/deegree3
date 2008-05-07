//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

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
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deegree.commons.utils.FileUtils;

/**
 * This class reads and writes raster files. The actual raster loading and writing is handled by {@link RasterReader}
 * and {@link RasterWriter} implementations.
 * 
 * TODO use the new, not yet implemented, configuration framework to allow customization of the IO classes
 * 
 * @version $Revision: $
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author: $
 * 
 */
public class RasterFactory {

    private static ServiceLoader<RasterIOProvider> rasterIOLoader = ServiceLoader.load( RasterIOProvider.class );

    private static Log log = LogFactory.getLog( RasterFactory.class );

    /**
     * Load a raster from a file.
     * 
     * @param filename
     *            the filename of the raster
     * @return the loaded raster as an AbstractRaster
     * @throws IOException
     */
    public static AbstractRaster loadRasterFromFile( File filename )
                            throws IOException {
        String extension = FileUtils.getFileExtension( filename );
        RasterReader reader = getRasterReader( filename, extension );
        if ( reader == null ) {
            log.error( "couldn't find raster reader for " + filename );
            throw new IOException( "couldn't find raster reader" );
        }
        return reader.load( filename );
    }

    /**
     * Save a raster to a file.
     * 
     * @param raster
     * @param filename
     * @throws IOException
     */
    public static void saveRasterToFile( AbstractRaster raster, File filename )
                            throws IOException {
        saveRasterToFile( raster, filename, new HashMap<String, String>() );
    }

    /**
     * Save a raster to a file.
     * 
     * @param raster
     * @param filename
     * @param options
     *            map with options for the raster writer
     * @throws IOException
     */
    public static void saveRasterToFile( AbstractRaster raster, File filename, Map<String, String> options )
                            throws IOException {
        String format = FileUtils.getFileExtension( filename );
        RasterWriter writer = getRasterWriter( raster, filename, format, options );
        if ( writer == null ) {
            log.error( "couldn't find raster writer for " + filename );
            throw new IOException( "couldn't find raster writer" );
        }

        writer.write( raster, filename, options );
    }

    private static RasterReader getRasterReader( File filename, String format ) {
        for ( RasterIOProvider reader : rasterIOLoader ) {
            RasterReader possibleReader = reader.getRasterReader( format );
            if ( possibleReader != null && possibleReader.canLoad( filename ) ) {
                return possibleReader;
            }
        }
        return null;
    }

    private static RasterWriter getRasterWriter( AbstractRaster raster, File filename, String format,
                                                 @SuppressWarnings("unused")
                                                 Map<String, String> options ) {
        for ( RasterIOProvider writer : rasterIOLoader ) {
            RasterWriter possibleWriter = writer.getRasterWriter( format );
            if ( possibleWriter != null && possibleWriter.canWrite( raster, filename ) ) {
                return possibleWriter;
            }
        }
        return null;
    }
}
