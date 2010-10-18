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

import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.feature.property.Property;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.expression.Function;
import org.deegree.filter.function.FunctionProvider;
import org.deegree.geometry.Geometry;

/**
 * Returns a list of centroids for each geometry. Other values are ignored.
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Centroid implements FunctionProvider {

    private static final String NAME = "Centroid";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getArgCount() {
        return 1;
    }

    @Override
    public Function create( List<Expression> params ) {
        return new Function( NAME, params ) {
            private Geometry getGeomValue( TypedObjectNode node ) {
                Geometry geom = null;
                if ( node instanceof Geometry ) {
                    geom = (Geometry) node;
                } else if ( node instanceof Property && ( (Property) node ).getValue() instanceof Geometry ) {
                    geom = (Geometry) ( (Property) node ).getValue();
                }
                return geom;
            }

            @Override
            public <T> TypedObjectNode[] evaluate( T obj, XPathEvaluator<T> xpathEvaluator )
                                    throws FilterEvaluationException {

                TypedObjectNode[] inputs = getParams()[0].evaluate( obj, xpathEvaluator );
                List<TypedObjectNode> centroids = new ArrayList<TypedObjectNode>( inputs.length );
                for ( TypedObjectNode val : inputs ) {
                    Geometry geom = getGeomValue( val );
                    if ( geom != null ) {
                        centroids.add( geom.getCentroid() );
                    }
                }
                return centroids.toArray( new TypedObjectNode[centroids.size()] );
            }
        };
    }
}