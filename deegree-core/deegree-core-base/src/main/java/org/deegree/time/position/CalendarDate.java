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
package org.deegree.time.position;

import java.math.BigInteger;

/**
 * {@link TemporalPosition} that may include year, year and month or year, month and day.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CalendarDate implements TemporalPosition {

    private final BigInteger year;

    private final TimeZone timeZone;

    private final Short month;

    private final Short day;

    public CalendarDate( BigInteger year, TimeZone timeZone, Short month, Short day ) {
        this.year = year;
        this.timeZone = timeZone;
        this.month = month;
        this.day = day;
    }

    /**
     * Returns the year.
     * 
     * @return year, never <code>null</code>
     */
    public BigInteger getYear() {
        return year;
    }

    /**
     * Returns the time zone.
     * 
     * @return time zone, can be <code>null</code> (no timezone information)
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Returns the month.
     * 
     * @return month, can be <code>null</code> (no month information)
     */
    public Short getMonth() {
        return month;
    }

    /**
     * Returns the day.
     * 
     * @return day, can be <code>null</code> (no day information)
     */
    public Short getDay() {
        return day;
    }

    @Override
    public int compareTo( TemporalPosition arg0 ) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public IndeterminateValue getIndeterminateValue() {
        // TODO Auto-generated method stub
        return null;
    }
}
