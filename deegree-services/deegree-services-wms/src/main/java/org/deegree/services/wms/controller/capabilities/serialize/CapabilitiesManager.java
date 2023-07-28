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
package org.deegree.services.wms.controller.capabilities.serialize;

import static org.deegree.commons.ows.exception.OWSException.INVALID_PARAMETER_VALUE;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * Manages GetCapabilities formats.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class CapabilitiesManager {

	private static final Logger LOG = getLogger(CapabilitiesManager.class);

	private final Map<String, CapabilitiesSerializer> capabilitiesSerializers = new LinkedHashMap<String, CapabilitiesSerializer>();

	/**
	 * @param addDefaultFormats true if the default format (text/xml) should be enabled.
	 */
	public CapabilitiesManager(boolean addDefaultFormats) {
		if (addDefaultFormats)
			capabilitiesSerializers.put("text/xml", new CopySerializer());
	}

	/**
	 * Adds the new format or replace a format with the same name if it exists.
	 * @param format to add, never <code>null</code>
	 * @param xsltUrl the url to the cswlt script, never <code>null</code>
	 * @param workspace the workspace never <code>null</code>
	 */
	public void addOrReplaceXsltFormat(String format, URL xsltUrl, Workspace workspace) {
		LOG.debug("Adding xslt feature info format {}", format);

		XsltCapabilitiesSerializer xslt = new XsltCapabilitiesSerializer(xsltUrl, workspace);
		capabilitiesSerializers.put(format.toLowerCase(), xslt);
	}

	/**
	 * Checks if the requested format is supported.
	 * @param format never <code>null</code>
	 * @return <code>true</code> if the format is supported, <code>false</code> otherwise
	 */
	public boolean isSupported(String format) {
		return capabilitiesSerializers.containsKey(format);
	}

	/**
	 * @return all supported formats, never <code>null</code>
	 */
	public Set<String> getSupportedFormats() {
		return capabilitiesSerializers.keySet();
	}

	/**
	 * Writes the capabilities in the requested format in the response stream.
	 * @param format requested format, never <code>null</code>
	 * @param capabilitiesXmlStream the capabilities xml as stream, never
	 * <code>null</code>
	 * @param responseStream to write the capabilities in, never <code>null</code>
	 * @throws IOException if an error occured
	 * @throws OWSException if the requested format is not supported
	 */
	public void serializeCapabilities(String format, InputStream capabilitiesXmlStream, OutputStream responseStream)
			throws IOException, OWSException {
		LOG.debug("Generating capabilities output for format: {}", format);

		CapabilitiesSerializer serializer = capabilitiesSerializers.get(format.toLowerCase());
		if (serializer != null) {
			serializer.serialize(capabilitiesXmlStream, responseStream);
		}
		else {
			throw new OWSException("Capabilities format '" + format + "' is unknown.", INVALID_PARAMETER_VALUE);
		}
	}

}