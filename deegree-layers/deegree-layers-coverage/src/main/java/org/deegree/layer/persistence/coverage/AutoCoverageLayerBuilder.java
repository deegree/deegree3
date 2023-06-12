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
package org.deegree.layer.persistence.coverage;

import static org.deegree.layer.persistence.coverage.CoverageFeatureTypeBuilder.buildFeatureType;

import java.util.Collections;

import org.deegree.commons.ows.metadata.Description;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.coverage.Coverage;
import org.deegree.coverage.persistence.CoverageProvider;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.MultiResolutionRaster;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.layer.Layer;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.SingleLayerStore;
import org.deegree.layer.persistence.coverage.jaxb.CoverageLayers.AutoLayers;
import org.deegree.style.persistence.StyleStore;
import org.deegree.style.persistence.StyleStoreProvider;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * Responsible for creating coverage layers from jaxb beans, AutoLayers variant.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class AutoCoverageLayerBuilder {

	private Workspace workspace;

	private ResourceMetadata<LayerStore> metadata;

	AutoCoverageLayerBuilder(Workspace workspace, ResourceMetadata<LayerStore> metadata) {
		this.workspace = workspace;
		this.metadata = metadata;
	}

	LayerStore createFromAutoLayers(AutoLayers cfg) {
		String cid = cfg.getCoverageStoreId();
		String sid = cfg.getStyleStoreId();
		Coverage cov = workspace.getResource(CoverageProvider.class, cid);
		StyleStore sstore = null;
		if (sid != null) {
			sstore = workspace.getResource(StyleStoreProvider.class, sid);
		}

		SpatialMetadata smd = new SpatialMetadata(cov.getEnvelope(),
				Collections.singletonList(cov.getCoordinateSystem()));
		Description desc = new Description(cid, Collections.singletonList(new LanguageString(cid, null)), null, null);
		LayerMetadata md = new LayerMetadata(cid, desc, smd);

		md.getFeatureTypes().add(buildFeatureType());

		if (sstore != null) {
			for (Style s : sstore.getAll(cid)) {
				md.getStyles().put(s.getName(), s);
			}
		}

		Layer l = new CoverageLayer(md, cov instanceof AbstractRaster ? (AbstractRaster) cov : null,
				cov instanceof MultiResolutionRaster ? (MultiResolutionRaster) cov : null);
		return new SingleLayerStore(l, metadata);
	}

}
