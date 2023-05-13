package org.deegree.time.operator;

import static org.deegree.time.operator.TimeCompareUtils.compareBegin;
import static org.deegree.time.operator.TimeCompareUtils.compareEnd;

import org.deegree.time.primitive.TimeGeometricPrimitive;

public class LaxDuring {

	public boolean evaluate(final TimeGeometricPrimitive a, final TimeGeometricPrimitive b) {
		return compareBegin(b, a) <= 0 && compareEnd(a, b) <= 0;
	}

}
