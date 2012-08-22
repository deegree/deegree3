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
package org.deegree.ogcwebservices.csw.manager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.deegree.datatypes.time.TimeDuration;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.io.DBConnectionPool;
import org.deegree.io.DBPoolException;
import org.deegree.io.JDBCConnection;

/**
 * A harvest repository is a database that stores harvest requests and that caches basic record
 * informations to optimizes harvesting of large sources (e.g. other catalogues). This class
 * encapsulates access to this database.
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
class HarvestRepository {

    private static final ILogger LOG = LoggerFactory.getLogger( HarvestRepository.class );

    private static URL url = null;
    static {
        url = HarvestRepository.class.getResource( "/harvestrepository.properties" );
        if ( url == null ) {
            url = HarvestRepository.class.getResource( "harvestrepository.properties" );
        }
    }

    private static HarvestRepository repository = null;

    private static DBConnectionPool pool = DBConnectionPool.getInstance();

    private JDBCConnection jdbc = null;

    private Properties prop = null;

    // possible metadata source types
    static enum ResourceType {
        /**
         *
         */
        catalogue, /**
                     *
                     */
        service, /**
                     *
                     */
        csw_profile, /**
                         *
                         */
        FGDC, /**
                 *
                 */
        dublincore, /**
                     *
                     */
        unknown
    }

    /**
     * returns an instance of a <code>HarvestRepository</code>
     *
     * @return an instance of a <code>HarvestRepository</code>
     * @throws IOException
     */
    static HarvestRepository getInstance()
                            throws IOException {
        if ( repository == null ) {
            repository = new HarvestRepository();
        }
        return repository;
    }

    /**
     *
     */
    private HarvestRepository() throws IOException {
        prop = new Properties();
        InputStream is = url.openStream();
        prop.load( is );
        is.close();
        jdbc = new JDBCConnection( prop.getProperty( "harvester.Driver" ), prop.getProperty( "harvester.Url" ),
                                   prop.getProperty( "harvester.User" ), prop.getProperty( "harvester.Password" ),
                                   null, null, null );
    }

    /**
     * stores a harvest request
     *
     * @param request
     * @throws DBPoolException
     * @throws SQLException
     */
    synchronized void storeRequest( Harvest request )
                            throws DBPoolException, SQLException {

        LOG.logDebug( "storing harvest request into harvest repository ..." );

        Connection con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        try {
            con.setAutoCommit( false );
        } catch ( Exception ignore ) {
            // it's ignored
        }

        try {
            // insert into harvestsource table
            PreparedStatement ps = con.prepareStatement( prop.getProperty( "harvester.storeRequest1" ) );
            ps.setString( 1, request.getSource().toASCIIString() );
            TimeDuration td = request.getHarvestInterval();
            if ( td != null ) {
                ps.setLong( 2, td.getAsMilliSeconds() / 1000 );
            } else {
                ps.setLong( 2, -1 );
            }
            ps.setTimestamp( 3, new Timestamp( request.getStartTimestamp().getTime() ) );
            ps.setBoolean( 4, false );
            if ( request.getResourceType() == null ) {
                ps.setString( 5, "unknown" );
            } else {
                ps.setString( 5, request.getResourceType().toASCIIString() );
            }
            ps.execute();
            ps.close();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery( "select max(id) from harvestsource" );
            rs.next();
            int id1 = rs.getInt( 1 );
            rs.close();
            stmt.close();

            // insert into responsehandler table and assigns to harvestsource by
            // performing an insert into jt_source_responsehandler
            List<URI> list = request.getResponseHandler();
            for ( Iterator<URI> iter = list.iterator(); iter.hasNext(); ) {
                URI handler = iter.next();
                ps = con.prepareStatement( prop.getProperty( "harvester.storeRequest2" ) );
                ps.setString( 1, handler.toASCIIString() );
                ps.setBoolean( 2, handler.toASCIIString().toLowerCase().startsWith( "mailto:" ) );
                ps.execute();
                ps.close();

                stmt = con.createStatement();
                rs = stmt.executeQuery( "select max(id) from responsehandler" );
                rs.next();
                int id2 = rs.getInt( 1 );
                rs.close();
                stmt.close();

                ps = con.prepareStatement( prop.getProperty( "harvester.storeRequest3" ) );
                ps.setInt( 1, id1 );
                ps.setInt( 2, id2 );
                ps.execute();
                ps.close();
            }

            con.commit();
        } catch ( SQLException e ) {
            con.rollback();
            e.printStackTrace();
            throw new SQLException( getClass().getName() + " storeRequest(..) " + e.getMessage() );
        } catch ( Exception e ) {
            con.rollback();
            e.printStackTrace();
            throw new SQLException( getClass().getName() + " storeRequest(..) "
                                    + "could not insert harvest request into repository: " + e.getMessage() );
        } finally {
            pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        }
    }

    /**
     * drops a request from the backend
     *
     * @param source
     * @throws DBPoolException
     * @throws SQLException
     */
    synchronized void dropRequest( URI source )
                            throws DBPoolException, SQLException {

        LOG.logDebug( "dropping harvest request from harvest repository ..." );

        Connection con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        try {
            con.setAutoCommit( false );
        } catch ( Exception ignore ) {
            // it's ignored
        }

        try {
            PreparedStatement ps = con.prepareStatement( prop.getProperty( "harvester.dropRequest1" ) );
            ps.setString( 1, source.toASCIIString() );
            ResultSet rs = ps.executeQuery();
            rs.next();
            int id1 = rs.getInt( 1 );
            rs.close();
            ps = con.prepareStatement( prop.getProperty( "harvester.dropRequest2" ) );
            ps.setInt( 1, id1 );
            rs = ps.executeQuery();
            List<Integer> handlers = new ArrayList<Integer>();
            while ( rs.next() ) {
                handlers.add( rs.getInt( 1 ) );
            }
            rs.close();
            ps.close();
            // remove assigned entries from jointable
            ps = con.prepareStatement( prop.getProperty( "harvester.dropRequest3" ) );
            ps.setInt( 1, id1 );
            ps.execute();
            // remove assigend entries from reponse handler table
            for ( int i = 0; i < handlers.size(); i++ ) {
                Integer id = handlers.get( i );
                ps = con.prepareStatement( prop.getProperty( "harvester.dropRequest4" ) );
                ps.setInt( 1, id.intValue() );
                ps.execute();
                ps.close();
            }
            // remove records from cache table
            ps = con.prepareStatement( prop.getProperty( "harvester.dropRequest5" ) );
            ps.setInt( 1, id1 );
            ps.execute();
            ps.close();

            // remove root from harvest source table
            ps = con.prepareStatement( prop.getProperty( "harvester.dropRequest6" ) );
            ps.setInt( 1, id1 );
            ps.execute();
            ps.close();

            con.commit();

        } catch ( SQLException e ) {
            con.rollback();
            throw e;
        } catch ( Exception e ) {
            con.rollback();
            throw new SQLException( "could not frop request from repository: " + e.getMessage() );
        } finally {
            pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        }

    }

    /**
     * returns all sources registered to a harvest process
     *
     * @return all sources registered to a harvest process
     * @throws DBPoolException
     * @throws SQLException
     * @throws URISyntaxException
     */
    synchronized List<URI> getSources()
                            throws DBPoolException, SQLException, URISyntaxException {

        LOG.logDebug( "reading sources from harvest repository ..." );

        Connection con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        List<URI> sources = new ArrayList<URI>();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery( prop.getProperty( "harvester.getSources" ) );
            while ( rs.next() ) {
                sources.add( new URI( rs.getString( 1 ) ) );
            }
            rs.close();
            stmt.close();
        } catch ( SQLException e ) {
            throw e;
        } catch ( URISyntaxException e ) {
            throw e;
        } finally {
            pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        }

        return sources;
    }

    /**
     * returns the type of the passed source
     *
     * @param source
     * @return the type of the passed source
     * @throws DBPoolException
     * @throws SQLException
     */
    synchronized ResourceType getSourceType( URI source )
                            throws DBPoolException, SQLException {

        LOG.logDebug( "reading sources type for source: " + source );

        Connection con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        String s = null;

        try {
            PreparedStatement ps = con.prepareStatement( prop.getProperty( "harvester.getSourceType" ) );
            ps.setString( 1, source.toASCIIString() );
            ResultSet rs = ps.executeQuery();
            rs.next();
            s = rs.getString( 1 );
            rs.close();
            ps.close();
        } catch ( SQLException e ) {
            throw e;
        } finally {
            pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        }

        ResourceType st = ResourceType.unknown;

        if ( "csw:profile".equals( s ) ) {
            st = ResourceType.csw_profile;
        } else if ( "dublincore".equals( s ) ) {
            st = ResourceType.dublincore;
        } else if ( "FGDC".equals( s ) ) {
            st = ResourceType.FGDC;
        } else if ( "service".equals( s ) ) {
            st = ResourceType.service;
        } else if ( "catalogue".equals( s ) ) {
            st = ResourceType.catalogue;
        } else  if ( s.equals( "http://www.isotc211.org/schemas/2005/gmd" ) ) {
            st = ResourceType.csw_profile;
        }

        return st;
    }

    /**
     * returns true if last harvesting iteration for the passed source has been successful
     *
     * @param source
     *
     * @return <code>true</code> if last harvesting iteration for the passed source has been
     *         successful
     * @throws DBPoolException
     * @throws SQLException
     */
    synchronized boolean getStatus( URI source )
                            throws DBPoolException, SQLException {

        LOG.logDebug( "reading sources status for source: " + source );

        Connection con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        boolean status = false;

        try {
            PreparedStatement ps = con.prepareStatement( prop.getProperty( "harvester.getStatus" ) );
            ps.setString( 1, source.toASCIIString() );
            ResultSet rs = ps.executeQuery();
            rs.next();
            status = rs.getBoolean( 1 );
            rs.close();
            ps.close();
        } catch ( SQLException e ) {
            throw e;
        } finally {
            pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        }

        return status;
    }

    /**
     * returns the <code>Date</code> a source has been harvested successful the last time
     *
     * @param source
     * @return the <code>Date</code> a source has been harvested successful the last time
     * @throws DBPoolException
     * @throws SQLException
     */
    synchronized Date getLastHarvestingTimestamp( URI source )
                            throws DBPoolException, SQLException {

        LOG.logDebug( "reading sources last harvesting timestamp for source: " + source );

        Connection con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        Date date = null;
        try {
            PreparedStatement ps = con.prepareStatement( prop.getProperty( "harvester.getLastHarvestingTimestamp" ) );
            ps.setString( 1, source.toASCIIString() );
            ResultSet rs = ps.executeQuery();
            rs.next();
            Timestamp ts = rs.getTimestamp( 1 );
            rs.close();
            ps.close();
            if ( ts != null ) {
                date = new Date( ts.getTime() );
            }
        } catch ( SQLException e ) {
            throw e;
        } finally {
            pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        }
        return date;
    }

    /**
     * sets the timestamp when a source has been harvested successfully for the last time
     *
     * @param source
     * @param date
     * @throws DBPoolException
     * @throws SQLException
     */
    synchronized void setLastHarvestingTimestamp( URI source, Date date )
                            throws DBPoolException, SQLException {

        LOG.logDebug( "set timestamp for source: " + source + " last harvesting" );

        Connection con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        try {
            con.setAutoCommit( false );
        } catch ( Exception ignore ) {
            // it's ignored
        }
        try {
            PreparedStatement ps = con.prepareStatement( prop.getProperty( "harvester.setLastHarvestingTimestamp" ) );
            ps.setTimestamp( 1, new Timestamp( date.getTime() ) );
            ps.setString( 2, source.toASCIIString() );
            ps.execute();
            ps.close();
            con.commit();
        } catch ( SQLException e ) {
            con.rollback();
            throw e;
        } finally {
            pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        }
    }

    /**
     * returns the next Date a source shall be harvested
     *
     * @param source
     * @return the next Date a source shall be harvested
     * @throws DBPoolException
     * @throws SQLException
     */
    synchronized Date getNextHarvestingTimestamp( URI source )
                            throws DBPoolException, SQLException {
        LOG.logDebug( "reading timestamp for source: " + source + " next harvesting" );

        Connection con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        Date date = null;
        try {
            PreparedStatement ps = con.prepareStatement( prop.getProperty( "harvester.getNextHarvestingTimestamp" ) );
            ps.setString( 1, source.toASCIIString() );
            ResultSet rs = ps.executeQuery();
            rs.next();
            Timestamp ts = rs.getTimestamp( 1 );
            rs.close();
            ps.close();
            date = new Date( ts.getTime() );
        } catch ( SQLException e ) {
            throw e;
        } finally {
            pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        }
        return date;
    }

    /**
     * sets the next date a source shall be harvested
     *
     * @param source
     * @param date
     * @throws DBPoolException
     * @throws SQLException
     */
    synchronized void setNextHarvestingTimestamp( URI source, Date date )
                            throws DBPoolException, SQLException {

        LOG.logDebug( "set timestamp for source: " + source + " last harvesting" );

        Connection con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        try {
            con.setAutoCommit( false );
        } catch ( Exception ignore ) {
            // it's ignored
        }
        try {
            PreparedStatement ps = con.prepareStatement( prop.getProperty( "harvester.setNextHarvestingTimestamp" ) );
            ps.setTimestamp( 1, new Timestamp( date.getTime() ) );
            ps.setString( 2, source.toASCIIString() );
            ps.execute();
            ps.close();
            con.commit();
        } catch ( SQLException e ) {
            con.rollback();
        } finally {
            pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        }

    }

    /**
     * returns the interval in
     *
     * @param source
     * @return the interval
     * @throws DBPoolException
     * @throws SQLException
     */
    synchronized long getHarvestInterval( URI source )
                            throws DBPoolException, SQLException {

        LOG.logDebug( "reading harvest interval for source: " + source );

        Connection con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        long interval = 0;
        try {
            PreparedStatement ps = con.prepareStatement( prop.getProperty( "harvester.getHarvestInterval" ) );
            ps.setString( 1, source.toASCIIString() );
            ResultSet rs = ps.executeQuery();
            rs.next();
            interval = rs.getLong( 1 ) * 1000l;
            rs.close();
            ps.close();
        } catch ( SQLException e ) {
            throw e;
        } finally {
            pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        }

        return interval;
    }

    /**
     * returns a list
     *
     * @param source
     * @return the list
     * @throws DBPoolException
     * @throws SQLException
     * @throws URISyntaxException
     */
    synchronized List<ResponseHandler> getResponseHandlers( URI source )
                            throws DBPoolException, SQLException, URISyntaxException {

        LOG.logDebug( "reading response handler for source: " + source );

        Connection con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        List<ResponseHandler> list = new ArrayList<ResponseHandler>();
        try {
            PreparedStatement ps = con.prepareStatement( prop.getProperty( "harvester.getResponseHandlers1" ) );
            ps.setString( 1, source.toASCIIString() );
            ResultSet rs = ps.executeQuery();
            rs.next();
            int id1 = rs.getInt( 1 );
            rs.close();
            ps.close();

            ps = con.prepareStatement( prop.getProperty( "harvester.getResponseHandlers2" ) );
            ps.setInt( 1, id1 );
            rs = ps.executeQuery();
            StringBuffer sb = new StringBuffer( " (" );
            int kk = 0;
            while ( rs.next() ) {
                kk++;
                sb.append( rs.getInt( 1 ) ).append( ',' );
            }
            rs.close();
            ps.close();

            if ( kk > 0 ) {
                // just access response handler informations if available
                String s = sb.substring( 0, sb.length() - 1 ) + ')';
                ps = con.prepareStatement( prop.getProperty( "harvester.getResponseHandlers3" ) + s );
                rs = ps.executeQuery();

                while ( rs.next() ) {
                    String addr = rs.getString( 1 );
                    boolean isMail = rs.getBoolean( 2 );
                    list.add( new ResponseHandler( new URI( addr ), isMail ) );
                }
                rs.close();
                ps.close();
            }
        } catch ( SQLException e ) {
            throw e;
        } catch ( URISyntaxException e ) {
            throw e;
        } finally {
            pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        }

        return list;
    }

    /**
     * returns a <code>Record</code> from the harvesters cache. A instance of a
     * <code>Record</code> includes its fileIdentifier,the datestamp when it has been changed for
     * the last time and the source it belongs too.
     *
     * @param source
     * @param fileIdentifier
     * @return a <code>Record</code> from the harvesters cache
     * @throws DBPoolException
     * @throws SQLException
     */
    synchronized Record getRecordByID( URI source, String fileIdentifier )
                            throws DBPoolException, SQLException {

        LOG.logDebug( "reading record: " + fileIdentifier + " from harvest cache" );

        Connection con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        Record record = null;
        try {
            PreparedStatement ps = con.prepareStatement( prop.getProperty( "harvester.getRecordByID1" ) );
            ps.setString( 1, source.toASCIIString() );
            ResultSet rs = ps.executeQuery();
            rs.next();
            int id = rs.getInt( 1 );
            rs.close();
            ps.close();

            ps = con.prepareStatement( prop.getProperty( "harvester.getRecordByID2" ) );
            ps.setInt( 1, id );
            ps.setString( 2, fileIdentifier );
            rs = ps.executeQuery();

            if ( rs.next() ) {
                Date date = rs.getDate( 1 );
                record = new Record( id, date, fileIdentifier, source );
            }
            rs.close();
            ps.close();
        } catch ( SQLException e ) {
            throw e;
        } finally {
            pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        }

        return record;
    }

    /**
     * stores a record into the cache table used by the harvester
     *
     * @param record
     * @throws DBPoolException
     * @throws SQLException
     */
    synchronized void storeRecord( Record record )
                            throws DBPoolException, SQLException {

        LOG.logDebug( "storing record in cache; fileIdentifier: " + record.getFileIdentifier() );

        Connection con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        try {
            con.setAutoCommit( false );
        } catch ( Exception ignore ) {
            // it's ignored
        }
        try {
            String fid = record.getFileIdentifier();
            PreparedStatement ps = con.prepareStatement( prop.getProperty( "harvester.storeRecord1" ) );
            ps.setString( 1, fid );
            ResultSet rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt( 1 );
            if ( count == 0 ) {
                ps = con.prepareStatement( prop.getProperty( "harvester.storeRecord2" ) );
                ps.setInt( 1, getSourceID( record.getSource() ) );
                ps.setString( 2, fid );
                ps.setTimestamp( 3, new Timestamp( record.getDatestamp().getTime() ) );
                ps.execute();
                ps.close();

                con.commit();
            }

        } catch ( SQLException e ) {
            con.rollback();
            throw e;
        } catch ( Exception e ) {
            con.rollback();
            throw new SQLException( "could not insert harvest request " + "into repository: " + e.getMessage() );
        } finally {
            pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        }

    }

    /**
     * updates a record within the cache table used by the harvester
     *
     * @param record
     * @throws DBPoolException
     * @throws SQLException
     */
    synchronized void updateRecord( Record record )
                            throws DBPoolException, SQLException {

        LOG.logDebug( "updating record in cache; fileIdentifier: " + record.getFileIdentifier() );

        Connection con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        try {
            con.setAutoCommit( false );
        } catch ( Exception ignore ) {
            // it's ignored
        }
        try {
            PreparedStatement ps = con.prepareStatement( prop.getProperty( "harvester.updateRecord" ) );
            ps.setDate( 1, new java.sql.Date( record.getDatestamp().getTime() ) );
            ps.setString( 2, record.getFileIdentifier() );
            ps.setInt( 3, record.getSourceId() );
            ps.execute();
            ps.close();

            con.commit();

        } catch ( SQLException e ) {
            con.rollback();
            throw e;
        } catch ( Exception e ) {
            con.rollback();
            throw new SQLException( "could not insert harvest request " + "into repository: " + e.getMessage() );
        } finally {
            pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        }

    }

    /**
     * drops a record from the cache table used by the harvester
     *
     * @param record
     * @throws DBPoolException
     * @throws SQLException
     */
    synchronized void dropRecord( Record record )
                            throws DBPoolException, SQLException {

        LOG.logDebug( "deleting record from cache; fileIdentifier: " + record.getFileIdentifier() );

        Connection con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        try {
            con.setAutoCommit( false );
        } catch ( Exception ignore ) {
            // it's ignored
        }
        try {

            PreparedStatement ps = con.prepareStatement( prop.getProperty( "harvester.dropRecord" ) );
            ps.setString( 1, record.getFileIdentifier() );
            ps.setInt( 2, record.getSourceId() );
            ps.execute();
            ps.close();

            con.commit();

        } catch ( SQLException e ) {
            con.rollback();
            throw e;
        } catch ( Exception e ) {
            con.rollback();
            throw new SQLException( "could not insert harvest request " + "into repository: " + e.getMessage() );
        } finally {
            pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        }

    }

    /**
     * returns fileidentifiers of all records assigend to a source from the harvest cache
     *
     * @param source
     * @return fileidentifiers of all records assigend to a source from the harvest cache
     * @throws DBPoolException
     * @throws SQLException
     */
    synchronized List<String> getAllRecords( URI source )
                            throws DBPoolException, SQLException {

        LOG.logDebug( "getting list of all record fileidentifiers for source: " + source + " from cache" );

        List<String> fileIds = new ArrayList<String>( 10000 );

        Connection con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        try {
            PreparedStatement ps = con.prepareStatement( prop.getProperty( "harvester.getAllRecords1" ) );
            ps.setString( 1, source.toASCIIString() );
            ResultSet rs = ps.executeQuery();
            rs.next();
            int id = rs.getInt( 1 );
            rs.close();
            ps.close();

            ps = con.prepareStatement( prop.getProperty( "harvester.getAllRecords2" ) );
            ps.setInt( 1, id );
            rs = ps.executeQuery();
            while ( rs.next() ) {
                fileIds.add( rs.getString( 1 ) );
            }
            rs.close();
            ps.close();
        } catch ( SQLException e ) {
            throw e;
        } finally {
            pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        }

        return fileIds;
    }

    /**
     * returns the row ID of the passed source
     *
     * @param source
     * @return the row ID of the passed source
     * @throws DBPoolException
     * @throws SQLException
     */
    synchronized int getSourceID( URI source )
                            throws DBPoolException, SQLException {
        LOG.logDebug( "reading row ID of source: " + source );

        Connection con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        int id = -1;
        try {
            PreparedStatement ps = con.prepareStatement( prop.getProperty( "harvester.getSourceID" ) );
            ps.setString( 1, source.toASCIIString() );
            ResultSet rs = ps.executeQuery();
            rs.next();
            id = rs.getInt( 1 );
            rs.close();
            ps.close();
        } catch ( SQLException e ) {
            throw e;
        } finally {
            pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        }

        return id;
    }

    /**
     * returs true is a harvesting shall be forced outside the regular harvesting interval
     *
     * @param source
     * @return true if a CSW shall be harvested outside the regular harvesting interval
     * @throws DBPoolException
     */
    synchronized boolean shallForceHarvesting( URI source )
                            throws DBPoolException {
        Connection con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        boolean force = false;
        try {
            PreparedStatement ps = con.prepareStatement( prop.getProperty( "harvester.forceHarvesting" ) );
            ps.setString( 1, source.toASCIIString() );
            ResultSet rs = ps.executeQuery();
            rs.next();
            force = rs.getInt( 1 ) == 1;
            rs.close();
            ps.close();
        } catch ( Exception e ) {
            // TODO
            // this is because downward compliance; older CSW does not know the requested field
            // harvestsource.forceharvesting
            // throw e;
        } finally {
            pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        }
        return force;
    }

    /**
     * inner class for encapsulating response handler informations
     *
     * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
     * @author last edited by: $Author$
     *
     * @version $Revision$, $Date$
     */
    class ResponseHandler {

        private URI uri = null;

        private boolean isMailAddress = false;

        /**
         * @param uri
         * @param isMailAddress
         */
        ResponseHandler( URI uri, boolean isMailAddress ) {
            this.uri = uri;
            this.isMailAddress = isMailAddress;
        }

        /**
         * @return true, if it is
         */
        boolean isMailAddress() {
            return isMailAddress;
        }

        /**
         * @return uri
         */
        URI getUri() {
            return uri;
        }

    }

    /**
     *
     *
     *
     * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
     * @author last edited by: $Author$
     *
     * @version $Revision$, $Date$
     *
     */
    public class Record {

        private Date datestamp = null;

        private String fileIdentifier = null;

        private URI source = null;

        private int sourceId;

        /**
         * @param sourceId
         * @param datestamp
         * @param fileIdentifier
         * @param source
         */
        public Record( int sourceId, Date datestamp, String fileIdentifier, URI source ) {
            this.datestamp = datestamp;
            this.fileIdentifier = fileIdentifier;
            this.source = source;
            this.sourceId = sourceId;
        }

        /**
         * @return datestamp
         */
        public Date getDatestamp() {
            return datestamp;
        }

        /**
         * @return fileIdentifier
         */
        public String getFileIdentifier() {
            return fileIdentifier;
        }

        /**
         * @return source
         */
        public URI getSource() {
            return source;
        }

        /**
         * @return sourceId
         */
        public int getSourceId() {
            return sourceId;
        }

    }

}
