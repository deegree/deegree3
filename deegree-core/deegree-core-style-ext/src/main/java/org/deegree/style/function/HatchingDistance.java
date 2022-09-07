package org.deegree.style.function;

import static org.deegree.commons.utils.math.MathUtils.isZero;
import static org.deegree.filter.function.ParameterType.DOUBLE;

import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.property.SimpleProperty;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.expression.Function;
import org.deegree.filter.function.FunctionProvider;
import org.deegree.filter.function.ParameterType;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.Workspace;

public class HatchingDistance implements FunctionProvider {

    private static final String NAME = "HatchingDistance";

    private static final List<ParameterType> INPUTS = new ArrayList<ParameterType>( 2 );

    static {
        INPUTS.add( DOUBLE );
        INPUTS.add( DOUBLE );
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<ParameterType> getArgs() {
        return INPUTS;
    }

    @Override
    public ParameterType getReturnType() {
        return DOUBLE;
    }

    @Override
    public Function create( List<Expression> params ) {
        return new Function( NAME, params ) {

            private <T> Pair<Double, Double> extractValues( Expression first, Expression second, T f,
                                                            XPathEvaluator<T> xpathEvaluator )
                                    throws FilterEvaluationException {
                TypedObjectNode[] vals1 = first.evaluate( f, xpathEvaluator );
                TypedObjectNode[] vals2 = second.evaluate( f, xpathEvaluator );

                checkTwoArguments( NAME, vals1, vals2 );

                PrimitiveValue pv1;
                PrimitiveValue pv2;
                if ( vals1[0] instanceof PrimitiveValue ) {
                    pv1 = (PrimitiveValue) vals1[0];
                } else {
                    pv1 = ( (SimpleProperty) vals1[0] ).getValue();
                }
                if ( vals2[0] instanceof PrimitiveValue ) {
                    pv2 = (PrimitiveValue) vals2[0];
                } else {
                    pv2 = ( (SimpleProperty) vals2[0] ).getValue();
                }

                return new Pair<Double, Double>( Double.valueOf( pv1.getValue().toString() ),
                                                 Double.valueOf( pv2.getValue().toString() ) );
            }

            @Override
            public <T> TypedObjectNode[] evaluate( T obj, XPathEvaluator<T> xpathEvaluator )
                                    throws FilterEvaluationException {
                Pair<Double, Double> p = extractValues( getParams()[0], getParams()[1], obj, xpathEvaluator );
                double angle = p.getFirst();
                double distance;
                while ( angle < 0.0 )
                    angle += 90.0d;
                while ( angle >= 90.0 )
                    angle -= 90.0d;

                if ( isZero( angle ) ) {
                    distance = p.getSecond();
                } else {
                    double ak = p.getSecond() / Math.sin( Math.toRadians( angle ) );
                    distance = ak / Math.cos( Math.toRadians( angle ) );
                }

                return new TypedObjectNode[] { new PrimitiveValue( Double.valueOf( distance ) ) };
            }
        };
    }

    static void checkTwoArguments( String name, TypedObjectNode[] vals1, TypedObjectNode[] vals2 )
                            throws FilterEvaluationException {
        if ( vals1.length == 0 || vals2.length == 0 ) {
            String msg = "The " + name + " function expects two arguments, but ";
            if ( vals1.length == 0 && vals2.length == 0 ) {
                msg += "both arguments were missing.";
            } else {
                msg += "the ";
                msg += vals1.length == 0 ? "first" : "second";
                msg += " argument was missing.";
            }
            throw new FilterEvaluationException( msg );
        }
    }

    @Override
    public void init( Workspace ws )
                            throws ResourceInitException {
        // nothing to do
    }

    @Override
    public void destroy() {
        // nothing to do
    }
}
