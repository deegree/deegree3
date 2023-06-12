package org.deegree.time.operator;

import static org.deegree.commons.tom.datetime.ISO8601Converter.formatDateTime;
import static org.deegree.time.position.IndeterminateValue.UNKNOWN;

import org.deegree.commons.tom.datetime.Temporal;
import org.deegree.time.position.TimePosition;
import org.deegree.time.primitive.GenericTimeInstant;
import org.deegree.time.primitive.GenericTimePeriod;
import org.deegree.time.primitive.TimeGeometricPrimitive;
import org.deegree.time.primitive.TimeInstant;
import org.deegree.time.primitive.TimePeriod;

public class TimeObjectFactory {

	public TimeInstant createInstant(final Temporal t) {
		final TimePosition position = createPosition(t);
		return new GenericTimeInstant(null, null, null, null, position);
	}

	public TimePeriod createPeriod(final Temporal begin, final Temporal end) {
		final TimePosition pos1 = createPosition(begin);
		final TimePosition pos2 = createPosition(end);
		return new GenericTimePeriod(null, null, null, null, pos1, pos2);
	}

	public TimeGeometricPrimitive createPeriodOrInstant(final Temporal begin, final Temporal end) {
		if (begin != null && begin.equals(end)) {
			return createInstant(begin);
		}
		return createPeriod(begin, end);
	}

	public TimePosition createPosition(final Temporal t1) {
		if (t1 == null) {
			return new TimePosition(null, null, UNKNOWN, null);
		}
		final String encodedTemporal = encodeTemporal(t1);
		return new TimePosition(null, null, null, encodedTemporal);
	}

	private String encodeTemporal(final Temporal t1) {
		return formatDateTime(t1);
	}

}
