/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2024 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.featureinfo.serializing;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.FeatureCollection;
import org.deegree.featureinfo.FeatureInfoContext;
import org.deegree.featureinfo.FeatureInfoParams;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GeoJsonFeatureInfoSerializerTest {

	@Test
	public void testSerialize() throws Exception {
		StringWriter writer = new StringWriter();
		GeoJsonFeatureInfoSerializer serializer = new GeoJsonFeatureInfoSerializer(false, true);
		FeatureInfoParams params = createParams();
		FeatureInfoContext context = mockContext(writer);
		serializer.serialize(params, context);

		String geoJson = writer.toString();
		assertTrue(geoJson.contains("\"type\":"));
		assertTrue(geoJson.contains("\"FeatureCollection\""));
		assertTrue(geoJson.contains("AdministrativeUnit_10044117"));
		assertTrue(geoJson.contains("6.910811280489623"));
	}

	@Test
	public void testSerialize_withInfoCrsNotAllowed() throws Exception {
		StringWriter writer = new StringWriter();
		GeoJsonFeatureInfoSerializer serializer = new GeoJsonFeatureInfoSerializer(false, true);
		FeatureInfoParams params = createParams("EPSG:25832");
		FeatureInfoContext context = mockContext(writer);
		serializer.serialize(params, context);

		String geoJson = writer.toString();
		assertTrue(geoJson.contains("\"type\":"));
		assertTrue(geoJson.contains("\"FeatureCollection\""));
		assertTrue(geoJson.contains("AdministrativeUnit_10044117"));
		assertTrue(geoJson.contains("6.910811280489623"));
	}

	@Test
	public void testSerialize_allowOtherCrs_InfoCrs() throws Exception {
		StringWriter writer = new StringWriter();
		GeoJsonFeatureInfoSerializer serializer = new GeoJsonFeatureInfoSerializer(true, true);
		FeatureInfoParams params = createParams("EPSG:25832");
		FeatureInfoContext context = mockContext(writer);
		serializer.serialize(params, context);

		String geoJson = writer.toString();
		assertTrue(geoJson.contains("\"type\":"));
		assertTrue(geoJson.contains("\"FeatureCollection\""));
		assertTrue(geoJson.contains("AdministrativeUnit_10044117"));
		assertTrue(geoJson.contains("348736.888"));
	}

	@Test
	public void testSerialize_allowOtherCrs_SkipGeometries() throws Exception {
		StringWriter writer = new StringWriter();
		GeoJsonFeatureInfoSerializer serializer = new GeoJsonFeatureInfoSerializer(false, false);
		FeatureInfoParams params = createParams();
		FeatureInfoContext context = mockContext(writer);
		serializer.serialize(params, context);

		String geoJson = writer.toString();
		assertTrue(geoJson.contains("\"type\":"));
		assertTrue(geoJson.contains("\"FeatureCollection\""));
		assertTrue(geoJson.contains("AdministrativeUnit_10044117"));
		assertFalse(geoJson.contains("geometries"));
	}

	@Test
	public void testSerialize_allowOtherCrs_InfoCrsAndSkipGeometries() throws Exception {
		StringWriter writer = new StringWriter();
		GeoJsonFeatureInfoSerializer serializer = new GeoJsonFeatureInfoSerializer(true, false);
		FeatureInfoParams params = createParams("EPSG:25832");
		FeatureInfoContext context = mockContext(writer);
		serializer.serialize(params, context);

		String geoJson = writer.toString();
		assertTrue(geoJson.contains("\"type\":"));
		assertTrue(geoJson.contains("\"FeatureCollection\""));
		assertTrue(geoJson.contains("AdministrativeUnit_10044117"));
		assertFalse(geoJson.contains("geometries"));
	}

	private FeatureInfoParams createParams() throws Exception {
		return createParams(null);
	}

	private FeatureInfoParams createParams(String crs) throws Exception {
		URL resource = TemplateFeatureInfoSerializer.class.getResource("featurecollection.gml");
		GMLStreamReader gmlStreamReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_32, resource);
		Map<String, String> nsBindings = new HashMap<>();
		FeatureCollection col = gmlStreamReader.readFeatureCollection();
		ICRS infoCrs = crs != null ? CRSManager.lookup(crs) : null;
		return new FeatureInfoParams(nsBindings, col, "text/html", true, null, null, null, infoCrs);
	}

	private FeatureInfoContext mockContext(Writer writer) throws IOException {
		FeatureInfoContext mock = mock(FeatureInfoContext.class);
		when(mock.getWriter()).thenReturn(writer);
		return mock;
	}

}
