package org.deegree.tools.featurestoresql.loader;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GmlLoaderHelpUsage {

    public static void printUsage() {
        System.out.println( "Usage: java -jar deegree-gml-tool.jar -pathToFile=<path/to/gmlfile> -workspaceName=<workspace_identifier> -sqlFeatureStoreId=<feature_store_identifier> GmlLoader [options]" );
        System.out.println( "Description: Imports a GML file directly into a given deegree SQLFeatureStore" );
        System.out.println();
        System.out.println( "arguments:" );
        System.out.println( " -pathToFile=<path/to/gmlfile>, the path to the GML file to import" );
        System.out.println( " -workspaceName=<workspace_identifier>, the name of the deegree workspace used for the import. Must be located at default DEEGREE_WORKSPACE_ROOT directory" );
        System.out.println( " -sqlFeatureStoreId=<feature_store_identifier>, the ID of the SQLFeatureStore in the given workspace" );
        System.out.println();
        System.out.println( "options:" );
        System.out.println( " -disabledResources=<urlpatterns>, a comma separated list url patterns which should not be resolved, not set by default" );
        System.out.println();
        System.out.println( "Example:" );
        System.out.println( " java -jar deegree-gml-tool.jar GmlLoader -pathToFile=/path/to/cadastralparcels.gml -workspaceName=inspire -sqlFeatureStoreId=cadastralparcels" );
    }

}
