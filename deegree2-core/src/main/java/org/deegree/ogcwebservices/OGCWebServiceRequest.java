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
package org.deegree.ogcwebservices;

import java.util.Map;

/**
 * This is the base interface for all request on OGC Web Services (OWS). Each class that capsulates
 * a request against an OWS has to implements this interface.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0, $Revision$, $Date$
 *
 * @since 1.0
 *
 */
public interface OGCWebServiceRequest {

    /**
     * Finally, the requests allow for optional vendor-specific parameters (VSPs) that will enhance
     * the results of a request. Typically, these are used for private testing of non-standard
     * functionality prior to possible standardization. A generic client is not required or expected
     * to make use of these VSPs.
     *
     * @return the vendor specific parameters
     */
    @SuppressWarnings("unchecked")
    Map getVendorSpecificParameters();

    /**
     * Finally, the requests allow for optional vendor-specific parameters (VSPs) that will enhance
     * the results of a request. Typically, these are used for private testing of non-standard
     * functionality prior to possible standardization. A generic client is not required or expected
     * to make use of these VSPs.
     *
     * @param name
     *            the "key" of a vsp
     * @return the value requested by the key
     */
    String getVendorSpecificParameter( String name );

    /**
     * @return the ID of a request
     */
    String getId();

    /**
     * @return the requested service version
     */
    String getVersion();

    /**
     * @return the name of the service that is targeted by the request
     */
    String getServiceName();

    /**
     * @return the URI of a HTTP GET request. If the request doesn't support HTTP GET a
     *         <tt>WebServiceException</tt> will be thrown
     * @throws OGCWebServiceException   
     */
    String getRequestParameter()
                            throws OGCWebServiceException;
}
