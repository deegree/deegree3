/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.wfs;

import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_NS;

import java.net.URL;

import org.deegree.commons.tom.ows.Version;
import org.deegree.protocol.wfs.WFSRequestType;
import org.deegree.services.OWS;
import org.deegree.services.OWSProvider;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * {@link OWSProvider} for the {@link WebFeatureService}.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class WFSProvider extends OWSProvider {

	protected static final ImplementationMetadata<WFSRequestType> IMPLEMENTATION_METADATA = new ImplementationMetadata<WFSRequestType>() {
		{
			supportedVersions = new Version[] { VERSION_100, VERSION_110, VERSION_200 };
			handledNamespaces = new String[] { WFS_NS, WFS_200_NS };
			handledRequests = WFSRequestType.class;
			serviceName = new String[] { "WFS" };
		}
	};

	@Override
	public String getNamespace() {
		return "http://www.deegree.org/services/wfs";
	}

	@Override
	public URL getSchema() {
		return WFSProvider.class.getResource("/META-INF/schemas/services/wfs/wfs_configuration.xsd");
	}

	@Override
	public ImplementationMetadata<WFSRequestType> getImplementationMetadata() {
		return IMPLEMENTATION_METADATA;
	}

	@Override
	public ResourceMetadata<OWS> createFromLocation(Workspace workspace, ResourceLocation<OWS> location) {
		return new WfsMetadata(workspace, location, this);
	}

}
