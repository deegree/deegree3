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

package org.deegree.cs.components;

import static org.deegree.cs.utilities.ProjectionUtils.EPS11;

import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;

/**
 * The <code>Ellipsoid</code> class hold all parameters which are necessary to define an Ellipsoid. Every Ellipsoid has
 * a semi-major-axis and one of inverse_flattening, eccentricity or semi-minor-axis.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */

public class Ellipsoid extends CRSIdentifiable {

    /**
     * WGS 1984 ellipsoid. This ellipsoid is used in the GPS system and is the "default" Ellipsoid.
     */
    public static final Ellipsoid WGS84 = new Ellipsoid( 6378137.0, Unit.METRE, 298.257223563,
                                                         new CRSCodeType( "7030", "EPSG" ), "WGS84_Ellipsoid" );

    /**
     * the larger one of the two (semi)axis of an ellipsoid
     */
    private final double semiMajorAxis;

    /**
     * the smaller one of the two (semi)axis of an ellipsoid
     */
    private final double semiMinorAxis;

    /**
     * the units of the axis
     */
    private Unit units;

    /**
     * Flattening f is dependent on both the semi-major axis a and the semi-minor axis b. f = (a - b) / a
     */
    private final double flattening;

    /**
     * Flattening f is normally given as 1/... value therefore an inverse is often given.
     */
    private final double inverseFlattening;

    /**
     * The ellipsoid may also be defined by its semi-major axis a and eccentricity e, which is given by: e*e = 2f - f*f
     */
    private final double eccentricity;

    /**
     * The ellipsoid may also be defined by its semi-major axis a and eccentricity e, which is given by: e*e = 2f - f*f,
     * this is it's squared value.
     */
    private final double squaredEccentricity;

    /**
     * @param units
     * @param semiMajorAxis
     * @param semiMinorAxis
     * @param id
     *            containing the relevant information
     */
    public Ellipsoid( Unit units, double semiMajorAxis, double semiMinorAxis, CRSIdentifiable id ) {
        super( id );
        this.units = units;
        this.semiMajorAxis = semiMajorAxis;
        this.semiMinorAxis = semiMinorAxis;
        flattening = ( semiMajorAxis - semiMinorAxis ) / semiMajorAxis;
        if ( Math.abs( flattening ) > 0.00001 ) {
            inverseFlattening = 1. / flattening;
        } else {
            inverseFlattening = 0;
        }
        this.squaredEccentricity = calcSquaredEccentricity( flattening );
        this.eccentricity = Math.sqrt( squaredEccentricity );
    }

    /**
     * @param units
     * @param semiMajorAxis
     * @param semiMinorAxis
     * @param codes
     * @param names
     * @param versions
     * @param descriptions
     * @param areasOfUse
     */
    public Ellipsoid( Unit units, double semiMajorAxis, double semiMinorAxis, CRSCodeType[] codes, String[] names,
                      String[] versions, String[] descriptions, String[] areasOfUse ) {
        this( units, semiMajorAxis, semiMinorAxis, new CRSIdentifiable( codes, names, versions, descriptions,
                                                                        areasOfUse ) );
    }

    /**
     * @param units
     * @param semiMajorAxis
     * @param semiMinorAxis
     * @param code
     * @param name
     * @param version
     * @param description
     * @param areaOfUse
     */
    public Ellipsoid( Unit units, double semiMajorAxis, double semiMinorAxis, CRSCodeType code, String name,
                      String version, String description, String areaOfUse ) {
        this( units, semiMajorAxis, semiMinorAxis, new CRSCodeType[] { code }, new String[] { name },
              new String[] { version }, new String[] { description }, new String[] { areaOfUse } );
    }

    /**
     * @param units
     * @param semiMajorAxis
     * @param semiMinorAxis
     * @param codes
     */
    public Ellipsoid( Unit units, double semiMajorAxis, double semiMinorAxis, CRSCodeType[] codes ) {
        this( units, semiMajorAxis, semiMinorAxis, codes, null, null, null, null );
    }

    /**
     * @param units
     * @param semiMajorAxis
     * @param semiMinorAxis
     * @param code
     * @param name
     */
    public Ellipsoid( Unit units, double semiMajorAxis, double semiMinorAxis, CRSCodeType code, String name ) {
        this( units, semiMajorAxis, semiMinorAxis, new CRSCodeType[] { code }, new String[] { name }, null, null, null );
    }

    /**
     * @param semiMajorAxis
     * @param units
     * @param inverseFlattening
     * @param id
     *            containing all id relevant data.
     */
    public Ellipsoid( double semiMajorAxis, Unit units, double inverseFlattening, CRSIdentifiable id ) {
        super( id );
        this.units = units;
        this.semiMajorAxis = semiMajorAxis;
        this.inverseFlattening = inverseFlattening;
        if ( Math.abs( this.inverseFlattening ) > 0.00001 ) {
            flattening = 1. / this.inverseFlattening;
        } else {
            flattening = 0;
        }
        this.squaredEccentricity = calcSquaredEccentricity( this.flattening );
        eccentricity = Math.sqrt( squaredEccentricity );
        this.semiMinorAxis = this.semiMajorAxis - ( flattening * this.semiMajorAxis );
    }

    /**
     * @param semiMajorAxis
     * @param units
     * @param inverseFlattening
     * @param codes
     * @param names
     * @param versions
     * @param descriptions
     * @param areasOfUse
     */
    public Ellipsoid( double semiMajorAxis, Unit units, double inverseFlattening, CRSCodeType[] codes, String[] names,
                      String[] versions, String[] descriptions, String[] areasOfUse ) {
        this( semiMajorAxis, units, inverseFlattening, new CRSIdentifiable( codes, names, versions, descriptions,
                                                                            areasOfUse ) );
    }

    /**
     * @param semiMajorAxis
     * @param units
     * @param inverseFlattening
     * @param code
     * @param name
     * @param version
     * @param description
     * @param areaOfUse
     */
    public Ellipsoid( double semiMajorAxis, Unit units, double inverseFlattening, CRSCodeType code, String name,
                      String version, String description, String areaOfUse ) {
        this( semiMajorAxis, units, inverseFlattening, new CRSCodeType[] { code }, new String[] { name },
              new String[] { version }, new String[] { description }, new String[] { areaOfUse } );
    }

    /**
     * @param semiMajorAxis
     * @param units
     * @param inverseFlattening
     * @param codes
     */
    public Ellipsoid( double semiMajorAxis, Unit units, double inverseFlattening, CRSCodeType[] codes ) {
        this( semiMajorAxis, units, inverseFlattening, codes, null, null, null, null );
    }

    /**
     * @param semiMajorAxis
     * @param units
     * @param inverseFlattening
     * @param code
     * @param name
     */
    public Ellipsoid( double semiMajorAxis, Unit units, double inverseFlattening, CRSCodeType code, String name ) {
        this( semiMajorAxis, units, inverseFlattening, new CRSCodeType[] { code }, new String[] { name }, null, null,
              null );
    }

    /**
     * @param semiMajorAxis
     * @param eccentricity
     * @param units
     * @param id
     *            containing all id relevant data.
     */
    public Ellipsoid( double semiMajorAxis, double eccentricity, Unit units, CRSIdentifiable id ) {
        super( id );
        this.units = units;
        this.semiMajorAxis = semiMajorAxis;
        this.eccentricity = eccentricity;
        this.squaredEccentricity = this.eccentricity * this.eccentricity;
        this.flattening = calcFlattening( eccentricity );
        if ( Math.abs( flattening ) > 0.00001 ) {
            this.inverseFlattening = 1d / flattening;
        } else {
            this.inverseFlattening = 0;
        }
        this.semiMinorAxis = this.semiMajorAxis - ( flattening * this.semiMajorAxis );
    }

    /**
     * @param semiMajorAxis
     * @param eccentricity
     * @param units
     * @param codes
     * @param names
     * @param versions
     * @param descriptions
     * @param areasOfUse
     */
    public Ellipsoid( double semiMajorAxis, double eccentricity, Unit units, CRSCodeType[] codes, String[] names,
                      String[] versions, String[] descriptions, String[] areasOfUse ) {
        this( semiMajorAxis, eccentricity, units,
              new CRSIdentifiable( codes, names, versions, descriptions, areasOfUse ) );
    }

    /**
     * @param semiMajorAxis
     * @param eccentricity
     * @param units
     * @param code
     * @param name
     * @param version
     * @param description
     * @param areaOfUse
     */
    public Ellipsoid( double semiMajorAxis, double eccentricity, Unit units, CRSCodeType code, String name,
                      String version, String description, String areaOfUse ) {
        this( semiMajorAxis, eccentricity, units, new CRSCodeType[] { code }, new String[] { name },
              new String[] { version }, new String[] { description }, new String[] { areaOfUse } );
    }

    /**
     * @param semiMajorAxis
     * @param eccentricity
     * @param units
     * @param codes
     */
    public Ellipsoid( double semiMajorAxis, double eccentricity, Unit units, CRSCodeType[] codes ) {
        this( semiMajorAxis, eccentricity, units, codes, null, null, null, null );
    }

    /**
     * @param semiMajorAxis
     * @param eccentricity
     * @param units
     * @param code
     * @param name
     */
    public Ellipsoid( double semiMajorAxis, double eccentricity, Unit units, CRSCodeType code, String name ) {
        this( semiMajorAxis, eccentricity, units, new CRSCodeType[] { code }, new String[] { name }, null, null, null );
    }

    /**
     * @return the eccentricity.
     */
    public final double getEccentricity() {
        return eccentricity;
    }

    /**
     * @return the squared eccentricity of the ellipsoid-
     */
    public final double getSquaredEccentricity() {
        return squaredEccentricity;
    }

    /**
     * @return the flattening.
     */
    public final double getFlattening() {
        return flattening;
    }

    /**
     * @return the inverseFlattening.
     */
    public final double getInverseFlattening() {
        return inverseFlattening;
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

    /**
     * @return the units.
     */
    public final Unit getUnits() {
        return units;
    }

    /**
     * 
     * @param units
     */
    public final void setUnits( Unit units ) {
        this.units = units;
    }

    /**
     * @param other
     *            another ellipsoid
     * @return true if the other ellipsoid != null and its units, semi-major-axis and eccentricity are the same.
     */
    @Override
    public boolean equals( Object other ) {
        if ( other != null && other instanceof Ellipsoid ) {
            final Ellipsoid that = (Ellipsoid) other;
            return this.units.equals( that.units ) && ( Math.abs( this.semiMajorAxis - that.semiMajorAxis ) < EPS11 )
                   && ( Math.abs( this.eccentricity - that.eccentricity ) < EPS11 ) && super.equals( that );
        }
        return false;
    }

    /**
     * Calc the eccentricity from the flattening
     * 
     * @param flattening
     *            given.
     * @return the squared eccentricity which is given by e^2 = 2*f - f*f.
     */
    private double calcSquaredEccentricity( double flattening ) {
        return ( 2. * flattening ) - ( flattening * flattening );
    }

    /**
     * calcs the flattening of an ellispoid using the eccentricity.
     * 
     * @param eccentricity
     *            given
     * @return 1-sqrt( 1- e^2) or 0 if e^1 > 1
     */
    private double calcFlattening( double eccentricity ) {
        if ( eccentricity * eccentricity > 1 ) {
            return 0;
        }
        return 1 - Math.sqrt( ( 1 - eccentricity * eccentricity ) );
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder( super.toString() );
        sb.append( ", - Units: " ).append( units );
        sb.append( ", - semi-major-axis(a): " ).append( semiMajorAxis );
        sb.append( ", - semi-minor-axis(b): " ).append( semiMinorAxis );
        sb.append( ", - inverse-flattening: " ).append( inverseFlattening );
        sb.append( ", - eccentricity: " ).append( eccentricity );
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
        long tmp = Double.doubleToLongBits( semiMajorAxis );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );

        tmp = Double.doubleToLongBits( eccentricity );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );

        return (int) ( code >>> 32 ) ^ (int) code;
    }

    /**
     * @return true if this ellipsoid has no eccentricity.
     */
    public boolean isSphere() {
        return eccentricity == 0;
    }
}
