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
package org.deegree.commons.tom.datetime;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.YEAR;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.TimeZone;

import org.junit.Test;

/**
 * Test cases for {@link DateTime}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class TimeInstantTest {

    @Test
    public void testDateTimeFromXsDateTimeUtc() {
        DateTime dt = new DateTime( "2002-05-30T09:00:00Z" );
        assertFalse( dt.isLocal );
        assertEquals( 2002, dt.getCalendar().get( YEAR ) );
        // month is 0-based
        assertEquals( 4, dt.getCalendar().get( MONTH ) );
        assertEquals( 30, dt.getCalendar().get( DAY_OF_MONTH ) );
        assertEquals( 9, dt.getCalendar().get( HOUR_OF_DAY ) );
        assertEquals( 0, dt.getCalendar().get( MINUTE ) );
        assertEquals( 0, dt.getCalendar().get( SECOND ) );
        assertEquals( 0, dt.getCalendar().get( MILLISECOND ) );
        assertEquals( 0, dt.getCalendar().getTimeZone().getRawOffset() );
        assertEquals( "2002-05-30T09:00:00Z", dt.toString() );
        assertEquals( "2002-05-30T09:00:00.000Z", dt.toXsDateTimeGmt() );
        assertEquals( 1022749200000L, dt.getTimeInMilliseconds() );        
        assertEquals( 1022749200000L, dt.getSQLDate().getTime() );
    }

    @Test
    public void testDateTimeFromXsDateTimeWithOffset() {
        DateTime dt = new DateTime( "2002-05-30T09:00:00+01:00" );
        assertFalse( dt.isLocal );
        assertEquals( 2002, dt.getCalendar().get( YEAR ) );
        // month is 0-based
        assertEquals( 4, dt.getCalendar().get( MONTH ) );
        assertEquals( 30, dt.getCalendar().get( DAY_OF_MONTH ) );
        assertEquals( 9, dt.getCalendar().get( HOUR_OF_DAY ) );
        assertEquals( 0, dt.getCalendar().get( MINUTE ) );
        assertEquals( 0, dt.getCalendar().get( SECOND ) );
        assertEquals( 0, dt.getCalendar().get( MILLISECOND ) );
        assertEquals( 3600000, dt.getCalendar().getTimeZone().getRawOffset() );
        assertEquals( "2002-05-30T09:00:00+01:00", dt.toString() );
        assertEquals( "2002-05-30T08:00:00.000Z", dt.toXsDateTimeGmt() );
        assertEquals( 1022745600000L, dt.getTimeInMilliseconds() );
        assertEquals( 1022745600000L, dt.getSQLDate().getTime() );
    }

    @Test
    public void testDateTimeFromXsDateTimeLocalTime() {
        DateTime dt = new DateTime( "2002-05-30T09:00:00" );
        assertTrue( dt.isLocal );        
        assertEquals( 2002, dt.getCalendar().get( YEAR ) );
        // month is 0-based
        assertEquals( 4, dt.getCalendar().get( MONTH ) );
        assertEquals( 30, dt.getCalendar().get( DAY_OF_MONTH ) );
        assertEquals( 9, dt.getCalendar().get( HOUR_OF_DAY ) );
        assertEquals( 0, dt.getCalendar().get( MINUTE ) );
        assertEquals( 0, dt.getCalendar().get( SECOND ) );
        assertEquals( 0, dt.getCalendar().get( MILLISECOND ) );
        int offset = TimeZone.getDefault().getOffset( dt.getTimeInMilliseconds() );
        assertEquals( 1022749200000L - offset, dt.getTimeInMilliseconds() );        
        assertEquals( 1022749200000L - offset, dt.getSQLDate().getTime() );
    }
}
