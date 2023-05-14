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

package org.deegree.services;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.utils.io.StreamBufferStore;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.exception.serializer.ExceptionSerializer;
import org.slf4j.Logger;

/**
 * Sink for producing the HTTP response to an {@link OWS} request.
 * <p>
 * This is similar to {@link HttpServletResponse}, but reduces potential pitfalls and
 * offers specific functionality that's common for {@link OWS} implementations.
 * </p>
 * <p>
 * Special functionality:
 * <ul>
 * <li><b>Buffering</b>: By default, output is buffered and only sent to the client when
 * {@link #commit()} is called. This allows to set headers (e.g 'Content-length') even
 * after the response data has been produced and to rollback the response (e.g. in order
 * to generate an ExceptionReport response after a partial response has already been
 * written). Alternatively, buffering can be disabled by calling
 * {@link #disableBuffering()} to turn of buffering overhead.</li>
 * <li><b>XML output</b>: Convenience methods for getting an {@link XMLStreamWriter}.</li>
 * <li><b>Transparent compression</b>: If the client supports it (and the mime type of the
 * output is suitable for this), output is automatically written in gzipped form.</li>
 * <li><b>Logging</b>:</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class OWSResponse {

	private static final Logger LOG = getLogger(OWSResponse.class);

	// true, if the client supports gzip-compression
	private final boolean supportsGzip;

	private final HttpServletResponse response;

	// actual servlet output stream (never null)
	private final ServletOutputStream os;

	// if null, buffering is disabled
	private StreamBufferStore bos;

	// if null, gzip-compression is not used
	private GZIPOutputStream gos;

	// if null, no XML output has been requested
	private XMLStreamWriter xmlWriter;

	private boolean outputObjectRequested;

	/**
	 * Creates a new {@link OWSResponse} instance.
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	public OWSResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
		this.response = response;
		os = response.getOutputStream();
		bos = new StreamBufferStore();

		// check if client supports gzip
		String encoding = request.getHeader("Accept-Encoding");
		supportsGzip = encoding != null && encoding.toLowerCase().contains("gzip");
	}

	/**
	 * Disables buffering of the output.
	 * <p>
	 * This method may only be called, if neither {@link #getOutputStream(String))} nor
	 * {@link #getXMLStream(String))} has been called before.
	 * </p>
	 */
	public void disableBuffering() {
		if (outputObjectRequested) {
			throw new IllegalStateException();
		}
		LOG.debug("Disabling buffering.");
		this.bos = null;
	}

	/**
	 * Returns an {@link OutputStream} for writing a response.
	 * @param contentType mime type of the response content, can be <code>null</code>
	 * @return
	 * @throws IOException
	 * @throws IllegalStateException
	 */
	public OutputStream getOutputStream(String contentType) throws IOException {
		if (outputObjectRequested) {
			throw new IllegalStateException();
		}
		outputObjectRequested = true;

		OutputStream os = this.os;
		if (bos != null) {
			os = bos;
		}

		// set content type header
		if (contentType != null) {
			response.setContentType(contentType);
			// enable gzip compression, if content is suitable && supported
			if (supportsGzip) {
				if (contentType.startsWith("text") || contentType.startsWith("application/xml")) {
					gos = new GZIPOutputStream(os);
					os = gos;
					response.setHeader("Content-Encoding", "gzip");
				}
			}
		}
		return os;
	}

	/**
	 * Returns an {@link XMLStreamWriter} for writing an XML response.
	 * @param contentType mime type of the response content, can be <code>null</code>
	 * @return
	 * @throws IOException
	 * @throws XMLStreamException
	 * @throws IllegalStateException
	 */
	public XMLStreamWriter getXMLWriter(String contentType) throws IOException, XMLStreamException {

		if (outputObjectRequested) {
			throw new IllegalStateException();
		}
		outputObjectRequested = true;

		OutputStream os = this.os;
		if (bos != null) {
			os = bos;
		}

		// set content type header
		if (contentType != null) {
			response.setContentType(contentType);
		}

		// enable gzip compression, if supported
		if (supportsGzip) {
			gos = new GZIPOutputStream(os);
			os = gos;
			response.setHeader("Content-Encoding", "gzip");
		}

		// create XMLStreamWriter
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		factory.setProperty("javax.xml.stream.isRepairingNamespaces", Boolean.TRUE);
		xmlWriter = new IndentingXMLStreamWriter(factory.createXMLStreamWriter(os, "UTF-8"));
		xmlWriter.writeStartDocument("UTF-8", "1.0");
		return xmlWriter;
	}

	/**
	 * Commits the response so it gets sent to the client.
	 * <p>
	 * {@link OWS} implementation should never call this, as it is called by the
	 * {@link OGCFrontController}.
	 * </p>
	 * @throws IOException
	 */
	public void commit() throws IOException {
		if (xmlWriter != null) {
			try {
				xmlWriter.close();
			}
			catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (gos != null) {
			gos.close();
		}
		if (bos != null) {
			bos.close();
		}
		os.flush();
		response.flushBuffer();
	}

	/**
	 * Performs a rollback of the written data and HTTP status codes / headers.
	 * <p>
	 * This is only possible if the response has not been committed yet and buffering is
	 * enabled.
	 * </p>
	 * @throws IllegalStateException
	 */
	private void rollback() throws IllegalStateException {
		if (response.isCommitted()) {
			throw new IllegalStateException("Cannot rollback response, already committed.");
		}
		if (bos == null) {
			throw new IllegalStateException("Cannot rollback response, buffering was disabled.");
		}
		bos.reset();
		gos = null;
		xmlWriter = null;
		response.reset();
	}

	/**
	 * Sends an {@link OWSException} to the client.
	 * @param contentType
	 * @param encoding
	 * @param additionalHeaders
	 * @param httpStatusCode
	 * @param serializer
	 * @param exception
	 */
	public void sendException(String contentType, Map<String, String> additionalHeaders, int httpStatusCode,
			ExceptionSerializer serializer, OWSException exception) {
		rollback();
	}

}