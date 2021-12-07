package org.deegree.tools.featurestoresql.config;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeatureStoreConfigUsagePrinter {

    public static void printUsage() {
        System.out.println( "Usage: java -jar deegree-gml-tool.jar FeatureStoreConfigLoader -schemaUrl=<url-or-path/to/file> [options]" );
        System.out.println();
        System.out.println( "arguments:" );
        System.out.println( " -schemaUrl=<url-or-path/to/file>, path to the schema, may be an local reference or http url" );
        System.out.println();
        System.out.println( "options:" );
        System.out.println( " -format={deegree|ddl|all}, default=deegree" );
        System.out.println( " -srid=<epsg_code>, default=4258" );
        System.out.println( " -idtype={int|uuid}, default=int" );
        System.out.println( " -mapping={relational|blob}, default=relational" );
        System.out.println( " -dialect={postgis|oracle}, default=postgis" );
        System.out.println( " -cycledepth=INT, positive integer value to specify the depth of cycles, default=0" );
        System.out.println( " -listOfPropertiesWithPrimitiveHref=<path/to/file>, not set by default" );
        System.out.println( " -referenceData=<path/to/file> (GML Feature collection containing reference features. The generated config is simplified to map this feature collection.)" );
        System.out.println();
        System.out.println( "The option listOfPropertiesWithPrimitiveHref references a file listing properties which are written with primitive instead of feature mappings (see deegree-webservices documentation and README of this tool for further information):" );
        System.out.println( "---------- begin file ----------" );
        System.out.println( "# lines beginning with an # are ignored" );
        System.out.println( "# property with namespace binding" );
        System.out.println( "{http://inspire.ec.europa.eu/schemas/ps/4.0}designation" );
        System.out.println( "# property without namespace binding" );
        System.out.println( "designation" );
        System.out.println( "# empty lines are ignored" );
        System.out.println();
        System.out.println( "# leading and trailing white spaces are ignored" );
        System.out.println( "---------- end file ----------" );
    }

}
