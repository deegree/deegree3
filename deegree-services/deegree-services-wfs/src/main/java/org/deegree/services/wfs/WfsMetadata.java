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
package org.deegree.services.wfs;

import java.util.Collection;
import java.util.List;

import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreManager;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.services.OWS;
import org.deegree.services.OWSProvider;
import org.deegree.services.OwsManager;
import org.deegree.services.jaxb.wfs.DeegreeWFS;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.services.metadata.OWSMetadataProviderManager;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceMetadata;
import org.deegree.workspace.standard.AbstractResourceProvider;
import org.deegree.workspace.standard.DefaultResourceIdentifier;

/**
 * Resource metadata implementation for WFS.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class WfsMetadata extends AbstractResourceMetadata<OWS> {

	private static final String CONFIG_JAXB_PACKAGE = "org.deegree.services.jaxb.wfs";

	public WfsMetadata(Workspace workspace, ResourceLocation<OWS> location, AbstractResourceProvider<OWS> provider) {
		super(workspace, location, provider);
	}

	@Override
	public ResourceBuilder<OWS> prepare() {
		try {
			DeegreeWFS cfg = (DeegreeWFS) JAXBUtils.unmarshall(CONFIG_JAXB_PACKAGE, provider.getSchema(),
					location.getAsStream(), workspace);

			List<String> list = cfg.getFeatureStoreId();
			if (list != null && !list.isEmpty()) {
				for (String id : list) {
					dependencies.add(new DefaultResourceIdentifier<FeatureStore>(FeatureStoreProvider.class, id));
				}
			}
			else {
				FeatureStoreManager fmgr = workspace.getResourceManager(FeatureStoreManager.class);
				for (ResourceMetadata<FeatureStore> md : fmgr.getResourceMetadata()) {
					softDependencies.add(md.getIdentifier());
				}
			}

			OwsManager mgr = workspace.getResourceManager(OwsManager.class);
			Collection<ResourceMetadata<OWS>> mds = mgr.getResourceMetadata();
			for (ResourceMetadata<OWS> md : mds) {
				OWSProvider prov = (OWSProvider) md.getProvider();
				for (String name : prov.getImplementationMetadata().getImplementedServiceName()) {
					if (name.equalsIgnoreCase("CSW")) {
						softDependencies.add(md.getIdentifier());
					}
				}
			}

			OWSMetadataProviderManager mmgr = workspace.getResourceManager(OWSMetadataProviderManager.class);
			for (ResourceMetadata<OWSMetadataProvider> md : mmgr.getResourceMetadata()) {
				ResourceIdentifier<OWSMetadataProvider> id = md.getIdentifier();
				if (id.getId().equals(getIdentifier().getId() + "_metadata")) {
					softDependencies.add(id);
				}
			}

			return new WfsBuilder(this, workspace, cfg);
		}
		catch (Exception e) {
			throw new ResourceInitException(e.getLocalizedMessage(), e);
		}
	}

}
