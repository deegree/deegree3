package org.deegree.feature.persistence.sql;

import static java.util.Collections.emptyList;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.deegree.cs.persistence.CRSManager.getCRSRef;
import static org.deegree.db.ConnectionProviderUtils.getSyntheticProvider;
import static org.deegree.feature.persistence.sql.PostGISSetupHelper.ADMIN_PASS;
import static org.deegree.feature.persistence.sql.PostGISSetupHelper.ADMIN_USER;
import static org.deegree.feature.persistence.sql.PostGISSetupHelper.HOST;
import static org.deegree.feature.persistence.sql.PostGISSetupHelper.PORT;
import static org.deegree.feature.persistence.sql.PostGISSetupHelper.TEST_DB;
import static org.deegree.feature.persistence.sql.PostGISSetupHelper.createTestDatabase;
import static org.deegree.feature.persistence.sql.PostGISSetupHelper.dropTestDatabase;
import static org.deegree.gml.GMLOutputFactory.createGMLStreamWriter;
import static org.deegree.gml.GMLVersion.GML_32;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.db.ConnectionProvider;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.sql.ddl.DDLCreator;
import org.deegree.filter.Filter;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.protocol.wfs.transaction.action.IDGenMode;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultResourceIdentifier;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.junit.Assert;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Abstract base class for integration tests of the {@link SQLFeatureStore}.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.4
 */
public abstract class SQLFeatureStoreTestCase extends XMLTestCase {

    protected static String GML_NS = "http://www.opengis.net/gml/3.2";

    protected static final String AIXM_NS = "http://www.aixm.aero/schema/5.1";

    protected static final QName GML_IDENTIFIER = QName.valueOf( "{http://www.opengis.net/gml/3.2}identifier" );

    private Workspace ws;

    /**
     * Destroys the test workspace and drops the test database.
     */
    @Override
    public void tearDown()
                            throws SQLException {
        ws.destroy();
        dropTestDatabase();
    }

    /**
     * Creates a test database and initializes the given feature store configuration from the test workspace on the
     * classpath.
     *
     * @param featureStoreId
     *            identifier of the feature store resource, must not be <code>null</code>
     * @param workspacePath
     *            resource path to the workspace (relative to {@link SQLFeatureStore} class), must not be
     *            <code>null</code>
     * @return initialized feature store (connected to the test database), never <code>null</code>
     */
    protected SQLFeatureStore setUpFeatureStore( final String featureStoreId, final String workspacePath )
                            throws Exception {
        createTestDatabase();
        ws = startWorkspaceOnClasspath( workspacePath );
        addConnectionProviderToWorkspace( ADMIN_USER, ADMIN_PASS, HOST, PORT, TEST_DB );
        return initFeatureStore( featureStoreId );
    }

    /**
     * Imports the specified GML dataset into the given {@link SQLFeatureStore}.
     *
     * @param fs
     *            feature store, must not be <code>null</code>
     * @param gmlResourceName
     *            resource name of the GML dataset, must not be <code>null</code>
     * @param idGenMode
     *            id generation mode, must not be <code>null</code>
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     * @throws XMLParsingException
     * @throws UnknownCRSException
     * @throws FeatureStoreException
     */
    protected void importGml( final FeatureStore fs, final String gmlResourceName, final IDGenMode idGenMode )
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException, FeatureStoreException {
        final URL datasetURL = SQLFeatureStoreTestCase.class.getResource( gmlResourceName );
        final GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GML_32, datasetURL );
        gmlReader.setApplicationSchema( fs.getSchema() );
        FeatureCollection fc = gmlReader.readFeatureCollection();
        gmlReader.close();

        final FeatureStoreTransaction ta = fs.acquireTransaction();
        ta.performInsert( fc, idGenMode );
        ta.commit();
    }

    /**
     * Creates a {@link Query} that targets the feature with the given <code>gml:id</code>.
     *
     * @param identifier
     *            value of the gml:id, must not be <code>null</code>
     * @param featureTypeName
     *            name of the feature type, must not be <code>null</code>
     * @return query instance, never <code>null</code>
     */
    protected Query buildGmlIdQuery( final String identifier, final QName featureTypeName ) {
        final Filter filter = new IdFilter( identifier );
        return new Query( featureTypeName, filter, -1, -1, -1 );
    }

    /**
     * Creates a {@link Query} that targets the feature with the given <code>gml:identifier</code>.
     *
     * @param identifier
     *            value of the gml:identifier, must not be <code>null</code>
     * @param featureTypeName
     *            name of the feature type, must not be <code>null</code>
     * @return query instance, never <code>null</code>
     */
    protected Query buildGmlIdentifierQuery( final String identifier, final QName featureTypeName ) {
        final Filter filter = buildGmlIdentifierFilter( identifier );
        return new Query( featureTypeName, filter, -1, -1, -1 );
    }

    /**
     * Creates a {@link Filter} that targets the given <code>gml:identifier</code>.
     *
     * @param identifier
     *            value of the gml:identifier, must not be <code>null</code>
     * @return filter instance, never <code>null</code>
     */
    protected Filter buildGmlIdentifierFilter( final String identifier ) {
        final ValueReference propName = new ValueReference( GML_IDENTIFIER );
        final Literal<PrimitiveValue> literal = new Literal<PrimitiveValue>( identifier );
        final PropertyIsEqualTo oper = new PropertyIsEqualTo( propName, literal, false, null );
        return new OperatorFilter( oper );
    }

    /**
     * Asserts that the given {@link GMLObject} is equal to the specified GML file.
     *
     * @param actualObject
     *            object to be tested, must not be <code>null</code>
     * @param expectedGmlResourceName
     *            name of tje GML resource to test against, must not be <code>null</code>
     */
    protected void assertGmlEquals( final GMLObject actualObject, final String expectedGmlResourceName ) {
        final byte[] actual = toGml( actualObject );
        final byte[] expected = getResource( expectedGmlResourceName );
        assertGmlEquals( expected, actual );
    }

    /**
     * Returns the gml:identifier of the given feature.
     *
     * @param feature
     *            must not be null and have a gml:identifier property
     * @return value of the gml:identifier, never <code>null</code>
     */
    protected String getGmlIdentifier( final Feature feature ) {
        final Property property = feature.getProperties( GML_IDENTIFIER ).get( 0 );
        final TypedObjectNode value = property.getValue();
        return "" + value;
    }

    private void assertGmlEquals( final byte[] expected, final byte[] actual ) {
        XMLUnit.setIgnoreComments( true );
        XMLUnit.setNormalizeWhitespace( true );
        final DetailedDiff diffs = createDetailedDiff( expected, actual );
        for ( final Object object : diffs.getAllDifferences() ) {
            final Difference diff = (Difference) object;
            final boolean isIgnorable = isIgnorable( diff );
            if ( !isIgnorable ) {
                writeUnexpectedResponse( actual );
                Assert.fail( diff.toString() );
            }
        }
    }

    private byte[] toGml( final GMLObject object ) {
        try {
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            XMLStreamWriter xmlStream = XMLOutputFactory.newInstance().createXMLStreamWriter( os );
            xmlStream = new IndentingXMLStreamWriter( xmlStream );
            writeGml( object, xmlStream );
            xmlStream.close();
            return os.toByteArray();
        } catch ( Exception e ) {
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    private void writeGml( final GMLObject object, final XMLStreamWriter xmlStream )
                            throws XMLStreamException, FactoryConfigurationError, UnknownCRSException,
                            TransformationException {
        final GMLStreamWriter gmlWriter = createGMLStreamWriter( GML_32, xmlStream );
        final Map<String, String> prefixToNs = new HashMap<String, String>();
        prefixToNs.put( "aixm", AIXM_NS );
        prefixToNs.put( "gml", GML_32.getNamespace() );
        prefixToNs.put( "gmd", "http://www.isotc211.org/2005/gmd" );
        prefixToNs.put( "gco", "http://www.isotc211.org/2005/gco" );
        gmlWriter.setNamespaceBindings( prefixToNs );
        gmlWriter.setOutputCrs( getCRSRef( "urn:ogc:def:crs:EPSG::4326" ) );
        gmlWriter.write( object );
    }

    private void writeUnexpectedResponse( final byte[] actual ) {
        try {
            final File file = File.createTempFile( "unexpected", ".gml" );
            System.out.println( "Unexpected result, writing to: " + file );
            FileUtils.writeByteArrayToFile( file, actual );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    private DetailedDiff createDetailedDiff( final byte[] expected, final byte[] actual ) {
        final ByteArrayInputStream expectedStream = new ByteArrayInputStream( expected );
        final InputSource expectedSource = new InputSource( expectedStream );
        final ByteArrayInputStream actualStream = new ByteArrayInputStream( actual );
        final InputSource actualSource = new InputSource( actualStream );
        try {
            return new DetailedDiff( compareXML( expectedSource, actualSource ) );
        } catch ( Exception e ) {
            writeUnexpectedResponse( actual );
            throw new RuntimeException( e );
        }
    }

    private boolean isIgnorable( final Difference diff ) {
        if ( isGmlCoordinateListElement( diff.getControlNodeDetail().getNode() ) ) {
            final String expectedValue = diff.getControlNodeDetail().getValue();
            final String actualValue = diff.getTestNodeDetail().getValue();
            return compareDoubleListsWithDelta( expectedValue, actualValue, 0.000001 );
        }
        final String xpathLocation = diff.getControlNodeDetail().getXpathLocation();
        if ( xpathLocation.endsWith( "@id" ) ) {
            return true;
        }
        return equals( diff.getControlNodeDetail().getNode(), diff.getTestNodeDetail().getNode() );
    }

    /**
     * Workaround for shortcoming in XMLUnit (xsi:nil="true" and xsi:nil="true") are not recognized as equal if the
     * namespace binding is not on the same level.
     */
    private boolean equals( final Node control, final Node test ) {
        if ( !control.getNodeName().equals( "xsi:nil" ) ) {
            return false;
        }
        if ( !test.getNodeName().equals( "xsi:nil" ) ) {
            return false;
        }
        if ( control.getNodeType() != test.getNodeType() ) {
            return false;
        }
        if ( !control.getLocalName().equals( test.getLocalName() ) ) {
            return false;
        }
        if ( !control.getNamespaceURI().equals( test.getNamespaceURI() ) ) {
            return false;
        }
        if ( control.getNodeValue() != null ) {
            return control.getNodeValue().equals( test.getNodeValue() );
        }
        return test.getNodeValue() == null;
    }

    private byte[] getResource( final String resourcePath ) {
        try {
            return toByteArray( SQLFeatureStoreTestCase.class.getResourceAsStream( resourcePath ) );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    private boolean isGmlCoordinateListElement( final Node xmlNode ) {
        return isDoubleList( xmlNode.getNodeValue() );
    }

    private boolean isDoubleList( final String s ) {
        return !parseAsDoubles( s ).isEmpty();
    }

    private boolean compareDoubleListsWithDelta( final String expected, final String actual, final double delta ) {
        final List<Double> expectedDoubles = parseAsDoubles( expected );
        final List<Double> actualDoubles = parseAsDoubles( actual );
        if ( actualDoubles.size() != expectedDoubles.size() ) {
            return false;
        }
        for ( int i = 0; i < expectedDoubles.size(); i++ ) {
            double actualDelta = expectedDoubles.get( i ) - actualDoubles.get( i );
            if ( Math.abs( actualDelta ) > delta ) {
                return false;
            }
        }
        return true;
    }

    private List<Double> parseAsDoubles( String s ) {
        if ( s == null ) {
            return Collections.emptyList();
        }
        final String[] tokens = s.trim().split( " " );
        final List<Double> doubles = new ArrayList<Double>();
        try {
            for ( final String token : tokens ) {
                if ( !token.trim().isEmpty() ) {
                    doubles.add( new Double( Double.parseDouble( token ) ) );
                }
            }
        } catch ( Exception e ) {
            return emptyList();
        }
        return doubles;
    }

    private Workspace startWorkspaceOnClasspath( final String workspacePath )
                            throws URISyntaxException {
        final URL url = SQLFeatureStoreTestCase.class.getResource( workspacePath );
        final File dir = new File( url.toURI() );
        final Workspace ws = new DefaultWorkspace( dir );
        ws.startup();
        return ws;
    }

    private void addConnectionProviderToWorkspace( final String user, final String pass, final String host,
                                                   final String port, final String dbName ) {
        final String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName + "?stringtype=unspecified";
        final ResourceLocation<ConnectionProvider> loc = getSyntheticProvider( "deegree-test", jdbcUrl, "postgres",
                                                                               "postgres" );
        ws.getLocationHandler().addExtraResource( loc );
    }

    private SQLFeatureStore initFeatureStore( final String id ) {
        ws.init( new DefaultResourceIdentifier<FeatureStore>( FeatureStoreProvider.class, id ), ws.prepare() );
        return (SQLFeatureStore) ws.getResource( FeatureStoreProvider.class, id );
    }

}
