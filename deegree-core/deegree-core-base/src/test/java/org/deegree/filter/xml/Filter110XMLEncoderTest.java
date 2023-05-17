/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.filter.xml;

import static org.junit.Assert.assertNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.filter.Filter;
import org.deegree.filter.IdFilter;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.ComparisonOperator;
import org.deegree.filter.logical.And;
import org.deegree.filter.logical.LogicalOperator;
import org.deegree.junit.XMLAssert;
import org.deegree.junit.XMLMemoryStreamWriter;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

/**
 * Tests the correct parsing and exporting of Filter Encoding 1.1.0 documents.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class Filter110XMLEncoderTest {

	private static final Logger LOG = getLogger(Filter110XMLEncoderTest.class);

	@Before
	public void setUp() throws Exception {
		new DefaultWorkspace(new File("nix")).initAll();
	}

	private Filter testImportExportImport(String resource) throws XMLStreamException, FactoryConfigurationError,
			UnknownCRSException, TransformationException, IOException {
		URL url = Filter110XMLEncoderTest.class.getResource("v110/" + resource);
		XMLStreamReader in = XMLInputFactory.newInstance().createXMLStreamReader(url.toString(), url.openStream());
		in.nextTag();
		Filter filter = Filter110XMLDecoder.parse(in);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter(bos);
		Filter110XMLEncoder.export(filter, out);
		out.close();
		in = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(bos.toByteArray()));
		in.nextTag();
		System.out.println(new String(bos.toByteArray()));
		return Filter110XMLDecoder.parse(in);
	}

	@Test
	public void parseIdFilter() throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException,
			TransformationException {
		Filter filter = testImportExportImport("testfilter_110_id.xml");
		assertNotNull(filter);
		Assert.assertEquals(Filter.Type.ID_FILTER, filter.getType());
		IdFilter idFilter = (IdFilter) filter;
		Assert.assertEquals(4, idFilter.getMatchingIds().size());
		Assert.assertTrue(idFilter.getMatchingIds().contains("PHILOSOPHER_966"));
		Assert.assertTrue(idFilter.getMatchingIds().contains("PHILOSOPHER_967"));
		Assert.assertTrue(idFilter.getMatchingIds().contains("PHILOSOPHER_968"));
		Assert.assertTrue(idFilter.getMatchingIds().contains("PHILOSOPHER_969"));
	}

	@Test(expected = XMLParsingException.class)
	public void parseMixedIdFilter() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		testImportExportImport("testfilter_110_id_mixed.xml");
	}

	@Test
	public void parseOperatorFilter() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		Filter filter = testImportExportImport("testfilter_110_operator.xml");
		Assert.assertNotNull(filter);
		Assert.assertEquals(Filter.Type.OPERATOR_FILTER, filter.getType());
		OperatorFilter operatorFilter = (OperatorFilter) filter;
		Assert.assertEquals(Operator.Type.LOGICAL, operatorFilter.getOperator().getType());
		LogicalOperator logicalOperator = (LogicalOperator) operatorFilter.getOperator();
		Assert.assertEquals(LogicalOperator.SubType.AND, logicalOperator.getSubType());
		And and = (And) logicalOperator;
		Assert.assertEquals(2, and.getSize());
		Assert.assertEquals(Operator.Type.COMPARISON, and.getParameter(0).getType());
		ComparisonOperator param1Oper = (ComparisonOperator) and.getParameter(0);
		Assert.assertEquals(ComparisonOperator.SubType.PROPERTY_IS_GREATER_THAN, param1Oper.getSubType());
		Assert.assertEquals(Operator.Type.COMPARISON, and.getParameter(1).getType());
		ComparisonOperator param2Oper = (ComparisonOperator) and.getParameter(1);
		Assert.assertEquals(ComparisonOperator.SubType.PROPERTY_IS_EQUAL_TO, param2Oper.getSubType());

	}

	@Test(expected = XMLParsingException.class)
	public void parseBrokenIdFilterDocument() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		testImportExportImport("testfilter_110_id.invalid_xml");
	}

	@Test(expected = XMLParsingException.class)
	public void parseBrokenIdFilterDocument2() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		testImportExportImport("testfilter_110_id2.invalid_xml");
	}

	@Test
	public void parseAndExportFilterDocument() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {

		Filter filter = testImportExportImport("testfilter_110_operator.xml");

		XMLMemoryStreamWriter writer = new XMLMemoryStreamWriter();
		Filter110XMLEncoder.export(filter, writer.getXMLStreamWriter());

		String schemaLocation = "http://schemas.opengis.net/filter/1.1.0/filter.xsd";
		XMLAssert.assertValidity(writer.getReader(), schemaLocation);
	}

	@Test
	public void parseBeyondFilter() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		Filter filter = testImportExportImport("testfilter15.xml");
		Assert.assertNotNull(filter);

	}

	@Test
	public void parseDisjointFilter() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		Filter filter = testImportExportImport("testfilter16.xml");
		Assert.assertNotNull(filter);

	}

	@Test
	public void parseContainsFilter() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		Filter filter = testImportExportImport("testfilter17.xml");
		Assert.assertNotNull(filter);

	}

	@Test
	public void parseCrossesFilter() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		Filter filter = testImportExportImport("testfilter18.xml");
		Assert.assertNotNull(filter);

	}

	@Test
	public void parseDWithinFilter() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		Filter filter = testImportExportImport("testfilter19.xml");
		Assert.assertNotNull(filter);

	}

	@Test
	public void parseIntersectsFilter() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		Filter filter = testImportExportImport("testfilter20.xml");
		Assert.assertNotNull(filter);

	}

	@Test
	public void parseEqualsFilter() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		Filter filter = testImportExportImport("testfilter21.xml");
		Assert.assertNotNull(filter);

	}

	@Test
	public void parseOverlapsFilter() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		Filter filter = testImportExportImport("testfilter22.xml");
		Assert.assertNotNull(filter);

	}

	@Test
	public void parseTouchesFilter() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		Filter filter = testImportExportImport("testfilter23.xml");
		Assert.assertNotNull(filter);

	}

	@Test
	public void parseWithinFilter() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		Filter filter = testImportExportImport("testfilter24.xml");
		Assert.assertNotNull(filter);

	}

	@Test
	public void parseBBoxWithSpatialJoin() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		Filter filter = testImportExportImport("bboxWithSpatialJoin.xml");
		Assert.assertNotNull(filter);
	}

	@Test
	public void parseContainsWithSpatialJoin() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		Filter filter = testImportExportImport("containsWithSpatialJoin.xml");
		Assert.assertNotNull(filter);
	}

	@Test
	public void parseCrossesWithSpatialJoin() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		Filter filter = testImportExportImport("crossesWithSpatialJoin.xml");
		Assert.assertNotNull(filter);
	}

	@Test
	public void parseDisjointWithSpatialJoin() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		Filter filter = testImportExportImport("disjointWithSpatialJoin.xml");
		Assert.assertNotNull(filter);
	}

	@Test
	public void parseEqualsWithSpatialJoin() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		Filter filter = testImportExportImport("equalsWithSpatialJoin.xml");
		Assert.assertNotNull(filter);
	}

	@Test
	public void parseIntersectsWithSpatialJoin() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		Filter filter = testImportExportImport("intersectsWithSpatialJoin.xml");
		Assert.assertNotNull(filter);
	}

	@Test
	public void parseOverlapsWithSpatialJoin() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		Filter filter = testImportExportImport("overlapsWithSpatialJoin.xml");
		Assert.assertNotNull(filter);
	}

	@Test
	public void parseTouchesWithSpatialJoin() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		Filter filter = testImportExportImport("touchesWithSpatialJoin.xml");
		Assert.assertNotNull(filter);
	}

	@Test
	public void parseWithinWithSpatialJoin() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {
		Filter filter = testImportExportImport("withinWithSpatialJoin.xml");
		Assert.assertNotNull(filter);
	}

}
