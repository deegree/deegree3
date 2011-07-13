//$HeadURL: svn+ssh://criador.lat-lon.de/srv/svn/deegree-intern/trunk/latlon-sqldialect-oracle/src/main/java/de/latlon/deegree/sqldialect/oracle/OracleDialect.java $
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
package org.deegree.sqldialect.oracle;

import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.deegree.commons.jdbc.QTableName;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.sql.PrimitiveParticleConverter;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.cs.CRSUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.persistence.sql.MappedApplicationSchema;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.sql.AbstractWhereBuilder;
import org.deegree.filter.sql.PropertyNameMapper;
import org.deegree.filter.sql.UnmappableException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.utils.GeometryParticleConverter;
import org.deegree.sqldialect.SQLDialect;
import org.slf4j.Logger;

/**
 * {@link SQLDialect} for Oracle Spatial databases.
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: 348 $, $Date: 2011-07-01 18:02:24 +0200 (Fr, 01. Jul 2011) $
 */
public class OracleDialect implements SQLDialect {

    private static final Logger LOG = getLogger( OracleDialect.class );

    private final String user, version;

    public OracleDialect( String user, String version ) {
        this.user = user;
        this.version = version;
    }

    @Override
    public int getMaxColumnNameLength() {
        return 30;
    }

    @Override
    public int getMaxTableNameLength() {
        return 30;
    }

    public String getDefaultSchema() {
        return user;
    }

    public String stringPlus() {
        return "||";
    }

    public String stringIndex( String pattern, String string ) {
        return "INSTR(" + pattern + "," + string + ")";
    }

    public String cast( String expr, String type ) {
        return "CAST(" + expr + " AS " + type + ")";
    }

    @Override
    public String geometryMetadata( QTableName qTable, String column ) {
        // TODO: Andreas!!!!!!!!!
        return "SELECT 2, -1, 'GEOMETRY' from DUAL";
    }

    @Override
    public AbstractWhereBuilder getWhereBuilder( PropertyNameMapper mapper, OperatorFilter filter,
                                                 SortProperty[] sortCrit, boolean allowPartialMappings )
                            throws UnmappableException, FilterEvaluationException {
        return new OracleWhereBuilder( mapper, filter, sortCrit, allowPartialMappings );
    }

    @Override
    public String getUndefinedSrid() {
        return null;
    }

    @Override
    public String getBBoxAggregateSnippet( String column ) {
        if ( version.equals( "11" ) ) {
            return "SDO_AGGR_MBR(sdo_cs.make_2d(" + column + "))";
        }
        return "SDO_AGGR_MBR(" + column + ")";
    }

    @Override
    public Envelope getBBoxAggregateValue( ResultSet rs, int colIdx, ICRS crs ) {
        try {
            return new OracleGeometryConverter( null, crs, "0" ).toParticle( rs, colIdx ).getEnvelope();
        } catch ( SQLException e ) {
            LOG.warn( "Could not detemine aggregated envelope, using world." );
            LOG.trace( "Stack trace:", e );
        }
        return new GeometryFactory().createEnvelope( -180, -90, 180, 90, CRSUtils.EPSG_4326 );
    }

    @Override
    public String[] getDDL( Object schema ) {
        return new OracleDDLCreator( (MappedApplicationSchema) schema, this ).getDDL();
    }

    @Override
    public GeometryParticleConverter getGeometryConverter( String column, ICRS crs, String srid, boolean is2D ) {
        return new OracleGeometryConverter( column, crs, srid );
    }

    @Override
    public PrimitiveParticleConverter getPrimitiveConverter( String column, PrimitiveType pt ) {
        return new OraclePrimitiveConverter( pt, column );
    }

    @Override
    public void createDB( Connection adminConn, String dbName )
                            throws SQLException {

        Statement stmt = null;
        try {
            stmt = adminConn.createStatement();
            stmt.executeUpdate( "CREATE USER " + dbName + " IDENTIFIED BY " + dbName );
            stmt.executeUpdate( "GRANT CONNECT TO " + dbName );
            stmt.executeUpdate( "GRANT RESOURCE TO " + dbName );
        } finally {
            JDBCUtils.close( null, stmt, null, LOG );
        }
    }

    @Override
    public void dropDB( Connection adminConn, String dbName )
                            throws SQLException {

        Statement stmt = null;
        try {
            stmt = adminConn.createStatement();
            stmt.executeUpdate( "DROP USER " + dbName + " CASCADE" );
        } finally {
            JDBCUtils.close( null, stmt, null, LOG );
        }
    }

    @Override
    public void createAutoColumn( StringBuffer currentStmt, List<StringBuffer> additionalSmts, String column,
                                  String table ) {
        currentStmt.append( column );
        currentStmt.append( " integer not null" );
        // TODO Markus!!!!!!!!!
        additionalSmts.add( new StringBuffer( "create sequence " ).append( table ).append( "_" ).append( column ).append( "_seq start with 1 increment by 1 nomaxvalue" ) );
        additionalSmts.add( new StringBuffer( "create or replace trigger " ).append( table ).append( "_" ).append( column ).append( "_trigger before insert on " ).append( table ).append( " for each row begin select " ).append( table ).append( "_" ).append( column ).append( "_seq.nextval into :new." ).append( column ).append( " from dual; end;" ) );
    }

    @Override
    public ResultSet getTableColumnMetadata( DatabaseMetaData md, QTableName qTable )
                            throws SQLException {
        String schema = qTable.getSchema() != null ? qTable.getSchema() : getDefaultSchema();
        String table = qTable.getTable();
        return md.getColumns( null, schema.toUpperCase(), table.toUpperCase(), null );
    }
}