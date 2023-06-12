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

import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.gdal.jaxb.GDALLayers;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceMetadata;
import org.deegree.workspace.standard.AbstractResourceProvider;

/**
 * Resource metadata implementation for GDAL layer stores.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
class GdalLayerStoreMetadata extends AbstractResourceMetadata<LayerStore> {

	public GdalLayerStoreMetadata(Workspace workspace, ResourceLocation<LayerStore> location,
			AbstractResourceProvider<LayerStore> provider) {
		super(workspace, location, provider);
	}

	@Override
	public ResourceBuilder<LayerStore> prepare() {
		try {
			GDALLayers cfg;
			cfg = (GDALLayers) JAXBUtils.unmarshall("org.deegree.layer.persistence.gdal.jaxb", provider.getSchema(),
					location.getAsStream(), workspace);
			return new GdalLayerStoreBuilder(cfg, workspace, this);
		}
		catch (Exception e) {
			throw new ResourceInitException("Error while creating GDAL layers: " + e.getLocalizedMessage(), e);
		}
	}

}
