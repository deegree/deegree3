/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.services.wms.controller.exceptions;

import static org.deegree.protocol.wms.WMSConstants.VERSION_111;
import static org.deegree.protocol.wms.WMSConstants.VERSION_130;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ows.Version;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.wms.controller.WMSController;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * Manages exception formats.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class ExceptionsManager {

	private static final Logger LOG = getLogger(ExceptionsManager.class);

	private final Map<Version, Map<String, ExceptionsSerializer>> exceptionSerializers = new LinkedHashMap<Version, Map<String, ExceptionsSerializer>>();

	/**
	 * @param addDefaultFormats true if the default format (text/xml) should be enabled.
	 * @param controller
	 * @param exceptionSerializer
	 */
	public ExceptionsManager(boolean addDefaultFormats, WMSController controller) {
		if (addDefaultFormats) {
			addSerializer(VERSION_111, "application/vnd.ogc.se_xml", new XmlExceptionSerializer(controller));
			addSerializer(VERSION_111, "application/vnd.ogc.se_blank", new BlankExceptionSerializer(controller));
			addSerializer(VERSION_111, "application/vnd.ogc.se_inimage", new InImageExceptionSerializer(controller));

			addSerializer(VERSION_130, "XML", new XmlExceptionSerializer(controller));
			addSerializer(VERSION_130, "BLANK", new BlankExceptionSerializer(controller));
			addSerializer(VERSION_130, "INIMAGE", new InImageExceptionSerializer(controller));

		}
	}

	/**
	 * Adds the new format or replace a format with the same name if it exists. Default
	 * formats are not overwritten!
	 * @param format to add, never <code>null</code>
	 * @param xsltUrl the url to the cswlt script, never <code>null</code>
	 * @param workspace the workspace never <code>null</code>
	 */
	public void addOrReplaceXsltFormat(String format, URL xsltUrl, Workspace workspace) {
		LOG.debug("Adding xslt exception format {}", format);
		addSerializer(VERSION_111, format, new XsltExceptionSerializer(format, xsltUrl, workspace));
		addSerializer(VERSION_130, format, new XsltExceptionSerializer(format, xsltUrl, workspace));
	}

	/**
	 * Checks if the requested format is supported.
	 * @param format never <code>null</code>
	 * @return <code>true</code> if the format is supported, <code>false</code> otherwise
	 */
	public boolean isSupported(String format) {
		return exceptionSerializers.containsKey(format);
	}

	/**
	 * @return all supported formats, never <code>null</code>
	 */
	public Set<String> getSupportedFormats(Version version) {
		if (!exceptionSerializers.containsKey(version))
			exceptionSerializers.put(version, new HashMap<String, ExceptionsSerializer>());
		return exceptionSerializers.get(version).keySet();
	}

	/**
	 * Writes the exception in the requested format in the response stream.
	 * @param format requested format, never <code>null</code>
	 * @param response to write exception in, never <code>null</code>
	 * @param exception to write, never <code>null</code>
	 * @param default exception serializer to use, never <code>null</code>
	 * @throws IOException if an error occurred
	 * @throws XMLStreamException if an error occurred
	 * @throws OWSException if an error occurred if the requested format is not supported
	 */
	public void serialize(Version version, String format, HttpResponseBuffer response, OWSException exception,
			XMLExceptionSerializer exceptionSerializer, Map<String, String> map) throws SerializingException {
		LOG.debug("Generating capabilities output for format: {}", format);

		ExceptionsSerializer serializer = getSerializer(version, format);
		if (serializer != null) {
			serializer.serializeException(response, exception, exceptionSerializer, map);
		}
		else {
			throw new SerializingException("Exceptions format '" + format + "' is unknown.");
		}
	}

	private ExceptionsSerializer getSerializer(Version version, String format) {
		Map<String, ExceptionsSerializer> format2serializer = exceptionSerializers.get(version);
		if (format2serializer != null)
			return format2serializer.get(format);
		return null;
	}

	private void addSerializer(Version version, String format, ExceptionsSerializer xmlExceptionSerializer) {
		if (!exceptionSerializers.containsKey(version))
			exceptionSerializers.put(version, new HashMap<String, ExceptionsSerializer>());
		exceptionSerializers.get(version).put(format, xmlExceptionSerializer);
	}

}