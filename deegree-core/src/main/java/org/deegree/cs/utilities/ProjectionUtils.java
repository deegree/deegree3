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

package org.deegree.cs.utilities;

import java.awt.geom.Rectangle2D;

/**
 * The <code>Utils</code> class combines some helpful constants and forms.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */

public class ProjectionUtils {

    // Very handy for setting maximum number of iterations in a for loop.
    private final static int MAX_ITER = 10;

    /**
     * A small epsilon value
     */
    public final static double EPS10 = 1e-10;

    /**
     * An even smaller epsilon value
     */
    public final static double EPS11 = 1e-11;

    /**
     * Cotaining the value 0.5*pi
     */
    public final static double HALFPI = Math.PI * 0.5;

    /**
     * Containing the value 0.25*pi
     */
    public final static double QUARTERPI = Math.PI * 0.25;

    /**
     * Containing the value 2*pi
     */
    public final static double TWOPI = Math.PI * 2.0;

    /**
     * Radians to Degrees (180.0/Math.PI)
     */
    public final static double RTD = 180.0 / Math.PI;

    /**
     * Degrees to Radians (Math.PI/180.0)
     */
    public final static double DTR = Math.PI / 180.0;

    /**
     * The max and min of the projected word map in radians (-Math.PI, -HALFPI, TWOPI, Math.PI)
     */
    public final static Rectangle2D WORLD_BOUNDS_RAD = new Rectangle2D.Double( -Math.PI, -HALFPI, TWOPI, Math.PI );

    /**
     * The max and min of the projected word map in degrees (-180, -90, 360, 180)
     */
    public final static Rectangle2D WORLD_BOUNDS = new Rectangle2D.Double( -180, -90, 360, 180 );

    /**
     * From the proj4 library, to determine small q which is needed to calculate the authalic (equal-areaed) latitude
     * beta, on a sphere having the same surface area as the ellipsoid, relative to the ellipsoid. Snyder (3 -12).
     *
     * @param sinphi
     *            the sine of the angle between the positive z-axis and the line formed between the origin and P.
     * @param e
     *            the eccentricity
     * @return the q value from Snyder (3-12)
     * @deprecated use {@link ProjectionUtils#calcQForAuthalicLatitude(double, double)} instead.
     */
    @Deprecated
    public static double qsfn( double sinphi, double e ) {
        return calcQForAuthalicLatitude( sinphi, e );
    }

    /**
     * From the proj4 library, to determine small q which is needed to calculate the authalic (equal-areaed) latitude
     * beta, on a sphere having the same surface area as the ellipsoid, relative to the ellipsoid. Snyder (3 -12).
     *
     * @param sinphi
     *            the sine of the angle between the positive z-axis and the line formed between the origin and P.
     * @param eccentricity
     *            the eccentricity of the ellipsoid to map the sphere to.
     * @return the q value from Snyder (3-12)
     */
    public static double calcQForAuthalicLatitude( double sinphi, double eccentricity ) {
        if ( eccentricity >= EPS10 ) {
            double eAndSinphi = eccentricity * sinphi;
            double es = eccentricity * eccentricity;
            return ( 1 - es )
                   * ( sinphi / ( 1. - eAndSinphi * eAndSinphi ) - ( .5 / eccentricity )
                                                                   * Math.log( ( 1. - eAndSinphi ) / ( 1. + eAndSinphi ) ) );
        }
        // we have a sphere.
        return ( sinphi + sinphi );
    }

    /**
     * Pre-calculated values for Snyder's formula (3-5)
     */
    private final static double T00 = 0.5;

    private final static double T01 = .20833333333333333333;/* 5/24. */

    private final static double T02 = .08333333333333333333;/* 1/12. */

    private final static double T03 = .03611111111111111111;/* 13/360 */

    private final static double T10 = .14583333333333333333;/* 7/48 */

    private final static double T11 = .12083333333333333333;/* 29/240 */

    private final static double T12 = .07039930555555555555;/* 811/11520 */

    private final static double T20 = .05833333333333333333;/* 7/120 */

    private final static double T21 = .07232142857142857142;/* 81/1120 */

    private final static double T30 = .02653149801587301587;/* 4279/161280 */

    /**
     * Pre-Calculates the values (used for the adams? series) which will be used to calculate the phi value of an
     * inverse projection. Snyder (3-5).
     *
     * @param eccentricitySquared
     *            the squared eccentricity from the ellipsoid to calculate the theta for.
     * @return the precalculated values.
     */
    public static double[] preCalcedThetaSeries( double eccentricitySquared ) {
        double[] precalculatedSerie = new double[4];
        precalculatedSerie[0] = eccentricitySquared * T00;

        // eccentricity^4
        double tmp = eccentricitySquared * eccentricitySquared;
        precalculatedSerie[0] += tmp * T01;
        precalculatedSerie[1] = tmp * T10;

        // eccentricity^6
        tmp *= eccentricitySquared;
        precalculatedSerie[0] += tmp * T02;
        precalculatedSerie[1] += tmp * T11;
        precalculatedSerie[2] = tmp * T20;

        // eccentricity^8
        tmp *= eccentricitySquared;
        precalculatedSerie[0] += tmp * T03;
        precalculatedSerie[1] += tmp * T12;
        precalculatedSerie[2] += tmp * T21;
        precalculatedSerie[3] = tmp * T30;

        return precalculatedSerie;
    }

    /**
     * Gets Phi from the given conformal latitude chi and the precalculated values (gotten from
     * {@link ProjectionUtils#preCalcedThetaSeries(double)} ) of the adams? serie. From Snyder (3-5).
     *
     * @param chi
     *            the conformal latitude
     * @param APA
     *            the precalculated values from the serie gotten from
     *            {@link ProjectionUtils#preCalcedThetaSeries(double)}.
     * @return the Phi as a polarcoordinate on the ellipsoid or chi if the length of APA != 4.
     */
    public static double calcPhiFromConformalLatitude( double chi, double[] APA ) {
        if ( APA.length != 4 ) {
            return chi;
        }
        double tmp = chi + chi;
        return ( chi + APA[0] * Math.sin( tmp ) + APA[1] * Math.sin( tmp + tmp ) + APA[2] * Math.sin( tmp + tmp + tmp ) + APA[3]
                                                                                                                          * Math.sin( tmp
                                                                                                                                      + tmp
                                                                                                                                      + tmp
                                                                                                                                      + tmp ) );
    }

    /**
     * P[0][0-2] = 1/3, 31/180, 517/5040, P[1][0-2] = 23/360, 251/3780 P[2][0] = 761/45360
     */
    private final static double P00 = .33333333333333333333; /* 1/3 */

    private final static double P01 = .17222222222222222222; /* 31 / 180 */

    private final static double P02 = .10257936507936507936; /* 517 / 5040 */

    private final static double P10 = .06388888888888888888; /* 23/360 */

    private final static double P11 = .06640211640211640211; /* 251/3780 */

    private final static double P20 = .01641501294219154443; /* 761/45360 */

    /**
     * Pre-Calculates the values (used for the adams? series) which will be used to calculate the authalic latitude.
     * Snyder (3-18).
     *
     * @param eccentricitySquared
     *            the squared eccentricity from the ellipsoid to calculate the authalic latitude for.
     * @return the precalculated values.
     * @deprecated use {@link ProjectionUtils#getAuthalicLatitudeSeriesValues(double)} instead.;
     */
    @Deprecated
    public static double[] authset( double eccentricitySquared ) {
        return getAuthalicLatitudeSeriesValues( eccentricitySquared );
    }

    /**
     * Pre-Calculates the values (used for the adams? series) which will be used to calculate the authalic latitude.
     * Snyder (3-18).
     *
     * @param eccentricitySquared
     *            the squared eccentricity from the ellipsoid to calculate the authalic latitude for.
     * @return the precalculated values [0] = e^2/3 + e^4*(31/180) + e^6*(517/5040), [1]= e^4*(23/360) + e^6*(251/3780)
     *         and [2] = e^6*(761/45360).
     */
    public static double[] getAuthalicLatitudeSeriesValues( double eccentricitySquared ) {
        double[] precalculatedSerie = new double[3];
        precalculatedSerie[0] = eccentricitySquared * P00;
        double t = eccentricitySquared * eccentricitySquared;
        precalculatedSerie[0] += t * P01;
        precalculatedSerie[1] = t * P10;
        t *= eccentricitySquared;
        precalculatedSerie[0] += t * P02;
        precalculatedSerie[1] += t * P11;
        precalculatedSerie[2] = t * P20;
        return precalculatedSerie;
    }

    /**
     * Gets phi from the authalic latitude beta and the precalculated values of the adams? serie. From Snyder (3-18).
     *
     * @param beta
     *            authalic latitude.
     * @param APA
     *            the precalculated values from the series gotten from
     *            {@link ProjectionUtils#getAuthalicLatitudeSeriesValues(double)}.
     * @return the phi on the ellipsoid.
     * @deprecated use {@link ProjectionUtils#calcPhiFromAuthalicLatitude(double, double[])} instead.;
     */
    @Deprecated
    public static double authlat( double beta, double[] APA ) {
        return calcPhiFromAuthalicLatitude( beta, APA );
    }

    /**
     * Gets phi from the authalic latitude beta and the precalculated values of the adams? serie. From Snyder (3-18).
     *
     * @param beta
     *            authalic latitude.
     * @param APA
     *            the precalculated values from the serie gotten from
     *            {@link ProjectionUtils#getAuthalicLatitudeSeriesValues(double)}.
     * @return the phi on the ellipsoid.
     */
    public static double calcPhiFromAuthalicLatitude( double beta, double[] APA ) {
        double t = beta + beta;
        return ( beta + APA[0] * Math.sin( t ) + APA[1] * Math.sin( t + t ) + APA[2] * Math.sin( t + t + t ) );
    }

    /**
     * Calcs the length of a vector given by two points x and y
     *
     * @param dx
     *            of the vector
     * @param dy
     *            of the vector
     * @return the length
     */
    public static double length( double dx, double dy ) {
        return Math.hypot( dx, dy );
    }

    /**
     * This method calculates the innerpart of the conformal latitude's definition (Snyder p.15 3-1). This formula is
     * almost equal to the calculation of the half colatitude from the conformal latitude (Snyder p.108 15-9). They only
     * differ a sign in the first term.
     *
     * @param phi
     *            to calculate the conformal latitude from
     * @param sinphi
     *            the sinus of the phi.
     * @param eccentricity
     *            of the ellipsoid to which the phi should be made conformal to.
     * @return the value of the innerpart of the conformal latitude formula. i.e. tan( pi/4 <b>+</b> phi/2)<b>*</b>[(1-e*sin(phi))/1+e*sin(phi))]^e/2.
     */
    public static double conformalLatitudeInnerPart( double phi, double sinphi, double eccentricity ) {
        sinphi *= eccentricity;
        return ( Math.tan( .5 * ( HALFPI + phi ) ) ) * Math.pow( ( 1. - sinphi ) / ( 1. + sinphi ), .5 * eccentricity );
    }

    /**
     * This method calculates the innerpart of the conformal latitude's definition (Snyder p.15 3-1). This formula is
     * almost equal to the calculation of the half colatitude from the conformal latitude (Snyder p.108 15-9). They only
     * differ a sign in the first term.
     *
     * @param phi
     *            to calculate the conformal latitude from
     * @param sinphi
     *            the sinus of the phi.
     * @param eccentricity
     *            of the ellipsoid to which the phi should be made conformal to.
     * @return the value of the innerpart of the conformal latitude formula. i.e. tan( pi/4 <b>+</b>
     *         phi/2)*[(1-e*sin(phi))/1+e*sin(phi))]^e/2.
     * @deprecated Use {@link #conformalLatitudeInnerPart(double,double,double)} instead
     */
    @Deprecated
    public static double ssfn( double phi, double sinphi, double eccentricity ) {
        return conformalLatitudeInnerPart( phi, sinphi, eccentricity );
    }

    /**
     * This method calculates the tangens of the half colatitude from the conformal latitude (Snyder p.108 15-9).
     *
     * @param phi
     *            to calculate the half of the co latitude of the conformal latitude from
     * @param sinphi
     *            the sinus of the phi.
     * @param eccentricity
     *            of the ellipsoid to which the phi should be made conformal to.
     * @return the value of the tangens of half of the conformal latitude formula. i.e. tan( pi/4 <b>-</b> phi/2)<b>/</b>[(1-e*sin(phi))/1+e*sin(phi))]^e/2.
     */
    public static double tanHalfCoLatitude( double phi, double sinphi, double eccentricity ) {
        sinphi *= eccentricity;
        return ( Math.tan( .5 * ( HALFPI - phi ) ) ) / Math.pow( ( 1. - sinphi ) / ( 1. + sinphi ), .5 * eccentricity );
    }

    /**
     * This method calculates the tangens of the half colatitude from the conformal latitude (Snyder p.108 15-9). This
     * formula is almost equal to the calculation of the innerpart of the conformal latitude's definition (Snyder p.15
     * 3-1). They only differ a sign in the first term.
     *
     * @param phi
     *            to calculate the half of the co latitude of the conformal latitude from
     * @param sinphi
     *            the sinus of the phi.
     * @param eccentricity
     *            of the ellipsoid to which the phi should be made conformal to.
     * @return the value of the innerpart of the conformal latitude formula (given sign + or -). i.e. tan( pi/4 (+-)
     *         phi/2)*[(1-e*sin(phi))/1+e*sin(phi))]^e/2.
     * @deprecated Use {@link #tanHalfCoLatitude(double,double,double)} instead
     */
    @Deprecated
    public static double tsfn( double phi, double sinphi, double eccentricity ) {
        return tanHalfCoLatitude( phi, sinphi, eccentricity );
    }

    /**
     * This method can be used to calculate the value of a variable called 'm' by Snyder (Snyder p.101 14-15).
     *
     * @param sinphi
     *            the sinus of the phi
     * @param cosphi
     *            the cosinus of the phi
     * @param eccentricitySquared
     *            the value eccentricity * eccentricity.
     * @return cos( phi) / Math.sqrt( 1 - eccentricity*eccentricity*sin(phi)*sin(phi) ).
     * @deprecated Use {@link #calcMFromSnyder(double,double,double)} instead
     */
    @Deprecated
    public static double msfn( double sinphi, double cosphi, double eccentricitySquared ) {
        return calcMFromSnyder( sinphi, cosphi, eccentricitySquared );
    }

    /**
     * This method can be used to calculate the value of a variable called 'm' by Snyder (Snyder p.101 14-15).
     *
     * @param sinphi
     *            the sinus of the phi
     * @param cosphi
     *            the cosinus of the phi
     * @param eccentricitySquared
     *            the value eccentricity * eccentricity.
     * @return cos( phi) / Math.sqrt( 1 - eccentricity*eccentricity*sin(phi)*sin(phi) ).
     */
    public static double calcMFromSnyder( double sinphi, double cosphi, double eccentricitySquared ) {
        return cosphi / Math.sqrt( 1.0 - eccentricitySquared * sinphi * sinphi );
    }

    /**
     * Copied these value from proj4, I think they are reformed to fit some rule... but I don't know which rule that is
     * :-(
     */
    private final static double C00 = 1; /* 1 :-) */

    private final static double C02 = .25; /* 1/4 */

    private final static double C04 = .046875;/* 3/64 */

    private final static double C06 = .01953125;/* 5/256 */

    private final static double C08 = .01068115234375;/* 175 / 16384 */

    private final static double C22 = .75;

    private final static double C44 = .46875;

    private final static double C46 = .01302083333333333333;

    private final static double C48 = .00712076822916666666;

    private final static double C66 = .36458333333333333333;

    private final static double C68 = .00569661458333333333;

    private final static double C88 = .3076171875;

    /**
     * Pre Calculates the values for the series to calculate for a given ellipsoid with given eccentricity the distance
     * along the meridian from the equator to a given latitude
     * {@link #getDistanceAlongMeridian(double, double, double, double[])}.
     *
     * @param es
     *            the squared eccentricity of the underlying ellipsoid.
     * @return the precalculated values for given ellipsoid.
     * @deprecated Use {@link #getRectifiyingLatitudeValues(double)} instead
     */
    @Deprecated
    public static double[] enfn( double es ) {
        return getRectifiyingLatitudeValues( es );
    }

    /**
     * Pre Calculates the values for the series to calculate for a given ellipsoid with given eccentricity the distance
     * along the meridian from the equator to a given latitude
     * {@link #getDistanceAlongMeridian(double, double, double, double[])}.
     *
     * @param es
     *            the squared eccentricity of the underlying ellipsoid.
     * @return the precalculated values for given ellipsoid.
     */
    public static double[] getRectifiyingLatitudeValues( double es ) {
        double[] en = new double[5];
        en[0] = C00 - es * ( C02 + es * ( C04 + es * ( C06 + es * C08 ) ) );
        en[1] = es * ( C22 - es * ( C04 + es * ( C06 + es * C08 ) ) );

        double t = es * es;
        en[2] = t * ( C44 - es * ( C46 + es * C48 ) );

        t *= es;
        en[3] = t * ( C66 - es * C68 );

        en[4] = t * es * C88;

        return en;
    }

    /**
     * This method calcs for a a given ellispoid the distance along the meridian from the equator to latitude phi Snyder
     * (p.17 3-21). It is used to calculate the rectifying latitude <i>mu</i>.
     *
     * @param phi
     *            the lattitude of the point in radians
     * @param sphi
     *            the sinus of the latitude
     * @param cphi
     *            the cosinus of the latitude
     * @param en
     *            an array (of length 5) containing the precalculate values for this ellipsoid gotten from
     *            {@link #getRectifiyingLatitudeValues(double)}.
     * @return the distance along the meridian from the equator to latitude phi.
     * @deprecated Use {@link #getDistanceAlongMeridian(double,double,double,double[])} instead
     */
    @Deprecated
    public static double mlfn( double phi, double sphi, double cphi, double[] en ) {
        return getDistanceAlongMeridian( phi, sphi, cphi, en );
    }

    /**
     * This method calcs the distance along the meridian from the equator to latitude phi for a a given ellispoid Snyder
     * (p.17 3-21). It is used to calculate the rectifying latitude <i>mu</i>.
     *
     * @param phi
     *            the lattitude of the point in radians
     * @param sphi
     *            the sinus of the latitude
     * @param cphi
     *            the cosinus of the latitude
     * @param en
     *            an array (of length 5) containing the precalculate values for this ellipsoid gotten from
     *            {@link #getRectifiyingLatitudeValues(double)}.
     * @return the distance along the meridian from the equator to latitude phi.
     */
    public static double getDistanceAlongMeridian( double phi, double sphi, double cphi, double[] en ) {
        cphi *= sphi;
        sphi *= sphi;
        return ( en[0] * phi ) - ( cphi * ( en[1] + sphi * ( en[2] + sphi * ( en[3] + sphi * en[4] ) ) ) );
    }

    /**
     * This method calcs lattitude phi from a given distance along the meridian to the equator for a a given ellispoid
     * and is therefore the inverse of the {@link #getDistanceAlongMeridian(double, double, double, double[])}. Phi is
     * determined to EPS (1e-11) radians, which is about 1e-6 seconds.
     *
     * @param initialValue
     *            to calculate phi from, a good starting value is using the (distance along the meridian / y*scale) e.g.
     *            the scaled y value on the meridian.
     * @param squaredEccentricity
     *            the squared eccentricity of the ellipsoid.
     * @param en
     *            an array (of length 5) containing the precalculate values for this ellipsoid gotten from
     *            {@link #getRectifiyingLatitudeValues(double)}.
     * @return the lattitude phi.
     * @deprecated Use {@link #calcPhiFromMeridianDistance(double,double,double[])} instead
     */
    @Deprecated
    public static double inv_mlfn( double initialValue, double squaredEccentricity, double[] en ) {
        return calcPhiFromMeridianDistance( initialValue, squaredEccentricity, en );
    }

    /**
     * This method calcs lattitude phi from a given distance along the meridian to the equator for a a given ellispoid
     * and is therefore the inverse of the {@link #getDistanceAlongMeridian(double, double, double, double[])}. Phi is
     * determined to EPS (1e-11) radians, which is about 1e-6 seconds.
     *
     * @param initialValue
     *            to calculate phi from, a good starting value is using the (distance along the meridian / y*scale) e.g.
     *            the scaled y value on the meridian.
     * @param squaredEccentricity
     *            the squared eccentricity of the ellipsoid.
     * @param en
     *            an array (of length 5) containing the precalculate values for this ellipsoid gotten from
     *            {@link #getRectifiyingLatitudeValues(double)}.
     * @return the lattitude phi or the best approximated value if no suitable convergence was found.
     */
    public static double calcPhiFromMeridianDistance( double initialValue, double squaredEccentricity, double[] en ) {
        double k = 1. / ( 1. - squaredEccentricity );
        double phi = initialValue;
        /* (from proj4: ->) rarely goes over 2 iterations */
        for ( int i = MAX_ITER; i != 0; i-- ) {
            double s = Math.sin( phi );
            double t = 1. - squaredEccentricity * s * s;
            t = ( getDistanceAlongMeridian( phi, s, Math.cos( phi ), en ) - initialValue ) * ( t * Math.sqrt( t ) ) * k;
            phi -= t;
            if ( Math.abs( t ) < EPS11 ) {
                return phi;
            }
        }
        return phi;
    }

    /**
     * A helper method, which returns the acos from value or if value < -1 pi or value>1 0.
     *
     * @param value
     *            (in radians) from which the acos must be calculated
     * @return the acos from value or if value < -1 pi or if value > 1 0.
     */
    public static double acosScaled( double value ) {
        return ( value < -1 ) ? Math.PI : ( value > 1 ) ? 0 : Math.acos( value );
    }

    /**
     * A helper method, which returns the asin from value or if value < -1 (-pi/2) or value>1 (pi/2).
     *
     * @param value
     *            (in radians) from which the asin must be calculated
     * @return the asin from value or if value < -1 (-pi/2) or value>1 (pi/2).
     */
    public static double asinScaled( double value ) {
        return ( value < -1 ) ? -HALFPI : ( value > 1 ) ? HALFPI : Math.asin( value );
    }

    /**
     * A helper method modulos (pi)the given angle (in radians) until the result fits betwee -HALFPI and HALF_PI.
     *
     * @param angle
     *            in radians
     * @return the angle adjusted to -pi/2 + pi/2 or 0 if the angle is NaN or Infinite.
     */
    public static double normalizeLatitude( double angle ) {
        if ( Double.isInfinite( angle ) || Double.isNaN( angle ) ) {
            return 0;
        }
        while ( angle > HALFPI ) {
            angle -= Math.PI;
        }
        while ( angle < -HALFPI ) {
            angle += Math.PI;
        }
        return angle;
    }

    /**
     * A helper method modulos (2*pi)the given angle (in radians) until the result fits betwee -PI and PI.
     *
     * @param angle
     *            to be normalized
     * @return the angle adjusted to -2*pi + pi*2 or 0 if the angle is NaN or Infinite.
     */
    public static double normalizeLongitude( double angle ) {
        if ( Double.isInfinite( angle ) || Double.isNaN( angle ) ) {
            return 0;
        }
        while ( angle > Math.PI ) {
            angle -= TWOPI;
        }
        while ( angle < -Math.PI ) {
            angle += TWOPI;
        }
        return angle;
    }

    /**
     * Converts a Deegree.MinSec value into it's radian equivalent. <code>
     * for example 13.120637 dms -> 13.201769444444446° -> 0.23041434389473822 rd
     * </code>
     *
     * @param inCoord
     *            to be converted to radians.
     * @return the radian equivalent of the inCoord.
     */
    public static double decMinSecToRadians( double inCoord ) {
        // get decimal minutes
        double remainder = getRemainder( inCoord ) * 100;
        // add the decimal minutes to the decimal seconds
        remainder = ( Math.floor( remainder ) * 60 ) + ( getRemainder( remainder ) * 100 );
        return ( Math.floor( inCoord ) + ( remainder / 3600 ) ) * DTR;
    }

    /**
     * Converts a radian to its Deegree.MinSec equivalent.<code>
     * For example 0.23041434389473822 rd -> 13.201769444444446° -> 13.120637 dms
     * </code>
     *
     * @param inCoord
     *            to be converted to degrees.minsec
     * @return the radian equivalent of the inCoord.
     */
    public static double radiansToDecMinSec( double inCoord ) {

        double degrees = RTD * inCoord;
        double remainder = getRemainder( degrees );
        return Math.floor( degrees ) + ( Math.floor( remainder * 60 ) / 100 )
               + ( ( remainder * .36 ) - ( Math.floor( remainder * 60 ) * .006 ) );
    }

    /**
     * Retrieve the remainder of a given double value, e.g. value - Math.floor( value ).
     *
     * @param value
     *            to get the remainder from.
     *
     * @return the remainder of the given value.
     */
    private static double getRemainder( double value ) {
        return value - Math.floor( value );
    }
}
