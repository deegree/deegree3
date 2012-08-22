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
package org.deegree.security.session;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This exception shall be thrown when a session(ID) will be used that has been expired.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */

public class MemoryBasedSessionManager implements SessionManager {

    private Map<String, Session> sessionsById = Collections.synchronizedMap( new HashMap<String, Session>( 100 ) );

    private Map<String, Session> sessionsByUser = Collections.synchronizedMap( new HashMap<String, Session>( 100 ) );

    private static URL config = null;

    private static MemoryBasedSessionManager self = null;

    /**
     * realizes Singelton pattern <br>
     * returns an instance of the <tt>SessionManager</tt>. Before this method can be invoked
     *
     * @return single instance of SessionManager in a JVM
     */
    public synchronized static MemoryBasedSessionManager getInstance() {
        if ( self == null ) {
            self = new MemoryBasedSessionManager( config );
        }
        return self;
    }

    /**
     * creates a session that never expires for a named user who will be authentificated through his name and password.
     * If the user doesn't exists or the passwoed is invalid for an existing user an exception will be thrown.
     *
     * @param user
     *            user name
     * @return the session
     */
    public static Session createSession( String user ) {
        return createSession( user, -1 );
    }

    /**
     * creates a session for a named user who will be authentificated through his name and password. The session expires
     * after the passed duration after the last access to it. If the user doesn't exists or the passwoed is invalid for
     * an existing user an exception will be thrown.
     *
     * @param user
     *            user name
     * @param duration
     * @return the session
     */
    public static Session createSession( String user, int duration ) {

        Session ses = new Session( user, duration );
        try {
            MemoryBasedSessionManager.getInstance().addSession( ses );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return ses;
    }

    /**
     * creates a session for an anonymous user that never expires
     *
     * @return the session
     */
    public static Session createSession() {
        return createSession( -1 );
    }

    /**
     * creates a session for an anonymous user that expires after the passed duration after the last access to it.
     *
     * @param duration
     * @return the session
     */
    public static Session createSession( int duration ) {
        Session ses = new Session( duration );
        try {
            MemoryBasedSessionManager.getInstance().addSession( ses );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return ses;
    }

    /**
     * private constructor. just to be used by the initSessionManager method
     *
     * @param config
     */
    private MemoryBasedSessionManager( URL config ) {
        MemoryBasedSessionManager.config = config;
    }

    /**
     * returns the session identified by its ID. If no session with the passed ID is known <tt>null</tt> will be
     * returned. If the requested session isn't alive anymore it will be removed from the session manager
     *
     * @param id
     * @return the session identified by its ID. If no session with the passed ID is known <tt>null</tt> will be
     *         returned.
     * @throws SessionStatusException
     */
    public Session getSessionByID( String id )
                            throws SessionStatusException {
        Session ses = sessionsById.get( id );
        if ( ses != null ) {
            if ( !ses.isAlive() ) {
                removeSessionByID( id );
            } else {
                ses.reset();
            }
        }
        return ses;
    }

    /**
     * returns the session assigned to the passed user. If no session is assigend to the passed user <tt>null</tt> will
     * be returned. If the requested session isn't alive anymore it will be removed from the session manager
     *
     * @param user
     * @return the session assigned to the passed user. If no session is assigend to the passed user <tt>null</tt> will
     *         be returned.
     */
    public Session getSessionByUser( String user )
                            throws SessionStatusException {
        Session ses = sessionsByUser.get( user );
        if ( ses != null ) {
            if ( !ses.isAlive() ) {
                removeSessionByID( ses.getSessionID().getId() );
            } else {
                ses.reset();
            }
        }
        return ses;
    }

    /**
     * adds a session to the session managment. the session will be stored within two lists. one addresses the session
     * with its ID the other with its user name. If the session is anonymous it just will be stored in the first list.
     *
     * @param session
     * @throws SessionStatusException
     */
    public void addSession( Session session )
                            throws SessionStatusException {
        if ( session.getUser() != null ) {
            sessionsByUser.put( session.getUser(), session );
        }
        try {
            sessionsById.put( session.getSessionID().getId(), session );
        } catch ( Exception e ) {
            throw new SessionStatusException( "can't add session to session manager:\n" + e.getMessage() );
        }
    }

    /**
     * removes a session identified by its ID from the session managment. the removed session will be returned.
     *
     * @param id
     * @return the session
     */
    public Session removeSessionByID( String id ) {
        Session ses = sessionsById.remove( id );
        if ( ses != null && ses.getUser() != null ) {
            sessionsByUser.remove( ses.getUser() );
        }
        return ses;
    }

    /**
     * removes all sessions that are expired from the session management
     */
    public synchronized void clearExpired() {
        synchronized ( sessionsById ) {
            synchronized ( sessionsByUser ) {
                Iterator<String> ids = sessionsById.keySet().iterator();
                while ( ids.hasNext() ) {
                    Object key = ids.next();
                    Session ses = sessionsById.get( key );
                    if ( !ses.isAlive() ) {
                        sessionsById.remove( key );
                        if ( ses.getUser() != null ) {
                            sessionsByUser.remove( ses.getUser() );
                        }
                    }
                }
            }
        }

    }

}
