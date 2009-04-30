//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
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
package org.deegree.commons.filter.spatial;

import org.deegree.commons.filter.FilterEvaluationException;
import org.deegree.commons.filter.Operator;
import org.deegree.geometry.Geometry;

/**
 * Defines a topological predicate that can be evaluated on {@link Geometry} objects.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public abstract class SpatialOperator implements Operator {

    /**
     * Convenience enum type for discriminating the different {@link SpatialOperator} types.
     */
    public enum SubType {
        /** True iff the two operands are identical. The {@link SpatialOperator} is an instance of {@link Equals}. */
        EQUALS,
        /** True iff the two operands are disjoint. The {@link SpatialOperator} is an instance of {@link Disjoint}. */
        DISJOINT,
        /** True iff the two operands touch. The {@link SpatialOperator} is an instance of {@link Touches}. */
        TOUCHES,
        /**
         * True iff the first operand is completely inside the second. The {@link SpatialOperator} is an instance of
         * {@link Within}.
         */
        WITHIN,
        /** True iff ... The {@link SpatialOperator} is an instance of {@link Overlaps}. */
        OVERLAPS,
        /** True iff ... The {@link SpatialOperator} is an instance of {@link Crosses}. */
        CROSSES,
        /** True iff ... The {@link SpatialOperator} is an instance of {@link Intersects}. */
        INTERSECTS,
        /** True iff ... The {@link SpatialOperator} is an instance of {@link Contains}. */
        CONTAINS,
        /** True iff ... The {@link SpatialOperator} is an instance of {@link DWithin}. */
        DWITHIN,
        /** True iff ... The {@link SpatialOperator} is an instance of {@link Beyond}. */
        BEYOND,
        /** True iff ... The {@link SpatialOperator} is an instance of {@link BBOX}. */
        BBOX
    }

    /**
     * Always returns {@link Operator.Type#SPATIAL} (for {@link SpatialOperator} instances).
     * 
     * @return {@link Operator.Type#SPATIAL}
     */
    public Type getType() {
        return Type.SPATIAL;
    }

    /**
     * Returns the type of spatial operator. Use this to safely determine the subtype of {@link SpatialOperator}.
     * 
     * @return type of spatial operator
     */
    public SubType getSubType() {
        return SubType.valueOf( getClass().getSimpleName().toUpperCase() );
    }

    protected Geometry checkGeometryOrNull( Object value )
                            throws FilterEvaluationException {
        if ( value != null && !( value instanceof Geometry ) ) {
            String msg = "Cannot evaluate operator '" + getType().name() + "'. Parameter '" + value
                         + "' is not geometric.";
            throw new FilterEvaluationException( msg );
        }
        return (Geometry) value;
    }
}
