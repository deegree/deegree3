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

package org.deegree.ogcwebservices.csw.capabilities;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.xlink.SimpleLink;
import org.deegree.i18n.Messages;

/**
 * The <code>EBRIMCapabilities</code> class encapsulates the extra capabilities of ebrim.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */

public class EBRIMCapabilities {

    private Map<URI, CSWFeature> serviceFeatures;

    private Map<URI, List<String>> serviceProperties;

    private SimpleLink wsdl_services;

    /**
     * @param serviceFeatures
     * @param serviceProperties
     * @param wsdl_services
     * @throws IllegalArgumentException if any of the given parameters are null.
     */
    public EBRIMCapabilities( Map<URI, CSWFeature> serviceFeatures, Map<URI, List<String>> serviceProperties,
                              SimpleLink wsdl_services ) {
        if( serviceFeatures == null ){
            throw new IllegalArgumentException( Messages.getMessage( "CSW_EBRIM_NULL_NOT_ALLOWED", "ServiceFeatures" )  );
        }
        this.serviceFeatures = serviceFeatures;
        if( serviceProperties == null ){
            throw new IllegalArgumentException( Messages.getMessage( "CSW_EBRIM_NULL_NOT_ALLOWED", "ServiceProperties" )  );
        }
        this.serviceProperties = serviceProperties;
        if( wsdl_services == null ){
            throw new IllegalArgumentException( Messages.getMessage( "CSW_EBRIM_NULL_NOT_ALLOWED", "WSDL_Services" )  );
        }
        this.wsdl_services = wsdl_services;
    }

    /**
     * @return the serviceFeatures.
     */
    public Map<URI, CSWFeature> getServiceFeatures() {
        return serviceFeatures;
    }

    /**
     * @return the serviceProperties.
     */
    public Map<URI, List<String>> getServiceProperties() {
        return serviceProperties;
    }

    /**
     * @return the wsdl_services.
     */
    public SimpleLink getWsdl_services() {
        return wsdl_services;
    }

}
