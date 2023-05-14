/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.wmts;

import java.net.URL;

import org.deegree.commons.tom.ows.Version;
import org.deegree.protocol.wmts.WMTSConstants.WMTSRequestType;
import org.deegree.services.OWS;
import org.deegree.services.OWSProvider;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * SPI provider class for WMTS services.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */

public class WMTSProvider extends OWSProvider {

	public static final ImplementationMetadata<WMTSRequestType> IMPLEMENTATION_METADATA = new ImplementationMetadata<WMTSRequestType>() {
		{
			supportedVersions = new Version[] { Version.parseVersion("1.0.0") };
			handledNamespaces = new String[] { "http://www.opengis.net/wmts/1.0" };
			handledRequests = WMTSRequestType.class;
			serviceName = new String[] { "WMTS" };
		}
	};

	@Override
	public String getNamespace() {
		return "http://www.deegree.org/services/wmts";
	}

	@Override
	public URL getSchema() {
		return WMTSProvider.class.getResource("/META-INF/schemas/services/wmts/wmts.xsd");
	}

	@Override
	public ImplementationMetadata<WMTSRequestType> getImplementationMetadata() {
		return IMPLEMENTATION_METADATA;
	}

	@Override
	public ResourceMetadata<OWS> createFromLocation(Workspace workspace, ResourceLocation<OWS> location) {
		return new WmtsMetadata(workspace, location, this);
	}

}
