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

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.protocol.csw.CSWConstants;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class TransactionXMLEncoder {

	public static void exportInsert(List<OMElement> records, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartDocument();
		writer.writeStartElement(CSWConstants.CSW_202_PREFIX, "Transaction", CSW_202_NS);
		writer.writeAttribute("version", "2.0.2");
		writer.writeAttribute("service", "CSW");
		writer.writeNamespace(CSWConstants.CSW_202_PREFIX, CSW_202_NS);
		writer.writeNamespace(CommonNamespaces.XSI_PREFIX, CommonNamespaces.XSINS);
		writer.writeAttribute(CommonNamespaces.XSINS, "schemaLocation",
				CSW_202_NS + " " + CSWConstants.CSW_202_PUBLICATION_SCHEMA);
		writer.writeStartElement(CSWConstants.CSW_202_PREFIX, "Insert", CSW_202_NS);

		for (OMElement record : records) {
			record.serialize(writer);
		}
	}

}
