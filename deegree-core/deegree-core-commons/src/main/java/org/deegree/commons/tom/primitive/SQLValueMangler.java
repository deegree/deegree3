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
package org.deegree.commons.tom.primitive;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;

import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.commons.tom.datetime.Time;
import org.deegree.commons.utils.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts between internal object values and SQL objects.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SQLValueMangler {

    private static final Logger LOG = LoggerFactory.getLogger( SQLValueMangler.class );

    /**
     * Converts the given {@link PrimitiveValue} value to the corresponding SQL object type.
     * 
     * @param value
     * @return
     */
    public static Object internalToSQL( Object value ) {
        Object sqlValue = null;
        if ( value != null ) {
            BasicType pt = BasicType.determinePrimitiveType( value );
            switch ( pt ) {
            case BOOLEAN:
                sqlValue = value;
                break;
            case DATE:
                sqlValue = new java.sql.Date( ( (Date) value ).getDate().getTime() );
                break;
            case DATE_TIME:
                sqlValue = new Timestamp( ( (DateTime) value ).getValue().getTime() );
                break;
            case DECIMAL:
                sqlValue = ( (BigDecimal) value ).doubleValue();
                break;
            case DOUBLE:
                sqlValue = value;
                break;
            case INTEGER:
                sqlValue = Integer.parseInt( value.toString() );
                break;
            case STRING:
                sqlValue = value;
                break;
            case TIME:
                throw new IllegalArgumentException( "SQL type conversion for '" + pt + "' is not implemented yet." );
            }
        }
        return sqlValue;
    }

    /**
     * Converts the given {@link PrimitiveValue} value to the corresponding SQL object type.
     * 
     * @param value
     * @return
     */
    public static Object internalToSQL( PrimitiveValue pv ) {
        Object sqlValue = null;
        Object value = pv.getValue();
        if ( value != null ) {
            BasicType pt = pv.getType();
            switch ( pt ) {
            case BOOLEAN:
                sqlValue = value;
                break;
            case DATE:
                sqlValue = new java.sql.Date( ( (Date) value ).getDate().getTime() );
                break;
            case DATE_TIME:
                sqlValue = new Timestamp( ( (DateTime) value ).getValue().getTime() );
                break;
            case DECIMAL:
                sqlValue = ( (BigDecimal) value ).doubleValue();
                break;
            case DOUBLE:
                sqlValue = value;
                break;
            case INTEGER:
                sqlValue = Integer.parseInt( value.toString() );
                break;
            case STRING:
                sqlValue = value;
                break;
            case TIME:
                throw new IllegalArgumentException( "SQL type conversion for '" + pt + "' is not implemented yet." );
            }
        }
        return sqlValue;
    }

    public static Object internalToSQL( Object o, int sqlTypeCode ) {
        throw new UnsupportedOperationException( "Not implemented yet" );
    }

    /**
     * @param rs
     * @param columnIndex
     * @param pt
     * @return corresponding primitive value, can be <code>null</code>
     * @throws SQLException
     */
    public static PrimitiveValue sqlToInternal( ResultSet rs, int columnIndex, BasicType pt )
                            throws SQLException {
        Object o = null;
        switch ( pt ) {
        case BOOLEAN:
            o = rs.getBoolean( columnIndex );
            break;
        case DATE:
            java.sql.Date sqlDate = rs.getDate( columnIndex );
            if ( sqlDate != null ) {
                try {
                    o = new Date( DateUtils.formatISO8601DateWOTime( sqlDate ) );
                } catch ( ParseException e ) {
                    throw new SQLException( e.getMessage(), e );
                }
            }
            break;
        case DATE_TIME:
            Timestamp sqlTimeStamp = rs.getTimestamp( columnIndex );
            if ( sqlTimeStamp != null ) {
                try {
                    o = new DateTime( DateUtils.formatISO8601DateWOMS( sqlTimeStamp ) );
                } catch ( ParseException e ) {
                    throw new SQLException( e.getMessage(), e );
                }
            }
            break;
        case DECIMAL:
            o = rs.getObject( columnIndex );
            // needed to avoid rounding problems observed for PostgreSQL double columns...
            if ( o != null && !( o instanceof BigDecimal ) ) {
                o = new BigDecimal( o.toString() );
            }
            break;
        case DOUBLE:
            o = rs.getObject( columnIndex );
            if ( o != null && !( o instanceof Double ) ) {
                o = new Double( o.toString() );
            }
            break;
        case INTEGER:
            if ( rs.getObject( columnIndex ) != null ) {
                o = new BigInteger( rs.getString( columnIndex ) );
            }
            break;
        case STRING:
            o = rs.getString( columnIndex );
            break;
        case TIME:
            java.sql.Time sqlTime = rs.getTime( columnIndex );
            if ( sqlTime != null ) {
                try {
                    o = new Time( DateUtils.formatISO8601Time( sqlTime ) );
                } catch ( ParseException e ) {
                    throw new SQLException( e.getMessage(), e );
                }
            }
            break;
        }
        if ( o != null ) {
            return new PrimitiveValue( o );
        }
        return null;
    }
}