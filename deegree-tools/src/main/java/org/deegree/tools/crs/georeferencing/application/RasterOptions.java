//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.tools.crs.georeferencing.application;

import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.tools.crs.georeferencing.communication.GRViewerGUI;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class RasterOptions {

    private RasterIOOptions options;

    private static final String RASTERIO_LAYER = "RASTERIO_LAYER";

    private static final String RASTER_FORMATLIST = "RASTER_FORMATLIST";

    private static final String RASTER_URL = "RASTER_URL";

    private static final String RIO_WMS_SYS_ID = "RASTERIO_WMS_SYS_ID";

    private static final String RIO_WMS_MAX_SCALE = "RASTERIO_WMS_MAX_SCALE";

    private static final String RIO_WMS_DEFAULT_FORMAT = "RASTERIO_WMS_DEFAULT_FORMAT";

    private static final String RIO_WMS_MAX_WIDTH = "RASTERIO_WMS_MAX_WIDTH";

    private static final String RIO_WMS_MAX_HEIGHT = "RASTERIO_WMS_MAX_HEIGHT";

    private static final String RIO_WMS_LAYERS = "RASTERIO_WMS_REQUESTED_LAYERS";

    private static final String RIO_WMS_ENABLE_TRANSPARENT = "RASTERIO_WMS_ENABLE_TRANSPARENCY";

    private static final String RIO_WMS_TIMEOUT = "RIO_WMS_TIMEOUT";

    /**
     * Specifies the size of the full drawn side.
     */
    private static final String RESOLUTION = "RESOLUTION";

    public RasterOptions( GRViewerGUI view ) {
        options = new RasterIOOptions();

        options.add( RasterIOOptions.CRS, "EPSG:4326" );
        // options.add( RasterIOOptions.CRS, "EPSG:32618" );
        // options.add( RIO_WMS_LAYERS, "populationgrid" );
        options.add( RESOLUTION, "1.0" );
        options.add( RIO_WMS_LAYERS, "root" );
        // options.add( RASTER_FORMATLIST, "image/jpeg" );
        options.add( RASTER_URL, view.openUrl() );
        options.add( RasterIOOptions.OPT_FORMAT, "WMS_111" );
        options.add( RIO_WMS_SYS_ID, view.openUrl() );
        options.add( RIO_WMS_MAX_SCALE, "0.1" );
        options.add( RIO_WMS_DEFAULT_FORMAT, "image/jpeg" );
        // specify the quality
        options.add( RIO_WMS_MAX_WIDTH, Integer.toString( 200 ) );
        options.add( RIO_WMS_MAX_HEIGHT, Integer.toString( 200 ) );
        options.add( RIO_WMS_ENABLE_TRANSPARENT, "true" );
        // options.add( RIO_WMS_TIMEOUT, "1000" );
    }

    public RasterIOOptions getOptions() {
        return options;
    }

}
