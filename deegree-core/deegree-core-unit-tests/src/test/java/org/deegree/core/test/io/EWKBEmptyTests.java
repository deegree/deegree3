package org.deegree.core.test.io;

import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.ewkb.Axis;
import org.deegree.geometry.io.ewkb.BinaryParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertNull;

@RunWith(Parameterized.class)
public class EWKBEmptyTests extends AbstractConversionTests {

	@Parameterized.Parameters(name = "{index}: {2}")
	public static List<Object[]> getTests() throws Exception {
		return getTests("empty").toList();
	}

	@Parameterized.Parameter(0)
	public Path eWkbFile;

	@Parameterized.Parameter(1)
	public Path gmlFile;

	@Parameterized.Parameter(2)
	public String testName;

	@Test
	public void testConvertEmptyToNullAuto() throws Exception {
		assertNull("No geometry for EMPTY type", gmlFile);
		final byte[] eWkbBytes = readEWKBGeometry(eWkbFile);

		Geometry parsedGeom = BinaryParser.parse(Axis.AUTO, BinaryParser.SRID_TO_NONE, eWkbBytes, 0);

		assertNull("EWKB -> Null (Geometry)", parsedGeom);
	}

	@Test
	public void testConvertEmptyToNullXY() throws Exception {
		assertNull("No geometry for EMPTY type", gmlFile);
		final byte[] eWkbBytes = readEWKBGeometry(eWkbFile);

		Geometry parsedGeom = BinaryParser.parse(Axis.XY, BinaryParser.SRID_TO_NONE, eWkbBytes, 0);

		assertNull("EWKB -> Null (Geometry)", parsedGeom);
	}

}
