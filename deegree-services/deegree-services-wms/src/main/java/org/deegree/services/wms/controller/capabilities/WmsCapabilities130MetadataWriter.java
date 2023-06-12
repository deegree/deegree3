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

import static org.deegree.commons.xml.CommonNamespaces.SLDNS;
import static org.deegree.commons.xml.CommonNamespaces.WMSNS;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.XMLAdapter.maybeWriteElementNS;
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
import org.deegree.protocol.wms.WMSConstants.WMSRequestType;
import org.deegree.services.jaxb.wms.DeegreeWMS;
import org.deegree.services.wms.controller.WMSController;

/**
 * Used to write out WMS 1.3.0 capabilities metadata parts.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class WmsCapabilities130MetadataWriter {

	private ServiceIdentification identification;

	private ServiceProvider provider;

	private String getUrl;

	private String postUrl;

	private WMSController controller;

	WmsCapabilities130MetadataWriter(ServiceIdentification identification, ServiceProvider provider, String getUrl,
			String postUrl, WMSController controller) {
		this.identification = identification;
		this.provider = provider;
		this.getUrl = getUrl;
		this.postUrl = postUrl;
		this.controller = controller;
	}

	private void writeDCP(XMLStreamWriter writer, boolean get, boolean post) throws XMLStreamException {
		writer.writeStartElement(WMSNS, "DCPType");
		writer.writeStartElement(WMSNS, "HTTP");
		if (get) {
			writer.writeStartElement(WMSNS, "Get");
			writer.writeStartElement(WMSNS, "OnlineResource");
			writer.writeAttribute(XLNNS, "type", "simple");
			writer.writeAttribute(XLNNS, "href", getUrl + "?");
			writer.writeEndElement();
			writer.writeEndElement();
		}
		if (post) {
			writer.writeStartElement(WMSNS, "Post");
			writer.writeStartElement(WMSNS, "OnlineResource");
			writer.writeAttribute(XLNNS, "type", "simple");
			writer.writeAttribute(XLNNS, "href", postUrl);
			writer.writeEndElement();
			writer.writeEndElement();
		}
		writer.writeEndElement();
		writer.writeEndElement();
	}

	void writeRequest(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(WMSNS, "Request");

		writer.writeStartElement(WMSNS, "GetCapabilities");
		writeCapabilitiesFormats(writer);
		writeDCP(writer, isGetSupported(GetCapabilities), isPostSupported(GetCapabilities));
		writer.writeEndElement();

		writer.writeStartElement(WMSNS, "GetMap");
		writeImageFormats(writer);
		writeDCP(writer, isGetSupported(GetMap), isPostSupported(GetMap));
		writer.writeEndElement();

		writer.writeStartElement(WMSNS, "GetFeatureInfo");
		writeInfoFormats(writer);
		writeDCP(writer, isGetSupported(GetFeatureInfo), isPostSupported(GetFeatureInfo));
		writer.writeEndElement();

		writer.writeStartElement(SLDNS, "GetLegendGraphic");
		writeImageFormats(writer);
		writeDCP(writer, isGetSupported(GetLegendGraphic), false);
		writer.writeEndElement();

		writer.writeEndElement();
	}

	private void writeCapabilitiesFormats(XMLStreamWriter writer) throws XMLStreamException {
		for (String f : controller.getCapabilitiesManager().getSupportedFormats()) {
			writeElement(writer, WMSNS, "Format", f);
		}
	}

	private void writeImageFormats(XMLStreamWriter writer) throws XMLStreamException {
		for (String f : controller.getSupportedImageFormats()) {
			writeElement(writer, "Format", f);
		}
	}

	private void writeInfoFormats(XMLStreamWriter writer) throws XMLStreamException {
		for (String f : controller.getFeatureInfoManager().getSupportedFormats()) {
			writeElement(writer, "Format", f);
		}
	}

	void writeService(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(WMSNS, "Service");

		writeElement(writer, WMSNS, "Name", "WMS");

		List<LanguageString> titles = identification == null ? null : identification.getTitles();
		String title = (titles != null && !titles.isEmpty()) ? titles.get(0).getString() : "deegree 3 WMS";
		writeElement(writer, WMSNS, "Title", title);

		List<LanguageString> abstracts = identification == null ? null : identification.getAbstracts();
		if (abstracts != null && !abstracts.isEmpty()) {
			writeElement(writer, WMSNS, "Abstract", abstracts.get(0).getString());
		}

		writeKeywords(writer);

		writer.writeStartElement(WMSNS, "OnlineResource");
		writer.writeAttribute(XLNNS, "type", "simple");
		writer.writeAttribute(XLNNS, "href", getServiceUrl());
		writer.writeEndElement();

		writeServiceProvider(writer);

		final DeegreeWMS config = controller.getConfig();
		maybeWriteElementNS(writer, WMSNS, "LayerLimit", config.getLayerLimit());
		maybeWriteElementNS(writer, WMSNS, "MaxWidth", config.getMaxWidth());
		maybeWriteElementNS(writer, WMSNS, "MaxHeight", config.getMaxHeight());

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
			writer.writeStartElement(WMSNS, "KeywordList");

			for (Pair<List<LanguageString>, CodeType> key : keywords) {
				CodeType type = key.second;
				for (LanguageString lanString : key.first) {
					writer.writeStartElement(WMSNS, "Keyword");
					if (type != null) {
						writer.writeAttribute("vocabulary", type.getCodeSpace());
					}
					writer.writeCharacters(lanString.getString());
					writer.writeEndElement();
				}
			}

			writer.writeEndElement();
		}
	}

	private void writeServiceProvider(XMLStreamWriter writer) throws XMLStreamException {
		if (provider != null) {
			ResponsibleParty contact = provider.getServiceContact();
			if (contact != null) {
				writer.writeStartElement(WMSNS, "ContactInformation");

				if (contact.getIndividualName() != null) {
					writer.writeStartElement(WMSNS, "ContactPersonPrimary");
					writeElement(writer, WMSNS, "ContactPerson", contact.getIndividualName());
					writeElement(writer, WMSNS, "ContactOrganization", provider.getProviderName());
					writer.writeEndElement();
				}

				maybeWriteElementNS(writer, WMSNS, "ContactPosition", contact.getPositionName());
				final Address addr = contact.getContactInfo().getAddress();
				if (addr != null && addr.isPhysicalInfoAvailable()) {
					writer.writeStartElement(WMSNS, "ContactAddress");
					writeElement(writer, WMSNS, "AddressType", "postal");
					for (String s : addr.getDeliveryPoint()) {
						maybeWriteElementNS(writer, WMSNS, "Address", s);
					}
					writeElement(writer, WMSNS, "City", addr.getCity());
					writeElement(writer, WMSNS, "StateOrProvince", addr.getAdministrativeArea());
					writeElement(writer, WMSNS, "PostCode", addr.getPostalCode());
					writeElement(writer, WMSNS, "Country", addr.getCountry());
					writer.writeEndElement();
				}

				maybeWriteElementNS(writer, WMSNS, "ContactVoiceTelephone",
						contact.getContactInfo().getPhone().getVoice().get(0));
				maybeWriteElementNS(writer, WMSNS, "ContactFacsimileTelephone",
						contact.getContactInfo().getPhone().getFacsimile().get(0));
				if (addr != null && !addr.getElectronicMailAddress().isEmpty()) {
					maybeWriteElementNS(writer, WMSNS, "ContactElectronicMailAddress",
							addr.getElectronicMailAddress().get(0));
				}

				writer.writeEndElement();
			}

			writeServiceIdentificationParts(writer);

		}
	}

	private void writeServiceIdentificationParts(XMLStreamWriter writer) throws XMLStreamException {
		if (identification != null) {
			maybeWriteElementNS(writer, WMSNS, "Fees", identification.getFees());
			List<String> constr = identification.getAccessConstraints();
			if (constr != null) {
				for (String cons : constr) {
					maybeWriteElementNS(writer, WMSNS, "AccessConstraints", cons);
				}
			}
		}
		else {
			writeElement(writer, WMSNS, "Fees", "none");
			writeElement(writer, WMSNS, "AccessConstraints", "none");
		}
	}

	private boolean isGetSupported(WMSRequestType requestType) {
		return controller.getSupportedEncodings().isEncodingSupported(requestType, "KVP");
	}

	private boolean isPostSupported(WMSRequestType requestType) {
		return controller.getSupportedEncodings().isEncodingSupported(requestType, "XML");
	}

}
