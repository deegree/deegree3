/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2014 by:

 IDgis bv

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

 IDgis bv
 Boomkamp 16
 7461 AX Rijssen
 The Netherlands
 http://idgis.nl/

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

package org.deegree.services.controller.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.featureinfo.FeatureInfoContext;

public class StandardFeatureInfoContext implements FeatureInfoContext {

	private final HttpResponseBuffer response;

	private OutputStream outputStream = null;

	private XMLStreamWriter xmlWriter = null;

	private Writer writer = null;

	private boolean redirected = false;

	public StandardFeatureInfoContext(HttpResponseBuffer response) {
		this.response = response;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {

		if (outputStream != null) {
			return outputStream;
		}

		if (redirected) {
			throw new IllegalStateException("sendRedirect() already called for FeatureInfoContext");
		}

		if (xmlWriter != null) {
			throw new IllegalStateException("getXmlWriter() already called for FeatureInfoContext");
		}

		return outputStream = response.getOutputStream();
	}

	@Override
	public XMLStreamWriter getXmlWriter() throws IOException, XMLStreamException {

		if (xmlWriter != null) {
			return xmlWriter;
		}

		if (redirected) {
			throw new IllegalStateException("sendRedirect() already called for FeatureInfoContext");
		}

		if (outputStream != null) {
			throw new IllegalStateException("getOutputStream() already called for FeatureInfoContext");
		}

		return xmlWriter = response.getXMLWriter();
	}

	@Override
	public Writer getWriter() throws IOException {
		if (writer != null) {
			return writer;
		}
		if (redirected) {
			throw new IllegalStateException("sendRedirect() already called for FeatureInfoContext");
		}
		if (outputStream != null) {
			throw new IllegalStateException("getOutputStream() already called for FeatureInfoContext");
		}
		return writer = response.getWriter();
	}

	@Override
	public void sendRedirect(String location) throws IOException {

		if (redirected) {
			throw new IllegalStateException("sendRedirect() already called for FeatureInfoContext");
		}

		if (outputStream != null) {
			throw new IllegalStateException("getOutputStream() already called for FeatureInfoContext");
		}

		if (xmlWriter != null) {
			throw new IllegalStateException("getXmlWriter() already called for FeatureInfoContext");
		}

		response.getWrappee().sendRedirect(location);

		redirected = true;
	}

}
