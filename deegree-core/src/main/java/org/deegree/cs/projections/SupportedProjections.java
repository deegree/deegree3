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
import org.deegree.cs.projections.azimuthal.LambertAzimuthalEqualArea;
import org.deegree.cs.projections.azimuthal.StereographicAlternative;
import org.deegree.cs.projections.azimuthal.StereographicAzimuthal;
import org.deegree.cs.projections.conic.LambertConformalConic;
import org.deegree.cs.projections.cylindric.TransverseMercator;

/**
 * The <code>SupportedProjections</code> enumeration defines currently supported projections
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public enum SupportedProjections {
    /**
     * The {@link TransverseMercator} projection
     */
    TRANSVERSE_MERCATOR,
    /**
     * The {@link LambertConformalConic} projection
     */
    LAMBERT_CONFORMAL,
    /**
     * The {@link LambertAzimuthalEqualArea} projection
     */
    LAMBERT_AZIMUTHAL_EQUAL_AREA,
    /**
     * Snyders {@link StereographicAzimuthal} implementation of the stereographic azimuthal projection
     */
    STEREOGRAPHIC_AZIMUTHAL,
    /**
     * EPSG {@link StereographicAlternative} implementation of the Stereographic azimuthal projection
     */
    STEREOGRAPHIC_AZIMUTHAL_ALTERNATIVE,
    /**
     * A not supported projection
     */
    NOT_SUPPORTED;

    /**
     * 
     * @param codes
     *            to check for.
     * @return a mapped projection or {@link SupportedProjections#NOT_SUPPORTED}, never <code>null</code>
     */
    public static SupportedProjections fromCodes( CRSCodeType[] codes ) {
        if ( codes == null || codes.length == 0 ) {
            return SupportedProjections.NOT_SUPPORTED;
        }
        for ( CRSCodeType code : codes ) {
            if ( code != null ) {
                String compare = code.getOriginal();
                if ( "TransverseMercator".equalsIgnoreCase( compare )
                     || "Transverse Merctator".equalsIgnoreCase( compare )
                     || matchEPSGString( compare, "method", "9807" ) ) {
                    return SupportedProjections.TRANSVERSE_MERCATOR;
                } else if ( "lambertAzimuthalEqualArea".equalsIgnoreCase( compare )
                            || "Lambert Azimuthal Equal Area".equalsIgnoreCase( compare )
                            || "Lambert Azimuthal Equal Area (Spherical)".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "method", "9820" )
                            || matchEPSGString( compare, "method", "9821" ) ) {
                    return SupportedProjections.LAMBERT_AZIMUTHAL_EQUAL_AREA;
                } else if ( "stereographicAlternative".equalsIgnoreCase( compare )
                            || "Oblique Stereographic".equalsIgnoreCase( compare )
                            || compare.contains( "Polar Stereographic" ) || matchEPSGString( compare, "method", "9809" )
                            || matchEPSGString( compare, "method", "9810" )
                            || matchEPSGString( compare, "method", "9829" )
                            || matchEPSGString( compare, "method", "9830" ) ) {
                    return SupportedProjections.STEREOGRAPHIC_AZIMUTHAL_ALTERNATIVE;
                } else if ( "stereographicAzimuthal".equalsIgnoreCase( compare ) ) {
                    return SupportedProjections.STEREOGRAPHIC_AZIMUTHAL;
                } else if ( "lambertConformalConic".equalsIgnoreCase( compare )
                            || compare.contains( "Lambert Conic Conformal" )
                            || matchEPSGString( compare, "method", "9801" )
                            || matchEPSGString( compare, "method", "9802" )
                            || matchEPSGString( compare, "method", "9803" ) ) {
                    return SupportedProjections.LAMBERT_CONFORMAL;
                }
            }
        }
        return SupportedProjections.NOT_SUPPORTED;
    }
}
