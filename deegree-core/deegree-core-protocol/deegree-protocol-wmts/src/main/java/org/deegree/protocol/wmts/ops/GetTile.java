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

package org.deegree.protocol.wmts.ops;

import static org.deegree.commons.ows.exception.OWSException.INVALID_PARAMETER_VALUE;
import static org.deegree.commons.ows.exception.OWSException.MISSING_PARAMETER_VALUE;

import java.util.Map;

import org.deegree.commons.ows.exception.OWSException;

/**
 * A <code>GetTile</code> request to a WMTS.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class GetTile {

	private final String layer;

	private final String style;

	private final String format;

	private final String tileMatrixSet;

	private final String tileMatrix;

	private final long tileRow;

	private final long tileCol;

	private Map<String, String> overriddenParameters;

	/**
	 * Creates a new {@link GetTile} instance.
	 * @param layer requested layer, must not be <code>null</code>
	 * @param style requested style, must not be <code>null</code>
	 * @param format requested format, must not be <code>null</code>
	 * @param tileMatrixSet requested tile matrix set, must not be <code>null</code>
	 * @param tileMatrix requested tile matrix, must not be <code>null</code>
	 * @param x index of the tile row
	 * @param y index of the tile column
	 */
	public GetTile(String layer, String style, String format, String tileMatrixSet, String tileMatrix, long x, long y,
			Map<String, String> overriddenParameters) {
		this.layer = layer;
		this.style = style;
		this.format = format;
		this.tileMatrixSet = tileMatrixSet;
		this.tileMatrix = tileMatrix;
		this.tileCol = x;
		this.tileRow = y;
		this.overriddenParameters = overriddenParameters;
	}

	/**
	 * Convenience constructor for WMTS.
	 * @param map
	 * @throws OWSException if TILEROW/COL are missing or not integers.
	 */
	public GetTile(Map<String, String> map) throws OWSException {
		if (map.get("VERSION") == null) {
			throw new OWSException("The VERSION parameter is missing.", MISSING_PARAMETER_VALUE, "version");
		}
		if (map.get("LAYER") == null) {
			throw new OWSException("The LAYER parameter is missing.", MISSING_PARAMETER_VALUE, "layer");
		}
		if (map.get("STYLE") == null) {
			throw new OWSException("The STYLE parameter is missing.", MISSING_PARAMETER_VALUE, "style");
		}
		if (map.get("FORMAT") == null) {
			throw new OWSException("The FORMAT parameter is missing.", MISSING_PARAMETER_VALUE, "format");
		}
		if (map.get("TILEMATRIXSET") == null) {
			throw new OWSException("The TILEMATRIXSET parameter is missing.", MISSING_PARAMETER_VALUE, "tileMatrixSet");
		}
		if (map.get("TILEMATRIX") == null) {
			throw new OWSException("The TILEMATRIX parameter is missing.", MISSING_PARAMETER_VALUE, "tileMatrix");
		}
		this.layer = map.get("LAYER");
		this.style = map.get("STYLE");
		this.format = map.get("FORMAT");
		this.tileMatrixSet = map.get("TILEMATRIXSET");
		this.tileMatrix = map.get("TILEMATRIX");
		String row = map.get("TILEROW");
		if (row == null) {
			throw new OWSException("The TILEROW parameter is missing.", MISSING_PARAMETER_VALUE, "tileRow");
		}
		try {
			this.tileRow = Integer.parseInt(row);
		}
		catch (NumberFormatException e) {
			throw new OWSException("The TILEROW parameter value of '" + row + "' is not a valid index.",
					INVALID_PARAMETER_VALUE, "tileRow");
		}
		String col = map.get("TILECOL");
		if (col == null) {
			throw new OWSException("The TILECOL parameter is missing.", MISSING_PARAMETER_VALUE, "tileCol");
		}
		try {
			this.tileCol = Integer.parseInt(col);
		}
		catch (NumberFormatException e) {
			throw new OWSException("The TILECOL parameter value of '" + col + "' is not a valid index.",
					INVALID_PARAMETER_VALUE, "tileCol");
		}
	}

	/**
	 * @return the layer
	 */
	public String getLayer() {
		return layer;
	}

	/**
	 * @return the style
	 */
	public String getStyle() {
		return style;
	}

	/**
	 * @return the format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * @return the tileMatrixSet
	 */
	public String getTileMatrixSet() {
		return tileMatrixSet;
	}

	/**
	 * @return the tileMatrix
	 */
	public String getTileMatrix() {
		return tileMatrix;
	}

	/**
	 * @return the tileRow
	 */
	public long getTileRow() {
		return tileRow;
	}

	/**
	 * @return the tileCol
	 */
	public long getTileCol() {
		return tileCol;
	}

	/**
	 * @return null, or parameters to be overridden when used in client requests.
	 */
	public Map<String, String> getOverriddenParameters() {
		return overriddenParameters;
	}

}
