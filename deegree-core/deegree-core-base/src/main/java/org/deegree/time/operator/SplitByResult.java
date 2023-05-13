package org.deegree.time.operator;

import org.deegree.time.primitive.TimeGeometricPrimitive;

public class SplitByResult {

	private final TimeGeometricPrimitive begin;

	private final TimeGeometricPrimitive intersection;

	private final TimeGeometricPrimitive end;

	public SplitByResult(final TimeGeometricPrimitive begin, final TimeGeometricPrimitive intersection,
			final TimeGeometricPrimitive end) {
		this.begin = begin;
		this.intersection = intersection;
		this.end = end;
	}

	public TimeGeometricPrimitive getBegin() {
		return begin;
	}

	public TimeGeometricPrimitive getIntersection() {
		return intersection;
	}

	public TimeGeometricPrimitive getEnd() {
		return end;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SplitByResult other = (SplitByResult) obj;
		if (begin == null) {
			if (other.begin != null)
				return false;
		}
		else if (!new Equals().evaluate(this.begin, other.begin))
			return false;
		if (end == null) {
			if (other.end != null)
				return false;
		}
		else if (!new Equals().evaluate(this.end, other.end))
			return false;
		if (intersection == null) {
			if (other.intersection != null)
				return false;
		}
		else if (!new Equals().evaluate(this.intersection, other.intersection))
			return false;
		return true;
	}

}
