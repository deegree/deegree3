package org.deegree.sqldialect;

import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.cs.CRSUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.EWKTReader;
import org.deegree.geometry.io.ewkb.BinaryParser;
import org.deegree.geometry.io.ewkb.BinaryWriter;
import org.deegree.geometry.io.ewkb.Axis;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.fail;

public class EWKBReaderTest {

	private final static Logger LOG = LoggerFactory.getLogger(EWKBReaderTest.class);

	public static void main(String[] args) {
		String url = "jdbc:postgresql://localhost:5432/naspg";
		String user = "loader";
		String password = "3ZOs8qPlLOAEnrPcF5BO";

		String sql = "SELECT i, d, ST_AsEWKB(g) , ST_AsEWKT(g) FROM t1 ";

		ICRS crs = CRSUtils.getAxisAwareCrs("EPSG:25832");
		EWKTReader rdr = new EWKTReader();
		rdr.setFlags(EnumSet.of(EWKTReader.WKTFlag.USE_CURVE_INSTEADOF_COMPOSITE_CURVE));

		HexFormat fmt = HexFormat.of();

		try (Connection conn = DriverManager.getConnection(url, user, password);
				PreparedStatement pstmt = conn.prepareStatement(sql);
				PreparedStatement ptxt = conn.prepareStatement("SELECT ST_AsEWKT(?::geometry)::text ");
				ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				// Beispielsweiser Zugriff auf die Spalten (Datentypen ggf. anpassen)
				int i = rs.getInt(1);
				String d = rs.getString(2);
				byte[] ewkb = rs.getBytes(3);
				String ewkt = rs.getString(4);

				Geometry ewkbGeom = BinaryParser.parse(Axis.XY, BinaryParser.SRID_TO_NONE, ewkb, 0);
				Geometry ewktGeom = rdr.read(ewkt);
				// ewktGeom.setCoordinateSystem(crs);

				String ewkbGeomText = writeGMLGeometry(ewkbGeom);
				String ewktGeomText = writeGMLGeometry(ewktGeom);

				if (ewkbGeomText.equals(ewktGeomText)) {
					LOG.warn("ROW: [{}] {} [{}] {}", i, d, ewkbGeomText.equals(ewktGeomText));
				}
				else {
					LOG.warn("ROW: [{}] {} [{}] {} \n {} \n {} \n {}", i, d, ewkbGeom.getClass(),
							ewkbGeomText.equals(ewktGeomText), ewkt, ewkbGeomText, ewktGeomText);
				}

				byte[] ewkbOut = BinaryWriter.writeBinary(Axis.XY, ewkbGeom, -1);
				String hexEwkbIn = fmt.formatHex(ewkb);
				String hexEwkbOut = fmt.formatHex(ewkbOut);

				if (hexEwkbIn.equals(hexEwkbOut)) {
					LOG.warn("  -> EWKB -> Geom -> EWKB = true");
				}
				else {
					LOG.warn("  -> EWKB -> Geom -> EWKB = false");
					LOG.warn("     IN:  {}", hexEwkbIn);
					LOG.warn("          {}", ewkt);
					LOG.warn("     OUT: {}", hexEwkbOut);
					ptxt.setString(1, hexEwkbOut);
					try {
						ResultSet rsTxt = ptxt.executeQuery();
						if (rsTxt.next()) {
							LOG.warn("          {}", rsTxt.getString(1));
						}
					}
					catch (Exception e) {
						LOG.warn("     OUT: {}", e.getMessage());
					}
				}

			}

		}
		catch (Exception e) {
			LOG.error("Exception: {}", e.getMessage());
			LOG.error("Exception", e);
		}

	}

	private static String writeGMLGeometry2(Geometry geom) throws Exception {
		XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
		XMLStreamWriter xmlWriter = new IndentingXMLStreamWriter(memoryWriter.getXMLStreamWriter());
		GMLStreamWriter exporter = GMLOutputFactory.createGMLStreamWriter(GMLVersion.GML_2, xmlWriter);

		exporter.write(geom);
		return replaceGmlIDs(memoryWriter.toString());
	}

	private static String writeGMLGeometry(Geometry geom) throws Exception {
		XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
		XMLStreamWriter xmlWriter = new IndentingXMLStreamWriter(memoryWriter.getXMLStreamWriter());
		GMLStreamWriter exporter = GMLOutputFactory.createGMLStreamWriter(GMLVersion.GML_32, xmlWriter);

		exporter.write(geom);
		return replaceGmlIDs(memoryWriter.toString());
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

	static class XMLMemoryStreamWriter {

		private static final Logger LOG = LoggerFactory.getLogger(XMLMemoryStreamWriter.class);

		private XMLStreamWriter xmlWriter;

		private StringWriter writer;

		/**
		 * COPY FROM {@link org.deegree.junit.XMLMemoryStreamWriter#getXMLStreamWriter()
		 */
		public XMLStreamWriter getXMLStreamWriter() {
			if (xmlWriter == null) {
				writer = new StringWriter();
				XMLOutputFactory factory = XMLOutputFactory.newInstance();
				factory.setProperty("javax.xml.stream.isRepairingNamespaces", Boolean.TRUE);
				try {
					xmlWriter = factory.createXMLStreamWriter(writer);
				}
				catch (XMLStreamException e) {
					fail("error while creating the xml writer: " + e.getMessage());
				}
			}
			return xmlWriter;
		}

		/**
		 * COPY FROM {@link org.deegree.junit.XMLMemoryStreamWriter#getReader())
		 */
		public Reader getReader() {
			if (xmlWriter != null) {
				try {
					xmlWriter.flush();
					xmlWriter.close();
					xmlWriter = null;
					writer.close();
				}
				catch (IOException e) {
					throw new RuntimeException("error while closing StringWriter: " + e.getMessage());
				}
				catch (XMLStreamException e) {
					throw new RuntimeException("error while closing XMLStreamWriter: " + e.getMessage());
				}
			}
			return new StringReader(writer.getBuffer().toString());
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			BufferedReader reader = new BufferedReader(getReader());
			String line = null;
			try {
				while ((line = reader.readLine()) != null) {
					sb.append(line);
					sb.append('\n');
				}
			}
			catch (IOException e) {
				LOG.info(e.getMessage());
			}
			return sb.toString();
		}

	}

}
