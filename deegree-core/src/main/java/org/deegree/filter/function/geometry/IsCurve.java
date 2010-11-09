//$HeadURL$
package org.deegree.filter.function.geometry;

import java.util.List;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.property.Property;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.expression.Function;
import org.deegree.filter.function.FunctionProvider;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.primitive.Curve;

/**
 * Returns no value in case the argument expression evaluates to no value, or multiple values, or the value can not be
 * interpreted as a geometry.
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class IsCurve implements FunctionProvider {

    private static final String NAME = "IsCurve";

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

        if ( params.size() != 1 ) {
            throw new IllegalArgumentException( NAME + " requires exactly one parameter." );
        }

        return new Function( NAME, params ) {
            @Override
            public <T> TypedObjectNode[] evaluate( T obj, XPathEvaluator<T> xpathEvaluator )
                                    throws FilterEvaluationException {

                TypedObjectNode[] vals = getParams()[0].evaluate( obj, xpathEvaluator );
                if ( vals.length != 1 || !( vals[0] instanceof Geometry ) && !( vals[0] instanceof Property )
                     && !( ( (Property) vals[0] ).getValue() instanceof Geometry ) ) {
                    return new TypedObjectNode[0];
                }
                Geometry geom = vals[0] instanceof Geometry ? (Geometry) vals[0]
                                                           : (Geometry) ( (Property) vals[0] ).getValue();

                // TODO is handling of multi geometries like this ok?
                boolean result = geom instanceof Curve || geom instanceof MultiCurve || geom instanceof MultiLineString;
                return new TypedObjectNode[] { new PrimitiveValue( Boolean.toString( result ) ) };
            }
        };
    }
}