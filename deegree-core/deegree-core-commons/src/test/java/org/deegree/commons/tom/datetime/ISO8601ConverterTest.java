/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.commons.tom.datetime;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.YEAR;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.deegree.commons.tom.datetime.ISO8601Converter.formatDate;
import static org.deegree.commons.tom.datetime.ISO8601Converter.formatDateTime;
import static org.deegree.commons.tom.datetime.ISO8601Converter.formatDuration;
import static org.deegree.commons.tom.datetime.ISO8601Converter.parseDate;
import static org.deegree.commons.tom.datetime.ISO8601Converter.parseDateTime;
import static org.deegree.commons.tom.datetime.ISO8601Converter.parseDuration;

import java.text.ParseException;
import java.util.TimeZone;

import org.junit.Test;

/**
 * Test cases for {@link ISO8601Converter}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class ISO8601ConverterTest {

	private final static TimeZone GMT = TimeZone.getTimeZone("GMT");

	@Test
	public void testDateUtc() {
		Date dt = parseDate("2002-05-30Z");
		assertFalse(dt.isTimeZoneUnknown());
		assertTestDate(dt);
		assertEquals(0, dt.getCalendar().get(HOUR_OF_DAY));
		assertEquals(0, dt.getCalendar().get(MINUTE));
		assertEquals(0, dt.getCalendar().get(SECOND));
		assertEquals(0, dt.getCalendar().get(MILLISECOND));
		assertEquals(0, dt.getCalendar().getTimeZone().getRawOffset());

		assertEquals(1022716800000L, dt.getTimeInMilliseconds());
		assertEquals(1022716800000L, dt.getDate().getTime());

		assertEquals("2002-05-30Z", formatDate(dt));
		assertEquals("2002-05-30T00:00:00Z", formatDateTime(dt));
	}

	@Test
	public void testDateWithOffset() {
		Date dt = parseDate("2002-05-30+01:00");
		assertFalse(dt.isTimeZoneUnknown());
		assertTestDate(dt);
		assertEquals(0, dt.getCalendar().get(HOUR_OF_DAY));
		assertEquals(0, dt.getCalendar().get(MINUTE));
		assertEquals(0, dt.getCalendar().get(SECOND));
		assertEquals(0, dt.getCalendar().get(MILLISECOND));
		assertEquals(3600000, dt.getCalendar().getTimeZone().getRawOffset());
		assertEquals(1022713200000L, dt.getTimeInMilliseconds());
		assertEquals(1022713200000L, dt.getDate().getTime());

		assertEquals("2002-05-30+01:00", formatDate(dt));
		assertEquals("2002-05-30T00:00:00+01:00", formatDateTime(dt));
		assertEquals("2002-05-29Z", formatDate(dt.toTimeZone(GMT)));
	}

	@Test
	public void testDateLocalTime() {
		Date dt = parseDate("2002-05-30");
		assertTrue(dt.isTimeZoneUnknown());
		assertTestDate(dt);
		assertEquals(0, dt.getCalendar().get(HOUR_OF_DAY));
		assertEquals(0, dt.getCalendar().get(MINUTE));
		assertEquals(0, dt.getCalendar().get(SECOND));
		assertEquals(0, dt.getCalendar().get(MILLISECOND));
		int offset = TimeZone.getDefault().getOffset(dt.getTimeInMilliseconds());
		assertEquals(1022716800000L - offset, dt.getTimeInMilliseconds());
		assertEquals(1022716800000L - offset, dt.getDate().getTime());
		assertEquals("2002-05-30", formatDate(dt));
		assertEquals("2002-05-30T00:00:00", formatDateTime(dt));
	}

	@Test
	public void testDateTimeUtc() {
		DateTime dt = parseDateTime("2002-05-30T09:00:00Z");
		assertFalse(dt.isTimeZoneUnknown());
		assertTestDate(dt);
		assertEquals(9, dt.getCalendar().get(HOUR_OF_DAY));
		assertEquals(0, dt.getCalendar().get(MINUTE));
		assertEquals(0, dt.getCalendar().get(SECOND));
		assertEquals(0, dt.getCalendar().get(MILLISECOND));
		assertEquals(0, dt.getCalendar().getTimeZone().getRawOffset());

		assertEquals(1022749200000L, dt.getTimeInMilliseconds());
		assertEquals(1022749200000L, dt.getDate().getTime());

		assertEquals("2002-05-30T09:00:00Z", dt.toString());
		assertEquals("2002-05-30T09:00:00Z", formatDateTime(dt));
	}

	@Test
	public void testDateTimeWithOffset() {
		DateTime dt = parseDateTime("2002-05-30T09:00:00+01:00");
		assertFalse(dt.isTimeZoneUnknown());
		assertTestDate(dt);
		assertEquals(9, dt.getCalendar().get(HOUR_OF_DAY));
		assertEquals(0, dt.getCalendar().get(MINUTE));
		assertEquals(0, dt.getCalendar().get(SECOND));
		assertEquals(0, dt.getCalendar().get(MILLISECOND));
		assertEquals(3600000, dt.getCalendar().getTimeZone().getRawOffset());
		assertEquals(1022745600000L, dt.getTimeInMilliseconds());
		assertEquals(1022745600000L, dt.getDate().getTime());

		assertEquals("2002-05-30T09:00:00+01:00", dt.toString());
		assertEquals("2002-05-30T08:00:00Z", formatDateTime(dt.toTimeZone(GMT)));
	}

	@Test
	public void testDateTimeLocalTime() {
		DateTime dt = parseDateTime("2002-05-30T09:00:00");
		assertTrue(dt.isTimeZoneUnknown());
		assertTestDate(dt);
		assertEquals(9, dt.getCalendar().get(HOUR_OF_DAY));
		assertEquals(0, dt.getCalendar().get(MINUTE));
		assertEquals(0, dt.getCalendar().get(SECOND));
		assertEquals(0, dt.getCalendar().get(MILLISECOND));
		int offset = TimeZone.getDefault().getOffset(dt.getTimeInMilliseconds());
		assertEquals(1022749200000L - offset, dt.getTimeInMilliseconds());
		assertEquals(1022749200000L - offset, dt.getDate().getTime());
		assertEquals("2002-05-30T09:00:00", formatDateTime(dt));
	}

	@Test
	public void testDuration() throws ParseException {

		Duration duration = parseDuration("P1Y5M5DT3H90M");
		assertEquals(1, duration.getYears());
		assertEquals(5, duration.getMonths());
		assertEquals(5, duration.getDays());
		assertEquals(3, duration.getHours());
		assertEquals(90, duration.getMinutes());

		assertEquals("P1Y5M5DT3H90M", formatDuration(duration));

		DateTime dt = parseDateTime("2002-05-30T09:00:00");
		DateTime after = duration.getEnd(dt);
		assertEquals("2003-11-04T13:30:00", formatDateTime(after));

		DateTime begin = duration.getBegin(after);
		assertEquals("2002-05-30T09:00:00", formatDateTime(begin));
	}

	private static void assertTestDate(Temporal dt) {
		assertEquals(2002, dt.getCalendar().get(YEAR));
		// month is 0-based
		assertEquals(4, dt.getCalendar().get(MONTH));
		assertEquals(30, dt.getCalendar().get(DAY_OF_MONTH));
	}

}
