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
package org.deegree.feature.persistence.postgis;

import java.util.Collection;

import javax.xml.namespace.QName;

import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.Expression;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.ComparisonOperator;
import org.deegree.filter.comparison.PropertyIsBetween;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.comparison.PropertyIsGreaterThan;
import org.deegree.filter.comparison.PropertyIsGreaterThanOrEqualTo;
import org.deegree.filter.comparison.PropertyIsLessThan;
import org.deegree.filter.comparison.PropertyIsLessThanOrEqualTo;
import org.deegree.filter.comparison.PropertyIsNotEqualTo;
import org.deegree.filter.comparison.PropertyIsNull;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.logical.LogicalOperator;
import org.deegree.filter.spatial.SpatialOperator;
import org.jaxen.expr.Expr;

/**
 * Creates SQL-WHERE clauses from {@link Filter} expressions (to restrict SQL <code>ResultSet</code>s to those features
 * that match a given filter). Also handles the creation of ORDER-BY clauses.
 * <p>
 * Note that the generated WHERE and ORDER-BY clauses are sometimes not sufficient to guarantee that the
 * <code>ResultSet</code> contains the targeted feature instances only and/or keeps the requested feature order. This
 * happens when the {@link PropertyName}s used in the Filter/sort criteria are not mapped to columns in the database or
 * the contained XPath expressions are not mappable to an equivalent SQL expression. In these cases, one or both of the
 * methods {@link #needsPostFiltering}/{@link #needsPostSorting} return true and the corresponding
 * {@link FeatureResultSet} must be filtered/sorted in memory to guarantee the requested constraints/order.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class WhereBuilder {

    private final PostGISFeatureStore fs;

    private final FeatureType ft;

    private final FeatureTypeMapping mapping;

    private final OperatorFilter filter;

    private boolean needsPostFiltering;

    private boolean needsPostSorting;

    private StringBuilder whereClause;

    private Collection<Object> whereParams;

    private StringBuilder orderBy;

    private Collection<Object> orderByParams;

    /**
     * @param fs
     * @param ft
     *            {@link FeatureType} to be queried, must not be <code>null</code> and served by {@link #fs}
     * @param mapping
     *            relational mapping for {@link #ft}, must not be <code>null</code>
     * @param filter
     *            Filter to use for generating the WHERE clause, must not be <code>null</code>
     * @throws FilterEvaluationException
     *             if the filter is invalid
     */
    WhereBuilder( PostGISFeatureStore fs, FeatureType ft, FeatureTypeMapping mapping, OperatorFilter filter )
                            throws FilterEvaluationException {
        this.fs = fs;
        this.ft = ft;
        this.mapping = mapping;
        this.filter = filter;
        process( filter.getOperator() );
    }

    private void process( Operator op )
                            throws FilterEvaluationException {
        switch ( op.getType() ) {
        case COMPARISON: {
            process( (ComparisonOperator) op );
            break;
        }
        case LOGICAL: {
            process( (LogicalOperator) op );
            break;
        }
        case SPATIAL: {
            process( (SpatialOperator) op );
            break;
        }
        }
    }

    private void process( ComparisonOperator op )
                            throws FilterEvaluationException {
        switch ( op.getSubType() ) {
        case PROPERTY_IS_BETWEEN: {
            PropertyIsBetween propIsBetween = (PropertyIsBetween) op;
            process( propIsBetween.getUpperBoundary(), false );
            whereClause.append( ">=" );
            process( propIsBetween.getExpression(), false );
            whereClause.append( "<=" );
            process( propIsBetween.getLowerBoundary(), false );
            break;
        }
        case PROPERTY_IS_EQUAL_TO: {
            PropertyIsEqualTo propIsEqualTo = (PropertyIsEqualTo) op;
            process( propIsEqualTo.getParameter1(), !propIsEqualTo.getMatchCase() );
            whereClause.append( "=" );
            process( propIsEqualTo.getParameter2(), !propIsEqualTo.getMatchCase() );
            break;
        }
        case PROPERTY_IS_GREATER_THAN: {
            PropertyIsGreaterThan propIsGT = (PropertyIsGreaterThan) op;
            process( propIsGT.getParameter1(), !propIsGT.getMatchCase() );
            whereClause.append( ">" );
            process( propIsGT.getParameter2(), !propIsGT.getMatchCase() );
            break;
        }
        case PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO: {
            PropertyIsGreaterThanOrEqualTo propIsGTOrEqualTo = (PropertyIsGreaterThanOrEqualTo) op;
            process( propIsGTOrEqualTo.getParameter1(), !propIsGTOrEqualTo.getMatchCase() );
            whereClause.append( ">=" );
            process( propIsGTOrEqualTo.getParameter2(), !propIsGTOrEqualTo.getMatchCase() );
            break;
        }
        case PROPERTY_IS_LESS_THAN: {
            PropertyIsLessThan propIsLT = (PropertyIsLessThan) op;
            process( propIsLT.getParameter1(), !propIsLT.getMatchCase() );
            whereClause.append( "<" );
            process( propIsLT.getParameter2(), !propIsLT.getMatchCase() );
            break;
        }
        case PROPERTY_IS_LESS_THAN_OR_EQUAL_TO: {
            PropertyIsLessThanOrEqualTo propIsLTOrEqualTo = (PropertyIsLessThanOrEqualTo) op;
            process( propIsLTOrEqualTo.getParameter1(), !propIsLTOrEqualTo.getMatchCase() );
            whereClause.append( "<=" );
            process( propIsLTOrEqualTo.getParameter2(), !propIsLTOrEqualTo.getMatchCase() );
            break;
        }
        case PROPERTY_IS_LIKE: {
            // TODO
            break;
        }
        case PROPERTY_IS_NOT_EQUAL_TO: {
            PropertyIsNotEqualTo propIsNotEqualTo = (PropertyIsNotEqualTo) op;
            process( propIsNotEqualTo.getParameter1(), !propIsNotEqualTo.getMatchCase() );
            whereClause.append( "<>" );
            process( propIsNotEqualTo.getParameter2(), !propIsNotEqualTo.getMatchCase() );
            break;
        }
        case PROPERTY_IS_NULL: {
            PropertyIsNull propIsNull = (PropertyIsNull) op;
            process( propIsNull.getPropertyName(), false );
            whereClause.append( " IS NULL" );
            break;
        }
        }
    }

    private void process( LogicalOperator op )
                            throws FilterEvaluationException {
        switch ( op.getSubType() ) {
        case AND: {
            whereClause.append( "(" );
            process( op.getParams()[0] );
            whereClause.append( ")" );
            for ( int i = 1; i < op.getParams().length; i++ ) {
                whereClause.append( " AND (" );
                process( op.getParams()[i] );
                whereClause.append( ")" );
            }
            break;
        }
        case OR: {
            whereClause.append( "(" );
            process( op.getParams()[0] );
            whereClause.append( ")" );
            for ( int i = 1; i < op.getParams().length; i++ ) {
                whereClause.append( " OR (" );
                process( op.getParams()[i] );
                whereClause.append( ")" );
            }
            break;
        }
        case NOT: {
            whereClause.append( "NOT (" );
            process( op.getParams()[0] );
            whereClause.append( ")" );
            break;
        }
        }
    }

    private void process( SpatialOperator op )
                            throws FilterEvaluationException {
        switch ( op.getSubType() ) {
        case BBOX: {
            break;
        }
        case BEYOND: {
            break;
        }
        case CONTAINS: {
            break;
        }
        case CROSSES: {
            break;
        }
        case DISJOINT: {
            break;
        }
        case DWITHIN: {
            break;
        }
        case EQUALS: {
            break;
        }
        case INTERSECTS: {
            break;
        }
        case OVERLAPS: {
            break;
        }
        case TOUCHES: {
            break;
        }
        case WITHIN: {
            break;
        }
        }
    }

    private void process( Expression expr, boolean lowerCase )
                            throws FilterEvaluationException {
        switch ( expr.getType() ) {
        case ADD: {
            // TODO
            break;
        }
        case DIV: {
            break;
        }
        case FUNCTION: {
            break;
        }
        case LITERAL: {
            if ( lowerCase ) {
                whereClause.append( "LOWER(?)" );
            } else {
                whereClause.append( "?" );
            }
            break;
        }
        case MUL: {
            break;
        }
        case PROPERTY_NAME: {
            PropertyName propName = (PropertyName) expr;
            break;
        }
        case SUB: {
            break;
        }
        }
    }

    /**
     * @return
     */
    StringBuilder getWhereClause() {
        return whereClause;
    }

    Collection<Object> getWhereParams() {
        return whereParams;
    }

    /**
     * @return
     */
    StringBuilder getOrderBy() {
        return orderBy;
    }

    /**
     * Returns whether the <code>ResultSet</code> will need post-filtering in memory.
     * 
     * @return true, if the <code>ResultSet<code> must be re-filtered, false otherwise
     */
    boolean needsPostFiltering() {
        return needsPostFiltering;
    }

    /**
     * Returns whether the <code>ResultSet</code> will need post-sorting in memory.
     * 
     * @return true, if the <code>ResultSet<code> must be re-sorted, false otherwise
     */
    boolean needsPostSorting() {
        return needsPostSorting;
    }
}
