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
/*
 * (c) 2004 Mike Nidel
 *
 * Take, Modify, Distribute freely
 * Buy, Sell, Pass it off as your own
 *
 * Use this code at your own risk, the author makes no guarantee
 * of performance and retains no liability for the failure of this
 * software.
 *
 * If you feel like it, send any suggestions for improvement or
 * bug fixes, or modified source code to mike 'at' gelbin.org
 *
 * Do not taunt Happy Fun Ball.
 *
 * http://www.happyfunball.com/hfb.html
 *
 */

package org.deegree.coverage.raster.io.imageio.geotiff;

import java.util.StringTokenizer;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

import it.geosolutions.imageio.plugins.tiff.GeoTIFFTagSet;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class provides an abstraction from the details of TIFF data access for the purpose of retrieving GeoTIFF
 * metadata from an image.
 * <p>
 * All of the GeoKey values are included here as constants, and the portions of the GeoTIFF specification pertaining to
 * each have been copied for easy access.
 * <p>
 * The majority of the possible GeoKey values and their meanings are NOT reproduced here. Only the most important GeoKey
 * code values have been copied, for others see the specification.
 * <p>
 * Convenience methods have been included to retrieve the various TIFFFields that are not part of the GeoKey directory,
 * such as the Model Transformation and Model TiePoints. Retrieving a GeoKey from the GeoKey directory is a bit more
 * specialized and requires knowledge of the correct key code.
 * <p>
 * Making use of the geographic metadata still requires some basic understanding of the GeoKey values that is not
 * provided here.
 * <p>
 * For more information see the GeoTIFF specification at http://www.remotesensing.org/geotiff/spec/geotiffhome.html
 * 
 * @author Mike Nidel
 * @version 1.1
 * @hist 2005-11-03 -- Pascal Quesseveur <quesseveur@abaksystemes.fr>:
 *       <ul>
 *       <li> add support for Rational tags, <li> correct a bug with substring() in getTiffAscii, <li> limit line length
 *       to 80 cars, <li> change private access to protected for some accessors, <li> add PCS_WGS72BE_UTM_zone_60N
 *       constant
 *       </ul>
 */
public class GeoTiffIIOMetadataAdapter {

    // The following values are taken from the GeoTIFF specification

    // GeoTIFF Configuration GeoKeys

    /**
     * GTModelTypeGeoKey Key ID = 1024 Type: SHORT (code) Values: Section 6.3.1.1 Codes This GeoKey defines the general
     * type of model Coordinate system used, and to which the raster space will be transformed: unknown, Geocentric
     * (rarely used), Geographic, Projected Coordinate System, or user-defined. If the coordinate system is a PCS, then
     * only the PCS code need be specified. If the coordinate system does not fit into one of the standard registered
     * PCS'S, but it uses one of the standard projections and datums, then its should be documented as a PCS model with
     * "user-defined" type, requiring the specification of projection parameters, etc.
     * 
     * GeoKey requirements for User-Defined Model Type (not advisable): GTCitationGeoKey
     * 
     */
    public static final int GTModelTypeGeoKey = 1024;

    /**
     * GTRasterTypeGeoKey Key ID = 1025 Type = Section 6.3.1.2 codes
     * 
     * This establishes the Raster Space coordinate system used; there are currently only two, namely RasterPixelIsPoint
     * and RasterPixelIsArea. No user-defined raster spaces are currently supported. For variance in imaging display
     * parameters, such as pixel aspect-ratios, use the standard TIFF 6.0 device-space tags instead.
     * 
     */
    public static final int GTRasterTypeGeoKey = 1025;

    /**
     * GTCitationGeoKey Key ID = 1026 Type = ASCII
     * 
     * As with all the "Citation" GeoKeys, this is provided to give an ASCII reference to published documentation on the
     * overall configuration of this GeoTIFF file.
     * 
     */
    public static final int GTCitationGeoKey = 1026;

    // Geographic Coordinate System Parameter GeoKeys

    /**
     * GeographicTypeGeoKey Key ID = 2048 Type = SHORT (code) Values = Section 6.3.2.1 Codes
     * http://www.remotesensing.org/geotiff/spec/geotiff6.html#6.3.2.1
     * 
     * This key may be used to specify the code for the geographic coordinate system used to map lat-long to a specific
     * ellipsoid over the earth.
     * 
     * GeoKey Requirements for User-Defined geographic CS:
     * 
     * GeogCitationGeoKey GeogGeodeticDatumGeoKey GeogAngularUnitsGeoKey (if not degrees) GeogPrimeMeridianGeoKey (if
     * not Greenwich)
     * 
     */
    public static final int GeographicTypeGeoKey = 2048;

    /**
     * GeogCitationGeoKey Key ID = 2049 Type = ASCII Values = text
     * 
     * General citation and reference for all Geographic CS parameters.
     * 
     */
    public static final int GeogCitationGeoKey = 2049;

    /**
     * GeogGeodeticDatumGeoKey Key ID = 2050 Type = SHORT (code) Values = Section 6.3.2.2 Codes
     * http://www.remotesensing.org/geotiff/spec/geotiff6.html#6.3.2.2
     * 
     * This key may be used to specify the horizontal datum, defining the size, position and orientation of the
     * reference ellipsoid used in user-defined geographic coordinate systems.
     * 
     * GeoKey Requirements for User-Defined Horizontal Datum: GeogCitationGeoKey GeogEllipsoidGeoKey
     * 
     */
    public static final int GeogGeodeticDatumGeoKey = 2050;

    /**
     * GeogPrimeMeridianGeoKey Key ID = 2051 Type = SHORT (code) Units: Section 6.3.2.4 code
     * http://www.remotesensing.org/geotiff/spec/geotiff6.html#6.3.2.4
     * 
     * Allows specification of the location of the Prime meridian for user-defined geographic coordinate systems. The
     * default standard is Greenwich, England.
     * 
     */
    public static final int GeogPrimeMeridianGeoKey = 2051;

    /**
     * GeogPrimeMeridianLongGeoKey Key ID = 2061 Type = DOUBLE Units = GeogAngularUnits
     * 
     * This key allows definition of user-defined Prime Meridians, the location of which is defined by its longitude
     * relative to Greenwich.
     * 
     */
    public static final int GeogPrimeMeridianLongGeoKey = 2061;

    /**
     * GeogLinearUnitsGeoKey Key ID = 2052 Type = SHORT Values: Section 6.3.1.3 Codes
     * http://www.remotesensing.org/geotiff/spec/geotiff6.html#6.3.1.3
     * 
     * Allows the definition of geocentric CS linear units for user-defined GCS.
     * 
     */
    public static final int GeogLinearUnitsGeoKey = 2052;

    /**
     * GeogLinearUnitSizeGeoKey Key ID = 2053 Type = DOUBLE Units: meters
     * 
     * Allows the definition of user-defined linear geocentric units, as measured in meters.
     * 
     */
    public static final int GeogLinearUnitSizeGeoKey = 2053;

    /**
     * GeogAngularUnitsGeoKey Key ID = 2054 Type = SHORT (code) Values = Section 6.3.1.4 Codes
     * 
     * Allows the definition of geocentric CS Linear units for user-defined GCS and for ellipsoids.
     * 
     * GeoKey Requirements for "user-defined" units: GeogCitationGeoKey GeogAngularUnitSizeGeoKey
     * 
     */
    public static final int GeogAngularUnitsGeoKey = 2054;

    /**
     * GeogAngularUnitSizeGeoKey Key ID = 2055 Type = DOUBLE Units: radians
     * 
     * Allows the definition of user-defined angular geographic units, as measured in radians.
     * 
     */
    public static final int GeogAngularUnitSizeGeoKey = 2055;

    /**
     * GeogEllipsoidGeoKey Key ID = 2056 Type = SHORT (code) Values = Section 6.3.2.3 Codes
     * http://www.remotesensing.org/geotiff/spec/geotiff6.html#6.3.2.3
     * 
     * This key may be used to specify the coded ellipsoid used in the geodetic datum of the Geographic Coordinate
     * System.
     * 
     * GeoKey Requirements for User-Defined Ellipsoid: GeogCitationGeoKey [GeogSemiMajorAxisGeoKey,
     * [GeogSemiMinorAxisGeoKey | GeogInvFlatteningGeoKey] ]
     * 
     */
    public static final int GeogEllipsoidGeoKey = 2056;

    /**
     * GeogSemiMajorAxisGeoKey Key ID = 2057 Type = DOUBLE Units: Geocentric CS Linear Units
     * 
     * Allows the specification of user-defined Ellipsoid Semi-Major Axis (a).
     * 
     */
    public static final int GeogSemiMajorAxisGeoKey = 2057;

    /**
     * GeogSemiMinorAxisGeoKey Key ID = 2058 Type = DOUBLE Units: Geocentric CS Linear Units
     * 
     * Allows the specification of user-defined Ellipsoid Semi-Minor Axis (b).
     * 
     */
    public static final int GeogSemiMinorAxisGeoKey = 2058;

    /**
     * GeogInvFlatteningGeoKey Key ID = 2059 Type = DOUBLE Units: none.
     * 
     * Allows the specification of the inverse of user-defined Ellipsoid's flattening parameter (f). The
     * eccentricity-squared e^2 of the ellipsoid is related to the non-inverted f by: e^2 = 2*f - f^2
     * 
     * Note: if the ellipsoid is spherical the inverse-flattening becomes infinite; use the GeogSemiMinorAxisGeoKey
     * instead, and set it equal to the semi-major axis length.
     * 
     */
    public static final int GeogInvFlatteningGeoKey = 2059;

    /**
     * GeogAzimuthUnitsGeoKey Key ID = 2060 Type = SHORT (code) Values = Section 6.3.1.4 Codes
     * 
     * This key may be used to specify the angular units of measurement used to defining azimuths, in geographic
     * coordinate systems. These may be used for defining azimuthal parameters for some projection algorithms, and may
     * not necessarily be the same angular units used for lat-long.
     * 
     */
    public static final int GeogAzimuthUnitsGeoKey = 2060;

    // Projected Coordinate System Parameter GeoKeys

    /**
     * ProjectedCSTypeGeoKey Key ID = 3072 Type = SHORT (codes) Values: Section 6.3.3.1 codes This code is provided to
     * specify the projected coordinate system.
     * 
     * GeoKey requirements for "user-defined" PCS families: PCSCitationGeoKey ProjectionGeoKey
     * 
     */
    public static final int ProjectedCSTypeGeoKey = 3072;

    /**
     * PCSCitationGeoKey Key ID = 3073 Type = ASCII
     * 
     * As with all the "Citation" GeoKeys, this is provided to give an ASCII reference to published documentation on the
     * Projected Coordinate System particularly if this is a "user-defined" PCS.
     * 
     */
    public static final int PCSCitationGeoKey = 3073;

    // Projection Definition GeoKeys
    //
    // With the exception of the first two keys, these are mostly
    // projection-specific parameters, and only a few will be required
    // for any particular projection type. Projected coordinate systems
    // automatically imply a specific projection type, as well as
    // specific parameters for that projection, and so the keys below
    // will only be necessary for user-defined projected coordinate
    // systems.

    /**
     * ProjectionGeoKey Key ID = 3074 Type = SHORT (code) Values: Section 6.3.3.2 codes
     * http://www.remotesensing.org/geotiff/spec/geotiff6.html#6.3.3.2
     * 
     * Allows specification of the coordinate transformation method and projection zone parameters. Note : when
     * associated with an appropriate Geographic Coordinate System, this forms a Projected Coordinate System.
     * 
     * GeoKeys Required for "user-defined" Projections: PCSCitationGeoKey ProjCoordTransGeoKey ProjLinearUnitsGeoKey
     * (additional parameters depending on ProjCoordTransGeoKey).
     * 
     */
    public static final int ProjectionGeoKey = 3074;

    /**
     * ProjCoordTransGeoKey Key ID = 3075 Type = SHORT (code) Values: Section 6.3.3.3 codes
     * http://www.remotesensing.org/geotiff/spec/geotiff6.html#6.3.3.3
     * 
     * Allows specification of the coordinate transformation method used. Note: this does not include the definition of
     * the corresponding Geographic Coordinate System to which the projected CS is related; only the transformation
     * method is defined here.
     * 
     * GeoKeys Required for "user-defined" Coordinate Transformations: PCSCitationGeoKey (additional parameter geokeys
     * depending on the Coord. Trans. specified).
     * 
     */
    public static final int ProjCoordTransGeoKey = 3075;

    /**
     * ProjLinearUnitsGeoKey Key ID = 3076 Type = SHORT (code) Values: Section 6.3.1.3 codes
     * 
     * Defines linear units used by this projection. http://www.remotesensing.org/geotiff/spec/geotiff6.html#6.3.1.3
     * 
     */
    public static final int ProjLinearUnitsGeoKey = 3076;

    /**
     * ProjLinearUnitSizeGeoKey Key ID = 3077 Type = DOUBLE Units: meters
     * 
     * Defines size of user-defined linear units in meters.
     * 
     */
    public static final int ProjLinearUnitSizeGeoKey = 3077;

    /**
     * ProjStdParallel1GeoKey Key ID = 3078 Type = DOUBLE Units: GeogAngularUnit Alias: ProjStdParallelGeoKey (from Rev
     * 0.2)
     * 
     * Latitude of primary Standard Parallel.
     * 
     */
    public static final int ProjStdParallel1GeoKey = 3078;

    /**
     * ProjStdParallel2GeoKey Key ID = 3079 Type = DOUBLE Units: GeogAngularUnit
     * 
     * Latitude of second Standard Parallel.
     * 
     */
    public static final int ProjStdParallel2GeoKey = 3079;

    /**
     * ProjNatOriginLongGeoKey Key ID = 3080 Type = DOUBLE Units: GeogAngularUnit Alias: ProjOriginLongGeoKey
     * 
     * Longitude of map-projection Natural origin.
     * 
     */
    public static final int ProjNatOriginLongGeoKey = 3080;

    /**
     * ProjNatOriginLatGeoKey Key ID = 3081 Type = DOUBLE Units: GeogAngularUnit Alias: ProjOriginLatGeoKey
     * 
     * Latitude of map-projection Natural origin.
     * 
     */
    public static final int ProjNatOriginLatGeoKey = 3081;

    /**
     * ProjFalseEastingGeoKey Key ID = 3082 Type = DOUBLE Units: ProjLinearUnit Gives the easting coordinate of the map
     * projection Natural origin.
     * 
     */
    public static final int ProjFalseEastingGeoKey = 3082;

    /**
     * ProjFalseNorthingGeoKey Key ID = 3083 Type = DOUBLE Units: ProjLinearUnit Gives the northing coordinate of the
     * map projection Natural origin.
     * 
     */
    public static final int ProjFalseNorthingGeoKey = 3083;

    /**
     * ProjFalseOriginLongGeoKey Key ID = 3084 Type = DOUBLE Units: GeogAngularUnit Gives the longitude of the False
     * origin.
     * 
     */
    public static final int ProjFalseOriginLongGeoKey = 3084;

    /**
     * ProjFalseOriginLatGeoKey Key ID = 3085 Type = DOUBLE Units: GeogAngularUnit Gives the latitude of the False
     * origin.
     * 
     */
    public static final int ProjFalseOriginLatGeoKey = 3085;

    /**
     * ProjFalseOriginEastingGeoKey Key ID = 3086 Type = DOUBLE Units: ProjLinearUnit Gives the easting coordinate of
     * the false origin. This is NOT the False Easting, which is the easting attached to the Natural origin.
     * 
     */
    public static final int ProjFalseOriginEastingGeoKey = 3086;

    /**
     * ProjFalseOriginNorthingGeoKey Key ID = 3087 Type = DOUBLE Units: ProjLinearUnit Gives the northing coordinate of
     * the False origin. This is NOT the False Northing, which is the northing attached to the Natural origin.
     * 
     */
    public static final int ProjFalseOriginNorthingGeoKey = 3087;

    /**
     * ProjCenterLongGeoKey Key ID = 3088 Type = DOUBLE Units: GeogAngularUnit
     * 
     * Longitude of Center of Projection. Note that this is not necessarily the origin of the projection.
     * 
     */
    public static final int ProjCenterLongGeoKey = 3088;

    /**
     * ProjCenterLatGeoKey Key ID = 3089 Type = DOUBLE Units: GeogAngularUnit
     * 
     * Latitude of Center of Projection. Note that this is not necessarily the origin of the projection.
     * 
     */
    public static final int ProjCenterLatGeoKey = 3089;

    /**
     * ProjCenterEastingGeoKey Key ID = 3090 Type = DOUBLE Units: ProjLinearUnit Gives the easting coordinate of the
     * center. This is NOT the False Easting.
     * 
     */
    public static final int ProjCenterEastingGeoKey = 3090;

    /**
     * ProjCenterNorthingGeoKey Key ID = 3091 Type = DOUBLE Units: ProjLinearUnit Gives the northing coordinate of the
     * center. This is NOT the False Northing.
     * 
     * NOTE this value is incorrectly named at http://www.remotesensing.org/geotiff/spec/geotiff2.7.html#2.7
     * 
     */
    public static final int ProjCenterNorthingGeoKey = 3091;

    /**
     * ProjScaleAtNatOriginGeoKey Key ID = 3092 Type = DOUBLE Units: none Alias: ProjScaleAtOriginGeoKey (Rev. 0.2)
     * 
     * Scale at Natural Origin. This is a ratio, so no units are required.
     * 
     */
    public static final int ProjScaleAtNatOriginGeoKey = 3092;

    /**
     * ProjScaleAtCenterGeoKey Key ID = 3093 Type = DOUBLE Units: none
     * 
     * Scale at Center. This is a ratio, so no units are required.
     * 
     */
    public static final int ProjScaleAtCenterGeoKey = 3093;

    /**
     * ProjAzimuthAngleGeoKey Key ID = 3094 Type = DOUBLE Units: GeogAzimuthUnit
     * 
     * Azimuth angle east of true north of the central line passing through the projection center (for elliptical
     * (Hotine) Oblique Mercator). Note that this is the standard method of measuring azimuth, but is opposite the usual
     * mathematical convention of positive indicating counter-clockwise.
     * 
     */
    public static final int ProjAzimuthAngleGeoKey = 3094;

    /**
     * ProjStraightVertPoleLongGeoKey Key ID = 3095 Type = DOUBLE Units: GeogAngularUnit
     * 
     * Longitude at Straight Vertical Pole. For polar stereographic.
     * 
     */
    public static final int ProjStraightVertPoleLongGeoKey = 3095;

    // Vertical CS Parameter Keys
    //
    // Note: Vertical coordinate systems are not yet implemented.
    // These sections are provided for future development, and any
    // vertical coordinate systems in the current revision must be
    // defined using the VerticalCitationGeoKey.

    /**
     * VerticalCSTypeGeoKey Key ID = 4096 Type = SHORT (code) Values = Section 6.3.4.1 Codes
     * http://www.remotesensing.org/geotiff/spec/geotiff6.html#6.3.4.1
     * 
     * This key may be used to specify the vertical coordinate system.
     * 
     */
    public static final int VerticalCSTypeGeoKey = 4096;

    /**
     * VerticalCitationGeoKey Key ID = 4097 Type = ASCII Values = text
     * 
     * This key may be used to document the vertical coordinate system used, and its parameters.
     * 
     */
    public static final int VerticalCitationGeoKey = 4097;

    /**
     * VerticalDatumGeoKey Key ID = 4098 Type = SHORT (code) Values = Section 6.3.4.2 codes
     * http://www.remotesensing.org/geotiff/spec/geotiff6.html#6.3.4.2
     * 
     * This key may be used to specify the vertical datum for the vertical coordinate system.
     * 
     */
    public static final int VerticalDatumGeoKey = 4098;

    /**
     * VerticalUnitsGeoKey Key ID = 4099 Type = SHORT (code) Values = Section 6.3.1.3 Codes
     * http://www.remotesensing.org/geotiff/spec/geotiff6.html#6.3.1.3
     * 
     * This key may be used to specify the vertical units of measurement used in the geographic coordinate system, in
     * cases where geographic CS's need to reference the vertical coordinate. This, together with the Citation key,
     * comprise the only fully implemented keys in this section, at present.
     * 
     */
    public static final int VerticalUnitsGeoKey = 4099;

    // GeoKey value codes
    // Many of the GeoKey values are reproduced here for ease of use.
    // For the rest, see the GeoTIFF specification
    // http://www.remotesensing.org/geotiff/spec/geotiff6.html

    /**
     * 6.3.1.1 Model Type Codes Ranges: 0 = undefined [ 1, 32766] = GeoTIFF Reserved Codes 32767 = user-defined [32768,
     * 65535] = Private User Implementations GeoTIFF defined CS Model Type Codes: ModelTypeProjected = 1 Projection
     * Coordinate System ModelTypeGeographic = 2 Geographic latitude-longitude System ModelTypeGeocentric = 3 Geocentric
     * (X,Y,Z) Coordinate System
     * 
     * Notes: 1. ModelTypeGeographic and ModelTypeProjected correspond to the FGDC metadata Geographic and
     * Planar-Projected coordinate system types.
     * 
     */
    public static final int ModelTypeProjected = 1;

    public static final int ModelTypeGeographic = 2;

    public static final int ModelTypeGeocentric = 3;

    /**
     * 6.3.1.2 Raster Type Codes Ranges: 0 = undefined [ 1, 1023] = Raster Type Codes (GeoTIFF Defined) [1024, 32766] =
     * Reserved 32767 = user-defined [32768, 65535]= Private User Implementations
     * 
     * Note: Use of "user-defined" or "undefined" raster codes is not recommended.
     * 
     */
    public static final int RasterPixelIsArea = 1;

    public static final int RasterPixelIsPoint = 2;

    /**
     * 6.3.1.3 Linear Units Codes There are several different kinds of units that may be used in geographically related
     * raster data: linear units, angular units, units of time (e.g. for radar-return), CCD-voltages, etc. For this
     * reason there will be a single, unique range for each kind of unit, broken down into the following currently
     * defined ranges:
     * 
     * Ranges: 0 = undefined [ 1, 2000] = Obsolete GeoTIFF codes [2001, 8999] = Reserved by GeoTIFF [9000, 9099] = EPSG
     * Linear Units. [9100, 9199] = EPSG Angular Units. 32767 = user-defined unit [32768, 65535]= Private User
     * Implementations
     */
    public static final int Linear_Meter = 9001;

    public static final int Linear_Foot = 9002;

    public static final int Linear_Foot_US_Survey = 9003;

    public static final int Linear_Foot_Modified_American = 9004;

    public static final int Linear_Foot_Clarke = 9005;

    public static final int Linear_Foot_Indian = 9006;

    public static final int Linear_Link = 9007;

    public static final int Linear_Link_Benoit = 9008;

    public static final int Linear_Link_Sears = 9009;

    public static final int Linear_Chain_Benoit = 9010;

    public static final int Linear_Chain_Sears = 9011;

    public static final int Linear_Yard_Sears = 9012;

    public static final int Linear_Yard_Indian = 9013;

    public static final int Linear_Fathom = 9014;

    public static final int Linear_Mile_International_Nautical = 9015;

    /**
     * 6.3.1.4 Angular Units Codes These codes shall be used for any key that requires specification of an angular unit
     * of measurement.
     * 
     */
    public static final int Angular_Radian = 9101;

    public static final int Angular_Degree = 9102;

    public static final int Angular_Arc_Minute = 9103;

    public static final int Angular_Arc_Second = 9104;

    public static final int Angular_Grad = 9105;

    public static final int Angular_Gon = 9106;

    public static final int Angular_DMS = 9107;

    public static final int Angular_DMS_Hemisphere = 9108;

    /**
     * 6.3.2.1 Geographic CS Type Codes Note: A Geographic coordinate system consists of both a datum and a Prime
     * Meridian. Some of the names are very similar, and differ only in the Prime Meridian, so be sure to use the
     * correct one. The codes beginning with GCSE_xxx are unspecified GCS which use ellipsoid (xxx); it is recommended
     * that only the codes beginning with GCS_ be used if possible.
     * 
     * Ranges:
     * 
     * 0 = undefined [ 1, 1000] = Obsolete EPSG/POSC Geographic Codes [ 1001, 3999] = Reserved by GeoTIFF [ 4000, 4199]
     * = EPSG GCS Based on Ellipsoid only [ 4200, 4999] = EPSG GCS Based on EPSG Datum [ 5000, 32766] = Reserved by
     * GeoTIFF 32767 = user-defined GCS [32768, 65535] = Private User Implementations Values: Note: Geodetic datum using
     * Greenwich PM have codes equal to the corresponding Datum code - 2000. Ellipsoid-Only GCS: Note: the numeric code
     * is equal to the code of the correspoding EPSG ellipsoid, minus 3000.
     * 
     * Note: Only a handful of values have been reproduced here, for the remainder see the GeoTIFF specification.
     */
    public static final int GCS_NAD27 = 4267;

    public static final int GCS_NAD83 = 4269;

    public static final int GCS_WGS_72 = 4322;

    public static final int GCS_WGS_72BE = 4324;

    public static final int GCS_WGS_84 = 4326;

    public static final int GCSE_WGS84 = 4030;

    /**
     * 6.3.3.1 Projected CS Type Codes Ranges: [ 1, 1000] = Obsolete EPSG/POSC Projection System Codes [20000, 32760] =
     * EPSG Projection System codes 32767 = user-defined [32768, 65535] = Private User Implementations Special Ranges:
     * 1. For PCS utilizing GeogCS with code in range 4201 through 4321: As far as is possible the PCS code will be of
     * the format gggzz where ggg is (geodetic datum code -4000) and zz is zone. 2. For PCS utilizing GeogCS with code
     * out of range 4201 through 4321 (i.e. geodetic datum code 6201 through 6319). PCS code 20xxx where xxx is a
     * sequential number. 3. Other: WGS72 / UTM northern hemisphere: 322zz where zz is UTM zone number WGS72 / UTM
     * southern hemisphere: 323zz where zz is UTM zone number WGS72BE / UTM northern hemisphere: 324zz where zz is UTM
     * zone number WGS72BE / UTM southern hemisphere: 325zz where zz is UTM zone number WGS84 / UTM northern hemisphere:
     * 326zz where zz is UTM zone number WGS84 / UTM southern hemisphere: 327zz where zz is UTM zone number US State
     * Plane (NAD27): 267xx/320xx US State Plane (NAD83): 269xx/321xx Note: These are only a subset of the possible
     * values
     * 
     */
    public static final int PCS_WGS72_UTM_zone_1N = 32201;

    public static final int PCS_WGS72_UTM_zone_60N = 32260;

    public static final int PCS_WGS72_UTM_zone_1S = 32301;

    public static final int PCS_WGS72_UTM_zone_60S = 32360;

    public static final int PCS_WGS72BE_UTM_zone_1N = 32401;

    public static final int PCS_WGS72BE_UTM_zone_60N = 32460;

    public static final int PCS_WGS72BE_UTM_zone_1S = 32501;

    public static final int PCS_WGS72BE_UTM_zone_60S = 32560;

    public static final int PCS_WGS84_UTM_zone_1N = 32601;

    public static final int PCS_WGS84_UTM_zone_60N = 32660;

    public static final int PCS_WGS84_UTM_zone_1S = 32701;

    public static final int PCS_WGS84_UTM_zone_60S = 32760;

    /**
     * An index into the geoKey directory for the directory version number
     */
    public static final int GEO_KEY_DIRECTORY_VERSION_INDEX = 0;

    /**
     * An index into the geoKey directory for the geoKey revision number
     */
    public static final int GEO_KEY_REVISION_INDEX = 1;

    /**
     * An index into the geoKey directory for the geoKey minor revision number
     */
    public static final int GEO_KEY_MINOR_REVISION_INDEX = 2;

    /**
     * An index into the geoKey directory for the number of geoKeys
     */
    public static final int GEO_KEY_NUM_KEYS_INDEX = 3;

    /**
     * The DOM element ID (tag) for a TIFF Image File Directory
     */
    public static final String TIFF_IFD_TAG = "TIFFIFD";

    /**
     * The DOM element ID (tag) for a TIFF Field
     */
    public static final String TIFF_FIELD_TAG = "TIFFField";

    /**
     * The DOM element ID (tag) for a set of TIFF Double values
     */
    public static final String TIFF_DOUBLES_TAG = "TIFFDoubles";

    /**
     * The DOM element ID (tag) for a single TIFF double. The value is stored in an attribute named "value"
     */
    public static final String TIFF_DOUBLE_TAG = "TIFFDouble";

    /**
     * The DOM element ID (tag) for a set of TIFF Short values
     */
    public static final String TIFF_SHORTS_TAG = "TIFFShorts";

    /**
     * The DOM element ID (tag) for a single TIFF Short value. The value is stored in an attribute named "value"
     */
    public static final String TIFF_SHORT_TAG = "TIFFShort";

    /**
     * The DOM element ID (tag) for a set of TIFF Rational values
     */
    public static final String TIFF_RATIONALS_TAG = "TIFFRationals";

    /**
     * The DOM element ID (tag) for a single TIFF Rational value. The value is stored in an attribute named "value"
     */
    public static final String TIFF_RATIONAL_TAG = "TIFFRational";

    /**
     * The DOM element ID (tag) for a set of TIFF Ascii values
     */
    public static final String TIFF_ASCIIS_TAG = "TIFFAsciis";

    /**
     * The DOM element ID (tag) for a single TIFF Ascii value
     */
    public static final String TIFF_ASCII_TAG = "TIFFAscii";

    /**
     * The DOM attribute name for a TIFF Field Tag (number)
     */
    public static final String NUMBER_ATTR = "number";

    /**
     * The DOM attribute name for a TIFF Entry value (whether Short, Double, or Ascii)
     */
    public static final String VALUE_ATTR = "value";

    /**
     * This class is a holder for a GeoKey record containing four short values. The values are a GeoKey ID, the TIFFTag
     * number of the location of this data, the count of values for this GeoKey, and the offset (or value if the
     * location is 0).
     * <p>
     * If the Tiff Tag location is 0, then the value is a Short and is contained in the offset. Otherwise, there is one
     * or more value in the specified external Tiff tag. The number is specified by the count field, and the offset into
     * the record is the offset field.
     */
    public static class GeoKeyRecord {
        private int myKeyID;

        private int myTiffTagLocation;

        private int myCount;

        private int myValueOffset;

        public GeoKeyRecord( int keyID, int tagLoc, int count, int offset ) {
            myKeyID = keyID;
            myTiffTagLocation = tagLoc;
            myCount = count;
            myValueOffset = offset;
        }

        public int getKeyID() {
            return myKeyID;
        }

        public int getTiffTagLocation() {
            return myTiffTagLocation;
        }

        public int getCount() {
            return myCount;
        }

        public int getValueOffset() {
            return myValueOffset;
        }
    } // end of class GeoKeyRecord

    /**
     * The root of the metadata DOM tree
     */
    private IIOMetadataNode myRootNode;

    /**
     * The constructor builds a metadata adapter for the image metadata root IIOMetadataNode.
     * 
     * @param imageNode
     *            The image metadata
     */
    public GeoTiffIIOMetadataAdapter( IIOMetadata imageMetadata ) {
        String formatName = imageMetadata.getNativeMetadataFormatName();
        myRootNode = (IIOMetadataNode) imageMetadata.getAsTree( formatName );
    }

    /**
     * Gets the version of the GeoKey directory. This is typically a value of 1 and can be used to check that the data
     * is of a valid format.
     */
    public int getGeoKeyDirectoryVersion() {
        // First get the geokeys directory
        IIOMetadataNode geoKeyDir = getTiffField( GeoTIFFTagSet.TAG_GEO_KEY_DIRECTORY );
        if ( geoKeyDir == null ) {
            throw new UnsupportedOperationException( "GeoKey directory does not exist" );
        }

        // now get the value from the correct TIFFShort location
        int result = getTiffShort( geoKeyDir, GEO_KEY_DIRECTORY_VERSION_INDEX );
        return result;
    }

    /**
     * Gets the revision number of the GeoKeys in this metadata.
     */
    public int getGeoKeyRevision() {
        // First get the geokeys directory
        IIOMetadataNode geoKeyDir = getTiffField( GeoTIFFTagSet.TAG_GEO_KEY_DIRECTORY );
        if ( geoKeyDir == null ) {
            throw new UnsupportedOperationException( "GeoKey directory does not exist" );
        }

        // Get the value from the correct TIFFShort
        int result = getTiffShort( geoKeyDir, GEO_KEY_REVISION_INDEX );
        return result;
    }

    /**
     * Gets the minor revision number of the GeoKeys in this metadata.
     */
    public int getGeoKeyMinorRevision() {
        // First get the geokeys directory
        IIOMetadataNode geoKeyDir = getTiffField( GeoTIFFTagSet.TAG_GEO_KEY_DIRECTORY );
        if ( geoKeyDir == null ) {
            throw new UnsupportedOperationException( "GeoKey directory does not exist" );
        }

        // Get the value from the correct TIFFShort
        int result = getTiffShort( geoKeyDir, GEO_KEY_MINOR_REVISION_INDEX );
        return result;
    }

    /**
     * Gets the number of GeoKeys in the geokeys directory.
     */
    public int getNumGeoKeys() {
        // First get the geokeys directory
        IIOMetadataNode geoKeyDir = getTiffField( GeoTIFFTagSet.TAG_GEO_KEY_DIRECTORY );
        if ( geoKeyDir == null ) {
            throw new UnsupportedOperationException( "GeoKey directory does not exist" );
        }

        // Get the value from the correct TIFFShort
        int result = getTiffShort( geoKeyDir, GEO_KEY_NUM_KEYS_INDEX );
        return result;
    }

    /**
     * Gets a GeoKey value as a String. This implementation should be &quotquiet&quot in the sense that it should not
     * throw any exceptions but only return null in the event that the data organization is not as expected.
     * 
     * @param keyID
     *            The numeric ID of the GeoKey
     * @return A string representing the value, or null if the key was not found.
     */
    public String getGeoKey( int keyID ) {
        String result = null;

        GeoKeyRecord rec = getGeoKeyRecord( keyID );

        if ( rec != null ) {
            if ( rec.getTiffTagLocation() == 0 ) {
                // value is stored directly in the GeoKey record
                result = String.valueOf( rec.getValueOffset() );
            } else {
                // value is stored externally
                // get the TIFF field where the data is actually stored
                IIOMetadataNode field = getTiffField( rec.getTiffTagLocation() );
                if ( field != null ) {
                    Node sequence = field.getFirstChild();
                    if ( sequence != null ) {
                        if ( sequence.getNodeName().equals( TIFF_ASCIIS_TAG ) ) {
                            // TIFFAscii values are handled specially
                            result = getTiffAscii( (IIOMetadataNode) sequence, rec.getValueOffset(), rec.getCount() );
                        } else {
                            // value is numeric
                            NodeList valueNodes = sequence.getChildNodes();
                            Node node = valueNodes.item( rec.getValueOffset() );
                            result = getValueAttribute( node );
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Gets a record containing the four TIFFShort values for a geokey entry. For more information see the GeoTIFF
     * specification.
     * 
     * @return the record with the given keyID, or null if none is found
     */
    public GeoKeyRecord getGeoKeyRecord( int keyID ) {
        // First get the geokey directory in which to search for this geokey
        IIOMetadataNode geoKeyDir = getTiffField( GeoTIFFTagSet.TAG_GEO_KEY_DIRECTORY );

        if ( geoKeyDir == null ) {
            throw new UnsupportedOperationException( "GeoKey directory does not exist" );
        }

        GeoKeyRecord result = null;

        // GeoKey IDs are at every 4th position starting at index 0
        IIOMetadataNode tiffShortsNode = (IIOMetadataNode) geoKeyDir.getFirstChild();
        NodeList keys = tiffShortsNode.getElementsByTagName( TIFF_SHORT_TAG );

        // embed the exit condition in the for loop
        for ( int i = 4; i < keys.getLength() && result == null; i += 4 ) {
            Node n = keys.item( i );
            int thisKeyID = getIntValueAttribute( n );
            if ( thisKeyID == keyID ) {
                // we've found the right GeoKey, now build it
                Node locNode = keys.item( i + 1 );
                Node countNode = keys.item( i + 2 );
                Node offsetNode = keys.item( i + 3 );

                int loc = getIntValueAttribute( locNode );
                int count = getIntValueAttribute( countNode );
                int offset = getIntValueAttribute( offsetNode );
                result = new GeoKeyRecord( thisKeyID, loc, count, offset );
            }
        }
        return result;
    }

    /**
     * Gets the model pixel scales from the correct TIFFField
     */
    public double[] getModelPixelScales() {
        IIOMetadataNode modelTiePointNode = getTiffField( GeoTIFFTagSet.TAG_MODEL_PIXEL_SCALE );
        double[] result = getTiffDoubles( modelTiePointNode );
        return result;
    }

    /**
     * Gets the model tie points from the appropriate TIFFField
     * 
     * @return the tie points, or null if not found
     */
    public double[] getModelTiePoints() {
        double[] result = null;
        IIOMetadataNode modelTiePointNode = getTiffField( GeoTIFFTagSet.TAG_MODEL_TIE_POINT );
        if ( modelTiePointNode != null ) {
            result = getTiffDoubles( modelTiePointNode );
        }
        return result;
    }

    /**
     * Gets the model tie points from the appropriate TIFFField
     * 
     * @return the tie points, or null if not found
     */
    public double[] getModelTransformation() {
        double[] result = null;
        IIOMetadataNode modelTransNode = getTiffField( GeoTIFFTagSet.TAG_MODEL_TRANSFORMATION );
        if ( modelTransNode != null ) {
            result = getTiffDoubles( modelTransNode );
        }

        return result;
    }

    /**
     * Gets the value attribute of the given Node.
     * 
     * @param node
     *            A Node containing a value attribute, for example the node &ltTIFFShort value=&quot123&quot&gt
     * @return A String containing the text from the value attribute. In the above example, the string would be 123
     */
    protected String getValueAttribute( Node node ) {
        return node.getAttributes().getNamedItem( VALUE_ATTR ).getNodeValue();
    }

    /**
     * Gets the value attribute's contents and parses it as an int
     */
    protected int getIntValueAttribute( Node node ) {
        return Integer.parseInt( getValueAttribute( node ) );
    }

    /**
     * RÃ©cupÃ¨re la valeur d'un attribut et la dÃ©code sous la forme d'un rationel x/y
     * 
     * @param node
     *            noeud traitÃ©
     * @return valeur
     */
    protected double getRationalValueAttribute( Node node ) {
        double dv;
        String str = getValueAttribute( node );
        StringTokenizer stk = new StringTokenizer( str, "/" );
        if ( stk != null && stk.hasMoreTokens() ) {
            String st = stk.nextToken();
            dv = Double.parseDouble( st );
            if ( stk.hasMoreTokens() ) {
                st = stk.nextToken();
                double dv2 = Double.parseDouble( st );
                dv /= dv2;
            }
        } else {
            dv = Double.parseDouble( str );
        }
        return dv;
    }

    /**
     * Gets a TIFFField node with the given tag number. This is done by searching for a TIFFField with attribute number
     * whose value is the specified tag value.
     */
    protected IIOMetadataNode getTiffField( int tag ) {
        IIOMetadataNode result = null;
        IIOMetadataNode tiffDirectory = getTiffDirectory();
        NodeList children = tiffDirectory.getElementsByTagName( TIFF_FIELD_TAG );

        // embed the exit condition in the for loop
        for ( int i = 0; i < children.getLength() && result == null; i++ ) {
            // search through all the TIFF fields to find the one with the
            // given tag value
            Node child = children.item( i );
            Node number = child.getAttributes().getNamedItem( NUMBER_ATTR );
            if ( number != null ) {
                int num = Integer.parseInt( number.getNodeValue() );
                if ( num == tag ) {
                    result = (IIOMetadataNode) child;
                }
            }
        }
        return result;
    }

    /**
     * Gets the TIFFIFD (image file directory) node.
     */
    private IIOMetadataNode getTiffDirectory() {
        // there should only be one, and it should be the only node
        // in the metadata, so just get it.
        return (IIOMetadataNode) myRootNode.getFirstChild();
    }

    /**
     * Gets an array of int values stored in a TIFFShorts element that contains a sequence of TIFFShort values.
     * 
     * @param tiffField
     *            An IIOMetadataNode pointing to a TIFFField element that contains a TIFFShorts element.
     */
    protected int[] getTiffShorts( IIOMetadataNode tiffField ) {
        IIOMetadataNode shortsElement = (IIOMetadataNode) tiffField.getFirstChild();
        NodeList shorts = shortsElement.getElementsByTagName( TIFF_SHORT_TAG );
        int[] result = new int[shorts.getLength()];
        for ( int i = 0; i < shorts.getLength(); i++ ) {
            Node node = shorts.item( i );
            result[i] = getIntValueAttribute( node );
        }
        return result;
    }

    /**
     * Gets a single TIFFShort value at the given index.
     * 
     * @param tiffField
     *            An IIOMetadataNode pointing to a TIFFField element that contains a TIFFShorts element.
     * @param index
     *            The 0-based index of the desired short value
     */
    protected int getTiffShort( IIOMetadataNode tiffField, int index ) {
        IIOMetadataNode shortsElement = (IIOMetadataNode) tiffField.getFirstChild();
        NodeList shorts = shortsElement.getElementsByTagName( TIFF_SHORT_TAG );
        Node node = shorts.item( index );
        int result = getIntValueAttribute( node );
        return result;
    }

    /**
     * Gets an array of double values from a TIFFDoubles TIFFField.
     * 
     * @param tiffField
     *            An IIOMetadataNode pointing to a TIFFField element that contains a TIFFDoubles element.
     */
    protected double[] getTiffDoubles( IIOMetadataNode tiffField ) {
        if ( tiffField == null ) {
            return new double[] {};
        }
        IIOMetadataNode doublesElement = (IIOMetadataNode) tiffField.getFirstChild();
        NodeList doubles = doublesElement.getElementsByTagName( TIFF_DOUBLE_TAG );
        double[] result = new double[doubles.getLength()];
        for ( int i = 0; i < doubles.getLength(); i++ ) {
            Node node = doubles.item( i );
            result[i] = Double.parseDouble( getValueAttribute( node ) );
        }
        return result;
    }

    /**
     * Gets a single double value at the specified index from a sequence of TIFFDoubles
     * 
     * @param tiffField
     *            An IIOMetadataNode pointing to a TIFFField element that contains a TIFFDoubles element.
     */
    protected double getTiffDouble( IIOMetadataNode tiffField, int index ) {
        IIOMetadataNode doublesElement = (IIOMetadataNode) tiffField.getFirstChild();
        NodeList doubles = doublesElement.getElementsByTagName( TIFF_DOUBLE_TAG );
        Node node = doubles.item( index );
        double result = Double.parseDouble( getValueAttribute( node ) );
        return result;
    }

    /**
     * Gets an array of int values stored in a TIFFRationals element that contains a sequence of TIFFRational values.
     * 
     * @param tiffField
     *            An IIOMetadataNode pointing to a TIFFField element that contains a TIFFRationals element.
     */
    protected double[] getTiffRationals( IIOMetadataNode tiffField ) {
        IIOMetadataNode numsElement = (IIOMetadataNode) tiffField.getFirstChild();
        NodeList nums = numsElement.getElementsByTagName( TIFF_RATIONAL_TAG );
        double[] result = new double[nums.getLength()];
        for ( int i = 0; i < nums.getLength(); i++ ) {
            Node node = nums.item( i );
            result[i] = getRationalValueAttribute( node );
        }
        return result;
    }

    /**
     * Gets a single TIFFRational value at the given index.
     * 
     * @param tiffField
     *            An IIOMetadataNode pointing to a TIFFField element that contains a TIFFRational element.
     * @param index
     *            The 0-based index of the desired short value
     */
    protected double getTiffRational( IIOMetadataNode tiffField, int index ) {
        IIOMetadataNode numsElement = (IIOMetadataNode) tiffField.getFirstChild();
        NodeList nums = numsElement.getElementsByTagName( TIFF_RATIONAL_TAG );
        Node node = nums.item( index );
        double result = getRationalValueAttribute( node );
        return result;
    }

    /**
     * Gets a portion of a TIFFAscii string with the specified start character and length;
     * 
     * @param tiffField
     *            An IIOMetadataNode pointing to a TIFFField element that contains a TIFFAsciis element. This element
     *            should contain a single TiffAscii element.
     * @return A substring of the value contained in the TIFFAscii node, with the final '|' character removed.
     */
    protected String getTiffAscii( IIOMetadataNode tiffField, int start, int length ) {
        IIOMetadataNode asciisElement = (IIOMetadataNode) tiffField.getFirstChild();
        NodeList asciis = asciisElement.getElementsByTagName( TIFF_ASCII_TAG );
        // there should be only one, so get the first
        Node node = asciis.item( 0 );
        // GeoTIFF specification places a vertical bar '|' in place of \0
        // null delimiters so drop off the vertical bar for Java Strings
        String result = getValueAttribute( node ).substring( start, start + length - 1 );
        return result;
    }

} // end of class GeoTiffIIOMetadataAdapter
