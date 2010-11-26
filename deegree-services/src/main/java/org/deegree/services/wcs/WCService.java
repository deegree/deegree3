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
package org.deegree.services.wcs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.services.wcs.coverages.WCSCoverage;

/**
 * This is the WCService, a collection of Coverages.
 * <p>
 * This class represents the main entry point for access to coverages. You can create a WCService with the static
 * createService methods that takes a service configuration (see resources/schema/wcs/wcs_configuration.xsd and
 * resources/schema/example/conf/wcs/wcs_configuration.xml). An WCService contains one or more Coverages, which contain
 * the actual raster data.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class WCService {

    private Map<String, WCSCoverage> coverages = new HashMap<String, WCSCoverage>();

    /**
     * Add another coverage to the service.
     * 
     * @param coverage
     */
    void addCoverage( WCSCoverage coverage ) {
        this.coverages.put( coverage.getName(), coverage );
    }

    /**
     * @return all coverages of this service
     */
    public List<WCSCoverage> getAllCoverages() {
        return new ArrayList<WCSCoverage>( this.coverages.values() );
    }

    /**
     * @param coverage
     * @return true if the service contains the named coverage
     */
    public boolean hasCoverage( String coverage ) {
        return coverages.containsKey( coverage );
    }

    /**
     * @param coverage
     * @return the requested coverage or <code>null</code>, if it does not exist
     */
    public WCSCoverage getCoverage( String coverage ) {
        return coverages.get( coverage );
    }

}
