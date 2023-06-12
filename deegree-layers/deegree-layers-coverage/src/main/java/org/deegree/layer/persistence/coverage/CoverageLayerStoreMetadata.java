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
package org.deegree.layer.persistence.coverage;

import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.coverage.Coverage;
import org.deegree.coverage.persistence.CoverageProvider;
import org.deegree.layer.config.ConfigUtils;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.coverage.jaxb.CoverageLayerType;
import org.deegree.layer.persistence.coverage.jaxb.CoverageLayers;
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
 * Resource metadata implementation for coverage layer stores.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class CoverageLayerStoreMetadata extends AbstractResourceMetadata<LayerStore> {

	public CoverageLayerStoreMetadata(Workspace workspace, ResourceLocation<LayerStore> location,
			AbstractResourceProvider<LayerStore> provider) {
		super(workspace, location, provider);
	}

	@Override
	public ResourceBuilder<LayerStore> prepare() {
		try {
			CoverageLayers cfg;
			cfg = (CoverageLayers) JAXBUtils.unmarshall("org.deegree.layer.persistence.coverage.jaxb",
					provider.getSchema(), location.getAsStream(), workspace);

			if (cfg.getAutoLayers() != null) {
				String cid = cfg.getAutoLayers().getCoverageStoreId();
				if (cid != null) {
					dependencies.add(new DefaultResourceIdentifier<Coverage>(CoverageProvider.class, cid));
				}
				String sid = cfg.getAutoLayers().getStyleStoreId();
				if (sid != null) {
					dependencies.add(new DefaultResourceIdentifier<StyleStore>(StyleStoreProvider.class, sid));
				}
			}
			else {
				dependencies
					.add(new DefaultResourceIdentifier<Coverage>(CoverageProvider.class, cfg.getCoverageStoreId()));
				for (CoverageLayerType lay : cfg.getCoverageLayer()) {
					dependencies.addAll(ConfigUtils.getStyleDeps(lay.getStyleRef()));
				}
			}

			return new CoverageLayerStoreBuilder(cfg, workspace, this);
		}
		catch (Exception e) {
			throw new ResourceInitException("Error while creating coverage layers: " + e.getLocalizedMessage(), e);
		}
	}

}
