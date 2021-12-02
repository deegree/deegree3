package org.deegree.tools.featurestoresql.config;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;

import org.deegree.tools.featurestoresql.CommonConfiguration;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeatureStoreLoaderConfigApp {

    public static void run( String[] args ) {
        if ( args.length == 1
             || ( args.length > 1 && ( "--help".equals( args[1] ) || "-help".equals( args[1] ) || "-h".equals( args[1] ) ) ) ) {
            FeatureStoreConfigUsagePrinter.printUsage();
        } else if ( args.length == 1 ) {
            printUnexpectedNumberOfParameters();
            FeatureStoreConfigUsagePrinter.printUsage();
        } else {
            SpringApplication app = new SpringApplication( CommonConfiguration.class,
                                                           FeatureStoreLoaderConfiguration.class );
            app.setBannerMode( Banner.Mode.OFF );
            app.run( args );
        }
    }

    private static void printUnexpectedNumberOfParameters() {
        System.out.println( "Number of arguments is invalid, must be more one." );
        System.out.println();
    }

}