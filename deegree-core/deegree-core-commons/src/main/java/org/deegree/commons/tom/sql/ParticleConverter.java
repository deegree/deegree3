/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.commons.tom.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.deegree.commons.tom.TypedObjectNode;

/**
 * Implementations convert particles between {@link TypedObjectNode} instances and SQL
 * argument / parameter objects in {@link PreparedStatement} / {@link ResultSet}
 * instances.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public interface ParticleConverter<T extends TypedObjectNode> {

	/**
	 * Returns an SQL fragment for SELECTing the particle value from the associated
	 * database table.
	 * @param tableAlias alias that's used for disambiguating the table, may be
	 * <code>null</code>
	 * @return SQL fragment (e.g. <code>X1.columname</code>), may be <code>null</code>
	 */
	public String getSelectSnippet(String tableAlias);

	/**
	 * Builds a particle from the specified column of the current row of the given
	 * {@link ResultSet}.
	 * @param rs result set, never <code>null</code>
	 * @param colIndex index of the column in the result set
	 * @return particle, may be <code>null</code>
	 */
	public T toParticle(ResultSet rs, int colIndex) throws SQLException;

	/**
	 * Returns a {@link PreparedStatement} fragment for setting the given particle value
	 * in an SQL statement.
	 * <p>
	 * The value may be set in a literal SQL fashion (e.g. '2007-08-09') or as a
	 * {@link PreparedStatement} placeholder ('?').
	 * </p>
	 * @param particle particle value, can be <code>null</<code>
	 * @return SQL fragment (e.g. <code>?</code>), may be <code>null</code>
	 */
	public String getSetSnippet(T particle);

	/**
	 * Converts the given particle and sets the designated SQL parameter in the given
	 * {@link PreparedStatement}.
	 * @param stmt prepared statement, never <code>null</code>
	 * @param particle particle value, can be <code>null</<code>
	 * @param paramIndex index of the SQL parameter in the statement
	 */
	public void setParticle(PreparedStatement stmt, T particle, int paramIndex) throws SQLException;

}