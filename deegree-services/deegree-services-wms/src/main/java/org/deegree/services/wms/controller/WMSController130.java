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

import static org.deegree.commons.ows.exception.OWSException.INVALID_PARAMETER_VALUE;
import static org.deegree.services.i18n.Messages.get;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.commons.tom.ows.Version;
import org.deegree.protocol.wms.WMSConstants;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.services.ows.PreOWSExceptionReportSerializer;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.controller.capabilities.Capabilities130XMLAdapter;
import org.deegree.services.wms.controller.capabilities.serialize.CapabilitiesManager;
import org.deegree.services.wms.controller.exceptions.ExceptionsManager;

/**
 * <code>WMSController130</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class WMSController130 extends WMSControllerBase {

	private static final String TEXT_XML_FORMAT = "text/xml";

	private final CapabilitiesManager capabilitiesManager;

	/**
	 * @param capabilitiesManager handling export of capabilities, never <code>null</code>
	 * @param exceptionsManager used to serialize exceptions, never <code>null</code>
	 */
	public WMSController130(CapabilitiesManager capabilitiesManager, ExceptionsManager exceptionsManager) {
		super(exceptionsManager);
		this.capabilitiesManager = capabilitiesManager;
		EXCEPTION_DEFAULT = "XML";
		EXCEPTION_BLANK = "BLANK";
		EXCEPTION_INIMAGE = "INIMAGE";
		exceptionSerializer = new PreOWSExceptionReportSerializer(EXCEPTION_MIME);
	}

	@Override
	public void sendException(OWSException ex, HttpResponseBuffer response, WMSController controller)
			throws ServletException {
		controller.sendException(null, exceptionSerializer, ex, response);
	}

	@Override
	public void throwSRSException(String name) throws OWSException {
		throw new OWSException(get("WMS.INVALID_SRS", name), OWSException.INVALID_CRS);
	}

	@Override
	protected void exportCapas(String getUrl, String postUrl, MapService service, HttpResponseBuffer response,
			ServiceIdentification identification, ServiceProvider provider, Map<String, String> customParameters,
			WMSController controller, OWSMetadataProvider metadata) throws IOException, OWSException {
		String format = detectFormat(customParameters);
		response.setContentType(format);

		try {
			if (TEXT_XML_FORMAT.equals(format)) {
				XMLStreamWriter xmlWriter = response.getXMLWriter();
				new Capabilities130XMLAdapter(identification, provider, metadata, getUrl, postUrl, service, controller)
					.export(xmlWriter);
			}
			else {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(stream);
				new Capabilities130XMLAdapter(identification, provider, metadata, getUrl, postUrl, service, controller)
					.export(xmlWriter);
				xmlWriter.close();
				capabilitiesManager.serializeCapabilities(format, new ByteArrayInputStream(stream.toByteArray()),
						response.getOutputStream());
			}
		}
		catch (XMLStreamException e) {
			throw new IOException(e);
		}
	}

	@Override
	protected Version getVersion() {
		return WMSConstants.VERSION_130;
	}

	private String detectFormat(Map<String, String> customParameters) throws OWSException {
		String format = customParameters.get("FORMAT");
		if (capabilitiesManager.isSupported(format))
			return format;

		if (capabilitiesManager.isSupported(TEXT_XML_FORMAT))
			return TEXT_XML_FORMAT;
		throw new OWSException("Requested format '" + format + "' is not supported!", INVALID_PARAMETER_VALUE);
	}

}