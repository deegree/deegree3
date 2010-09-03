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
package org.deegree.services.wcs.coverages;

import java.io.IOException;
import java.io.OutputStream;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.services.wcs.model.CoverageResult;

/**
 * This is a container for coverage results.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class SimpleRasterResult implements CoverageResult {

    private final AbstractRaster result;

    private final String format;

    /**
     * @param result
     * @param format
     *            the output format (tiff, jpeg, etc.)
     */
    public SimpleRasterResult( AbstractRaster result, String format ) {
        this.result = result;
        this.format = format;
    }

    @Override
    public void write( OutputStream out )
                            throws IOException {
        RasterIOOptions options = new RasterIOOptions();

        // lets fake GeoTiff until we have a real GeoTiff writer (TODO)
        if ( format.equalsIgnoreCase( "GeoTiff" ) ) {
            options.add( RasterIOOptions.OPT_FORMAT, "tiff" );
        } else {
            options.add( RasterIOOptions.OPT_FORMAT, format );
        }
        RasterFactory.saveRasterToStream( result, out, options );

    }

}
