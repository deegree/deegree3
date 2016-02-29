package org.deegree.feature.persistence.sql.aixm;

import static org.deegree.protocol.wfs.transaction.action.IDGenMode.GENERATE_NEW;

import javax.xml.namespace.QName;

import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.persistence.sql.SQLFeatureStoreTestCase;

/**
 * Tests the query behaviour of the {@link SQLFeatureStore} for an AIXM configuration (relational mode).
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.4
 */
public class AixmQueryRelationalIT extends SQLFeatureStoreTestCase {

    private static final QName AIRSPACE_NAME = new QName( AIXM_NS, "Airspace" );

    private SQLFeatureStore fs;

    @Override
    public void setUp()
                            throws Exception {
        fs = setUpFeatureStore( "aixm-relational", "aixm/workspace" );
        importGml( fs, "aixm/data/Donlon.xml", GENERATE_NEW );
    }

    public void testQueryByGmlIdentifier()
                            throws Exception {
        final Query query = buildGmlIdentifierQuery( "010d8451-d751-4abb-9c71-f48ad024045b", AIRSPACE_NAME );
        final FeatureCollection fc = fs.query( query ).toCollection();
        assertEquals( 1, fc.size() );
    }

    public void testQueryByGmlIdentifierUnspecifiedFeatureType()
                            throws Exception {
        final Query query = buildGmlIdentifierQuery( "010d8451-d751-4abb-9c71-f48ad024045b", null );
        final FeatureCollection fc = fs.query( query ).toCollection();
        assertEquals( 1, fc.size() );
    }
}
