/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2026 by:
 - Department of Geography, University of Bonn -
 and
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -
 and others

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

 e-mail: info@deegree.org

 website: https://www.deegree.org/
----------------------------------------------------------------------------*/
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
