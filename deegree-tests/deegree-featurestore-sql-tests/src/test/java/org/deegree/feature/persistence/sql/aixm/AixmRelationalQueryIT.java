package org.deegree.feature.persistence.sql.aixm;

import static org.deegree.protocol.wfs.transaction.action.IDGenMode.GENERATE_NEW;

import java.io.IOException;
import java.sql.SQLException;

import javax.xml.namespace.QName;

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.persistence.sql.SQLFeatureStoreTestCase;
import org.deegree.filter.FilterEvaluationException;

/**
 * Tests the query behaviour of the {@link SQLFeatureStore} for an AIXM configuration.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.4
 */
public class AixmRelationalQueryIT extends SQLFeatureStoreTestCase {

    private static final QName AIRPORT_NAME = new QName( AIXM_NS, "AirportHeliport" );

    private static final QName AIRSPACE_NAME = new QName( AIXM_NS, "Airspace" );

    private static final QName VERTICAL_STRUCTURE_NAME = new QName( AIXM_NS, "VerticalStructure" );

    private SQLFeatureStore fs;

    @Override
    public void setUp()
                            throws Exception {
        fs = setUpFeatureStore( "aixm-relational", "aixm/workspace" );
        createTablesFromConfig( fs );
        importGml( fs, "aixm/data/Donlon.xml", GENERATE_NEW );
    }

    @Override
    public void tearDown()
                            throws SQLException {
//        super.tearDown();
    }

    public void testQueryVerticalStructureCrane5()
                            throws FeatureStoreException, FilterEvaluationException, IOException {
        final Query query = buildGmlIdentifierQuery( "8c755520-b42b-11e3-a5e2-0800500c9a66", VERTICAL_STRUCTURE_NAME );
        final FeatureCollection fc = fs.query( query ).toCollection();
        assertEquals( 1, fc.size() );
        assertGmlEquals( fc.iterator().next(), "aixm/expected/crane_5.xml" );
    }

}