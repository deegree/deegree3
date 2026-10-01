package org.deegree.sqldialect.postgis;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.io.ewkb.Axis;
import org.deegree.geometry.io.ewkb.BinaryParser;
import org.deegree.geometry.io.ewkb.BinaryWriter;
import org.deegree.geometry.utils.GeometryParticleConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.IntFunction;

public class PostGISEWKBGeometryConverter implements GeometryParticleConverter {

	private static Logger LOG = LoggerFactory.getLogger(PostGISEWKBGeometryConverter.class);

	private final String column;

	private final boolean useLegacyPredicates;

	private final ICRS crs;

	private final IntFunction<ICRS> sridToCrs;

	private int srid;

	private final Axis axisMode;

	public PostGISEWKBGeometryConverter(String column, ICRS crs, String srid, boolean useLegacyPredicates,
			Axis axisMode, IntFunction<ICRS> sridToCrs) {
		this.column = column;
		this.crs = crs;
		this.axisMode = axisMode;
		this.sridToCrs = sridToCrs;

		try {
			this.srid = Integer.valueOf(srid);
		}
		catch (NumberFormatException e) {
			LOG.warn("Failed to convert srid {} to number. Using -1 instead: {}", srid, e.getMessage());
			LOG.trace("Exception", e);
			this.srid = -1;
		}
		this.useLegacyPredicates = useLegacyPredicates;
	}

	public String getSelectSnippet(String tableAlias) {
		String asewkb = this.useLegacyPredicates ? "AsEWKB" : "ST_AsEWKB";
		return tableAlias != null ? asewkb + "(" + tableAlias + "." + this.column + ")"
				: asewkb + "(" + this.column + ")";
	}

	public Geometry toParticle(ResultSet rs, int colIndex) throws SQLException {
		byte[] wkb = rs.getBytes(colIndex);
		if (wkb == null) {
			return null;
		}
		else {
			try {
				return BinaryParser.parse(axisMode, sridToCrs, wkb, 0);
			}
			catch (Throwable t) {
				throw new IllegalArgumentException(t.getMessage(), t);
			}
		}
	}

	public String getSetSnippet(Geometry particle) {
		return "?";
	}

	public void setParticle(PreparedStatement stmt, Geometry particle, int paramIndex) throws SQLException {
		byte[] wkb = null;
		if (particle != null) {
			try {
				Geometry compatible = this.getCompatibleGeometry(particle);
				wkb = BinaryWriter.writeBinary(axisMode, compatible, this.srid);
			}
			catch (Throwable t) {
				throw new IllegalArgumentException(t.getMessage(), t);
			}
		}

		stmt.setBytes(paramIndex, wkb);
	}

	private Geometry getCompatibleGeometry(Geometry literal) throws SQLException {
		if (this.crs == null) {
			return literal;
		}
		else {
			Geometry transformedLiteral = literal;
			if (literal != null) {
				ICRS literalCRS = literal.getCoordinateSystem();
				if (literalCRS != null && !this.crs.equals(literalCRS)) {
					LOG.debug("Need transformed literal geometry for evaluation: {} -> {}", literalCRS.getAlias(),
							this.crs.getAlias());
					try {
						GeometryTransformer transformer = new GeometryTransformer(this.crs);
						transformedLiteral = transformer.transform(literal);
					}
					catch (Exception e) {
						throw new SQLException(e.getMessage(), e);
					}
				}
			}

			return transformedLiteral;
		}
	}

	public String getSrid() {
		return "" + this.srid;
	}

	public ICRS getCrs() {
		return this.crs;
	}

}
