//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstraße 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de

 ---------------------------------------------------------------------------*/

package org.deegree.commons.utils;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.util.Calendar.DAY_OF_YEAR;
import static java.util.Calendar.HOUR;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.YEAR;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.deegree.commons.utils.math.Vectors3f;

/**
 * 
 * The <code>SunPosition</code> class is a utility class which can be used to calculate the sun's altitude for a given
 * latitue and a Calendar.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class SunPosition {

    private static double RAD_23_5 = 0.41015237421866745057706;

    /**
     * calculates the solar altitude for given latitude, year, month, date, hour and minute
     * 
     * @param date
     * 
     * @param latitude
     *            latitude of the the viewers position in degrees
     * @return the solar altitude of given latitude
     */
    public static double getSunAltitude( GregorianCalendar date, double latitude ) {

        // double days = getDaySinceVernalEquinox( year, month, date );
        double sinD = Math.sin( RAD_23_5 )
                      * Math.sin( Math.toRadians( getDaySinceVernalEquinox( date ) * 360 / 365.25 ) );
        double cosD = Math.cos( Math.asin( sinD ) );
        // the sun hour angle is zero when the object is on the meridian
        double h = getHourAngle( date );
        double radL = Math.toRadians( latitude );
        double sinA = sinD * Math.sin( radL ) + cosD * Math.cos( radL ) * Math.cos( h );

        return Math.asin( sinA );
    }

    public static float[] getEucledianSunDirection( GregorianCalendar date, double latitude ) {
        float[] direction = new float[3];
        double vPos = getSunAltitude( date, latitude );
        double hPos = getHourAngle( date );
        direction[0] = (float) sin( hPos );
        direction[1] = (float) sin( vPos );
        direction[2] = (float) -Math.abs( Math.cos( hPos ) );
        return direction;
    }

    public static double getAltitudeFromWiki( GregorianCalendar date, double latitude, double longitude ) {
        // the WORLDTIME - UT, calculated from Julian Day
        double jd = getJulianDay( date );
        System.out.println( "JD: " + jd );
        double n = getJulianDay( date ) - 2451545;
        System.out.println( "n: " + n );
        double eclLength = getSunEclipticalCoordinate( n );
        System.out.println( "Ecliptical length: " + Math.toDegrees( eclLength ) );
        double e = getEcliptik( n );
        System.out.println( "e: " + Math.toDegrees( e ) );
        double equatorialCoord = Math.asin( Math.sin( e ) * Math.sin( eclLength ) );
        System.out.println( "delta: " + Math.toDegrees( equatorialCoord ) );
        double rekta = Math.atan( ( Math.cos( e ) * Math.sin( eclLength ) ) / Math.cos( eclLength ) );

        double t0 = ( 2453953.5 - 2451545 ) / 36525.;
        double t = getT( date ) / 36525.0;
        System.out.println( "T0: " + t0 );
        double delta_g_h = ( 6.697376 + ( 2400.05134 * t0 ) + ( 1.002738 * t ) ) % 24;
        System.out.println( "delta_g_h: " + delta_g_h );

        double tau = delta_g_h - rekta;
        double latRad = Math.toRadians( latitude );
        return Math.asin( ( cos( latRad ) * cos( Math.toRadians( tau ) ) * cos( equatorialCoord ) )
                          + sin( equatorialCoord ) + sin( latitude ) );

    }

    private static double getSunEclipticalCoordinate( double julianDate ) {
        double middleEclipticalLength = ( 280.46 + ( .9856474 * julianDate ) ) % 360;
        System.out.println( "L: " + middleEclipticalLength );

        double middelAnalomie = Math.toRadians( ( 357.528 + ( .9856003 * julianDate ) ) % 360 );
        System.out.println( "g: " + Math.toDegrees( middelAnalomie ) );
        double eclipticalLenth = middleEclipticalLength + ( 1.915 * sin( middelAnalomie ) )
                                 + ( 0.020 * sin( 2 * middelAnalomie ) );
        return Math.toRadians( eclipticalLenth );
    }

    private static double getEcliptik( double julianDay ) {
        return Math.toRadians( 23.439 - ( 0.0000004 * julianDay ) );
    }

    private static double getJulianDay( GregorianCalendar date ) {

        // wenn Monat > 2 dann Y = Jahr, M = Monat
        // sonst Y = Jahr-1, M = Monat+12
        int year = date.get( YEAR );
        int month = date.get( Calendar.MONTH ) + 1;
        if ( month == 0 ) {
            month = 12;
            year--;
        }
        //
        // D = Tag
        //
        // H = Stunde/24 + Minute/1440 + Sekunde/86.400
        double h = ( date.get( HOUR_OF_DAY ) / 24.0 ) + ( date.get( MINUTE ) / 1440.0 )
                   + ( date.get( SECOND ) / 86400.0 );
        //
        // wenn TT.MM.YYYY >= 15.10.1582
        // dann Gregorianischer Kalender: A = Int(Y/100), B = 2 - A + Int(A/4)
        double a = year / 100.0;
        double b = 2 - a + ( a / 4.0 );

        //
        // wenn TT.MM.YYYY <= 04.10.1582
        // dann Julianischer Kalender: B = 0
        //
        // sonst Fehler: Das Datum zwischen dem 04.10.1582 und dem 15.10.1582 existiert nicht.
        // Auf den 04.10.1582 (Julianischer Kalender) folgte
        // unmittelbar der 15.10.1582 (Gregorianischer Kalender).
        //
        // JD = Int(365,25*(Y+4716)) + Int(30,6001*(M+1)) + D + H + B - 1524,5
        return ( 365.25 * ( year + 4716 ) ) + ( 30.6001 * ( month + 1 ) ) + date.get( Calendar.DAY_OF_MONTH ) + h + b
               - 1524.5;
    }

    /**
     * return the number of days in given year.
     * 
     * @param date
     *            to calculate the number of days from.
     * @return the number of days.
     */
    private static int numberOfDays( GregorianCalendar date ) {
        return date.isLeapYear( date.get( YEAR ) ) ? 366 : 365;
    }

    /**
     * calculates the horizontal angle of the sun depending only on hour and minute!
     * 
     * @param date
     * 
     * @return the horizontal angle
     */
    private static double getHourAngle( GregorianCalendar date ) {
        double d = date.get( HOUR ) + ( date.get( MINUTE ) / 60.0 );
        // Calculate hour angle (in radians): http://squ1.org/wiki/Solar_Position_Calculator
        return Math.toRadians( ( ( d - 12 ) * 15.0 ) );
    }

    /**
     * caluculates for a given date the number of days since the last vernal(spring) equinox. (leap years are
     * considered)
     */
    private static double getDaySinceVernalEquinox( GregorianCalendar date ) {
        // 0 for january
        int currentYear = date.get( YEAR );
        GregorianCalendar calendar = new GregorianCalendar( currentYear, 2, 21 );
        int vEq = calendar.get( DAY_OF_YEAR );
        int doy = date.get( DAY_OF_YEAR );
        if ( doy < vEq ) {
            doy = ( ( numberOfDays( date ) ) - vEq ) + doy;
        }
        return doy - vEq;
    }

    private static double getT( GregorianCalendar date ) {
        int currentYear = date.get( YEAR );
        GregorianCalendar calendar = new GregorianCalendar( currentYear, 2, 21 );
        double jd = getJulianDay( calendar );
        return ( jd - getDaySinceVernalEquinox( date ) ) - 2451545;
    }

    public static void main( String[] args ) {
        GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance();
        // GregorianCalendar cal = new GregorianCalendar( 2006, 7, 6, 6, 0 );
        System.out.println( "direction: " + Vectors3f.asString( getEucledianSunDirection( cal, 50.4 ) ) );
        // System.out.println( Math.toDegrees( getAltitudeFromWiki( cal, 48.1, 7.06 ) ) );
    }
}