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
package org.deegree.observation.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This {@link MeasurementBase} is a container to store data that is common to all {@link SimpleMeasurement}s.
 * 
 * <p>
 * A {@link MeasurementBase} object is immutable.
 * 
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class MeasurementBase {

    private final String featureOfInterest;

    private final List<Property> properties;

    /**
     * @param foi
     * @param properties
     */
    public MeasurementBase( String foi, Collection<Property> properties ) {
        this.featureOfInterest = foi;
        this.properties = new ArrayList<Property>( properties );
    }

    /**
     * @TODO return Feature
     * @return the feature of interest
     */
    public String getFeatureOfInterest() {
        return featureOfInterest;
    }

    /**
     * @TODO measurement for more than one property
     * @return the observedProperty
     */
    public List<Property> getProperties() {
        return properties;
    }
}
