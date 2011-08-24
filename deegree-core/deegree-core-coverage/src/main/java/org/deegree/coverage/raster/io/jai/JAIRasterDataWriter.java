//$HeadURL:svn+ssh://otonnhofer@svn.wald.intevation.org/deegree/deegree3/model/trunk/src/org/deegree/model/coverage/raster/implementation/io/JAIRasterWriter.java $
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
package org.deegree.coverage.raster.io.jai;

import static org.deegree.coverage.raster.utils.RasterFactory.rasterDataToImage;

import java.awt.image.BufferedImage;
import java.io.OutputStream;

import javax.media.jai.JAI;

import org.deegree.coverage.raster.data.RasterData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a simple writer for raster files.
 * 
 * It is based on Java Advanced Imaging and saves RasterData objects as a raster image.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author:otonnhofer $
 * 
 * @version $Revision:10872 $, $Date:2008-04-01 15:41:48 +0200 (Tue, 01 Apr 2008) $
 */
public class JAIRasterDataWriter {

    private static Logger LOG = LoggerFactory.getLogger( JAIRasterDataWriter.class );

    /**
     * Saves a RasterData to TIFF file
     * 
     * @param rasterData
     *            RasterData to save
     * @param filename
     *            filename for output raster image
     */
    public static void saveRasterDataToFile( RasterData rasterData, String filename ) {
        JAIRasterDataWriter.saveRasterDataToFile( rasterData, filename, "TIFF" );
    }

    /**
     * Saves a RasterData to file.
     * 
     * The format must be supported by JAI (i.e. BMP, JPEG, PNG, PNM, TIFF)
     * 
     * @param sourceRaster
     *            RasterData to save
     * @param filename
     *            filename for output raster image
     * @param format
     *            format for output raster
     */
    public static void saveRasterDataToFile( RasterData sourceRaster, String filename, String format ) {
        BufferedImage result = rasterDataToImage( sourceRaster );
        try {
            // jai_imageio registers ImageWrite with support for more formats (eg. JPEG 2000)
            LOG.debug( "trying to write: " + filename + " with format: " + format );
            JAI.create( "ImageWrite", result, filename, format, null );
        } catch ( IllegalArgumentException ex ) {
            // else use build in "filestore"
            JAI.create( "filestore", result, filename, format, null );
        }
    }

    /**
     * Saves a RasterData to stream.
     * 
     * The format must be supported by JAI (i.e. BMP, JPEG, PNG, PNM, TIFF)
     * 
     * @param sourceRaster
     *            RasterData to save
     * @param stream
     *            stream for output raster image
     * @param format
     *            format for output raster
     */
    public static void saveRasterDataToStream( RasterData sourceRaster, OutputStream stream, String format ) {
        BufferedImage result = rasterDataToImage( sourceRaster );
        try {
            // jai_imageio registers ImageWrite with support for more formats (eg. JPEG 2000)
            LOG.debug( "trying to write to stream with format: " + format );
            JAI.create( "ImageWrite", result, stream, format, null );
        } catch ( IllegalArgumentException ex ) {
            // else use build in "filestore"
            JAI.create( "encode", result, stream, format, null );
        }
    }
}
