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

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
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

    final static TimeZone GMT = TimeZone.getTimeZone( "GMT" );

    final static TimeZone systemDefaultTZ = TimeZone.getDefault();

    @Before
    public void setUTC() {
        TimeZone.setDefault( TimeZone.getTimeZone( "GMT" ) );
    }

    @After
    public void setDefault() {
        TimeZone.setDefault( systemDefaultTZ );
    }

    @Test
    public void testDates_yyyy_MM()
                            throws ParseException {
        Date date = DateUtils.parseISO8601Date( "1983-02" );
        Calendar calendar = Calendar.getInstance( GMT );
        calendar.setTime( date );
        assertEquals( 1983, calendar.get( Calendar.YEAR ) );
        assertEquals( 2 - 1, calendar.get( Calendar.MONTH ) );
        assertEquals( 1, calendar.get( Calendar.DAY_OF_MONTH ) );
        assertUTCMidnight( date );
    }

    @Test
    public void testDates_yyyyMM()
                            throws ParseException {
        Date date = DateUtils.parseISO8601Date( "198302" );
        Calendar calendar = Calendar.getInstance( GMT );
        calendar.setTime( date );
        assertEquals( 1983, calendar.get( Calendar.YEAR ) );
        assertEquals( 2 - 1, calendar.get( Calendar.MONTH ) );
        assertEquals( 1, calendar.get( Calendar.DAY_OF_MONTH ) );
        assertUTCMidnight( date );
    }

    @Test
    public void testDates_yyyyMMdd()
                            throws ParseException {
        Date date = DateUtils.parseISO8601Date( "19830205" );
        assertTestDate( date );
        assertUTCMidnight( date );
    }

    @Test
    public void testDates_yyyy_MM_dd()
                            throws ParseException {
        Date date = DateUtils.parseISO8601Date( "1983-02-05" );
        assertTestDate( date );
        assertUTCMidnight( date );
    }

    @Test
    public void testDates_yyyy_DD()
                            throws ParseException {
        Date date = DateUtils.parseISO8601Date( "1983-036" );
        assertTestDate( date );
        assertUTCMidnight( date );
    }

    @Test
    public void testDates_yyyyDD()
                            throws ParseException {
        Date date = DateUtils.parseISO8601Date( "1983036" );
        assertTestDate( date );
        assertUTCMidnight( date );
    }

    @Test
    public void testDates_yyyy()
                            throws ParseException {
        Date date = DateUtils.parseISO8601Date( "1983" );
        Calendar calendar = Calendar.getInstance( GMT );
        calendar.setTime( date );
        assertEquals( 1983, calendar.get( Calendar.YEAR ) );
        assertEquals( 1 - 1, calendar.get( Calendar.MONTH ) );
        assertEquals( 1, calendar.get( Calendar.DAY_OF_MONTH ) );
        assertUTCMidnight( date );
    }

    @Test
    public void testDates_yyyy_MM_ddTHH_mm_ss_SSS()
                            throws ParseException {
        Date date = DateUtils.parseISO8601Date( "1983-02-05T16:42:23.823" );
        assertTestDate( date );
        assertUTCTime( 16, 42, 23, 823, date );
    }

    @Test
    public void testDates_yyyy_MM_ddTHHmmss_SSS()
                            throws ParseException {
        Date date = DateUtils.parseISO8601Date( "1983-02-05T164223.823" );
        assertTestDate( date );
        assertUTCTime( 16, 42, 23, 823, date );
    }

    @Test
    public void testDates_yyyy_MM_ddTHH_mm_ss()
                            throws ParseException {
        Date date = DateUtils.parseISO8601Date( "1983-02-05T16:42:23" );
        assertTestDate( date );
        assertUTCTime( 16, 42, 23, 0, date );
    }

    @Test
    public void testDates_yyyy_MM_ddTHHmmss()
                            throws ParseException {
        Date date = DateUtils.parseISO8601Date( "1983-02-05T164223" );
        assertTestDate( date );
        assertUTCTime( 16, 42, 23, 0, date );
    }

    @Test
    public void testDates_yyyy_MM_ddTHH_mm()
                            throws ParseException {
        Date date = DateUtils.parseISO8601Date( "1983-02-05T16:42" );
        assertTestDate( date );
        assertUTCTime( 16, 42, 0, 0, date );
    }

    @Test
    public void testDates_yyyy_MM_ddTHHmm()
                            throws ParseException {
        Date date = DateUtils.parseISO8601Date( "1983-02-05T1642" );
        assertTestDate( date );
        assertUTCTime( 16, 42, 0, 0, date );
    }

    @Test
    public void testDates_yyyy_MM_ddTHHmmssSSS()
                            throws ParseException {
        Date date = DateUtils.parseISO8601Date( "1983-02-05T164200.999" );
        assertTestDate( date );
        assertUTCTime( 16, 42, 0, 999, date );
    }

    @Test
    public void testDates_yyyy_MM_ddTHHmmssSS()
                            throws ParseException {
        Date date = DateUtils.parseISO8601Date( "1983-02-05T164200.99" );
        assertTestDate( date );
        assertUTCTime( 16, 42, 0, 990, date );
    }

    @Test
    public void testDates_yyyy_MM_ddTHHmmssS()
                            throws ParseException {
        Date date = DateUtils.parseISO8601Date( "1983-02-05T164200.9" );
        assertTestDate( date );
        assertUTCTime( 16, 42, 0, 900, date );
    }

    @Test
    public void testDates_yyyy_MM_ddTHH()
                            throws ParseException {
        Date date = DateUtils.parseISO8601Date( "1983-02-05T16" );
        assertTestDate( date );
        assertUTCTime( 16, 0, 0, 0, date );
    }

    @Test
    public void testDates_yyyy_MM_ddTHH_mm_ss_TZ()
                            throws ParseException {
        Date date = DateUtils.parseISO8601Date( "1983-02-05T16:42:23+01:00" );
        assertTestDate( date );
        assertUTCTime( 15, 42, 23, 0, date );
    }

    @Test
    public void testDates_yyyy_MM_ddTHH_mm_ss_locale()
                            throws ParseException {
        TimeZone.setDefault( TimeZone.getTimeZone( "Europe/Berlin" ) );
        Date date = DateUtils.parseISO8601Date( "1983-02-05T16:42:23" );
        assertTestDate( date );
        assertUTCTime( 15, 42, 23, 0, date );
    }

    @Test
    public void testDates_yyyy_MM_ddTHH_mm_ss_TZ_locale()
                            throws ParseException {
        TimeZone.setDefault( TimeZone.getTimeZone( "Europe/Berlin" ) );
        Date date = DateUtils.parseISO8601Date( "1983-02-05T16:42:23Z" );
        assertTestDate( date );
        assertUTCTime( 16, 42, 23, 0, date );
    }

    @Test
    public void testDates_yyyy_MM_ddZ_locale()
                            throws ParseException {
        TimeZone.setDefault( TimeZone.getTimeZone( "GMT-1:00" ) );
        Date date = DateUtils.parseISO8601Date( "1983-02-05Z" );
        assertTestDate( date );
        assertUTCTime( 0, 0, 0, 0, date );
        TimeZone.setDefault( TimeZone.getTimeZone( "GMT+1:00" ) );
        date = DateUtils.parseISO8601Date( "1983-02-05Z" );
        assertTestDate( date );
        assertUTCTime( 0, 0, 0, 0, date );
    }

    @Test
    public void testFormatISO8601()
                            throws ParseException {
        Date date = DateUtils.parseISO8601Date( "1983-02-05T16:42:23Z" );
        String formatedDate = DateUtils.formatISO8601Date( date );
        assertEquals( "1983-02-05T16:42:23.000Z", formatedDate );
    }

    // @Test
    public void testFormatLocale()
                            throws ParseException {
        TimeZone.setDefault( TimeZone.getTimeZone( "Europe/Berlin" ) );
        Date date = DateUtils.parseISO8601Date( "1983-02-05T16:42:23Z" );
        String formatedDate = DateUtils.formatLocaleDate( date, Locale.GERMANY );
        assertEquals( "05.02.1983 17:42:23 CET", formatedDate );
    }

    @Test
    public void testDuration()
                            throws ParseException {
        Date date = DateUtils.parseISO8601Date( "1981-08-31T12:12:23Z" );
        Duration duration = DateUtils.parseISO8601Duration( "P1Y5M5DT3H90M" );
        Date dateAfter = duration.getDateAfter( date );
        assertTestDate( dateAfter );
        assertUTCTime( 16, 42, 23, 0, dateAfter );
    }

    private void assertUTCTime( int hour, int min, int sec, int msec, Date date ) {
        Calendar calendar = Calendar.getInstance( GMT );
        calendar.setTime( date );
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

    private static void assertTestDate( Date date ) {
        Calendar calendar = Calendar.getInstance( GMT );
        calendar.setTime( date );
        assertEquals( 1983, calendar.get( Calendar.YEAR ) );
        assertEquals( 2 - 1, calendar.get( Calendar.MONTH ) );
        assertEquals( 5, calendar.get( Calendar.DAY_OF_MONTH ) );
    }

}
