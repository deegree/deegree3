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
package org.deegree.theme.persistence.standard;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;

import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.LayerStoreProvider;
import org.deegree.theme.Theme;
import org.deegree.theme.persistence.standard.jaxb.Themes;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceMetadata;
import org.deegree.workspace.standard.AbstractResourceProvider;
import org.deegree.workspace.standard.DefaultResourceIdentifier;

/**
 * Metadata implementation for standard themes.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class StandardThemeMetadata extends AbstractResourceMetadata<Theme> {

	public StandardThemeMetadata(Workspace workspace, ResourceLocation<Theme> location,
			AbstractResourceProvider<Theme> provider) {
		super(workspace, location, provider);
	}

	@Override
	public ResourceBuilder<Theme> prepare() {
		String pkg = "org.deegree.theme.persistence.standard.jaxb";
		try {
			Themes cfg;
			cfg = (Themes) unmarshall(pkg, provider.getSchema(), location.getAsStream(), workspace);

			for (String id : cfg.getLayerStoreId()) {
				softDependencies.add(new DefaultResourceIdentifier<LayerStore>(LayerStoreProvider.class, id));
			}

			return new StandardThemeBuilder(cfg, this, workspace);
		}
		catch (Exception e) {
			throw new ResourceInitException("Could not parse theme configuration file.", e);
		}
	}

}
