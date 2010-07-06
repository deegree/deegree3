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

    private static final String LEFT_LOWER_X = "LEFT_LOWER_X";

    private static final String LEFT_LOWER_Y = "LEFT_LOWER_Y";

    private static final String RIGHT_UPPER_X = "RIGHT_UPPER_X";

    private static final String RIGHT_UPPER_Y = "RIGHT_UPPER_Y";

    /**
     * Specifies the size of the full drawn side.
     */
    private static final String RESOLUTION = "RESOLUTION";

    public RasterOptions( GRViewerGUI view ) {
        options = new RasterIOOptions();

        options.add( RasterIOOptions.CRS, "EPSG:4326" );
        options.add( RIO_WMS_DEFAULT_FORMAT, "image/png" );
        options.add( RIO_WMS_LAYERS, "cite:BasicPolygons" );
        options.add( LEFT_LOWER_X, "-2.0" );
        options.add( LEFT_LOWER_Y, "-1.0" );
        options.add( RIGHT_UPPER_X, "2.0" );
        options.add( RIGHT_UPPER_Y, "6.0" );

        // options.add( RasterIOOptions.CRS, "EPSG:4326" );
        // options.add( RIO_WMS_DEFAULT_FORMAT, "image/png" );
        // options.add( RIO_WMS_LAYERS, "cite:Lakes" );
        // options.add( LEFT_LOWER_X, "6.0E-4" );
        // options.add( LEFT_LOWER_Y, "-0.0018" );
        // options.add( RIGHT_UPPER_X, "0.0031" );
        // options.add( RIGHT_UPPER_Y, "-1.0E-4" );

        // (minX, minY, maxX, maxY) -> 2568720.0,5629890.0,2568800.0,5629970.0
        // options.add( RasterIOOptions.CRS, "EPSG:31466" );
        // options.add( RIO_WMS_DEFAULT_FORMAT, "image/png" );
        // options.add( RIO_WMS_LAYERS, "DTK" );
        // options.add( LEFT_LOWER_X, "2568720.0" );
        // options.add( LEFT_LOWER_Y, "5629890.0" );
        // options.add( RIGHT_UPPER_X, "2568800.0" );
        // options.add( RIGHT_UPPER_Y, "5629970.0" );

        // options.add( RasterIOOptions.CRS, "EPSG:32618" );
        // options.add( RIO_WMS_DEFAULT_FORMAT, "image/jpeg" );
        // options.add( RIO_WMS_LAYERS, "populationgrid" );

        options.add( RESOLUTION, "1.0" );
        options.add( RASTER_URL, view.openUrl() );
        options.add( RasterIOOptions.OPT_FORMAT, "WMS_111" );
        options.add( RIO_WMS_SYS_ID, view.openUrl() );
        options.add( RIO_WMS_MAX_SCALE, "0.1" );

        // specify the quality
        options.add( RIO_WMS_MAX_WIDTH, Integer.toString( 500 ) );
        options.add( RIO_WMS_MAX_HEIGHT, Integer.toString( 500 ) );
        options.add( RIO_WMS_ENABLE_TRANSPARENT, "true" );
        // options.add( RIO_WMS_TIMEOUT, "1000" );
    }

    public RasterIOOptions getOptions() {
        return options;
    }

}
// http://gdi.bonn.de/mapbender/dhtml/imagepack.php?basis=gdi.bonn.de/Deegree2wms/services&SERVICE=WMS&REQUEST=GetMap&SRS=EPSG:31466&FORMAT=image/png&STYLES=&VERSION=1.1.1&TRANSPARENT=FALSE&BGCOLOR=0x000000&LAYERS=DD_Stadtplan&interface_id=3&quality=middle&BBOX=
// 2568347,5610821,2589019,5627689
