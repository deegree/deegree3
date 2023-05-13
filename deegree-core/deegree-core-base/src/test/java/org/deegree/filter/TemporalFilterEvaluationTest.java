package org.deegree.filter;

import static org.deegree.gml.GMLVersion.GML_32;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.xpath.TypedObjectNodeXPathEvaluator;
import org.deegree.filter.function.FunctionManager;
import org.deegree.filter.xml.Filter200XMLDecoder;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.jaxen.SimpleNamespaceContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TemporalFilterEvaluationTest {

	private FeatureCollection fc;

	private SimpleNamespaceContext nsContext;

	@Before
	public void setUp() throws Exception {
		Workspace workspace = new DefaultWorkspace(new File("nix"));
		workspace.initAll();
		URL docURL = this.getClass().getResource("../gml/aixm/feature/AIXM51_BasicMessage.gml");
		GMLStreamReader gmlStream = GMLInputFactory.createGMLStreamReader(GML_32, docURL);
		fc = (FeatureCollection) gmlStream.readFeature();
		gmlStream.getIdContext().resolveLocalRefs();
		nsContext = new SimpleNamespaceContext();
		nsContext.addNamespace("gml", "http://www.opengis.net/gml/3.2");
		nsContext.addNamespace("aixm", "http://www.aixm.aero/schema/5.1");
		new FunctionManager().init(workspace);
	}

	@Test
	public void evaluateTEquals() throws FilterEvaluationException, XMLStreamException, FactoryConfigurationError {
		final Filter filter = parseFilter("tequals.xml");
		assertResultSet(fc.getMembers(filter, new TypedObjectNodeXPathEvaluator()), "EADD", "EADH");
	}

	private void assertResultSet(FeatureCollection fc, String... expectedIds) {
		Assert.assertEquals(expectedIds.length, fc.size());
		Set<String> ids = new HashSet<String>();
		for (Feature feature : fc) {
			ids.add(feature.getId());
		}
		for (String string : expectedIds) {
			Assert.assertTrue(ids.contains(string));
		}
	}

	private Filter parseFilter(String resourceName) throws XMLStreamException, FactoryConfigurationError {
		InputStream is = TemporalFilterEvaluationTest.class.getResourceAsStream("xml/v200/temporal/" + resourceName);
		XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(is);
		xmlStream.nextTag();
		return Filter200XMLDecoder.parse(xmlStream);
	}

}
