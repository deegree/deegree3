/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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
package org.deegree.services.wms.controller.capabilities.theme;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;

import java.util.List;

import org.deegree.commons.utils.DoublePair;
import org.deegree.layer.Layer;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.rendering.r2d.context.MapOptions;
import org.deegree.theme.Theme;
import org.deegree.theme.Themes;

/**
 * Merges {@link LayerMetadata} of {@link Theme} objects.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 * @since 3.3
 */
class LayerMetadataMerger {

	private static final int QUERYABLE_DEFAULT_MASK = 1;

	private static final int QUERYABLE_DISABLED_MASK = 2;

	private static final int QUERYABLE_ENABLED_MASK = 4;

	/**
	 * Returns the combined layer metadata for the given theme/sublayers.
	 *
	 * @see LayerMetadata#merge(LayerMetadata)
	 * @param theme must not be <code>null</code>
	 * @return combined layer metadata, never <code>null</code>
	 */
	LayerMetadata merge(final Theme theme) {
		final LayerMetadata themeMetadata = theme.getLayerMetadata();
		LayerMetadata layerMetadata = new LayerMetadata(null, null, null);

		int queryable = 0;
		boolean opaque = false;
		int cascaded = 0;

		for (final Layer l : Themes.getAllLayers(theme)) {
			queryable |= analyseQueryable(l.getMetadata());
			if (checkIfOpaque(l.getMetadata()))
				opaque = true;
			if (checkIfLargerCascadedValue(cascaded, l.getMetadata()))
				cascaded = l.getMetadata().getCascaded();
			layerMetadata.merge(l.getMetadata());
		}
		themeMetadata.merge(layerMetadata);
		adjustMapOptions(themeMetadata, queryable, opaque, cascaded);
		return themeMetadata;
	}

	/**
	 * Returns the combined (least restrictive) scale denominators for the given
	 * theme/sublayers.
	 * @param theme must not be <code>null</code>
	 * @return combined scale denomiators, first value is min, second is max, never
	 * <code>null</code>
	 */
	DoublePair mergeScaleDenominators(final Theme theme) {
		Double min = POSITIVE_INFINITY;
		Double max = NEGATIVE_INFINITY;
		if (theme.getLayerMetadata() != null && theme.getLayerMetadata().getScaleDenominators() != null) {
			final DoublePair themeScales = theme.getLayerMetadata().getScaleDenominators();
			if (!themeScales.first.isInfinite()) {
				min = themeScales.first;
			}
			if (!themeScales.second.isInfinite()) {
				max = themeScales.second;
			}
		}
		final List<Layer> layers = Themes.getAllLayers(theme);
		if (layers != null) {
			for (final Layer layer : layers) {
				if (layer.getMetadata() != null) {
					final DoublePair layerScales = layer.getMetadata().getScaleDenominators();
					if (layerScales != null) {
						min = Math.min(min, layerScales.first);
						max = Math.max(max, layerScales.second);
					}
				}
			}
		}
		return new DoublePair(min, max);
	}

	private int analyseQueryable(LayerMetadata m) {
		if (m.getMapOptions() == null)
			return QUERYABLE_DEFAULT_MASK;

		int r = m.getMapOptions().getFeatureInfoRadius();

		if (r < 0)
			return QUERYABLE_DEFAULT_MASK;
		else if (r == 0)
			return QUERYABLE_DISABLED_MASK;
		else
			return QUERYABLE_ENABLED_MASK;
	}

	private boolean checkIfOpaque(LayerMetadata metadata) {
		return metadata != null && metadata.getMapOptions() != null && metadata.getMapOptions().isOpaque();
	}

	private boolean checkIfLargerCascadedValue(int cascaded, LayerMetadata metadata) {
		return metadata != null && cascaded < metadata.getCascaded();
	}

	private void adjustMapOptions(LayerMetadata themeMetadata, int queryable, boolean opaque, int cascaded) {
		if (themeMetadata.getMapOptions() == null) {
			themeMetadata.setMapOptions(new MapOptions.Builder().build());
		}
		if (queryable == QUERYABLE_DISABLED_MASK) {
			themeMetadata.getMapOptions().setFeatureInfoRadius(0);
		}
		else {
			themeMetadata.getMapOptions().setFeatureInfoRadius(-1);
		}
		themeMetadata.getMapOptions().setOpaque(opaque);
		themeMetadata.setCascaded(cascaded);
	}

}