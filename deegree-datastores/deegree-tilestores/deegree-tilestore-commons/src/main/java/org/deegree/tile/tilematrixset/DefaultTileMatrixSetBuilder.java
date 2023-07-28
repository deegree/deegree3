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
package org.deegree.tile.tilematrixset;

import static org.deegree.commons.utils.MapUtils.DEFAULT_PIXEL_SIZE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.deegree.commons.utils.MapUtils;
import org.deegree.cs.components.Unit;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.tilematrixset.jaxb.TileMatrixSetConfig;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;

/**
 * This class is responsible for building tile matrix sets.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class DefaultTileMatrixSetBuilder implements ResourceBuilder<TileMatrixSet> {

	private TileMatrixSetConfig cfg;

	private DefaultTileMatrixSetMetadata metadata;

	public DefaultTileMatrixSetBuilder(TileMatrixSetConfig cfg, DefaultTileMatrixSetMetadata metadata) {
		this.cfg = cfg;
		this.metadata = metadata;
	}

	@Override
	public TileMatrixSet build() {
		try {
			ICRS crs = CRSManager.getCRSRef(cfg.getCRS());
			List<TileMatrix> matrices = new ArrayList<TileMatrix>();
			for (TileMatrixSetConfig.TileMatrix tm : cfg.getTileMatrix()) {
				double res;
				if (crs.getUnits()[0].equals(Unit.DEGREE)) {
					res = MapUtils.calcDegreeResFromScale(tm.getScaleDenominator());
				}
				else {
					res = tm.getScaleDenominator() * DEFAULT_PIXEL_SIZE;
				}
				double minx = tm.getTopLeftCorner().get(0);
				double maxy = tm.getTopLeftCorner().get(1);
				double maxx = tm.getTileWidth().longValue() * tm.getMatrixWidth().longValue() * res + minx;
				double miny = maxy - tm.getTileHeight().longValue() * tm.getMatrixHeight().longValue() * res;
				Envelope env = new GeometryFactory().createEnvelope(minx, miny, maxx, maxy, crs);
				SpatialMetadata smd = new SpatialMetadata(env, Collections.singletonList(crs));
				TileMatrix md = new TileMatrix(tm.getIdentifier(), smd, tm.getTileWidth(), tm.getTileHeight(), res,
						tm.getMatrixWidth(), tm.getMatrixHeight());
				matrices.add(md);
			}

			String identifier = metadata.getIdentifier().getId();
			String wknScaleSet = cfg.getWellKnownScaleSet();
			return new TileMatrixSet(identifier, wknScaleSet, matrices, matrices.get(0).getSpatialMetadata(), metadata);
		}
		catch (Exception e) {
			throw new ResourceInitException("Could not create tile matrix set. Reason: " + e.getLocalizedMessage(), e);
		}
	}

}
