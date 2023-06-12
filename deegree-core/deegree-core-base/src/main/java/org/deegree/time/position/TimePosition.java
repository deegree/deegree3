/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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
package org.deegree.time.position;

import org.deegree.time.primitive.TimePositionOrInstant;

/**
 * A temporal position that implements ISO 19108 TM_Position.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
public class TimePosition implements TimePositionOrInstant {

	private final String frame;

	private final String calendarEraName;

	private final IndeterminateValue indeterminatePosition;

	private final String value;

	/**
	 * Creates a new {@link TimePosition} instance.
	 * @param frame temporal reference system, can be <code>null</code> (default)
	 * @param calendarEraName name of the calendar era, can be <code>null</code>
	 * (unspecified)
	 * @param indeterminatePosition type of inexactness, can be <code>null</code> (value
	 * is exact)
	 * @param value encoded time value, must not be <code>null</code>
	 */
	public TimePosition(final String frame, final String calendarEraName,
			final IndeterminateValue indeterminatePosition, final String value) {
		this.frame = frame;
		this.calendarEraName = calendarEraName;
		this.indeterminatePosition = indeterminatePosition;
		this.value = value;
	}

	/**
	 * Returns the temporal reference system.
	 * @return temporal reference system, can be <code>null</code> (Gregorian calendar
	 * with UTC)
	 */
	public String getFrame() {
		return frame;
	}

	/**
	 * Returns the name of the calendar era.
	 * @return name of the calendar era, can be <code>null</code>
	 */
	public String getCalendarEraName() {
		return calendarEraName;
	}

	/**
	 * Returns the type of inexactness.
	 * @return type of inexactness, can be <code>null</code> (value is exact)
	 */
	public IndeterminateValue getIndeterminatePosition() {
		return indeterminatePosition;
	}

	/*
	 * Returns the encoded time value. <p> Any of the following XML schema simple types
	 * can be used for encoding: <ul> <li>date</li> <li>gYearMonth</li> <li>gYear</li>
	 * <li>time</li> <li>dateTime</li> <li>anyURI</li> <li>decimal</li> </ul> </p>
	 *
	 * @return encoded time value, never <code>null</code>
	 */
	public String getValue() {
		return value;
	}

}
