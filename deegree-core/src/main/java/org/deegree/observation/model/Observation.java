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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.protocol.sos.time.TimePeriod;

/**
 * This class is a collection of muliple {@link Measurement}s. The Measurements are grouped by its procedures in
 * {@link MeasurementCollection}s.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class Observation implements Iterable<MeasurementCollection> {

    private final Map<Procedure, MeasurementCollection> measurements = new HashMap<Procedure, MeasurementCollection>();

    private final TimePeriod samplePeriod = new TimePeriod();

    private final List<Property> properties;

    /**
     * Create a Observation with some observed properties.
     * 
     * @param properties
     */
    public Observation( Collection<Property> properties ) {
        this.properties = new ArrayList<Property>( properties );
    }

    /**
     * Add a new measurement to the collection.
     * 
     * @param measurement
     */
    public void add( Measurement measurement ) {
        samplePeriod.extend( measurement.getSamplingTime() );
        add( measurement.getProcedure(), measurement );
    }

    private void add( Procedure procedure, Measurement measurement ) {
        if ( measurements.containsKey( procedure ) ) {
            measurements.get( procedure ).add( measurement );
        } else {
            MeasurementCollection m = new MeasurementCollection( properties );
            m.add( measurement );
            measurements.put( procedure, m );
        }
    }

    @Override
    public String toString() {
        return String.format( "ObservationResult (n: %d) %s", measurements.size(), samplePeriod );
    }

    public Iterator<MeasurementCollection> iterator() {
        return measurements.values().iterator();
    }

    /**
     * @return the number of measurements in this collection
     */
    public int size() {
        return measurements.size();
    }

    /**
     * @return a list of all stored properties.
     */
    public List<Property> getProperties() {
        return new ArrayList<Property>( properties );
    }

    /**
     * @return the sampling time of this collection. The TimePeriod will contain all SamplingTimes of this collection.
     */
    public TimePeriod getSamplingTime() {
        return samplePeriod;
    }

}
