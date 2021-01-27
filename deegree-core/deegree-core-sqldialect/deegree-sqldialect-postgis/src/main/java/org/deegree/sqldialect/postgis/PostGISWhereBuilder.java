//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/deegree-core-base/src/main/java/org/deegree/filter/sql/postgis/PostGISWhereBuilder.java $
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
package org.deegree.sqldialect.postgis;

import static java.sql.Types.BOOLEAN;
import static org.deegree.commons.tom.primitive.BaseType.DATE_TIME;
import static org.deegree.commons.tom.primitive.BaseType.DECIMAL;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.datetime.ISO8601Converter;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.tom.sql.DefaultPrimitiveConverter;
import org.deegree.commons.tom.sql.PrimitiveParticleConverter;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsLike;
import org.deegree.filter.expression.Function;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.spatial.BBOX;
import org.deegree.filter.spatial.Beyond;
import org.deegree.filter.spatial.Contains;
import org.deegree.filter.spatial.Crosses;
import org.deegree.filter.spatial.DWithin;
import org.deegree.filter.spatial.Disjoint;
import org.deegree.filter.spatial.Equals;
import org.deegree.filter.spatial.Intersects;
import org.deegree.filter.spatial.Overlaps;
import org.deegree.filter.spatial.SpatialOperator;
import org.deegree.filter.spatial.Touches;
import org.deegree.filter.spatial.Within;
import org.deegree.filter.temporal.TemporalOperator;
import org.deegree.geometry.Geometry;
import org.deegree.sqldialect.filter.AbstractWhereBuilder;
import org.deegree.sqldialect.filter.PropertyNameMapper;
import org.deegree.sqldialect.filter.UnmappableException;
import org.deegree.sqldialect.filter.expression.SQLArgument;
import org.deegree.sqldialect.filter.expression.SQLExpression;
import org.deegree.sqldialect.filter.expression.SQLOperation;
import org.deegree.sqldialect.filter.expression.SQLOperationBuilder;
import org.deegree.sqldialect.filter.islike.IsLikeString;
import org.deegree.time.position.IndeterminateValue;
import org.deegree.time.position.TimePosition;
import org.deegree.time.primitive.GenericTimeInstant;
import org.deegree.time.primitive.GenericTimePeriod;

/**
 * {@link AbstractWhereBuilder} implementation for PostGIS databases.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31186 $, $Date: 2011-07-01 18:01:58 +0200 (Fr, 01. Jul 2011) $
 */
public class PostGISWhereBuilder extends AbstractWhereBuilder {

    private final boolean useLegacyPredicates;

    /**
     * Creates a new {@link PostGISWhereBuilder} instance.
     * 
     * @param dialect
     *            SQL dialect, can be <code>null</code> (TODO refactor code, so not null is always used)
     * @param mapper
     *            provides the mapping from {@link ValueReference}s to DB columns, must not be <code>null</code>
     * @param filter
     *            Filter to use for generating the WHERE clause, can be <code>null</code>
     * @param sortCrit
     *            criteria to use for generating the ORDER BY clause, can be <code>null</code>
     * @param allowPartialMappings
     *            if false, any unmappable expression will cause an {@link UnmappableException} to be thrown
     * @param useLegacyPredicates
     *            if true, legacy-style PostGIS spatial predicates are used (e.g. <code>Intersects</code> instead of
     *            <code>ST_Intersects</code>)
     * @throws FilterEvaluationException
     *             if the expression contains invalid {@link ValueReference}s
     * @throws UnmappableException
     *             if allowPartialMappings is false and an expression could not be mapped to the db
     */
    public PostGISWhereBuilder( PostGISDialect dialect, PropertyNameMapper mapper, OperatorFilter filter,
                                SortProperty[] sortCrit, boolean allowPartialMappings, boolean useLegacyPredicates )
                                                        throws FilterEvaluationException, UnmappableException {
        super( dialect, mapper, filter, sortCrit );
        this.useLegacyPredicates = useLegacyPredicates;
        build( allowPartialMappings );
    }

    /**
     * Translates the given {@link PropertyIsLike} into an {@link SQLOperation}.
     * <p>
     * NOTE: This method appends the generated argument inline, i.e. not using a <code>?</code>. This is because of a
     * problem that has been observed with PostgreSQL 8.0; the execution of the inline version is *much* faster.
     * </p>
     * 
     * @param op
     *            comparison operator to be translated, must not be <code>null</code>
     * @return corresponding SQL expression, never <code>null</code>
     * @throws UnmappableException
     *             if translation is not possible (usually due to unmappable property names)
     * @throws FilterEvaluationException
     *             if the expression contains invalid {@link ValueReference}s
     */
    @Override
    protected SQLOperation toProtoSQL( PropertyIsLike op )
                            throws UnmappableException, FilterEvaluationException {

        Expression pattern = op.getPattern();
        if ( pattern instanceof Literal ) {
            String literal = ( (Literal<?>) pattern ).getValue().toString();
            return toProtoSql( op, literal );
        } else if ( pattern instanceof Function ) {
            String valueAsString = getStringValueFromFunction( pattern );
            return toProtoSql( op, valueAsString );
        }
        String msg = "Mapping of PropertyIsLike with non-literal or non-function comparisons to SQL is not implemented yet.";
        throw new UnsupportedOperationException( msg );
    }

    protected String getStringValueFromFunction( Expression pattern )
                            throws UnmappableException, FilterEvaluationException {
        Function function = (Function) pattern;
        List<SQLExpression> params = new ArrayList<SQLExpression>( function.getParameters().size() );
        appendParamsFromFunction( function, params );
        TypedObjectNode value = evaluateFunction( function, params );
        if ( !( value instanceof PrimitiveValue ) ) {
            throw new UnsupportedOperationException( "SQL IsLike request with a function evaluating to a non-primitive value is not supported!" );
        }
        String valueAsString = ( (PrimitiveValue) value ).getAsText();
        return valueAsString;
    }

    private SQLOperation toProtoSql( PropertyIsLike op, String literal )
                            throws UnmappableException, FilterEvaluationException {
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
        return getOperationFromBuilder( op, propName, sqlEncoded );
    }

    private SQLOperation getOperationFromBuilder( PropertyIsLike op, SQLExpression propName, String sqlEncoded ) {
        SQLOperationBuilder builder = new SQLOperationBuilder();
        if ( !op.isMatchCase() ) {
            builder.add( "LOWER(" );
        }
        builder.add( propName );
        if ( op.isMatchCase() ) {
            builder.add( "::TEXT LIKE '" );
        } else {
            builder.add( "::TEXT) LIKE '" );
        }
        builder.add( sqlEncoded );
        builder.add( "'" );
        return builder.toOperation();
    }

    @Override
    protected SQLOperation toProtoSQL( SpatialOperator op )
                            throws UnmappableException, FilterEvaluationException {

        SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );

        SQLExpression propNameExpr = toProtoSQLSpatial( op.getPropName() );
        if ( !propNameExpr.isSpatial() ) {
            String msg = "Cannot evaluate spatial operator on database. Targeted property name '" + op.getPropName()
                         + "' does not denote a spatial column.";
            throw new InvalidParameterValueException( msg );
        }

        ICRS storageCRS = propNameExpr.getCRS();
        int srid = propNameExpr.getSRID() != null ? Integer.parseInt( propNameExpr.getSRID() ) : -1;

        switch ( op.getSubType() ) {
        case BBOX: {
            BBOX bbox = (BBOX) op;
            if ( !bbox.getAllowFalsePositives() ) {
                builder.add( " (" );
            }
            builder.add( propNameExpr );
            builder.add( " && " );
            builder.add( toProtoSQL( bbox.getBoundingBox(), storageCRS, srid ) );
            if ( !bbox.getAllowFalsePositives() ) {
                builder.add( " AND " );
                if ( useLegacyPredicates ) {
                    builder.add( "intersects(" );
                } else {
                    builder.add( "ST_Intersects(" );
                }
                builder.add( propNameExpr );
                builder.add( "," );
                builder.add( toProtoSQL( bbox.getBoundingBox(), storageCRS, srid ) );
                builder.add( "))" );
            }
            break;
        }
        case BEYOND: {
            Beyond beyond = (Beyond) op;
            if ( useLegacyPredicates ) {
                builder.add( "NOT dwithin(" );
            } else {
                builder.add( "NOT ST_DWithin(" );
            }
            builder.add( propNameExpr );
            builder.add( "," );
            builder.add( toProtoSQL( beyond.getGeometry(), storageCRS, srid ) );
            builder.add( "," );
            // TODO uom handling
            PrimitiveType pt = new PrimitiveType( DECIMAL );
            PrimitiveValue value = new PrimitiveValue( beyond.getDistance().getValue(), pt );
            PrimitiveParticleConverter converter = new DefaultPrimitiveConverter( pt, null, false );
            SQLArgument argument = new SQLArgument( value, converter );
            builder.add( argument );
            builder.add( ")" );
            break;
        }
        case CONTAINS: {
            Contains contains = (Contains) op;
            if ( useLegacyPredicates ) {
                builder.add( "contains(" );
            } else {
                builder.add( "ST_Contains(" );
            }
            builder.add( propNameExpr );
            builder.add( "," );
            builder.add( toProtoSQL( contains.getGeometry(), storageCRS, srid ) );
            builder.add( ")" );
            break;
        }
        case CROSSES: {
            Crosses crosses = (Crosses) op;
            if ( useLegacyPredicates ) {
                builder.add( "crosses(" );
            } else {
                builder.add( "ST_Crosses(" );
            }
            builder.add( propNameExpr );
            builder.add( "," );
            builder.add( toProtoSQL( crosses.getGeometry(), storageCRS, srid ) );
            builder.add( ")" );
            break;
        }
        case DISJOINT: {
            Disjoint disjoint = (Disjoint) op;
            if ( useLegacyPredicates ) {
                builder.add( "disjoint(" );
            } else {
                builder.add( "ST_Disjoint(" );
            }
            builder.add( propNameExpr );
            builder.add( "," );
            builder.add( toProtoSQL( disjoint.getGeometry(), storageCRS, srid ) );
            builder.add( ")" );
            break;
        }
        case DWITHIN: {
            DWithin dWithin = (DWithin) op;
            if ( useLegacyPredicates ) {
                builder.add( "dwithin(" );
            } else {
                builder.add( "ST_DWithin(" );
            }
            builder.add( propNameExpr );
            builder.add( "," );
            builder.add( toProtoSQL( dWithin.getGeometry(), storageCRS, srid ) );
            builder.add( "," );
            // TODO uom handling
            PrimitiveType pt = new PrimitiveType( DECIMAL );
            PrimitiveValue value = new PrimitiveValue( dWithin.getDistance().getValue(), pt );
            PrimitiveParticleConverter converter = new DefaultPrimitiveConverter( pt, null, false );
            SQLArgument argument = new SQLArgument( value, converter );
            builder.add( argument );
            builder.add( ")" );
            break;
        }
        case EQUALS: {
            Equals equals = (Equals) op;
            if ( useLegacyPredicates ) {
                builder.add( "equals(" );
            } else {
                builder.add( "ST_Equals(" );
            }
            builder.add( propNameExpr );
            builder.add( "," );
            builder.add( toProtoSQL( equals.getGeometry(), storageCRS, srid ) );
            builder.add( ")" );
            break;
        }
        case INTERSECTS: {
            Intersects intersects = (Intersects) op;
            if ( useLegacyPredicates ) {
                builder.add( "intersects(" );
            } else {
                builder.add( "ST_Intersects(" );
            }
            builder.add( propNameExpr );
            builder.add( "," );
            builder.add( toProtoSQL( intersects.getGeometry(), storageCRS, srid ) );
            builder.add( ")" );
            break;
        }
        case OVERLAPS: {
            Overlaps overlaps = (Overlaps) op;
            if ( useLegacyPredicates ) {
                builder.add( "overlaps(" );
            } else {
                builder.add( "ST_Overlaps(" );
            }
            builder.add( propNameExpr );
            builder.add( "," );
            builder.add( toProtoSQL( overlaps.getGeometry(), storageCRS, srid ) );
            builder.add( ")" );
            break;
        }
        case TOUCHES: {
            Touches touches = (Touches) op;
            if ( useLegacyPredicates ) {
                builder.add( "touches(" );
            } else {
                builder.add( "ST_Touches(" );
            }
            builder.add( propNameExpr );
            builder.add( "," );
            builder.add( toProtoSQL( touches.getGeometry(), storageCRS, srid ) );
            builder.add( ")" );
            break;
        }
        case WITHIN: {
            Within within = (Within) op;
            if ( useLegacyPredicates ) {
                builder.add( "within(" );
            } else {
                builder.add( "ST_Within(" );
            }
            builder.add( propNameExpr );
            builder.add( "," );
            builder.add( toProtoSQL( within.getGeometry(), storageCRS, srid ) );
            builder.add( ")" );
            break;
        }
        }
        return builder.toOperation();
    }

    protected SQLOperation toProtoSQL( TemporalOperator op )
                            throws UnmappableException, FilterEvaluationException {
        SQLOperation sql = null;
        switch ( op.getSubType() ) {
        case AFTER: {
            SQLExpression first = toProtoSQL( op.getParameter1() );
            Expression parameter2 = op.getParameter2();
            SQLExpression second;
            if ( isTimeInstant( parameter2 ) ) {
                TimePosition timePosition = ( (GenericTimeInstant) ( (Literal<?>) parameter2 ).getValue() ).getPosition();
                second = createDateExpression( timePosition );
            } else  if ( isTimePeriod( parameter2 ) ) {
                TimePosition end = ( (GenericTimePeriod) ( (Literal<?>) parameter2 ).getValue() ).getEndPosition();
                second = createDateExpression( end );
            } else {
                second = toProtoSQL( parameter2 );
            }
            inferType( first, second );
            sql = createSqlAfter( first, second );
            break;
        }
        case BEFORE: {
            SQLExpression first = toProtoSQL( op.getParameter1() );
            Expression parameter2 = op.getParameter2();
            SQLExpression second;
            if ( isTimeInstant( parameter2 ) ) {
                TimePosition timePosition = ( (GenericTimeInstant) ( (Literal<?>) parameter2 ).getValue() ).getPosition();
                second = createDateExpression( timePosition );
            } else  if ( isTimePeriod( parameter2 ) ) {
                TimePosition begin = ( (GenericTimePeriod) ( (Literal<?>) parameter2 ).getValue() ).getBeginPosition();
                second = createDateExpression( begin );
            } else {
                second = toProtoSQL( parameter2 );
            }
            inferType( first, second );
            sql = createSqlBefore( first, second );
            break;
        }
        case TEQUALS: {
            SQLExpression first = toProtoSQL( op.getParameter1() );
            Expression parameter2 = op.getParameter2();
            SQLExpression second;
            if ( isTimeInstant( parameter2 ) ) {
                TimePosition timePosition = ( (GenericTimeInstant) ( (Literal<?>) parameter2 ).getValue() ).getPosition();
                second = createDateExpression( timePosition );
            } else {
                second = toProtoSQL( parameter2 );
            }
            inferType( first, second );
            sql = createSqlEquals( first, second );
            break;
        }
        case DURING: {
            Expression parameter2 = op.getParameter2();
            if ( isTimePeriod( parameter2 ) ) {
                TimePosition begin = ( (GenericTimePeriod) ( (Literal<?>) parameter2 ).getValue() ).getBeginPosition();
                TimePosition end = ( (GenericTimePeriod) ( (Literal<?>) parameter2 ).getValue() ).getEndPosition();
                SQLExpression valueReference = toProtoSQL( op.getParameter1() );
                SQLExpression beginExpr = createDateExpression( begin );
                SQLExpression endExpr = createDateExpression( end );
                sql = createSqlDuring( valueReference, beginExpr, endExpr );
            }
            break;
        }
        default:
            sql = super.toProtoSQL( op );
        }
        return sql;
    }

    private SQLExpression createDateExpression( TimePosition timePosition ) {
        if ( timePosition.getIndeterminatePosition() != null ) {
            if ( timePosition.getIndeterminatePosition().equals( IndeterminateValue.NOW ) ) {
                String encodedTemporal = ISO8601Converter.formatDateTime( new Date() );
                TimePosition now = new TimePosition( null, null, null, encodedTemporal );
                return createDateExpressionFromTimePosition( now );
            } else {
                return null;
            }
        }
        return createDateExpressionFromTimePosition( timePosition );
    }

    private SQLExpression createDateExpressionFromTimePosition( TimePosition now ) {
        PrimitiveValue value = new PrimitiveValue( now.getValue(), new PrimitiveType( DATE_TIME ) );
        DefaultPrimitiveConverter converter = new DefaultPrimitiveConverter( new PrimitiveType( DATE_TIME ), null,
                                                                             false );
        return new SQLArgument( value, converter );
    }

    @Override
    protected void addExpression( SQLOperationBuilder builder, SQLExpression expr, Boolean matchCase ) {
        if ( matchCase == null || matchCase ) {
            builder.add( expr );
        } else {
            builder.add( "LOWER(" );
            builder.add( expr );
            builder.add( "::TEXT)" );
        }
    }

    private SQLExpression toProtoSQL( Geometry geom, ICRS targetCRS, int srid )
                            throws FilterEvaluationException {
        return new SQLArgument( geom, new PostGISGeometryConverter( null, targetCRS, "" + srid, useLegacyPredicates ) );
    }

    private SQLOperation createSqlDuring( SQLExpression valueReference, SQLExpression beginExpr,
                                          SQLExpression endExpr ) {
        if ( beginExpr == null && endExpr == null )
            return null;
        if ( beginExpr != null && endExpr == null ) {
            SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );
            builder.add( "(" );
            addExpression( builder, valueReference );
            builder.add( " >= " );
            addExpression( builder, beginExpr );
            builder.add( ")" );
            return builder.toOperation();
        }
        if ( beginExpr == null && endExpr != null ) {
            SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );
            builder.add( "(" );
            addExpression( builder, valueReference );
            builder.add( " <= " );
            addExpression( builder, endExpr );
            builder.add( ")" );
            return builder.toOperation();
        }
        inferType( valueReference, beginExpr, endExpr );
        SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );
        builder.add( "(" );
        addExpression( builder, valueReference );
        builder.add( " >= " );
        addExpression( builder, beginExpr );
        builder.add( " AND " );
        addExpression( builder, valueReference );
        builder.add( " <= " );
        addExpression( builder, endExpr );
        builder.add( ")" );
        return builder.toOperation();
    }

    private SQLOperation createSqlEquals( SQLExpression first, SQLExpression second ) {
        SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );
        builder.add( "(" );
        addExpression( builder, first );
        builder.add( " = " );
        addExpression( builder, second );
        builder.add( ")" );
        return builder.toOperation();
    }

    private SQLOperation createSqlBefore( SQLExpression first, SQLExpression second ) {
        SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );
        builder.add( "(" );
        addExpression( builder, first );
        builder.add( " < " );
        addExpression( builder, second );
        builder.add( ")" );
        return builder.toOperation();
    }

    private SQLOperation createSqlAfter( SQLExpression first, SQLExpression second ) {
        SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );
        builder.add( "(" );
        addExpression( builder, first );
        builder.add( " > " );
        addExpression( builder, second );
        builder.add( ")" );
        return builder.toOperation();
    }

    private boolean isTimePeriod( Expression parameter2 ) {
        return parameter2 instanceof Literal && ( (Literal<?>) parameter2 ).getValue() instanceof GenericTimePeriod;
    }

    private boolean isTimeInstant( Expression parameter2 ) {
        return parameter2 instanceof Literal && ( (Literal<?>) parameter2 ).getValue() instanceof GenericTimeInstant;
    }

}