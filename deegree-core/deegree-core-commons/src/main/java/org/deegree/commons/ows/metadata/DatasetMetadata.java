/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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
package org.deegree.commons.ows.metadata;

import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.ows.metadata.layer.Attribution;
import org.deegree.commons.ows.metadata.layer.ExternalIdentifier;
import org.deegree.commons.ows.metadata.layer.UrlWithFormat;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.Pair;

/**
 * Encapsulates metadata on a dataset (layer, feature type, etc.) served by an OGC web
 * service (as reported in the capabilities document).
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
public class DatasetMetadata extends Description {

	private final QName name;

	private final List<MetadataUrl> metadataUrls;

	private final List<ExternalIdentifier> externalIds;

	private final List<UrlWithFormat> dataUrls;

	private final List<UrlWithFormat> featureListUrls;

	private final Attribution attribution;

	private final List<ExtendedDescription> extendedDescriptions;

	/**
	 * Creates a new {@link DatasetMetadata} instance.
	 * @param name name, must not be <code>null</code>
	 * @param titles titles, may be <code>null</code> (no titles)
	 * @param abstracts abstracts, may be <code>null</code> (no titles)
	 * @param keywords keywords, may be <code>null</code> (no keywords)
	 * @param metadataUrls urls of metadata records, may be <code>null</code> (no metadata
	 * records)
	 * @param externalIds external identifiers, may be <code>null</code> (no external
	 * identifiers)
	 * @param dataUrls links to the underlying data (of a layer), may be <code>null</code>
	 * (no links)
	 * @param featureListUrls links to the list of the features (used in a layer), may be
	 * <code>null</code> (no links)
	 * @param attribution indicates the provider of a layer, may be <code>null</code> (no
	 * attribution)
	 * @param extendedDescriptions extended descriptions of a layer, may be
	 * <code>null</code> (no extendedDescriptions)
	 */
	public DatasetMetadata(final QName name, final List<LanguageString> titles, final List<LanguageString> abstracts,
			final List<Pair<List<LanguageString>, CodeType>> keywords, final List<MetadataUrl> metadataUrls,
			final List<ExternalIdentifier> externalIds, final List<UrlWithFormat> dataUrls,
			final List<UrlWithFormat> featureListUrls, final Attribution attribution,
			final List<ExtendedDescription> extendedDescriptions) {
		super(name.getLocalPart(), titles, abstracts, keywords);
		this.name = name;
		this.metadataUrls = metadataUrls;
		this.externalIds = externalIds;
		this.dataUrls = dataUrls;
		this.featureListUrls = featureListUrls;
		this.attribution = attribution;
		this.extendedDescriptions = extendedDescriptions;
	}

	/**
	 * Returns the qualified name.
	 * @return qualified name of the dataset, never <code>null</code>
	 */
	public QName getQName() {
		return name;
	}

	/**
	 * Returns the URLs of metadata records.
	 * @return urls of metadata records, may be <code>null</code> (no metadata records)
	 */
	public List<MetadataUrl> getMetadataUrls() {
		return metadataUrls;
	}

	/**
	 * Returns the external identifiers.
	 * @return external identifiers, may be <code>null</code> (no external identifiers)
	 */
	public List<ExternalIdentifier> getExternalIds() {
		return externalIds;
	}

	/**
	 * Returns the links to the underlying data (of a layer).
	 * @return links to the underlying data (of a layer), may be <code>null</code> (no
	 * links)
	 */
	public List<UrlWithFormat> getDataUrls() {
		return dataUrls;
	}

	/**
	 * Returns the links to the list of the features (used in a layer).
	 * @return links to the list of the features (used in a layer), may be
	 * <code>null</code> (no links)
	 */
	public List<UrlWithFormat> getFeatureListUrls() {
		return featureListUrls;
	}

	/**
	 * Returns information on the provider (of a layer).
	 * @return information on the provider, may be <code>null</code> (no information)
	 */
	public Attribution getAttribution() {
		return attribution;
	}

	/**
	 * Returns the extended descriptions.
	 * @return extended descriptions, may be <code>null</code> (no extended descriptions)
	 */
	public List<ExtendedDescription> getExtendedDescriptions() {
		return extendedDescriptions;
	}

}
