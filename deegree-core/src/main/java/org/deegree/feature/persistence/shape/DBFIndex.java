//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.feature.persistence.shape;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.property.Property;
import org.deegree.feature.property.SimpleProperty;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.sql.expression.SQLExpression;
import org.deegree.filter.sql.expression.SQLLiteral;
import org.slf4j.Logger;

/**
 * This class converts the dbf file into a derby database, to enable proper filtering.
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DBFIndex {

    private static final Logger LOG = getLogger( DBFIndex.class );

    private String connid;

    /**
     * @param dbf
     * @param file
     * @param envelopes
     * @throws IOException
     */
    public DBFIndex( DBFReader dbf, File file, Pair<ArrayList<Pair<float[], Long>>, Boolean> envelopes )
                            throws IOException {
        StringBuilder create = new StringBuilder( "create table dbf_index (record_number integer,file_index bigint" );

        ArrayList<String> fields = new ArrayList<String>();

        for ( PropertyType pt : dbf.getFields() ) {
            if ( pt instanceof SimplePropertyType ) {
                create.append( ", " );
                SimplePropertyType spt = (SimplePropertyType) pt;
                String sqlType = null;
                switch ( spt.getPrimitiveType() ) {
                case BOOLEAN:
                    sqlType = "boolean";
                    break;
                case DATE:
                case DATE_TIME:
                case TIME:
                    sqlType = "timestamp";
                    break;
                case DECIMAL:
                case DOUBLE:
                    sqlType = "double";
                    break;
                case INTEGER:
                    sqlType = "integer";
                    break;
                case STRING:
                    sqlType = "varchar";
                    break;
                }
                String field = pt.getName().getLocalPart().toLowerCase();
                fields.add( field );
                create.append( field + " " + sqlType );
            }
        }
        create.append( ")" );
        file = file.getAbsoluteFile();
        File dbfile = new File( file.toString().substring( 0, file.toString().lastIndexOf( '.' ) ) );

        ConnectionManager.addConnection( connid = file.getName(), "jdbc:h2:" + dbfile, "SA", "", 0, 5 );

        if ( new File( dbfile.toString() + ".h2.db" ).exists() ) {
            // TODO proper check for database consistency
            return;
        }

        LOG.debug( "Creating h2 db index..." );

        Connection conn = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        StringBuilder qms = new StringBuilder();
        try {
            conn = ConnectionManager.getConnection( file.getName() );
            stmt = conn.prepareStatement( create.toString() );
            stmt.executeUpdate();
            stmt.close();

            for ( String field : fields ) {
                stmt = conn.prepareStatement( "create index " + field + "_index on dbf_index (" + field + ")" );
                stmt.executeUpdate();
                stmt.close();
            }

            conn.setAutoCommit( false );
            Iterator<Pair<float[], Long>> iter = envelopes.first.iterator();
            for ( int i = 0; i < dbf.size(); ++i ) {
                sb.setLength( 0 );
                qms.setLength( 0 );
                qms.append( "?,?" );
                HashMap<SimplePropertyType, Property> entry = dbf.getEntry( i );
                sb.append( "insert into dbf_index (record_number,file_index" );
                for ( SimplePropertyType spt : entry.keySet() ) {
                    sb.append( "," );
                    qms.append( ",?" );
                    sb.append( spt.getName().getLocalPart().toLowerCase() );
                }
                sb.append( ") values (" ).append( qms ).append( ")" );
                stmt = conn.prepareStatement( sb.toString() );

                int idx = 2;
                stmt.setInt( 1, i );
                Pair<float[], Long> p = iter.next();
                stmt.setLong( 2, p.second );
                for ( SimplePropertyType spt : entry.keySet() ) {
                    PrimitiveValue primVal = ( (SimpleProperty) entry.get( spt ) ).getValue();
                    if ( primVal.getValue() == null ) {
                        switch ( spt.getPrimitiveType() ) {
                        case BOOLEAN:
                            stmt.setNull( ++idx, Types.BOOLEAN );
                            break;
                        case DATE:
                        case DATE_TIME:
                        case TIME:
                            stmt.setNull( ++idx, Types.DATE );
                            break;
                        case DECIMAL:
                        case DOUBLE:
                            stmt.setNull( ++idx, Types.DECIMAL );
                            break;
                        case INTEGER:
                            stmt.setNull( ++idx, Types.INTEGER );
                            break;
                        case STRING:
                            stmt.setNull( ++idx, Types.VARCHAR );
                            break;
                        }
                        continue;
                    }
                    switch ( spt.getPrimitiveType() ) {
                    case BOOLEAN:
                        stmt.setBoolean( ++idx, (Boolean) primVal.getValue() );
                        break;
                    case DATE:
                    case DATE_TIME:
                    case TIME:
                        // unknown whether this works, test data!
                        stmt.setLong( ++idx, ( (java.util.Date) primVal.getValue() ).getTime() );
                        break;
                    case DECIMAL:
                    case DOUBLE:
                        stmt.setBigDecimal( ++idx, (BigDecimal) primVal.getValue() );
                        break;
                    case INTEGER:
                        stmt.setInt( ++idx, ( (BigInteger) primVal.getValue() ).intValue() );
                        break;
                    case STRING:
                        stmt.setString( ++idx, (String) primVal.getValue() );
                        break;
                    }
                }

                stmt.executeUpdate();
                stmt.close();
            }

            conn.commit();

        } catch ( SQLException e ) {
            e.printStackTrace();
        } finally {
            if ( stmt != null ) {
                try {
                    stmt.close();
                } catch ( SQLException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if ( conn != null ) {
                try {
                    conn.close();
                } catch ( SQLException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        LOG.debug( "Done creating h2 db index." );

    }

    /**
     * @param available
     *            is modified in place to contain only matches!
     * @param filter
     * @param sort
     * @return null, if there was an error, else a pair of left overs (with possibly null values if everything could be
     *         mapped)
     * @throws FilterEvaluationException
     */
    public Pair<Filter, SortProperty[]> query( List<Pair<Integer, Long>> available, Filter filter, SortProperty[] sort )
                            throws FilterEvaluationException {

        if ( filter == null && ( sort == null || sort.length == 0 ) ) {
            return new Pair<Filter, SortProperty[]>();
        }

        // TODO handle filter == null
        H2WhereBuilder where = new H2WhereBuilder( (OperatorFilter) filter, sort );
        SQLExpression generated = where.getWhere();
        if ( generated == null ) {
            return null;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet set = null;
        try {
            conn = ConnectionManager.getConnection( connid );
            String clause = generated.getSQL().toString();
            stmt = conn.prepareStatement( "select record_number,file_index from dbf_index where " + clause );

            int i = 1;
            for ( SQLLiteral lit : generated.getLiterals() ) {
                Object o = lit.getValue();
                if ( o instanceof PrimitiveValue ) {
                    o = ( (PrimitiveValue) o ).getValue();
                }
                stmt.setObject( i++, o );

            }

            set = stmt.executeQuery();

            while ( set.next() ) {
                available.add( new Pair<Integer, Long>( set.getInt( "record_number" ), set.getLong( "file_index" ) ) );
            }

            return new Pair<Filter, SortProperty[]>( where.getPostFilter(), where.getPostSortCriteria() );
        } catch ( SQLException e ) {
            e.printStackTrace();
        } finally {
            if ( set != null ) {
                try {
                    set.close();
                } catch ( SQLException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if ( stmt != null ) {
                try {
                    stmt.close();
                } catch ( SQLException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if ( conn != null ) {
                try {
                    conn.close();
                } catch ( SQLException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        return null;

    }

}
