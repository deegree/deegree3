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
package org.deegree.services.csw;

import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.metadata.persistence.MetadataStoreManager;
import org.deegree.metadata.persistence.MetadataStoreProvider;
import org.deegree.services.OWS;
import org.deegree.services.jaxb.csw.DeegreeCSW;
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
 * Resource metadata for CSW services.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class CswMetadata extends AbstractResourceMetadata<OWS> {

	private static final String CONFIG_JAXB_PACKAGE = "org.deegree.services.jaxb.csw";

	public CswMetadata(Workspace workspace, ResourceLocation<OWS> location, AbstractResourceProvider<OWS> provider) {
		super(workspace, location, provider);
	}

	@Override
	public ResourceBuilder<OWS> prepare() {
		try {
			DeegreeCSW cfg = (DeegreeCSW) JAXBUtils.unmarshall(CONFIG_JAXB_PACKAGE, provider.getSchema(),
					location.getAsStream(), workspace);

			String id = cfg.getMetadataStoreId();
			if (id != null) {
				dependencies.add(new DefaultResourceIdentifier(MetadataStoreProvider.class, id));
			}
			else {
				for (ResourceMetadata<?> md : workspace.getResourceManager(MetadataStoreManager.class)
					.getResourceMetadata()) {
					softDependencies.add(md.getIdentifier());
				}
			}

			OWSMetadataProviderManager mmgr = workspace.getResourceManager(OWSMetadataProviderManager.class);
			for (ResourceMetadata<OWSMetadataProvider> md : mmgr.getResourceMetadata()) {
				ResourceIdentifier<OWSMetadataProvider> mdId = md.getIdentifier();
				if (mdId.getId().equals(getIdentifier().getId() + "_metadata")) {
					softDependencies.add(mdId);
				}
			}

			return new CswBuilder(this, workspace, cfg);
		}
		catch (Exception e) {
			throw new ResourceInitException(e.getLocalizedMessage(), e);
		}
	}

}
