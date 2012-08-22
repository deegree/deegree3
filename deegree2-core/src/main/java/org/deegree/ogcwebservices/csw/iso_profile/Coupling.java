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

package org.deegree.ogcwebservices.csw.iso_profile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.ogcwebservices.csw.CSW202PropertiesAccess;

/**
 * <code>Coupling</code>
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class Coupling {

    private static final ILogger LOG = LoggerFactory.getLogger( Coupling.class );

    /**
     * @param identifier
     *            identifier of a data metadataset
     * @return returns true, if an dataset or series with the given identifier of the MD_DataIdentification element
     *         exists
     */
    public static boolean existsRecord( String identifier ) {
        try {
            Connection conn = CSW202PropertiesAccess.getConnection();
            if ( conn != null ) {
                String sql = "SELECT count(identifier) FROM CQP_Main WHERE identifier = ?";
                LOG.logDebug( "Request CSW if dataset or series with MD_DataIdentification/identifier/*/code = "
                              + identifier + " exists [" + sql + "]" );                
                PreparedStatement st = conn.prepareStatement( sql );
                st.setString( 1, identifier );
                ResultSet rs = st.executeQuery();
                if ( rs.next() && rs.getInt( 1 ) > 0 ) {
                    return true;
                }
            }
        } catch ( SQLException e ) {
            LOG.logError( "could not request dataidentification id for fileidentifier " + identifier, e );
        }

        return false;
    }

    /**
     * @return the url of the service
     */
    public static String getCSWUrl() {
        return CSW202PropertiesAccess.getString( "csw.url" );
    }

    /**
     * Replaces all ':' with '_' and added the prefix "uuid_" if the given uuid begins with a number or invalid char.
     * 
     * @param uuid
     *            the uuid
     * @return the uuid if it is a valid id, otherwise a modified version of the uuid
     */
    public static String getValidId( String uuid ) {
        uuid = uuid.replace( ':', '_' );
        String first = uuid.substring( 0, 1 );
        if ( !first.matches( "[a-zA-Z_]" ) ) {
            return "uuid_" + uuid;
        }
        return uuid;
    }

    /**
     * @return a random created id, based on a uuid
     */
    public static String createValidId() {
        return getValidId( UUID.randomUUID().toString() );
    }

}
