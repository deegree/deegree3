package org.deegree.core.test.io;

import org.apache.commons.io.FilenameUtils;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.core.test.utils.XMLMemoryStreamWriter;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.ewkb.Axis;
import org.deegree.geometry.io.ewkb.BinaryParser;
import org.deegree.geometry.io.ewkb.BinaryWriter;
import org.deegree.gml.GMLStreamWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.deegree.gml.GMLOutputFactory.createGMLStreamWriter;
import static org.deegree.gml.GMLVersion.GML_32;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class EWKBConversionTests {

	@Parameters(name = "{index}: {2}")
	public static List<Object[]> getTests() throws Exception {
		Path dir = Paths.get("src/test/resources/org/deegree/core/test/io/ewkb");

		return Files.list(dir) //
			.filter(Files::isRegularFile)
			.filter(f -> f.toString().endsWith(".wkb"))
			.map(wkb -> {
				String baseName = FilenameUtils.removeExtension(wkb.getFileName().toString());
				Path gml = wkb.getParent().resolve(baseName + ".gml");
				if (!Files.isRegularFile(gml)) {
					return null;
				}
				return new Object[] { wkb, gml, baseName };
			})
			.filter(Objects::nonNull)
			.toList();
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
		assertArrayEquals("EWKB -> Geom -> EWKB", eWkbBytes, convertedEWkbBytes);
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
		assertArrayEquals("EWKB -> Geom -> EWKB", eWkbBytes, convertedEWkbBytes);
	}

	static String replaceGmlIDs(String text) {
		StringBuffer result = new StringBuffer();
		Pattern pattern = Pattern.compile("gml:id=\"[^\"]+\"");
		Matcher matcher = pattern.matcher(text);
		int id = 0;
		while (matcher.find()) {
			matcher.appendReplacement(result, "gml:id=\"ID_" + (++id) + "\"");
		}
		matcher.appendTail(result);
		return result.toString();
	}

	static byte[] readEWKBGeometry(Path file) throws IOException {
		String hexEWKB = Files.readString(file);
		return HexFormat.of().parseHex(hexEWKB);
	}

	static String writeGMLGeometry(Geometry geom) throws Exception {
		XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
		XMLStreamWriter xmlWriter = new IndentingXMLStreamWriter(memoryWriter.getXMLStreamWriter());
		GMLStreamWriter exporter = createGMLStreamWriter(GML_32, xmlWriter);

		exporter.write(geom);
		return replaceGmlIDs(memoryWriter.toString());
	}

}
