/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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

import static org.deegree.commons.tom.datetime.ISO8601Converter.formatDuration;

import java.util.Calendar;

/**
 * Represents a temporal duration (e.g. <code>xs:duration</code>).
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 */
public class Duration {

	private final int years;

	private final int months;

	private final int days;

	private final int hours;

	private final int minutes;

	private final int seconds;

	/**
	 * Creates a new {@link Duration} instance.
	 * @param years
	 * @param months
	 * @param days
	 * @param hours
	 * @param minutes
	 * @param seconds
	 */
	public Duration(int years, int months, int days, int hours, int minutes, int seconds) {
		this.years = years;
		this.months = months;
		this.days = days;
		this.hours = hours;
		this.minutes = minutes;
		this.seconds = seconds;
	}

	/**
	 * @return the days
	 */
	public int getDays() {
		return days;
	}

	/**
	 * @return the hours
	 */
	public int getHours() {
		return hours;
	}

	/**
	 * @return the minutes
	 */
	public int getMinutes() {
		return minutes;
	}

	/**
	 * @return the months
	 */
	public int getMonths() {
		return months;
	}

	/**
	 * @return the seconds
	 */
	public int getSeconds() {
		return seconds;
	}

	/**
	 * @return the years
	 */
	public int getYears() {
		return years;
	}

	/**
	 * Returns the point in time that is at the beginning of this duration (relative to
	 * the given {@link DateTime}.
	 * @param end end of the duration interval, must not be <code>null</code>
	 * @return the point in time that marks the begin of the duration interval, never
	 * <code>null</code>
	 */
	public DateTime getBegin(DateTime end) {
		Calendar before = Calendar.getInstance(end.getCalendar().getTimeZone());
		before.setTime(end.getDate());
		before.add(Calendar.YEAR, -years);
		before.add(Calendar.MONTH, -months);
		before.add(Calendar.DAY_OF_MONTH, -days);
		before.add(Calendar.HOUR_OF_DAY, -hours);
		before.add(Calendar.MINUTE, -minutes);
		before.add(Calendar.SECOND, -seconds);
		return new DateTime(before, end.isTimeZoneUnknown());
	}

	/**
	 * Returns the point in time that is at the end of this duration (relative to the
	 * given {@link DateTime}.
	 * @param begin begin of the duration interval, must not be <code>null</code>
	 * @return the point in time that marks the end of the duration interval, never
	 * <code>null</code>
	 */
	public DateTime getEnd(DateTime begin) {
		Calendar after = Calendar.getInstance(begin.getCalendar().getTimeZone());
		after.setTime(begin.getDate());
		after.add(Calendar.YEAR, years);
		after.add(Calendar.MONTH, months);
		after.add(Calendar.DAY_OF_MONTH, days);
		after.add(Calendar.HOUR_OF_DAY, hours);
		after.add(Calendar.MINUTE, minutes);
		after.add(Calendar.SECOND, seconds);
		return new DateTime(after, begin.isTimeZoneUnknown());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Duration)) {
			return false;
		}
		Duration that = (Duration) obj;
		return this.years == that.years && this.months == that.months && this.days == that.days
				&& this.hours == that.hours && this.minutes == that.minutes && this.seconds == that.seconds;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + this.years;
		hash = 31 * hash + this.months;
		hash = 31 * hash + this.days;
		hash = 31 * hash + this.hours;
		hash = 31 * hash + this.minutes;
		hash = 31 * hash + this.seconds;
		return hash;
	}

	@Override
	public String toString() {
		return formatDuration(this);
	}

}
