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
package org.deegree.commons.tom.datetime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import javax.xml.bind.DatatypeConverter;

/**
 * {@link TimeInstant} for representing dates with time information (e.g. <code>xs:dateTime</code>).
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DateTime extends TimeInstant {

    private static final String ISO_8601_FORMAT_GMT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private static final String ISO_8601_FORMAT_NO_MILLISECONDS_GMT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * Creates a new {@link DateTime} instance from the given <code>xs:dateTime</code> encoded value.
     * 
     * @param xsDateTime
     *            encoded dateTime, must not be <code>null</code>
     * @throws IllegalArgumentException
     *             if parameter does not conform to lexical value space defined in XML Schema Part 2: Datatypes for
     *             <code>xs:dateTime</code>
     */
    public DateTime( String xsDateTime ) throws IllegalArgumentException {
        super( DatatypeConverter.parseDateTime( xsDateTime ), isLocal( xsDateTime ) );
    }

    public DateTime( java.util.Date date, TimeZone tz ) {
        super( date, tz );
    }

    public DateTime( Calendar cal, boolean isUnknown ) {
        super( cal, isUnknown );
    }

    public String toXsDateTimeGmt() {
        SimpleDateFormat sdf = new SimpleDateFormat( ISO_8601_FORMAT_GMT );
        sdf.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
        return sdf.format( getCalendar().getTime() );
    }

    public String toXsDateTimeNoMillisecondsGmt() {
        SimpleDateFormat sdf = new SimpleDateFormat( ISO_8601_FORMAT_NO_MILLISECONDS_GMT );
        sdf.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
        return sdf.format( getCalendar().getTime() );
    }

    @Override
    public String toString() {
        return DatatypeConverter.printDateTime( getCalendar() );
    }
}
