package org.deegree.sqldialect.filter;

import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.spatial.SpatialOperator;
import org.deegree.sqldialect.filter.expression.SQLOperation;

/**
 * Mock implementation of {@link AbstractWhereBuilder} that uses the {@link MockPropertyNameMapper}.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus schneider</a>
 *
 * @since 3.4
 */
class MockWhereBuilder extends AbstractWhereBuilder {

    MockWhereBuilder( final OperatorFilter filter, final SortProperty[] sortCrit ) throws FilterEvaluationException {
        super( null, new MockPropertyNameMapper(), filter, sortCrit );
    }

    @Override
    protected SQLOperation toProtoSQL( final SpatialOperator op )
                            throws UnmappableException, FilterEvaluationException {
        return null;
    }

}