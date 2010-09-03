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

package org.deegree.cs.projections.azimuthal;

import static org.deegree.cs.utilities.ProjectionUtils.EPS10;
import static org.deegree.cs.utilities.ProjectionUtils.EPS11;
import static org.deegree.cs.utilities.ProjectionUtils.HALFPI;
import static org.deegree.cs.utilities.ProjectionUtils.QUARTERPI;
import static org.deegree.cs.utilities.ProjectionUtils.calcPhiFromAuthalicLatitude;
import static org.deegree.cs.utilities.ProjectionUtils.calcQForAuthalicLatitude;
import static org.deegree.cs.utilities.ProjectionUtils.getAuthalicLatitudeSeriesValues;
import static org.deegree.cs.utilities.ProjectionUtils.length;

import javax.vecmath.Point2d;

import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.EPSGCode;
import org.deegree.cs.components.Unit;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.exceptions.ProjectionException;
import org.deegree.cs.projections.Projection;

/**
 * The <code>LambertAzimuthalEqualArea</code> projection has following properties (From J.S. Snyder, Map Projections a
 * Working Manual p. 182):
 * <ul>
 * <li>Azimuthal</li>
 * <li>Equal-Area</li>
 * <li>All meridians in the polar aspect, the central meridian in other aspects, and the Equator in the equatorial
 * aspect are straight lines</li>
 * <li>The outer meridian of a hemisphere in the equatorial aspect (for the sphere) and the parallels in the polar
 * aspect (sphere or ellipsoid) are circles.</li>
 * <li>All other meridians and the parallels are complex curves</li>
 * <li>Not a perspective projection</li>
 * <li>Scale decreases radially as the distance increases from the center, the only point without distortion</li>
 * <li>Directions from the center are true for the sphere and the polar ellipsoidal forms.</li>
 * <li>Point opposite the center is shown as a circle surrounding the map (for the sphere).</li>
 * <li>Used for maps of continents and hemispheres</li>
 * <li>presented by lambert in 1772</li>
 * </ul>
 * 
 * <p>
 * The difference to orthographic and stereographic projection, comes from the spacing between the parallels. The space
 * decreases with increasing distance from the pole. The opposite pole not visible on either the orthographic or
 * stereographic may be shown on the lambert as a large circle surrounding the map, almost half again as far as the
 * equator from the center. Normally the projectction is not shown beyond one hemisphere (or beyond the equator in the
 * polar aspect).
 * </p>
 * 
 * <p>
 * It is known to be used by following epsg transformations:
 * <ul>
 * <li>EPSG:3035</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */

public class LambertAzimuthalEqualArea extends AzimuthalProjection {

    private double sinb1;

    private double cosb1;

    /**
     * qp is q (needed for authalicLatitude Snyder 3-12) evaluated for a phi of 90°.
     */
    private double qp;

    /**
     * Will hold the value D (A slide adjustment for the standardpoint, to achieve a correct scale in all directions at
     * the center of the projection) calculated by Snyder (24-20).
     */
    private double dd;

    /**
     * Radius for the sphere having the same surface area as the ellipsoid. Calculated with Snyder (3-13).
     */
    private double rq;

    /**
     * precalculated series values to calculate the authalic latitude value from.
     */
    private double[] apa;

    /**
     * A variable to hold a precalculated value for the x parameter of the oblique projection
     */
    private double xMultiplyForward;

    /**
     * A variable to hold a precalculated value for the y parameter of the oblique or equatorial projection
     */
    private double yMultiplyForward;

    /**
     * @param geographicCRS
     * @param falseNorthing
     * @param falseEasting
     * @param naturalOrigin
     * @param units
     * @param scale
     * @param id
     *            an identifiable instance containing information about this projection
     */
    public LambertAzimuthalEqualArea( GeographicCRS geographicCRS, double falseNorthing, double falseEasting,
                                      Point2d naturalOrigin, Unit units, double scale, CRSIdentifiable id ) {
        super( geographicCRS, falseNorthing, falseEasting, naturalOrigin, units, scale, false/* not conformal */,
               true/* equals-area */, id );
        if ( !isSpherical() ) {
            // sin(rad(90)) = 1;
            qp = calcQForAuthalicLatitude( 1., getEccentricity() );
            rq = getSemiMajorAxis() * Math.sqrt( .5 * qp );// Snyder (3-13)
            apa = getAuthalicLatitudeSeriesValues( getSquaredEccentricity() );

            switch ( getMode() ) {
            case NORTH_POLE:
            case SOUTH_POLE:
                xMultiplyForward = getSemiMajorAxis();
                yMultiplyForward = ( getMode() == NORTH_POLE ) ? -getSemiMajorAxis() : getSemiMajorAxis();
                dd = 1.;
                break;
            case EQUATOR:
                dd = 1. / ( rq );
                xMultiplyForward = getSemiMajorAxis();
                yMultiplyForward = getSemiMajorAxis() * .5 * qp;
                break;
            case OBLIQUE:
                double sinphi = getSinphi0();
                // arcsin( q/ qp) = beta . Snyder (3-11)
                sinb1 = calcQForAuthalicLatitude( sinphi, getEccentricity() ) / qp;
                // sin*sin + cos*cos = 1
                cosb1 = Math.sqrt( 1. - sinb1 * sinb1 );
                // (24-20) D = a*m_1 / (Rq*cos(beta_1) )
                double m_1 = getCosphi0() / Math.sqrt( 1. - getSquaredEccentricity() * sinphi * sinphi );
                dd = getSemiMajorAxis() * m_1 / ( rq * cosb1 );
                xMultiplyForward = rq * dd;
                yMultiplyForward = rq / dd;
                break;
            }
        }
    }

    /**
     * @param geographicCRS
     * @param falseNorthing
     * @param falseEasting
     * @param naturalOrigin
     * @param units
     * @param scale
     */
    public LambertAzimuthalEqualArea( GeographicCRS geographicCRS, double falseNorthing, double falseEasting,
                                      Point2d naturalOrigin, Unit units, double scale ) {
        this( geographicCRS, falseNorthing, falseEasting, naturalOrigin, units, scale,
              new CRSIdentifiable( new EPSGCode( 9820 ) ) );
    }

    /**
     * @param geographicCRS
     * @param falseNorthing
     * @param falseEasting
     * @param naturalOrigin
     * @param units
     * @param id
     *            an identifiable instance containing information about this projection
     */
    public LambertAzimuthalEqualArea( GeographicCRS geographicCRS, double falseNorthing, double falseEasting,
                                      Point2d naturalOrigin, Unit units, CRSIdentifiable id ) {
        this( geographicCRS, falseNorthing, falseEasting, naturalOrigin, units, 1, id );
    }

    /**
     * @param geographicCRS
     * @param falseNorthing
     * @param falseEasting
     * @param naturalOrigin
     * @param units
     */
    public LambertAzimuthalEqualArea( GeographicCRS geographicCRS, double falseNorthing, double falseEasting,
                                      Point2d naturalOrigin, Unit units ) {
        this( geographicCRS, falseNorthing, falseEasting, naturalOrigin, units, 1 );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.cs.projections.Projection#doInverseProjection(double, double)
     */
    @Override
    public Point2d doInverseProjection( double x, double y )
                            throws ProjectionException {
        Point2d lp = new Point2d( 0, 0 );
        x -= getFalseEasting();
        y -= getFalseNorthing();
        // Snyder (20-18)
        double rho = length( x, y );
        if ( isSpherical() ) {
            double cosC = 0;
            double sinC = 0;

            // Snyder (20-18)
            if ( rho * .5 > 1. ) {
                throw new ProjectionException( "The Y value is beyond the maximum mappable area" );
            }
            // lp.y = 2. * Math.asin( rho*.5 );
            // Snyder (24-16)
            double c = 2 * Math.asin( rho * 0.5 );

            if ( getMode() == OBLIQUE || getMode() == EQUATOR ) {
                sinC = Math.sin( c );
                cosC = Math.cos( c );
            }
            switch ( getMode() ) {
            case OBLIQUE:
                // the theta from Snyder (20-14)
                lp.y = ( rho <= EPS10 ) ? getProjectionLatitude() : Math.asin( cosC * getSinphi0()
                                                                               + ( ( y * sinC * getCosphi0() ) / rho ) );

                // For the calculation of the Lamda (Snyder[20-15]) proj4 obviously uses the atan2 method, I don't know
                // if this is correct.

                // x = the radial coordinate (usually denoted as r) it denotes the point's distance from a central point
                // known as the pole (equivalent to the origin in the Cartesian system)
                x *= sinC * getCosphi0();

                // y = The angular coordinate (also known as the polar angle or the azimuth angle, and usually denoted
                // by θ or t) denotes the positive or anticlockwise (counterclockwise) angle required to reach the point
                // from the 0° ray or polar axis (which is equivalent to the positive x-axis in the Cartesian coordinate
                // plane).
                y = ( cosC - Math.sin( lp.y ) * getSinphi0() ) * rho;
                /**
                 * it could be something like this too.
                 */
                // lp.x = Math.atan( ( x * sinC ) / ( ( rho * getCosphi0() * cosC ) - ( y * sinC * getSinphi0() ) ) );
                break;
            case EQUATOR:
                lp.y = ( rho <= EPS10 ) ? 0. : Math.asin( y * sinC / rho );
                x *= sinC;
                y = cosC * rho;
                break;
            case NORTH_POLE:
                y = -y;
                // cos(90 or -90) = 0, therefore the last term from (20-14) is null
                // sin( 90 or -90 = + or - 1 what is
                // left is asin( (-or+) cosC )
                // from cos(c) = sin( HALFPI - c ) follows
                lp.y = HALFPI - c;
                break;
            case SOUTH_POLE:
                lp.y = c - HALFPI;
                break;
            }
            // calculation of the Lamda (Snyder[20-15]) is this correct???
            lp.x = ( ( y == 0. && ( getMode() == EQUATOR || getMode() == OBLIQUE ) ) ? 0. : Math.atan2( x, y ) );
        } else {
            double q = 0;
            double arcSinusBeta = 0;
            switch ( getMode() ) {
            case EQUATOR:
            case OBLIQUE:
                // Snyder (p.189 24-28)
                x /= dd;
                y *= dd;
                rho = length( x, y );
                if ( rho < EPS10 ) {
                    return new Point2d( 0, getProjectionLatitude() );
                }
                // Snyder (p.189 24-29).
                double ce = 2. * Math.asin( ( .5 * rho ) / rq );
                double cosinusCe = Math.cos( ce );
                double sinusCe = Math.sin( ce );

                x *= sinusCe;
                if ( getMode() == OBLIQUE ) {
                    // Snyder (p.189 24-30)
                    arcSinusBeta = cosinusCe * sinb1 + ( ( y * sinusCe * cosb1 ) / rho );
                    // Snyder (p.188 24-27)
                    q = qp * ( arcSinusBeta );
                    // calculate the angular coordinate to be used in the atan2 method.
                    y = rho * cosb1 * cosinusCe - y * sinb1 * sinusCe;
                } else {
                    arcSinusBeta = y * sinusCe / rho;
                    q = qp * ( arcSinusBeta );
                    y = rho * cosinusCe;
                }
                break;
            case NORTH_POLE:
                y = -y;
            case SOUTH_POLE:
                // will be used to calc q.
                q = ( x * x + y * y );
                if ( Math.abs( q ) < EPS10 ) {
                    return new Point2d( 0, getProjectionLatitude() );
                }
                // Simplified Snyder (p.190 24-32), because sin(phi) = 1, the qp can be used to calc arcSinusBeta.
                // xMultiplyForward = getSemiMajorAxis();
                double aSquare = xMultiplyForward * xMultiplyForward;
                arcSinusBeta = 1. - ( q / ( aSquare * qp ) );
                if ( getMode() == SOUTH_POLE ) {
                    arcSinusBeta = -arcSinusBeta;
                }
                break;
            }
            lp.x = Math.atan2( x, y );
            lp.y = calcPhiFromAuthalicLatitude( Math.asin( arcSinusBeta ), apa );
        }
        lp.x += getProjectionLongitude();
        return lp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.cs.projections.Projection#doProjection(double, double)
     */
    @Override
    public Point2d doProjection( double lambda, double phi )
                            throws ProjectionException {
        Point2d result = new Point2d( 0, 0 );
        lambda -= getProjectionLongitude();
        double sinphi = Math.sin( phi );
        double cosLamda = Math.cos( lambda );
        double sinLambda = Math.sin( lambda );

        if ( isSpherical() ) {
            double cosphi = Math.cos( phi );
            double kAccent = 0;
            switch ( getMode() ) {
            case OBLIQUE:
                // Calculation of k' Snyder (24-2)
                kAccent = 1. + ( getSinphi0() * sinphi ) + ( getCosphi0() * cosphi * cosLamda );
                if ( Math.abs( kAccent ) <= EPS11 ) {
                    throw new ProjectionException(
                                                   "The scalefactor (k') in the perpendicular direction to the radius from the center of the map equals: "
                                                                           + kAccent
                                                                           + " this will cause a divide by zero." );
                }
                kAccent = Math.sqrt( 2. / kAccent );

                result.x = kAccent * cosphi * sinLambda;
                result.y = kAccent * ( ( getCosphi0() * sinphi ) - ( getSinphi0() * cosphi * cosLamda ) );
                break;
            case EQUATOR:
                kAccent = 1. + cosphi * cosLamda;
                if ( kAccent <= EPS11 ) {
                    throw new ProjectionException(
                                                   "The scalefactor (k') in the perpendicular direction to the radius from the center of the map equals: "
                                                                           + kAccent
                                                                           + " this will cause a divide by zero." );
                }
                kAccent = Math.sqrt( 2. / kAccent );
                result.x = kAccent * cosphi * sinLambda;
                result.y = kAccent * sinphi;
                break;
            case NORTH_POLE:
                cosLamda = -cosLamda;
            case SOUTH_POLE:
                if ( Math.abs( phi + getProjectionLatitude() ) < EPS11 ) {
                    throw new ProjectionException( "The requested phi: " + phi + " lies on the singularity ("
                                                   + ( ( getMode() == SOUTH_POLE ) ? "South-Pole" : "North-Pole" )
                                                   + ") of this projection's mappable area." );
                }
                result.y = QUARTERPI - ( phi * .5 );
                result.y = 2. * ( getMode() == SOUTH_POLE ? Math.cos( result.y ) : Math.sin( result.y ) );
                result.x = result.y * sinLambda;
                result.y *= cosLamda;
                break;
            }
            // the radius is stil to be multiplied.
            result.x *= getSemiMajorAxis();
            result.y *= getSemiMajorAxis();
        } else {
            double sinb = 0;
            double cosb = 0;

            // The big B of snyder (24-19), but will also be used as a place holder for the none oblique calculations.
            double bigB = 0;

            double q = calcQForAuthalicLatitude( sinphi, getEccentricity() );
            if ( getMode() == OBLIQUE || getMode() == EQUATOR ) {
                // snyder ( 3-11 )
                sinb = q / qp;
                cosb = Math.sqrt( 1. - sinb * sinb );
            }
            switch ( getMode() ) {
            case OBLIQUE:
                // Snyder (24-19)
                bigB = 1. + sinb1 * sinb + cosb1 * cosb * cosLamda;
                break;
            case EQUATOR:
                // dd, sin as well as cos(beta1) fall out Snyder (24-21).
                bigB = 1. + cosb * cosLamda;
                break;
            case NORTH_POLE:
                bigB = HALFPI + phi;
                q = qp - q;
                break;
            case SOUTH_POLE:
                bigB = phi - HALFPI;
                q = qp + q;
                break;
            }
            /**
             * Test to see if the projection point is 0, -> divide by zero.
             */
            if ( Math.abs( bigB ) < EPS10 ) {
                throw new ProjectionException(
                                               "The projectionPoint B from the authalic latitude beta: "
                                                                       + ( Math.toDegrees( Math.asin( sinb ) ) )
                                                                       + "° lies on the singularity of this projection's mappable area, resulting in a divide by zero." );
            }
            switch ( getMode() ) {
            case OBLIQUE:
                bigB = Math.sqrt( 2 / bigB );
                result.x = xMultiplyForward * bigB * cosb * sinLambda;
                result.y = yMultiplyForward * bigB * ( cosb1 * sinb - sinb1 * cosb * cosLamda );
                break;
            case EQUATOR:
                bigB = Math.sqrt( 2 / bigB );
                // dd, sin as well as cosbeta1 fall out Snyder (24-21), xMulti = getSemimajorAxis(), yMulti =
                // getSemiMajorAxsis() * 0.5 * qp
                result.x = xMultiplyForward * bigB * cosb * sinLambda;
                result.y = yMultiplyForward * bigB * sinb;
                break;
            case NORTH_POLE:
            case SOUTH_POLE:
                if ( q >= 0. ) {
                    bigB = Math.sqrt( q );
                    // xMulti = yMulti = getSemimajorAxis()
                    result.x = xMultiplyForward * bigB * sinLambda;
                    // if NORTH, yMultiplyForward = -getSemiMajorAxis.
                    result.y = yMultiplyForward * cosLamda * bigB;
                } else {
                    result.x = 0;
                    result.y = 0;
                }
                break;
            }
        }
        result.x += getFalseEasting();
        result.y += getFalseNorthing();
        return result;
    }

    @Override
    public String getImplementationName() {
        return "lambertAzimuthalEqualArea";
    }

    @Override
    public Projection clone( GeographicCRS newCRS ) {
        return new LambertAzimuthalEqualArea( newCRS, getFalseNorthing(), getFalseEasting(), getNaturalOrigin(),
                                              getUnits(), getScale(), new CRSIdentifiable( getCodes(), getNames(),
                                                                                           getVersions(),
                                                                                           getDescriptions(),
                                                                                           getAreasOfUse() ) );
    }

}
