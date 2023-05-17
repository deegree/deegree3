/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.http.HttpResponse;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.protocol.ows.exception.OWSExceptionReader;
import org.deegree.protocol.ows.exception.OWSExceptionReport;

/**
 * {@link OwsHttpResponse} used by {@link OwsHttpClientMock}.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
class OwsHttpResponseMock implements OwsHttpResponse {

	private final URL responseBody;

	private final String contentType;

	private final int httpStatus;

	OwsHttpResponseMock(URL responseBody, String contentType, int httpStatus) {
		this.responseBody = responseBody;
		this.contentType = contentType;
		this.httpStatus = httpStatus;
	}

	@Override
	public HttpResponse getAsHttpResponse() {
		throw new UnsupportedOperationException();
	}

	@Override
	public CloseRequiredInputStream getAsBinaryStream() {
		try {
			return new CloseRequiredInputStream(this, responseBody.openStream());
		}
		catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public XMLStreamReader getAsXMLStream() throws OWSExceptionReport, XMLStreamException {
		return XMLInputFactory.newInstance().createXMLStreamReader(getAsBinaryStream(), "UTF-8");
	}

	@Override
	public void assertHttpStatus200() throws OWSExceptionReport {
		if (httpStatus != 200) {
			List<OWSException> exceptions = Collections.emptyList();
			throw new OWSExceptionReport(exceptions, null, null);
		}
	}

	@Override
	public void assertNoXmlContentTypeAndExceptionReport() throws OWSExceptionReport, XMLStreamException {
		if ("text/xml".equals(contentType)) {
			XMLStreamReader xmlStream = getAsXMLStream();
			OWSExceptionReader.assertNoExceptionReport(xmlStream);
		}
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
	}

}
