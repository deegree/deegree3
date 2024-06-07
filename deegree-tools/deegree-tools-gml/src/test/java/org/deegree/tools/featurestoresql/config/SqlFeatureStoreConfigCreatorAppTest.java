package org.deegree.tools.featurestoresql.config;

import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class SqlFeatureStoreConfigCreatorAppTest {

	final String SCHEMA_URL_CP_ARG = "-schemaUrl=http://inspire.ec.europa.eu/schemas/cp/4.0/CadastralParcels.xsd";

	@Test
	public void testMain_Empty() throws Exception {
		String[] args = new String[] { "gmlLoader" };
		SqlFeatureStoreConfigCreatorApp.run(args);
	}

	@Test
	public void testMain_H() throws Exception {
		String[] args = new String[] { "gmlLoader", "-h" };
		SqlFeatureStoreConfigCreatorApp.run(args);
	}

	@Test
	public void testMain_Help() throws Exception {
		String[] args = new String[] { "gmlLoader", "-help" };
		SqlFeatureStoreConfigCreatorApp.run(args);
	}

	@Test
	public void testMain_Help2() throws Exception {
		String[] args = new String[] { "gmlLoader", "--help" };
		SqlFeatureStoreConfigCreatorApp.run(args);
	}

	@Test
	public void schemaOnly() throws Exception {
		String[] args = { "gmlLoader", SCHEMA_URL_CP_ARG };
		SqlFeatureStoreConfigCreatorApp.run(args);
	}

	@Test
	public void schemaToDeegreeConfig() throws Exception {
		String[] args = { "gmlLoader", SCHEMA_URL_CP_ARG, "-format=deegree" };
		SqlFeatureStoreConfigCreatorApp.run(args);
	}

	@Test
	public void schemaToDeegreeConfigWithSrid() throws Exception {
		String[] args = { "gmlLoader", SCHEMA_URL_CP_ARG, "-format=deegree", "-srid=31468" };
		SqlFeatureStoreConfigCreatorApp.run(args);
	}

	@Test
	public void schemaToDeegreeConfigAndSqlDdl() throws Exception {
		String[] args = { "gmlLoader", SCHEMA_URL_CP_ARG, "-format=all", "-srid=31468" };
		SqlFeatureStoreConfigCreatorApp.run(args);
	}

	@Test
	public void schemaToDeegreeConfigAndSqlDdlAndIdTypeInteger() throws Exception {
		String[] args = { "gmlLoader", SCHEMA_URL_CP_ARG, "-format=all", "-srid=4326", "-idtype=int" };
		SqlFeatureStoreConfigCreatorApp.run(args);
	}

	@Test
	public void schemaToDeegreeConfigAndSqlDdlAndIdTypeUuid() throws Exception {
		String[] args = { "gmlLoader", SCHEMA_URL_CP_ARG, "-format=all", "-srid=4326", "-idtype=uuid" };
		SqlFeatureStoreConfigCreatorApp.run(args);
	}

	@Test
	public void schemaToSqlDdl() throws Exception {
		String[] args = { "gmlLoader", SCHEMA_URL_CP_ARG, "-format=ddl" };
		SqlFeatureStoreConfigCreatorApp.run(args);
	}

	@Test
	public void schemaToDeegreeConfigAndSqlDdlWithBlobMapping() throws Exception {
		String[] args = { "gmlLoader", SCHEMA_URL_CP_ARG, "gmlLoader", "-format=all", "-mapping=blob" };
		SqlFeatureStoreConfigCreatorApp.run(args);
	}

	@Test
	public void schemaToDeegreeConfigAndSqlDdlWithBlobMappingAndAllOptions() throws Exception {
		String[] args = { "gmlLoader", SCHEMA_URL_CP_ARG, "-format=all", "-srid=4326", "-mapping=blob" };
		SqlFeatureStoreConfigCreatorApp.run(args);
	}

	@Test
	public void schemaToDeegreeConfigAndSqlDdlWithForOracle() throws Exception {
		String[] args = { "gmlLoader", SCHEMA_URL_CP_ARG, "-format=all", "-srid=4326", "-dialect=oracle" };
		SqlFeatureStoreConfigCreatorApp.run(args);
	}

	@Test
	public void schemaToDeegreeConfigAndSqlDdlWithBlobMappingForOracle() throws Exception {
		String[] args = { "gmlLoader", SCHEMA_URL_CP_ARG, "-format=all", "-srid=4326", "-mapping=blob",
				"-dialect=oracle" };
		SqlFeatureStoreConfigCreatorApp.run(args);
	}

	@Test
	public void schemaToDeegreeConfigAndListOfPropertiesWithPrimitiveHref() throws Exception {
		String[] args = { "gmlLoader", SCHEMA_URL_CP_ARG, "-format=deegree",
				"-listOfPropertiesWithPrimitiveHref=src/test/resources/listOfPropertiesWithPrimitiveHref" };
		SqlFeatureStoreConfigCreatorApp.run(args);
	}

}