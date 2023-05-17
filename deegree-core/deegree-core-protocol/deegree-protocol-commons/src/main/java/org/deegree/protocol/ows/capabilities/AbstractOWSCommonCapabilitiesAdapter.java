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
package org.deegree.protocol.ows.capabilities;

import static org.deegree.commons.ows.metadata.domain.RangeClosure.CLOSED;
import static org.deegree.commons.ows.metadata.domain.RangeClosure.CLOSED_OPEN;
import static org.deegree.commons.ows.metadata.domain.RangeClosure.OPEN;
import static org.deegree.commons.ows.metadata.domain.RangeClosure.OPEN_CLOSED;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.metadata.Description;
import org.deegree.commons.ows.metadata.OperationsMetadata;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.commons.ows.metadata.domain.AllowedValues;
import org.deegree.commons.ows.metadata.domain.AnyValue;
import org.deegree.commons.ows.metadata.domain.Domain;
import org.deegree.commons.ows.metadata.domain.NoValues;
import org.deegree.commons.ows.metadata.domain.PossibleValues;
import org.deegree.commons.ows.metadata.domain.Range;
import org.deegree.commons.ows.metadata.domain.RangeClosure;
import org.deegree.commons.ows.metadata.domain.Value;
import org.deegree.commons.ows.metadata.domain.Values;
import org.deegree.commons.ows.metadata.domain.ValuesReference;
import org.deegree.commons.ows.metadata.operation.DCP;
import org.deegree.commons.ows.metadata.party.Address;
import org.deegree.commons.ows.metadata.party.ContactInfo;
import org.deegree.commons.ows.metadata.party.ResponsibleParty;
import org.deegree.commons.ows.metadata.party.Telephone;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for {@link OWSCapabilitiesAdapter} implementations that process
 * <a href="http://www.opengeospatial.org/standards/common">OWS Common</a>-based
 * capabilities documents.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
abstract class AbstractOWSCommonCapabilitiesAdapter extends XMLAdapter implements OWSCapabilitiesAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractOWSCommonCapabilitiesAdapter.class);

	protected static final String XML1998NS = "http://www.w3.org/XML/1998/namespace";

	protected final NamespaceBindings nsContext = new NamespaceBindings();

	protected AbstractOWSCommonCapabilitiesAdapter(String owsNs) {
		nsContext.addNamespace("ows", owsNs);
		nsContext.addNamespace("xlink", XLNNS);
	}

	/**
	 * @return a {@link OWSCapabilities} instance, never <code>null</code>
	 */
	public OWSCapabilities parseMetadata() {

		Version version = getNodeAsVersion(rootElement, new XPath("@version", nsContext), null);
		String sequence = getNodeAsString(rootElement, new XPath("@updateSequence", nsContext), null);

		ServiceIdentification serviceId = parseServiceIdentification();
		ServiceProvider serviceProvider = parseServiceProvider();
		OperationsMetadata opMetadata = parseOperationsMetadata();
		List<String> languages = parseLanguages();

		return new OWSCapabilities(version, sequence, serviceId, serviceProvider, opMetadata, languages);
	}

	@Override
	public ServiceIdentification parseServiceIdentification() {

		OMElement serviceIdEl = getElement(getRootElement(), new XPath("ows:ServiceIdentification", nsContext));
		if (serviceIdEl == null) {
			return null;
		}

		Description description = parseDescription(serviceIdEl);

		OMElement serviceTypeEl = getElement(serviceIdEl, new XPath("ows:ServiceType", nsContext));
		CodeType serviceType = parseCodeSpace(serviceTypeEl);

		XPath xpath = new XPath("ows:ServiceTypeVersion", nsContext);
		List<OMElement> serviceTypeVersionEls = getElements(serviceIdEl, xpath);
		List<Version> serviceTypeVersions = null;
		if (serviceTypeVersionEls != null) {
			serviceTypeVersions = new ArrayList<Version>(serviceTypeVersionEls.size());
			for (OMElement serviceTypeVersionEl : serviceTypeVersionEls) {
				Version version = getNodeAsVersion(serviceTypeVersionEl, new XPath(".", nsContext), null);
				serviceTypeVersions.add(version);
			}
		}

		String[] profilesArray = getNodesAsStrings(serviceIdEl, new XPath("ows:Profiles", nsContext));
		List<String> profiles = new ArrayList<String>(profilesArray.length);
		for (int i = 0; i < profilesArray.length; i++) {
			profiles.add(profilesArray[i]);
		}

		String fees = getNodeAsString(serviceIdEl, new XPath("ows:Fees", nsContext), null);

		String[] constraintsArray = getNodesAsStrings(serviceIdEl, new XPath("ows:AccessConstraints", nsContext));
		List<String> constraints = new ArrayList<String>(constraintsArray.length);
		for (String constraint : constraintsArray) {
			constraints.add(constraint);
		}

		return new ServiceIdentification(description.getName(), description.getTitles(), description.getAbstracts(),
				null, serviceType, serviceTypeVersions, profiles, fees, constraints);
	}

	/**
	 * @param serviceIdEl context {@link OMElement}
	 * @return an {@link Description} instance, never <code>null</code>
	 */
	protected Description parseDescription(OMElement serviceIdEl) {

		List<OMElement> titleEls = getElements(serviceIdEl, new XPath("ows:Title", nsContext));
		List<LanguageString> titles = new ArrayList<LanguageString>(titleEls.size());
		for (OMElement titleEl : titleEls) {
			String lang = titleEl.getAttributeValue(new QName(XML1998NS, "lang"));
			titles.add(new LanguageString(titleEl.getText(), lang));
		}

		List<OMElement> abstractEls = getElements(serviceIdEl, new XPath("ows:Abstract", nsContext));
		List<LanguageString> abstracts = new ArrayList<LanguageString>(abstractEls.size());
		for (OMElement abstractEl : abstractEls) {
			String lang = abstractEl.getAttributeValue(new QName(XML1998NS, "lang"));
			abstracts.add(new LanguageString(abstractEl.getText(), lang));
		}

		List<OMElement> keywordsEls = getElements(serviceIdEl, new XPath("ows:Keywords", nsContext));
		List<Pair<List<LanguageString>, CodeType>> keywords = new ArrayList<Pair<List<LanguageString>, CodeType>>(
				keywordsEls.size());
		for (OMElement keywordsEl : keywordsEls) {
			List<OMElement> keywordEls = getElements(keywordsEl, new XPath("ows:Keyword", nsContext));
			List<LanguageString> keywordLS = new ArrayList<LanguageString>();
			if (keywordEls != null) {
				for (OMElement keywordEl : keywordEls) {
					String lang = keywordEl.getAttributeValue(new QName(XML1998NS, "lang"));
					keywordLS.add(new LanguageString(keywordEl.getText(), lang));
				}
			}
			OMElement typeEl = getElement(keywordsEl, new XPath("ows:Type", nsContext));
			CodeType type = null;
			if (typeEl != null) {
				type = parseCodeSpace(typeEl);
			}
			keywords.add(new Pair<List<LanguageString>, CodeType>(keywordLS, type));
		}

		return new Description(null, titles, abstracts, keywords);
	}

	@Override
	public ServiceProvider parseServiceProvider() {

		OMElement serviceProviderEl = getElement(getRootElement(), new XPath("ows:ServiceProvider", nsContext));
		if (serviceProviderEl == null) {
			return null;
		}

		String providerName = getNodeAsString(serviceProviderEl, new XPath("ows:ProviderName", nsContext), null);
		String providerSite = getNodeAsString(serviceProviderEl, new XPath("ows:ProviderSite/@xlink:href", nsContext),
				null);

		OMElement serviceContactEl = getElement(serviceProviderEl, new XPath("ows:ServiceContact", nsContext));
		ResponsibleParty serviceContact = null;
		if (serviceContactEl != null) {
			serviceContact = parseServiceContact(serviceContactEl);
		}
		return new ServiceProvider(providerName, providerSite, serviceContact);
	}

	/**
	 * @param serviceContactEl context {@link OMElement}
	 * @return an {@link ResponsibleParty} instance, never <code>null</code>
	 */
	protected ResponsibleParty parseServiceContact(OMElement serviceContactEl) {
		ResponsibleParty serviceContact = new ResponsibleParty();

		XPath xpath = new XPath("ows:IndividualName", nsContext);
		serviceContact.setIndividualName(getNodeAsString(serviceContactEl, xpath, null));

		xpath = new XPath("ows:PositionName", nsContext);
		serviceContact.setPositionName(getNodeAsString(serviceContactEl, xpath, null));

		xpath = new XPath("ows:ContactInfo", nsContext);
		ContactInfo contactInfo = parseContactInfo(getElement(serviceContactEl, xpath));
		serviceContact.setContactInfo(contactInfo);

		xpath = new XPath("ows:Role", nsContext);
		OMElement roleEl = getElement(serviceContactEl, xpath);
		if (roleEl != null) {
			serviceContact.setRole(parseCodeSpace(roleEl));
		}
		return serviceContact;
	}

	/**
	 * @param contactInfoEl context {@link OMElement}
	 * @return an {@link ContactInfo} instance, never <code>null</code>
	 */
	protected ContactInfo parseContactInfo(OMElement contactInfoEl) {
		ContactInfo contactInfo = new ContactInfo();

		XPath xpath = new XPath("ows:Phone", nsContext);
		Telephone phone = parsePhone(getElement(contactInfoEl, xpath));
		contactInfo.setPhone(phone);

		xpath = new XPath("ows:Address", nsContext);
		OMElement addressEl = getElement(contactInfoEl, xpath);
		contactInfo.setAddress(parseAddress(addressEl));

		xpath = new XPath("ows:OnlineResource/@xlink:href", nsContext);
		contactInfo.setOnlineResource(getNodeAsURL(contactInfoEl, xpath, null));

		xpath = new XPath("ows:HoursOfService", nsContext);
		contactInfo.setHoursOfService(getNodeAsString(contactInfoEl, xpath, null));

		xpath = new XPath("ows:ContactInstructions", nsContext);
		contactInfo.setContactInstructions(getNodeAsString(contactInfoEl, xpath, null));

		return contactInfo;
	}

	/**
	 * @param phoneEl context {@link OMElement}
	 * @return an {@link Telephone} instance, never <code>null</code>
	 */
	protected Telephone parsePhone(OMElement phoneEl) {
		Telephone phone = new Telephone();

		XPath xpath = new XPath("ows:Voice", nsContext);
		String[] voices = getNodesAsStrings(phoneEl, xpath);
		for (int i = 0; i < voices.length; i++) {
			phone.getVoice().add(voices[i]);
		}

		xpath = new XPath("ows:Facsimile", nsContext);
		String[] faxes = getNodesAsStrings(phoneEl, xpath);
		for (int i = 0; i < faxes.length; i++) {
			phone.getFacsimile().add(faxes[i]);
		}

		return phone;
	}

	/**
	 * @param addressEl context {@link OMElement}
	 * @return an {@link Address} instance, never <code>null</code>
	 */
	protected Address parseAddress(OMElement addressEl) {
		Address address = new Address();

		XPath xpath = new XPath("ows:DeliveryPoint", nsContext);
		String[] deliveryPoints = getNodesAsStrings(addressEl, xpath);
		for (String deliveryPoint : deliveryPoints) {
			address.getDeliveryPoint().add(deliveryPoint);
		}

		xpath = new XPath("ows:City", nsContext);
		address.setCity(getNodeAsString(addressEl, xpath, null));

		xpath = new XPath("ows:AdministrativeArea", nsContext);
		address.setAdministrativeArea(getNodeAsString(addressEl, xpath, null));

		xpath = new XPath("ows:PostalCode", nsContext);
		address.setPostalCode(getNodeAsString(addressEl, xpath, null));

		xpath = new XPath("ows:Country", nsContext);
		address.setCountry(getNodeAsString(addressEl, xpath, null));

		xpath = new XPath("ows:ElectronicMailAddress", nsContext);
		String[] eMails = getNodesAsStrings(addressEl, xpath);
		for (int i = 0; i < eMails.length; i++) {
			address.getElectronicMailAddress().add(eMails[i]);
		}

		return address;
	}

	/**
	 * @param omelement context {@link OMElement}
	 * @return an {@link CodeType} instance, never <code>null</code>
	 */
	protected CodeType parseCodeSpace(OMElement omelement) {
		String codeSpace = getNodeAsString(omelement, new XPath("codeSpace", nsContext), null);
		if (codeSpace != null) {
			return new CodeType(omelement.getText(), codeSpace);
		}
		return new CodeType(omelement.getText());
	}

	@Override
	public List<String> parseLanguages() {
		OMElement languagesEl = getElement(getRootElement(), new XPath("ows:Languages", nsContext));
		if (languagesEl == null) {
			return null;
		}
		String[] langs = getNodesAsStrings(languagesEl, new XPath("ows:Language", nsContext));
		List<String> languages = new ArrayList<String>();
		for (String language : langs) {
			languages.add(language);
		}
		return languages;
	}

	protected abstract Domain parseDomain(OMElement domainEl);

	protected PossibleValues parsePossibleValues(OMElement possibleValuesEl) {
		PossibleValues possibleValues = null;
		String name = possibleValuesEl.getLocalName();
		if ("AllowedValues".equals(name)) {
			List<Values> values = new ArrayList<Values>();
			@SuppressWarnings("unchecked")
			Iterator<OMElement> iter = possibleValuesEl.getChildElements();
			while (iter.hasNext()) {
				OMElement childEl = iter.next();
				String childName = childEl.getLocalName();
				if ("Range".equals(childName)) {
					values.add(parseRange(childEl));
				}
				else if ("Value".equals(childName)) {
					values.add(new Value(childEl.getText()));
				}
				else {
					LOG.warn("Unrecognized child element in 'ows:Range': '" + childEl.getQName() + "'.");
				}
			}
			possibleValues = new AllowedValues(values);
		}
		else if ("AnyValue".equals(name)) {
			possibleValues = new AnyValue();
		}
		else if ("NoValues".equals(name)) {
			possibleValues = new NoValues();
		}
		else if ("ValuesReference".equals(name)) {
			String valuesRefName = possibleValuesEl.getText();
			String valuesRefRef = getNodeAsString(possibleValuesEl, new XPath("@ows:reference", nsContext), null);
			possibleValues = new ValuesReference(valuesRefName, valuesRefRef);
		}
		else {
			throw new XMLParsingException(this, possibleValuesEl, "Element from 'ows:PossibleValues' group expected.");
		}
		return possibleValues;
	}

	/**
	 * Parses the given <code>ows:Range</code> element.
	 * <p>
	 * Verified for the following OWS Commons versions:
	 * <ul>
	 * <li>OWS 1.0.0</li>
	 * <li>OWS 1.1.0</li>
	 * <li>OWS 2.0</li>
	 * </ul>
	 * </p>
	 * @param rangeEl <code>ows:Range</code> element, must not be <code>null</code>
	 * @return corresponding object representation, never <code>null</code>
	 */
	public Range parseRange(OMElement rangeEl) {

		// <element ref="ows:MinimumValue" minOccurs="0"/>
		String min = getNodeAsString(rangeEl, new XPath("ows:MinimumValue", nsContext), null);

		// <element ref="ows:MaximumValue" minOccurs="0"/>
		String max = getNodeAsString(rangeEl, new XPath("ows:MaximumValue", nsContext), null);

		// <element ref="ows:Spacing" minOccurs="0">
		String spacing = getNodeAsString(rangeEl, new XPath("ows:Spacing", nsContext), null);

		// <attribute ref="ows:rangeClosure" use="optional">
		RangeClosure closure = null;
		String rangeClosureStr = getNodeAsString(rangeEl, new XPath("@ows:rangeClosure", nsContext), null);
		if (rangeClosureStr != null) {
			if ("closed".equals(rangeClosureStr)) {
				closure = CLOSED;
			}
			else if ("open".equals(rangeClosureStr)) {
				closure = OPEN;
			}
			else if ("open-closed".equals(rangeClosureStr)) {
				closure = OPEN_CLOSED;
			}
			else if ("closed-open".equals(rangeClosureStr)) {
				closure = CLOSED_OPEN;
			}
			else {
				LOG.warn("Unrecognized range closure: '" + rangeClosureStr + "'.");
			}
		}

		return new Range(min, max, spacing, closure);
	}

	/**
	 * @param dcpEl context {@link OMElement}
	 * @return an {@link DCP} instance, never <code>null</code>
	 */
	protected DCP parseDCP(OMElement dcpEl) {
		XPath xpath = new XPath("ows:HTTP/ows:Get", nsContext);

		List<OMElement> getEls = getElements(dcpEl, xpath);
		List<Pair<URL, List<Domain>>> getEndpoints = new ArrayList<Pair<URL, List<Domain>>>(getEls.size());
		if (getEls != null) {
			for (OMElement getEl : getEls) {
				xpath = new XPath("@xlink:href", nsContext);
				URL href = getNodeAsURL(getEl, xpath, null);

				xpath = new XPath("ows:Constraint", nsContext);
				List<OMElement> constaintEls = getElements(getEl, xpath);
				List<Domain> domains = new ArrayList<Domain>();
				for (OMElement constaintEl : constaintEls) {
					Domain constraint = parseDomain(constaintEl);
					domains.add(constraint);
				}

				getEndpoints.add(new Pair<URL, List<Domain>>(href, domains));
			}
		}

		xpath = new XPath("ows:HTTP/ows:Post", nsContext);
		List<OMElement> postEls = getElements(dcpEl, xpath);
		List<Pair<URL, List<Domain>>> postEndpoints = new ArrayList<Pair<URL, List<Domain>>>(postEls.size());
		if (postEls != null) {
			for (OMElement postEl : postEls) {
				xpath = new XPath("@xlink:href", nsContext);
				URL href = getNodeAsURL(postEl, xpath, null);

				xpath = new XPath("ows:Constraint", nsContext);
				List<OMElement> constaintEls = getElements(postEl, xpath);
				List<Domain> domains = new ArrayList<Domain>();
				for (OMElement constaintEl : constaintEls) {
					Domain constraint = parseDomain(constaintEl);
					domains.add(constraint);
				}

				postEndpoints.add(new Pair<URL, List<Domain>>(href, domains));
			}
		}

		return new DCP(getEndpoints, postEndpoints);
	}

}
