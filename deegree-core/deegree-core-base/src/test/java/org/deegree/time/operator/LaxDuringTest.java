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

public class LaxDuringTest {

	private final LaxDuring laxDuring = new LaxDuring();

	@Test
	public void laxDuringBegins() {
		assertLaxDuring(instant("00:00:02"), period("00:00:02", "00:00:03"));
		assertLaxDuring(instant("00:00:02"), period("00:00:02", null));
		assertLaxDuring(period("00:00:02", "00:00:05"), period("00:00:02", "00:00:06"));
		assertLaxDuring(period("00:00:02", "00:00:05"), period("00:00:02", null));
	}

	@Test
	public void laxDuringBegunBy() {
		assertNotLaxDuring(period("00:00:02", "00:00:05"), instant("00:00:02"));
		assertNotLaxDuring(period("00:00:02", "00:00:05"), period("00:00:02", "00:00:03"));
		assertNotLaxDuring(period("00:00:02", null), instant("00:00:02"));
		assertNotLaxDuring(period("00:00:02", null), period("00:00:02", "00:00:03"));
	}

	@Test
	public void laxDuringEnds() {
		assertLaxDuring(period("00:00:02", "00:00:05"), period("00:00:01", "00:00:05"));
		assertLaxDuring(period("00:00:02", "00:00:05"), period(null, "00:00:05"));
	}

	@Test
	public void laxDuringEndedBy() {
		assertNotLaxDuring(period("00:00:02", "00:00:05"), period("00:00:04", "00:00:05"));
		assertNotLaxDuring(period(null, "00:00:05"), period("00:00:04", "00:00:05"));
	}

	@Test
	public void laxDuringTContains() {
		assertNotLaxDuring(period("00:00:02", "00:00:05"), instant("00:00:03"));
		assertNotLaxDuring(period("00:00:02", "00:00:05"), period("00:00:03", "00:00:04"));
		assertNotLaxDuring(period("00:00:02", null), instant("00:00:03"));
		assertNotLaxDuring(period("00:00:02", null), period("00:00:03", "00:00:04"));
		assertNotLaxDuring(period(null, "00:00:05"), instant("00:00:03"));
		assertNotLaxDuring(period(null, "00:00:05"), period("00:00:03", "00:00:04"));
		assertNotLaxDuring(period(null, null), instant("00:00:03"));
		assertNotLaxDuring(period(null, null), period("00:00:03", "00:00:04"));
	}

	@Test
	public void laxDuringDuring() {
		assertLaxDuring(instant("00:00:02"), period("00:00:01", "00:00:06"));
		assertLaxDuring(instant("00:00:02"), period("00:00:01", null));
		assertLaxDuring(instant("00:00:02"), period(null, "00:00:06"));
		assertLaxDuring(instant("00:00:02"), period(null, null));
		assertLaxDuring(period("00:00:02", "00:00:05"), period("00:00:01", "00:00:06"));
		assertLaxDuring(period("00:00:02", "00:00:05"), period("00:00:01", null));
		assertLaxDuring(period("00:00:02", "00:00:05"), period(null, "00:00:06"));
		assertLaxDuring(period("00:00:02", "00:00:05"), period(null, null));
	}

	@Test
	public void laxDuringTEquals() {
		assertLaxDuring(instant("00:00:02"), instant("00:00:02"));
		assertLaxDuring(period("00:00:02", "00:00:05"), period("00:00:02", "00:00:05"));
		assertLaxDuring(period("00:00:02", null), period("00:00:02", null));
		assertLaxDuring(period(null, "00:00:05"), period(null, "00:00:05"));
		assertLaxDuring(period(null, null), period(null, null));
	}

	@Test
	public void laxDuringTOverlaps() {
		assertNotLaxDuring(period("00:00:02", "00:00:05"), period("00:00:04", "00:00:06"));
		assertNotLaxDuring(period("00:00:02", "00:00:05"), period("00:00:04", null));
		assertNotLaxDuring(period(null, "00:00:05"), period("00:00:04", "00:00:06"));
		assertNotLaxDuring(period(null, "00:00:05"), period("00:00:04", null));
	}

	@Test
	public void laxDuringOverlappedBy() {
		assertNotLaxDuring(period("00:00:02", "00:00:05"), period("00:00:01", "00:00:03"));
		assertNotLaxDuring(period("00:00:02", "00:00:05"), period(null, "00:00:03"));
		assertNotLaxDuring(period("00:00:02", null), period("00:00:01", "00:00:03"));
		assertNotLaxDuring(period("00:00:02", null), period(null, "00:00:03"));
	}

	@Test
	public void laxDuringAfter() {
		assertNotLaxDuring(instant("00:00:02"), instant("00:00:01"));
		assertNotLaxDuring(instant("00:00:02"), period("00:00:00", "00:00:01"));
		assertNotLaxDuring(instant("00:00:02"), period(null, "00:00:01"));
		assertNotLaxDuring(period("00:00:02", "00:00:05"), instant("00:00:01"));
		assertNotLaxDuring(period("00:00:02", "00:00:05"), period("00:00:00", "00:00:01"));
		assertNotLaxDuring(period("00:00:02", "00:00:05"), period(null, "00:00:01"));
		assertNotLaxDuring(period("00:00:02", null), instant("00:00:01"));
		assertNotLaxDuring(period("00:00:02", null), period("00:00:00", "00:00:01"));
		assertNotLaxDuring(period("00:00:02", null), period(null, "00:00:01"));
	}

	@Test
	public void laxDuringBefore() {
		assertNotLaxDuring(instant("00:00:05"), instant("00:00:06"));
		assertNotLaxDuring(instant("00:00:05"), period("00:00:06", "00:00:07"));
		assertNotLaxDuring(instant("00:00:05"), period("00:00:06", null));
		assertNotLaxDuring(period("00:00:02", "00:00:05"), instant("00:00:06"));
		assertNotLaxDuring(period("00:00:02", "00:00:05"), period("00:00:06", "00:00:07"));
		assertNotLaxDuring(period("00:00:02", "00:00:05"), period("00:00:06", null));
		assertNotLaxDuring(period(null, "00:00:05"), instant("00:00:06"));
		assertNotLaxDuring(period(null, "00:00:05"), period("00:00:06", "00:00:07"));
		assertNotLaxDuring(period(null, "00:00:05"), period("00:00:06", null));
	}

	@Test
	public void laxDuringMeets() {
		assertNotLaxDuring(period("00:00:02", "00:00:05"), instant("00:00:05"));
		assertNotLaxDuring(period("00:00:02", "00:00:05"), period("00:00:05", "00:00:06"));
		assertNotLaxDuring(period("00:00:02", "00:00:05"), period("00:00:05", null));
		assertNotLaxDuring(period(null, "00:00:05"), instant("00:00:05"));
		assertNotLaxDuring(period(null, "00:00:05"), period("00:00:05", "00:00:06"));
		assertNotLaxDuring(period(null, "00:00:05"), period("00:00:05", null));
	}

	@Test
	public void laxDuringMetBy() {
		assertNotLaxDuring(instant("00:00:02"), period("00:00:01", "00:00:02"));
		assertNotLaxDuring(instant("00:00:02"), period(null, "00:00:02"));
		assertNotLaxDuring(period("00:00:02", "00:00:05"), period("00:00:01", "00:00:02"));
		assertNotLaxDuring(period("00:00:02", "00:00:05"), period(null, "00:00:02"));
		assertNotLaxDuring(period("00:00:02", null), period("00:00:01", "00:00:02"));
		assertNotLaxDuring(period("00:00:02", null), period(null, "00:00:02"));
	}

	private void assertLaxDuring(TimeGeometricPrimitive a, TimeGeometricPrimitive b) {
		Assert.assertTrue(laxDuring(a, b));
	}

	private void assertNotLaxDuring(TimeGeometricPrimitive a, TimeGeometricPrimitive b) {
		Assert.assertFalse(laxDuring(a, b));
	}

	private boolean laxDuring(TimeGeometricPrimitive a, TimeGeometricPrimitive b) {
		return laxDuring.evaluate(a, b);
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
