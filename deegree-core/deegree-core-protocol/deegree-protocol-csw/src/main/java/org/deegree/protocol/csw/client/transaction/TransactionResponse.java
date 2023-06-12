/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.protocol.csw.client.transaction;

import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_PREFIX;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLProcessingException;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.deegree.protocol.ows.http.OwsHttpResponse;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class TransactionResponse extends XMLAdapter {

	private final OwsHttpResponse response;

	static {
		nsContext.addNamespace(CSW_202_PREFIX, CSW_202_NS);
	}

	public OwsHttpResponse getResponse() {
		return response;
	}

	public TransactionResponse(OwsHttpResponse response)
			throws XMLProcessingException, OWSExceptionReport, XMLStreamException {
		this.response = response;
		this.load(response.getAsXMLStream());
	}

	public int getNumberOfRecordsInserted() {
		return getNodeAsInt(getRootElement(), getXPath("totalInserted"), 0);
	}

	public int getNumberOfRecordsUpdated() {
		return getNodeAsInt(getRootElement(), getXPath("totalUpdated"), 0);
	}

	public int getNumberOfRecordsDeleted() {
		return getNodeAsInt(getRootElement(), getXPath("totalDeleted"), 0);
	}

	private XPath getXPath(String attribute) {
		return new XPath("//" + CSW_202_PREFIX + ":TransactionResponse/" + CSW_202_PREFIX + ":TransactionSummary/"
				+ CSW_202_PREFIX + ":" + attribute, nsContext);
	}

	public void close() throws IOException {
		response.close();
	}

}
