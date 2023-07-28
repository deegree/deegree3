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

import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.xml.XsltUtils;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * Serializes an exception as exception and transforms the exception with a given xslt
 * script.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class XsltExceptionSerializer implements ExceptionsSerializer {

	private static final Logger LOG = getLogger(ExceptionsManager.class);

	private final String contenType;

	private final URL xsltUrl;

	private final Workspace workspace;

	public XsltExceptionSerializer(String contentType, URL xsltUrl, Workspace workspace) {
		contenType = contentType;
		this.xsltUrl = xsltUrl;
		this.workspace = workspace;
	}

	@Override
	public void serializeException(HttpResponseBuffer response, OWSException ex,
			XMLExceptionSerializer exceptionSerializer, Map<String, String> map) throws SerializingException {
		try {
			response.setContentType(contenType);
			ByteArrayOutputStream stream = writeToStream(ex, exceptionSerializer);
			transform(new ByteArrayInputStream(stream.toByteArray()), response);
		}
		catch (XMLStreamException e) {
			throw new SerializingException(e);
		}
		catch (FactoryConfigurationError e) {
			throw new SerializingException(e);
		}
	}

	private ByteArrayOutputStream writeToStream(OWSException ex, XMLExceptionSerializer exceptionSerializer)
			throws XMLStreamException, FactoryConfigurationError {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(stream);
		try {
			exceptionSerializer.serializeExceptionToXML(writer, ex);
			return stream;
		}
		finally {
			writer.close();
		}
	}

	private void transform(InputStream stream, HttpResponseBuffer response) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(workspace.getModuleClassLoader());
		try {
			OutputStream outputStream = response.getOutputStream();
			XsltUtils.transform(stream, this.xsltUrl, outputStream);
		}
		catch (Exception e) {
			LOG.warn("Unable to transform Capabilities: {}.", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
		}
		finally {
			Thread.currentThread().setContextClassLoader(loader);
		}
	}

}