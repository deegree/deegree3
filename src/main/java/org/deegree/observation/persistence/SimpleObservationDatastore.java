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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.deegree.commons.utils.ArrayUtils;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.observation.model.MeasurementBase;
import org.deegree.observation.model.Observation;
import org.deegree.observation.model.Offering;
import org.deegree.observation.model.Procedure;
import org.deegree.observation.model.Property;
import org.deegree.observation.model.Result;
import org.deegree.observation.model.SimpleDoubleResult;
import org.deegree.observation.model.SimpleMeasurement;
import org.deegree.protocol.sos.filter.FilterCollection;
import org.deegree.protocol.sos.filter.ProcedureFilter;
import org.deegree.protocol.sos.filter.PropertyFilter;
import org.deegree.protocol.sos.filter.ResultFilter;
import org.deegree.protocol.sos.filter.SpatialBBOXFilter;
import org.deegree.protocol.sos.filter.SpatialFilter;
import org.deegree.protocol.sos.filter.TimeFilter;
import org.deegree.protocol.sos.time.IndeterminateTime;
import org.deegree.protocol.sos.time.SamplingTime;
import org.deegree.protocol.sos.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple {@link ObservationDatastore} that stores all measurements with time.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class SimpleObservationDatastore extends SQLObservationDatastore {

    private static final Logger LOG = LoggerFactory.getLogger( SimpleObservationDatastore.class );

    private final SQLFilterConverter filterConverter;

    private final TimeZone timezone;

    /**
     * the name of the database column that contains the procedure id
     */
    protected final String procColumn;

    /**
     * the name of the database column that contains the timestamp
     */
    protected final String timeColumn;

    /**
     * @param conf
     * @throws SOServiceException
     */
    public SimpleObservationDatastore( DatastoreConfiguration conf ) {
        super( conf );
        this.timezone = initTimezone();
        this.filterConverter = new GenericFilterConverter( conf, this.timezone );

        this.timeColumn = getDSConfig().getColumnName( "timestamp" );
        this.procColumn = getDSConfig().getColumnName( "procedureId" );
    }

    private TimeZone initTimezone() {
        TimeZone result;
        String tz = getDSConfig().getOptionValue( "db_timezone" );
        if ( tz == null ) {
            result = TimeZone.getDefault();
        } else {
            result = TimeZone.getTimeZone( tz );
            // returns GMT if unknown, warn if GMT is not set explicit
            if ( !"GMT".equalsIgnoreCase( tz ) && result.hasSameRules( TimeZone.getTimeZone( "GMT" ) ) ) {
                LOG.warn( "unknown timezone {} using GMT", tz );
            }
        }
        return result;
    }

    public Observation getObservation( FilterCollection filter, Offering offering )
                            throws ObservationDatastoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            List<Property> properties = getPropertyMap( filter );

            Observation measurements = new Observation( properties );

            MeasurementBase measurementBase = new MeasurementBase( "", properties ); // TODO

            Calendar template = Calendar.getInstance( this.timezone );

            conn = getConnection();
            List<String> columns = new LinkedList<String>();
            for ( Property property : properties ) {
                columns.add( property.getColumnName() );
            }

            if ( procColumn != null ) {
                columns.add( procColumn );
            }
            columns.add( timeColumn );

            stmt = getStatement( filter, columns, conn, offering );
            resultSet = stmt.executeQuery();

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
                Date date = resultSet.getTimestamp( timeColumn, template );

                Procedure p = getProcedure( resultSet, offering );

                if ( p != null ) {
                    SimpleMeasurement measurement = new SimpleMeasurement( measurementBase, date, p, results );
                    measurements.add( measurement );
                } else {
                    LOG.error( "no procedure found for result set {}, {}", date, results );
                }
            }

            return measurements;
        } catch ( SQLException e ) {
            LOG.error( "error while retrieving an observation", e );
            throw new ObservationDatastoreException( "internal error, unable to retrieve observation from datastore", e );
        } catch ( FilterException e ) {
            throw new ObservationDatastoreException( "unable to evaluate filter", e );
        } finally {
            JDBCUtils.close( resultSet );
            JDBCUtils.close( stmt );
            JDBCUtils.close( conn );
        }
    }

    /**
     * @param resultSet
     * @return the procedure of this result
     * @throws SQLException
     */
    protected Procedure getProcedure( ResultSet resultSet, Offering offering )
                            throws SQLException {
        Procedure p = null;
        if ( procColumn != null ) {
            String s = resultSet.getString( procColumn );
            if ( s != null ) {
                p = offering.getProcedureBySensorId( s.trim() );
            }
        } else {
            // no procedures configured, return first
            p = offering.getProcedures().get( 0 );
        }
        return p;
    }

    public SamplingTime getSamplingTime() {
        SamplingTime result = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            conn = getConnection();
            conn.setAutoCommit( true );
            String timestampCol;
            timestampCol = getDSConfig().getColumnName( "timestamp" );
            String tableName = getDSConfig().getTableName();

            stmt = conn.prepareStatement( String.format( "SELECT min(%s) as start_date, max(%s) as end_date FROM %s",
                                                         timestampCol, timestampCol, tableName ) );

            resultSet = stmt.executeQuery();

            Calendar template = Calendar.getInstance( this.timezone );
            if ( resultSet.next() ) {
                Date start = new Date( resultSet.getTimestamp( "start_date", template ).getTime() );
                Date end = new Date( resultSet.getTimestamp( "end_date", template ).getTime() );
                result = new TimePeriod( start, end );
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

    /**
     * @return the sql filter converter
     */
    protected SQLFilterConverter getFilterConverter() {
        return filterConverter;
    }

    /**
     * Create the sql statement.
     * 
     * @param filter
     * @param collection
     * @param conn
     * @param offering
     * @return the sql select statement with where clauses
     * @throws FilterException
     * @throws SQLException
     */
    protected PreparedStatement getStatement( FilterCollection filter, Collection<String> collection, Connection conn,
                                              Offering offering )
                            throws FilterException, SQLException {

        QueryBuilder q = new QueryBuilder();

        String columns = ArrayUtils.join( ", ", collection );
        if ( columns.length() == 0 ) {
            columns = "*";
        }
        q.add( "SELECT " + columns + " FROM " + getDSConfig().getTableName() );

        List<TimeFilter> timeFilter = filter.getTimeFilter();
        List<ProcedureFilter> procFilter = getProcedureFilter( filter, offering );
        List<ResultFilter> resultFilter = filter.getResultFilter();

        if ( timeFilter.size() > 0 || procFilter.size() > 0 || resultFilter.size() > 0 ) {
            q.add( "WHERE" );
        }
        boolean needSep = false;
        if ( timeFilter.size() > 0 ) {
            getFilterConverter().buildTimeClause( q, timeFilter );
            needSep = true;
        }
        if ( procFilter.size() > 0 ) {
            if ( needSep ) {
                q.add( "AND" );
            }
            getFilterConverter().buildProcedureClause( q, procFilter, offering );
            needSep = true;
        }
        if ( resultFilter.size() > 0 ) {
            if ( needSep ) {
                q.add( "AND" );
            }
            getFilterConverter().buildResultClause( q, resultFilter );
        }

        LOG.debug( "query: {}", q );

        return q.buildStatement( conn );
    }

    // this datastore doesn't support spatial DBs, each procedure has a fixed position.
    // this method creates a procedure filters for the given procedure filters and the given spatial filters.
    private List<ProcedureFilter> getProcedureFilter( FilterCollection filter, Offering offering ) {
        if ( filter.getSpatialFilter().isEmpty() ) {
            return filter.getProcedureFilter();
        }
        List<ProcedureFilter> procFilters = filter.getProcedureFilter();
        List<Procedure> procedures;
        if ( !procFilters.isEmpty() ) {
            procedures = new LinkedList<Procedure>();
            for ( ProcedureFilter procFilter : procFilters ) {
                procedures.add( offering.getProcedureByHref( procFilter.getProcedureName() ) );
            }
        } else {
            procedures = offering.getProcedures();
        }
        return getProcedureFilterForSpatialFilter( filter.getSpatialFilter(), procedures );
    }

    private List<ProcedureFilter> getProcedureFilterForSpatialFilter( List<SpatialFilter> spatialFilters,
                                                                      List<Procedure> procedures ) {
        List<ProcedureFilter> result = new LinkedList<ProcedureFilter>();
        if ( !spatialFilters.isEmpty() ) {
            SpatialFilter spatialFilter = spatialFilters.get( 0 );
            if ( spatialFilter instanceof SpatialBBOXFilter ) {
                SpatialBBOXFilter bboxFilter = (SpatialBBOXFilter) spatialFilter;
                for ( Procedure proc : procedures ) {
                    if ( proc.getLocation() != null ) {
                        if ( bboxFilter.getBBOX().intersects( proc.getLocation() ) ) {
                            result.add( new ProcedureFilter( proc.getProcedureHref() ) );
                        }
                    }
                }
                if ( result.isEmpty() ) {
                    result.add( new ProcedureFilter(
                                                     "spatial filter didn't match anything, so we don't expect any result" ) );
                }
            }
        }
        return result;
    }

    /**
     * Create a map with all requested properties.
     * 
     * @param filter
     * @return a map with properties and the corresponding column names
     * @throws SOServiceException
     */
    protected List<Property> getPropertyMap( FilterCollection filter )
                            throws ObservationDatastoreException {
        if ( filter.getPropertyFilter().size() == 0 ) {
            return getDSConfig().getProperties();
        }
        Set<String> filteredProperties = new HashSet<String>();
        for ( PropertyFilter propFilter : filter.getPropertyFilter() ) {
            filteredProperties.add( propFilter.getPropertyName() );
        }

        List<Property> properties = new ArrayList<Property>();
        for ( Property property : getDSConfig().getProperties() ) {
            if ( filteredProperties.contains( property.getHref() ) ) {
                properties.add( property );
                filteredProperties.remove( property.getHref() );
            }
        }
        if ( filteredProperties.size() != 0 ) {
            String msg = "the offering does not contain the observedProperty: "
                         + ArrayUtils.join( ", ", filteredProperties );
            throw new ObservationDatastoreException( msg );

        }
        return properties;
    }

}
