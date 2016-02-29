package org.deegree.feature.persistence.sql.aixm;

import static org.deegree.protocol.wfs.transaction.action.IDGenMode.USE_EXISTING;

import javax.xml.namespace.QName;

import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.persistence.sql.SQLFeatureStoreTestCase;

/**
 * Tests the query behaviour of the {@link SQLFeatureStore} for an AIXM configuration.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.4
 */
public class AixmQueryIT extends SQLFeatureStoreTestCase {

    private static final QName AIRSPACE_NAME = new QName( AIXM_NS, "Airspace" );

    private static final QName VERTICAL_STRUCTURE_NAME = new QName( AIXM_NS, "VerticalStructure" );

    private SQLFeatureStore fs;

    @Override
    public void setUp()
                            throws Exception {
        fs = setUpFeatureStore( "aixm-blob", "aixm/workspace" );
        importGml( fs, "aixm/data/Donlon.xml", USE_EXISTING );
    }

    public void testQueryVerticalStructureCrane5() throws Exception {
        final Query query = buildGmlIdQuery( "uuid.8c755520-b42b-11e3-a5e2-0800500c9a66", VERTICAL_STRUCTURE_NAME );
        final FeatureCollection fc = fs.query( query ).toCollection();
        assertEquals( 1, fc.size() );
        assertGmlEquals( fc.iterator().next(), "aixm/expected/crane_5.xml" );
    }

    public void testQueryAirspaceEamm2() throws Exception {
        final Query query = buildGmlIdQuery( "uuid.010d8451-d751-4abb-9c71-f48ad024045b", AIRSPACE_NAME );
        final FeatureCollection fc = fs.query( query ).toCollection();
        assertEquals( 1, fc.size() );
        assertGmlEquals( fc.iterator().next(), "aixm/expected/airspace_eamm2.xml" );
    }

    public void testQueryByGmlIdentifier() throws Exception {
        final Query query = buildGmlIdentifierQuery( "010d8451-d751-4abb-9c71-f48ad024045b", AIRSPACE_NAME );
        final FeatureCollection fc = fs.query( query ).toCollection();
        assertEquals( 1, fc.size() );
        assertGmlEquals( fc.iterator().next(), "aixm/expected/airspace_eamm2.xml" );
    }

    public void testQueryByGmlIdentifierUnspecifiedFeatureType() throws Exception {
        final Query query = buildGmlIdentifierQuery( "010d8451-d751-4abb-9c71-f48ad024045b", null );
        final FeatureCollection fc = fs.query( query ).toCollection();
        assertEquals( 1, fc.size() );
        assertGmlEquals( fc.iterator().next(), "aixm/expected/airspace_eamm2.xml" );
    }
}
