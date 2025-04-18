/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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

package org.deegree.services.wms.controller;

import static org.deegree.commons.ows.exception.OWSException.INVALID_SRS;
import static org.deegree.services.i18n.Messages.get;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.commons.tom.ows.Version;
import org.deegree.protocol.wms.WMSConstants;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.controller.capabilities.Capabilities111XMLAdapter;
import org.deegree.services.wms.controller.exceptions.ExceptionsManager;

/**
 * <code>WMSController111</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class WMSController111 extends WMSControllerBase {

	/**
	 * @param exceptionsManager used to serialize exceptions, never <code>null</code>
	 */
	public WMSController111(ExceptionsManager exceptionsManager) {
		super(exceptionsManager);
		EXCEPTION_DEFAULT = "application/vnd.ogc.se_xml";
		EXCEPTION_BLANK = "application/vnd.ogc.se_blank";
		EXCEPTION_INIMAGE = "application/vnd.ogc.se_inimage";

		exceptionSerializer = new WMS111ExceptionReportSerializer();

		EXCEPTION_MIME = EXCEPTION_DEFAULT;
	}

	@Override
	public void sendException(OWSException ex, HttpResponseBuffer response, WMSController controller)
			throws ServletException {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Disposition", "inline; filename=\"exception.xml\"");
		controller.sendException(headers, exceptionSerializer, ex, response);
	}

	@Override
	public void throwSRSException(String name) throws OWSException {
		throw new OWSException(get("WMS.INVALID_SRS", name), INVALID_SRS);
	}

	@Override
	protected void exportCapas(String getUrl, String postUrl, MapService service, HttpResponseBuffer response,
			ServiceIdentification identification, ServiceProvider provider, Map<String, String> customParameters,
			WMSController controller, OWSMetadataProvider metadata) throws IOException, OWSException {
		response.setContentType("application/vnd.ogc.wms_xml");
		String userAgent = OGCFrontController.getContext().getUserAgent();

		if (userAgent != null && userAgent.toLowerCase().contains("mozilla")) {
			response.setContentType("application/xml");
		}

		response.addHeader("Content-Disposition", "inline; filename=\"capabilities.xml\"");
		try {
			XMLStreamWriter xmlWriter = response.getXMLWriter();
			new Capabilities111XMLAdapter(identification, provider, metadata, getUrl, postUrl, service, controller)
				.export(xmlWriter);
		}
		catch (XMLStreamException e) {
			throw new IOException(e);
		}
	}

	@Override
	protected Version getVersion() {
		return WMSConstants.VERSION_111;
	}

}
