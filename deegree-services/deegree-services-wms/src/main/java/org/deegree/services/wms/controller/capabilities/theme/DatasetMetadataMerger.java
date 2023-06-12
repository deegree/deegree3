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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.metadata.DatasetMetadata;
import org.deegree.commons.ows.metadata.ExtendedDescription;
import org.deegree.commons.ows.metadata.MetadataUrl;
import org.deegree.commons.ows.metadata.layer.Attribution;
import org.deegree.commons.ows.metadata.layer.ExternalIdentifier;
import org.deegree.commons.ows.metadata.layer.UrlWithFormat;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.Pair;

/**
 * Merges {@link DatasetMetadata} instances.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.3
 */
class DatasetMetadataMerger {

	/**
	 * Merges a list of {@link DatasetMetadata} instances.
	 * @param metadata list of metadata to merge, can be <code>null</code> or empty
	 * @return merged metadata, can be <code>null</code>
	 */
	DatasetMetadata merge(final List<DatasetMetadata> metadata) {
		if (metadata == null || metadata.isEmpty())
			return null;
		DatasetMetadata mergedMetadata = null;
		for (DatasetMetadata datasetMetadata : metadata) {
			if (mergedMetadata == null)
				mergedMetadata = datasetMetadata;
			else
				mergedMetadata = merge(mergedMetadata, datasetMetadata);
		}
		return mergedMetadata;
	}

	/**
	 * Merges two {@link DatasetMetadata} instances.
	 * @param providerMetadata metadata from provider (takes precedence), can be
	 * <code>null</code>
	 * @param layerMetadata metadata from layer, can be <code>null</code>
	 * @return merged metadata, can be <code>null</code>
	 */
	DatasetMetadata merge(final DatasetMetadata providerMetadata, final DatasetMetadata layerMetadata) {
		if (providerMetadata == null) {
			return layerMetadata;
		}
		else if (layerMetadata == null) {
			return providerMetadata;
		}
		final QName name = layerMetadata.getQName();
		final List<LanguageString> titles = merge(providerMetadata.getTitles(), layerMetadata.getTitles());
		final List<LanguageString> abstracts = merge(providerMetadata.getAbstracts(), layerMetadata.getAbstracts());
		final List<Pair<List<LanguageString>, CodeType>> keywords = new ArrayList<Pair<List<LanguageString>, CodeType>>();
		if (providerMetadata.getKeywords() != null) {
			keywords.addAll(providerMetadata.getKeywords());
		}
		if (layerMetadata.getKeywords() != null) {
			keywords.addAll(layerMetadata.getKeywords());
		}
		final List<MetadataUrl> metadataUrls = new ArrayList<MetadataUrl>();
		if (providerMetadata.getMetadataUrls() != null) {
			metadataUrls.addAll(providerMetadata.getMetadataUrls());
		}
		if (layerMetadata.getMetadataUrls() != null) {
			metadataUrls.addAll(layerMetadata.getMetadataUrls());
		}
		final List<ExternalIdentifier> externalIds = new ArrayList<ExternalIdentifier>();
		if (providerMetadata.getExternalIds() != null) {
			externalIds.addAll(providerMetadata.getExternalIds());
		}
		if (layerMetadata.getExternalIds() != null) {
			externalIds.addAll(layerMetadata.getExternalIds());
		}
		final List<UrlWithFormat> dataUrls = new ArrayList<UrlWithFormat>();
		if (providerMetadata.getDataUrls() != null) {
			dataUrls.addAll(providerMetadata.getDataUrls());
		}
		if (layerMetadata.getDataUrls() != null) {
			dataUrls.addAll(layerMetadata.getDataUrls());
		}
		final List<UrlWithFormat> featureListUrls = new ArrayList<UrlWithFormat>();
		if (providerMetadata.getFeatureListUrls() != null) {
			featureListUrls.addAll(providerMetadata.getFeatureListUrls());
		}
		if (layerMetadata.getDataUrls() != null) {
			featureListUrls.addAll(layerMetadata.getFeatureListUrls());
		}
		Attribution attribution = providerMetadata.getAttribution();
		if (attribution == null) {
			attribution = layerMetadata.getAttribution();
		}
		List<ExtendedDescription> extendedDescriptions = providerMetadata.getExtendedDescriptions();
		if (extendedDescriptions == null) {
			extendedDescriptions = layerMetadata.getExtendedDescriptions();
		}
		return new DatasetMetadata(name, titles, abstracts, keywords, metadataUrls, externalIds, dataUrls,
				featureListUrls, attribution, extendedDescriptions);
	}

	private List<LanguageString> merge(final List<LanguageString> first, final List<LanguageString> second) {
		final List<LanguageString> merged = new ArrayList<LanguageString>();
		if (first != null) {
			merged.addAll(first);
		}
		if (second != null) {
			merged.addAll(second);
		}
		return merged;
	}

}
