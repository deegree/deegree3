package org.deegree.services.wms.controller.capabilities;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static javax.xml.stream.XMLOutputFactory.newInstance;
import static org.deegree.commons.xml.CommonNamespaces.WMSNS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.metadata.DatasetMetadata;
import org.deegree.commons.ows.metadata.Description;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.StringPair;
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
import org.deegree.workspace.ResourceMetadata;
import org.junit.Before;
import org.junit.Test;

public class WmsCapabilities130ThemeWriterTest {

    private WmsCapabilities130ThemeWriter themeWriter;

    @Before
    public void setup() {
        final Capabilities130XMLAdapter capWriter = null;
        final OWSMetadataProvider metadataProvider = createMockMetadataProvider();
        final String getUrl = "";
        themeWriter = new WmsCapabilities130ThemeWriter( capWriter, metadataProvider, getUrl );
    }

    @Test
    public void writeThemeWithDatasetMetadataFromProvider()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final XMLStreamWriter xmlWriter = newInstance().createXMLStreamWriter( bos );
        xmlWriter.writeStartElement( "Container" );
        xmlWriter.writeNamespace( "", WMSNS );
        final Theme theme = createSimpleThemeWithNoSubthemes();
        themeWriter.writeTheme( xmlWriter, theme );
        xmlWriter.writeEndElement();
        xmlWriter.flush();
        bos.close();
    }

    private OWSMetadataProvider createMockMetadataProvider() {
        final OWSMetadataProvider metadataProvider = mock( OWSMetadataProvider.class );
        final DatasetMetadata dataset = createDatasetMetadata();
        final List<DatasetMetadata> datasets = singletonList( dataset );
        when( metadataProvider.getDatasetMetadata() ).thenReturn( datasets );
        when( metadataProvider.getDatasetMetadata( dataset.getQName() ) ).thenReturn( dataset );
        return metadataProvider;
    }

    private DatasetMetadata createDatasetMetadata() {
        final QName name = new QName( "SimpleTheme" );
        final List<LanguageString> titles = singletonList( new LanguageString( "SimpleTheme Title", null ) );
        final List<LanguageString> abstracts = singletonList( new LanguageString( "SimpleTheme Abstract", null ) );
        final List<Pair<List<LanguageString>, CodeType>> keywords = new ArrayList<Pair<List<LanguageString>, CodeType>>();
        final String url = "http://www.url.net";
        final List<StringPair> externalUrls = new ArrayList<StringPair>();
        return new DatasetMetadata( name, titles, abstracts, keywords, url, externalUrls );
    }

    private Theme createSimpleThemeWithNoSubthemes() {
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
        final LayerMetadata themeMetadata = new LayerMetadata( "Theme1", description, spatialMetadata );
        final List<Theme> subThemes = emptyList();
        final List<Layer> layers = emptyList();
        final ResourceMetadata<Theme> resourceMetadata = null;
        return new StandardTheme( themeMetadata, subThemes, layers, resourceMetadata );
    }

}
