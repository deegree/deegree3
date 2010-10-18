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

import static java.sql.Types.BOOLEAN;

import java.sql.Types;

import org.deegree.cs.CRS;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsLike;
import org.deegree.filter.expression.PropertyName;
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
import org.deegree.filter.sql.AbstractWhereBuilder;
import org.deegree.filter.sql.PropertyNameMapping;
import org.deegree.filter.sql.UnmappableException;
import org.deegree.filter.sql.expression.SQLColumn;
import org.deegree.filter.sql.expression.SQLExpression;
import org.deegree.filter.sql.expression.SQLLiteral;
import org.deegree.filter.sql.expression.SQLOperation;
import org.deegree.filter.sql.expression.SQLOperationBuilder;
import org.deegree.filter.sql.islike.IsLikeString;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.io.WKTWriter;

/**
 * {@link AbstractWhereBuilder} implementation for PostGIS databases.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PostGISWhereBuilder extends AbstractWhereBuilder {

    private final PostGISMapping mapping;

    private final boolean useLegacyPredicates;

    /**
     * Creates a new {@link PostGISWhereBuilder} instance.
     * 
     * @param mapping
     *            provides the mapping from {@link PropertyName}s to DB columns, must not be <code>null</code>
     * @param filter
     *            Filter to use for generating the WHERE clause, can be <code>null</code>
     * @param sortCrit
     *            criteria to use generating the ORDER BY clause, can be <code>null</code>
     * @param useLegacyPredicates
     *            if true, legacy-style PostGIS spatial predicates are used (e.g. <code>Intersects</code> instead of
     *            <code>ST_Intersects</code>)
     * @throws FilterEvaluationException
     *             if the expression contains invalid {@link PropertyName}s
     */
    public PostGISWhereBuilder( PostGISMapping mapping, OperatorFilter filter, SortProperty[] sortCrit,
                                boolean useLegacyPredicates ) throws FilterEvaluationException {
        super( filter, sortCrit );
        this.useLegacyPredicates = useLegacyPredicates;
        this.mapping = mapping;
        build();
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
     *             if the expression contains invalid {@link PropertyName}s
     */
    @Override
    protected SQLOperation toProtoSQL( PropertyIsLike op )
                            throws UnmappableException, FilterEvaluationException {

        String literal = op.getLiteral().getValue().toString();
        String escape = "" + op.getEscapeChar();
        String wildCard = "" + op.getWildCard();
        String singleChar = "" + op.getSingleChar();

        IsLikeString specialString = new IsLikeString( literal, wildCard, singleChar, escape );
        // TODO lowerCasing?
        String sqlEncoded = specialString.toSQL( !op.getMatchCase() );

        SQLOperationBuilder builder = new SQLOperationBuilder( op.getMatchCase() );
        builder.add( toProtoSQL( op.getPropertyName() ) );
        builder.add( "::TEXT LIKE '" );
        builder.add( sqlEncoded );
        builder.add( "'" );
        return builder.toOperation();
    }

    @Override
    protected SQLOperation toProtoSQL( SpatialOperator op )
                            throws UnmappableException, FilterEvaluationException {

        SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );

        SQLExpression propNameExpr = toProtoSQL( op.getPropName() );
        if ( !propNameExpr.isSpatial() ) {
            String msg = "Cannot evaluate spatial operator on database. Targeted property name '" + op.getPropName()
                         + "' does not denote a spatial column.";
            throw new FilterEvaluationException( msg );
        }

        CRS storageCRS = propNameExpr.getSRS();

        switch ( op.getSubType() ) {
        case BBOX: {
            BBOX bbox = (BBOX) op;
            builder.add( propNameExpr );
            builder.add( " && " );
            builder.add( toProtoSQL( bbox.getBoundingBox(), storageCRS ) );
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
            builder.add( toProtoSQL( beyond.getGeometry(), storageCRS ) );
            builder.add( "," );
            // TODO uom handling
            builder.add( new SQLLiteral( beyond.getDistance().getValue(), Types.NUMERIC ) );
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
            builder.add( toProtoSQL( contains.getGeometry(), storageCRS ) );
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
            builder.add( toProtoSQL( crosses.getGeometry(), storageCRS ) );
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
            builder.add( toProtoSQL( disjoint.getGeometry(), storageCRS ) );
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
            builder.add( toProtoSQL( dWithin.getGeometry(), storageCRS ) );
            builder.add( "," );
            // TODO uom handling
            builder.add( new SQLLiteral( dWithin.getDistance().getValue(), Types.NUMERIC ) );
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
            builder.add( toProtoSQL( equals.getGeometry(), storageCRS ) );
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
            builder.add( toProtoSQL( intersects.getGeometry(), storageCRS ) );
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
            builder.add( toProtoSQL( overlaps.getGeometry(), storageCRS ) );
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
            builder.add( toProtoSQL( touches.getGeometry(), storageCRS ) );
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
            builder.add( toProtoSQL( within.getGeometry(), storageCRS ) );
            builder.add( ")" );
            break;
        }
        }
        return builder.toOperation();
    }

    @Override
    protected SQLExpression toProtoSQL( PropertyName propName )
                            throws UnmappableException, FilterEvaluationException {
        SQLExpression sql = null;
        PropertyNameMapping propMapping = mapping.getMapping( propName, aliasManager );
        if ( propMapping != null ) {
            propNameMappingList.add( propMapping );
            // TODO
            sql = new SQLColumn(
                                 propMapping.getTargetField().getAlias() != null ? propMapping.getTargetField().getAlias()
                                                                                : propMapping.getTargetField().getTable(),
                                 propMapping.getTargetField().getColumn(), true, -1 );
        } else {
            throw new UnmappableException( "Unable to map property '" + propName + "' to database column." );
        }
        return sql;
    }

    private SQLExpression toProtoSQL( Geometry geom, CRS targetCRS )
                            throws FilterEvaluationException {

        Geometry transformedGeom = geom;
        if ( targetCRS != null && !targetCRS.equals( geom.getCoordinateSystem() ) ) {
            try {
                GeometryTransformer transformer = new GeometryTransformer( targetCRS.getWrappedCRS() );
                transformedGeom = transformer.transform( geom );
            } catch ( Exception e ) {
                String msg = "Transforming of geometry literal to storage CRS failed: " + e.getMessage();
                throw new FilterEvaluationException( msg );
            }
        }

        SQLOperationBuilder builder = new SQLOperationBuilder();
        if ( useLegacyPredicates ) {
            builder.add( "GeomFromText(" );
        } else {
            builder.add( "ST_GeometryFromText(" );
        }
        String wkt = WKTWriter.write( transformedGeom );
        builder.add( new SQLLiteral( wkt, Types.VARCHAR ) );
        builder.add( ")" );

        // byte[] wkb = null;
        // try {
        // wkb = WKBWriter.write( transformedGeom );
        // } catch ( ParseException e ) {
        // String msg = "Transforming of geometry literal to WKB: " + e.getMessage();
        // throw new FilterEvaluationException( msg );
        // }
        // return new SQLLiteral( wkb, Types.BINARY );
        return builder.toOperation();
    }
}
