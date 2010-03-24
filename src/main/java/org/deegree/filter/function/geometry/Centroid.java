//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.filter.function.geometry;

import java.util.List;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.feature.property.Property;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.MatchableObject;
import org.deegree.filter.expression.Function;
import org.deegree.geometry.Geometry;

/**
 * <code>Centroid</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Centroid extends Function {

    /**
     * @param exprs
     */
    public Centroid( List<Expression> exprs ) {
        super( "Centroid", exprs );
    }

    @Override
    public TypedObjectNode[] evaluate( MatchableObject f )
                            throws FilterEvaluationException {
        TypedObjectNode[] vals = getParams()[0].evaluate( f );

        if ( vals.length != 1 ) {
            throw new FilterEvaluationException( "The Centroid function takes exactly one argument (got " + vals.length
                                                 + ")." );
        }
        if ( vals[0] instanceof Geometry
             || ( vals[0] instanceof Property && ( (Property) vals[0] ).getValue() instanceof Geometry ) ) {
            Geometry geom = vals[0] instanceof Geometry ? (Geometry) vals[0]
                                                       : (Geometry) ( (Property) vals[0] ).getValue();
            return new TypedObjectNode[] { geom.getCentroid() };
        }
        if ( vals[0] == null ) {
            throw new FilterEvaluationException( "The argument to the Centroid function must be a geometry (was null)." );
        }

        throw new FilterEvaluationException( "The argument to the Centroid function must be a geometry (was a "
                                             + vals[0].getClass() + ")." );
    }

}
