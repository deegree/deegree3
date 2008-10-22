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

package org.deegree.rendering.strokes;

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
