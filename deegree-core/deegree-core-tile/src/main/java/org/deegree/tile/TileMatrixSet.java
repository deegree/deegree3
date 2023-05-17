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

package org.deegree.tile;

import java.util.List;

import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceMetadata;

/**
 * Metadata describing the structure of a {@link TileDataSet}.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class TileMatrixSet implements Resource {

	private final String identifier;

	private final String wknScaleSet;

	private final List<TileMatrix> matrices;

	private final SpatialMetadata spatialMetadata;

	private ResourceMetadata<TileMatrixSet> metadata;

	/**
	 * Creates a new {@link TileMatrixSet} instance.
	 * @param identifier identifier for the {@link TileMatrixSet}, must not be
	 * <code>null</code>
	 * @param wknScaleSet URI of the well-known scale set that this matrix set is
	 * compatible with, can be <code>null</code>
	 * @param matrices the {@link TileMatrix}es this matrix set consists of, must not be
	 * <code>null</code>
	 * @param spatialMetadat the spatial metadata (envelope, CRS) of the tile matrix set,
	 * must not be <code>null</code>
	 * @param metadata the resource metadata object for this resource, must not be
	 * <code>null</code>
	 */
	public TileMatrixSet(String identifier, String wknScaleSet, List<TileMatrix> matrices,
			SpatialMetadata spatialMetadata, ResourceMetadata<TileMatrixSet> metadata) {
		this.identifier = identifier;
		this.wknScaleSet = wknScaleSet;
		this.matrices = matrices;
		this.spatialMetadata = spatialMetadata;
		this.metadata = metadata;
	}

	/**
	 * Returns the identifier.
	 * @return identifier, never <code>null</code>
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Returns the URI of the well-known scale set that this matrix set is compatible
	 * with.
	 * @return URI of the compatible well-known scale set, can be <code>null</code>
	 */
	public String getWellKnownScaleSet() {
		return wknScaleSet;
	}

	/**
	 * @return the tile matrices this matrix set consists of
	 */
	public List<TileMatrix> getTileMatrices() {
		return matrices;
	}

	/**
	 * @return the extent and CRS of this tile matrix set
	 */
	public SpatialMetadata getSpatialMetadata() {
		return spatialMetadata;
	}

	@Override
	public void destroy() {
		// nothing to do
	}

	@Override
	public ResourceMetadata<? extends Resource> getMetadata() {
		return metadata;
	}

	@Override
	public void init() {
		// nothing to do
	}

}
