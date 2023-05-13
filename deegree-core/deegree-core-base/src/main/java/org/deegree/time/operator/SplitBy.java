package org.deegree.time.operator;

import static org.deegree.time.operator.TimeCompareUtils.begin;
import static org.deegree.time.operator.TimeCompareUtils.compareBegin;
import static org.deegree.time.operator.TimeCompareUtils.compareEnd;
import static org.deegree.time.operator.TimeCompareUtils.end;

import org.deegree.time.position.TimePosition;
import org.deegree.time.primitive.GenericTimePeriod;
import org.deegree.time.primitive.TimeGeometricPrimitive;
import org.deegree.time.primitive.TimeInstant;
import org.deegree.time.primitive.TimePeriod;

public class SplitBy {

	public SplitByResult evaluate(final TimeGeometricPrimitive a, final TimeGeometricPrimitive b) {
		if (!new AnyInteracts().evaluate(a, b)) {
			return new SplitByResult(a, null, null);
		}
		if (a instanceof TimeInstant) {
			return new SplitByResult(null, a, null);
		}
		if (b instanceof TimeInstant) {
			throw new IllegalArgumentException("Invalid arguments for SplitBy");
		}
		final boolean hasBegin = compareBegin(a, b) < 0;
		final boolean hasEnd = compareEnd(b, a) < 0;
		final TimePeriod begin = hasBegin ? create(begin(a), begin(b)) : null;
		final TimePosition intersectionBegin = hasBegin ? begin(b) : begin(a);
		final TimePosition intersectionEnd = hasEnd ? end(b) : end(a);
		final TimePeriod end = hasEnd ? create(end(b), end(a)) : null;
		final TimePeriod intersection = create(intersectionBegin, intersectionEnd);
		return new SplitByResult(begin, intersection, end);
	}

	private TimePeriod create(final TimePosition begin, final TimePosition end) {
		return new GenericTimePeriod(null, null, null, null, begin, end);
	}

}
