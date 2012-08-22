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

package org.deegree.portal.owswatch.configs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A class to hold the information of a service. What are the available versions of this service, which request types
 * are there and their corresponding html pages
 *
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class Service implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -3228645248451301100L;

    private String serviceName = null;

    private Map<String, ServiceVersion> serviceVersions = null;

    /**
     * @param serviceName
     */
    public Service( String serviceName ) {
        this.serviceName = serviceName;
        this.serviceVersions = new HashMap<String, ServiceVersion>();
    }

    /**
     * @return String
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * @param serviceName
     */
    public void setServiceName( String serviceName ) {
        this.serviceName = serviceName;
    }

    /**
     * @return String
     */
    public Map<String, ServiceVersion> getServiceVersions() {
        return serviceVersions;
    }

    /**
     * @param serviceVersions
     */
    public void setServiceVersions( Map<String, ServiceVersion> serviceVersions ) {
        this.serviceVersions = serviceVersions;
    }

}
