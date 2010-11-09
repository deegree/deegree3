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
package org.deegree.observation.persistence.continuous;

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

import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.commons.utils.time.Duration;
import org.deegree.observation.model.MeasurementBase;
import org.deegree.observation.model.Observation;
import org.deegree.observation.model.Offering;
import org.deegree.observation.model.Property;
import org.deegree.observation.model.Result;
import org.deegree.observation.model.SimpleDoubleResult;
import org.deegree.observation.model.SimpleMeasurement;
import org.deegree.observation.persistence.ContinuousFilterConverter;
import org.deegree.observation.persistence.DatastoreConfiguration;
import org.deegree.observation.persistence.FilterException;
import org.deegree.observation.persistence.ObservationDatastore;
import org.deegree.observation.persistence.ObservationDatastoreException;
import org.deegree.observation.persistence.SQLFilterConverter;
import org.deegree.observation.persistence.simple.SimpleObservationDatastore;
import org.deegree.protocol.sos.filter.FilterCollection;
import org.deegree.protocol.sos.time.IndeterminateTime;
import org.deegree.protocol.sos.time.SamplingTime;
import org.deegree.protocol.sos.time.TimePeriod;
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
    public ContinuousObservationDatastore( String jdbcId, String tableName, Map<String, String> columnMap,
                                           Map<String, String> optionMap, List<Property> properties )
                            throws ObservationDatastoreException {
        super( jdbcId, tableName, columnMap, optionMap, properties );
        try {
            begin = DateUtils.parseISO8601Date( optionMap.get( "beginDate" ) );
            Duration duration = DateUtils.parseISO8601Duration( optionMap.get( "interval" ) );
            interval = duration.getDateAfter( begin ).getTime() - begin.getTime();
            String firstID = optionMap.get( "firstID" );
            int id = 1;
            if ( firstID != null ) {
                id = Integer.parseInt( firstID );
            }
            String idField = optionMap.get( "id" );
            if ( idField == null ) {
                throw new ObservationDatastoreException( "the datastore configuration is missing the 'id' column" );
            }
            filterConverter = new ContinuousFilterConverter( columnMap, idField, begin, interval, id );
        } catch ( ParseException e ) {
            throw new ObservationDatastoreException( "error setting the beginDate/interval", e.getCause() );
        }
    }

    @Override
    public Observation getObservation( FilterCollection filter, Offering offering )
                            throws ObservationDatastoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            List<Property> properties = getPropertyMap( filter );

            Observation measurements = new Observation( properties );
            MeasurementBase measurementBase = new MeasurementBase( "", properties ); // TODO

            conn = ConnectionManager.getConnection( jdbcId );
            List<String> columns = new LinkedList<String>();
            for ( Property property : properties ) {
                columns.add( property.getColumnName() );
            }
            String idColumn = columnMap.get( "id" );
            columns.add( idColumn );
            stmt = getStatement( filter, columns, conn, offering );
            resultSet = stmt.executeQuery();

            // Calendar template = Calendar.getInstance( TimeZone.getTimeZone( "GMT" ) );
            List<Result> results = new ArrayList<Result>( properties.size() );
            while ( resultSet.next() ) {
                results.clear();
                for ( Property property : properties ) {
                    double value = resultSet.getDouble( property.getColumnName() );
                    if ( resultSet.wasNull() ) {
                        value = Double.NaN;
                    }
                    results.add( new SimpleDoubleResult( value, property ) );
                }
                int id = resultSet.getInt( idColumn );
                // Date date = resultSet.getTimestamp( "time", template );
                Date date = filterConverter.dateForRowID( id );
                SimpleMeasurement measurement = new SimpleMeasurement( measurementBase, date,
                                                                       offering.getProcedures().get( 0 ), results );

                measurements.add( measurement );
            }
            resultSet.close();
            conn.close();

            return measurements;
        } catch ( SQLException e ) {
            LOG.error( "error while retrieving on observation", e );
            throw new ObservationDatastoreException( "internal error, unable to retrieve observation from datastore", e );
        } catch ( FilterException e ) {
            throw new ObservationDatastoreException( "unable to evaluate filter", e );
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
            conn = ConnectionManager.getConnection( jdbcId );
            stmt = conn.prepareStatement( "select count(*) as n from " + tableName );
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

    protected SQLFilterConverter getFilterConverter() {
        return filterConverter;
    }

}
