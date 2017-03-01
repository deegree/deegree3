package org.deegree.feature.persistence.sql.org.deegree.feature.persistence.sql.config;

import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.persistence.sql.GeometryStorageParams;
import org.deegree.feature.persistence.sql.MappedAppSchema;
import org.deegree.feature.persistence.sql.config.SQLFeatureStoreConfigWriter;
import org.deegree.feature.persistence.sql.mapper.AppSchemaMapper;
import org.deegree.feature.types.AppSchema;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xmlmatchers.validation.SchemaFactory;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.deegree.commons.xml.CommonNamespaces.getNamespaceContext;
import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.xmlmatchers.XmlMatchers.conformsTo;
import static org.xmlmatchers.XmlMatchers.hasXPath;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class SQLFeatureStoreConfigWriterIT {

    private static final int MAX_TABLE_NAME_LENGTH_POSTGRES = 63;

    private static final String UNDEFINED_SRID_POSTGRES = "0";

    private static final NamespaceBindings NAMESPACE_CONTEXT = getNamespaceContext();

    static {
        NAMESPACE_CONTEXT.addNamespace( "fsc", "http://www.deegree.org/datasource/feature/sql" );
    }

    @Test
    public void testWriteConfig()
                            throws Exception {
        URL appSchemaResource = new URL( "http://inspire.ec.europa.eu/schemas/ps/4.0/ProtectedSites.xsd" );
        List<String> schemaUrls = new ArrayList<String>();
        AppSchema appSchema = readApplicationSchema( appSchemaResource );
        MappedAppSchema mappedSchema = mapApplicationSchema( appSchema );
        byte[] config = writeConfig( mappedSchema, schemaUrls );

        assertThat( the( config ), hasXPath( "/fsc:SQLFeatureStore/@configVersion",
                                             NAMESPACE_CONTEXT, is( "3.4.0" ) ) );
        assertThat( the( config ), conformsTo( sqlFeatureStore34Schema() ) );
    }

    private Source the( byte[] config ) {
        StreamSource streamSource = new StreamSource();
        streamSource.setInputStream( new ByteArrayInputStream( config ) );
        return streamSource;
    }

    private Schema sqlFeatureStore34Schema()
                            throws SAXException {

        URL schemaResource = SQLFeatureStoreConfigWriterIT.class.getResource(
                                "/META-INF/schemas/datasource/feature/sql/3.4.0/sql.xsd" );
        return SchemaFactory.w3cXmlSchemaFrom( schemaResource );
    }

    private static AppSchema readApplicationSchema( URL schemaUrl )
                            throws ClassNotFoundException, InstantiationException, IllegalAccessException,
                            MalformedURLException {
        GMLAppSchemaReader decoder = new GMLAppSchemaReader( null, null, schemaUrl.toString() );
        return decoder.extractAppSchema();
    }

    private static byte[] writeConfig( MappedAppSchema mappedSchema, List<String> schemaUrls )
                            throws IOException, XMLStreamException, FactoryConfigurationError {
        SQLFeatureStoreConfigWriter configWriter = new SQLFeatureStoreConfigWriter( mappedSchema );
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( bos );
        xmlWriter = new IndentingXMLStreamWriter( xmlWriter );
        configWriter.writeConfig( xmlWriter, "jdbcId", schemaUrls );
        xmlWriter.close();
        return bos.toByteArray();
    }

    private static StreamSource asSource( byte[] bytes ) {
        StreamSource streamSource = new StreamSource();
        streamSource.setInputStream( new ByteArrayInputStream( bytes ) );
        return streamSource;
    }

    private static MappedAppSchema mapApplicationSchema( AppSchema appSchema ) {
        ICRS crs = CRSManager.getCRSRef( "EPSG:4326" );
        GeometryStorageParams storageParams = new GeometryStorageParams( crs, UNDEFINED_SRID_POSTGRES, DIM_2 );
        boolean createBlobMapping = false;
        boolean createRelationalMapping = true;
        AppSchemaMapper mapper = new AppSchemaMapper( appSchema, createBlobMapping, createRelationalMapping,
                                                      storageParams, MAX_TABLE_NAME_LENGTH_POSTGRES, false, true );
        return mapper.getMappedSchema();
    }

}