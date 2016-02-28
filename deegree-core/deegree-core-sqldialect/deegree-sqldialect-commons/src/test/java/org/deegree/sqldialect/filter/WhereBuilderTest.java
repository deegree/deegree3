package org.deegree.sqldialect.filter;

import static org.deegree.filter.MatchAction.ANY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.logical.And;
import org.junit.Test;

/**
 * Tests for {@link AbstractWhereBuilder}.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.4
 */
public class WhereBuilderTest {

    @Test
    public void buildUsingFullMapping()
                            throws FilterEvaluationException, UnmappableException {
        final OperatorFilter filter = createAndFilter( 2, 0 );
        final MockWhereBuilder wb = new MockWhereBuilder( filter, null );
        wb.build( true );

        // full filter was mapped to SQL
        final String actualSql = wb.getWhere().getSQL().toString();
        assertEquals( "(MAPPABLE_0 = ? AND MAPPABLE_1 = ?)", actualSql );

        // post filter is null
        final OperatorFilter actualPostFilter = wb.getPostFilter();
        assertNull( actualPostFilter );
    }

    @Test
    public void buildUsingPartialMapping()
                            throws FilterEvaluationException, UnmappableException {
        final OperatorFilter filter = createAndFilter( 1, 1 );
        final MockWhereBuilder wb = new MockWhereBuilder( filter, null );
        wb.build( true );

        // one part of filter was mapped to SQL
        final String actualSql = wb.getWhere().getSQL().toString();
        assertEquals( "MAPPABLE_0 = ?", actualSql );

        // remaining part of filter was extracted as post filter
        final OperatorFilter actualPostFilter = wb.getPostFilter();
        final PropertyIsEqualTo operator = (PropertyIsEqualTo) actualPostFilter.getOperator();
        final ValueReference ref = (ValueReference) operator.getParameter1();
        assertEquals( "UNMAPPABLE_0", ref.getAsQName().getLocalPart() );
    }

    private OperatorFilter createAndFilter( final int numMappedOperands, final int numUnmappedOperands ) {
        final Collection<Operator> operators = new ArrayList<Operator>();
        for ( int i = 0; i < numMappedOperands; i++ ) {
            final ValueReference param1 = new ValueReference( new QName( "MAPPABLE_" + i ) );
            final Literal<PrimitiveValue> param2 = new Literal<PrimitiveValue>( "VALUE" );
            operators.add( new PropertyIsEqualTo( param1, param2, true, ANY ) );
        }
        for ( int i = 0; i < numUnmappedOperands; i++ ) {
            final ValueReference param1 = new ValueReference( new QName( "UNMAPPABLE_" + i ) );
            final Literal<PrimitiveValue> param2 = new Literal<PrimitiveValue>( "VALUE" );
            operators.add( new PropertyIsEqualTo( param1, param2, true, ANY ) );
        }
        final And and = new And( operators.toArray( new Operator[operators.size()] ) );
        return new OperatorFilter( and );
    }
}
