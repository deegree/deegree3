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

import java.sql.Timestamp;
import java.util.Date;
import java.util.GregorianCalendar;

import org.deegree.framework.util.IDGenerator;

/**
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class SessionID {

    private String id = null;

    private Date created = null;

    private Date expiration = null;

    private int duration = 0;

    private boolean closed = false;

    /**
     * The constructor ensures that a sessionID that is unique within the JVM will be created. If
     * the sessionID must be unique across two or more JVMs use the construtor
     * {@link #SessionID(String, int)} and ensure that the passed ID is valid.
     *
     * @param duration
     *            duration in millis the ID is valid. -1 indicates that a sessionID never expires.
     */
    public SessionID( int duration ) {
        this.id = "ID" + IDGenerator.getInstance().generateUniqueID() + "-" + Math.random();
        this.duration = duration;
        created = ( new GregorianCalendar() ).getTime();
        expiration = new Timestamp( created.getTime() + duration );
    }

    /**
     * @param id
     *            encapsulated ID
     * @param duration
     *            duration in millis the ID is valid. -1 indicates that a sessionID never expires.
     */
    public SessionID( String id, int duration ) {
        this.id = id;
        this.duration = duration;
        created = ( new GregorianCalendar() ).getTime();
        expiration = new Timestamp( created.getTime() + duration );
    }

    /**
     * @return Returns the created.
     *
     */
    public Date getCreated() {
        return created;
    }

    /**
     * if sessionID never expires this method returns <tt>new Timestamp(0)</tt>
     *
     * @return Returns the expiration.
     *
     */
    public Date getExpiration() {
        if ( duration < 0 )
            return new Timestamp( 0 );
        return expiration;
    }

    /**
     * this method throws an exception if the sessinID has been killed or is alive anymore
     *
     * @return Returns the id.
     * @throws SessionStatusException
     *
     */
    public String getId()
                            throws SessionStatusException {
        if ( closed ) {
            throw new SessionStatusException( "session has been killed" );
        }
        if ( !isAlive() ) {
            throw new SessionExpiredException( "session has been expired" );
        }
        return id;
    }

    /**
     * returns true if the expiration date of the <tt>SessionID</tt> is after the current
     * timestamp
     *
     * @return <code>true</code> if the expiration date of the <tt>SessionID</tt> is after the
     *         current timestamp
     */
    public boolean isAlive() {
        if ( closed )
            return false;
        if ( duration < 0 )
            return true;
        return expiration.after( new Timestamp( System.currentTimeMillis() ) );
    }

    /**
     * resets the expiration date to the current timestamp plus duration passed to the constructor
     * when creating the <tt>SessionID</tt> this method throws an exception if the sessinID has
     * been killed or is alive anymore
     *
     * @throws SessionStatusException
     */
    public void reset()
                            throws SessionStatusException {
        if ( closed ) {
            throw new SessionStatusException( "session has been killed" );
        }
        if ( !isAlive() ) {
            throw new SessionExpiredException( "session has been expired" );
        }
        if ( duration < 0 )
            return;
        expiration = new Timestamp( System.currentTimeMillis() + duration );
    }

    /**
     * kills a SessionID by marking it as invalid. A killed SessionID can't be reseted
     */
    public void close() {
        closed = true;
    }
}
