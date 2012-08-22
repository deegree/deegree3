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
package org.deegree.enterprise.control;

import java.util.HashMap;

import org.deegree.portal.PortalException;

/**
 * The class listens to the get request against iso 19115 formated metadata here the result shall be
 * returned in full format
 *
 * <p>
 * ---------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @version $Revision$ $Date$
 */
public abstract class AbstractDBListener extends AbstractListener {

    /**
     * This method is called either to search for metadata or to lookup keywords from the thesaurus
     */
    public void actionPerformed( FormEvent event ) {

        RPCWebEvent rpcEvent = (RPCWebEvent) event;

        try {
            validateRequest( rpcEvent );
        } catch ( Exception e ) {
            gotoErrorPage( "Invalid Operations: " + e.toString() );
            return;
        }

        HashMap result = null;
        try {
            result = performRequest( rpcEvent );
        } catch ( Exception e ) {
            e.printStackTrace();
            gotoErrorPage( "Invalid Operations: " + e.toString() );
            return;
        }

        Object res = null;
        try {
            res = validateResult( result );
        } catch ( Exception e ) {
            e.printStackTrace();
            gotoErrorPage( "Invalid Result: " + e.toString() );
            return;
        }

        try {
            handleResult( res );
        } catch ( Exception e ) {
            gotoErrorPage( "Error handling result: " + e.toString() );
            return;
        }

    }

    /**
     * validates the request to be performed.
     *
     * @param event
     *            event object containing the request to be performed
     */
    protected abstract void validateRequest( RPCWebEvent event )
                            throws PortalException;

    /**
     * creates a request from the <tt>RPCWebEvent</tt> passed to this listener
     *
     * @param rpcEvent
     *            event object containing the request to be performed
     * @return string representation of a request
     * @exception PortalException
     *                will be throwns if it is impossible to create the request
     */
    protected abstract HashMap createRequest( RPCWebEvent rpcEvent )
                            throws PortalException;

    /**
     * performs the request contained in the passed <tt>RPCWebEvent</tt>
     *
     * @param event
     *            event object containing the request to be performed
     * @return result of the GetRecord request
     * @exception PortalException
     */
    protected abstract HashMap performRequest( RPCWebEvent event )
                            throws PortalException;

    /**
     * validates the result of the catalog request and returns an <tt>Object</tt> depending on the
     * results content.
     *
     * @param result
     *            result to a request
     * @return validated result to a request
     * @exception PortalException
     */
    protected abstract Object validateResult( HashMap result )
                            throws PortalException;

    /**
     * handles the result of a 'FULL' catalog query
     *
     * @param result
     *            result to a GetRecord request
     */
    protected abstract void handleResult( Object result )
                            throws PortalException;

}
