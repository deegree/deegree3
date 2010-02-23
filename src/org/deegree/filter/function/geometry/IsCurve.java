//$HeadURL$
package org.deegree.filter.function.geometry;

import java.util.List;

import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.MatchableObject;
import org.deegree.filter.expression.Function;
import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.primitive.Curve;

/**
 * <code>IsCurve</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class IsCurve extends Function {

    /**
     * @param exprs
     */
    public IsCurve( List<Expression> exprs ) {
        super( "IsCurve", exprs );
    }

    @Override
    public Object[] evaluate( MatchableObject f )
                            throws FilterEvaluationException {
        Object[] vals = getParams()[0].evaluate( f );
        // TODO is handling of multi geometries like this ok?
        boolean result = vals != null
                         && vals.length > 0
                         && ( vals[0] instanceof Curve || vals[0] instanceof MultiCurve || vals[0] instanceof MultiLineString );
        return new Object[] { new Boolean( result ).toString() };
    }

}
