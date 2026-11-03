package org.deegree.core.test.io;

import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.ewkb.Axis;
import org.deegree.geometry.io.ewkb.BinaryParser;
import org.deegree.geometry.io.ewkb.BinaryWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class EWKBEmptyCollectionTests extends AbstractConversionTests {

	@Parameterized.Parameters(name = "{index}: {2}")
	public static List<Object[]> getTests() throws Exception {
		return getTests("empty-collection").filter(o -> o[1] != null).toList();
	}

	@Parameterized.Parameter(0)
	public Path eWkbFile;

	@Parameterized.Parameter(1)
	public Path gmlFile;

	@Parameterized.Parameter(2)
	public String testName;

	@Test
	public void testConvertEWKBToGeometryAndBackAuto() throws Exception {
		final String gmlText = Files.readString(gmlFile);
		final byte[] eWkbBytes = readEWKBGeometry(eWkbFile);

		Geometry parsedGeom = BinaryParser.parse(Axis.AUTO, BinaryParser.SRID_TO_NONE, eWkbBytes, 0);
		String parsedText = writeGMLGeometry(parsedGeom);

		assertNotNull("EWKB -> Geom (Geometry)", parsedGeom);
		assertNotNull("EWKB -> Geom (GML)", parsedText);
		assertEquals("EWKB -> Geom", gmlText, parsedText);

		byte[] convertedEWkbBytes = BinaryWriter.writeBinary(Axis.AUTO, parsedGeom, -1);
		assertNotNull("EWKB -> Geom -> EWKB (bytes)", parsedText);
		assertHexEquals("EWKB -> Geom -> EWKB", eWkbBytes, convertedEWkbBytes);
	}

	@Test
	public void testConvertEWKBToGeometryAndBackXY() throws Exception {
		final String gmlText = Files.readString(gmlFile);
		final byte[] eWkbBytes = readEWKBGeometry(eWkbFile);

		Geometry parsedGeom = BinaryParser.parse(Axis.XY, BinaryParser.SRID_TO_NONE, eWkbBytes, 0);
		String parsedText = writeGMLGeometry(parsedGeom);

		assertNotNull("EWKB -> Geom (Geometry)", parsedGeom);
		assertNotNull("EWKB -> Geom (GML)", parsedText);
		assertEquals("EWKB -> Geom", gmlText, parsedText);

		byte[] convertedEWkbBytes = BinaryWriter.writeBinary(Axis.XY, parsedGeom, -1);
		assertNotNull("EWKB -> Geom -> EWKB (bytes)", parsedText);
		assertHexEquals("EWKB -> Geom -> EWKB", eWkbBytes, convertedEWkbBytes);
	}

}
