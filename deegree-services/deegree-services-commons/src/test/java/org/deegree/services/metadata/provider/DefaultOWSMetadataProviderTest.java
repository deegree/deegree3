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
package org.deegree.services.metadata.provider;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
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
 * Unit tests for {@link DefaultOWSMetadataProvider}.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class DefaultOWSMetadataProviderTest {

	@Test
	public void testGetDatasetMetadata() throws Exception {
		DefaultOWSMetadataProvider metadataProvider = createProvider();

		List<DatasetMetadata> datasetMetadata = metadataProvider.getDatasetMetadata();

		assertThat(datasetMetadata.size(), is(3));
	}

	@Test
	public void testGetDatasetMetadataByName() throws Exception {
		DefaultOWSMetadataProvider metadataProvider = createProvider();

		DatasetMetadata datasetMetadata = metadataProvider.getDatasetMetadata(new QName("name1"));

		assertThat(datasetMetadata, is(notNullValue()));
	}

	@Test
	public void testGetAllDatasetMetadataByName() throws Exception {
		DefaultOWSMetadataProvider metadataProvider = createProvider();

		List<DatasetMetadata> datasetMetadata = metadataProvider.getAllDatasetMetadata(new QName("name1"));

		assertThat(datasetMetadata.size(), is(2));
	}

	private DefaultOWSMetadataProvider createProvider() {
		List<DatasetMetadata> datasetMetadata = new ArrayList<DatasetMetadata>();
		datasetMetadata.add(createDatasetMetadata("name1", "http:/url.org/1"));
		datasetMetadata.add(createDatasetMetadata("name1", "http:/url.org/2", "http:/url.org/3"));
		datasetMetadata.add(createDatasetMetadata("name2", "http:/url.org/4"));
		return new DefaultOWSMetadataProvider(null, null, null, datasetMetadata, null, null);
	}

	private DatasetMetadata createDatasetMetadata(String name, String... urls) {
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
		List<ExtendedDescription> extendedDescriptions = new ArrayList<>();
		return new DatasetMetadata(new QName(name), titles, abstracts, keywords, metadataUrls, externalIds, dataUrls,
				featureListUrls, attribution, extendedDescriptions);
	}

}