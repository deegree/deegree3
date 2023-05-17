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
package org.deegree.services.wcs;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;

/**
 * This class can generates ServiceExceptionReports v. 1.2.0, the format accepted by WCS
 * 1.0.0.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 *
 */
public class WCS100ServiceExceptionReportSerializer extends XMLExceptionSerializer {

	private static final String OGC_NS = "http://www.opengis.net/ogc";

	private static final String OGC_SCHEMA = "http://schemas.opengis.net/wcs/1.0.0/OGC-exception.xsd";

	@Override
	public void serializeExceptionToXML(XMLStreamWriter writer, OWSException ex) throws XMLStreamException {

		writer.writeStartElement(DEFAULT_NS_PREFIX, "ServiceExceptionReport", OGC_NS);
		writer.writeNamespace(DEFAULT_NS_PREFIX, OGC_NS);
		writer.writeNamespace("xsi", XSINS);
		writer.writeAttribute(XSINS, "schemaLocation", OGC_NS + " " + OGC_SCHEMA);
		writer.writeAttribute("version", "1.2.0");
		writer.writeStartElement(OGC_NS, "ServiceException");
		writer.writeAttribute("code", ex.getExceptionCode());
		if (ex.getLocator().length() > 0) {
			writer.writeAttribute("locator", ex.getLocator());
		}
		writer.writeCharacters(ex.getMessage() != null ? ex.getMessage() : "not available");
		writer.writeEndElement(); // ServiceException
		writer.writeEndElement(); // ServiceExceptionReport
	}

}
