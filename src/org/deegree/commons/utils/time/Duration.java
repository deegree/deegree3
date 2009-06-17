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
package org.deegree.commons.utils.time;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * This class stores a time duration.
 *
 * <p>
 * A {@link Duration} object is immutable.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class Duration {

    private final static TimeZone GMT = TimeZone.getTimeZone( "GMT" );
    private final int years;
    private final int months;
    private final int days;
    private final int hours;
    private final int minutes;
    private final int seconds;

    /**
     * @param years
     * @param months
     * @param days
     * @param hours
     * @param minutes
     * @param seconds
     */
    public Duration( int years, int months, int days, int hours, int minutes, int seconds ) {
        this.years = years;
        this.months = months;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    /**
     * @param date
     * @return a new date that is before the given date
     */
    public Date getDateBefore( Date date ) {
        Calendar tmp = Calendar.getInstance(GMT);
        tmp.setTime( date );
        tmp.add( Calendar.YEAR, -years );
        tmp.add( Calendar.MONTH, -months );
        tmp.add( Calendar.DAY_OF_MONTH, -days );
        tmp.add( Calendar.HOUR_OF_DAY, -hours );
        tmp.add( Calendar.MINUTE, -minutes );
        tmp.add( Calendar.SECOND, -seconds );
        return tmp.getTime();
    }

    /**
     * @param date
     * @return a new date that is after the given date
     */
    public Date getDateAfter( Date date ) {
        Calendar tmp = Calendar.getInstance(GMT);
        tmp.setTime( date );
        tmp.add( Calendar.YEAR, years );
        tmp.add( Calendar.MONTH, months );
        tmp.add( Calendar.DAY_OF_MONTH, days );
        tmp.add( Calendar.HOUR_OF_DAY, hours );
        tmp.add( Calendar.MINUTE, minutes );
        tmp.add( Calendar.SECOND, seconds );
        return tmp.getTime();
    }

    @Override
    public boolean equals( Object obj ) {
        if ( obj == null || ! (obj instanceof Duration) ) {
            return false;
        }
        Duration that = (Duration) obj;
        return this.years == that.years
            && this.months == that.months
            && this.days == that.days
            && this.hours == that.hours
            && this.minutes == that.minutes
            && this.seconds == that.seconds;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + this.years;
        hash = 31 * hash + this.months;
        hash = 31 * hash + this.days;
        hash = 31 * hash + this.hours;
        hash = 31 * hash + this.minutes;
        hash = 31 * hash + this.seconds;
        return hash;
    }

    @Override
    public String toString() {
        return DateUtils.formatISO8601Duration( this );
    }

    /**
     * @return the days
     */
    public int getDays() {
        return days;
    }

    /**
     * @return the hours
     */
    public int getHours() {
        return hours;
    }

    /**
     * @return the minutes
     */
    public int getMinutes() {
        return minutes;
    }

    /**
     * @return the months
     */
    public int getMonths() {
        return months;
    }

    /**
     * @return the seconds
     */
    public int getSeconds() {
        return seconds;
    }

    /**
     * @return the years
     */
    public int getYears() {
        return years;
    }

}
