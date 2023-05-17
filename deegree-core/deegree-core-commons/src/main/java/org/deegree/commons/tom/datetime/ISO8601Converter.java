/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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

import static java.util.Calendar.MILLISECOND;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

/**
 * Converts between <a href="http://en.wikipedia.org/wiki/ISO_8601">ISO 8601:2004</a>
 * encodings and deegree's temporal primitives.
 * <p>
 * Currently, not the full lexical space of ISO 8601 is supported, but only the subset
 * that's used by the following XML schema types:
 * <ul>
 * <li><code>xs:date</code></li>
 * <li><code>xs:dateTime</code></li>
 * <li><code>xs:time</code></li>
 * <li><code>xs:duration</code></li>
 * </ul>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 */
public final class ISO8601Converter {

	private static final String ISO_8601_2004_FORMAT_TIME = "HH:mm:ss.SSS";

	private static final String ISO_8601_2004_FORMAT_TIME_NO_MS = "HH:mm:ss";

	private static final String ISO_8601_2004_FORMAT_DATE = "yyyy-MM-dd";

	private static final String ISO_8601_2004_FORMAT_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss.SSS";

	private static final String ISO_8601_2004_FORMAT_DATE_TIME_NO_MS = "yyyy-MM-dd'T'HH:mm:ss";

	private final static TimeZone GMT = TimeZone.getTimeZone("GMT");

	/**
	 * Parses the given <code>xs:date</code> string.
	 * @param xsDate the <code>xs:date</code> to be parsed, must not be <code>null</code>
	 * @return the parsed date, never <code>null</code> (available timezone information is
	 * kept)
	 * @throws IllegalArgumentException if parameter does not conform to lexical value
	 * space defined in XML Schema Part 2: Datatypes for <code>xs:date</code>
	 */
	public static Date parseDate(final String xsDate) throws IllegalArgumentException {
		Calendar cal = DatatypeConverter.parseDate(xsDate);
		boolean isTimeZoneUnknown = isLocal(xsDate);
		return new org.deegree.commons.tom.datetime.Date(cal, isTimeZoneUnknown);
	}

	/**
	 * Parses the given <code>xs:time</code> string.
	 * @param xsTime the <code>xs:time</code> to be parsed, must not be <code>null</code>
	 * @return the parsed date, never <code>null</code> (available timezone information is
	 * kept)
	 * @throws IllegalArgumentException if parameter does not conform to lexical value
	 * space defined in XML Schema Part 2: Datatypes for <code>xs:date</code>
	 */
	public static Time parseTime(final String xsTime) throws IllegalArgumentException {
		Calendar cal = DatatypeConverter.parseTime(xsTime);
		boolean isTimeZoneUnknown = isLocal(xsTime);
		return new Time(cal, isTimeZoneUnknown);
	}

	/**
	 * Parses the given <code>xs:dateTime</code> string.
	 * @param xsDateTime the <code>xs:dateTime</code> to be parsed, must not be
	 * <code>null</code>
	 * @return the parsed date, never <code>null</code> (available timezone information is
	 * kept)
	 * @throws IllegalArgumentException if parameter does not conform to lexical value
	 * space defined in XML Schema Part 2: Datatypes for <code>xs:dateTime</code>
	 */
	public static DateTime parseDateTime(final String xsDateTime) throws IllegalArgumentException {
		Calendar cal = DatatypeConverter.parseDateTime(xsDateTime);
		boolean isTimeZoneUnknown = isLocal(xsDateTime);
		return new DateTime(cal, isTimeZoneUnknown);
	}

	private static boolean isLocal(final String s) {
		if (s.endsWith("Z")) {
			return false;
		}
		int len = s.length();
		if (len < 6) {
			return true;
		}
		if (s.charAt(len - 3) == ':' && (s.charAt(len - 6) == '-') || (s.charAt(len - 6) == '+')) {
			return false;
		}
		return true;
	}

	/**
	 * Parses ISO8601 duration strings like P1Y2MT5H, PT5M
	 * @param duration
	 * @return a new duration
	 * @throws ParseException
	 */
	public static Duration parseDuration(final String duration) throws ParseException {
		// Example: P2Y3M14DT10H23M42S
		//
		// Group Optional Field Description
		// ----- -------- --------- ------------------------------------------
		// 1 no P
		// 2 yes 2Y
		// 3 yes 2 years
		// 4 yes 3M
		// 5 yes 3 month
		// 6 yes 14 D
		// 7 yes 14 days
		// 8 yes T
		// 9 yes 10H
		// 10 yes 10 hours
		// 11 yes 23M
		// 12 yes 23 minutes
		// 13 yes 42S
		// 14 yes 42 seconds

		final String regex = "^(P((\\d+)Y)?((\\d+)M)?((\\d+)D)?(T((\\d+)H)?((\\d+)M)?((\\d+)S)?)?)$";
		final Pattern pattern = Pattern.compile(regex);
		final Matcher matcher = pattern.matcher(duration);
		if (!matcher.matches()) {
			throw new ParseException("error while parsing iso8601 date: " + duration, 0);
		}

		int years = getIntFromMatcher(matcher, 3);
		int months = getIntFromMatcher(matcher, 5);
		int days = getIntFromMatcher(matcher, 7);
		int hours = getIntFromMatcher(matcher, 10);
		int minutes = getIntFromMatcher(matcher, 12);
		int seconds = getIntFromMatcher(matcher, 14);

		return new Duration(years, months, days, hours, minutes, seconds);
	}

	private static int getIntFromMatcher(Matcher matcher, int group) {
		String value = matcher.group(group);
		if (value == null) {
			return 0;
		}
		return Integer.parseInt(value);
	}

	/**
	 * Returns an encoding of the given {@link Temporal} that complies with
	 * <code>xs:date</code>.
	 * <p>
	 * The returned format is <code<YYYY-MM-DD[TZ]</code>. The timezone is used from the
	 * {@link Temporal} object, if time zone is unknown
	 * {@link Temporal#isTimeZoneUnknown()}, no time zone information is appended (= local
	 * time).
	 * </p>
	 * @param date point in time to be encoded, must not be <code>null</code>
	 * @return encoded <code>xs:date</code>, never <code>null</code>
	 */
	public static String formatDate(Temporal date) {

		TimeZone tz = date.getCalendar().getTimeZone();
		SimpleDateFormat sdf = new SimpleDateFormat(ISO_8601_2004_FORMAT_DATE);

		sdf.setTimeZone(tz);
		String s = sdf.format(date.getDate());

		if (!date.isTimeZoneUnknown()) {
			s += getTzString(tz, date.getTimeInMilliseconds());
		}
		return s;
	}

	/**
	 * Returns an encoding of the given {@link Temporal} that complies with
	 * <code>xs:time</code>.
	 * <p>
	 * The returned format is <code>hh:mm:ss[.SSS][TZ]</code>. The timezone is used from
	 * the {@link Temporal} object, if time zone is unknown
	 * {@link Temporal#isTimeZoneUnknown()}, no time zone information is appended (= local
	 * time).
	 * </p>
	 * @param date point in time to be encoded, must not be <code>null</code>
	 * @return encoded <code>xs:date</code>, never <code>null</code>
	 */
	public static String formatTime(Temporal date) {

		TimeZone tz = date.getCalendar().getTimeZone();
		SimpleDateFormat sdf = null;
		if (date.getCalendar().get(MILLISECOND) == 0) {
			sdf = new SimpleDateFormat(ISO_8601_2004_FORMAT_TIME_NO_MS);
		}
		else {
			sdf = new SimpleDateFormat(ISO_8601_2004_FORMAT_TIME);
		}

		sdf.setTimeZone(tz);
		String s = sdf.format(date.getDate());

		if (!date.isTimeZoneUnknown()) {
			s += getTzString(tz, date.getTimeInMilliseconds());
		}
		return s;
	}

	/**
	 * Returns an encoding of the given {@link Temporal} that complies with
	 * <code>xs:dateTime</code>.
	 * <p>
	 * The returned format is <code<YYYY-MM-DDThh:mm:ss[.SSS][TZ]</code>. The timezone is
	 * used from the {@link Temporal} object, if time zone is unknown
	 * {@link Temporal#isTimeZoneUnknown()}, no time zone information is appended (= local
	 * time).
	 * </p>
	 * @param date point in time to be encoded, must not be <code>null</code>
	 * @return encoded <code>xs:dateTime</code>, never <code>null</code>
	 */
	public static String formatDateTime(Temporal date) {

		TimeZone tz = date.getCalendar().getTimeZone();
		SimpleDateFormat sdf = null;
		if (date.getCalendar().get(MILLISECOND) == 0) {
			sdf = new SimpleDateFormat(ISO_8601_2004_FORMAT_DATE_TIME_NO_MS);
		}
		else {
			sdf = new SimpleDateFormat(ISO_8601_2004_FORMAT_DATE_TIME);
		}

		sdf.setTimeZone(tz);
		String s = sdf.format(date.getDate());

		if (!date.isTimeZoneUnknown()) {
			s += getTzString(tz, date.getTimeInMilliseconds());
		}
		return s;
	}

	/**
	 * Returns an encoding of the given {@link Duration} that complies with
	 * <code>xs:duration</code>.
	 * @param duration to be encoded, must not be <code>null</code>
	 * @return encoded <code>xs:duration</code>, never <code>null</code>
	 */
	public static String formatDuration(final Duration duration) {

		StringBuilder result = new StringBuilder("P");
		if (duration.getYears() > 0) {
			result.append(duration.getYears()).append('Y');
		}
		if (duration.getMonths() > 0) {
			result.append(duration.getMonths()).append('M');
		}
		if (duration.getDays() > 0) {
			result.append(duration.getDays()).append('D');
		}
		if (duration.getHours() > 0 || duration.getMinutes() > 0 || duration.getSeconds() > 0) {
			result.append('T');
			if (duration.getHours() > 0) {
				result.append(duration.getHours()).append('H');
			}
			if (duration.getMinutes() > 0) {
				result.append(duration.getMinutes()).append('M');
			}
			if (duration.getSeconds() > 0) {
				result.append(duration.getSeconds()).append('S');
			}
		}
		return result.toString();
	}

	private static String getTzString(TimeZone tz, long date) {
		int offset = tz.getOffset(date);
		if (offset == 0) {
			return "Z";
		}

		String sign = "+";
		int offsetHours = 0;
		int offsetMin = 0;

		if (offset > 0) {
			offsetHours = offset / 3600000;
			offsetMin = offset % 3600000;
		}
		else {
			offsetHours = -offset / 3600000;
			offsetMin = -offset % 3600000;
		}

		String offsetHourString = offsetHours >= 10 ? "" + offsetHours : "0" + offsetHours;
		String offsetMinString = offsetMin >= 10 ? "" + offsetMin : "0" + offsetMin;

		return sign + offsetHourString + ":" + offsetMinString;
	}

	@Deprecated
	public static String formatDate(final java.util.Date date) {
		return formatDate(new org.deegree.commons.tom.datetime.Date(date, GMT));
	}

	@Deprecated
	public static String formatDateTime(java.util.Date date) {
		return formatDateTime(new DateTime(date, GMT));
	}

	private ISO8601Converter() {
		// Prevent instantiation
	}

}