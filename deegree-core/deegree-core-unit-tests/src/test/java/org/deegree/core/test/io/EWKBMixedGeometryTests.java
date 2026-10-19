package org.deegree.core.test.io;

import org.apache.commons.io.FilenameUtils;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.ewkb.Axis;
import org.deegree.geometry.io.ewkb.BinaryParser;
import org.deegree.geometry.io.ewkb.BinaryWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class EWKBMixedGeometryTests extends AbstractConversionTests {

	@Parameters(name = "{index}: {2}")
	public static List<Object[]> getTests() throws Exception {
		return getTests("mixed").filter(o -> o[1] != null).toList();
	}

	@Parameter(0)
	public Path eWkbFile;

	@Parameter(1)
	public Path gmlFile;

	@Parameter(2)
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
