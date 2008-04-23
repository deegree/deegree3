//$HeadURL: $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.model.crs.projections.cylindric;

import javax.vecmath.Point2d;

import org.deegree.model.crs.components.Unit;
import org.deegree.model.crs.coordinatesystems.GeographicCRS;
import org.deegree.model.crs.projections.Projection;

/**
 * The <code>CylindricalProjection</code> is a super class for all cylindrical projections.
 * <p>
 * <q>(From Snyder p.97)</q>
 * </p>
 * <p>
 * Cylindrical projections are used primarily for complete world maps, or for maps along narrow strips of a great circle
 * arc, such as the Equator, a meridian or an oblique great circle.
 * </p>
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */

public abstract class CylindricalProjection extends Projection {

    /**
     * @param geographicCRS 
     * @param falseNorthing
     * @param falseEasting
     * @param naturalOrigin
     * @param units
     * @param scale
     * @param conformal
     * @param equalArea 
     */
    public CylindricalProjection( GeographicCRS geographicCRS, double falseNorthing, double falseEasting,
                                  Point2d naturalOrigin, Unit units, double scale, boolean conformal, boolean equalArea ) {
        super(  geographicCRS,
               falseNorthing,
               falseEasting,
               naturalOrigin,
               units,
               scale,
               conformal,
               equalArea );
    }
  
}
