package org.deegree.feature.persistence.cache;

import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.namespace.QName;
import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class BBoxPropertiesCacheTest {

	private static BBoxPropertiesCache CACHE;

	private static final double DELTA = 0.001;

	private static Envelope ENV;

	@BeforeClass
	public static void setUp() throws Exception {
		URL testFileURL = BBoxPropertiesCacheTest.class.getResource("bbox-test-1.properties");
		File testFile = new File(testFileURL.toURI());

		assertTrue("Testfile exists", testFile.exists());
		CACHE = new BBoxPropertiesCache(testFile);

		ENV = (new GeometryFactory()).createEnvelope(10, 11, 12, 13, CRSManager.lookup("EPSG:4326"));

		assertNotNull(CACHE);
		assertNotNull(ENV);
	}

	@Test
	public void testExistingEnvelope() {
		QName single = qname("testa", "FeatureA");
		assertTrue("Found envelope", CACHE.contains(single));
		assertNotNull("Found envelope", CACHE.get(single));
	}

	@Test
	public void testEmptyEnvelope() {
		QName single = qname("testa", "FeatureEmpty");
		assertTrue("Empty envelope", CACHE.contains(single));
		assertNull("Empty envelope", CACHE.get(single));
	}

	@Test
	public void testNonExistingEnvelope() {
		QName single = qname("testa", "FeatureDoesNotExists");
		assertFalse("Does not exists", CACHE.contains(single));
		assertThrows("Get throws exception", IllegalArgumentException.class, () -> CACHE.get(single));
	}

	@Test
	public void testExistingOverwrite() {
		QName single = qname("testa", "FeatureO");
		assertTrue("Found envelope", CACHE.contains(single));
		Envelope first = CACHE.get(single);
		assertNotNull("Found envelope", first);
		assertEquals("MINX = 1", 1.0d, first.getMin().get0(), DELTA);

		CACHE.set(single, ENV);
		Envelope second = CACHE.get(single);
		assertNotNull("Exists", second);
		assertEquals("MINX = 10", 10.0d, second.getMin().get0(), DELTA);
	}

	@Test
	public void testReadOnlySingle() {
		QName single = qname("testb", "FeatureB");
		Envelope first = CACHE.get(single);
		assertNotNull("Exists", first);
		assertEquals("MINX = 1", 1.0d, first.getMin().get0(), DELTA);

		CACHE.set(single, ENV);

		Envelope second = CACHE.get(single);
		assertNotNull("Exists", second);
		assertEquals("MINX = 1", 1.0d, second.getMin().get0(), DELTA);
	}

	@Test
	public void testReadOnlyNamespace() {
		QName single = qname("testc", "FeatureAA");
		Envelope first = CACHE.get(qname("testc", "FeatureAA"));
		assertNotNull("Exists", first);
		assertEquals("MINX = 1", 1.0d, first.getMin().get0(), DELTA);

		CACHE.set(single, ENV);

		Envelope second = CACHE.get(single);
		assertNotNull("Exists", second);
		assertEquals("MINX = 1", 1.0d, second.getMin().get0(), DELTA);
	}

	private static QName qname(String namespaceSuffix, String localName) {
		// noinspection HttpUrlsUsage
		return new QName("http://www.deegree.org/" + namespaceSuffix, localName);
	}

}
