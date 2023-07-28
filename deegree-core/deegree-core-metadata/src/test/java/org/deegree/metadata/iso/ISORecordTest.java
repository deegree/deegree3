/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.metadata.iso;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.commons.io.IOUtils;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.FilteringXMLStreamWriter;
import org.deegree.cs.CRSUtils;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.spatial.BBOX;
import org.deegree.geometry.GeometryFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Basic tests for creation (XML parsing) of {@link ISORecord} instances.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class ISORecordTest {

	private final static NamespaceBindings nsContext = CommonNamespaces.getNamespaceContext();

	private static final URL DATASET = ISORecordTest.class.getResource("full.xml");

	@Ignore
	@Test
	public void testFull() throws Exception {
		XMLAdapter xml = new XMLAdapter(DATASET);
		OMElement filterEl = xml.getRootElement();
		String actual = writeOut(filterEl, null);
		String expected = IOUtils.toString(ISORecordTest.class.getResourceAsStream("full_expected.xml"), UTF_8);
		assertThat(actual, isSimilarTo(expected).ignoreWhitespace());
	}

	@Ignore
	@Test
	public void testBrief() throws Exception {
		XMLAdapter xml = new XMLAdapter(DATASET);
		OMElement filterEl = xml.getRootElement();
		String actual = writeOut(filterEl, ISORecord.briefFilterElementsXPath);
		String expected = IOUtils.toString(ISORecordTest.class.getResourceAsStream("brief.xml"), UTF_8);
		assertThat(actual, isSimilarTo(expected).ignoreWhitespace());
	}

	@Ignore
	@Test
	public void testSummary() throws Exception {
		XMLAdapter xml = new XMLAdapter(DATASET);
		OMElement filterEl = xml.getRootElement();
		String actual = writeOut(filterEl, ISORecord.summaryFilterElementsXPath);
		String expected = IOUtils.toString(ISORecordTest.class.getResourceAsStream("summary.xml"), UTF_8);
		assertThat(actual, isSimilarTo(expected).ignoreWhitespace());
	}

	private String writeOut(OMElement filterEl, List<XPath> paths) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(bos);
		if (paths != null) {
			writer = new FilteringXMLStreamWriter(writer, paths);
		}
		filterEl.serialize(writer);
		writer.close();
		return bos.toString();
	}

	@Test
	public void testInstantiationFromXMLStream() throws XMLStreamException, FactoryConfigurationError {
		InputStream is = ISORecordTest.class.getResourceAsStream("datasetRecord.xml");
		XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(is);
		ISORecord record = new ISORecord(xmlStream);
		assertEquals("5E50884F-5549-2A7A-99E3-334234A887C81", record.getIdentifier());
	}

	@Test
	public void testInstantiationFromXMLStreamOfBrokenRecord() throws XMLStreamException, FactoryConfigurationError {
		InputStream is = ISORecordTest.class.getResourceAsStream("datasetRecord_invalidDate.invalid");
		XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(is);
		ISORecord record = new ISORecord(xmlStream);
		boolean exception = false;
		try {
			record.getIdentifier();
		}
		catch (Exception e) {
			exception = true;
		}
		assertTrue(exception);
	}

	@Test
	public void testEvalFilterSubject() throws Exception {
		InputStream is = ISORecordTest.class.getResourceAsStream("datasetRecord.xml");
		XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(is);
		ISORecord record = new ISORecord(xmlStream);

		Literal<PrimitiveValue> literal = new Literal<PrimitiveValue>("Hydrography");
		Operator operator = new PropertyIsEqualTo(new ValueReference("Subject", nsContext), literal, true, null);

		Filter filter = new OperatorFilter(operator);
		boolean isMatching = record.eval(filter);
		assertTrue(isMatching);
	}

	@Test
	public void testEvalFilterSubjectUnmatching() throws Exception {
		InputStream is = ISORecordTest.class.getResourceAsStream("datasetRecord.xml");
		XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(is);
		ISORecord record = new ISORecord(xmlStream);

		Literal<PrimitiveValue> literal = new Literal<PrimitiveValue>("NotAKeywordInRecord");
		Operator operator = new PropertyIsEqualTo(new ValueReference("Subject", nsContext), literal, true, null);

		Filter filter = new OperatorFilter(operator);
		boolean isMatching = record.eval(filter);
		Assert.assertFalse(isMatching);
	}

	@Test
	public void testEvalFilterBbox() throws Exception {
		InputStream is = ISORecordTest.class.getResourceAsStream("datasetRecord.xml");
		XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(is);
		ISORecord record = new ISORecord(xmlStream);

		GeometryFactory geomFactory = new GeometryFactory();
		ValueReference reference = new ValueReference("apiso:BoundingBox", nsContext);
		Operator operator = new BBOX(reference,
				geomFactory.createEnvelope(7.2, 49.30, 10.70, 53.70, CRSUtils.EPSG_4326));
		Filter filter = new OperatorFilter(operator);
		boolean isMatching = record.eval(filter);
		assertTrue(isMatching);
	}

	@Test(expected = FilterEvaluationException.class)
	public void testEvalFilterUnknownPropertyName() throws Exception {
		InputStream is = ISORecordTest.class.getResourceAsStream("datasetRecord.xml");
		XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(is);
		ISORecord record = new ISORecord(xmlStream);

		Literal<PrimitiveValue> literal = new Literal<PrimitiveValue>("Hydrography");
		Operator operator = new PropertyIsEqualTo(new ValueReference("Unknown", nsContext), literal, true, null);

		Filter filter = new OperatorFilter(operator);
		boolean isMatching = record.eval(filter);
		assertTrue(isMatching);
	}

	@Test
	public void testInstantiationOfSpatialDataServiceRecord() throws Exception {
		InputStream is = ISORecordTest.class.getResourceAsStream("sds-example.xml");
		XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(is);
		ISORecord record = new ISORecord(xmlStream);

		assertThat(record.getIdentifier(), is("mysdsid1"));
		assertThat(record.getType(), is("service"));
		assertThat(record.getAbstract().length, is(1));
		assertThat(record.getAbstract()[0], containsString(
				"This Spatial Data Service supports various operations on the Digital Subsurface Models"));
		assertThat(record.getParsedElement().getQueryableProperties().getKeywords().size(), is(1));
		assertThat(record.getParsedElement().getQueryableProperties().getKeywords().get(0).getKeywords().size(), is(5));
		assertThat(record.getParsedElement().getQueryableProperties().getKeywords().get(0).getKeywords(),
				hasItems("infoCoverageAccessService", "subsurface", "geology", "Netherlands", "3D model"));
		assertThat(record.getParsedElement().getQueryableProperties().getServiceType(), is("other"));
	}

}