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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */

public class Session {

    private SessionID sessionID = null;

    private String user = null;

    private Map<Object, Object> attributes = Collections.synchronizedMap( new HashMap<Object, Object>() );

    /**
     * creates a session that never expires for an anonymous user
     */
    public Session() {
        this.sessionID = new SessionID( -1 );
    }

    /**
     * creates a session that never expires
     *
     * @param user
     *            user the session is assigned to
     */
    public Session( String user ) {
        this.sessionID = new SessionID( -1 );
        this.user = user;
    }

    /**
     * creates a session with a specific lifetime for an anonymous user. the expiration date will be
     * updated each time a user accesses his session
     *
     * @param duration
     */
    public Session( int duration ) {
        this( null, duration );
    }

    /**
     * creates a session with a specific lifetime. the expiration date will be updated each time a
     * uses accesses his session
     *
     * @param duration
     * @param user
     */
    public Session( String user, int duration ) {
        this.sessionID = new SessionID( duration );
        this.user = user;
    }

    /**
     * creates a session with a specific SessionID for an anonymous user. the expiration date will
     * be updated each time a uses accesses his session
     *
     * @param sessionID
     */
    public Session( SessionID sessionID ) {
        this( null, sessionID );
    }

    /**
     * creates a session with a specific SessionID. the expiration date will be updated each time a
     * uses accesses his session
     *
     * @param sessionID
     * @param user
     */
    public Session( String user, SessionID sessionID ) {
        super();
        this.sessionID = sessionID;
        this.user = user;
    }

    /**
     * returns the name user the user who owns the session. returns null if its a session for an
     * anonymous user
     *
     * @return the name user the user who owns the session. returns <code>null</code> if its a
     *         session for an anonymous user
     *
     */
    public String getUser() {
        return user;
    }

    /**
     * adds an attribute to the session. calling this method will reset the expiration date of the
     * encapsulated sessionID<br>
     * this method throws an exception if the sessinID has been killed or is alive anymore
     *
     * @param key
     * @param value
     * @throws SessionStatusException
     */
    public void addAttribute( Object key, Object value )
                            throws SessionStatusException {
        sessionID.reset();
        attributes.put( key, value );
    }

    /**
     * returns the values of the attribute identified by the passed key. calling this method will
     * reset the expiration date of the encapsulated sessionID<br>
     * this method throws an exception if the sessinID has been killed or is alive anymore
     *
     * @param key
     * @return the values of the attribute identified by the passed key. calling this method will
     *         reset the expiration date of the encapsulated sessionID
     * @throws SessionStatusException
     */
    public Object getAttribute( Object key )
                            throws SessionStatusException {
        sessionID.reset();
        return attributes.get( key );
    }

    /**
     * removes the attribute identified by the passed key from the session. calling this method will
     * reset the expiration date of the encapsulated sessionID<br>
     * this method throws an exception if the sessinID has been killed or is alive anymore
     *
     * @param key
     * @return the attribute
     * @throws SessionStatusException
     */
    public Object removeAttribute( Object key )
                            throws SessionStatusException {
        sessionID.reset();
        return attributes.remove( key );
    }

    /**
     * returns true if the session is still alive or false if the expiration date of the sessionID
     * has been reached
     *
     * @return <code>true</code> if the session is still alive or <code>false</code> if the
     *         expiration date of the sessionID has been reached
     */
    public boolean isAlive() {
        return sessionID.isAlive();
    }

    /**
     * returns the sessionID encapsulated in this session.
     *
     * @return the sessionID encapsulated in this session.
     *
     */
    public SessionID getSessionID() {
        return sessionID;
    }

    /**
     * kills a Session by marking the encapsulated SessionID as invalid. A killed SessionID can't be
     * reseted
     */
    public void close() {
        sessionID.close();
    }

    /**
     * resets the expiration date of the session
     *
     * @throws SessionStatusException
     *
     */
    public void reset()
                            throws SessionStatusException {
        sessionID.reset();
    }
}
