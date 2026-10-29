package org.deegree.sqldialect.postgis;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.io.ewkb.Axis;
import org.deegree.geometry.utils.GeometryParticleConverter;

import java.util.function.IntFunction;

public class PostGISEWKBDialect extends PostGISDialect {

	private Axis axisMode;

	/**
	 * Creates a new {@link PostGISEWKBDialect} instance.
	 * @param version version string, as returned by "SELECT postgis_version()", can be
	 * <code>null</code> (unknown)
	 */
	public PostGISEWKBDialect(String version, Axis axisMode) {
		super(version);
		this.axisMode = axisMode;
	}

	public GeometryParticleConverter getGeometryConverter(String column, ICRS crs, String sridString, boolean is2D) {
		return new PostGISEWKBGeometryConverter(column, crs, sridString, isUseLegacyPredicates(), axisMode,
				(srid) -> crs);
	}

}
