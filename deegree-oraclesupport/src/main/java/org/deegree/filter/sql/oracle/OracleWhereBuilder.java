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
package org.deegree.filter.sql.oracle;

import static java.sql.Types.BOOLEAN;

import java.sql.SQLException;
import java.sql.Types;

import oracle.jdbc.OracleConnection;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.deegree.cs.CRS;
import org.deegree.feature.persistence.oracle.JGeometryAdapter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
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
import org.deegree.filter.sql.UnmappableException;
import org.deegree.filter.sql.expression.SQLColumn;
import org.deegree.filter.sql.expression.SQLExpression;
import org.deegree.filter.sql.expression.SQLLiteral;
import org.deegree.filter.sql.expression.SQLOperation;
import org.deegree.filter.sql.expression.SQLOperationBuilder;
import org.deegree.filter.sql.postgis.PostGISMapping;
import org.deegree.filter.sql.postgis.PropertyNameMapping;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;

/**
 * {@link AbstractWhereBuilder} implementation for Oracle Spatial databases (TODO which versions).
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OracleWhereBuilder extends AbstractWhereBuilder {

    private final PostGISMapping mapping;

    private OracleConnection conn;

    /**
     * Creates a new {@link OracleWhereBuilder} instance.
     * 
     * @param mapping
     *            provides the mapping from {@link PropertyName}s to DB columns, must not be <code>null</code>
     * @param filter
     *            Filter to use for generating the WHERE clause, can be <code>null</code>
     * @param sortCrit
     *            criteria to use generating the ORDER BY clause, can be <code>null</code>
     * @param conn
     *            Oracle connection, must not be <code>null</code>
     * @throws FilterEvaluationException
     */
    public OracleWhereBuilder( PostGISMapping mapping, OperatorFilter filter, SortProperty[] sortCrit,
                               OracleConnection conn ) throws FilterEvaluationException {
        super( filter, sortCrit );
        this.mapping = mapping;
        this.conn = conn;
        build();
    }

    @Override
    protected SQLOperation toProtoSQL( SpatialOperator op )
                            throws UnmappableException, FilterEvaluationException {

        SQLOperationBuilder builder = new SQLOperationBuilder( BOOLEAN );

        SQLExpression propNameExpr = toProtoSQL( op.getPropName() );
        // if ( !propNameExpr.isSpatial() ) {
        // String msg = "Cannot evaluate spatial operator on database. Targeted property name '" + op.getPropName()
        // + "' does not denote a spatial column.";
        // throw new FilterEvaluationException( msg );
        // }

        CRS storageCRS = propNameExpr.getSRS();

        switch ( op.getSubType() ) {
        case BBOX: {
            BBOX bbox = (BBOX) op;
            builder.add( "MDSYS.SDO_RELATE(" );
            builder.add( propNameExpr );
            builder.add( "," );
            builder.add( toProtoSQL( bbox.getBoundingBox(), storageCRS ) );
            builder.add( ",'MASK=ANYINTERACT QUERYTYPE=FILTER')='TRUE'" );
            break;
        }
        case BEYOND: {
            Beyond beyond = (Beyond) op;
            builder.add( "NOT SDO_WITHIN_DISTANCE(" );
            builder.add( propNameExpr );
            builder.add( "," );
            builder.add( toProtoSQL( beyond.getGeometry(), storageCRS ) );
            builder.add( ",'DISTANCE=" );
            // TODO uom handling
            builder.add( new SQLLiteral( beyond.getDistance().getValue(), Types.NUMERIC ) );
            builder.add( "')='TRUE'" );
            break;
        }
        case CONTAINS: {
            Contains contains = (Contains) op;
            builder.add( "MDSYS.SDO_RELATE(" );
            builder.add( propNameExpr );
            builder.add( "," );
            builder.add( toProtoSQL( contains.getGeometry(), storageCRS ) );
            builder.add( ",'MASK=CONTAINS+COVERS')='TRUE'" );
            break;
        }
        case CROSSES: {
            Crosses crosses = (Crosses) op;
            builder.add( "MDSYS.SDO_RELATE(" );
            builder.add( propNameExpr );
            builder.add( "," );
            builder.add( toProtoSQL( crosses.getGeometry(), storageCRS ) );
            builder.add( ",'MASK=OVERLAPBDYDISJOINT')='TRUE'" );
            break;
        }
        case DISJOINT: {
            Disjoint disjoint = (Disjoint) op;
            builder.add( "NOT MDSYS.SDO_RELATE(" );
            builder.add( propNameExpr );
            builder.add( "," );
            builder.add( toProtoSQL( disjoint.getGeometry(), storageCRS ) );
            builder.add( ",'MASK=ANYINTERACT')='TRUE'" );
            break;
        }
        case DWITHIN: {
            DWithin dWithin = (DWithin) op;
            builder.add( "SDO_WITHIN_DISTANCE(" );
            builder.add( propNameExpr );
            builder.add( "," );
            builder.add( toProtoSQL( dWithin.getGeometry(), storageCRS ) );
            builder.add( ",'DISTANCE=" );
            // TODO uom handling
            builder.add( new SQLLiteral( dWithin.getDistance().getValue(), Types.NUMERIC ) );
            builder.add( "')='TRUE'" );
            break;
        }
        case EQUALS: {
            Equals equals = (Equals) op;
            builder.add( "MDSYS.SDO_RELATE(" );
            builder.add( propNameExpr );
            builder.add( "," );
            builder.add( toProtoSQL( equals.getGeometry(), storageCRS ) );
            builder.add( ",'MASK=EQUAL')='TRUE'" );
            break;
        }
        case INTERSECTS: {
            Intersects intersects = (Intersects) op;
            builder.add( "MDSYS.SDO_RELATE(" );
            builder.add( propNameExpr );
            builder.add( "," );
            builder.add( toProtoSQL( intersects.getGeometry(), storageCRS ) );
            builder.add( ",'MASK=ANYINTERACT')='TRUE'" );
            break;
        }
        case OVERLAPS: {
            Overlaps overlaps = (Overlaps) op;
            builder.add( "MDSYS.SDO_RELATE(" );
            builder.add( propNameExpr );
            builder.add( "," );
            builder.add( toProtoSQL( overlaps.getGeometry(), storageCRS ) );
            builder.add( ",'MASK=OVERLAPBDYINTERSECT')='TRUE'" );
            break;
        }
        case TOUCHES: {
            Touches touches = (Touches) op;
            builder.add( "MDSYS.SDO_RELATE(" );
            builder.add( propNameExpr );
            builder.add( "," );
            builder.add( toProtoSQL( touches.getGeometry(), storageCRS ) );
            builder.add( ",'MASK=TOUCH')='TRUE'" );
            break;
        }
        case WITHIN: {
            Within within = (Within) op;
            builder.add( "MDSYS.SDO_RELATE(" );
            builder.add( propNameExpr );
            builder.add( "," );
            builder.add( toProtoSQL( within.getGeometry(), storageCRS ) );
            builder.add( ",'MASK=INSIDE+COVEREDBY')='TRUE'" );
            break;
        }
        }
        return builder.toOperation();
    }

    @Override
    protected SQLExpression toProtoSQL( PropertyName propName )
                            throws UnmappableException, FilterEvaluationException {
        SQLExpression sql = null;
        PropertyNameMapping propMapping = mapping.getMapping( propName );
        if ( propMapping != null ) {
            sql = new SQLColumn( propMapping.getTable(), propMapping.getColumn(), propMapping.isSpatial(),
                                 propMapping.getSQLType() );
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

        // TODO What about the SRID?
        JGeometryAdapter adapter = new JGeometryAdapter( transformedGeom.getCoordinateSystem(), -1 );
        JGeometry jg = adapter.toJGeometry( geom );
        STRUCT struct = null;
        try {
            struct = JGeometry.store( jg, conn );
        } catch ( SQLException e ) {
            String msg = "Transforming of geometry to Oracle STRUCT failed: " + e.getMessage();
            throw new FilterEvaluationException( msg );
        }
        return new SQLLiteral( struct, Types.STRUCT );
    }
}
