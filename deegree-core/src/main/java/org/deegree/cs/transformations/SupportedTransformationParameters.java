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

/**
 * The <code>SupportedTransformationParameters</code> enumeration defines currently supported transformation parameters
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public enum SupportedTransformationParameters {
    /**
     * The X TRANSLATION of a (3/7) helmert transformation.
     */
    X_AXIS_TRANSLATION,
    /**
     * The Y TRANSLATION of a (3/7) helmert transformation.
     */
    Y_AXIS_TRANSLATION,
    /**
     * The Z TRANSLATION of a (3/7) helmert transformation.
     */
    Z_AXIS_TRANSLATION,
    /**
     * The X Rotation of a (3/7) helmert transformation.
     */
    X_AXIS_ROTATION,
    /**
     * The Y Rotation of a (3/7) helmert transformation.
     */
    Y_AXIS_ROTATION,
    /**
     * The Z Rotation of a (3/7) helmert transformation.
     */
    Z_AXIS_ROTATION,
    /**
     * The Difference of scale of a (3/7) helmert transformation.
     */
    SCALE_DIFFERENCE,
    /**
     * The longitude offset of a longitude rotation
     */
    LONGITUDE_OFFSET,
    /**
     * GENERIC transformation parameters are not yet supported.
     */
    GENERIC_POLYNOMIAL_PARAM,
    /**
     * A not supported projection parameter.
     */
    NOT_SUPPORTED;

    /**
     * 
     * @param codes
     *            to check for.
     * @return a mapped transformation or {@link SupportedTransformations#NOT_SUPPORTED}, never <code>null</code>
     */
    public static SupportedTransformationParameters fromCodes( CRSCodeType[] codes ) {
        if ( codes == null || codes.length == 0 ) {
            return NOT_SUPPORTED;
        }
        for ( CRSCodeType code : codes ) {
            if ( code != null ) {
                String compare = code.getOriginal();
                if ( "Longitude offset".equalsIgnoreCase( compare ) || matchEPSGString( compare, "parameter", "8602" ) ) {
                    return LONGITUDE_OFFSET;
                } else if ( "X-axis translation".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8605" ) ) {
                    return X_AXIS_TRANSLATION;
                } else if ( "Y-axis translation".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8606" ) ) {
                    return Y_AXIS_TRANSLATION;
                } else if ( "Z-axis translation".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8607" ) ) {
                    return Z_AXIS_TRANSLATION;
                } else if ( "X-axis rotation".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8608" ) ) {
                    return X_AXIS_ROTATION;
                } else if ( "Y-axis rotation".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8609" ) ) {
                    return Y_AXIS_ROTATION;
                } else if ( "Z-axis rotation".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8610" ) ) {
                    return Z_AXIS_ROTATION;
                } else if ( "Scale difference".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8611" ) ) {
                    return SCALE_DIFFERENCE;
                }
            }
        }
        return NOT_SUPPORTED;
    }
}
