package org.deegree.time.operator;

import static java.util.Collections.emptyList;
import static org.deegree.time.position.IndeterminateValue.UNKNOWN;

import java.util.List;

import org.deegree.commons.tom.gml.property.Property;
import org.deegree.time.position.TimePosition;
import org.deegree.time.primitive.GenericTimeInstant;
import org.deegree.time.primitive.GenericTimePeriod;
import org.deegree.time.primitive.RelatedTime;
import org.deegree.time.primitive.TimeGeometricPrimitive;
import org.deegree.time.primitive.TimeInstant;
import org.deegree.time.primitive.TimePeriod;
import org.junit.Assert;
import org.junit.Test;

public class AnyInteractsTest {

	private final AnyInteracts anyInteracts = new AnyInteracts();

	@Test
	public void anyInteractsBegins() {
		assertAnyInteracts(instant("00:00:02"), period("00:00:02", "00:00:03"));
		assertAnyInteracts(instant("00:00:02"), period("00:00:02", null));
		assertAnyInteracts(period("00:00:02", "00:00:05"), period("00:00:02", "00:00:06"));
		assertAnyInteracts(period("00:00:02", "00:00:05"), period("00:00:02", null));
	}

	@Test
	public void anyInteractsBegunBy() {
		assertAnyInteracts(period("00:00:02", "00:00:05"), instant("00:00:02"));
		assertAnyInteracts(period("00:00:02", "00:00:05"), period("00:00:02", "00:00:03"));
		assertAnyInteracts(period("00:00:02", null), instant("00:00:02"));
		assertAnyInteracts(period("00:00:02", null), period("00:00:02", "00:00:03"));
	}

	@Test
	public void anyInteractsEnds() {
		assertAnyInteracts(period("00:00:02", "00:00:05"), period("00:00:01", "00:00:05"));
		assertAnyInteracts(period("00:00:02", "00:00:05"), period(null, "00:00:05"));
	}

	@Test
	public void anyInteractsEndedBy() {
		assertAnyInteracts(period("00:00:02", "00:00:05"), period("00:00:04", "00:00:05"));
		assertAnyInteracts(period(null, "00:00:05"), period("00:00:04", "00:00:05"));
	}

	@Test
	public void anyInteractsTContains() {
		assertAnyInteracts(period("00:00:02", "00:00:05"), instant("00:00:03"));
		assertAnyInteracts(period("00:00:02", "00:00:05"), period("00:00:03", "00:00:04"));
		assertAnyInteracts(period("00:00:02", null), instant("00:00:03"));
		assertAnyInteracts(period("00:00:02", null), period("00:00:03", "00:00:04"));
		assertAnyInteracts(period(null, "00:00:05"), instant("00:00:03"));
		assertAnyInteracts(period(null, "00:00:05"), period("00:00:03", "00:00:04"));
		assertAnyInteracts(period(null, null), instant("00:00:03"));
		assertAnyInteracts(period(null, null), period("00:00:03", "00:00:04"));
	}

	@Test
	public void anyInteractsDuring() {
		assertAnyInteracts(instant("00:00:02"), period("00:00:01", "00:00:06"));
		assertAnyInteracts(instant("00:00:02"), period("00:00:01", null));
		assertAnyInteracts(instant("00:00:02"), period(null, "00:00:06"));
		assertAnyInteracts(instant("00:00:02"), period(null, null));
		assertAnyInteracts(period("00:00:02", "00:00:05"), period("00:00:01", "00:00:06"));
		assertAnyInteracts(period("00:00:02", "00:00:05"), period("00:00:01", null));
		assertAnyInteracts(period("00:00:02", "00:00:05"), period(null, "00:00:06"));
		assertAnyInteracts(period("00:00:02", "00:00:05"), period(null, null));
	}

	@Test
	public void anyInteractsTEquals() {
		assertAnyInteracts(instant("00:00:02"), instant("00:00:02"));
		assertAnyInteracts(period("00:00:02", "00:00:05"), period("00:00:02", "00:00:05"));
		assertAnyInteracts(period("00:00:02", null), period("00:00:02", null));
		assertAnyInteracts(period(null, "00:00:05"), period(null, "00:00:05"));
		assertAnyInteracts(period(null, null), period(null, null));
	}

	@Test
	public void anyInteractsTOverlaps() {
		assertAnyInteracts(period("00:00:02", "00:00:05"), period("00:00:04", "00:00:06"));
		assertAnyInteracts(period("00:00:02", "00:00:05"), period("00:00:04", null));
		assertAnyInteracts(period(null, "00:00:05"), period("00:00:04", "00:00:06"));
		assertAnyInteracts(period(null, "00:00:05"), period("00:00:04", null));
	}

	@Test
	public void anyInteractsOverlappedBy() {
		assertAnyInteracts(period("00:00:02", "00:00:05"), period("00:00:01", "00:00:03"));
		assertAnyInteracts(period("00:00:02", "00:00:05"), period(null, "00:00:03"));
		assertAnyInteracts(period("00:00:02", null), period("00:00:01", "00:00:03"));
		assertAnyInteracts(period("00:00:02", null), period(null, "00:00:03"));
	}

	@Test
	public void anyInteractsAfter() {
		assertNotAnyInteracts(instant("00:00:02"), instant("00:00:01"));
		assertNotAnyInteracts(instant("00:00:02"), period("00:00:00", "00:00:01"));
		assertNotAnyInteracts(instant("00:00:02"), period(null, "00:00:01"));
		assertNotAnyInteracts(period("00:00:02", "00:00:05"), instant("00:00:01"));
		assertNotAnyInteracts(period("00:00:02", "00:00:05"), period("00:00:00", "00:00:01"));
		assertNotAnyInteracts(period("00:00:02", "00:00:05"), period(null, "00:00:01"));
		assertNotAnyInteracts(period("00:00:02", null), instant("00:00:01"));
		assertNotAnyInteracts(period("00:00:02", null), period("00:00:00", "00:00:01"));
		assertNotAnyInteracts(period("00:00:02", null), period(null, "00:00:01"));
	}

	@Test
	public void anyInteractsBefore() {
		assertNotAnyInteracts(instant("00:00:05"), instant("00:00:06"));
		assertNotAnyInteracts(instant("00:00:05"), period("00:00:06", "00:00:07"));
		assertNotAnyInteracts(instant("00:00:05"), period("00:00:06", null));
		assertNotAnyInteracts(period("00:00:02", "00:00:05"), instant("00:00:06"));
		assertNotAnyInteracts(period("00:00:02", "00:00:05"), period("00:00:06", "00:00:07"));
		assertNotAnyInteracts(period("00:00:02", "00:00:05"), period("00:00:06", null));
		assertNotAnyInteracts(period(null, "00:00:05"), instant("00:00:06"));
		assertNotAnyInteracts(period(null, "00:00:05"), period("00:00:06", "00:00:07"));
		assertNotAnyInteracts(period(null, "00:00:05"), period("00:00:06", null));
	}

	@Test
	public void anyInteractsMeets() {
		assertNotAnyInteracts(period("00:00:02", "00:00:05"), instant("00:00:05"));
		assertNotAnyInteracts(period("00:00:02", "00:00:05"), period("00:00:05", "00:00:06"));
		assertNotAnyInteracts(period("00:00:02", "00:00:05"), period("00:00:05", null));
		assertNotAnyInteracts(period(null, "00:00:05"), instant("00:00:05"));
		assertNotAnyInteracts(period(null, "00:00:05"), period("00:00:05", "00:00:06"));
		assertNotAnyInteracts(period(null, "00:00:05"), period("00:00:05", null));
	}

	@Test
	public void anyInteractsMetBy() {
		assertNotAnyInteracts(instant("00:00:02"), period("00:00:01", "00:00:02"));
		assertNotAnyInteracts(instant("00:00:02"), period(null, "00:00:02"));
		assertNotAnyInteracts(period("00:00:02", "00:00:05"), period("00:00:01", "00:00:02"));
		assertNotAnyInteracts(period("00:00:02", "00:00:05"), period(null, "00:00:02"));
		assertNotAnyInteracts(period("00:00:02", null), period("00:00:01", "00:00:02"));
		assertNotAnyInteracts(period("00:00:02", null), period(null, "00:00:02"));
	}

	private void assertAnyInteracts(TimeGeometricPrimitive a, TimeGeometricPrimitive b) {
		Assert.assertTrue(anyInteracts(a, b));
	}

	private void assertNotAnyInteracts(TimeGeometricPrimitive a, TimeGeometricPrimitive b) {
		Assert.assertFalse(anyInteracts(a, b));
	}

	private boolean anyInteracts(TimeGeometricPrimitive a, TimeGeometricPrimitive b) {
		return anyInteracts.evaluate(a, b);
	}

	private TimeInstant instant(final String s) {
		final List<Property> props = emptyList();
		final List<RelatedTime> relatedTimes = emptyList();
		TimePosition pos = null;
		if (s == null) {
			pos = new TimePosition(null, null, UNKNOWN, "");
		}
		else {
			pos = new TimePosition(null, null, null, s);
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
