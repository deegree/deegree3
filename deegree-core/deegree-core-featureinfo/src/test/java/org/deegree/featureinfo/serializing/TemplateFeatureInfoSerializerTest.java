package org.deegree.featureinfo.serializing;

import org.deegree.feature.FeatureCollection;
import org.deegree.featureinfo.FeatureInfoContext;
import org.deegree.featureinfo.FeatureInfoParams;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class TemplateFeatureInfoSerializerTest {

	@Test
	public void testSerialize_DefautHtmlGfi_ShouldReturnHtml() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		TemplateFeatureInfoSerializer serializer = new TemplateFeatureInfoSerializer();
		FeatureInfoParams params = createParams();
		FeatureInfoContext context = mockContext(bos);
		serializer.serialize(params, context);

		String html = bos.toString().trim();
		assertThat(html, startsWith("<html>"));
		assertThat(html, endsWith("</html>"));
	}

	private FeatureInfoParams createParams() throws Exception {
		URL resource = TemplateFeatureInfoSerializer.class.getResource("featurecollection.gml");
		GMLStreamReader gmlStreamReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_32, resource);
		Map<String, String> nsBindings = new HashMap<>();
		FeatureCollection col = gmlStreamReader.readFeatureCollection();
		return new FeatureInfoParams(nsBindings, col, "text/html", true, null, null, null, null);
	}

	private FeatureInfoContext mockContext(OutputStream os) throws IOException {
		FeatureInfoContext mock = mock(FeatureInfoContext.class);
		when(mock.getOutputStream()).thenReturn(os);
		return mock;
	}

}
