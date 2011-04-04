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
package org.deegree.metadata.persistence.iso.inspectors;

import static org.deegree.commons.utils.JDBCUtils.close;
import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deegree.commons.jdbc.ConnectionManager.Type;
import org.deegree.metadata.i18n.Messages;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.iso.MSSQLMappingsISODC;
import org.deegree.metadata.persistence.iso.PostGISMappingsISODC;
import org.deegree.protocol.csw.MetadataStoreException;
import org.slf4j.Logger;

/**
 * <Code>ParsingUtils</Code>
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class IdUtils {

    private static final Logger LOG = getLogger( IdUtils.class );

    private final Connection conn;

    private List<String> idList;

    private String mainTable;

    private String fileIdColumn;

    IdUtils( Connection conn, Type connectionType ) {
        this.conn = conn;
        idList = Collections.synchronizedList( new ArrayList<String>() );
        if ( connectionType == Type.MSSQL ) {
            mainTable = MSSQLMappingsISODC.DatabaseTables.idxtb_main.name();
            fileIdColumn = MSSQLMappingsISODC.CommonColumnNames.fileidentifier.name();
        }
        if ( connectionType == Type.PostgreSQL ) {
            mainTable = PostGISMappingsISODC.DatabaseTables.idxtb_main.name();
            fileIdColumn = PostGISMappingsISODC.CommonColumnNames.fileidentifier.name();
        }
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
    String generateUUID()
                            throws MetadataInspectorException {

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

            String compareIdentifier = "SELECT " + fileIdColumn + " FROM " + mainTable + " WHERE " + fileIdColumn
                                       + " = ?";
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
            String msg = Messages.getMessage( "ERROR_SQL", stm.toString(), e.getMessage() );
            LOG.debug( msg );
            throw new MetadataInspectorException( msg );
        } finally {
            close( rs );
            close( stm );
        }
        return uuid;

    }

    boolean checkUUIDCompliance( String uuid ) {

        char firstChar = uuid.charAt( 0 );
        Pattern p = Pattern.compile( "[0-9]" );
        Matcher m = p.matcher( "" + firstChar );
        if ( m.matches() ) {
            return false;
        }
        return true;
    }

    boolean checkUUIDCompliance( List<String> list ) {

        for ( String s : list ) {
            boolean isUUID = checkUUIDCompliance( s );
            if ( !isUUID ) {
                return false;
            }
        }
        return true;
    }
}