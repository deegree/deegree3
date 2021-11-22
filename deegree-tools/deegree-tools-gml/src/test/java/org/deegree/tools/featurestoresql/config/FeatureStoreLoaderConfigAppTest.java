package org.deegree.tools.featurestoresql.config;

import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeatureStoreLoaderConfigAppTest {

    final String SCHEMA_URL_CP_ARG = "-schemaUrl=http://inspire.ec.europa.eu/schemas/cp/4.0/CadastralParcels.xsd";

    @Test
    public void testMain_Empty() {
        String[] args = new String[] { "gmlLoader" };
        FeatureStoreLoaderConfigApp.run( args );
    }

    @Test
    public void testMain_H() {
        String[] args = new String[] { "gmlLoader", "-h" };
        FeatureStoreLoaderConfigApp.run( args );
    }

    @Test
    public void testMain_Help() {
        String[] args = new String[] { "gmlLoader", "-help" };
        FeatureStoreLoaderConfigApp.run( args );
    }

    @Test
    public void testMain_Help2() {
        String[] args = new String[] { "gmlLoader", "--help" };
        FeatureStoreLoaderConfigApp.run( args );
    }

    @Test
    public void schemaOnly() {
        String[] args = { "gmlLoader", SCHEMA_URL_CP_ARG };
        FeatureStoreLoaderConfigApp.run( args );
    }

    @Test
    public void schemaToDeegreeConfig() {
        String[] args = { "gmlLoader", SCHEMA_URL_CP_ARG, "-format=deegree" };
        FeatureStoreLoaderConfigApp.run( args );
    }

    @Test
    public void schemaToDeegreeConfigWithSrid() {
        String[] args = { "gmlLoader", SCHEMA_URL_CP_ARG, "-format=deegree", "-srid=31468" };
        FeatureStoreLoaderConfigApp.run( args );
    }

    @Test
    public void schemaToDeegreeConfigAndSqlDdl() {
        String[] args = { "gmlLoader", SCHEMA_URL_CP_ARG, "-format=all", "-srid=31468" };
        FeatureStoreLoaderConfigApp.run( args );
    }

    @Test
    public void schemaToDeegreeConfigAndSqlDdlAndIdTypeInteger() {
        String[] args = { "gmlLoader", SCHEMA_URL_CP_ARG, "-format=all", "-srid=4326", "-idtype=int" };
        FeatureStoreLoaderConfigApp.run( args );
    }

    @Test
    public void schemaToDeegreeConfigAndSqlDdlAndIdTypeUuid() {
        String[] args = { "gmlLoader", SCHEMA_URL_CP_ARG, "-format=all", "-srid=4326", "-idtype=uuid" };
        FeatureStoreLoaderConfigApp.run( args );
    }

    @Test
    public void schemaToSqlDdl() {
        String[] args = { "gmlLoader", SCHEMA_URL_CP_ARG, "-format=ddl" };
        FeatureStoreLoaderConfigApp.run( args );
    }

    @Test
    public void schemaToDeegreeConfigAndSqlDdlWithBlobMapping() {
        String[] args = { "gmlLoader", SCHEMA_URL_CP_ARG, "gmlLoader", "-format=all", "-mapping=blob" };
        FeatureStoreLoaderConfigApp.run( args );
    }

    @Test
    public void schemaToDeegreeConfigAndSqlDdlWithBlobMappingAndAllOptions() {
        String[] args = { "gmlLoader", SCHEMA_URL_CP_ARG, "-format=all", "-srid=4326", "-mapping=blob" };
        FeatureStoreLoaderConfigApp.run( args );
    }

    @Test
    public void schemaToDeegreeConfigAndSqlDdlWithForOracle() {
        String[] args = { "gmlLoader", SCHEMA_URL_CP_ARG, "-format=all", "-srid=4326", "-dialect=oracle" };
        FeatureStoreLoaderConfigApp.run( args );
    }

    @Test
    public void schemaToDeegreeConfigAndSqlDdlWithBlobMappingForOracle() {
        String[] args = { "gmlLoader", SCHEMA_URL_CP_ARG, "-format=all", "-srid=4326", "-mapping=blob",
                         "-dialect=oracle" };
        FeatureStoreLoaderConfigApp.run( args );
    }

    @Test
    public void schemaToDeegreeConfigAndListOfPropertiesWithPrimitiveHref() {
        String[] args = { "gmlLoader", SCHEMA_URL_CP_ARG, "-format=deegree",
                         "-listOfPropertiesWithPrimitiveHref=src/test/resources/listOfPropertiesWithPrimitiveHref" };
        FeatureStoreLoaderConfigApp.run( args );
    }

}