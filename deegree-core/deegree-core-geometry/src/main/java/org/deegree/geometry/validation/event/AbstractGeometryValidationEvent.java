//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.geometry.validation.event;

import java.util.List;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.components.Axis;

/**
 * Abstract base class for {@link GeometryValidationEvent} implementations.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
abstract class AbstractGeometryValidationEvent implements GeometryValidationEvent {

    private final List<Object> geometryParticleHierachy;

    /**
     * Creates a new {@link AbstractGeometryValidationEvent} instance.
     * 
     * @param geometryParticleHierarchy
     *            list of affected geometry particles (that the geometry particle belongs to), must not be
     *            <code>null</code>
     */
    protected AbstractGeometryValidationEvent( List<Object> geometryParticleHierarchy ) {
        this.geometryParticleHierachy = geometryParticleHierarchy;
    }

    @Override
    public List<Object> getGeometryParticleHierarchy() {
        return geometryParticleHierachy;
    }

    /**
     * Returns true if the geometry has a left handed CRS.
     * 
     * @return <code>true</code> if geometry has a left handed CRS, <code>false</code> if CRS is right handed
     */
    protected boolean isLeftHanded( ICRS crs ) {
        // get number of dimensions (it should be 2)
        if ( crs.getDimension() == 2 ) {
            int axis1 = crs.getAxis()[0].getOrientation();
            int axis2 = crs.getAxis()[1].getOrientation();

            // check if CRS is left handed
            if ( axis1 == Axis.AO_EAST || axis1 == Axis.AO_WEST ) {
                if ( axis1 == Axis.AO_EAST && ( axis2 == Axis.AO_SOUTH || axis2 == Axis.AO_DOWN ) ) {
                    return true;
                }
                else if ( axis1 == Axis.AO_WEST && ( axis2 == Axis.AO_NORTH || axis2 == Axis.AO_UP ) ) {
                    return true;
                }
            }
            else {
                if ( ( axis1 == Axis.AO_SOUTH || axis1 == Axis.AO_DOWN ) && axis2 == Axis.AO_WEST ) {
                    return true;
                }
                else if ( ( axis1 == Axis.AO_NORTH || axis1 == Axis.AO_UP ) && axis2 == Axis.AO_EAST ) {
                    return true;
                }
            }
        }

        // return false in any other case
        return false;
    }

}
