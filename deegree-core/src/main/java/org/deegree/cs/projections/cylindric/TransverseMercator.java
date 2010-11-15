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

package org.deegree.cs.projections.cylindric;

import static org.deegree.cs.utilities.ProjectionUtils.EPS10;
import static org.deegree.cs.utilities.ProjectionUtils.HALFPI;
import static org.deegree.cs.utilities.ProjectionUtils.acosScaled;
import static org.deegree.cs.utilities.ProjectionUtils.asinScaled;
import static org.deegree.cs.utilities.ProjectionUtils.calcPhiFromMeridianDistance;
import static org.deegree.cs.utilities.ProjectionUtils.getDistanceAlongMeridian;
import static org.deegree.cs.utilities.ProjectionUtils.getRectifiyingLatitudeValues;
import static org.deegree.cs.utilities.ProjectionUtils.normalizeLatitude;
import static org.deegree.cs.utilities.ProjectionUtils.normalizeLongitude;

import javax.vecmath.Point2d;

import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.EPSGCode;
import org.deegree.cs.components.Unit;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.exceptions.ProjectionException;
import org.deegree.cs.projections.Projection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>TransverseMercator</code> projection has following properties:
 * <ul>
 * <li>Cylindrical (transverse)</li>
 * <li>Conformal</li>
 * <li>The central meridian, each meridian 90° from central meridian and the equator are straight lines</li>
 * <li>All other meridians and parallels are complex curves</li>
 * <li>Scale is true along central meridian or along two straight lines equidistant from and parallel to central
 * merdian. (These lines are only approximately straight for the ellipsoid)</li>
 * <li>Scale becomes infinite on sphere 90° from central meridian</li>
 * <li>Used extensively for quadrangle maps at scales from 1:24.000 to 1:250.000</li>
 * <li>Often used to show regions with greater north-south extent</li>
 * <li>presented by lambert in 1772</li>
 * </ul>
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
@LoggingNotes(debug = "Get information about incoming ordinates of the (inverse) projection.")
public class TransverseMercator extends CylindricalProjection {

    private static Logger LOG = LoggerFactory.getLogger( TransverseMercator.class );

    /**
     * Constants used for the forward and inverse transform for the elliptical case of the Transverse Mercator.
     */
    private static final double FC1 = 1., // 1/1
                            FC2 = 0.5, // 1/2
                            FC3 = 0.16666666666666666666666, // 1/6
                            FC4 = 0.08333333333333333333333, // 1/12
                            FC5 = 0.05, // 1/20
                            FC6 = 0.03333333333333333333333, // 1/30
                            FC7 = 0.02380952380952380952380, // 1/42
                            FC8 = 0.01785714285714285714285; // 1/56

    // 1 for northern hemisphere -1 for southern.
    private int hemisphere;

    // esp will can hold two values, for the sphere it will hold the scale, for the ellipsoid Snyder (p.61 8-12).
    private double esp;

    private double ml0;

    private double[] en;

    /**
     * @param northernHemisphere
     *            true if on the northern hemisphere false otherwise.
     * @param geographicCRS
     * @param falseNorthing
     * @param falseEasting
     * @param naturalOrigin
     * @param units
     * @param scale
     * @param id
     *            an identifiable instance containing information about this projection
     */
    public TransverseMercator( boolean northernHemisphere, GeographicCRS geographicCRS, double falseNorthing,
                               double falseEasting, Point2d naturalOrigin, Unit units, double scale, CRSIdentifiable id ) {
        super( geographicCRS, falseNorthing, falseEasting, naturalOrigin, units, scale, true,// always conformal
               false/* not equalArea */, id );
        this.hemisphere = ( northernHemisphere ) ? 1 : -1;
        if ( isSpherical() ) {
            esp = getScale();
            ml0 = .5 * esp;
        } else {
            en = getRectifiyingLatitudeValues( getSquaredEccentricity() );
            ml0 = getDistanceAlongMeridian( getProjectionLatitude(), getSinphi0(), getCosphi0(), en );
            esp = getSquaredEccentricity() / ( 1. - getSquaredEccentricity() );
            // esp = ( ( getSemiMajorAxis() * getSemiMajorAxis() ) / ( getSemiMinorAxis() * getSemiMinorAxis() ) ) -
            // 1.0;
        }

    }

    /**
     * Sets the id to EPSG:9807
     * 
     * @param northernHemisphere
     *            true if on the northern hemisphere false otherwise.
     * @param geographicCRS
     * @param falseNorthing
     * @param falseEasting
     * @param naturalOrigin
     * @param units
     * @param scale
     */
    public TransverseMercator( boolean northernHemisphere, GeographicCRS geographicCRS, double falseNorthing,
                               double falseEasting, Point2d naturalOrigin, Unit units, double scale ) {
        this( northernHemisphere, geographicCRS, falseNorthing, falseEasting, naturalOrigin, units, scale,
              new CRSIdentifiable( new EPSGCode( 9807 ) ) );
    }

    /**
     * Sets the false-easting to 50000, false-northing to 0 or 10000000 (depending on the hemisphere), the
     * projection-longitude is calculated from the zone and the projection-latitude is set to 0. The scale will be
     * 0.9996.
     * 
     * @param zone
     *            to add
     * @param northernHemisphere
     *            true if the projection is on the northern hemisphere
     * @param geographicCRS
     * @param units
     * @param id
     *            an identifiable instance containing information about this projection
     */
    public TransverseMercator( int zone, boolean northernHemisphere, GeographicCRS geographicCRS, Unit units,
                               CRSIdentifiable id ) {
        super( geographicCRS, ( northernHemisphere ? 0 : 10000000 ), 500000, new Point2d( ( --zone + .5 ) * Math.PI
                                                                                          / 30. - Math.PI, 0 ), units,
               0.9996, true /* always conformal */, false /* not equalArea */, id );
        this.hemisphere = ( northernHemisphere ) ? 1 : -1;
        if ( isSpherical() ) {
            esp = getScale();
            ml0 = .5 * esp;
        } else {
            // recalculate the rectifying latitudes and the distance along the meridian.
            en = getRectifiyingLatitudeValues( getSquaredEccentricity() );
            ml0 = getDistanceAlongMeridian( getProjectionLatitude(), getSinphi0(), getCosphi0(), en );
            esp = getSquaredEccentricity() / ( 1. - getSquaredEccentricity() );
        }

    }

    /**
     * Sets the false-easting to 50000, false-northing to 0 or 10000000 (depending on the hemisphere), the
     * projection-longitude is calculated from the zone and the projection-latitude is set to 0. The scale will be
     * 0.9996.
     * 
     * @param zone
     *            to add
     * @param northernHemisphere
     *            true if the projection is on the northern hemisphere
     * @param geographicCRS
     * @param units
     */
    public TransverseMercator( int zone, boolean northernHemisphere, GeographicCRS geographicCRS, Unit units ) {
        this( zone, northernHemisphere, geographicCRS, units, new CRSIdentifiable( new EPSGCode( 9807 ) ) );
    }

    /**
     * A northern hemisphere conformal transverse mercator projection with a scale of one. Using the given datum.
     * 
     * @param geographicCRS
     * @param falseNorthing
     * @param falseEasting
     * @param naturalOrigin
     * @param units
     */
    public TransverseMercator( GeographicCRS geographicCRS, double falseNorthing, double falseEasting,
                               Point2d naturalOrigin, Unit units ) {
        this( true, geographicCRS, falseNorthing, falseEasting, naturalOrigin, units, 1. );
    }

    /**
     * A northern hemisphere conformal transverse mercator projection with a scale of one. Using the given datum.
     * 
     * @param geographicCRS
     * @param falseNorthing
     * @param falseEasting
     * @param naturalOrigin
     * @param units
     * @param id
     *            an identifiable instance containing information about this projection
     */
    public TransverseMercator( GeographicCRS geographicCRS, double falseNorthing, double falseEasting,
                               Point2d naturalOrigin, Unit units, CRSIdentifiable id ) {
        this( true, geographicCRS, falseNorthing, falseEasting, naturalOrigin, units, 1., id );
    }

    @Override
    public Point2d doInverseProjection( double x, double y )
                            throws ProjectionException {
        Point2d result = new Point2d( 0, 0 );
        LOG.debug( "InverseProjection, incoming points x: " + x + " y: " + y );
        x = ( x - getFalseEasting() ) / getScaleFactor();
        y = ( y - getFalseNorthing() ) / getScaleFactor();
        y *= hemisphere;

        if ( isSpherical() ) {
            // h holds e^x, the sinh = 0.5*(e^x - e^-x), cosh = 0.5(e^x + e^-x)
            double h = Math.exp( x / getScaleFactor() );
            // sinh holds the sinh from Snyder (p.60 8-7)
            double sinh = .5 * ( h - 1. / h );

            // Snyder (p.60 8-8)
            // reuse variable
            double cosD = Math.cos( getProjectionLatitude() + ( y/* / getScale() */) );
            /**
             * To calc phi from Snyder (p.60 8-6), use following trick! sin^2(D) + cos^2(D) = 1 => sin(D) = sqrt( 1-
             * cos^2(D) ) and cosh^2(x) - sin^2(x) = 1 => cosh(x) = sqrt( 1+sin^2(x) )
             */
            result.y = asinScaled( Math.sqrt( ( 1. - cosD * cosD ) / ( 1. + sinh * sinh ) ) );
            // if ( y < 0 ) {// southern hemisphere
            // out.y = -out.y;
            // }
            result.x = Math.atan2( sinh, cosD );
        } else {
            // out.y will hold the phi_1 from Snyder (p.63 8-18).
            result.y = calcPhiFromMeridianDistance( ml0 + ( y/* / getScale() */), getSquaredEccentricity(), en );
            // result.y = calcPhiFromMeridianDistance( ml0 + ( y / getScale() ),
            // getSquaredEccentricity(),
            // en );
            if ( Math.abs( result.y ) >= HALFPI ) {
                result.y = y < 0. ? -HALFPI : HALFPI;
                result.x = 0;
            } else {

                double sinphi = Math.sin( result.y );
                double cosphi = Math.cos( result.y );
                // largeT Will hold the tan^2(phi) Snyder (p.64 8-22).
                double largeT = ( Math.abs( cosphi ) > EPS10 ) ? sinphi / cosphi : 0;

                // will hold the C_1 from Synder (p.64 8-21)
                double largeC = esp * cosphi * cosphi;

                // Holds a modified N from Synder (p.64 8-23), multiplied with the largeT, it is the first term fo the
                // calculation of phi e.g. N*T/R
                double con = 1. - ( getSquaredEccentricity() * sinphi * sinphi );
                // largeD holds the D from Snyder (p.64 8-25). (x/(1/N) = x*N)
                // double largeD = x * Math.sqrt( con ) / getScaleFactor();
                double largeD = x * Math.sqrt( con )/* / getScale() */;
                con *= largeT;
                largeT *= largeT;
                double ds = largeD * largeD;

                /**
                 * As for the forward projection, I'm not sure if this is correct, this should be checked!
                 */
                result.y -= ( con * ds / ( 1. - getSquaredEccentricity() ) )
                            * FC2
                            * ( 1. - ds
                                     * FC4
                                     * ( 5. + largeT * ( 3. - 9. * largeC ) + largeC * ( 1. - 4 * largeC ) - ds
                                                                                                             * FC6
                                                                                                             * ( 61.
                                                                                                                 + largeT
                                                                                                                 * ( 90. - 252. * largeC + 45. * largeT )
                                                                                                                 + 46.
                                                                                                                 * largeC - ds
                                                                                                                            * FC8
                                                                                                                            * ( 1385. + largeT
                                                                                                                                        * ( 3633. + largeT
                                                                                                                                                    * ( 4095. + 1574. * largeT ) ) ) ) ) );
                result.x = largeD
                           * ( FC1 - ds
                                     * FC3
                                     * ( 1. + 2. * largeT + largeC - ds
                                                                     * FC5
                                                                     * ( 5. + largeT
                                                                         * ( 28. + 24. * largeT + 8. * largeC ) + 6.
                                                                         * largeC - ds
                                                                                    * FC7
                                                                                    * ( 61. + largeT
                                                                                              * ( 662. + largeT
                                                                                                         * ( 1320. + 720. * largeT ) ) ) ) ) )
                           / cosphi;
            }
        }
        // result.y += getProjectionLatitude();
        result.x += getProjectionLongitude();

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.cs.projections.Projection#doProjection(double, double)
     */
    @Override
    public Point2d doProjection( double lambda, double phi )
                            throws ProjectionException {
        // LOG.debug( "Projection, incoming points lambda: " + lambda + " phi: " + phi );
        LOG.debug( "Projection, incoming points lambda: " + Math.toDegrees( lambda ) + " phi: " + Math.toDegrees( phi ) );
        Point2d result = new Point2d( 0, 0 );
        lambda -= getProjectionLongitude();
        // phi -= getProjectionLatitude();

        phi *= hemisphere;
        double cosphi = Math.cos( phi );
        if ( isSpherical() ) {
            double b = cosphi * Math.sin( lambda );

            // Snyder (p.58 8-1)
            result.x = ml0 * getScaleFactor() * Math.log( ( 1. + b ) / ( 1. - b ) );

            // reformed and inserted the k from (p.58 8-4), so no tangens has to be calculated.
            double ty = cosphi * Math.cos( lambda ) / Math.sqrt( 1. - b * b );
            ty = acosScaled( ty );
            if ( phi < 0.0 ) {
                ty = -ty;
            }
            // esp just holds the scale
            result.y = esp * ( ty - getProjectionLatitude() );
        } else {
            double sinphi = Math.sin( phi );
            double largeT = ( Math.abs( cosphi ) > EPS10 ) ? sinphi / cosphi : 0.0;
            // largeT holds Snyder (p.61 8-13).
            largeT *= largeT;
            double largeA = cosphi * lambda;
            double squaredLargeA = largeA * largeA;
            // largeA now holds A/N Snyder (p.61 4-20 and 8-15)
            largeA /= Math.sqrt( 1. - ( getSquaredEccentricity() * sinphi * sinphi ) );

            // largeA *= getSemiMajorAxis();

            // largeC will hold Snyder (p.61 8-14), esp holds Snyder (p.61 8-12).
            double largeC = esp * cosphi * cosphi;
            double largeM = getDistanceAlongMeridian( phi, sinphi, cosphi, en );

            result.x = largeA
                       * ( FC1 + FC3
                                 * squaredLargeA
                                 * ( 1. - largeT + largeC + FC5
                                                            * squaredLargeA
                                                            * ( 5. + largeT * ( largeT - 18. ) + largeC
                                                                * ( 14. - 58. * largeT ) + FC7
                                                                                           * squaredLargeA
                                                                                           * ( 61. + largeT
                                                                                                     * ( largeT
                                                                                                         * ( 179. - largeT ) - 479. ) ) ) ) );

            result.y = ( largeM - ml0 )
                       + sinphi
                       * largeA
                       * lambda
                       * FC2
                       * ( 1. + FC4
                                * squaredLargeA
                                * ( 5. - largeT + largeC * ( 9. + 4. * largeC ) + FC6
                                                                                  * squaredLargeA
                                                                                  * ( 61. + largeT * ( largeT - 58. )
                                                                                      + largeC * ( 270. - 330 * largeT ) + FC8
                                                                                                                           * squaredLargeA
                                                                                                                           * ( 1385. + largeT
                                                                                                                                       * ( largeT
                                                                                                                                           * ( 543. - largeT ) - 3111. ) ) ) ) );

        }

        result.x = ( result.x * getScaleFactor() ) + getFalseEasting();
        result.y = ( result.y * getScaleFactor() ) + getFalseNorthing();

        return result;
    }

    /**
     * @param latitude
     *            to get the nearest paralles to.
     * @return the nearest parallel in radians of given latitude
     */
    public int getRowFromNearestParallel( double latitude ) {
        int degrees = (int) Math.round( Math.toDegrees( normalizeLatitude( latitude ) ) );
        if ( degrees < -80 || degrees > 84 ) {
            return 0;
        }
        if ( degrees > 80 ) {
            return 24; // last parallel
        }
        return ( ( degrees + 80 ) / 8 ) + 3;
    }

    /**
     * the utm zone from a given meridian
     * 
     * @param longitude
     *            in radians
     * @return the utm zone.
     */
    public int getZoneFromNearestMeridian( double longitude ) {
        int zone = (int) Math.floor( ( normalizeLongitude( longitude ) + Math.PI ) * 30.0 / Math.PI ) + 1;
        if ( zone < 1 ) {
            zone = 1;
        } else if ( zone > 60 ) {
            zone = 60;
        }
        return zone;
    }

    @Override
    public String getImplementationName() {
        return "transverseMercator";
    }

    /**
     * @return the true if defined on the northern hemisphere.
     */
    public final boolean getHemisphere() {
        return ( hemisphere == 1 );
    }

    @Override
    public Projection clone( GeographicCRS newCRS ) {
        return new TransverseMercator( getHemisphere(), newCRS, getFalseNorthing(), getFalseEasting(),
                                       getNaturalOrigin(), getUnits(), getScale(),
                                       new CRSIdentifiable( getCodes(), getNames(), getVersions(), getDescriptions(),
                                                            getAreasOfUse() ) );
    }
}
