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
package org.deegree.layer.persistence.feature;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;

import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.layer.config.ConfigUtils;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.feature.jaxb.FeatureLayerType;
import org.deegree.layer.persistence.feature.jaxb.FeatureLayers;
import org.deegree.style.persistence.StyleStore;
import org.deegree.style.persistence.StyleStoreProvider;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceMetadata;
import org.deegree.workspace.standard.AbstractResourceProvider;
import org.deegree.workspace.standard.DefaultResourceIdentifier;

/**
 * Resource metadata implementation for feature layer stores.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class FeatureLayerStoreMetadata extends AbstractResourceMetadata<LayerStore> {

	public FeatureLayerStoreMetadata(Workspace workspace, ResourceLocation<LayerStore> location,
			AbstractResourceProvider<LayerStore> provider) {
		super(workspace, location, provider);
	}

	@Override
	public ResourceBuilder<LayerStore> prepare() {
		String pkg = "org.deegree.layer.persistence.feature.jaxb";
		try {
			FeatureLayers lays = (FeatureLayers) unmarshall(pkg, provider.getSchema(), location.getAsStream(),
					workspace);

			if (lays.getAutoLayers() != null) {
				String fid = lays.getAutoLayers().getFeatureStoreId();
				if (fid != null) {
					dependencies.add(new DefaultResourceIdentifier<FeatureStore>(FeatureStoreProvider.class, fid));
				}
				String sid = lays.getAutoLayers().getStyleStoreId();
				if (sid != null) {
					dependencies.add(new DefaultResourceIdentifier<StyleStore>(StyleStoreProvider.class, sid));
				}
			}
			else {
				dependencies.add(new DefaultResourceIdentifier<FeatureStore>(FeatureStoreProvider.class,
						lays.getFeatureStoreId()));
				for (FeatureLayerType flt : lays.getFeatureLayer()) {
					dependencies.addAll(ConfigUtils.getStyleDeps(flt.getStyleRef()));
				}
			}
			return new FeatureLayerStoreBuilder(lays, this, workspace);
		}
		catch (Exception e) {
			throw new ResourceInitException("Could not parse layer configuration file.", e);
		}
	}

}
