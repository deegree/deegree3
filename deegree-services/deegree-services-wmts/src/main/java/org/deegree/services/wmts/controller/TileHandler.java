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

package org.deegree.services.wmts.controller;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;
import static org.deegree.commons.ows.exception.OWSException.INVALID_PARAMETER_VALUE;
import static org.deegree.commons.ows.exception.OWSException.NO_APPLICABLE_CODE;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.layer.Layer;
import org.deegree.layer.persistence.tile.TileLayer;
import org.deegree.protocol.wmts.ops.GetTile;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.theme.Theme;
import org.deegree.theme.Themes;
import org.deegree.tile.Tile;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;

/**
 * Responsible for handling GetTile requests.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */

class TileHandler {

	private Map<String, TileLayer> layers;

	TileHandler(List<Theme> themes) {
		layers = new HashMap<String, TileLayer>();
		for (Theme theme : themes) {
			for (Layer l : Themes.getAllLayers(theme)) {
				if (l instanceof TileLayer) {
					layers.put(l.getMetadata().getName(), ((TileLayer) l));
				}
			}
		}
	}

	void getTile(Map<String, String> map, HttpResponseBuffer response) throws OWSException, ServletException {
		GetTile op = new GetTile(map);
		getTile(op, response);
	}

	private void getTile(final GetTile op, final HttpResponseBuffer response) throws OWSException, ServletException {
		final TileLayer layer = layers.get(op.getLayer());
		if (layer == null) {
			throw new OWSException("Unknown layer: " + op.getLayer(), INVALID_PARAMETER_VALUE);
		}

		final TileDataSet tds = layer.getTileDataSet(op.getTileMatrixSet());
		if (tds == null) {
			throw new OWSException("The layer " + op.getLayer()
					+ " has not been configured to offer the tile matrix set " + op.getTileMatrixSet() + ".",
					INVALID_PARAMETER_VALUE);
		}

		final String format = op.getFormat();
		if (format != null && !format.isEmpty() && !tds.getNativeImageFormat().equals(format)) {
			throw new OWSException("Unknown format: " + format, INVALID_PARAMETER_VALUE);
		}

		final TileDataLevel level = tds.getTileDataLevel(op.getTileMatrix());
		if (level == null) {
			throw new OWSException("No tile matrix with id " + op.getTileMatrix() + " in tile matrix set "
					+ op.getTileMatrixSet() + ".", INVALID_PARAMETER_VALUE);
		}

		List<String> styles = level.getStyles();
		if (styles != null) {
			for (String style : styles) {
				if (!style.equals(op.getStyle())) {
					throw new OWSException("The STYLE parameter value of '" + op.getStyle() + "' is not valid.",
							INVALID_PARAMETER_VALUE, "Style");
				}
			}
		}

		final Tile t = level.getTile(op.getTileCol(), op.getTileRow());
		if (t == null) {
			// exception or empty tile?
			throw new OWSException("No such tile found.", INVALID_PARAMETER_VALUE);
		}

		InputStream in = null;
		try {
			in = t.getAsStream();
			if (in == null) {
				throw new OWSException("Tile yielded no data.", NO_APPLICABLE_CODE);
			}
			response.setContentType(format);
			copy(in, response.getOutputStream());
		}
		catch (Throwable e) {
			throw new OWSException(e.getMessage(), e, NO_APPLICABLE_CODE);
		}
		finally {
			closeQuietly(in);
		}
	}

}
