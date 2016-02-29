package org.deegree.feature.persistence.sql.aixm;

import static org.deegree.protocol.wfs.transaction.action.IDGenMode.GENERATE_NEW;

import org.deegree.commons.tom.gml.GMLReferenceResolver;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStoreGmlIdentifierResolver;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.persistence.sql.SQLFeatureStoreTestCase;

/**
 * Tests the behavior when resolving AIXM-style references using an {@link SQLFeatureStore}.(relational mode).
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.4
 */
public class GmlIdentifierReferenceResolverRelationalIT extends SQLFeatureStoreTestCase {

    private GMLReferenceResolver resolver;

    @Override
    public void setUp()
                            throws Exception {
        final SQLFeatureStore fs = setUpFeatureStore( "aixm-relational", "aixm/workspace" );
        importGml( fs, "aixm/data/Donlon.xml", GENERATE_NEW );
        resolver = new FeatureStoreGmlIdentifierResolver( fs );
    }

    public void testGetObject()
                            throws Exception {
        final Feature feature = (Feature) resolver.getObject( "urn:uuid:010d8451-d751-4abb-9c71-f48ad024045b", null );
        assertEquals( "010d8451-d751-4abb-9c71-f48ad024045b", getGmlIdentifier( feature ) );
    }

    public void testGetObjectThatDoesNotExist()
                            throws Exception {
        final Feature feature = (Feature) resolver.getObject( "urn:uuid:eeeeee-d751-4abb-9c71-f48ad024045b", null );
        assertNull( feature );
    }
}
