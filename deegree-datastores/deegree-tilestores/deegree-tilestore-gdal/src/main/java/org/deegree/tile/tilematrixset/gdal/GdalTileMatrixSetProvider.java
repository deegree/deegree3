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
package org.deegree.tile.tilematrixset.gdal;

import java.net.URL;

import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.tilematrixset.TileMatrixSetProvider;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.gdal.gdal.gdal;

/**
 * {@link TileMatrixSetProvider} for {@link TileMatrixSet}s based on overview and tiling
 * information reported by <a href="http://www.gdal.org">GDAL</a>.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.5
 */
public class GdalTileMatrixSetProvider extends TileMatrixSetProvider {

	private static final String SCHEMA_NAMESPACE = "http://www.deegree.org/datasource/tile/tilematrixset/gdal";

	private static final URL SCHEMA_URL = GdalTileMatrixSetProvider.class
		.getResource("/META-INF/schemas/datasource/tile/tilematrixset/gdal/gdal.xsd");

	@Override
	public String getNamespace() {
		return SCHEMA_NAMESPACE;
	}

	@Override
	public ResourceMetadata<TileMatrixSet> createFromLocation(Workspace workspace,
			ResourceLocation<TileMatrixSet> location) {
		gdal.AllRegister();
		return new GdalTileMatrixSetMetadata(workspace, location, this);
	}

	@Override
	public URL getSchema() {
		return SCHEMA_URL;
	}

}
