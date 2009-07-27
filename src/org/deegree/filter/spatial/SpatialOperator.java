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
package org.deegree.filter.spatial;

import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.Operator;
import org.deegree.filter.i18n.Messages;
import org.deegree.geometry.Geometry;

/**
 * Defines a topological predicate that can be evaluated on {@link Geometry} valued objects.
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

    /**
     * Performs a checked cast to {@link Geometry}. If the given value is neither null nor a {@link Geometry} instance, a
     * corresponding {@link FilterEvaluationException} is thrown.
     * 
     * @param value
     * @return the very same value (if it is a {@link Geometry} or <code>null</code>)
     * @throws FilterEvaluationException
     *             if the value is neither <code>null</code> nor a {@link Geometry}
     */
    protected Geometry checkGeometryOrNull( Object value )
                            throws FilterEvaluationException {
        if ( value != null && !( value instanceof Geometry ) ) {
            String msg = Messages.getMessage( "FILTER_EVALUATION_NOT_GEOMETRY", getType().name(), value );
            throw new FilterEvaluationException( msg );
        }
        return (Geometry) value;
    }
}
