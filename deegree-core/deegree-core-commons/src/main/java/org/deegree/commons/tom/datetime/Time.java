/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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

import static java.util.Calendar.getInstance;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * {@link Temporal} for representing times (e.g. <code>xs:time</code>).
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class Time extends Temporal {

	/**
	 * Creates a new {@link Time} instance.
	 * @param cal point in time, must not be <code>null</code>
	 * @param isUnknown <code>true</code>, if the time zone was not available when
	 * creating the <code>Calendar</code> (system's local time zone was assumed),
	 * <code>false</code> otherwise (time zone was available and used)
	 */
	public Time(Calendar cal, boolean isUnknown) {
		super(cal, isUnknown);
	}

	/**
	 * Creates a new {@link Time} instance.
	 * @param date point in time, must not be <code>null</code>
	 * @param tz time zone, can be <code>null</code> (no timezone information,
	 * <code>Date</code> will be interpreted according to system's local time zone)
	 */
	public Time(java.util.Date date, TimeZone tz) {
		super(date, tz);
	}

	@Override
	public Time toTimeZone(TimeZone tz) {
		Calendar cal = null;
		if (tz == null) {
			cal = getInstance();
		}
		else {
			cal = getInstance(tz);
		}
		cal.setTimeInMillis(this.cal.getTimeInMillis());
		return new Time(cal, tz == null);
	}

	@Override
	public String toString() {
		return ISO8601Converter.formatTime(this);
	}

}
