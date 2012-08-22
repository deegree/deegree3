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

package org.deegree.ogcwebservices.wass.wss.operation;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.ogcwebservices.wass.common.AuthenticationData;
import org.deegree.ogcwebservices.wass.common.WASSSecurityManager;
import org.deegree.ogcwebservices.wass.exceptions.DoServiceException;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.model.User;

/**
 * This class handles a webservice request which is . It's primary roles are to check if the user
 * has (sufficient) credentials and to delegate the request to the service provider behind this
 * proxy.
 *
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public class DoServicePasswordHandler extends DoServiceHandler {

    private static final ILogger LOG = LoggerFactory.getLogger( DoServicePasswordHandler.class );

    private final SecurityAccessManager manager;

    /**
     * @param securityManager
     * @throws GeneralSecurityException
     */
    public DoServicePasswordHandler( WASSSecurityManager securityManager )
                            throws GeneralSecurityException {
        manager = securityManager.getSecurityAccessManager();
    }

    /**
     * Checks if the request has sufficient credentials to request the feature, and if so request
     * the feature at the service.
     *
     * @throws DoServiceException
     */
    @Override
    public void handleRequest( DoService request )
                            throws DoServiceException {

        AuthenticationData authData = request.getAuthenticationData();
        // password authentication used?
        if ( authData.usesPasswordAuthentication() ) {
            try {
                String user = authData.getUsername();
                String pass = authData.getPassword();
                User usr = manager.getUserByName( user );
                usr.authenticate( pass );
                // SecurityAccess secAccess = manager.acquireAccess( usr );
                // usr.hasRight( secAccess );
                /**
                 * TODO Here it is specified that the wss should check if the user has the
                 * sufficient right to do the service request. Deegree does these request in the
                 * owsRequestvalidator package, which means we only support - for the moment - a
                 * check if the user is registered. For Details on how to get the right for
                 * particular object please look at the following method.
                 *
                 * @see org.deegree.security.owsrequestvalidator.GetFeatureRequestValidator#validateAgainstRightsDB
                 *
                 */
            } catch ( GeneralSecurityException e ) {
                LOG.logError( e.getLocalizedMessage(), e );
                throw new DoServiceException( e.getLocalizedMessage(), e );
            } catch ( StringIndexOutOfBoundsException e ) {
                LOG.logError( e.getLocalizedMessage(), e );
                throw new DoServiceException( Messages.getMessage( "WASS_ERROR_USERPASS_NOT_PARSED",
                                                               "WSS" ) );
            }
        }

        setRequestAllowed( true );

    }

}
