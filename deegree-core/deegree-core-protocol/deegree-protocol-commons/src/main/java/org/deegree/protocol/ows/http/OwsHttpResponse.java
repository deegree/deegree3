/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.protocol.ows.http;

import java.io.Closeable;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.http.HttpResponse;
import org.deegree.protocol.ows.exception.OWSExceptionReport;

/**
 * Encapsulates an HTTP response from an OGC web service.
 * <p>
 * NOTE: The receiver <b>must</b> call {@link #close()} eventually, otherwise the HTTP
 * connection will not be freed.
 * </p>
 *
 */
public interface OwsHttpResponse extends Closeable {

	/**
	 * Provides access to the raw response.
	 * @return http response, never <code>null</code>
	 */
	public HttpResponse getAsHttpResponse();

	/**
	 * Provides access to the response body as a binary stream.
	 * @return binary stream, never <code>null</code>
	 */
	public CloseRequiredInputStream getAsBinaryStream();

	/**
	 * Provides access to the response body as an XML stream.
	 * @return xml stream, never <code>null</code>
	 * @throws OWSExceptionReport if the stream contains an XML-encoded OWS Exception
	 * report
	 * @throws XMLStreamException if accessing the stream fails (e.g. no XML payload)
	 */
	public XMLStreamReader getAsXMLStream() throws OWSExceptionReport, XMLStreamException;

	/**
	 * Throws an {@link OWSExceptionReport} if the status code is not 200 (OK).
	 * @throws OWSExceptionReport if status code isn't 200
	 */
	public void assertHttpStatus200() throws OWSExceptionReport;

	/**
	 * Throws an {@link OWSExceptionReport} if the <code>Content-Type</code> header
	 * indicates an XML response and the contained document actually is an exception
	 * report.
	 * @throws OWSExceptionReport if XML content type and payload is an exception report
	 */
	public void assertNoXmlContentTypeAndExceptionReport() throws OWSExceptionReport, XMLStreamException;

	/**
	 * Closes the HTTP connection.
	 */
	@Override
	public void close();

}
