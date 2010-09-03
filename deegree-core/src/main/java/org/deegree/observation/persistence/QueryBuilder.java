//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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
package org.deegree.observation.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * This is a simple builder for prepared statements.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class QueryBuilder {

    private final StringBuffer buf = new StringBuffer();

    private final List<SetObject> setters = new LinkedList<SetObject>();

    /**
     * Add a new part to the statement. For each '?' wildcard in the statement string you must call
     * {@link #add(SetObject)}. It will append whitespace to separate tokens.
     *
     * @param stmt
     *            the part of the query
     * @return <code>this</code> for method chaining
     */
    public QueryBuilder add( String stmt ) {
        buf.append( stmt );
        if ( !stmt.endsWith( " " ) ) {
            buf.append( " " );
        }
        return this;
    }

    /**
     * Add a new object setter. You must add one setter for each parameter (?) in the query.
     *
     * @param setter
     * @return <code>this</code> for method chaining
     */
    public QueryBuilder add( SetObject setter ) {
        this.setters.add( setter );
        return this;
    }

    /**
     * Create a prepared statment with all parameter set.
     *
     * @param conn
     * @return the prepared statement
     * @throws SQLException
     */
    public PreparedStatement buildStatement( Connection conn )
                            throws SQLException {
        PreparedStatement stmt = conn.prepareStatement( buf.toString() );
        if ( stmt.getParameterMetaData().getParameterCount() != setters.size() ) {
            throw new SQLException( "can't build statement: query contains "
                                    + stmt.getParameterMetaData().getParameterCount() + " parameters but got "
                                    + setters.size() + " parameter setters." );
        }

        int i = 1;
        for ( SetObject setter : this.setters ) {
            setter.set( stmt, i );
            i++;
        }
        return stmt;
    }

    @Override
    public String toString() {
        return this.buf.toString();
    }

    /**
     * @param value
     *            the string to set
     * @return a SetObject that sets a String
     */
    public static SetObject stringSetter( final String value ) {
        SetObject result = new QueryBuilder.SetObject() {
            @Override
            public void set( PreparedStatement stmt, int i )
                                    throws SQLException {
                stmt.setString( i, value );
            }
        };
        return result;
    }

    /**
     * This interface is used to set objects into prepared statements. The set method is called when the sql statement
     * is created. Users of the QueryBuilder must implement this interface for each parameter (?) to set the
     */
    public static interface SetObject {
        /**
         * This method should add a value to the i'th parameter of the PreparedStatement. (e.g. stmt.setString( i,
         * "foo"))
         *
         * @param stmt
         *            the PreparedStatement to add the parameter value
         * @param i
         *            the number of the parameter
         * @throws SQLException
         */
        public void set( PreparedStatement stmt, int i )
                                throws SQLException;
    }

}
