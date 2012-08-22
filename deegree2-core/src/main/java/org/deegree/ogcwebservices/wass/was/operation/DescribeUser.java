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
package org.deegree.ogcwebservices.wass.was.operation;

import java.util.Map;

import org.deegree.ogcwebservices.AbstractOGCWebServiceRequest;

/**
 * <code>DescribeUser</code> is the request class for the deegree specific DescribeUser
 * operation. The DescribeUser operation returns user data such as email address when given
 * a session ID.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public class DescribeUser extends AbstractOGCWebServiceRequest {

    private static final long serialVersionUID = 6876661820417769484L;

    private String sessionID;

    /**
     * Creates a new <code>DescribeUser</code> object from the given
     * values.
     *
     * @param id the request id
     * @param values the request parameters
     */
    public DescribeUser( String id, Map<String, String> values ) {
        super( values.get( "VERSION" ), id, values );
        sessionID = values.get( "SESSIONID" );
    }

    /**
     * @return Returns the session id.
     */
    public String getSessionID() {
        return sessionID;
    }

    public String getServiceName() {
        return "WAS";
    }

}

