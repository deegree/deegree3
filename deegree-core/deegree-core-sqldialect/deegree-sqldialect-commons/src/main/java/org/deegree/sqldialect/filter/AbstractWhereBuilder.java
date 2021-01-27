//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/deegree-core-base/src/main/java/org/deegree/filter/sql/AbstractWhereBuilder.java $
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
package org.deegree.sqldialect.filter;

import static java.sql.Types.BOOLEAN;
import static org.deegree.commons.tom.primitive.BaseType.STRING;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.filter.Filters.extractPrefilterBBoxConstraint;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.tom.sql.DefaultPrimitiveConverter;
import org.deegree.commons.tom.sql.PrimitiveParticleConverter;
import org.deegree.commons.utils.StringUtils;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.filter.Expression;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.MatchAction;
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
import org.deegree.filter.comparison.PropertyIsNil;
import org.deegree.filter.comparison.PropertyIsNotEqualTo;
import org.deegree.filter.comparison.PropertyIsNull;
import org.deegree.filter.expression.Function;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.logical.LogicalOperator;
import org.deegree.filter.logical.Not;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.spatial.BBOX;
import org.deegree.filter.spatial.SpatialOperator;
import org.deegree.filter.temporal.TemporalOperator;
import org.deegree.geometry.Geometry;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.filter.expression.SQLArgument;
import org.deegree.sqldialect.filter.expression.SQLColumn;
import org.deegree.sqldialect.filter.expression.SQLExpression;
import org.deegree.sqldialect.filter.expression.SQLOperation;
import org.deegree.sqldialect.filter.expression.SQLOperationBuilder;
import org.deegree.sqldialect.filter.function.SQLFunctionManager;
import org.deegree.sqldialect.filter.function.SQLFunctionProvider;
import org.deegree.sqldialect.filter.islike.IsLikeString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for creating SQL predicates from {@link Filter} expressions. Such an expression restricts an SQL
 * <code>ResultSet</code> to those rows that contain objects that match the given filter. Also handles the creation of
 * ORDER BY clauses.
 * <p>
 * Note that the generated WHERE and ORDER-BY expressions are sometimes not sufficient to guarantee that a
 * <code>ResultSet</code> only contains the targeted objects and/or keeps the requested order. This happens when the
 * {@link ValueReference}s used in the filter/sort criteria are not mappable to columns in the database or the contained
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
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31370 $, $Date: 2011-07-28 19:37:13 +0200 (Do, 28. Jul 2011) $
 */
public abstract class AbstractWhereBuilder {

    private static final Logger LOG = LoggerFactory.getLogger( AbstractWhereBuilder.class );

    protected final SQLDialect dialect;

    protected final PropertyNameMapper mapper;

    protected final OperatorFilter filter;

    protected final SortProperty[] sortCrit;

    /** Keeps track of all generated table aliases. */
    protected final TableAliasManager aliasManager = new TableAliasManager();

    /** Keeps track of all successfully mapped property names. */
    protected final List<PropertyNameMapping> propNameMappingList = new ArrayList<PropertyNameMapping>();

    protected SQLExpression whereClause;

    protected SQLExpression orderByClause;

    protected OperatorFilter postFilter;

    protected SortProperty[] postSortCrit;

    /**
     * Creates a new {@link AbstractWhereBuilder} instance.
     * 
     * @param dialect
     *            SQL dialect, can be <code>null</code> (TODO refactor code, so not null is always used)
     * @param mapper
     *            provides the mapping from {@link ValueReference}s to DB columns, must not be <code>null</code>
     * @param filter
     *            Filter to use for generating the WHERE clause, can be <code>null</code>
     * @param sortCrit
     *            criteria to use for generating the ORDER-BY clause, can be <code>null</code>
     * @throws FilterEvaluationException
     *             if the filter contains invalid {@link ValueReference}s
     */
    protected AbstractWhereBuilder( SQLDialect dialect, PropertyNameMapper mapper, OperatorFilter filter,
                                    SortProperty[] sortCrit ) throws FilterEvaluationException {
        this.dialect = dialect;
        this.mapper = mapper;
        this.filter = filter;
        this.sortCrit = sortCrit;
    }

    /**
     * Invokes the building of the internal variables that store filter and sort criteria.
     * 
     * @param allowPartialMappings
     *            if false, any unmappable expression will cause an {@link UnmappableException} to be thrown
     * @throws FilterEvaluationException
     * @throws UnmappableException
     *             if allowPartialMappings is false and an expression could not be mapped to the db
     */
    protected void build( boolean allowPartialMappings )
                            throws FilterEvaluationException, UnmappableException {

        if ( filter != null ) {
            try {
                whereClause = toProtoSQL( filter.getOperator() );
            } catch ( UnmappableException e ) {
                if ( !allowPartialMappings ) {
                    throw e;
                }
                LOG.debug( "Unable to map full filter to WHERE-clause. Trying mapping of bbox constraint only." );
                LOG.trace( "Stack trace:", e );

                BBOX preFilterBBox = extractPrefilterBBoxConstraint( filter );
                if ( preFilterBBox != null ) {
                    try {
                        whereClause = toProtoSQL( preFilterBBox );
                    } catch ( UnmappableException e2 ) {
                        LOG.warn( "Unable to map any filter constraints to WHERE-clause. Fallback to full memory filtering." );
                        LOG.trace( "Stack trace:", e2 );
                    }
                }
                postFilter = filter;
            } catch ( FilterEvaluationException e ) {
                throw e;
            } catch ( InvalidParameterValueException e ) {
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
                if ( !allowPartialMappings ) {
                    throw e;
                }
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
     * Returns the mappings of all {@link ValueReference}s from the filter / sort criteria that have been mapped to the
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
     *             if the filter contains invalid {@link ValueReference}s
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
        case TEMPORAL: {
            sql = toProtoSQL( (TemporalOperator) op );
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
     *             if the filter contains invalid {@link ValueReference}s
     */
    protected SQLExpression toProtoSQL( ComparisonOperator op )
                            throws UnmappableException, FilterEvaluationException {

        SQLOperation sqlOper = null;

        if ( op.getMatchAction() != null && op.getMatchAction() != MatchAction.ANY ) {
            LOG.warn( "Mapping of operators with matchAction=" + op.getMatchAction()
                      + " to SQL is not implemented (and probably not possible)." );
        }

        switch ( op.getSubType() ) {
        case PROPERTY_IS_BETWEEN: {
            PropertyIsBetween propIsBetween = (PropertyIsBetween) op;
            SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );
            SQLExpression lower = toProtoSQL( propIsBetween.getLowerBoundary(), true );
            SQLExpression expr = toProtoSQL( propIsBetween.getExpression(), true );
            SQLExpression upper = toProtoSQL( propIsBetween.getUpperBoundary(), true );
            inferType( lower, expr, upper );
            builder.add( "(" );
            addExpression( builder, lower, op.isMatchCase() );
            builder.add( " <= " );
            addExpression( builder, expr, op.isMatchCase() );
            builder.add( " AND " );
            addExpression( builder, expr, op.isMatchCase() );
            builder.add( " <= " );
            addExpression( builder, upper, op.isMatchCase() );
            builder.add( ")" );
            sqlOper = builder.toOperation();
            break;
        }
        case PROPERTY_IS_EQUAL_TO: {
            PropertyIsEqualTo propIsEqualTo = (PropertyIsEqualTo) op;
            SQLExpression param1 = toProtoSQL( propIsEqualTo.getParameter1() );
            SQLExpression param2 = toProtoSQL( propIsEqualTo.getParameter2() );
            if ( !param1.isMultiValued() && !param2.isMultiValued() ) {
                inferType( param1, param2 );
                SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );
                addExpression( builder, param1, op.isMatchCase() );
                builder.add( " = " );
                addExpression( builder, param2, op.isMatchCase() );
                sqlOper = builder.toOperation();
            } else {
                Expression propName = propIsEqualTo.getParameter1();
                Expression literal = propIsEqualTo.getParameter2();
                if ( propName instanceof ValueReference && literal instanceof Literal ) {
                    PropertyIsLike propIsLike = buildIsLike( propName, literal, propIsEqualTo.isMatchCase() );
                    sqlOper = toProtoSQL( propIsLike );
                } else {
                    String msg = "Can not map filter. Multi-valued columns can only be compared to literals.";
                    throw new UnmappableException( msg );
                }
            }
            break;
        }
        case PROPERTY_IS_GREATER_THAN: {
            PropertyIsGreaterThan propIsGT = (PropertyIsGreaterThan) op;
            SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );
            SQLExpression param1 = toProtoSQL( propIsGT.getParameter1(), true );
            SQLExpression param2 = toProtoSQL( propIsGT.getParameter2(), true );
            inferType( param1, param2 );
            addExpression( builder, param1, op.isMatchCase() );
            builder.add( " > " );
            addExpression( builder, param2, op.isMatchCase() );
            sqlOper = builder.toOperation();
            break;
        }
        case PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO: {
            PropertyIsGreaterThanOrEqualTo propIsGTOrEqualTo = (PropertyIsGreaterThanOrEqualTo) op;
            SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );
            SQLExpression param1 = toProtoSQL( propIsGTOrEqualTo.getParameter1(), true );
            SQLExpression param2 = toProtoSQL( propIsGTOrEqualTo.getParameter2(), true );
            inferType( param1, param2 );
            addExpression( builder, param1, op.isMatchCase() );
            builder.add( " >= " );
            addExpression( builder, param2, op.isMatchCase() );
            sqlOper = builder.toOperation();
            break;
        }
        case PROPERTY_IS_LESS_THAN: {
            PropertyIsLessThan propIsLT = (PropertyIsLessThan) op;
            SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );
            SQLExpression param1 = toProtoSQL( propIsLT.getParameter1(), true );
            SQLExpression param2 = toProtoSQL( propIsLT.getParameter2(), true );
            inferType( param1, param2 );
            addExpression( builder, param1, op.isMatchCase() );
            builder.add( " < " );
            addExpression( builder, param2, op.isMatchCase() );
            sqlOper = builder.toOperation();
            break;
        }
        case PROPERTY_IS_LESS_THAN_OR_EQUAL_TO: {
            PropertyIsLessThanOrEqualTo propIsLTOrEqualTo = (PropertyIsLessThanOrEqualTo) op;
            SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );
            SQLExpression param1 = toProtoSQL( propIsLTOrEqualTo.getParameter1(), true );
            SQLExpression param2 = toProtoSQL( propIsLTOrEqualTo.getParameter2(), true );
            inferType( param1, param2 );
            addExpression( builder, param1, op.isMatchCase() );
            builder.add( " <= " );
            addExpression( builder, param2, op.isMatchCase() );
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
            SQLExpression param1 = toProtoSQL( propIsNotEqualTo.getParameter1(), true );
            SQLExpression param2 = toProtoSQL( propIsNotEqualTo.getParameter2(), true );
            if ( !param1.isMultiValued() && !param2.isMultiValued() ) {
                inferType( param1, param2 );
                addExpression( builder, param1, op.isMatchCase() );
                builder.add( " <> " );
                addExpression( builder, param2, op.isMatchCase() );
                sqlOper = builder.toOperation();
            } else {
                Expression propName = propIsNotEqualTo.getParameter1();
                Expression literal = propIsNotEqualTo.getParameter2();
                if ( propName instanceof ValueReference && literal instanceof Literal ) {
                    PropertyIsLike propIsLike = buildIsLike( propName, literal, propIsNotEqualTo.isMatchCase() );
                    sqlOper = toProtoSQL( new Not( propIsLike ) );
                } else {
                    String msg = "Can not map filter. Multi-valued columns can only be compared to literals.";
                    throw new UnmappableException( msg );
                }
            }
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
        case PROPERTY_IS_NIL: {
            PropertyIsNil propIsNil = (PropertyIsNil) op;
            SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );
            Expression expr = propIsNil.getPropertyName();
            if ( !( expr instanceof ValueReference ) ) {
                String msg = "Mapping of PropertyIsNil is only supported for ValueReference expressions.";
                throw new UnmappableException( msg );
            }
            ValueReference valRef = (ValueReference) expr;
            NamespaceBindings nsContext = valRef.getNsContext();
            nsContext.addNamespace( "xsi", XSINS );
            valRef = new ValueReference( valRef.getAsText() + "/@xsi:nil", nsContext );
            // TODO what if the xsi:nil attribute is not explicitly mapped (fallback to NULL mapping?)
            builder.add( toProtoSQL( valRef ) );
            sqlOper = builder.toOperation();
            break;
        }
        }
        return sqlOper;
    }

    protected void inferType( SQLExpression expr1, SQLExpression expr2 ) {
        PrimitiveType pt1 = expr1.getPrimitiveType();
        PrimitiveType pt2 = expr2.getPrimitiveType();
        if ( pt1 == null && pt2 != null ) {
            expr1.cast( expr2 );
        } else if ( pt1 != null && pt2 == null ) {
            expr2.cast( expr1 );
        } else if ( pt1 != null && pt2 != null ) {
            if ( pt1.getBaseType() != pt2.getBaseType() ) {
                LOG.warn( "Comparison on different types (" + pt1 + "/" + pt2 + "). Relying on db type conversion." );
            }
        }
    }

    protected void inferType( SQLExpression expr1, SQLExpression expr2, SQLExpression expr3 ) {
        PrimitiveType pt1 = expr1.getPrimitiveType();
        PrimitiveType pt2 = expr2.getPrimitiveType();
        PrimitiveType pt3 = expr3.getPrimitiveType();
        if ( pt1 != null ) {
            inferType( expr1, expr2 );
            inferType( expr1, expr3 );
        } else if ( pt2 != null ) {
            inferType( expr2, expr1 );
            inferType( expr2, expr3 );
        } else if ( pt3 != null ) {
            inferType( expr3, expr1 );
            inferType( expr3, expr2 );
        }
    }

    private PropertyIsLike buildIsLike( Expression propName, Expression literal, boolean matchCase )
                            throws UnmappableException {

        if ( !( propName instanceof ValueReference ) || !( literal instanceof Literal ) ) {
            String msg = "Can not map filter. Multi-valued columns can only be compared to literals.";
            throw new UnmappableException( msg );
        }

        String wildCard = "*";
        String singleChar = "?";
        String escapeChar = "\\";
        String s = ( (Literal<?>) literal ).getValue().toString();
        s = StringUtils.replaceAll( s, escapeChar, escapeChar + escapeChar );
        s = StringUtils.replaceAll( s, singleChar, escapeChar + singleChar );
        s = StringUtils.replaceAll( s, wildCard, escapeChar + wildCard );
        Literal<PrimitiveValue> escapedLiteral = new Literal<PrimitiveValue>( new PrimitiveValue( s ), null );
        return new PropertyIsLike( (ValueReference) propName, escapedLiteral, wildCard, singleChar, escapeChar,
                                   matchCase, null );
    }

    protected void addExpression( SQLOperationBuilder builder, SQLExpression expr ) {
        addExpression( builder, expr, true );
    }

    protected void addExpression( SQLOperationBuilder builder, SQLExpression expr, Boolean matchCase ) {
        if ( matchCase == null || matchCase ) {
            builder.add( expr );
        } else {
            builder.add( "LOWER(" );
            builder.add( expr );
            builder.add( ")" );
        }
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
     *             if the filter contains invalid {@link ValueReference}s
     */
    protected SQLOperation toProtoSQL( PropertyIsLike op )
                            throws UnmappableException, FilterEvaluationException {

        if ( !( op.getPattern() instanceof Literal ) ) {
            String msg = "Mapping of PropertyIsLike with non-literal comparisons to SQL is not implemented yet.";
            throw new UnsupportedOperationException( msg );
        }

        String literal = ( (Literal) op.getPattern() ).getValue().toString();
        String escape = "" + op.getEscapeChar();
        String wildCard = "" + op.getWildCard();
        String singleChar = "" + op.getSingleChar();

        SQLExpression propName = toProtoSQL( op.getExpression() );

        IsLikeString specialString = new IsLikeString( literal, wildCard, singleChar, escape );
        String sqlEncoded = specialString.toSQL( !op.isMatchCase() );

        if ( propName.isMultiValued() ) {
            // TODO escaping of pipe symbols
            sqlEncoded = "%|" + sqlEncoded + "|%";
        }

        SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );
        if ( !op.isMatchCase() ) {
            builder.add( "LOWER (" + propName + ")" );
        } else {
            builder.add( propName );
        }
        builder.add( " LIKE " );
        PrimitiveType pt = new PrimitiveType( STRING );
        PrimitiveValue value = new PrimitiveValue( sqlEncoded, pt );
        PrimitiveParticleConverter converter = new DefaultPrimitiveConverter( pt, null, propName.isMultiValued() );
        SQLArgument argument = new SQLArgument( value, converter );
        builder.add( argument );

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
     *             if the filter contains invalid {@link ValueReference}s
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
     *             if the filter contains invalid {@link ValueReference}s
     */
    protected abstract SQLOperation toProtoSQL( SpatialOperator op )
                            throws UnmappableException, FilterEvaluationException;

    /**
     * Translates the given {@link TemporalOperator} into an {@link SQLOperation}.
     * 
     * @param op
     *            temporal operator to be translated, must not be <code>null</code>
     * @return corresponding SQL expression, may be <code>null</code>
     * @throws UnmappableException
     *             if translation is not possible (usually due to unmappable property names)
     * @throws FilterEvaluationException
     *             if the filter contains invalid {@link ValueReference}s
     */
    protected SQLOperation toProtoSQL( TemporalOperator op )
                            throws UnmappableException, FilterEvaluationException {
        LOG.warn( "Mapping of temporal operators to SQL is not implemented (and probably not possible)." );
        return null;
    }

    /**
     * Translates the given {@link Expression} into an {@link SQLExpression}.
     * 
     * @param expr
     *            expression to be translated, must not be <code>null</code>
     * @return corresponding SQL expression, never <code>null</code>
     * @throws UnmappableException
     *             if translation is not possible (usually due to unmappable property names)
     * @throws FilterEvaluationException
     *             if the filter contains invalid {@link ValueReference}s
     */
    protected SQLExpression toProtoSQL( Expression expr )
                            throws UnmappableException, FilterEvaluationException {

        SQLExpression sql = null;

        switch ( expr.getType() ) {
        case ADD: {
            SQLOperationBuilder builder = new SQLOperationBuilder();
            builder.add( "(" );
            builder.add( toProtoSQL( expr.getParams()[0], true ) );
            builder.add( "+" );
            builder.add( toProtoSQL( expr.getParams()[1], true ) );
            builder.add( ")" );
            sql = builder.toOperation();
            break;
        }
        case DIV: {
            SQLOperationBuilder builder = new SQLOperationBuilder();
            builder.add( "(" );
            builder.add( toProtoSQL( expr.getParams()[0], true ) );
            builder.add( "/" );
            builder.add( toProtoSQL( expr.getParams()[1], true ) );
            builder.add( ")" );
            sql = builder.toOperation();
            break;
        }
        case FUNCTION: {
            sql = toProtoSQL( (Function) expr );
            break;
        }
        case LITERAL: {
            sql = toProtoSQL( (Literal<?>) expr );
            break;
        }
        case MUL: {
            SQLOperationBuilder builder = new SQLOperationBuilder();
            builder.add( "(" );
            builder.add( toProtoSQL( expr.getParams()[0], true ) );
            builder.add( "*" );
            builder.add( toProtoSQL( expr.getParams()[1], true ) );
            builder.add( ")" );
            sql = builder.toOperation();
            break;
        }
        case VALUE_REFERENCE: {
            sql = toProtoSQL( (ValueReference) expr );
            break;
        }
        case SUB: {
            SQLOperationBuilder builder = new SQLOperationBuilder();
            builder.add( "(" );
            builder.add( toProtoSQL( expr.getParams()[0], true ) );
            builder.add( "-" );
            builder.add( toProtoSQL( expr.getParams()[1], true ) );
            builder.add( ")" );
            sql = builder.toOperation();
            break;
        }
        }
        return sql;
    }

    protected SQLExpression toProtoSQL( Expression expr, boolean assertNotMultiValued )
                            throws UnmappableException, FilterEvaluationException {
        SQLExpression sql = toProtoSQL( expr );
        if ( assertNotMultiValued ) {
            assertNotMultiValued( sql );
        }
        return sql;
    }

    /**
     * Translates the given {@link Function} into an {@link SQLExpression}.
     * 
     * @param function
     *            function to be translated, must not be <code>null</code>
     * @return corresponding SQL expression, or <code>null</code> if function can not be mapped to an SQL function call
     * @throws UnmappableException
     *             if translation is not possible (usually due to unmappable property names)
     * @throws FilterEvaluationException
     *             if the filter contains invalid {@link ValueReference}s
     */
    protected SQLExpression toProtoSQL( Function function )
                            throws UnmappableException, FilterEvaluationException {

        SQLExpression sql = null;
        List<SQLExpression> params = new ArrayList<SQLExpression>( function.getParameters().size() );
        boolean isConstant = appendParamsFromFunction( function, params );
        SQLFunctionProvider sqlFunction = SQLFunctionManager.getFunctionProvider( function.getName() );
        if ( sqlFunction != null ) {
            // let the DB evaluate the function
            sql = sqlFunction.toProtoSQL( params, dialect );
        } else if ( isConstant ) {
            // evaluate function in memory
            TypedObjectNode value = evaluateFunction( function, params );
            if ( value instanceof Geometry ) {
                return new SQLArgument( (Geometry) value, null );
            } else if ( value instanceof PrimitiveValue ) {
                return new SQLArgument( (PrimitiveValue) value, null );
            } else {
                return new SQLArgument( (PrimitiveValue) null, null );
            }
        } else {
            // not constant and no SQLFunction available
            String msg = "Unable to translate function to SQL function call. No SQLFunction implementation for function '"
                         + function.getName() + "' available.";
            LOG.warn( msg );
            throw new UnmappableException( msg );
        }

        return sql;
    }

    protected TypedObjectNode evaluateFunction( Function function, List<SQLExpression> params )
                            throws FilterEvaluationException {
        List<TypedObjectNode[]> args = new ArrayList<TypedObjectNode[]>();
        for ( SQLExpression sqlExpr : params ) {
            SQLArgument sqlArg = (SQLArgument) sqlExpr;
            args.add( new TypedObjectNode[] { sqlArg.getValue() } );
        }
        TypedObjectNode[] outputs = function.evaluate( args );
        TypedObjectNode value = null;
        if ( outputs.length > 0 ) {
            // TODO should we support multiple values here?
            value = outputs[0];
        }
        return value;
    }

    protected boolean appendParamsFromFunction( Function function, List<SQLExpression> params )
                            throws UnmappableException, FilterEvaluationException {
        boolean isConstant = true;
        for ( Expression param : function.getParameters() ) {
            SQLExpression sqlParam = toProtoSQL( param );
            if ( !( sqlParam instanceof SQLArgument ) ) {
                isConstant = false;
            }
            params.add( sqlParam );
        }
        return isConstant;
    }

    /**
     * Translates the given {@link Literal} into an {@link SQLExpression}.
     * <p>
     * The returned literal does not have any type information. Literal types are inferred in
     * {@link #toProtoSQL(ComparisonOperator)}.
     * </p>
     * 
     * @param literal
     *            literal to be translated, must not be <code>null</code>
     * @return corresponding SQL expression, never <code>null</code>
     * @throws UnmappableException
     *             if translation is not possible (usually due to unmappable property names)
     * @throws FilterEvaluationException
     *             if the filter contains invalid {@link ValueReference}s
     */
    protected SQLExpression toProtoSQL( Literal<?> literal )
                            throws UnmappableException, FilterEvaluationException {
        Object value = literal.getValue();
        if ( value != null ) {
            if ( value instanceof PrimitiveValue ) {
                PrimitiveValue pv = (PrimitiveValue) value;
                return new SQLArgument( pv, null );
            } else {
                throw new UnmappableException( "Only primitive valued literals are currently supported." );
            }
        }
        PrimitiveParticleConverter converter = new DefaultPrimitiveConverter( new PrimitiveType( STRING ), null,
                                                                              false );
        return new SQLArgument( null, converter );
    }

    /**
     * Translates the given {@link ValueReference} into an {@link SQLExpression}.
     * 
     * @param expr
     *            expression to be translated, must not be <code>null</code>
     * @return corresponding SQL expression, never <code>null</code>
     * @throws UnmappableException
     *             if translation is not possible (usually due to unmappable property names)
     * @throws FilterEvaluationException
     *             if the filter contains invalid {@link ValueReference}s
     */
    protected SQLExpression toProtoSQL( ValueReference propName )
                            throws UnmappableException, FilterEvaluationException {
        SQLExpression sql = null;
        PropertyNameMapping propMapping = mapper.getMapping( propName, aliasManager );
        if ( propMapping != null ) {
            propNameMappingList.add( propMapping );
            if ( propMapping instanceof ConstantPropertyNameMapping ) {
                // TODO get rid of ConstantPropertyNameMapping
                PrimitiveType pt = new PrimitiveType( STRING );
                PrimitiveValue value = new PrimitiveValue( ""
                                                           + ( (ConstantPropertyNameMapping) propMapping ).getValue(),
                                                           pt );
                PrimitiveParticleConverter converter = new DefaultPrimitiveConverter( pt, null, false );
                sql = new SQLArgument( value, converter );
            } else {
                sql = new SQLColumn( propMapping.getTableAlias(), propMapping.getColumn(), propMapping.getConverter() );
            }
        } else {
            throw new UnmappableException( "Unable to map property '" + propName + "' to database column." );
        }
        return sql;
    }

    /**
     * Translates the given spatial {@link ValueReference} into an {@link SQLExpression}.
     * 
     * @param expr
     *            expression to be translated, must not be <code>null</code>
     * @return corresponding SQL expression, never <code>null</code>
     * @throws UnmappableException
     *             if translation is not possible (usually due to unmappable property names)
     * @throws FilterEvaluationException
     *             if the filter contains invalid {@link ValueReference}s
     */
    protected SQLExpression toProtoSQLSpatial( ValueReference propName )
                            throws FilterEvaluationException, UnmappableException {
        SQLExpression sql = null;
        PropertyNameMapping propMapping = mapper.getSpatialMapping( propName, aliasManager );
        if ( propMapping != null ) {
            propNameMappingList.add( propMapping );
            if ( propMapping instanceof ConstantPropertyNameMapping ) {
                // TODO get rid of ConstantPropertyNameMapping
                PrimitiveType pt = new PrimitiveType( STRING );
                PrimitiveValue value = new PrimitiveValue( ""
                                                           + ( (ConstantPropertyNameMapping) propMapping ).getValue(),
                                                           pt );
                PrimitiveParticleConverter converter = new DefaultPrimitiveConverter( pt, null, false );
                sql = new SQLArgument( value, converter );
            } else {
                sql = new SQLColumn( propMapping.getTableAlias(), propMapping.getColumn(), propMapping.getConverter() );
            }
        } else {
            throw new UnmappableException( "Unable to map property '" + propName + "' to database column." );
        }
        return sql;
    }

    /**
     * Translates the given {@link SortProperty} array into an {@link SQLExpression}.
     * 
     * @param sortCrits
     *            sort criteria to be translated, must not be <code>null</code>
     * @return corresponding SQL expression, never <code>null</code>
     * @throws UnmappableException
     *             if translation is not possible (usually due to unmappable property names)
     * @throws FilterEvaluationException
     *             if the filter contains invalid {@link ValueReference}s
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

    /**
     * Ensures that the given {@link SQLExpression} is not an {@link SQLExpression} that is multi-valued.
     * 
     * @param expr
     *            SQL expression, must not be <code>null</code>
     * @throws UnmappableException
     */
    protected void assertNotMultiValued( SQLExpression expr )
                            throws UnmappableException {
        if ( expr.isMultiValued() ) {
            String msg = "Cannot apply filter as it refers to a column that stores multiple values in concatenated form.'";
            throw new UnmappableException( msg );
        }
    }
}