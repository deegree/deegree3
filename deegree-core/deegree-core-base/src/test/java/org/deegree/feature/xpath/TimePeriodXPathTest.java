package org.deegree.feature.xpath;

import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipStartDocument;
import static org.deegree.gml.GMLVersion.GML_32;
import static org.junit.Assert.assertEquals;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.ValueReference;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.time.TimeObject;
import org.deegree.time.gml.reader.GmlTimePeriodReader;
import org.deegree.time.gml.reader.GmlTimePositionTypeReader;
import org.jaxen.SimpleNamespaceContext;
import org.junit.Test;

public class TimePeriodXPathTest {

	@Test
	public void evaluateGmlId() throws Exception {
		final TypedObjectNode[] result = evaluate("/gml:TimePeriod/@gml:id");
		assertEquals(1, result.length);
		final PrimitiveValue value = (PrimitiveValue) result[0];
		assertEquals("p1", value.getAsText());
	}

	@Test
	public void evaluateBeginPosition() throws Exception {
		final TypedObjectNode[] result = evaluate("/gml:TimePeriod/gml:beginPosition");
		assertEquals(1, result.length);
		final Property property = (Property) result[0];
		assertEquals(new QName(GML3_2_NS, "beginPosition"), property.getName());
	}

	@Test
	public void evaluateEndPositionValue() throws Exception {
		final TypedObjectNode[] result = evaluate("/gml:TimePeriod/gml:beginPosition/text()");
		assertEquals(1, result.length);
		final PrimitiveValue value = (PrimitiveValue) result[0];
		assertEquals("2001-05-23", value.getAsText());
	}

	@Test
	public void evaluateEndPositionIndeterminatePosition() throws Exception {
		final TypedObjectNode[] result = evaluate("time_period_indeterminate.gml",
				"/gml:TimePeriod/gml:endPosition/@indeterminatePosition");
		assertEquals(1, result.length);
		final PrimitiveValue value = (PrimitiveValue) result[0];
		assertEquals("unknown", value.getAsText());
	}

	private TypedObjectNode[] evaluate(final String xpath) throws Exception, FilterEvaluationException {
		return evaluate("time_period_minimal.gml", xpath);
	}

	private TypedObjectNode[] evaluate(final String example, final String xpath)
			throws Exception, FilterEvaluationException {
		final SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
		nsContext.addNamespace("gml", "http://www.opengis.net/gml/3.2");
		final TimeObject object = readExample(example);
		return new TypedObjectNodeXPathEvaluator().eval(object, new ValueReference(xpath, nsContext));
	}

	private TimeObject readExample(final String example) throws Exception {
		final GMLStreamReader reader = getGmlStreamReader(example);
		final XMLStreamReader xmlStream = reader.getXMLReader();
		return new GmlTimePeriodReader(reader).read(xmlStream);
	}

	private GMLStreamReader getGmlStreamReader(final String exampleName) throws Exception {
		final URL url = GmlTimePositionTypeReader.class.getResource(exampleName);
		GMLStreamReader reader = GMLInputFactory.createGMLStreamReader(GML_32, url);
		skipStartDocument(reader.getXMLReader());
		return reader;
	}

}
