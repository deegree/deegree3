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

package org.deegree.crs.projections.azimuthal;

import static org.deegree.crs.projections.ProjectionUtils.EPS10;
import static org.deegree.crs.projections.ProjectionUtils.EPS11;
import static org.deegree.crs.projections.ProjectionUtils.HALFPI;
import static org.deegree.crs.projections.ProjectionUtils.QUARTERPI;
import static org.deegree.crs.projections.ProjectionUtils.calcPhiFromConformalLatitude;
import static org.deegree.crs.projections.ProjectionUtils.conformalLatitudeInnerPart;
import static org.deegree.crs.projections.ProjectionUtils.length;
import static org.deegree.crs.projections.ProjectionUtils.preCalcedThetaSeries;
import static org.deegree.crs.projections.ProjectionUtils.tanHalfCoLatitude;

import javax.vecmath.Point2d;

import org.deegree.crs.CRSCodeType;
import org.deegree.crs.CRSIdentifiable;
import org.deegree.crs.components.Unit;
import org.deegree.crs.coordinatesystems.GeographicCRS;
import org.deegree.crs.exceptions.ProjectionException;

/**
 * The <code>StereographicAzimuthal</code> class allows for Stereographic Projections of the Poles, equator as well as
 * oblique. This projection has following properties (Snyder p. 154):
 * <ul>
 * <li>Azimuthal</li>
 * <li>Conformal</li>
 * <li>The central meridian an a particular parallel (if shown) are straight lines.</li>
 * <li>All meridians on the polar aspects and the equator on the equatorial aspect are straight lines.</li>
 * <li>All other meidians and parallels are shown as arcs of circles</li>
 * <li>A perspective projections for the sphere</li>
 * <li>Directions from the center of the projection are true (except on ellipsoidal oblique and equatorial aspects)</li>
 * <li>Scale increases away from the center of the projection</li>
 * <li>Point opposite the center of the projection cannot be plotted</li>
 * <li>Used for polar maps and miscellaneous special maps</li>
 * </ul>
 * 
 * <p>
 * Like Orthographic, the stereographic projection is a true perspective in its isSpherical() form. It is the only known
 * true perspective projection of any kind that is also conformal. Its point of projection is on the the surface of the
 * sphere at a point jus opposite the oint of tangency of the plane or the center point of the projection. Thus, if the
 * north pole is the center of the map, the projection is from the south-pole.
 * </p>
 * <p>
 * It is known to be used by following epsg transformations:
 * <ul>
 * <li>EPSG:28992</li>
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

public class StereographicAzimuthal extends AzimuthalProjection {

    private static final long serialVersionUID = 5399110969291553925L;

    /**
     * This variable will hold different values, for the ellipsoidal projection:
     * <ul>
     * <li>for Oblique projection it is the first Part (2*a*k_0*m_1, hence it's name) of A (p.160 21-27)</li>
     * <li>for the north-south it may hold the rho of (21-33) or (21-34)
     * <li>for the equatorial it holds 2*scale.</li>
     * <li>For the
     * </ul>
     * For the spherical projection:
     * <ul>
     * <li>For oblique and equatorial: 2*scale, which is compliant with 2*k_0 from Snyder (p.157 21-4)</li>
     * <li>For north and south: cos(truelat) / tan( pi/4 - phi/2) ????</li>
     * </ul>
     */
    private double akm1;

    private double trueScaleLatitude;

    private double[] preCalcedPhiSeries;

    // use for the OBLIQUE projection instead of the projectionLatitude
    private double conformalLatitude = 0;

    // sine of the conformal latitude
    private double sinCL = 0;

    // cosine of the conformal latitude
    private double cosCL = 0;

    /**
     * @param trueScaleLatitude
     *            the latitude (in radians) of a circle around the projection point, which contains the true scale.
     * @param geographicCRS
     * @param falseNorthing
     * @param falseEasting
     * @param naturalOrigin
     * @param units
     * @param scale
     * @param id
     *            an identifiable instance containing information about this projection
     */
    public StereographicAzimuthal( double trueScaleLatitude, GeographicCRS geographicCRS, double falseNorthing,
                                   double falseEasting, Point2d naturalOrigin, Unit units, double scale,
                                   CRSIdentifiable id ) {
        super( geographicCRS, falseNorthing, falseEasting, naturalOrigin, units, scale, true,
               false/* not equal area */, id );

        preCalcedPhiSeries = preCalcedThetaSeries( getSquaredEccentricity() );

        this.trueScaleLatitude = trueScaleLatitude;
        if ( !isSpherical() ) {
            // double X;
            double tmp = 0;
            switch ( getMode() ) {
            case NORTH_POLE:
            case SOUTH_POLE:
                /**
                 * The t from 21-33 and 21-34 is still to be calculated.
                 */
                // If true scale or known scale factor k_0 is to occur at the pole, the following applies:
                if ( Double.isNaN( trueScaleLatitude )
                     || Math.abs( Math.abs( this.trueScaleLatitude ) - HALFPI ) < EPS10 ) {
                    // Snyder (p.161 21-33) akm1 = rho.
                    akm1 = 2.
                           * getScaleFactor()
                           / Math.sqrt( Math.pow( 1 + getEccentricity(), 1 + getEccentricity() )
                                        * Math.pow( 1 - getEccentricity(), 1 - getEccentricity() ) );

                } else {
                    // For true scale along the circle represented by trueScaleLatitude (phi_c in snyder)..
                    // akm1 will hold m_c/t_c
                    // Calculate the rho from Snyder (p.161 21-34) and place in akm1
                    tmp = Math.sin( this.trueScaleLatitude );

                    // First part of m of Snyder (p.160 14-15)
                    akm1 = Math.cos( this.trueScaleLatitude )
                           / tanHalfCoLatitude( this.trueScaleLatitude, tmp, getEccentricity() );
                    tmp *= getEccentricity();
                    // Second part of m of Snyder (p.160 14-15), t = (e*sin(phi_c))
                    akm1 /= Math.sqrt( 1. - tmp * tmp );

                }
                break;
            case EQUATOR:
                akm1 = 2. * getScaleFactor();
                break;
            case OBLIQUE:
                tmp = getSinphi0();// Math.sin( getProjectionLatitude() );
                // Calculate the ConformalLatitude for this ellipsoid Snyder (p.160 3-1) the X
                conformalLatitude = ( 2. * Math.atan( conformalLatitudeInnerPart( getProjectionLatitude(), tmp,
                                                                                  getEccentricity() ) ) )
                                    - HALFPI;

                sinCL = Math.sin( conformalLatitude );
                cosCL = Math.cos( conformalLatitude );

                tmp *= getEccentricity();
                // the first part (2a*k_0*m) of the largeA in Snyder (p.160 21-27) is stored in akm1 it still must be
                // devided by the cos X ... etc. to get largeA
                akm1 = 2. * getScaleFactor() * getCosphi0() / Math.sqrt( 1. - tmp * tmp );

                // Setting the sinPhi0 to the conformal Latitude X.
                // setProjectionLatitude( X );
                break;
            }
        } else {
            akm1 = 2. * getScaleFactor();
        }
    }

    /**
     * Sets the id to "Snyder-StereoGraphic"
     * 
     * @param trueScaleLatitude
     *            the latitude (in radians) of a circle around the projection point, which contains the true scale.
     * @param geographicCRS
     * @param falseNorthing
     * @param falseEasting
     * @param naturalOrigin
     * @param units
     * @param scale
     */
    public StereographicAzimuthal( double trueScaleLatitude, GeographicCRS geographicCRS, double falseNorthing,
                                   double falseEasting, Point2d naturalOrigin, Unit units, double scale ) {
        this( trueScaleLatitude, geographicCRS, falseNorthing, falseEasting, naturalOrigin, units, scale,
              new CRSIdentifiable( CRSCodeType.valueOf( "Snyder-StereoGraphic" ) ) );
    }

    /**
     * Create a {@link StereographicAzimuthal} which has a true scale latitude at MapUtils.HALFPI.
     * 
     * @param geographicCRS
     * @param falseNorthing
     * @param falseEasting
     * @param naturalOrigin
     * @param units
     * @param scale
     * @param id
     *            an identifiable instance containing information about this projection
     */
    public StereographicAzimuthal( GeographicCRS geographicCRS, double falseNorthing, double falseEasting,
                                   Point2d naturalOrigin, Unit units, double scale, CRSIdentifiable id ) {
        this( HALFPI, geographicCRS, falseNorthing, falseEasting, naturalOrigin, units, scale, id );
    }

    /**
     * Create a {@link StereographicAzimuthal} which has a true scale latitude at MapUtils.HALFPI. Sets the id to
     * "Snyder-StereoGraphic"
     * 
     * @param geographicCRS
     * @param falseNorthing
     * @param falseEasting
     * @param naturalOrigin
     * @param units
     * @param scale
     */
    public StereographicAzimuthal( GeographicCRS geographicCRS, double falseNorthing, double falseEasting,
                                   Point2d naturalOrigin, Unit units, double scale ) {
        this( HALFPI, geographicCRS, falseNorthing, falseEasting, naturalOrigin, units, scale );
    }

    /**
     * Create a {@link StereographicAzimuthal} which has a scale of 1 and a true scale latitude,
     * 
     * @param trueScaleLatitude
     *            the latitude (in radians) of a circle around the projection point, which contains the true scale.
     * @param geographicCRS
     * @param falseNorthing
     * @param falseEasting
     * @param naturalOrigin
     * @param units
     * @param id
     *            an identifiable instance containing information about this projection
     */
    public StereographicAzimuthal( double trueScaleLatitude, GeographicCRS geographicCRS, double falseNorthing,
                                   double falseEasting, Point2d naturalOrigin, Unit units, CRSIdentifiable id ) {
        this( trueScaleLatitude, geographicCRS, falseNorthing, falseEasting, naturalOrigin, units, 1, id );
    }

    /**
     * Create a {@link StereographicAzimuthal} which has a scale of 1 and a true scale latitude. Sets the id to
     * "Snyder-StereoGraphic".
     * 
     * @param trueScaleLatitude
     *            the latitude (in radians) of a circle around the projection point, which contains the true scale.
     * @param geographicCRS
     * @param falseNorthing
     * @param falseEasting
     * @param naturalOrigin
     * @param units
     */
    public StereographicAzimuthal( double trueScaleLatitude, GeographicCRS geographicCRS, double falseNorthing,
                                   double falseEasting, Point2d naturalOrigin, Unit units ) {
        this( trueScaleLatitude, geographicCRS, falseNorthing, falseEasting, naturalOrigin, units, 1 );
    }

    /**
     * Create a {@link StereographicAzimuthal} which is conformal, has a scale of 1 and a truescale latitude at pi*0.5.
     * 
     * @param geographicCRS
     * @param falseNorthing
     * @param falseEasting
     * @param naturalOrigin
     * @param units
     * @param id
     *            an identifiable instance containing information about this projection
     */
    public StereographicAzimuthal( GeographicCRS geographicCRS, double falseNorthing, double falseEasting,
                                   Point2d naturalOrigin, Unit units, CRSIdentifiable id ) {
        this( HALFPI, geographicCRS, falseNorthing, falseEasting, naturalOrigin, units, 1, id );
    }

    /**
     * Create a {@link StereographicAzimuthal} which is conformal, has a scale of 1 and a truescale latitude at pi*0.5.
     * Sets the id to "Snyder-StereoGraphic".
     * 
     * @param geographicCRS
     * @param falseNorthing
     * @param falseEasting
     * @param naturalOrigin
     * @param units
     */
    public StereographicAzimuthal( GeographicCRS geographicCRS, double falseNorthing, double falseEasting,
                                   Point2d naturalOrigin, Unit units ) {
        this( HALFPI, geographicCRS, falseNorthing, falseEasting, naturalOrigin, units, 1 );
    }

    @Override
    public Point2d doInverseProjection( double x, double y ) {
        Point2d result = new Point2d( 0, 0 );
        x -= getFalseEasting();
        y -= getFalseNorthing();
        double rho = length( x, y );
        if ( isSpherical() ) {
            // akm1 = 2*scale.
            double c = 2. * Math.atan( rho / akm1 );
            double sinc = Math.sin( c );
            double cosc = Math.cos( c );
            switch ( getMode() ) {
            case OBLIQUE:
                // First find theta from Snyder (p.158 20-14).
                if ( Math.abs( rho ) <= EPS10 ) {
                    result.y = getProjectionLatitude();
                } else {
                    result.y = Math.asin( cosc * getSinphi0() + ( ( y * sinc * getCosphi0() ) / rho ) );
                }
                // And now lambda but instead of using the atan, use atan2. Is this correct????
                c = cosc - getSinphi0() * Math.sin( result.y );
                if ( Math.abs( c ) > EPS10 || x != 0. ) {
                    result.x = Math.atan2( x * sinc * getCosphi0(), c * rho );
                }
                break;
            case EQUATOR:
                if ( Math.abs( rho ) > EPS10 ) {
                    result.y = Math.asin( y * sinc / rho );
                }
                if ( cosc != 0. || x != 0. ) {
                    result.x = Math.atan2( x * sinc, cosc * rho );
                }
                break;
            case NORTH_POLE:
                y = -y;
            case SOUTH_POLE:
                if ( Math.abs( rho ) <= EPS10 ) {
                    result.y = getProjectionLatitude();
                } else {
                    result.y = Math.asin( getMode() == SOUTH_POLE ? -cosc : cosc );
                }
                result.x = Math.atan2( x, y );
                break;
            }
        } else {
            double cosCe = 0;
            double sinCe = 0;
            double X = 0;
            // for oblique and equatorial projections, akm1 = first part of Snyder (p.160 21-27). which is 2*scale*m_1
            // double ce = 2. * Math.atan2( rho * getCosphi0(), akm1 );
            // if ( getMode() == OBLIQUE || getMode() == EQUATOR ) {
            // cosCe = Math.cos( ce );
            // sinCe = Math.sin( ce );
            // }
            double ce = 0;
            switch ( getMode() ) {
            case OBLIQUE:
                ce = 2. * Math.atan2( rho * cosCL, akm1 );
                cosCe = Math.cos( ce );
                sinCe = Math.sin( ce );
                X = Math.asin( cosCe * sinCL + ( ( y * sinCe * cosCL ) / rho ) );
                result.x = Math.atan2( x * sinCe, rho * cosCL * cosCe - y * sinCL * sinCe );
                break;
            case EQUATOR:
                ce = 2. * Math.atan2( rho * getCosphi0(), akm1 );
                cosCe = Math.cos( ce );
                sinCe = Math.sin( ce );
                X = Math.asin( cosCe * getSinphi0() + ( ( y * sinCe * getCosphi0() ) / rho ) );
                result.x = Math.atan2( x * sinCe, rho * getCosphi0() * cosCe - y * getSinphi0() * sinCe );
                break;
            case NORTH_POLE:
                y = -y;
            case SOUTH_POLE:
                /**
                 * akm1 can hold rho from 21-34 or 21-33 (if no truescale latitude was given) without the parameter t.
                 * Either way it must only be multiplied with t to fulfill the formula. Independent of the true scale
                 * latitude, the value of akm1 must therefore only be inverted to calculate Snyder (p.162 21-39 or
                 * 21-40).
                 */
                double t = rho * 1. / this.akm1;
                X = HALFPI - 2 * Math.atan( t );
                if ( getMode() == SOUTH_POLE ) {
                    X *= -1;
                }
                result.x = Math.atan2( x, y );
                break;
            }
            result.y = calcPhiFromConformalLatitude( X, preCalcedPhiSeries );
            result.x += getProjectionLongitude();
        }
        return result;
    }

    @Override
    public Point2d doProjection( double lambda, double phi )
                            throws ProjectionException {
        Point2d result = new Point2d( 0, 0 );
        lambda -= getProjectionLongitude();
        double cosLamda = Math.cos( lambda );
        double sinLamda = Math.sin( lambda );
        double sinPhi = Math.sin( phi );

        if ( isSpherical() ) {
            double cosphi = Math.cos( phi );

            switch ( getMode() ) {
            case OBLIQUE:
                // Calc k from Snyder (p.158 21-14) and store in xy.y
                result.y = 1. + getSinphi0() * sinPhi + getCosphi0() * cosphi * cosLamda;
                if ( result.y <= EPS11 ) {
                    throw new ProjectionException( "Division by zero" );
                }
                // akm1 was set to 2*scale.
                result.y = akm1 / result.y;

                // Calc x (p.157 21-2) and y (p.157 21-3)
                result.x = result.y * cosphi * sinLamda;
                result.y *= getCosphi0() * sinPhi - getSinphi0() * cosphi * cosLamda;
                break;
            case EQUATOR:
                // Calc k from Snyder (p.158 21-14) and store in xy.y
                result.y = 1. + cosphi * cosLamda;
                if ( result.y <= EPS11 ) {
                    throw new ProjectionException( "Division by zero" );
                }
                // akm1 was set to 2*scale.
                result.y = akm1 / result.y;

                // Calc x (p.157 21-2) and y (p.158 21-13)
                result.x = result.y * cosphi * sinLamda;
                result.y *= sinPhi;
                break;
            case NORTH_POLE:
                cosLamda = -cosLamda;
                phi = -phi;
            case SOUTH_POLE:
                if ( Math.abs( phi - HALFPI ) < EPS10 ) {
                    throw new ProjectionException( "The requested phi: " + phi + " lies on the singularity ("
                                                   + ( ( getMode() == SOUTH_POLE ) ? "South-Pole" : "North-Pole" )
                                                   + ") of this projection's mappable area." );
                }
                // akm1 was set to 2*scale.
                result.y = akm1 * Math.tan( QUARTERPI + .5 * phi );
                result.x = sinLamda * ( result.y         );
                result.y *= cosLamda;
                break;
            }
        } else {
            double sinX = 0;
            double cosX = 0;
            double largeA;

            if ( getMode() == OBLIQUE || getMode() == EQUATOR ) {
                // Calculate the conformal latitue Snyder (p.160 3-1).
                double X = 2. * Math.atan( conformalLatitudeInnerPart( phi, sinPhi, getEccentricity() ) ) - HALFPI;
                sinX = Math.sin( X );
                cosX = Math.cos( X );
            }
            switch ( getMode() ) {
            case OBLIQUE:
                // akm1 was set to the first part of Snyder (p.160 21-27)
                // sinPhi0 and cosPhi0 where set to the conformal latitude of the natural origin.
                largeA = akm1 / ( cosCL * ( 1. + sinCL * sinX + cosCL * cosX * cosLamda ) );
                result.y = largeA * ( cosCL * sinX - sinCL * cosX * cosLamda );
                result.x = largeA * cosX;
                break;
            case EQUATOR:
                // akm1 was set to 2. * scale;
                // Calculate the A of Snyder (p.161 21-29).
                largeA = 2. * akm1 / ( 1. + cosX * cosLamda );
                result.y = largeA * sinX;
                result.x = largeA * cosX;
                break;
            case SOUTH_POLE:
                phi = -phi;
                cosLamda = -cosLamda;
                sinPhi = -sinPhi;
            case NORTH_POLE:
                // akm1 holds the rho's from 21-33 or 21-34, but without the t (==halfColatitude of the conformal
                // latitude).
                result.x = akm1 * tanHalfCoLatitude( phi, sinPhi, getEccentricity() );
                result.y = -result.x * cosLamda;
                break;
            }
            // Sinus(Lambda) was still to be multiplied on the x.
            result.x = result.x * sinLamda;
        }
        result.x += getFalseEasting();
        result.y += getFalseNorthing();
        return result;
    }

    @Override
    public String getImplementationName() {
        return "stereographicAzimuthal";
    }

    /**
     * @return the trueScaleLatitude.
     */
    public final double getTrueScaleLatitude() {
        return trueScaleLatitude;
    }
}
