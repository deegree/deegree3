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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Map.Entry;

import org.deegree.commons.utils.ArrayUtils;
import org.deegree.commons.utils.JDBCUtils;
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
import org.deegree.services.sos.SOServiceExeption;
import org.deegree.services.sos.model.MeasurementBase;
import org.deegree.services.sos.model.Observation;
import org.deegree.services.sos.model.Procedure;
import org.deegree.services.sos.model.Property;
import org.deegree.services.sos.model.Result;
import org.deegree.services.sos.model.SimpleDoubleResult;
import org.deegree.services.sos.model.SimpleMeasurement;
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
     */
    public SimpleObservationDatastore( DatastoreConfiguration conf ) {
        super( conf );
        this.timezone = initTimezone();
        this.filterConverter = new GenericFilterConverter( conf, this.timezone );

        this.timeColumn = getDSConfig().getDSColumnName( "timestamp" );
        this.procColumn = getDSConfig().getDSColumnName( "procedure" );

        String driver = conf.getJdbcDriver();
        try {
            Class.forName( driver );
        } catch ( java.lang.ClassNotFoundException e ) {
            // TODO
            e.printStackTrace();
        }
    }

    private TimeZone initTimezone() {
        TimeZone result;
        String tz = getDSConfig().getOption( "db_timezone" );
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

    public Observation getObservation( FilterCollection filter )
                            throws SOServiceExeption {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            Map<Property, String> properties = getPropertyMap( filter );

            Observation measurements = new Observation( properties.keySet() );

            MeasurementBase measurementBase = new MeasurementBase( "", properties.keySet() ); // TODO

            Calendar template = Calendar.getInstance( this.timezone );

            conn = getConnection();
            List<String> columns = new LinkedList<String>( properties.values() );

            if ( procColumn != null ) {
                columns.add( procColumn );
            }
            columns.add( timeColumn );

            stmt = getStatement( filter, columns, conn );
            resultSet = stmt.executeQuery();

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
                Date date = resultSet.getTimestamp( timeColumn, template );

                Procedure p = getProcedure( resultSet );

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
            throw new SOServiceExeption( "internal error, unable to retrieve observation from datastore", e );
        } catch ( FilterException e ) {
            throw new SOServiceExeption( "unable to evaluate filter", e );
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
    protected Procedure getProcedure( ResultSet resultSet )
                            throws SQLException {
        Procedure p = null;
        if ( procColumn != null ) {
            String s = resultSet.getString( procColumn );
            if ( s != null ) {
                p = getDSConfig().getProcedure( s.trim() );
            }
        } else {
            // no procedures configured, return first
            p = getDSConfig().getProcedure( null );
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
            String timestampCol = getDSConfig().getDSColumnName( "timestamp" );
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
     * @return the sql select statement with where clauses
     * @throws FilterException
     * @throws SQLException
     */
    protected PreparedStatement getStatement( FilterCollection filter, Collection<String> collection, Connection conn )
                            throws FilterException, SQLException {

        QueryBuilder q = new QueryBuilder();

        String columns = ArrayUtils.join( ", ", collection );
        if ( columns.length() == 0 ) {
            columns = "*";
        }
        q.add( "SELECT " + columns + " FROM " + getDSConfig().getTableName() );

        List<TimeFilter> timeFilter = filter.getTimeFilter();
        List<ProcedureFilter> procFilter = getProcedureFilter( filter );
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
            getFilterConverter().buildProcedureClause( q, procFilter );
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
    private List<ProcedureFilter> getProcedureFilter( FilterCollection filter ) {
        if ( filter.getSpatialFilter().isEmpty() ) {
            return filter.getProcedureFilter();
        }
        List<ProcedureFilter> procFilters = filter.getProcedureFilter();
        List<Procedure> procedures;
        if ( !procFilters.isEmpty() ) {
            procedures = new LinkedList<Procedure>();
            for ( ProcedureFilter procFilter : procFilters ) {
                String procID = getDSConfig().getProcedureIDFromName( procFilter.getProcedureName() );
                if ( procID != null ) {
                    procedures.add( getDSConfig().getProcedure( procID ) );
                }
            }
        } else {
            procedures = getDSConfig().getProcedures();
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
                    if ( proc.getGeometry() != null ) {
                        if ( bboxFilter.getBBOX().intersects( proc.getGeometry() ) ) {
                            result.add( new ProcedureFilter( proc.getName() ) );
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
     * @throws SOServiceExeption
     */
    protected Map<Property, String> getPropertyMap( FilterCollection filter )
                            throws SOServiceExeption {
        if ( filter.getPropertyFilter().size() == 0 ) {
            return getDSConfig().getPropertyColumnMap();
        }
        Set<String> filteredProperties = new HashSet<String>();
        for ( PropertyFilter propFilter : filter.getPropertyFilter() ) {
            filteredProperties.add( propFilter.getPropertyName() );
        }

        Map<Property, String> properties = new HashMap<Property, String>();
        for ( Entry<Property, String> property : getDSConfig().getPropertyColumnMap().entrySet() ) {
            Property key = property.getKey();
            if ( filteredProperties.contains( key.getName() ) ) {
                properties.put( key, property.getValue() );
                filteredProperties.remove( key.getName() );
            }
        }
        if ( filteredProperties.size() != 0 ) {
            String msg = "the offering does not contain the observedProperty: "
                         + ArrayUtils.join( ", ", filteredProperties );
            throw new SOServiceExeption( msg );

        }
        return properties;
    }

}
