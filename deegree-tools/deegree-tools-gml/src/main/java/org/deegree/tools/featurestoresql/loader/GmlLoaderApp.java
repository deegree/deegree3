package org.deegree.tools.featurestoresql.loader;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;

import org.deegree.tools.featurestoresql.CommonConfiguration;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GmlLoaderApp {

    public static void run( String[] args ) {
        if ( args.length == 1
             || ( args.length > 1 && ( "--help".equals( args[1] ) || "-help".equals( args[1] ) || "-h".equals( args[1] ) ) ) ) {
            GmlLoaderHelpUsage.printUsage();
        } else if ( args.length < 4 ) {
            printUnexpectedNumberOfParameters( args );
            GmlLoaderHelpUsage.printUsage();
        } else {
            SpringApplication app = new SpringApplication( CommonConfiguration.class, GmlLoaderConfiguration.class );
            app.setBannerMode( Banner.Mode.OFF );
            app.run( args );
        }
    }

    private static void printUnexpectedNumberOfParameters( String[] args ) {
        System.out.println( "Number of arguments is invalid, must be at least three but was " + ( args.length -1 ) );
        System.out.println();
    }

}
