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
package org.deegree.commons.tom.primitive;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Types;

import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.feature.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Primitive type system for object properties (e.g. for {@link Feature} instances).
 * <p>
 * Based on XML schema types, but stripped down to leave out any distinctions that are not strictly necessary in the
 * feature model.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public enum PrimitiveType {

    /** Property value is of class <code>String</code>. */
    STRING( String.class ),
    /** Property value is of class <code>Boolean</code>. */
    BOOLEAN( Boolean.class ),
    /** Property value is of class <code>BigDecimal</code>. */
    DECIMAL( BigDecimal.class ),
    /**
     * Property value is of class <code>Double</code> (needed because BigDecimal cannot express "NaN", "-INF" and
     * "INF"), which are required by <code>xs:double</code> / <code>xs:float</code>.
     */
    DOUBLE( Double.class ),
    /** Property value is of class <code>BigInteger</code>. */
    INTEGER( BigInteger.class ),
    /** Property value is of class {@link Date}. */
    DATE( Date.class ),
    /** Property value is of class {@link DateTime}. */
    DATE_TIME( DateTime.class ),
    /** Property value is of class {@link Time}. */
    TIME( Time.class );

    private static final Logger LOG = LoggerFactory.getLogger( PrimitiveType.class );

    private Class<?> valueClass;

    private PrimitiveType( Class<?> valueClass ) {
        this.valueClass = valueClass;
    }

    /**
     * Returns the class that primitive values of this type must have.
     * 
     * @return the corresponding class for values
     */
    public Class<?> getValueClass() {
        return valueClass;
    }

    /**
     * Returns the {@link PrimitiveType} for the given value.
     * 
     * @param value
     * @return corresponding {@link PrimitiveType}, never <code>null</code>
     * @throws IllegalArgumentException
     */
    public static PrimitiveType determinePrimitiveType( Object value )
                            throws IllegalArgumentException {
        Class<?> oClass = value.getClass();
        for ( PrimitiveType pt : values() ) {
            if ( pt.getValueClass() == oClass ) {
                return pt;
            }
        }
        String msg = "Cannot determine PrimitiveType for object class: " + value.getClass();
        throw new IllegalArgumentException( msg );
    }

    /**
     * Returns the {@link PrimitiveType} for the given SQL type (from {@link Types}).
     * 
     * @see Types
     * 
     * @param sqlType
     * @return corresponding {@link PrimitiveType}, never <code>null</code>
     * @throws IllegalArgumentException
     *             if the SQL type can not be mapped to a {@link PrimitiveType}
     */
    public static PrimitiveType determinePrimitiveType( int sqlType ) {

        PrimitiveType pt = null;

        switch ( sqlType ) {
        case Types.BIGINT:
        case Types.INTEGER:
        case Types.SMALLINT:
        case Types.TINYINT: {
            pt = INTEGER;
            break;
        }
        case Types.DECIMAL:
        case Types.DOUBLE:
        case Types.FLOAT:
        case Types.NUMERIC:
        case Types.REAL: {
            pt = DECIMAL;
            break;
        }
        case Types.CHAR:
        case Types.VARCHAR: {
            pt = STRING;
            break;
        }
        case Types.DATE: {
            pt = DATE;
            break;
        }
        case Types.TIMESTAMP: {
            pt = DATE_TIME;
            break;
        }
        case Types.TIME: {
            pt = TIME;
            break;
        }
        case Types.ARRAY:
        case Types.BINARY:
        case Types.BIT:
        case Types.BLOB:
        case Types.BOOLEAN:
        case Types.CLOB:
        case Types.DATALINK:
        case Types.DISTINCT:
        case Types.JAVA_OBJECT:
        case Types.LONGNVARCHAR:
        case Types.LONGVARBINARY:
        case Types.LONGVARCHAR:
        case Types.NCHAR:
        case Types.NCLOB:
        case Types.NULL:
        case Types.NVARCHAR:
        case Types.OTHER:
        case Types.REF:
        case Types.ROWID:
        case Types.SQLXML:
        case Types.STRUCT:
        case Types.VARBINARY: {
            String msg = "Unmappable SQL type encountered: " + sqlType;
            LOG.warn( msg );
            throw new IllegalArgumentException( msg );
        }
        default: {
            String msg = "Internal error: unknown SQL type encountered: " + sqlType;
            LOG.error( msg );
            throw new IllegalArgumentException( msg );
        }
        }
        return pt;
    }
}
