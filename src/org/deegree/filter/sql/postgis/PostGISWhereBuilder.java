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
package org.deegree.filter.sql.postgis;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.deegree.feature.persistence.postgis.TypeMangler;
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
import org.deegree.filter.spatial.BBOX;
import org.deegree.filter.spatial.SpatialOperator;
import org.deegree.filter.sql.islike.IsLikeString;
import org.deegree.geometry.Geometry;

/**
 * Creates SQL-WHERE clauses from {@link Filter} expressions (to restrict SQL <code>ResultSet</code>s to rows that
 * contain objects that match a given filter). Also handles the creation of ORDER BY clauses.
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
public class PostGISWhereBuilder {

    private final PostGISMapping mapping;

    private final OperatorFilter filter;

    private final SortProperty[] sortCrit;

    private OperatorFilter postFilter;

    private SortProperty[] postSortCrit;

    private StringBuilder whereClause = new StringBuilder();

    private Collection<Object> whereParams = new ArrayList<Object>();

    private StringBuilder orderBy = new StringBuilder();

    /**
     * Creates a new {@link PostGISWhereBuilder} instance.
     * 
     * @param mapping
     *            provides the mapping for {@link PropertyName}s, must not be <code>null</code>
     * @param filter
     *            Filter to use for generating the WHERE clause, can be <code>null</code>
     * @param sortCrit
     *            criteria to use generating the ORDER BY clause, can be <code>null</code>
     * @throws FilterEvaluationException
     *             if the filter contains invalid {@link PropertyName}s
     */
    public PostGISWhereBuilder( PostGISMapping mapping, OperatorFilter filter, SortProperty[] sortCrit )
                            throws FilterEvaluationException {
        this.mapping = mapping;
        this.filter = filter;
        this.sortCrit = sortCrit;
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
        return orderBy;
    }

    /**
     * TODO
     * 
     * @return
     */
    public String getJoinTables() {
        return null;
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

    private void buildWhere( Operator op )
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

    private void buildWhere( ComparisonOperator op )
                            throws FilterEvaluationException {
        switch ( op.getSubType() ) {
        case PROPERTY_IS_BETWEEN: {
            PropertyIsBetween propIsBetween = (PropertyIsBetween) op;
            buildWhere( propIsBetween.getUpperBoundary(), !propIsBetween.getMatchCase() );
            whereClause.append( ">=" );
            buildWhere( propIsBetween.getExpression(), !propIsBetween.getMatchCase() );
            whereClause.append( "<=" );
            buildWhere( propIsBetween.getLowerBoundary(), !propIsBetween.getMatchCase() );
            break;
        }
        case PROPERTY_IS_EQUAL_TO: {
            PropertyIsEqualTo propIsEqualTo = (PropertyIsEqualTo) op;
            buildWhere( propIsEqualTo.getParameter1(), !propIsEqualTo.getMatchCase() );
            whereClause.append( "=" );
            buildWhere( propIsEqualTo.getParameter2(), !propIsEqualTo.getMatchCase() );
            break;
        }
        case PROPERTY_IS_GREATER_THAN: {
            PropertyIsGreaterThan propIsGT = (PropertyIsGreaterThan) op;
            buildWhere( propIsGT.getParameter1(), !propIsGT.getMatchCase() );
            whereClause.append( ">" );
            buildWhere( propIsGT.getParameter2(), !propIsGT.getMatchCase() );
            break;
        }
        case PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO: {
            PropertyIsGreaterThanOrEqualTo propIsGTOrEqualTo = (PropertyIsGreaterThanOrEqualTo) op;
            buildWhere( propIsGTOrEqualTo.getParameter1(), !propIsGTOrEqualTo.getMatchCase() );
            whereClause.append( ">=" );
            buildWhere( propIsGTOrEqualTo.getParameter2(), !propIsGTOrEqualTo.getMatchCase() );
            break;
        }
        case PROPERTY_IS_LESS_THAN: {
            PropertyIsLessThan propIsLT = (PropertyIsLessThan) op;
            buildWhere( propIsLT.getParameter1(), !propIsLT.getMatchCase() );
            whereClause.append( "<" );
            buildWhere( propIsLT.getParameter2(), !propIsLT.getMatchCase() );
            break;
        }
        case PROPERTY_IS_LESS_THAN_OR_EQUAL_TO: {
            PropertyIsLessThanOrEqualTo propIsLTOrEqualTo = (PropertyIsLessThanOrEqualTo) op;
            buildWhere( propIsLTOrEqualTo.getParameter1(), !propIsLTOrEqualTo.getMatchCase() );
            whereClause.append( "<=" );
            buildWhere( propIsLTOrEqualTo.getParameter2(), !propIsLTOrEqualTo.getMatchCase() );
            break;
        }
        case PROPERTY_IS_LIKE: {
            buildWhere( (PropertyIsLike) op );
            break;
        }
        case PROPERTY_IS_NOT_EQUAL_TO: {
            PropertyIsNotEqualTo propIsNotEqualTo = (PropertyIsNotEqualTo) op;
            buildWhere( propIsNotEqualTo.getParameter1(), !propIsNotEqualTo.getMatchCase() );
            whereClause.append( "<>" );
            buildWhere( propIsNotEqualTo.getParameter2(), !propIsNotEqualTo.getMatchCase() );
            break;
        }
        case PROPERTY_IS_NULL: {
            PropertyIsNull propIsNull = (PropertyIsNull) op;
            buildWhere( propIsNull.getPropertyName(), false );
            whereClause.append( " IS NULL" );
            break;
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
    private void buildWhere( PropertyIsLike op )
                            throws FilterEvaluationException {

        String literal = op.getLiteral().getValue().toString();
        String escape = "" + op.getEscapeChar();
        String wildCard = "" + op.getWildCard();
        String singleChar = "" + op.getSingleChar();

        IsLikeString specialString = new IsLikeString( literal, wildCard, singleChar, escape );
        // TODO lowerCasing?
        String sqlEncoded = specialString.toSQL(!op.getMatchCase());

        // if isMatchCase == false surround first argument with LOWER (...) and convert characters
        // in second argument to lower case
        if ( op.getMatchCase() ) {
            buildWhere( op.getPropertyName() );
        } else {
            whereClause.append( "LOWER(" );
            buildWhere( op.getPropertyName() );
            whereClause.append( ')' );
        }

        whereClause.append( "::TEXT LIKE '" );
        whereClause.append( sqlEncoded );
        whereClause.append( "'" );
    }

    private void buildWhere( LogicalOperator op )
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

    private void buildWhere( SpatialOperator op )
                            throws FilterEvaluationException {
        switch ( op.getSubType() ) {
        case BBOX: {
            BBOX bbox = (BBOX) op;
            buildWhere( bbox.getPropertyName() );
            whereClause.append( " && " );
            try {
                buildWhere( bbox.getBoundingBox() );
            } catch ( SQLException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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

    private void buildWhere( PropertyName propName )
                            throws FilterEvaluationException {

        PropertyNameMapping propMapping = mapping.getMapping( propName );
        if ( propMapping != null ) {
            whereClause.append( propMapping.getTable() );
            whereClause.append( '.' );
            whereClause.append( propMapping.getColumn() );
        } else {
            // TODO propagate information that no mapping is possible
        }
    }

    private void buildWhere( Geometry geometry )
                            throws SQLException {
        whereClause.append( "GeomFromWKB(?,-1)" );
        whereParams.add( TypeMangler.toPostGIS( geometry ) );
    }

    private void buildWhere( Expression expr, boolean lowerCase )
                            throws FilterEvaluationException {
        switch ( expr.getType() ) {
        case ADD: {
            whereClause.append( "(" );
            buildWhere( expr.getParams()[0], false );
            whereClause.append( "+" );
            buildWhere( expr.getParams()[1], false );
            whereClause.append( ")" );
            break;
        }
        case DIV: {
            whereClause.append( "(" );
            buildWhere( expr.getParams()[0], false );
            whereClause.append( "/" );
            buildWhere( expr.getParams()[1], false );
            whereClause.append( ")" );
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
            whereParams.add( ( (Literal<?>) expr ).getValue().toString() );
            break;
        }
        case MUL: {
            whereClause.append( "(" );
            buildWhere( expr.getParams()[0], false );
            whereClause.append( "*" );
            buildWhere( expr.getParams()[1], false );
            whereClause.append( ")" );
            break;
        }
        case PROPERTY_NAME: {
            PropertyNameMapping propMapping = mapping.getMapping( (PropertyName) expr );
            if ( propMapping != null ) {
                whereClause.append( propMapping.getTable() );
                whereClause.append( '.' );
                whereClause.append( propMapping.getColumn() );
            }
            break;
        }
        case SUB: {
            whereClause.append( "(" );
            buildWhere( expr.getParams()[0], false );
            whereClause.append( "-" );
            buildWhere( expr.getParams()[1], false );
            whereClause.append( ")" );
            break;
        }
        }
    }

    private void buildOrderBy( SortProperty[] sortCrits )
                            throws FilterEvaluationException {

        for ( SortProperty sortCrit : sortCrits ) {
            PropertyNameMapping propMapping = mapping.getMapping( sortCrit.getSortProperty() );
            if ( propMapping == null ) {
                postSortCrit = sortCrits;
                continue;
            }
            if ( orderBy.length() > 0 ) {
                orderBy.append( ',' );
            }
            orderBy.append( propMapping.getTable() );
            orderBy.append( '.' );
            orderBy.append( propMapping.getColumn() );
            if ( sortCrit.getSortOrder() ) {
                orderBy.append( " ASC" );
            } else {
                orderBy.append( " DESC" );
            }
        }
    }
}
