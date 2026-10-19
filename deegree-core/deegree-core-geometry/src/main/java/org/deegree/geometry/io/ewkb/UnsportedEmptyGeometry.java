package org.deegree.geometry.io.ewkb;

import java.io.Serial;

public class UnsportedEmptyGeometry extends Exception {

	@Serial
	private static final long serialVersionUID = 3602797633233022816L;

	public UnsportedEmptyGeometry() {
		super("Creating EMPTY geometries is not supported.");
	}

}
