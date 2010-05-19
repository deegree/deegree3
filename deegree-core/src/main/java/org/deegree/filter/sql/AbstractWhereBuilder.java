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
package org.deegree.filter.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.deegree.filter.comparison.PropertyIsLike;
import org.deegree.filter.comparison.PropertyIsNotEqualTo;
import org.deegree.filter.comparison.PropertyIsNull;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.logical.LogicalOperator;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.spatial.SpatialOperator;
import org.deegree.filter.sql.islike.IsLikeString;
import org.deegree.geometry.Geometry;

/**
 * DB-independent base class for the creation of SQL-WHERE clauses from {@link Filter} expressions (to restrict SQL
 * <code>ResultSet</code>s to those rows that contain objects that match a given filter). Also handles the creation of
 * ORDER BY clauses.
 * <p>
 * Note that the generated WHERE and ORDER-BY clauses are sometimes not sufficient to guarantee that the
 * <code>ResultSet</code> only contains the targeted objects and/or keeps the requested order. This happens when the
 * {@link PropertyName}s used in the Filter/sort criteria are not mappable to columns in the database or the contained
 * XPath expressions are not mappable to an equivalent SQL expression. In these cases, one or both of the methods
 * {@link #getPostFilter()}/{@link #getPostSortCriteria()} return not null and the objects extracted from the
 * corresponding {@link ResultSet} must be filtered/sorted in memory to guarantee the requested constraints/order.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class AbstractWhereBuilder {

    private final OperatorFilter filter;

    private OperatorFilter postFilter;

    private SortProperty[] postSortCrit;

    protected StringBuilder whereClause = new StringBuilder();

    private Collection<Object> whereParams = new ArrayList<Object>();

    private StringBuilder orderBy = new StringBuilder();

    /**
     * Creates a new {@link AbstractWhereBuilder} instance.
     * 
     * @param filter
     *            Filter to use for generating the WHERE clause, can be <code>null</code>
     * @param sortCrit
     *            criteria to use generating the ORDER BY clause, can be <code>null</code>
     * @throws FilterEvaluationException
     *             if the filter contains invalid {@link PropertyName}s
     */
    protected AbstractWhereBuilder( OperatorFilter filter, SortProperty[] sortCrit ) throws FilterEvaluationException {
        this.filter = filter;
        if ( filter != null ) {
            buildWhere( filter.getOperator() );
        }
        if ( sortCrit != null ) {
            buildOrderBy( sortCrit );
        }
    }

    /**
     * Returns the SQL-WHERE clause, without leading "WHERE" keyword.
     * 
     * @return the WHERE clause, can be empty, but never <code>null</code>
     */
    public StringBuilder getWhereClause() {
        if ( postFilter != null ) {
            return new StringBuilder();
        }
        return whereClause;
    }

    /**
     * Returns the parameters to be set ({@link PreparedStatement#setObject(int, Object)} for the SQL-WHERE clause.
     * 
     * @return the list of parameters, can be empty, but never <code>null</code>
     */
    public Collection<Object> getWhereParams() {
        return whereParams;
    }

    /**
     * Returns the SQL-WHERE clause, without leading "ORDER BY" keyword.
     * 
     * @return the ORDER BY clause, can be empty, but never <code>null</code>
     */
    public StringBuilder getOrderBy() {
        if ( postSortCrit != null ) {
            return new StringBuilder();
        }
        return orderBy;
    }

    /**
     * Returns a {@link Filter} that contains all constraints from the input filter that could not be expressed in the
     * WHERE clause.
     * 
     * @return filter to apply on the objects from the <code>ResultSet</code>, may be <code>null</code> (no
     *         post-filtering necessary)
     */
    public OperatorFilter getPostFilter() {
        return postFilter;
    }

    /**
     * Returns the sort criteria that contains all parts from the input sort criteria that could not be expressed in the
     * ORDER BY clause.
     * 
     * @return sort criteria to apply on the objects from the <code>ResultSet</code>, may be <code>null</code> (no
     *         post-sorting necessary)
     */
    public SortProperty[] getPostSortCriteria() {
        return postSortCrit;
    }

    /**
     * @param op
     * @throws FilterEvaluationException
     */
    protected void buildWhere( Operator op )
                            throws FilterEvaluationException {

        switch ( op.getType() ) {
        case COMPARISON: {
            buildWhere( (ComparisonOperator) op );
            break;
        }
        case LOGICAL: {
            buildWhere( (LogicalOperator) op );
            break;
        }
        case SPATIAL: {
            buildWhere( (SpatialOperator) op );
            break;
        }
        }
    }

    /**
     * @param op
     * @throws FilterEvaluationException
     */
    protected void buildWhere( ComparisonOperator op )
                            throws FilterEvaluationException {

        // TODO make type inference fail-proof
        List<PropertyName> involvedProps = new ArrayList<PropertyName>();
        for ( Expression expr : op.getParams() ) {
            determineInvolvedProps( expr, involvedProps );
        }
        PropertyName correspondingProp = null;
        if ( !involvedProps.isEmpty() ) {
            correspondingProp = involvedProps.get( 0 );
        }

        switch ( op.getSubType() ) {
        case PROPERTY_IS_BETWEEN: {
            PropertyIsBetween propIsBetween = (PropertyIsBetween) op;
            buildWhere( propIsBetween.getUpperBoundary(), !propIsBetween.getMatchCase(), correspondingProp );
            whereClause.append( ">=" );
            buildWhere( propIsBetween.getExpression(), !propIsBetween.getMatchCase(), correspondingProp );
            whereClause.append( "<=" );
            buildWhere( propIsBetween.getLowerBoundary(), !propIsBetween.getMatchCase(), correspondingProp );
            break;
        }
        case PROPERTY_IS_EQUAL_TO: {
            PropertyIsEqualTo propIsEqualTo = (PropertyIsEqualTo) op;
            buildWhere( propIsEqualTo.getParameter1(), !propIsEqualTo.getMatchCase(), correspondingProp );
            whereClause.append( "=" );
            buildWhere( propIsEqualTo.getParameter2(), !propIsEqualTo.getMatchCase(), correspondingProp );
            break;
        }
        case PROPERTY_IS_GREATER_THAN: {
            PropertyIsGreaterThan propIsGT = (PropertyIsGreaterThan) op;
            buildWhere( propIsGT.getParameter1(), !propIsGT.getMatchCase(), correspondingProp );
            whereClause.append( ">" );
            buildWhere( propIsGT.getParameter2(), !propIsGT.getMatchCase(), correspondingProp );
            break;
        }
        case PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO: {
            PropertyIsGreaterThanOrEqualTo propIsGTOrEqualTo = (PropertyIsGreaterThanOrEqualTo) op;
            buildWhere( propIsGTOrEqualTo.getParameter1(), !propIsGTOrEqualTo.getMatchCase(), correspondingProp );
            whereClause.append( ">=" );
            buildWhere( propIsGTOrEqualTo.getParameter2(), !propIsGTOrEqualTo.getMatchCase(), correspondingProp );
            break;
        }
        case PROPERTY_IS_LESS_THAN: {
            PropertyIsLessThan propIsLT = (PropertyIsLessThan) op;
            buildWhere( propIsLT.getParameter1(), !propIsLT.getMatchCase(), correspondingProp );
            whereClause.append( "<" );
            buildWhere( propIsLT.getParameter2(), !propIsLT.getMatchCase(), correspondingProp );
            break;
        }
        case PROPERTY_IS_LESS_THAN_OR_EQUAL_TO: {
            PropertyIsLessThanOrEqualTo propIsLTOrEqualTo = (PropertyIsLessThanOrEqualTo) op;
            buildWhere( propIsLTOrEqualTo.getParameter1(), !propIsLTOrEqualTo.getMatchCase(), correspondingProp );
            whereClause.append( "<=" );
            buildWhere( propIsLTOrEqualTo.getParameter2(), !propIsLTOrEqualTo.getMatchCase(), correspondingProp );
            break;
        }
        case PROPERTY_IS_LIKE: {
            buildWhere( (PropertyIsLike) op );
            break;
        }
        case PROPERTY_IS_NOT_EQUAL_TO: {
            PropertyIsNotEqualTo propIsNotEqualTo = (PropertyIsNotEqualTo) op;
            buildWhere( propIsNotEqualTo.getParameter1(), !propIsNotEqualTo.getMatchCase(), correspondingProp );
            whereClause.append( "<>" );
            buildWhere( propIsNotEqualTo.getParameter2(), !propIsNotEqualTo.getMatchCase(), correspondingProp );
            break;
        }
        case PROPERTY_IS_NULL: {
            PropertyIsNull propIsNull = (PropertyIsNull) op;
            buildWhere( propIsNull.getPropertyName(), false, correspondingProp );
            whereClause.append( " IS NULL" );
            break;
        }
        }
    }

    /**
     * TODO better shot at type inference
     */
    private void determineInvolvedProps( Expression expr, List<PropertyName> props ) {
        if ( expr instanceof PropertyName ) {
            props.add( (PropertyName) expr );
        } else {
            for ( Expression subExpr : expr.getParams() ) {
                determineInvolvedProps( subExpr, props );
            }
        }
    }

    /**
     * NOTE: Currently, this method appends the generated argument inline, i.e. not using a <code>?</code>. This is
     * because of a problem that occurred in PostgreSQL; the execution of the inline version is *much* faster (at least
     * with version 8.0).
     * 
     * @param op
     * 
     * @throws FilterEvaluationException
     */
    protected void buildWhere( PropertyIsLike op )
                            throws FilterEvaluationException {

        String literal = op.getLiteral().getValue().toString();
        String escape = "" + op.getEscapeChar();
        String wildCard = "" + op.getWildCard();
        String singleChar = "" + op.getSingleChar();

        IsLikeString specialString = new IsLikeString( literal, wildCard, singleChar, escape );
        // TODO lowerCasing?
        String sqlEncoded = specialString.toSQL( !op.getMatchCase() );

        // if isMatchCase == false surround first argument with LOWER (...) and convert characters
        // in second argument to lower case
        if ( op.getMatchCase() ) {
            buildWhere( op.getPropertyName() );
        } else {
            whereClause.append( "LOWER(" );
            buildWhere( op.getPropertyName() );
            whereClause.append( ')' );
        }
    }

    /**
     * @param op
     * @throws FilterEvaluationException
     */
    protected void buildWhere( LogicalOperator op )
                            throws FilterEvaluationException {
        switch ( op.getSubType() ) {
        case AND: {
            whereClause.append( "(" );
            buildWhere( op.getParams()[0] );
            whereClause.append( ")" );
            for ( int i = 1; i < op.getParams().length; i++ ) {
                whereClause.append( " AND (" );
                buildWhere( op.getParams()[i] );
                whereClause.append( ")" );
            }
            break;
        }
        case OR: {
            whereClause.append( "(" );
            buildWhere( op.getParams()[0] );
            whereClause.append( ")" );
            for ( int i = 1; i < op.getParams().length; i++ ) {
                whereClause.append( " OR (" );
                buildWhere( op.getParams()[i] );
                whereClause.append( ")" );
            }
            break;
        }
        case NOT: {
            whereClause.append( "NOT (" );
            buildWhere( op.getParams()[0] );
            whereClause.append( ")" );
            break;
        }
        }
    }

    protected abstract void buildWhere( SpatialOperator op )
                            throws FilterEvaluationException;

    protected abstract void buildWhere( PropertyName propName )
                            throws FilterEvaluationException;

    protected abstract void buildWhere( Geometry geometry, PropertyName correspondingProp )
                            throws FilterEvaluationException;

    protected abstract void buildWhere( Expression expr, boolean lowerCase, PropertyName correspondingProp )
                            throws FilterEvaluationException;

    protected abstract void buildOrderBy( SortProperty[] sortCrits )
                            throws FilterEvaluationException;
}
