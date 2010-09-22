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

import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.metadata.persistence.genericmetadatastore.PostGISMappingsISODC;
import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig.IdentifierInspector;
import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig.IdentifierInspector.Param;
import org.slf4j.Logger;

/**
 * Inspects whether the fileIdentifier should be set when inserting a metadata or not and what consequences should
 * occur.
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

    private final List<String> idList;

    private String id;

    private String uuid;

    private FileIdentifierInspector( IdentifierInspector inspector, String connectionId ) {
        this.connectionId = connectionId;
        this.inspector = inspector;
        this.idList = new ArrayList<String>();
    }

    public static FileIdentifierInspector newInstance( IdentifierInspector inspector, String connectionId ) {
        return new FileIdentifierInspector( inspector, connectionId );
    }

    private boolean isFileIdentifierRejected() {
        if ( inspector == null ) {
            return false;
        } else {
            List<Param> paramList = inspector.getParam();

            for ( Param p : paramList ) {
                if ( p.getKey().equals( REJECT_EMPTY_FILE_IDENTIFIER ) ) {
                    return Boolean.getBoolean( p.getValue() );
                }
            }

            return false;
        }
    }

    /**
     * 
     * @param fi
     *            the fileIdentifier that should be determined for one metadata, can be <Code>null</Code>.
     * @param rsList
     *            the list of resourceIdentifier, not <Code>null</Code>.
     * @param id
     *            the id-attribute, can be <Code>null<Code>.
     * @param uuid
     *            the uuid-attribure, can be <Code>null</Code>.
     * @param isFileIdentifierExistenceDesired
     *            true, if the existence of the fileIdentifier is desired in backend.
     * @return the new fileIdentifier.
     * @throws MetadataStoreException
     */
    public List<String> determineFileIdentifier( String fi, List<String> rsList, String id, String uuid,
                                                 boolean isFileIdentifierExistenceDesired )
                            throws MetadataStoreException {
        this.id = id;
        this.uuid = uuid;
        this.idList.clear();
        if ( fi != null ) {
            if ( proveIdExistence( fi ) ) {
                LOG.info( "'{}' accepted as a valid fileIdentifier. ", fi );
                idList.add( fi );
                return idList;
            }
            if ( isFileIdentifierExistenceDesired ) {
                LOG.info( "'{}' is stored in backend and should be updated. ", fi );
                idList.add( fi );
                return idList;
            }
            LOG.debug( "The metadata with id '{}' is stored in backend, already! ", fi );
            throw new MetadataStoreException( "The metadata with id '" + fi + "' stored in backend, already!" );
        } else {
            // default behavior if there is no inspector provided
            if ( isFileIdentifierRejected() == false ) {
                if ( rsList.size() == 0 || id == null || uuid == null ) {

                    LOG.debug( "(DEFAULT) There is no Identifier available, so a new UUID will be generated..." );
                    idList.add( ParsingUtils.newInstance( connectionId ).generateUUID() );
                    LOG.debug( "(DEFAULT) The new FileIdentifier: " + idList );
                } else {
                    if ( rsList.size() == 0 && id != null ) {
                        LOG.debug( "(DEFAULT) The id attribute will be taken: {}", id );
                        idList.add( id );
                    } else if ( rsList.size() == 0 && uuid != null ) {
                        LOG.debug( "(DEFAULT) The uuid attribute will be taken: {}", uuid );
                        idList.add( uuid );
                    }
                    LOG.debug( "(DEFAULT) The ResourseIdentifier will be taken: {}", rsList.get( 0 ) );
                    idList.add( rsList.get( 0 ) );
                }
                return idList;
            } else {
                if ( rsList.size() == 0 ) {
                    LOG.debug( "This file must be rejected because the configuration-file requires at least a fileIdentifier or one resourceIdentifier!" );
                    throw new MetadataStoreException(
                                                      "The configuration-file requires at least a fileIdentifier or one resourceIdentifier!" );
                } else {
                    LOG.debug( "(DEFAULT) The ResourseIdentifier will be taken: {}", rsList.get( 0 ) );
                    idList.add( rsList.get( 0 ) );
                    return idList;
                }
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
    public boolean proveIdExistence( String identifier )
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

    public List<String> getIdList() {
        return idList;
    }

    public String getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

}
