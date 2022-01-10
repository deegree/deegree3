package org.deegree.tools.featurestoresql;

import org.deegree.tools.featurestoresql.config.SqlFeatureStoreConfigCreatorApp;
import org.deegree.tools.featurestoresql.loader.GmlLoaderApp;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the command line interface of deegree GMLTools.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
@SpringBootApplication
public class GmlToolsApp {

    public static void main( String[] args ) {
        if ( args.length == 0 || "--help".equals( args[0] ) || "-help".equals( args[0] )  || "-h".equals( args[0] ) ) {
            printUsage();
        } else if ( "SqlFeatureStoreConfigCreator".equalsIgnoreCase( args[0] ) ) {
            SqlFeatureStoreConfigCreatorApp.run( args );
        } else if ( "GmlLoader".equalsIgnoreCase( args[0] ) ) {
            GmlLoaderApp.run( args );
        } else {
            printUsage();
        }
    }

    private static void printUsage() {
        System.out.println( "The deegree CLI includes tools to create SQLFeatureStore configurations and load GML files." );
        System.out.println( "Use the keywords 'SqlFeatureStoreConfigCreator' or 'GmlLoader' to choose between the tools:" );
        System.out.println( "   SqlFeatureStoreConfigCreator -h (Prints the usage for this tool)" );
        System.out.println( "   GmlLoader -h (Prints the usage for this tool)" );
    }

}