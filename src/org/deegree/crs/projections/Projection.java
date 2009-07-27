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

package org.deegree.crs.projections;

import static org.deegree.crs.projections.ProjectionUtils.EPS11;
import static org.deegree.crs.projections.ProjectionUtils.normalizeLatitude;
import static org.deegree.crs.projections.ProjectionUtils.normalizeLongitude;

import javax.vecmath.Point2d;

import org.deegree.crs.CRSIdentifiable;
import org.deegree.crs.components.Datum;
import org.deegree.crs.components.Ellipsoid;
import org.deegree.crs.components.PrimeMeridian;
import org.deegree.crs.components.Unit;
import org.deegree.crs.coordinatesystems.GeographicCRS;
import org.deegree.crs.exceptions.ProjectionException;

/**
 * Map <code>conversion</code> is the process of changing the map grid coordinates (usually, but not always, Easting &
 * Northing) of a Projected Coordinate Reference System to its corresponding geographical coordinates (Latitude &
 * Longitude) or vice versa.
 * <p>
 * A projection is conformal if an infinitesimal small perfect circle on the earth's surface results in an infinitesimal
 * small projected perfect circle (an ellipsoid with no eccentricity). In other words, the relative local angles about
 * every point on the map are shown correctly.
 * </p>
 * <p>
 * An equal area projection can be best explained with a coin (Snyder), a coin (of any size) covers exactly the same
 * area of the actual earth as the same coin on any other part of the map. This can only be done by distorting shape,
 * scale and and angles of the original earth's layout.
 * </p>
 * 
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */

public abstract class Projection extends CRSIdentifiable {

    private final boolean conformal;

    private boolean equalArea;

    private double scale;

    /**
     * the scale*semimajor-axis, often revered to as R*k_0 in Snyder.
     */
    private double scaleFactor;

    private final double falseNorthing;

    private double falseEasting;

    private final Point2d naturalOrigin;

    // Values gotten from the natural origin
    private double projectionLatitude;

    private double projectionLongitude;

    // the sin of the projection latitude
    private double sinphi0;

    // the cos of the projection latitude
    private double cosphi0;

    private final Unit units;

    private final GeographicCRS geographicCRS;

    private boolean isSpherical;

    /**
     * Creates a Projection. <b>Caution</b>, the given natural origin should be given in radians rather then degrees.
     * 
     * @param geographicCRS
     *            which this projection uses.
     * @param falseNorthing
     *            in given units
     * @param falseEasting
     *            in given units
     * @param naturalOrigin
     *            in radians longitude, latitude.
     * @param units
     *            of the map projection
     * @param scale
     *            at the prime meridian (e.g. 0.9996 for UTM)
     * @param conformal
     *            if the projection is conformal
     * @param equalArea
     *            if the projection result in an equal area map
     * @param id
     *            an identifiable instance containing information about this projection.
     */
    public Projection( GeographicCRS geographicCRS, double falseNorthing, double falseEasting, Point2d naturalOrigin,
                       Unit units, double scale, boolean conformal, boolean equalArea, CRSIdentifiable id ) {
        super( id );
        this.scale = scale;
        this.conformal = conformal;
        this.equalArea = equalArea;
        this.geographicCRS = geographicCRS;
        this.falseNorthing = falseNorthing;
        this.falseEasting = falseEasting;
        this.units = units;

        checkForNullObject( geographicCRS, "Projection", "geographicCRS" );
        checkForNullObject( geographicCRS.getGeodeticDatum(), "Projection", "geographicCRS.datum" );
        checkForNullObject( geographicCRS.getGeodeticDatum().getEllipsoid(), "Projection",
                            "geographicCRS.datum.ellipsoid" );
        checkForNullObject( naturalOrigin, "Projection", "naturalOrigin" );
        checkForNullObject( units, "Projection", "units" );

        this.scaleFactor = scale * getSemiMajorAxis();

        this.naturalOrigin = new Point2d( normalizeLongitude( naturalOrigin.x ), normalizeLatitude( naturalOrigin.y ) );

        // uses different library
        // this.projectionLongitude = this.naturalOrigin.getX();
        // this.projectionLatitude = this.naturalOrigin.getY();
        this.projectionLongitude = this.naturalOrigin.x;
        this.projectionLatitude = this.naturalOrigin.y;

        sinphi0 = Math.sin( projectionLatitude );
        cosphi0 = Math.cos( projectionLatitude );

        isSpherical = geographicCRS.getGeodeticDatum().getEllipsoid().getEccentricity() < 0.0000001;
    }

    /**
     * The actual transform method doing a projection from geographic coordinates to map coordinates.
     * 
     * @param lambda
     *            the longitude
     * @param phi
     *            the latitude
     * @return the projected Point or Point(Double.NAN, Double.NAN) if an error occurred.
     * @throws ProjectionException
     *             if the given lamba and phi coordinates could not be projected to x and y.
     */
    public abstract Point2d doProjection( double lambda, double phi )
                            throws ProjectionException;

    /**
     * Do an inverse projection from projected (map) coordinates to geographic coordinates.
     * 
     * @param x
     *            coordinate on the map
     * @param y
     *            coordinate on the map
     * @return the projected Point with x = lambda and y = phi;
     * @throws ProjectionException
     *             if the given x and y coordinates could not be inverted to lambda and phi.
     */
    public abstract Point2d doInverseProjection( double x, double y )
                            throws ProjectionException;

    /**
     * @return A deegree specific name which will be used for the export of a projection.
     */
    public abstract String getImplementationName();

    /**
     * @return true if the projection projects conformal.
     */
    public final boolean isConformal() {
        return conformal;
    }

    /**
     * @return true if the projection is projects equal Area.
     */
    public final boolean isEqualArea() {
        return equalArea;
    }

    /**
     * @return the scale.
     */
    public final double getScale() {
        return scale;
    }

    /**
     * Sets the old scale to the given scale, also adjusts the scaleFactor.
     * 
     * @param scale
     *            the new scale
     */
    public void setScale( double scale ) {
        this.scale = scale;
        this.scaleFactor = scale * getSemiMajorAxis();
    }

    /**
     * @return the scale*semimajor-axis, often revered to as R*k_0 in Snyder.
     */
    public final double getScaleFactor() {
        return scaleFactor;
    }

    /**
     * @return the datum.
     */
    public final Datum getDatum() {
        return geographicCRS.getGeodeticDatum();
    }

    /**
     * @return the falseEasting.
     */
    public final double getFalseEasting() {
        return falseEasting;
    }

    /**
     * sets the false easting to given value. (Used in for example transverse mercator, while setting the utm zone).
     * 
     * @param newFalseEasting
     *            the new false easting parameter.
     */
    public void setFalseEasting( double newFalseEasting ) {
        this.falseEasting = newFalseEasting;
    }

    /**
     * @return the falseNorthing.
     */
    public final double getFalseNorthing() {
        return falseNorthing;
    }

    /**
     * @return the naturalOrigin.
     */
    public final Point2d getNaturalOrigin() {
        return naturalOrigin;
    }

    /**
     * @return the units.
     */
    public final Unit getUnits() {
        return units;
    }

    /**
     * @return the primeMeridian of the datum.
     */
    public final PrimeMeridian getPrimeMeridian() {
        return geographicCRS.getGeodeticDatum().getPrimeMeridian();
    }

    /**
     * @return the ellipsoid of the datum.
     */
    public final Ellipsoid getEllipsoid() {
        return geographicCRS.getGeodeticDatum().getEllipsoid();
    }

    /**
     * @return the eccentricity of the ellipsoid of the datum.
     */
    public final double getEccentricity() {
        return geographicCRS.getGeodeticDatum().getEllipsoid().getEccentricity();
    }

    /**
     * @return the eccentricity of the ellipsoid of the datum.
     */
    public final double getSquaredEccentricity() {
        return geographicCRS.getGeodeticDatum().getEllipsoid().getSquaredEccentricity();
    }

    /**
     * @return the semiMajorAxis (a) of the ellipsoid of the datum.
     */
    public final double getSemiMajorAxis() {
        return geographicCRS.getGeodeticDatum().getEllipsoid().getSemiMajorAxis();
    }

    /**
     * @return the semiMinorAxis (a) of the ellipsoid of the datum.
     */
    public final double getSemiMinorAxis() {
        return geographicCRS.getGeodeticDatum().getEllipsoid().getSemiMinorAxis();
    }

    /**
     * @return true if the ellipsoid of the datum is a sphere and not an ellipse.
     */
    public final boolean isSpherical() {
        return isSpherical;
    }

    /**
     * @return the projectionLatitude also known as central-latitude or latitude-of-origin, in Snyder referenced as
     *         phi_1 for azimuthal, phi_0 for other projections.
     */
    public final double getProjectionLatitude() {
        return projectionLatitude;
    }

    /**
     * @return the projectionLongitude also known as projection-meridian or central-meridian, in Snyder referenced as
     *         lambda_0
     */
    public final double getProjectionLongitude() {
        return projectionLongitude;
    }

    /**
     * @return the sinphi0, the sine of the projection latitude
     */
    public final double getSinphi0() {
        return sinphi0;
    }

    /**
     * @return the cosphi0, the cosine of the projection latitude
     */
    public final double getCosphi0() {
        return cosphi0;
    }

    /**
     * @return the geographicCRS.
     */
    public final GeographicCRS getGeographicCRS() {
        return geographicCRS;
    }

    @Override
    public boolean equals( Object other ) {
        if ( other != null && other instanceof Projection ) {
            final Projection that = (Projection) other;
            return this.units.equals( that.units )
                   && Math.abs( ( this.projectionLatitude - that.projectionLatitude ) ) < EPS11
                   && Math.abs( ( this.projectionLongitude - that.projectionLongitude ) ) < EPS11
                   && Math.abs( ( this.falseNorthing - that.falseNorthing ) ) < EPS11
                   && Math.abs( ( this.falseEasting - that.falseEasting ) ) < EPS11
                   && Math.abs( ( this.scale - that.scale ) ) < EPS11 && ( this.conformal == that.conformal )
                   && ( this.equalArea == that.equalArea ) && this.getGeographicCRS().equals( that.getGeographicCRS() );
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder( super.toString() );
        sb.append( "\n - underlying-geographic-CRS: " ).append( geographicCRS );
        sb.append( "\n - units: " ).append( units );
        sb.append( "\n - projection-longitude: " ).append( projectionLongitude );
        sb.append( "\n - projection-latitude: " ).append( projectionLatitude );
        sb.append( "\n - is-spherical: " ).append( isSpherical() );
        sb.append( "\n - is-conformal: " ).append( isConformal() );
        sb.append( "\n - natural-origin: " ).append( getNaturalOrigin() );
        sb.append( "\n - false-easting: " ).append( getFalseEasting() );
        sb.append( "\n - false-northing: " ).append( getFalseNorthing() );
        sb.append( "\n - scale: " ).append( getScale() );
        return sb.toString();

    }

    /**
     * Implementation as proposed by Joshua Block in Effective Java (Addison-Wesley 2001), which supplies an even
     * distribution and is relatively fast. It is created from field <b>f</b> as follows:
     * <ul>
     * <li>boolean -- code = (f ? 0 : 1)</li>
     * <li>byte, char, short, int -- code = (int)f</li>
     * <li>long -- code = (int)(f ^ (f &gt;&gt;&gt;32))</li>
     * <li>float -- code = Float.floatToIntBits(f);</li>
     * <li>double -- long l = Double.doubleToLongBits(f); code = (int)(l ^ (l &gt;&gt;&gt; 32))</li>
     * <li>all Objects, (where equals(&nbsp;) calls equals(&nbsp;) for this field) -- code = f.hashCode(&nbsp;)</li>
     * <li>Array -- Apply above rules to each element</li>
     * </ul>
     * <p>
     * Combining the hash code(s) computed above: result = 37 * result + code;
     * </p>
     * 
     * @return (int) ( result >>> 32 ) ^ (int) result;
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        // the 2nd millionth prime, :-)
        long code = 32452843;
        if ( units != null ) {
            code = code * 37 + units.hashCode();
        }

        long tmp = Double.doubleToLongBits( projectionLatitude );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );

        tmp = Double.doubleToLongBits( projectionLongitude );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );

        tmp = Double.doubleToLongBits( falseNorthing );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );

        tmp = Double.doubleToLongBits( falseEasting );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );

        tmp = Double.doubleToLongBits( scale );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );

        code = code * 37 + ( conformal ? 0 : 1 );
        code = code * 37 + ( equalArea ? 0 : 1 );
        if ( geographicCRS != null ) {
            code = code * 37 + geographicCRS.hashCode();
        }

        return (int) ( code >>> 32 ) ^ (int) code;
    }

}
