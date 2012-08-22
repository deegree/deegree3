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

package org.deegree.ogcwebservices.wpvs.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * @version $Revision$ $Date$
 */
public class SunPosition {

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private double daysSinceVernalEquinox;

    /**
     * Constructs a sunposition at the currenttime
     */
    public SunPosition(){
      GregorianCalendar calendar = new GregorianCalendar();
      this.year = calendar.get( Calendar.YEAR );
      this.month = calendar.get( Calendar.MONTH ) + 1;
      this.day = calendar.get( Calendar.DAY_OF_MONTH );
      this.hour = calendar.get( Calendar.HOUR_OF_DAY );
      this.minute = calendar.get( Calendar.MINUTE );
      daysSinceVernalEquinox = getDaySinceVernalEquinox( );
    }
    /**
     * Constructs a sunposition with the given Calendar
     * @param calendar a given Calendar
     */
    public SunPosition( Calendar calendar ){
      this( calendar.get( Calendar.YEAR ),
            calendar.get( Calendar.MONTH ) + 1,
            calendar.get( Calendar.DAY_OF_MONTH ),
            calendar.get( Calendar.HOUR_OF_DAY ),
            calendar.get( Calendar.MINUTE ) );
    }

    /**
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     */
    public SunPosition( int year, int month, int day, int hour, int minute ){
        this.year = year;
        this.month = month;
        if( month <= 0 || month > 12 )
            this.month = 1;
        this.day = day;
        if( day <= 0 || day > 32 )
            this.day = 1;
        this.hour = hour;
        if( hour < 0 || hour >=24 )
            this.hour = 0;
        this.minute = minute;
        if( minute < 0 || minute >= 60 )
            this.minute = 0;

        daysSinceVernalEquinox = getDaySinceVernalEquinox( );
    }
    /**
     * calculates the solar altitude for given latitude, year, month, date, hour
     * and minute
     * @param latitude latitude of the the viewers position
     * @return the solar altitud fo given latitude
     */
    public double getVerticalSunposition( double latitude )
    {
        // Hour Angle (H),
        // Solar Declination (D),
        // Latitude (L)
        // solar altitude (A).
        // sin(A) = sin(D)*sin(L) + cos(D)*cos(L)*cos(H)
        double rad23_5 = 0.41015237421866745057706;
        //double days = getDaySinceVernalEquinox( year, month, date );
        double sinD = Math.sin( rad23_5 ) * Math.sin( Math.toRadians(  daysSinceVernalEquinox * 360.0 / 365.0 ) );
        double cosD = Math.cos( Math.asin( sinD ) );
        // the sun hour angle is zero when the object is on the meridian
        double h = getHorizontalSunPosition( ) - Math.toRadians(180);
        double radL = Math.toRadians( latitude );
        double sinA = sinD * Math.sin(radL) + cosD * Math.cos(radL) * Math.cos(h);

        return Math.asin( sinA );
    }

    /**
     * calculates the horizontal angle of the sun depending only on hour and minute!
     * @return the horizontal angle
     */
    public double getHorizontalSunPosition( )
    {
        double d = hour + minute/60.0;
        d = 180 + (( d - 12 ) * 15.0);
        return Math.toRadians(d);
    }

    /**
     * caluculates for a given date the number of days since the last vernal(spring)
     * equinox. (leap years are considered)
     */
    private double getDaySinceVernalEquinox( )
    {
        //0 for january
        GregorianCalendar calendar = new GregorianCalendar( year, 2, 21 );
        int vEq = calendar.get( Calendar.DAY_OF_YEAR );
        calendar = new GregorianCalendar( year, month-1, day-1 );
        int doy = calendar.get( Calendar.DAY_OF_YEAR );
        if ( doy < vEq ) {
            doy = ( (calendar.isLeapYear(year)?366:365) - vEq ) + doy;
        }
        return doy-vEq;
    }

}
