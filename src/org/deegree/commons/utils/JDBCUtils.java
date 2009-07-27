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
package org.deegree.commons.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * This class contains static utility methods for working with JDBC.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public final class JDBCUtils {

    /**
     * Close the object, suppress all errors/exceptions. Useful for finally clauses.
     *
     * @param conn
     */
    public static void close( Connection conn ) {
        try {
            conn.close();
        } catch ( Exception ex ) {
            //
        }
    }

    /**
     * Close the object, suppress all errors/exceptions. Useful for finally clauses.
     *
     * @param resultSet
     */
    public static void close( ResultSet resultSet ) {
        try {
            resultSet.close();
        } catch ( Exception ex ) {
            //
        }
    }

    /**
     * Close the object, suppress all errors/exceptions. Useful for finally clauses.
     *
     * @param stmt
     */
    public static void close( Statement stmt ) {
        try {
            stmt.close();
        } catch ( Exception ex ) {
            //
        }
    }

    /**
     * Close the object, suppress all errors/exceptions. Useful for finally clauses.
     *
     * @param stmt
     */
    public static void close( PreparedStatement stmt ) {
        try {
            stmt.close();
        } catch ( Exception ex ) {
            //
        }
    }

}
