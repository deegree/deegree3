package org.deegree.feature.persistence.sql.ddl;

import static org.deegree.commons.utils.JDBCUtils.close;
import static org.deegree.commons.utils.JDBCUtils.rollbackQuietly;
import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.deegree.commons.jdbc.TableName;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.db.ConnectionProvider;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.MappedAppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.sqldialect.SQLDialect;
import org.slf4j.Logger;

public class DbSetupUtils {

    private static final Logger LOG = getLogger( DbSetupUtils.class );

    public static void createTables( final MappedAppSchema schema, final SQLDialect dialect,
                                     final ConnectionProvider connProvider )
                            throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = connProvider.getConnection();
            conn.setAutoCommit( false );
            stmt = conn.createStatement();
            final DDLCreator ddlCreator = DDLCreator.newInstance( schema, dialect );
            stmt = conn.createStatement();
            for ( final String sql : ddlCreator.getDDL() ) {
                LOG.info( "Executing: " + sql );
                stmt.execute( sql );
            }
            conn.commit();
        } catch ( SQLException e ) {
            rollbackQuietly( conn );
            String msg = "Error creating tables: " + e.getMessage();
            throw new SQLException( msg, e );
        } finally {
            close( null, stmt, conn, LOG );
        }
    }

    @SuppressWarnings("resource")
    public static boolean isTableCreationNeeded( final MappedAppSchema schema, final ConnectionProvider connProvider ) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = connProvider.getConnection();
            stmt = conn.createStatement();
            if ( schema.getBlobMapping() != null ) {
                final TableName table = schema.getBlobMapping().getTable();
                return !isTablePresent( table, conn, stmt );
            } else {
                for ( final FeatureType ft : schema.getFeatureTypes() ) {
                    final FeatureTypeMapping ftMapping = schema.getFtMapping( ft.getName() );
                    if ( ftMapping != null ) {
                        final TableName table = ftMapping.getFtTable();
                        return !isTablePresent( table, conn, stmt );
                    }
                }
            }
        } catch ( SQLException e ) {
            // nothing to do
        } finally {
            close( null, stmt, conn, LOG );
        }
        return false;
    }

    private static boolean isTablePresent( final TableName table, final Connection conn, final Statement stmt ) {
        final String sql = "SELECT 1 FROM " + table + " WHERE 1=0";
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery( sql );
        } catch ( SQLException e ) {
            return false;
        } finally {
            JDBCUtils.close( rs );
        }
        return true;
    }

}
