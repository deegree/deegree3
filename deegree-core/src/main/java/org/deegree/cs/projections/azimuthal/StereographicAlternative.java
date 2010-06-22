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

import static org.deegree.cs.utilities.ProjectionUtils.EPS11;
import static org.deegree.cs.utilities.ProjectionUtils.HALFPI;
import static org.deegree.cs.utilities.ProjectionUtils.QUARTERPI;

import javax.vecmath.Point2d;

import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.EPSGCode;
import org.deegree.cs.components.Unit;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.exceptions.ProjectionException;
import org.deegree.cs.projections.Projection;

/**
 * <code>StereographicAlternative</code> projection may be imagined to be a projection of the earth's surface onto a
 * plane in contact with the earth at a single tangent point from the opposite end of the diameter through that tangent
 * point.
 * <p>
 * An alternative approach is given by Snyder {@link StereographicAzimuthal}, where, instead of defining a single
 * conformal sphere at the origin point, the conformal latitude at each point on the ellipsoid is computed. The
 * conformal longitude is then always equivalent to the geodetic longitude. This approach is a valid alternative to the
 * above, but gives slightly different results away from the origin point. It is therefore considered by EPSG to be a
 * different projection method. Hence this implementation.
 * </p>
 * <p>
 * This projection is best known in its polar form and is frequently used for mapping polar areas where it complements
 * the Universal Transverse Mercator used for lower latitudes. Its spherical form has also been widely used by the US
 * Geological Survey for planetary mapping and the mapping at small scale of continental hydrocarbon provinces. In its
 * transverse or oblique ellipsoidal forms it is useful for mapping limited areas centered on the point where the plane
 * of the projection is regarded as tangential to the ellipsoid., e.g. the Netherlands. The tangent point is the origin
 * of the projected coordinate system and the meridian through it is regarded as the central meridian. In order to
 * reduce the scale error at the extremities of the projection area it is usual to introduce a scale factor of less than
 * unity at the origin such that a unit scale factor applies on a near circle centered at the origin and some distance
 * from it.
 * </p>
 * 
 * <p>
 * The coordinate transformation from geographical to projected coordinates is executed via the distance and azimuth of
 * the point from the center point or origin. For a sphere the formulas are relatively simple. For the ellipsoid the
 * same formulas are used but with auxiliary latitudes, known as conformal latitudes, substituted for the geodetic
 * latitudes of the spherical formulas for the origin and the point .
 * </p>
 * <quote>from <a
 * href="http://www.posc.org/Epicentre.2_2/DataModel/ExamplesofUsage/eu_cs34j.html">http://www.posc.org/</a></quote>
 * 
 * <p>
 * Determinations of oblique projections on an ellipsoid can be difficult to solve and result in long, complex
 * computations. Because conformal transformations can be made multiple time without loss of the conformal property a
 * method of determining oblique projections involves conformal transformation of the elliptical coordinates to
 * coordinates on a conformal sphere. The transformed coordinates can now be translated/rotated on the sphere and then
 * converted to planar coordinates with a conformal spherical projection. is Ce/2 1 − e sin φ 2 arctan K tanC (π/4 +
 * φ/2) − π/2 (3.6) χ = 1 + e sin φ λc = Cλ (3.7) √ 1 − e2 Rc = (3.8) 1 − e2 sin2 φ0
 * </p>
 * From the <a href="http://members.verizon.net/~gerald.evenden/proj4/manual.pdf">libproj4-manual</a> by Gerald I.
 * Evenden
 * 
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class StereographicAlternative extends AzimuthalProjection {

    private double sinc0 = 0;

    private double cosc0 = 0;

    /**
     * Two times the radius of the conformal sphere, which is denoted by Gerald I. Evenden as R_c
     */
    private final double R2;

    /**
     * THe latitude of on the conformal sphere which is denoted by Gerald I. Evenden as Chi_0
     */
    private final double latitudeOnCS;

    /**
     * The exponent of the calculation of conformal latitude Chi in Gerald I. Evenden (3.6)
     */
    private final double clExponent;

    /**
     * The value K in Gerald I. Evenden (3.11)
     */
    private final double K;

    /**
     * The central geographic latitude of the projection on the conformal sphere, denoted by Gerald I. Evenden (3.9)
     * with C
     */
    private final double centralGeographicLatitude;

    // private double chi = 0;

    // private double radiusOfCS = 0;

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
    public StereographicAlternative( GeographicCRS geographicCRS, double falseNorthing, double falseEasting,
                                     Point2d naturalOrigin, Unit units, double scale, CRSIdentifiable id ) {
        super( geographicCRS, falseNorthing, falseEasting, naturalOrigin, units, scale, true/* conformal */, false, id );

        // if (!(P->en = pj_gauss_ini(P->e, P->phi0, &(P->phic0), &R))) E_ERROR_0;
        double es = getSquaredEccentricity();
        double sinPhi0 = getSinphi0();
        double cosPhiSquare = getCosphi0() * getCosphi0();// Math.cos( getProjectionLatitude() );

        // R_c
        double radiusOfCS = Math.sqrt( 1. - es ) / ( 1. - es * sinPhi0 * sinPhi0 );
        centralGeographicLatitude = Math.sqrt( 1. + es * cosPhiSquare * cosPhiSquare / ( 1. - es ) );
        // xsi_0
        latitudeOnCS = Math.asin( sinPhi0 / centralGeographicLatitude );
        clExponent = 0.5 * centralGeographicLatitude * getEccentricity();
        K = Math.tan( .5 * latitudeOnCS + QUARTERPI )
            / ( Math.pow( Math.tan( .5 * getProjectionLatitude() + QUARTERPI ), centralGeographicLatitude ) * srat(
                                                                                                                    getEccentricity()
                                                                                                                                            * sinPhi0,
                                                                                                                    clExponent ) );
        sinc0 = Math.sin( latitudeOnCS );
        cosc0 = Math.cos( latitudeOnCS );
        R2 = 2 * radiusOfCS;
    }

    /**
     * Sets the id of this projection to epsg::9809 (Oblique Stereographic)
     * 
     * @param geographicCRS
     * @param falseNorthing
     * @param falseEasting
     * @param naturalOrigin
     * @param units
     * @param scale
     */
    public StereographicAlternative( GeographicCRS geographicCRS, double falseNorthing, double falseEasting,
                                     Point2d naturalOrigin, Unit units, double scale ) {
        this( geographicCRS, falseNorthing, falseEasting, naturalOrigin, units, scale,
              new CRSIdentifiable( new EPSGCode( 9809 ) ) );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.cs.projections.Projection#doInverseProjection(double, double)
     */
    @Override
    public Point2d doInverseProjection( double x, double y )
                            throws ProjectionException {
        Point2d result = new Point2d();
        x -= getFalseEasting();
        y -= getFalseNorthing();

        x /= getScaleFactor();
        y /= getScaleFactor();
        double rho = Math.hypot( x, y );

        if ( rho > EPS11 ) {
            double c = 2 * Math.atan2( rho, R2 );
            double sinc = Math.sin( c );
            double cosc = Math.cos( c );
            result.y = Math.asin( cosc * sinc0 + y * sinc * cosc0 / rho );
            result.x = Math.atan2( x * sinc, rho * cosc0 * cosc - y * sinc0 * sinc );
        } else {
            result.y = latitudeOnCS;
            result.x = 0;
        }
        result = pj_inv_gauss( result );
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
        Point2d result = new Point2d();
        lambda -= getProjectionLongitude();
        Point2d lp = pj_gauss( lambda, phi );
        double sinc = Math.sin( lp.y );
        double cosc = Math.cos( lp.y );
        double cosl = Math.cos( lp.x );

        double k = getScaleFactor() * ( R2 / ( 1. + sinc0 * sinc + cosc0 * cosc * cosl ) );
        result.x = k * cosc * Math.sin( lp.x );
        result.y = k * ( cosc0 * sinc - sinc0 * cosc * cosl );

        result.x += getFalseEasting();
        result.y += getFalseNorthing();
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.cs.projections.Projection#getDeegreeSpecificName()
     */
    @Override
    public String getImplementationName() {
        return "stereographicAlternative";
    }

    private static double srat( double esinp, double exp ) {
        return Math.pow( ( 1. - esinp ) / ( 1. + esinp ), exp );
    }

    /**
     * To determine the inverse solution, geographic coordinates from Gaussian sphere coordinates, execute with the
     * initial value of φi−1 = χ and φi−1 iteratively replaced by φ until |φ − φi−1 | is less than an acceptable error
     * value. (taken from the proj-lib manual.
     */
    private Point2d pj_inv_gauss( Point2d slp )
                            throws ProjectionException {
        Point2d elp = new Point2d();

        elp.x = slp.x / centralGeographicLatitude;
        double num = Math.pow( Math.tan( .5 * slp.y + QUARTERPI ) / K, 1. / centralGeographicLatitude );
        int MAX_ITER = 20;
        int i = MAX_ITER;
        for ( ; i > 0; --i ) {
            elp.y = 2. * Math.atan( num * srat( getEccentricity() * Math.sin( slp.y ), -.5 * getEccentricity() ) )
                    - HALFPI;
            if ( Math.abs( ( elp.y - slp.y ) ) < EPS11 ) {
                break;
            }
            slp.y = elp.y;
        }
        /* convergence failed */
        if ( i == 0 ) {
            throw new ProjectionException( "No convertgence while calculation the inverse gaus approximation" );
        }
        return elp;
    }

    /**
     * The conformal transformation of ellipsoid coordinates (φ, λ) to conformal sphere coordinates (χ, λ_c ), where λ
     * is relative to the longitude of projection origin, R_c is radius of the conformal sphere. χ_0 is the latitude on
     * the conformal sphere at the central geographic latitude of the projection.
     */
    private Point2d pj_gauss( double lambda, double phi ) {
        Point2d slp = new Point2d();
        slp.y = 2.
                * Math.atan( K * Math.pow( Math.tan( .5 * phi + QUARTERPI ), centralGeographicLatitude )
                             * srat( getEccentricity() * Math.sin( phi ), clExponent ) ) - HALFPI;
        slp.x = centralGeographicLatitude * ( lambda );
        return slp;
    }

    @Override
    public Projection clone( GeographicCRS newCRS ) {
        return new StereographicAlternative( newCRS, getFalseNorthing(), getFalseEasting(), getNaturalOrigin(),
                                             getUnits(), getScale(), new CRSIdentifiable( getCodes(), getNames(),
                                                                                          getVersions(),
                                                                                          getDescriptions(),
                                                                                          getAreasOfUse() ) );
    }

}
