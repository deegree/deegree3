/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.services.wms.controller.capabilities.theme;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
import org.junit.Test;

/**
 * Unit tests for {@link DatasetMetadataMerger}.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class DatasetMetadataMergerTest {

	private static final String URL_1 = "http://metadataUrl.org/1";

	private static final String URL_2 = "http://metadataUrl.org/2";

	private static final String URL_3 = "http://metadataUrl.org/3";

	private static final String URL_4 = "http://metadataUrl.org/4";

	@Test
	public void testMerge() {
		DatasetMetadataMerger datasetMetadataMerger = new DatasetMetadataMerger();
		DatasetMetadata providerMetadata = createDatasetMetadata(URL_1);
		DatasetMetadata layerMetadata = createDatasetMetadata(URL_2, URL_3);

		DatasetMetadata mergedMetadata = datasetMetadataMerger.merge(providerMetadata, layerMetadata);

		List<MetadataUrl> metadataUrls = mergedMetadata.getMetadataUrls();
		assertThat(metadataUrls.size(), is(3));
	}

	@Test
	public void testMergeThree() {
		DatasetMetadataMerger datasetMetadataMerger = new DatasetMetadataMerger();
		DatasetMetadata metadata1 = createDatasetMetadata(URL_1);
		DatasetMetadata metadata2 = createDatasetMetadata(URL_2, URL_3);
		DatasetMetadata metadata3 = createDatasetMetadata(URL_4);

		DatasetMetadata mergedMetadata = datasetMetadataMerger.merge(asList(metadata1, metadata2, metadata3));

		List<MetadataUrl> metadataUrls = mergedMetadata.getMetadataUrls();
		assertThat(metadataUrls.size(), is(4));
	}

	private DatasetMetadata createDatasetMetadata(String... urls) {
		List<LanguageString> titles = new ArrayList<LanguageString>();
		List<LanguageString> abstracts = new ArrayList<LanguageString>();
		List<Pair<List<LanguageString>, CodeType>> keywords = new ArrayList<Pair<List<LanguageString>, CodeType>>();
		List<MetadataUrl> metadataUrls = new ArrayList<MetadataUrl>();
		List<ExternalIdentifier> externalIds = new ArrayList<ExternalIdentifier>();
		List<UrlWithFormat> dataUrls = new ArrayList<UrlWithFormat>();
		List<UrlWithFormat> featureListUrls = new ArrayList<UrlWithFormat>();
		Attribution attribution = null;
		for (String url : urls) {
			metadataUrls.add(new MetadataUrl(url, "ISO19115:2003", "application/xml"));
		}
		List<ExtendedDescription> extendedDescriptions = null;
		return new DatasetMetadata(new QName("provider"), titles, abstracts, keywords, metadataUrls, externalIds,
				dataUrls, featureListUrls, attribution, extendedDescriptions);
	}

}