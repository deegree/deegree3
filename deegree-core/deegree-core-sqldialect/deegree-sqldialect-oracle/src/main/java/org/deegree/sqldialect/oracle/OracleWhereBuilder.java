//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

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
package org.deegree.sqldialect.oracle;

import static java.sql.Types.BOOLEAN;
import static org.deegree.commons.tom.primitive.BaseType.DECIMAL;
import static org.deegree.commons.tom.primitive.BaseType.STRING;

import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.tom.sql.DefaultPrimitiveConverter;
import org.deegree.commons.tom.sql.PrimitiveParticleConverter;
import org.deegree.commons.uom.Measure;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsLike;
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
import org.deegree.geometry.Geometry;
import org.deegree.sqldialect.filter.AbstractWhereBuilder;
import org.deegree.sqldialect.filter.PropertyNameMapper;
import org.deegree.sqldialect.filter.UnmappableException;
import org.deegree.sqldialect.filter.expression.SQLArgument;
import org.deegree.sqldialect.filter.expression.SQLExpression;
import org.deegree.sqldialect.filter.expression.SQLOperation;
import org.deegree.sqldialect.filter.expression.SQLOperationBuilder;
import org.deegree.sqldialect.filter.islike.IsLikeString;

/**
 * {@link AbstractWhereBuilder} implementation for Oracle Spatial databases.
 * 
 * Oracle Database Version 10g or 11g are recommended (Oracle Version 9i and 8.1.7 may also work)
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class OracleWhereBuilder extends AbstractWhereBuilder {

    private int databaseMajorVersion;

    /**
     * Creates a new {@link OracleWhereBuilder} instance.
     * 
     * @param dialect
     *            SQL dialect, must not be <code>null</code>
     * @param mapping
     *            provides the mapping from {@link ValueReference}s to DB columns, must not be <code>null</code>
     * @param filter
     *            Filter to use for generating the WHERE clause, can be <code>null</code>
     * @param sortCrit
     *            criteria to use generating the ORDER BY clause, can be <code>null</code>
     * @param allowPartialMappings
     *            if false, any unmappable expression will cause an {@link UnmappableException} to be thrown
     * @throws FilterEvaluationException
     * @throws UnmappableException
     *             if allowPartialMappings is false and an expression could not be mapped to the db
     */
    OracleWhereBuilder( OracleDialect dialect, PropertyNameMapper mapper, OperatorFilter filter,
                        SortProperty[] sortCrit, boolean allowPartialMappings, int databaseMajorVersion )
                            throws FilterEvaluationException, UnmappableException {
        super( dialect, mapper, filter, sortCrit );
        this.databaseMajorVersion = databaseMajorVersion;
        build( allowPartialMappings );
    }

    @Override
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

        SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );
        builder.add( " CONTAINS ( " );
        builder.add( propName );
        builder.add( ", " );
        PrimitiveType pt = new PrimitiveType( STRING );
        PrimitiveValue value = new PrimitiveValue( sqlEncoded, pt );
        PrimitiveParticleConverter converter = new DefaultPrimitiveConverter( pt, null, propName.isMultiValued() );
        SQLArgument argument = new SQLArgument( value, converter );
        builder.add( argument );
        builder.add( " ) > 0 " );

        return builder.toOperation();
    }

    @Override
    protected SQLOperation toProtoSQL( SpatialOperator op )
                            throws UnmappableException, FilterEvaluationException {

        SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );
        SQLExpression propNameExpr = toProtoSQLSpatial( op.getPropName() );

        switch ( op.getSubType() ) {
        case BBOX:
            BBOX bbox = (BBOX) op;
            if ( bbox.getAllowFalsePositives() ) {
                appendFilterOperation( builder, propNameExpr, ( (BBOX) op ).getBoundingBox() );
            } else {
                appendRelateOperation( builder, propNameExpr, ( (BBOX) op ).getBoundingBox(), "ANYINTERACT" );
            }
            break;
        case INTERSECTS:
            appendRelateOperation( builder, propNameExpr, ( (Intersects) op ).getGeometry(), "ANYINTERACT" );
            break;
        case EQUALS:
            appendRelateOperation( builder, propNameExpr, ( (Equals) op ).getGeometry(), "EQUAL" );
            break;
        case DISJOINT:
            builder.add( "NOT " );
            appendRelateOperation( builder, propNameExpr, ( (Disjoint) op ).getGeometry(), "ANYINTERACT" );
            break;
        case TOUCHES:
            appendRelateOperation( builder, propNameExpr, ( (Touches) op ).getGeometry(), "TOUCH" );
            break;
        case WITHIN:
            appendRelateOperation( builder, propNameExpr, ( (Within) op ).getGeometry(), "INSIDE+COVEREDBY" );
            break;
        case OVERLAPS:
            appendRelateOperation( builder, propNameExpr, ( (Overlaps) op ).getGeometry(), "OVERLAPBDYINTERSECT" );
            break;
        case CROSSES:
            appendRelateOperation( builder, propNameExpr, ( (Crosses) op ).getGeometry(), "OVERLAPBDYDISJOINT" );
            break;
        case CONTAINS:
            appendRelateOperation( builder, propNameExpr, ( (Contains) op ).getGeometry(), "CONTAINS+COVERS" );
            break;
        case DWITHIN:
            appendDWithinOperation( builder, propNameExpr, ( (DWithin) op ).getGeometry(),
                                    ( (DWithin) op ).getDistance() );
            break;
        case BEYOND:
            builder.add( "NOT " );
            appendDWithinOperation( builder, propNameExpr, ( (Beyond) op ).getGeometry(), ( (Beyond) op ).getDistance() );
            break;
        }

        return builder.toOperation();
    }

    /**
     * Append a primary (index) filter to the query
     */
    private void appendFilterOperation( SQLOperationBuilder builder, SQLExpression propNameExpr, Geometry geom ) {
        ICRS storageCRS = propNameExpr.getCRS();
        int srid = propNameExpr.getSRID() != null ? Integer.parseInt( propNameExpr.getSRID() ) : -1;

        builder.add( "MDSYS.SDO_FILTER(" );
        builder.add( propNameExpr );
        builder.add( "," );
        builder.add( toProtoSQL( geom, storageCRS, srid ) );

        if ( databaseMajorVersion < 10 )
            builder.add( ",'querytype=WINDOW')='TRUE'" );
        else
            builder.add( ")='TRUE'" );
    }

    /**
     * Append a primary (index) and secondary (geometry) filter to the query
     */
    private void appendRelateOperation( SQLOperationBuilder builder, SQLExpression propNameExpr, Geometry geom,
                                        String mask ) {
        ICRS storageCRS = propNameExpr.getCRS();
        int srid = propNameExpr.getSRID() != null ? Integer.parseInt( propNameExpr.getSRID() ) : -1;

        builder.add( "MDSYS.SDO_RELATE(" );
        builder.add( propNameExpr );
        builder.add( "," );
        builder.add( toProtoSQL( geom, storageCRS, srid ) );
        if ( databaseMajorVersion < 10 )
            builder.add( ",'MASK=" + mask + " querytype=WINDOW')='TRUE'" );
        else
            builder.add( ",'MASK=" + mask + "')='TRUE'" );
    }

    /**
     * Append a primary (index) and secondary (geometry) distance based filter to the query
     */
    private void appendDWithinOperation( SQLOperationBuilder builder, SQLExpression propNameExpr, Geometry geom,
                                         Measure distance ) {
        ICRS storageCRS = propNameExpr.getCRS();
        int srid = propNameExpr.getSRID() != null ? Integer.parseInt( propNameExpr.getSRID() ) : -1;

        builder.add( "MDSYS.SDO_WITHIN_DISTANCE(" );
        builder.add( propNameExpr );
        builder.add( "," );
        builder.add( toProtoSQL( geom, storageCRS, srid ) );
        // TODO handle uom correctly

        PrimitiveType pt = new PrimitiveType( DECIMAL );
        PrimitiveValue value = new PrimitiveValue( distance.getValue(), pt );
        PrimitiveParticleConverter converter = new DefaultPrimitiveConverter( pt, null, false );
        SQLArgument argument = new SQLArgument( value, converter );

        // TRICKY do not use "QUERYTYPE=FILTER" as parameter to skip the secondary filter operation, which will lead to
        // imprecise results

        builder.add( ",'DISTANCE=" );
        builder.add( argument );
        builder.add( "')='TRUE'" );
    }

    private SQLExpression toProtoSQL( Geometry geom, ICRS storageCRS, int srid ) {
        return new SQLArgument( geom, new OracleGeometryConverter( null, storageCRS, "" + srid ) );
    }
}