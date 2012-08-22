//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.portal.standard.admin.control;

import java.io.IOException;
import java.nio.charset.Charset;

import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.portal.cataloguemanager.model.ExceptionBean;
import org.deegree.portal.standard.admin.model.ExtJsDataBean;
import org.deegree.portal.standard.admin.model.ExtJsUserGroupBean;
import org.deegree.security.drm.SecurityAccess;
import org.deegree.security.drm.model.Group;
import org.deegree.security.drm.model.User;

/**
 * Returns all user groups read from deegree users and rights management database as a JSON object that will be
 * understood by a extJs ComboBox
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GetGroupsListener extends SecurityListener {

    private static ILogger LOG = LoggerFactory.getLogger( GetGroupsListener.class );

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.enterprise.control.ajax.AbstractListener#actionPerformed(org.deegree.enterprise.control.ajax.WebEvent
     * , org.deegree.enterprise.control.ajax.ResponseHandler)
     */

    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {
        LOG.logDebug( "parameters", event.getParameter() );

        try {
            // initialize security manager
            setUp();
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            responseHandler.writeAndClose( true, new ExceptionBean( getClass().getName(), e.getMessage() ) );
            return;
        }
        // read all groups for user and rights management database
        Group[] groups;
        try {
            User user = manager.getUserByName( "SEC_ADMIN" );
            user.authenticate( secAdminPassword );
            SecurityAccess access = manager.acquireAccess(  user );
            groups = access.getAllGroups();
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            responseHandler.writeAndClose( true, new ExceptionBean( getClass().getName(), e.getMessage() ) );
            return;
        }

        // transform into extJS JSON objects by first creating according java beans
        ExtJsUserGroupBean[] userGroups = new ExtJsUserGroupBean[groups.length];
        for ( int i = 0; i < groups.length; i++ ) {
            userGroups[i] = new ExtJsUserGroupBean();
            userGroups[i].setName( groups[i].getName() );
            userGroups[i].setTitle( groups[i].getTitle() );
        }
        ExtJsDataBean bean = new ExtJsDataBean();
        bean.setOptions( userGroups );

        String charEnc = getRequest().getCharacterEncoding();
        if ( charEnc == null ) {
            charEnc = Charset.defaultCharset().displayName();
        }
        responseHandler.setContentType( "application/json; charset=" + charEnc );
        responseHandler.writeAndClose( false, bean );
    }

}
