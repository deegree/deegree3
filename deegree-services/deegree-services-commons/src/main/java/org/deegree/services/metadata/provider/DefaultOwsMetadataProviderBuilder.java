/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.services.metadata.provider;

import static java.util.Collections.emptyList;
import static org.deegree.services.metadata.MetadataUtils.convertFromJAXB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.metadata.DatasetMetadata;
import org.deegree.commons.ows.metadata.ExtendedDescription;
import org.deegree.commons.ows.metadata.MetadataUrl;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.commons.ows.metadata.layer.Attribution;
import org.deegree.commons.ows.metadata.layer.ExternalIdentifier;
import org.deegree.commons.ows.metadata.layer.LogoUrl;
import org.deegree.commons.ows.metadata.layer.UrlWithFormat;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.StringUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.services.jaxb.metadata.DatasetMetadataType;
import org.deegree.services.jaxb.metadata.DatasetMetadataType.Attribution.LogoURL;
import org.deegree.services.jaxb.metadata.DatasetMetadataType.DataURL;
import org.deegree.services.jaxb.metadata.DatasetMetadataType.FeatureListURL;
import org.deegree.services.jaxb.metadata.DatasetMetadataType.MetadataURL;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.metadata.ExtendedCapabilitiesType;
import org.deegree.services.jaxb.metadata.ExtendedDescriptionType;
import org.deegree.services.jaxb.metadata.ExternalMetadataAuthorityType;
import org.deegree.services.jaxb.metadata.ExternalMetadataSetIdType;
import org.deegree.services.jaxb.metadata.KeywordsType;
import org.deegree.services.jaxb.metadata.LanguageStringType;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;
import org.w3c.dom.Element;

/**
 * This class is responsible for building web service metadata providers.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class DefaultOwsMetadataProviderBuilder implements ResourceBuilder<OWSMetadataProvider> {

	private final JAXBElement<DeegreeServicesMetadataType> md;

	private final ResourceMetadata<OWSMetadataProvider> metadata;

	public DefaultOwsMetadataProviderBuilder(JAXBElement<DeegreeServicesMetadataType> md,
			ResourceMetadata<OWSMetadataProvider> metadata) {
		this.md = md;
		this.metadata = metadata;
	}

	@Override
	public OWSMetadataProvider build() {
		try {
			Pair<ServiceIdentification, ServiceProvider> smd = convertFromJAXB(md.getValue());
			Map<String, List<OMElement>> extendedCapabilities = new HashMap<String, List<OMElement>>();
			if (md.getValue().getExtendedCapabilities() != null) {
				for (ExtendedCapabilitiesType ex : md.getValue().getExtendedCapabilities()) {
					String version = ex.getProtocolVersions();
					if (version == null) {
						version = "default";
					}
					List<OMElement> list = extendedCapabilities.get(version);
					if (list == null) {
						list = new ArrayList<OMElement>();
						extendedCapabilities.put(version, list);
					}
					DOMSource domSource = new DOMSource(ex.getAny());
					XMLStreamReader xmlStream;
					try {
						xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(domSource);
					}
					catch (Exception t) {
						throw new ResourceInitException("Error extracting extended capabilities: " + t.getMessage(), t);
					}
					list.add(new XMLAdapter(xmlStream).getRootElement());
				}
			}
			List<DatasetMetadata> datasets = fromJaxb(md.getValue().getDatasetMetadata());
			Map<String, String> authorities = new HashMap<String, String>();
			if (md.getValue().getDatasetMetadata() != null) {
				for (ExternalMetadataAuthorityType at : md.getValue()
					.getDatasetMetadata()
					.getExternalMetadataAuthority()) {
					authorities.put(at.getName(), at.getValue());
				}
			}
			return new DefaultOWSMetadataProvider(smd.first, smd.second, extendedCapabilities, datasets, authorities,
					metadata);
		}
		catch (Exception e) {
			throw new ResourceInitException("Unable to read service metadata config: " + e.getLocalizedMessage(), e);
		}
	}

	private List<DatasetMetadata> fromJaxb(
			org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType.DatasetMetadata jaxbDatasetMetadata) {
		List<DatasetMetadata> datasets = new ArrayList<DatasetMetadata>();
		if (jaxbDatasetMetadata != null) {
			for (DatasetMetadataType jaxbEl : jaxbDatasetMetadata.getDataset()) {
				datasets.add(fromJaxb(jaxbEl, jaxbDatasetMetadata.getMetadataUrlTemplate()));
			}
		}
		return datasets;
	}

	private DatasetMetadata fromJaxb(final DatasetMetadataType jaxbEl,
			final List<DeegreeServicesMetadataType.DatasetMetadata.MetadataUrlTemplate> metadataUrlTemplates) {
		final QName name = jaxbEl.getName();
		final List<LanguageString> titles = fromJaxb(jaxbEl.getTitle());
		final List<LanguageString> abstracts = fromJaxb(jaxbEl.getAbstract());
		final List<Pair<List<LanguageString>, CodeType>> keywords = fromJaxbKeywords(jaxbEl.getKeywords());
		final List<MetadataUrl> metadataUrls = new ArrayList<MetadataUrl>();
		if (metadataUrlTemplates != null) {
			for (DeegreeServicesMetadataType.DatasetMetadata.MetadataUrlTemplate metadataUrlTemplate : metadataUrlTemplates) {
				final String metadataUrl = buildMetadataUrl(metadataUrlTemplate, jaxbEl.getMetadataSetId());
				final String format = parseFormat(metadataUrlTemplate);
				if (metadataUrl != null) {
					metadataUrls.add(new MetadataUrl(metadataUrl, null, format));
				}
			}
		}
		if (jaxbEl.getMetadataURL() != null) {
			for (final MetadataURL jaxbMetadataUrl : jaxbEl.getMetadataURL()) {
				metadataUrls.add(new MetadataUrl(jaxbMetadataUrl.getValue(), jaxbMetadataUrl.getType(),
						jaxbMetadataUrl.getFormat()));
			}
		}
		final List<ExternalIdentifier> externalIds = new ArrayList<ExternalIdentifier>();
		for (ExternalMetadataSetIdType metadatsetIdType : jaxbEl.getExternalMetadataSetId()) {
			externalIds.add(fromJaxb(metadatsetIdType));
		}
		final List<UrlWithFormat> dataUrls = new ArrayList<UrlWithFormat>();
		for (final DataURL jaxbDataUrl : jaxbEl.getDataURL()) {
			dataUrls.add(new UrlWithFormat(jaxbDataUrl.getValue(), jaxbDataUrl.getFormat()));
		}
		final List<UrlWithFormat> featureListUrls = new ArrayList<UrlWithFormat>();
		for (final FeatureListURL jaxbFeatureListUrl : jaxbEl.getFeatureListURL()) {
			featureListUrls.add(new UrlWithFormat(jaxbFeatureListUrl.getValue(), jaxbFeatureListUrl.getFormat()));
		}
		final Attribution attribution = fromJaxb(jaxbEl.getAttribution());
		final List<ExtendedDescription> extendedDescriptions = new ArrayList<>();
		List<ExtendedDescriptionType> extendedDescriptionTypes = jaxbEl.getExtendedDescription();
		if (extendedDescriptionTypes != null) {
			extendedDescriptionTypes.stream().forEach(extendedDescriptionType -> {
				ExtendedDescription extendedDescription = new ExtendedDescription(extendedDescriptionType.getName(),
						extendedDescriptionType.getType(), extendedDescriptionType.getMetadata(),
						extendedDescriptionType.getValue());
				extendedDescriptions.add(extendedDescription);
			});
		}
		return new DatasetMetadata(name, titles, abstracts, keywords, metadataUrls, externalIds, dataUrls,
				featureListUrls, attribution, extendedDescriptions);
	}

	private String buildMetadataUrl(DeegreeServicesMetadataType.DatasetMetadata.MetadataUrlTemplate template,
			String datasetId) {
		if (template == null || template.getValue() == null || datasetId == null) {
			return null;
		}
		return StringUtils.replaceAll(template.getValue(), "${metadataSetId}", datasetId);
	}

	private String parseFormat(DeegreeServicesMetadataType.DatasetMetadata.MetadataUrlTemplate template) {
		if (template == null) {
			return null;
		}
		return template.getFormat();
	}

	private List<Pair<List<LanguageString>, CodeType>> fromJaxbKeywords(final List<KeywordsType> jaxbEls) {
		if (jaxbEls == null) {
			return emptyList();
		}
		final List<Pair<List<LanguageString>, CodeType>> keywords = new ArrayList<Pair<List<LanguageString>, CodeType>>();
		for (final KeywordsType jaxbEl : jaxbEls) {
			keywords.add(fromJaxb(jaxbEl));
		}
		return keywords;
	}

	private List<LanguageString> fromJaxb(List<LanguageStringType> strings) {
		List<LanguageString> languageStrings = new ArrayList<LanguageString>();
		if (strings != null) {
			for (LanguageStringType string : strings) {
				languageStrings.add(new LanguageString(string.getValue(), string.getLang()));
			}
		}
		return languageStrings;
	}

	private ExternalIdentifier fromJaxb(final ExternalMetadataSetIdType jaxbEl) {
		return new ExternalIdentifier(jaxbEl.getValue(), jaxbEl.getAuthority());
	}

	private Attribution fromJaxb(final org.deegree.services.jaxb.metadata.DatasetMetadataType.Attribution jaxbEl) {
		if (jaxbEl == null) {
			return null;
		}
		LogoUrl logoUrl = null;
		if (jaxbEl.getLogoURL() != null) {
			LogoURL jaxbLogoUrl = jaxbEl.getLogoURL();
			logoUrl = new LogoUrl(jaxbLogoUrl.getValue(), jaxbLogoUrl.getFormat(), jaxbLogoUrl.getWidth(),
					jaxbLogoUrl.getHeight());
		}
		return new Attribution(jaxbEl.getTitle(), jaxbEl.getURL(), logoUrl);
	}

	private Pair<List<LanguageString>, CodeType> fromJaxb(final KeywordsType jaxbEl) {
		final CodeType type = jaxbEl.getType() == null ? null
				: new CodeType(jaxbEl.getType().getValue(), jaxbEl.getType().getCodeSpace());
		final List<LanguageString> languageStrings = fromJaxb(jaxbEl.getKeyword());
		return new Pair<List<LanguageString>, CodeType>(languageStrings, type);
	}

}
