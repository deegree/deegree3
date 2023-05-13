/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2022 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.sqldialect.oracle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.utils.GeometryParticleConverter;
import org.deegree.sqldialect.oracle.sdo.SDOGeometryConverter;
import org.deegree.sqldialect.oracle.sdo.SDOInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oracle.jdbc.OracleConnection;
import oracle.sql.STRUCT;

/**
 * {@link GeometryParticleConverter} for Oracle Spatial.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>O
 * @since 3.2
 */
public class OracleGeometryConverter implements GeometryParticleConverter {

	private static Logger LOG = LoggerFactory.getLogger(OracleGeometryConverter.class);

	private final String column;

	private final ICRS crs;

	private final String srid;

	private int isrid;

	private SDOInspector sdoInspector;

	/**
	 * Creates a new {@link OracleGeometryConverter} instance.
	 * @param column (unqualified) column that stores the geometry, must not be
	 * <code>null</code>
	 * @param crs CRS of the stored geometries, can be <code>null</code>
	 * @param srid Oracle spatial reference identifier, must not be <code>null</code>
	 */
	public OracleGeometryConverter(String column, ICRS crs, String srid) {
		this.column = column;
		this.crs = crs;
		this.srid = srid;
		this.isrid = 0;
		try {
			if (srid != null)
				this.isrid = Integer.valueOf(srid);
		}
		catch (NumberFormatException nfe) {
			// TODO handle it smoother
		}
	}

	@Override
	public String getSelectSnippet(String tableAlias) {
		if (tableAlias != null) {
			return tableAlias + "." + column;
		}
		return column;
	}

	@Override
	public String getSetSnippet(Geometry particle) {
		return "?";
	}

	@Override
	public Geometry toParticle(ResultSet rs, int colIndex) throws SQLException {
		Object sqlValue = rs.getObject(colIndex);
		if (sqlValue == null) {
			return null;
		}
		try {
			return new SDOGeometryConverter(sdoInspector).toGeometry((STRUCT) sqlValue, crs);
		}
		catch (Throwable t) {
			throw new IllegalArgumentException(t);
		}
	}

	@Override
	public void setParticle(PreparedStatement stmt, Geometry particle, int paramIndex) throws SQLException {
		try {
			if (particle == null) {
				stmt.setNull(paramIndex, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
			}
			else {
				Geometry compatible = getCompatibleGeometry(particle);
				// TODO clarify if this was only a wkt/wkb requirement ?!
				// (background Envelope -> Optimized Rectangles in Oracle are preferred
				// and faster for SDO_RELATE
				// filters )
				//
				// if ( compatible instanceof Envelope ) {
				// compatible = compatible.getConvexHull();
				// }
				OracleConnection ocon = getOracleConnection(stmt.getConnection());
				Object struct = new SDOGeometryConverter(sdoInspector).fromGeometry(ocon, isrid, compatible, true);
				stmt.setObject(paramIndex, struct);
			}
		}
		catch (Throwable t) {
			throw new IllegalArgumentException(t);
		}
	}

	private OracleConnection getOracleConnection(Connection conn) throws SQLException {
		OracleConnection ocon = null;
		if (conn instanceof OracleConnection) {
			ocon = (OracleConnection) conn;
		}
		else {
			ocon = conn.unwrap(OracleConnection.class);
		}
		return ocon;
	}

	private Geometry getCompatibleGeometry(Geometry literal) throws SQLException {
		if (crs == null) {
			return literal;
		}

		Geometry transformedLiteral = literal;
		if (literal != null) {
			ICRS literalCRS = literal.getCoordinateSystem();
			if (literalCRS != null && !(crs.equals(literalCRS))) {
				LOG.debug("Need transformed literal geometry for evaluation: " + literalCRS.getAlias() + " -> "
						+ crs.getAlias());
				try {
					GeometryTransformer transformer = new GeometryTransformer(crs);
					transformedLiteral = transformer.transform(literal);
				}
				catch (Exception e) {
					throw new SQLException(e.getMessage());
				}
			}
		}
		return transformedLiteral;
	}

	@Override
	public String getSrid() {
		return srid;
	}

	@Override
	public ICRS getCrs() {
		return crs;
	}

	public void setSdoInsepctor(SDOInspector sdoInspector) {
		this.sdoInspector = sdoInspector;
	}

}
