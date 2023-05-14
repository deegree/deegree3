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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.services.wmts.controller.capabilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.metadata.OperationsMetadata;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.domain.Domain;
import org.deegree.commons.ows.metadata.operation.DCP;
import org.deegree.commons.ows.metadata.operation.Operation;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.ows.capabilities.OWSCapabilitiesXMLAdapter;

/**
 * Responsible for writing out capabilities metadata.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */

class WmtsCapabilitiesMetadataWriter extends OWSCapabilitiesXMLAdapter {

	private XMLStreamWriter writer;

	private ServiceIdentification identification;

	WmtsCapabilitiesMetadataWriter(XMLStreamWriter writer, ServiceIdentification identification) {
		this.writer = writer;
		this.identification = identification;
	}

	void exportServiceIdentification() throws XMLStreamException {
		writer.writeStartElement(OWS110_NS, "ServiceIdentification");
		if (identification == null) {
			writeElement(writer, OWS110_NS, "Title", "deegree 3 WMTS");
			writeElement(writer, OWS110_NS, "Abstract", "deegree 3 WMTS implementation");
		}
		else {
			LanguageString title = identification.getTitle(null);
			writeElement(writer, OWS110_NS, "Title", title == null ? "deegree 3 WMTS" : title.getString());
			LanguageString _abstract = identification.getAbstract(null);
			writeElement(writer, OWS110_NS, "Abstract",
					_abstract == null ? "deegree 3 WMTS implementation" : _abstract.getString());
		}
		writeElement(writer, OWS110_NS, "ServiceType", "WMTS");
		writeElement(writer, OWS110_NS, "ServiceTypeVersion", "1.0.0");
		writer.writeEndElement();
	}

	void exportOperationsMetadata() throws XMLStreamException {

		List<Operation> operations = new LinkedList<Operation>();

		List<DCP> dcps = null;
		try {
			DCP dcp = new DCP(new URL(OGCFrontController.getHttpGetURL()), null);
			dcps = Collections.singletonList(dcp);
		}
		catch (MalformedURLException e) {
			// should never happen
		}

		List<Domain> params = new ArrayList<Domain>();
		List<Domain> constraints = new ArrayList<Domain>();
		constraints.add(new Domain("GetEncoding", Collections.singletonList("KVP")));
		List<OMElement> mdEls = new ArrayList<OMElement>();

		operations.add(new Operation("GetCapabilities", dcps, params, constraints, mdEls));
		operations.add(new Operation("GetTile", dcps, params, constraints, mdEls));
		operations.add(new Operation("GetFeatureInfo", dcps, params, constraints, mdEls));

		OperationsMetadata operationsMd = new OperationsMetadata(operations, params, constraints, null);

		exportOperationsMetadata110(writer, operationsMd);
	}

}
