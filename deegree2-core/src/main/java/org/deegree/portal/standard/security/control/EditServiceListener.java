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

import static org.deegree.framework.log.LoggerFactory.getLogger;
import static org.deegree.framework.util.StringTools.stackTraceToString;
import static org.deegree.i18n.Messages.get;
import static org.deegree.portal.standard.security.control.ClientHelper.acquireAccess;
import static org.deegree.portal.standard.security.control.SecurityHelper.checkForAdminRole;

import javax.servlet.ServletRequest;

import org.deegree.enterprise.control.AbstractListener;
import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.drm.SecurityAccess;
import org.deegree.security.drm.model.Service;

/**
 * <code>EditServiceListener</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class EditServiceListener extends AbstractListener {

    private static final ILogger LOG = getLogger( EditServiceListener.class );

    @Override
    public void actionPerformed( FormEvent event ) {
        RPCParameter[] params = ( (RPCWebEvent) event ).getRPCMethodCall().getParameters();

        ServletRequest request = getRequest();

        SecurityAccess access = null;

        try {
            // perform access check
            access = acquireAccess( this );
            checkForAdminRole( access );

            String address = params[0].getValue().toString();

            Service service = access.getServiceByAddress( address );
            request.setAttribute( "SERVICE", service );

        } catch ( GeneralSecurityException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE", get( "IGEO_STD_SEC_ERROR_EDIT_SERVICE", e.getMessage() ) );
            setNextPage( "error.jsp" );
            LOG.logError( e.getMessage(), e );
        } catch ( Exception e ) {
            LOG.logError( get( "IGEO_STD_SEC_ERROR_UNKNOWN", stackTraceToString( e ) ) );
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE", get( "IGEO_STD_SEC_ERROR_EDIT_SERVICE", e.getMessage() ) );
            setNextPage( "error.jsp" );
        }
    }

}
