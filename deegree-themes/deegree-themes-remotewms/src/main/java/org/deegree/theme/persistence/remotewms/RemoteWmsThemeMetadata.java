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
package org.deegree.theme.persistence.remotewms;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;

import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.LayerStoreProvider;
import org.deegree.remoteows.RemoteOWS;
import org.deegree.remoteows.RemoteOWSProvider;
import org.deegree.theme.Theme;
import org.deegree.theme.persistence.remotewms.jaxb.RemoteWMSThemes;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceMetadata;
import org.deegree.workspace.standard.AbstractResourceProvider;
import org.deegree.workspace.standard.DefaultResourceIdentifier;

/**
 * Resource metadata implementation for remote WMS themes.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class RemoteWmsThemeMetadata extends AbstractResourceMetadata<Theme> {

	public RemoteWmsThemeMetadata(Workspace workspace, ResourceLocation<Theme> location,
			AbstractResourceProvider<Theme> provider) {
		super(workspace, location, provider);
	}

	@Override
	public ResourceBuilder<Theme> prepare() {
		try {
			RemoteWMSThemes cfg = (RemoteWMSThemes) unmarshall("org.deegree.theme.persistence.remotewms.jaxb",
					provider.getSchema(), location.getAsStream(), workspace);
			String id = cfg.getRemoteWMSId();

			String lid = cfg.getLayerStoreId();

			dependencies.add(new DefaultResourceIdentifier<LayerStore>(LayerStoreProvider.class, lid));
			dependencies.add(new DefaultResourceIdentifier<RemoteOWS>(RemoteOWSProvider.class, id));

			return new RemoteWmsThemeBuilder(this, workspace, cfg);
		}
		catch (Exception e) {
			throw new ResourceInitException("Could not parse remote WMS theme config.", e);
		}
	}

}
