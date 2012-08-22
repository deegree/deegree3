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

/**
 * This exception shall be thrown when a session(ID) will be used that has been expired.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public interface SessionManager {

    /**
     * returns the session identified by its ID. If no session with the passed ID is known
     * <tt>null</tt> will be returned. If the requested session isn't alive anymore it will be
     * removed from the session manager
     *
     * @param id
     * @return the session identified by its ID. If no session with the passed ID is known
     *         <tt>null</tt> will be returned.
     * @throws SessionStatusException
     */
    public Session getSessionByID( String id )
                            throws SessionStatusException;

    /**
     * returns the session assigned to the passed user. If no session is assigend to the passed user
     * <tt>null</tt> will be returned. If the requested session isn't alive anymore it will be
     * removed from the session manager
     *
     * @param user
     * @return the session assigned to the passed user. If no session is assigend to the passed user
     *         <tt>null</tt> will be returned.
     * @throws SessionStatusException
     */
    public Session getSessionByUser( String user )
                            throws SessionStatusException;

    /**
     * adds a session to the session managment. the session will be stored within two lists. one
     * addresses the session with its ID the other with its user name. If the session is anonymous
     * it just will be stored in the first list.
     *
     * @param session
     * @throws SessionStatusException
     */
    public void addSession( Session session )
                            throws SessionStatusException;

    /**
     * removes a session identified by its ID from the session managment. the removed session will
     * be returned.
     *
     * @param id
     * @return the session
     */
    public Session removeSessionByID( String id );

    /**
     * removes all sessions that are expired from the session management
     */
    public void clearExpired();

}
