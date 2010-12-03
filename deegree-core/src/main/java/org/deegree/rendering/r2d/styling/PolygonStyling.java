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

package org.deegree.rendering.r2d.styling;

import static org.deegree.commons.utils.JavaUtils.generateToString;
import static org.deegree.rendering.r2d.styling.components.UOM.Pixel;

import org.deegree.rendering.r2d.styling.components.Fill;
import org.deegree.rendering.r2d.styling.components.PerpendicularOffsetType;
import org.deegree.rendering.r2d.styling.components.Stroke;
import org.deegree.rendering.r2d.styling.components.UOM;

/**
 * <code>PolygonStyling</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PolygonStyling implements Copyable<PolygonStyling>, Styling {

    /**
     * The unit of measure for all values.
     */
    public UOM uom = Pixel;

    /**
     * Default is null.
     */
    public Fill fill;

    /**
     * Default is null.
     */
    public Stroke stroke;

    /**
     * Default is 0.
     */
    public double displacementX;

    /**
     * Default is 0.
     */
    public double displacementY;

    /**
     * Default is 0.
     */
    public double perpendicularOffset;

    /** Default is Standard. */
    public PerpendicularOffsetType perpendicularOffsetType = new PerpendicularOffsetType();

    public PolygonStyling copy() {
        PolygonStyling copy = new PolygonStyling();
        copy.fill = fill == null ? null : fill.copy();
        copy.stroke = stroke == null ? null : stroke.copy();
        copy.displacementX = displacementX;
        copy.displacementY = displacementY;
        copy.perpendicularOffset = perpendicularOffset;
        copy.perpendicularOffsetType = perpendicularOffsetType.copy();
        copy.uom = uom;
        return copy;
    }

    @Override
    public String toString() {
        return generateToString( this );
    }

}
