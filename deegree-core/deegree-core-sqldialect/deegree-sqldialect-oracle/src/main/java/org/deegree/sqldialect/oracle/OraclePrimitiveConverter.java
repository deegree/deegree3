//$HeadURL: svn+ssh://criador.lat-lon.de/srv/svn/deegree-intern/trunk/latlon-sqldialect-oracle/src/main/java/de/latlon/deegree/sqldialect/oracle/OraclePrimitiveConverter.java $
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
package org.deegree.sqldialect.oracle;

import static org.deegree.commons.utils.time.DateUtils.formatISO8601DateWOMS;

import java.sql.SQLException;
import java.text.ParseException;

import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.tom.sql.DefaultPrimitiveConverter;
import org.deegree.commons.utils.time.DateUtils;

/**
 * Implementations convert between {@link PrimitiveValue} particles and SQL column values.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schmitz $
 * 
 * @version $Revision: 297 $, $Date: 2011-06-10 10:23:47 +0200 (Fr, 10. Jun 2011) $
 */
public class OraclePrimitiveConverter extends DefaultPrimitiveConverter {

    public OraclePrimitiveConverter( PrimitiveType pt, String column ) {
        super( pt, column );
    }

    @Override
    protected PrimitiveValue toDateParticle( Object sqlValue ) {
        Date value = null;
        if ( sqlValue instanceof oracle.sql.TIMESTAMP ) {
            try {
                value = new Date( DateUtils.formatISO8601DateWOTime( ( (oracle.sql.TIMESTAMP) sqlValue ).dateValue() ) );
            } catch ( ParseException e ) {
                throw new IllegalArgumentException( e.getMessage(), e );
            } catch ( SQLException e ) {
                throw new IllegalArgumentException( e.getMessage(), e );
            }
        } else {
            PrimitiveValue particle = super.toDateParticle( sqlValue );
            return particle;
        }
        return new PrimitiveValue( value, pt );
    }

    @Override
    protected PrimitiveValue toDateTimeParticle( Object sqlValue ) {
        DateTime value = null;
        if ( sqlValue instanceof oracle.sql.TIMESTAMP ) {
            try {
                value = new DateTime( formatISO8601DateWOMS( ( (oracle.sql.TIMESTAMP) sqlValue ).dateValue() ) );
            } catch ( ParseException e ) {
                throw new IllegalArgumentException( "Unable to convert sql result value of type '"
                                                    + sqlValue.getClass() + "' to DateTime object.", e );
            } catch ( SQLException e ) {
                throw new IllegalArgumentException( e.getMessage(), e );
            }
        } else if ( sqlValue instanceof java.sql.Timestamp ) {
            try {
                java.util.Date d = new java.util.Date( ( (java.sql.Timestamp) sqlValue ).getTime() );
                value = new DateTime( formatISO8601DateWOMS( d ) );
            } catch ( ParseException e ) {
                throw new IllegalArgumentException( "Unable to convert sql result value of type '"
                                                    + sqlValue.getClass() + "' to DateTime object.", e );
            }
        } else {
            throw new IllegalArgumentException( "Unable to convert sql result value of type '" + sqlValue.getClass()
                                                + "' to DateTime object." );
        }
        return new PrimitiveValue( value, pt );
    }

    // @Override
    // protected PrimitiveValue toTimeParticle( Object sqlValue ) {
    // Time value = null;
    // if ( sqlValue instanceof Time ) {
    // value = (Time) sqlValue;
    // } else if ( sqlValue instanceof Date ) {
    // try {
    // value = new Time( DateUtils.formatISO8601Time( (java.util.Date) sqlValue ) );
    // } catch ( ParseException e ) {
    // throw new IllegalArgumentException( e.getMessage(), e );
    // }
    // } else {
    // throw new IllegalArgumentException( "Unable to convert sql result value of type '" + sqlValue.getClass()
    // + "' to Time object." );
    // }
    // return new PrimitiveValue( value, pt );
    // }
}