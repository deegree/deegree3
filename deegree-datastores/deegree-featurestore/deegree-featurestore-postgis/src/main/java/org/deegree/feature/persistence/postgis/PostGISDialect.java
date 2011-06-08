//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.sql.DefaultPrimitiveConverter;
import org.deegree.commons.tom.sql.PrimitiveParticleConverter;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.persistence.sql.MappedApplicationSchema;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.sql.AbstractWhereBuilder;
import org.deegree.filter.sql.PropertyNameMapper;
import org.deegree.filter.sql.SQLDialect;
import org.deegree.filter.sql.UnmappableException;
import org.deegree.filter.sql.postgis.PostGISGeometryConverter;
import org.deegree.filter.sql.postgis.PostGISWhereBuilder;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.geometry.utils.GeometryParticleConverter;
import org.postgis.PGboxbase;

/**
 * {@link SQLDialect} for PostgreSQL / PostGIS databases.
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PostGISDialect implements SQLDialect {

    private final boolean useLegacyPredicates;

    public PostGISDialect( boolean useLegacyPredicates ) {
        this.useLegacyPredicates = useLegacyPredicates;
    }

    public String getDefaultSchema() {
        return "public";
    }

    public String stringPlus() {
        return "||";
    }

    public String stringIndex( String pattern, String string ) {
        return "POSITION(" + pattern + " IN " + string + ")";
    }

    public String cast( String expr, String type ) {
        return expr + "::" + type;
    }

    public String geometryMetadata( String dbSchema, String table, String column ) {
        return "SELECT coord_dimension,srid,type FROM public.geometry_columns WHERE f_table_schema='"
               + dbSchema.toLowerCase() + "' AND f_table_name='" + table.toLowerCase() + "' AND f_geometry_column='"
               + column.toLowerCase() + "'";
    }

    @Override
    public AbstractWhereBuilder getWhereBuilder( PropertyNameMapper mapper, OperatorFilter filter,
                                                 SortProperty[] sortCrit, boolean allowPartialMappings )
                            throws UnmappableException, FilterEvaluationException {
        return new PostGISWhereBuilder( mapper, filter, sortCrit, allowPartialMappings, useLegacyPredicates );
    }

    @Override
    public String getUndefinedSrid() {
        return "-1";
    }

    @Override
    public String getBBoxAggregateSnippet( String column ) {
        StringBuilder sql = new StringBuilder();
        if ( useLegacyPredicates ) {
            sql.append( "extent" );
        } else {
            sql.append( "ST_Extent" );
        }
        sql.append( "(" );
        sql.append( column );
        sql.append( ")::BOX2D FROM " );
        return sql.toString();
    }

    @Override
    public Envelope getBBoxAggregateValue( ResultSet rs, int colIdx, ICRS crs )
                            throws SQLException {
        Envelope env = null;
        PGboxbase pgBox = (PGboxbase) rs.getObject( colIdx );
        if ( pgBox != null ) {
            org.deegree.geometry.primitive.Point min = buildPoint( pgBox.getLLB(), crs );
            org.deegree.geometry.primitive.Point max = buildPoint( pgBox.getURT(), crs );
            env = new DefaultEnvelope( null, crs, null, min, max );
        }
        return env;
    }

    private org.deegree.geometry.primitive.Point buildPoint( org.postgis.Point p, ICRS crs ) {
        double[] coords = new double[p.getDimension()];
        coords[0] = p.getX();
        coords[1] = p.getY();
        if ( p.getDimension() > 2 ) {
            coords[2] = p.getZ();
        }
        return new DefaultPoint( null, crs, null, coords );
    }

    @Override
    public String[] getDDL( Object schema ) {
        return new PostGISDDLCreator( (MappedApplicationSchema) schema ).getDDL();
    }

    @Override
    public GeometryParticleConverter getGeometryConverter( String column, ICRS crs, String srid, boolean is2D ) {
        return new PostGISGeometryConverter( column, crs, srid, useLegacyPredicates );
    }

    @Override
    public PrimitiveParticleConverter getPrimitiveConverter( String column, PrimitiveType pt ) {
        return new DefaultPrimitiveConverter( pt, column );
    }
}