// $HeadURL:
// /cvsroot/deegree/src/org/deegree/ogcwebservices/wms/protocol/WMPSProtocolFactory.java,v
// 1.7 2004/07/12 06:12:11 ap Exp $
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
package org.deegree.ogcwebservices.wmps.operation;

import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.wmps.capabilities.WMPSCapabilities;

/**
 * Factory that builds the different types of WMPS-Requests & Responses.
 *
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh </a>
 *
 * @version 2.0
 */
public class WMPSProtocolFactory {

    /**
     * creates an instance of a <tt>WMPSGetCapabilitiesResult</tt> object
     *
     * @param request
     *            request that lead to the response
     * @param exception
     *            exception if one occuered
     * @param capabilities
     *            WMS capabilities
     *
     * @return <tt>WMPSGetCapabilitiesResult</tt>
     */
    public static WMPSGetCapabilitiesResult createGetCapabilitiesResult( OGCWebServiceRequest request,
                                                                         OGCWebServiceException exception,
                                                                         WMPSCapabilities capabilities ) {

        WMPSGetCapabilitiesResult res = null;
        if ( exception == null ) {
            res = new WMPSGetCapabilitiesResult( request, capabilities );
        } else {
            res = new WMPSGetCapabilitiesResult( request, exception );
        }

        return res;
    }

}
