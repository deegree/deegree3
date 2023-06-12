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

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;

import java.io.InputStream;
import java.net.URL;

import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.tilematrixset.gdal.jaxb.GdalTileMatrixSetConfig;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceMetadata;

/**
 * {@link ResourceMetadata} for {@link TileMatrixSet}s derived from raster files using
 * <a href="http://www.gdal.org">GDAL</a>.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.5
 */
class GdalTileMatrixSetMetadata extends AbstractResourceMetadata<TileMatrixSet> {

	private static final String JAXB_PACKAGE = "org.deegree.tile.tilematrixset.gdal.jaxb";

	/**
	 * Creates a new {@link GdalTileMatrixSetMetadata} instance.
	 * @param ws workspace that the resource belongs to, must not be <code>null</code>
	 * @param location resource configuration, must not be <code>null</code>
	 * @param provider resource provider, must not be <code>null</code>
	 */
	GdalTileMatrixSetMetadata(Workspace ws, ResourceLocation<TileMatrixSet> location,
			GdalTileMatrixSetProvider provider) {
		super(ws, location, provider);
	}

	@Override
	public ResourceBuilder<TileMatrixSet> prepare() {
		try {
			InputStream is = location.getAsStream();
			URL schemaUrl = provider.getSchema();
			GdalTileMatrixSetConfig cfg = (GdalTileMatrixSetConfig) unmarshall(JAXB_PACKAGE, schemaUrl, is, workspace);
			return new GdalTileMatrixSetBuilder(cfg, this);
		}
		catch (Exception e) {
			throw new ResourceInitException(e.getLocalizedMessage(), e);
		}
	}

}
