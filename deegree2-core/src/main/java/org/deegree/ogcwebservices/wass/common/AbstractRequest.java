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
package org.deegree.ogcwebservices.wass.common;

import java.util.Map;

import org.deegree.ogcwebservices.AbstractOGCWebServiceRequest;

/**
 * Base class for the GDI NRW access control specification's requests.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public abstract class AbstractRequest extends AbstractOGCWebServiceRequest {

    private static final long serialVersionUID = -629986931810096636L;

    private String service = null;

    private String request = null;

    /**
     * Constructs new base request from the given map.
     * @param id the request id
     * @param kvp
     *            the map
     */
    public AbstractRequest( String id, Map<String, String> kvp ) {
        super( kvp.get( "VERSION" ), id, null );
        service = kvp.get( "SERVICE" );
        request = kvp.get( "REQUEST" );
    }

    /**
     * Constructs new base request from the given values.
     *
     * @param id the request id
     * @param version
     *            request version
     * @param service
     *            request service
     * @param request
     *            request name
     */
    public AbstractRequest(  String id, String version, String service, String request ) {
        super( version, id, null );
        this.service = service;
        this.request = request;
    }

    /**
     * @return the service of this request
     */
    public String getServiceName() {
        return service;
    }

    /**
     * @return the request's name
     */
    public String getRequest() {
        return request;
    }

}
