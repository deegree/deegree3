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
package org.deegree.security.drm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.DataBaseIDGenerator;
import org.deegree.framework.util.StringPair;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLTools;
import org.deegree.io.DBConnectionPool;
import org.deegree.io.IDGeneratorFactory;
import org.deegree.model.filterencoding.AbstractFilter;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.FilterConstructionException;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.drm.model.Group;
import org.deegree.security.drm.model.Privilege;
import org.deegree.security.drm.model.Right;
import org.deegree.security.drm.model.RightType;
import org.deegree.security.drm.model.Role;
import org.deegree.security.drm.model.SecurableObject;
import org.deegree.security.drm.model.SecuredObject;
import org.deegree.security.drm.model.Service;
import org.deegree.security.drm.model.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is an implementation of a <code>Registry</code> using an SQL-Database (via JDBC) as backend.
 * 
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @version $Revision$
 */
public final class SQLRegistry implements SecurityRegistry {

    private static final ILogger LOG = LoggerFactory.getLogger( SQLRegistry.class );

    private String dbDriver;

    private String dbName;

    private String dbUser;

    private String dbPassword;

    /** Exclusive connection for a transaction (only one at a time). */
    private Connection transactionalConnection = null;

    public void clean( SecurityTransaction transaction )
                            throws GeneralSecurityException {

        PreparedStatement pstmt = null;
        try {
            BufferedReader reader = new BufferedReader(
                                                        new InputStreamReader(
                                                                               SQLRegistry.class.getResourceAsStream( "clean.sql" ) ) );
            StringBuffer sb = new StringBuffer( 5000 );
            String line = null;
            while ( ( line = reader.readLine() ) != null ) {
                sb.append( line );
            }
            String tmp = sb.toString();
            String[] commands = StringTools.toArray( tmp, ";", false );
            for ( int i = 0; i < commands.length; i++ ) {
                String command = commands[i].trim();
                if ( !command.equals( "" ) ) {
                    pstmt = transactionalConnection.prepareStatement( command );
                    pstmt.executeUpdate();
                    closeStatement( pstmt );
                    pstmt = null;
                }
            }
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.clean() failed. Rollback performed. " + "Error message: "
                                                + e.getMessage() );
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( "SQLRegistry.clean() failed. Problem reading sql command file. "
                                                + "Error message: " + e.getMessage() );
        }
    }

    /**
     * Adds a new user account to the <code>Registry</code>.
     * 
     * @param transaction
     * @param name
     * @throws GeneralSecurityException
     *             this is a <code>DuplicateException</code> if the group already existed
     */
    public User registerUser( SecurityTransaction transaction, String name, String password, String lastName,
                              String firstName, String emailAddress )
                            throws GeneralSecurityException {
        try {
            getUserByName( transaction, name );
            throw new DuplicateException( "Registration of user '" + name + "' failed! A user with "
                                          + "this name already exists." );
        } catch ( UnknownException e ) {
            // then it's no duplicate
        }

        User user = new User( getID( transaction, "SEC_SECURABLE_OBJECTS" ), name, password, firstName, lastName,
                              emailAddress, this );
        PreparedStatement pstmt = null;

        try {
            pstmt = transactionalConnection.prepareStatement( "INSERT INTO SEC_SECURABLE_OBJECTS (ID,NAME,TITLE) VALUES (?,?,?)" );
            pstmt.setInt( 1, user.getID() );
            pstmt.setString( 2, user.getName() );
            pstmt.setString( 3, user.getTitle() );
            pstmt.executeUpdate();
            closeStatement( pstmt );
            pstmt = null;

            pstmt = transactionalConnection.prepareStatement( "INSERT INTO SEC_USERS (ID,PASSWORD,FIRSTNAME,LASTNAME,EMAIL) VALUES (?,?,?,?,?)" );
            pstmt.setInt( 1, user.getID() );
            pstmt.setString( 2, password );
            pstmt.setString( 3, user.getFirstName() );
            pstmt.setString( 4, user.getLastName() );
            pstmt.setString( 5, user.getEmailAddress() );
            pstmt.executeUpdate();
            closeStatement( pstmt );
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.registerUser() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
        return user;
    }

    /**
     * Removes an existing <code>User<code> from the <code>Registry</code> (including its relations).
     * 
     * @param transaction
     * @param user
     * @throws GeneralSecurityException
     */
    public void deregisterUser( SecurityTransaction transaction, User user )
                            throws GeneralSecurityException {
        PreparedStatement pstmt = null;

        try {
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_USERS_GROUPS WHERE FK_USERS=?" );
            pstmt.setInt( 1, user.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_USERS_ROLES WHERE FK_USERS=?" );
            pstmt.setInt( 1, user.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_USERS WHERE ID=?" );
            pstmt.setInt( 1, user.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_ROLES_SECOBJECTS WHERE FK_SECURABLE_OBJECTS=?" );
            pstmt.setInt( 1, user.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_SECURABLE_OBJECTS WHERE ID=?" );
            pstmt.setInt( 1, user.getID() );
            pstmt.executeUpdate();
            pstmt.close();
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.deregisterUser() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
    }

    /**
     * Updates the metadata (name, email, etc.) of a <code>User</code> in the <code>Registry</code>.
     * 
     * @throws GeneralSecurityException
     *             this is a <code>DuplicateException</code> if a user with the new name already existed
     */
    public void updateUser( SecurityTransaction transaction, User user )
                            throws GeneralSecurityException {

        PreparedStatement pstmt = null;

        try {
            pstmt = transactionalConnection.prepareStatement( "UPDATE SEC_SECURABLE_OBJECTS SET NAME=?,TITLE=? WHERE ID=?" );
            pstmt.setString( 1, user.getName() );
            pstmt.setString( 2, user.getTitle() );
            pstmt.setInt( 3, user.getID() );
            pstmt.executeUpdate();
            closeStatement( pstmt );
            pstmt = null;

            pstmt = transactionalConnection.prepareStatement( "UPDATE SEC_USERS SET PASSWORD=?,FIRSTNAME=?,LASTNAME=?,EMAIL=? WHERE ID=?" );
            pstmt.setString( 1, user.getPassword() );
            pstmt.setString( 2, user.getFirstName() );
            pstmt.setString( 3, user.getLastName() );
            pstmt.setString( 4, user.getEmailAddress() );
            pstmt.setInt( 5, user.getID() );
            pstmt.executeUpdate();
            closeStatement( pstmt );
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.registerUser() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
    }

    /**
     * Retrieves a <code>User</code> from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @param name
     * @throws GeneralSecurityException
     *             this is an <code>UnknownException</code> if the user is not known to the <code>Registry</code>
     * 
     */
    public User getUserByName( SecurityAccess securityAccess, String name )
                            throws GeneralSecurityException {
        User user = null;
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT SEC_USERS.ID,SEC_USERS.PASSWORD,SEC_USERS.FIRSTNAME,SEC_USERS.LASTNAME,SEC_USERS.EMAIL "
                     + "FROM SEC_USERS,SEC_SECURABLE_OBJECTS " + "WHERE SEC_USERS.ID=SEC_SECURABLE_OBJECTS.ID AND "
                     + "SEC_SECURABLE_OBJECTS.NAME=?";
        try {
            pstmt = con.prepareStatement( sql );
            pstmt.setString( 1, name );
            rs = pstmt.executeQuery();
            if ( rs.next() ) {
                user = new User( rs.getInt( 1 ), name, rs.getString( 2 ), rs.getString( 3 ), rs.getString( 4 ),
                                 rs.getString( 5 ), this );
            } else {
                throw new UnknownException( "Lookup of user '" + name
                                            + "' failed! A user with this name does not exist." );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }

        return user;
    }

    /**
     * Retrieves a <code>User</code> from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @param id
     * @throws GeneralSecurityException
     *             this is an <code>UnknownException</code> if the user is not known to the <code>Registry</code>
     */
    public User getUserById( SecurityAccess securityAccess, int id )
                            throws GeneralSecurityException {
        User user = null;
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT SEC_SECURABLE_OBJECTS.NAME,"
                     + "SEC_USERS.PASSWORD,SEC_USERS.FIRSTNAME,SEC_USERS.LASTNAME,"
                     + "SEC_USERS.EMAIL FROM SEC_USERS,SEC_SECURABLE_OBJECTS "
                     + "WHERE SEC_SECURABLE_OBJECTS.ID=? AND SEC_USERS.ID=SEC_SECURABLE_OBJECTS.ID";
        try {
            pstmt = con.prepareStatement( sql );
            pstmt.setInt( 1, id );
            rs = pstmt.executeQuery();
            if ( rs.next() ) {
                user = new User( id, rs.getString( 1 ), rs.getString( 2 ), rs.getString( 3 ), rs.getString( 4 ),
                                 rs.getString( 5 ), this );
            } else {
                throw new UnknownException( "Lookup of user with id: " + id
                                            + " failed! A user with this id does not exist." );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return user;
    }

    /**
     * Retrieves all <code>User</code> s from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @throws GeneralSecurityException
     */
    public User[] getAllUsers( SecurityAccess securityAccess )
                            throws GeneralSecurityException {
        ArrayList<User> users = new ArrayList<User>( 500 );
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT SEC_USERS.ID,SEC_SECURABLE_OBJECTS.NAME,SEC_USERS.PASSWORD,SEC_USERS.FIRSTNAME,SEC_USERS.LASTNAME,SEC_USERS.EMAIL "
                     + "FROM SEC_USERS,SEC_SECURABLE_OBJECTS WHERE SEC_USERS.ID=SEC_SECURABLE_OBJECTS.ID";
        try {
            pstmt = con.prepareStatement( sql );
            rs = pstmt.executeQuery();
            while ( rs.next() ) {
                users.add( new User( rs.getInt( 1 ), rs.getString( 2 ), rs.getString( 3 ), rs.getString( 4 ),
                                     rs.getString( 5 ), rs.getString( 6 ), this ) );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return users.toArray( new User[users.size()] );
    }

    /**
     * Retrieves all <code>Users</code> s from the <code>Registry</code> that are associated DIRECTLY (i.e. not via
     * group memberships) with a given <code>Role</code>.
     * 
     * @param securityAccess
     * @param role
     * @throws GeneralSecurityException
     */
    public User[] getUsersWithRole( SecurityAccess securityAccess, Role role )
                            throws GeneralSecurityException {
        ArrayList<User> users = new ArrayList<User>( 500 );
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT SEC_USERS.ID,SEC_SECURABLE_OBJECTS.NAME,SEC_USERS.PASSWORD,SEC_USERS.FIRSTNAME,SEC_USERS.LASTNAME,"
                     + "SEC_USERS.EMAIL "
                     + "FROM SEC_USERS,SEC_SECURABLE_OBJECTS,SEC_JT_USERS_ROLES "
                     + "WHERE SEC_SECURABLE_OBJECTS.ID=SEC_USERS.ID AND SEC_JT_USERS_ROLES.FK_USERS=SEC_USERS.ID"
                     + " AND SEC_JT_USERS_ROLES.FK_ROLES=?";
        try {
            pstmt = con.prepareStatement( sql );
            pstmt.setInt( 1, role.getID() );
            rs = pstmt.executeQuery();
            while ( rs.next() ) {
                users.add( new User( rs.getInt( 1 ), rs.getString( 2 ), rs.getString( 3 ), rs.getString( 4 ),
                                     rs.getString( 5 ), rs.getString( 6 ), this ) );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return users.toArray( new User[users.size()] );
    }

    /**
     * Retrieves all <code>User</code> s from the <code>Registry</code> that belong to the given <code>Group</code>
     * DIRECTLY (i.e. not via inheritance).
     * 
     * @param securityAccess
     * @param group
     * @throws GeneralSecurityException
     */
    public User[] getUsersInGroup( SecurityAccess securityAccess, Group group )
                            throws GeneralSecurityException {
        ArrayList<User> users = new ArrayList<User>( 500 );
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT SEC_USERS.ID,SEC_SECURABLE_OBJECTS.NAME,SEC_USERS.PASSWORD,SEC_USERS.FIRSTNAME,SEC_USERS.LASTNAME,"
                     + "SEC_USERS.EMAIL "
                     + "FROM SEC_USERS,SEC_SECURABLE_OBJECTS,SEC_JT_USERS_GROUPS "
                     + "WHERE SEC_SECURABLE_OBJECTS.ID=SEC_USERS.ID AND SEC_JT_USERS_GROUPS.FK_USERS=SEC_USERS.ID"
                     + " AND SEC_JT_USERS_GROUPS.FK_GROUPS=?";

        try {
            pstmt = con.prepareStatement( sql );
            pstmt.setInt( 1, group.getID() );
            rs = pstmt.executeQuery();
            while ( rs.next() ) {
                users.add( new User( rs.getInt( 1 ), rs.getString( 2 ), rs.getString( 3 ), rs.getString( 4 ),
                                     rs.getString( 5 ), rs.getString( 6 ), this ) );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return users.toArray( new User[users.size()] );
    }

    /**
     * Adds a new group account to the <code>Registry</code>.
     * 
     * 
     * @param transaction
     * @param name
     * @throws GeneralSecurityException
     *             this is a <code>DuplicateException</code> if the group already existed
     */
    public Group registerGroup( SecurityTransaction transaction, String name, String title )
                            throws GeneralSecurityException {
        try {
            getGroupByName( transaction, name );
            throw new DuplicateException( "Registration of group '" + name + "' failed! A group with "
                                          + "this name already exists." );
        } catch ( UnknownException e ) {
            // then it's no duplicate
        }

        Group group = new Group( getID( transaction, "SEC_SECURABLE_OBJECTS" ), name, title, this );
        PreparedStatement pstmt = null;

        try {
            pstmt = transactionalConnection.prepareStatement( "INSERT INTO SEC_SECURABLE_OBJECTS (ID,NAME,TITLE) VALUES (?,?,?)" );
            pstmt.setInt( 1, group.getID() );
            pstmt.setString( 2, group.getName() );
            pstmt.setString( 3, group.getTitle() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;

            pstmt = transactionalConnection.prepareStatement( "INSERT INTO SEC_GROUPS (ID) VALUES (?)" );
            pstmt.setInt( 1, group.getID() );
            pstmt.executeUpdate();
            pstmt.close();
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.registerGroup() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
        return group;
    }

    /**
     * Removes an existing <code>Group</code> from the <code>Registry</code> (including its relations).
     * 
     * @param transaction
     * @param group
     * @throws GeneralSecurityException
     */
    public void deregisterGroup( SecurityTransaction transaction, Group group )
                            throws GeneralSecurityException {
        PreparedStatement pstmt = null;

        try {
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_USERS_GROUPS WHERE FK_GROUPS=?" );
            pstmt.setInt( 1, group.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_GROUPS_GROUPS WHERE FK_GROUPS=? OR FK_GROUPS_MEMBER=?" );
            pstmt.setInt( 1, group.getID() );
            pstmt.setInt( 2, group.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_GROUPS_ROLES WHERE FK_GROUPS=?" );
            pstmt.setInt( 1, group.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_GROUPS WHERE ID=?" );
            pstmt.setInt( 1, group.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_ROLES_SECOBJECTS WHERE FK_SECURABLE_OBJECTS=?" );
            pstmt.setInt( 1, group.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_SECURABLE_OBJECTS WHERE ID=?" );
            pstmt.setInt( 1, group.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.deregisterGroup() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
    }

    /**
     * Retrieves a <code>Group</code> from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @param name
     * @throws GeneralSecurityException
     *             this is an <code>UnknownException</code> if the group is not known to the <code>Registry</code>
     */
    public Group getGroupByName( SecurityAccess securityAccess, String name )
                            throws GeneralSecurityException {
        Group group = null;
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT SEC_GROUPS.ID,SEC_SECURABLE_OBJECTS.TITLE FROM SEC_GROUPS,SEC_SECURABLE_OBJECTS "
                     + "WHERE SEC_GROUPS.ID=SEC_SECURABLE_OBJECTS.ID AND SEC_SECURABLE_OBJECTS.NAME=?";
        try {
            pstmt = con.prepareStatement( sql );
            pstmt.setString( 1, name );
            rs = pstmt.executeQuery();
            if ( rs.next() ) {
                group = new Group( rs.getInt( 1 ), name, rs.getString( 2 ), this );
            } else {
                throw new UnknownException( "Lookup of group '" + name
                                            + "' failed! A group with this name does not exist." );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return group;
    }

    /**
     * Retrieves a <code>Group</code> from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @param id
     * @throws GeneralSecurityException
     *             this is an <code>UnknownException</code> if the group is not known to the <code>Registry</code>
     */
    public Group getGroupById( SecurityAccess securityAccess, int id )
                            throws GeneralSecurityException {
        Group group = null;
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT SEC_SECURABLE_OBJECTS.NAME,SEC_SECURABLE_OBJECTS.TITLE "
                     + "FROM SEC_GROUPS,SEC_SECURABLE_OBJECTS "
                     + "WHERE SEC_SECURABLE_OBJECTS.ID=? AND SEC_GROUPS.ID=SEC_SECURABLE_OBJECTS.ID";
        try {
            pstmt = con.prepareStatement( sql );
            pstmt.setInt( 1, id );
            rs = pstmt.executeQuery();
            if ( rs.next() ) {
                group = new Group( id, rs.getString( 1 ), rs.getString( 2 ), this );
            } else {
                throw new UnknownException( "Lookup of group with id: " + id
                                            + " failed! A group with this id does not exist." );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return group;
    }

    /**
     * Retrieves all <code>Group</code> s from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @throws GeneralSecurityException
     */
    public Group[] getAllGroups( SecurityAccess securityAccess )
                            throws GeneralSecurityException {
        ArrayList<Group> groups = new ArrayList<Group>( 50 );
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT SEC_GROUPS.ID,SEC_SECURABLE_OBJECTS.NAME,SEC_SECURABLE_OBJECTS.TITLE "
                     + "FROM SEC_GROUPS,SEC_SECURABLE_OBJECTS WHERE SEC_GROUPS.ID=SEC_SECURABLE_OBJECTS.ID";
        try {
            pstmt = con.prepareStatement( sql );
            rs = pstmt.executeQuery();
            while ( rs.next() ) {
                groups.add( new Group( rs.getInt( 1 ), rs.getString( 2 ), rs.getString( 3 ), this ) );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return groups.toArray( new Group[groups.size()] );
    }

    /**
     * Adds a new role to the <code>Registry</code>.
     * 
     * @param transaction
     * @param name
     * @throws GeneralSecurityException
     *             this is a <code>DuplicateException</code> if the role already existed
     */
    public Role registerRole( SecurityTransaction transaction, String name )
                            throws GeneralSecurityException {
        try {
            getRoleByName( transaction, name );
            throw new DuplicateException( "Registration of role '" + name + "' failed! A role with "
                                          + "this name already exists." );
        } catch ( UnknownException e ) {
            // then it's no duplicate
        }

        Role role = new Role( getID( transaction, "SEC_SECURABLE_OBJECTS" ), name, this );
        PreparedStatement pstmt = null;

        try {
            pstmt = transactionalConnection.prepareStatement( "INSERT INTO SEC_SECURABLE_OBJECTS (ID,NAME,TITLE) VALUES (?,?,?)" );
            pstmt.setInt( 1, role.getID() );
            pstmt.setString( 2, role.getName() );
            pstmt.setString( 3, role.getTitle() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;

            pstmt = transactionalConnection.prepareStatement( "INSERT INTO SEC_ROLES (ID) VALUES (?)" );
            pstmt.setInt( 1, role.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.registerRole() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
        return role;
    }

    /**
     * Removes an existing <code>Role</code> from the <code>Registry</code> (including its relations).
     * 
     * @param transaction
     * @param role
     * @throws GeneralSecurityException
     */
    public void deregisterRole( SecurityTransaction transaction, Role role )
                            throws GeneralSecurityException {
        PreparedStatement pstmt = null;

        try {
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_USERS_ROLES WHERE FK_ROLES=?" );
            pstmt.setInt( 1, role.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_GROUPS_ROLES WHERE FK_ROLES=?" );
            pstmt.setInt( 1, role.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_ROLES_SECOBJECTS WHERE FK_ROLES=? OR FK_SECURABLE_OBJECTS=?" );
            pstmt.setInt( 1, role.getID() );
            pstmt.setInt( 2, role.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_ROLES WHERE ID=?" );
            pstmt.setInt( 1, role.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_SECURABLE_OBJECTS WHERE ID=?" );
            pstmt.setInt( 1, role.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.deregisterRole() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
    }

    /**
     * Retrieves a <code>Role</code> from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @param name
     * @throws GeneralSecurityException
     *             this is an <code>UnknownException</code> if the role is not known to the <code>Registry</code>
     */
    public Role getRoleByName( SecurityAccess securityAccess, String name )
                            throws GeneralSecurityException {
        Role role = null;
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT SEC_ROLES.ID FROM SEC_ROLES,SEC_SECURABLE_OBJECTS "
                     + "WHERE SEC_ROLES.ID=SEC_SECURABLE_OBJECTS.ID AND SEC_SECURABLE_OBJECTS.NAME=?";
        try {
            pstmt = con.prepareStatement( sql );
            pstmt.setString( 1, name );
            rs = pstmt.executeQuery();
            if ( rs.next() ) {
                role = new Role( rs.getInt( 1 ), name, this );
            } else {
                throw new UnknownException( "Lookup of role '" + name
                                            + "' failed! A role with this name does not exist." );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return role;
    }

    /**
     * Retrieves all <code>Roles</code> s from the <code>Registry</code> that have a certain namespace.
     * 
     * @param securityAccess
     * @param ns
     *            null for default namespace
     * @throws GeneralSecurityException
     */
    public Role[] getRolesByNS( SecurityAccess securityAccess, String ns )
                            throws GeneralSecurityException {
        ArrayList<Role> roles = new ArrayList<Role>( 100 );
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;
        try {
            if ( ns != null && ( !ns.equals( "" ) ) ) {
                sql = "SELECT SEC_ROLES.ID,SEC_SECURABLE_OBJECTS.NAME FROM SEC_ROLES,SEC_SECURABLE_OBJECTS "
                      + "WHERE SEC_SECURABLE_OBJECTS.ID=SEC_ROLES.ID AND SEC_SECURABLE_OBJECTS.NAME LIKE ?";
                pstmt = con.prepareStatement( sql );
                pstmt.setString( 1, ns + ":%" );
            } else {
                sql = "SELECT SEC_ROLES.ID,SEC_SECURABLE_OBJECTS.NAME FROM SEC_ROLES,SEC_SECURABLE_OBJECTS "
                      + "WHERE SEC_SECURABLE_OBJECTS.ID=SEC_ROLES.ID AND "
                      + "SEC_SECURABLE_OBJECTS.NAME NOT LIKE '%:%'";
                pstmt = con.prepareStatement( sql );
            }

            rs = pstmt.executeQuery();
            while ( rs.next() ) {
                roles.add( new Role( rs.getInt( 1 ), rs.getString( 2 ), this ) );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return roles.toArray( new Role[roles.size()] );
    }

    /**
     * Retrieves a <code>Role</code> from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @param id
     * @throws GeneralSecurityException
     *             this is an <code>UnknownException</code> if the role is not known to the <code>Registry</code>
     */
    public Role getRoleById( SecurityAccess securityAccess, int id )
                            throws GeneralSecurityException {
        Role role = null;
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT SEC_SECURABLE_OBJECTS.NAME FROM SEC_ROLES,SEC_SECURABLE_OBJECTS "
                     + "WHERE SEC_SECURABLE_OBJECTS.ID=? AND SEC_ROLES.ID=SEC_SECURABLE_OBJECTS.ID";
        try {
            pstmt = con.prepareStatement( sql );
            pstmt.setInt( 1, id );
            rs = pstmt.executeQuery();
            if ( rs.next() ) {
                role = new Role( id, rs.getString( 1 ), this );
            } else {
                throw new UnknownException( "Lookup of role with id: " + id
                                            + " failed! A role with this id does not exist." );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return role;
    }

    /**
     * Retrieves all <code>Role</code> s from the <code>Registry</code>, except those that are only used internally
     * (these have namespaces that begin with $).
     * 
     * @param securityAccess
     * @throws GeneralSecurityException
     */
    public Role[] getAllRoles( SecurityAccess securityAccess )
                            throws GeneralSecurityException {
        ArrayList<Role> roles = new ArrayList<Role>( 50 );
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT SEC_ROLES.ID,SEC_SECURABLE_OBJECTS.NAME FROM SEC_ROLES,SEC_SECURABLE_OBJECTS "
                     + "WHERE SEC_ROLES.ID=SEC_SECURABLE_OBJECTS.ID AND "
                     + "SEC_SECURABLE_OBJECTS.NAME NOT LIKE '$%:%'";
        try {
            pstmt = con.prepareStatement( sql );
            rs = pstmt.executeQuery();
            while ( rs.next() ) {
                roles.add( new Role( rs.getInt( 1 ), rs.getString( 2 ), this ) );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return roles.toArray( new Role[roles.size()] );
    }

    /**
     * Adds a new <code>SecuredObject</code> to the <code>Registry</code>.
     * 
     * @param transaction
     * @param type
     * @param name
     * @param title
     * @throws GeneralSecurityException
     *             this is a <code>DuplicateException</code> if the object already existed
     */
    public SecuredObject registerSecuredObject( SecurityTransaction transaction, String type, String name, String title )
                            throws GeneralSecurityException {
        try {
            getSecuredObjectByName( transaction, name, type );
            throw new DuplicateException( "Registration of secured object '" + name + "' with type '" + type
                                          + "' failed! A secured object with this name and type " + "already exists." );
        } catch ( UnknownException e ) {
            // then it's no duplicate
        }

        PreparedStatement pstmt = null;
        SecuredObject object = null;
        ResultSet rs = null;

        try {
            // check for ID of object type (add type if necessary)
            int typeId = 0;
            pstmt = transactionalConnection.prepareStatement( "SELECT ID FROM SEC_SECURED_OBJECT_TYPES WHERE NAME=?" );
            pstmt.setString( 1, type );
            rs = pstmt.executeQuery();
            if ( rs.next() ) {
                typeId = rs.getInt( 1 );
                rs.close();
                rs = null;
                pstmt.close();
                pstmt = null;
            } else {
                typeId = getID( transaction, "SEC_SECURED_OBJECT_TYPES" );
                rs.close();
                rs = null;
                pstmt.close();
                pstmt = null;
                pstmt = transactionalConnection.prepareStatement( "INSERT INTO SEC_SECURED_OBJECT_TYPES (ID,NAME) VALUES (?,?)" );
                pstmt.setInt( 1, typeId );
                pstmt.setString( 2, type );
                pstmt.executeUpdate();
                pstmt.close();
                pstmt = null;
            }

            // insert securable object part
            object = new SecuredObject( getID( transaction, "SEC_SECURABLE_OBJECTS" ), typeId, name, title, this );
            pstmt = transactionalConnection.prepareStatement( "INSERT INTO SEC_SECURABLE_OBJECTS (ID,NAME,TITLE) VALUES (?,?,?)" );
            pstmt.setInt( 1, object.getID() );
            pstmt.setString( 2, object.getName() );
            pstmt.setString( 3, object.getTitle() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;

            // insert secured object
            pstmt = transactionalConnection.prepareStatement( "INSERT INTO SEC_SECURED_OBJECTS (ID, FK_SECURED_OBJECT_TYPES) VALUES (?,?)" );
            pstmt.setInt( 1, object.getID() );
            pstmt.setInt( 2, typeId );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            closeResultSet( rs );
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.registerSecuredObject() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
        return object;
    }

    /**
     * Removes an existing <code>SecuredObject</code> from the <code>Registry</code> (including its associations).
     * 
     * @param transaction
     * @param object
     * @throws GeneralSecurityException
     */
    public void deregisterSecuredObject( SecurityTransaction transaction, SecuredObject object )
                            throws GeneralSecurityException {
        PreparedStatement pstmt = null;

        try {
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_SECURED_OBJECTS WHERE ID=?" );
            pstmt.setInt( 1, object.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_ROLES_SECOBJECTS WHERE FK_SECURABLE_OBJECTS=?" );
            pstmt.setInt( 1, object.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_SECURABLE_OBJECTS WHERE ID=?" );
            pstmt.setInt( 1, object.getID() );
            pstmt.executeUpdate();
            pstmt = null;
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.deregisterSecuredObject() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
    }

    /**
     * Retrieves a <code>SecuredObject</code> from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @param name
     * @throws GeneralSecurityException
     *             this is an <code>UnknownException</code> if the object is not known to the <code>Registry</code>
     */
    public SecuredObject getSecuredObjectByName( SecurityAccess securityAccess, String name, String type )
                            throws GeneralSecurityException {
        SecuredObject object = null;
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT SEC_SECURED_OBJECTS.ID,SEC_SECURED_OBJECT_TYPES.ID, SEC_SECURABLE_OBJECTS.TITLE "
                     + "FROM SEC_SECURED_OBJECTS,SEC_SECURED_OBJECT_TYPES,SEC_SECURABLE_OBJECTS "
                     + "WHERE SEC_SECURED_OBJECTS.FK_SECURED_OBJECT_TYPES=SEC_SECURED_OBJECT_TYPES.ID AND "
                     + "SEC_SECURED_OBJECTS.ID=SEC_SECURABLE_OBJECTS.ID AND SEC_SECURABLE_OBJECTS.NAME = ? AND "
                     + "SEC_SECURED_OBJECT_TYPES.NAME=?";
        try {
            pstmt = con.prepareStatement( sql );
            pstmt.setString( 1, name );
            pstmt.setString( 2, type );

            if ( LOG.isDebug() ) {
                LOG.logDebug( "getSecuredObjectByName", pstmt );
            }

            rs = pstmt.executeQuery();
            if ( rs.next() ) {
                object = new SecuredObject( rs.getInt( 1 ), rs.getInt( 2 ), name, rs.getString( 3 ), this );
            } else {
                throw new UnknownException( "Lookup of secured object '" + name + "' with type '" + type
                                            + "' failed! A secured object with this " + "name and type does not exist." );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return object;
    }

    /**
     * Retrieves a <code>SecuredObject</code> from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @param id
     * @throws GeneralSecurityException
     *             this is an <code>UnknownException</code> if the object is not known to the <code>Registry</code>
     */
    public SecuredObject getSecuredObjectById( SecurityAccess securityAccess, int id )
                            throws GeneralSecurityException {
        SecuredObject object = null;
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT SEC_SECURED_OBJECTS.FK_SECURED_OBJECT_TYPES,SEC_SECURABLE_OBJECTS.NAME,"
                     + "SEC_SECURABLE_OBJECTS.TITLE FROM SEC_SECURED_OBJECTS,SEC_SECURABLE_OBJECTS "
                     + "WHERE SEC_SECURED_OBJECTS.ID=SEC_SECURABLE_OBJECTS.ID AND SEC_SECURABLE_OBJECTS.ID=?";
        try {
            pstmt = con.prepareStatement( sql );
            pstmt.setInt( 1, id );
            rs = pstmt.executeQuery();
            if ( rs.next() ) {
                object = new SecuredObject( id, rs.getInt( 1 ), rs.getString( 2 ), rs.getString( 3 ), this );
            } else {
                throw new UnknownException( "Lookup of secured object with id: " + id
                                            + " failed! A secured object with this id does not exist." );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return object;
    }

    /**
     * Retrieves all <code>SecuredObject</code> s from the <code>Registry</code> that have a certain namespace.
     * 
     * @param securityAccess
     * @param ns
     *            null for default namespace
     * @param type
     * @throws GeneralSecurityException
     */
    public SecuredObject[] getSecuredObjectsByNS( SecurityAccess securityAccess, String ns, String type )
                            throws GeneralSecurityException {
        ArrayList<SecuredObject> objects = new ArrayList<SecuredObject>( 100 );
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;
        try {
            if ( ns != null && ( !ns.equals( "" ) ) ) {
                sql = "SELECT SEC_SECURED_OBJECTS.ID,SEC_SECURED_OBJECT_TYPES.ID, "
                      + "SEC_SECURABLE_OBJECTS.NAME,SEC_SECURABLE_OBJECTS.TITLE "
                      + "FROM SEC_SECURED_OBJECTS,SEC_SECURED_OBJECT_TYPES,SEC_SECURABLE_OBJECTS "
                      + "WHERE SEC_SECURED_OBJECTS.FK_SECURED_OBJECT_TYPES=SEC_SECURED_OBJECT_TYPES.ID AND "
                      + "SEC_SECURABLE_OBJECTS.ID=SEC_SECURED_OBJECTS.ID AND SEC_SECURED_OBJECT_TYPES.NAME=? "
                      + "AND SEC_SECURABLE_OBJECTS.NAME LIKE ?";
                pstmt = con.prepareStatement( sql );
                pstmt.setString( 1, type );
                pstmt.setString( 2, ns + ":%" );
            } else {
                sql = "SELECT SEC_SECURED_OBJECTS.ID,SEC_SECURED_OBJECT_TYPES.ID, "
                      + "SEC_SECURABLE_OBJECTS.NAME,SEC_SECURABLE_OBJECTS.TITLE "
                      + "FROM SEC_SECURED_OBJECTS,SEC_SECURED_OBJECT_TYPES,SEC_SECURABLE_OBJECTS "
                      + "WHERE SEC_SECURED_OBJECTS.FK_SECURED_OBJECT_TYPES=SEC_SECURED_OBJECT_TYPES.ID AND "
                      + "SEC_SECURABLE_OBJECTS.ID=SEC_SECURED_OBJECTS.ID AND SEC_SECURED_OBJECT_TYPES.NAME=? "
                      + "AND SEC_SECURABLE_OBJECTS.NAME NOT LIKE '%:%'";
                pstmt = con.prepareStatement( sql );
                pstmt.setString( 1, type );
            }
            rs = pstmt.executeQuery();
            while ( rs.next() ) {
                objects.add( new SecuredObject( rs.getInt( 1 ), rs.getInt( 2 ), rs.getString( 3 ), rs.getString( 4 ),
                                                this ) );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return objects.toArray( new SecuredObject[objects.size()] );
    }

    /**
     * Retrieves all <code>SecuredObject</code> s with the given type from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @param type
     * @throws GeneralSecurityException
     */
    public SecuredObject[] getAllSecuredObjects( SecurityAccess securityAccess, String type )
                            throws GeneralSecurityException {
        ArrayList<SecuredObject> objects = new ArrayList<SecuredObject>( 100 );
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT SEC_SECURED_OBJECTS.ID,SEC_SECURED_OBJECT_TYPES.ID, "
                     + "SEC_SECURABLE_OBJECTS.NAME,SEC_SECURABLE_OBJECTS.TITLE "
                     + "FROM SEC_SECURED_OBJECTS,SEC_SECURED_OBJECT_TYPES,SEC_SECURABLE_OBJECTS "
                     + "WHERE SEC_SECURED_OBJECTS.FK_SECURED_OBJECT_TYPES=SEC_SECURED_OBJECT_TYPES.ID AND "
                     + "SEC_SECURABLE_OBJECTS.ID=SEC_SECURED_OBJECTS.ID AND SEC_SECURED_OBJECT_TYPES.NAME=?";
        try {
            pstmt = con.prepareStatement( sql );
            pstmt.setString( 1, type );
            rs = pstmt.executeQuery();
            while ( rs.next() ) {
                objects.add( new SecuredObject( rs.getInt( 1 ), rs.getInt( 2 ), rs.getString( 3 ), rs.getString( 4 ),
                                                this ) );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return objects.toArray( new SecuredObject[objects.size()] );
    }

    /**
     * Adds a new <code>Privilege</code> to the <code>Registry</code>.
     * 
     * @param transaction
     * @param name
     * @throws GeneralSecurityException
     *             this is a <code>DuplicateException</code> if the <code>Privilege</code> already existed
     */
    public Privilege registerPrivilege( SecurityTransaction transaction, String name )
                            throws GeneralSecurityException {
        try {
            getPrivilegeByName( transaction, name );
            throw new DuplicateException( "Registration of privilege '" + name + "' failed! A privilege with "
                                          + "this name already exists." );
        } catch ( UnknownException e ) {
            // then it's no duplicate
        }

        int id = getID( transaction, "SEC_PRIVILEGES" );
        Privilege privilege = new Privilege( id, name );
        PreparedStatement pstmt = null;

        String sql = "INSERT INTO SEC_PRIVILEGES (ID, NAME) VALUES (?,?)";
        try {
            pstmt = transactionalConnection.prepareStatement( sql );
            pstmt.setInt( 1, id );
            pstmt.setString( 2, name );
            pstmt.executeUpdate();
            pstmt.close();
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e.getMessage(), e );
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.registerPrivilege() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
        return privilege;
    }

    /**
     * Removes an existing</code> Privilege</code> from the <code>Registry </code> (including its relations).
     * 
     * @param transaction
     * @param privilege
     * @throws GeneralSecurityException
     */
    public void deregisterPrivilege( SecurityTransaction transaction, Privilege privilege )
                            throws GeneralSecurityException {
        PreparedStatement pstmt = null;

        try {
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_ROLES_PRIVILEGES WHERE FK_PRIVILEGES=?" );
            pstmt.setInt( 1, privilege.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_PRIVILEGES WHERE ID=?" );
            pstmt.setInt( 1, privilege.getID() );
            pstmt.executeUpdate();
            pstmt.close();
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.deregisterPrivilege() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
    }

    /**
     * Retrieves a <code>Privilege</code> from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @param name
     * @throws GeneralSecurityException
     *             this is an <code>UnknownException</code> if the privilege is not known to the <code>Registry</code>
     */
    public Privilege getPrivilegeByName( SecurityAccess securityAccess, String name )
                            throws GeneralSecurityException {
        Privilege privilege = null;
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT ID FROM SEC_PRIVILEGES WHERE NAME=?";
        try {
            pstmt = con.prepareStatement( sql );
            pstmt.setString( 1, name );
            rs = pstmt.executeQuery();
            if ( rs.next() ) {
                privilege = new Privilege( rs.getInt( 1 ), name );
            } else {
                throw new UnknownException( "Lookup of privilege '" + name
                                            + "' failed! A privilege with this name does not exist." );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return privilege;
    }

    /**
     * Retrieves all <code>Privileges</code> s from the <code>Registry</code> that are associated DIRECTLY (i.e. not via
     * group memberships) with a given <code>Role</code>.
     * 
     * @param securityAccess
     * @param role
     * @throws GeneralSecurityException
     */
    public Privilege[] getPrivilegesForRole( SecurityAccess securityAccess, Role role )
                            throws GeneralSecurityException {
        ArrayList<Privilege> privileges = new ArrayList<Privilege>();
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT SEC_PRIVILEGES.ID,SEC_PRIVILEGES.NAME "
                     + "FROM SEC_JT_ROLES_PRIVILEGES, SEC_PRIVILEGES WHERE "
                     + "SEC_JT_ROLES_PRIVILEGES.FK_ROLES=? AND "
                     + "SEC_JT_ROLES_PRIVILEGES.FK_PRIVILEGES=SEC_PRIVILEGES.ID";
        try {
            pstmt = con.prepareStatement( sql );
            pstmt.setInt( 1, role.getID() );
            rs = pstmt.executeQuery();
            while ( rs.next() ) {
                privileges.add( new Privilege( rs.getInt( 1 ), rs.getString( 2 ) ) );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return privileges.toArray( new Privilege[privileges.size()] );
    }

    /**
     * Sets all <code>Privilege</code> s that are associated with a given <code>Role</code>.
     * 
     * @param transaction
     * @param role
     * @param privileges
     * @throws GeneralSecurityException
     */
    public void setPrivilegesForRole( SecurityTransaction transaction, Role role, Privilege[] privileges )
                            throws GeneralSecurityException {
        PreparedStatement pstmt = null;

        try {
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_ROLES_PRIVILEGES WHERE FK_ROLES=?" );
            pstmt.setInt( 1, role.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;

            for ( int i = 0; i < privileges.length; i++ ) {
                pstmt = transactionalConnection.prepareStatement( "INSERT INTO SEC_JT_ROLES_PRIVILEGES (FK_ROLES, FK_PRIVILEGES) VALUES (?,?)" );
                pstmt.setInt( 1, role.getID() );
                pstmt.setInt( 2, privileges[i].getID() );
                pstmt.executeUpdate();
                pstmt.close();
                pstmt = null;
            }
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.setPrivilegesForRols() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
    }

    /**
     * Adds a new <code>Right</code> to the <code>Registry</code>.
     * 
     * @param transaction
     * @param name
     * @throws GeneralSecurityException
     *             this is a <code>DuplicateException</code> if the <code>Right</code> already existed
     */
    public RightType registerRightType( SecurityTransaction transaction, String name )
                            throws GeneralSecurityException {
        try {
            getRightTypeByName( transaction, name );
            throw new DuplicateException( "Registration of right '" + name + "' failed! A right with "
                                          + "this name already exists." );
        } catch ( UnknownException e ) {
            // then it's no duplicate
        }

        int id = getID( transaction, "SEC_RIGHTS" );
        RightType right = new RightType( id, name );
        PreparedStatement pstmt = null;

        String sql = "INSERT INTO SEC_RIGHTS (ID, NAME) VALUES (?,?)";
        try {
            pstmt = transactionalConnection.prepareStatement( sql );
            pstmt.setInt( 1, id );
            pstmt.setString( 2, name );
            pstmt.executeUpdate();
            pstmt.close();
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e.getMessage(), e );
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.registerRight() failed. Rollback "
                                                + "performed. Error message: " + e.getMessage() );
        }
        return right;
    }

    /**
     * Removes an existing <code>RightType</code> from the <code>Registry</code> (including its relations).
     * 
     * @param transaction
     * @param type
     * @throws GeneralSecurityException
     */
    public void deregisterRightType( SecurityTransaction transaction, RightType type )
                            throws GeneralSecurityException {
        PreparedStatement pstmt = null;

        try {
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_ROLES_SECOBJECTS WHERE FK_RIGHTS=?" );
            pstmt.setInt( 1, type.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_RIGHTS WHERE ID=?" );
            pstmt.setInt( 1, type.getID() );
            pstmt.executeUpdate();
            pstmt.close();
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.deregisterRight() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
    }

    /**
     * Retrieves a <code>Right</code> from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @param name
     * @throws GeneralSecurityException
     *             this is an <code>UnknownException</code> if the <code>Right</code> is not known to the
     *             <code>Registry</code>
     */
    public RightType getRightTypeByName( SecurityAccess securityAccess, String name )
                            throws GeneralSecurityException {
        RightType right = null;
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT ID FROM SEC_RIGHTS WHERE NAME=?";
        try {
            pstmt = con.prepareStatement( sql );
            pstmt.setString( 1, name );
            rs = pstmt.executeQuery();
            if ( rs.next() ) {
                right = new RightType( rs.getInt( 1 ), name );
            } else {
                throw new UnknownException( "Lookup of right '" + name
                                            + "' failed! A right with this name does not exist." );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return right;
    }

    /**
     * Retrieves the <code>Rights</code> from the <code>Registry</code> that are associated with a given
     * <code>Role</code> and a <code>SecurableObject</code>.
     * 
     * @param securityAccess
     * @param object
     * @param role
     * @throws GeneralSecurityException
     */
    public Right[] getRights( SecurityAccess securityAccess, SecurableObject object, Role role )
                            throws GeneralSecurityException {
        ArrayList<Right> rights = new ArrayList<Right>();
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT SEC_RIGHTS.ID,SEC_RIGHTS.NAME,"
                     + "SEC_JT_ROLES_SECOBJECTS.CONSTRAINTS FROM SEC_JT_ROLES_SECOBJECTS,"
                     + "SEC_RIGHTS WHERE SEC_JT_ROLES_SECOBJECTS.FK_ROLES=? AND "
                     + "SEC_JT_ROLES_SECOBJECTS.FK_SECURABLE_OBJECTS=? AND "
                     + "SEC_JT_ROLES_SECOBJECTS.FK_RIGHTS=SEC_RIGHTS.ID";
        try {
            pstmt = con.prepareStatement( sql );
            pstmt.setInt( 1, role.getID() );
            pstmt.setInt( 2, object.getID() );
            rs = pstmt.executeQuery();
            ResultSetMetaData metadata = rs.getMetaData();
            int constraintType = metadata.getColumnType( 3 );

            while ( rs.next() ) {
                Right right = null;
                RightType type = new RightType( rs.getInt( 1 ), rs.getString( 2 ) );
                String constraints = null;
                Object o = rs.getObject( 3 );
                if ( o != null ) {
                    if ( constraintType == Types.CLOB ) {
                        Reader reader = ( (Clob) o ).getCharacterStream();
                        StringBuffer sb = new StringBuffer( 2000 );
                        int c;
                        try {
                            while ( ( c = reader.read() ) > -1 ) {
                                sb.append( (char) c );
                            }
                            reader.close();
                        } catch ( IOException e ) {
                            throw new GeneralSecurityException( "Error converting CLOB to constraint string: "
                                                                + e.getMessage() );
                        }
                        constraints = sb.toString();
                    } else {
                        constraints = o.toString();
                    }
                }

                // check if the right has constraints
                if ( constraints != null && constraints.length() > 3 ) {
                    right = new Right( object, type, buildFilter( constraints ) );
                } else {
                    right = new Right( object, type, null );
                }

                rights.add( right );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return rights.toArray( new Right[rights.size()] );
    }

    /**
     * Retrieves the <code>Rights</code> from the <code>Registry</code> that are associated with a given
     * <code>Role</code> and a <code>SecurableObject</code>.
     * 
     * @param securityAccess
     * @param object
     * @param roles
     * @param type
     * @throws GeneralSecurityException
     */
    public Right[] getRights( SecurityAccess securityAccess, SecurableObject object, Role[] roles, RightType type )
                            throws GeneralSecurityException {
        // TODO
        // must be tested
        ArrayList<Right> rights = new ArrayList<Right>();
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;
        try {
            sql = "SELECT SEC_RIGHTS.ID,SEC_RIGHTS.NAME,"
                  + "SEC_JT_ROLES_SECOBJECTS.CONSTRAINTS FROM SEC_JT_ROLES_SECOBJECTS," + "SEC_RIGHTS WHERE (";
            for ( int i = 0; i < roles.length; i++ ) {
                sql += "SEC_JT_ROLES_SECOBJECTS.FK_ROLES=? ";
                if ( i < roles.length - 1 ) {
                    sql += " OR ";
                }
            }
            sql += ") AND SEC_JT_ROLES_SECOBJECTS.FK_SECURABLE_OBJECTS=? AND "
                   + "SEC_JT_ROLES_SECOBJECTS.FK_RIGHTS=SEC_RIGHTS.ID AND " + "SEC_RIGHTS.NAME = ?";
            pstmt = con.prepareStatement( sql );
            for ( int i = 0; i < roles.length; i++ ) {
                pstmt.setInt( i + 1, roles[i].getID() );
            }

            LOG.logDebug( sql );

            pstmt.setInt( roles.length + 1, object.getID() );
            pstmt.setString( roles.length + 2, type.getName() );
            rs = pstmt.executeQuery();
            ResultSetMetaData metadata = rs.getMetaData();
            int constraintType = metadata.getColumnType( 3 );

            while ( rs.next() ) {
                Right right = null;
                type = new RightType( rs.getInt( 1 ), rs.getString( 2 ) );
                String constraints = null;
                Object o = rs.getObject( 3 );
                if ( o != null ) {
                    if ( constraintType == Types.CLOB ) {
                        Reader reader = ( (Clob) o ).getCharacterStream();
                        StringBuffer sb = new StringBuffer( 2000 );
                        int c;
                        try {
                            while ( ( c = reader.read() ) > -1 ) {
                                sb.append( (char) c );
                            }
                            reader.close();
                        } catch ( IOException e ) {
                            throw new GeneralSecurityException( "Error converting CLOB to constraint string: "
                                                                + e.getMessage() );
                        }
                        constraints = sb.toString();
                    } else {
                        constraints = o.toString();
                    }
                }

                // check if the right has constraints
                if ( constraints != null && constraints.length() > 3 ) {
                    right = new Right( object, type, buildFilter( constraints ) );
                } else {
                    right = new Right( object, type, null );
                }

                rights.add( right );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return rights.toArray( new Right[rights.size()] );
    }

    /**
     * Sets the <code>Rights</code> to be associated with a given <code>Role</code> and <code>SecurableObject</code>.
     * 
     * @param transaction
     * @param object
     * @param role
     * @param rights
     * @throws GeneralSecurityException
     */
    public void setRights( SecurityTransaction transaction, SecurableObject object, Role role, Right[] rights )
                            throws GeneralSecurityException {
        PreparedStatement pstmt = null;

        try {
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_ROLES_SECOBJECTS WHERE FK_ROLES=? AND FK_SECURABLE_OBJECTS=?" );
            pstmt.setInt( 1, role.getID() );
            pstmt.setInt( 2, object.getID() );
            pstmt.executeUpdate();
            pstmt.close();

            for ( int i = 0; i < rights.length; i++ ) {

                String constraints = null;
                if ( rights[i].getConstraints() != null ) {
                    constraints = rights[i].getConstraints().to110XML().toString();
                }
                LOG.logDebug( "constraints to add: ", constraints );
                if ( transactionalConnection.getClass().getCanonicalName().equals( "oracle.jdbc.OracleConnection" ) ) {
                    /* transactionalConnection instanceof OracleConnection */
                    handleOracle( object, role, rights[i], constraints );
                } else {
                    pstmt = transactionalConnection.prepareStatement( "INSERT INTO SEC_JT_ROLES_SECOBJECTS (FK_ROLES, FK_SECURABLE_OBJECTS, FK_RIGHTS,CONSTRAINTS) VALUES (?,?,?,?)" );
                    pstmt.setInt( 1, role.getID() );
                    pstmt.setInt( 2, object.getID() );
                    pstmt.setInt( 3, rights[i].getType().getID() );
                    pstmt.setString( 4, constraints );
                    pstmt.executeUpdate();
                    pstmt.close();
                }

            }
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.setRights() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
    }

    private void handleOracle( SecurableObject object, Role role, Right right, String constraints )
                            throws SQLException {

        PreparedStatement pstmt;
        pstmt = transactionalConnection.prepareStatement( "INSERT INTO SEC_JT_ROLES_SECOBJECTS (FK_ROLES, FK_SECURABLE_OBJECTS, FK_RIGHTS, CONSTRAINTS) VALUES (?,?,?, EMPTY_CLOB() )" );
        pstmt.setInt( 1, role.getID() );
        pstmt.setInt( 2, object.getID() );
        pstmt.setInt( 3, right.getType().getID() );
        pstmt.executeUpdate();
        pstmt.close();
        transactionalConnection.commit();

        if ( constraints != null ) {
            pstmt = transactionalConnection.prepareStatement( "select CONSTRAINTS from SEC_JT_ROLES_SECOBJECTS where FK_ROLES = ? and FK_SECURABLE_OBJECTS = ? and FK_RIGHTS = ? FOR UPDATE" );
            pstmt.setInt( 1, role.getID() );
            pstmt.setInt( 2, object.getID() );
            pstmt.setInt( 3, right.getType().getID() );
            ResultSet rs = pstmt.executeQuery();
            rs.next();

            Clob clob = rs.getClob( 1 );
            try {
                Writer writer = clob.setCharacterStream( 0 );
                // use that output stream to write character data to the Oracle data store
                writer.write( constraints.toCharArray() );
                // write data and commit
                writer.flush();
                writer.close();
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        }
        pstmt.close();
    }

    /**
     * Sets one <code>Right</code> to be associated with a given <code>Role</code> and all given
     * <code>SecurableObjects</code>.
     * 
     * @param transaction
     * @param objects
     * @param role
     * @param right
     * @throws GeneralSecurityException
     */
    public void setRights( SecurityTransaction transaction, SecurableObject[] objects, Role role, Right right )
                            throws GeneralSecurityException {
        PreparedStatement pstmt = null;

        try {
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_ROLES_SECOBJECTS WHERE FK_ROLES=? AND FK_RIGHTS=?" );
            pstmt.setInt( 1, role.getID() );
            pstmt.setInt( 2, right.getType().getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;

            for ( int i = 0; i < objects.length; i++ ) {
                String constraints = null;
                if ( right.getConstraints() != null ) {
                    constraints = right.getConstraints().to110XML().toString();
                }
                pstmt = transactionalConnection.prepareStatement( "INSERT INTO SEC_JT_ROLES_SECOBJECTS (FK_ROLES, FK_SECURABLE_OBJECTS, FK_RIGHTS, CONSTRAINTS) VALUES (?,?,?,?)" );
                pstmt.setInt( 1, role.getID() );
                pstmt.setInt( 2, objects[i].getID() );
                pstmt.setInt( 3, right.getType().getID() );
                pstmt.setString( 4, constraints );
                pstmt.executeUpdate();
                pstmt.close();
                pstmt = null;
            }
        } catch ( SQLException e ) {
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.setRights() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
    }

    public void setServicesRights( SecurityTransaction transaction, Collection<Integer> services, Role role )
                            throws GeneralSecurityException {
        Connection con = acquireLocalConnection( transaction );
        PreparedStatement pstmt = null;

        try {
            pstmt = con.prepareStatement( "delete from sec_jt_roles_services where fk_roles = ?" );
            pstmt.setInt( 1, role.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            for ( Integer id : services ) {
                pstmt = con.prepareStatement( "insert into sec_jt_roles_services (fk_roles, fk_services) values (?, ?)" );
                pstmt.setInt( 1, role.getID() );
                pstmt.setInt( 2, id );
                pstmt.executeUpdate();
            }
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeStatement( pstmt );
            releaseLocalConnection( transaction, con );
        }

    }

    /**
     * Retrieves all <code>Group</code> s from the <code>Registry</code> that the given <code>User</code> is a DIRECT
     * (i.e. not via inheritance) member of.
     * 
     * @param securityAccess
     * @param user
     * @throws GeneralSecurityException
     */
    public Group[] getGroupsForUser( SecurityAccess securityAccess, User user )
                            throws GeneralSecurityException {
        ArrayList<Group> groups = new ArrayList<Group>( 100 );
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT SEC_GROUPS.ID,SEC_SECURABLE_OBJECTS.NAME,SEC_SECURABLE_OBJECTS.TITLE "
                     + "FROM SEC_SECURABLE_OBJECTS,SEC_GROUPS,SEC_JT_USERS_GROUPS WHERE "
                     + "SEC_SECURABLE_OBJECTS.ID=SEC_GROUPS.ID AND "
                     + "SEC_JT_USERS_GROUPS.FK_GROUPS=SEC_GROUPS.ID AND " + "SEC_JT_USERS_GROUPS.FK_USERS=?";
        try {
            pstmt = con.prepareStatement( sql );
            pstmt.setInt( 1, user.getID() );
            rs = pstmt.executeQuery();
            while ( rs.next() ) {
                groups.add( new Group( rs.getInt( 1 ), rs.getString( 2 ), rs.getString( 3 ), this ) );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return groups.toArray( new Group[groups.size()] );
    }

    /**
     * Retrieves all <code>Groups</code> s from the <code>Registry</code> that are members of another <code>Group</code>
     * DIRECTLY (i.e. not via inheritance).
     * 
     * @param securityAccess
     * @param group
     * @throws GeneralSecurityException
     */
    public Group[] getGroupsInGroup( SecurityAccess securityAccess, Group group )
                            throws GeneralSecurityException {
        ArrayList<Group> groups = new ArrayList<Group>( 100 );
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT SEC_GROUPS.ID,SEC_SECURABLE_OBJECTS.NAME,SEC_SECURABLE_OBJECTS.TITLE "
                     + "FROM SEC_GROUPS,SEC_SECURABLE_OBJECTS,SEC_JT_GROUPS_GROUPS "
                     + "WHERE SEC_SECURABLE_OBJECTS.ID=SEC_GROUPS.ID"
                     + " AND SEC_JT_GROUPS_GROUPS.FK_GROUPS_MEMBER=SEC_GROUPS.ID"
                     + " AND SEC_JT_GROUPS_GROUPS.FK_GROUPS=?";
        try {
            pstmt = con.prepareStatement( sql );
            pstmt.setInt( 1, group.getID() );
            rs = pstmt.executeQuery();
            while ( rs.next() ) {
                groups.add( new Group( rs.getInt( 1 ), rs.getString( 2 ), rs.getString( 3 ), this ) );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return groups.toArray( new Group[groups.size()] );
    }

    /**
     * Retrieves all <code>Group</code> s from the <code>Registry</code> that the given <code>Group</code> is a DIRECT
     * member (i.e. not via inheritance) of.
     * 
     * @param securityAccess
     * @param group
     * @throws GeneralSecurityException
     */
    public Group[] getGroupsForGroup( SecurityAccess securityAccess, Group group )
                            throws GeneralSecurityException {
        ArrayList<Group> groups = new ArrayList<Group>( 100 );
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT SEC_GROUPS.ID,SEC_SECURABLE_OBJECTS.NAME,SEC_SECURABLE_OBJECTS.TITLE "
                     + "FROM SEC_SECURABLE_OBJECTS,SEC_GROUPS,SEC_JT_GROUPS_GROUPS WHERE "
                     + "SEC_SECURABLE_OBJECTS.ID=SEC_GROUPS.ID AND "
                     + "SEC_JT_GROUPS_GROUPS.FK_GROUPS=SEC_GROUPS.ID AND " + "SEC_JT_GROUPS_GROUPS.FK_GROUPS_MEMBER=?";
        try {
            pstmt = con.prepareStatement( sql );
            pstmt.setInt( 1, group.getID() );
            rs = pstmt.executeQuery();
            while ( rs.next() ) {
                groups.add( new Group( rs.getInt( 1 ), rs.getString( 2 ), rs.getString( 3 ), this ) );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return groups.toArray( new Group[groups.size()] );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.security.drm.SecurityRegistry#getGroupsForGroups(org.deegree.security.drm.SecurityAccess,
     * org.deegree.security.drm.model.Group[])
     */
    public Group[] getGroupsForGroups( SecurityAccess securityAccess, Group[] groups )
                            throws GeneralSecurityException {
        ArrayList<Group> groupsList = new ArrayList<Group>( 100 );
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT SEC_GROUPS.ID,SEC_SECURABLE_OBJECTS.NAME,SEC_SECURABLE_OBJECTS.TITLE "
                     + "FROM SEC_SECURABLE_OBJECTS,SEC_GROUPS,SEC_JT_GROUPS_GROUPS WHERE "
                     + "SEC_SECURABLE_OBJECTS.ID=SEC_GROUPS.ID AND "
                     + "SEC_JT_GROUPS_GROUPS.FK_GROUPS=SEC_GROUPS.ID AND (";
        for ( int i = 0; i < groups.length; i++ ) {
            sql += "SEC_JT_GROUPS_GROUPS.FK_GROUPS_MEMBER=?";
            if ( i < groups.length - 1 ) {
                sql += " OR ";
            }
        }
        sql += ")";
        LOG.logDebug( sql );

        try {
            pstmt = con.prepareStatement( sql );
            for ( int i = 0; i < groups.length; i++ ) {
                pstmt.setInt( i + 1, groups[i].getID() );
            }
            rs = pstmt.executeQuery();
            while ( rs.next() ) {
                groupsList.add( new Group( rs.getInt( 1 ), rs.getString( 2 ), rs.getString( 3 ), this ) );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return groupsList.toArray( new Group[groupsList.size()] );
    }

    /**
     * Retrieves all <code>Group</code> s from the <code>Registry</code> that are associated with a given
     * <code>Role</code> DIRECTLY (i.e. not via inheritance).
     * 
     * @param securityAccess
     * @param role
     * @throws GeneralSecurityException
     */
    public Group[] getGroupsWithRole( SecurityAccess securityAccess, Role role )
                            throws GeneralSecurityException {
        ArrayList<Group> groups = new ArrayList<Group>( 100 );
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT SEC_GROUPS.ID,SEC_SECURABLE_OBJECTS.NAME,SEC_SECURABLE_OBJECTS.TITLE "
                     + "FROM SEC_SECURABLE_OBJECTS,SEC_GROUPS,SEC_JT_GROUPS_ROLES WHERE "
                     + "SEC_SECURABLE_OBJECTS.ID=SEC_GROUPS.ID AND "
                     + "SEC_JT_GROUPS_ROLES.FK_GROUPS=SEC_GROUPS.ID AND " + "SEC_JT_GROUPS_ROLES.FK_ROLES=?";
        try {
            pstmt = con.prepareStatement( sql );
            pstmt.setInt( 1, role.getID() );
            rs = pstmt.executeQuery();
            while ( rs.next() ) {
                groups.add( new Group( rs.getInt( 1 ), rs.getString( 2 ), rs.getString( 3 ), this ) );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return groups.toArray( new Group[groups.size()] );
    }

    /**
     * Retrieves all <code>Role</code> s from the <code>Registry</code> that are associated with a given
     * <code>User</code> DIRECTLY (i.e. not via group memberships).
     * 
     * @param securityAccess
     * @param user
     * @throws GeneralSecurityException
     */
    public Role[] getRolesForUser( SecurityAccess securityAccess, User user )
                            throws GeneralSecurityException {
        ArrayList<Role> roles = new ArrayList<Role>( 100 );
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT SEC_ROLES.ID,SEC_SECURABLE_OBJECTS.NAME "
                     + "FROM SEC_SECURABLE_OBJECTS,SEC_ROLES,SEC_JT_USERS_ROLES WHERE "
                     + "SEC_SECURABLE_OBJECTS.ID=SEC_ROLES.ID AND SEC_JT_USERS_ROLES.FK_ROLES=SEC_ROLES.ID "
                     + "AND SEC_JT_USERS_ROLES.FK_USERS=?";
        try {
            pstmt = con.prepareStatement( sql );
            pstmt.setInt( 1, user.getID() );
            rs = pstmt.executeQuery();
            while ( rs.next() ) {
                roles.add( new Role( rs.getInt( 1 ), rs.getString( 2 ), this ) );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return roles.toArray( new Role[roles.size()] );
    }

    /**
     * Retrieves all <code>Role</code> s from the <code>Registry</code> that are associated with a given
     * <code>Group</code> DIRECTLY (i.e. not via inheritance).
     * 
     * @param securityAccess
     * @param group
     * @throws GeneralSecurityException
     */
    public Role[] getRolesForGroup( SecurityAccess securityAccess, Group group )
                            throws GeneralSecurityException {
        ArrayList<Role> roles = new ArrayList<Role>( 100 );
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT SEC_ROLES.ID,SEC_SECURABLE_OBJECTS.NAME "
                     + "FROM SEC_SECURABLE_OBJECTS,SEC_ROLES,SEC_JT_GROUPS_ROLES WHERE "
                     + "SEC_SECURABLE_OBJECTS.ID=SEC_ROLES.ID AND SEC_JT_GROUPS_ROLES.FK_ROLES=SEC_ROLES.ID "
                     + "AND SEC_JT_GROUPS_ROLES.FK_GROUPS=?";
        try {
            pstmt = con.prepareStatement( sql );
            pstmt.setInt( 1, group.getID() );
            rs = pstmt.executeQuery();
            while ( rs.next() ) {
                roles.add( new Role( rs.getInt( 1 ), rs.getString( 2 ), this ) );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return roles.toArray( new Role[roles.size()] );
    }

    /**
     * Retrieves all <code>Role</code> s from the <code>Registry</code> that are associated with a given
     * <code>Group</code>s DIRECTLY (i.e. not via group memberships).
     * 
     * @param securityAccess
     * @param groups
     * @return the roles
     * 
     * @throws GeneralSecurityException
     */
    public Role[] getRolesForGroups( SecurityAccess securityAccess, Group[] groups )
                            throws GeneralSecurityException {
        ArrayList<Role> roles = new ArrayList<Role>( 100 );
        Connection con = acquireLocalConnection( securityAccess );
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;
        try {
            sql = "SELECT SEC_ROLES.ID,SEC_SECURABLE_OBJECTS.NAME "
                  + "FROM SEC_SECURABLE_OBJECTS,SEC_ROLES,SEC_JT_GROUPS_ROLES WHERE "
                  + "SEC_SECURABLE_OBJECTS.ID=SEC_ROLES.ID AND SEC_JT_GROUPS_ROLES.FK_ROLES=SEC_ROLES.ID AND (";
            for ( int i = 0; i < groups.length; i++ ) {
                sql += "SEC_JT_GROUPS_ROLES.FK_GROUPS=?";
                if ( i < groups.length - 1 ) {
                    sql += " OR ";
                }
            }
            sql += " )";
            LOG.logDebug( sql );
            pstmt = con.prepareStatement( sql );
            for ( int i = 0; i < groups.length; i++ ) {
                pstmt.setInt( i + 1, groups[i].getID() );
            }
            rs = pstmt.executeQuery();
            while ( rs.next() ) {
                roles.add( new Role( rs.getInt( 1 ), rs.getString( 2 ), this ) );
            }
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( securityAccess, con );
        }
        return roles.toArray( new Role[roles.size()] );
    }

    /**
     * Sets the <code>Group</code> s that a given <code>User</code> is member of DIRECTLY (i.e. not via inheritance).
     * 
     * @param transaction
     * @param user
     * @param groups
     * @throws GeneralSecurityException
     */
    public void setGroupsForUser( SecurityTransaction transaction, User user, Group[] groups )
                            throws GeneralSecurityException {
        PreparedStatement pstmt = null;

        try {
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_USERS_GROUPS WHERE FK_USERS=?" );
            pstmt.setInt( 1, user.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;

            for ( int i = 0; i < groups.length; i++ ) {
                pstmt = transactionalConnection.prepareStatement( "INSERT INTO SEC_JT_USERS_GROUPS (FK_USERS, FK_GROUPS) VALUES (?,?)" );
                pstmt.setInt( 1, user.getID() );
                pstmt.setInt( 2, groups[i].getID() );
                pstmt.executeUpdate();
                pstmt.close();
                pstmt = null;
            }
        } catch ( SQLException e ) {
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.setGroupsForUser() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
    }

    /**
     * Sets the <code>Group</code> s that a given <code>Group</code> is member of DIRECTLY (i.e. not via inheritance).
     * 
     * @param transaction
     * @param group
     * @param groups
     * @throws GeneralSecurityException
     */
    public void setGroupsForGroup( SecurityTransaction transaction, Group group, Group[] groups )
                            throws GeneralSecurityException {
        PreparedStatement pstmt = null;

        try {
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_GROUPS_GROUPS WHERE FK_GROUPS_MEMBER=?" );
            pstmt.setInt( 1, group.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;

            for ( int i = 0; i < groups.length; i++ ) {
                pstmt = transactionalConnection.prepareStatement( "INSERT INTO SEC_JT_GROUPS_GROUPS (FK_GROUPS_MEMBER, FK_GROUPS) VALUES (?,?)" );
                pstmt.setInt( 1, group.getID() );
                pstmt.setInt( 2, groups[i].getID() );
                pstmt.executeUpdate();
                pstmt.close();
                pstmt = null;
            }
        } catch ( SQLException e ) {
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.setGroupsForGroup() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
    }

    /**
     * Sets the <code>Group</code> s that a given <code>Role</code> is associated to DIRECTLY (i.e. not via
     * inheritance).
     * 
     * @param transaction
     * @param role
     * @param groups
     * @throws GeneralSecurityException
     */
    public void setGroupsWithRole( SecurityTransaction transaction, Role role, Group[] groups )
                            throws GeneralSecurityException {
        PreparedStatement pstmt = null;

        try {
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_GROUPS_ROLES WHERE FK_ROLES=?" );
            pstmt.setInt( 1, role.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;

            for ( int i = 0; i < groups.length; i++ ) {
                pstmt = transactionalConnection.prepareStatement( "INSERT INTO SEC_JT_GROUPS_ROLES (FK_GROUPS, FK_ROLES) VALUES (?,?)" );
                pstmt.setInt( 1, groups[i].getID() );
                pstmt.setInt( 2, role.getID() );
                pstmt.executeUpdate();
                pstmt.close();
                pstmt = null;
            }
        } catch ( SQLException e ) {
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.setGroupsWithRole() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
    }

    /**
     * Sets the <code>User</code> s that a given <code>Role</code> is associated to DIRECTLY (i.e. not via
     * <code>Group</code> membership).
     * 
     * @param transaction
     * @param role
     * @param users
     * @throws GeneralSecurityException
     */
    public void setUsersWithRole( SecurityTransaction transaction, Role role, User[] users )
                            throws GeneralSecurityException {

        PreparedStatement pstmt = null;

        try {
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_USERS_ROLES WHERE FK_ROLES=?" );
            pstmt.setInt( 1, role.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;

            for ( int i = 0; i < users.length; i++ ) {
                pstmt = transactionalConnection.prepareStatement( "INSERT INTO SEC_JT_USERS_ROLES (FK_USERS, FK_ROLES) VALUES (?,?)" );
                pstmt.setInt( 1, users[i].getID() );
                pstmt.setInt( 2, role.getID() );
                pstmt.executeUpdate();
                pstmt.close();
                pstmt = null;
            }
        } catch ( SQLException e ) {
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.setUsersWithRole() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
    }

    /**
     * Sets the <code>User</code> s that are members of a given <code>Group</code> DIRECTLY (i.e. not via inheritance).
     * 
     * @param transaction
     * @param group
     * @param users
     * @throws GeneralSecurityException
     */
    public void setUsersInGroup( SecurityTransaction transaction, Group group, User[] users )
                            throws GeneralSecurityException {
        PreparedStatement pstmt = null;

        try {
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_USERS_GROUPS WHERE FK_GROUPS=?" );
            pstmt.setInt( 1, group.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;

            for ( int i = 0; i < users.length; i++ ) {
                closeStatement( pstmt );
                pstmt = transactionalConnection.prepareStatement( "INSERT INTO SEC_JT_USERS_GROUPS (FK_USERS, FK_GROUPS) VALUES (?,?)" );
                pstmt.setInt( 1, users[i].getID() );
                pstmt.setInt( 2, group.getID() );
                pstmt.executeUpdate();
                pstmt.close();
                pstmt = null;
            }
        } catch ( SQLException e ) {
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.setUsersInGroup() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
    }

    /**
     * Sets the <code>Groups</code> s that are members of a given <code>Group</code> DIRECTLY (i.e. not via
     * inheritance).
     * 
     * @param transaction
     * @param group
     * @param groups
     * @throws GeneralSecurityException
     */
    public void setGroupsInGroup( SecurityTransaction transaction, Group group, Group[] groups )
                            throws GeneralSecurityException {
        PreparedStatement pstmt = null;

        try {
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_GROUPS_GROUPS WHERE FK_GROUPS=?" );
            pstmt.setInt( 1, group.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;

            for ( int i = 0; i < groups.length; i++ ) {
                pstmt = transactionalConnection.prepareStatement( "INSERT INTO SEC_JT_GROUPS_GROUPS (FK_GROUPS_MEMBER, FK_GROUPS) VALUES (?,?)" );
                pstmt.setInt( 1, groups[i].getID() );
                pstmt.setInt( 2, group.getID() );
                pstmt.executeUpdate();
                pstmt.close();
                pstmt = null;
            }
        } catch ( SQLException e ) {
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.setGroupsInGroup() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
    }

    /**
     * Sets the <code>Role</code> s that a given <code>User</code> is directly associated to (i.e. not via
     * <code>Group</code> membership).
     * 
     * @param transaction
     * @param user
     * @param roles
     * @throws GeneralSecurityException
     */
    public void setRolesForUser( SecurityTransaction transaction, User user, Role[] roles )
                            throws GeneralSecurityException {
        PreparedStatement pstmt = null;

        try {
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_USERS_ROLES WHERE FK_USERS=?" );
            pstmt.setInt( 1, user.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;

            for ( int i = 0; i < roles.length; i++ ) {
                pstmt = transactionalConnection.prepareStatement( "INSERT INTO SEC_JT_USERS_ROLES (FK_USERS, FK_ROLES) VALUES (?,?)" );
                pstmt.setInt( 1, user.getID() );
                pstmt.setInt( 2, roles[i].getID() );
                pstmt.executeUpdate();
                pstmt.close();
                pstmt = null;
            }
        } catch ( SQLException e ) {
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.setRolesForUser() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
    }

    /**
     * Sets the <code>Role</code> s that a given <code>Group</code> is associated to directly (i.e. not via
     * inheritance).
     * 
     * @param transaction
     * @param group
     * @param roles
     * @throws GeneralSecurityException
     */
    public void setRolesForGroup( SecurityTransaction transaction, Group group, Role[] roles )
                            throws GeneralSecurityException {
        PreparedStatement pstmt = null;

        try {
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_GROUPS_ROLES WHERE FK_GROUPS=?" );
            pstmt.setInt( 1, group.getID() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;

            for ( int i = 0; i < roles.length; i++ ) {
                pstmt = transactionalConnection.prepareStatement( "INSERT INTO SEC_JT_GROUPS_ROLES (FK_GROUPS, FK_ROLES) VALUES (?,?)" );
                pstmt.setInt( 1, group.getID() );
                pstmt.setInt( 2, roles[i].getID() );
                pstmt.executeUpdate();
                pstmt.close();
                pstmt = null;
            }
        } catch ( SQLException e ) {
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.setRolesForGroup() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
    }

    /**
     * Initializes the <code>SQLRegistry</code> -instance according to the contents of the submitted
     * <code>Properties</code>.
     * 
     * @param properties
     * @throws GeneralSecurityException
     */
    public void initialize( Properties properties )
                            throws GeneralSecurityException {
        this.dbDriver = properties.getProperty( "driver" );
        this.dbName = properties.getProperty( "url" );
        this.dbUser = properties.getProperty( "user" );
        this.dbPassword = properties.getProperty( "password" );
    }

    /**
     * Signals the <code>SQLRegistry</code> that a new transaction begins.
     * 
     * Only one transaction can be active at a time.
     * 
     * 
     * @param transaction
     * @throws GeneralSecurityException
     */
    public synchronized void beginTransaction( SecurityTransaction transaction )
                            throws GeneralSecurityException {
        try {
            transactionalConnection = DBConnectionPool.getInstance().acquireConnection( dbDriver, dbName, dbUser,
                                                                                        dbPassword );
            // transactionalConnection.setAutoCommit(false);
        } catch ( Exception e ) {
            throw new GeneralSecurityException( e );
        }
    }

    /**
     * Signals the <code>SQLRegistry</code> that the current transaction ends, i.e. the changes made by the transaction
     * are made persistent.
     * 
     * @param transaction
     * @throws GeneralSecurityException
     */
    public void commitTransaction( SecurityTransaction transaction )
                            throws GeneralSecurityException {
        try {
            transactionalConnection.commit();
        } catch ( SQLException e ) {
            throw new GeneralSecurityException( "Committing of transaction failed: " + e.getMessage() );
        } finally {
            try {
                DBConnectionPool.getInstance().releaseConnection( transactionalConnection, dbDriver, dbName, dbUser,
                                                                  dbPassword );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Signals the <code>SQLRegistry</code> that the current transaction shall be aborted. Changes made during the
     * transaction are undone.
     * 
     * @param transaction
     * @throws GeneralSecurityException
     */
    public void abortTransaction( SecurityTransaction transaction )
                            throws GeneralSecurityException {
        try {
            transactionalConnection.rollback();
        } catch ( SQLException e ) {
            throw new GeneralSecurityException( "Aborting of transaction failed: " + e.getMessage() );
        } finally {
            try {
                DBConnectionPool.getInstance().releaseConnection( transactionalConnection, dbDriver, dbName, dbUser,
                                                                  dbPassword );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Acquires a new <code>Connection</code>. If the given securityAccess is the exclusive Read/Write-transaction
     * holder, the transactionalConnection is returned, else a new <code>Connection</code> is taken from the pool.
     * 
     * @param securityAccess
     * @throws GeneralSecurityException
     */
    private Connection acquireLocalConnection( SecurityAccess securityAccess )
                            throws GeneralSecurityException {

        Connection con = null;

        if ( securityAccess instanceof SecurityTransaction ) {
            con = transactionalConnection;
        } else {
            try {
                con = DBConnectionPool.getInstance().acquireConnection( dbDriver, dbName, dbUser, dbPassword );
                // con.setAutoCommit(false);
            } catch ( Exception e ) {
                throw new GeneralSecurityException( e );
            }
        }
        return con;
    }

    /**
     * Releases a <code>Connection</code>. If the given securityAccess is the exclusive Read/Write-transaction holder,
     * nothing happens, else it is returned to the pool.
     * 
     * @param securityAccess
     * @param con
     * @throws GeneralSecurityException
     */
    private void releaseLocalConnection( SecurityAccess securityAccess, Connection con )
                            throws GeneralSecurityException {

        if ( !( securityAccess instanceof SecurityTransaction ) ) {
            if ( con != null ) {
                try {
                    DBConnectionPool.getInstance().releaseConnection( con, dbDriver, dbName, dbUser, dbPassword );
                } catch ( Exception e ) {
                    throw new GeneralSecurityException( e );
                }
            }
        }
    }

    /**
     * Closes the given <code>Statement</code> if it is not null.
     * 
     * @param stmt
     * @throws GeneralSecurityException
     */
    private void closeStatement( Statement stmt )
                            throws GeneralSecurityException {
        if ( stmt != null ) {
            try {
                stmt.close();
            } catch ( SQLException e ) {
                throw new GeneralSecurityException( e );
            }
        }
    }

    /**
     * Closes the given <code>ResultSet</code> if it is not null.
     * 
     * @param rs
     * @throws GeneralSecurityException
     */
    private void closeResultSet( ResultSet rs )
                            throws GeneralSecurityException {
        if ( rs != null ) {
            try {
                rs.close();
            } catch ( SQLException e ) {
                throw new GeneralSecurityException( e );
            }
        }
    }

    /**
     * Retrieves an unused PrimaryKey-value for the given table. The table must have its PrimaryKey in an Integer-field
     * named 'ID'.
     * 
     * @param table
     */
    private int getID( SecurityTransaction transaction, String table )
                            throws GeneralSecurityException {
        int id = 0;
        Connection con = acquireLocalConnection( transaction );

        try {
            DataBaseIDGenerator idGenerator = IDGeneratorFactory.createIDGenerator( con, table, "ID" );
            Object o = idGenerator.generateUniqueId();
            if ( !( o instanceof Integer ) ) {
                throw new GeneralSecurityException( "Error generating new PrimaryKey for table '" + table + "'." );
            }
            id = ( (Integer) o ).intValue();
        } catch ( SQLException e ) {
            throw new GeneralSecurityException( e );
        } finally {
            releaseLocalConnection( transaction, con );
        }
        return id;
    }

    /**
     * Tries to build a <code>ComplexFilter</code> from the given string representation.
     * 
     * @param constraints
     * @throws GeneralSecurityException
     */
    private ComplexFilter buildFilter( String constraints )
                            throws GeneralSecurityException {
        Filter filter = null;
        try {
            Document document = XMLTools.parse( new StringReader( constraints ) );
            Element element = document.getDocumentElement();
            filter = AbstractFilter.buildFromDOM( element, false );
        } catch ( FilterConstructionException e ) {
            throw new GeneralSecurityException( "The stored constraint is not a valid filter: " + e.getMessage() );
        } catch ( Exception e ) {
            throw new GeneralSecurityException( "Error parsing the stored constraint: " + e.getMessage() );
        }
        if ( !( filter instanceof ComplexFilter ) ) {
            throw new GeneralSecurityException( "The stored constraint is not of type 'ComplexFilter'." );
        }
        return (ComplexFilter) filter;
    }

    /**
     * @param access
     * @param address
     * @return the service from the db
     * @throws GeneralSecurityException
     */
    public Service getServiceByAddress( SecurityAccess access, String address )
                            throws GeneralSecurityException {
        Connection con = acquireLocalConnection( access );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        Service service;
        String sql = "select sec_services.id,sec_services.address,sec_services.title,sec_services.type,sec_services_objects.name,sec_services_objects.title from "
                     + " sec_services,sec_services_objects "
                     + "where sec_services.id = sec_services_objects.serviceid and sec_services.address = ?";
        try {
            pstmt = con.prepareStatement( sql );
            pstmt.setString( 1, address );
            rs = pstmt.executeQuery();
            String title = null;
            String type = null;
            LinkedList<StringPair> objects = new LinkedList<StringPair>();
            int id = 0;
            while ( rs.next() ) {
                id = rs.getInt( 1 );
                title = rs.getString( 3 );
                type = rs.getString( 4 );
                objects.add( new StringPair( rs.getString( 5 ), rs.getString( 6 ) ) );
            }

            if ( id == 0 ) {
                throw new UnknownException( "Lookup of service '" + address
                                            + "' failed! A service with this address does not exist." );
            }

            service = new Service( id, address, title, objects, type );
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( access, con );
        }

        return service;
    }

    /**
     * @param access
     * @param id
     * @return a new service object
     * @throws GeneralSecurityException
     */
    public Service getServiceById( SecurityAccess access, int id )
                            throws GeneralSecurityException {
        Connection con = acquireLocalConnection( access );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        Service service;
        String sql = "select sec_services.id,sec_services.address,sec_services.title,sec_services.type,sec_services_objects.name,sec_services_objects.title from "
                     + " sec_services,sec_services_objects "
                     + "where sec_services.id = sec_services_objects.serviceid and sec_services.id = ?";
        try {
            pstmt = con.prepareStatement( sql );
            pstmt.setInt( 1, id );
            rs = pstmt.executeQuery();
            String title = null;
            String type = null;
            String address = null;
            LinkedList<StringPair> objects = new LinkedList<StringPair>();
            while ( rs.next() ) {
                address = rs.getString( 2 );
                title = rs.getString( 3 );
                type = rs.getString( 4 );
                objects.add( new StringPair( rs.getString( 5 ), rs.getString( 6 ) ) );
            }

            if ( id == 0 ) {
                throw new UnknownException( "Lookup of service with id '" + id
                                            + "' failed! A service with this id does not exist." );
            }

            service = new Service( id, address, title, objects, type );
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( access, con );
        }

        return service;
    }

    /**
     * @param transaction
     * @param address
     * @param title
     * @param objects
     * @param type
     * @return the new service object
     * @throws GeneralSecurityException
     */
    public Service registerService( SecurityTransaction transaction, String address, String title,
                                    List<StringPair> objects, String type )
                            throws GeneralSecurityException {
        try {
            getServiceByAddress( transaction, address );
            throw new DuplicateException( "Registration of service '" + address + "' failed! A service with "
                                          + "this address already exists." );
        } catch ( UnknownException e ) {
            // then it's no duplicate
        }

        int id = getID( transaction, "SEC_SERVICES" );
        Service service = new Service( id, address, title, objects, type );

        PreparedStatement pstmt = null;

        try {
            pstmt = transactionalConnection.prepareStatement( "INSERT INTO SEC_SERVICES (ID,ADDRESS,TITLE,TYPE) VALUES (?,?,?,?)" );
            pstmt.setInt( 1, id );
            pstmt.setString( 2, address );
            pstmt.setString( 3, title );
            pstmt.setString( 4, type );
            pstmt.executeUpdate();
            closeStatement( pstmt );
            pstmt = null;

            for ( StringPair pair : objects ) {
                pstmt = transactionalConnection.prepareStatement( "INSERT INTO SEC_SERVICES_OBJECTS (SERVICEID,NAME,TITLE) VALUES (?,?,?)" );
                pstmt.setInt( 1, id );
                pstmt.setString( 2, pair.first );
                pstmt.setString( 3, pair.second );
                pstmt.executeUpdate();
                closeStatement( pstmt );
                pstmt = null;
            }
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.registerService() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }

        return service;
    }

    /**
     * @param transaction
     * @param service
     * @throws GeneralSecurityException
     */
    public void deregisterService( SecurityTransaction transaction, Service service )
                            throws GeneralSecurityException {
        PreparedStatement pstmt = null;

        try {
            pstmt = transactionalConnection.prepareStatement( "delete from sec_services where id = ?" );
            pstmt.setInt( 1, service.getId() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
            pstmt = transactionalConnection.prepareStatement( "delete from sec_services_objects where serviceid = ?" );
            pstmt.setInt( 1, service.getId() );
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.deregisterService() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
    }

    public LinkedList<Service> getAllServices( SecurityAccess access )
                            throws GeneralSecurityException {
        Connection con = acquireLocalConnection( access );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        LinkedList<String> addresses = new LinkedList<String>();
        LinkedList<Service> services = new LinkedList<Service>();
        String sql = "select address from sec_services";
        try {
            pstmt = con.prepareStatement( sql );
            rs = pstmt.executeQuery();
            while ( rs.next() ) {
                addresses.add( rs.getString( 1 ) );
            }
            rs.close();
            pstmt.close();
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( access, con );
        }

        for ( String address : addresses ) {
            services.add( getServiceByAddress( access, address ) );
        }

        return services;
    }

    /**
     * @param access
     * @param role
     * @return the accessible services for the role
     * @throws GeneralSecurityException
     */
    public LinkedList<Service> getRolesServices( SecurityAccess access, Role role )
                            throws GeneralSecurityException {
        Connection con = acquireLocalConnection( access );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        LinkedList<Service> services = new LinkedList<Service>();
        String sql = "select fk_services from sec_jt_roles_services where fk_roles = ?";
        try {
            pstmt = con.prepareStatement( sql );
            pstmt.setInt( 1, role.getID() );
            rs = pstmt.executeQuery();
            while ( rs.next() ) {
                int id = rs.getInt( 1 );
                services.add( getServiceById( access, id ) );
            }
            rs.close();
            pstmt.close();
        } catch ( SQLException e ) {
            LOG.logWarning( sql );
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( access, con );
        }

        return services;
    }

    public void updateService( SecurityTransaction transaction, Service oldService, Service newService )
                            throws GeneralSecurityException {
        Connection con = acquireLocalConnection( transaction );
        PreparedStatement pstmt = null;

        Map<String, String> oldObjects = new HashMap<String, String>();
        for ( StringPair pair : oldService.getObjects() ) {
            oldObjects.put( pair.first, pair.second );
        }
        Map<String, String> newObjects = new HashMap<String, String>();
        for ( StringPair pair : newService.getObjects() ) {
            newObjects.put( pair.first, pair.second );
        }

        try {
            for ( StringPair pair : newService.getObjects() ) {
                if ( !oldObjects.containsKey( pair.first ) ) {
                    pstmt = con.prepareStatement( "insert into sec_services_objects (serviceid, name, title) values (?,?,?)" );
                    pstmt.setInt( 1, oldService.getId() );
                    pstmt.setString( 2, pair.first );
                    pstmt.setString( 3, pair.second );
                    pstmt.executeUpdate();
                }
            }

            for ( StringPair pair : oldService.getObjects() ) {
                if ( !newObjects.containsKey( pair.first ) ) {
                    pstmt = con.prepareStatement( "delete from sec_services_objects where name = ? and serviceid = ?" );
                    pstmt.setString( 1, pair.first );
                    pstmt.setInt( 2, oldService.getId() );
                    pstmt.executeUpdate();
                } else {
                    pstmt = con.prepareStatement( "update sec_services_objects set title = ? where name = ? and serviceid = ?" );
                    pstmt.setString( 1, newObjects.get( pair.first ) );
                    pstmt.setString( 2, pair.first );
                    pstmt.setInt( 3, oldService.getId() );
                    pstmt.executeUpdate();
                    pstmt = con.prepareStatement( "update sec_securable_objects set title = ? where name = ?" );
                    pstmt.setString( 1, newObjects.get( pair.first ) );
                    pstmt.setString( 2, "[" + newService.getAddress() + "]:" + pair.first );
                    pstmt.executeUpdate();
                }
            }
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeStatement( pstmt );
            releaseLocalConnection( transaction, con );
        }
    }

    public void renameObject( SecurityTransaction transaction, Service service, String oldName, String newName )
                            throws GeneralSecurityException {
        Connection con = acquireLocalConnection( transaction );
        PreparedStatement pstmt = null;

        try {
            pstmt = con.prepareStatement( "update sec_services_objects set name = ? where name = ? and serviceid = ?" );
            pstmt.setString( 1, newName );
            pstmt.setString( 2, oldName );
            pstmt.setInt( 3, service.getId() );
            pstmt.executeUpdate();
            pstmt = con.prepareStatement( "update sec_securable_objects set name = ? where name = ?" );
            String prefix = "[" + service.getAddress() + "]:";
            pstmt.setString( 1, prefix + newName );
            pstmt.setString( 2, prefix + oldName );
            pstmt.executeUpdate();
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeStatement( pstmt );
            releaseLocalConnection( transaction, con );
        }
    }

    public void editService( SecurityTransaction transaction, Service service, String newTitle, String newAddress )
                            throws GeneralSecurityException {
        Connection con = acquireLocalConnection( transaction );
        PreparedStatement pstmt = null;

        try {
            if ( newTitle != null ) {
                pstmt = con.prepareStatement( "update sec_services set title = ? where id = ?" );
                pstmt.setString( 1, newTitle );
                pstmt.setInt( 2, service.getId() );
                pstmt.executeUpdate();
            }
            if ( newAddress != null ) {
                pstmt = con.prepareStatement( "update sec_services set address = ? where id = ?" );
                pstmt.setString( 1, newAddress );
                pstmt.setInt( 2, service.getId() );
                pstmt.executeUpdate();

                String oldPrefix = "[" + service.getAddress() + "]:";
                String newPrefix = "[" + newAddress + "]:";
                for ( StringPair pair : service.getObjects() ) {
                    pstmt = con.prepareStatement( "update sec_securable_objects set name = ? where name = ?" );
                    pstmt.setString( 1, newPrefix + pair.first );
                    pstmt.setString( 2, oldPrefix + pair.first );
                    pstmt.executeUpdate();
                }
            }
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( e );
        } finally {
            closeStatement( pstmt );
            releaseLocalConnection( transaction, con );
        }
    }

    public void setServiceRight( SecurityTransaction transaction, Service service, Role role, RightType right )
                            throws GeneralSecurityException {
        PreparedStatement pstmt = null;

        try {
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_ROLES_SERVICES WHERE FK_ROLES=? AND FK_SERVICES=?" );
            pstmt.setInt( 1, role.getID() );
            pstmt.setInt( 2, service.getId() );
            pstmt.executeUpdate();
            pstmt.close();

            if ( right != null ) {
                pstmt = transactionalConnection.prepareStatement( "INSERT INTO SEC_JT_ROLES_SERVICES (FK_ROLES, FK_SERVICES, FK_RIGHTS) VALUES (?,?,?)" );
                pstmt.setInt( 1, role.getID() );
                pstmt.setInt( 2, service.getId() );
                pstmt.setInt( 3, right.getID() );
                pstmt.executeUpdate();
                pstmt.close();
            }
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.setRights() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
    }

    public boolean hasServiceRight( SecurityAccess access, Service service, Role role, RightType right )
                            throws GeneralSecurityException {
        Connection conn = acquireLocalConnection( access );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = conn.prepareStatement( "SELECT * FROM SEC_JT_ROLES_SERVICES WHERE FK_ROLES = ? AND FK_SERVICES = ? AND FK_RIGHTS = ?" );
            pstmt.setInt( 1, role.getID() );
            pstmt.setInt( 2, service.getId() );
            pstmt.setInt( 3, right.getID() );
            rs = pstmt.executeQuery();
            return rs.next();
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( "SQLRegistry.getRights() failed. Error message: " + e.getMessage() );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( access, conn );
        }
    }

    public String getConstraints( SecurityAccess access, Role role, Service service )
                            throws GeneralSecurityException {
        Connection conn = acquireLocalConnection( access );
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = conn.prepareStatement( "SELECT constraints FROM SEC_JT_ROLES_CONSTRAINTS WHERE FK_ROLES = ? AND FK_SERVICES = ?" );
            pstmt.setInt( 1, role.getID() );
            pstmt.setInt( 2, service.getId() );
            rs = pstmt.executeQuery();
            if ( rs.next() ) {
                return rs.getString( 1 );
            }
            return null;
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            throw new GeneralSecurityException( "SQLRegistry.getConstraints() failed. Error message: " + e.getMessage() );
        } finally {
            closeResultSet( rs );
            closeStatement( pstmt );
            releaseLocalConnection( access, conn );
        }
    }

    public void setConstraints( SecurityTransaction transaction, Service service, Role role, String constraints )
                            throws GeneralSecurityException {
        PreparedStatement pstmt = null;

        try {
            pstmt = transactionalConnection.prepareStatement( "DELETE FROM SEC_JT_ROLES_CONSTRAINTS WHERE FK_ROLES=? AND FK_SERVICES=?" );
            pstmt.setInt( 1, role.getID() );
            pstmt.setInt( 2, service.getId() );
            pstmt.executeUpdate();
            pstmt.close();

            if ( constraints != null ) {
                pstmt = transactionalConnection.prepareStatement( "INSERT INTO SEC_JT_ROLES_CONSTRAINTS (FK_ROLES, FK_SERVICES, CONSTRAINTS) VALUES (?,?,?)" );
                pstmt.setInt( 1, role.getID() );
                pstmt.setInt( 2, service.getId() );
                pstmt.setString( 3, constraints );
                pstmt.executeUpdate();
                pstmt.close();
            }
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            closeStatement( pstmt );
            abortTransaction( transaction );
            throw new GeneralSecurityException( "SQLRegistry.setRights() failed. Rollback performed. "
                                                + "Error message: " + e.getMessage() );
        }
    }
}
