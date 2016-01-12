package org.deegree.feature.persistence.sql;

import static org.deegree.commons.utils.JDBCUtils.close;
import static org.deegree.gml.GMLVersion.GML_32;
import static org.deegree.protocol.wfs.transaction.action.IDGenMode.USE_EXISTING;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.sql.ddl.DDLCreator;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;

class DbSetupHelper {

    static void createPostgisDb( final String user, final String pass, final String host, final String adminDb,
                                 final String newDb )
                            throws SQLException {
        createPostgresDb( user, pass, host, adminDb, newDb );
        addPostgisExtension( user, pass, host, newDb );
    }

    static void dropPostgisDb( final String user, final String pass, final String host, final String adminDb,
                               final String dropDb )
                            throws SQLException {
        final Connection adminConn = getConnection( user, pass, host, adminDb );
        Statement stmt = null;
        try {
            stmt = adminConn.createStatement();
            stmt.execute( "DROP DATABASE \"" + dropDb + "\"" );
        } finally {
            close( null, stmt, adminConn, null );
        }
    }

    static void createTablesFromConfig( final SQLFeatureStore fs )
                            throws Exception {
        final String[] ddl = DDLCreator.newInstance( fs.getSchema(), fs.getDialect() ).getDDL();
        final Connection conn = fs.getConnection();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            for ( final String sql : ddl ) {
                stmt.execute( sql );
            }
            conn.commit();
        } finally {
            stmt.close();
            conn.close();
        }
    }

    static void importGml( final FeatureStore fs, final URL datasetURL )
                            throws Throwable {
        final GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GML_32, datasetURL );
        gmlReader.setApplicationSchema( fs.getSchema() );
        FeatureCollection fc = gmlReader.readFeatureCollection();
        gmlReader.close();

        final FeatureStoreTransaction ta = fs.acquireTransaction();
        ta.performInsert( fc, USE_EXISTING );
        ta.commit();
    }

    private static void createPostgresDb( final String user, final String pass, final String host,
                                          final String adminDb, final String newDb )
                            throws SQLException {
        final Connection adminConn = getConnection( user, pass, host, adminDb );
        Statement stmt = null;
        try {
            stmt = adminConn.createStatement();
            stmt.execute( "CREATE DATABASE \"" + newDb + "\"" );
        } finally {
            close( null, stmt, adminConn, null );
        }
    }

    private static void addPostgisExtension( final String user, final String pass, final String host, final String db )
                            throws SQLException {
        final Connection adminConn = getConnection( user, pass, host, db );
        Statement stmt = null;
        try {
            stmt = adminConn.createStatement();
            stmt.execute( "CREATE EXTENSION postgis" );
        } finally {
            close( null, stmt, adminConn, null );
        }
    }

    private static Connection getConnection( final String user, final String pass, final String host,
                                             final String dbName )
                            throws SQLException {
        final String url = "jdbc:postgresql://" + host + "/" + dbName;
        return DriverManager.getConnection( url, user, pass );
    }

}
