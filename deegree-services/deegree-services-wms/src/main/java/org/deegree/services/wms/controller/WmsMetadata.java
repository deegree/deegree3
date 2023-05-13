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
package org.deegree.services.wms.controller;

import java.util.Collection;

import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.metadata.persistence.MetadataStoreProvider;
import org.deegree.services.OWS;
import org.deegree.services.OWSProvider;
import org.deegree.services.OwsManager;
import org.deegree.services.jaxb.wms.DeegreeWMS;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.services.metadata.OWSMetadataProviderManager;
import org.deegree.theme.Theme;
import org.deegree.theme.persistence.ThemeProvider;
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
 * Resource metadata for WMS services.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class WmsMetadata extends AbstractResourceMetadata<OWS> {

	private static final String CONFIG_JAXB_PACKAGE = "org.deegree.services.jaxb.wms";

	public WmsMetadata(Workspace workspace, ResourceLocation<OWS> location, AbstractResourceProvider<OWS> provider) {
		super(workspace, location, provider);
	}

	@Override
	public ResourceBuilder<OWS> prepare() {
		try {
			DeegreeWMS cfg = (DeegreeWMS) JAXBUtils.unmarshall(CONFIG_JAXB_PACKAGE, provider.getSchema(),
					location.getAsStream(), workspace);

			String id = cfg.getMetadataStoreId();
			if (id != null) {
				// is that really a metadata store id? Saw services with UUID here.
				softDependencies.add(new DefaultResourceIdentifier(MetadataStoreProvider.class, id));
			}

			for (String tid : cfg.getServiceConfiguration().getThemeId()) {
				dependencies.add(new DefaultResourceIdentifier<Theme>(ThemeProvider.class, tid));
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
				ResourceIdentifier<OWSMetadataProvider> mdId = md.getIdentifier();
				if (mdId.getId().equals(getIdentifier().getId() + "_metadata")) {
					softDependencies.add(mdId);
				}
			}

			return new WmsBuilder(this, workspace, cfg);
		}
		catch (Exception e) {
			throw new ResourceInitException(e.getLocalizedMessage(), e);
		}
	}

}
