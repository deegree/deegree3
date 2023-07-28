package org.deegree.time.operator;

import static org.deegree.time.position.IndeterminateValue.NOW;
import static org.deegree.time.position.IndeterminateValue.UNKNOWN;

import org.deegree.commons.tom.datetime.Temporal;
import org.deegree.time.position.IndeterminateValue;
import org.deegree.time.position.TimePosition;
import org.deegree.time.primitive.TimeGeometricPrimitive;
import org.deegree.time.primitive.TimeInstant;
import org.deegree.time.primitive.TimePeriod;

public class Equals {

	public boolean evaluate(final TimeGeometricPrimitive t1, final TimeGeometricPrimitive t2) {
		if (t1 == null || t2 == null) {
			if (t1 == null && t2 == null) {
				return true;
			}
			return false;
		}
		if (!t1.getClass().isInstance(t2)) {
			return false;
		}
		if (t1 instanceof TimeInstant) {
			return equals((TimeInstant) t1, (TimeInstant) t2);
		}
		if (t1 instanceof TimePeriod) {
			return equals((TimePeriod) t1, (TimePeriod) t2);
		}
		throw new UnsupportedOperationException();
	}

	private boolean equals(final TimeInstant t1, final TimeInstant t2) {
		return equals(t1.getPosition(), t2.getPosition());
	}

	private boolean equals(final TimePeriod t1, final TimePeriod t2) {
		return equals(t1.getBeginPosition(), t2.getBeginPosition()) && equals(t1.getEndPosition(), t2.getEndPosition());
	}

	private boolean equals(final TimePosition p1, final TimePosition p2) {
		IndeterminateValue indeterminateness = p1.getIndeterminatePosition();
		if (indeterminateness != p2.getIndeterminatePosition()) {
			return false;
		}
		if (indeterminateness == UNKNOWN || indeterminateness == NOW) {
			return true;
		}
		else if (indeterminateness != null) {
			throw new UnsupportedOperationException();
		}
		final String v1 = p1.getValue();
		final String v2 = p2.getValue();
		if (v1 == null || v2 == null) {
			return false;
		}
		return equals(v1, v2);
	}

	boolean equals(final String v1, final String v2) {
		if (equalsLiterally(v1, v2)) {
			return true;
		}
		return equalsParsed(v1, v2);
	}

	private boolean equalsLiterally(final String v1, final String v2) {
		return v1.equals(v2);
	}

	private boolean equalsParsed(final String v1, final String v2) {
		try {
			final Temporal d1 = new TemporalConverter().convert(v1);
			final Temporal d2 = new TemporalConverter().convert(v2);
			return d1.equals(d2);
		}
		catch (IllegalArgumentException e) {
			// nothing to do
		}
		return false;
	}

}
