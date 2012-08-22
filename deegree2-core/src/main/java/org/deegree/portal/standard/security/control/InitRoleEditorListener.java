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
package org.deegree.portal.standard.security.control;

import static org.deegree.i18n.Messages.get;

import org.deegree.enterprise.control.AbstractListener;
import org.deegree.enterprise.control.FormEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.drm.SecurityAccess;

/**
 * This <code>Listener</code> reacts on 'initRoleGroupEditor' events and passes the roles to be displayed on the JSP.
 *
 * Access constraints:
 * <ul>
 * <li>only users that have the 'SEC_ADMIN'-role are allowed</li>
 * </ul>
 *
 * @author <a href="mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class InitRoleEditorListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( InitRoleEditorListener.class );

    @Override
    public void actionPerformed( FormEvent event ) {

        try {
            // perform access check
            SecurityAccess access = SecurityHelper.acquireAccess( this );
            if ( access == null ) {
                getRequest().setAttribute( "SOURCE", this.getClass().getName() );
                getRequest().setAttribute( "MESSAGE", get( "IGEO_STD_SEC_ERROR_UNAUTHORIZED_ACCESS" ) );
                setNextPage( "error.jsp" );
                return;
            }
            SecurityHelper.checkForAdminRole( access );

            // submit data objects for the JSP
            getRequest().setAttribute( "ROLES", access.getRolesByNS( null ) );
            getRequest().setAttribute( "GROUPS", access.getAllGroups() );
            getRequest().setAttribute( "ACCESS", access );
        } catch ( GeneralSecurityException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE", get( "IGEO_STD_SEC_FAIL_INIT_ROLE_EDITOR", e.getMessage() ) );
            setNextPage( "error.jsp" );
            LOG.logError( e.getMessage(), e );
        }
    }
}
