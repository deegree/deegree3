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
package org.deegree.layer.persistence.gdal;

import java.io.File;
import java.util.List;

import org.deegree.commons.gdal.GdalSettings;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.geometry.Envelope;
import org.deegree.layer.AbstractLayer;
import org.deegree.layer.Layer;
import org.deegree.layer.LayerQuery;
import org.deegree.layer.metadata.LayerMetadata;

/**
 * {@link Layer} implementation for layers backed by GDAL datasets.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
class GdalLayer extends AbstractLayer {

	private final List<File> datasets;

	private final GdalSettings gdalSettings;

	GdalLayer(LayerMetadata md, List<File> datasets, GdalSettings gdalSettings) {
		super(md);
		this.datasets = datasets;
		this.gdalSettings = gdalSettings;
	}

	@Override
	public GdalLayerData mapQuery(LayerQuery query, List<String> headers) throws OWSException {
		Envelope bbox = query.getEnvelope();
		return new GdalLayerData(datasets, bbox, query.getWidth(), query.getHeight(), gdalSettings);
	}

	@Override
	public GdalLayerData infoQuery(LayerQuery query, List<String> headers) throws OWSException {
		Envelope bbox = query.calcClickBox(query.getRenderingOptions().getFeatureInfoRadius(getMetadata().getName()));
		return new GdalLayerData(datasets, bbox, query.getWidth(), query.getHeight(), gdalSettings);
	}

}
