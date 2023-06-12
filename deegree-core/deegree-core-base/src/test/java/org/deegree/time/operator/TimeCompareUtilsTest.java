/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.time.operator;

import static java.util.Collections.emptyList;
import static org.deegree.time.operator.TimeCompareUtils.compareEndWithBegin;
import static org.deegree.time.position.IndeterminateValue.UNKNOWN;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.deegree.commons.tom.gml.property.Property;
import org.deegree.time.position.TimePosition;
import org.deegree.time.primitive.GenericTimeInstant;
import org.deegree.time.primitive.GenericTimePeriod;
import org.deegree.time.primitive.RelatedTime;
import org.deegree.time.primitive.TimeInstant;
import org.deegree.time.primitive.TimePeriod;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class TimeCompareUtilsTest {

	@Test(expected = NullPointerException.class)
	public void evaluateInstantInstant_BothNull() {
		compareEndWithBegin(null, null);
	}

	@Test(expected = NullPointerException.class)
	public void evaluateInstantInstant_FirstNull() {
		compareEndWithBegin(null, instant("2014-01-01"));
	}

	@Test(expected = NullPointerException.class)
	public void evaluateInstantInstant_SecondNull() {
		compareEndWithBegin(instant("2014-01-01"), null);
	}

	@Test
	public void evaluateInstantInstant() {
		assertThat(compareEndWithBegin(instant("2014-01-01"), instant("2014-01-01")), is(0));
		assertThat(compareEndWithBegin(instant("2014-01-01T00:00:01"), instant("2014-01-01T00:00:01")), is(0));
		assertThat(compareEndWithBegin(instant("2014-01-01T00:00:01.000"), instant("2014-01-01T00:00:01.000")), is(0));
		assertThat(compareEndWithBegin(instant("INDETERMINATE"), instant("INDETERMINATE")), is(0));
		assertThat(compareEndWithBegin(instant("INDETERMINATE"), instant("2014-01-01T00:00:01")), is(0));
		assertThat(compareEndWithBegin(instant("2014-01-01T00:00:01"), instant("INDETERMINATE")), is(0));

		assertThat(compareEndWithBegin(instant("2015-01-01"), instant("2014-01-01")), is(1));
		assertThat(compareEndWithBegin(instant("2014-01-01T00:00:02"), instant("2014-01-01T00:00:01")), is(1));
		assertThat(compareEndWithBegin(instant("2014-01-01T00:00:01.001"), instant("2014-01-01T00:00:01.000")), is(1));

		assertThat(compareEndWithBegin(instant("2014-01-01"), instant("2015-01-01")), is(-1));
		assertThat(compareEndWithBegin(instant("2014-01-01T00:00:01"), instant("2014-01-01T00:00:02")), is(-1));
		assertThat(compareEndWithBegin(instant("2014-01-01T00:00:01.000"), instant("2014-01-01T00:00:01.001")), is(-1));
	}

	@Test
	public void evaluateInstantPeriod() {
		assertThat(compareEndWithBegin(instant("2014-01-01T00:00:01"),
				period("2014-01-01T00:00:01", "2014-01-01T00:00:01")), is(0));
		assertThat(compareEndWithBegin(instant("2014-01-01T00:00:01"),
				period("2014-01-01T00:00:01", "2014-01-01T00:00:03")), is(0));

		assertThat(compareEndWithBegin(instant("2014-01-01T00:00:02"),
				period("2014-01-01T00:00:01", "2014-01-01T00:00:03")), is(1));
		assertThat(compareEndWithBegin(instant("2014-01-01T00:00:03"),
				period("2014-01-01T00:00:01", "2014-01-01T00:00:03")), is(1));
		assertThat(compareEndWithBegin(instant("2014-01-01T00:00:04"),
				period("2014-01-01T00:00:01", "2014-01-01T00:00:03")), is(1));

		assertThat(compareEndWithBegin(instant("2014-01-01T00:00:00"),
				period("2014-01-01T00:00:01", "2014-01-01T00:00:03")), is(-1));
	}

	@Test
	public void evaluatePeriodInstant() {
		assertThat(compareEndWithBegin(period("2014-01-01T00:00:01", "2014-01-01T00:00:01"),
				instant("2014-01-01T00:00:01")), is(0));
		assertThat(compareEndWithBegin(period("2014-01-01T00:00:01", "2014-01-01T00:00:03"),
				instant("2014-01-01T00:00:03")), is(0));

		assertThat(compareEndWithBegin(period("2014-01-01T00:00:01", "2014-01-01T00:00:03"),
				instant("2014-01-01T00:00:02")), is(1));
		assertThat(compareEndWithBegin(period("2014-01-01T00:00:01", "2014-01-01T00:00:03"),
				instant("2014-01-01T00:00:01")), is(1));
		assertThat(compareEndWithBegin(period("2014-01-01T00:00:01", "2014-01-01T00:00:03"),
				instant("2014-01-01T00:00:00")), is(1));

		assertThat(compareEndWithBegin(period("2014-01-01T00:00:01", "2014-01-01T00:00:03"),
				instant("2014-01-01T00:00:04")), is(-1));
	}

	@Test
	public void evaluatePeriodPeriod() {
		assertThat(compareEndWithBegin(period("2014-01-01T00:00:01", "2014-01-01T00:00:01"),
				period("2014-01-01T00:00:01", "2014-01-01T00:00:01")), is(0));
		assertThat(compareEndWithBegin(period("2014-01-01T00:00:00", "2014-01-01T00:00:01"),
				period("2014-01-01T00:00:01", "2014-01-01T00:00:02")), is(0));

		assertThat(compareEndWithBegin(period("2014-01-01T00:00:02", "2014-01-01T00:00:03"),
				period("2014-01-01T00:00:00", "2014-01-01T00:00:01")), is(1));
		assertThat(compareEndWithBegin(period("2014-01-01T00:00:02", "2014-01-01T00:00:03"),
				period("2014-01-01T00:00:01", "2014-01-01T00:00:02")), is(1));
		assertThat(compareEndWithBegin(period("2014-01-01T00:00:02", "2014-01-01T00:00:03"),
				period("2014-01-01T00:00:02", "2014-01-01T00:00:03")), is(1));

		assertThat(compareEndWithBegin(period("2014-01-01T00:00:01", "2014-01-01T00:00:02"),
				period("2014-01-01T00:00:03", "2014-01-01T00:00:04")), is(-1));
	}

	private TimeInstant instant(final String s) {
		final List<Property> props = emptyList();
		final List<RelatedTime> relatedTimes = emptyList();
		TimePosition pos = null;
		if ("INDETERMINATE".equals(s)) {
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