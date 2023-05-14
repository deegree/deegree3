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

/**
 * Tests the correct evaluation of filter expressions on AIXM features / geometries.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public class AIXMFilterEvaluationTest {

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
	public void filterByGMLIdentifier()
			throws FilterEvaluationException, XMLStreamException, FactoryConfigurationError {
		Filter filter = parseFilter("aixm_by_gml_identifier.xml");
		assertResultSet(fc.getMembers(filter, new TypedObjectNodeXPathEvaluator()), "ORGCIVIL_AVIATION");
	}

	@Test
	public void filterByAIXMCustomGeometryProperty()
			throws FilterEvaluationException, XMLStreamException, FactoryConfigurationError {
		final Filter filter = parseFilter("aixm_custom_geometry_property.xml");
		assertResultSet(fc.getMembers(filter, new TypedObjectNodeXPathEvaluator()), "EADD");
	}

	@Test
	public void filterByAIXMCustomGeometryBBOX()
			throws FilterEvaluationException, XMLStreamException, FactoryConfigurationError {
		final Filter filter = parseFilter("aixm_custom_geometry_bbox.xml");
		assertResultSet(fc.getMembers(filter, new TypedObjectNodeXPathEvaluator()), "EADH");
	}

	@Test
	public void filterByTimeInstant() throws FilterEvaluationException, XMLStreamException, FactoryConfigurationError {
		final Filter filter = parseFilter("aixm_timeinstant_begin.xml");
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
		InputStream is = AIXMFilterEvaluationTest.class.getResourceAsStream("xml/v200/" + resourceName);
		XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(is);
		xmlStream.nextTag();
		return Filter200XMLDecoder.parse(xmlStream);
	}

}
