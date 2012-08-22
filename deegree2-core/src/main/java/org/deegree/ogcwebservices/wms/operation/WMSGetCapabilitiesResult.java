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
package org.deegree.ogcwebservices.wms.operation;

import org.deegree.ogcwebservices.DefaultOGCWebServiceResponse;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.wms.configuration.WMSConfigurationType;


/**
*
*
* <p>--------------------------------------------------------</p>
*
* @author Katharina Lupp <a href="mailto:k.lupp@web.de">Katharina Lupp</a>
* @version $Revision$ $Date$
*/
public class WMSGetCapabilitiesResult extends DefaultOGCWebServiceResponse {

    private WMSConfigurationType capabilities = null;

    /**
     * constructor initializing the class with the <WMSFilterServiceResponse>
     */
     WMSGetCapabilitiesResult( OGCWebServiceRequest request,
                               WMSConfigurationType capabilities ) {
         super( request );
         setCapabilities( capabilities );
     }

    /**
    * constructor initializing the class with the <WMSFilterServiceResponse>
    */
    WMSGetCapabilitiesResult( OGCWebServiceRequest request,
                              OGCWebServiceException exception ) {
        super( request, exception );
        setCapabilities( capabilities );
    }

    /**
     * returns the capabilities as result of an GetCapabilities request. If an
     * excption raised processing the request or the request has been invalid
     * <tt>null</tt> will be returned.
     * @return the capabilities object or null
     */
    public WMSConfigurationType getCapabilities() {
        return capabilities;
    }

    /**
     * sets the capabilities as result of an GetCapabilities request.
     * @param capabilities
    */
    public void setCapabilities( WMSConfigurationType capabilities ) {
        this.capabilities = capabilities;
    }
}
