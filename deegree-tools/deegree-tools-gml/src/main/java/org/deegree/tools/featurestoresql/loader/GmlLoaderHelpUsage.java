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
package org.deegree.tools.featurestoresql.loader;

/**
 * GmlLoader CLI usage info.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GmlLoaderHelpUsage {

    public static void printUsage() {
        System.out.println( "Usage: java -jar deegree-tools-gml.jar GmlLoader -pathToFile=<path/to/gmlfile> -workspaceName=<workspace_identifier> -sqlFeatureStoreId=<feature_store_identifier> [options]" );
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
        System.out.println( " java -jar deegree-tools-gml.jar GmlLoader -pathToFile=/path/to/cadastralparcels.gml -workspaceName=inspire -sqlFeatureStoreId=cadastralparcels" );
    }

}
