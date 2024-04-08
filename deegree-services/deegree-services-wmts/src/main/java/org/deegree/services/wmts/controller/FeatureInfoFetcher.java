/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.wmts.controller;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.FeatureCollection;
import org.deegree.featureinfo.FeatureInfoManager;
import org.deegree.featureinfo.FeatureInfoParams;
import org.deegree.layer.persistence.tile.TileLayer;
import org.deegree.protocol.wmts.ops.GetFeatureInfo;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.controller.utils.StandardFeatureInfoContext;
import org.deegree.tile.Tile;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static org.deegree.commons.ows.exception.OWSException.INVALID_PARAMETER_VALUE;

/**
 * Responsible for fetching features from tile layers, prepared to immediately be
 * serialized.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class FeatureInfoFetcher {

	private TileLayer layer;

	private GetFeatureInfo gfi;

	FeatureInfoFetcher(TileLayer layer, GetFeatureInfo gfi) {
		this.layer = layer;
		this.gfi = gfi;
	}

	void fetch(FeatureInfoManager featureInfoManager, HttpResponseBuffer response)
			throws OWSException, IOException, XMLStreamException {
		TileDataSet tds = layer.getTileDataSet(gfi.getTileMatrixSet());
		if (tds == null) {
			throw new OWSException(
					"The TileMatrixSet parameter value of '" + gfi.getTileMatrixSet() + "' is not valid.",
					INVALID_PARAMETER_VALUE, "tileMatrixSet");
		}
		TileDataLevel tdl = tds.getTileDataLevel(gfi.getTileMatrix());
		if (tdl == null) {
			throw new OWSException("The TileMatrix parameter value of '" + gfi.getTileMatrix() + "' is not valid.",
					INVALID_PARAMETER_VALUE, "tileMatrix");
		}
		List<String> styles = tdl.getStyles();
		if (styles != null) {
			for (String style : styles) {
				if (!style.equals(gfi.getStyle())) {
					throw new OWSException("The STYLE parameter value of '" + gfi.getStyle() + "' is not valid.",
							INVALID_PARAMETER_VALUE, "style");
				}
			}
		}
		Tile t = tdl.getTile(gfi.getTileCol(), gfi.getTileRow());
		FeatureCollection col = t.getFeatures(gfi.getI(), gfi.getJ(), 10);
		ICRS crs = tds.getTileMatrixSet().getSpatialMetadata().getEnvelope().getCoordinateSystem();
		HashMap<String, String> nsBindings = new HashMap<String, String>();

		FeatureInfoParams params = new FeatureInfoParams(nsBindings, col, gfi.getInfoFormat(), false, null, null, crs,
				null);

		featureInfoManager.serializeFeatureInfo(params, new StandardFeatureInfoContext(response));
	}

}
