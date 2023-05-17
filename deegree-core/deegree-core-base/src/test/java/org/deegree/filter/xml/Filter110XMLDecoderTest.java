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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.filter.Filter;
import org.deegree.filter.IdFilter;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.ComparisonOperator;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.logical.And;
import org.deegree.filter.logical.LogicalOperator;
import org.deegree.filter.spatial.BBOX;
import org.deegree.filter.spatial.Contains;
import org.deegree.filter.spatial.Crosses;
import org.deegree.filter.spatial.Disjoint;
import org.deegree.filter.spatial.Equals;
import org.deegree.filter.spatial.Intersects;
import org.deegree.filter.spatial.Overlaps;
import org.deegree.filter.spatial.Touches;
import org.deegree.filter.spatial.Within;
import org.deegree.junit.XMLAssert;
import org.deegree.junit.XMLMemoryStreamWriter;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

/**
 * Tests the correct parsing and exporting of Filter Encoding 1.1.0 documents.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class Filter110XMLDecoderTest {

	private static final Logger LOG = getLogger(Filter110XMLDecoderTest.class);

	@Before
	public void setUp() throws Exception {
		new DefaultWorkspace(new File("nix")).initAll();
	}

	@Test
	public void parseIdFilter() throws XMLStreamException, FactoryConfigurationError, IOException {
		Filter filter = parse("testfilter_110_id.xml");
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
	public void parseMixedIdFilter() throws XMLStreamException, FactoryConfigurationError, IOException {
		parse("testfilter_110_id_mixed.xml");
	}

	@Test
	public void parseOperatorFilter() throws XMLStreamException, FactoryConfigurationError, IOException {
		Filter filter = parse("testfilter_110_operator.xml");
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
	public void parseBrokenIdFilterDocument() throws XMLStreamException, FactoryConfigurationError, IOException {
		parse("testfilter_110_id.invalid_xml");
	}

	@Test(expected = XMLParsingException.class)
	public void parseBrokenIdFilterDocument2() throws XMLStreamException, FactoryConfigurationError, IOException {
		parse("testfilter_110_id2.invalid_xml");
	}

	@Test
	public void parseAndExportFilterDocument() throws XMLStreamException, FactoryConfigurationError, IOException,
			UnknownCRSException, TransformationException {

		Filter filter = parse("testfilter_110_operator.xml");

		XMLMemoryStreamWriter writer = new XMLMemoryStreamWriter();
		Filter110XMLEncoder.export(filter, writer.getXMLStreamWriter());

		String schemaLocation = "http://schemas.opengis.net/filter/1.1.0/filter.xsd";
		XMLAssert.assertValidity(writer.getReader(), schemaLocation);
	}

	private Filter parse(String resourceName) throws XMLStreamException, FactoryConfigurationError, IOException {
		URL url = Filter110XMLDecoderTest.class.getResource("v110/" + resourceName);
		XMLStreamReader xmlStream = XMLInputFactory.newInstance()
			.createXMLStreamReader(url.toString(), url.openStream());
		xmlStream.nextTag();
		Location loc = xmlStream.getLocation();
		LOG.debug("" + loc.getLineNumber());
		LOG.debug("" + loc.getSystemId());
		LOG.debug("" + loc.getColumnNumber());
		return Filter110XMLDecoder.parse(xmlStream);
	}

	@Test
	public void parseBeyondFilter() throws XMLStreamException, FactoryConfigurationError, IOException {
		Filter filter = parse("testfilter15.xml");
		Assert.assertNotNull(filter);

	}

	@Test
	public void parseDisjointFilter() throws XMLStreamException, FactoryConfigurationError, IOException {
		Filter filter = parse("testfilter16.xml");
		Assert.assertNotNull(filter);

	}

	@Test
	public void parseContainsFilter() throws XMLStreamException, FactoryConfigurationError, IOException {
		Filter filter = parse("testfilter17.xml");
		Assert.assertNotNull(filter);

	}

	@Test
	public void parseCrossesFilter() throws XMLStreamException, FactoryConfigurationError, IOException {
		Filter filter = parse("testfilter18.xml");
		Assert.assertNotNull(filter);

	}

	@Test
	public void parseDWithinFilter() throws XMLStreamException, FactoryConfigurationError, IOException {
		Filter filter = parse("testfilter19.xml");
		Assert.assertNotNull(filter);

	}

	@Test
	public void parseIntersectsFilter() throws XMLStreamException, FactoryConfigurationError, IOException {
		Filter filter = parse("testfilter20.xml");
		Assert.assertNotNull(filter);

	}

	@Test
	public void parseEqualsFilter() throws XMLStreamException, FactoryConfigurationError, IOException {
		Filter filter = parse("testfilter21.xml");
		Assert.assertNotNull(filter);

	}

	@Test
	public void parseOverlapsFilter() throws XMLStreamException, FactoryConfigurationError, IOException {
		Filter filter = parse("testfilter22.xml");
		Assert.assertNotNull(filter);

	}

	@Test
	public void parseTouchesFilter() throws XMLStreamException, FactoryConfigurationError, IOException {
		Filter filter = parse("testfilter23.xml");
		Assert.assertNotNull(filter);

	}

	@Test
	public void parseWithinFilter() throws XMLStreamException, FactoryConfigurationError, IOException {
		Filter filter = parse("testfilter24.xml");
		Assert.assertNotNull(filter);

	}

	@Test
	public void parseBBoxWithSpatialJoin() throws XMLStreamException, FactoryConfigurationError, IOException {
		Filter filter = parse("bboxWithSpatialJoin.xml");
		BBOX bbox = (BBOX) ((OperatorFilter) filter).getOperator();

		assertThat(((ValueReference) bbox.getParam1()).getAsText(), is("app:ft1/app:geom"));
		assertThat(bbox.getGeometry(), is(CoreMatchers.nullValue()));
		assertThat(bbox.getValueReference().getAsText(), is("app:ft2/app:geom"));
	}

	@Test
	public void parseContainsWithSpatialJoin() throws XMLStreamException, FactoryConfigurationError, IOException {
		Filter filter = parse("containsWithSpatialJoin.xml");
		Contains contains = (Contains) ((OperatorFilter) filter).getOperator();

		assertThat(((ValueReference) contains.getParam1()).getAsText(), is("app:ft1/app:geom"));
		assertThat(contains.getGeometry(), is(CoreMatchers.nullValue()));
		assertThat(contains.getValueReference().getAsText(), is("app:ft2/app:geom"));
	}

	@Test
	public void parseCrossesWithSpatialJoin() throws XMLStreamException, FactoryConfigurationError, IOException {
		Filter filter = parse("crossesWithSpatialJoin.xml");
		Crosses crosses = (Crosses) ((OperatorFilter) filter).getOperator();

		assertThat(((ValueReference) crosses.getParam1()).getAsText(), is("app:ft1/app:geom"));
		assertThat(crosses.getGeometry(), is(CoreMatchers.nullValue()));
		assertThat(crosses.getValueReference().getAsText(), is("app:ft2/app:geom"));
	}

	@Test
	public void parseDisjointWithSpatialJoin() throws XMLStreamException, FactoryConfigurationError, IOException {
		Filter filter = parse("disjointWithSpatialJoin.xml");
		Disjoint disjoint = (Disjoint) ((OperatorFilter) filter).getOperator();

		assertThat(((ValueReference) disjoint.getParam1()).getAsText(), is("app:ft1/app:geom"));
		assertThat(disjoint.getGeometry(), is(CoreMatchers.nullValue()));
		assertThat(disjoint.getValueReference().getAsText(), is("app:ft2/app:geom"));
	}

	@Test
	public void parseEqualsWithSpatialJoin() throws XMLStreamException, FactoryConfigurationError, IOException {
		Filter filter = parse("equalsWithSpatialJoin.xml");
		Equals equals = (Equals) ((OperatorFilter) filter).getOperator();

		assertThat(((ValueReference) equals.getParam1()).getAsText(), is("app:ft1/app:geom"));
		assertThat(equals.getGeometry(), is(CoreMatchers.nullValue()));
		assertThat(equals.getValueReference().getAsText(), is("app:ft2/app:geom"));
	}

	@Test
	public void parseIntersectsWithSpatialJoin() throws XMLStreamException, FactoryConfigurationError, IOException {
		Filter filter = parse("intersectsWithSpatialJoin.xml");
		Intersects intersects = (Intersects) ((OperatorFilter) filter).getOperator();

		assertThat(((ValueReference) intersects.getParam1()).getAsText(), is("app:ft1/app:geom"));
		assertThat(intersects.getGeometry(), is(CoreMatchers.nullValue()));
		assertThat(intersects.getValueReference().getAsText(), is("app:ft2/app:geom"));
	}

	@Test
	public void parseOverlapsWithSpatialJoin() throws XMLStreamException, FactoryConfigurationError, IOException {
		Filter filter = parse("overlapsWithSpatialJoin.xml");
		Overlaps overlaps = (Overlaps) ((OperatorFilter) filter).getOperator();

		assertThat(((ValueReference) overlaps.getParam1()).getAsText(), is("app:ft1/app:geom"));
		assertThat(overlaps.getGeometry(), is(CoreMatchers.nullValue()));
		assertThat(overlaps.getValueReference().getAsText(), is("app:ft2/app:geom"));
	}

	@Test
	public void parseTouchesWithSpatialJoin() throws XMLStreamException, FactoryConfigurationError, IOException {
		Filter filter = parse("touchesWithSpatialJoin.xml");
		Touches touches = (Touches) ((OperatorFilter) filter).getOperator();

		assertThat(((ValueReference) touches.getParam1()).getAsText(), is("app:ft1/app:geom"));
		assertThat(touches.getGeometry(), is(CoreMatchers.nullValue()));
		assertThat(touches.getValueReference().getAsText(), is("app:ft2/app:geom"));
	}

	@Test
	public void parseWithinWithSpatialJoin() throws XMLStreamException, FactoryConfigurationError, IOException {
		Filter filter = parse("withinWithSpatialJoin.xml");
		Within within = (Within) ((OperatorFilter) filter).getOperator();

		assertThat(((ValueReference) within.getParam1()).getAsText(), is("app:ft1/app:geom"));
		assertThat(within.getGeometry(), is(CoreMatchers.nullValue()));
		assertThat(within.getValueReference().getAsText(), is("app:ft2/app:geom"));
	}

}