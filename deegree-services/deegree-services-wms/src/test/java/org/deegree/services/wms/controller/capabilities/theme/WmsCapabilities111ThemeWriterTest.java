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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static javax.xml.stream.XMLOutputFactory.newInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.metadata.DatasetMetadata;
import org.deegree.commons.ows.metadata.Description;
import org.deegree.commons.ows.metadata.ExtendedDescription;
import org.deegree.commons.ows.metadata.MetadataUrl;
import org.deegree.commons.ows.metadata.layer.Attribution;
import org.deegree.commons.ows.metadata.layer.ExternalIdentifier;
import org.deegree.commons.ows.metadata.layer.LogoUrl;
import org.deegree.commons.ows.metadata.layer.UrlWithFormat;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.layer.Layer;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.theme.Theme;
import org.deegree.theme.persistence.standard.StandardTheme;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit tests for {@link WmsCapabilities111ThemeWriter}.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
public class WmsCapabilities111ThemeWriterTest {

	private final WmsCapabilities111ThemeWriter themeWriter = new WmsCapabilities111ThemeWriter(null, null, null);

	@Test
	public void writeThemeMinimal() throws Exception {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final XMLStreamWriter writer = newInstance().createXMLStreamWriter(bos);
		writer.writeStartElement("Layer");
		XMLAdapter.writeElement(writer, "Title", "Container");
		final LayerMetadata layerMetadata = createLayerMetadataMinimal();
		layerMetadata.setQueryable(false);
		final DatasetMetadata datasetMetadata = createDatasetMetadataMinimal();
		final Map<String, String> authorityNameToUrl = emptyMap();
		themeWriter.writeTheme(writer, layerMetadata, datasetMetadata, authorityNameToUrl, null, null);
		writer.writeEndElement();
		writer.flush();
		bos.close();

		String expected = org.apache.commons.io.IOUtils
			.toString(WmsCapabilities111ThemeWriterTest.class.getResourceAsStream("wms111_layer_minimal.xml"), UTF_8);
		String actual = bos.toString();
		assertThat(actual, isSimilarTo(expected).ignoreWhitespace().ignoreElementContentWhitespace());
	}

	@Test
	public void writeThemeFull() throws Exception {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final XMLStreamWriter writer = newInstance().createXMLStreamWriter(bos);
		writer.writeStartElement("Layer");
		XMLAdapter.writeElement(writer, "Title", "Container");
		final LayerMetadata layerMetadata = createLayerMetadata();
		final DatasetMetadata datasetMetadata = createDatasetMetadataFull();
		final DoublePair scaleDenominators = new DoublePair(0.0, 999999.9);
		final Map<String, String> authorityNameToUrl = createAuthorityNameToUrlMap();
		themeWriter.writeTheme(writer, layerMetadata, datasetMetadata, authorityNameToUrl, scaleDenominators, null);
		writer.writeEndElement();
		writer.flush();
		bos.close();

		String expected = org.apache.commons.io.IOUtils
			.toString(WmsCapabilities111ThemeWriterTest.class.getResourceAsStream("wms111_layer_full.xml"), UTF_8);
		String actual = bos.toString();
		assertThat(actual, isSimilarTo(expected).ignoreWhitespace().ignoreElementContentWhitespace());
	}

	@Test
	public void testWriteThemeMultipleMetadataUrls() throws Exception {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final XMLStreamWriter writer = newInstance().createXMLStreamWriter(bos);
		writer.writeStartElement("Layer");
		XMLAdapter.writeElement(writer, "Title", "Container");

		final LayerMetadata layerMetadata = createLayerMetadata();

		String mdurlTemplate = "http://md.url.org/template";
		OWSMetadataProvider provider = Mockito.mock(OWSMetadataProvider.class);
		List<DatasetMetadata> mds = new ArrayList<DatasetMetadata>();
		mds.add(createDatasetMetadata("http://url1", "http://url2"));
		mds.add(createDatasetMetadata("http://url3"));
		when(provider.getAllDatasetMetadata(Mockito.any(QName.class))).thenReturn(mds);

		WmsCapabilities111ThemeWriter themeWriter = new WmsCapabilities111ThemeWriter(provider, null, mdurlTemplate);
		Theme theme = new StandardTheme(layerMetadata, Collections.<Theme>emptyList(), Collections.<Layer>emptyList(),
				null);
		themeWriter.writeTheme(writer, theme);
		writer.writeEndElement();
		writer.flush();
		bos.close();

		String expected = org.apache.commons.io.IOUtils.toString(
				WmsCapabilities111ThemeWriterTest.class.getResourceAsStream("wms111_layer_multipleMetadataUrls.xml"),
				UTF_8);
		String actual = bos.toString();
		assertThat(actual, isSimilarTo(expected).ignoreWhitespace().ignoreElementContentWhitespace());
	}

	private DatasetMetadata createDatasetMetadataMinimal() {
		final QName name = new QName("SimpleTheme");
		final List<LanguageString> titles = emptyList();
		final List<LanguageString> abstracts = emptyList();
		final List<Pair<List<LanguageString>, CodeType>> keywords = new ArrayList<Pair<List<LanguageString>, CodeType>>();
		final List<MetadataUrl> metadataUrls = null;
		final List<ExternalIdentifier> externalIds = null;
		final List<UrlWithFormat> dataUrls = null;
		final List<UrlWithFormat> featureListUrls = null;
		final Attribution attribution = null;
		final List<ExtendedDescription> extendedDescriptions = null;
		return new DatasetMetadata(name, titles, abstracts, keywords, metadataUrls, externalIds, dataUrls,
				featureListUrls, attribution, extendedDescriptions);
	}

	private DatasetMetadata createDatasetMetadataFull() {
		final QName name = new QName("SimpleTheme");
		final List<LanguageString> titles = singletonList(new LanguageString("SimpleTheme Title", null));
		final List<LanguageString> abstracts = singletonList(new LanguageString("SimpleTheme Abstract", null));
		final List<Pair<List<LanguageString>, CodeType>> keywords = new ArrayList<Pair<List<LanguageString>, CodeType>>();
		final List<LanguageString> keywordsList1 = new ArrayList<LanguageString>();
		keywordsList1.add(new LanguageString("keyword1", null));
		keywordsList1.add(new LanguageString("keyword2", null));
		keywordsList1.add(new LanguageString("keyword3", null));
		final CodeType code1 = new CodeType("code", "codeSpace");
		final Pair<List<LanguageString>, CodeType> keywords1 = new Pair<List<LanguageString>, CodeType>(keywordsList1,
				code1);
		keywords.add(keywords1);
		final List<MetadataUrl> metadataUrls = new ArrayList<MetadataUrl>();
		metadataUrls.add(new MetadataUrl("http://www.url.net", null, null));
		metadataUrls.add(new MetadataUrl("http://www.url2.net", null, null));
		final List<ExternalIdentifier> externalIds = new ArrayList<ExternalIdentifier>();
		externalIds.add(new ExternalIdentifier("extid1", "authority1"));
		externalIds.add(new ExternalIdentifier("extid2", "authority2"));
		final List<UrlWithFormat> dataUrls = new ArrayList<UrlWithFormat>();
		dataUrls.add(new UrlWithFormat("http://data1.url", "text/xml"));
		dataUrls.add(new UrlWithFormat("http://data2.url", "text/plain"));
		final List<UrlWithFormat> featureListUrls = new ArrayList<UrlWithFormat>();
		featureListUrls.add(new UrlWithFormat("http://featurelist1.url", "text/xml"));
		featureListUrls.add(new UrlWithFormat("http://featurelist2.url", "text/plain"));
		final LogoUrl logoUrl = new LogoUrl("http://logo.url", "image/png", 64, 32);
		final Attribution attribution = new Attribution("AttributionTitle", "http://attribution.url", logoUrl);
		final List<ExtendedDescription> extendedDescriptions = Collections.emptyList();
		return new DatasetMetadata(name, titles, abstracts, keywords, metadataUrls, externalIds, dataUrls,
				featureListUrls, attribution, extendedDescriptions);
	}

	private Map<String, String> createAuthorityNameToUrlMap() {
		final Map<String, String> authorityNameToUrl = new LinkedHashMap<String, String>();
		authorityNameToUrl.put("authority1", "http://whatever.authority1.com");
		authorityNameToUrl.put("authority2", "http://whatever.authority2.com");
		return authorityNameToUrl;
	}

	private LayerMetadata createLayerMetadataMinimal() {
		final List<LanguageString> titles = emptyList();
		final Description description = new Description("SimpleTheme", titles, null, null);
		final SpatialMetadata spatialMetadata = null;
		return new LayerMetadata("SimpleTheme", description, spatialMetadata);
	}

	private LayerMetadata createLayerMetadata() {
		final Description description = new Description("SimpleTheme", null, null, null);
		final ICRS epsg28992 = CRSManager.getCRSRef("EPSG:28992");
		final ICRS epsg25832 = CRSManager.getCRSRef("EPSG:25832");
		final double[] min = new double[] { -59874.0, 249043.0 };
		final double[] max = new double[] { 316878.0, 885500.0 };
		final Envelope envelope = new GeometryFactory().createEnvelope(min, max, epsg28992);
		final List<ICRS> crsList = new ArrayList<ICRS>();
		crsList.add(epsg28992);
		crsList.add(epsg25832);
		final SpatialMetadata spatialMetadata = new SpatialMetadata(envelope, crsList);
		final LayerMetadata themeMetadata = new LayerMetadata("SimpleTheme", description, spatialMetadata);
		themeMetadata.setCascaded(5);
		return themeMetadata;
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
