/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.filter.xml;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.filter.Filter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xmlunit.matchers.CompareMatcher;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
@RunWith(Parameterized.class)
public class Filter200XMLEncoderParameterizedTest {

	private String filterUnderTest;

	private String testName;

	public Filter200XMLEncoderParameterizedTest(String testName, String filterUnderTest) {
		this.testName = testName;
		this.filterUnderTest = filterUnderTest;
	}

	@Parameters
	public static List<Object[]> data() throws IOException {
		List<Object[]> filterTests = new ArrayList<Object[]>();
		filterTests.add(new Object[] { "testfilter1.xml", asString("v200/testfilter1.xml") });
		filterTests.add(new Object[] { "testfilter3.xml", asString("v200/testfilter3.xml") });
		filterTests.add(new Object[] { "testfilter4.xml", asString("v200/testfilter4.xml") });
		filterTests.add(new Object[] { "testfilter5.xml", asString("v200/testfilter5.xml") });
		filterTests.add(new Object[] { "testfilter6.xml", asString("v200/testfilter6.xml") });
		filterTests.add(new Object[] { "testfilter7.xml", asString("v200/testfilter7.xml") });
		filterTests.add(new Object[] { "testfilter8.xml", asString("v200/testfilter8.xml") });
		filterTests.add(new Object[] { "aixm_by_gml_identifier.xml", asString("v200/aixm_by_gml_identifier.xml") });
		filterTests
			.add(new Object[] { "aixm_custom_geometry_bbox.xml", asString("v200/aixm_custom_geometry_bbox.xml") });
		filterTests.add(new Object[] { "aixm_custom_geometry_property.xml",
				asString("v200/aixm_custom_geometry_property.xml") });
		filterTests.add(new Object[] { "aixm_timeinstant_begin.xml", asString("v200/aixm_timeinstant_begin.xml") });
		filterTests.add(new Object[] { "temporal/tequals.xml", asString("v200/temporal/tequals.xml") });
		filterTests.add(new Object[] { "bboxWithSpatialJoin.xml", asString("v200/bboxWithSpatialJoin.xml") });
		filterTests.add(new Object[] { "beyondWithSpatialJoin.xml", asString("v200/beyondWithSpatialJoin.xml") });
		filterTests.add(new Object[] { "containsWithSpatialJoin.xml", asString("v200/containsWithSpatialJoin.xml") });
		filterTests.add(new Object[] { "crossesWithSpatialJoin.xml", asString("v200/crossesWithSpatialJoin.xml") });
		filterTests.add(new Object[] { "disjointWithSpatialJoin.xml", asString("v200/disjointWithSpatialJoin.xml") });
		filterTests.add(new Object[] { "dwithinWithSpatialJoin.xml", asString("v200/dwithinWithSpatialJoin.xml") });
		filterTests.add(new Object[] { "equalsWithSpatialJoin.xml", asString("v200/equalsWithSpatialJoin.xml") });
		filterTests
			.add(new Object[] { "intersectsWithSpatialJoin.xml", asString("v200/intersectsWithSpatialJoin.xml") });
		filterTests.add(new Object[] { "overlapsWithSpatialJoin.xml", asString("v200/overlapsWithSpatialJoin.xml") });
		filterTests.add(new Object[] { "touchesWithSpatialJoin.xml", asString("v200/touchesWithSpatialJoin.xml") });
		filterTests.add(new Object[] { "withinWithSpatialJoin.xml", asString("v200/withinWithSpatialJoin.xml") });
		return filterTests;
	}

	@Test
	public void testExport() throws Exception {
		Filter filter = parseFilter(filterUnderTest);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter(bos);
		IndentingXMLStreamWriter writer = new IndentingXMLStreamWriter(out);
		Filter200XMLEncoder.export(filter, writer);
		out.close();

		assertThat("Failed test: " + testName, bos.toString(),
				CompareMatcher.isSimilarTo(filterUnderTest).ignoreWhitespace());
	}

	private static String asString(String filterResource) throws IOException {
		InputStream resourceAsStream = Filter200XMLEncoderParameterizedTest.class.getResourceAsStream(filterResource);
		return IOUtils.toString(resourceAsStream, UTF_8);
	}

	private Filter parseFilter(String filterAsString) throws XMLStreamException, FactoryConfigurationError {
		XMLStreamReader in = XMLInputFactory.newInstance().createXMLStreamReader(toInputStream(filterAsString, UTF_8));
		in.nextTag();
		return Filter200XMLDecoder.parse(in);
	}

}