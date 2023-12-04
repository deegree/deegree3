/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.wms.utils;

import static org.deegree.commons.ows.exception.OWSException.INVALID_PARAMETER_VALUE;

import java.math.BigInteger;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.protocol.wms.ops.GetMap;
import org.deegree.services.jaxb.wms.DeegreeWMS;

/**
 * Checks whether a {@link GetMap} request is valid with regard to the configured limits
 * on image size and layer count.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
public class GetMapLimitChecker {

	/**
	 * Checks whether the given {@link GetMap} request can be processed with regard to the
	 * configured limits on image size and layer count.
	 * @param request request, must not be <code>null</code>
	 * @param config WMS config, must not be <code>null</code>
	 * @throws OWSException if the request can not be processed
	 */
	public void checkRequestedSizeAndLayerCount(final GetMap request, final DeegreeWMS config) throws OWSException {
		checkWidth(request.getWidth(), toIntegerNullSafe(config.getMaxWidth()));
		checkHeight(request.getHeight(), toIntegerNullSafe(config.getMaxHeight()));
		int layerCount = request.getLayers() != null ? request.getLayers().size() : 0;
		checkLayerCount(layerCount, toIntegerNullSafe(config.getLayerLimit()));
	}

	private Integer toIntegerNullSafe(final BigInteger value) {
		if (value == null) {
			return null;
		}
		return value.intValue();
	}

	/**
	 * Checks whether the requested map width is greater than zero and does not exceed the
	 * maximum allowed width (if set).
	 * @param requestedWidth requested map width
	 * @param maxWidth maximum allowed width, can be <code>null</code> (no limit)
	 * @throws OWSException if the requested map width is invalid
	 */
	void checkWidth(final int requestedWidth, final Integer maxWidth) throws OWSException {
		if (requestedWidth <= 0) {
			final String msg = "Width must be positive.";
			throw new OWSException(msg, INVALID_PARAMETER_VALUE, "width");
		}
		if (maxWidth == null) {
			return;
		}
		if (requestedWidth > maxWidth) {
			final String msg = "Width out of range. Maximum width: " + maxWidth;
			throw new OWSException(msg, INVALID_PARAMETER_VALUE, "width");
		}
	}

	/**
	 * Checks whether the requested map height is greater than zero and does not exceed
	 * the maximum allowed height (if set).
	 * @param requestedWidth requested map height
	 * @param maxHeight maximum allowed height, can be <code>null</code> (no limit)
	 * @throws OWSException if the requested map height width is invalid
	 */
	void checkHeight(final int requestedHeight, final Integer maxHeight) throws OWSException {
		if (requestedHeight <= 0) {
			final String msg = "Height must be positive.";
			throw new OWSException(msg, INVALID_PARAMETER_VALUE, "height");
		}
		if (maxHeight == null) {
			return;
		}
		if (requestedHeight > maxHeight) {
			final String msg = "Height out of range. Maximum height: " + maxHeight;
			throw new OWSException(msg, INVALID_PARAMETER_VALUE, "height");
		}
	}

	/**
	 * Checks whether the number of requested layers does not exceed the maximum the
	 * maximum allowed number (if set).
	 * @param requestedLayerCount requested map height
	 * @param maxLayers maximum number of layers, can be <code>null</code> (no limit)
	 * @throws OWSException if too many layers are requested
	 */
	void checkLayerCount(final int requestedLayerCount, final Integer maxLayers) throws OWSException {
		if (maxLayers == null) {
			return;
		}
		if (requestedLayerCount > maxLayers) {
			final String msg = "Too many layers requested. Maximum number of layers: " + maxLayers;
			throw new OWSException(msg, INVALID_PARAMETER_VALUE, "layer");
		}
	}

}
