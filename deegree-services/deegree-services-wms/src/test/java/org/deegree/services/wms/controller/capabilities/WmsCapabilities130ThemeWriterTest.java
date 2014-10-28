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
package org.deegree.services.wms.controller.capabilities;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static javax.xml.stream.XMLOutputFactory.newInstance;
import static org.deegree.commons.xml.CommonNamespaces.WMSNS;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.metadata.DatasetMetadata;
import org.deegree.commons.ows.metadata.Description;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.StringPair;
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
import org.h2.util.IOUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link WmsCapabilities130ThemeWriter}.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.3
 */
public class WmsCapabilities130ThemeWriterTest {

    private WmsCapabilities130ThemeWriter themeWriter;

    @Before
    public void setup() {
        final OWSMetadataProvider metadataProvider = createMockMetadataProvider();
        themeWriter = new WmsCapabilities130ThemeWriter( metadataProvider, null, null );
    }

    @Test
    public void writeTheme()
                            throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final XMLStreamWriter xmlWriter = newInstance().createXMLStreamWriter( bos );
        xmlWriter.writeStartElement( "Layer" );
        xmlWriter.writeNamespace( "", WMSNS );
        xmlWriter.writeNamespace( "xlink", XLNNS );
        XMLAdapter.writeElement( xmlWriter, "Title", "Container" );
        final Theme theme = createExampleTheme();
        themeWriter.writeTheme( xmlWriter, theme );
        xmlWriter.writeEndElement();
        xmlWriter.flush();
        bos.close();
        final InputStream is = WmsCapabilities130ThemeWriterTest.class.getResourceAsStream( "wms130_layer.xml" );
        final byte[] expected = IOUtils.readBytesAndClose( is, -1 );
        System.out.println(new String (bos.toByteArray()));
        assertArrayEquals( expected, bos.toByteArray() );
    }

    private OWSMetadataProvider createMockMetadataProvider() {
        final OWSMetadataProvider metadataProvider = mock( OWSMetadataProvider.class );
        final DatasetMetadata dataset = createDatasetMetadata();
        final List<DatasetMetadata> datasets = singletonList( dataset );
        when( metadataProvider.getDatasetMetadata() ).thenReturn( datasets );
        when( metadataProvider.getDatasetMetadata( dataset.getQName() ) ).thenReturn( dataset );
        final Map<String, String> authorityNameToUrl = new HashMap<String, String>();
        authorityNameToUrl.put( "authority1", "http://www.authority1.com" );
        authorityNameToUrl.put( "authority2", "http://www.authority2.com" );
        when( metadataProvider.getExternalMetadataAuthorities() ).thenReturn( authorityNameToUrl );
        return metadataProvider;
    }

    private DatasetMetadata createDatasetMetadata() {
        final QName name = new QName( "SimpleTheme" );
        final List<LanguageString> titles = singletonList( new LanguageString( "SimpleTheme Title", null ) );
        final List<LanguageString> abstracts = singletonList( new LanguageString( "SimpleTheme Abstract", null ) );
        final List<Pair<List<LanguageString>, CodeType>> keywords = new ArrayList<Pair<List<LanguageString>, CodeType>>();
        final List<LanguageString> keywordsList1 = new ArrayList<LanguageString>();
        keywordsList1.add( new LanguageString( "keyword1", null ) );
        keywordsList1.add( new LanguageString( "keyword2", null ) );
        keywordsList1.add( new LanguageString( "keyword3", null ) );
        final CodeType code1 = new CodeType( "code", "codeSpace" );
        final Pair<List<LanguageString>, CodeType> keywords1 = new Pair<List<LanguageString>, CodeType>( keywordsList1,
                                                                                                         code1 );
        keywords.add( keywords1 );
        final String url = "http://www.url.net";
        final List<StringPair> externalUrls = new ArrayList<StringPair>();
        externalUrls.add( new StringPair( "authority1", "http://www.authority1.com/url1" ) );
        externalUrls.add( new StringPair( "authority2", "http://www.authority2.com/url2" ) );
        return new DatasetMetadata( name, titles, abstracts, keywords, url, externalUrls );
    }

    private Theme createExampleTheme() {
        final LanguageString title = new LanguageString( "SimpleThemeTitle", null );
        final List<LanguageString> titles = Collections.singletonList( title );
        final Description description = new Description( "SimpleTheme", titles, null, null );
        final ICRS epsg28992 = CRSManager.getCRSRef( "EPSG:28992" );
        final ICRS epsg25832 = CRSManager.getCRSRef( "EPSG:25832" );
        final double[] min = new double[] { -59874.0, 249043.0 };
        final double[] max = new double[] { 316878.0, 885500.0 };
        final Envelope envelope = new GeometryFactory().createEnvelope( min, max, epsg28992 );
        final List<ICRS> crsList = new ArrayList<ICRS>();
        crsList.add( epsg28992 );
        crsList.add( epsg25832 );
        final SpatialMetadata spatialMetadata = new SpatialMetadata( envelope, crsList );
        final LayerMetadata themeMetadata = new LayerMetadata( "SimpleTheme", description, spatialMetadata );
        themeMetadata.setCascaded( 5 );
        final DoublePair scaleDenominators = new DoublePair( 0.0, 999999.9 );
        themeMetadata.setScaleDenominators( scaleDenominators );
        final List<Theme> subThemes = emptyList();
        final List<Layer> layers = emptyList();
        return new StandardTheme( themeMetadata, subThemes, layers, null );
    }

}
