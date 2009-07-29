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

package org.deegree.crs.coordinatesystems;

import org.deegree.crs.CRSIdentifiable;
import org.deegree.crs.components.Axis;
import org.deegree.crs.components.VerticalDatum;

/**
 * The <code>VerticalCRS</code> represents a crs based on one axis only, typically this crs is used as an extension on
 * an already present crs, and adds a 'heightaxis' to the original crs.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class VerticalCRS extends CoordinateSystem {

    /**
     * @param datum
     * @param axisOrder
     * @param identity
     */
    public VerticalCRS( VerticalDatum datum, Axis[] axisOrder, CRSIdentifiable identity ) {
        super( datum, axisOrder, identity );
        checkForNullObject( axisOrder, "VerticalCRS", "axisOrder" );
        if ( axisOrder.length != 1 ) {
            throw new IllegalArgumentException( "A vertical crs can only be 1 dimensional." );
        }
    }

    @Override
    public int getDimension() {
        return 1;
    }

    @Override
    public int getType() {
        return CoordinateSystem.VERTICAL_CRS;
    }

    /**
     * @return the vertical datum of this crs
     */
    public VerticalDatum getVerticalDatum() {
        return (VerticalDatum) super.getDatum();
    }

    /**
     * @return the axis of this vertical crs.
     */
    public Axis getVerticalAxis() {
        return getAxis()[0];
    }
}
