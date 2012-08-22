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

/**
 * This is the base interface for all responses to OGC Web Services (OWS) requests. Each class that
 * capsulates a response within an OWS has to implement this interface.
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp</a>
 * @version 2002-03-01
 */
public class DefaultOGCWebServiceResponse implements OGCWebServiceResponse {

    protected OGCWebServiceRequest request = null;

    protected OGCWebServiceException exception = null;

    /**
     * constructor initializing the class with the <DefaultOGCWebServiceResponse>
     */
    public DefaultOGCWebServiceResponse( OGCWebServiceRequest request ) {
        this.request = request;
    }

    /**
     * constructor initializing the class with the <DefaultOGCWebServiceResponse>
     */
    public DefaultOGCWebServiceResponse( OGCWebServiceRequest request, OGCWebServiceException exception ) {
        this.request = request;
        this.exception = exception;
    }

    /**
     * returns the request that causes the response.
     */
    public OGCWebServiceRequest getRequest() {
        return request;
    }

    /**
     * returns an XML encoding of the exception that raised. If no exception raised <tt>null</tt>
     * will be returned.
     */
    public OGCWebServiceException getException() {
        return exception;
    }

    public String toString() {
        String ret = null;
        ret = getClass().getName() + ":\n";
        ret += "request = " + request + "\n";
        ret += "exception = " + exception + "\n";
        return ret;
    }

}
