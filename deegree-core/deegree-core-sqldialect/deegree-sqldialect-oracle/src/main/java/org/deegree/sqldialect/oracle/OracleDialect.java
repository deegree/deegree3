//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.jdbc.TableName;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.sql.PrimitiveParticleConverter;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.cs.CRSUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.sort.SortProperty;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.utils.GeometryParticleConverter;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.filter.AbstractWhereBuilder;
import org.deegree.sqldialect.filter.PropertyNameMapper;
import org.deegree.sqldialect.filter.UnmappableException;
import org.slf4j.Logger;
import org.deegree.sqldialect.AbstractSQLDialect;

/**
 * {@link SQLDialect} for Oracle Spatial databases.
 * 
 * @see Oracle(R) Database SQL Reference 10g Release 2 (10.2) B14200-02 Chapter 2
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OracleDialect extends AbstractSQLDialect implements SQLDialect {

    private static final Logger LOG = getLogger( OracleDialect.class );

    private final String schema;

    private final int versionMajor;

    @SuppressWarnings("unused")
    private final int versionMinor;

    public OracleDialect( String schema, int major, int minor ) {
        this.schema = schema;
        this.versionMajor = major;
        this.versionMinor = minor;
    }

    @Override
    /**
     * Returns the maximum number of characters allowed for column names.
     * 
     * @return maximum number of characters
     */
    public int getMaxColumnNameLength() {
        return 30;
    }

    @Override
    /**
     * Returns the maximum number of characters allowed for table names.
     * 
     * @return maximum number of characters
     */
    public int getMaxTableNameLength() {
        return 30;
    }

    public String getDefaultSchema() {
        return schema;
    }

    public String stringPlus() {
        return "||";
    }

    public String stringIndex( String pattern, String string ) {
        // INSTR ( string , substring [, position [, occurrence ]] )
        return "INSTR(" + string + "," + pattern + ")";
    }

    public String cast( String expr, String type ) {
        return "CAST(" + expr + " AS " + type + ")";
    }

    @Override
    public String geometryMetadata( TableName qTable, String column, boolean isGeography ) {
        // return "SELECT 2, -1, 'GEOMETRY' FROM DUAL";

        StringBuilder sb = new StringBuilder();
        sb.append( "SELECT COUNT(1), X1.SRID, 'GEOMETRY' FROM " );

        if ( qTable.getSchema() != null )
            sb.append( "ALL_SDO_GEOM_METADATA " );
        else
            sb.append( "USER_SDO_GEOM_METADATA " );

        sb.append( "X1, TABLE( X1.DIMINFO ) X2 WHERE " );

        if ( qTable.getSchema() != null )
            sb.append( "OWNER='" ).append( qTable.getSchema() ).append( "' AND " );

        sb.append( "TABLE_NAME='" ).append( qTable.getTable() ).append( "' AND " );
        sb.append( "COLUMN_NAME='" ).append( column ).append( "' GROUP BY X1.TABLE_NAME, X1.COLUMN_NAME, X1.SRID" );
        return sb.toString();
    }

    @Override
    public AbstractWhereBuilder getWhereBuilder( PropertyNameMapper mapper, OperatorFilter filter,
                                                 SortProperty[] sortCrit, boolean allowPartialMappings )
                            throws UnmappableException, FilterEvaluationException {
        return new OracleWhereBuilder( this, mapper, filter, sortCrit, allowPartialMappings, versionMajor );
    }

    @Override
    public String getUndefinedSrid() {
        return "-1";
    }

    @Override
    public String getBBoxAggregateSnippet( String column ) {
        // The use of sdo_aggr_mbr is a bit problematic, since it could be slow and SDO_TUNE.EXTENT_OF is deprecated and
        // cannot be used inside a select on a table

        if ( 11 == versionMajor ) {
            return "SDO_AGGR_MBR(SDO_CS.MAKE_2D(" + column + "))";
        }
        return "SDO_AGGR_MBR(" + column + ")";
    }

    @Override
    public Envelope getBBoxAggregateValue( ResultSet rs, int colIdx, ICRS crs ) {
        try {
            Geometry p = new OracleGeometryConverter( null, crs, "0" ).toParticle( rs, colIdx );
            if ( p != null ) {
                return p.getEnvelope();
            }
        } catch ( SQLException e ) {
            LOG.trace( "Stack trace:", e );
        }
        LOG.warn( "Could not determine aggregated envelope, using world." );
        return new GeometryFactory().createEnvelope( -180, -90, 180, 90, CRSUtils.EPSG_4326 );
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
    public void createAutoColumn( StringBuffer currentStmt, List<StringBuffer> additionalSmts, SQLIdentifier column,
                                  SQLIdentifier table ) {
        currentStmt.append( column );
        currentStmt.append( " integer not null" );
        // TODO Markus!!!!!!!!!
        // TODO maybe very problematic, since object names from table_column_postfix can be to long
        additionalSmts.add( new StringBuffer( "create sequence " ).append( table ).append( "_" ).append( column ).append( "_seq start with 1 increment by 1 nomaxvalue" ) );
        additionalSmts.add( new StringBuffer( "create or replace trigger " ).append( table ).append( "_" ).append( column ).append( "_trigger before insert on " ).append( table ).append( " for each row begin select " ).append( table ).append( "_" ).append( column ).append( "_seq.nextval into :new." ).append( column ).append( " from dual; end;" ) );
    }

    @Override
    public ResultSet getTableColumnMetadata( DatabaseMetaData md, TableName qTable )
                            throws SQLException {
        String schema = qTable.getSchema() != null ? qTable.getSchema() : getDefaultSchema();
        String table = qTable.getTable();

        if ( versionMajor < 11 )
            return md.getColumns( null, schema.toUpperCase(), table.toUpperCase(), null );
        else
            return md.getColumns( null, schema, table, null );
    }

    @Override
    public boolean requiresTransactionForCursorMode() {
        return false;
    }

    @Override
    public String getSelectSequenceNextVal( String sequence ) {
        return "SELECT " + sequence + ".NEXTVAL from DUAL";
    }

    @Override
    public boolean isRowLimitingCapable() {
        return versionMajor < 12 ? false: true;
    }
}
