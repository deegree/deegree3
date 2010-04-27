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
package org.deegree.services.sos.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.commons.utils.time.Duration;
import org.deegree.protocol.sos.filter.FilterCollection;
import org.deegree.protocol.sos.time.IndeterminateTime;
import org.deegree.protocol.sos.time.SamplingTime;
import org.deegree.protocol.sos.time.TimePeriod;
import org.deegree.services.sos.SOSConfigurationException;
import org.deegree.services.sos.SOServiceExeption;
import org.deegree.services.sos.model.MeasurementBase;
import org.deegree.services.sos.model.Observation;
import org.deegree.services.sos.model.Property;
import org.deegree.services.sos.model.Result;
import org.deegree.services.sos.model.SimpleDoubleResult;
import org.deegree.services.sos.model.SimpleMeasurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ObservationDatastore} for measurements that are stored in a fixed interval. At the moment it only supports
 * single procedures.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class ContinuousObservationDatastore extends SimpleObservationDatastore {

    private static final Logger LOG = LoggerFactory.getLogger( ContinuousObservationDatastore.class );

    private final ContinuousFilterConverter filterConverter;

    private long interval;

    private Date begin;

    /**
     * @param dsConfig
     *            the datastore configuration
     * @throws SOSConfigurationException
     *             if the {@link DatastoreConfiguration} contains no beginDate or interval.
     */
    public ContinuousObservationDatastore( DatastoreConfiguration dsConfig ) throws SOSConfigurationException {
        super( dsConfig );
        try {
            begin = DateUtils.parseISO8601Date( dsConfig.getOption( "beginDate" ) );
            Duration duration = DateUtils.parseISO8601Duration( dsConfig.getOption( "interval" ) );
            interval = duration.getDateAfter( begin ).getTime() - begin.getTime();
            String firstID = dsConfig.getOption( "firstID" );
            int id = 1;
            if ( firstID != null ) {
                id = Integer.parseInt( firstID );
            }
            String columnName = dsConfig.getDSColumnName( "id" );
            if ( columnName == null ) {
                throw new SOSConfigurationException( "the datastore configuration is missing the 'id' column" );
            }
            filterConverter = new ContinuousFilterConverter( dsConfig, columnName, begin, interval, id );
        } catch ( ParseException e ) {
            throw new SOSConfigurationException( "error setting the beginDate/interval", e.getCause() );
        }
    }

    @Override
    public Observation getObservation( FilterCollection filter )
                            throws SOServiceExeption {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            Map<Property, String> properties = getPropertyMap( filter );

            Observation measurements = new Observation( properties.keySet() );
            MeasurementBase measurementBase = new MeasurementBase( "", properties.keySet() ); // TODO

            conn = getConnection();
            List<String> columns = new LinkedList<String>( properties.values() );
            String idColumn = getDSConfig().getDSColumnName( "id" );
            columns.add( idColumn );
            stmt = getStatement( filter, columns, conn );
            resultSet = stmt.executeQuery();

            // Calendar template = Calendar.getInstance( TimeZone.getTimeZone( "GMT" ) );
            List<Result> results = new ArrayList<Result>( properties.size() );
            while ( resultSet.next() ) {
                results.clear();
                for ( Entry<Property, String> property : properties.entrySet() ) {
                    double value = resultSet.getDouble( property.getValue() );
                    if ( resultSet.wasNull() ) {
                        value = Double.NaN;
                    }
                    results.add( new SimpleDoubleResult( value, property.getKey() ) );
                }
                int id = resultSet.getInt( idColumn );
                // Date date = resultSet.getTimestamp( "time", template );
                Date date = filterConverter.dateForRowID( id );
                SimpleMeasurement measurement = new SimpleMeasurement( measurementBase, date,
                                                                       getDSConfig().getProcedure( null ), results );

                measurements.add( measurement );
            }
            resultSet.close();
            conn.close();

            return measurements;
        } catch ( SQLException e ) {
            LOG.error( "error while retrieving on observation", e );
            throw new SOServiceExeption( "internal error, unable to retrieve observation from datastore", e );
        } catch ( FilterException e ) {
            throw new SOServiceExeption( "unable to evaluate filter", e );
        } finally {
            JDBCUtils.close( resultSet );
            JDBCUtils.close( stmt );
            JDBCUtils.close( conn );
        }
    }

    @Override
    public SamplingTime getSamplingTime() {
        SamplingTime result = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement( "select count(*) as n from " + getDSConfig().getTableName() );
            resultSet = stmt.executeQuery();
            if ( resultSet.next() ) {
                int count = resultSet.getInt( "n" );
                Duration dur = new Duration( 0, 0, 0, 0, 0, (int) ( ( count - 1 ) * interval / 1000.0 ) );
                Date end = dur.getDateAfter( begin );
                result = new TimePeriod( begin, end );
            }
        } catch ( SQLException e ) {
            LOG.error( "error while retrieving sampling time", e );
            result = IndeterminateTime.unknown();
        } finally {
            JDBCUtils.close( resultSet );
            JDBCUtils.close( stmt );
            JDBCUtils.close( conn );
        }
        return result;
    }

    @Override
    protected SQLFilterConverter getFilterConverter() {
        return filterConverter;
    }

}
