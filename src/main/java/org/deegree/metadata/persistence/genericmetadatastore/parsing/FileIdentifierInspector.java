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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jj2000.j2k.NotImplementedError;

import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.metadata.persistence.genericmetadatastore.PostGISMappingsISODC;
import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig.IdentifierInspector;
import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig.IdentifierInspector.Param;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FileIdentifierInspector {
    private static final Logger LOG = getLogger( FileIdentifierInspector.class );

    private static final String REJECT_EMPTY_FILE_IDENTIFIER = "rejectEmptyFileIdentifier";

    private final String connectionId;

    private final IdentifierInspector inspector;

    // public FileIdentifierInspector( String connectionId ) {
    // this.connectionId = connectionId;
    // }
    private FileIdentifierInspector( IdentifierInspector inspector, String connectionId ) {
        this.connectionId = connectionId;
        this.inspector = inspector;
    }

    public static FileIdentifierInspector newInstance( IdentifierInspector inspector, String connectionId ) {
        return new FileIdentifierInspector( inspector, connectionId );
    }

    public boolean isFileIdentifierRejected() {
        // TODO prove null for inspector
        List<Param> paramList = inspector.getParam();

        for ( Param p : paramList ) {
            if ( p.getKey().equals( REJECT_EMPTY_FILE_IDENTIFIER ) ) {
                return Boolean.getBoolean( p.getValue() );
            }
        }

        return false;
    }

    /**
     * 
     * @param fi
     *            the fileIdentifier that should be determined, can be <Code>null</Code>.
     * @param rsList
     *            the list of resourceIdentifier, not <Code>null</Code>.
     * @return the new fileIdentifier.
     * @throws MetadataStoreException
     */
    public List<String> determineFileIdentifier( String fi, List<String> rsList )
                            throws MetadataStoreException {
        List<String> idList = new ArrayList<String>();
        if ( fi != null ) {
            if ( proveIdExistence( fi ) ) {
                LOG.info( "'{}' accepted as a valid fileIdentifier. ", fi );
                idList.add( fi );
                return idList;
            }
            LOG.debug( "'{}' is stored in backend, already! ", fi );
            throw new MetadataStoreException( fi + " stored in backend, already!" );
        } else {
            // default behavior if there is no inspector provided
            if ( inspector == null ) {
                if ( rsList.size() == 0 ) {
                    LOG.debug( "(DEFAULT) A new UUID will be generated..." );
                    idList.add( generateUUID() );
                    LOG.debug( "(DEFAULT) The new FileIdentifier: " + idList );
                } else {
                    LOG.debug( "(DEFAULT) The ResourseIdentifier will be taken. " );
                    idList.add( rsList.get( 0 ) );
                }
                return idList;
            } else {
                LOG.debug( "(CUSTOM) Here should be the implementation of the custom handled ID. " );
                throw new NotImplementedError(
                                               "If there is a custom configuration for the identifier problematic, this should be implemented!" );
            }
        }
    }

    /**
     * Proves the availability of the identifier in the backend.
     * 
     * @param identifier
     *            that is proved, not <Code>null</Code>.
     * @return true, if the identifier is not found in the backend, otherwise false.
     * @throws MetadataStoreException
     */
    private boolean proveIdExistence( String identifier )
                            throws MetadataStoreException {
        PreparedStatement stm = null;
        ResultSet rs = null;
        Connection conn = null;
        boolean notAvailable = true;
        try {
            conn = ConnectionManager.getConnection( connectionId );
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
    private String generateUUID()
                            throws MetadataStoreException {

        ResultSet rs = null;
        PreparedStatement stm = null;
        Connection conn = null;
        String uuid = null;

        try {

            conn = ConnectionManager.getConnection( connectionId );

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
                close( rs, stm, conn, LOG );
                return generateUUID();
            }
        } catch ( SQLException e ) {
            LOG.debug( "Error while generating a new UUID for the metadata: {}", e.getMessage() );
            throw new MetadataStoreException( "Error while generating a new UUID for the metadata: {}", e );
        } finally {
            close( rs, stm, conn, LOG );
        }
        return uuid;

    }

}
