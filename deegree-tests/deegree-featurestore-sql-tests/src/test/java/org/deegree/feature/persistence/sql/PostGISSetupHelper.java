package org.deegree.feature.persistence.sql;

import static org.deegree.commons.utils.JDBCUtils.close;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Methods tor creating and dropping PostGIS test databases.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.4
 */
public class PostGISSetupHelper {

    final static String TEST_DB = "deegree-test";

    final static String ADMIN_USER = "postgres";

    final static String ADMIN_PASS = "postgres";

    final static String ADMIN_DB = "postgres";

    final static String HOST = "localhost";

    final static String PORT = "5432";

    /**
     * Creates a new PostgreSQL test database with PostGIS extension.
     */
    static void createTestDatabase()
                            throws SQLException {
        dropTestDatabase();
        createPostgresDb( ADMIN_USER, ADMIN_PASS, HOST, PORT, ADMIN_DB, TEST_DB );
        addPostgisExtension( ADMIN_USER, ADMIN_PASS, HOST, PORT, TEST_DB );
    }

    /**
     * Drops the PostgreSQL test database.
     */
    static void dropTestDatabase()
                            throws SQLException {
        final Connection adminConn = getConnection( ADMIN_USER, ADMIN_PASS, HOST, PORT, ADMIN_DB );
        Statement stmt = null;
        try {
            stmt = adminConn.createStatement();
            stmt.execute( "DROP DATABASE IF EXISTS \"" + TEST_DB + "\"" );
        } finally {
            close( null, stmt, adminConn, null );
        }
    }

    private static void createPostgresDb( final String user, final String pass, final String host, final String port,
                                          final String adminDb, final String newDb )
                            throws SQLException {
        final Connection adminConn = getConnection( user, pass, host, port, adminDb );
        Statement stmt = null;
        try {
            stmt = adminConn.createStatement();
            stmt.execute( "CREATE DATABASE \"" + newDb + "\"" );
        } finally {
            close( null, stmt, adminConn, null );
        }
    }

    private static void addPostgisExtension( final String user, final String pass, final String host,
                                             final String port, final String db )
                            throws SQLException {
        final Connection adminConn = getConnection( user, pass, host, port, db );
        Statement stmt = null;
        try {
            stmt = adminConn.createStatement();
            stmt.execute( "CREATE EXTENSION postgis" );
        } finally {
            close( null, stmt, adminConn, null );
        }
    }

    private static Connection getConnection( final String user, final String pass, final String host,
                                             final String port, final String dbName )
                            throws SQLException {
        final String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
        return DriverManager.getConnection( url, user, pass );
    }

}
