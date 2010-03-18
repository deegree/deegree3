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
import java.text.ParseException;

import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.commons.tom.datetime.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts between internal object values and XML strings.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class XMLValueMangler {

    private static final Logger LOG = LoggerFactory.getLogger( XMLValueMangler.class );

    /**
     * Returns the internal representation for the given XML string and {@link PrimitiveType}.
     * 
     * @param s
     * @param pt
     * @return
     * @throws IllegalArgumentException
     */
    public static Object xmlToInternal( String s, PrimitiveType pt )
                            throws IllegalArgumentException {
        Object value = s;
        switch ( pt ) {
        case BOOLEAN: {
            if ( s.equals( "true" ) || s.equals( "1" ) ) {
                value = Boolean.TRUE;
            } else if ( s.equals( "false" ) || s.equals( "0" ) ) {
                value = Boolean.FALSE;
            } else {
                String msg = "Value ('" + s + "') is not valid with respect to the xs:boolean type."
                             + "Valid values are 'true', 'false', '1' and '0'.";
                throw new IllegalArgumentException( msg );
            }
            break;
        }
        case DATE: {
            try {
                value = new Date( s );
            } catch ( ParseException e ) {
                String msg = "Value ('" + s + "') is not valid with respect to the xs:date type.";
                throw new IllegalArgumentException( msg );
            }
            break;
        }
        case DATE_TIME: {
            try {
                value = new DateTime( s );
            } catch ( ParseException e ) {
                String msg = "Value ('" + s + "') is not valid with respect to the xs:dateTime type.";
                throw new IllegalArgumentException( msg );
            }
            break;
        }
        case DECIMAL: {
            value = new BigDecimal( s );
            break;
        }
        case DOUBLE: {
            value = new Double( s );
            break;
        }
        case INTEGER: {
            value = new BigInteger( s );
            break;
        }
        case STRING: {
            break;
        }
        case TIME: {
            try {
                value = new Time( s );
            } catch ( ParseException e ) {
                String msg = "Value ('" + s + "') is not valid with respect to the xs:time type.";
                throw new IllegalArgumentException( msg );
            }
            break;
        }
        default: {
            LOG.warn( "Unhandled primitive type " + pt + " -- treating as string value." );
        }
        }
        return value;
    }

    public static String internalToXML( Object o ) {
        String xml = null;
        if ( o != null ) {
            PrimitiveType pt = PrimitiveType.determinePrimitiveType( o );
            switch ( pt ) {
            case BOOLEAN:
            case DATE:
            case DATE_TIME:
            case DECIMAL:
            case DOUBLE:
            case INTEGER:
            case STRING:
            case TIME:
                // TODO is this always sufficient?
                xml = o.toString();
                break;
            }
        }
        return xml;
    }

    /**
     * Returns the best matching {@link PrimitiveType} for the given XSD simple type definition.
     * 
     * @param xsdTypeDef
     * @return best matching {@link PrimitiveType}, never <code>null</code>
     */
    public static PrimitiveType getPrimitiveType( XSSimpleTypeDefinition xsdTypeDef ) {

        switch ( xsdTypeDef.getBuiltInKind() ) {

        // date and time types
        case XSConstants.DATE_DT: {
            return PrimitiveType.DATE;
        }
        case XSConstants.DATETIME_DT: {
            return PrimitiveType.DATE_TIME;
        }
        case XSConstants.TIME_DT: {
            return PrimitiveType.TIME;
        }

            // numeric types
            // -1.23, 0, 123.4, 1000.00
        case XSConstants.DECIMAL_DT:
            // -INF, -1E4, -0, 0, 12.78E-2, 12, INF, NaN (equivalent to double-precision 64-bit floating point)
        case XSConstants.DOUBLE_DT:
            // -INF, -1E4, -0, 0, 12.78E-2, 12, INF, NaN (single-precision 32-bit floating point)
        case XSConstants.FLOAT_DT: {
            return PrimitiveType.DECIMAL;
        }

            // integer types

            // ...-1, 0, 1, ...
        case XSConstants.INTEGER_DT:
            // 1, 2, ...
        case XSConstants.POSITIVEINTEGER_DT:
            // ... -2, -1
        case XSConstants.NEGATIVEINTEGER_DT:
            // 0, 1, 2, ...
        case XSConstants.NONNEGATIVEINTEGER_DT:
            // ... -2, -1, 0
        case XSConstants.NONPOSITIVEINTEGER_DT:
            // -9223372036854775808, ... -1, 0, 1, ... 9223372036854775807
        case XSConstants.LONG_DT:
            // 0, 1, ... 18446744073709551615
        case XSConstants.UNSIGNEDLONG_DT:
            // -2147483648, ... -1, 0, 1, ... 2147483647
        case XSConstants.INT_DT:
            // 0, 1, ...4294967295
        case XSConstants.UNSIGNEDINT_DT:
            // -32768, ... -1, 0, 1, ... 32767
        case XSConstants.SHORT_DT:
            // 0, 1, ... 65535
        case XSConstants.UNSIGNEDSHORT_DT:
            // -128, ...-1, 0, 1, ... 127
        case XSConstants.BYTE_DT:
            // 0, 1, ... 255
        case XSConstants.UNSIGNEDBYTE_DT: {
            return PrimitiveType.INTEGER;
        }

            // other types
        case XSConstants.ANYSIMPLETYPE_DT:
        case XSConstants.ANYURI_DT:
        case XSConstants.BASE64BINARY_DT:
        case XSConstants.BOOLEAN_DT:
        case XSConstants.DURATION_DT:
        case XSConstants.ENTITY_DT:
        case XSConstants.GDAY_DT:
        case XSConstants.GMONTH_DT:
        case XSConstants.GMONTHDAY_DT:
        case XSConstants.GYEAR_DT:
        case XSConstants.GYEARMONTH_DT:
        case XSConstants.HEXBINARY_DT:
        case XSConstants.ID_DT:
        case XSConstants.IDREF_DT:
        case XSConstants.LANGUAGE_DT:
        case XSConstants.LIST_DT:
        case XSConstants.LISTOFUNION_DT:
        case XSConstants.NAME_DT:
        case XSConstants.NCNAME_DT:
        case XSConstants.NORMALIZEDSTRING_DT:
        case XSConstants.NOTATION_DT:
        case XSConstants.QNAME_DT:
        case XSConstants.STRING_DT:
        case XSConstants.TOKEN_DT:
        case XSConstants.UNAVAILABLE_DT: {
            return PrimitiveType.STRING;
        }
        }
        throw new IllegalArgumentException( "Unexpected simple type: " + xsdTypeDef.getBuiltInKind() );
    }
}
