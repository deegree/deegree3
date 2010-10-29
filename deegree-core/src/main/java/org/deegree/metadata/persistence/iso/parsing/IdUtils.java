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
package org.deegree.metadata.persistence.iso.parsing;

import static org.deegree.commons.utils.JDBCUtils.close;
import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.metadata.persistence.iso.PostGISMappingsISODC;
import org.slf4j.Logger;

/**
 * <Code>ParsingUtils</Code>
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class IdUtils {
    private static final Logger LOG = getLogger( IdUtils.class );

    private final Connection conn;

    private IdUtils( Connection conn ) {
        this.conn = conn;
    }

    public static IdUtils newInstance( Connection conn ) {
        return new IdUtils( conn );
    }

    /**
     * Method to generate via the Java UUID-API a UUID if there is no identifier available.<br>
     * If the generated ID begins with a number then this is replaced with a random letter from the ASCII table. This
     * has to be done because the id attribute in the xml does not support any number at the beginning of an uuid. The
     * uppercase letters are in range from 65 to 90 whereas the lowercase letters are from 97 to 122. After the
     * generation there is a check if (in spite of the nearly impossibility) this uuid exists in the database already.
     * 
     * 
     * @return a uuid that is unique in the backend.
     * @throws MetadataStoreException
     */
    public String generateUUID()
                            throws MetadataStoreException {

        ResultSet rs = null;
        PreparedStatement stm = null;
        String uuid = null;

        try {

            uuid = UUID.randomUUID().toString();
            char firstChar = uuid.charAt( 0 );
            Pattern p = Pattern.compile( "[0-9]" );
            Matcher m = p.matcher( "" + firstChar );
            if ( m.matches() ) {
                int i;
                double ma = Math.random();
                if ( ma < 0.5 ) {
                    i = 65;

                } else {
                    i = 97;
                }

                firstChar = (char) ( (int) ( i + ma * 26 ) );
                uuid = uuid.replaceFirst( "[0-9]", String.valueOf( firstChar ) );
            }
            boolean uuidIsEqual = false;

            String compareIdentifier = "SELECT identifier FROM qp_identifier WHERE identifier = ?";
            stm = conn.prepareStatement( compareIdentifier );
            stm.setObject( 1, uuid );
            rs = stm.executeQuery();
            while ( rs.next() ) {
                uuidIsEqual = true;

            }

            if ( uuidIsEqual == true ) {
                close( rs );
                close( stm );
                return generateUUID();
            }
        } catch ( SQLException e ) {
            LOG.debug( "Error while generating a new UUID for the metadata: {}", e.getMessage() );
            throw new MetadataStoreException( "Error while generating a new UUID for the metadata: {}", e );
        } finally {
            close( rs );
            close( stm );
        }
        return uuid;

    }

    /**
     * Proves the availability of the identifier in the backend.
     * 
     * @param identifier
     *            that is proved, not <Code>null</Code>.
     * @return true, if the identifier is not found in the backend, otherwise false.
     * @throws MetadataStoreException
     */
    public boolean proveIdExistence( String identifier )
                            throws MetadataStoreException {
        PreparedStatement stm = null;
        ResultSet rs = null;
        boolean notAvailable = true;
        try {
            String s = "SELECT i.identifier FROM " + PostGISMappingsISODC.DatabaseTables.qp_identifier.name()
                       + " AS i WHERE i.identifier = ?;";
            stm = conn.prepareStatement( s );
            stm.setObject( 1, identifier );
            rs = stm.executeQuery();
            LOG.debug( s );
            if ( rs.next() ) {
                notAvailable = false;
            }

        } catch ( SQLException e ) {
            LOG.debug( "Error while proving the IDs stored in the backend: {}", e.getMessage() );
            throw new MetadataStoreException( "Error while proving the IDs stored in the backend: {}" + e.getMessage() );
        } finally {
            close( stm );
            close( rs );
        }
        return notAvailable;
    }

    public boolean checkUUIDCompliance( String uuid ) {

        char firstChar = uuid.charAt( 0 );
        Pattern p = Pattern.compile( "[0-9]" );
        Matcher m = p.matcher( "" + firstChar );
        if ( m.matches() ) {
            return false;
        }
        return true;
    }

}
