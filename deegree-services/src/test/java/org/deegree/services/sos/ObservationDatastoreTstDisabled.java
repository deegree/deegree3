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
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.deegree.commons.utils.time.DateUtils;
import org.deegree.protocol.sos.filter.DurationFilter;
import org.deegree.protocol.sos.filter.EndFilter;
import org.deegree.protocol.sos.filter.FilterCollection;
import org.deegree.protocol.sos.time.TimePeriod;
import org.deegree.services.sos.model.Measurement;
import org.deegree.services.sos.model.MeasurementCollection;
import org.deegree.services.sos.model.Observation;
import org.deegree.services.sos.model.Procedure;
import org.deegree.services.sos.model.Property;
import org.deegree.services.sos.storage.ContinuousObservationDatastore;
import org.deegree.services.sos.storage.DatastoreConfiguration;
import org.deegree.services.sos.storage.ObservationDatastore;
import org.deegree.services.sos.storage.SimpleObservationDatastore;
import org.junit.Test;

/**
 * This class tests, if the continuous and simple observation datastore return the same datasets.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class ObservationDatastoreTstDisabled {

    // call resources/build-demo.xml ant file to get the observations db
    static private final String dbName = System.getProperty( "user.dir" )
                                         + "/build/webapp/WEB-INF/classes/observations";

    /**
     * @throws Exception
     */
    @Test
    public void compareSimpleAndContiuousDS()
                            throws Exception {
        FilterCollection filter = new FilterCollection();
        TimePeriod period = TimePeriod.createTimePeriod( "2008-02-03Z", "2008-02-05Z", "" );
        filter.add( new DurationFilter( period.getBegin(), period.getEnd() ) );
        compareSimpleAndContiuousDS( filter );
    }

    /**
     * @throws Exception
     */
    @Test
    public void compareSimpleAndContiuousDS_Combined()
                            throws Exception {
        FilterCollection filter = new FilterCollection();
        TimePeriod period = TimePeriod.createTimePeriod( "2008-02-03Z", "2008-02-05Z", "" );
        filter.add( new DurationFilter( period.getBegin(), true, period.getEnd(), false ) );
        filter.add( new EndFilter( DateUtils.parseISO8601Date( "2008-01-11T12Z" ) ) );
        compareSimpleAndContiuousDS( filter );
    }

    private static void compareSimpleAndContiuousDS( FilterCollection filter )
                            throws Exception {
        Observation simple = simpleODS( filter );
        Observation cont = continuousODS( filter );

        assertEquals( simple.size(), cont.size() );
        assertTrue( simple.getSamplingTime().equals( cont.getSamplingTime() ) );

        Iterator<MeasurementCollection> a = simple.iterator();
        Iterator<MeasurementCollection> b = cont.iterator();

        while ( a.hasNext() ) {
            Iterator<Measurement> aa = a.next().iterator();
            Iterator<Measurement> bb = b.next().iterator();

            assertEquals( aa.next().getResults().get( 0 ).getResultAsString(),
                          bb.next().getResults().get( 0 ).getResultAsString() );
        }

    }

    private static Observation continuousODS( FilterCollection filter )
                            throws Exception {
        // configuration of the datastore
        DatastoreConfiguration dsConfig = new DatastoreConfiguration( "org.apache.derby.jdbc.EmbeddedDriver",
                                                                      "jdbc:derby:" + dbName, "", "", "observations" );

        dsConfig.addPropertyColumnMapping( new Property( "foo", "foo.bar", "Cel" ), "temp" );
        dsConfig.addProcedure( "default_proc", new Procedure( "default" ) );

        dsConfig.addOption( "beginDate", "2008-01-01" );
        dsConfig.addOption( "interval", "PT1H" );
        dsConfig.addOption( "firstID", "1" );
        dsConfig.addDSColumnMapping( "id", "id" );

        ObservationDatastore ods = new ContinuousObservationDatastore( dsConfig );

        return ods.getObservation( filter );
    }

    private static Observation simpleODS( FilterCollection filter )
                            throws Exception {
        DatastoreConfiguration dsConfig = new DatastoreConfiguration( "org.apache.derby.jdbc.EmbeddedDriver",
                                                                      "jdbc:derby:" + dbName, "", "", "observations" );
        dsConfig.addDSColumnMapping( "timestamp", "time" );
        dsConfig.addPropertyColumnMapping( new Property( "foo", "foo.bar", "Cel" ), "temp" );
        dsConfig.addProcedure( "default_proc", new Procedure( "default" ) );

        ObservationDatastore ods = new SimpleObservationDatastore( dsConfig );

        return ods.getObservation( filter );
    }

}
