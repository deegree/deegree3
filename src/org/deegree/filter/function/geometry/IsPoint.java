//$HeadURL$
package org.deegree.filter.function.geometry;

import java.util.List;

import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.MatchableObject;
import org.deegree.filter.expression.Function;
import org.deegree.geometry.primitive.Point;

/**
 * <code>IsPoint</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class IsPoint extends Function {

    /**
     * @param exprs
     */
    public IsPoint( List<Expression> exprs ) {
        super( "IsPoint", exprs );
    }

    @Override
    public Object[] evaluate( MatchableObject f )
                            throws FilterEvaluationException {
        Object[] vals = getParams()[0].evaluate( f );
        return new Object[] { new Boolean( vals != null && vals.length > 0 && vals[0] instanceof Point ).toString() };
    }

}
