/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.tile.tilematrixset.geotiff;

import java.net.URL;

import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.tilematrixset.TileMatrixSetProvider;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * This class is the tile matrix set provider for GeoTIFF tile matrices.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * 
 * @since 3.4
 */
public class GeoTiffTileMatrixSetProvider extends TileMatrixSetProvider {

    private static final URL SCHEMA_URL = GeoTiffTileMatrixSetProvider.class.getResource( "/META-INF/schemas/datasource/tile/tilematrixset/3.4.0/geotiff/geotiff.xsd" );

    @Override
    public String getNamespace() {
        return "http://www.deegree.org/datasource/tile/tilematrixset/geotiff";
    }

    @Override
    public ResourceMetadata<TileMatrixSet> createFromLocation( Workspace workspace,
                                                               ResourceLocation<TileMatrixSet> location ) {
        return new GeoTiffTileMatrixSetMetadata( workspace, location, this );
    }

    @Override
    public URL getSchema() {
        return SCHEMA_URL;
    }

}
