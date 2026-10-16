package org.deegree.geometry.io.ewkb;

@SuppressWarnings("SpellCheckingInspection")
public enum Axis {

	AUTO(0), XY(2), XYZ(3), XYM(3), XYZM(4);

	private final int dimensions;

	Axis(int dimensions) {
		this.dimensions = dimensions;
	}

	public int size(int cnt) {
		if (this == AUTO) {
			throw new IllegalArgumentException("Axis of type AUTO has no size.");
		}
		return dimensions * cnt;
	}

	public Axis withoutAuto(boolean haveZ, boolean haveM) {
		if (this != AUTO) {
			return this;
		}
		else if (haveZ && haveM) {
			return XYZM;
		}
		else if (!haveZ && !haveM) {
			return XY;
		}
		else {
			return XYZ;
		}
	}

	public Axis withoutAuto(int dimensions) {
		if (this != AUTO) {
			return this;
		}
		else if (dimensions > 3) {
			return XYZM;
		}
		else if (dimensions <= 2) {
			return XY;
		}
		else {
			return XYZ;
		}

	}

	public boolean hasZ(int dimensions) {
		Axis chk = withoutAuto(dimensions);
		return chk == Axis.XYZ || chk == Axis.XYZM;
	}

	public boolean hasM(int dimensions) {
		Axis chk = withoutAuto(dimensions);
		return chk == Axis.XYM || chk == Axis.XYZM;
	}

}
