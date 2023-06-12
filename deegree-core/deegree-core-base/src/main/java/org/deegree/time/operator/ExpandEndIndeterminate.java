package org.deegree.time.operator;

import static org.deegree.time.position.IndeterminateValue.UNKNOWN;

import org.deegree.time.position.TimePosition;
import org.deegree.time.primitive.GenericTimePeriod;
import org.deegree.time.primitive.TimeGeometricPrimitive;
import org.deegree.time.primitive.TimeInstant;
import org.deegree.time.primitive.TimePeriod;

public class ExpandEndIndeterminate {

	public TimePeriod evaluate(final TimeGeometricPrimitive t) {
		if (t instanceof TimeInstant) {
			return createPeriod(((TimeInstant) t).getPosition());
		}
		if (((TimePeriod) t).getEndPosition().getIndeterminatePosition() == UNKNOWN) {
			return (TimePeriod) t;
		}
		throw new IllegalArgumentException(
				"ExpandEndIndeterminate requires a time instant or a time period with indeterminate end");
	}

	private TimePeriod createPeriod(final TimePosition begin) {
		final TimePosition end = new TimeObjectFactory().createPosition(null);
		return new GenericTimePeriod(null, null, null, null, begin, end);
	}

}
