package org.deegree.feature.persistence.sql.aixm;

import static org.deegree.protocol.wfs.transaction.action.IDGenMode.GENERATE_NEW;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.persistence.sql.SQLFeatureStoreTestCase;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;

/**
 * Tests the transactional behaviour of the {@link SQLFeatureStore} for an AIXM configuration.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.4
 */
public class AixmTransactionHybridIT extends SQLFeatureStoreTestCase {

    private static final String DATASET_LOCATION = "aixm/data/Donlon.xml";

    private static final String FEATURE_STORE_ID = "aixm-relational";

    private static final String WORKSPACE_LOCATION = "aixm/workspace";

    private static final QName AIRSPACE_NAME = new QName( AIXM_NS, "Airspace" );

    private SQLFeatureStore fs;

    @Override
    public void setUp()
                            throws Exception {
        fs = setUpFeatureStore( FEATURE_STORE_ID, WORKSPACE_LOCATION );
        createTablesFromConfig( fs );
        importGml( fs, DATASET_LOCATION, GENERATE_NEW );
    }

    public void testDeleteAirspaceEamm2()
                            throws FeatureStoreException, FilterEvaluationException, IOException {

        // before: 13 Airspace features
        final Query query = new Query( AIRSPACE_NAME, null, -1, -1, -1 );
        final FeatureCollection fcBefore = fs.query( query ).toCollection();
        assertEquals( 13, fcBefore.size() );

        // delete one Airspace feature
        final Filter filter = buildGmlIdentifierFilter( "010d8451-d751-4abb-9c71-f48ad024045b" );
        final FeatureStoreTransaction ta = fs.acquireTransaction();
        try {
            ta.performDelete( AIRSPACE_NAME, (OperatorFilter) filter, null );
            ta.commit();
        } catch ( Exception e ) {
            ta.rollback();
        }

        // after: 12 Airspace features
        final FeatureCollection fcAfter = fs.query( query ).toCollection();
        assertEquals( 12, fcAfter.size() );
    }

}
