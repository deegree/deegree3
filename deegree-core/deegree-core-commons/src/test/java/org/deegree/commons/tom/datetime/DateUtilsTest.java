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
package org.deegree.commons.tom.datetime;

import static java.util.TimeZone.getTimeZone;
import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

/**
 * 
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class DateUtilsTest {

    final static TimeZone GMT = getTimeZone( "GMT" );

    final static TimeZone systemDefaultTZ = TimeZone.getDefault();

    @Test
    public void testDates_yyyy_MM_dd()
                            throws ParseException {
        DateTime date = DateUtils.parseISO8601Date( "1983-02-05Z" );
        assertTestDate( date );
        assertUTCMidnight( date.getSQLDate() );
    }

    @Test
    public void testDates_yyyy_MM_ddTHH_mm_ss_SSS()
                            throws ParseException {
        DateTime date = DateUtils.parseISO8601Date( "1983-02-05T16:42:23.823Z" );
        assertTestDate( date );
        assertTime( 16, 42, 23, 823, date );
    }

    @Test
    public void testDates_yyyy_MM_ddTHH_mm_ss()
                            throws ParseException {
        DateTime date = DateUtils.parseISO8601Date( "1983-02-05T16:42:23Z" );
        assertTestDate( date );
        assertTime( 16, 42, 23, 0, date );
    }

//    @Test
//    public void testDates_yyyy_MM_ddTHH_mm()
//                            throws ParseException {
//        DateTime date = DateUtils.parseISO8601Date( "1983-02-05T16:42Z" );
//        assertTestDate( date );
//        assertUTCTime( 16, 42, 0, 0, date );
//    }

    @Test
    public void testDates_yyyy_MM_ddTHH_mm_ss_TZ()
                            throws ParseException {
        DateTime date = DateUtils.parseISO8601Date( "1983-02-05T16:42:23+01:00" );
        assertTestDate( date );
        assertTime( 16, 42, 23, 0, date );
        assertEquals( 3600000, date.getCalendar().getTimeZone().getRawOffset() );
    }

    @Test
    public void testDates_yyyy_MM_ddTHH_mm_ss_locale()
                            throws ParseException {
        DateTime date = DateUtils.parseISO8601Date( "1983-02-05T16:42:23" );
        // assertTestDate( date );
        // assertUTCTime( 15, 42, 23, 0, date );
    }

    // @Test
    // public void testDates_yyyy_MM_ddTHH_mm_ss_TZ_locale()
    // throws ParseException {
    // TimeZone.setDefault( TimeZone.getTimeZone( "Europe/Berlin" ) );
    // Date date = DateUtils.parseISO8601Date( "1983-02-05T16:42:23Z" );
    // assertTestDate( date );
    // assertUTCTime( 16, 42, 23, 0, date );
    // }
    //
    // @Test
    // public void testDates_yyyy_MM_ddZ_locale()
    // throws ParseException {
    // TimeZone.setDefault( TimeZone.getTimeZone( "GMT-1:00" ) );
    // Date date = DateUtils.parseISO8601Date( "1983-02-05Z" );
    // assertTestDate( date );
    // assertUTCTime( 0, 0, 0, 0, date );
    // TimeZone.setDefault( TimeZone.getTimeZone( "GMT+1:00" ) );
    // date = DateUtils.parseISO8601Date( "1983-02-05Z" );
    // assertTestDate( date );
    // assertUTCTime( 0, 0, 0, 0, date );
    // }

    @Test
    public void testFormatISO8601()
                            throws ParseException {
        DateTime date = DateUtils.parseISO8601Date( "1983-02-05T16:42:23Z" );
        String formatedDate = DateUtils.formatISO8601Date( date );
        assertEquals( "1983-02-05T16:42:23.000Z", formatedDate );
    }

    @Test
    public void testDuration()
                            throws ParseException {
        DateTime date = DateUtils.parseISO8601Date( "1981-08-31T12:12:23Z" );
        Duration duration = DateUtils.parseISO8601Duration( "P1Y5M5DT3H90M" );
        Date dateAfter = duration.getDateAfter( date.getSQLDate() );
//        assertTestDate( dateAfter );
//        assertTime( 16, 42, 23, 0, dateAfter );
    }

    private void assertTime( int hour, int min, int sec, int msec, DateTime date ) {
        Calendar calendar = date.getCalendar();
        assertEquals( hour, calendar.get( Calendar.HOUR_OF_DAY ) );
        assertEquals( min, calendar.get( Calendar.MINUTE ) );
        assertEquals( sec, calendar.get( Calendar.SECOND ) );
        assertEquals( msec, calendar.get( Calendar.MILLISECOND ) );
    }

    private static void assertUTCMidnight( Date date ) {
        Calendar calendar = Calendar.getInstance( GMT );
        calendar.setTime( date );
        assertEquals( 0, calendar.get( Calendar.HOUR_OF_DAY ) );
        assertEquals( 0, calendar.get( Calendar.MINUTE ) );
        assertEquals( 0, calendar.get( Calendar.SECOND ) );
        assertEquals( 0, calendar.get( Calendar.MILLISECOND ) );
    }

    private static void assertTestDate( DateTime date ) {
        Calendar cal = date.getCalendar();
        assertEquals( 1983, cal.get( Calendar.YEAR ) );
        assertEquals( 2 - 1, cal.get( Calendar.MONTH ) );
        assertEquals( 5, cal.get( Calendar.DAY_OF_MONTH ) );
    }
}
