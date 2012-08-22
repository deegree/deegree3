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
import java.util.List;
import java.util.Map;

/**
 * Holds the Serviceconfigurations section of the owsWatchConfigurations xml
 *
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ServiceDescription implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8396049315537488430L;

    private List<Integer> testIntervals = null;

    private Map<String, Service> services = null;

    /**
     * @param testIntervals
     * @param services
     */
    public ServiceDescription( List<Integer> testIntervals, Map<String, Service> services ) {
        this.testIntervals = testIntervals;
        this.services = services;
    }

    /**
     * @return Services
     */
    public Map<String, Service> getServices() {
        return services;
    }

    /**
     * @param services
     */
    public void setServices( Map<String, Service> services ) {
        this.services = services;
    }

    /**
     * @return TestIntervals
     */
    public List<Integer> getTestIntervals() {
        return testIntervals;
    }

    /**
     * @param testIntervals
     */
    public void setTestIntervals( List<Integer> testIntervals ) {
        this.testIntervals = testIntervals;
    }
}
