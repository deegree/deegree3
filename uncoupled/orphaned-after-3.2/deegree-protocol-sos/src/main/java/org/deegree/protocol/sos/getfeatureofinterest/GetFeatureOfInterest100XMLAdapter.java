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
package org.deegree.protocol.sos.getfeatureofinterest;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.XPath;
import org.deegree.observation.filter.SpatialFilter;
import org.deegree.observation.filter.TimeFilter;
import org.deegree.protocol.sos.SOSRequest100XMLAdapter;

/**
 * The <code>GetFeatureOfInterest100XMLAdapter</code> class TODO
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class GetFeatureOfInterest100XMLAdapter extends SOSRequest100XMLAdapter {

    /**
     * @param rootElement
     */
    public GetFeatureOfInterest100XMLAdapter( OMElement rootElement ) {
        this.setRootElement( rootElement );
    }

    /**
     * @return the {@link GetFeatureOfInterest} object that was parsed from the request
     */
    public GetFeatureOfInterest parse() {
        String[] foi = getFeatureOfInterest();

        SpatialFilter location = null;
        // TODO spatilFilter
        // if ( foi == null ) {
        // location = (SpatialFilter) getRequiredNode( rootElement,
        // new XPath( "/sos:GetFeatureOfInterest/sos:location",
        // nsContext ) );
        // }

        TimeFilter eventTime = getEventTime();

        return new GetFeatureOfInterest( foi, location, eventTime );
    }

    private TimeFilter getEventTime() {
        return (TimeFilter) getNode( rootElement, new XPath( "/sos:GetFeatureOfInterest/sos:eventTime", nsContext ) );
    }

    private String[] getFeatureOfInterest() {
        return getNodesAsStrings( rootElement, new XPath( "/sos:GetFeatureOfInterest/sos:FeatureOfInterestId",
                                                          nsContext ) );
    }
}
