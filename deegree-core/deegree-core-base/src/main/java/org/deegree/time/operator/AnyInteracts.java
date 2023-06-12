package org.deegree.time.operator;

import static org.deegree.time.operator.TimeCompareUtils.compareBeginWithEnd;

import org.deegree.time.primitive.TimeGeometricPrimitive;

public class AnyInteracts {

	public boolean evaluate(final TimeGeometricPrimitive t1, final TimeGeometricPrimitive t2) {
		if (t1 == null || t2 == null) {
			return false;
		}
		return compareBeginWithEnd(t1, t2) < 0 && compareBeginWithEnd(t2, t1) < 0;
	}

}
