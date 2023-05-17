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
package org.deegree.services.metadata;

import static org.deegree.commons.metadata.MetadataJAXBConverter.LANG_MAPPER;
import static org.deegree.commons.utils.CollectionUtils.map;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.commons.ows.metadata.party.Address;
import org.deegree.commons.ows.metadata.party.ContactInfo;
import org.deegree.commons.ows.metadata.party.ResponsibleParty;
import org.deegree.commons.ows.metadata.party.Telephone;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.CollectionUtils.Mapper;
import org.deegree.commons.utils.Pair;
import org.deegree.services.jaxb.metadata.AddressType;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.metadata.KeywordsType;
import org.deegree.services.jaxb.metadata.LanguageStringType;
import org.deegree.services.jaxb.metadata.ServiceContactType;
import org.deegree.services.jaxb.metadata.ServiceIdentificationType;
import org.deegree.services.jaxb.metadata.ServiceProviderType;

/**
 * Class with utilities to work/convert various metadata 'formats', currently to
 * protocol.ows.metadata from the metadata jaxb beans.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class MetadataUtils {

	static final Mapper<LanguageString, LanguageStringType> LANG_LANG_MAPPER = new Mapper<LanguageString, LanguageStringType>() {
		@Override
		public LanguageString apply(LanguageStringType u) {
			return new LanguageString(u.getValue(), u.getLang());
		}
	};

	static final Mapper<CodeType, org.deegree.services.jaxb.metadata.CodeType> CODE_TYPE_MAPPER = new Mapper<CodeType, org.deegree.services.jaxb.metadata.CodeType>() {
		@Override
		public CodeType apply(org.deegree.services.jaxb.metadata.CodeType u) {
			return new CodeType(u.getValue(), u.getCodeSpace());
		}
	};

	static final Mapper<Pair<List<LanguageString>, CodeType>, KeywordsType> KW_MAPPER = new Mapper<Pair<List<LanguageString>, CodeType>, KeywordsType>() {
		@Override
		public Pair<List<LanguageString>, CodeType> apply(KeywordsType u) {
			Pair<List<LanguageString>, CodeType> p = new Pair<List<LanguageString>, CodeType>();
			p.first = map(u.getKeyword(), LANG_LANG_MAPPER);
			if (u.getType() != null) {
				p.second = CODE_TYPE_MAPPER.apply(u.getType());
			}
			return p;
		}
	};

	/**
	 * @param si
	 * @return null, if si is null
	 */
	public static ServiceIdentification convertFromJAXB(ServiceIdentificationType si) {
		if (si == null) {
			return null;
		}
		List<Pair<List<LanguageString>, CodeType>> keywords = null;
		if (si.getKeywords() != null) {
			keywords = map(si.getKeywords(), KW_MAPPER);
		}
		List<LanguageString> titles = map(si.getTitle(), LANG_MAPPER);
		List<LanguageString> abstracts = map(si.getAbstract(), LANG_MAPPER);
		String fees = si.getFees();
		List<String> accessConstraints = si.getAccessConstraints();
		return new ServiceIdentification(null, titles, abstracts, keywords, null, null, null, fees, accessConstraints);
	}

	public static Address convertFromJAXB(AddressType ad) {
		if (ad == null) {
			return null;
		}
		Address address = new Address();
		address.setAdministrativeArea(ad.getAdministrativeArea());
		address.setCity(ad.getCity());
		address.setCountry(ad.getCountry());
		address.setPostalCode(ad.getPostalCode());
		address.setDeliveryPoint(ad.getDeliveryPoint());
		return address;
	}

	public static ResponsibleParty convertFromJAXB(ServiceContactType sc) {
		if (sc == null) {
			return null;
		}
		ResponsibleParty res = new ResponsibleParty();
		res.setIndividualName(sc.getIndividualName());
		res.setPositionName(sc.getPositionName());
		res.setRole(new CodeType(sc.getRole()));
		ContactInfo info = new ContactInfo();
		info.setContactInstructions(sc.getContactInstructions());
		info.setHoursOfService(sc.getHoursOfService());
		try {
			info.setOnlineResource(new URL(sc.getOnlineResource()));
		}
		catch (MalformedURLException e) {
			// ignore this, schemas should be fixed so it already is an URL
		}
		Telephone phone = new Telephone();
		phone.setFacsimile(Collections.singletonList(sc.getFacsimile()));
		phone.setVoice(Collections.singletonList(sc.getPhone()));
		info.setPhone(phone);
		Address ad = convertFromJAXB(sc.getAddress());
		if (ad != null) {
			ad.setElectronicMailAddress(sc.getElectronicMailAddress());
			info.setAddress(ad);
		}
		res.setContactInfo(info);
		return res;
	}

	/**
	 * @param sp
	 * @return null, if sp is null
	 */
	public static ServiceProvider convertFromJAXB(ServiceProviderType sp) {
		if (sp == null) {
			return null;
		}
		return new ServiceProvider(sp.getProviderName(), sp.getProviderSite(), convertFromJAXB(sp.getServiceContact()));
	}

	public static Pair<ServiceIdentification, ServiceProvider> convertFromJAXB(DeegreeServicesMetadataType md) {
		ServiceIdentification si = convertFromJAXB(md.getServiceIdentification());
		ServiceProvider sp = convertFromJAXB(md.getServiceProvider());
		return new Pair<ServiceIdentification, ServiceProvider>(si, sp);
	}

}
