/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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
package org.deegree.services.wms.controller.capabilities.theme;

import static org.deegree.commons.utils.StringUtils.replaceAll;
import static org.deegree.theme.Themes.getAllLayers;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.metadata.DatasetMetadata;
import org.deegree.commons.ows.metadata.Description;
import org.deegree.commons.ows.metadata.ExtendedDescription;
import org.deegree.commons.ows.metadata.MetadataUrl;
import org.deegree.commons.ows.metadata.layer.Attribution;
import org.deegree.commons.ows.metadata.layer.ExternalIdentifier;
import org.deegree.commons.ows.metadata.layer.UrlWithFormat;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.Pair;
import org.deegree.layer.Layer;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.theme.Theme;

/**
 * Creates {@link DatasetMetadata} objects from {@link LayerMetadata}.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.3
 */
class DatasetMetadataFactory {

	DatasetMetadata buildDatasetMetadata(final LayerMetadata layerMetadata, final Theme theme,
			final String mdUrlTemplate) {
		String localName = layerMetadata.getName();
		if (localName == null) {
			localName = "unnamed";
		}
		final QName name = new QName(localName);
		final List<LanguageString> titles = new ArrayList<LanguageString>();
		final List<LanguageString> abstracts = new ArrayList<LanguageString>();
		final List<Pair<List<LanguageString>, CodeType>> keywords = new ArrayList<Pair<List<LanguageString>, CodeType>>();
		final String metadataSetId = getFirstMetadataSetId(theme);
		final String metadataSetUrl = getUrlForMetadataSetId(metadataSetId, mdUrlTemplate);
		final Description description = layerMetadata.getDescription();
		if (description != null) {
			if (description.getTitles() != null) {
				titles.addAll(description.getTitles());
			}
			if (description.getAbstracts() != null) {
				abstracts.addAll(description.getAbstracts());
			}
			if (description.getKeywords() != null) {
				keywords.addAll(description.getKeywords());
			}
		}
		final List<MetadataUrl> metadataUrls = new ArrayList<MetadataUrl>();
		if (metadataSetUrl != null) {
			metadataUrls.add(new MetadataUrl(metadataSetUrl, null, null));
		}
		final List<ExternalIdentifier> externalIds = new ArrayList<ExternalIdentifier>();
		if (metadataSetId != null) {
			externalIds.add(new ExternalIdentifier(metadataSetId, null));
		}
		final List<UrlWithFormat> dataUrls = null;
		final List<UrlWithFormat> featureListUrls = null;
		final Attribution attribution = null;
		final List<ExtendedDescription> extendedDescriptions = null;
		return new DatasetMetadata(name, titles, abstracts, keywords, metadataUrls, externalIds, dataUrls,
				featureListUrls, attribution, extendedDescriptions);
	}

	private String getFirstMetadataSetId(final Theme theme) {
		if (theme.getLayerMetadata().getMetadataId() != null) {
			return theme.getLayerMetadata().getMetadataId();
		}
		for (final Layer layer : getAllLayers(theme)) {
			if (layer.getMetadata().getMetadataId() != null) {
				return layer.getMetadata().getMetadataId();
			}
		}
		return null;
	}

	private String getUrlForMetadataSetId(final String id, final String mdUrlTemplate) {
		if (id == null || mdUrlTemplate == null) {
			return null;
		}
		return replaceAll(mdUrlTemplate, "${metadataSetId}", id);
	}

}
