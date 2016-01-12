package org.deegree.feature.persistence.sql;

import static org.deegree.db.ConnectionProviderUtils.getSyntheticProvider;
import static org.deegree.feature.persistence.sql.DbSetupHelper.createPostgisDb;
import static org.deegree.feature.persistence.sql.DbSetupHelper.createTablesFromConfig;
import static org.deegree.feature.persistence.sql.DbSetupHelper.dropPostgisDb;
import static org.deegree.feature.persistence.sql.DbSetupHelper.importGml;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.namespace.QName;

import org.deegree.db.ConnectionProvider;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.feature.persistence.query.Query;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultResourceIdentifier;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AixmQueryIT {

    private static final QName AIRPORT_NAME = QName.valueOf( "{http://www.aixm.aero/schema/5.1}AirportHeliport" );

    private static final QName GML_IDENTIFIER = QName.valueOf( "{http://www.opengis.net/gml/3.2}identifier" );

    private Workspace ws;

    private SQLFeatureStore fs;

    @Before
    public void setup() throws Throwable {
        createPostgisDb( "postgres", "postgres", "localhost", "postgres", "deegree-test" );
        ws = startWorkspaceOnClasspath();
        addConnectionProviderToWorkspace( "postgres", "postgres", "localhost", "deegree-test" );
        fs = initFeatureStore( "aixm-blob" );
        createTablesFromConfig( fs );
        final URL datasetURL = AixmQueryIT.class.getResource( "aixm/data/Donlon.xml" );
        importGml( fs, datasetURL );
    }

    @After
    public void tearDown() throws Throwable {
        ws.destroy();
        dropPostgisDb( "postgres", "postgres", "localhost", "postgres", "deegree-test" );
    }

    @Test
    public void queryAllAirports()
                            throws FeatureStoreException, FilterEvaluationException {

        Query query = new Query( AIRPORT_NAME, null, -1, -1, -1 );
        FeatureCollection fc = fs.query( query ).toCollection();
        assertEquals( 2, fc.size() );
    }

    private Workspace startWorkspaceOnClasspath()
                            throws URISyntaxException {
        final URL url = AixmQueryIT.class.getResource( "/org/deegree/feature/persistence/sql/aixm" );
        final File dir = new File( url.toURI() );
        final Workspace ws = new DefaultWorkspace( dir );
        ws.startup();
        return ws;
    }

    private void addConnectionProviderToWorkspace(final String user, final String pass, final String host, final String dbName) {
        final String jdbcUrl = "jdbc:postgresql://" + host + "/" + dbName + "?stringtype=unspecified";
        final ResourceLocation<ConnectionProvider> loc = getSyntheticProvider( "deegree-test", jdbcUrl, "postgres", "postgres");
        ws.getLocationHandler().addExtraResource( loc );
    }

    private SQLFeatureStore initFeatureStore( final String id ) {
        ws.init( new DefaultResourceIdentifier<FeatureStore>( FeatureStoreProvider.class, id ), ws.prepare() );
        return (SQLFeatureStore) ws.getResource( FeatureStoreProvider.class, id );
    }

}
