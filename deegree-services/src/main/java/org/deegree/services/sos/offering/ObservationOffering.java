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
package org.deegree.services.sos.offering;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.deegree.geometry.Envelope;
import org.deegree.protocol.sos.filter.FilterCollection;
import org.deegree.protocol.sos.time.SamplingTime;
import org.deegree.services.sos.SOServiceExeption;
import org.deegree.services.sos.model.Observation;
import org.deegree.services.sos.model.Procedure;
import org.deegree.services.sos.model.Property;
import org.deegree.services.sos.storage.ObservationDatastore;

/**
 * 
 * This class implements a single observation.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class ObservationOffering {
    private final String id;

    private final String name;

    private final ObservationDatastore datastore;

    private final List<Property> properties;

    private final List<Procedure> procedures;

    /**
     * @param id
     *            a short id
     * @param name
     *            the offering name
     * @param datastore
     *            the source for the observation data
     * @param procedures
     *            the procedure of this offering
     * @param properties
     *            the properties of this offering
     */
    public ObservationOffering( String id, String name, ObservationDatastore datastore,
                                Collection<Procedure> procedures, Collection<Property> properties ) {
        this.id = id;
        this.name = name;
        this.datastore = datastore;
        this.procedures = new LinkedList<Procedure>( procedures );
        this.properties = new LinkedList<Property>( properties );
    }

    /**
     * @return the bbox
     */
    public Envelope getBBOX() {
        Envelope result = null;
        for ( Procedure proc : procedures ) {
            if ( proc.getGeometry() != null ) {
                Envelope env = proc.getGeometry().getEnvelope();
                if ( env != null ) {
                    if ( result != null ) {
                        result = result.merge( env );
                    } else {
                        result = env;
                    }
                }
            }
        }
        return result;
    }

    /**
     * @return the datastore
     */
    private ObservationDatastore getDatastore() {
        return datastore;
    }

    /**
     * @param filter
     * @return the resulting measurements
     * @throws SOServiceExeption
     */
    public Observation getObservation( FilterCollection filter )
                            throws SOServiceExeption {
        return getDatastore().getObservation( filter );
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the procedure
     */
    public List<Procedure> getProcedures() {
        return procedures;
    }

    /**
     * @return the id
     */
    public String getID() {
        return id;
    }

    /**
     * @return all observed properties of this offering
     */
    public List<Property> getProperties() {
        return new LinkedList<Property>( properties );
    }

    /**
     * Retruns the time span (sampling time) of all observations in this offering.
     * 
     * @return the sampling time
     */
    public SamplingTime getSamplingTime() {
        return datastore.getSamplingTime();
    }

}
