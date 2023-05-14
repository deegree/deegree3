/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.geometry.wkbadapter;

import static junit.framework.Assert.assertTrue;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Geometry.GeometryType;
import org.deegree.geometry.io.WKBReader;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.junit.Test;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:goltz@deegree.org">Lyn Goltz</a>
 */
public class WKBReaderTest {

	private final String BASE_DIR = "../wkb/";

	@Test
	public void testWKBToGML() throws Exception {
		ICRS crs = CRSManager.lookup("EPSG:4326");

		InputStream is = WKBReaderTest.class.getResourceAsStream(BASE_DIR + "Polygon.wkb");
		byte[] wkb = IOUtils.toByteArray(is);

		Polygon geom = (Polygon) WKBReader.read(wkb, crs);
		assertTrue(geom.getGeometryType() == GeometryType.PRIMITIVE_GEOMETRY);

		StringWriter sw = new StringWriter();
		XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
		outFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);

		XMLStreamWriter writer = outFactory.createXMLStreamWriter(sw);
		writer.setDefaultNamespace(CommonNamespaces.GML3_2_NS);
		GMLStreamWriter gmlSw = GMLOutputFactory.createGMLStreamWriter(GMLVersion.GML_32, writer);
		gmlSw.write(geom);
		writer.close();

		String s = "<gml:posList>5.148530 59.951879 5.134692 59.736522 5.561175 59.728897 5.577771 59.944188 5.148530 59.951879</gml:posList>";
		assertTrue(sw.toString().contains(s));
	}

}