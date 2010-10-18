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
package org.deegree.cs.transformations;

import static org.deegree.cs.utilities.MappingUtils.matchEPSGString;

import org.deegree.cs.CRSCodeType;
import org.deegree.cs.transformations.coordinate.GeocentricTransform;
import org.deegree.cs.transformations.helmert.Helmert;
import org.deegree.cs.transformations.polynomial.PolynomialTransformation;

/**
 * The <code>SupportedTransformations</code> enumeration defines currently supported transformations
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public enum SupportedTransformations {
    /**
     * The {@link Helmert}, transformation with 7 values
     */
    HELMERT_7,
    /**
     * The {@link Helmert}, transformation with 3 values
     */
    HELMERT_3,
    /**
     * The {@link GeocentricTransform} going from geographic to geocentric.
     */
    GEOGRAPHIC_GEOCENTRIC,
    /**
     * The primemeridian rotation going from any to greenwich
     */
    LONGITUDE_ROTATION,
    /**
     * The {@link PolynomialTransformation} defining the general 2, 3, ... degree polynomial transformation
     */
    GENERAL_POLYNOMIAL,
    /**
     * The ntv2, currently not supported
     */
    NTV2,
    /**
     * A not supported projection
     */
    NOT_SUPPORTED;

    /**
     * 
     * @param codes
     *            to check for.
     * @return a mapped transformation or {@link SupportedTransformations#NOT_SUPPORTED}, never <code>null</code>
     */
    public static SupportedTransformations fromCodes( CRSCodeType[] codes ) {
        if ( codes == null || codes.length == 0 ) {
            return NOT_SUPPORTED;
        }
        for ( CRSCodeType code : codes ) {
            if ( code != null ) {
                String compare = code.getOriginal();
                if ( "Longitude rotation".equalsIgnoreCase( compare ) || matchEPSGString( compare, "method", "9601" ) ) {
                    return LONGITUDE_ROTATION;
                } else if ( "Geographic/geocentric conversions".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "method", "9602" ) ) {
                    return GEOGRAPHIC_GEOCENTRIC;
                } else if ( "Geocentric translations".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "method", "9603" ) ) {
                    return HELMERT_3;
                } else if ( "Position Vector 7-param. transformation".equalsIgnoreCase( compare )
                            || "Coordinate Frame rotation".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "method", "9606" )
                            || matchEPSGString( compare, "method", "9607" ) ) {
                    return HELMERT_7;
                } else if ( "NTv2".equalsIgnoreCase( compare ) || matchEPSGString( compare, "method", "9615" ) ) {
                    return NTV2;
                } else if ( matchEPSGString( compare, "method", "9645" ) || matchEPSGString( compare, "method", "9646" )
                            || matchEPSGString( compare, "method", "9647" )
                            || matchEPSGString( compare, "method", "9648" ) ) {
                    return GENERAL_POLYNOMIAL;
                }
            }
        }
        return NOT_SUPPORTED;
    }
}