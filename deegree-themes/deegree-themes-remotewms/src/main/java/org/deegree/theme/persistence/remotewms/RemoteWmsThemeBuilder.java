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

import static org.deegree.theme.Themes.aggregateSpatialMetadata;

import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.struct.Tree;
import org.deegree.layer.Layer;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.LayerStoreProvider;
import org.deegree.protocol.wms.client.WMSClient;
import org.deegree.remoteows.RemoteOWS;
import org.deegree.remoteows.RemoteOWSProvider;
import org.deegree.remoteows.wms.RemoteWMS;
import org.deegree.theme.Theme;
import org.deegree.theme.persistence.remotewms.jaxb.RemoteWMSThemes;
import org.deegree.theme.persistence.standard.StandardTheme;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * This class is responsible for building remote WMS themes.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class RemoteWmsThemeBuilder implements ResourceBuilder<Theme> {

	private ResourceMetadata<Theme> metadata;

	private Workspace workspace;

	private RemoteWMSThemes cfg;

	public RemoteWmsThemeBuilder(ResourceMetadata<Theme> metadata, Workspace workspace, RemoteWMSThemes cfg) {
		this.metadata = metadata;
		this.workspace = workspace;
		this.cfg = cfg;
	}

	@Override
	public Theme build() {
		try {
			String id = cfg.getRemoteWMSId();

			String lid = cfg.getLayerStoreId();
			LayerStore store = workspace.getResource(LayerStoreProvider.class, lid);
			if (store == null) {
				throw new ResourceInitException("The layer store with id " + lid + " was not available.");
			}

			RemoteOWS ows = workspace.getResource(RemoteOWSProvider.class, id);
			if (!(ows instanceof RemoteWMS)) {
				throw new ResourceInitException(
						"The remote OWS store with id " + id + " was not of type WMS or was not available.");
			}

			WMSClient client = ((RemoteWMS) ows).getClient();
			Tree<LayerMetadata> tree = client.getLayerTree();

			Theme theme = buildTheme(tree, store);
			aggregateSpatialMetadata(theme);
			return theme;
		}
		catch (Exception e) {
			throw new ResourceInitException("Could not parse remote WMS theme config.", e);
		}
	}

	private Theme buildTheme(Tree<LayerMetadata> tree, LayerStore store) {
		List<Theme> thms = new ArrayList<Theme>();
		List<Layer> lays = new ArrayList<Layer>();
		if (tree.value.getName() != null) {
			Layer l = store.get(tree.value.getName());
			if (l != null) {
				lays.add(l);
			}
		}
		Theme thm = new StandardTheme(tree.value, thms, lays, metadata);
		for (Tree<LayerMetadata> child : tree.children) {
			thms.add(buildTheme(child, store));
		}
		return thm;
	}

}
