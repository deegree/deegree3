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
 * This class represents the possible GeoTIFF keys (from 1024 to 4099) in the GeoKeyDirectoryTag (34735).
 * 
 * @author <a href="mailto:schaefer@lat-lon.de">Axel Schaefer </A>
 * @author last edited by: $Author$
 * @version 2.0. $Revision$, $Date$
 * @since
 */
public class GeoTiffKey {

    public static final int GTModelTypeGeoKey = 1024;

    public static final int GTRasterTypeGeoKey = 1025;

    public static final int GTCitationGeoKey = 1026;

    public static final int GeographicTypeGeoKey = 2048;

    public static final int GeogCitationGeoKey = 2049;

    public static final int GeogGeodeticDatumGeoKey = 2050;

    public static final int GeogPrimeMeridianGeoKey = 2051;

    public static final int GeogLinearUnitsGeoKey = 2052;

    public static final int GeogLinearUnitSizeGeoKey = 2053;

    public static final int GeogAngularUnitsGeoKey = 2054;

    public static final int GeogAngularUnitSizeGeoKey = 2055;

    public static final int GeogEllipsoidGeoKey = 2056;

    public static final int GeogSemiMajorAxisGeoKey = 2057;

    public static final int GeogSemiMinorAxisGeoKey = 2058;

    public static final int GeogInvFlatteningGeoKey = 2059;

    public static final int GeogAzimuthUnitsGeoKey = 2060;

    public static final int GeogPrimeMeridianLongGeoKey = 2061;

    public static final int ProjectedCSTypeGeoKey = 3072;

    public static final int PCSCitationGeoKey = 3073;

    public static final int ProjectionGeoKey = 3074;

    public static final int ProjCoordTransGeoKey = 3075;

    public static final int ProjLinearUnitsGeoKey = 3076;

    public static final int ProjLinearUnitSizeGeoKey = 3077;

    public static final int ProjStdParallel1GeoKey = 3078;

    // public static final int ProjStdParallelGeoKey =$ProjStdParallel1GeoKey;
    public static final int ProjStdParallel2GeoKey = 3079;

    public static final int ProjNatOriginLongGeoKey = 3080;

    // public static final int ProjOriginLongGeoKey =$ProjNatOriginLongGeoKey
    public static final int ProjNatOriginLatGeoKey = 3081;

    // public static final int ProjOriginLatGeoKey =$ProjNatOriginLatGeoKey
    public static final int ProjFalseEastingGeoKey = 3082;

    public static final int ProjFalseNorthingGeoKey = 3083;

    public static final int ProjFalseOriginLongGeoKey = 3084;

    public static final int ProjFalseOriginLatGeoKey = 3085;

    public static final int ProjFalseOriginEastingGeoKey = 3086;

    public static final int ProjFalseOriginNorthingGeoKey = 3087;

    public static final int ProjCenterLongGeoKey = 3088;

    public static final int ProjCenterLatGeoKey = 3089;

    public static final int ProjCenterEastingGeoKey = 3090;

    public static final int ProjCenterNorthingGeoKey = 3091;

    public static final int ProjScaleAtNatOriginGeoKey = 3092;

    // public static final int ProjScaleAtOriginGeoKey
    // =$ProjScaleAtNatOriginGeoKey
    public static final int ProjScaleAtCenterGeoKey = 3093;

    public static final int ProjAzimuthAngleGeoKey = 3094;

    public static final int ProjStraightVertPoleLongGeoKey = 3095;

    public static final int VerticalCSTypeGeoKey = 4096;

    public static final int VerticalCitationGeoKey = 4097;

    public static final int VerticalDatumGeoKey = 4098;

    public static final int VerticalUnitsGeoKey = 4099;

    /**
     * private default constructor prevents instantiation
     */
    private GeoTiffKey() {
    }

}
