package org.deegree.sqldialect.oracle.sdo;

import static org.deegree.sqldialect.oracle.sdo.SDOGeometryConverterExampleTests.loadFromFile;
import static org.deegree.sqldialect.oracle.sdo.SDOGeometryConverterExampleTests.writeGMLGeometry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Scanner;
import org.deegree.geometry.Geometry;
import org.junit.Test;

public class SDOGeometryConverterOrientedTest {

	private File sampleSdo = new File("src/test/resources/test/oracle/sdo/oriented/MultiPoint.sdo");

	private File sampleGml = new File("src/test/resources/test/oracle/sdo/oriented/MultiPoint.gml.oriented");

	@Test
	public void testWithOrientationExported() throws Exception {
		SDOGeometryConverter converter = new SDOGeometryConverter();
		converter.setExportOrientedPointAsExtra(true);

		SDOGeometry sdo = loadFromFile(sampleSdo);

		@SuppressWarnings("resource")
		String geomString = new Scanner(sampleGml).useDelimiter("\\Z").next().replace("\r", "").trim();

		Geometry sdoGeom = converter.toGeometry(sdo, null);

		String sdoGeomString = writeGMLGeometry(sdoGeom).replace("\r", "").trim();

		assertNotNull("Convertable 1", sdoGeomString);
		assertNotNull("Convertable 2", geomString);
		assertEquals("SDO -> Geom", geomString, sdoGeomString);
	}

}
