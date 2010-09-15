//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.metadata.persistence.genericmetadatastore.parsing;

import static org.deegree.commons.utils.JDBCUtils.close;
import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CoupledDataInspector {

    private static final Logger LOG = getLogger( CoupledDataInspector.class );

    private final String connectionId;

    private CoupledDataInspector( String connectionId ) {
        this.connectionId = connectionId;
    }

    public static CoupledDataInspector newInstance( String connectionId ) {
        return new CoupledDataInspector( connectionId );
    }

    /**
     * If there is a data metadata record available for the service metadata record.
     * 
     * @param resourceIdentifierList
     * @return
     * @throws MetadataStoreException
     */
    private boolean getCoupledDataMetadatasets( String resourceIdentifier )
                            throws MetadataStoreException {
        boolean gotOneDataset = false;
        ResultSet rs = null;
        PreparedStatement stm = null;
        Connection conn = null;
        String s = "SELECT resourceidentifier FROM isoqp_resourceidentifier WHERE resourceidentifier = ?;";

        try {
            conn = ConnectionManager.getConnection( connectionId );
            stm = conn.prepareStatement( s );
            stm.setObject( 1, resourceIdentifier );
            rs = stm.executeQuery();
            while ( rs.next() ) {
                gotOneDataset = true;
            }
        } catch ( SQLException e ) {
            LOG.debug( "Error while proving the ID for the coupled resources: {}", e.getMessage() );
            throw new MetadataStoreException( "Error while proving the ID for the coupled resources: {}"
                                              + e.getMessage() );
        } finally {
            close( rs, stm, conn, LOG );
        }

        return gotOneDataset;
    }

}
