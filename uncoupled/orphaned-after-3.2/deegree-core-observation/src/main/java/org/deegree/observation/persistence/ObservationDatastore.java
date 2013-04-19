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
package org.deegree.observation.persistence;

import java.util.List;

import org.deegree.commons.config.Resource;
import org.deegree.observation.filter.FilterCollection;
import org.deegree.observation.model.Observation;
import org.deegree.observation.model.Offering;
import org.deegree.observation.model.Property;
import org.deegree.observation.time.SamplingTime;

/**
 * This is the interface for a storage of observations.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public interface ObservationDatastore extends Resource {

    /**
     * Get measurements from the datastore.
     * 
     * @param filter
     * @return the resulting measurements
     * @throws ObservationDatastoreException
     */
    public Observation getObservation( FilterCollection filter, Offering offering )
                            throws ObservationDatastoreException;

    /**
     * Get the time span (sampling time) of all observations in this datastore.
     * 
     * @return the sampling time of the datastore
     */
    public SamplingTime getSamplingTime();

    public List<Property> getProperties();
}
