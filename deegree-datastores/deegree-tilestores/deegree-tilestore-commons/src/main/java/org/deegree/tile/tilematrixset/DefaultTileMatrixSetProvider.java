/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.tile.tilematrixset;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.deegree.tile.TileMatrixSet;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultResourceIdentifier;
import org.deegree.workspace.standard.IncorporealResourceLocation;
import org.slf4j.Logger;

/**
 * <code>DefaultTileMatrixSetProvider</code> is responsible for reading the default tile
 * matrices as well as overriding them with configured ones.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class DefaultTileMatrixSetProvider extends TileMatrixSetProvider {

	private static final Logger LOG = getLogger(DefaultTileMatrixSetProvider.class);

	private static final String CONFIG_NAMESPACE = "http://www.deegree.org/datasource/tile/tilematrixset";

	private static final URL SCHEMA_URL = DefaultTileMatrixSetProvider.class
		.getResource("/META-INF/schemas/datasource/tile/tilematrixset/tilematrixset.xsd");

	@Override
	public String getNamespace() {
		return CONFIG_NAMESPACE;
	}

	@Override
	public ResourceMetadata<TileMatrixSet> createFromLocation(Workspace workspace,
			ResourceLocation<TileMatrixSet> location) {
		return new DefaultTileMatrixSetMetadata(workspace, location, this);
	}

	@Override
	public URL getSchema() {
		return SCHEMA_URL;
	}

	@Override
	public List<ResourceMetadata<TileMatrixSet>> getAdditionalResources(Workspace workspace) {
		List<ResourceMetadata<TileMatrixSet>> list = new ArrayList<ResourceMetadata<TileMatrixSet>>();
		addStandardConfig(workspace, "inspirecrs84quad", null, list);
		addStandardConfig(workspace, "googlecrs84quad", "urn:ogc:def:wkss:OGC:1.0:GoogleCRS84Quad", list);
		addStandardConfig(workspace, "globalcrs84pixel", "urn:ogc:def:wkss:OGC:1.0:GlobalCRS84Pixel", list);
		addStandardConfig(workspace, "globalcrs84scale", "urn:ogc:def:wkss:OGC:1.0:GlobalCRS84Scale", list);
		addStandardConfig(workspace, "googlemapscompatible", "urn:ogc:def:wkss:OGC:1.0:GoogleMapsCompatible", list);
		return list;
	}

	private void addStandardConfig(Workspace workspace, String name, String wkssId,
			List<ResourceMetadata<TileMatrixSet>> list) {
		URL url = DefaultTileMatrixSetProvider.class.getResource(name + ".xml");
		ResourceIdentifier<TileMatrixSet> id = new DefaultResourceIdentifier<TileMatrixSet>(TileMatrixSetProvider.class,
				name);
		byte[] bs;
		try {
			bs = IOUtils.toByteArray(url);
			ResourceLocation<TileMatrixSet> loc = new IncorporealResourceLocation<TileMatrixSet>(bs, id);
			DefaultTileMatrixSetMetadata md = new DefaultTileMatrixSetMetadata(workspace, loc, this);
			list.add(md);
			if (wkssId != null) {
				id = new DefaultResourceIdentifier<TileMatrixSet>(TileMatrixSetProvider.class, wkssId);
				loc = new IncorporealResourceLocation<TileMatrixSet>(bs, id);
				md = new DefaultTileMatrixSetMetadata(workspace, loc, this);
				list.add(md);
			}
		}
		catch (IOException e) {
			LOG.error("Unable to load standard tile matrix set config {}.", id);
		}
	}

}
