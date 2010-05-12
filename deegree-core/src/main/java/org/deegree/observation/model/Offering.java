//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.geometry.Envelope;
import org.deegree.observation.persistence.ObservationDatastore;
import org.deegree.observation.persistence.ObservationDatastoreException;
import org.deegree.observation.persistence.ObservationStoreManager;
import org.deegree.protocol.sos.filter.FilterCollection;

/**
 * The <code>Offering</code> class encapsulates the information from an offering (as it is present in the sos
 * configuration).
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class Offering {

    private final String offeringName;

    private final String observationStoreId;

    private final String srsName;

    private final List<Procedure> procedures;

    /**
     * map between procedure sensor id and the procedure bean
     */
    private Map<String, Procedure> procedureMap = new HashMap<String, Procedure>();

    /**
     * map between procedure href and the procedure bean
     */
    private Map<String, Procedure> procedureHrefMap = new HashMap<String, Procedure>();

    /**
     * map between procedure href and procedure sensor ids
     */
    private Map<String, String> procedureNameToId = new HashMap<String, String>();

    /**
     * 
     * @param offeringName
     * @param observationStoreId
     * @param srsName
     * @param procedures
     * @param propertiesHref
     */
    public Offering( String offeringName, String observationStoreId, String srsName, List<Procedure> procedures ) {
        this.offeringName = offeringName;
        this.observationStoreId = observationStoreId;
        this.srsName = srsName;
        this.procedures = procedures;

        for ( Procedure proc : procedures ) {
            procedureMap.put( proc.getSensorId(), proc );
            procedureHrefMap.put( proc.getProcedureHref(), proc );
            procedureNameToId.put( proc.getProcedureHref(), proc.getSensorId() );
        }
    }

    /**
     * @return the bbox
     */
    public Envelope getBBOX() {
        Envelope result = null;
        for ( Procedure proc : procedures ) {
            if ( proc.getLocation() != null ) {
                Envelope env = proc.getLocation().getEnvelope();
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

    public Observation getObservation( FilterCollection filter )
                            throws ObservationDatastoreException {
        return getDatastore().getObservation( filter, this );
    }

    public ObservationDatastore getDatastore()
                            throws ObservationDatastoreException {
        return ObservationStoreManager.getDatastoreById( observationStoreId );
    }

    public Procedure getProcedureBySensorId( String sensorId ) {
        return procedureMap.get( sensorId );
    }

    public Procedure getProcedureByHref( String href ) {
        return procedureHrefMap.get( href );
    }

    public String getProcedureIdFromHref( String href ) {
        return procedureNameToId.get( href );
    }

    public String getObservationStoreId() {
        return observationStoreId;
    }

    /**
     * @return
     */
    public String getOfferingName() {
        return offeringName;
    }

    /**
     * @return
     */
    public List<Procedure> getProcedures() {
        return procedures;
    }

}
