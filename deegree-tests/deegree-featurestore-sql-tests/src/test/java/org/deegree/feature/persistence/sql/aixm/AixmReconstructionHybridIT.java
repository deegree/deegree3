package org.deegree.feature.persistence.sql.aixm;

import static org.deegree.protocol.wfs.transaction.action.IDGenMode.GENERATE_NEW;

import java.io.IOException;

import javax.xml.namespace.QName;

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
public class AixmReconstructionHybridIT extends SQLFeatureStoreTestCase {

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

    public void testQueryAllAirports()
                            throws FeatureStoreException, FilterEvaluationException {

        final Query query = new Query( AIRPORT_NAME, null, -1, -1, -1 );
        final FeatureCollection fc = fs.query( query ).toCollection();
        assertEquals( 2, fc.size() );
    }

    public void testQueryAirspaceEamm2()
                            throws FeatureStoreException, FilterEvaluationException, IOException {
        final Query query = buildGmlIdentifierQuery( "010d8451-d751-4abb-9c71-f48ad024045b", AIRSPACE_NAME );
        final FeatureCollection fc = fs.query( query ).toCollection();

        assertEquals( 1, fc.size() );
        assertGmlEquals( fc.iterator().next(), "aixm/expected/airspace_eamm2.xml" );
    }

    public void testQueryVerticalStructureCrane5()
                            throws FeatureStoreException, FilterEvaluationException, IOException {
        final Query query = buildGmlIdentifierQuery( "8c755520-b42b-11e3-a5e2-0800500c9a66", VERTICAL_STRUCTURE_NAME );
        final FeatureCollection fc = fs.query( query ).toCollection();
        assertEquals( 1, fc.size() );
        System.out.println (new String (toGml( fc.iterator().next() )));
        assertGmlEquals( fc.iterator().next(), "aixm/expected/crane_5.xml" );
    }

}
