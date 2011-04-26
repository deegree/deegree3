//$HeadURL$
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
package org.deegree.feature.persistence.sql.transformer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.deegree.commons.jdbc.StatementBuilder;
import org.deegree.commons.tom.TypedObjectNode;

/**
 * Implementations convert particles between JDBC argument objects and {@link TypedObjectNode} instances.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface ParticleConverter<T extends TypedObjectNode> {

    /**
     * Appends the given particle as an argument to the given {@link StatementBuilder}.
     * 
     * @param stmt
     *            statement builder, must not be <code>null</code>
     * @param particle
     *            particle to convert and append, can be <code>null</code>
     */
    public void appendArgument( StatementBuilder stmt, T particle );

    /**
     * Returns the SQL snippet for building a {@link Statement} to retrieve the SQL values needed for building the
     * particle.
     * 
     * @return SQL snippet, never <code>null</code>
     */
    public String getRetrieveSQLSnippet();

    /**
     * Retrieves the particle value from the given {@link ResultSet}.
     * 
     * @param rs
     *            result set, must not be <code>null</code>
     * @param columnIdx
     *            column index for accessing the SQL value
     * @return particle, can be <code>null</code>
     * @throws SQLException
     *             if rebuilding the particle fails
     */
    public T retrieveValue( ResultSet rs, int columnIdx )
                            throws SQLException;
}