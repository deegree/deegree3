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

package org.deegree.services.wmts.controller;

import static org.deegree.services.metadata.MetadataUtils.convertFromJAXB;

import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.featureinfo.FeatureInfoManager;
import org.deegree.protocol.ows.getcapabilities.GetCapabilitiesKVPParser;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.services.metadata.provider.OWSMetadataProviderProvider;
import org.deegree.services.wmts.controller.capabilities.WMTSCapabilitiesWriter;
import org.deegree.theme.Theme;
import org.deegree.workspace.Workspace;

/**
 * Responsible for handling capabilities requests.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */

class CapabilitiesHandler {

	private ServiceIdentification identification;

	private ServiceProvider provider;

	private String metadataUrlTemplate;

	private List<Theme> themes;

	private FeatureInfoManager mgr;

	CapabilitiesHandler(DeegreeServicesMetadataType mainMetadataConf, Workspace workspace, String metadataUrlTemplate,
			String wmtsId, List<Theme> themes, FeatureInfoManager mgr) {
		this.themes = themes;
		this.mgr = mgr;
		identification = convertFromJAXB(mainMetadataConf.getServiceIdentification());
		provider = convertFromJAXB(mainMetadataConf.getServiceProvider());

		OWSMetadataProvider metadata = workspace.getResource(OWSMetadataProviderProvider.class, wmtsId + "_metadata");
		if (metadata != null) {
			identification = metadata.getServiceIdentification();
			provider = metadata.getServiceProvider();
		}

		this.metadataUrlTemplate = metadataUrlTemplate;
	}

	void handleGetCapabilities(Map<String, String> map, XMLStreamWriter writer) throws XMLStreamException {
		// GetCapabilities gc =
		GetCapabilitiesKVPParser.parse(map);
		new WMTSCapabilitiesWriter(writer, identification, provider, themes, metadataUrlTemplate, mgr).export100();
	}

}
