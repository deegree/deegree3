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
/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package org.deegree.rendering.r2d.strokes;

import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;

/**
 * <code>CompoundStroke</code>
 *
 * @author Jerry Huxtable
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class CompoundStroke implements Stroke {

    private Stroke stroke1, stroke2;

    private Operation operation;

    /**
     * @param stroke1
     * @param stroke2
     * @param operation
     */
    public CompoundStroke( Stroke stroke1, Stroke stroke2, Operation operation ) {
        this.stroke1 = stroke1;
        this.stroke2 = stroke2;
        this.operation = operation;
    }

    public Shape createStrokedShape( Shape shape ) {
        Area area1 = new Area( stroke1.createStrokedShape( shape ) );
        Area area2 = new Area( stroke2.createStrokedShape( shape ) );
        switch ( operation ) {
        case ADD:
            area1.add( area2 );
            break;
        case SUBSTRACT:
            area1.subtract( area2 );
            break;
        case INTERSECT:
            area1.intersect( area2 );
            break;
        case DIFFERENCE:
            area1.exclusiveOr( area2 );
            break;
        }
        return area1;
    }

    /**
     * <code>Operation</code>
     *
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     *
     * @version $Revision$, $Date$
     */
    public enum Operation {
        /**
         *
         */
        ADD,
        /**
         *
         */
        SUBSTRACT,
        /**
         *
         */
        INTERSECT,
        /**
         *
         */
        DIFFERENCE
    }

}
