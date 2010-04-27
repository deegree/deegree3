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
package org.deegree.services.sos.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public abstract class SQLObservationDatastore implements ObservationDatastore {

    /**
     * the datastore configuration
     */
    private final DatastoreConfiguration dsConfig;

    /**
     * @param dsConfig
     */
    protected SQLObservationDatastore( DatastoreConfiguration dsConfig ) {
        this.dsConfig = dsConfig;
    }

    /**
     * @return a new sql connection for the configured jdbc database.
     * @throws SQLException
     */
    protected Connection getConnection()
                            throws SQLException {

                                String connectionURL = getDSConfig().getJdbcConnection();
                                String username = getDSConfig().getUsername();
                                String password = getDSConfig().getPassword();

                                return DriverManager.getConnection( connectionURL, username, password );
                            }

    /**
     * @return the dsConfig
     */
    public DatastoreConfiguration getDSConfig() {
        return dsConfig;
    }

}
