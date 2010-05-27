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

import static org.deegree.coverage.raster.utils.RasterFactory.rasterDataToImage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.deegree.coverage.raster.data.RasterData;

/**
 * 
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class IIORasterDataWriter {

    /**
     * Saves a RasterData to file.
     * 
     * The format must be supported by JAI (i.e. BMP, JPEG, PNG, PNM, TIFF)
     * 
     * @param sourceRaster
     *            RasterData to save
     * @param file
     *            file for output raster image
     * @param format
     *            format for output raster
     * @throws IOException
     */
    public static void saveRasterDataToFile( RasterData sourceRaster, File file, String format )
                            throws IOException {
        BufferedImage result = rasterDataToImage( sourceRaster );
        boolean written = ImageIO.write( result, format, file );
        if ( !written ) {
            throw new IOException( "Could't write raster with ImageIO for format: " + format );
        }
    }

    /**
     * Saves a RasterData to stream.
     * 
     * The format must be supported by JAI (i.e. BMP, JPEG, PNG, PNM, TIFF)
     * 
     * @param sourceRaster
     *            RasterData to save
     * @param out
     *            stream for output raster image
     * @param format
     *            format for output raster
     * @throws IOException
     */
    public static void saveRasterDataToStream( RasterData sourceRaster, OutputStream out, String format )
                            throws IOException {
        BufferedImage result = rasterDataToImage( sourceRaster );
        boolean written = ImageIO.write( result, format, out );
        out.flush();

        if ( !written ) {
            throw new IOException( "could't find ImageIO writer for " + format );
        }

    }

}
