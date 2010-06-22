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

package org.deegree.cs.transformations.coordinate;

import static org.deegree.cs.utilities.ProjectionUtils.EPS11;
import static org.deegree.cs.utilities.ProjectionUtils.length;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;

import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.components.Ellipsoid;
import org.deegree.cs.components.Unit;
import org.deegree.cs.coordinatesystems.CompoundCRS;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.coordinatesystems.GeocentricCRS;
import org.deegree.cs.coordinatesystems.CoordinateSystem.CRSType;
import org.deegree.cs.transformations.Transformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>GeocentricTransform</code> class is used to create a transformation between a geocentric CRS (having
 * lat-lon coordinates) and it's geodetic CRS (having x-y-z) coordinates and vice versa.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
@LoggingNotes(debug = "Get information about the incoming ordinates of a geocentric transformation.")
public class GeocentricTransform extends Transformation {

    private static Logger LOG = LoggerFactory.getLogger( GeocentricTransform.class );

    /**
     * Cosine of 67.5 degrees.
     */
    private static final double COS_67P5 = 0.38268343236508977;

    /**
     * Toms region 1 constant, which will allow for a difference between ellipsoid of 2000 Kilometers. <quote>Under this
     * policy the maximum error is less than 42 centimeters for altitudes less then 10.000.000 Kilometers</quote>
     */
    private static final double AD_C = 1.0026;

    /**
     * Semi-major axis of ellipsoid in meters.
     */
    private final double semiMajorAxis;

    /**
     * Semi-minor axis of ellipsoid in meters.
     */
    private final double semiMinorAxis;

    /**
     * Square of semi-major axis {@link #semiMajorAxis}.
     */
    private final double squaredSemiMajorAxis;

    /**
     * Square of semi-minor axis {@link #semiMinorAxis}.
     */
    private final double squaredSemiMinorAxis;

    /**
     * Eccentricity squared.
     */
    private final double squaredEccentricity;

    /**
     * 2nd eccentricity squared.
     */
    private final double ep2;

    /**
     * true if the given points will use heights (e.g. have a z/height-value).
     */
    private boolean hasHeight;

    private final double defaultHeightValue;

    /**
     * @param source
     *            the geographic crs.
     * @param target
     *            the geocentric crs.
     * @param id
     *            an identifiable instance containing information about this transformation
     */
    public GeocentricTransform( CoordinateSystem source, GeocentricCRS target, CRSIdentifiable id ) {
        super( source, target, id );
        this.hasHeight = ( source.getType() == CRSType.COMPOUND );
        defaultHeightValue = ( hasHeight ) ? ( (CompoundCRS) source ).getDefaultHeight() : 0;
        Ellipsoid ellipsoid = source.getGeodeticDatum().getEllipsoid();
        semiMajorAxis = Unit.METRE.convert( ellipsoid.getSemiMajorAxis(), ellipsoid.getUnits() );
        semiMinorAxis = Unit.METRE.convert( ellipsoid.getSemiMinorAxis(), ellipsoid.getUnits() );
        squaredSemiMajorAxis = semiMajorAxis * semiMajorAxis;
        squaredSemiMinorAxis = semiMinorAxis * semiMinorAxis;
        squaredEccentricity = ellipsoid.getSquaredEccentricity();
        // e2 = ( a2 - b2 ) / a2;
        ep2 = ( squaredSemiMajorAxis - squaredSemiMinorAxis ) / squaredSemiMinorAxis;
    }

    /**
     * @param source
     *            the geographic crs.
     * @param target
     *            the geocentric crs.
     */
    public GeocentricTransform( CoordinateSystem source, GeocentricCRS target ) {
        this( source, target, new CRSIdentifiable( CRSCodeType.valueOf( createFromTo( source.getCode().toString(),
                                                                                      target.getCode().toString() ) ) ) );
    }

    @Override
    public List<Point3d> doTransform( List<Point3d> srcPts ) {
        List<Point3d> result = new ArrayList<Point3d>( srcPts );
        if ( LOG.isDebugEnabled() ) {
            StringBuilder sb = new StringBuilder( isInverseTransform() ? "An inverse " : "A " );
            sb.append( getImplementationName() );
            sb.append( " with incoming points: " );
            sb.append( srcPts );
            LOG.debug( sb.toString() );
        }
        if ( isInverseTransform() ) {
            toGeographic( result );
        } else {
            toGeoCentric( result );
        }
        return result;
    }

    /**
     * Converts geocentric coordinates (x, y, z) to geodetic coordinates (longitude, latitude, height), according to the
     * current ellipsoid parameters. The method used here is derived from "An Improved Algorithm for Geocentric to
     * Geodetic Coordinate Conversion", by Ralph Toms, Feb 1996 UCRL-JC-123138.
     * 
     * @param srcPts
     *            the points which must be transformed.
     */
    protected void toGeographic( List<Point3d> srcPts ) {
        for ( Point3d p : srcPts ) {
            // Note: Variable names follow the notation used in Toms, Feb 1996

            final double T0 = p.z * AD_C; // initial estimate of vertical component
            final double W = length( p.x, p.y );// distance from Z axis
            final double S0 = length( T0, W );// initial estimate of horizontal component

            final double sin_B0 = T0 / S0; // sin(B0), B0 is estimate of Bowring variable
            final double cos_B0 = W / S0; // cos(B0)
            final double sin3_B0 = sin_B0 * sin_B0 * sin_B0; // cube of sin(B0)
            final double T1 = p.z + semiMinorAxis * ep2 * sin3_B0; // corrected estimate of vertical component

            // numerator of cos(phi1)
            final double sum = W - semiMajorAxis * squaredEccentricity * ( cos_B0 * cos_B0 * cos_B0 );

            // corrected estimate of horizontal component
            final double S1 = length( T1, sum );// Math.sqrt( T1 * T1 + sum * sum );

            // sin(phi), phi is estimated latitude
            final double sinPhi = T1 / S1;
            final double cosPhi = sum / S1; // cos(phi)

            // Lambda in tom.
            p.x = Math.atan2( p.y, p.x );// longitude;
            p.y = Math.atan( sinPhi / cosPhi );// latitude;
            if ( hasHeight ) {
                double height = 1;
                // rn = radius of curvature of the prime vertical, of the ellipsoid at location
                final double rn = semiMajorAxis / Math.sqrt( 1 - squaredEccentricity * ( sinPhi * sinPhi ) );

                if ( cosPhi >= +COS_67P5 ) {
                    height = W / +cosPhi - rn;
                } else if ( cosPhi <= -COS_67P5 ) {
                    height = W / -cosPhi - rn;
                } else {
                    height = p.z / sinPhi + rn * ( squaredEccentricity - 1.0 );
                }
                p.z = height;
            } else {
                p.z = Double.NaN;
            }
        }
    }

    /**
     * Converts geographic (longitude, latitude, height) to cartesian (x,y,z) coordinates.
     * 
     * @param srcPts
     *            to convert.
     */
    protected void toGeoCentric( List<Point3d> srcPts ) {
        for ( Point3d p : srcPts ) {
            final double lambda = p.x; // Longitude
            final double phi = p.y; // Latitude
            // first check the p.z value if it is defined, if not, use the defaultheight value, which will be
            // initialized with 0 or the configured compound crs value.
            if ( Double.isNaN( p.z ) || Math.abs( p.z ) < EPS11 ) {
                p.z = defaultHeightValue;
            }
            final double h = hasHeight ? p.z : 0; // Height above the ellipsoid (metres).

            final double cosPhi = Math.cos( phi );
            final double sinPhi = Math.sin( phi );
            final double rn = semiMajorAxis / Math.sqrt( 1 - squaredEccentricity * ( sinPhi * sinPhi ) );

            p.x = ( rn + h ) * cosPhi * Math.cos( lambda );
            p.y = ( rn + h ) * cosPhi * Math.sin( lambda );
            p.z = ( rn * ( 1 - squaredEccentricity ) + h ) * sinPhi;
        }
    }

    @Override
    public boolean isIdentity() {
        return false;
    }

    /**
     * @return the semiMajorAxis.
     */
    public final double getSemiMajorAxis() {
        return semiMajorAxis;
    }

    /**
     * @return the semiMinorAxis.
     */
    public final double getSemiMinorAxis() {
        return semiMinorAxis;
    }

    @Override
    public String getImplementationName() {
        return "Geocentric-Transform";
    }

}
