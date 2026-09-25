package org.deegree.geometry.io.ewkb;

public class Types {

	// https://gitea.osgeo.org/postgis/postgis/src/commit/84890ccbe53549130de8a2b75a72ab3a54b570d1/doc/bnf-wkb.txt

	public static final int POINT_TYPE = 1;

	public static final int LINESTRING_TYPE = 2;

	public static final int POLYGON_TYPE = 3;

	public static final int MULTIPOINT_TYPE = 4;

	public static final int MULTILINESTRING_TYPE = 5;

	public static final int MULTIPOLYGON_TYPE = 6;

	public static final int GEOMETRY_COLLECTION_TYPE = 7;

	public static final int CIRCULAR_STRING_TYPE = 8;

	public static final int COMPOUND_CURVE_TYPE = 9;

	public static final int CURVE_POLYGON_TYPE = 10;

	public static final int MULTICURVE_TYPE = 11;

	public static final int MULTISURFACE_TYPE = 12;

	public static final int TIN_TYPE = 17;

	public static final int TRIANGLE_TYPE = 17;

}
