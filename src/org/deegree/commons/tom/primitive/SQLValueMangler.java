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

import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.utils.Pair;
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

    public static Object internalToSQL( Object value ) {
        Object sqlValue = null;
        if ( value != null ) {
            PrimitiveType pt = PrimitiveType.determinePrimitiveType( value );
            switch ( pt ) {
            case BOOLEAN:
                sqlValue = value;
                break;
            case DATE:
                sqlValue = new java.sql.Date( ( (Date) value ).getDate().getTime() );
                break;
            case DATE_TIME:
                throw new IllegalArgumentException( "SQL type conversion for '" + pt + "' is not implemented yet." );
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

    public static Pair<Object, Integer> sqlToInternal( Object o, PrimitiveType pt ) {
        throw new UnsupportedOperationException( "Not implemented yet" );
    }
}
