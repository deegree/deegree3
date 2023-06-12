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
package org.deegree.services.sos;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.Point;
import org.deegree.observation.model.Procedure;
import org.deegree.observation.persistence.ObservationStoreManager;
import org.deegree.services.jaxb.sos.ServiceConfiguration;
import org.deegree.services.jaxb.sos.ServiceConfiguration.Offering;
import org.deegree.services.jaxb.sos.ServiceConfiguration.Offering.Procedure.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * 
 * 
 */
public class SOSBuilder {

    private static final Logger LOG = LoggerFactory.getLogger( SOSBuilder.class );

    private final ServiceConfigurationXMLAdapter adapter;

    /**
     * @param config
     */
    public SOSBuilder( ServiceConfigurationXMLAdapter config ) {
        this.adapter = config;
    }

    /**
     * Create a SOS configuration from a XMLStreamReader.
     * 
     * @param config
     * @return a new SOSConfiguration
     */
    public static SOService createService( DeegreeWorkspace workspace, URL configUrl ) {
        ServiceConfigurationXMLAdapter ad = new ServiceConfigurationXMLAdapter();
        ad.setSystemId( configUrl.toExternalForm() );
        SOSBuilder builder = new SOSBuilder( ad );
        return builder.buildService( workspace, configUrl );
    }

    private SOService buildService( DeegreeWorkspace workspace, URL configUrl ) {
        SOService result = new SOService();
        ServiceConfiguration serviceConf = ServiceConfigurationXMLAdapter.parse( workspace, configUrl );

        ObservationStoreManager storeMgr = workspace.getSubsystemManager( ObservationStoreManager.class );

        for ( Offering conf : serviceConf.getOffering() ) {
            result.addOffering( createOffering( conf, storeMgr ) );
        }
        return result;
    }

    /**
     * @param conf
     * @param storeMgr
     * @return
     */
    private org.deegree.observation.model.Offering createOffering( Offering conf, ObservationStoreManager storeMgr ) {
        String offeringName = conf.getName();
        String observationStoreId = conf.getObservationStoreId();
        String srsName = conf.getSrsName();
        List<org.deegree.services.jaxb.sos.ServiceConfiguration.Offering.Procedure> listProc = conf.getProcedure();
        List<Procedure> procedures = new ArrayList<Procedure>();
        for ( org.deegree.services.jaxb.sos.ServiceConfiguration.Offering.Procedure proc : listProc ) {
            try {
                String procHref = proc.getHref();

                Location locationType = proc.getLocation();
                GeometryFactory geometryFactory = new GeometryFactory();
                Point location = geometryFactory.createPoint( null, Double.parseDouble( locationType.getLon() ),
                                                              Double.parseDouble( locationType.getLat() ),
                                                              CRSManager.getCRSRef( srsName ) );

                String foiHref = proc.getFeatureOfInterest().getHref();
                String sensorId = proc.getSensor().getId();
                String sensorName = proc.getSensor().getName();
                String sensorURL = proc.getSensor().getHref();
                procedures.add( new Procedure( procHref, location, foiHref, sensorId, sensorName,
                                               adapter.resolve( sensorURL ) ) );
            } catch ( MalformedURLException e ) {
                LOG.error( "Couldn't parse sensor location for {}. Ignoring this procedure!", proc.getHref() );
                continue;
            }
        }

        org.deegree.observation.model.Offering offering = new org.deegree.observation.model.Offering(
                                                                                                      offeringName,
                                                                                                      observationStoreId,
                                                                                                      srsName,
                                                                                                      procedures,
                                                                                                      storeMgr );
        return offering;
    }
}
