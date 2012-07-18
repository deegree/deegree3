//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.cs;

import static org.deegree.commons.utils.MapUtils.calcDegreeResFromScale;
import static org.deegree.commons.utils.MapUtils.calcMetricResFromScale;
import static org.deegree.cs.components.Unit.DEGREE;
import static org.deegree.cs.components.Unit.METRE;

import org.deegree.cs.components.IUnit;
import org.deegree.cs.coordinatesystems.CRS;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.coordinatesystems.ICRS;

/**
 * TODO: move this!
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class CRSUtils {

    /** The commonly used geographic 'EPSG:4326', with axis order X, Y. */
    public static final CRS EPSG_4326 = GeographicCRS.WGS84;

    /**
     * Calculates the resolution (world units / pixel) for the given scale denominator (1 / map scale) and coordinate
     * reference system (determines the world units).
     * 
     * @param scaleDenominator
     *            scale denominator (1 / map scale)
     * @param crs
     *            coordinate reference system, must not be <code>null</code>
     * @return resolution in world units per pixel
     */
    public static double calcResolution( double scaleDenominator, ICRS crs ) {
        IUnit units = crs.getAxis()[0].getUnits();
        return calcResolution( scaleDenominator, units );
    }

    /**
     * Calculates the resolution (world units / pixel) for the given scale denominator (1 / map scale) and unit system.
     * 
     * @param scaleDenominator
     *            scale denominator (1 / map scale)
     * @param units
     *            units, must not be <code>null</code>
     * @return resolution in world units per pixel
     */
    public static double calcResolution( double scaleDenominator, IUnit units ) {
        if ( units.equals( METRE ) ) {
            return calcMetricResFromScale( scaleDenominator );
        } else if ( units.equals( DEGREE ) ) {
            return calcDegreeResFromScale( scaleDenominator );
        }
        String msg = "Unhandled unit type: " + units
                     + ". Conversion from scale denominator to resolution not implemented";
        throw new IllegalArgumentException( msg );
    }
}
