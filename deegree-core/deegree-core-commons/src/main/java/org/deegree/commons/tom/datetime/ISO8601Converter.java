//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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

package org.deegree.commons.tom.datetime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts between <a href="http://en.wikipedia.org/wiki/ISO_8601">ISO 8601:2004</a> representations and deegree's
 * temporal primitives.
 * 
 * TODO finish refactoring (http://tracker.deegree.org/deegree-services/ticket/301)
 * 
 * <p>
 * <h4>Differences between deegree's temporal primitive model a
 * </p>
 * <p>
 * Many of the methods that convert dates to and from strings utilize the <a
 * href="http://en.wikipedia.org/wiki/ISO_8601">ISO 8601:2004</a> standard string format
 * <code>yyyy-MM-ddTHH:mm:ss.SSSZ</code>, where <blockquote>
 * 
 * <pre>
 * Symbol   Meaning                 Presentation        Example
 * ------   -------                 ------------        -------
 * y        year                    (Number)            1996
 * M        month in year           (Number)            07
 * d        day in month            (Number)            10
 * h        hour in am/pm (1&tilde;12)    (Number)            12
 * H        hour in day (0&tilde;23)      (Number)            0
 * m        minute in hour          (Number)            30
 * s        second in minute        (Number)            55
 * S        millisecond             (Number)            978
 * Z        time zone               (Number)            -0600
 * </pre>
 * 
 * </blockquote>
 * </p>
 * <p>
 * This class is written to be thread safe. As {@link SimpleDateFormat} is not threadsafe, no shared instances are used.
 * </p>
 * 
 * @author Randall Hauch
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version r304 http://anonsvn.jboss.org/repos/dna/trunk/dna-common/src/main/java/org/jboss/dna/common/util/
 * @version $Revision$, $Date$
 */
public final class ISO8601Converter {

    private static final String ISO_8601_2004_FORMAT_GMT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private static final String ISO_8601_2004_FORMAT_GMT_WO_TIME = "yyyy-MM-dd";

    private static final String ISO_8601_2004_FORMAT_GMT_WO_MS = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private static final String ISO_8601_2004_FORMAT_GMT_TIME = "HH:mm:ss";

    private final static TimeZone GMT = TimeZone.getTimeZone( "GMT" );

    /**
     * Parse the date contained in the supplied string and return a UTC Calendar object. The date must follow one of the
     * standard ISO 8601 formats, of the form <code><i>datepart</i>T<i>timepart</i></code>, where
     * <code><i>datepart</i></code> is one of the following forms:
     * <p>
     * <dl>
     * <dt>YYYYMMDD</dt>
     * <dd>The 4-digit year, the 2-digit month (00-12), and the 2-digit day of the month (00-31). The month and day are
     * optional, but the month is required if the day is given.</dd>
     * <dt>YYYY-MM-DD</dt>
     * <dd>The 4-digit year, the 2-digit month (00-12), and the 2-digit day of the month (00-31). The month and day are
     * optional, but the month is required if the day is given.</dd>
     * <dt>YYYY-Www-D</dt>
     * <dd>The 4-digit year followed by 'W', the 2-digit week number (00-53), and the day of the week (1-7). The day of
     * week number is optional.</dd>
     * <dt>YYYYWwwD</dt>
     * <dd>The 4-digit year followed by 'W', the 2-digit week number (00-53), and the day of the week (1-7). The day of
     * week number is optional.</dd>
     * <dt>YYYY-DDD</dt>
     * <dd>The 4-digit year followed by the 3-digit day of the year (000-365)</dd>
     * <dt>YYYYDDD</dt>
     * <dd>The 4-digit year followed by the 3-digit day of the year (000-365)</dd>
     * </dl>
     * </p>
     * <p>
     * The <code><i>timepart</i></code> consists of one of the following forms that contain the 2-digit hour (00-24),
     * the 2-digit minutes (00-59), the 2-digit seconds (00-59), and the 1-to-3 digit milliseconds. The minutes, seconds
     * and milliseconds are optional, but any component is required if it is followed by another component (e.g.,
     * minutes are required if the seconds are given).
     * <dl>
     * <dt>hh:mm:ss.SSS</dt>
     * <dt>hhmmssSSS</dt>
     * </dl>
     * </p>
     * <p>
     * followed by one of the following time zone definitions:
     * <dt>Z</dt>
     * <dd>The uppercase or lowercase 'Z' to denote UTC time</dd>
     * <dt>&#177;hh:mm</dt>
     * <dd>The 2-digit hour and the 2-digit minute offset from UTC</dd>
     * <dt>&#177;hhmm</dt>
     * <dd>The 2-digit hour and the 2-digit minute offset from UTC</dd>
     * <dt>&#177;hh</dt>
     * <dd>The 2-digit hour offset from UTC</dd>
     * <dt>hh:mm</dt>
     * <dd>The 2-digit hour and the 2-digit minute offset from UTC</dd>
     * <dt>hhmm</dt>
     * <dd>The 2-digit hour and the 2-digit minute offset from UTC</dd>
     * <dt>hh</dt>
     * <dd>The 2-digit hour offset from UTC</dd>
     * </dl>
     * </p>
     * 
     * @param dateString
     *            the string containing the date to be parsed
     * @return the parsed date as a {@link Calendar} object. The return value is always in UTC time zone. Conversion
     *         occurs when necessary.
     * @throws ParseException
     *             if there is a problem parsing the string
     */
    public static DateTime parseISO8601TimeInstant( final String dateString )
                            throws ParseException {
        // TODO check if want to go back to supporting more ISO-style (non-XSD) formats
        return new DateTime( dateString );
    }

    /**
     * Parses ISO8601 duration strings like P1Y2MT5H, PT5M
     * 
     * @param duration
     * @return a new duration
     * @throws ParseException
     */
    public static Duration parseISO8601Duration( final String duration )
                            throws ParseException {
        // Example: P2Y3M14DT10H23M42S
        //
        // Group Optional Field Description
        // ----- -------- --------- ------------------------------------------
        // 1 no P
        // 2 yes 2Y
        // 3 yes 2 years
        // 4 yes 3M
        // 5 yes 3 month
        // 6 yes 14 D
        // 7 yes 14 days
        // 8 yes T
        // 9 yes 10H
        // 10 yes 10 hours
        // 11 yes 23M
        // 12 yes 23 minutes
        // 13 yes 42S
        // 14 yes 42 seconds

        final String regex = "^(P((\\d+)Y)?((\\d+)M)?((\\d+)D)?(T((\\d+)H)?((\\d+)M)?((\\d+)S)?)?)$";
        final Pattern pattern = Pattern.compile( regex );
        final Matcher matcher = pattern.matcher( duration );
        if ( !matcher.matches() ) {
            throw new ParseException( "error while parsing iso8601 date: " + duration, 0 );
        }

        int years = getIntFromMatcher( matcher, 3 );
        int months = getIntFromMatcher( matcher, 5 );
        int days = getIntFromMatcher( matcher, 7 );
        int hours = getIntFromMatcher( matcher, 10 );
        int minutes = getIntFromMatcher( matcher, 12 );
        int seconds = getIntFromMatcher( matcher, 14 );

        return new Duration( years, months, days, hours, minutes, seconds );
    }

    private static int getIntFromMatcher( Matcher matcher, int group ) {
        String value = matcher.group( group );
        if ( value == null ) {
            return 0;
        }
        return Integer.parseInt( value );
    }

    /**
     * Obtain an ISO 8601:2004 string representation of the supplied date.
     * 
     * @param date
     *            the date
     * @return the string in the {@link #ISO_8601_2004_FORMAT_GMT standard format}
     */
    public static String formatISO8601Date( TimeInstant date ) {
        SimpleDateFormat sdf = new SimpleDateFormat( ISO_8601_2004_FORMAT_GMT );
        sdf.setTimeZone( GMT );
        return sdf.format( date.getDate() );
    }

    public static String formatISO8601Date( java.util.Date date ) {
        SimpleDateFormat sdf = new SimpleDateFormat( ISO_8601_2004_FORMAT_GMT );
        sdf.setTimeZone( GMT );
        return sdf.format( date );
    }

    /**
     * @param date
     * @return the date string WithOutMilliSeconds
     */
    public static String formatISO8601DateWOMS( final TimeInstant date ) {
        SimpleDateFormat sdf = new SimpleDateFormat( ISO_8601_2004_FORMAT_GMT_WO_MS );
        sdf.setTimeZone( GMT );
        return sdf.format( date.getDate() );
    }

    /**
     * @param date
     * @return the date string WithOutMilliSeconds
     */
    public static String formatISO8601DateWOMS( final Date date ) {
        SimpleDateFormat sdf = new SimpleDateFormat( ISO_8601_2004_FORMAT_GMT_WO_MS );
        sdf.setTimeZone( GMT );
        return sdf.format( date );
    }

    /**
     * @param date
     * @return the date string without time
     */
    public static String formatISO8601DateWOTime( final TimeInstant date ) {
        SimpleDateFormat sdf = new SimpleDateFormat( ISO_8601_2004_FORMAT_GMT_WO_TIME );
        sdf.setTimeZone( GMT );
        return sdf.format( date.getDate() );
    }

    public static String formatISO8601DateWOTime( final Date date ) {
        SimpleDateFormat sdf = new SimpleDateFormat( ISO_8601_2004_FORMAT_GMT_WO_TIME );
        sdf.setTimeZone( GMT );
        return sdf.format( date );
    }

    /**
     * Obtain an ISO 8601:2004 string representation of the supplied date.
     * 
     * @param date
     *            the date
     * @return the string representation (only time)
     */
    public static String formatISO8601Time( final TimeInstant date ) {
        SimpleDateFormat sdf = new SimpleDateFormat( ISO_8601_2004_FORMAT_GMT_TIME );
        sdf.setTimeZone( GMT );
        return sdf.format( date.getDate() );
    }

    /**
     * Obtain an ISO 8601:2004 string representation of the duration given.
     * 
     * @param duration
     * @return the duration string (eg. P1Y3M, PT6H30M, ...)
     */
    public static String formatISO8601Duration( final Duration duration ) {
        StringBuilder result = new StringBuilder( "P" );
        if ( duration.getYears() > 0 ) {
            result.append( duration.getYears() ).append( 'Y' );
        }
        if ( duration.getMonths() > 0 ) {
            result.append( duration.getMonths() ).append( 'M' );
        }
        if ( duration.getHours() > 0 || duration.getMinutes() > 0 || duration.getSeconds() > 0 ) {
            result.append( 'T' );
            if ( duration.getHours() > 0 ) {
                result.append( duration.getHours() ).append( 'H' );
            }
            if ( duration.getMinutes() > 0 ) {
                result.append( duration.getMinutes() ).append( 'M' );
            }
            if ( duration.getSeconds() > 0 ) {
                result.append( duration.getSeconds() ).append( 'S' );
            }
        }
        return result.toString();
    }

    private ISO8601Converter() {
        // Prevent instantiation
    }
}