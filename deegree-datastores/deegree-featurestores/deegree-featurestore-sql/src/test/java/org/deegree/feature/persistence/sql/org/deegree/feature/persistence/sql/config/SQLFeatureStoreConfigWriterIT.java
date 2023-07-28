package org.deegree.feature.persistence.sql.org.deegree.feature.persistence.sql.config;

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
import org.xmlunit.builder.Input;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.EvaluateXPathMatcher.hasXPath;
import static org.xmlunit.matchers.ValidationMatcher.valid;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class SQLFeatureStoreConfigWriterIT {

	private static final int MAX_TABLE_NAME_LENGTH_POSTGRES = 63;

	private static final String UNDEFINED_SRID_POSTGRES = "0";

	@Test
	public void testWriteConfig() throws Exception {
		URL appSchemaResource = new URL("http://inspire.ec.europa.eu/schemas/ps/4.0/ProtectedSites.xsd");
		List<String> schemaUrls = new ArrayList<String>();
		AppSchema appSchema = readApplicationSchema(appSchemaResource);
		MappedAppSchema mappedSchema = mapApplicationSchema(appSchema);
		String config = writeConfig(mappedSchema, schemaUrls);

		assertThat(config, valid(
				Input.fromStream(getClass().getResourceAsStream("/META-INF/schemas/datasource/feature/sql/sql.xsd"))));
	}

	private static AppSchema readApplicationSchema(URL schemaUrl)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		GMLAppSchemaReader decoder = new GMLAppSchemaReader(null, null, schemaUrl.toString());
		return decoder.extractAppSchema();
	}

	private static String writeConfig(MappedAppSchema mappedSchema, List<String> schemaUrls)
			throws XMLStreamException, FactoryConfigurationError {
		SQLFeatureStoreConfigWriter configWriter = new SQLFeatureStoreConfigWriter(mappedSchema);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(bos);
		xmlWriter = new IndentingXMLStreamWriter(xmlWriter);
		configWriter.writeConfig(xmlWriter, "jdbcId", schemaUrls);
		xmlWriter.close();
		return bos.toString();
	}

	private static MappedAppSchema mapApplicationSchema(AppSchema appSchema) {
		ICRS crs = CRSManager.getCRSRef("EPSG:4326");
		GeometryStorageParams storageParams = new GeometryStorageParams(crs, UNDEFINED_SRID_POSTGRES, DIM_2);
		boolean createBlobMapping = false;
		boolean createRelationalMapping = true;
		AppSchemaMapper mapper = new AppSchemaMapper(appSchema, createBlobMapping, createRelationalMapping,
				storageParams, MAX_TABLE_NAME_LENGTH_POSTGRES, false, true);
		return mapper.getMappedSchema();
	}

	private Map<String, String> nsContext() {
		return Collections.singletonMap("fsc", "http://www.deegree.org/datasource/feature/sql");
	}

}