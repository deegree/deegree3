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
package org.deegree.coverage.raster.io.imageio.geotiff;

/**
 * An interface defining the used geo tiff keys and their allowed values. All constants are in their original geotiff
 * name, and no (opposing java conventions) in upper case.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface GeoTiffKey {

    /** ID defining the crs model, http://www.remotesensing.org/geotiff/spec/geotiff6.html#6.3.1.1 */
    public static final int GTModelTypeGeoKey = 1024;

    /** ID defining the raster sample type, http://www.remotesensing.org/geotiff/spec/geotiff6.html#6.3.1.2 */
    public static final int GTRasterTypeGeoKey = 1025;

    /** ID defining a geographic crs, http://www.remotesensing.org/geotiff/spec/geotiff6.html#6.3.2.1 */
    public static final int GeographicTypeGeoKey = 2048;

    /** ID defining a projected crs, http://www.remotesensing.org/geotiff/spec/geotiff6.html#6.3.3.1 */
    public static final int ProjectedCSTypeGeoKey = 3072;

    /** http://www.remotesensing.org/geotiff/spec/geotiff6.html#6.3.4.1 */
    public static final int VerticalCSTypeGeoKey = 4096;

    /**
     * Value defining the raster sample point is center (area),
     * http://www.remotesensing.org/geotiff/spec/geotiff2.5.html#2.5.2.2
     */
    public static final char RasterPixelIsArea = 1;

    /**
     * Value defining the raster sample point is outer (point),
     * http://www.remotesensing.org/geotiff/spec/geotiff2.5.html#2.5.2.2
     */
    public static final char RasterPixelIsPoint = 2;

    /** Value defining the crs to be a projected model, http://www.remotesensing.org/geotiff/spec/geotiff6.html#6.3.1.1 */
    public static final char ModelTypeProjected = 1;

    /** Value defining the crs to be a geographic model, http://www.remotesensing.org/geotiff/spec/geotiff6.html#6.3.1.1 */
    public static final char ModelTypeGeographic = 2;

    /** Value defining the crs to be a geocentric model, http://www.remotesensing.org/geotiff/spec/geotiff6.html#6.3.1.1 */
    public static final char ModelTypeGeocentric = 3;
}
