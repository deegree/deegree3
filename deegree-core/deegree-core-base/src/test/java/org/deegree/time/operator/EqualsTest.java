package org.deegree.time.operator;

import static java.util.Collections.emptyList;
import static org.deegree.time.position.IndeterminateValue.UNKNOWN;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.deegree.commons.tom.gml.property.Property;
import org.deegree.time.position.TimePosition;
import org.deegree.time.primitive.GenericTimeInstant;
import org.deegree.time.primitive.GenericTimePeriod;
import org.deegree.time.primitive.RelatedTime;
import org.deegree.time.primitive.TimeGeometricPrimitive;
import org.deegree.time.primitive.TimeInstant;
import org.deegree.time.primitive.TimePeriod;
import org.junit.Test;

public class EqualsTest {

	private final Equals equals = new Equals();

	@Test
	public void equals() {
		assertTrue(equals.equals("2014-01-01T00:00:01", "2014-01-01T00:00:01"));
		assertTrue(equals.equals("2014-01-01T00:00:01", "2014-01-01T00:00:01.000"));
		assertFalse(equals.equals("2014-01-01T00:00:01", "2014-01-01T00:00:02"));
		assertFalse(equals.equals("2014-01-01T00:00:01", "2014-01-01T00:00:01.001"));
	}

	@Test
	public void evaluateInstantInstant() {
		assertEquals(null, null);
		assertEquals(instant("00:00:01"), instant("00:00:01"));
		assertEquals(instant("INDETERMINATE"), instant("INDETERMINATE"));
		assertNotEquals(instant("00:00:01"), instant("00:00:02"));
		assertNotEquals(instant("00:00:01"), instant("INDETERMINATE"));
		assertNotEquals(instant("INDETERMINATE"), instant("00:00:01"));
		assertNotEquals(instant("00:00:01"), null);
		assertNotEquals(null, instant("00:00:01"));
	}

	@Test
	public void evaluateInstantPeriod() {
		assertNotEquals(instant("00:00:01"), period("00:00:01", "00:00:01"));
	}

	@Test
	public void evaluatePeriodPeriod() {
		assertEquals(period("00:00:01", "00:00:02"), period("00:00:01", "00:00:02"));
		assertNotEquals(period("00:00:00", "00:00:02"), period("00:00:01", "00:00:02"));
		assertNotEquals(period("00:00:01", "00:00:02"), period("00:00:01", "00:00:03"));
	}

	private void assertEquals(final TimeGeometricPrimitive t1, final TimeGeometricPrimitive t2) {
		assertTrue(equals.evaluate(t1, t2));
	}

	private void assertNotEquals(final TimeGeometricPrimitive t1, final TimeGeometricPrimitive t2) {
		assertFalse(equals.evaluate(t1, t2));
	}

	private TimeInstant instant(final String s) {
		final List<Property> props = emptyList();
		final List<RelatedTime> relatedTimes = emptyList();
		TimePosition pos = null;
		if ("INDETERMINATE".equals(s)) {
			pos = new TimePosition(null, null, UNKNOWN, "");
		}
		else {
			pos = new TimePosition(null, null, null, "2014-01-01T" + s);
		}
		return new GenericTimeInstant(null, props, relatedTimes, null, pos);
	}

	private TimePeriod period(final String t1, final String t2) {
		final TimeInstant begin = instant(t1);
		final TimeInstant end = instant(t2);
		final List<Property> props = emptyList();
		final List<RelatedTime> relatedTimes = emptyList();
		return new GenericTimePeriod(null, props, relatedTimes, null, begin, end);
	}

}
