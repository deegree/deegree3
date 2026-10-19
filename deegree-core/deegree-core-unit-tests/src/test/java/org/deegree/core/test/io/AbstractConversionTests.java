package org.deegree.core.test.io;

import org.apache.commons.io.FilenameUtils;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.core.test.utils.XMLMemoryStreamWriter;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLStreamWriter;
import org.junit.Assert;

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
import java.util.stream.Stream;

import static org.deegree.gml.GMLOutputFactory.createGMLStreamWriter;
import static org.deegree.gml.GMLVersion.GML_32;

public abstract class AbstractConversionTests {

	static Stream<Object[]> getTests(String subfolder) throws Exception {
		Path dir = Paths.get("src/test/resources/org/deegree/core/test/io/ewkb/", subfolder);

		return Files.list(dir) //
			.filter(Files::isRegularFile)
			.filter(f -> f.toString().endsWith(".wkb"))
			.map(wkb -> {
				String baseName = FilenameUtils.removeExtension(wkb.getFileName().toString());
				Path gml = wkb.getParent().resolve(baseName + ".gml");
				if (!Files.isRegularFile(gml)) {
					gml = null;
				}
				return new Object[] { wkb, gml, baseName };
			})
			.filter(Objects::nonNull);
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
		return AbstractConversionTests.replaceGmlIDs(memoryWriter.toString());
	}

	static void assertHexEquals(String message, byte[] expected, byte[] actual) {
		String exp = expected != null ? HexFormat.of().formatHex(expected) : null;
		String act = expected != null ? HexFormat.of().formatHex(expected) : null;
		Assert.assertEquals(message, exp, act);
	}

}
