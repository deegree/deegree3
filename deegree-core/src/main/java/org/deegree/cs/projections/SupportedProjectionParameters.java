//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.cs.projections;

import static org.deegree.cs.utilities.MappingUtils.matchEPSGString;

import org.deegree.cs.CRSCodeType;

/**
 * The <code>SupportedProjectionParameters</code> enumeration defines currently supported projection parameters
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public enum SupportedProjectionParameters {
    /**
     * The latitude of natural origin of a given projection, aka. projectionLatitude, central-latitude or
     * latitude-of-origin, in Snyder referenced as phi_1 for azimuthal, phi_0 for other projections.
     */
    LATITUDE_OF_NATURAL_ORIGIN,
    /**
     * The longitude of natural origin of a given projection, aka. projectionLongitude, projection-meridian,
     * central-meridian, in Snyder referenced as lambda_0
     */
    LONGITUDE_OF_NATURAL_ORIGIN,
    /**
     * The false easting of the projection.
     */
    FALSE_EASTING,
    /**
     * The false northing of the projection.
     */
    FALSE_NORTHING,
    /**
     * The scale at the natural origin of the projection.
     */
    SCALE_AT_NATURAL_ORIGIN,
    /**
     * The latitude which the scale is 1 of a stereographic azimuthal projection.
     */
    TRUE_SCALE_LATITUDE,
    /**
     * The first parallel latitude of conic projections.
     */
    FIRST_PARALLEL_LATITUDE,
    /**
     * The second parallel latitude of conic projections.
     */
    SECOND_PARALLEL_LATITUDE,

    /**
     * A not supported projection parameter.
     */
    NOT_SUPPORTED;

    /**
     * 
     * @param codes
     *            to check for.
     * @return a mapped projections parameters or {@link SupportedProjectionParameters#NOT_SUPPORTED}, never
     *         <code>null</code>
     */
    public static SupportedProjectionParameters fromCodes( CRSCodeType[] codes ) {
        if ( codes == null || codes.length == 0 ) {
            return SupportedProjectionParameters.NOT_SUPPORTED;
        }
        for ( CRSCodeType name : codes ) {
            if ( name != null ) {
                String compare = name.getOriginal().toLowerCase();
                if ( ( compare.contains( "latitude" ) && ( compare.contains( "origin" ) || compare.contains( "central" ) ) )
                     || matchEPSGString( compare, "parameter", "8801" )
                     || matchEPSGString( compare, "parameter", "8811" )
                     || matchEPSGString( compare, "parameter", "8821" ) ) {
                    return SupportedProjectionParameters.LATITUDE_OF_NATURAL_ORIGIN;
                } else if ( ( compare.contains( "longitude" ) && ( compare.contains( "origin" ) || compare.contains( "central" ) ) )
                            || "Central Meridian".equalsIgnoreCase( compare )
                            || "centralmeridian".equalsIgnoreCase( compare )
                            || "CM".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8802" )
                            || matchEPSGString( compare, "parameter", "8812" )
                            || matchEPSGString( compare, "parameter", "8822" ) ) {
                    return SupportedProjectionParameters.LONGITUDE_OF_NATURAL_ORIGIN;
                } else if ( "Scale factor at natural origin".equalsIgnoreCase( compare )
                            || ( compare.contains( "scale" ) && compare.contains( "factor" ) )
                            || ( compare.contains( "scale" ) && compare.contains( "natural" ) && compare.contains( "origin" ) )
                            || matchEPSGString( compare, "parameter", "8805" ) ) {
                    return SupportedProjectionParameters.SCALE_AT_NATURAL_ORIGIN;
                } else if ( "Latitude of pseudo standard parallel ".equalsIgnoreCase( compare )
                            || "Latitude of standard parallel ".equalsIgnoreCase( compare )
                            || "trueScaleLatitude".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8832" )
                            || matchEPSGString( compare, "parameter", "8818" ) ) {
                    return SupportedProjectionParameters.TRUE_SCALE_LATITUDE;
                } else if ( "False easting".equalsIgnoreCase( compare ) || "falseEasting".equalsIgnoreCase( compare )
                            || "false westing".equalsIgnoreCase( compare )
                            || "Easting at false origin".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8806" )
                            || matchEPSGString( compare, "parameter", "8816" )
                            || matchEPSGString( compare, "parameter", "8826" ) ) {
                    return SupportedProjectionParameters.FALSE_EASTING;
                } else if ( "False northing".equalsIgnoreCase( compare ) || "falseNorthing".equalsIgnoreCase( compare )
                            || "false southing".equalsIgnoreCase( compare )
                            || "Northing at false origin".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8807" )
                            || matchEPSGString( compare, "parameter", "8827" )
                            || matchEPSGString( compare, "parameter", "8817" ) ) {
                    return SupportedProjectionParameters.FALSE_NORTHING;
                } else if ( "Latitude of 1st standard parallel".equalsIgnoreCase( compare )
                            || "firstParallelLatitude".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8823" ) ) {
                    return SupportedProjectionParameters.FIRST_PARALLEL_LATITUDE;
                } else if ( "Latitude of 2nd standard parallel".equalsIgnoreCase( compare )
                            || "secondParallelLatitude".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8824" ) ) {
                    return SupportedProjectionParameters.SECOND_PARALLEL_LATITUDE;
                }

            }
        }
        return SupportedProjectionParameters.NOT_SUPPORTED;
    }
}