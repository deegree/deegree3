package org.deegree.commons.tom.primitive;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.commons.tom.datetime.Time;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class XMLValueManglerTest {

	@Test
	public void testXmlToInternal_boolean() {
		Object o = XMLValueMangler.xmlToInternal("true", BaseType.BOOLEAN);
		assertTrue(o instanceof Boolean);
		assertEquals(Boolean.TRUE, o);
	}

	@Test
	public void testXmlToInternal_emptyBoolean() {
		Object o = XMLValueMangler.xmlToInternal("", BaseType.BOOLEAN);
		assertNull(o);
	}

	@Test
	public void testXmlToInternal_nullBoolean() {
		Object o = XMLValueMangler.xmlToInternal(null, BaseType.BOOLEAN);
		assertNull(o);
	}

	@Test
	public void testXmlToInternal_date() {
		Object o = XMLValueMangler.xmlToInternal("2026-01-29", BaseType.DATE);
		assertTrue(o instanceof Date);
	}

	@Test
	public void testXmlToInternal_emptyDate() {
		Object o = XMLValueMangler.xmlToInternal("", BaseType.DATE);
		assertNull(o);
	}

	@Test
	public void testXmlToInternal_nullDate() {
		Object o = XMLValueMangler.xmlToInternal(null, BaseType.DATE);
		assertNull(o);
	}

	@Test
	public void testXmlToInternal_dateTime() {
		Object o = XMLValueMangler.xmlToInternal("2026-01-29T00:00:00Z", BaseType.DATE_TIME);
		assertTrue(o instanceof DateTime);
	}

	@Test
	public void testXmlToInternal_emptyDateTime() {
		Object o = XMLValueMangler.xmlToInternal("", BaseType.DATE_TIME);
		assertNull(o);
	}

	@Test
	public void testXmlToInternal_nullDateTime() {
		Object o = XMLValueMangler.xmlToInternal(null, BaseType.DATE_TIME);
		assertNull(o);
	}

	@Test
	public void testXmlToInternal_decimal() {
		Object o = XMLValueMangler.xmlToInternal("5.8", BaseType.DECIMAL);
		assertTrue(o instanceof BigDecimal);
		assertEquals(BigDecimal.valueOf(5.8), o);
	}

	@Test
	public void testXmlToInternal_emptyDecimal() {
		Object o = XMLValueMangler.xmlToInternal("", BaseType.DECIMAL);
		assertNull(o);
	}

	@Test
	public void testXmlToInternal_nullDecimal() {
		Object o = XMLValueMangler.xmlToInternal(null, BaseType.DECIMAL);
		assertNull(o);
	}

	@Test
	public void testXmlToInternal_double() {
		Object o = XMLValueMangler.xmlToInternal("5.8", BaseType.DOUBLE);
		assertTrue(o instanceof Double);
		assertEquals(5.8d, o);
	}

	@Test
	public void testXmlToInternal_emptyDouble() {
		Object o = XMLValueMangler.xmlToInternal("", BaseType.DOUBLE);
		assertNull(o);
	}

	@Test
	public void testXmlToInternal_nullDouble() {
		Object o = XMLValueMangler.xmlToInternal(null, BaseType.DOUBLE);
		assertNull(o);
	}

	@Test
	public void testXmlToInternal_integer() {
		Object o = XMLValueMangler.xmlToInternal("5", BaseType.INTEGER);
		assertTrue(o instanceof BigInteger);
		assertEquals(BigInteger.valueOf(5), o);
	}

	@Test
	public void testXmlToInternal_emptyInteger() {
		Object o = XMLValueMangler.xmlToInternal("", BaseType.INTEGER);
		assertNull(o);
	}

	@Test
	public void testXmlToInternal_nullInteger() {
		Object o = XMLValueMangler.xmlToInternal(null, BaseType.INTEGER);
		assertNull(o);
	}

	@Test
	public void testXmlToInternal_string() {
		Object o = XMLValueMangler.xmlToInternal("text", BaseType.STRING);
		assertTrue(o instanceof String);
		assertEquals("text", o);
	}

	@Test
	public void testXmlToInternal_emptyString() {
		Object o = XMLValueMangler.xmlToInternal("", BaseType.STRING);
		assertTrue(o instanceof String);
		assertEquals("", o);
	}

	@Test
	public void testXmlToInternal_nullString() {
		Object o = XMLValueMangler.xmlToInternal(null, BaseType.STRING);
		assertEquals(null, o);
	}

	@Test
	public void testXmlToInternal_time() {
		Object o = XMLValueMangler.xmlToInternal("00:00:00Z", BaseType.TIME);
		assertTrue(o instanceof Time);
	}

	@Test
	public void testXmlToInternal_emptyTime() {
		Object o = XMLValueMangler.xmlToInternal("", BaseType.TIME);
		assertNull(o);
	}

	@Test
	public void testXmlToInternal_nullTime() {
		Object o = XMLValueMangler.xmlToInternal(null, BaseType.TIME);
		assertNull(o);
	}

}
