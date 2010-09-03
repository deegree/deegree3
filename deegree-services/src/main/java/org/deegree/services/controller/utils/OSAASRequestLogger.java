//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.services.controller.utils;

import static org.deegree.commons.jdbc.ConnectionManager.getConnection;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.deegree.services.controller.Credentials;
import org.deegree.services.controller.RequestLogger;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OSAASRequestLogger implements RequestLogger {

    private static final Logger LOG = getLogger( OSAASRequestLogger.class );

    private String connid;

    private String table;

    /**
     * @param o
     * @throws InstantiationException
     */
    public OSAASRequestLogger( Object o ) throws InstantiationException {
        Element e = (Element) o;
        NodeList connidElem = e.getElementsByTagName( "OSAASConnectionId" );
        if ( connidElem == null || connidElem.getLength() == 0 ) {
            LOG.info( "No OSAASConnectionId element was provided in the configuration of the request logger." );
            throw new InstantiationException( "No connection id was configured." );
        }
        connid = connidElem.item( 0 ).getTextContent();
        NodeList tableElem = e.getElementsByTagName( "OSAASTable" );
        if ( tableElem == null || tableElem.getLength() == 0 ) {
            LOG.info( "No OSAASTable element was provided in the configuration of the request logger." );
            throw new InstantiationException( "No table was configured." );
        }
        table = tableElem.item( 0 ).getTextContent();
    }

    public void logKVP( String address, String queryString, long startTime, long endTime, Credentials creds ) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection( connid );
            stmt = conn.prepareStatement( "insert into " + table + "(wfsidintern,wfsidextern,username,starttime"
                                          + ",endtime,requestformat,rawrequest) values (?,?,?,?,?,?,?)" );
            String[] ss = address.split( "\\?" );
            stmt.setString( 1, ss[0] );
            stmt.setString( 2, ss[1] );
            stmt.setString( 3, creds.getUser() );
            stmt.setTimestamp( 4, new Timestamp( startTime ) );
            stmt.setTimestamp( 5, new Timestamp( endTime ) );
            stmt.setInt( 6, 2 );
            stmt.setBytes( 7, queryString.getBytes() ); // it's url encoded anyway
            LOG.debug( "Logging KVP request with statement:" );
            LOG.debug( "{}", conn );
            stmt.executeUpdate();
        } catch ( SQLException e ) {
            e.printStackTrace();
            LOG.debug( "Could not log KVP request: {}", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } finally {
            if ( stmt != null ) {
                try {
                    stmt.close();
                } catch ( SQLException e ) {
                    LOG.trace( "Stack trace:", e );
                }
            }
            if ( conn != null ) {
                try {
                    conn.close();
                } catch ( SQLException e ) {
                    LOG.trace( "Stack trace:", e );
                }
            }
        }
    }

    public void logXML( String address, File logFile, long startTime, long endTime, Credentials creds ) {
        Connection conn = null;
        PreparedStatement stmt = null;
        FileInputStream is = null;
        try {
            conn = getConnection( connid );
            stmt = conn.prepareStatement( "insert into " + table + "(wfsidintern,wfsidextern,username,starttime"
                                          + ",endtime,requestformat,rawrequest) values (?,?,?,?,?,?,?)" );
            String[] ss = address.split( "\\?" );
            stmt.setString( 1, ss[0] );
            stmt.setString( 2, ss[1] );
            stmt.setString( 3, creds.getUser() );
            stmt.setTimestamp( 4, new Timestamp( startTime ) );
            stmt.setTimestamp( 5, new Timestamp( endTime ) );
            stmt.setInt( 6, 1 );
            is = new FileInputStream( logFile );
            // the methods taking a long or not taking the length at all are not implemented in postgres drivers!
            stmt.setBinaryStream( 7, is, (int) logFile.length() );
            LOG.debug( "Logging XML request with statement:" );
            LOG.debug( "{}", conn );
            stmt.executeUpdate();

            logFile.delete();
        } catch ( SQLException e ) {
            e.printStackTrace();
            LOG.debug( "Could not log XML request: {}", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } catch ( FileNotFoundException e ) {
            LOG.debug( "XML log file '{}' could not be found: {}", logFile, e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } finally {
            if ( stmt != null ) {
                try {
                    stmt.close();
                } catch ( SQLException e ) {
                    LOG.trace( "Stack trace:", e );
                }
            }
            if ( conn != null ) {
                try {
                    conn.close();
                } catch ( SQLException e ) {
                    LOG.trace( "Stack trace:", e );
                }
            }
        }
    }

}
