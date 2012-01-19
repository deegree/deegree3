//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.commons.tom.sql;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.commons.tom.datetime.Time;
import org.deegree.commons.tom.datetime.TimeInstant;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;

/**
 * {@link PrimitiveParticleConverter} for canonical conversion between SQL types and {@link PrimitiveValue}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DefaultPrimitiveConverter implements PrimitiveParticleConverter {

    protected final PrimitiveType pt;

    protected BaseType bt;

    protected final String column;

    private final boolean isConcatenated;

    public DefaultPrimitiveConverter( PrimitiveType pt, String column ) {
        this.pt = pt;
        this.bt = pt.getBaseType();
        this.column = column;
        this.isConcatenated = false;
    }

    public DefaultPrimitiveConverter( PrimitiveType pt, String column, boolean isConcatenated ) {
        this.pt = pt;
        this.bt = pt.getBaseType();
        this.column = column;
        this.isConcatenated = isConcatenated;
    }

    @Override
    public String getSelectSnippet( String tableAlias ) {
        if ( tableAlias != null ) {
            return tableAlias + "." + column;
        }
        return column;
    }

    @Override
    public String getSetSnippet( PrimitiveValue particle ) {
        return "?";
    }

    @Override
    public PrimitiveValue toParticle( ResultSet rs, int colIndex )
                            throws SQLException {
        Object sqlValue = rs.getObject( colIndex );
        if ( sqlValue == null ) {
            return null;
        }
        switch ( bt ) {
        case BOOLEAN:
            return toBooleanParticle( sqlValue );
        case DATE:
            return toDateParticle( sqlValue );
        case DATE_TIME:
            return toDateTimeParticle( sqlValue );
        case DECIMAL:
            return toDecimalParticle( sqlValue );
        case DOUBLE:
            return toDoubleParticle( sqlValue );
        case INTEGER:
            return toIntegerParticle( sqlValue );
        case STRING:
            return toStringParticle( sqlValue );
        case TIME:
            return toTimeParticle( sqlValue );
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public PrimitiveType getType() {
        return pt;
    }

    protected PrimitiveValue toBooleanParticle( Object sqlValue ) {
        Boolean value = null;
        if ( sqlValue instanceof Boolean ) {
            value = (Boolean) sqlValue;
        } else {
            String s = "" + sqlValue;
            if ( "1".equals( s ) || "true".equalsIgnoreCase( s ) ) {
                value = Boolean.TRUE;
            } else if ( "0".equals( s ) || "false".equalsIgnoreCase( s ) ) {
                value = Boolean.FALSE;
            } else {
                throw new IllegalArgumentException( "Unable to convert sql result value of type '"
                                                    + sqlValue.getClass() + "' to Boolean object." );
            }
        }
        return new PrimitiveValue( value, pt );
    }

    protected PrimitiveValue toDateParticle( Object sqlValue ) {
        TimeInstant value = null;
        if ( sqlValue instanceof java.util.Date ) {
            Calendar cal = Calendar.getInstance();
            cal.setTime( (java.util.Date) sqlValue );
            value = new Date( cal, true );
        } else if ( sqlValue != null ) {
            throw new IllegalArgumentException( "Unable to convert sql result value of type '" + sqlValue.getClass()
                                                + "' to Date object." );
        }
        return new PrimitiveValue( value, pt );
    }

    protected PrimitiveValue toDateTimeParticle( Object sqlValue ) {
        TimeInstant value = null;
        if ( sqlValue instanceof java.util.Date ) {
            Calendar cal = Calendar.getInstance();
            cal.setTime( (java.util.Date) sqlValue );
            value = new DateTime( cal, true );
        } else if ( sqlValue != null ) {
            throw new IllegalArgumentException( "Unable to convert sql result value of type '" + sqlValue.getClass()
                                                + "' to DateTime object." );
        }
        return new PrimitiveValue( value, pt );
    }

    protected PrimitiveValue toTimeParticle( Object sqlValue ) {
        TimeInstant value = null;
        if ( sqlValue instanceof java.util.Date ) {
            Calendar cal = Calendar.getInstance();
            cal.setTime( (java.util.Date) sqlValue );
            value = new Time( cal, true );
        } else if ( sqlValue != null ) {
            throw new IllegalArgumentException( "Unable to convert sql result value of type '" + sqlValue.getClass()
                                                + "' to Time object." );
        }
        return new PrimitiveValue( value, pt );
    }

    protected PrimitiveValue toDecimalParticle( Object sqlValue )
                            throws NumberFormatException {
        BigDecimal value = null;
        if ( sqlValue instanceof BigDecimal ) {
            value = (BigDecimal) sqlValue;
        } else {
            value = new BigDecimal( sqlValue.toString() );
        }
        return new PrimitiveValue( value, pt );
    }

    protected PrimitiveValue toDoubleParticle( Object sqlValue )
                            throws NumberFormatException {
        Double value = null;
        if ( sqlValue instanceof Double ) {
            value = (Double) sqlValue;
        } else {
            value = new Double( sqlValue.toString() );
        }
        return new PrimitiveValue( value, pt );
    }

    protected PrimitiveValue toIntegerParticle( Object sqlValue )
                            throws NumberFormatException {
        BigInteger value = null;
        if ( sqlValue instanceof BigInteger ) {
            value = (BigInteger) sqlValue;
        } else {
            value = new BigInteger( sqlValue.toString() );
        }
        return new PrimitiveValue( value, pt );
    }

    protected PrimitiveValue toStringParticle( Object sqlValue ) {
        return new PrimitiveValue( "" + sqlValue, pt );
    }

    @Override
    public void setParticle( PreparedStatement stmt, PrimitiveValue particle, int colIndex )
                            throws SQLException {
        // TODO rework this
        Object sqlValue = SQLValueMangler.internalToSQL( particle );
        stmt.setObject( colIndex, sqlValue );
    }

    @Override
    public boolean isConcatenated() {
        return isConcatenated;
    }
}
