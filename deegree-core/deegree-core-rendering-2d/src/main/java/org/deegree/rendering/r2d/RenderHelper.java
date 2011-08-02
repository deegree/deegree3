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

package org.deegree.rendering.r2d;

import static org.deegree.style.utils.ShapeHelper.getShapeFromMark;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Shape;

import org.deegree.style.styling.components.Mark;
import org.deegree.style.styling.components.UOM;
import org.slf4j.Logger;

/**
 * <code>RenderHelper</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class RenderHelper {

    private static final Logger LOG = getLogger( RenderHelper.class );

    /**
     * @param mark
     * @param size
     * @param uom
     * @param renderer
     * @param x
     * @param y
     * @param rotation
     */
    public static void renderMark( Mark mark, int size, UOM uom, Java2DRenderer renderer, double x, double y,
                                   double rotation ) {
        if ( size == 0 ) {
            LOG.debug( "Not rendering a symbol because the size is zero." );
            return;
        }
        if ( mark.fill == null && mark.stroke == null ) {
            LOG.debug( "Not rendering a symbol because no fill/stroke is available/configured." );
            return;
        }

        Shape shape = getShapeFromMark( mark, size - 1, rotation, true, x, y );

        if ( mark.fill != null ) {
            renderer.applyFill( mark.fill, uom );
            renderer.graphics.fill( shape );
        }
        if ( mark.stroke != null ) {
            renderer.applyStroke( mark.stroke, uom, shape, 0, null );
        }
    }

}
