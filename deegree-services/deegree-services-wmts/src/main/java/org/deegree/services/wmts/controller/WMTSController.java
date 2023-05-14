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

import static org.deegree.commons.ows.exception.OWSException.OPERATION_NOT_SUPPORTED;
import static org.deegree.commons.tom.ows.Version.parseVersion;
import static org.deegree.protocol.wmts.WMTSConstants.VERSION_100;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.fileupload.FileItem;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.RequestUtils;
import org.deegree.protocol.wmts.WMTSConstants.WMTSRequestType;
import org.deegree.services.OWS;
import org.deegree.services.OWSProvider;
import org.deegree.services.controller.AbstractOWS;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.deegree.services.ows.OWS110ExceptionReportSerializer;
import org.deegree.services.wmts.jaxb.DeegreeWMTS;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * OWS implementation for WMTS protocol.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class WMTSController extends AbstractOWS {

	private static final Logger LOG = getLogger(WMTSController.class);

	private String metadataUrlTemplate;

	private WmtsRequestDispatcher dispatcher;

	public WMTSController(ResourceMetadata<OWS> metadata, Workspace workspace, Object jaxbConfig) {
		super(metadata, workspace, jaxbConfig);
	}

	@Override
	public void init(DeegreeServicesMetadataType serviceMetadata, DeegreeServiceControllerType mainConfig,
			Object controllerConf) {
		WmtsBuilder builder = new WmtsBuilder(workspace, (DeegreeWMTS) controllerConf);

		this.metadataUrlTemplate = builder.getMetadataUrlTemplate();

		dispatcher = new WmtsRequestDispatcher((DeegreeWMTS) controllerConf, serviceMetadata, workspace, builder,
				getMetadata().getIdentifier().getId(), getMetadata().getLocation());
	}

	@Override
	public void doKVP(Map<String, String> map, HttpServletRequest request, HttpResponseBuffer response,
			List<FileItem> multiParts) throws ServletException, IOException {
		RequestUtils.getCurrentThreadRequestParameters().set(map);
		try {
			ImplementationMetadata<?> serviceInfo = ((OWSProvider) getMetadata().getProvider())
				.getImplementationMetadata();

			String v = map.get("VERSION");
			Version version = v == null ? serviceInfo.getImplementedVersions().iterator().next() : parseVersion(v);

			WMTSRequestType req;
			try {
				req = (WMTSRequestType) ((ImplementationMetadata) serviceInfo).getRequestTypeByName(map.get("REQUEST"));
			}
			catch (IllegalArgumentException e) {
				sendException(new OWSException("'" + map.get("REQUEST") + "' is not a supported WMTS operation.",
						OPERATION_NOT_SUPPORTED), response);
				return;
			}
			catch (NullPointerException e) {
				sendException(new OWSException("The REQUEST parameter is missing.", OPERATION_NOT_SUPPORTED), response);
				return;
			}

			try {
				dispatcher.handleRequest(req, response, map, version);
			}
			catch (OWSException e) {
				LOG.debug("The response is an exception with the message '{}'", e.getLocalizedMessage());
				LOG.trace("Stack trace of OWSException being sent", e);

				sendException(e, response);
			}
		}
		finally {
			RequestUtils.getCurrentThreadRequestParameters().remove();
		}
	}

	@Override
	public void doXML(XMLStreamReader xmlStream, HttpServletRequest request, HttpResponseBuffer response,
			List<FileItem> multiParts) throws ServletException, IOException {
		OWSException ex = new OWSException("XML support is not implemented for WMTS.",
				OWSException.OPERATION_NOT_SUPPORTED);
		sendException(ex, response);
	}

	@Override
	public void destroy() {
		// anything to destroy?
	}

	private void sendException(OWSException e, HttpResponseBuffer response) throws ServletException {
		sendException(null, new OWS110ExceptionReportSerializer(VERSION_100), e, response);
	}

	/**
	 * @return null, if no template is configured
	 */
	public String getMetadataUrlTemplate() {
		return metadataUrlTemplate;
	}

}
