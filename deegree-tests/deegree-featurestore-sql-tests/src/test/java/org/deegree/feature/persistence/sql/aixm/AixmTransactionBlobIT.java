package org.deegree.feature.persistence.sql.aixm;

import static org.deegree.gml.GMLVersion.GML_32;
import static org.deegree.protocol.wfs.transaction.action.IDGenMode.USE_EXISTING;
import static org.deegree.protocol.wfs.transaction.action.UpdateAction.INSERT_AFTER;
import static org.deegree.protocol.wfs.transaction.action.UpdateAction.INSERT_BEFORE;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.persistence.sql.SQLFeatureStoreTestCase;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.expression.ValueReference;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.protocol.wfs.transaction.action.ParsedPropertyReplacement;
import org.deegree.protocol.wfs.transaction.action.UpdateAction;

/**
 * Tests the transactional behaviour of the {@link SQLFeatureStore} for an AIXM configuration.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.4
 */
public class AixmTransactionBlobIT extends SQLFeatureStoreTestCase {

    private static final String DATASET_LOCATION = "aixm/data/Donlon.xml";

    private static final String FEATURE_STORE_ID = "aixm-blob";

    private static final String WORKSPACE_LOCATION = "aixm/workspace";

    private static final QName AIRSPACE_NAME = new QName( AIXM_NS, "Airspace" );

    private static final QName VERTICAL_STRUCTURE_NAME = new QName( AIXM_NS, "VerticalStructure" );

    private static final QName TIMESLICE_PROP_NAME = new QName( AIXM_NS, "timeSlice" );

    private SQLFeatureStore fs;

    @Override
    public void setUp()
                            throws Exception {
        fs = setUpFeatureStore( FEATURE_STORE_ID, WORKSPACE_LOCATION );
        importGml( fs, DATASET_LOCATION, USE_EXISTING );
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

    public void testUpdateCrane5InsertTimeSliceAfter()
                            throws Exception {

        // before: CRANE 5 has 5 time slices
        final Query query = buildGmlIdentifierQuery( "8c755520-b42b-11e3-a5e2-0800500c9a66", VERTICAL_STRUCTURE_NAME );
        final Feature featureBefore = fs.query( query ).toCollection().iterator().next();
        assertEquals( 5, featureBefore.getProperties( TIMESLICE_PROP_NAME ).size() );

        // add another timeslice via update
        final Filter filter = buildGmlIdentifierFilter( "8c755520-b42b-11e3-a5e2-0800500c9a66" );
        final FeatureStoreTransaction ta = fs.acquireTransaction();
        try {
            final List<ParsedPropertyReplacement> replacements = new ArrayList<ParsedPropertyReplacement>();
            final PropertyType propDecl = featureBefore.getProperties( TIMESLICE_PROP_NAME ).get( 0 ).getType();
            final Property newProp = readGmlProperty( "aixm/data/crane5_vertical_structure_timeslice.xml", propDecl );
            final UpdateAction action = INSERT_AFTER;
            final ValueReference path = new ValueReference( TIMESLICE_PROP_NAME );
            final int index = 5;
            final ParsedPropertyReplacement replacement = new ParsedPropertyReplacement( newProp, action, path, index );
            replacements.add( replacement );
            ta.performUpdate( VERTICAL_STRUCTURE_NAME, replacements, filter, null );
            ta.commit();
        } catch ( Exception e ) {
            ta.rollback();
            throw e;
        }

        // after: CRANE 5 has 6 time slices (and the new one is last)
        final Feature featureAfter = fs.query( query ).toCollection().iterator().next();
        assertEquals( 6, featureAfter.getProperties( TIMESLICE_PROP_NAME ).size() );
        assertGmlEquals( featureAfter, "aixm/expected/crane_5_timeslice_added_after.xml" );
    }

    public void testUpdateCrane5InsertTimeSliceBefore()
                            throws Exception {

        // before: CRANE 5 has 5 time slices
        final Query query = buildGmlIdentifierQuery( "8c755520-b42b-11e3-a5e2-0800500c9a66", VERTICAL_STRUCTURE_NAME );
        final Feature featureBefore = fs.query( query ).toCollection().iterator().next();
        assertEquals( 5, featureBefore.getProperties( TIMESLICE_PROP_NAME ).size() );

        // add another timeslice via update
        final Filter filter = buildGmlIdentifierFilter( "8c755520-b42b-11e3-a5e2-0800500c9a66" );
        final FeatureStoreTransaction ta = fs.acquireTransaction();
        try {
            final List<ParsedPropertyReplacement> replacments = new ArrayList<ParsedPropertyReplacement>();
            final PropertyType propDecl = featureBefore.getProperties( TIMESLICE_PROP_NAME ).get( 0 ).getType();
            final Property newProp = readGmlProperty( "aixm/data/crane5_vertical_structure_timeslice.xml", propDecl );
            final UpdateAction action = INSERT_BEFORE;
            final ValueReference path = new ValueReference( TIMESLICE_PROP_NAME );
            final int index = 1;
            final ParsedPropertyReplacement replacement = new ParsedPropertyReplacement( newProp, action, path, index );
            replacments.add( replacement );
            ta.performUpdate( VERTICAL_STRUCTURE_NAME, replacments, filter, null );
            ta.commit();
        } catch ( Exception e ) {
            ta.rollback();
            throw e;
        }

        // after: CRANE 5 has 6 time slices (and the new one is first)
        final Feature featureAfter = fs.query( query ).toCollection().iterator().next();
        assertEquals( 6, featureAfter.getProperties( TIMESLICE_PROP_NAME ).size() );
        assertGmlEquals( featureAfter, "aixm/expected/crane_5_timeslice_added_before.xml" );
    }

    protected Property readGmlProperty( final String location, final PropertyType propDecl )
                            throws Exception {
        final URL url = SQLFeatureStoreTestCase.class.getResource( location );
        final GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GML_32, url );
        gmlReader.setApplicationSchema( fs.getSchema() );
        final XMLStreamReaderWrapper xmlStream = new XMLStreamReaderWrapper( gmlReader.getXMLReader(), null );
        final ICRS crs = null;
        return gmlReader.getFeatureReader().parseProperty( xmlStream, propDecl, crs );
    }

}
