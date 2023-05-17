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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Base class for temporal primitives that represent a point in time.
 * <p>
 * A {@link Temporal} is a thin wrapper around {@link Calendar} that tracks whether the
 * {@link Temporal} has been created with our without explicit time zone information. If
 * it has been constructed without an explicit time zone, the default time zone
 * ({@link TimeZone#getDefault()}) is used for the underlying calendar.
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public abstract class Temporal implements Comparable<Temporal> {

	protected final Calendar cal;

	protected final boolean tzIsUnknown;

	/**
	 * Creates a new {@link Temporal} instance.
	 * @param cal calendar, must not be <code>null</code>
	 * @param tzIsUnknown <code>true</code>, if the timezone is unknown,
	 * <code>false</code> otherwise
	 */
	Temporal(Calendar cal, boolean tzIsUnknown) {
		this.cal = cal;
		this.tzIsUnknown = tzIsUnknown;
	}

	/**
	 * Creates a new {@link Temporal} instance.
	 * @param date time instant as a date, must not be <code>null</code>
	 * @param tz timezone used by the date, can be <code>null</code> (time zone unknown)
	 */
	Temporal(java.util.Date date, TimeZone tz) {
		Calendar cal = null;
		if (tz != null) {
			cal = Calendar.getInstance(tz);
			tzIsUnknown = false;
		}
		else {
			cal = Calendar.getInstance();
			tzIsUnknown = true;
		}
		this.cal = cal;
		cal.setTime(date);
	}

	protected static boolean isLocal(String s) {
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
	 * Returns this time instant as a {@link Calendar}.
	 * <p>
	 * NOTE: When the time zone is unknown, the default time zone is used
	 * ({@link TimeZone#getDefault()} ).
	 * </p>
	 * @return calendar, never <code>null</code>
	 */
	public Calendar getCalendar() {
		return cal;
	}

	/**
	 * Returns this time instant as a {@link Date}.
	 * <p>
	 * NOTE: When the time zone is unknown, the default time zone is used
	 * ({@link TimeZone#getDefault()} ).
	 * </p>
	 * @return calendar, never <code>null</code>
	 */
	public Date getDate() {
		return cal.getTime();
	}

	/**
	 * Returns the milliseconds since January 1, 1970, 00:00:00 GMT.
	 * <p>
	 * NOTE: When the time zone is unknown, this method will assume the default time zone
	 * ({@link TimeZone#getDefault()} ).
	 * </p>
	 * @return the milliseconds since January 1, 1970, 00:00:00 GMT
	 */
	public long getTimeInMilliseconds() {
		return cal.getTimeInMillis();
	}

	/**
	 * Returns whether this time instant has been created without explicit time zone
	 * information.
	 * @return <code>true</code>, if this time instant has been created without explicit
	 * time zone, <code>false</code> otherwise
	 */
	public boolean isTimeZoneUnknown() {
		return tzIsUnknown;
	}

	/**
	 * Returns a new {@link Temporal} that represents the same point in time, but using
	 * the specified time zone.
	 * @param tz time zone can be <code>null</code> (use system's local time zone)
	 * @return time instant using the given time zone, never <code>null</code>
	 */
	public abstract Temporal toTimeZone(TimeZone tz);

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Temporal)) {
			return false;
		}
		Temporal that = (Temporal) o;
		return this.cal.equals(that.cal);
	}

	@Override
	public int compareTo(Temporal that) {
		return this.cal.compareTo(that.cal);
	}

}
