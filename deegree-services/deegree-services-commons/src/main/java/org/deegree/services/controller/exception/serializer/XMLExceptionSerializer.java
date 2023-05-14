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

package org.deegree.services.controller.exception.serializer;

import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.deegree.commons.ows.exception.OWSException.NOT_FOUND;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.services.controller.utils.HttpResponseBuffer;

/**
 * The <code>XMLExceptionSerializer</code> class TODO add class documentation here.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 */
public abstract class XMLExceptionSerializer implements ExceptionSerializer {

	@Override
	public void serializeException(HttpResponseBuffer response, OWSException exception)
			throws IOException, XMLStreamException {

		response.reset();
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/vnd.ogc.se_xml");
		setExceptionStatusCode(response, exception);
		setStatusCode(response, exception);
		serializeExceptionToXML(response.getXMLWriter(), exception);
	}

	/**
	 * Sets the statusCode to the response.
	 * @param response
	 * @param exception
	 */
	public void setExceptionStatusCode(HttpResponseBuffer response, OWSException exception) {
		response.setStatus(200);
	}

	/**
	 * Implementations can use the xml writer to serialize the given exception as a
	 * specific xml representation.
	 * @param writer a formatting xml writer, wrapped around an output stream.
	 * @param exception to serialize
	 * @throws XMLStreamException if an error occurred while serializing the given
	 * exception.
	 */
	public abstract void serializeExceptionToXML(XMLStreamWriter writer, OWSException exception)
			throws XMLStreamException;

	/**
	 * Sets the status code to the response.
	 * @param exception the exception to serialize, never <code>null</code>
	 * @param response the response to set the status code for, never <code>null</code>
	 */
	protected void setStatusCode(HttpResponseBuffer response, OWSException exception) {
		if (NOT_FOUND.equals(exception.getExceptionCode()))
			response.setStatus(SC_NOT_FOUND);
		else
			response.setStatus(200);
	}

}