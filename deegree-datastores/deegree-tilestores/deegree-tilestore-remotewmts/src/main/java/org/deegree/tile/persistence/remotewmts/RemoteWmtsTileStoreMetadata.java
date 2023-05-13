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
package org.deegree.tile.persistence.remotewmts;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;

import javax.xml.bind.JAXBException;

import org.deegree.remoteows.RemoteOWS;
import org.deegree.remoteows.RemoteOWSProvider;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.remotewmts.jaxb.RemoteWMTSTileStoreJAXB;
import org.deegree.tile.tilematrixset.TileMatrixSetProvider;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceMetadata;
import org.deegree.workspace.standard.AbstractResourceProvider;
import org.deegree.workspace.standard.DefaultResourceIdentifier;

/**
 * Resource metadata implementation for remote WMTS tile stores.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class RemoteWmtsTileStoreMetadata extends AbstractResourceMetadata<TileStore> {

	private static final String JAXB_PACKAGE = "org.deegree.tile.persistence.remotewmts.jaxb";

	public RemoteWmtsTileStoreMetadata(Workspace workspace, ResourceLocation<TileStore> location,
			AbstractResourceProvider<TileStore> provider) {
		super(workspace, location, provider);
	}

	@Override
	public ResourceBuilder<TileStore> prepare() {
		try {
			RemoteWMTSTileStoreJAXB config = unmarshallConfig();
			String wmts = config.getRemoteWMTSId();

			dependencies.add(new DefaultResourceIdentifier<RemoteOWS>(RemoteOWSProvider.class, wmts));

			// does not really collect all dependencies, only the ones that can be
			// determined from config
			// probably needs to be adapted to also use the matrix set ids from remote as
			// dependencies
			for (RemoteWMTSTileStoreJAXB.TileDataSet tds : config.getTileDataSet()) {
				if (tds.getTileMatrixSetId() != null) {
					dependencies.add(new DefaultResourceIdentifier<TileMatrixSet>(TileMatrixSetProvider.class,
							tds.getTileMatrixSetId()));
				}
			}

			return new RemoteWmtsTileStoreBuilder(config, workspace, this);
		}
		catch (Exception e) {
			throw new ResourceInitException(
					"Unable to create resource " + location.getIdentifier() + ": " + e.getLocalizedMessage(), e);
		}
	}

	private RemoteWMTSTileStoreJAXB unmarshallConfig() throws JAXBException {
		return (RemoteWMTSTileStoreJAXB) unmarshall(JAXB_PACKAGE, provider.getSchema(), location.getAsStream(),
				workspace);
	}

}
