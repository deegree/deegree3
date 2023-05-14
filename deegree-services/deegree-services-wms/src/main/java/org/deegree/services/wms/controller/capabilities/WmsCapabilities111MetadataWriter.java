/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.wms.controller.capabilities;

import static org.deegree.commons.xml.CommonNamespaces.XLINK_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.XMLAdapter.maybeWriteElement;
import static org.deegree.commons.xml.XMLAdapter.writeElement;
import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.GetCapabilities;
import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.GetFeatureInfo;
import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.GetLegendGraphic;
import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.GetMap;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.commons.ows.metadata.party.Address;
import org.deegree.commons.ows.metadata.party.ResponsibleParty;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.Pair;
import org.deegree.protocol.wms.WMSConstants;
import org.deegree.services.wms.controller.WMSController;

/**
 * Used to write out metadata parts of WMS 1.1.1 capabilities.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class WmsCapabilities111MetadataWriter {

	private ServiceIdentification identification;

	private ServiceProvider provider;

	private String getUrl;

	private String postUrl;

	private WMSController controller;

	WmsCapabilities111MetadataWriter(ServiceIdentification identification, ServiceProvider provider, String getUrl,
			String postUrl, WMSController controller) {
		this.identification = identification;
		this.provider = provider;
		this.getUrl = getUrl;
		this.postUrl = postUrl;
		this.controller = controller;

	}

	void writeDCP(XMLStreamWriter writer, boolean get, boolean post) throws XMLStreamException {
		writer.writeStartElement("DCPType");
		writer.writeStartElement("HTTP");
		if (get) {
			writer.writeStartElement("Get");
			writer.writeStartElement("OnlineResource");
			writer.writeNamespace(XLINK_PREFIX, XLNNS);
			writer.writeAttribute(XLNNS, "type", "simple");
			writer.writeAttribute(XLNNS, "href", getUrl + "?");
			writer.writeEndElement();
			writer.writeEndElement();
		}
		if (post) {
			writer.writeStartElement("Post");
			writer.writeStartElement("OnlineResource");
			writer.writeNamespace(XLINK_PREFIX, XLNNS);
			writer.writeAttribute(XLNNS, "type", "simple");
			writer.writeAttribute(XLNNS, "href", postUrl);
			writer.writeEndElement();
			writer.writeEndElement();
		}
		writer.writeEndElement();
		writer.writeEndElement();
	}

	void writeRequest(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("Request");

		if (isGetSupported(GetCapabilities)) {
			writer.writeStartElement("GetCapabilities");
			writeElement(writer, "Format", "application/vnd.ogc.wms_xml");
			writeDCP(writer, true, false);
			writer.writeEndElement();
		}

		if (isGetSupported(GetMap)) {
			writer.writeStartElement("GetMap");
			writeImageFormats(writer);
			writeDCP(writer, true, false);
			writer.writeEndElement();
		}

		if (isGetSupported(GetFeatureInfo)) {
			writer.writeStartElement("GetFeatureInfo");
			writeInfoFormats(writer);
			writeDCP(writer, true, false);
			writer.writeEndElement();
		}

		if (isGetSupported(GetLegendGraphic)) {
			writer.writeStartElement("GetLegendGraphic");
			writeImageFormats(writer);
			writeDCP(writer, true, false);
			writer.writeEndElement();
		}

		writer.writeEndElement();
	}

	void writeImageFormats(XMLStreamWriter writer) throws XMLStreamException {
		for (String f : controller.getSupportedImageFormats()) {
			writeElement(writer, "Format", f);
		}
	}

	void writeInfoFormats(XMLStreamWriter writer) throws XMLStreamException {
		for (String f : controller.getFeatureInfoManager().getSupportedFormats()) {
			writeElement(writer, "Format", f);
		}
	}

	void writeService(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("Service");

		writeElement(writer, "Name", "OGC:WMS");

		List<LanguageString> titles = identification == null ? null : identification.getTitles();
		String title = (titles != null && !titles.isEmpty()) ? titles.get(0).getString() : "deegree 3 WMS";
		writeElement(writer, "Title", title);

		List<LanguageString> abstracts = identification == null ? null : identification.getAbstracts();
		if (abstracts != null && !abstracts.isEmpty()) {
			writeElement(writer, "Abstract", abstracts.get(0).getString());
		}

		writeKeywords(writer);

		writer.writeStartElement("OnlineResource");
		writer.writeNamespace(XLINK_PREFIX, XLNNS);
		writer.writeAttribute(XLNNS, "type", "simple");
		writer.writeAttribute(XLNNS, "href", getServiceUrl());
		writer.writeEndElement();

		writeServiceProvider(writer);

		writer.writeEndElement();
	}

	private String getServiceUrl() {
		String url = getUrl;
		if (provider != null && provider.getServiceContact() != null
				&& provider.getServiceContact().getContactInfo() != null
				&& provider.getServiceContact().getContactInfo().getOnlineResource() != null) {
			url = provider.getServiceContact().getContactInfo().getOnlineResource().toExternalForm();
		}
		return url;
	}

	private void writeKeywords(XMLStreamWriter writer) throws XMLStreamException {
		List<Pair<List<LanguageString>, CodeType>> keywords = identification == null ? null
				: identification.getKeywords();
		if (keywords != null && !keywords.isEmpty()) {
			writer.writeStartElement("KeywordList");

			for (Pair<List<LanguageString>, CodeType> key : keywords) {
				for (LanguageString lanString : key.first) {
					writeElement(writer, "Keyword", lanString.getString());
				}
			}

			writer.writeEndElement();
		}
	}

	private void writeServiceProvider(XMLStreamWriter writer) throws XMLStreamException {
		if (provider != null) {
			ResponsibleParty contact = provider.getServiceContact();
			if (contact != null) {
				writer.writeStartElement("ContactInformation");

				if (contact.getIndividualName() != null) {
					writer.writeStartElement("ContactPersonPrimary");
					writeElement(writer, "ContactPerson", contact.getIndividualName());
					writeElement(writer, "ContactOrganization", provider.getProviderName());
					writer.writeEndElement();
				}

				maybeWriteElement(writer, "ContactPosition", contact.getPositionName());
				final Address addr = contact.getContactInfo().getAddress();
				if (addr != null && addr.isPhysicalInfoAvailable()) {
					writer.writeStartElement("ContactAddress");
					writeElement(writer, "AddressType", "postal");
					for (String s : addr.getDeliveryPoint()) {
						maybeWriteElement(writer, "Address", s);
					}
					writeElement(writer, "City", addr.getCity());
					writeElement(writer, "StateOrProvince", addr.getAdministrativeArea());
					writeElement(writer, "PostCode", addr.getPostalCode());
					writeElement(writer, "Country", addr.getCountry());
					writer.writeEndElement();
				}

				maybeWriteElement(writer, "ContactVoiceTelephone",
						contact.getContactInfo().getPhone().getVoice().get(0));
				maybeWriteElement(writer, "ContactFacsimileTelephone",
						contact.getContactInfo().getPhone().getFacsimile().get(0));
				if (addr != null && !addr.getElectronicMailAddress().isEmpty()) {
					maybeWriteElement(writer, "ContactElectronicMailAddress", addr.getElectronicMailAddress().get(0));
				}

				writer.writeEndElement();
			}

			writeServiceIdentificationParts(writer);

		}
	}

	private void writeServiceIdentificationParts(XMLStreamWriter writer) throws XMLStreamException {
		if (identification != null) {
			maybeWriteElement(writer, "Fees", identification.getFees());
			List<String> constr = identification.getAccessConstraints();
			if (constr != null) {
				for (String cons : constr) {
					maybeWriteElement(writer, "AccessConstraints", cons);
				}
			}
		}
		else {
			writeElement(writer, "Fees", "none");
			writeElement(writer, "AccessConstraints", "none");
		}
	}

	private boolean isGetSupported(WMSConstants.WMSRequestType requestType) {
		return controller.getSupportedEncodings().isEncodingSupported(requestType, "KVP");
	}

}
