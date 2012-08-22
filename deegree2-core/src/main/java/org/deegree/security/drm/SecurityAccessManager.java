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

import java.util.Properties;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.UnauthorizedException;
import org.deegree.security.drm.model.Role;
import org.deegree.security.drm.model.User;

/**
 * This singleton manages access to the data stored in an associated <code>SecurityRegistry</code> -instance.
 * <p>
 * In order to use methods that read from the registry, a <code>SecurityAccess</code> instance has to be acquired first:
 * <p>
 * <b>Example Code: </b>
 * 
 * <pre>
 * SecurityAccess access = SecurityAccessManager.getInstance();
 * 
 * ReadToken accessToken = access.acquireReadToken();
 * 
 * Role role = access.getRoleById( accessToken, 1 );
 * </pre>
 * 
 * <p>
 * If write access is needed as well, one has to acquire the exclusive <code>SecurityTransaction</code>. This is only
 * possible if the <code>User</code> has the "write"-privilege.
 * <p>
 * <b>Example Code: </b>
 * 
 * <pre>
 *   SecurityAccess access = SecurityAccess.getInstance ();
 *   SecurityTransaction lock = access.acquireSecurityTransaction (user);
 *   access.registerUser (lock, &quot;TESTUSER&quot;);
 *   ...
 *   access.commitTransaction (lock);
 *   // after committing changes are made persistent
 * </pre>
 * 
 * @author <a href="mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:wanhoff$
 * 
 * @version $Revision$, $Date:26.03.2007$
 * 
 */
public class SecurityAccessManager {

    private static final ILogger LOG = LoggerFactory.getLogger( SecurityAccessManager.class );

    private static SecurityAccessManager instance = null;

    private SecurityRegistry registry = null;

    // the currently valid (exclusive) transaction
    private SecurityTransaction currentTransaction;

    // maximal duration that a transaction lasts (milliseconds)
    private long timeout;

    // admin user (predefined)
    private User adminUser;

    // admin group (predefined)
    // private Group adminGroup;

    // admin role (predefined)
    private Role adminRole;

    /**
     * Initializes the <code>SecurityAccessManager</code> -singleton with the given <code>Registry</code> -instance.
     * 
     * @param registryClassName
     * @param registryProperties
     * @param timeout
     * @throws GeneralSecurityException
     */
    public static synchronized void initialize( String registryClassName, Properties registryProperties, long timeout )
                            throws GeneralSecurityException {
        if ( SecurityAccessManager.instance != null ) {
            throw new GeneralSecurityException( "SecurityAccessManager may only be initialized once." );
        }
        SecurityRegistry registry;
        try {
            registry = (SecurityRegistry) Class.forName( registryClassName ).newInstance();
        } catch ( Exception e ) {
            throw new GeneralSecurityException( "Unable to instantiate RegistryClass for class name '"
                                                + registryClassName + "': " + e.getMessage() );
        }
        registry.initialize( registryProperties );
        SecurityAccessManager.instance = new SecurityAccessManager( registry, timeout );
    }

    /**
     * @return true if there is an instance
     */
    public static boolean isInitialized() {
        return SecurityAccessManager.instance != null;
    }

    /**
     * Returns the only instance of this class.
     * 
     * @return the only instance of this class.
     * @throws GeneralSecurityException
     * 
     */
    public static synchronized SecurityAccessManager getInstance()
                            throws GeneralSecurityException {
        if ( SecurityAccessManager.instance == null ) {
            throw new GeneralSecurityException( "SecurityAccessManager has not been initialized yet." );
        }
        return SecurityAccessManager.instance;
    }

    /**
     * This method is only to be used to get an initial <code>User</code> object. (Otherwise one would need a
     * <code>User</code> to perform a <code>User</code> lookup.)
     * 
     * @param name
     * @return the user
     * @throws GeneralSecurityException
     */
    public User getUserByName( String name )
                            throws GeneralSecurityException {
        return registry.getUserByName( null, name );
    }

    /**
     * Tries to acquire a <code>SecurityAccess</code> -instance.
     * 
     * @param user
     * @return the instance
     * 
     * @throws GeneralSecurityException
     * @throws UnauthorizedException
     */
    public SecurityAccess acquireAccess( User user )
                            throws GeneralSecurityException, UnauthorizedException {

        if ( user == null ) {
            throw new UnauthorizedException( "Can't acquire security access for anonymous user" );
        }
        if ( !user.isAuthenticated() ) {
            throw new UnauthorizedException( "Can't acquire security access for '" + user.getName()
                                             + "'. User has not been authorized to the system." );
        }

        return new SecurityAccess( user, registry );
    }

    /**
     * Tries to acquire the <code>SecurityTransaction</code> for the given <code>User</code>. Only possibly for
     * <code>User</code> s that have the "modify"-privilege.
     * <p>
     * NOTE: The implementation checks if the <code>currentTransaction</code> timed out BEFORE it checks if the user is
     * allowed to write to the registry at all. This is because some JDBC-drivers (at least the JDBC-ODBC- bridge
     * together with Microsoft Access (tm)) have been observed to return strange results sometimes when there's a
     * transaction still going on (so that the privileges of the user cannot be retrieved reliably from the registry).
     * 
     * @param user
     * @return the transaction
     * 
     * @throws GeneralSecurityException
     * @throws UnauthorizedException
     */
    public SecurityTransaction acquireTransaction( User user )
                            throws GeneralSecurityException, UnauthorizedException {
        if ( currentTransaction != null ) {
            if ( System.currentTimeMillis() < currentTransaction.getTimestamp() + timeout ) {
                throw new ReadWriteLockInUseException( "Can't get ReadWriteLock, because it is currently in use." );
            }
            try {
                registry.abortTransaction( currentTransaction );
            } catch ( GeneralSecurityException e ) {
                e.printStackTrace();
            }

        }
        if ( !user.isAuthenticated() ) {
            throw new UnauthorizedException( "Can't acquire ReadWriteLock for '" + user.getName()
                                             + "'. User has not been authorized " + "to the system." );
        }
        SecurityAccess tempAccess = new SecurityAccess( user, registry );
        if ( !user.hasPrivilege( tempAccess, tempAccess.getPrivilegeByName( "write" ) ) ) {
            throw new UnauthorizedException( "Can't acquire transaction: " + "User is not allowed to perform changes." );
        }
        currentTransaction = new SecurityTransaction( user, registry, adminRole );
        registry.beginTransaction( currentTransaction );
        return currentTransaction;
    }

    /**
     * Private constructor to enforce the singleton pattern.
     * 
     * @param registry
     * @param timeout
     * @throws GeneralSecurityException
     */
    private SecurityAccessManager( SecurityRegistry registry, long timeout ) throws GeneralSecurityException {
        this.registry = registry;
        this.timeout = timeout;

        adminUser = getUserByName( "SEC_ADMIN" );
        SecurityAccess access = new SecurityAccess( adminUser, registry );
        // TODO adminGroup will never been read; can be removed?
        // adminGroup = access.getGroupByName( "SEC_ADMIN" );
        adminRole = registry.getRoleByName( access, "SEC_ADMIN" );
    }

    /**
     * Verifies that the submitted <code>Transaction</code> is valid. There are two ways for it to become invalid:
     * <ul>
     * <li>it is too old (and timed out)
     * <li>it ended (has been aborted / committed)
     * </ul>
     * 
     * @param transaction
     * @throws ReadWriteLockInvalidException
     * @throws GeneralSecurityException
     *             if transaction is invalid
     */
    void verify( SecurityTransaction transaction )
                            throws ReadWriteLockInvalidException {
        if ( transaction == null || transaction != currentTransaction ) {
            throw new ReadWriteLockInvalidException( "The SecurityTransaction is invalid." );
        } else if ( System.currentTimeMillis() > currentTransaction.getTimestamp() + timeout ) {
            long ts = currentTransaction.getTimestamp();
            currentTransaction = null;
            try {
                registry.abortTransaction( transaction );
            } catch ( GeneralSecurityException e ) {
                e.printStackTrace();
            }
            LOG.logInfo( "timeout: " + timeout );
            LOG.logInfo( "current: " + System.currentTimeMillis() );
            LOG.logInfo( "lock ts: " + ts );

            throw new ReadWriteLockInvalidException( "The SecurityTransaction timed out." );
        }
        currentTransaction.renew();
    }

    /**
     * Ends the current transaction and commits all changes to the <code>Registry</code>.
     * 
     * @param transaction
     * 
     * @throws GeneralSecurityException
     */
    public void commitTransaction( SecurityTransaction transaction )
                            throws GeneralSecurityException {
        verify( transaction );
        currentTransaction = null;
        registry.commitTransaction( transaction );
    }

    /**
     * Aborts the current transaction and undoes all changes made to the <code>Registry</code>.
     * 
     * @param lock
     * 
     * @throws GeneralSecurityException
     */
    public void abortTransaction( SecurityTransaction lock )
                            throws GeneralSecurityException {
        verify( lock );
        currentTransaction = null;
        registry.abortTransaction( lock );
    }
}
