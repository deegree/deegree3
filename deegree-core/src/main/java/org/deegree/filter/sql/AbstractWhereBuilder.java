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

import static java.sql.Types.BOOLEAN;

import java.sql.ResultSet;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.deegree.commons.utils.time.DateUtils;
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
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.logical.LogicalOperator;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.spatial.SpatialOperator;
import org.deegree.filter.sql.expression.SQLExpression;
import org.deegree.filter.sql.expression.SQLLiteral;
import org.deegree.filter.sql.expression.SQLOperation;
import org.deegree.filter.sql.expression.SQLOperationBuilder;
import org.deegree.filter.sql.islike.IsLikeString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for creating SQL predicates from {@link Filter} expressions. Such an expression restricts an SQL
 * <code>ResultSet</code> to those rows that contain objects that match the given filter. Also handles the creation of
 * ORDER BY clauses.
 * <p>
 * Note that the generated WHERE and ORDER-BY expressions are sometimes not sufficient to guarantee that the
 * <code>ResultSet</code> only contains the targeted objects and/or keeps the requested order. This happens when the
 * {@link PropertyName}s used in the filter/sort criteria are not mappable to columns in the database or the contained
 * XPath expressions are not mappable to an equivalent SQL expression. In these cases, one or both of the methods
 * {@link #getPostFilter()}/{@link #getPostSortCriteria()} return not <code>null</code> and the objects extracted from
 * the corresponding {@link ResultSet} must be filtered/sorted in memory to guarantee the requested constraints/order.
 * </p>
 * <p>
 * TODO: Implement partial backend filtering / sorting. Currently, filtering / sorting is performed completely by the
 * database <i>or</i> by the post filter / criteria (if any property name has been encountered that could not be
 * mapped).
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class AbstractWhereBuilder {

    private static final Logger LOG = LoggerFactory.getLogger( AbstractWhereBuilder.class );

    /** Keeps track of all generated table aliases. */
    protected final TableAliasManager aliasManager = new TableAliasManager();

    /** Keeps track of all successfully mapped property names. */
    protected final List<PropertyNameMapping> propNameMappingList = new ArrayList<PropertyNameMapping>();

    private final OperatorFilter filter;

    private final SortProperty[] sortCrit;

    private SQLExpression whereClause;

    private SQLExpression orderByClause;

    private OperatorFilter postFilter;

    private SortProperty[] postSortCrit;

    /**
     * Creates a new {@link AbstractWhereBuilder} instance.
     * 
     * @param filter
     *            Filter to use for generating the WHERE clause, can be <code>null</code>
     * @param sortCrit
     *            criteria to use for generating the ORDER-BY clause, can be <code>null</code>
     * @throws FilterEvaluationException
     *             if the filter contains invalid {@link PropertyName}s
     */
    protected AbstractWhereBuilder( OperatorFilter filter, SortProperty[] sortCrit ) throws FilterEvaluationException {
        this.filter = filter;
        this.sortCrit = sortCrit;
    }

    /**
     * Invokes the building of the internal variables that store filter and sort criteria.
     * 
     * @throws FilterEvaluationException
     */
    protected void build()
                            throws FilterEvaluationException {
        if ( filter != null ) {
            try {
                whereClause = toProtoSQL( filter.getOperator() );
            } catch ( UnmappableException e ) {
                LOG.debug( "Unable to map filter to WHERE-clause. Setting post filter.", e );
                LOG.warn( "Using full filter for post filtering step. Partial backend-filtering is not implemented yet. " );
                postFilter = filter;
            } catch ( FilterEvaluationException e ) {
                throw e;
            } catch ( RuntimeException e ) {
                LOG.error( e.getMessage(), e );
                throw e;
            }
        }
        if ( sortCrit != null && sortCrit.length != 0 ) {
            try {
                orderByClause = toProtoSQL( sortCrit );
            } catch ( UnmappableException e ) {
                LOG.debug( "Unable to map sort criteria to ORDER BY-clause. Setting post order criteria.", e );
                LOG.warn( "Using all sort criteria for post sorting step. Partial backend-sorting is not implemented yet. " );
                postSortCrit = sortCrit;
            } catch ( FilterEvaluationException e ) {
                throw e;
            } catch ( RuntimeException e ) {
                LOG.error( e.getMessage(), e );
                throw e;
            }
        }
    }

    /**
     * Returns the expression for the SQL-WHERE clause.
     * 
     * @return the WHERE clause, can be <code>null</code>
     */
    public SQLExpression getWhere() {
        return whereClause;
    }

    /**
     * Returns the expression for the SQL-ORDER-BY clause.
     * 
     * @return the ORDER-BY clause, can be <code>null</code>
     */
    public SQLExpression getOrderBy() {
        return orderByClause;
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
     * ORDER-BY clause.
     * 
     * @return sort criteria to apply on the objects from the <code>ResultSet</code>, may be <code>null</code> (no
     *         post-sorting necessary)
     */
    public SortProperty[] getPostSortCriteria() {
        return postSortCrit;
    }

    /**
     * Returns the {@link TableAliasManager} that keeps track of the used table aliases.
     * <p>
     * The returned manager may also be used for generating additional aliases that are needed for creating the final
     * SQL statement.
     * </p>
     * 
     * @return the table alias manager, never <code>null</code>
     */
    public TableAliasManager getAliasManager() {
        return aliasManager;
    }

    /**
     * Returns the mappings of all {@link PropertyName}s from the filter / sort criteria that have been mapped to the
     * relational model.
     * 
     * @return the successful mappings, can be empty but never <code>null</code>
     */
    public List<PropertyNameMapping> getMappedPropertyNames() {
        return propNameMappingList;
    }

    /**
     * Translates the given {@link Operator} into an {@link SQLExpression}.
     * 
     * @param op
     *            operator to be translated, must not be <code>null</code>
     * @return corresponding SQL expression, never <code>null</code>
     * @throws UnmappableException
     *             if translation is not possible (usually due to unmappable property names)
     * @throws FilterEvaluationException
     *             if the filter contains invalid {@link PropertyName}s
     */
    protected SQLExpression toProtoSQL( Operator op )
                            throws UnmappableException, FilterEvaluationException {

        SQLExpression sql = null;
        switch ( op.getType() ) {
        case COMPARISON: {
            sql = toProtoSQL( (ComparisonOperator) op );
            break;
        }
        case LOGICAL: {
            sql = toProtoSQL( (LogicalOperator) op );
            break;
        }
        case SPATIAL: {
            sql = toProtoSQL( (SpatialOperator) op );
            break;
        }
        }
        return sql;
    }

    /**
     * Translates the given {@link ComparisonOperator} into an {@link SQLExpression}.
     * 
     * @param op
     *            comparison operator to be translated, must not be <code>null</code>
     * @return corresponding SQL expression, never <code>null</code>
     * @throws UnmappableException
     *             if translation is not possible (usually due to unmappable property names)
     * @throws FilterEvaluationException
     *             if the filter contains invalid {@link PropertyName}s
     */
    protected SQLExpression toProtoSQL( ComparisonOperator op )
                            throws UnmappableException, FilterEvaluationException {

        SQLOperation sqlOper = null;

        switch ( op.getSubType() ) {
        case PROPERTY_IS_BETWEEN: {
            PropertyIsBetween propIsBetween = (PropertyIsBetween) op;
            SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );
            builder.add( "(" );
            builder.add( toProtoSQL( propIsBetween.getLowerBoundary() ) );
            builder.add( " <= " );
            builder.add( toProtoSQL( propIsBetween.getExpression() ) );
            builder.add(" AND ");
            builder.add( toProtoSQL( propIsBetween.getExpression() ) );
            builder.add( " <= " );
            builder.add( toProtoSQL( propIsBetween.getUpperBoundary() ) );
            builder.add( ")" );
            sqlOper = builder.toOperation();
            break;
        }
        case PROPERTY_IS_EQUAL_TO: {
            PropertyIsEqualTo propIsEqualTo = (PropertyIsEqualTo) op;
            SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );
            builder.add( toProtoSQL( propIsEqualTo.getParameter1() ) );
            builder.add( " = " );
            builder.add( toProtoSQL( propIsEqualTo.getParameter2() ) );
            sqlOper = builder.toOperation();
            break;
        }
        case PROPERTY_IS_GREATER_THAN: {
            PropertyIsGreaterThan propIsGT = (PropertyIsGreaterThan) op;
            SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );
            builder.add( toProtoSQL( propIsGT.getParameter1() ) );
            builder.add( " > " );
            builder.add( toProtoSQL( propIsGT.getParameter2() ) );
            sqlOper = builder.toOperation();
            break;
        }
        case PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO: {
            PropertyIsGreaterThanOrEqualTo propIsGTOrEqualTo = (PropertyIsGreaterThanOrEqualTo) op;
            SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );
            builder.add( toProtoSQL( propIsGTOrEqualTo.getParameter1() ) );
            builder.add( " >= " );
            builder.add( toProtoSQL( propIsGTOrEqualTo.getParameter2() ) );
            sqlOper = builder.toOperation();
            break;
        }
        case PROPERTY_IS_LESS_THAN: {
            PropertyIsLessThan propIsLT = (PropertyIsLessThan) op;
            SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );
            builder.add( toProtoSQL( propIsLT.getParameter1() ) );
            builder.add( " < " );
            builder.add( toProtoSQL( propIsLT.getParameter2() ) );
            sqlOper = builder.toOperation();
            break;
        }
        case PROPERTY_IS_LESS_THAN_OR_EQUAL_TO: {
            PropertyIsLessThanOrEqualTo propIsLTOrEqualTo = (PropertyIsLessThanOrEqualTo) op;
            SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );
            builder.add( toProtoSQL( propIsLTOrEqualTo.getParameter1() ) );
            builder.add( " <= " );
            builder.add( toProtoSQL( propIsLTOrEqualTo.getParameter2() ) );
            sqlOper = builder.toOperation();
            break;
        }
        case PROPERTY_IS_LIKE: {
            sqlOper = toProtoSQL( (PropertyIsLike) op );
            break;
        }
        case PROPERTY_IS_NOT_EQUAL_TO: {
            PropertyIsNotEqualTo propIsNotEqualTo = (PropertyIsNotEqualTo) op;
            SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );
            builder.add( toProtoSQL( propIsNotEqualTo.getParameter1() ) );
            builder.add( " <> " );
            builder.add( toProtoSQL( propIsNotEqualTo.getParameter2() ) );
            sqlOper = builder.toOperation();
            break;
        }
        case PROPERTY_IS_NULL: {
            PropertyIsNull propIsNull = (PropertyIsNull) op;
            SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );
            builder.add( toProtoSQL( propIsNull.getPropertyName() ) );
            builder.add( " IS NULL" );
            sqlOper = builder.toOperation();
            break;
        }
        }
        return sqlOper;
    }

    /**
     * Translates the given {@link PropertyIsLike} into an {@link SQLOperation}.
     * 
     * @param op
     *            comparison operator to be translated, must not be <code>null</code>
     * @return corresponding SQL expression, never <code>null</code>
     * @throws UnmappableException
     *             if translation is not possible (usually due to unmappable property names)
     * @throws FilterEvaluationException
     *             if the filter contains invalid {@link PropertyName}s
     */
    protected SQLOperation toProtoSQL( PropertyIsLike op )
                            throws UnmappableException, FilterEvaluationException {

        String literal = op.getLiteral().getValue().toString();
        String escape = "" + op.getEscapeChar();
        String wildCard = "" + op.getWildCard();
        String singleChar = "" + op.getSingleChar();

        IsLikeString specialString = new IsLikeString( literal, wildCard, singleChar, escape );
        // TODO lowerCasing?
        String sqlEncoded = specialString.toSQL( !op.getMatchCase() );

        SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );
        builder.add( toProtoSQL( op.getPropertyName() ) );
        builder.add( " LIKE " );
        builder.add( new SQLLiteral( sqlEncoded, Types.VARCHAR ) );

        return builder.toOperation();
    }

    /**
     * Translates the given {@link LogicalOperator} into an {@link SQLOperation}.
     * 
     * @param op
     *            logical operator to be translated, must not be <code>null</code>
     * @return corresponding SQL expression, never <code>null</code>
     * @throws UnmappableException
     *             if translation is not possible (usually due to unmappable property names)
     * @throws FilterEvaluationException
     *             if the filter contains invalid {@link PropertyName}s
     */
    protected SQLOperation toProtoSQL( LogicalOperator op )
                            throws UnmappableException, FilterEvaluationException {

        SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );

        switch ( op.getSubType() ) {
        case AND: {
            builder.add( "(" );
            builder.add( toProtoSQL( op.getParams()[0] ) );
            for ( int i = 1; i < op.getParams().length; i++ ) {
                builder.add( " AND " );
                builder.add( toProtoSQL( op.getParams()[i] ) );
            }
            builder.add( ")" );
            break;
        }
        case OR: {
            builder.add( "(" );
            builder.add( toProtoSQL( op.getParams()[0] ) );
            for ( int i = 1; i < op.getParams().length; i++ ) {
                builder.add( " OR " );
                builder.add( toProtoSQL( op.getParams()[i] ) );
            }
            builder.add( ")" );
            break;
        }
        case NOT: {
            builder.add( "NOT (" );
            builder.add( toProtoSQL( op.getParams()[0] ) );
            builder.add( ")" );
            break;
        }
        }
        return builder.toOperation();
    }

    /**
     * Translates the given {@link SpatialOperator} into an {@link SQLOperation}.
     * 
     * @param op
     *            spatial operator to be translated, must not be <code>null</code>
     * @return corresponding SQL expression, never <code>null</code>
     * @throws UnmappableException
     *             if translation is not possible (usually due to unmappable property names)
     * @throws FilterEvaluationException
     *             if the filter contains invalid {@link PropertyName}s
     */
    protected abstract SQLOperation toProtoSQL( SpatialOperator op )
                            throws UnmappableException, FilterEvaluationException;

    /**
     * Translates the given {@link Expression} into an {@link SQLExpression}.
     * 
     * @param expr
     *            expression to be translated, must not be <code>null</code>
     * @return corresponding SQL expression, never <code>null</code>
     * @throws UnmappableException
     *             if translation is not possible (usually due to unmappable property names)
     * @throws FilterEvaluationException
     *             if the filter contains invalid {@link PropertyName}s
     */
    protected SQLExpression toProtoSQL( Expression expr )
                            throws UnmappableException, FilterEvaluationException {

        SQLExpression sql = null;

        switch ( expr.getType() ) {
        case ADD: {
            SQLOperationBuilder builder = new SQLOperationBuilder();
            builder.add( "(" );
            builder.add( toProtoSQL( expr.getParams()[0] ) );
            builder.add( "+" );
            builder.add( toProtoSQL( expr.getParams()[1] ) );
            builder.add( ")" );
            sql = builder.toOperation();
            break;
        }
        case DIV: {
            SQLOperationBuilder builder = new SQLOperationBuilder();
            builder.add( "(" );
            builder.add( toProtoSQL( expr.getParams()[0] ) );
            builder.add( "/" );
            builder.add( toProtoSQL( expr.getParams()[1] ) );
            builder.add( ")" );
            sql = builder.toOperation();
            break;
        }
        case FUNCTION: {
            String msg = "Translating of functions into SQL-WHERE constraints is not implemented.";
            LOG.warn( msg );
            throw new UnmappableException( msg );
        }
        case LITERAL: {
            sql = toProtoSQL( (Literal<?>) expr );
            break;
        }
        case MUL: {
            SQLOperationBuilder builder = new SQLOperationBuilder();
            builder.add( "(" );
            builder.add( toProtoSQL( expr.getParams()[0] ) );
            builder.add( "*" );
            builder.add( toProtoSQL( expr.getParams()[1] ) );
            builder.add( ")" );
            sql = builder.toOperation();
            break;
        }
        case PROPERTY_NAME: {
            sql = toProtoSQL( (PropertyName) expr );
            break;
        }
        case SUB: {
            SQLOperationBuilder builder = new SQLOperationBuilder();
            builder.add( "(" );
            builder.add( toProtoSQL( expr.getParams()[0] ) );
            builder.add( "-" );
            builder.add( toProtoSQL( expr.getParams()[1] ) );
            builder.add( ")" );
            sql = builder.toOperation();
            break;
        }
        }
        return sql;
    }

    /**
     * Translates the given {@link Literal} into an {@link SQLExpression}. <br>
     * TODO be aware: if the literal can be parsed as a date from {@link DateUtils} the literals sqlType will be set to
     * TIMESTAMP.
     * 
     * @param literal
     *            literal to be translated, must not be <code>null</code>
     * @return corresponding SQL expression, never <code>null</code>
     * @throws UnmappableException
     *             if translation is not possible (usually due to unmappable property names)
     * @throws FilterEvaluationException
     *             if the filter contains invalid {@link PropertyName}s
     */
    protected SQLExpression toProtoSQL( Literal<?> literal )
                            throws UnmappableException, FilterEvaluationException {
        int javaType = -1;
        Date date;
        try {
            date = DateUtils.parseISO8601Date( literal.getValue().toString() );

        } catch ( ParseException e ) {
            date = null;
        }
        if ( date != null ) {
            javaType = Types.TIMESTAMP;
            return new SQLLiteral( literal.getValue(), javaType );
        }
        if ( literal.getValue().toString().equals( "true" ) || literal.getValue().toString().equals( "false" ) ) {
            javaType = Types.BOOLEAN;
            return new SQLLiteral( literal.getValue(), javaType );
        }
        return new SQLLiteral( literal );
    }

    /**
     * Translates the given {@link PropertyName} into an {@link SQLExpression}.
     * 
     * @param expr
     *            expression to be translated, must not be <code>null</code>
     * @return corresponding SQL expression, never <code>null</code>
     * @throws UnmappableException
     *             if translation is not possible (usually due to unmappable property names)
     * @throws FilterEvaluationException
     *             if the filter contains invalid {@link PropertyName}s
     */
    protected abstract SQLExpression toProtoSQL( PropertyName expr )
                            throws UnmappableException, FilterEvaluationException;

    /**
     * Translates the given {@link SortProperty} array into an {@link SQLExpression}.
     * 
     * @param sortCrits
     *            sort criteria to be translated, must not be <code>null</code>
     * @return corresponding SQL expression, never <code>null</code>
     * @throws UnmappableException
     *             if translation is not possible (usually due to unmappable property names)
     * @throws FilterEvaluationException
     *             if the filter contains invalid {@link PropertyName}s
     */
    protected SQLExpression toProtoSQL( SortProperty[] sortCrits )
                            throws UnmappableException, FilterEvaluationException {

        SQLOperationBuilder builder = new SQLOperationBuilder();
        for ( int i = 0; i < sortCrits.length; i++ ) {
            SortProperty sortCrit = sortCrits[i];
            if ( i > 0 ) {
                builder.add( "," );
            }
            builder.add( toProtoSQL( sortCrit.getSortProperty() ) );
            if ( sortCrit.getSortOrder() ) {
                builder.add( " ASC" );
            } else {
                builder.add( " DESC" );
            }
        }
        return builder.toOperation();
    }
}
