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
package org.deegree.framework.util;

import static org.deegree.framework.log.LoggerFactory.getLogger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Properties;

import org.deegree.framework.log.ILogger;

/**
 * The <code>TimeTools</code> class can be used to format Strings to timecodes and get Calenadars of a given Timecode.
 *
 * <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */

public class TimeTools {

    private static final ILogger LOG = getLogger( TimeTools.class );

    /**
     * A final Year representation
     */
    public static final int YEAR = 0;

    /**
     * A final Month representation
     */
    public static final int MONTH = 1;

    /**
     * A final Day representation
     */
    public static final int DAY = 2;

    /**
     * A final Hour representation
     */
    public static final int HOUR = 3;

    /**
     * A final Minute representation
     */
    public static final int MINUTE = 4;

    /**
     * A final Second representation
     */
    public static final int SECOND = 5;

    /**
     * A final MilliSecond representation
     */
    public static final int MILLISECOND = 6;

    private static SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.GERMANY );

    private static boolean CORRECT_TIMEZONES = false;

    static {
        Properties props = new Properties();
        try {
            props.load( TimeTools.class.getResourceAsStream( "timetools.properties" ) );
            Object prop = props.get( "usecorrecttimezones" );
            CORRECT_TIMEZONES = prop != null && prop.toString().equalsIgnoreCase( "true" );
        } catch ( IOException e ) {
            LOG.logError( "Unknown error", e );
        }
    }

    /**
     * @return the current timestamp in ISO format
     */
    public static String getISOFormattedTime() {
        return getISOFormattedTime( new Date( System.currentTimeMillis() ) );
    }

    /**
     * returns the date calendar in ISO format
     *
     * @param date
     * @return the date calendar in ISO format
     */
    public static String getISOFormattedTime( Date date ) {
        if ( CORRECT_TIMEZONES ) {
            return TimeTools2.getISOFormattedTime( date );
        }
        return sdf.format( date ).replace( ' ', 'T' );
    }

    /**
     * @param date
     *            the date object to get the time values of
     * @param locale
     *            the locale to convert to
     * @return the date calendar in ISO format considering the passed locale
     */
    public static String getISOFormattedTime( Date date, Locale locale ) {
        SimpleDateFormat sdf_ = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS", locale );
        return sdf_.format( date );
    }

    /**
     *
     * @param cal
     *            a Calendar to get the timevalues of
     * @return the passed calendar in ISO format
     */
    public static String getISOFormattedTime( Calendar cal ) {
        return getISOFormattedTime( cal.getTime() );
    }

    /**
     * returns a part of the submitted iso-formatted timestamp. possible values
     *
     * @param value
     *            <ul>
     *            <li>YEAR
     *            <li>MONTH
     *            <li>DAY
     *            <li>HOUR
     *            <li>MINUTE
     *            <li>SECOND
     *            </ul>
     * @param isoTimestamp
     *            an ISO timestamp-> year-mon-dayThours:min:sec
     * @return the timevalue of the given value
     */
    private static int get( int value, String[] isoTimestamp ) {
        if ( value > isoTimestamp.length - 1 ) {
            return 0;
        }
        return Integer.parseInt( isoTimestamp[value] );
    }

    /**
     * Notice that a Calendar does not consider milli seconds. This means 2008-09-22T12:31:00.999Z and
     * 2008-09-22T12:31:00.111Z will return the same Calendar value!
     *
     * @param isoDate
     *            an ISO timestamp-> year-mon-dayThours:min:sec
     * @return an instance of a <code>GregorianCalendar</tt> from an ISO timestamp
     * @throws NumberFormatException
     *             if the parsted values of the given String are no proper numbers.
     */
    public static GregorianCalendar createCalendar( String isoDate )
                            throws NumberFormatException {
        if ( CORRECT_TIMEZONES ) {
            return (GregorianCalendar) TimeTools2.createCalendar( isoDate );
        }

        String s = isoDate.trim();
        if ( s.endsWith( "Z" ) ) {
            s = s.substring( 0, s.length() - 1 );
        }
        String[] tmp = StringTools.toArray( s, "-:T. ", false );
        int y = TimeTools.get( TimeTools.YEAR, tmp );
        int m = TimeTools.get( TimeTools.MONTH, tmp );
        int d = TimeTools.get( TimeTools.DAY, tmp );
        int h = TimeTools.get( TimeTools.HOUR, tmp );
        int min = TimeTools.get( TimeTools.MINUTE, tmp );
        int sec = TimeTools.get( TimeTools.SECOND, tmp );
        return new GregorianCalendar( y, m - 1, d, h, min, sec );
    }

    /**
     *
     * @param isoDate
     *            an ISO timestamp-> year-mon-dayThours:min:sec.millis
     * @return an instance of a <code>java.util.Date</tt> from an ISO timestamp
     * @throws NumberFormatException
     *             if the parsted values of the given String are no proper numbers.
     */
    public static Date createDate( String isoDate ) {
        String s = isoDate.trim();
        if ( s.endsWith( "Z" ) ) {
            s = s.substring( 0, s.length() - 1 );
        }
        String[] tmp = StringTools.toArray( s, "-:T. ", false );
        int y = TimeTools.get( TimeTools.YEAR, tmp );
        int m = TimeTools.get( TimeTools.MONTH, tmp );
        int d = TimeTools.get( TimeTools.DAY, tmp );
        int h = TimeTools.get( TimeTools.HOUR, tmp );
        int min = TimeTools.get( TimeTools.MINUTE, tmp );
        int sec = TimeTools.get( TimeTools.SECOND, tmp );
        int millis = TimeTools.get( TimeTools.MILLISECOND, tmp );
        long l = new GregorianCalendar( y, m - 1, d, h, min, sec ).getTimeInMillis();
        l += millis;
        return new Date( l );

    }

}
