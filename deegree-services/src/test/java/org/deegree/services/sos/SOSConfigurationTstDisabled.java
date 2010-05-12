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
package org.deegree.services.sos;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.deegree.observation.model.Observation;
import org.deegree.observation.model.Offering;
import org.deegree.observation.persistence.ObservationDatastoreException;
import org.deegree.protocol.sos.filter.DurationFilter;
import org.deegree.protocol.sos.filter.FilterCollection;
import org.deegree.protocol.sos.time.TimePeriod;
import org.junit.Test;

/**
 * 
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class SOSConfigurationTstDisabled {

    /**
     * Test method for {@link org.deegree.services.sos.SOService#getOffering(String)}.
     * 
     * @throws ObservationDatastoreException
     * @throws SOSConfigurationException
     */
    @Test
    public void testGetOfferings()
                            throws ObservationDatastoreException, SOSConfigurationException {
        URL confURL = SOSConfigurationTstDisabled.class.getResource( "sos_configuration.xml" );
        SOService conf = SOSBuilder.createService( confURL );

        assertEquals( 6, conf.getAllOfferings().size() );
        Offering offering = conf.getOffering( "urn:MyOrg:offering:1" );
        assertEquals( "urn:ogc:object:Sensor:latlon:foobarnator", offering.getProcedures().get( 0 ).getProcedureHref() );

        TimePeriod period = TimePeriod.createTimePeriod( "2008-03-01", "", "P7D" );
        DurationFilter filter = new DurationFilter( period.getBegin(), period.getEnd() );
        Observation result = offering.getObservation( new FilterCollection( filter ) );

        // for ( ObservationOffering offering : offerings ) {
        // System.out.print( offering.getName() + ": " );
        // System.out.println( offering.getProcedure() );
        //
        // TimePeriod period = DateUtil.createTimePeriod( "2008-03-01", "", "P7D" );
        //
        // DurationFilter filter = new DurationFilter( period.getBegin(), period.getEnd() );
        // MeasurementCollection result = offering.getDatastore().getMeasurements( filter );
        //
        //
        // try {
        // XMLStreamWriter writer = getXMLStreamWriter();
        // ObservationXMLAdapter_1_0.export( result, writer );
        // writer.close();
        // } catch ( XMLStreamException e ) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        //
        // }

        // try {
        // XMLStreamWriter writer = getXMLStreamWriter();
        // CapabilitiesXMLAdapter_1_0_0.export( offerings, writer );
        // writer.close();
        // } catch ( XMLStreamException e ) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

    }

}
