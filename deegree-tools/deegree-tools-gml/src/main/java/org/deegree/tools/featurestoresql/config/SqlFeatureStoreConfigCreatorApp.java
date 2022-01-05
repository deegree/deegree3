/*-
 * #%L
 * deegree-cli-utility
 * %%
 * Copyright (C) 2016 - 2021 lat/lon GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.deegree.tools.featurestoresql.config;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;

import org.deegree.tools.featurestoresql.CommonConfiguration;

/**
 * Entry point of the command line interface of SqlFeatureStoreConfigCreator.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class SqlFeatureStoreConfigCreatorApp {

    public static void run( String[] args ) {
        if ( args.length == 1
             || ( args.length > 1 && ( "--help".equals( args[1] ) || "-help".equals( args[1] ) || "-h".equals( args[1] ) ) ) ) {
            SqlFeatureStoreConfigCreatorUsagePrinter.printUsage();
        } else if ( args.length == 1 ) {
            printUnexpectedNumberOfParameters();
            SqlFeatureStoreConfigCreatorUsagePrinter.printUsage();
        } else {
            SpringApplication app = new SpringApplication( CommonConfiguration.class,
                                                           SqlFeatureStoreConfigCreatorConfiguration.class );
            app.setBannerMode( Banner.Mode.OFF );
            app.run( args );
        }
    }

    private static void printUnexpectedNumberOfParameters() {
        System.out.println( "Number of arguments is invalid, must be more one." );
        System.out.println();
    }

}