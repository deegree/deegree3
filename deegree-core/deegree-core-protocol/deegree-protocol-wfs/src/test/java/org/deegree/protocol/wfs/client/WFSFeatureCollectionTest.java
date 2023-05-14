/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.protocol.wfs.client;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.feature.Feature;
import org.deegree.feature.types.AppSchema;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.junit.Test;

public class WFSFeatureCollectionTest {

	@Test
	public void testDeegree3WFS100UtahStateBoundariesGML2() throws Exception {

		AppSchema appSchema = getAppSchema("deegree3_utah_gml2.xsd");
		XMLStreamReader xmlStream = getXMLStream("deegree3_utah_wfs100_gml2.dontvalidate");
		GMLVersion gmlVersion = appSchema.getGMLSchema().getVersion();
		WFSFeatureCollection<Feature> fc = new WFSFeatureCollection<Feature>(xmlStream, gmlVersion, appSchema);

		assertNull(fc.getLockId());
		assertNull(fc.getNextUri());
		assertNull(fc.getNumberMatched());
		assertNull(fc.getNumberReturned());
		assertNull(fc.getPreviousUri());
		assertNull(fc.getTimeStamp());

		Iterator<Feature> iter = fc.getMembers();
		int count = 0;
		while (iter.hasNext()) {
			Feature f = iter.next();
			assertEquals(QName.valueOf("{http://www.deegree.org/app}SGID024_StateBoundary"), f.getName());
			count++;
		}
		assertEquals(2, count);
	}

	@Test
	public void testDeegree3WFS110UtahStateBoundariesGML31() throws Exception {

		AppSchema appSchema = getAppSchema("deegree3_utah_gml31.xsd");
		XMLStreamReader xmlStream = getXMLStream("deegree3_utah_wfs110_gml31.dontvalidate");
		GMLVersion gmlVersion = appSchema.getGMLSchema().getVersion();
		WFSFeatureCollection<Feature> fc = new WFSFeatureCollection<Feature>(xmlStream, gmlVersion, appSchema);

		assertNull(fc.getLockId());
		assertNull(fc.getNextUri());
		assertNull(fc.getNumberMatched());
		assertNull(fc.getNumberReturned());
		assertNull(fc.getPreviousUri());
		assertEquals("2011-09-21T13:27:26.965Z", fc.getTimeStamp());

		Iterator<Feature> iter = fc.getMembers();
		int count = 0;
		while (iter.hasNext()) {
			Feature f = iter.next();
			assertEquals(QName.valueOf("{http://www.deegree.org/app}SGID024_StateBoundary"), f.getName());
			count++;
		}
		assertEquals(2, count);
	}

	@Test
	public void testDeegree3WFS200UtahStateBoundariesGML32() throws Exception {

		AppSchema appSchema = getAppSchema("deegree3_utah_gml32.xsd");
		XMLStreamReader xmlStream = getXMLStream("deegree3_utah_wfs200_gml32.invalidxml");
		GMLVersion gmlVersion = appSchema.getGMLSchema().getVersion();
		WFSFeatureCollection<Feature> fc = new WFSFeatureCollection<Feature>(xmlStream, gmlVersion, appSchema);

		assertNull(fc.getLockId());
		assertNull(fc.getNextUri());
		assertNull(fc.getNumberMatched());
		assertNull(fc.getNumberReturned());
		assertNull(fc.getPreviousUri());
		assertEquals("2011-09-21T14:21:10.681Z", fc.getTimeStamp());

		Iterator<Feature> iter = fc.getMembers();
		int count = 0;
		while (iter.hasNext()) {
			Feature f = iter.next();
			assertEquals(QName.valueOf("{http://www.deegree.org/app}SGID024_StateBoundary"), f.getName());
			count++;
		}
		assertEquals(2, count);
	}

	private XMLStreamReader getXMLStream(String resource)
			throws XMLStreamException, FactoryConfigurationError, IOException {
		URL url = WFSFeatureCollectionTest.class.getResource(resource);
		return XMLInputFactory.newInstance().createXMLStreamReader(url.toString(), url.openStream());
	}

	private AppSchema getAppSchema(String... schemaResources) throws Exception {
		String[] resolvedURLs = new String[schemaResources.length];
		for (int i = 0; i < schemaResources.length; i++) {
			resolvedURLs[i] = WFSFeatureCollectionTest.class.getResource(schemaResources[i]).toString();
		}
		return new GMLAppSchemaReader(null, null, resolvedURLs).extractAppSchema();
	}

}
