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
import java.util.Map;

/**
 * This interface is for abstraction of the raster writing handling.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author: $
 * 
 * @version $Revision: $
 * 
 */
public interface RasterWriter {

    /**
     * Write the raster into file.
     * 
     * @param raster
     * @param file
     * @param options
     *            additional information for the raster writer (format, etc)
     * @throws IOException
     *             may be thrown when the file can't be written
     */
    public void write( AbstractRaster raster, File file, Map<String, String> options )
                            throws IOException;

    /**
     * Check if the raster writer is able to write the given raster file.
     * 
     * @param raster
     * @param file
     * @return true if the class can write the raster
     */
    public boolean canWrite( AbstractRaster raster, File file );
}
