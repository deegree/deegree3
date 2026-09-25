package org.deegree.geometry.io.ewkb;

@SuppressWarnings("SpellCheckingInspection")
public enum Axis {

	XY(2), XYZ(3), XYM(3), XYZM(4);

	private final int dimensions;

	Axis(int dimensions) {
		this.dimensions = dimensions;
	}

	public int size(int cnt) {
		return dimensions * cnt;
	}

}
