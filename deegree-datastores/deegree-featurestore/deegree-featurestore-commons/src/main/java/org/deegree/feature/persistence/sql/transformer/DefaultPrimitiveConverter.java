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
package org.deegree.feature.persistence.sql.transformer;

import static org.deegree.commons.tom.primitive.BaseType.DATE;
import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.text.ParseException;

import javax.xml.namespace.QName;

import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.commons.tom.datetime.Time;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.tom.sql.ParticleConverter;
import org.deegree.commons.utils.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementations convert between {@link PrimitiveValue} particles and SQL column values.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DefaultPrimitiveConverter implements ParticleConverter<PrimitiveValue> {

    private static Logger LOG = LoggerFactory.getLogger( DefaultPrimitiveConverter.class );

    private final PrimitiveType pt;

    private BaseType bt;

    private final String column;

    private static final QName GML32_TIME_UNION = new QName( GML3_2_NS, "TimePositionUnion" );

    public DefaultPrimitiveConverter( PrimitiveType pt, String column ) {
        this.pt = pt;
        this.bt = pt.getBaseType();
        this.column = column;
        XSSimpleTypeDefinition xsTypeDef = pt.getXSType();
        if ( xsTypeDef != null && !( xsTypeDef.getAnonymous() ) ) {
            QName typeName = new QName( xsTypeDef.getNamespace(), xsTypeDef.getName() );
            if ( GML32_TIME_UNION.equals( typeName ) ) {
                LOG.info( "Detected " + GML32_TIME_UNION + " simple type. Treating as date." );
                bt = DATE;
            }
        }
    }

    @Override
    public String getSelectSnippet( String tableAlias ) {
        if ( tableAlias == null ) {
            return tableAlias + "." + column;
        }
        return null;
    }

    public String getSetSnippet() {
        return "?";
    }

    @Override
    public PrimitiveValue toParticle( Object sqlValue ) {
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
        Date value = null;
        if ( sqlValue instanceof java.util.Date ) {
            try {
                value = new Date( DateUtils.formatISO8601DateWOTime( (java.util.Date) sqlValue ) );
            } catch ( ParseException e ) {
                throw new IllegalArgumentException( e.getMessage(), e );
            }
        } else {
            throw new IllegalArgumentException( "Unable to convert sql result value of type '" + sqlValue.getClass()
                                                + "' to Date object." );
        }
        return new PrimitiveValue( value, pt );
    }

    protected PrimitiveValue toDateTimeParticle( Object sqlValue ) {
        DateTime value = null;
        if ( sqlValue instanceof java.util.Date ) {
            try {
                value = new DateTime( DateUtils.formatISO8601DateWOMS( (java.util.Date) sqlValue ) );
            } catch ( ParseException e ) {
                throw new IllegalArgumentException( "Unable to convert sql result value of type '"
                                                    + sqlValue.getClass() + "' to DateTime object." );
            }
        } else {
            throw new IllegalArgumentException( "Unable to convert sql result value of type '" + sqlValue.getClass()
                                                + "' to DateTime object." );
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

    protected PrimitiveValue toTimeParticle( Object sqlValue ) {
        Time value = null;
        if ( sqlValue instanceof Time ) {
            value = (Time) sqlValue;
        } else if ( sqlValue instanceof Date ) {
            try {
                value = new Time( DateUtils.formatISO8601Time( (java.util.Date) sqlValue ) );
            } catch ( ParseException e ) {
                throw new IllegalArgumentException( e.getMessage(), e );
            }
        } else {
            throw new IllegalArgumentException( "Unable to convert sql result value of type '" + sqlValue.getClass()
                                                + "' to Time object." );
        }
        return new PrimitiveValue( value, pt );
    }

    @Override
    public Object toSQLArgument( PrimitiveValue particle, Connection conn ) {
        // TODO Auto-generated method stub
        return null;
    }
}